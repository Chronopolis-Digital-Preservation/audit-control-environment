/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.store;

import edu.umiacs.ace.exception.ACEException;
import edu.umiacs.ace.exception.StatusCode;
import javax.ejb.ApplicationException;

/**
 *
 * @author mmcgann
 */
@ApplicationException(rollback=false)
public class TokenStoreExpiredException extends ACEException
{
    private long requestNumber;
    
    public TokenStoreExpiredException(long requestNumber)
    {
        super(StatusCode.TOKEN_STORE_EXPIRED, "Token store expired: " + 
                requestNumber);
        this.requestNumber = requestNumber;
    }
    
    public long getRequestNumber()
    {
        return requestNumber;
    }
}
