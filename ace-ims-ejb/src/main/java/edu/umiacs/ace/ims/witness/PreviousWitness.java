/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.witness;

import edu.umiacs.ace.ims.tokenclass.TokenClass;
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
@Table(name="previous_witness")
@NamedQueries
({
    @NamedQuery(name="PreviousWitness.findByTokenClass", 
                query="select pw from PreviousWitness pw " + 
                      "where pw.tokenClass = :tokenClass")
})
public class PreviousWitness implements Serializable 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private TokenClass tokenClass;
    
    @OneToOne(cascade=CascadeType.PERSIST)
    private Witness witness;
    
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
        if ( !(object instanceof PreviousWitness) )
        {
            return false;
        }
        PreviousWitness other = (PreviousWitness)object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "edu.umiacs.ace.ims.witness.PreviousWitness[id=" + id + "]";
    }

    public TokenClass getTokenClass()
    {
        return tokenClass;
    }

    public void setTokenClass(TokenClass tokenClass)
    {
        this.tokenClass = tokenClass;
    }

    public Witness getWitness()
    {
        return witness;
    }

    public void setWitness(Witness witness)
    {
        this.witness = witness;
    }

}
