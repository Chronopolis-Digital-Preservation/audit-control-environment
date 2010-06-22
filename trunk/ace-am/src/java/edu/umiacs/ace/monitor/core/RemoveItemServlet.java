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
// $Id: RemoveItemServlet.java 3187 2010-06-22 14:41:19Z toaster $

package edu.umiacs.ace.monitor.core;

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.monitor.log.LogEventManager;
import edu.umiacs.ace.monitor.access.browse.BrowseServlet;
import edu.umiacs.ace.monitor.access.browse.DirectoryTree;
import edu.umiacs.ace.monitor.log.LogEnum;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 * Remove an item from list of actively monitored items. This will not remove
 * log entries for the file. It will log the delete.
 * 
 * @author toaster
 */
public class RemoveItemServlet extends EntityManagerServlet {

    public static final String PARAM_REDIRECT = "redirect";
    public static final String DEFAULT_REDIRECT = "browse.jsp";
    private static final Logger LOG = Logger.getLogger(RemoveItemServlet.class);

    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em ) throws ServletException, IOException {
        MonitoredItem item;
        HttpSession session = request.getSession();
        DirectoryTree dt =
                (DirectoryTree) session.getAttribute(BrowseServlet.SESSION_DIRECTORY_TREE);

        long itemId = getParameter(request, PARAM_ITEM_ID, 0);
        if ( itemId > 0 ) {
            item = em.getReference(MonitoredItem.class, itemId);
            if ( item != null ) {
                LogEventManager lem =
                        new LogEventManager(new Date().getTime(), item.getParentCollection());
                lem.persistItemEvent(LogEnum.REMOVE_ITEM, item.getPath(), null, em);
                if ( !item.isDirectory() ) {
                    String parent = item.getParentPath();
                    Collection c = item.getParentCollection();
                    EntityTransaction trans = em.getTransaction();
                    trans.begin();
                    if ( item.getToken() != null ) {
                        em.remove(item.getToken());
                    }
                    em.remove(item);
                    trans.commit();
                    reloadTree(dt, parent, c, em);
                } else {
                    new MyDeleteThread(item, dt).start();
                }
            }
        }

        String redirect = request.getParameter(PARAM_REDIRECT);
        if ( Strings.isEmpty(redirect) ) {
            redirect = DEFAULT_REDIRECT;
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(redirect);
        dispatcher.forward(request, response);
    }

    private static void reloadTree( DirectoryTree dt, String parent,
            Collection c, EntityManager em ) {
        if ( dt == null ) {
            return;
        }
        MonitoredItemManager mim = new MonitoredItemManager(em);
        MonitoredItem mi = mim.getItemByPath(parent, c);
        if ( mi != null ) {
            dt.toggleItem(mi.getId());
            dt.toggleItem(mi.getId());
        }


    }

    private class MyDeleteThread extends Thread {

        private MonitoredItem item;
        private MonitoredItemManager mim;
        private EntityManager em;
        private DirectoryTree dt;

        public MyDeleteThread( MonitoredItem item, DirectoryTree dt ) {
            super("Delete thread " + item.getId());
            this.item = item;
            this.dt = dt;
        }

        @Override
        public void run() {
            LOG.trace("Starting delete thread");
            em = PersistUtil.getEntityManager();
            try {
                String parent = item.getParentPath();
                Collection c = item.getParentCollection();
                mim = new MonitoredItemManager(em);
                EntityTransaction trans = em.getTransaction();
                trans.begin();
                clearDir(em.merge(item));
                trans.commit();
                reloadTree(dt, parent, c, em);
            } catch ( Throwable t ) {
                LOG.error("Error removing", t);
            } finally {
                em.close();
                LOG.trace("Finishing delete thread");
            }
        }

        private void clearDir( MonitoredItem item ) {
            LOG.trace("Removing dir: " + item.getPath());
            for ( MonitoredItem mi : mim.listChildren(item.getParentCollection(),
                    item.getPath()) ) {
                if ( mi.isDirectory() ) {
                    clearDir(mi);
                } else {
                    LOG.trace("Removing file: " + mi.getPath());
                    em.remove(mi);
                }

            }
            em.remove(item);
        }
    }
}
