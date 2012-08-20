/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.store;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author mmcgann
 */
@Entity
@Table(name="token_store")
@NamedQueries
({
    @NamedQuery(name="TokenStore.find", 
                query="select ts from TokenStore ts where ts.id = :id and " +
                      "ts.sessionKey = :sessionKey"),
    @NamedQuery(name="TokenStore.deleteOldIssuedTokens", 
                query="delete from IssuedToken i where ( select ts.timestamp from TokenStore ts where i.tokenStore = ts ) <= :time"),
    @NamedQuery(name="TokenStore.deleteOldTokenStores",
                query="delete from TokenStore ts where ts.timestamp <= :time")
                      
})
public class TokenStore implements Serializable 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="session_key")
    private long sessionKey;
    
    @Column(name="time_stamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    
    @Column(name="token_count")
    private int tokenCount;
    
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
        if ( !(object instanceof TokenStore) )
        {
            return false;
        }
        TokenStore other = (TokenStore)object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "edu.umiacs.ace.ims.round.TokenStore[id=" + id + "]";
    }

    public long getSessionKey()
    {
        return sessionKey;
    }

    public void setSessionKey(long sessionKey)
    {
        this.sessionKey = sessionKey;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }

    public int getTokenCount()
    {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount)
    {
        this.tokenCount = tokenCount;
    }

}
