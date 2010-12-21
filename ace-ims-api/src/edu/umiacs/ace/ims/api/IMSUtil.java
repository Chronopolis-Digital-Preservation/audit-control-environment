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
package edu.umiacs.ace.ims.api;

import edu.umiacs.ace.ims.ws.ProofElement;
import edu.umiacs.ace.ims.ws.TokenResponse;
import edu.umiacs.ace.token.AceToken;
import edu.umiacs.ace.token.AceTokenBuilder;
import edu.umiacs.ace.util.HashValue;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public class IMSUtil {

    private static final Logger LOG = Logger.getLogger(IMSUtil.class);
    private static ThreadLocal<AceTokenBuilder> tokenBuilder = new ThreadLocal<AceTokenBuilder>()
    {

        @Override
        protected AceTokenBuilder initialValue() {
            return new AceTokenBuilder();
        }

    };

    public static String formatProof(TokenResponse resp)
    {
         StringBuilder proof = new StringBuilder();
        
        for (ProofElement pe : resp.getProofElements())
        {
            proof.append(formatHashLevel(pe));
            proof.append('\n');
        }
        return proof.toString();
    }

    public static String formatHashLevel(ProofElement pe)
    {
        int inheritIdx = pe.getIndex();
        List<String> hashes = pe.getHashes();
//        LOG.trace("received "+ inheritIdx + " " + hashes.toString());
        
        StringBuilder level = new StringBuilder();
        int i = 0;

        for (; i < hashes.size(); i++)
        {
            if (i == inheritIdx)
            {
                level.append("X:");
            }
            level.append(hashes.get(i));
            if (i != (hashes.size() -1))
                level.append(':');
        }

        if (inheritIdx == i)
            level.append(":X");

//        LOG.trace("converted "+ level.toString());
        return level.toString();

    }

//    public static TokenResponse convertToken(AceToken token) {
//
//        TokenResponse response = new TokenResponse();
//        response.setTimestamp()
//        bldr.setDate(response.getTimestamp().toGregorianCalendar().getTime());
//
//        bldr.setDigestAlgorithm(response.getDigestService());
//        bldr.setRound(response.getRoundId());
//        bldr.setIms(ims);
//        bldr.setImsService(response.getTokenClassName());
//
//        for (ProofElement pe : response.getProofElements())
//        {
//            bldr.startProofLevel(pe.getHashes().size() + 1);
//            bldr.setLevelInheritIndex(pe.getIndex());
//
//            for (String hash : pe.getHashes())
//            {
//                bldr.addLevelHash(HashValue.asBytes(hash));
//            }
//
//        }
////        return bldr.createToken();
//        return response;
//    }

    public static AceToken convertResponse(TokenResponse response, String ims) {
        
        AceTokenBuilder bldr = tokenBuilder.get();
        bldr.setDate(response.getTimestamp().toGregorianCalendar().getTime());
        bldr.setDigestAlgorithm(response.getDigestService());
        bldr.setRound(response.getRoundId());
        bldr.setIms(ims);
        bldr.setImsService(response.getTokenClassName());

        for (ProofElement pe : response.getProofElements())
        {
            bldr.startProofLevel(pe.getHashes().size() + 1);
            bldr.setLevelInheritIndex(pe.getIndex());
            
            for (String hash : pe.getHashes())
            {
                bldr.addLevelHash(HashValue.asBytes(hash));
            }
            
        }
        return bldr.createToken();
    }
}
