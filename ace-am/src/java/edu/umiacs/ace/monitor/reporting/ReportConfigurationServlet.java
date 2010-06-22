/*
 * Copyright (c) 2007-2010, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ACE Components were written in the ADAPT Project at the University of
 * Maryland Institute for Advanced Computer Study.
 */
// $Id: ReportConfigurationServlet.java 3181 2010-06-16 20:46:16Z toaster $

package edu.umiacs.ace.monitor.reporting;

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.text.ParseException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.quartz.CronExpression;

/**
 * Servlet to configure automated generation of ReportSummarys .
 * email = email address list, seperated by ',' can be empty (optional)
 * name = descriptive name for this report (optional)
 * removeid = id of report to delete
 * month = month list to run
 * day = day of month to run
 * reportid = id of existing to modify, if empty this will create a new one
 * 
 *
 * @author toaster
 */
public class ReportConfigurationServlet extends EntityManagerServlet {

    private static final String PARAM_REMOVEID = "removeid";
    private static final String PARAM_REPORTID = "reportid";
    private static final String PARAM_MONTH = "month";
    private static final String PARAM_DAY = "day";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_SUBMIT = "submit";
    private static final String PAGE_REPORTS = "reportlist";
    private static final String PAGE_REPORT = "report";
    private static final String PAGE_DAY = "day";
    private static final String PAGE_COLLECTION = "collection";
    private static final Logger LOG = Logger.getLogger(
            ReportConfigurationServlet.class);

    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em ) throws ServletException, IOException {

        long removeid = getParameter(request, PARAM_REMOVEID, 0);
        ReportPolicy rp = getReportPolicy(request, em);
        Collection coll = null;
        if ( rp != null ) {
            coll = rp.getCollection();
        } else {
            coll = getCollection(request, em);
        }

        if ( removeid > 0 ) {
            ReportPolicy removePolicy = em.getReference(ReportPolicy.class,
                    removeid);
            LOG.debug("Removing report policy " + removePolicy.getName());
            if ( removePolicy != null ) {
                coll = removePolicy.getCollection();
                EntityTransaction trans = em.getTransaction();
                trans.begin();
                try {
                    SchedulerContextListener.removeJob(rp);
                    em.remove(removePolicy);
                    trans.commit();
                } catch ( Exception e ) {
                    LOG.error("Error removing report", e);
                }
            }
        } // are we saving a new or modifying existing
        else if ( !Strings.isEmpty(request.getParameter(PARAM_SUBMIT)) ) {

            String name = getParameter(request, PARAM_NAME, null);
            CronExpression ce = null;
            try {
                ce = extractCronString(request);
            } catch ( Exception e ) {
                LOG.error("Bad regex", e);
                throw new ServletException(e);
            }
            LOG.debug("Extracted cron string " + ce.getCronExpression());

            if ( rp != null ) {


                if ( !Strings.isEmpty(name) ) {
                    rp.setName(name);
                }
                rp.setEmailList(getParameter(request, PARAM_EMAIL, null));
                rp.setCronString(ce.getCronExpression());
                EntityTransaction trans = em.getTransaction();
                trans.begin();
                try {
                    em.merge(rp);
                    trans.commit();
                    SchedulerContextListener.removeJob(rp);
                    SchedulerContextListener.addJob(rp);
                } catch ( Exception e ) {
                    LOG.error("Error merging report", e);
                }

            } else {
                rp = new ReportPolicy();
                // if name is null && months are null, do not persist
                if ( Strings.isEmpty(name) ) {
                    name = "unnamed";
                }

                rp.setName(name);
                rp.setCronString(ce.getCronExpression());
                rp.setEmailList(getParameter(request, PARAM_EMAIL, null));
                if ( coll == null ) {
                    throw new ServletException(
                            "No collection included for report");
                }
                rp.setCollection(coll);

                EntityTransaction trans = em.getTransaction();
                trans.begin();
                try {
                    em.persist(rp);
                    trans.commit();
                    SchedulerContextListener.addJob(rp);
                } catch ( Exception e ) {
                    LOG.error("Error saving report", e);
                }
            }
            rp = null;
        }


        Query q = em.createNamedQuery("ReportPolicy.listByCollection");
        q.setParameter("coll", coll);

        request.setAttribute(PAGE_REPORTS, q.getResultList());
        request.setAttribute(PAGE_COLLECTION, coll);
        request.setAttribute(PAGE_REPORT, rp);
        if ( rp != null ) {
            LOG.debug("Shoving report policy into page context: " + rp.getName());
            // hack for now to extract day/month string and set months
            //TODO: this should be moved into a taglib if we decide to support
            // generic cron functionality.
//            request.setAttribute(name, );

            request.setAttribute(PAGE_DAY, extractDay(rp.getCronString()));
            for ( String s : extractMonthList(rp.getCronString()) ) {
                request.setAttribute(s, "checked");
            }
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(
                "reportpolicy.jsp");
        dispatcher.forward(request, response);
    }

    public String extractDay( String c ) {
        String cronList[] = c.split("\\s");
        return cronList[3];
    }

    public String[] extractMonthList( String c ) {
        String cronList[] = c.split("\\s");
        return cronList[4].split(",");
    }

    public CronExpression extractCronString( HttpServletRequest request ) throws ParseException {
        StringBuilder sb = new StringBuilder();

        sb.append("0 0 0 "); // s, m, h

        String day = getParameter(request, PARAM_DAY, null);
        LOG.trace("Attempting to extract day from " + day);
        if ( day != null && day.matches("^([012]?[0-9]{1}|L)$") ) {
            sb.append(day + " ");
        } else {
            throw new IllegalArgumentException(
                    day + " is not valid day, required 1-28 or L");
        }
        String[] monthList = getParameterList(request, PARAM_MONTH);
        if ( monthList != null ) {
            for ( String s : monthList ) {
                if ( s.matches(
                        "^(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)$") ) {
                    sb.append(s + ",");
                }

            }
            sb.deleteCharAt(sb.length() - 1);
        } else {
            throw new IllegalArgumentException("Must specify at least one month");
        }

        sb.append(" ? *");
        LOG.debug("String " + sb.toString());
        return new CronExpression(sb.toString());

    }

    public ReportPolicy getReportPolicy( HttpServletRequest request,
            EntityManager em ) {
        long reportId;

        if ( (reportId = getParameter(request, PARAM_REPORTID, 0)) > 0 ) {
            try {
                return em.getReference(ReportPolicy.class, reportId);
            } catch ( EntityNotFoundException e ) {
                return null;
            }
        }
        return null;
    }
}
