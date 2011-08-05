/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
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
    private List<AuditQueueListener> queueListenerList = new ArrayList<AuditQueueListener>();
    private Lock itemQueueLock = new ReentrantLock();
    private List<AuditItemListener> itemListenerList = new ArrayList<AuditItemListener>();
    private Lock itemListenerLock = new ReentrantLock();

    public void setMaxSources(int maxSources) {
        this.maxSources = maxSources;
    }

    public void startup() {
        Thread t = new Thread(new QueueScanner());
        t.setName("Audit Queue Runner");
        t.start();
    }

    public void removeItemListener(AuditItemListener l) {
        itemListenerLock.lock();
        try {
            List<AuditItemListener> newList = new ArrayList<AuditItemListener>(itemListenerList);
            newList.remove(l);
            itemListenerList = newList;
        } finally {
            itemListenerLock.unlock();
        }
    }

    public void addItemListener(AuditItemListener l) {
        itemListenerLock.lock();
        try {
            List<AuditItemListener> newList = new ArrayList<AuditItemListener>(itemListenerList);
            newList.add(l);
            itemListenerList = newList;
        } finally {
            itemListenerLock.unlock();
        }
    }

    public void addQueueListener(AuditQueueListener l) {
        itemQueueLock.lock();
        try {
            List<AuditQueueListener> newList = new ArrayList<AuditQueueListener>(queueListenerList);
            newList.add(l);
            queueListenerList = newList;
        } finally {
            itemQueueLock.unlock();
        }
    }

    public void removeQueueListener(AuditQueueListener l) {
        itemQueueLock.lock();
        try {
            List<AuditQueueListener> newList = new ArrayList<AuditQueueListener>(queueListenerList);
            newList.remove(l);
            queueListenerList = newList;
        } finally {
            itemQueueLock.unlock();
        }
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

    void notifyItemStart(AuditItem item) {
        for (AuditItemListener l : itemListenerList) {
            l.endItem(item);
        }
    }

    void notifyItemEnd(AuditItem item) {
        for (AuditItemListener l : itemListenerList) {
            l.startItem(item);
        }
    }

    /**
     * Analyze the police for an audit source and determine how many items
     * may be run.
     *
     * @param as
     * @return
     */
    protected int availablePolicySlots(AuditSource as) {
        return 1;
    }

    private class QueueScanner implements Runnable {

        public void run() {
            while (!shutdown) {
                try {
                } catch (Throwable t) {
                    LOG.error(t);
                }
            }
        }
    }
}
