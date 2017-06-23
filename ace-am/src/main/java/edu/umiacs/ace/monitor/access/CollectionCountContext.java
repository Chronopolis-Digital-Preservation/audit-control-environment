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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to gather a count of all collections at startup.
 * This class includes the ability to update a collection and has the appropriate
 * servlet listeners
 * 
 * @author toaster
 */
public class CollectionCountContext implements ServletContextListener {

    public static final String CTX_STARTUP = "startup_complete";
    private static final Logger LOG = Logger.getLogger(
            CollectionCountContext.class);
    private static Map<Collection, Long> fileCountMap = new ConcurrentHashMap<>();
    private static Map<Collection, Long> fileActiveMap = new ConcurrentHashMap<>();
    private static Map<Collection, Long> fileCorruptMap = new ConcurrentHashMap<>();
    private static Map<Collection, Long> fileMissingMap = new ConcurrentHashMap<>();
    private static Map<Collection, Long> fileMissingTokenMap = new ConcurrentHashMap<>();
    private static Map<Collection, Long> fileTokenMismatchMap = new ConcurrentHashMap<>();
    private static Map<Collection, Long> totalErrorMap = new ConcurrentHashMap<>();
    private static Map<Collection, Long> totalSizeMap = new ConcurrentHashMap<>();
    private static Map<Collection, Long> fileRemoteMissing = new ConcurrentHashMap<>();
    private static Map<Collection, Long> fileRemoteCorrupt = new ConcurrentHashMap<>();
    private static AtomicInteger totalCollections = new AtomicInteger(0);
    private static Lock lock = new ReentrantLock();
    private static boolean abort = false;

