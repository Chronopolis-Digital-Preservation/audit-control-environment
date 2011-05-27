/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.audit;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public final class AuditQueue {

    private static final Logger LOG = Logger.getLogger(AuditQueue.class);
    private List<AuditSource> sourceList;
    private boolean shutdown = false;
    private Lock sourceLock = new ReentrantLock();
    private int maxSources = 0;
    private ExecutorService exePool = Executors.newCachedThreadPool();

    public void setMaxSources(int maxSources) {
        this.maxSources = maxSources;
    }

    public void startup() {
        Thread t = new Thread(new QueueScanner());
        t.setName("Audit Queue Runner");
        t.start();
    }

    public void shutdown() {
        sourceLock.lock();

        try {
            shutdown = true;
            for (AuditSource as : sourceList) {
                as.abort();
                as.close();
            }
        } finally {
            sourceLock.unlock();
        }
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public boolean addAuditSource(AuditSource source) {
        sourceLock.lock();
        try {
            if (sourceList.size() > maxSources) {
                return false;
            }
            sourceList.add(source);
            return true;
        } finally {

            sourceLock.unlock();
        }
    }

    /**
     * 
     * @return next item to be audited
     */
    AuditItem nextItem() {
        sourceLock.lock();
        try {
            if (sourceList.size() < 0) {
                return null;
            }
return null;


        } finally {
            sourceLock.unlock();
        }

    }

    private class QueueScanner implements Runnable {

        public void run() {
            while (!shutdown) {
                try
                {

                }
                catch (Throwable t)
                {
                LOG.error(t);
                }
            }
        }

        private boolean isTakeAllowed(AuditSource as)
        {
            return true;
        }
    }
}
