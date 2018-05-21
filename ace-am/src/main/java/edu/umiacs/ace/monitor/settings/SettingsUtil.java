package edu.umiacs.ace.monitor.settings;

import edu.umiacs.ace.monitor.audit.AuditThreadFactory;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.reporting.SchedulerContextListener;
import edu.umiacs.ace.util.PersistUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author shake
 */
public class SettingsUtil {

    /**
     * Retrieve a Setting based off it's attribute name
     *
     * @param attr the attribute to query on
     * @return the Setting associated with the attribute
     */
    public static SettingsParameter getItemByAttr( String attr ) {
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("SettingsParameter.getAttr");
        q.setParameter("attr", attr);

        try {
            return (SettingsParameter) q.getSingleResult();
        } catch (NoResultException ex) {
            // zzz
        } finally {
            em.close();
        }

        return null;
    }

    /**
     * Get a list of all custom settings
     *
     * @return custom settings
     */
    public static List<SettingsParameter> getCustomSettings() {
        EntityManager em = PersistUtil.getEntityManager();
        TypedQuery<SettingsParameter> q = em.createNamedQuery(
                "SettingsParameter.getCustomSettings",
                SettingsParameter.class);
        try {
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get a list of all settings
     *
     * @return all  settings
     */
    public static List<SettingsParameter> getCurrentSettings() {
        EntityManager em = PersistUtil.getEntityManager();
        TypedQuery<SettingsParameter> q = em.createNamedQuery(
                "SettingsParameter.getCurrentSettings",
                SettingsParameter.class);
        try {
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Update a given List of settings
     *
     * @param settings the settings to update
     */
    public static void updateSettings(List<SettingsParameter> settings) {
        EntityManager em = PersistUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        trans.begin();

        for ( SettingsParameter setting : settings ) {
            // Skip any empty settings
            if ( setting.getName().trim().isEmpty() ||
                    setting.getValue().trim().isEmpty() ) {
                continue;
            }

            SettingsParameter old = getItemByAttr(setting.getName());

            // If there is no item, persist the new setting
            if ( old == null ) {
                em.persist(setting);
            } else {
                // Else update and merge the old item
                old.setValue(setting.getValue());
                em.merge(old);
            }
        }

        trans.commit();
        em.clear();

        reloadSettings();
    }

    /**
     * Delete a given List of settings
     *
     * @param settings the settings to delete
     */
    public static void deleteSettings(List<String> settings) {
        EntityManager em = PersistUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        trans.begin();

        for ( String name : settings ) {
            // TODO: Find a better way to do this
            //       Could possibly wrap the ID into the settings list
            SettingsParameter setting = getItemByAttr(name);
            SettingsParameter managedSetting =
                    em.find(SettingsParameter.class, setting.getId());

            if ( setting != null ) {
                em.remove(managedSetting);
            }
        }

        trans.commit();
        em.close();
    }

    // Get the names of all current settings
    public static Set<String> getParamNames() {
        List<SettingsParameter> settings = getCurrentSettings();
        Set<String> paramSet = new HashSet<>();
        for ( SettingsParameter s : settings ) {
            paramSet.add(s.getName());
        }
        return paramSet;
    }


    /**
     * Get the default values for all settings
     *
     * @return the default settings
     */
    public static List<SettingsParameter> getDefaultSettings() {
        List<SettingsParameter> defaults = new ArrayList<>();

        defaults.add(new SettingsParameter(SettingsConstants.PARAM_IMS,
                SettingsConstants.ims,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_IMS_PORT,
                SettingsConstants.imsPort,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_IMS_SSL,
                SettingsConstants.imsSSL,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_IMS_TOKEN_CLASS,
                SettingsConstants.imsTokenClass,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_AUTO_AUDIT_ENABLE,
                SettingsConstants.autoAudit,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_THROTTLE_MAXAUDIT,
                SettingsConstants.maxAudit,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_TIME,
                SettingsConstants.throttleWait,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_BPS,
                SettingsConstants.throttleBPS,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_SMTP_SERVER,
                SettingsConstants.mailServer,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_FROM,
                SettingsConstants.mailFrom,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_USER_AUTH,
                SettingsConstants.authManagement,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_4J_APPENDER,
                SettingsConstants.log4JA1,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_4J_BACKUP_INDEX,
                SettingsConstants.log4JA1MaxBackupIndex,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_4J_CLASS,
                SettingsConstants.log4JLoggerUMIACS,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_4J_CONV_PAT,
                SettingsConstants.log4JA1layoutConversationPattern,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_4J_FILE,
                SettingsConstants.log4JA1File,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_4J_FILE_SIZE,
                SettingsConstants.log4JA1MaxFileSize,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_4J_IRODS,
                SettingsConstants.log4JLoggerIrods,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_4J_LAYOUT,
                SettingsConstants.log4JA1Layout,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_4J_ROOT_LOGGER,
                SettingsConstants.log4JRootLogger,false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_INGEST,
                SettingsConstants.maxIngestThreads, false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_AUDIT_ONLY,
                SettingsConstants.auditOnly, false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_AUDIT_SAMPLE,
                SettingsConstants.auditSample, false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_AUDIT_BLOCKING,
                SettingsConstants.auditBlocking, false));
        defaults.add(new SettingsParameter(SettingsConstants.PARAM_AUDIT_MAX_BLOCK_TIME,
                SettingsConstants.auditMaxBlockTime, false));

        return defaults;
    }

    /**
     *
     * @param c The collection to query
     * @param attr The attribute to query for
     * @return true if collection, settings are not null and parameter is "true"
     */
    public static boolean getBoolean(Collection c, String attr) {
        if (!containsKey(c, attr)) {
            return false;
        }

        return "true".equalsIgnoreCase(c.getSettings().get(attr));
    }

    public static String getString(Collection c, String attr) {
        if (!containsKey(c, attr)) {
            return null;
        }

        return c.getSettings().get(attr);
    }

    public static int getInt(Collection c, String attr, int def) {
        if (!containsKey(c, attr)) {
            return def;
        }
        try {
            return Integer.parseInt(c.getSettings().get(attr));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static boolean containsKey(Collection c, String attr) {
        return (c != null && c.getSettings() != null
                && c.getSettings().containsKey(attr));
    }

    // Update the settings our context listeners would normally do
    private static void reloadSettings() {
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("SettingsParameter.getAttr");
        SettingsParameter s = null;

        // Host
        q.setParameter("attr", SettingsConstants.PARAM_IMS);
        s = (SettingsParameter) q.getSingleResult();
        AuditThreadFactory.setIMS(s.getValue());

        // Port
        q.setParameter("attr", SettingsConstants.PARAM_IMS_PORT);
        s = (SettingsParameter) q.getSingleResult();
        AuditThreadFactory.setImsPort(Integer.parseInt(s.getValue()));

        // SSL
        q.setParameter("attr", SettingsConstants.PARAM_IMS_SSL);
        s = (SettingsParameter) q.getSingleResult();
        AuditThreadFactory.setSSL(Boolean.valueOf(s.getValue()));

        // Token Class
        q.setParameter("attr", SettingsConstants.PARAM_IMS_TOKEN_CLASS);
        s = (SettingsParameter) q.getSingleResult();
        AuditThreadFactory.setTokenClass(s.getValue());

        // Audit Only
        q.setParameter("attr", SettingsConstants.PARAM_AUDIT_ONLY);
        s = (SettingsParameter) q.getSingleResult();
        AuditThreadFactory.setAuditOnly(Boolean.valueOf(s.getValue()));

        // Max Audits
        /* This idles all threads, and can get messy if we actually want to do it properly
           (drain the thread pool, etc). Basically, let's keep the count on start up only.
        q.setParameter("attr", SettingsConstants.PARAM_THROTTLE_MAXAUDIT);
        s = (SettingsParameter) q.getSingleResult();
        if (Strings.isValidInt(s.getValue())) {
            AuditThreadFactory.setMaxAudits(Integer.parseInt(s.getValue()));
        }
        */

        // Mail Server
        q.setParameter("attr", SettingsConstants.PARAM_SMTP_SERVER);
        s = (SettingsParameter) q.getSingleResult();
        SchedulerContextListener.setMailServer(s.getValue());

        // Mail From
        q.setParameter("attr", SettingsConstants.PARAM_FROM);
        s = (SettingsParameter) q.getSingleResult();
        SchedulerContextListener.setMailFrom(s.getValue());

        // Audit Blocking
        q.setParameter("attr", SettingsConstants.PARAM_AUDIT_BLOCKING);
        s = (SettingsParameter) q.getSingleResult();
        AuditThreadFactory.setBlocking(Boolean.valueOf(s.getValue()));

        // Max block time
        q.setParameter("attr", SettingsConstants.PARAM_AUDIT_MAX_BLOCK_TIME);
        s = (SettingsParameter) q.getSingleResult();
        AuditThreadFactory.setMaxBlockTime(Integer.parseInt(s.getValue()));
    }

    /**
     * Get a SettingsParameter by its attribute name or set the default value and persist it
     *
     * @param attr the Attribute to query for
     * @param defaultValue the default value of the attribute
     * @param em the EntityManager to query with
     * @return the queried SettingsParameter
     */
    public static SettingsParameter getOrDefault(String attr,
                                                 String defaultValue,
                                                 EntityManager em) {
        SettingsParameter result = new SettingsParameter(attr, defaultValue, false);
        TypedQuery<SettingsParameter> q = em.createNamedQuery("SettingsParameter.getAttr",
                SettingsParameter.class);
        q.setParameter("attr", attr);

        try {
            result = q.getSingleResult();
            // just in case
            if (result.getValue() == null || result.getValue().isEmpty()) {
                result.setValue(defaultValue);
            }
        } catch (NoResultException ex) {
            em.getTransaction().begin();
            em.persist(result);
            em.getTransaction().commit();
        }

        return result;
    }
}