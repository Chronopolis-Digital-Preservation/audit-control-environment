package edu.umiacs.ace.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by shake on 9/11/15.
 */
public class CollectionThreadPoolContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        CollectionThreadPoolExecutor.getExecutor().shutdownNow();
    }
}
