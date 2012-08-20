/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.tokenclass;

import edu.umiacs.ace.digest.DigestFactory;
import edu.umiacs.ace.digest.DigestService;
import edu.umiacs.ace.ims.round.RoundLocal;
import edu.umiacs.ace.ims.round.RoundSummary;
import edu.umiacs.ace.ims.witness.WitnessLocal;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.util.Check;
import edu.umiacs.util.Strings;
import java.security.MessageDigest;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author mmcgann
 */
@Stateless
public class TokenClassBean implements TokenClassLocal 
{
    @PersistenceContext
    private EntityManager em;
    
    @EJB
    private RoundLocal roundSession;
    
    @EJB
    private WitnessLocal witnessSession;
    
    private static final DigestFactory digestFactory = 
            DigestFactory.getInstance();
    
    public TokenClass create(TokenClass tokenClass)
    {
        Check.notNull("tokenClass", tokenClass);
        
        if ( Strings.isEmpty(tokenClass.getName()) )
        {
            throw new InvalidTokenClassException("name", tokenClass.getName());
        }
        if ( Strings.isEmpty(tokenClass.getDigestServiceName()) )
        {
            throw new InvalidTokenClassException("digestServiceName", 
                    tokenClass.getDigestServiceName());
        }
        
        // Check to make sure an actual message digest can be created
        // with the specified provider and service
        DigestService service = digestFactory.getService(
                tokenClass.getDigestProviderName(), 
                tokenClass.getDigestServiceName());
        MessageDigest digest = service.createMessageDigest();
        
        try
        {
            em.persist(tokenClass);
            em.flush();
        }
        catch ( EntityExistsException eee )
        {
            throw new DuplicateTokenClassException(tokenClass);
        }
        
        RoundSummary roundSummary = new RoundSummary();
        roundSummary.setTokenClass(tokenClass);
        roundSummary.setHashValue(HashValue.asHexString(digest.digest()));
        roundSession.seedRoundSummary(roundSummary);
        witnessSession.seedWitness(roundSummary);
        
        return tokenClass;
    }

    @SuppressWarnings("unchecked")
    public List<TokenClass> list()
    {
        return em.createNamedQuery("TokenClass.list").getResultList();        
    }
    
    public TokenClass findByName(String name)
    {
        Check.notEmpty("name", name);
        
        Query q = em.createNamedQuery("TokenClass.findByName");
        q.setParameter("name", name);
        return (TokenClass)q.getSingleResult();
    }
 
}
