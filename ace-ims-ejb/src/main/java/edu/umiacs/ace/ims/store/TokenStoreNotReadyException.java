/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.store;

import edu.umiacs.ace.exception.ACEException;
import edu.umiacs.ace.exception.StatusCode;

/**
 *
 * @author mmcgann
 */
public class TokenStoreNotReadyException extends ACEException 
{
    private long requestNumber;
    
    public TokenStoreNotReadyException(long requestNumber)
    {
        super(StatusCode.TOKEN_STORE_NOT_READY, "Token store not ready: " + 
                requestNumber);
        this.requestNumber = requestNumber;
    }
    
    public long getRequestNumber()
    {
        return requestNumber;
    }
}
