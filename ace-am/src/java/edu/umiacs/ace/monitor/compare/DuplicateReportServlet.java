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
// $Id: DuplicateReportServlet.java 3181 2010-06-16 20:46:16Z toaster $

package edu.umiacs.ace.monitor.compare;

import edu.umiacs.ace.monitor.access.CollectionCountContext;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.sql.SQL;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public class DuplicateReportServlet extends EntityManagerServlet {

    private static final Logger LOG = Logger.getLogger(DuplicateReportServlet.class);
    public static final String PAGE_LIST = "duplicates";
    public static final String PAGE_TOTAL_DUPS = "totaldups";
    public static final String PAGE_TOTAL_FILES = "totalfiles";
    public static final String PAGE_HISTO = "histogram";
    public static final String PAGE_COLLECTION = "collection";
    public static final String PAGE_TIME = "time";

    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em ) throws ServletException, IOException {
        Collection collection = null;
        RequestDispatcher dispatcher;
        Connection conn = null;
        long totalDups = 0;
        long startTime = System.currentTimeMillis();
//        long totalFiles = 0;

        // digest, # instances of digest
        Set<MyEntry> duplicateCount = new TreeSet<MyEntry>();
        // # digest instances, total over all digests
        List<HistEntry> histogram = new ArrayList<HistEntry>();

        long collectionId = getParameter(request, PARAM_COLLECTION_ID, 0);
        if ( collectionId > 0 ) {
            collection = em.getReference(Collection.class, collectionId);

            try {
                conn = PersistUtil.getDataSource().getConnection();
                PreparedStatement pst =
                        conn.prepareStatement(
                        "SELECT monitored_item.FILEDIGEST, count(monitored_item.FILEDIGEST) "
                        + "FROM monitored_item " + "WHERE monitored_item.PARENTCOLLECTION_ID = ? "
                        + "GROUP BY monitored_item.FILEDIGEST HAVING COUNT(monitored_item.FILEDIGEST) > 1");
                pst.setLong(1, collectionId);
                ResultSet rs = pst.executeQuery();

                while ( rs.next() ) {
                    String hash = rs.getString(1);
                    int count = rs.getInt(2);
//                    totalFiles += count;
                    if ( count > 1 ) {
                        duplicateCount.add(new MyEntry(count, hash));
                        totalDups += count - 1;
                        increment(histogram, count);
                    }
                }

                rs.close();
                pst.close();

            } catch ( SQLException e ) {
                throw new ServletException(e);
            } finally {
                SQL.release(conn);
            }
        }

        Collections.sort(histogram);

        request.setAttribute(PAGE_COLLECTION, collectionId);
        request.setAttribute(PAGE_TOTAL_FILES, CollectionCountContext.getFileCount(collection));
        request.setAttribute(PAGE_TOTAL_DUPS, totalDups);
        request.setAttribute(PAGE_LIST, duplicateCount);
        request.setAttribute(PAGE_HISTO, histogram);
        request.setAttribute(PAGE_TIME, (System.currentTimeMillis() - startTime));
        LOG.info("Duplicate report on " + collection.getName() + " time "
                + (System.currentTimeMillis() - startTime) + " total dups " + totalDups);

        if ( hasJson(request) ) {
            dispatcher = request.getRequestDispatcher("duplicatereport-json.jsp");
        } else {
            dispatcher = request.getRequestDispatcher("duplicatereport.jsp");
        }
        dispatcher.forward(request, response);
    }

    private void increment( List<HistEntry> map, int idx ) {
        for ( HistEntry e : map ) {
            if ( e.digestCount == idx ) {
                e.instances++;
                return;
            }
        }
//        System.out.println("adding for " + idx);
        map.add(new HistEntry(idx));
    }

    public class HistEntry implements Comparable<HistEntry> {

        int digestCount;
        int instances;

        public HistEntry( int digestCount ) {
            this.digestCount = digestCount;
            this.instances = 1;
        }

        public int getInstances() {
            return instances;
        }

        public int getDigestCount() {
            return digestCount;
        }

        @Override
        public int compareTo( HistEntry o ) {
            if ( digestCount == o.digestCount ) {
                return digestCount - o.digestCount;
            }
            return instances - o.instances;
        }
    }

    public class MyEntry implements Comparable<MyEntry> {

        int count;
        String digest;

        public MyEntry( int count, String digest ) {
            this.count = count;
            this.digest = digest;
        }

        @Override
        public int compareTo( MyEntry o ) {
            if ( o.count != count ) {
                return o.count - count;
            } else {
                return o.digest.compareTo(digest);
            }
        }

        public String getDigest() {
            return digest;
        }

        public int getCount() {
            return count;
        }
    }
}
