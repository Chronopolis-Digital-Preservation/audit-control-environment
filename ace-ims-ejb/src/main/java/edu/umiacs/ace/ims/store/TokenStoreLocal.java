/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.store;

import java.util.Date;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author mmcgann
 */
@Local
public interface TokenStoreLocal
{
    TokenStore createTokenStore(int numTokens);

    void addTokenStoreItems(List<IssuedToken> tokens);

    List<IssuedToken> retreiveTokens(long requestNumber, long sessionKey);
    
    public void deleteOldTokens(Date beforeDate);
}
