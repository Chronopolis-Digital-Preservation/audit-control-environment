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
package edu.umiacs.ace.monitor.core;

import edu.umiacs.ace.ims.api.IMSUtil;
import edu.umiacs.ace.ims.ws.TokenResponse;
import edu.umiacs.ace.monitor.audit.AuditConfigurationContext;
import edu.umiacs.ace.monitor.audit.AuditConfigurationContext.PauseBean;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.sql.SQL;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 * Token migration listner to migrate tokens from the older pre-1.6 version to the current 1.6+
 * version. This will only run once and will stop any auditing from occurring until finished
 * 
 * @author toaster
 */
public class TokenMigrationContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(TokenMigrationContextListener.class);

    @Override
    public void contextInitialized( ServletContextEvent sce ) {
        PauseBean pb =
                (PauseBean) sce.getServletContext().getAttribute(AuditConfigurationContext.ATTRIBUTE_PAUSE);

        NDC.push("[MIGRATION]");
        Connection conn = null;
        boolean migrated;

        try {
            if ( pb == null ) {
                LOG.error("Could not pause auditing (pb null), stopping application to prevent damage");
                throw new RuntimeException("PauseBean not present in servlet context");
            }

            boolean oldState = pb.isPaused();
            pb.setPaused(true);
            DataSource ds = PersistUtil.getDataSource();


            try {
                conn = ds.getConnection();
                migrated = hasMigrated(conn);
            } catch ( SQLException e ) {
                LOG.fatal("Error getting SQL connection or table data", e);
                throw new RuntimeException("Error grabbing SQL connection or table data", e);
            }


            if ( !migrated ) {
                LOG.info("Token Migration starting");
                sce.getServletContext().setAttribute("globalMessage", "Auditing Paused: Token migration in progress");
                try {

                    clearTable(conn);
                    moveTokens(conn);
                    dropOldTable(conn);
                    pb.setPaused(oldState);
                    LOG.info("Token migration successfully finished");

                } catch ( Exception e ) {
                    LOG.error("Error migrating old tokens: ", e);
                    sce.getServletContext().setAttribute("globalMessage", "Error migrating tokens, please check logs");
                }
            } else {
                LOG.info("Skipping token migration (already performed)");
                pb.setPaused(oldState);
            }
        } finally {
            sce.getServletContext().setAttribute("globalMessage", "");

            if ( conn != null ) {
                SQL.release(conn);
            }
            NDC.pop();
        }
    }

    @Override
    public void contextDestroyed( ServletContextEvent sce ) {
    }

    private boolean hasMigrated( Connection conn ) throws SQLException {
        DatabaseMetaData dbm = conn.getMetaData();
        String types[] = {"TABLE"};
        boolean newTable = false;
        boolean migrated = true;

        ResultSet rs = dbm.getTables(null, null, null, types);
        try {
            while ( rs.next() ) {
                String tabName = rs.getString("TABLE_NAME");
                if ( "token".equals(tabName) ) {
                    LOG.info("Old token table still exists, migration triggered");
                    migrated = false;
                }
                if ( "acetoken".equals(tabName) ) {
                    LOG.info("Found new acetoken table");
                    newTable = true;
                }
            }
        } catch ( SQLException e ) {
            LOG.error("Error retrieving table metadata");
            throw e;
        } finally {
            SQL.release(rs);
        }
        
        if ( !newTable ) {
            LOG.info("SQL patch to 1.6+ has not been installed, table 'acetoken' does no exist, shutting down!");
            throw new IllegalStateException("SQL patch to 1.6+ has not been installed, table 'acetoken' does no exist");
        }
        return migrated;
    }

    private void dropOldTable( Connection conn ) throws SQLException {
        LOG.info("Starting old token table drop");
        try {
            PreparedStatement pstmt = conn.prepareStatement("DROP table token");
            pstmt.executeUpdate();
            SQL.release(pstmt);
        } catch ( SQLException e ) {
            LOG.error("Error dropping old table ");
            throw e;

        } finally {
            LOG.info("Exiting old token table drop");
        }
    }

    /**
     * 
     *
     * @param conn
     * @throws SQLException
     */
    private void moveTokens( Connection conn ) throws SQLException, IOException, ClassNotFoundException {
        LOG.info("Starting Token Migration");
        try {
            DataSource ds = PersistUtil.getDataSource();
            Connection writeConn = ds.getConnection();
            writeConn.setAutoCommit(false);

            PreparedStatement writeStmt =
                    writeConn.prepareStatement("insert into acetoken(id,createdate,valid,"
                    + "lastvalidated,prooftext,imsservice,proofalgorithm,round,parentcollection_id) values(?,?,?,?,?,?,?,?,?)");

            PreparedStatement pst = conn.prepareStatement("select count(*) from token");
            ResultSet rs = pst.executeQuery();
            rs.next();
            long total = rs.getLong(1);

            SQL.release(rs);
            SQL.release(pst);

            pst =
                    conn.prepareStatement("select id,createdate,valid,lastvalidated,"
                    + "java_token_response,parentcollection_id from token");
            pst.setFetchSize(Integer.MIN_VALUE);

            try {
                rs = pst.executeQuery();
                int i = 0;
                while ( rs.next() ) {
                    TokenResponse tr = readBlob(rs.getBlob("java_token_response"));
                    writeStmt.setLong(1, rs.getLong("id"));
                    writeStmt.setDate(2, rs.getDate("createdate"));
                    writeStmt.setBoolean(3, rs.getBoolean("valid"));
                    writeStmt.setDate(4, rs.getDate("lastvalidated"));
                    writeStmt.setString(5, IMSUtil.formatProof(tr));
                    writeStmt.setString(6, tr.getTokenClassName());
                    writeStmt.setString(7, tr.getDigestService());
                    writeStmt.setLong(8, tr.getRoundId());
                    writeStmt.setLong(9, rs.getByte("parentcollection_id"));
                    writeStmt.addBatch();
                    i++;
                    if ( i % 10000 == 0 ) {
                        LOG.info("Migrated: " + i + "/" + total + " tokens processed: "
                                + writeStmt.executeBatch().length);
                        writeConn.commit();
                    }
                }
                LOG.info("Migrated: " + i + "/" + total + " tokens processed: "
                        + writeStmt.executeBatch().length);
                writeConn.commit();

                LOG.info("Entried processed: " + i);
            } catch ( SQLException e ) {
                writeConn.rollback();
                LOG.error("SQL Exception while moving tokens");
                throw e;
            } finally {
                SQL.release(rs);
                SQL.release(pst);
                SQL.release(writeStmt);
                SQL.release(writeConn);
            }
        } finally {
            LOG.info("Exiting Token Migration");
        }
    }

    private TokenResponse readBlob( Blob blob ) throws SQLException, IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(blob.getBinaryStream());
        TokenResponse resp = (TokenResponse) ois.readObject();
        ois.close();
        return resp;
    }

    private void clearTable( Connection conn ) throws SQLException {
        LOG.info("Starting acetoken clear");
        try {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM acetoken");
            int rows = pstmt.executeUpdate();
            LOG.info("Clean new token table: " + rows);
            SQL.release(pstmt);
        } catch ( SQLException e ) {
            LOG.error("Error cleaning new acetoken table");
            throw e;
        } finally {
            LOG.info("Exiting acetoken clear");
        }
    }
}
