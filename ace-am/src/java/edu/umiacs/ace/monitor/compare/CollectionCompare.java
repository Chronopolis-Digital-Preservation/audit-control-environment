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

import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.sql.SQL;
import edu.umiacs.util.Strings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

/**
 * Target is a pre-existing collection in the AM
 * Supplied is the collection supplied on the inputstream
 * 
 * @author toaster
 */
public class CollectionCompare {

    private DataSource db;
    private long[] suppliedSeen;
    private int totalSupplied;
    private long[] targetSeen;
    private int totalTarget;
    private String suppliedTable;
    private String targetTable;
    private boolean running = true;
    private String message = null;
    private List<String> unseenTarget = null;
    private List<String> unseenSupplied = null;
    private Set<DifferingName> differingNames = new TreeSet<DifferingName>();
    private Set<DifferingDigest> differingDigests = new TreeSet<DifferingDigest>();
    private static final Logger LOG = Logger.getLogger(CollectionCompare.class);

    public CollectionCompare( InputStream sourceFile, String prefix ) {
        if ( sourceFile == null ) {
            throw new NullPointerException("null sourceFile inputstream");
        }
        targetTable = "tmpColl" + System.currentTimeMillis();
        suppliedTable = "tmpInput" + System.currentTimeMillis();
        db = PersistUtil.getDataSource();
        // load input
        parseInputStream(sourceFile, prefix);

    }

