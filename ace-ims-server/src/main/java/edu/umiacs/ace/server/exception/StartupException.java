/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.server.exception;

/**
 *
 * @author mmcgann
 */
public class StartupException extends RuntimeException
{
    public StartupException(String message, Throwable throwable)
    {
        super("Context failed to start: " + message, throwable);
    }
    
    public StartupException(String message)
    {
        this(message, null);
    }
}
