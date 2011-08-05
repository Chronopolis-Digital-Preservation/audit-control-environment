/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.audit;

/**
 *
 * @author toaster
 */
public interface AuditQueueListener {

    public void sourceStart(AuditSource as);
    public void sourceFinish(AuditSource as);
}
