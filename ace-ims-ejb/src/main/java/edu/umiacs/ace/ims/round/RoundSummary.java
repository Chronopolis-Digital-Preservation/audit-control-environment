/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.round;

import edu.umiacs.ace.ims.tokenclass.TokenClass;
import edu.umiacs.ace.util.HashFormatter;
import edu.umiacs.util.ToStringBuilder;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 *
 * @author mmcgann
 */
@Entity
@Table(name="round_summary")
@NamedQuery(name="RoundSummary.selectRoundRange", query="SELECT r from RoundSummary r WHERE r.tokenClass = :tokenClass AND r.id >= :start AND r.id <= :finish")
public class RoundSummary implements Serializable 
{
    @Transient
    private static final HashFormatter hashFormatter = new HashFormatter();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private TokenClass tokenClass;
  
    @Column(name="hash_value", nullable=false)
    private String hashValue;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="time_stamp", nullable=false)
    private Date timestamp;

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
        if ( !(object instanceof RoundSummary) )
        {
            return false;
        }
        RoundSummary other = (RoundSummary)object;
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
                .append("timestamp", timestamp)
                .append("hashValue", hashFormatter.format(hashValue))
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

    public String getHashValue()
    {
        return hashValue;
    }

    public void setHashValue(String hashValue)
    {
        this.hashValue = hashValue;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }

}
