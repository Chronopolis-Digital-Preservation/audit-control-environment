/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.processor;

import java.util.Date;

/**
 *
 * @author mmcgann
 */
public class LinkResponse 
{
    private String previousHash;
    private String rootHash;
    private String tokenClassName;
    private Date timestamp;
    private long roundId;

    public String getPreviousHash()
    {
        return previousHash;
    }

    public void setPreviousHash(String previousHash)
    {
        this.previousHash = previousHash;
    }

    public String getRootHash()
    {
        return rootHash;
    }

    public void setRootHash(String rootHash)
    {
        this.rootHash = rootHash;
    }

    public String getTokenClassName()
    {
        return tokenClassName;
    }

    public void setTokenClassName(String tokenClassName)
    {
        this.tokenClassName = tokenClassName;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }

    public long getRoundId()
    {
        return roundId;
    }

    public void setRoundId(long roundId)
    {
        this.roundId = roundId;
    }

}
