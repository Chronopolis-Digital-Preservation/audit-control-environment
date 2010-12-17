package edu.umiacs.ace.hashtree;

import edu.umiacs.util.Check;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Build a new Proof from supplied elements.
 *
 * Note, the returned proof will only have proof elements, extra
 * metadata will be null.
 *
 * Currently, this is NOT threadsafe.
 *
 * @author toaster
 */
public class ProofBuilder {

    private String algorithm;
    private String provider;
    private List<ProofNode> elements;// = new LinkedList<ProofNode>();
    private int inheritIndex;
    private int writeIdx;
    private byte[][] hashes;

    public ProofBuilder() {
    }

    /**
     * start new level
     *
     * @param levelSize total hashes in this level (including inherited digest)
     */
    public void newLevel( int levelSize ) {
        if ( hashes != null ) {
            writeLevel();
        }

        this.hashes = new byte[levelSize - 1][];
        writeIdx = 0;
    }

    public void setLevelInheritIndex( int suppliedIndex ) {
        if ( suppliedIndex < 0 || suppliedIndex > hashes.length ) {
            throw new IllegalArgumentException("supplied index outside hashes list: "
                    + suppliedIndex + " / " + hashes.length);
        }
        inheritIndex = suppliedIndex;
    }

    public void addLevelHash( byte[] hash ) {
        if ( hashes == null ) {
            throw new IllegalStateException("level not open (use newLevel)");
        }
        Check.notNull("Hash", hash);

        if ( writeIdx >= hashes.length ) {
            throw new IllegalStateException("Attempting to add hash past round end: " + writeIdx);
        }

        hashes[writeIdx] = hash;
        writeIdx++;
    }

    /**
     * 
     * @param suppliedIndex
     * @param hashes
     */
    public void addProofLevel( int suppliedIndex, byte[]... hashes ) {

        if ( hashes == null || hashes.length < 1 ) {
            throw new IllegalArgumentException("hashe list must contain at leaset 1 hash");
        }

        newLevel(hashes.length + 1);
        setLevelInheritIndex(suppliedIndex);
        for ( byte[] b : hashes ) {
            addLevelHash(b);
        }
        writeLevel();

    }

    private void writeLevel() {
        if ( inheritIndex == -1 ) {
            throw new IllegalStateException("Inherit index not set for this level");
        }

        if ( hashes.length != writeIdx ) {
            throw new IllegalStateException("Level contains empty hashes, cannot assemble, idx: "
                    + writeIdx + "/" + hashes.length);
        }

        List<ProofHash> hashList = new ArrayList<ProofHash>(hashes.length);

        for ( int i = 0; i < hashes.length; i++ ) {
            int location = (i >= inheritIndex ? i + 1 : i);
            hashList.add(new ProofHash(hashes[i], location));
        }

        ProofNode pn = new ProofNode(hashList, inheritIndex);

        if ( elements == null ) {
            elements = new ArrayList<ProofNode>(5);
        }

        elements.add(pn);

        hashes = null;
        inheritIndex = -1;

    }

    public void setAlgorithm( String algorithm ) {
        this.algorithm = algorithm;
    }

    public void setProvider( String provider ) {
        this.provider = provider;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getProvider() {
        return provider;
    }

    public Proof buildProof() {

        if ( hashes != null ) {
            writeLevel();
        }

        if ( elements == null ) {
            throw new IllegalStateException("No proof elements added");
        }

        Proof newProof = new Proof(algorithm, provider, null, elements);

        elements = null;

        return newProof;

    }
}