    @Override
    public void contextInitialized(final ServletContextEvent arg0) {
        abort = false;
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {

                    arg0.getServletContext().setAttribute(CTX_STARTUP, false);

                    Thread.currentThread().setName("Startup Count Thread");
                    NDC.push("[Count]");
                    lock.lock();
                    LOG.debug("Starting count for all collections");
                    try {
                        EntityManager em = PersistUtil.getEntityManager();
                        Query collQuery = em.createNamedQuery(
                                "Collection.listAllCollections");

                        for (Object o : collQuery.getResultList()) {
                            if (abort) {
                                LOG.info("Collection count aborting, tomcat probably shutting down");
                                return;
                            }
                            queryCollection((Collection) o);
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
            }
        };
        new Thread(r).start();


    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        abort = true;

        // Null these out so that the references to the collections are dropped
        // after tomcat stops the webapp
        fileCountMap = null;
        fileActiveMap = null;
        fileCorruptMap = null;
        fileMissingMap = null;
        fileMissingTokenMap = null;
        fileTokenMismatchMap = null;
        totalErrorMap = null;
        totalSizeMap = null;
        fileRemoteMissing = null;
        fileRemoteCorrupt = null;
    }

    public static long getTokenMismatchCount(Collection c) {
        if (fileTokenMismatchMap.containsKey(c)) {
            return fileTokenMismatchMap.get(c);
        }
        return -1;
    }

    public static long getMissingTokenCount(Collection c) {
        if (fileMissingTokenMap.containsKey(c)) {
            return fileMissingTokenMap.get(c);
        }
        return -1;
    }

    public static long getMissingCount(Collection c) {
        if (fileMissingMap.containsKey(c)) {
            return fileMissingMap.get(c);
        }
        return -1;
    }

    public static long getCorruptCount(Collection c) {
        if (fileCorruptMap.containsKey(c)) {
            return fileCorruptMap.get(c);
        }
        return -1;

    }

    public static long getActiveCount(Collection c) {
        if (fileActiveMap.containsKey(c)) {
            return fileActiveMap.get(c);
        }
        return -1;
    }

    public static long getTotalErrors(Collection c) {
        if (totalErrorMap.containsKey(c)) {
            return totalErrorMap.get(c);
        }
        return -1;
    }

    public static long getFileCount(Collection c) {
        if (fileCountMap.containsKey(c)) {
            return fileCountMap.get(c);
        }
        return -1;
    }

    public static long getTotelSize(Collection c) {
        if (totalSizeMap.containsKey(c)) {
            return totalSizeMap.get(c);
        }
        return -1;
    }

    public static long getRemoteMissing(Collection c) {
        if (fileRemoteMissing.containsKey(c)) {
            return fileRemoteMissing.get(c);
        }
        return -1;
    }

    public static long getRemoteCorrupt(Collection c) {
        if (fileRemoteCorrupt.containsKey(c)) {
            return fileRemoteCorrupt.get(c);
        }
        return -1;
    }

    public static void incrementTotalCollections() {
        totalCollections.incrementAndGet();
    }

    public static void decrementTotalCollections(Collection collection) {
        totalCollections.decrementAndGet();

        fileCountMap.remove(collection);
        fileActiveMap.remove(collection);
        fileCorruptMap.remove(collection);
        fileMissingMap.remove(collection);
        fileMissingTokenMap.remove(collection);
        fileTokenMismatchMap.remove(collection);
        totalErrorMap.remove(collection);
        totalSizeMap.remove(collection);
        fileRemoteMissing.remove(collection);
        fileRemoteCorrupt.remove(collection);
        GroupSummaryContext.updateGroup(collection.getGroup());
    }

    public static int getTotalCollections() {
        return totalCollections.get();
    }

    /**
     * Update statistics for a collection.
     *
     * @param c
     */
    private static boolean queryCollection(Collection c) {
        boolean update = false;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            DataSource ds = PersistUtil.getDataSource();
            connection = ds.getConnection();
            ps = connection.prepareStatement(
                    "SELECT monitored_item.STATE, count(monitored_item.STATE) "
                    + "FROM monitored_item " + "WHERE monitored_item.PARENTCOLLECTION_ID = ? AND "
                    + "monitored_item.DIRECTORY = 0 " + "GROUP BY monitored_item.STATE");
            ps.setLong(1, c.getId());
            rs = ps.executeQuery();
            long total = 0;
            long totalErrors = 0;

            while (rs.next()) {
                if (abort) {
                    return update;
                }
                char state = rs.getString(1).charAt(0);
                long count = rs.getLong(2);

                total += count;

                switch (state) {
                    case 'A':
                        fileActiveMap.put(c, count);
                        break;
                    case 'C':
                        fileCorruptMap.put(c, count);
                        totalErrors += count;
                        break;
                    case 'M':
                        fileMissingMap.put(c, count);
                        totalErrors += count;
                        break;
                    case 'T':
                        fileMissingTokenMap.put(c, count);
                        totalErrors += count;
                        break;
                    case 'I':
                        fileTokenMismatchMap.put(c, count);
                        totalErrors += count;
                        break;
                    case 'P':
                        fileRemoteMissing.put(c, count);
                        totalErrors += count;
                        break;
                    case 'D':
                        fileRemoteCorrupt.put(c, count);
                        totalErrors += count;
                        break;
                }
            }

            update = !Objects.equals(fileCountMap.put(c, total), total);
            totalErrorMap.put(c, totalErrors);
            SQL.release(rs);
            SQL.release(ps);

            // sum up the collection size

            ps = connection.prepareStatement(
                    "SELECT sum(SIZE) " + "FROM monitored_item "
                    + "WHERE monitored_item.PARENTCOLLECTION_ID = ? AND "
                    + "monitored_item.DIRECTORY = 0 ");
            ps.setLong(1, c.getId());
            rs = ps.executeQuery();
            rs.next();
            long totalSize = rs.getLong(1);
            update = update | !Objects.equals(totalSizeMap.put(c, totalSize), totalSize);
        } catch (Exception e) {
            LOG.error("Error starting up, collection count", e);
        } finally {
            SQL.release(rs);
            SQL.release(ps);
            SQL.release(connection);
            LOG.trace("Finished count on " + c.getName());
        }

        return update;
    }

    public static void updateCollection(final Collection c) {
        LOG.debug("Starting update for: " + c.getName());
        Runnable r = new Runnable() {

            @Override
            public void run() {
                lock.lock();

                try {
                    boolean update = queryCollection(c);
                    if (update) {
                        GroupSummaryContext.updateGroup(c.getGroup());
                    }
                } finally {
                    lock.unlock();
                }

            }
        };
        new Thread(r).start();
    }
}
