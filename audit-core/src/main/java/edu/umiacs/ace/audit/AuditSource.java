/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.audit;

/**
 *
 * @author toaster
 */
public abstract class AuditSource {

    private long lastTake = 0;
    private int processingTasks = 0;
    private Policy auditPolicy;
    private boolean aborted;


    protected abstract AuditItem loadNext();
    protected abstract boolean moreItems();


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
     * 
     * @return next item or null if none
     */
    public AuditItem nextItem() {
        if (!hasNext()) {
            return null;
        }
        lastTake = System.currentTimeMillis();
        return loadNext();
    }

    /**
     * Return the last time an item was retrieved from this queue. Useful for
     * building policy.
     * 
     * @return
     */
    public long getLastTake() {
        return lastTake;
    }

    /**
     * Tell all items from this source to immediately stop processing.
     */
    public final void abort()
    {
        aborted = true;
        notifyAbort();
    }
    /**
     * No-op method. Implementers may override to receive 
     * 
     */
    protected void notifyAbort()
    {

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
    public final boolean hasNext()
    {
        if (aborted)
            return false;
        return moreItems();

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
