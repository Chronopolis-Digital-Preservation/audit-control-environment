/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.store;

import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

/**
 *
 * @author mmcgann
 */
@Stateless
public class TokenStoreBean implements TokenStoreLocal
{
    @PersistenceContext
    private EntityManager em;
    
    private Random random = new Random();

    public TokenStore createTokenStore(int numTokens)
    {
        TokenStore tokenStore = new TokenStore();
        tokenStore.setSessionKey(random.nextLong());
        tokenStore.setTimestamp(new Date());
        tokenStore.setTokenCount(numTokens);
        em.persist(tokenStore);
        return tokenStore;
    }

    public void addTokenStoreItems(List<IssuedToken> tokens)
    {
        for ( IssuedToken token : tokens )
        {
            em.persist(token);
        }
    }

    public List<IssuedToken> retreiveTokens(long requestNumber, long sessionKey)
    {
        Query q = em.createNamedQuery("TokenStore.find");
        q.setParameter("id", requestNumber);
        q.setParameter("sessionKey", sessionKey);
        TokenStore tokenStore = null;
        try
        {
            tokenStore = (TokenStore)q.getSingleResult();
        }
        catch ( NoResultException nre )
        {
            throw new TokenStoreExpiredException(requestNumber);
        }

        q = em.createNamedQuery("IssuedTokens.findByTokenStore");
        q.setParameter("tokenStore", tokenStore);
        List<IssuedToken> issuedTokens = q.getResultList();
        if ( issuedTokens.size() != tokenStore.getTokenCount() )
        {
            throw new TokenStoreNotReadyException(requestNumber);
        }
        return issuedTokens;
    }
    
    public void deleteOldTokens(Date beforeDate)
    {

        Query removeIssued = em.createNamedQuery("TokenStore.deleteOldIssuedTokens");
        removeIssued.setParameter("time", beforeDate, TemporalType.TIMESTAMP);

        Query removeStores = em.createNamedQuery("TokenStore.deleteOldTokenStores");
        removeStores.setParameter("time", beforeDate, TemporalType.TIMESTAMP);

        removeIssued.executeUpdate();
        removeStores.executeUpdate();
    }
}
