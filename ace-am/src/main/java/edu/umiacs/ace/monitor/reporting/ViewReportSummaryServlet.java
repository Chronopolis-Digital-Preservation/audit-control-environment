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
// $Id$
package edu.umiacs.ace.monitor.reporting;

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Retrieve previously generated report summaries
 * latest = true/false (default false) show only latest summary for collection
 * limit = int (default 0) show only X number of summaries, set to 0 for all
 * collection = retrieve reports for a given collection, default show all collections
 *
 * @author toaster
 */
public class ViewReportSummaryServlet extends EntityManagerServlet {

    private static final String PARAM_SUMMARY_ID = "summaryid";
    private static final String PAGE_SUMMARIES = "summaries";
    private static final String PAGE_COLLECTION = "coll";
    private static final String PARAM_LATEST = "latest";
    private static final String PARAM_LIMIT = "limit";
    private static final String PARAM_RSS = "rss";

    @Override
    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response, EntityManager em) throws
            ServletException, IOException {

        Collection coll = getCollection(request, em);
        int limit = (int) getParameter(request, PARAM_LIMIT, 0);
        boolean latest = getParameter(request, PARAM_LATEST, false);
        List<ReportSummary> summaries;
        Query query;
        ReportSummary summary = getSummary(request, em);

        if (summary != null)
        {
            summaries = new ArrayList<ReportSummary>(1);
            summaries.add(summary);
            coll = summary.getCollection();
        }
        else if (coll != null) {
            if (latest) {
                query = em.createNamedQuery("ReportSummary.listByCollectionRecent");
            } else {
                query = em.createNamedQuery("ReportSummary.listByCollection");
            }
            query.setParameter("coll", coll);
            summaries = runQuery(query, limit);
        } else {
            if (latest) {
                summaries = listAllRecent(em);
            } else {
                query = em.createNamedQuery("ReportSummary.listAllSummaries");
                summaries = runQuery(query, limit);
            }

        }

        request.setAttribute(PAGE_SUMMARIES, summaries);
        request.setAttribute(PAGE_COLLECTION, coll);

        RequestDispatcher rd;
        if (hasJson(request)) {
            rd = request.getRequestDispatcher("viewsummary-json.jsp");
        } else if (hasRss(request)) {
            rd = request.getRequestDispatcher("viewsummary-rss.jsp");
        } else {
            rd = request.getRequestDispatcher("viewsummary.jsp");
        }
        rd.forward(request, response);
    }

    private List listAllRecent(EntityManager em) {
        List<ReportSummary> retList = new ArrayList<ReportSummary>(32);
        Query query;
        query = em.createNamedQuery("ReportSummary.listAllRecentIds");
        List resultIds = query.getResultList();
        for (Object o : resultIds) {
            retList.add(em.getReference(ReportSummary.class, o));
        }

        return retList;
    }

    private List runQuery(Query query, int limit) {
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        return query.getResultList();
    }

    public boolean hasRss(HttpServletRequest request) {
        String value = (String) request.getParameter(PARAM_RSS);
        return !Strings.isEmpty(value);
    }

    public ReportSummary getSummary( HttpServletRequest request, EntityManager em ) {
        long summaryid;

        if ( (summaryid = getParameter(request, PARAM_SUMMARY_ID, 0)) > 0 ) {
            try {
                return em.getReference(ReportSummary.class, summaryid);
            } catch ( EntityNotFoundException e ) {
                return null;
            }
        }
        return null;
    }
}
