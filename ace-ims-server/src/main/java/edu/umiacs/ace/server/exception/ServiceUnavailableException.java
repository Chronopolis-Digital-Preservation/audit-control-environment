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
public class ServiceUnavailableException extends ACEException
{
    public ServiceUnavailableException(int statusCode)
    {
        super(statusCode, "Service unavailable");
    }
    
}
