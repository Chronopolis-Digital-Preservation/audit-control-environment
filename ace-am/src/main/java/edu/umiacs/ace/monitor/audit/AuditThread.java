/*
 * Copyright (c) 2007-2010, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ACE Components were written in the ADAPT Project at the University of
 * Maryland Institute for Advanced Computer Study.
 */
// $Id$
package edu.umiacs.ace.monitor.audit;

import edu.umiacs.ace.driver.AuditIterable;
import edu.umiacs.ace.driver.DriverStateBean;
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.driver.filter.SimpleFilter;
import edu.umiacs.ace.ims.api.IMSException;
import edu.umiacs.ace.ims.api.IMSService;
import edu.umiacs.ace.ims.api.TokenRequestBatch;
import edu.umiacs.ace.ims.api.TokenValidator;
import edu.umiacs.ace.ims.ws.TokenRequest;
import edu.umiacs.ace.monitor.access.CollectionCountContext;
import edu.umiacs.ace.monitor.compare.CollectionCompare2;
import edu.umiacs.ace.monitor.compare.CompareResults;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.ConfigConstants;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.MonitoredItemManager;
import edu.umiacs.ace.monitor.log.LogEnum;
import edu.umiacs.ace.monitor.log.LogEvent;
import edu.umiacs.ace.monitor.log.LogEventManager;
import edu.umiacs.ace.monitor.peers.PeerCollection;
import edu.umiacs.ace.monitor.reporting.ReportSummary;
import edu.umiacs.ace.monitor.reporting.SchedulerContextListener;
import edu.umiacs.ace.monitor.reporting.SummaryGenerator;
import edu.umiacs.ace.monitor.settings.SettingsUtil;
import edu.umiacs.ace.remote.JsonGateway;
import edu.umiacs.ace.token.AceToken;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.util.TokenUtil;
import edu.umiacs.util.Strings;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.sql.DataSource;
import java.io.InputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 * @author toaster
 */
public final class AuditThread extends Thread implements CancelCallback {

    private static final Logger LOG = Logger.getLogger(AuditThread.class);
    private boolean fallback = false;
    private Map<AceToken, MonitoredItem> itemMap =
            new ConcurrentHashMap<AceToken, MonitoredItem>();
    private String imsHost;
    private int imsPort;
    private Collection coll;
    private boolean hasRun = false;
    private boolean cancel = false;
    private StorageDriver driver;
    private long session;
    private boolean auditOnly;
    private boolean verbose = false;
    // exposed for jsp display
    private long totalErrors = 0;
    private long newFilesFound = 0;
    private long filesSeen = 0;
    private String lastFileSeen;
    private MonitoredItem[] baseItemPathList = null;
    private FileAuditCallback callback = null;
    private TokenRequestBatch batch = null;
    private TokenValidator validator = null;
    private Throwable abortException;
    private String tokenClassName;
    private LogEventManager logManager;
    private AuditIterable<FileBean> iterableItems;

    public AuditThread(Collection c, 
                       StorageDriver driver, 
                       boolean auditOnly,
                       MonitoredItem... startItem) {
        this(c, driver, auditOnly, true, startItem);
    }

    public AuditThread(Collection c, 
                       StorageDriver driver, 
                       boolean auditOnly,
                       boolean verbose, 
                       MonitoredItem... startItem) {
        this.auditOnly = auditOnly;
        this.verbose = verbose;
        this.coll = c;
        this.driver = driver;
        if (startItem != null
                && startItem.length > 0
                && startItem[0] != null) {
            baseItemPathList = Arrays.copyOf(startItem, startItem.length);
        }
        this.setName("audit: " + c.getName());
    }

    public DriverStateBean[] getDriverStatus() {
        if (iterableItems != null) {
            return iterableItems.getState();
        } else {
            return new DriverStateBean[0];
        }
    }

    public void setTokenClassName(String tokenClassName) {
        this.tokenClassName = tokenClassName;
    }

