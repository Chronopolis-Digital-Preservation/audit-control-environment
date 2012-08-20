/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.round;

import edu.umiacs.ace.server.exception.InternalException;
import javax.ejb.ApplicationException;

/**
 *
 * @author mmcgann
 */
@ApplicationException(rollback=true)
public class PreviousRoundException extends InternalException 
{
    public PreviousRoundException(int statusCode, String message)
    {
        super(statusCode, message);
    }
}
