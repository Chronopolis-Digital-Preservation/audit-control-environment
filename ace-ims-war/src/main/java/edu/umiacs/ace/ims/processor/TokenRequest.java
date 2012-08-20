/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.processor;

/**
 *
 * @author mmcgann
 */
public class TokenRequest 
{
    private String name;
    private String hashValue;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getHashValue()
    {
        return hashValue;
    }

    public void setHashValue(String hashValue)
    {
        this.hashValue = hashValue;
    }
    
}
