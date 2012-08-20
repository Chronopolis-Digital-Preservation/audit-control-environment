/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.tokenclass;

import edu.umiacs.ace.exception.StatusCode;
import edu.umiacs.ace.server.exception.KeyViolationException;
import javax.ejb.ApplicationException;

/**
 *
 * @author mmcgann
 */
@ApplicationException(rollback=true)
public class DuplicateTokenClassException extends KeyViolationException 
{

    public DuplicateTokenClassException(TokenClass tc)
    {
        super(StatusCode.DUPLICATE_TOKEN_CLASS, "Token class already exists: " + 
                tc.getName(), TokenClass.class, "name", tc.getName());
    }
}
