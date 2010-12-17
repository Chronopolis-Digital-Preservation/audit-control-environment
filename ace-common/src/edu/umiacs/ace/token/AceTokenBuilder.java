package edu.umiacs.ace.token;

import edu.umiacs.ace.hashtree.ProofBuilder;
import edu.umiacs.util.Check;
import edu.umiacs.util.Strings;
import java.util.Date;

/**
 *
 * @author toaster
 */
public class AceTokenBuilder {

    private String ims;
    private String digestAlg;
    private String imsService;
    private long round = -1;
    private Date date;
    private ProofBuilder proofBuilder = new ProofBuilder();

    public void reset() {
        ims = null;
        digestAlg = null;
        imsService = null;
        round = -1;
        date = null;
    }

    public void setRound( long round ) {
        this.round = Check.isPositive("Round", round);
    }

    public void setImsService( String imsService ) {
        this.imsService = Check.noWhitespace("IMS Service", imsService);
    }

    public void setIms( String ims ) {
        this.ims = Check.noWhitespace("IMS", ims);
    }

    public void setDigestAlgorithm( String digestAlg ) {
        this.digestAlg = Check.noWhitespace("Digest", digestAlg);
    }

    public void setDate( Date date ) {

        this.date = Check.notNull("Date", date);
    }

    public AceToken createToken() {
        if ( Strings.isEmpty(ims) || Strings.isEmpty(digestAlg) || Strings.isEmpty(imsService)
                || date == null || round == -1 ) {
            throw new IllegalStateException("All token parameters not filled out");
        }

        AceToken token = new AceToken();
        token.setDate(date);
        token.setDigestType(digestAlg);
        token.setIms(ims);
        token.setImsService(imsService);
        token.setRound(round);
        token.setProof(proofBuilder.buildProof());
        return token;
    }

    public void startProofLevel(int hashes)
    {
        proofBuilder.newLevel(hashes);
    }

    public void setLevelInheritIndex(int index)
    {
        proofBuilder.setLevelInheritIndex(index);
    }
    public void addLevelHash(byte[] ... hashes)
    {
        for (byte[] b : hashes)
        {
            proofBuilder.addLevelHash(b);
        }
    }
    /**
     * Add a proof level to the current token. proof levels should be added, starting
     * at bottom of proof tree.
     *
     */
    public void addProofLevel( int index, byte[]... hashes ) {
        proofBuilder.addProofLevel(index, hashes);
    }
}
