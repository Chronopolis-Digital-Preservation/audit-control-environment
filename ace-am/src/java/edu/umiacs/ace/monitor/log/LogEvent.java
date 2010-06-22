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
// $Id: LogEvent.java 3181 2010-06-16 20:46:16Z toaster $

package edu.umiacs.ace.monitor.log;

import edu.umiacs.ace.monitor.core.Collection;
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

/**
 * Log event database entry
 * @author toaster
 */
@Entity
@Table(name = "logevent")
@NamedQueries({
    @NamedQuery(name = "LogEvent.deleteByCollection", query =
    "DELETE FROM LogEvent e WHERE e.collection = :coll")
})
public class LogEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int logType;
    @Column(columnDefinition = "TEXT")
    private String description;
    private long session;
    @Column(columnDefinition = "VARCHAR(512) COLLATE latin1_bin")
    private String path;
    @ManyToOne
    private Collection collection;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date date;

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

        if ( !(object instanceof LogEvent) ) {
            return false;
        }
        LogEvent other = (LogEvent) object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "edu.umiacs.srb.monitor.log.LogEvent[id=" + id + "]";
    }

    public int getLogType() {
        return logType;
    }

    public void setLogType( int logType ) {
        this.logType = logType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public long getSession() {
        return session;
    }

    public void setSession( long session ) {
        this.session = session;
    }

    public void setCollection( Collection collection ) {
        this.collection = collection;
    }

    public void setPath( String path ) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public Collection getCollection() {
        return collection;
    }

    public Date getDate() {
        return date;
    }

    public void setDate( Date date ) {
        this.date = date;
    }
}
