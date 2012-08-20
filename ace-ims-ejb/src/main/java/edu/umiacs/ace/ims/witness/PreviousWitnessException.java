/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.witness;

import edu.umiacs.ace.server.exception.InternalException;
import javax.ejb.ApplicationException;

/**
 *
 * @author mmcgann
 */
@ApplicationException(rollback=true)
public class PreviousWitnessException extends InternalException 
{
    private String tokenClassName;
    
    public PreviousWitnessException(int statusCode, String message, 
            String tokenClassName) 
    {
        super(statusCode, message);
        this.tokenClassName = tokenClassName;
    }
    
    public String getTokenClassName()
    {
        return tokenClassName;
    }     
}
