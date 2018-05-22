/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.monitor.register;

import org.apache.log4j.NDC;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author shake
 */
public class IngestContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        NDC.push("[Ingest startup]");
        NDC.pop();
    }

    public void contextDestroyed(ServletContextEvent sce) {
        IngestThreadPool.shutdownPools();
    }

}
