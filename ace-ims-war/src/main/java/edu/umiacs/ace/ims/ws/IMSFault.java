/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.ws;

import edu.umiacs.ace.exception.ACEException;
import edu.umiacs.ace.ims.round.RoundLocal;
import edu.umiacs.ace.server.exception.ACEFault;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author mmcgann
 */
public class IMSFault extends ACEFault
{

    public IMSFault(ACEException e)
    {
        super(e);
    }
    
    public IMSFault(Exception e)
    {
        super(e);
        
    }
}
