/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.driver;

import edu.umiacs.ace.util.ThreadedDigestStreamListener;

/**
 *
 * @author toaster
 */
public class StateBeanDigestListener implements ThreadedDigestStreamListener{

    private DriverStateBean bean;

    public StateBeanDigestListener(DriverStateBean bean) {
        this.bean = bean;
    }
    
    public void bytesRead(int read) {
        bean.setRead(bean.getRead() + read);
        bean.updateLastChange();
    }

}
