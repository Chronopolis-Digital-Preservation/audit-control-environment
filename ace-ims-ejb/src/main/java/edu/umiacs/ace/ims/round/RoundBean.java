/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.round;

import edu.umiacs.ace.exception.StatusCode;
import edu.umiacs.ace.ims.tokenclass.TokenClass;
import edu.umiacs.ace.server.exception.InternalException;
import edu.umiacs.util.Check;
import java.util.Date;
import javax.ejb.Stateless;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author mmcgann
 */
@Stateless
public class RoundBean implements RoundLocal 
{
    @PersistenceContext
    private EntityManager em;
    
    public PreviousRound seedRoundSummary(RoundSummary roundSummary)
    {
        Check.notNull("roundSummary", roundSummary);
        TokenClass tokenClass = roundSummary.getTokenClass();
        Check.notNull("roundSummary.tokenClass", tokenClass);
        
        roundSummary.setTimestamp(new Date());

        PreviousRound previousRound = new PreviousRound();
        previousRound.setRoundSummary(roundSummary);
        previousRound.setTokenClass(tokenClass);
        
        try
        {
            em.persist(roundSummary);        
            em.persist(previousRound);
            em.flush();
        }
        catch ( EntityExistsException eee )
        {
            throw new PreviousRoundException(StatusCode.ROUND_SEED_EXISTS, 
                    "Seed round already exists for token class: " + 
                    tokenClass.getName());
        }
        return previousRound;
    }
    
    public PreviousRound findPreviousRound(TokenClass tokenClass)
    {
        Check.notNull("tokenClass", tokenClass);
        
        try
        {
            Query q = em.createNamedQuery("PreviousRound.findByTokenClass");
            q.setParameter("tokenClass", tokenClass);
            return (PreviousRound)q.getSingleResult();
        }
        catch ( NoResultException nre )
        {
            throw new PreviousRoundException(StatusCode.NO_PREVIOUS_ROUND,
                    "No previous round for: " + tokenClass.getName());
        }
    }
    
    public RoundSummary createRoundSummary(RoundSummary roundSummary)
    {
        Check.notNull("roundSummary", roundSummary);
        Check.notEmpty("hashValue", roundSummary.getHashValue());
        
        roundSummary.setTimestamp(new Date());
        em.persist(roundSummary);
        PreviousRound previousRound = findPreviousRound(
                roundSummary.getTokenClass());
        previousRound.setRoundSummary(roundSummary);
        return roundSummary;
    }
    
    public RoundSummary getRoundSummary(long roundId)
    {
        RoundSummary rs;
        rs = em.find(RoundSummary.class, roundId);
        return rs;
    }
}    


