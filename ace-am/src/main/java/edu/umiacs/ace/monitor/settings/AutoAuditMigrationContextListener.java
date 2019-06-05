package edu.umiacs.ace.monitor.settings;

import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.sql.SQL;
import edu.umiacs.util.Check;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static edu.umiacs.ace.monitor.settings.SettingsConstants.PARAM_IMS_MAX_RETRY;
import static edu.umiacs.ace.monitor.settings.SettingsConstants.PARAM_IMS_RESET_TIMEOUT;
import static edu.umiacs.ace.monitor.settings.SettingsConstants.imsMaxRetry;
import static edu.umiacs.ace.monitor.settings.SettingsConstants.imsResetTimeout;

/**
 * Context listener to migrate audit/ims settings
 * auto.audit.disable to auto.audit.enable
 * audit.max.block.time to ims.max.retry
 * add ims.reset.timeout
 * <p>
 * Created by shake on 4/5/17.
 */
public class AutoAuditMigrationContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AutoAuditMigrationContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        NDC.push("[MIGRATION] ");
        DataSource ds = PersistUtil.getDataSource();
        try (Connection conn = ds.getConnection()) {
            migrateAutoAudit(conn);
            migrateAuditBlocking(conn);
        } catch (Exception e) {
            LOG.error("Error migrating audit settings", e);
        } finally {
            NDC.pop();
        }

    }

    /**
     * Migrate from auto.audit.disable to auto.audit.enable
     *
     * @param conn The database connection
     * @throws SQLException if there's an exception communicating with the database
     */
    private void migrateAutoAudit(Connection conn) throws SQLException {
        String query = "SELECT id, value, custom FROM system_settings WHERE attr = 'auto.audit.disable'";
        try (PreparedStatement statement = conn.prepareStatement(query);
             ResultSet set = statement.executeQuery()) {
            while (set.next()) {
                Long id = set.getLong(1);
                Boolean value = Boolean.valueOf(set.getString(2));
                Boolean custom = set.getBoolean(3);
                if (!custom) {
                    LOG.info("Found auto.audit setting to migrate from " + value + " -> " + !value);
                    createNew(conn, "auto.audit.enable", String.valueOf(!value));
                    delete(conn, id);
                }
            }
        }
    }

    /**
     * Check if audit.max.block.time exists. If true, add ims.max.retry and ims.reset.timeout and
     * remove audit.max.block.time
     *
     * @param conn The database connection
     * @throws SQLException if there's an exception communicating with the database
     */
    private void migrateAuditBlocking(Connection conn) throws SQLException {
        String query = "SELECT id FROM system_settings WHERE attr = 'audit.max.block.time'";
        try (PreparedStatement statement = conn.prepareStatement(query);
             ResultSet set = statement.executeQuery()) {
            while (set.next()) {
                Long id = set.getLong(1);
                LOG.info("Found audit.max.block.time setting to migrate to ims.max.retry");
                createNew(conn, PARAM_IMS_MAX_RETRY, imsMaxRetry);
                createNew(conn, PARAM_IMS_RESET_TIMEOUT, imsResetTimeout);
                delete(conn, id);
            }
        }
    }

    private void delete(Connection conn, Long id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("DELETE FROM system_settings WHERE id = " + id);
        statement.executeUpdate();
        SQL.release(statement);
    }

    private void createNew(Connection conn, String attr, String value) throws SQLException {
        Check.notNull("attr", attr);
        Check.notNull("value", value);

        String query = "INSERT INTO system_settings VALUES (DEFAULT, '%s', '%s', 0)";
        try (PreparedStatement statement =
                     conn.prepareStatement(String.format(query, attr, value))) {
            statement.executeUpdate();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
