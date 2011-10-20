/*
 * Copyright (c) 2007-2011, University of Maryland
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
package edu.umiacs.ace.monitor.compare;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.util.PersistUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.queries.ScrollableCursor;

/**
 * Currently does all comparison in memory, this limits collections to a million
 * items or so, but prevents db thrashing.
 * 
 * 
 * @author toaster
 */
public final class CollectionCompare2 {

    private Map<String, String> sourceMap;
    private Map<String, String> sourceReverseMap;
    private static final Logger LOG = Logger.getLogger(CollectionCompare2.class);
    private List<String> parseErrors = new ArrayList<String>();

    public CollectionCompare2(InputStream sourceFile, String prefix, int hint) {
        sourceMap = new TreeMap<String, String>();
        sourceReverseMap = new TreeMap<String, String>();
        LOG.trace("initializing collection compare with hint: " + hint);
        try {
            parseInputStream(sourceFile, prefix);
        } catch (IOException e) {
            LOG.error("Error reading digest source", e);
            throw new RuntimeException(e);
        }
    }

    Map<String, String> getSourceMap() {
        return sourceMap;
    }

    public List<String> getParseErrors() {
        return Collections.unmodifiableList(parseErrors);
    }

    public void compareTo(CompareResults cr, Collection c, MonitoredItem item) {
        EntityManager em = PersistUtil.getEntityManager();
        long time = System.currentTimeMillis();
        long total = 0;
        DataSource db;
        MonitoredItem mi;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            LOG.info("Starting collection compare on " + c.getName() + " source size: " + sourceMap.size());
            db = PersistUtil.getDataSource();
            conn = db.getConnection();
            stmt = conn.prepareStatement(
                    "SELECT monitored_item.PATH, monitored_item.FILEDIGEST, monitored_item.TOKEN_ID "
                    + "FROM monitored_item "
                    + "WHERE monitored_item.PARENTCOLLECTION_ID = ? "
                    + "AND monitored_item.DIRECTORY = 0",
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setLong(1, c.getId());
            stmt.setFetchSize(Integer.MIN_VALUE);
            rs = stmt.executeQuery();
//            Query q = em.createNamedQuery("MonitoredItem.listFilesInCollection");
////            q.setLockMode(LockModeType.NONE);
//            q.setHint(QueryHints.JDBC_FETCH_SIZE, 5000);
////            q.setHint(QueryHints.RESULT_SET_TYPE, ResultSetType.ForwardOnly);
//            q.setHint("eclipselink.cursor.scrollable",true);
//            q.setParameter("coll", c);
////            em.getTransaction().begin();
//            ScrollableCursor cursor = (ScrollableCursor) q.getSingleResult();
////            List items = q.getResultList();
//
////            for (Object o : items) {
//            Object o;
//            while ((o = cursor.next()) != null) {
            while (rs.next()) {
                total++;

                if ((total % 100000) == 0) {
                    LOG.trace("Compared " + total);
                }
//                MonitoredItem aceItem = (MonitoredItem) o;
                String acePath = rs.getString(1);//aceItem.getPath();
                String aceDigest = rs.getString(2);//aceItem.getFileDigest();

                if (sourceMap.containsKey(acePath)) {
                    cr.fileExistsAtTarget(acePath);

                    if (sourceMap.get(acePath).matches(aceDigest)) {
//Perfect file, no-op
                    } else {
                        cr.mismatchedDigests(acePath, sourceMap.get(acePath), aceDigest);
                    }
                } else if (sourceReverseMap.containsKey(aceDigest)) {
                    cr.fileExistsAtTarget(sourceReverseMap.get(aceDigest));
                    cr.mismatchedNames(aceDigest, acePath, sourceReverseMap.get(aceDigest));
                } else {
                    cr.sourceFileNotFound(acePath);
                }
            }
        } catch (Exception e) {
            LOG.error("Error during load and compare: ", e);

        } finally {
        SQL.release(rs);
        SQL.release(conn);
            LOG.info("Finished collection compare on: "
                    + c.getName() + " time: " + (System.currentTimeMillis() - time) + " tested: " + total);
            cr.finished();
            em.close();
        }
    }

    private void parseInputStream(InputStream sourceFile, String prefix) throws IOException {

        BufferedReader input = new BufferedReader(new InputStreamReader(sourceFile));
        String line = input.readLine();


        long total = 0;

        // ignore ace manifest header


        if (line != null && line.matches("^[A-Z0-9\\-]+:.+$")) {
            line = input.readLine();


        }

        while (line != null) {
            total++;


            if ((total % 100000) == 0) {
                LOG.trace("Loaded " + total);


            }
            String tokens[] = line.split("\\s+", 2);


            if (tokens == null || tokens.length != 2) {
                LOG.error("Error processing line: " + line);
                parseErrors.add("Corrupt Line: " + line);


            } else {
                String path = tokens[1];
                // temp hack to make sure all paths start w/ / as ace expectes


                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                sourceMap.put(path, tokens[0]);
                sourceReverseMap.put(tokens[0], path);


            }
            line = input.readLine();

        }
    }
}
