package edu.umiacs.ace.monitor.settings;

import edu.umiacs.ace.monitor.audit.AuditThreadFactory;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.reporting.SchedulerContextListener;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.util.Strings;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

import static edu.umiacs.ace.monitor.settings.SettingsConstants.*;

/**
 * @author shake
 */
public class SettingsUtil {

    /**
     * Retrieve a Setting based off it's attribute name
     *
     * @param attr the attribute to query on
     * @return the Setting associated with the attribute
     */
    public static SettingsParameter getItemByAttr(String attr) {
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("SettingsParameter.getAttr");
        q.setParameter("attr", attr);

        try {
            return (SettingsParameter) q.getSingleResult();
        } catch (NoResultException ex) {
            // ignore, return null
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

        for (SettingsParameter setting : settings) {
            // Skip any empty settings
            if (setting.getName().trim().isEmpty() ||
                    setting.getValue().trim().isEmpty()) {
                continue;
            }

            SettingsParameter old = getItemByAttr(setting.getName());

            // If there is no item, persist the new setting
            if (old == null) {
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

        for (String name : settings) {
            // TODO: Find a better way to do this
            //       Could possibly wrap the ID into the settings list
            SettingsParameter setting = getItemByAttr(name);
            SettingsParameter managedSetting =
                    em.find(SettingsParameter.class, setting.getId());

            if (setting != null) {
                em.remove(managedSetting);
            }
        }

        trans.commit();
        em.close();
    }


    /**
     * Get the default values for all settings
     *
     * @return the default settings
     */
    public static List<SettingsParameter> getDefaultSettings() {
        List<SettingsParameter> defaults = new ArrayList<>();

        defaults.add(new SettingsParameter(PARAM_IMS, ims));
        defaults.add(new SettingsParameter(PARAM_IMS_PORT, imsPort));
        defaults.add(new SettingsParameter(PARAM_IMS_SSL, imsSSL));
        defaults.add(new SettingsParameter(PARAM_IMS_TOKEN_CLASS, imsTokenClass));
        defaults.add(new SettingsParameter(PARAM_AUTO_AUDIT_ENABLE, autoAudit));
        defaults.add(new SettingsParameter(PARAM_THROTTLE_MAXAUDIT, maxAudit));
        defaults.add(new SettingsParameter(PARAM_TIME, throttleWait));
        defaults.add(new SettingsParameter(PARAM_BPS, throttleBPS));
        defaults.add(new SettingsParameter(PARAM_SMTP_SERVER, mailServer));
        defaults.add(new SettingsParameter(PARAM_FROM, mailFrom));
        defaults.add(new SettingsParameter(PARAM_USER_AUTH, authManagement));
        defaults.add(new SettingsParameter(PARAM_4J_APPENDER, log4JA1));
        defaults.add(new SettingsParameter(PARAM_4J_BACKUP_INDEX, log4JA1MaxBackupIndex));
        defaults.add(new SettingsParameter(PARAM_4J_CLASS, log4JLoggerUMIACS));
        defaults.add(new SettingsParameter(PARAM_4J_CONV_PAT, log4JA1layoutConversationPattern));
        defaults.add(new SettingsParameter(PARAM_4J_FILE, log4JA1File));
        defaults.add(new SettingsParameter(PARAM_4J_FILE_SIZE, log4JA1MaxFileSize));
        defaults.add(new SettingsParameter(PARAM_4J_IRODS, log4JLoggerIrods));
        defaults.add(new SettingsParameter(PARAM_4J_LAYOUT, log4JA1Layout));
        defaults.add(new SettingsParameter(PARAM_4J_ROOT_LOGGER, log4JRootLogger));
        // defaults.add(new SettingsParameter(PARAM_INGEST, maxIngestThreads));
        defaults.add(new SettingsParameter(PARAM_AUDIT_ONLY, auditOnly));
        defaults.add(new SettingsParameter(PARAM_AUDIT_SAMPLE, auditSample));
        defaults.add(new SettingsParameter(PARAM_AUDIT_BLOCKING, auditBlocking));
        defaults.add(new SettingsParameter(PARAM_IMS_MAX_RETRY, imsMaxRetry));
        defaults.add(new SettingsParameter(PARAM_IMS_RESET_TIMEOUT, imsResetTimeout));

        return defaults;
    }

    /**
     * @param c    The collection to query
     * @param attr The attribute to query for
     * @return true if collection, settings are not null and parameter is "true"
     */
    public static boolean getBoolean(Collection c, String attr) {
        if (notContains(c, attr)) {
            return false;
        }

        return "true".equalsIgnoreCase(c.getSettings().get(attr));
    }

    public static String getString(Collection c, String attr) {
        if (notContains(c, attr)) {
            return null;
        }

        return c.getSettings().get(attr);
    }

    public static int getInt(Collection c, String attr, int def) {
        if (notContains(c, attr)) {
            return def;
        }
        try {
            return Integer.parseInt(c.getSettings().get(attr));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static boolean notContains(Collection c, String attr) {
        return (c == null || c.getSettings() == null
                || !c.getSettings().containsKey(attr));
    }

    // Update the settings our context listeners would normally do
    private static void reloadSettings() {
        EntityManager em = PersistUtil.getEntityManager();
        SettingsParameter s;

        // Host
        s = getOrDefault(PARAM_IMS, ims, em);
        AuditThreadFactory.setIMS(s.getValue());

        // Port
        s = getOrDefault(PARAM_IMS_PORT, imsPort, em);
        AuditThreadFactory.setImsPort(Integer.parseInt(s.getValue()));

        // SSL
        s = getOrDefault(PARAM_IMS_SSL, imsSSL, em);
        AuditThreadFactory.setSSL(Boolean.valueOf(s.getValue()));

        // Token Class
        s = getOrDefault(PARAM_IMS_TOKEN_CLASS, imsTokenClass, em);
        AuditThreadFactory.setTokenClass(s.getValue());

        // Audit Only
        s = getOrDefault(PARAM_AUDIT_ONLY, auditOnly, em);
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
        s = getOrDefault(PARAM_SMTP_SERVER, mailServer, em);
        SchedulerContextListener.setMailServer(s.getValue());

        // Mail From
        s = getOrDefault(PARAM_FROM, mailFrom, em);
        SchedulerContextListener.setMailFrom(s.getValue());

        // Audit Blocking
        s = getOrDefault(PARAM_AUDIT_BLOCKING, auditBlocking, em);
        AuditThreadFactory.setBlocking(Boolean.valueOf(s.getValue()));

        s = getOrDefault(PARAM_IMS_MAX_RETRY, imsMaxRetry, em);
        if (Strings.isNonNegativeInt(s.getValue())) {
            AuditThreadFactory.setImsRetryAttempts(Integer.parseInt(s.getValue()));
        }

        s = getOrDefault(PARAM_IMS_RESET_TIMEOUT, imsResetTimeout, em);
        if (Strings.isNonNegativeInt(s.getValue())) {
            AuditThreadFactory.setImsResetTimeout(Integer.parseInt(s.getValue()));
        }
    }

    /**
     * Get a SettingsParameter by its attribute name or set the default value and persist it
     *
     * @param attr         the Attribute to query for
     * @param defaultValue the default value of the attribute
     * @param em           the EntityManager to query with
     * @return the queried SettingsParameter
     */
    public static SettingsParameter getOrDefault(String attr,
                                                 String defaultValue,
                                                 EntityManager em) {
        SettingsParameter result = new SettingsParameter(attr, defaultValue, false);
        TypedQuery<SettingsParameter> q = em.createNamedQuery(
                "SettingsParameter.getAttr",
                SettingsParameter.class);
        q.setParameter("attr", attr);

        try {
            result = q.getSingleResult();
            // just in case
            if (result.getValue() == null || result.getValue().isEmpty()) {
                em.getTransaction().begin();
                result.setValue(defaultValue);
                em.merge(result);
                em.getTransaction().commit();
            }
        } catch (NoResultException ex) {
            em.getTransaction().begin();
            em.persist(result);
            em.getTransaction().commit();
        }

        return result;
    }
}