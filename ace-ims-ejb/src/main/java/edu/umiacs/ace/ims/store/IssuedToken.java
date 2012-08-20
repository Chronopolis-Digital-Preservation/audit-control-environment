/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.store;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author mmcgann
 */
@Entity
@Table(name="issued_token")
@NamedQueries({
    @NamedQuery(
    name="IssuedTokens.findByTokenStore",
            query="select it from IssuedToken it where it.tokenStore = :tokenStore")
})
public class IssuedToken implements Serializable
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private TokenStore tokenStore;
   
    @Column(name="java_token_response")
    @Lob
    private byte[] tokenResponse;
   
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
        if ( !(object instanceof IssuedToken) )
        {
            return false;
        }
        IssuedToken other = (IssuedToken)object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "edu.umiacs.ace.ims.round.IssuedToken[id=" + id + "]";
    }

    public TokenStore getTokenStore()
    {
        return tokenStore;
    }

    public void setTokenStore(TokenStore tokenStore)
    {
        this.tokenStore = tokenStore;
    }

    public byte[] getTokenResponse()
    {
        return tokenResponse;
    }

    public void setTokenResponse(byte[] tokenResponse)
    {
        this.tokenResponse = tokenResponse;
    }
}