    public boolean isRunning() {
        return running;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Files that exist in the collection, but not target
     * @return
     */
    public List<String> getUnseenSuppliedFiles() {
        if ( unseenSupplied == null ) {
            unseenSupplied = loadUnMarked(suppliedSeen, suppliedTable,
                    totalSupplied);
        }
        return unseenSupplied;
    }

    /**
     * Files that exist in the target but not collection
     * @return
     */
    public List<String> getUnseenTargetFiles() {
        if ( unseenTarget == null ) {
            unseenTarget = loadUnMarked(targetSeen, targetTable, totalTarget);
        }
        return unseenTarget;
    }

    public Set<DifferingDigest> getDifferingDigests() {
        return differingDigests;
    }

    public Set<DifferingName> getDifferingNames() {
        return differingNames;
    }

    private String getFile( String table, int index ) {
        Connection conn = null;
        PreparedStatement stmt = null;
        String statement = "SELECT path FROM " + table + " WHERE id = ?";
        String name;

        LOG.debug("start getFile " + (index + 1) + " table " + table);
        try {
            conn = db.getConnection();
            stmt = conn.prepareStatement(statement);
            stmt.setInt(1, index + 1);
            ResultSet rs = stmt.executeQuery();
            if ( rs.next() ) {
                name = rs.getString("path");
                rs.close();
                return name;
            }
            LOG.error(
                    "No results retrieved from " + table + " idx " + (index + 1));
            throw new RuntimeException(
                    "No results retrieved from " + table + " idx " + (index + 1));
        } catch ( SQLException e ) {
            LOG.error("getFileError", e);
            throw new RuntimeException(e);
        } finally {
            SQL.release(stmt);
            SQL.release(conn);
        }
    }

    private List<String> loadUnMarked( long[] array, String tableName, int total ) {
        List<String> list = new ArrayList<String>();
        long startTime = System.currentTimeMillis();
        int idx = 0;

        for ( ; idx < array.length; idx++ ) {
            long value = array[idx];
            // complete mask
            if ( (idx + 1) * 64 <= total ) {
                if ( ((long) -1) != value ) {
//                    LOG.debug(" expected " + showBits(((long) -1)) + " value " + showBits(
//                            value));
                    // we have unseen files, let's see what is affected
                    // This can be sped up by segmenting, but i'm lazy so linear wins
                    long mask = 1;
                    for ( int i = 0; i < 64; i++ ) {
//                        long mask = ((long) 1) << i;
                        if ( (value & mask) != mask ) {
                            LOG.debug(" expected " + showBits(((long) -1)) + " value " + showBits(
                                    value) + " error " + i);
                            list.add(getFile(tableName, idx + i));
                        }
                        mask <<= 1;
                    }
                }
            } // partial
            else {
                int emptyBits = 64 - total % 64;
//                LOG.debug(
//                        "value " + showBits(value) + " shift " 
//                        + showBits(((long) -1) >>> emptyBits) + " empty " 
//                        + emptyBits);

                if ( (((long) -1) >>> emptyBits) != value ) {
                    // we have unseen files, let's see what is affected
                    // This can be sped up by segmenting, but i'm lazy so linear wins
                    long mask = 1;
                    for ( int i = 0; i < (64 - emptyBits); i++ ) {
//                        long mask = ((long) 1) << i;
                        if ( (value & mask) != mask ) {
                            list.add(getFile(tableName, idx + i));
                        }
                        mask <<= 1;
                    }
                }
            }
        }
        LOG.debug(
                "finished loadUnmarked " + tableName + " time " + (System.currentTimeMillis()
                - startTime) + " idx " + idx);
        return list;
    }

    public void loadCollectionTable( Collection c, MonitoredItem item ) {
        Connection conn = null;
        PreparedStatement stmt = null;
        String createStmt;
        long startTime = System.currentTimeMillis();

        try {
            LOG.debug("start loadCollectionTable");
            message = "Loading target collection";
            conn = db.getConnection();

            createStmt = "CREATE TABLE " + targetTable + "(id bigint auto_increment not null, "
                    + "path VARCHAR(512) COLLATE latin1_bin, " + "digest VARCHAR(255), "
                    + "PRIMARY KEY(id), " + "KEY idx_digest_" + targetTable + " (digest(255)),"
                    + "KEY idx_name_" + targetTable + " (path(512)))";
            stmt = conn.prepareStatement(createStmt);
            stmt.execute();
            stmt.close();

            if ( item == null ) {
                stmt = conn.prepareStatement(
                        "INSERT INTO " + targetTable
                        + "(path,digest) SELECT monitored_item.PATH, monitored_item.FILEDIGEST "
                        + "FROM monitored_item " + "WHERE monitored_item.PARENTCOLLECTION_ID = ? "
                        + "AND monitored_item.DIRECTORY = 0");
            } else {
                stmt = conn.prepareStatement(
                        "INSERT INTO " + targetTable
                        + "(path,digest) SELECT monitored_item.PATH, monitored_item.FILEDIGEST "
                        + "FROM monitored_item " + "WHERE  monitored_item.PARENTCOLLECTION_ID = ? "
                        + "AND monitored_item.DIRECTORY = 0 " + "AND monitored_item.PATH like ?");
                stmt.setString(2, item.getPath() + "%");
            }
            stmt.setLong(1, c.getId());
            totalTarget = stmt.executeUpdate();
            stmt.close();
            targetSeen = new long[(totalTarget + 63) / 64];
            Arrays.fill(targetSeen, 0);
            LOG.debug(
                    "finished loadCollectionTable " + (System.currentTimeMillis() - startTime));
        } catch ( SQLException e ) {
            throw new RuntimeException(e);
        } finally {
            message = "";
            SQL.release(stmt);
            SQL.release(conn);
        }

    }

    public synchronized void doCompare() {
        if ( !running ) {
            return;
        }

        try {
            message = "Looking for identical entries";
            markIdentical();
            message = "Looking for identical digests but different names";
            markDifferingNames();
            message = "Looking for identical names but different digests";
            markDifferingDigests();
            message = "";
            getUnseenTargetFiles();
            getUnseenSuppliedFiles();
        } catch ( Exception e ) {
            message = Strings.exceptionAsString(e);
            LOG.error("Uncaught exception", e);
        } finally {
            running = false;
        }
    }

    private void markDifferingNames() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {

            // differing names
            LOG.debug("Starting markDifferingNames");
            long startTime = System.currentTimeMillis();

            String diffNamesSt = "SELECT t.digest,t.path,s.path,t.id,s.id FROM " + targetTable
                    + " t, " + suppliedTable + " s "
                    + "WHERE t.digest = s.digest AND t.path <> s.path";
            conn = db.getConnection();
            stmt = conn.prepareStatement(diffNamesSt, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            ResultSet rs = stmt.executeQuery();
            while ( rs.next() ) {
                String digest = rs.getString(1);
                String tName = rs.getString(2);
                String sName = rs.getString(3);
                int tid = rs.getInt(4);
                int sid = rs.getInt(5);

                if ( !hasMark(targetSeen, tid - 1) || !hasMark(suppliedSeen,
                        sid - 1) ) {
                    LOG.debug("Adding " + tName + "  " + sName);
                    differingNames.add(new DifferingName(tName, sName, digest));
                    markBitSeen(targetSeen, tid - 1);
                    markBitSeen(suppliedSeen, sid - 1);
                }
            }
            rs.close();
            stmt.close();
            LOG.debug(
                    "Ending markDifferingNames, time: " + (System.currentTimeMillis() - startTime));
        } catch ( SQLException e ) {
            throw new RuntimeException(e);
        } finally {
            SQL.release(stmt);
            SQL.release(conn);
        }

    }

    private void markDifferingDigests() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs;

        try {
            LOG.debug("Starting markDifferingDigests");
            long startTime = System.currentTimeMillis();
            // Case for same name, differing digests
            String diffDigestSt = "SELECT t.path,t.digest,s.digest,t.id,s.id FROM " + targetTable
                    + " t, " + suppliedTable + " s "
                    + "WHERE t.path = s.path AND t.digest <> s.digest";
            conn = db.getConnection();
            stmt = conn.prepareStatement(diffDigestSt);
            rs = stmt.executeQuery();
            while ( rs.next() ) {
                String name = rs.getString(1);
                String tDigest = rs.getString(2);
                String sDigest = rs.getString(3);
                int tid = rs.getInt(4);
                int sid = rs.getInt(5);
                LOG.debug("Adding " + name);

                differingDigests.add(new DifferingDigest(name, tDigest,
                        sDigest));
                markBitSeen(targetSeen, tid - 1);
                markBitSeen(suppliedSeen, sid - 1);
            }
            rs.close();
            stmt.close();
            LOG.debug(
                    "Ending markDifferingDigests, time: " + (System.currentTimeMillis() - startTime));
        } catch ( SQLException e ) {
            throw new RuntimeException(e);
        } finally {
            SQL.release(stmt);
            SQL.release(conn);
        }
    }

