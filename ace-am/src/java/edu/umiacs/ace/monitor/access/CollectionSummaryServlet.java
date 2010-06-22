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
// $Id: CollectionSummaryServlet.java 3182 2010-06-16 20:53:20Z toaster $
package edu.umiacs.ace.monitor.access;

import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.monitor.core.Collection;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * Show the current status of a collection, listing any non-active files
 * 
 * @author toaster
 */
public class CollectionSummaryServlet extends EntityManagerServlet {

    private static final Logger LOG = Logger.getLogger(CollectionSummaryServlet.class);
    public static final String PARAM_START = "start";
    public static final String PARAM_TOP = "top";
    public static final String PARAM_COUNT = "count";
    public static final String PARAM_TEXT = "text";
    public static final String PAGE_NEXT = "next";
    public static final String PAGE_COLLECTION = "collection";
    public static final String PAGE_ITEMS = "items";
    public static final String PAGE_COUNT = "count";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em )
            throws ServletException, IOException {
        RequestDispatcher dispatch;
        long startid = getParameter(request, PARAM_START, 0);
        long topid = getParameter(request, PARAM_TOP, 0);
        int count = (int) getParameter(request, PARAM_COUNT, 20);
        boolean listOnly = getParameter(request, PARAM_TEXT, false);


        //TODO: Can this move to fixed query? 
        String query = "SELECT m FROM MonitoredItem m WHERE m.state <> 'A' AND ";

        // determine start
        String startquery = "";
        if ( startid > 0 ) {
            startquery = "AND m.id > :id ";
        } else if ( topid > 0 ) {
            startquery = "AND m.id < :id ";
        }

        // build query

        Query q;
        Collection c = getCollection(request, em);
        query += "m.parentCollection = :coll " + startquery;
        q = em.createQuery(query);
        q.setParameter("coll", c);

        LOG.debug("query: " + query);
        // fill in start ids if necessary
        if ( startid > 0 ) {
            q.setParameter("id", startid);
        } else if ( topid > 0 ) {
            q.setParameter("id", topid);
        }

        if ( count != -1 ) {
            q.setMaxResults(count);
        }
        List<MonitoredItem> miList = q.getResultList();
        if ( miList != null && miList.size() > 0 ) {
            request.setAttribute(PAGE_NEXT,
                    miList.get(miList.size() - 1).getId() + 1);
        }

        if ( listOnly ) {
            Writer writer = response.getWriter();
            response.setContentType("text/plain");
            for ( MonitoredItem mi : miList ) {
                writer.write(mi.getState() + ":" + mi.getPath() + "\r\n");
            }
            return;
        }

        CollectionSummaryBean csb = new CollectionSummaryBean();
        csb.setCollection(c);
        csb.setTotalFiles(CollectionCountContext.getFileCount(c));
        csb.setActiveFiles(CollectionCountContext.getActiveCount(c));
        csb.setCorruptFiles(CollectionCountContext.getCorruptCount(c));
        csb.setInvalidDigests(CollectionCountContext.getTokenMismatchCount(
                c));
        csb.setMissingFiles(CollectionCountContext.getMissingCount(c));
        csb.setMissingTokens(CollectionCountContext.getMissingTokenCount(c));
        csb.setTotalErrors(CollectionCountContext.getTotalErrors(c));
        csb.setRemoteMissing(CollectionCountContext.getRemoteMissing(c));
        csb.setRemoteCorrupt(CollectionCountContext.getRemoteCorrupt(c));

        request.setAttribute(PAGE_ITEMS, miList);
        request.setAttribute(PAGE_COLLECTION, csb);
        request.setAttribute(PAGE_COUNT, count);

        if ( hasJson(request) ) {
            dispatch = request.getRequestDispatcher("report-json.jsp");
        } else {
            dispatch = request.getRequestDispatcher("report.jsp");

        }
        dispatch.forward(request, response);

    }
}