    void setImsHost(String imsHost) {
        this.imsHost = imsHost;
    }

    void setImsPort(int imsPort) {
        this.imsPort = imsPort;
    }

    public long getTotalErrors() {
        if (callback != null) {
            return totalErrors + callback.getCallbackErrors();
        }
        return totalErrors;
    }

    public long getNewFilesFound() {
        return newFilesFound;
    }

    public long getFilesSeen() {
        return filesSeen;
    }

    public String getLastFileSeen() {
        return lastFileSeen;
    }

    public long getTokensAdded() {
        if (callback != null) {
            return callback.getTokensAdded();
        }
        return 0;
    }

    public Collection getCollection() {
        return coll;
    }

    @Override
    public void cancel() {
        LOG.info("Received cancel for audit " + coll.getName());
        cancel = true;
        if (iterableItems != null) {
            iterableItems.cancel();
        }

        /*
        if (batch != null) {
            batch.close();
        }

        if (validator != null) {
            validator.close();
        }
        */

        if (AuditThreadFactory.isRunning(coll) || AuditThreadFactory.isQueued(coll)) {
            AuditThreadFactory.finished(coll);
        }

        this.interrupt();
    }

    @Override
    public void run() {
        NDC.push("[Audit] ");
        try {
            if (hasRun) {
                LOG.fatal("Cannot run thread, already executed");
                throw new IllegalStateException("Thread has already run");
            }
            hasRun = true;

            try {
                session = System.currentTimeMillis();
                logManager = new LogEventManager(session, coll);
                logAuditStart();

                callback = new FileAuditCallback(coll, session, this);
                boolean auditTokens = SettingsUtil.getBoolean(coll,
                                          ConfigConstants.ATTR_AUDIT_TOKENS);


                // Audit only does not attempt to connect to the IMS, so we
                // only need these checks if we are not in it
                if( !auditOnly ) {
                    // If we can open a connection to the IMS, check what mode
                    // we're in, else fallback so we go to audit only mode
                    if ( openIms() ) {
                        boolean openValidator = openTokenValidator(MessageDigest.getInstance("SHA-256"));
                        //short circuit
                        if ( auditTokens && !openValidator ) { 
                            return;
                        }
                    } else {
                        fallback = true;
                    }
                }

                performAudit();
                // Let outstanding tokens finish, TODO, de-hackify this.
                sleep(2000);
            } catch (Throwable e) {
                LOG.fatal("Uncaught exception in performAudit()", e);
                if (abortException != null) {
                    abortException = e;
                }
            }
        } finally {
            LOG.info("Audit ending. exception: "
                    + (abortException != null) + " null requester: " + (batch
                    == null) + " cancel: " + cancel);

            // make sure we don't leave these two open
            if (batch != null) {
                batch.close();
                batch = null;
            }
            if (validator != null) {
                validator.close();
                validator = null;
            }
            
            lastFileSeen = "Setting collection state";
            setCollectionState();

            if ( verbose ) {
                logAuditFinish();
                generateAuditReport();
            }

            AuditThreadFactory.finished(coll);
            NDC.pop();

        }
        LOG.info("Exiting audit thread");
    }

    private boolean openIms() {
        try {
            IMSService ims;
            ims = IMSService.connect(imsHost, 
                                     imsPort, 
                                     AuditThreadFactory.useSSL(),
                                     AuditThreadFactory.isBlocking(),
                                     AuditThreadFactory.getMaxBlockTime());

            batch = ims.createImmediateTokenRequestBatch(tokenClassName,
                                                         callback, 
                                                         1000, 
                                                         5000);
            return true;
        } catch (IMSException e) {
            EntityManager em;
            LOG.error("Cannot connect to IMS ", e);
            em = PersistUtil.getEntityManager();

            EntityTransaction trans = em.getTransaction();
            trans.begin();
            em.persist(logManager.createCollectionEvent(
                    LogEnum.UNKNOWN_IMS_COMMUNICATION_ERROR, e));

            em.persist(logManager.createCollectionEvent(
                    LogEnum.FILE_AUDIT_FALLBACK, imsHost));
            trans.commit();
            em.close();
            return false;
        }

    }

