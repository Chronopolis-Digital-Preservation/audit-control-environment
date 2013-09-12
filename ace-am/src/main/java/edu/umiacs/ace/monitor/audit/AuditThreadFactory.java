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
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.PersistUtil;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.log4j.Logger;

/**w
 *
 * @author toaster
 */
public class AuditThreadFactory {
    private static final Logger LOG = Logger.getLogger(AuditThreadFactory.class);

    private static int max_audits = 3;
    //private static final Map<Collection, AuditThread> runningThreads =
    //        new HashMap<Collection, AuditThread>();
    private static final ConcurrentHashMap<Collection, AuditThread> runningAudits = 
            new ConcurrentHashMap<Collection, AuditThread>();
    private static final LinkedBlockingQueue<Runnable> blockingQueue = 
            new LinkedBlockingQueue<Runnable>();
    private static final ThreadPoolExecutor executor = 
            new ThreadPoolExecutor(3, 3, 2, TimeUnit.MINUTES, blockingQueue);
    private static String imsHost = null;
    private static int imsPort = 8080;
    private static String tokenClass = "SHA-256";
    private static boolean auditOnly = false;
    private static boolean auditSample = false;
    private static boolean ssl = false;

    public static void setIMS( String ims ) {
        imsHost = ims;
    }

    public static String getTokenClass() {
        return tokenClass;
    }

    public static void setTokenClass( String tokenClass ) {
        AuditThreadFactory.tokenClass = tokenClass;
    }

    public static String getIMS() {
        return imsHost;
    }

    public static int getImsPort() {
        return imsPort;
    }

    public static void setImsPort( int imsPort ) {
        AuditThreadFactory.imsPort = imsPort;
    }

    public static void setAuditOnly(boolean auditOnlyMode) {
        AuditThreadFactory.auditOnly = auditOnlyMode;
    }

    public static void setAuditSampling(boolean auditSampling ) {
        AuditThreadFactory.auditSample = auditSampling;
    }

    public static boolean isAuditing() {
        return executor.getActiveCount() != 0;
        //return !runningThreads.isEmpty();
    }

    /**
     * Return a new or existing thread if any room is available New threads will start replication
     * 
     * @param c
     * @param tri
     * @return
     */
    public static AuditThread createThread( Collection c, StorageDriver tri, boolean verbose,
            MonitoredItem... startItem ) {
        synchronized ( blockingQueue ) {
            AuditThread newThread = new AuditThread(c, tri, auditOnly, verbose, startItem);
            newThread.setImsHost(imsHost);
            newThread.setImsport(imsPort);
            newThread.setTokenClassName(tokenClass);
            boolean contains = runningAudits.contains(c);
            if (!contains) {
                runningAudits.put(c, newThread);
                executor.execute(newThread);
            }
            return newThread;
        }
        /*
        synchronized ( runningThreads ) {
            AuditThread newThread = null;
            //if ( blockingQueue.contains(runningQueue))
            if ( !runningThreads.containsKey(c) && runningThreads.size() < max_audits ) {
                if ( auditSample ) {
                    startItem = getSampledList(c);
                }
                newThread = new AuditThread(c, tri, auditOnly,
                    verbose, startItem);
                newThread.setImsHost(imsHost);
                newThread.setImsport(imsPort);
                newThread.setTokenClassName(tokenClass);
                newThread.start();
                runningThreads.put(c, newThread);
            }
            return newThread;
        }
        */
    }

    public static AuditThread getThread(Collection c) {
        AuditThread thread;
        thread = runningAudits.get(c);
        return thread;
    }

    public static final boolean isQueued( Collection c ) {
        return blockingQueue.contains(getThread(c));
    }

    public static final boolean isRunning( Collection c ) {
        AuditThread thread = getThread(c);
        return thread != null && !blockingQueue.contains(thread);
        /*
        synchronized ( runningThreads ) {
            return runningThreads.containsKey(c);
        }
        */
    }

    static void cancellAll() {
        LOG.info("Shuttding down thread factory");
        executor.shutdown();
        if ( executor.isTerminated() ) {
            executor.shutdownNow();
        }
        runningAudits.clear();
        blockingQueue.clear();
        /*
        for ( AuditThread at : runningThreads.values() ) {
            at.cancel();
        }
        */
    }

    public static int getMaxAudits() {
        return max_audits;
    }

    public static void setMaxAudits( int max_audits ) {
        AuditThreadFactory.max_audits = max_audits;
        executor.setMaximumPoolSize(max_audits);
    }

    public static void setSSL(Boolean ssl) {
        AuditThreadFactory.ssl = ssl;
    }

    /**
     * Return the current thread for a collection.
     * @param c collection to fetch
     * 
     * @return current running thread or null if nothing is running
     * 
    public static AuditThread getThread( Collection c ) {
        synchronized ( runningThreads ) {
            if ( isRunning(c) ) {
                return runningThreads.get(c);
            }
        }
        return null;
    }
    */

    /**
     * Method for AuditThread to notify its finished
     * @param c
     */
    static void finished( Collection c ) {
        AuditThread thread = runningAudits.remove(c);
        if ( thread != null ) {
            executor.remove(thread);
            blockingQueue.remove(thread);
            thread = null;
        }
            /*
        synchronized ( runningThreads ) {
            AuditThread thread = runningThreads.remove(c);
            if (thread.isAlive()) {
                System.out.println("Here's yer problem");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
                thread.cancel();
            }
        }
            */
    }

    public static boolean useSSL() {
        return AuditThreadFactory.ssl;
    }

    private static MonitoredItem[] getSampledList(Collection c) {
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("MonitoredItem.listIds");
        q.setParameter("coll", c);
        List<Long> itemIds = q.getResultList();
        int size = (int) Math.ceil(Math.sqrt(itemIds.size()));

        SecureRandom rand = new SecureRandom();
        List<MonitoredItem> items = new LinkedList<MonitoredItem>();
        for ( int i=0;i < size; i++) {
            int idxToGet = rand.nextInt(itemIds.size());
            MonitoredItem item = em.find(MonitoredItem.class, itemIds.remove(idxToGet));
            items.add(item);
        }

        return items.toArray(new MonitoredItem[items.size()]);
    }

}
