package edu.umiacs.ace.token;

import edu.umiacs.ace.hashtree.ProofHash;
import edu.umiacs.ace.hashtree.ProofNode;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.util.Check;
import edu.umiacs.util.Strings;
import java.io.OutputStream;

/**
 *
 * @author toaster
 */
public class AceTokenWriter extends TokenStoreWriter<AceToken>
{

    public AceTokenWriter(OutputStream os) {
        super(os);
    }

    public void startToken(AceToken token)
    {
        Check.notNull("Token", token);
        setHeaderInformation(token.getDigestType(), token.getIms(), token.getImsService(),
                token.getRound(), token.getDate());

        for ( ProofNode node : token.getProof() ) {
            String[] hashList = new String[node.getHashes().size() + 1];
            hashList[node.getIndex()] = "X";
            for ( ProofHash element : node ) {
                hashList[element.getIndex()] = HashValue.asHexString(element.getHash());
            }
            addHashLevel(Strings.join(':', hashList));
        }
    }
}
