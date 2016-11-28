/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.witness;

import edu.umiacs.ace.digest.DigestFactory;
import edu.umiacs.ace.digest.DigestService;
import edu.umiacs.ace.exception.StatusCode;
import edu.umiacs.ace.hashtree.HashTree;
import edu.umiacs.ace.hashtree.HashTreeBuilder;
import edu.umiacs.ace.ims.round.RoundSummary;
import edu.umiacs.ace.ims.tokenclass.TokenClass;
import edu.umiacs.ace.ims.tokenclass.TokenClassLocal;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.sql.SQL;
import edu.umiacs.util.Check;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.sql.DataSource;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author mmcgann
 */
@Stateless
public class WitnessBean implements WitnessLocal
{

    @Resource(name = "db")
    private DataSource db;
    @PersistenceContext
    private EntityManager em;
    @EJB
    private TokenClassLocal tokenClassSession;

    public Witness seedWitness(RoundSummary roundSummary)
    {
        Check.notNull("roundSummary", roundSummary);
        TokenClass tokenClass = roundSummary.getTokenClass();
        Check.notNull("roundSummary.tokenClass", tokenClass);

        Witness witness = new Witness();
        witness.setTokenClass(tokenClass);
        witness.setStartRound(roundSummary);
        witness.setEndRound(roundSummary);
        witness.setRoundCount(1);
        witness.setHashValue(roundSummary.getHashValue());

        PreviousWitness previousWitness = new PreviousWitness();
        previousWitness.setTokenClass(tokenClass);
        previousWitness.setWitness(witness);

        try
        {
            em.persist(witness);
            em.persist(previousWitness);
            em.flush();
        }
        catch ( EntityExistsException eee )
        {
            throw new PreviousWitnessException(StatusCode.WITNESS_SEED_EXISTS,
                    "Seed already exists while initializing token class: " +
                    tokenClass.getName(), tokenClass.getName());
        }
        return witness;
    }

    /**
     * Generate a witness for the given token class. The witness is based on all
     * tokens created since the last witness. 
     * 
     * @param tokenClassName
     * @return
     */
    public Witness publishWitness(String tokenClassName)
    {
        Check.notEmpty("tokenClassName", tokenClassName);

        TokenClass tokenClass = tokenClassSession.findByName(tokenClassName);

        Query q = em.createNamedQuery("PreviousWitness.findByTokenClass");
        q.setParameter("tokenClass", tokenClass);
        PreviousWitness previousWitness = null;
        try
        {
            previousWitness = (PreviousWitness) q.getSingleResult();
        }
        catch ( NoResultException nre )
        {
            throw new PreviousWitnessException(StatusCode.NO_PREVIOUS_WITNESS,
                    "No previous witness for token class: " +
                    tokenClass.getName(), tokenClass.getName());
        }

        DigestFactory digestFactory = DigestFactory.getInstance();
        DigestService digestService = digestFactory.getService(
                tokenClass.getDigestProviderName(),
                tokenClass.getDigestServiceName());
        MessageDigest digest = digestService.createMessageDigest();
        HashTreeBuilder htb = new HashTreeBuilder(digest,
                tokenClass.getTreeOrder());

        Connection conn = null;
        PreparedStatement stmt = null;

        long firstId = -1;
        long currentId = -1;
        int count = 0;

        try
        {
            conn = db.getConnection();
            stmt = conn.prepareStatement(
                    "select id, tokenClass_id, hash_value " +
                    "from round_summary " +
                    "where tokenClass_id = ? and id > ? " +
                    "order by id");
            stmt.setLong(1, tokenClass.getId());
            stmt.setLong(2, previousWitness.getWitness().getEndRound().getId());
            ResultSet rs = stmt.executeQuery();

            while ( rs.next() )
            {
                currentId = rs.getLong("id");
                if ( firstId == -1 )
                {
                    firstId = currentId;
                }
                htb.add(HashValue.asBytes(rs.getString("hash_value")));
                count++;
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch ( SQLException se )
        {
            throw new PersistenceException(se.getMessage(), se);
        }
        finally
        {
            SQL.release(stmt);
            SQL.release(conn);
        }
        htb.link(HashValue.asBytes(
                previousWitness.getWitness().getHashValue()));
        HashTree hashTree = htb.build();
        String rootHash = HashValue.asHexString(
                hashTree.getRootNode().getHash());


        RoundSummary rsFirst = em.find(RoundSummary.class, firstId);
        RoundSummary rsLast = em.find(RoundSummary.class, currentId);
        Witness newWitness = new Witness();
        newWitness.setTokenClass(tokenClass);
        newWitness.setStartRound(rsFirst);
        newWitness.setEndRound(rsLast);
        newWitness.setHashValue(rootHash);
        newWitness.setRoundCount(count);

        em.persist(newWitness);
        previousWitness.setWitness(newWitness);

        return newWitness;
    }

    /**
     * Regenerate a witness proof tree for a given token class that includes
     * the round range that was described
     * 
     * @return
     */
    public HashTree generateWitnessHashTree(Witness witness)
    {
        Check.notNull("witness", witness);

        TokenClass tokenClass = witness.getTokenClass();
        Witness previousWitness;

        DigestFactory digestFactory = DigestFactory.getInstance();
        DigestService digestService = digestFactory.getService(
                tokenClass.getDigestProviderName(),
                tokenClass.getDigestServiceName());
        MessageDigest digest = digestService.createMessageDigest();
        HashTreeBuilder htb = new HashTreeBuilder(digest,
                tokenClass.getTreeOrder());

        Query q = em.createNamedQuery("RoundSummary.selectRoundRange");
        q.setParameter("tokenClass", tokenClass);
        q.setParameter("start", witness.getStartRound().getId());
        q.setParameter("finish", witness.getEndRound().getId());

        List results = q.getResultList();

        for ( Object o : results )
        {
            RoundSummary rs = (RoundSummary) o;
            htb.add(HashValue.asBytes(rs.getHashValue()));
        }
        
        q = em.createNamedQuery("Witness.getPreviousWitness");
        q.setParameter("witness", witness.getId());
        q.setParameter("token", witness.getTokenClass());
        q.setMaxResults(1);
        previousWitness = (Witness) q.getSingleResult();
        
        htb.link(HashValue.asBytes(
                previousWitness.getHashValue()));
        return htb.build();

    }

    public Witness getWitnessForRound(RoundSummary round)
    {
        Query q = em.createNamedQuery("Witness.getWitnessForRound");
        q.setParameter("id", round.getId());
        q.setParameter("token", round.getTokenClass());
        return (Witness) q.getSingleResult();
                
    }
}
