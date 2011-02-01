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

import edu.umiacs.ace.hashtree.Proof;
import java.util.Date;

/**
 *
 * @author toaster
 */
public class AceToken {

    private String digestType;
    private String imsService;
    private String ims;
    private Date date;
    private long round;
    private Proof tokenProof;

    AceToken() {
    }

    void setDate( Date date ) {
        this.date = date;
    }

    void setDigestType( String digestType ) {
        this.digestType = digestType;
    }

    void setIms( String ims ) {
        this.ims = ims;
    }

    void setImsService( String imsService ) {
        this.imsService = imsService;
    }

    void setRound( long round ) {
        this.round = round;
    }

    void setProof( Proof tokenProof ) {
        this.tokenProof = tokenProof;
    }

    public Date getDate() {
        return date;
    }

    public String getDigestType() {
        return digestType;
    }

    public String getIms() {
        return ims;
    }

    public String getImsService() {
        return imsService;
    }

    public long getRound() {
        return round;
    }

    public Proof getProof() {
        return tokenProof;
    }

    @Override
    public String toString() {
        return (imsService + "("+date+"/"+round+")@" + ims + " "+digestType+":"+tokenProof);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.digestType != null ? this.digestType.hashCode() : 0);
        hash = 37 * hash + (int) (this.round ^ (this.round >>> 32));
        hash = 37 * hash + (this.tokenProof != null ? this.tokenProof.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final AceToken other = (AceToken) obj;
        if ( (this.digestType == null) ? (other.digestType != null)
                : !this.digestType.equals(other.digestType) ) {
            return false;
        }
        if ( this.round != other.round ) {
            return false;
        }
        if ( this.tokenProof != other.tokenProof &&
                (this.tokenProof == null || !this.tokenProof.equals(other.tokenProof)) ) {
            return false;
        }
        return true;
    }


}
