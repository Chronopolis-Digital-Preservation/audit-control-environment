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

package edu.umiacs.ace.monitor.core;

import edu.umiacs.ace.hashtree.ProofHash;
import edu.umiacs.ace.hashtree.ProofNode;
import edu.umiacs.util.Strings;
import edu.umiacs.ace.monitor.core.Token;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.util.Check;
import java.util.Date;

/**
 *
 * @author shake
 */
public class TokenBuilder {
    private String digestAlg;
    private String imsService;
    private long round = -1;
    private Date date;
    StringBuilder proof = new StringBuilder();

    public void reset() {
        digestAlg = null;
        imsService = null;
        round = -1;
        date = null;
        proof = new StringBuilder();
    }

    public void setDate(Date date) {
        this.date = Check.notNull("Date", date);
    }

    public void setImsService(String imsService) {
        this.imsService = Check.noWhitespace("IMS Service", imsService);
    }

    public void setDigestAlgorithm(String digestAlg) {
        this.digestAlg = Check.noWhitespace("Digest", digestAlg);
    }

    public void setRound(long round) {
        this.round = Check.isPositive("Round", round);
        
    }

    public void startProofLevel() {
        if ( proof.length() != 0 ) {
            proof.append("\r\n");
        }
    }

    public void addHashLevel(int inheritIdx, ProofNode hashes) {
        int pos = 0;
        int size = hashes.getHashes().size();

        if ( inheritIdx < 0 || inheritIdx > hashes.getHashes().size() ) {
            throw new IllegalArgumentException("Supplied index outside hashes list: "
                    + inheritIdx + "/" + hashes.getHashes().size());
        }

        if ( hashes.getHashes() == null || hashes.getHashes().size() < 1 ) {
            throw new IllegalArgumentException("Hash list must have at least 1 hash");
        }

        for ( ProofHash hash : hashes.getHashes() ) {
            if ( pos == inheritIdx ) {
                proof.append("X:");
            }

            String hashStr = HashValue.asHexString(hash.getHash());
            proof.append(hashStr);

            if ( pos != size-1 ) {
                proof.append(":");
            }
            pos++;
        }

        // Last case -- inheritIdx is equal to the size of the hash list
        if( pos == inheritIdx ) {
            proof.append(":X");
        }
    }

    public void writeProof() {
        if ( Strings.isEmpty(proof) ) {
            //throw Exception
            throw new IllegalStateException("Proof not complete");
        }

        proof.append("\r\n");
    }

    public Token createToken() {
        if ( Strings.isEmpty(proof) || Strings.isEmpty(digestAlg) || Strings.isEmpty(imsService)
                || date == null || round == -1 ) {
            throw new IllegalStateException("All token parameters not filled out");
        }

        Token token = new Token();
        token.setCreateDate(date);
        token.setImsService(imsService);
        token.setRound(round);
        token.setProofAlgorithm(digestAlg);
        // Trim just in case...
        token.setProofText(proof.toString()); //.trim());

        return token;
    }
    
}
