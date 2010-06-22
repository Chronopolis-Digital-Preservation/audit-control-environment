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
// $Id: SrbDirectoryIterator.java 3181 2010-06-16 20:46:16Z toaster $

package edu.umiacs.ace.driver.srb;

import edu.umiacs.ace.driver.QueryThrottle;
import edu.sdsc.grid.io.srb.SRBFileInputStream;
import edu.sdsc.grid.io.srb.SRBFileSystem;
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.ace.util.ThrottledInputStream;
import edu.umiacs.io.IO;
import edu.umiacs.srb.connection.ConnectionPool;
import edu.umiacs.srb.util.SrbUtil;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 *
 * @author toaster
 */
public class SrbDirectoryIterator implements Iterator<FileBean> {

    private static final int MAX_THREADS = 5;
    private static final int RETRY = 5;
    private static final Logger LOG = Logger.getLogger(
            SrbDirectoryIterator.class);
    private Collection collection;
    private ConnectionPool pool;
    private boolean finished = false;
    private LinkedBlockingQueue<FileBean> readyList = new LinkedBlockingQueue<FileBean>();
    private Queue<String> dirsToProcess = new LinkedList<String>();
    private Queue<String> filesToProcess = new LinkedList<String>();
    private String root;
    private final List<ProcessFileThread> threads = new LinkedList<ProcessFileThread>();
    private PathFilter filter;
    private Thread takingThread = null;
    private double lastDelay = 0;

    public SrbDirectoryIterator( Collection c, SrbSettings settings,
            MonitoredItem[] basePath, PathFilter filter, String digestAlgorithm ) {
        this.collection = c;
        this.pool = settings.createPool();
        this.root = c.getDirectory();
        this.filter = filter;

        SRBFileSystem sfs = null;
        try {

            pool.freeConnection(sfs = pool.getConnection());
//            String startPath = root;
            if ( basePath != null ) {
                for ( MonitoredItem mi : basePath ) {
                    String startFile;
                    startFile = this.root + mi.getPath();
                    if ( mi.isDirectory() ) {
                        dirsToProcess.add(startFile);

                    } else {
                        filesToProcess.add(startFile);

                    }
                }
//                startPath = this.root + basePath;
            } else {
                dirsToProcess.add(root);
            }

            for ( int i = 0; i < MAX_THREADS; i++ ) {
                MessageDigest digest = MessageDigest.getInstance(digestAlgorithm);
                Thread t = new Thread(new ProcessFileThread(digest, i));
                t.setName("SRB Reader " + i + " " + c.getName());
                t.start();

            }
            // ensure this doesn't return before thread has started
            // race condition in hasNext otherwise
            if ( threads.isEmpty() ) {
                synchronized ( threads ) {
                    threads.wait();
                }
            }

        } catch ( NoSuchAlgorithmException ex ) {
            throw new RuntimeException(ex);
        } catch ( Throwable ioe ) {
            if ( sfs != null ) {
                pool.brokenConnection(sfs);
            }
            LOG.error("Could not connect to the srb", ioe);
            throw new RuntimeException("Could not connect to the srb", ioe);
        }

    }

    /**
     * Has next if it's not finished or queue isn't empty
     * @return
     */
    @Override
    public boolean hasNext() {
        return (!threads.isEmpty() || !readyList.isEmpty());
    }

    public void cancel( boolean force ) {
        LOG.debug("Cancel called on " + root);
        this.finished = true;
        if ( force ) {
            pool.poolShutdown();
        }
    }

    @Override
    public FileBean next() {
        takingThread = Thread.currentThread();
        try {
            return readyList.take();
        } catch ( InterruptedException ie ) {
            LOG.error("Interrupted Exception", ie);

            if ( readyList.isEmpty() ) {
                return null;
            }
            throw new RuntimeException(ie);
        } finally {
            takingThread = null;
        }

    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported");
    }

    /**
     * Load next file
     * @return
     */
    private synchronized String loadNext() {
        while ( filesToProcess.isEmpty() && !dirsToProcess.isEmpty() && !pool.isShutdown() ) {
            String directory = dirsToProcess.poll();
            LOG.trace("Popping directory dirsToProcess: " + directory);

            try {

                for ( String s : listDirectories(directory) ) {
                    if ( filter.process(extractPath(s), true) ) {
                        LOG.trace("Adding directory dirsToProcess" + s);
                        dirsToProcess.add(s);
                    }
                }

                for ( String s : listFiles(directory) ) {
                    String fullFile = directory + "/" + s;
                    if ( filter.process(extractPath(fullFile), false) ) {
                        LOG.trace("Adding file filesToProcess" + fullFile);
                        filesToProcess.add(fullFile);
                    }

                }
            } catch ( IOException ioe ) {
                LOG.error("IOexception reading directories", ioe);
                throw new RuntimeException(ioe);
            }

        }

        if ( !filesToProcess.isEmpty() ) {
            return filesToProcess.poll();
        } else {
            LOG.trace("No more files left in queue, setting finished");
            finished = true;
            return null;
        }

    }

