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

import edu.umiacs.ace.ims.api.IMSService;
import edu.umiacs.ace.ims.api.TokenValidator;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.CollectionState;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.MonitoredItemManager;
import edu.umiacs.ace.monitor.log.LogEnum;
import edu.umiacs.ace.monitor.log.LogEventManager;
import edu.umiacs.ace.token.AceToken;
import edu.umiacs.ace.util.CollectionThreadPoolExecutor;
import edu.umiacs.ace.util.KSFuture;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.util.Submittable;
import edu.umiacs.ace.util.TokenUtil;
import edu.umiacs.util.Strings;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author toaster
 */
public final class AuditTokens extends Thread implements CancelCallback {

    private static final Map<Collection, KSFuture<AuditTokens>> runningThreads =
            new ConcurrentHashMap<>();
    private Map<AceToken, MonitoredItem> itemMap =
            new ConcurrentHashMap<>();
    private static final Logger LOG = Logger.getLogger(AuditTokens.class);
    private Collection collection;
    private boolean cancel = false;
    private boolean hasRun = false;
    private long session;
    private long tokensSeen = 0;
    private TokenAuditCallback callback;
    private String imsHost;
    private int imsPort;
    private LogEventManager logManager;

    private AuditTokens(Collection collection) {
        this.collection = collection;
        session = System.currentTimeMillis();
        logManager = new LogEventManager(session, collection);
    }

    /**
     * Return the current thread for a collection.
     *
     * @param c collection to fetch
     * @return current running thread or null if nothing is running
     */
    public static AuditTokens getThread(Collection c) {
        synchronized (runningThreads) {
            if (isRunning(c)) {
                KSFuture<AuditTokens> future = runningThreads.get(c);
                return getAuditTokens(future);
            }
        }
        return null;
    }

    /**
     * Return a new or existing thread. New threads will start replication
     *
     * @param c the collection to create a thread for
     * @return The AuditTokens thread
     */
    public static AuditTokens createThread(Collection c) {
        CollectionThreadPoolExecutor executor = CollectionThreadPoolExecutor.getExecutor();
        AuditTokens at = new AuditTokens(c);
        at.imsHost = AuditThreadFactory.getIMS();
        at.imsPort = AuditThreadFactory.getImsPort();
        KSFuture<AuditTokens> future = executor.submitTokenAudit(c, at);

        // If we already submitted, return the existing thread
        // Else add the current thread to the map
        if (future == null) {
            KSFuture<AuditTokens> previous = runningThreads.get(c);
            at = getAuditTokens(previous);
        } else {
            runningThreads.put(c, future);
        }

        return at;
    }

    public static boolean isRunning(Collection c) {
        synchronized (runningThreads) {
            return runningThreads.containsKey(c);
        }
    }

    @Override
    public void cancel() {
        this.cancel = true;
        this.interrupt();
    }

    static void cancellAll() {
        for (KSFuture<AuditTokens> at : runningThreads.values()) {
            AuditTokens thread = getAuditTokens(at);
            thread.cancel();
            at.cancel(true);
        }
    }

    public static AuditTokens getAuditTokens(KSFuture<AuditTokens> future) {
        Submittable<AuditTokens> result = future.getKnownResult();
        return result.getThread();
    }

    public long getTotalErrors() {
        if (callback == null) {
            return 0;
        }
        return callback.getTotalErrors();
    }

    public long getTokensSeen() {
        return tokensSeen;
    }

    public long getValidTokens() {
        if (callback == null) {
            return 0;
        }
        return callback.getValidTokens();
    }

    @Override
    public void run() {

        if (hasRun) {
            LOG.fatal("Cannot run thread, already executed");
            throw new IllegalStateException("Thread has already run");
        }
        hasRun = true;

        try {
            // validate tokens
            doWork();


        } catch (Throwable e) {
            LOG.fatal("UNcaught exception in doWork()", e);
        } finally {
            itemMap.clear(); // free memory in case this gets stuck hanging around
        }
    }

    private void markMissingTokensOffline() {
        EntityManager em = PersistUtil.getEntityManager();
        MonitoredItemManager mim = new MonitoredItemManager(em);
        mim.setMissingTokensInvalid(collection, session);
        em.close();
    }

    private TokenValidator openIms() {
        TokenValidator validator = null;


        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            IMSService ims;
            ims = IMSService.connect(imsHost,
                    imsPort,
                    AuditThreadFactory.useSSL(),
                    AuditThreadFactory.isBlocking(),
                    AuditThreadFactory.getMaxBlockTime());
            callback = new TokenAuditCallback(itemMap, this, collection, session);
            validator = ims.createTokenValidator(callback, 1000, 5000,
                    digest);
        } catch (Throwable e) {
            EntityManager em;
            LOG.error("Cannot connect to IMS ", e);
            LogEventManager lem;
            em = PersistUtil.getEntityManager();

            lem = new LogEventManager(session, collection);
            EntityTransaction trans = em.getTransaction();
            trans.begin();
            em.persist(lem.createCollectionEvent(LogEnum.UNKNOWN_IMS_COMMUNICATION_ERROR, e));
            trans.commit();
            em.close();
        }
        return validator;
    }

    private void doWork() {
        EntityManager em;
        Throwable thrown = null;
        boolean interrupted = false;
        TokenValidator validator = openIms();

        if (validator == null) {
            runningThreads.remove(collection);
            return;
        }

        em = PersistUtil.getEntityManager();
        logManager.persistCollectionEvent(LogEnum.TOKEN_AUDIT_START, null, em);
        em.close();
        em = null;

        LOG.info("Starting token audit on collection " + collection.getName());

        try {

            em = PersistUtil.getEntityManager();
            Query q = em.createNamedQuery("MonitoredItem.listByCollection");

            q.setParameter("coll", collection);

            for (Object o : q.getResultList()) {
                MonitoredItem item = (MonitoredItem) o;
                AceToken t = TokenUtil.convertToAceToken(item.getToken());
                if (t != null) {
                    itemMap.put(t, item);
                    tokensSeen++;
                    validator.add(item.getFileDigest(), t);
                }
                if (cancel) {
                    break;
                }
            }

            LOG.trace("finished trolling result set");
            em.close();
            em = null;

            // mark unavailable tokens offline
            markMissingTokensOffline();
        } catch (Throwable t) {
            LOG.error("Unexpected error auditing tokens", t);
            thrown = t;
            interrupted = true;
        } finally {
            validator.close();
            LOG.trace("Validator closed");

            if (em != null) {
                em.close();
            }

            setCollectionState();
            if (interrupted) {
                em = PersistUtil.getEntityManager();
                logManager.persistCollectionEvent(
                        LogEnum.SYSTEM_ERROR, Strings.exceptionAsString(thrown), em);
                em.close();
            } else {
                // finished successfully
                em = PersistUtil.getEntityManager();
                logManager.persistCollectionEvent(
                        LogEnum.TOKEN_AUDIT_FINISH, "successful audit", em);
                em.close();
                LOG.trace("Token Audit ending successfully for " + collection.getName());
            }

            synchronized (runningThreads) {
                runningThreads.remove(collection);
            }
        }
    }

    private void setCollectionState() {
        EntityManager em = PersistUtil.getEntityManager();

        MonitoredItemManager mim = new MonitoredItemManager(em);
        if (mim.countErrorsInCollection(collection) == 0) {
            collection.setState(CollectionState.ACTIVE);
        } else {
            collection.setState(CollectionState.ERROR);
        }

        EntityTransaction trans = em.getTransaction();
        trans.begin();
        em.merge(collection);
        trans.commit();
    }
}
