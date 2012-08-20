/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
//import org.apache.log4j.Logger;

/**
 *
 * @author mmcgann
 */
public class ServiceLocator 
{
    private InitialContext context; 
    private Map<Class<?>,Object> services =
            Collections.synchronizedMap(new HashMap<Class<?>,Object>());
    private static final ServiceLocator instance;
//    private static final Logger print = Logger.getLogger(ServiceLocator.class);
    
    static
    {
        try
        {
            instance = new ServiceLocator();
        }
        catch ( NamingException ne )
        {
            throw new IllegalStateException(
                    "Unable to create service locator: " + ne.getMessage(), ne);
        }
    }
    
    private ServiceLocator() throws NamingException
    {
        context = new InitialContext();
    }
    
    public static ServiceLocator getInstance()
    {
        return instance;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getLocal(Class<T> localInterface)
    {
        try
        {
            Object local = services.get(localInterface);
            if ( local == null )
            {
                String jndiName = nameOf(localInterface);
//                print.debug("JNDI lookup: " + jndiName);
                local = context.lookup(jndiName);
                if ( local != null )
                {
                    services.put(localInterface, local);
                }
                else
                {
                    throw new NamingException("Returned object was null");
                }
            }
            return (T)local;
        }
        catch ( NamingException ne )
        {
            throw new IllegalArgumentException("No name for local interface: " + 
                    localInterface, ne);
        }
    }
    
    private String nameOf(Class<?> localInterface)
    {
        String name = localInterface.getSimpleName()
                .replaceAll("Local$", "Bean");
        return "java:comp/env/" + name;
    }
}

