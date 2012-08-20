/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.processor;

import edu.umiacs.ace.exception.StatusCode;
import edu.umiacs.ace.hashtree.HashTree;
import edu.umiacs.ace.hashtree.Proof;
import edu.umiacs.ace.ims.round.RoundLocal;
import edu.umiacs.ace.ims.round.RoundSummary;
import edu.umiacs.ace.ims.witness.Witness;
import edu.umiacs.ace.ims.witness.WitnessLocal;
import edu.umiacs.ace.ims.ws.RoundResponse;
import edu.umiacs.ace.server.ServiceLocator;
import edu.umiacs.ace.server.exception.InvalidParameterException;
import edu.umiacs.ace.util.HashValue;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJBException;
import javax.persistence.NoResultException;
import org.apache.log4j.Logger;

/**
 * Class for generating proofs linking supplied rounds to witness values
 * 
 * @author toaster
 */
public class WitnessValidation
{

    private WitnessLocal witnessBean =
            ServiceLocator.getInstance().getLocal(WitnessLocal.class);
    private RoundLocal roundBean =
            ServiceLocator.getInstance().getLocal(RoundLocal.class);
    private Map<Long, Witness> witnessMap = new HashMap<Long, Witness>();
    private Map<Witness, HashTree> hashMap = new HashMap<Witness, HashTree>();
    private static final Logger print = Logger.getLogger(WitnessValidation.class);

    public List<RoundResponse> createWitnessProofs(List<Long> rounds)
    {
        List<RoundResponse> response = new LinkedList<RoundResponse>();

        for ( long roundId : rounds )
        {
            RoundSummary rs;
            HashTree ht;
            Witness newWitness;
            Proof roundProof;

            if ( witnessMap.containsKey(roundId) )
            {
                continue;
            }

            rs = roundBean.getRoundSummary(roundId);
            // 1. Load witness and HashTree if we don't have it cached
            if ( !containsWitness(rs) )
            {

                try
                {
                    newWitness = witnessBean.getWitnessForRound(rs);
                }
                catch ( EJBException e )
                {
                    if ( e.getCause() instanceof NoResultException )
                    {
                        throw new InvalidParameterException(
                                StatusCode.WITNESS_NOT_YET_GENERATED,
                                "roundId",
                                "Witness has not been generated for round: " + roundId + " Try back tomorrow.");
                    }
                    else
                    {
                        throw e;
                    }
                }
                witnessMap.put(roundId, newWitness);
                ht = witnessBean.generateWitnessHashTree(newWitness);
                hashMap.put(newWitness, ht);
            }
            else
            {
                newWitness = witnessMap.get(roundId);
                ht = hashMap.get(newWitness);
            }

            roundProof = ht.proof(HashValue.asBytes(rs.getHashValue()));


            //2. Create response
            RoundResponse roundResponse = new RoundResponse();
            roundResponse.setRoundId(roundId);
            roundResponse.setWitnessId(newWitness.getId());
            roundResponse.setTokenClassName(newWitness.getTokenClass().getName());
            roundResponse.setDigestProvider(
                    newWitness.getTokenClass().getDigestProviderName());
            roundResponse.setDigestService(
                    newWitness.getTokenClass().getDigestServiceName());
            roundResponse.setProofElements(RoundProcessor.convertProof(
                    roundProof));
            roundResponse.setRoundTimestamp(rs.getTimestamp());
            print.trace(" generating proof for round: " + roundId + " proof: " + roundProof + " leaf: " + HashValue.asHexString(
                    roundProof.getLeafHash()));
            response.add(roundResponse);

        }

        return response;
    }

    /**
     * Test to see if we've loaded the witness yet. If we have, update the cache to
     * reflect that.
     * @param l roundId to look for
     * @param rs
     * @return
     */
    private boolean containsWitness(RoundSummary rs)
    {
        for ( Witness w : witnessMap.values() )
        {
            if ( w.getStartRound().getId() <= rs.getId() && w.getEndRound().getId() >= rs.getId() && w.getTokenClass().equals(
                    rs.getTokenClass()) )
            {
                witnessMap.put(rs.getId(), w);
                return true;
            }
        }
        return false;
    }
}