    private void markIdentical() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        long total = 0;

        String equalFiles = "SELECT s.id, t.id FROM " + suppliedTable + " s, " + targetTable
                + " t WHERE s.path = t.path AND s.digest = t.digest";
        LOG.debug("markIdentical statement: " + equalFiles);
        long startTime = System.currentTimeMillis();

        try {

            conn = db.getConnection();
            stmt = conn.prepareStatement(equalFiles);
            rs = stmt.executeQuery();
//            rs.

            while ( rs.next() ) {
                total++;
                markBitSeen(targetSeen, rs.getInt("t.id") - 1);
                markBitSeen(suppliedSeen, rs.getInt("s.id") - 1);
            }
            LOG.debug(
                    "markIdentical, found: " + total + " time " + (System.currentTimeMillis()
                    - startTime));
        } catch ( SQLException e ) {
            LOG.error("markIdentical exception", e);
            throw new RuntimeException(e);
        } finally {
            SQL.release(stmt);
            SQL.release(conn);
        }

    }

    private boolean hasMark( long[] array, int bit ) {
        int index = bit / 64;
        int offset = bit % 64;
        long mask = ((long) 1) << offset;
        return (array[index] & mask) == mask;
    }

    private void markBitSeen( long[] array, int bit ) {
        int index = bit / 64;
        int offset = bit % 64;
        long mask = ((long) 1) << offset;
//       LOG.debug("old value: " + array[index]);
        array[index] |= mask;
//       LOG.debug("new value: " + array[index] + " index " + index + " offset " + offset + " mask " + mask);
    }

    private void parseInputStream( InputStream sourceFile, String prefix ) {
        Connection conn = null;
        PreparedStatement stmt = null;
        String createStmt;

        LOG.debug("start parseInputStream");
        long startTime = System.currentTimeMillis();

        try {

            conn = db.getConnection();

            createStmt = "CREATE TABLE " + suppliedTable + "(id bigint auto_increment not null, "
                    + "path VARCHAR(512) COLLATE latin1_bin, " + "digest VARCHAR(255), "
                    + "PRIMARY KEY(id), " + "KEY idx_digest_" + suppliedTable + " (digest(255)),"
                    + "KEY idx_name_" + suppliedTable + " (path(512)))";
            stmt = conn.prepareStatement(createStmt);
            stmt.execute();
            stmt.close();

            // drop indices for load
            stmt = conn.prepareStatement(
                    "ALTER TABLE " + suppliedTable + " DISABLE KEYS");
            stmt.execute();
            stmt.close();

            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(
                    "INSERT INTO " + suppliedTable + "(path,digest) values (?,?)");
            totalSupplied = 0;


            for ( SourceFile source : readFile(sourceFile, prefix) ) {
                stmt.setString(1, source.getName());
                stmt.setString(2, source.getDigest());
                stmt.addBatch();
                if ( totalSupplied % 50000 == 0 ) {
                    stmt.executeBatch();
                    stmt.close();
//                    conn.commit();
                    stmt = conn.prepareStatement(
                            "INSERT INTO " + suppliedTable + "(path,digest) values (?,?)");
                }
//                stmt.executeUpdate();
                totalSupplied++;
//                if (totalSupplied % 5000 == 0)
//                {
//                    LOG.debug("Read " + totalSupplied);
//                }

            }
            stmt.executeBatch();
            stmt.close();

            // re-add indices for load
            stmt = conn.prepareStatement(
                    "ALTER TABLE " + suppliedTable + " ENABLE KEYS");
            stmt.execute();
            stmt.close();

            conn.commit();
            conn.setAutoCommit(true);
            suppliedSeen = new long[(int) ((totalSupplied + 63) / 64)];
            Arrays.fill(suppliedSeen, 0);
            LOG.trace(
                    "end parseInputStream, time: " + (System.currentTimeMillis() - startTime));
        } catch ( SQLException e ) {
            LOG.error("parseInputStream exception ", e);
            cleanup();
            throw new RuntimeException(e);
        } finally {
            SQL.release(stmt);
            SQL.release(conn);
        }
    }

    public void cleanup() {
        String dropCollStmt = "DROP TABLE IF EXISTS " + suppliedTable;
        String dropFileStmt = "DROP TABLE IF EXISTS " + targetTable;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {

            conn = db.getConnection();
            stmt = conn.prepareStatement(dropCollStmt);
            stmt.execute();

            conn = db.getConnection();
            stmt = conn.prepareStatement(dropFileStmt);
            stmt.execute();

        } catch ( SQLException e ) {
            LOG.error("cleanup exception", e);
            throw new RuntimeException(e);
        } finally {
            SQL.release(stmt);
            SQL.release(conn);
        }
    }

    private Iterable<SourceFile> readFile( final InputStream sourceColl,
            final String prefix ) {
        return new Iterable<SourceFile>() {

            @Override
            public Iterator<SourceFile> iterator() {
                return new ReadIterator(sourceColl, prefix);
            }
        };
    }

    static class ReadIterator implements Iterator<SourceFile> {

        private BufferedReader input;
        private SourceFile next = null;
        private String prefix = null;

        private ReadIterator( InputStream input, String prefix ) {
            this.prefix = prefix;
            this.input = new BufferedReader(new InputStreamReader(input));
            next = readNext();

        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public SourceFile next() {
            SourceFile oldNext = next;

            next = readNext();

            return oldNext;
        }

        private SourceFile readNext() {
            String[] tokens = readLine();

            while ( tokens != null && (tokens.length != 2 || (prefix != null && !tokens[1].startsWith(
                    prefix))) ) {
                tokens = readLine();
            }

            if ( tokens != null ) {
                return new SourceFile(tokens[1], tokens[0]);
            } else {
                return null;
            }
        }

        private String[] readLine() {
            try {
                String line = input.readLine();
                if ( line == null ) {
                    return null;
                }
                String[] tokens = line.split("\t");
                return tokens;

            } catch ( IOException e ) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    static class SourceFile {

        private String name;
        private String digest;

        private SourceFile( String name, String digest ) {
            this.name = name;
            this.digest = digest;
        }

        public void setDigest( String digest ) {
            this.digest = digest;
        }

        public void setName( String name ) {
            this.name = name;
        }

        public String getDigest() {
            return digest;
        }

        public String getName() {
            return name;
        }
    }

    public static class DifferingName implements Comparable<DifferingName> {

        private String compString;
        private String sourceName;
        private String destinationName;
        private String digest;

        private DifferingName( String sourceName, String destinationName,
                String digest ) {
            this.sourceName = sourceName;
            this.destinationName = destinationName;
            this.digest = digest;
            compString = sourceName + destinationName + digest;
        }

        public String getDestinationName() {
            return destinationName;
        }

        public String getDigest() {
            return digest;
        }

        public String getSourceName() {
            return sourceName;
        }

        public void setDestinationName( String destinationName ) {
            this.destinationName = destinationName;
        }

        public void setDigest( String digest ) {
            this.digest = digest;
        }

        public void setSourceName( String sourceName ) {
            this.sourceName = sourceName;
        }

        @Override
        public int hashCode() {
            return compString.hashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            if ( obj instanceof DifferingName ) {
                DifferingName dd = (DifferingName) obj;
                return compString.equals(dd.compString);
            }
            return false;
        }

        @Override
        public int compareTo( DifferingName o ) {
            return compString.compareTo(o.compString);
        }
    }

    public static class DifferingDigest implements Comparable<DifferingDigest> {

        private String name;
        private String sourceDigest;
        private String targetDigest;
        private String compString;

        private DifferingDigest( String name, String sourceDigest,
                String targetDigest ) {
            this.name = name;
            this.sourceDigest = sourceDigest;
            this.targetDigest = targetDigest;
            compString = name + sourceDigest + targetDigest;
        }

        public String getName() {
            return name;
        }

        public String getSourceDigest() {
            return sourceDigest;
        }

        public String getTargetDigest() {
            return targetDigest;
        }

        public void setName( String name ) {
            this.name = name;
        }

        public void setTargetDigest( String targetDigest ) {
            this.targetDigest = targetDigest;
        }

        public void setSourceDigest( String sourceDigest ) {
            this.sourceDigest = sourceDigest;
        }

        @Override
        public int compareTo( DifferingDigest o ) {
            return compString.compareTo(o.compString);
        }

        @Override
        public int hashCode() {
            return compString.hashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            if ( obj instanceof DifferingDigest ) {
                DifferingDigest dd = (DifferingDigest) obj;
                return compString.equals(dd.compString);
            }
            return false;
        }
    }

    /**
     * Debug function to display long as a list o bits
     * @param x
     * @return
     */
    private static String showBits( long x ) {
        StringBuffer sb = new StringBuffer();
        for ( int i = 63; i >= 0; --i ) {
            sb.append((x >> i) & 1);
            if ( (i % 4) == 0 ) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }
}
