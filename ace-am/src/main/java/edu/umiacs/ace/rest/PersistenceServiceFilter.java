/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.rest;

import edu.umiacs.ace.util.PersistUtil;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.log4j.Logger;

/**
 * A filter called when any ACE rest service is invoked. This replaces the older
 * EntityManagerServlet model.
 *
 * This filter will take care of closing any open entity manager
 * 
 * @author toaster
 */
public class PersistenceServiceFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(PersistenceServiceFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Throwable problem = null;
        try {
            chain.doFilter(request, response);
        } catch (Throwable t) {
            problem = t;
        } finally {
            PersistUtil.closeLocalManager();
        }
        if (problem != null) {
            if (problem instanceof ServletException) {
                throw (ServletException) problem;
            }
            if (problem instanceof IOException) {
                throw (IOException) problem;
            }
            LOG.error("Unexpected error in filter: ", problem);
            throw new IOException(problem);
        }


    }

    public void destroy() {
    }
}
