/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.tokenclass;

import edu.umiacs.ace.server.exception.InvalidParameterException;
import edu.umiacs.ace.exception.StatusCode;
import javax.ejb.ApplicationException;

/**
 *
 * @author mmcgann
 */
@ApplicationException(rollback=true)
public class InvalidTokenClassException extends InvalidParameterException 
{
    public InvalidTokenClassException(String name, Object value)
    {
        super(StatusCode.INVALID_TOKEN_CLASS, name, value);
    }
}
