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

package edu.umiacs.ace.monitor.reporting;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.util.Argument;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author toaster
 */
@Table(name = "report_summary")
@Entity()
@NamedQueries({
    @NamedQuery(name = "ReportSummary.listByCollection", query =
    "SELECT s FROM ReportSummary s WHERE s.collection = :coll"),
    @NamedQuery(name = "ReportSummary.listByCollectionRecent", query =
    "SELECT s FROM ReportSummary s WHERE s.id = (select max(s2.id) from ReportSummary s2 WHERE S2.collection = :coll)"),
    @NamedQuery(name = "ReportSummary.listAllSummaries", query = "SELECT s FROM ReportSummary s"),
//    @NamedQuery(name = "ReportSummary.listAllRecent", query =
//    "SELECT s FROM ReportSummary s WHERE s.id IN " +
//            "(select max(s2.id) from ReportSummary s2 GROUP BY s2.collection.id)"),
    @NamedQuery(name = "ReportSummary.listAllRecentIds", query =
    "select max(s2.id) from ReportSummary s2 GROUP BY s2.collection.id"),
////    @NamedQuery(name = "ReportSummary.listAllRecent", query = "SELECT s FROM ReportSummary s GROUP BY s.collection.id"),
    @NamedQuery(name = "ReportSummary.deleteByCollection", query =
    "DELETE FROM ReportSummary s WHERE s.collection = :coll")
})
public class ReportSummary implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date generatedDate;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date startDate;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date endDate;
    private long firstLogEntry;
    private long lastLogEntry;
    @ManyToOne
    private Collection collection;
    private String reportName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "report", fetch = FetchType.LAZY)
    private List<ReportItem> summaryItems;

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
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
        if ( !(object instanceof ReportSummary) ) {
            return false;
        }
        ReportSummary other = (ReportSummary) object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(
                other.id)) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "edu.umiacs.ace.monitor.log.ReportSummary[id=" + id + "]";
    }

    public Date getGeneratedDate() {
        return Argument.dateClone(generatedDate);
    }

    public void setGeneratedDate( Date generatedDate ) {
        this.generatedDate = Argument.dateClone(generatedDate);
    }

    public Date getStartDate() {
        return Argument.dateClone(startDate);
    }

    public void setStartDate( Date startDate ) {
        this.startDate = Argument.dateClone(startDate);
    }

    public Date getEndDate() {
        return Argument.dateClone(endDate);
    }

    public void setEndDate( Date endDate ) {
        this.endDate = Argument.dateClone(endDate);
    }

    public long getFirstLogEntry() {
        return firstLogEntry;
    }

    public void setFirstLogEntry( long firstLogEntry ) {
        this.firstLogEntry = firstLogEntry;
    }

    public long getLastLogEntry() {
        return lastLogEntry;
    }

    public void setLastLogEntry( long lastLogEntry ) {
        this.lastLogEntry = lastLogEntry;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection( Collection collection ) {
        this.collection = collection;
    }

    public List<ReportItem> getSummaryItems() {
        return summaryItems;
    }

    public void setSummaryItems( List<ReportItem> summaryItems ) {
        this.summaryItems = summaryItems;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName( String reportName ) {
        this.reportName = reportName;
    }

    public String createReport() {
        StringBuilder sb = new StringBuilder();

        sb.append("Report Name: \t");
        sb.append(reportName);
        sb.append("\r\n");

        sb.append("Collection: \t");
        sb.append(collection.getName());
        sb.append("\r\n");

        sb.append("Generated on: \t");
        sb.append(generatedDate);
        sb.append("\r\n");

        sb.append("Start Date: \t");
        sb.append(startDate);
        sb.append("\r\n");

        sb.append("End Date: \t");
        sb.append(endDate);
        sb.append("\r\n");

        sb.append("\r\n----------------------\r\nCollection summary\r\n\r\n");

        for ( ReportItem ri : summaryItems ) {
            if ( !ri.isLogType() ) {
                sb.append(ri.getAttribute());
                sb.append(": \t");
                sb.append(ri.getValue());
                sb.append("\r\n");
            }
        }
        sb.append("\r\n----------------------\r\nTotal Log Entries\r\n\r\n");
        for ( ReportItem ri : summaryItems ) {
            if ( ri.isLogType() ) {
                sb.append(ri.getAttribute());
                sb.append(": \t");
                sb.append(ri.getValue());
                sb.append("\r\n");
            }
        }
        return sb.toString();
    }
}
