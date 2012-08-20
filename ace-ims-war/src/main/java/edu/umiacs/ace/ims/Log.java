/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims;

import org.apache.log4j.Logger;

/**
 *
 * @author mmcgann
 */
public class Log 
{
    private static final Logger logSys = Logger.getLogger("ace.ims.system");
    private static final Logger logError = Logger.getLogger("ace.ims.error");
    
    public static final void system(Object message)
    {
        logSys.info(message);
    }
    
    public static final void error(Object message)
    {
        logSys.error(message);
    }
    
    public static final void error(Object message, Throwable throwable)
    {
        logSys.error(message);
        logError.error(message, throwable);
    }
}
