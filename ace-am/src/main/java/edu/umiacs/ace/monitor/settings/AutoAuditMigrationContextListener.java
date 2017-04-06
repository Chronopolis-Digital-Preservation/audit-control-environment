package edu.umiacs.ace.monitor.settings;

import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.sql.SQL;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Context listener to migrate from auto.audit.disable to auto.audit.enable
 *
 * Created by shake on 4/5/17.
 */
public class AutoAuditMigrationContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AutoAuditMigrationContextListener.class);

    private ResultSet set;
    private Connection conn;
    private PreparedStatement statement;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Connection conn = null;
        DataSource ds = PersistUtil.getDataSource();
        try {
            conn = ds.getConnection();
            migrate(conn);
        } catch (Exception e) {
            LOG.error("[MIGRATION] Error migrating audo.audit.disable setting", e);
        } finally {
            release();

        }

    }

    private void migrate(Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT id, value, custom FROM system_settings WHERE attr = 'auto.audit.disable'");
        ResultSet set = statement.executeQuery();
        while (set.next()) {
            Long id = set.getLong(1);
            Boolean value = Boolean.valueOf(set.getString(2));
            Boolean custom = set.getBoolean(3);
            if (!custom) {
                LOG.info("[MIGRATION] Found auto.audit setting to migrate from " + value + " -> " + !value);
                createNew(conn, !value);
                delete(conn, id);
            }
        }
    }

    private void delete(Connection conn, Long id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("DELETE FROM system_settings WHERE id = " + id);
        statement.executeUpdate();
        SQL.release(statement);
    }

    private void createNew(Connection conn, Boolean value) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("INSERT INTO system_settings VALUES (DEFAULT, 'auto.audit.enable','" + value.toString() + "',0)");
        statement.executeUpdate();
        SQL.release(statement);
    }

    private void release() {
        if (conn != null) {
            SQL.release(conn);
        }
        if (statement != null) {
            SQL.release(statement);
        }
        if (set != null) {
            SQL.release(set);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
