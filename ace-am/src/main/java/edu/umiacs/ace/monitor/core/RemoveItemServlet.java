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

package edu.umiacs.ace.monitor.core;

import com.google.common.collect.ImmutableSet;
import edu.umiacs.ace.monitor.access.CollectionCountContext;
import edu.umiacs.ace.monitor.access.browse.BrowseServlet;
import edu.umiacs.ace.monitor.access.browse.DirectoryTree;
import edu.umiacs.ace.monitor.log.LogEnum;
import edu.umiacs.ace.monitor.log.LogEventManager;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.sql.SQL;
import edu.umiacs.util.Strings;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Remove an item from list of actively monitored items. This will not remove
 * log entries for the file. It will log the delete.
 *
 * @author toaster
 */
public class RemoveItemServlet extends EntityManagerServlet {

    private static final String PARAM_TYPE = "type";
    private static final String PARAM_REDIRECT = "redirect";
    private static final String DEFAULT_REDIRECT = "browse.jsp";
    private static final String REMOVAL = "removal";
    private static final Logger LOG = Logger.getLogger(RemoveItemServlet.class);

    @Override
    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response, EntityManager em) throws ServletException, IOException {

        long[] itemIds;
        Set<Collection> mutations;
        HttpSession session = request.getSession();
        DirectoryTree dt =
                (DirectoryTree) session.getAttribute(BrowseServlet.SESSION_DIRECTORY_TREE);

        long eventSession = System.currentTimeMillis();

        String type = getParameter(request, PARAM_TYPE, null);
        long itemId = getParameter(request, PARAM_ITEM_ID, 0);
        long collectionid = getParameter(request, PARAM_COLLECTION_ID, 0);

        if (type != null && !type.isEmpty() && collectionid > 0) {
            mutations = referenceFor(Collection.class, collectionid, em)
                    .map(c -> removeForType(c, type, em, eventSession))
                    .orElseGet(ImmutableSet::of);
        } else if (itemId > 0) {
            mutations = referenceFor(MonitoredItem.class, itemId, em)
                    .map(mi -> removeItem(mi, em, eventSession, dt))
                    .map(ImmutableSet::of).orElseGet(ImmutableSet::of);
        } else {
            itemIds = getParameterList(request, REMOVAL, 0);
            mutations = new HashSet<>();
            if (itemIds != null) {
                for (long l : itemIds) {
                    if (l > 0) {
                        referenceFor(MonitoredItem.class, l, em)
                                .map(i -> removeItem(i, em, eventSession, dt))
                                .ifPresent(mutations::add);
                    }
                }
            }
        }

        LOG.trace(mutations.size() + " collections to update");
        for (Collection collection : mutations) {
            CollectionCountContext.updateCollection(collection);
        }

        String redirect = request.getParameter(PARAM_REDIRECT);
        if (Strings.isEmpty(redirect)) {
            redirect = DEFAULT_REDIRECT;
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(redirect);
        dispatcher.forward(request, response);
    }

    private Set<Collection> removeForType(Collection collection,
                                          String type,
                                          EntityManager em,
                                          long eventSession) {
        TypedQuery<MonitoredItem> query = em.createNamedQuery(
                "MonitoredItem.itemsByState",
                MonitoredItem.class);
        char state = 0;
        query.setParameter("coll", collection);

        // Only remove corrupt or missing items for now
        switch (type.toLowerCase(Locale.ENGLISH)) {
            case "corrupt":
                state = 'C';
                break;
            case "missing":
                state = 'M';
                break;
            default:
                LOG.warn("Not remove type of " + type + "; needs to be corrupt or missing");
        }

        if (state != 0) {
            DataSource dataSource;
            Connection connection = null;
            try {
                dataSource = PersistUtil.getDataSource();
                connection = dataSource.getConnection();
                // Create log_event items
                // We do this through a PreparedStatement because we can be working on many
                // items at once time which can be very slow when iterating each item individually
                PreparedStatement logStatement = connection.prepareStatement(
                        "INSERT INTO logevent(session, path, date, logtype, collection_id) " +
                        "SELECT ?, path, NOW(), ?, parentcollection_id FROM monitored_item m " +
                        "WHERE m.parentcollection_id = ? AND m.state = ?");
                connection.setAutoCommit(false);
                logStatement.setLong(1, eventSession);
                logStatement.setInt(2, LogEnum.REMOVE_ITEM.getType());
                logStatement.setLong(3, collection.getId());
                logStatement.setString(4, String.valueOf(state));

                // And remove with a PreparedStatement for the same reason
                PreparedStatement deleteStatement = connection.prepareStatement(
                        "DELETE FROM monitored_item WHERE parentcollection_id = ? AND state = ?");
                deleteStatement.setLong(1, collection.getId());
                deleteStatement.setString(2, String.valueOf(state));

                logStatement.executeUpdate();
                logStatement.close();
                deleteStatement.executeUpdate();
                deleteStatement.close();
                // I don't think we need this since we close the connection when we finish
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                LOG.error("Error removing items ", e);
                rollback(connection);
            } finally {
                SQL.release(connection);
            }
        }

        return ImmutableSet.of(collection);
    }

    private void rollback(@Nullable Connection connection) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException e) {
            LOG.warn("Unable to rollback last remove transaction!", e);
        }
    }

    private <T> Optional<T> referenceFor(Class<T> clazz, Long id, EntityManager em) {
        try {
            return Optional.of(em.getReference(clazz, id));
        } catch (EntityNotFoundException ex) {
            LOG.warn("EntityNotFound " + id, ex);
            return Optional.empty();
        }
    }

    private Collection removeItem(MonitoredItem item, EntityManager em, Long session, DirectoryTree dt) {
        Collection c = null;
        if (item != null) {
            LogEventManager lem = new LogEventManager(session, item.getParentCollection());
            lem.persistItemEvent(LogEnum.REMOVE_ITEM, item.getPath(), null, em);
            if (!item.isDirectory()) {
                String parent = item.getParentPath();
                c = item.getParentCollection();
                EntityTransaction trans = em.getTransaction();
                trans.begin();
                if (item.getToken() != null) {
                    em.remove(item.getToken());
                }
                em.remove(item);
                trans.commit();
                reloadTree(dt, parent, c, em);
            } else {
                new MyDeleteThread(item, dt).start();
            }
        }

        return c;
    }

    private static void reloadTree(DirectoryTree dt,
                                   String parent,
                                   Collection c,
                                   EntityManager em) {
        if (dt == null) {
            return;
        }
        MonitoredItemManager mim = new MonitoredItemManager(em);
        MonitoredItem mi = mim.getItemByPath(parent, c);
        if (mi != null) {
            dt.toggleItem(mi.getId());
            dt.toggleItem(mi.getId());
        }
    }

    private static class MyDeleteThread extends Thread {

        private MonitoredItem item;
        private MonitoredItemManager mim;
        private EntityManager em;
        private DirectoryTree dt;

        private MyDeleteThread(MonitoredItem item, DirectoryTree dt) {
            super("Delete thread " + item.getId());
            this.item = item;
            this.dt = dt;
        }

        @Override
        public void run() {
            LOG.trace("Starting delete thread");

            // because the delete is async, we should do this again here
            // it would be better to have both deletes on a separate thread and then do
            // the context refresh when they finish... but... whatever
            Set<Collection> mutations = new HashSet<>();
            em = PersistUtil.getEntityManager();
            try {
                String parent = item.getParentPath();
                Collection c = item.getParentCollection();
                mutations.add(c);
                mim = new MonitoredItemManager(em);
                EntityTransaction trans = em.getTransaction();
                trans.begin();
                clearDir(em.merge(item));
                trans.commit();
                reloadTree(dt, parent, c, em);
            } catch (Throwable t) {
                LOG.error("Error removing", t);
            } finally {
                em.close();
                LOG.trace("Finishing delete thread");

                for (Collection mutation : mutations) {
                    CollectionCountContext.updateCollection(mutation);
                }
            }
        }

        private void clearDir(MonitoredItem item) {
            LOG.trace("Removing dir: " + item.getPath());
            for (MonitoredItem mi : mim.listChildren(item.getParentCollection(),
                    item.getPath())) {
                if (mi.isDirectory()) {
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
