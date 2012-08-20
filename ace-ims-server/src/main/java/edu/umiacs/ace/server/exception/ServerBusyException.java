/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.server.exception;

import edu.umiacs.ace.exception.ACEException;
import edu.umiacs.ace.exception.StatusCode;

/**
 *
 * @author mmcgann
 */
public class ServerBusyException extends ACEException
{
    public ServerBusyException()
    {
        super(StatusCode.SERVER_BUSY, "Server busy: Try again");
    }
}
