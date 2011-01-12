package edu.umiacs.ace.util;

import edu.umiacs.ace.monitor.core.Token;
import edu.umiacs.ace.token.TokenStoreWriter;
import java.io.OutputStream;

/**
 *
 * @author toaster
 */
public class DbTokenWriter extends TokenStoreWriter<Token> {

    private String ims;

    public DbTokenWriter( String ims, OutputStream os ) {
        super(os);
        this.ims = ims;
    }

    @Override
    public void startToken( Token token ) {

        setHeaderInformation(token.getProofAlgorithm(), ims, token.getImsService(),
                token.getRound(), token.getCreateDate());
        String proof = token.getProofText();
        for ( String line : proof.split("[\\r\\n]+") ) {
            addHashLevel(line);
        }
    }
}
