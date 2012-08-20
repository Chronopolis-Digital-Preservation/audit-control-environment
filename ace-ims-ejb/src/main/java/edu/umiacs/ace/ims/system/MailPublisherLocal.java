/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.system;

import javax.ejb.Local;
import javax.mail.MessagingException;

/**
 *
 * @author toaster
 */
@Local
public interface MailPublisherLocal {

    public void addRecipient(java.lang.String address);

    public java.util.List<javax.mail.internet.InternetAddress> listRecipients();

    public void sendMessage(java.lang.String subject, java.lang.String message) throws MessagingException;
    
}
