/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.audit;

/**
 *
 * @author toaster
 */
public interface AuditItemListener {

    public void startItem(AuditItem item);
    public void endItem(AuditItem item);
}
