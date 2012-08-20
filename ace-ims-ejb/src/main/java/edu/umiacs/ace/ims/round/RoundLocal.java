/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.round;

import edu.umiacs.ace.ims.tokenclass.TokenClass;
import javax.ejb.Local;

/**
 *
 * @author mmcgann
 */
@Local
public interface RoundLocal
{
    PreviousRound findPreviousRound(TokenClass tokenClass);

    RoundSummary createRoundSummary(RoundSummary roundSummary);

    PreviousRound seedRoundSummary(RoundSummary roundSummary);
    
    public RoundSummary getRoundSummary(long roundId);
}
