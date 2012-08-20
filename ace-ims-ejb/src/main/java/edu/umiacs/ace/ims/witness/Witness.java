/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.witness;

import edu.umiacs.ace.ims.round.RoundSummary;
import edu.umiacs.ace.ims.tokenclass.TokenClass;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author mmcgann
 */
@Entity
@Table(name="witness")
@NamedQueries({
@NamedQuery(name="Witness.getPreviousWitness", query="SELECT w FROM Witness w WHERE w.id < :witness AND w.tokenClass = :token ORDER BY w.id DESC"),
@NamedQuery(name="Witness.getWitnessForRound", query="SELECT w FROM Witness w WHERE w.startRound.id <= :id AND w.endRound.id >= :id AND w.tokenClass = :token")
})
public class Witness implements Serializable 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TokenClass tokenClass;
    
    @ManyToOne(cascade=CascadeType.PERSIST)
    private RoundSummary startRound;
    
    @ManyToOne(cascade=CascadeType.PERSIST)
    private RoundSummary endRound;
    
    @Column(name="round_count", nullable=false)
    private int roundCount;
    
    @Column(name="hash_value", nullable=false)
    private String hashValue;
    
    public void setId(Long id)
    {
        this.id = id;
    }


    public Long getId()
    {
        return id;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if ( !(object instanceof Witness) )
        {
            return false;
        }
        Witness other = (Witness)object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "edu.umiacs.ace.ims.witness.Witness[id=" + id + "]";
    }

    public TokenClass getTokenClass()
    {
        return tokenClass;
    }

    public void setTokenClass(TokenClass tokenClass)
    {
        this.tokenClass = tokenClass;
    }

    public RoundSummary getStartRound()
    {
        return startRound;
    }

    public void setStartRound(RoundSummary startRound)
    {
        this.startRound = startRound;
    }

    public RoundSummary getEndRound()
    {
        return endRound;
    }

    public void setEndRound(RoundSummary endRound)
    {
        this.endRound = endRound;
    }

    public String getHashValue()
    {
        return hashValue;
    }

    public void setHashValue(String hashValue)
    {
        this.hashValue = hashValue;
    }

    public int getRoundCount()
    {
        return roundCount;
    }

    public void setRoundCount(int roundCount)
    {
        this.roundCount = roundCount;
    }

}
