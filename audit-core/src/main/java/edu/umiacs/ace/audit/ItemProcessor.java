/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.audit;

/**
 *
 * @author toaster
 */
public class ItemProcessor implements Runnable{

    private AuditItem ai;

    public ItemProcessor(AuditItem ai) {
        this.ai = ai;
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