    private boolean openTokenValidator(MessageDigest digest) {
        try {
            IMSService ims;
            ims = IMSService.connect(imsHost,
                                     imsPort,
                                     AuditThreadFactory.useSSL(),
                                     AuditThreadFactory.isBlocking(),
                                     AuditThreadFactory.getMaxBlockTime());
            TokenAuditCallback tokenCallback = new TokenAuditCallback(itemMap,
                    this,
                    coll,
                    session);
            validator = ims.createTokenValidator(tokenCallback, 1000,
                    5000, digest);
            //                    "SHA-256-0", new BatchCallback(), 1000, 5000);
            return true;
        } catch (IMSException e) {
            EntityManager em;
            LOG.error("Cannot connect to IMS ", e);
            em = PersistUtil.getEntityManager();


            EntityTransaction trans = em.getTransaction();
            trans.begin();
            em.persist(logManager.createCollectionEvent(
                    LogEnum.UNKNOWN_IMS_COMMUNICATION_ERROR, e));
            trans.commit();
            em.close();
            return false;
        }
    }

    private void performAudit() {

        // 1. Setup audit
        //TODO Clean up filtering creation
        PathFilter filter = new SimpleFilter(coll);
        Date startDate = new Date();

        // Sleep to ensure that we update a monitored item at a time strictly
        // greater than our start date
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            abortException = e;
            return;
        }


        // 2. Get file list
        try {
            iterableItems = driver.getWorkList(coll.getDigestAlgorithm(),
                                               filter, 
                                               baseItemPathList);
        } catch (Exception e) {
            abortException = e;
            return;
        }

        // 3. Process files
        try {
            for (FileBean currentFile : iterableItems) {
                if (currentFile == null) {
                    continue;
                }

                if (cancel || abortException != null) {
                    // iterableItems.cancel();
                    return;
                }

                filesSeen++;
                lastFileSeen = currentFile.getPathList()[0];
                processFile(currentFile);
            }
        } catch (Exception e) {

            abortException = e;
            LOG.info("Exception caught processing items");
            return;
        } finally {
            // just in case, we really, really want to exit
            LOG.trace("Exiting file supply iteration, Calling cancel on "
                    + "file supply iterator (just in case)");
            iterableItems.cancel();
        }

        if (cancel || abortException != null) {
            return;
        }

        // let token batch finish before processing or items waiting tokens
        // will appear as errors.
        //        Thread.sleep(2000);
        if (batch != null) {
            batch.close();
            batch = null;
        }

