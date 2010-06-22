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

package edu.umiacs.ace.monitor.compare;

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.monitor.core.MonitoredItem;
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
public class ShowDupsServlet extends EntityManagerServlet {

    public static final String PARAM_DIGEST = "digest";
    public static final String PAGE_LIST = "duplicates";
    public static final String PAGE_SOURCE = "source";
    public static final String PAGE_DIGEST = "digest";

    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em ) throws ServletException, IOException {
        RequestDispatcher dispatcher;
        MonitoredItem item = null;
        Collection coll = null;
        List<MonitoredItem> duplicateItems = new ArrayList<MonitoredItem>();

        coll = getCollection(request, em);
        String digest = getParameter(request, PARAM_DIGEST, null);
        long itemId = getParameter(request, PARAM_ITEM_ID, 0);
        if ( itemId > 0 ) {
            item = em.getReference(MonitoredItem.class, itemId);
            if ( item != null ) {
                Query q = em.createNamedQuery("MonitoredItem.listDuplicates");
                q.setParameter("digest", item.getFileDigest());
                q.setParameter("coll", coll);
                digest = item.getFileDigest();
                for ( Object o : q.getResultList() ) {
                    MonitoredItem mi = (MonitoredItem) o;
                    if ( mi.getParentCollection().equals(
                            item.getParentCollection()) ) {
                        duplicateItems.add(mi);
                    }
                }
            }
        } else if ( !Strings.isEmpty(digest) && coll != null ) {
            Query q = em.createNamedQuery("MonitoredItem.listDuplicates");
            q.setParameter("digest", digest);
            q.setParameter("coll", coll);
            for ( Object o : q.getResultList() ) {
                MonitoredItem mi = (MonitoredItem) o;
                if ( mi.getParentCollection().equals(coll) ) {
                    duplicateItems.add(mi);
                }
            }
        }
        request.setAttribute(PAGE_DIGEST, digest);
        request.setAttribute(PAGE_LIST, duplicateItems);
        request.setAttribute(PAGE_SOURCE, item);
        if ( hasJson(request) ) {
            dispatcher = request.getRequestDispatcher("duplicates-json.jsp");
        } else {
            dispatcher = request.getRequestDispatcher("duplicates.jsp");
        }
        dispatcher.forward(request, response);
    }
}
