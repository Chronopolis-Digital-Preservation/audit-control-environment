/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.processor;

import edu.umiacs.ace.digest.DigestFactory;
import edu.umiacs.ace.digest.DigestService;
import edu.umiacs.ace.ims.IMSContext;
import edu.umiacs.ace.ims.Log;
import edu.umiacs.ace.ims.tokenclass.TokenClass;
import edu.umiacs.ace.ims.witness.Witness;
import edu.umiacs.ace.ims.witness.WitnessLocal;
import edu.umiacs.ace.server.ServiceLocator;
import edu.umiacs.ace.util.HashValue;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Class responsible for automatically generating and publishing witnesses. One
 * instance per tokenClass will be created and used as a task by quartz in the 
 * ApplicationListener
 * 
 * @author toaster
 */
public class WitnessPublication
{

    private WitnessLocal witnessBean =
            ServiceLocator.getInstance().getLocal(WitnessLocal.class);
    private TokenClass tokenClass;
    private MessageDigest digest;

    public WitnessPublication(TokenClass tokenClass)
    {
        this.tokenClass = tokenClass;
        DigestService ds = DigestFactory.getInstance().getService(
                tokenClass.getDigestProviderName(), 
                tokenClass.getDigestServiceName());
        digest = ds.createMessageDigest();
    }

    /**
     * Witness publication involves two steps. First a random hash is inserted into
     * the current link list. This is in case there were no rounds during since the
     * last witness was issued. Second, a witness is created through the witnessbean ejb
     * 
     */
    public void publishWitness()
    {
        Witness witness;
        List<WitnessPublisher> publishers;
        String randomLink;

        // Step 1. Create random round to ensure at least one round has been generated
        Log.system("Publishing witness for " + tokenClass.getName() + " on " + new Date());
        randomLink = createRandomRound(digest);
        publishers = IMSContext.getInstance().getWitnessPublishers();
        RoundProcessors.getInstance().requestLink("IMS", tokenClass, randomLink);
        
        // Step 2. Create new witness value
        witness = witnessBean.publishWitness(tokenClass.getName());

        // Step 3. Publish witness to all interested parties
        if ( publishers == null )
        {
            Log.system("No available publishers, returning");
            return;
        }

        for ( WitnessPublisher publisher : publishers )
        {
            sendWitness(publisher, witness);
        }
        Log.system("Witness publication finished for " + tokenClass.getName());
    }

    private void sendWitness(WitnessPublisher publisher, Witness witness)
    {
        try
        {
            if ( publisher.validForClass(tokenClass) )
            {
                Log.system("publishing to: " + publisher.getClass());
                publisher.publishWitness(witness);
            }
        }
        catch ( Throwable t )
        {
            Log.error("Error publishing witness " + t.getMessage(), t);
        }
    }
    
    
    private String createRandomRound(MessageDigest digest)
    {
        
        byte[] randBytes = new byte[4096];
        Random r = new Random();
        r.nextBytes(randBytes);
        digest.update(randBytes);
        return HashValue.asHexString(digest.digest());
    }
}
