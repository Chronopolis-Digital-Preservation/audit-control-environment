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

import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.settings.SettingsConstants;
import edu.umiacs.ace.util.CollectionThreadPoolExecutor;
import edu.umiacs.ace.util.KSFuture;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.util.Submittable;
import edu.umiacs.util.Strings;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static edu.umiacs.ace.util.Submittable.RunState.QUEUED;
import static edu.umiacs.ace.util.Submittable.RunState.RUNNING;

/**w
 *
 * @author toaster
 */
public class AuditThreadFactory {
    private static final Logger LOG = Logger.getLogger(AuditThreadFactory.class);

    private static ConcurrentHashMap<Collection, KSFuture<AuditThread>> audits =
            new ConcurrentHashMap<>();
    private static int max_audits = 3;
    private static String imsHost = null;
    private static int imsPort = 80;
    private static String tokenClass = "SHA-256";
    private static boolean auditOnly = false;
    private static boolean ssl = false;
    private static boolean blocking = false;
    private static int imsRetryAttempts = 3;
    private static int imsResetTimeout = 3000;

    public static void setIMS( String ims ) {
        if (Strings.isEmpty(ims)) {
            LOG.error("Empty ims string, setting from default value");
            ims = SettingsConstants.ims;
        }
        imsHost = ims;
    }

    public static String getTokenClass() {
        return tokenClass;
    }

    public static void setTokenClass(String tokenClass) {
        AuditThreadFactory.tokenClass = tokenClass;
    }

    public static String getIMS() {
        return imsHost;
    }

    public static int getImsPort() {
        return imsPort;
    }

    public static void setImsPort(int imsPort) {
        if (imsPort < 1 || imsPort > 32768) {
            LOG.warn("ims port must be between 1 and 32768, setting default");
            imsPort = Integer.parseInt(SettingsConstants.imsPort);
        }
        AuditThreadFactory.imsPort = imsPort;
    }

    public static void setAuditOnly(boolean auditOnlyMode) {
        AuditThreadFactory.auditOnly = auditOnlyMode;
    }

    public static int getImsRetryAttempts() {
        return imsRetryAttempts;
    }

    public static void setImsRetryAttempts(int attempts) {
        if (attempts >= 0) {
            imsRetryAttempts = attempts;
        }
    }

    public static int getImsResetTimeout() {
        return imsResetTimeout;
    }

    public static void setImsResetTimeout(int timeout) {
        if (timeout >= 0) {
            imsResetTimeout = timeout;
        }
    }

    public static void setBlocking(boolean blocking) {
        AuditThreadFactory.blocking = blocking;
    }

    public static boolean isBlocking() {
        return AuditThreadFactory.blocking;
    }

    public static boolean isAuditing() {
        return !audits.isEmpty();
    }

    /**
     * Return a new or existing thread if any room is available New threads will start replication
     * 
     * @param collection the {@link Collection} to run a file audit on
     * @param driver the {@link StorageDriver} for retrieving files
     * @param verbose flag for setting verbose output of the {@link AuditThread}
     * @param startItem the first {@link MonitoredItem} to audit from or null to audit all items
     */
    public static void createThread(Collection collection,
                                    StorageDriver driver,
                                    boolean verbose,
                                    MonitoredItem... startItem) {
        // Note: Because we don't put the collection/thread in the map atomically, we need to lock
        CollectionThreadPoolExecutor executor = getExecutor();
        LOG.trace("Creating new thread for " + collection.getName());
        AuditThread newThread;
        newThread = new AuditThread(collection, driver, auditOnly, verbose, startItem);
        newThread.setImsHost(imsHost);
        newThread.setImsPort(imsPort);
        newThread.setTokenClassName(tokenClass);
        KSFuture<AuditThread> f = executor.submitFileAudit(collection, newThread);
        if (f != null) {
            audits.put(collection, f);
        }
    }

    public static AuditThread getThread(Collection c) {
        KSFuture<AuditThread> future = audits.get(c);
        if (future != null) {
            return future.getKnownResult().getThread();
        }

        return null;
    }

    public static boolean isQueued(Collection c) {
        return checkState(c, QUEUED);
    }

    public static boolean isRunning(Collection c) {
        return checkState(c, RUNNING);
    }

    private static boolean checkState(Collection c, Submittable.RunState state) {
        KSFuture<AuditThread> future = audits.get(c);
        if (future != null) {
            // purge
            if (future.isDone()) {
                audits.remove(c);
            }

            Submittable s = future.getKnownResult();
            if (s != null) {
                return s.getState() == state;
            }
        }

        return false;
    }

    static void cancellAll() {
        LOG.info("Shutting down audits");
        for (KSFuture submittable : audits.values()) {
            submittable.cancel(true);
        }
    }

    public static int getMaxAudits() {
        return max_audits;
    }

    // Why does max_audits have an underscore? Oh well...
    public static void setMaxAudits(int max_audits) {
        if ( max_audits <= 0 ) {
            return;
        }
        AuditThreadFactory.max_audits = max_audits;
    }

    public static void setSSL(Boolean ssl) {
        AuditThreadFactory.ssl = ssl;
    }

    /**
     * Method for AuditThread to notify its finished
     *
     * @param c
     */
    static void finished(Collection c) {
        LOG.trace("Finishing audit for collection " + c.getName());
        // Clean up everything which may contain a reference to the thread
        // Thread will only ever be removed once, so no need to worry about
        // race conditions
        audits.remove(c);
    }

    public static boolean useSSL() {
        return AuditThreadFactory.ssl;
    }

    private static CollectionThreadPoolExecutor getExecutor() {
        return CollectionThreadPoolExecutor.getExecutor();
    }

    private static MonitoredItem[] getSampledList(Collection c) {
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("MonitoredItem.listIds");
        q.setParameter("coll", c);
        List<Long> itemIds = q.getResultList();
        int size = (int) Math.ceil(Math.sqrt(itemIds.size()));

        SecureRandom rand = new SecureRandom();
        List<MonitoredItem> items = new LinkedList<>();
        for ( int i=0;i < size; i++) {
            int idxToGet = rand.nextInt(itemIds.size());
            MonitoredItem item = em.find(MonitoredItem.class, itemIds.remove(idxToGet));
            items.add(item);
        }

        return items.toArray(new MonitoredItem[items.size()]);
    }

    public static void cancel(Collection collection) {
        LOG.info("Cancelling audit on " + collection.getName());
        KSFuture<AuditThread> future = audits.get(collection);
        if (future != null) {
            Submittable<AuditThread> result = future.getKnownResult();
            if (result != null) {
                AuditThread thread = result.getThread();
                thread.cancel();
            }
            future.cancel(true);
        }
    }
}
