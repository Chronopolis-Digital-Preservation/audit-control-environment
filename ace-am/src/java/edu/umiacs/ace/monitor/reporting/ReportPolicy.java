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
// $Id: ReportPolicy.java 3181 2010-06-16 20:46:16Z toaster $

package edu.umiacs.ace.monitor.reporting;

import edu.umiacs.ace.monitor.core.Collection;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Automated report entry determining when a report is to run, as well
 * as who received e-mails.
 * 
 * @author toaster
 */
@Entity
@Table(name = "report_policy")
@NamedQueries({
    @NamedQuery(name = "ReportPolicy.listAll", query = "SELECT r FROM ReportPolicy r"),
    @NamedQuery(name = "ReportPolicy.listByCollection", query =
    "SELECT r FROM ReportPolicy r WHERE r.collection = :coll")
})
public class ReportPolicy implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cronString;
    @Column(columnDefinition = "TEXT")
    private String emailList;
    private String name;
    @ManyToOne(fetch = FetchType.EAGER)
    private Collection collection;

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public void setCronString( String cronString ) {
        this.cronString = cronString;
    }

    public void setEmailList( String emailList ) {
        this.emailList = emailList;
    }

    public String getEmailList() {
        return emailList;
    }

    public String getCronString() {
        return cronString;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setCollection( Collection collection ) {
        this.collection = collection;
    }

    public Collection getCollection() {
        return collection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals( Object object ) {
        if ( !(object instanceof ReportPolicy) ) {
            return false;
        }
        ReportPolicy other = (ReportPolicy) object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(
                other.id)) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "edu.umiacs.ace.monitor.reporting.ReportPolicy[id=" + id + ", name="+name+"]";
    }
}
