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
// $Id$
package edu.umiacs.ace.hashtree;

import edu.umiacs.util.Check;
import java.security.MessageDigest;
import java.util.Arrays;

/*******************************************************************************
 *
 *
 * @version {@code $Revision$ $Date$}
 *
 ******************************************************************************/
public final class ProofValidator {

    public ProofValidator() {
    }

    public byte[] rootHash( MessageDigest digest, Proof proof ) {
        return rootHash(digest, proof, null);
    }

    public byte[] rootHash( MessageDigest digest, Proof proof, byte[] leafHash ) {
        Check.notNull("digest", digest);
        Check.notNull("proof", proof);

        if ( leafHash == null ) {
            leafHash = proof.getLeafHash();
            if ( leafHash == null ) {
                throw new IllegalArgumentException("leafHash not specified");
            }
        }

        byte[] currentHash = leafHash;
        for ( ProofNode node : proof ) {
            byte[][] levelHashes = new byte[node.getHashes().size() + 1][];

            if ( node.getIndex() < 0 || node.getIndex() >= levelHashes.length ) {
                throw new IllegalStateException("Invalid node index");
            }
            levelHashes[node.getIndex()] = currentHash;

            for ( ProofHash proofHash : node ) {
                if ( proofHash.getIndex() < 0 || proofHash.getIndex() >= levelHashes.length ) {
                    throw new IllegalStateException("Invalid proofHash index");
                }
                levelHashes[proofHash.getIndex()] = proofHash.getHash();
            }
            for ( byte[] hash : levelHashes ) {
                if ( hash == null ) {
                    throw new IllegalStateException("hash is null");
                }
                digest.update(hash);
            }
            currentHash = digest.digest();
        }
        return currentHash;
    }

    public boolean validate( MessageDigest digest, Proof proof, byte[] rootHash ) {
        return Arrays.equals(rootHash, rootHash(digest, proof));
    }

    public boolean validate( MessageDigest digest, Proof proof, byte[] rootHash,
            byte[] leafHash ) {
        return Arrays.equals(rootHash, rootHash(digest, proof, leafHash));
    }
}
