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
package edu.umiacs.ace.monitor.audit;

import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.driver.StorageDriverFactory;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author toaster
 */
public final class StartSyncServlet extends EntityManagerServlet {

    public static final String PARAM_TYPE = "type";

    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em )
            throws ServletException, IOException {

        boolean isTokenAudit = false;
        boolean verbose = true;
        Collection collection;
        StorageDriver driver;
        MonitoredItem[] item = null;


        if ( "token".equals(request.getParameter(PARAM_TYPE)) ) {
            isTokenAudit = true;
        }



        if ( (collection = getCollection(request, em)) != null ) {
            driver = StorageDriverFactory.createStorageAccess(collection, em);
            if ( "corrupt".equals(request.getParameter(PARAM_TYPE)) ) {
                Query query = em.createNamedQuery("MonitoredItem.listLocalErrors");
                query.setParameter("coll", collection);
                List<MonitoredItem> resList = query.getResultList();
                item = resList.toArray(new MonitoredItem[resList.size()]);

            } else {
                // case for individual item
                item = new MonitoredItem[]{
                            getItem(request, em)
                        };
                if ( item[0] != null && (!item[0].isDirectory() || item[0].getParentCollection()
                        != collection) ) {
                    item = null;
                }
            }

            if ( isTokenAudit ) {
                AuditTokens.createThread(collection);
            } else {
                AuditThreadFactory.createThread(collection, driver, verbose, item);
            }
            response.sendRedirect("Status?collectionid=" + collection.getId());

        } else {
            response.sendRedirect("Status");
        }

    }
}
