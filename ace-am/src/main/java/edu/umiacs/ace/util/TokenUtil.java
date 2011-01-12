package edu.umiacs.ace.util;

import edu.umiacs.ace.hashtree.Proof;
import edu.umiacs.ace.hashtree.ProofBuilder;
import edu.umiacs.ace.monitor.audit.AuditThreadFactory;
import edu.umiacs.ace.monitor.core.Token;
import edu.umiacs.ace.token.AceToken;
import edu.umiacs.ace.token.AceTokenBuilder;

/**
 *
 * @author toaster
 */
public class TokenUtil {

    public static AceToken convertToAceToken( Token token ) {

        if ( token == null ) {
            return null;
        }
        AceTokenBuilder tokenBuilder = new AceTokenBuilder();

        tokenBuilder.setDate(token.getCreateDate());
        tokenBuilder.setDigestAlgorithm(token.getProofAlgorithm());
        tokenBuilder.setIms(AuditThreadFactory.getIMS());
        tokenBuilder.setImsService(token.getImsService());
        tokenBuilder.setRound(token.getRound());

        for ( String line : token.getProofText().split("[\\r\\n]+") ) {
            String[] items = line.split(":");

            tokenBuilder.startProofLevel(items.length);
            int pos = 0;
            for ( String hash : items ) {
                if ( "X".equals(hash) ) {
                    tokenBuilder.setLevelInheritIndex(pos);
                } else {
                    tokenBuilder.addLevelHash(HashValue.asBytes(hash));
                }
                pos++;
            }
        }

        return tokenBuilder.createToken();
    }

    public static Proof extractProof( Token token ) {
        ProofBuilder pf = new ProofBuilder();
        for ( String line : token.getProofText().split("[\\r\\n]+") ) {
            String[] items = line.split(":");

            pf.newLevel(items.length);
            int pos = 0;
            for ( String hash : items ) {
                if ( "X".equals(hash) ) {
                    pf.setLevelInheritIndex(pos);
                } else {
                    pf.addLevelHash(HashValue.asBytes(hash));
                }
                pos++;
            }
        }
        return pf.buildProof();
    }
}
