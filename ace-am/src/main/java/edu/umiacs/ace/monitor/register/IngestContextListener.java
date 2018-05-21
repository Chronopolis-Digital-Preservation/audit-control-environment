/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.monitor.register;

import edu.umiacs.ace.monitor.settings.SettingsConstants;
import edu.umiacs.ace.monitor.settings.SettingsParameter;
import edu.umiacs.ace.monitor.settings.SettingsUtil;
import edu.umiacs.ace.util.PersistUtil;
import org.apache.log4j.NDC;

import javax.persistence.EntityManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static edu.umiacs.ace.monitor.settings.SettingsConstants.PARAM_INGEST;

/**
 *
 * @author shake
 */
public class IngestContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        NDC.push("[Ingest startup]");
        EntityManager em = PersistUtil.getEntityManager();
        SettingsParameter ingestSettings = SettingsUtil.getOrDefault(PARAM_INGEST,
                SettingsConstants.maxIngestThreads, em);
        IngestThreadPool.setMaxThreads(Integer.parseInt(ingestSettings.getValue()));
        NDC.pop();
    }

    public void contextDestroyed(ServletContextEvent sce) {
        IngestThreadPool.shutdownPools();
    }

}
