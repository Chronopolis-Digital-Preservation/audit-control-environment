package edu.umiacs.ace.monitor.settings;

import edu.umiacs.ace.util.PersistUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 *
 * @author shake
 */
public class SettingsUtil {

    public static SettingsParameter getItemByAttr( String attr ) {
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("SettingsParameter.getAttr");
        q.setParameter("attr", attr);

        try {
            return (SettingsParameter) q.getSingleResult();
        } catch (NoResultException ex) {
            // zzz
        }

        return null;
    }

    public static List<SettingsParameter> getCustomSettings() {
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("SettingsParameter.getCustomSettings");

        return q.getResultList();
    }

    public static List<SettingsParameter> getCurrentSettings() {
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("SettingsParameter.getCurrentSettings");

        return q.getResultList();
    }

    public static void updateSettings(Map<String, String> settings, boolean isCustom) {
        EntityManager em = PersistUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        trans.begin();

        for ( String name : settings.keySet() ) {
            // Skip any empty strings
            if (name.trim().isEmpty() || settings.get(name).trim().isEmpty() ) {
                continue;
            }


            SettingsParameter item = getItemByAttr(name);
            if ( item == null ) {
                em.persist(new SettingsParameter(name, settings.get(name), isCustom));
            } else {
                item.setValue(settings.get(name));
                em.merge(item);
            }
        }

        trans.commit();
        em.clear();
    }

    public static Set<String> getParamSet() {
        Set<String> paramSet = new HashSet<String>();
        paramSet.add(SettingsConstants.PARAM_4J_APPENDER);
        paramSet.add(SettingsConstants.PARAM_4J_BACKUP_INDEX);
        paramSet.add(SettingsConstants.PARAM_4J_CLASS);
        paramSet.add(SettingsConstants.PARAM_4J_CONV_PAT);
        paramSet.add(SettingsConstants.PARAM_4J_FILE);
        paramSet.add(SettingsConstants.PARAM_4J_FILE_SIZE);
        paramSet.add(SettingsConstants.PARAM_4J_IRODS);
        paramSet.add(SettingsConstants.PARAM_4J_LAYOUT);
        paramSet.add(SettingsConstants.PARAM_4J_ROOT_LOGGER);
        paramSet.add(SettingsConstants.PARAM_BPS);
        paramSet.add(SettingsConstants.PARAM_DISABLE_AUDIT);
        paramSet.add(SettingsConstants.PARAM_FROM);
        paramSet.add(SettingsConstants.PARAM_IMS);
        paramSet.add(SettingsConstants.PARAM_IMS_PORT);
        paramSet.add(SettingsConstants.PARAM_IMS_TOKEN_CLASS);
        paramSet.add(SettingsConstants.PARAM_SMTP_SERVER);
        paramSet.add(SettingsConstants.PARAM_THROTTLE_MAXAUDIT);
        paramSet.add(SettingsConstants.PARAM_TIME);
        paramSet.add(SettingsConstants.PARAM_USER_AUTH);
        return paramSet;
    }

    public static Map<String, String> getDefaultMap() {
        Map<String, String> paramMap = new HashMap<String, String>();

        paramMap.put(SettingsConstants.PARAM_IMS,
                SettingsConstants.ims);
        paramMap.put(SettingsConstants.PARAM_IMS_PORT,
                SettingsConstants.imsPort);
        paramMap.put(SettingsConstants.PARAM_IMS_TOKEN_CLASS,
                SettingsConstants.imsTokenClass);
        paramMap.put(SettingsConstants.PARAM_DISABLE_AUDIT,
                SettingsConstants.autoAudit);
        paramMap.put(SettingsConstants.PARAM_THROTTLE_MAXAUDIT,
                SettingsConstants.maxAudit);
        paramMap.put(SettingsConstants.PARAM_TIME,
                SettingsConstants.throttleWait);
        paramMap.put(SettingsConstants.PARAM_BPS,
                SettingsConstants.throttleBPS);
        paramMap.put(SettingsConstants.PARAM_SMTP_SERVER,
                SettingsConstants.mailServer);
        paramMap.put(SettingsConstants.PARAM_FROM,
                SettingsConstants.mailFrom);
        paramMap.put(SettingsConstants.PARAM_USER_AUTH,
                SettingsConstants.authManagement);
        paramMap.put(SettingsConstants.PARAM_4J_APPENDER,
                SettingsConstants.log4JA1);
        paramMap.put(SettingsConstants.PARAM_4J_BACKUP_INDEX,
                SettingsConstants.log4JA1MaxBackupIndex);
        paramMap.put(SettingsConstants.PARAM_4J_CLASS,
                SettingsConstants.log4JLoggerUMIACS);
        paramMap.put(SettingsConstants.PARAM_4J_CONV_PAT,
                SettingsConstants.log4JA1layoutConversationPattern);
        paramMap.put(SettingsConstants.PARAM_4J_FILE,
                SettingsConstants.log4JA1File);
        paramMap.put(SettingsConstants.PARAM_4J_FILE_SIZE,
                SettingsConstants.log4JA1MaxFileSize);
        paramMap.put(SettingsConstants.PARAM_4J_IRODS,
                SettingsConstants.log4JLoggerIrods);
        paramMap.put(SettingsConstants.PARAM_4J_LAYOUT,
                SettingsConstants.log4JA1Layout);
        paramMap.put(SettingsConstants.PARAM_4J_ROOT_LOGGER,
                SettingsConstants.log4JRootLogger);


        return paramMap;
    }
}
