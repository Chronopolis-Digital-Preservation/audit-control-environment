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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.sql.SQL;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.collect.ImmutableList.of;


/**
 * Class to gather a count of all collections at startup.
 * This class includes the ability to update a collection and has the appropriate
 * servlet listeners
 *
 * @author toaster
 */
public class CollectionCountContext implements ServletContextListener {

    public static final String CTX_STARTUP = "startup_complete";
    private static final Logger LOG = Logger.getLogger(CollectionCountContext.class);

    private static LoadingCache<Collection, Long> fileCount = Caffeine.newBuilder()
            .build(CollectionCountContext::getCountForCollection);
    private static LoadingCache<Collection, Long> activeCount = Caffeine.newBuilder()
            .build((key) -> getCountForCollectionAndState(key, of("A", "R")));
    private static LoadingCache<Collection, Long> corruptCount = Caffeine.newBuilder()
            .build((key) -> getCountForCollectionAndState(key, of("C")));
    private static LoadingCache<Collection, Long> missingCount = Caffeine.newBuilder()
            .build((key) -> getCountForCollectionAndState(key, of("M")));
    private static LoadingCache<Collection, Long> missingTokenCount = Caffeine.newBuilder()
            .build((key) -> getCountForCollectionAndState(key, of("T")));
    private static LoadingCache<Collection, Long> tokenMismatchCount = Caffeine.newBuilder()
            .build((key) -> getCountForCollectionAndState(key, of("I")));
    private static LoadingCache<Collection, Long> remoteMissingCount = Caffeine.newBuilder()
            .build((key) -> getCountForCollectionAndState(key, of("P")));
    private static LoadingCache<Collection, Long> remoteCorruptCount = Caffeine.newBuilder()
            .build((key) -> getCountForCollectionAndState(key, of("D")));
    private static LoadingCache<Collection, Long> totalErrorCount = Caffeine.newBuilder()
            .build((key) -> getCountForCollectionAndState(key, of("C", "M", "T", "I", "P", "D")));
    private static LoadingCache<Collection, Long> totalSize = Caffeine.newBuilder()
            .build(CollectionCountContext::getSize);

    private static AtomicInteger totalCollections = new AtomicInteger(0);
    private static Lock lock = new ReentrantLock();
    private static boolean abort = false;

    @Override
    public void contextInitialized(final ServletContextEvent arg0) {
        abort = false;
        Runnable r = () -> {
            try {

                arg0.getServletContext().setAttribute(CTX_STARTUP, false);

                Thread.currentThread().setName("Startup Count Thread");
                NDC.push("[Count]");
                lock.lock();
                LOG.debug("Starting count for all collections");
                try {
                    EntityManager em = PersistUtil.getEntityManager();
                    Query collQuery = em.createNamedQuery(
                            "Collection.listAllCollections", Collection.class);

                    for (Object o : collQuery.getResultList()) {
                        Collection collection = (Collection) o;
                        if (abort) {
                            LOG.info("Collection count aborting, tomcat probably shutting down");
                            return;
                        }
                        queryCollection(collection);
                        incrementTotalCollections();
                    }
                    em.close();
                } catch (Exception e) {
                    LOG.error("Error starting up, collection count", e);
                } finally {
                    lock.unlock();
                }

                LOG.debug("Finished startup count");
                NDC.pop();
            } finally {
                arg0.getServletContext().setAttribute(CTX_STARTUP, true);
            }
        };
        new Thread(r).start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        abort = true;

        // Null these out so that the references to the collections are dropped
        // after tomcat stops the webapp
        fileCount.invalidateAll();
        fileCount = null;
        activeCount.invalidateAll();
        activeCount = null;
        corruptCount.invalidateAll();
        corruptCount = null;
        missingCount.invalidateAll();
        missingCount = null;
        missingTokenCount.invalidateAll();
        missingTokenCount = null;
        remoteCorruptCount.invalidateAll();
        remoteCorruptCount = null;
        remoteMissingCount.invalidateAll();
        remoteMissingCount = null;
        tokenMismatchCount.invalidateAll();
        totalSize.invalidateAll();
        totalSize = null;
        totalErrorCount.invalidateAll();
        totalErrorCount = null;
    }

    public static long getTokenMismatchCount(Collection c) {
        Long count = tokenMismatchCount.get(c);
        return count != null ? count : -1;
    }

    public static long getMissingTokenCount(Collection c) {
        Long count = missingTokenCount.get(c);
        return count != null ? count : -1;
    }

    public static long getMissingCount(Collection c) {
        Long count = missingCount.get(c);
        return count != null ? count : -1;
    }

    public static long getCorruptCount(Collection c) {
        Long count = corruptCount.get(c);
        return count != null ? count : -1;
    }

    public static long getActiveCount(Collection c) {
        Long count = activeCount.get(c);
        return count != null ? count : -1;
    }

    public static long getTotalErrors(Collection c) {
        Long count = totalErrorCount.get(c);
        return count != null ? count : -1;
    }

    public static long getFileCount(Collection c) {
        Long count = fileCount.get(c);
        return count != null ? count : -1;
    }

