/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.witness;

import edu.umiacs.ace.hashtree.HashTree;
import edu.umiacs.ace.ims.round.RoundSummary;
import javax.ejb.Local;

/**
 *
 * @author mmcgann
 */
@Local
public interface WitnessLocal 
{

    Witness seedWitness(RoundSummary roundSummary);

    Witness publishWitness(String tokenClassName);
    
    public HashTree generateWitnessHashTree(Witness witness);
    
    public Witness getWitnessForRound(RoundSummary round);
    
}