    private List<String> listDirectories( String path ) throws IOException {
        SRBFileSystem connection = null;
        List<String> retValue = null;
        IOException thrown = null;

        int count = 0;
        while ( retValue == null && count < RETRY ) {
            try {
                count++;
                if ( pool.isShutdown() ) {
                    return null;
                }
                connection = pool.getConnection();
                retValue = SrbUtil.listDirectoriesInDirectory(connection, path);
                pool.freeConnection(connection);
                return retValue;

            } catch ( IOException ex ) {
                LOG.debug("srb IOException ", ex);
                retValue = null;
                if ( connection != null ) {
                    pool.brokenConnection(connection);

                }
                thrown = ex;
            }
        }

        throw thrown;

    }

    private List<String> listFiles( String path ) throws IOException {
        SRBFileSystem connection = null;
        List<String> results = null;
        IOException thrown = null;

        int count = 0;
        while ( results == null && count < RETRY ) {
            try {
                count++;
                if ( pool.isShutdown() ) {
                    return null;
                }
                connection = pool.getConnection();
                results = SrbUtil.listFilesInDirectory(connection, path);
                pool.freeConnection(connection);
                return results;

            } catch ( IOException ex ) {
                results = null;
                LOG.debug("srb IOException ", ex);
                if ( connection != null ) {
                    pool.brokenConnection(connection);
                }
                thrown = ex;
            }
        }
        throw thrown;

    }

    private String[] extractPath( String file ) {
        int substrLength = root.length();

        // build directory path
        List<String> dirPathList = new ArrayList<String>();
        String currFile = file;
        while ( !currFile.equals(root) ) {
//            LOG.trace("Adding dir to path: " + currFile.substring(substrLength));
            dirPathList.add(currFile.substring(substrLength));
            currFile = currFile.substring(0, currFile.lastIndexOf('/'));
        }
        return dirPathList.toArray(new String[dirPathList.size()]);
    }

    class ProcessFileThread implements Runnable {

        private byte[] buffer = new byte[2097152];
        private MessageDigest digest;
        private int id;

        public ProcessFileThread( MessageDigest digest, int id ) {
            this.id = id;
            this.digest = digest;
        }

        @Override
        public void run() {
            NDC.push("[SRB" + id + "] ");
            LOG.debug("SRB Thread starting: " + Thread.currentThread().getName());
            synchronized ( threads ) {
                threads.add(this);
                threads.notify();
            }
            try {
                while ( !finished ) {
                    String nextFile = null;
                    int retry = 0;

                    while ( retry < RETRY ) {
                        try {
                            nextFile = loadNext();
                            retry = RETRY;
                        } catch ( Exception e ) {
                            LOG.error("loadNext threw error ", e);
                        }
                        retry++;
                    }

                    FileBean fb;

                    if ( nextFile == null ) {
                        continue;
                    }

                    int count = 1;
                    fb = processFile(nextFile);
                    while ( count < RETRY && fb.isError() && !pool.isShutdown() ) {
                        fb = processFile(nextFile);
                        count++;
                    }
                    // if pool is down, errored items should not be returned
                    if ( !pool.isShutdown() ) {
                        readyList.offer(fb);
                    }
                }
            } finally {
                LOG.debug(
                        "SRB Thread finishing: " + Thread.currentThread().
                        getName());

                synchronized ( threads ) {
                    threads.remove(this);
                    if ( threads.isEmpty() ) {
                        if ( readyList.isEmpty() && takingThread != null ) {
                            takingThread.interrupt();
                        }
                        LOG.debug("Final SRB Thread shutdown, closing pool");
                        pool.poolShutdown();
                    }
                }
            }
        }

        @SuppressWarnings("empty-statement")
        private FileBean processFile( String file ) {
            String hash;
            long size = 0;
            LOG.trace("Processing file: " + file + " finished state: " + finished + " pool active: "
                    + !pool.isShutdown());
            DigestInputStream dis = null;
            FileBean fb = new FileBean();
            fb.setPathList(extractPath(file));

            digest.reset();
            SRBFileSystem sfs = null;

            try {
                QueryThrottle.waitToRun();
                sfs = pool.getConnection();
                ThrottledInputStream tis = new ThrottledInputStream(new SRBFileInputStream(sfs, file),
                        QueryThrottle.getMaxBps() / MAX_THREADS, lastDelay);
                dis = new DigestInputStream(tis, digest);
//                LOG.trace("Opened srb input stream" + file);
                int read = 0;
                while ( (read = dis.read(buffer)) >= 0 ) {
                    size += read;
                }

                byte[] hashValue = digest.digest();
                dis.close();
                hash = HashValue.asHexString(hashValue);
                fb.setHash(hash);
                fb.setFileSize(size);
                lastDelay = tis.getSleepTime();
                LOG.trace(
                        "Closed Stream: " + file + " read: " + size + " hash: " + hash);
                pool.freeConnection(sfs);

            } catch ( Throwable e ) {
                if ( sfs != null ) {
                    pool.brokenConnection(sfs);
                }
                LOG.error("Error reading file: " + file, e);
                fb.setError(true);
                fb.setErrorMessage(Strings.exceptionAsString(e));
            } finally {
                IO.release(dis);
                if ( fb.getHash() == null && !fb.isError() ) {
                    fb.setError(true);
                    fb.setErrorMessage("Null digest");
                }
                return fb;
            }
        }
    }
}
