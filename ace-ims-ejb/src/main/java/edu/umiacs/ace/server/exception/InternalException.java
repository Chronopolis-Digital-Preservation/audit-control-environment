/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.server.exception;

import edu.umiacs.ace.exception.ACEException;

/**
 *
 * @author mmcgann
 */
public class InternalException extends ACEException 
{

    public InternalException(int statusCode, String message, 
            Throwable throwable)
    {
        super(statusCode, message, throwable);
    }
    
    public InternalException(int statusCode, String message)
    {
        super(statusCode, message);
    }
}