    public static long getTotalSize(Collection c) {
        Long size = totalSize.get(c);
        return size != null ? size : -1;
    }

    public static long getRemoteMissing(Collection c) {
        Long count = remoteMissingCount.get(c);
        return count != null ? count : -1;
    }

    public static long getRemoteCorrupt(Collection c) {
        Long count = remoteCorruptCount.get(c);
        return count != null ? count : -1;
    }

    public static void incrementTotalCollections() {
        totalCollections.incrementAndGet();
    }

    public static void decrementTotalCollections(Collection collection) {
        totalCollections.decrementAndGet();

        fileCount.invalidate(collection);
        activeCount.invalidate(collection);
        corruptCount.invalidate(collection);
        missingCount.invalidate(collection);
        missingTokenCount.invalidate(collection);
        tokenMismatchCount.invalidate(collection);
        remoteCorruptCount.invalidate(collection);
        remoteMissingCount.invalidate(collection);
        totalSize.invalidate(collection);
        totalErrorCount.invalidate(collection);
        GroupSummaryContext.updateGroup(collection.getGroup());
    }

    public static int getTotalCollections() {
        return totalCollections.get();
    }

    private static Long getCountForCollection(Collection key) {
        Long count = 0L;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Connection connection = null;

        DataSource ds = PersistUtil.getDataSource();
        try {
            connection = ds.getConnection();

            ps = connection.prepareStatement(
                    "SELECT count(monitored_item.id) FROM monitored_item " +
                            "WHERE monitored_item.PARENTCOLLECTION_ID = ? " +
                            "AND monitored_item.directory = 0");
            ps.setLong(1, key.getId());
            rs = ps.executeQuery();

            if (rs.next()) {
                count = rs.getLong(1);
            }
        } catch (SQLException e) {
            LOG.error("[Count] Unable to get file count for " + key.getName(), e);
        } finally {
            SQL.release(rs);
            SQL.release(ps);
            SQL.release(connection);
        }

        return count;
    }

    private static Long getCountForCollectionAndState(Collection key, List<String> states) {
        Long count = 0L;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Connection connection = null;

        try {
            DataSource ds = PersistUtil.getDataSource();
            connection = ds.getConnection();

            StringBuilder inClause = new StringBuilder();
            for (int i = 0; i < states.size(); i++) {
                inClause.append("?");
                if (i < states.size() - 1) {
                    inClause.append(",");
                }
            }

            ps = connection.prepareStatement(
                    "SELECT count(monitored_item.STATE) FROM monitored_item " +
                            "WHERE monitored_item.PARENTCOLLECTION_ID = ? " +
                            "AND monitored_item.directory = 0 AND monitored_item.state IN (" +
                            inClause.toString() + ")");
            ps.setLong(1, key.getId());
            int index = 2;
            for (String state : states) {
                ps.setString(index, state);
                index++;
            }

            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getLong(1);
            }
        } catch (SQLException e) {
            LOG.error("[COUNT] Unable to get item count for collection " + key.getName()
                    + " with state(s) " + states, e);
        } finally {
            SQL.release(rs);
            SQL.release(ps);
            SQL.release(connection);
        }

        return count;
    }

    private static Long getSize(Collection key) {
        Long sum = 0L;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Connection connection = null;

        try {
            DataSource ds = PersistUtil.getDataSource();
            connection = ds.getConnection();

            ps = connection.prepareStatement(
                    "SELECT sum(SIZE) " + "FROM monitored_item "
                            + "WHERE monitored_item.PARENTCOLLECTION_ID = ? AND "
                            + "monitored_item.DIRECTORY = 0");

            ps.setLong(1, key.getId());
            rs = ps.executeQuery();
            if (rs.next()) {
                sum = rs.getLong(1);
            }
        } catch (SQLException e) {
            LOG.error("[COUNT] Unable to get sum for collection " + key.getName(), e);
        } finally {
            SQL.release(rs);
            SQL.release(ps);
            SQL.release(connection);
        }

        return sum;
    }

    /**
     * Update statistics for a collection.
     *
     * @param c the collection to query
     */
    private static boolean queryCollection(Collection c) {
        // todo: compare old/new file count to determine if we need to refresh the group cache
        fileCount.refresh(c);
        activeCount.refresh(c);
        corruptCount.refresh(c);
        missingCount.refresh(c);
        missingTokenCount.refresh(c);
        tokenMismatchCount.refresh(c);
        remoteCorruptCount.refresh(c);
        remoteMissingCount.refresh(c);
        totalSize.refresh(c);
        totalErrorCount.refresh(c);
        LOG.trace("Finished count on " + c.getName());
        return true;
    }

    public static void updateCollection(final Collection c) {
        LOG.debug("Starting update for: " + c.getName());
        Runnable r = () -> {
            lock.lock();

            try {
                boolean update = queryCollection(c);
                if (update) {
                    GroupSummaryContext.updateGroup(c.getGroup());
                }
            } finally {
                lock.unlock();
            }

        };
        new Thread(r).start();
    }
}
