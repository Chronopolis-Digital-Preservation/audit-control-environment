/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.round;

import edu.umiacs.ace.ims.tokenclass.TokenClass;
import edu.umiacs.util.ToStringBuilder;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author mmcgann
 */
@Entity
@Table(name="previous_round")
@NamedQueries
({
    @NamedQuery(name="PreviousRound.findByTokenClass", 
                query="select pr from PreviousRound pr where " + 
                      "pr.tokenClass = :tokenClass")
})
public class PreviousRound implements Serializable
{ 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private TokenClass tokenClass;
    
    @OneToOne(cascade=CascadeType.PERSIST)
    private RoundSummary roundSummary;
    
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
        if ( !(object instanceof PreviousRound) )
        {
            return false;
        }
        PreviousRound other = (PreviousRound)object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder()
                .append("id", id)
                .append("tokenClassName", tokenClass.getName())
                .append("roundSummary",getRoundSummary())
                .toString();
    }

    public TokenClass getTokenClass()
    {
        return tokenClass;
    }

    public void setTokenClass(TokenClass tokenClass)
    {
        this.tokenClass = tokenClass;
    }

    public RoundSummary getRoundSummary()
    {
        return roundSummary;
    }

    public void setRoundSummary(RoundSummary roundSummary)
    {
        this.roundSummary = roundSummary;
    }

}
