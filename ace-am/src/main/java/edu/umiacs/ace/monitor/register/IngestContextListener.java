/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.monitor.register;

import edu.umiacs.ace.monitor.settings.SettingsParameter;
import edu.umiacs.ace.util.PersistUtil;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.NDC;

/**
 *
 * @author shake
 */
public class IngestContextListener implements ServletContextListener {
    public static final String PARAM_INGEST = "ingest.maxthreads";

    public void contextInitialized(ServletContextEvent sce) {
        NDC.push("[Ingest startup]");
        EntityManager em = PersistUtil.getEntityManager();
        Query threadQuery = em.createNamedQuery("SettingsParameter.getAttr");
        threadQuery.setParameter("attr", PARAM_INGEST);
        SettingsParameter s = (SettingsParameter) threadQuery.getSingleResult();
        IngestThreadPool.setMaxThreads(Integer.parseInt(s.getValue()));
        IngestThreadPool pool = IngestThreadPool.getInstance();
        //pool.start();

    }

    public void contextDestroyed(ServletContextEvent sce) {
        IngestThreadPool.shutdownPools();
    }

}
