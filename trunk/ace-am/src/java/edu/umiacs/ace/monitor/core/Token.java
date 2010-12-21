/*
 * Copyright (c) 2007-2010, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ACE Components were written in the ADAPT Project at the University of
 * Maryland Institute for Advanced Computer Study.
 */
// $Id$

package edu.umiacs.ace.monitor.core;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * ACE Token used to secure digests for an item.
 * TODO: break out the token response and store individual fields
 * @author toaster
 */
@Entity
@Table(name = "acetoken")
@NamedQueries({
    @NamedQuery(name = "Token.listByCollection", query =
    "SELECT t FROM MonitoredItem i, Token t WHERE i.parentCollection = :coll AND i.token = t"),
    @NamedQuery(name = "Token.deleteByCollection", query =
    "DELETE FROM Token t WHERE t.parentCollection = :coll")
})
public class Token implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastValidated;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    private boolean valid;
//    @Column(name = "java_token_response")
//    @Lob
//    private Serializable token;
    @ManyToOne
    private Collection parentCollection;
    // New items, from token itself
    @Column(name = "PROOFTEXT")
    private String proofText;
    @Column(name = "IMSSERVICE")
    private String imsService;
    @Column(name = "PROOFALGORITHM")
    private String proofAlgorithm;
    @Column(name = "ROUND")
    private long round;

    public void setId( Long id ) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals( Object object ) {
        if ( !(object instanceof Token) ) {
            return false;
        }
        Token other = (Token) object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "edu.umiacs.ace.monitor.items.Token[id=" + id + "]";
    }

    public String getImsService() {
        return imsService;
    }

    public String getProofAlgorithm() {
        return proofAlgorithm;
    }

    public String getProofText() {
        return proofText;
    }

    public long getRound() {
        return round;
    }

    public void setImsService( String imsService ) {
        this.imsService = imsService;
    }

    public void setProofAlgorithm( String proofAlgorithm ) {
        this.proofAlgorithm = proofAlgorithm;
    }

    public void setProofText( String proofText ) {
        this.proofText = proofText;
    }

    public void setRound( long round ) {
        this.round = round;
    }

    public Date getLastValidated() {
        return lastValidated;
    }

    public void setLastValidated( Date lastValidated ) {
        this.lastValidated = lastValidated;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate( Date createDate ) {
        this.createDate = createDate;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid( boolean valid ) {
        this.valid = valid;
    }
//
//    public Serializable getToken() {
//        return token;
//    }
//
//    public void setToken( Serializable token ) {
//        this.token = token;
//    }

    public void setParentCollection( Collection parentCollection ) {
        this.parentCollection = parentCollection;
    }

    public Collection getParentCollection() {
        return parentCollection;
    }
}
