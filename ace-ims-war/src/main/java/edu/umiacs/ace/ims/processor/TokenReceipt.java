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
public class TokenReceipt 
{
    private long requestNumber;
    private long sessionKey;
    private Date now;
    private Date readyAt;

    public long getRequestNumber()
    {
        return requestNumber;
    }

    public void setRequestNumber(long requestNumber)
    {
        this.requestNumber = requestNumber;
    }

    public long getSessionKey()
    {
        return sessionKey;
    }

    public void setSessionKey(long sessionKey)
    {
        this.sessionKey = sessionKey;
    }

    public Date getNow()
    {
        return now;
    }

    public void setNow(Date now)
    {
        this.now = now;
    }

    public Date getReadyAt()
    {
        return readyAt;
    }

    public void setReadyAt(Date readyAt)
    {
        this.readyAt = readyAt;
    }
}
