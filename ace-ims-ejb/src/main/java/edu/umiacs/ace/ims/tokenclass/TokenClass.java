/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.tokenclass;

import edu.umiacs.util.ToStringBuilder;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author mmcgann
 */
@Entity
@Table(name="token_class")
@NamedQueries
({
    @NamedQuery(name="TokenClass.list", 
                query="select tc from TokenClass tc order by tc.name"), 
    @NamedQuery(name="TokenClass.findByName", 
                query="select tc from TokenClass tc where tc.name = :name")
})
public class TokenClass implements Serializable 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", unique=true, nullable=false)
    private String name;
    
    @Column(name="digest_provider")
    private String digestProviderName;
    
    @Column(name="digest_service", nullable=false)
    private String digestServiceName;
    
    @Column(name="tree_order") 
    private int treeOrder;
        
    @Column(name="minimum_requests")
    private int minimumRequests;
    
    @Column(name="maximum_requests")
    private int maximumRequests;
    
    @Column(name="round_length")
    private long roundLength;
    
    @Column(name="requset_timeout")
    private long requestTimeout;
            
    @Column(name="allow_linking")
    private boolean linkingAllowed;
    
    @Column(name="allow_request_async")
    private boolean requestAsyncAllowed;
    
    @Column(name="allow_request_immediate")
    private boolean requestImmediateAllowed;
    
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
        if ( !(object instanceof TokenClass) )
        {
            return false;
        }
        TokenClass other = (TokenClass)object;
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
                .append("name", name)
                .append("digestProviderName", digestProviderName)
                .append("digestServiceName", digestServiceName)
                .toString();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDigestProviderName()
    {
        return digestProviderName;
    }

    public void setDigestProviderName(String digestProviderName)
    {
        this.digestProviderName = digestProviderName;
    }

    public String getDigestServiceName()
    {
        return digestServiceName;
    }

    public void setDigestServiceName(String digestServiceName)
    {
        this.digestServiceName = digestServiceName;
    }

    public long getRoundLength()
    {
        return roundLength;
    }

    public void setRoundLength(long roundLength)
    {
        this.roundLength = roundLength;
    }

    public long getRequestTimeout()
    {
        return requestTimeout;
    }

    public void setRequestTimeout(long immediateWaitTimeout)
    {
        this.requestTimeout = immediateWaitTimeout;
    }

    public boolean isLinkingAllowed()
    {
        return linkingAllowed;
    }

    public void setLinkingAllowed(boolean linkingAllowed)
    {
        this.linkingAllowed = linkingAllowed;
    }

    public boolean isRequestAsyncAllowed()
    {
        return requestAsyncAllowed;
    }

    public void setRequestAsyncAllowed(boolean requestAsyncAllowed)
    {
        this.requestAsyncAllowed = requestAsyncAllowed;
    }

    public boolean isRequestImmediateAllowed()
    {
        return requestImmediateAllowed;
    }

    public void setRequestImmediateAllowed(boolean requestImmediateAllowed)
    {
        this.requestImmediateAllowed = requestImmediateAllowed;
    }

    public int getMinimumRequests()
    {
        return minimumRequests;
    }

    public void setMinimumRequests(int minimumRequests)
    {
        this.minimumRequests = minimumRequests;
    }

    public int getMaximumRequests()
    {
        return maximumRequests;
    }

    public void setMaximumRequests(int maximumRequests)
    {
        this.maximumRequests = maximumRequests;
    }

    public int getTreeOrder()
    {
        return treeOrder;
    }

    public void setTreeOrder(int treeOrder)
    {
        this.treeOrder = treeOrder;
    }

}
