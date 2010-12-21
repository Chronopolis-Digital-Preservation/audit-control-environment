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
