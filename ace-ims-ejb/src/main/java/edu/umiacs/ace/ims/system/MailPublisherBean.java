/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.system;

import edu.umiacs.util.Check;
import edu.umiacs.util.Strings;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author toaster
 */
@Stateless
public class MailPublisherBean implements MailPublisherLocal
{
    @Resource(name = "mail/ace-ims")
    private Session mailSession;
    @PersistenceContext
    private EntityManager em;

    /**
     *
     * @param address
     * @throws IllegalArgumentException if address is bad
     */
    public void addRecipient(String address)
    {
        Check.notNull("address", address);
        try
        {
            new InternetAddress(address);
        } catch (AddressException ex)
        {
            throw new IllegalArgumentException("Bad email address");
        }

        MailRecipient mr = new MailRecipient();
        mr.setAddress(address);
        em.persist(mr);
    }

    public List<InternetAddress> listRecipients()
    {
        List<InternetAddress> recipients = new ArrayList();
        Query query = em.createNamedQuery("MailRecipient.list");
        for (Object o : query.getResultList())
        {
            MailRecipient mr = (MailRecipient) o;
            try
            {
                recipients.add(new InternetAddress(mr.getAddress()));
            } catch (AddressException e)
            {
                throw new RuntimeException(e);
            }
        }
        return recipients;
    }

    public void sendMessage(String subject, String content) throws
            MessagingException
    {


        MimeMessage message = new MimeMessage(mailSession);
        message.setSubject(subject);
        message.setContent(content, "text/plain");
        for (InternetAddress ia : listRecipients())
        {
            message.addRecipient(Message.RecipientType.TO, ia);
        }

        Transport transport = mailSession.getTransport();
        transport.connect();
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
        transport.close();


    }
}
