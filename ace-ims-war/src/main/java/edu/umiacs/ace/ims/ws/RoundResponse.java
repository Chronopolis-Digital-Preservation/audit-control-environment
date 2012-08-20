/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.ws;

import java.util.Date;
import java.util.List;

/**
 *
 * @author toaster
 */
public class RoundResponse
{

    private long witnessId;
    private long roundId;
    private List<ProofElement> proofElements;
    private String tokenClassName;
    private String digestService;
    private String digestProvider;
    private Date roundTimestamp;

    public List<ProofElement> getProofElements()
    {
        return proofElements;
    }

    public long getRoundId()
    {
        return roundId;
    }

    public void setRoundTimestamp(Date roundTimestamp)
    {
        this.roundTimestamp = roundTimestamp;
    }

    public Date getRoundTimestamp()
    {
        return roundTimestamp;
    }

    public void setProofElements(List<ProofElement> proofElements)
    {
        this.proofElements = proofElements;
    }

    public void setRoundId(long roundId)
    {
        this.roundId = roundId;
    }

    public void setWitnessId(long witnessId)
    {
        this.witnessId = witnessId;
    }

    public long getWitnessId()
    {
        return witnessId;
    }

    public void setTokenClassName(String tokenClassName)
    {
        this.tokenClassName = tokenClassName;
    }

    public void setDigestService(String digestService)
    {
        this.digestService = digestService;
    }

    public void setDigestProvider(String digestProvider)
    {
        this.digestProvider = digestProvider;
    }

    public String getTokenClassName()
    {
        return tokenClassName;
    }

    public String getDigestService()
    {
        return digestService;
    }

    public String getDigestProvider()
    {
        return digestProvider;
    }

    
}
