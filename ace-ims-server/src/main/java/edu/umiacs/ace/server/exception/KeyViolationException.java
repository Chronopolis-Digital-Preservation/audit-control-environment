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
public class KeyViolationException extends ACEException 
{
    private String entityClassName;
    private String name;
    private String value;
    
    
    public KeyViolationException(int statusCode, String message, 
            Class<?> entityClass, String name, Object value)
    {
        super(statusCode, message);
        this.entityClassName = entityClass.getName();
        this.name = name;
        this.value = ( value == null ) ? null : value.toString();
    }
    
    public String getEntityClassName()
    {
        return entityClassName;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getValue()
    {
        return value;
    }
    
}
