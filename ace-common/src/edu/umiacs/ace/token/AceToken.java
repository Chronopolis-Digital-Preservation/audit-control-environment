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
