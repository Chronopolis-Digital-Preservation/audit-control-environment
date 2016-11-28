/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.server.exception;

import edu.umiacs.ace.exception.*;

/**
 *
 * @author mmcgann
 */
public class ACEFault extends Exception 
{
    private int statusCode;
    private boolean serverFault = false;
    
    /**
     * Constructs an instance of <code>ACEFault</code> with the specified detail message.
     */
    public ACEFault(Exception e) 
    {
        super(getMessageText(e));
        statusCode = getStatusCode(e);
        serverFault = statusCode >= StatusCode.SERVER_FAULT_START_NUMBER;
    }
    
    private static String getMessageText(Exception e)
    {
        if ( e instanceof ACEException )
        {
            return e.getMessage();
        }
        return "Internal error";
    }
    
    private static int getStatusCode(Exception e)
    {
        if ( e instanceof ACEException )
        {
            return ((ACEException)e).getStatusCode();
        }
        return StatusCode.INTERNAL_ERROR;
    }
    
    public int getStatusCode()
    {
        return statusCode;
    }
    
    public boolean isServerFault()
    {
        return serverFault;
    }
    
}
