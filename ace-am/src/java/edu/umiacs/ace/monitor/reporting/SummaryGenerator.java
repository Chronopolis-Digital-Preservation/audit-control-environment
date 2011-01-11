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

import edu.umiacs.ace.monitor.log.*;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.sql.SQL;
import edu.umiacs.util.Argument;
import edu.umiacs.util.Strings;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

/**
 * Log summary generator, this will generate a report summarizing all log entries
 * made between the specified dates. If the end date is the current time, then
 * additional information will be contained showing the current state of the 
 * collection.
 * 
 * @author toaster
 */
public class SummaryGenerator extends ReportItemTypes {

    private Collection collection;
    private Date startDate;
    private long sessionID = 0;
    private String reportName;
    private List<ReportItem> itemList = new ArrayList<ReportItem>();
    private static final Logger LOG = Logger.getLogger(SummaryGenerator.class);

    public SummaryGenerator( String reportName, Collection collection,
            Date startDate ) {
        this.collection = collection;
        this.startDate = Argument.dateClone(startDate);

        if ( Strings.isEmpty(reportName) ) {
            reportName = "Report Starting " + startDate;
        }
        {
            this.reportName = reportName;
        }
    }

    public SummaryGenerator( Collection collection, long session ) {
        this.collection = collection;
        this.sessionID = session;
        reportName = "Session Report: " + session;
    }

    public SummaryGenerator( Collection collection ) {
        this.collection = collection;
        reportName = "Entire Collection Report";
    }

    public synchronized ReportSummary generateReport() {
        LOG.debug("Creating report for collection " + collection.getName());
        EntityManager em = null;
        Date reportEndDate = new Date();
        try {
            queryCurrentState();
            queryLogHistory();
        } catch ( SQLException e ) {
            LOG.error("Error creating report", e);
        }

        ReportSummary summary = new ReportSummary();
        summary.setSummaryItems(itemList);
        summary.setCollection(collection);
        summary.setGeneratedDate(new Date());
        summary.setStartDate(startDate);
        summary.setFirstLogEntry(sessionID);
        summary.setEndDate(reportEndDate);
        summary.setReportName(reportName);

        for ( ReportItem ri : itemList ) {
            ri.setReport(summary);
        }


        try {
            em = PersistUtil.getEntityManager();
            EntityTransaction et = em.getTransaction();
            et.begin();
            try {
                em.persist(summary);
                et.commit();
            } catch ( Exception e ) {
                LOG.error("Error commiting summary ", e);
                et.rollback();
            }
        } finally {
            if ( em != null ) {
                em.close();
            }
            LOG.debug("Finished creating report for " + collection.getName());
        }

        return summary;
    }

    private void queryLogHistory() throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        DataSource ds = PersistUtil.getDataSource();

        try {
            // query selects lowest date, id, count for groupings of log types
            connection = ds.getConnection();
            if ( sessionID > 0 ) {
                ps = connection.prepareStatement("SELECT MIN(logevent.DATE), "
                        + "MIN(logevent.ID), logevent.LOGTYPE, count(logevent.LOGTYPE)"
                        + "FROM logevent WHERE logevent.SESSION = ? AND "
                        + "logevent.COLLECTION_ID = ? GROUP BY logevent.LOGTYPE");
                ps.setLong(1, sessionID);
                ps.setLong(2, collection.getId());

            } else if ( startDate != null ) {

                ps = connection.prepareStatement("SELECT MIN(logevent.DATE), "
                        + "MIN(logevent.ID), logevent.LOGTYPE, count(logevent.LOGTYPE)"
                        + "FROM logevent WHERE logevent.DATE >= ? AND "
                        + "logevent.COLLECTION_ID = ? GROUP BY logevent.LOGTYPE");
                ps.setTimestamp(1, new Timestamp(startDate.getTime()));
                ps.setLong(2, collection.getId());

            } else {
                ps = connection.prepareStatement("SELECT MIN(logevent.DATE), "
                        + "MIN(logevent.ID), logevent.LOGTYPE, count(logevent.LOGTYPE)"
                        + "FROM logevent WHERE logevent.COLLECTION_ID = ? "
                        + "GROUP BY logevent.LOGTYPE");
                ps.setLong(2, collection.getId());
            }

            // create entries for each result, for start date and session id
            // use lowest returned
            rs = ps.executeQuery();
            while ( rs.next() ) {
                if ( startDate == null || startDate.after(rs.getTimestamp(1)) ) {
                    startDate = rs.getTimestamp(1);
                }
                if ( sessionID == 0 || sessionID > rs.getLong(2) ) {
                    sessionID = rs.getLong(2);
                }
                String logName = LogEnum.valueOf(rs.getInt(3)).getShortName();

                itemList.add(new ReportItem(logName, rs.getLong(4), true));

            }
        } finally {
            SQL.release(rs);
            SQL.release(ps);
            SQL.release(connection);
        }
    }

    private void queryCurrentState() throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        DataSource ds = PersistUtil.getDataSource();

        try {
            connection = ds.getConnection();
            ps = connection.prepareStatement(
                    "SELECT monitored_item.STATE, count(monitored_item.STATE) "
                    + "FROM monitored_item " + "WHERE monitored_item.PARENTCOLLECTION_ID = ? AND "
                    + "monitored_item.DIRECTORY = 0 " + "GROUP BY monitored_item.STATE");
            ps.setLong(1, collection.getId());
            rs = ps.executeQuery();
            long total = 0;
            long totalErrors = 0;

            while ( rs.next() ) {
                char state = rs.getString(1).charAt(0);
                long count = rs.getLong(2);

                total += count;


                switch ( state ) {
                    case 'A':
                        itemList.add(new ReportItem(ACTIVE, count, false));
                        break;
                    case 'C':
                        itemList.add(new ReportItem(CORRUPT_FILES, count, false));
                        totalErrors += count;
                        break;
                    case 'M':
                        itemList.add(new ReportItem(MISSING_FILES, count, false));
                        totalErrors += count;
                        break;
                    case 'T':
                        itemList.add(
                                new ReportItem(MISSING_TOKENS, count, false));
                        totalErrors += count;
                        break;
                    case 'I':
                        itemList.add(
                                new ReportItem(CORRUPT_DIGEST, count, false));
                        totalErrors += count;
                        break;
                }
            }

            itemList.add(new ReportItem(TOTAL_ITEMS, total, false));
            itemList.add(new ReportItem(TOTAL_ERRORS, totalErrors, false));
        } finally {
            SQL.release(rs);
            SQL.release(ps);
            SQL.release(connection);
        }
    }
}
