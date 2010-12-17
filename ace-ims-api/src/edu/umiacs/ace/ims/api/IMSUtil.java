/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.api;

import edu.umiacs.ace.ims.ws.ProofElement;
import edu.umiacs.ace.ims.ws.TokenResponse;
import edu.umiacs.ace.token.AceToken;
import edu.umiacs.ace.token.AceTokenBuilder;
import edu.umiacs.ace.util.HashValue;

/**
 *
 * @author toaster
 */
public class IMSUtil {

    private static ThreadLocal<AceTokenBuilder> tokenBuilder = new ThreadLocal<AceTokenBuilder>()
    {

        @Override
        protected AceTokenBuilder initialValue() {
            return new AceTokenBuilder();
        }

    };

    public static AceToken convertResponse(TokenResponse response, String ims) {
        
        AceTokenBuilder bldr = tokenBuilder.get();
        bldr.setDate(response.getTimestamp().toGregorianCalendar().getTime());
        bldr.setDigestAlgorithm(response.getDigestService());
        bldr.setRound(response.getRoundId());
        bldr.setIms(ims);
        bldr.setImsService(response.getTokenClassName());

        for (ProofElement pe : response.getProofElements())
        {
            bldr.startProofLevel(pe.getHashes().size() + 1);
            bldr.setLevelInheritIndex(pe.getIndex());
            
            for (String hash : pe.getHashes())
            {
                bldr.addLevelHash(HashValue.asBytes(hash));
            }
            
        }
        return bldr.createToken();
    }
}
