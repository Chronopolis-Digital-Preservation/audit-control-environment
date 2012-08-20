/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.processor;

import edu.umiacs.ace.ims.ws.ProofElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author mmcgann
 */
public class TokenResponse implements Serializable
{
    private String name;
    private String tokenClassName;
    private String digestService;
    private String digestProvider;
    private int statusCode;
    private long roundId;
    private Date timestamp;
    private List<ProofElement> proofElements;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
    }

    public long getRoundId()
    {
        return roundId;
    }

    public void setRoundId(long roundId)
    {
        this.roundId = roundId;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }

    public List<ProofElement> getProofElements()
    {
        return proofElements;
    }

    public void setProofElements(List<ProofElement> proofElements)
    {
        this.proofElements = proofElements;
    }

    public String getTokenClassName()
    {
        return tokenClassName;
    }

    public void setTokenClassName(String tokenClassName)
    {
        this.tokenClassName = tokenClassName;
    }

    public String getDigestService()
    {
        return digestService;
    }

    public void setDigestService(String digestService)
    {
        this.digestService = digestService;
    }

    public String getDigestProvider()
    {
        return digestProvider;
    }

    public void setDigestProvider(String digestProvider)
    {
        this.digestProvider = digestProvider;
    }



}
