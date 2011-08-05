/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.audit;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author toaster
 */
public abstract class AuditSource {

    private long lastTake = 0;
//    private int processingTasks = 0;
    private Policy auditPolicy;
    private boolean aborted;

    public Policy getAuditPolicy() {
        return auditPolicy;
    }

    /**
     * Set the audit policy associated with this source.
     * 
     * @param auditPolicy
     */
    public void setAuditPolicy(Policy auditPolicy) {
        this.auditPolicy = auditPolicy;
    }

    /**
     * Immediately return next item.
     *
     * @return next item or null if nothing is immediately available
     */
    public final AuditItem nextItem() {

        AuditItem next = getNext();

        if (next != null) {
            lastTake = System.currentTimeMillis();
        }
        return next;
    }

    /**
     * Return the last time an item was retrieved from this queue. Useful for
     * building policy.
     * 
     * @return
     */
    public final long getLastTake() {
        return lastTake;
    }

    /**
     * Tell all items from this source to immediately stop processing.
     */
    public final void abort() {
        aborted = true;
        notifyAbort();
    }

    public final boolean isAborted() {
        return aborted;
    }

    /**
     * Determine if any more items will ever be available from this queue.
     *
     * 
     * @return false if aborted or driver has no more items, true otherwise
     */
    public final boolean hasNext() {
        if (aborted) {
            return false;
        }
        return !finished();

    }

    /**
     *
     * @return true if driver has exhausted all items and will never return more
     */
    protected abstract boolean finished();

    /**
     * Return next immediately available item. If this call will block, implementers
     * should override available and queueNext to allow for background loading
     * 
     * @return next immediately available item or null if none available.
     */
    public abstract AuditItem getNext();

    /**
     * No-op method. Implementers may override to receive notification of
     * an abort
     *
     */
    protected void notifyAbort() {
    }

    /**
     * For long running audits, this should load the next item(s). This will be
     * called in a separate thread from the thread calling hasNext. Requesters may
     * hint at a preferred number of items to load. Overriding methods may ignore
     * the preferred request.
     * 
     * Default implementation is a no-op.
     * @param auditPolicy preferred items that should be available after this completes
     */
    public void queueNext(int preferred) {
    }

    /**
     * Return how many items are currently available. This is used to determine
     * if a background request to queueNext is required. Default implementation
     * returns -1. If you have long-running metadata listings, you should override
     *
     * @return items immediately available or -1 if items are always available.
     */
    public int available() {
        return -1;
    }

    /**
     * No-op method. Implementers may override to perform any source shutdown
     * operations. No additional calls to any data retrieval (next, hasNext) will
     * be called after this.
     * 
     */
    public void close() {
    }
}
