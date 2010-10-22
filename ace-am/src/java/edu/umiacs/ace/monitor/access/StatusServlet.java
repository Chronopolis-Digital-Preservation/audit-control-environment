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
package edu.umiacs.ace.monitor.access;

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author toaster
 */
public class StatusServlet extends EntityManagerServlet {

    private static final String PAGE_COLLECTIONS = "collections";
    private static final String SESSION_WORKINGCOLLECTION = "workingCollection";
    private static final String PARAM_CSV = "csv";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em )
            throws ServletException, IOException {
        RequestDispatcher dispatcher;
        List<CollectionSummaryBean> collections;
        List<Collection> items;


        Query query =
                em.createNamedQuery("Collection.listAllCollections");

        items = query.getResultList();

        collections = new ArrayList<CollectionSummaryBean>();

        if ( Strings.isValidLong(request.getParameter(PARAM_COLLECTION_ID)) && -1 == Long.parseLong(request.getParameter(
                PARAM_COLLECTION_ID)) ) {
            request.getSession().removeAttribute(SESSION_WORKINGCOLLECTION);
        }
        for ( Collection col : items ) {
            CollectionSummaryBean csb = new CollectionSummaryBean();
            csb.setCollection(col);
            csb.setTotalFiles(CollectionCountContext.getFileCount(col));
            csb.setActiveFiles(CollectionCountContext.getActiveCount(col));
            csb.setCorruptFiles(CollectionCountContext.getCorruptCount(col));
            csb.setInvalidDigests(CollectionCountContext.getTokenMismatchCount(
                    col));
            csb.setMissingFiles(CollectionCountContext.getMissingCount(col));
            csb.setMissingTokens(CollectionCountContext.getMissingTokenCount(col));
            csb.setTotalSize(CollectionCountContext.getTotelSize(col));
            csb.setTotalErrors(CollectionCountContext.getTotalErrors(col));
            csb.setRemoteMissing(CollectionCountContext.getRemoteMissing(col));
            csb.setRemoteCorrupt(CollectionCountContext.getRemoteCorrupt(col));
            collections.add(csb);

            // if param_collection was supplied as a parameter see if this bean
            // hsould be set as working/details bean
            if ( Strings.isValidLong(request.getParameter(PARAM_COLLECTION_ID)) && col.getId() == Long.parseLong(request.getParameter(
                    PARAM_COLLECTION_ID)) ) {
                request.getSession().setAttribute(SESSION_WORKINGCOLLECTION, csb);
            } else if ( request.getSession().getAttribute(
                    SESSION_WORKINGCOLLECTION) != null && ((CollectionSummaryBean) request.getSession().getAttribute(
                    SESSION_WORKINGCOLLECTION)).getCollection().getId() == col.getId() ) {
                request.getSession().setAttribute(SESSION_WORKINGCOLLECTION, csb);

            }

        }

        request.setAttribute(PAGE_COLLECTIONS, collections);
        if ( hasJson(request) ) {
            dispatcher = request.getRequestDispatcher("status-json.jsp");
        } else if ( hasCsv(request) ) {
            dispatcher = request.getRequestDispatcher("status-csv.jsp");
        } else {
            dispatcher = request.getRequestDispatcher("status.jsp");
        }
        dispatcher.forward(request, response);
    }

    private boolean hasCsv( HttpServletRequest request ) {
        String value = (String) request.getParameter(PARAM_CSV);
        return !Strings.isEmpty(value);
    }
}
