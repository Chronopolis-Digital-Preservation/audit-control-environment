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
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.MonitoredItemManager;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * List details for a file or directory
 * 
 * @author toaster
 */
public final class ListItemDetailsServlet extends EntityManagerServlet {

    private static final String PAGE_ITEMS = "children";
    private static final String PAGE_PARENT = "parent";
    private static final String PARAM_PATH = "itempath";

    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em ) throws ServletException, IOException {

        Collection coll = getCollection(request, em);
        String path = getParameter(request, PARAM_PATH, "");
        MonitoredItemManager mim = new MonitoredItemManager(em);

        if ( coll == null ) {
            throw new ServletException("No collection found");
        }

        if ( Strings.isEmpty(path) ) {


            request.setAttribute(PAGE_ITEMS, mim.getCollectionRoots(coll));
        } else {
            MonitoredItem item = mim.getItemByPath(path, coll);
            if ( item == null ) {
                throw new ServletException("No item for path " + path);
            }
            List<MonitoredItem> childItems;
            if ( item.isDirectory() ) {
                childItems = mim.listChildren(item.getParentCollection(),
                        item.getPath());
            } else {
                childItems = Collections.emptyList();
            }
            request.setAttribute(PAGE_PARENT, item);
            request.setAttribute(PAGE_ITEMS, childItems);
        }

        RequestDispatcher dispatcher;
        if ( hasJson(request) ) {
            dispatcher = request.getRequestDispatcher("listitem-json.jsp");
        } else {
            dispatcher = request.getRequestDispatcher("listitem.jsp");
        }
        dispatcher.forward(request, response);


    }
}
