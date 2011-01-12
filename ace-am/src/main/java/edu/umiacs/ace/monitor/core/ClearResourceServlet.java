 /*
 * Copyright (c) 2007-@year@, University of Maryland
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
package edu.umiacs.ace.monitor.core;

import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.driver.StorageDriverFactory;
import edu.umiacs.ace.monitor.log.LogEnum;
import edu.umiacs.ace.monitor.log.LogEvent;
import edu.umiacs.ace.monitor.log.LogEventManager;
import edu.umiacs.ace.util.EntityManagerServlet;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author toaster
 */
public class ClearResourceServlet extends EntityManagerServlet {

    @Override
    protected void processRequest( HttpServletRequest request, HttpServletResponse response,
            EntityManager em ) throws ServletException, IOException {

        Collection c = getCollection(request, em);
        RequestDispatcher dispatcher;
        if ( c != null ) {
            if (c.getStorage() != null)
            {
                LogEventManager lem = new LogEventManager(System.currentTimeMillis(), c);
                EntityTransaction t = em.getTransaction();
                t.begin();
                StorageDriver sd = StorageDriverFactory.createStorageAccess(c, em);
                LogEvent event = lem.createCollectionEvent(LogEnum.REMOVE_STORAGE_DRIVER, "Remove configuration for " + c.getStorage());
                sd.remove(em);
                c.setStorage(null);
                em.persist(event);
                em.merge(c);
                t.commit();
            }
            dispatcher = request.getRequestDispatcher("ManageCollection?collectionid="+c.getId());
        } else {

            dispatcher = request.getRequestDispatcher("status.jsp");
        }

        dispatcher.forward(request, response);
    }
}
