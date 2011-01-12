/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.api;

import edu.umiacs.ace.ims.ws.ProofElement;
import edu.umiacs.ace.ims.ws.TokenResponse;
import edu.umiacs.ace.token.TokenStoreWriter;
import java.io.OutputStream;

/**
 *
 * @author toaster
 */
public class TokenResponseStoreWriter extends TokenStoreWriter<TokenResponse> {

    private String ims;

    public TokenResponseStoreWriter( OutputStream os, String ims ) {
        super(os);
        this.ims = ims;
    }

    @Override
    public void startToken( TokenResponse token ) {
        setHeaderInformation(token.getDigestService(), ims,
                token.getTokenClassName(), token.getRoundId(), token.getTimestamp());

        for ( ProofElement pe : token.getProofElements() ) {
            addHashLevel(pe.getIndex(), pe.getHashes());
        }
    }
}
