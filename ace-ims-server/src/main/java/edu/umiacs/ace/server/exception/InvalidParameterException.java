/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.server.exception;

import edu.umiacs.ace.exception.*;
import edu.umiacs.util.Strings;

/**
 *
 * @author mmcgann
 */
public class InvalidParameterException extends ACEException 
{
    private String name;
    private String value;
    
    public InvalidParameterException(int statusCode, String name, Object value) 
    {
        super(statusCode, messageText(name, value));
        this.name = name;
        this.value = ( value == null ) ? null : value.toString();
    }

    public InvalidParameterException(String name)
    {
        this(StatusCode.INVALID_PARAMETER, name, null);
    }
    
    private static String messageText(String name, Object value)
    {
        return ( Strings.isEmpty(value) ) 
                ? "Missing value for parameter: " + name
                : "Invalid value for " + name + ": " + value;
    }

    public String getName()
    {
        return name;
    }
    
    public Object getValue()
    {
        return value;
    }
         
}
