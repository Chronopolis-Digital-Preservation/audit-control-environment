/*
 * Copyright (c) 2007-2011, University of Maryland
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
package edu.umiacs.ace.monitor.core;

import edu.umiacs.ace.monitor.audit.AuditConfigurationContext;
import edu.umiacs.ace.monitor.audit.AuditConfigurationContext.PauseBean;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.sql.SQL;
import edu.umiacs.util.Strings;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 * Listener to migrate pre-1.7 ACE settings to the new key/value table
 * 
 * @author toaster
 */
public class CollectionSettingsMigrationListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(CollectionSettingsMigrationListener.class);

    public void contextInitialized(ServletContextEvent sce) {
        PauseBean pb =
                (PauseBean) sce.getServletContext().getAttribute(AuditConfigurationContext.ATTRIBUTE_PAUSE);

        NDC.push("[SETTINGS]");
        Connection conn = null;
        boolean migrated;

        try {
            if (pb == null) {
                LOG.error("Could not pause auditing (pb null), stopping application to prevent damage");
                throw new RuntimeException("PauseBean not present in servlet context");
            }

            boolean oldState = pb.isPaused();
            pb.setPaused(true);
            DataSource ds = PersistUtil.getDataSource();


            try {
                conn = ds.getConnection();
                migrated = hasMigrated(conn);
            } catch (SQLException e) {
                LOG.fatal("Error getting SQL connection or table data", e);
                throw new RuntimeException("Error grabbing SQL connection or table data", e);
            }

            if (!migrated) {
                LOG.info("Settings Migration starting");
                sce.getServletContext().setAttribute("globalMessage", "Auditing Paused: Settings migration in progress");
                try {

                    clearTable(conn);
                    moveSettings(conn);
                    modifyCollectionTable(conn);
                    pb.setPaused(oldState);
                    LOG.info("Settings migration successfully finished");

                } catch (Exception e) {
                    LOG.error("Error migrating old tokens: ", e);
                    sce.getServletContext().setAttribute("globalMessage", "Error migrating settings, please check logs");
                    throw new RuntimeException("Settings migration failed, check logs for details", e);
                }
            } else {
                LOG.info("Skipping settings migration (already performed)");
                pb.setPaused(oldState);
            }

        } finally {
            sce.getServletContext().setAttribute("globalMessage", "");

            if (conn != null) {
                SQL.release(conn);
            }
            NDC.pop();
        }
    }

    private void moveSettings(Connection conn) throws SQLException {
        LOG.trace("Starting settings migration");
        String stmt = "SELECT ID,EMAILLIST,PROXYDATA,CHECKPERIOD,AUDITTOKENS FROM collection";
        String insertSt = "INSERT INTO settings(ATTR,VALUE,COLLECTION_ID) values (?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(stmt);
        PreparedStatement insertStmt = conn.prepareStatement(insertSt);
        ResultSet rs = pstmt.executeQuery();
        try {
            while (rs.next()) {
                long id = rs.getLong("ID");

                String emailList = rs.getString("EMAILLIST");
                if (!Strings.isEmpty(emailList)) {
                    performInsert(ConfigConstants.ATTR_EMAIL_RECIPIENTS, emailList, id, insertStmt);
                }

                boolean proxyData = rs.getBoolean("PROXYDATA");
                performInsert(ConfigConstants.ATTR_PROXY_DATA, Boolean.toString(proxyData), id, insertStmt);

                int checkperiod = rs.getInt("CHECKPERIOD");
                performInsert(ConfigConstants.ATTR_AUDIT_PERIOD, Integer.toString(checkperiod), id, insertStmt);

                boolean auditTokens = rs.getBoolean("AUDITTOKENS");
                performInsert(ConfigConstants.ATTR_AUDIT_TOKENS, Boolean.toString(auditTokens), id, insertStmt);

            }
        } finally {
            SQL.release(pstmt);
            SQL.release(insertStmt);
            SQL.release(rs);
            LOG.trace("Exiting settings migration");

        }

    }

    private void performInsert(String attr, String value, long id,
            PreparedStatement insertStmt) throws SQLException {
        insertStmt.clearParameters();
        insertStmt.setString(1, attr);
        insertStmt.setString(2, value);
        insertStmt.setLong(3, id);
        int result = insertStmt.executeUpdate();
        LOG.trace("Updated setting: " + attr + " value: " + value + " id: " + id + " results: " + 1);
        if (result != 1) {
            LOG.error("Attempt to insert new parameter failed, result = " + result);
            throw new RuntimeException("Attempt to insert new parameter failed, result = " + result);
        }
    }

    private boolean hasMigrated(Connection conn) throws SQLException {
        DatabaseMetaData dbm = conn.getMetaData();
        String types[] = {"TABLE"};
        boolean newTable = false;
        boolean migrated = true;

        ResultSet rs = dbm.getTables(null, null, null, types);

        // check for new table
        try {
            while (rs.next()) {
                String tabName = rs.getString("TABLE_NAME");
                if ("settings".equals(tabName)) {
                    LOG.info("Found new settings table");
                    newTable = true;
                }
            }
        } catch (SQLException e) {
            LOG.error("Error retrieving table metadata");
            throw e;
        } finally {
            SQL.release(rs);
        }

        if (!newTable) {
            LOG.info("SQL patch to 1.7+ has not been installed, table 'settings' does no exist, shutting down!");
            throw new IllegalStateException("SQL patch to 1.7+ has not been installed, table 'settings' does no exist");
        }

        // Do we have old columns?
        rs = dbm.getColumns(null, null, "collection", null);
        try {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                if ("AUDITTOKENS".equals(name)) {
                    LOG.info("Found old column name, migration triggered");
                    migrated = false;
                }
            }
        } catch (SQLException e) {
            LOG.error("Error retrieving table metadata");
            throw e;
        } finally {
            SQL.release(rs);
        }


        return migrated;
    }

    private void modifyCollectionTable(Connection conn) throws SQLException {
        LOG.trace("Removing old columns");
        String removeEmail = "ALTER TABLE collection DROP COLUMN EMAILLIST";
        String removeProxy = "ALTER TABLE collection DROP COLUMN PROXYDATA";
        String removePeriod = "ALTER TABLE collection DROP COLUMN CHECKPERIOD";
        String removeTokens = "ALTER TABLE collection DROP COLUMN AUDITTOKENS";
        PreparedStatement pstmt = null;

        try {
            LOG.trace("Removing EMAILLIST");
            pstmt = conn.prepareStatement(removeEmail);
            doRemove(pstmt);

            LOG.trace("Removing PROXYDATA");
            pstmt = conn.prepareStatement(removeProxy);
            doRemove(pstmt);

            LOG.trace("Removing CHECKPERIOD");
            pstmt = conn.prepareStatement(removePeriod);
            doRemove(pstmt);

            LOG.trace("Removing AUDITTOKENS");
            pstmt = conn.prepareStatement(removeTokens);
            doRemove(pstmt);
        } finally {
            LOG.trace("Exiting remove old columns");
        }
    }

    private void doRemove(PreparedStatement pstmt) throws SQLException {
        try {
            int result = pstmt.executeUpdate();
            LOG.trace("Remove finished: " + result);
            if (result < 1) {
                LOG.error("No column to remove, result: " + result);
                throw new RuntimeException("No column to remove, result: " + result);
            }
        } finally {
            SQL.release(pstmt);
        }
    }

    private void clearTable(Connection conn) throws SQLException {
        LOG.info("Starting settings clear");
        try {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM settings");
            int rows = pstmt.executeUpdate();
            LOG.info("Clean new settings table: " + rows);
            SQL.release(pstmt);
        } catch (SQLException e) {
            LOG.error("Error cleaning new settings table");
            throw e;
        } finally {
            LOG.info("Exiting settings clear");
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        //no-op
    }
}
