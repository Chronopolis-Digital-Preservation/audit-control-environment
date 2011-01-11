/*
 * Copyright (c) 2007-2010, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ACE Components were written in the ADAPT Project at the University of
 * Maryland Institute for Advanced Computer Study.
 */
package edu.umiacs.ace.hashtree;

import edu.umiacs.util.Check;
import java.util.ArrayList;
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
