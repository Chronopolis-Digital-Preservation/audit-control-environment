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

import edu.umiacs.util.Argument;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * state:
 * A - active
 * C - corrupt, file and checksum do not match
 * M - local file is missing
 * T - no token for file
 * I - invalid token and checksum
 *  stuff added for 1.4+
 * P - partner does not have this file
 * D - partner file has different digest
 * 1.6b+
 * R - file registered but not ready for auditing
 * @author toaster
 */
@Entity
@Table(name = "monitored_item")
@NamedQueries({
    @NamedQuery(name = "MonitoredItem.listIds", query =
    "SELECT m.id FROM MonitoredItem m WHERE m.parentCollection = :coll AND m.directory = false"),
    @NamedQuery(name = "MonitoredItem.getItemByPath", query =
    "SELECT m FROM MonitoredItem m WHERE m.path = :path AND m.parentCollection = :coll"),
    //@NamedQuery(name = "MonitoredItem.setItemState", query = "UPDATE MonitoredItem m set m.state = :state, m.lastSeen = :date WHERE m.path = :path AND m.parentCollection = :coll"),
    //@NamedQuery(name = "MonitoredItem.setItemStateAndChange", query = "UPDATE MonitoredItem m set m.state = :state, m.stateChange = :date WHERE m.path = :path AND m.parentCollection = :coll"),
    @NamedQuery(name = "MonitoredItem.listItemsBefore", query =
    "SELECT m FROM MonitoredItem m WHERE m.lastVisited < :date AND m.parentCollection = :coll"),
    @NamedQuery(name = "MonitoredItem.deleteByCollection", query =
    "DELETE FROM MonitoredItem WHERE parentCollection = :coll"),
    @NamedQuery(name = "MonitoredItem.listByCollection", query =
    "SELECT m FROM MonitoredItem m WHERE m.parentCollection = :coll"),
    @NamedQuery(name = "MonitoredItem.listRoots", query =
    "SELECT m FROM MonitoredItem m WHERE m.parentCollection = :coll AND m.parentPath IS NULL"),
    @NamedQuery(name = "MonitoredItem.listChildren", query =
    "SELECT m FROM MonitoredItem m WHERE m.parentCollection = :coll AND m.parentPath = :parent"),
    @NamedQuery(name = "MonitoredItem.countFilesInAllCollections", query =
    "SELECT m.parentCollection, count(m) FROM MonitoredItem m WHERE m.directory = false GROUP BY m.parentCollection"),
    @NamedQuery(name = "MonitoredItem.countFilesInCollection", query =
    "SELECT count(m) FROM MonitoredItem m WHERE m.directory = false AND m.parentCollection = :coll"),
    @NamedQuery(name = "MonitoredItem.listFilesInCollection", query =
    "SELECT m FROM MonitoredItem m WHERE m.directory = false AND m.parentCollection = :coll"),
    @NamedQuery(name = "MonitoredItem.countDirectoriesInAllCollections", query =
    "SELECT m.parentCollection, count(m) FROM MonitoredItem m WHERE m.directory = true GROUP BY m.parentCollection"),
    @NamedQuery(name = "MonitoredItem.countDirectoriesCollection", query =
    "SELECT count(m) FROM MonitoredItem m WHERE m.directory = true AND m.parentCollection = :coll"),
    @NamedQuery(name = "MonitoredItem.countErrorsInCollection", query =
    "SELECT count(m) FROM MonitoredItem m WHERE m.state <> 'A' AND m.parentCollection = :coll"),
    @NamedQuery(name = "MonitoredItem.itemsByState", query =
    "SELECT m FROM MonitoredItem m WHERE m.parentCollection = :coll AND m.directory = false AND m.state = :state ORDER BY m.id"),
    @NamedQuery(name = "MonitoredItem.listDuplicates", query =
    "SELECT m FROM MonitoredItem m WHERE m.fileDigest = :digest AND m.parentCollection = :coll"),
    @NamedQuery(name = "MonitoredItem.listNullTokens", query =
    "SELECT m FROM MonitoredItem m WHERE m.token IS NULL AND m.state <> 'T' AND m.parentCollection = :coll AND m.directory = false"),
    @NamedQuery(name = "MonitoredItem.listRemoteErrors", query =
    "SELECT m FROM MonitoredItem m WHERE (m.state = 'P' OR m.state = 'D') AND m.directory = false AND m.parentCollection = :coll"),
    @NamedQuery(name = "MonitoredItem.listLocalErrors", query =
    "SELECT m FROM MonitoredItem m WHERE (m.state = 'C' OR m.state = 'M' OR m.state = 'T' OR m.state = 'I') AND m.directory = false AND m.parentCollection = :coll"),
    @NamedQuery(name = "MonitoredItem.updateMissing", query =
    "UPDATE MonitoredItem SET state = 'M', stateChange = :date, lastVisited = :date WHERE parentCollection = :coll AND lastVisited < :date AND state != 'M'")
})
public class MonitoredItem implements Serializable, Comparable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "VARCHAR(255) COLLATE utf8")
    private String path; // path relative to base directory

    @Column(columnDefinition = "VARCHAR(255) COLLATE utf8")
    private String parentPath;

    @Column(nullable = false)
    private boolean directory; // true if directory

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastSeen;

    @Temporal(TemporalType.TIMESTAMP)
    private Date stateChange;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastVisited;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Collection parentCollection;

    @Column(nullable = false)
    private char state;

    @ManyToOne(cascade = CascadeType.ALL)
    private Token token;

    private long size;
    private String fileDigest;

    public void setId( Long id ) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public long getSize() {
        return size;
    }

    public void setSize( long size ) {
        this.size = size;
    }

    public Date getLastSeen() {
        return Argument.dateClone(lastSeen);
    }

    public String getPath() {
        return path;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory( boolean directory ) {
        this.directory = directory;
    }

    public void setLastSeen( Date lastSeen ) {
        this.lastSeen = Argument.dateClone(lastSeen);
    }

    public void setPath( String path ) {
        this.path = path;
    }

    public Token getToken() {
        return token;
    }

    public void setToken( Token token ) {
        this.token = token;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals( Object object ) {
        if ( !(object instanceof MonitoredItem) ) {
            return false;
        }
        MonitoredItem other = (MonitoredItem) object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(
                other.id)) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "edu.umiacs.srb.monitor.db.MonitoredItem[id=" + id + "]";
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath( String parentPath ) {
        this.parentPath = parentPath;
    }

    public Collection getParentCollection() {
        return parentCollection;
    }

    public void setParentCollection( Collection parentCollection ) {
        this.parentCollection = parentCollection;
    }

    public char getState() {
        return state;
    }

    public String getStateAsString() {
        return String.valueOf(state);
    }

    public void setState( char state ) {
        this.state = state;
    }

    public void setLastVisited( Date lastVisited ) {
        this.lastVisited = Argument.dateClone(lastVisited);
    }

    public Date getLastVisited() {
        return Argument.dateClone(lastVisited);
    }

    @Override
    public int compareTo( Object o ) {

        if ( equals(o) ) {
            return 0;
        }
        if ( o instanceof MonitoredItem ) {
            MonitoredItem other = (MonitoredItem) o;
            String thisName = path.substring(path.lastIndexOf("/") + 1);
            String theirName = other.path.substring(
                    other.path.lastIndexOf("/") + 1);
            return thisName.compareTo(theirName);
        }
        return -1;
    }

    public Date getStateChange() {
        return Argument.dateClone(stateChange);
    }

    public void setStateChange( Date stateChange ) {
        this.stateChange = Argument.dateClone(stateChange);
    }

    public void setFileDigest( String fileDigest ) {
        this.fileDigest = fileDigest;
    }

    public String getFileDigest() {
        return fileDigest;
    }

    public boolean isError() {
        return (this.state == 'I' || this.state == 'C');
    }
}
