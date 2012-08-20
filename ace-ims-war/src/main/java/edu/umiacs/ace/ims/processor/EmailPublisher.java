/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.processor;

import edu.umiacs.ace.ims.Log;
import edu.umiacs.ace.ims.system.MailPublisherLocal;
import edu.umiacs.ace.ims.tokenclass.TokenClass;
import edu.umiacs.ace.ims.witness.Witness;
import edu.umiacs.ace.server.ServiceLocator;
import java.util.Date;
import javax.mail.MessagingException;

/**
 *
 * @author toaster
 */
public class EmailPublisher extends WitnessPublisher
{
    

    public void publishWitness(Witness newWitness)
    {
        MailPublisherLocal mailBean =
            ServiceLocator.getInstance().getLocal(MailPublisherLocal.class);
        
        String subject = "IMS Witness for " + newWitness.getTokenClass().getName() + " rounds " + newWitness.getStartRound().getId() + " - " + newWitness.getEndRound().getId();

        StringBuilder sb = new StringBuilder();

        sb.append("Date: ");
        sb.append(new Date().toString());
        sb.append("\nWitness ID: ");
        sb.append(newWitness.getId());
        sb.append("\nWitness: ");
        sb.append(newWitness.getHashValue());
        sb.append("\nIncluded rounds: ");
        sb.append(newWitness.getRoundCount());
        sb.append("\nStart Round: ");
        sb.append(newWitness.getStartRound().getId());
        sb.append(" ");
        sb.append(newWitness.getStartRound().getHashValue());
        sb.append("\nEnd Round: ");
        sb.append(newWitness.getEndRound().getId());
        sb.append(" ");
        sb.append(newWitness.getEndRound().getHashValue());

        try
        {
            mailBean.sendMessage(subject, sb.toString());
            Log.system("Successfully Published witness to email ");
        }
        catch ( MessagingException ex )
        {
            Log.error("Unable to send witness to email", ex);
        }

    }

    public boolean validForClass(TokenClass tokenClass)
    {
        return true;
    }
}
