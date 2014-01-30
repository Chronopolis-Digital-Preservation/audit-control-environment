package edu.umiacs.ace.monitor.settings;

import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.sql.SQL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import org.apache.log4j.NDC;

/**
 *
 * @author shake
 */
public class SettingsMigrationContextListener implements ServletContextListener{

    public void contextInitialized(ServletContextEvent sce) {
        Connection conn = null;
        boolean migrated;

        NDC.push("[MIGRATION]");

        DataSource ds = PersistUtil.getDataSource();
        try {
            conn = ds.getConnection();
            migrated = hasMigrated(conn);
            if ( !migrated ) {
                SettingsUtil.updateSettings(SettingsUtil.getDefaultSettings());
            }
        } catch (SQLException ex) {
            Logger.getLogger(SettingsMigrationContextListener.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        if (conn != null) {
            SQL.release(conn);
        }
        NDC.pop();
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }

    private boolean hasMigrated(Connection conn) throws SQLException {
        DatabaseMetaData dmd = conn.getMetaData();
        String types[] = {"Table"};
        boolean migrated = false;
        boolean newTable = false;
        ResultSet rs = dmd.getTables(null, null, null, types);

        // Search for the new table
        while( rs.next() ) {
            String table = rs.getString("TABLE_NAME");
            if ( "system_settings".equals(table) ) {
                newTable = true;
            }
        }

        if (!newTable) {
            //LOG.info("SQL patch to 1.7+ has not been installed, table 'settings' does no exist, shutting down!");
            throw new IllegalStateException("SQL patch to 1.7+ has not been installed, table 'settings' does no exist");
        }

        // Find out if there are any elements in the table
        String search = "SELECT attr, value FROM system_settings";

        PreparedStatement pStmt = conn.prepareStatement(search);
        rs = pStmt.executeQuery();
        migrated = rs.first();
        SQL.release(rs);
        SQL.release(pStmt);

        return migrated;
    }

}
