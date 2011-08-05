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

import edu.umiacs.ace.monitor.peers.PeerCollection;
import edu.umiacs.util.Argument;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Collection states
 *  - A active
 *  - N - never completely scanned (default for new collections)
 *  - E - 
 * @author toaster
 */
@Entity
@Table(name = "collection")
@NamedQueries({
    @NamedQuery(name = "Collection.listAllCollections", query =
    "SELECT c FROM Collection c ORDER BY c.group"),
    @NamedQuery(name = "Collection.getCollectionByName", query =
    "SELECT c FROM Collection c WHERE c.name = :name"),
    @NamedQuery(name = "Collection.listGroups", query = "SELECT DISTINCT c.group FROM Collection c")
})
public class Collection implements Serializable {

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<PeerCollection> peerCollections;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = (long) 0;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String directory;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastSync;
    private String storage;
    private char state;
    @Column(name = "COLGROUP")
    private String group;
    private String digestAlgorithm;
    @ElementCollection
    @CollectionTable(name="settings", joinColumns=@JoinColumn(name="COLLECTION_ID"))
    @MapKeyColumn(name="ATTR")
    @Column(name="VALUE")
    private Map<String,String> settings;

    public void setId( Long id ) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }
    public void setPeerCollections( List<PeerCollection> peerCollections ) {
        this.peerCollections = peerCollections;
    }

    public List<PeerCollection> getPeerCollections() {
        return peerCollections;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm( String digestAlgorithm ) {
        this.digestAlgorithm = digestAlgorithm;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals( Object object ) {

        if ( !(object instanceof Collection) ) {
            return false;
        }
        Collection other = (Collection) object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(
                other.id)) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "edu.umiacs.srb.monitor.resource.Collection[id=" + id + "]";
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory( String directory ) {
        this.directory = directory;
    }

    public Date getLastSync() {
        return Argument.dateClone(lastSync);
    }

    public void setLastSync( Date lastSync ) {
        this.lastSync = Argument.dateClone(lastSync);
    }

    public char getState() {
        return state;
    }

    public void setState( char state ) {
        this.state = state;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage( String storage ) {
        this.storage = storage;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup( String group ) {
        this.group = group;
    }

}
