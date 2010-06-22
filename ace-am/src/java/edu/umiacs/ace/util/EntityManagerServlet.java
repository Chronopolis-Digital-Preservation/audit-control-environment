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

package edu.umiacs.ace.util;

import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.util.Strings;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.NDC;

/**
 * Base servlet that all ACE servlets should extens. This handles
 * opening and closing the entity manager for the duration of the call
 *
 * Classes that extend should take care to NOT put anything in a
 * clients session that will cause an entitymanager action upon rendering, as
 * the em is closed after processRequest exists.
 *
 * @author toaster
 */
public abstract class EntityManagerServlet extends HttpServlet {

    public static final String PARAM_COLLECTION_ID = "collectionid";
    public static final String PARAM_ITEM_ID = "itemid";
    public static final String PARAM_TOKEN_ID = "tokenid";
    private static final String PARAM_JSON = "json";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected abstract void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em )
            throws ServletException, IOException;

    private void wrapRequest( HttpServletRequest request,
            HttpServletResponse response )
            throws ServletException, IOException {

        EntityManager em = PersistUtil.getEntityManager();
        try {
            NDC.push("[request " + Thread.currentThread().getName() + "] ");
            processRequest(request, response, em);
        } finally {
            em.close();
            NDC.pop();
        }
    }

    public boolean hasJson( HttpServletRequest request ) {
        String value = (String) request.getParameter(PARAM_JSON);
        return !Strings.isEmpty(value);
    }

    public String getParameter( HttpServletRequest request, String paramName,
            String defaultValue ) {
        if ( !Strings.isEmpty(request.getParameter(paramName)) ) {
            return request.getParameter(paramName);
        } else {
            return defaultValue;
        }
    }

    public String[] getParameterList( HttpServletRequest request, String paramName ) {
        if ( !Strings.isEmpty(request.getParameter(paramName)) ) {
            return request.getParameterValues(paramName);
        } else {
            return null;
        }
    }

    public boolean getParameter( HttpServletRequest request, String paramName,
            boolean defaultValue ) {
        if ( Strings.isValidInt(request.getParameter(paramName)) ) {
            return (Integer.parseInt(request.getParameter(paramName)) > 0);
        } else {
            return defaultValue;
        }
    }

    public long getParameter( HttpServletRequest request,
            String paramName,
            long defaultValue ) {
        if ( Strings.isValidLong(request.getParameter(paramName)) ) {
            return Long.parseLong(request.getParameter(paramName));
        } else {
            return defaultValue;
        }
    }

    public Collection getCollection( HttpServletRequest request, EntityManager em ) {
        long collectionId;

        if ( (collectionId = getParameter(request, PARAM_COLLECTION_ID, 0)) > 0 ) {
            try {
                return em.getReference(Collection.class, collectionId);
            } catch ( EntityNotFoundException e ) {
                return null;
            }
        }
        return null;
    }

    public MonitoredItem getItem( HttpServletRequest request, EntityManager em ) {
        long itemId;

        if ( (itemId = getParameter(request, PARAM_ITEM_ID, 0)) > 0 ) {
            try {
                return em.getReference(MonitoredItem.class, itemId);
            } catch ( EntityNotFoundException e ) {
                return null;
            }
        }
        return null;
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet( HttpServletRequest request,
            HttpServletResponse response )
            throws ServletException, IOException {
        wrapRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost( HttpServletRequest request,
            HttpServletResponse response )
            throws ServletException, IOException {
        wrapRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