        // 4. Clean up, set local inactive
        lastFileSeen = "looking for missed items";
        setInactiveBefore(startDate);
        // harvest remote collections
        lastFileSeen = "comparing to peer sites";
        compareToPeers();


    }

    private void generateAuditReport() {
        LOG.trace("Generating audit report on " + session + " coll "
                + coll.getName());
        CollectionCountContext.updateCollection(coll);
        ReportSummary rs = new SummaryGenerator(coll, session).generateReport();
        try {
            SchedulerContextListener.mailReport(rs, createMailList());
        } catch (MessagingException e) {
            EntityManager em = PersistUtil.getEntityManager();
            logManager.persistCollectionEvent(LogEnum.SMTP_ERROR, 
                                              e.getMessage(), em);
            em.close();
            LOG.error("Could not send report summary", e);
        }
    }

    /**
     * Handle three different types of exits
     * 1. we got an exception somewhere, abortException will be set
     * 2. user requested cancel
     * 3. we finished as expected
     *
     */
    private void logAuditFinish() {
        EntityManager em = PersistUtil.getEntityManager();
        if (abortException != null) {
            if (abortException instanceof InterruptedException
                    || abortException.getCause() instanceof InterruptedException) {
                LOG.trace("Audit ending with Interrupt");
                logManager.persistCollectionEvent(LogEnum.FILE_AUDIT_FINISH,
                        "Audit Interrupted", em);
            } else {
                LOG.error("Uncaught exception in audit thread", abortException);

                String message = Strings.exceptionAsString(abortException);
                logManager.persistCollectionEvent(
                        LogEnum.SYSTEM_ERROR, message, em);
                logManager.persistCollectionEvent(LogEnum.FILE_AUDIT_FINISH,
                        "Uncaught audit thread exception ", em);
            }

        } else if (cancel) {
            logManager.persistCollectionEvent(LogEnum.FILE_AUDIT_FINISH,
                    "Audit interrupted by user or token registration", em);
            LOG.trace("Audit ending on cancel request");
        } else {
            String finMsg = null;
            if (baseItemPathList != null) {
                finMsg = "Finish partial audit on list "
                        + baseItemPathList.length;
            }
            logManager.persistCollectionEvent(LogEnum.FILE_AUDIT_FINISH,
                    finMsg, em);

            LOG.trace("Audit ending successfully");
        }

        em.close();
    }

    private void logAuditStart() {
        LOG.info("Starting audit on: " + coll.getName());
        EntityManager em = PersistUtil.getEntityManager();
        String message = null;
        if (baseItemPathList != null) {
            message = "Partial audit starting on list of size "
                    + baseItemPathList.length;
        }
        logManager.persistCollectionEvent(LogEnum.FILE_AUDIT_START,
                message, em);
        em.close();
        em = null;
    }

    private LogEvent setItemError(MonitoredItem item, String errorMessage) {
        totalErrors++;
        LOG.debug("Got error from currentFile: " + item.getPath());
        item.setState('M');
        return logManager.createItemEvent(LogEnum.ERROR_READING,
                item.getPath(), errorMessage);

    }

    private void processFile(FileBean currentFile) {
        EntityManager em = PersistUtil.getEntityManager();
        MonitoredItemManager mim;

        LOG.trace(
                "Driver returned Item: " + currentFile.getPathList()[0]
                + " error: " + currentFile.isError() + " error msg: "
                + currentFile.getErrorMessage() + " hash: "
                + currentFile.getHash());

        mim = new MonitoredItemManager(em);

        String fileName = currentFile.getPathList()[0];
        // make sure no invalid XML characters are in the filename
        // java.lang.String supports invalid unicode chars
        fileName = Strings.cleanStringForXml(fileName, '_');

        try {
            String parentName = extractAndRegisterParent(mim, currentFile);
            MonitoredItem item = null;
            if ((item = mim.getItemByPath(fileName, coll)) != null) {
                LogEvent event = null;
                LOG.trace("Updating existing item " + fileName);
                item.setLastVisited(new Date());
                // if item is in error, log a message and update its state to M
                if (currentFile.isError()) {
                    event = setItemError(item, currentFile.getErrorMessage());
                } else {
                    if (item.getToken() != null) {
                        event = validateIntegrity(currentFile, item);
                    } else if (!auditOnly && !fallback) {
                        // we have no token, so set state to T and enqueue
                        event = requestNewToken(currentFile, item);
                    }
                }
                // merge item back in
                EntityTransaction trans = em.getTransaction();
                trans.begin();
                if (event != null) {
                    em.persist(event);
                }
                em.merge(item);
                trans.commit();
            } else if (!auditOnly && !fallback) {
                // OK, no registered item, do the registration
                LogEvent[] event = addNewFile(currentFile, fileName,
                        parentName, mim);

                // transaction inside loop to ensure order of commits
                // default, jpa ignores order of persist calls
                for (LogEvent le : event) {
                    if (le != null) {
                        EntityTransaction trans = em.getTransaction();
                        trans.begin();
                        em.persist(le);
                        trans.commit();
                    }
                }
            }
        } finally {
            em.close();
            em = null;
        }

    }

    private LogEvent requestNewToken(FileBean currentFile, MonitoredItem item) {
        LogEvent event;
        if (item.getState() != 'T') {
            event = logManager.createItemEvent(LogEnum.MISSING_TOKEN,
                    item.getPath());
            item.setStateChange(new Date());
            item.setState('T');
            item.setSize(currentFile.getFileSize());
            LOG.trace(
                    "Toggling state to Missing Token for " + item.getPath());
        } else {
            event = null;
        }

        TokenRequest request = new TokenRequest();
        request.setName(item.getId().toString());
        request.setHashValue(currentFile.getHash());
        if (!Strings.isEmpty(item.getPath()) && 
            !Strings.isEmpty(currentFile.getHash()) && batch != null) {
            try {
                batch.add(request);
            } catch (InterruptedException e) {
                 abortException = e;
            }
        }
        return event;
    }

    private LogEvent validateIntegrity(FileBean currentFile, MonitoredItem item) {
        LOG.trace(
                "Generated checksum: " + currentFile.getHash()
                + " expected checksum: " + item.getFileDigest());
        LogEvent event;

        // If we have a registered file, set the digested value
        if (null == item.getFileDigest()) {
            LOG.trace("Setting digest for registered file " + item.getPath());
            item.setFileDigest(currentFile.getHash());
            item.setLastSeen(new Date());
        }

        if (currentFile.getHash().equals(item.getFileDigest())) {
            LOG.trace("Digests match for " + item.getPath());
            // File is active and intact
            // log the transition if it already isn't A
            // only toggle to A if state is not a remote error or registered
            // we handle those later
            if (item.getState() != 'A' && item.getState() != 'P'
                    && item.getState() != 'D' && item.getState() != 'R') {
                event = logManager.createItemEvent(LogEnum.FILE_ONLINE,
                        item.getPath());
                item.setState('A');
                item.setStateChange(new Date());
                LOG.trace("Toggling state to Active for " + item.getPath());
            } else {
                event = null;
            }
            item.setLastSeen(new Date());
            item.setSize(currentFile.getFileSize());
            AceToken token = TokenUtil.convertToAceToken(item.getToken());
            // add to token check queue
            if (validator != null) {
                itemMap.put(token, item);
                try {
                    validator.add(item.getFileDigest(), token);
                } catch (InterruptedException e) {
                    abortException = e;
                }
            }
        } else {
            if (item.getState() != 'C') {
                String msg = "Expected digest: " + item.getFileDigest()
                        + " Saw: " + currentFile.getHash();
                event = logManager.createItemEvent(LogEnum.FILE_CORRUPT,
                        item.getPath(), msg);
                item.setState('C');
                item.setStateChange(new Date());
                LOG.trace(
                        "Toggling state to Corrupt for " + item.getPath());
            } else {
                event = null;
            }
        }

        return event;
    }

    private LogEvent[] addNewFile(FileBean currentFile, String fileName,
            final String parentName, final MonitoredItemManager mim) {

        newFilesFound++;
        LogEvent[] event = new LogEvent[2];

        LOG.trace("Registering new item " + fileName);
        event[0] = logManager.createItemEvent(LogEnum.FILE_NEW, fileName,
                coll.getDirectory() + fileName);
        if (currentFile.isError()) {
            mim.addItem(fileName, parentName, false, coll, 'M', 0);
            event[1] = logManager.createItemEvent(LogEnum.ERROR_READING, fileName,
                    currentFile.getErrorMessage());
            totalErrors++;
        } else {
            // create new item with state of missing token
            MonitoredItem mi = mim.addItem(fileName, parentName, false, coll, 'T',
                    currentFile.getFileSize());
            event[1] = null;
            TokenRequest request = new TokenRequest();
            request.setName(mi.getId().toString());
            request.setHashValue(currentFile.getHash());
            if (!Strings.isEmpty(fileName) && 
                !Strings.isEmpty(currentFile.getHash()) && batch != null) {
                try {
                    batch.add(request);
                } catch (InterruptedException e) {
                    abortException = e;
                }
            }
        }
        return event;
    }

    private String extractAndRegisterParent(MonitoredItemManager mim,
            FileBean currentFile) {
        String parentName = (currentFile.getPathList().length > 1
                ? currentFile.getPathList()[1] : null);


        // 1. make sure directory path is registered
        if (parentName != null) {
            parentName = Strings.cleanStringForXml(parentName, '_');
            for (int i = 1; i < currentFile.getPathList().length; i++) {
                String parent = (currentFile.getPathList().length > i + 1
                        ? currentFile.getPathList()[i + 1] : null);
                parent = Strings.cleanStringForXml(parent, '_');
                mim.createDirectory(currentFile.getPathList()[i], parent, coll);
            }
        }

        return parentName;
    }

    private String[] createMailList() {
        String addrs = SettingsUtil.getString(coll, ConfigConstants.ATTR_EMAIL_RECIPIENTS);
        String[] maillist = (addrs == null ? null : addrs.split("\\s*,\\s*"));
        return maillist;
    }

    private void setCollectionState() {
        EntityManager em = PersistUtil.getEntityManager();

        if (baseItemPathList != null) {
            if (coll.getState() == '\0' || coll.getState() == 'N') {
                return;
            }
        } else {
            coll.setLastSync(new Date());
        }

        MonitoredItemManager mim = new MonitoredItemManager(em);

        if (mim.countErrorsInCollection(coll) == 0) {
            coll.setState('A');
        } else {
            coll.setState('E');
        }

        EntityTransaction trans = em.getTransaction();
        trans.begin();
        em.merge(coll);
        trans.commit();
        em.close();
    }

    private void setInactiveBefore(Date d) {
        if (baseItemPathList != null || cancel || abortException != null) {
            return;
        }

        DataSource dataSource = PersistUtil.getDataSource();
        EntityManager em = PersistUtil.getEntityManager();
        MonitoredItemManager mim = new MonitoredItemManager(em);
        EntityTransaction trans = em.getTransaction();
        trans.begin();

        try {
            Query query = em.createNamedQuery("MonitoredItem.updateMissing")
                .setParameter("coll", coll)
                .setParameter("date", d);

            int i = query.executeUpdate();
            if (i > 0) {
                LOG.info("Set " + i + " new missing items");

                Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement("INSERT INTO logevent(session, path, date, logtype, collection_id) " +
                        "SELECT ?, path, NOW(), ?, parentcollection_id FROM monitored_item m WHERE m.parentcollection_id = ? AND m.state = ? AND m.statechange = ?");
                ps.setLong(1, session);
                ps.setInt(2, LogEnum.FILE_MISSING.getType());
                ps.setLong(3, coll.getId());
                ps.setString(4, String.valueOf('M'));
                ps.setTimestamp(5, new Timestamp(d.getTime()));
                ps.executeUpdate();
                ps.close();
                connection.close();
            }
            trans.commit();
        } catch (SQLException e) {
            trans.rollback();
            LOG.error(e);
        } finally {
            em.close();
        }

        /*
        int idx = 0;
        for (MonitoredItem mi : mim.listItemsBefore(coll, d)) {
            LOG.trace("Updating missing item: " + mi.getPath());
            LOG.trace("Item information: LS= " + mi.getLastSeen()
                    + ", LV = " + mi.getLastVisited()
                    + ", SC = " + mi.getStateChange()
                    + "Audit started on: " + d);
            if (mi.getState() != 'M'
                && (mi.getStateChange() == null
                    || d.after(mi.getStateChange()))) {
                mi.setState('M');
                mi.setStateChange(new Date());
                em.persist(logManager.createItemEvent(LogEnum.FILE_MISSING,
                        mi.getPath()));
            }
            mi.setLastVisited(new Date());
            em.merge(mi);
            idx++;
            if (idx % 30 == 0) {
                em.flush();
                em.clear();
            }
        }
        */
    }

    private void compareToPeers() {

        EntityManager em = PersistUtil.getEntityManager();
        MonitoredItemManager mim = new MonitoredItemManager(em);

        List<MonitoredItem> currentErrors = mim.listRemoteErrors(coll);
        mim = null;
        em.close();
        em = null;

        for (PeerCollection pc : coll.getPeerCollections()) {
            InputStream digestStream = JsonGateway.getGateway().getDigestList(
                    pc.getSite(),
                    pc.getPeerId());
            if (digestStream == null) {
                em = PersistUtil.getEntityManager();
                logManager.persistCollectionEvent(LogEnum.SYSTEM_ERROR,
                        "Cannot collect digests from remote site: "
                        + pc.getSite().getRemoteURL(), em);
                em.close();
                em = null;
                LOG.info("remote site returned null stream " + pc.getSite().
                        getRemoteURL());
                continue;
            }
            CollectionCompare2 cc = new CollectionCompare2(digestStream, null);
            LOG.trace("Starting remote compare for " + pc.getSite());

            try {
                CompareResults cr = new CompareResults(cc);
                cc.compareTo(cr, coll, null);
                //                cc.loadCollectionTable(coll, null);
                //                cc.doCompare();
                // For now, we don't care about files that only exist remotely
                //                cc.getUnseenTargetFiles();
                // State: P, only set if file is currently active as local errors
                // take precedence
                em = PersistUtil.getEntityManager();
                mim = new MonitoredItemManager(em);

                for (String unseenFile : cr.getUnseenTargetFiles()) {
                    LOG.trace("Item missing at remote "
                            + unseenFile + " " + pc.getSite());
                    MonitoredItem mi = mim.getItemByPath(unseenFile, coll);
                    currentErrors.remove(mi);
                    if (mi.getState() == 'A') {

                        mi.setState('P');
                        mi.setStateChange(new Date());

                        EntityTransaction trans = em.getTransaction();
                        trans.begin();
                        em.merge(mi);
                        em.persist(logManager.createItemEvent(
                                LogEnum.REMOTE_FILE_MISSING, unseenFile,
                                pc.getSite().getRemoteURL()));
                        trans.commit();
                    }
                }
                // State: D
                for (CompareResults.DifferingDigest dd : cr.getDifferingDigests()) {
                    MonitoredItem mi = mim.getItemByPath(dd.getName(), coll);
                    currentErrors.remove(mi);
                    if (mi.getState() == 'A') {
                        LOG.trace(
                                "Item corrupt at remote " + mi.getPath() + " " + pc.getSite());
                        mi.setState('D');
                        mi.setStateChange(new Date());


                        EntityTransaction trans = em.getTransaction();
                        trans.begin();
                        em.merge(mi);
                        String errorMsg = "Expected digest: " + mi.getFileDigest() + " Saw: "
                                + dd.getTargetDigest() + " site: " + pc.getSite().getRemoteURL();
                        em.persist(logManager.createItemEvent(LogEnum.REMOTE_FILE_CORRUPT, dd.getName(), errorMsg));
                        trans.commit();
                    }
                }

                LOG.trace("Ending site compare " + pc.getSite());
            } finally {
                if (em != null) {
                    em.close();
                    em = null;
                }
            }
        }
        // Any files remaining in currentErrors should transition back
        // to active.
        em = PersistUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        Date currDate = new Date();

        trans.begin();
        for (MonitoredItem mi : currentErrors) {
            LOG.trace(
                    "Item online at remote " + mi.getPath());
            mi.setState('A');
            mi.setStateChange(currDate);
            em.persist(logManager.createItemEvent(LogEnum.REMOTE_FILE_ONLINE,
                    mi.getPath()));
            em.merge(mi);
        }

        trans.commit();
        em.close();
    }
}
