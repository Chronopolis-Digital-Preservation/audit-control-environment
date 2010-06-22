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
// $Id: ReportItem.java 3181 2010-06-16 20:46:16Z toaster $

package edu.umiacs.ace.monitor.reporting;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Individual data point in a report
 * @author toaster
 */
@Table(name = "report_item")
@Entity()
public class ReportItem implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private ReportSummary report;
    private String attribute;
    private long value;
    private boolean logType;

    public ReportItem( String attribute,
            long value, boolean logType ) {
        this.attribute = attribute;
        this.value = value;
        this.logType = logType;
    }

    public ReportItem() {
    }

    public String getAttribute() {
        return attribute;
    }

    public boolean isLogType() {
        return logType;
    }

    public void setLogType( boolean logType ) {
        this.logType = logType;
    }

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public ReportSummary getReport() {
        return report;
    }

    public long getValue() {
        return value;
    }

    public void setValue( long value ) {
        this.value = value;
    }

    public void setAttribute( String attribute ) {
        this.attribute = attribute;
    }

    public void setReport( ReportSummary report ) {
        this.report = report;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals( Object object ) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if ( !(object instanceof ReportItem) ) {
            return false;
        }
        ReportItem other = (ReportItem) object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "edu.umiacs.ace.monitor.log.ReportItem[id=" + id + "]";
    }
}
