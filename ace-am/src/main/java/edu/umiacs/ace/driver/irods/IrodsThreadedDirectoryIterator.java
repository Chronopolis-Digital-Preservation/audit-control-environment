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
// $Id: SrbDirectoryIterator.java 46 2011-01-12 19:32:51Z toaster $
package edu.umiacs.ace.driver.irods;

import edu.umiacs.ace.driver.QueryThrottle;
import edu.umiacs.ace.driver.DriverStateBean;
import edu.umiacs.ace.driver.DriverStateBean.State;
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.StateBeanDigestListener;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.ace.util.ThreadedDigestStream;
import edu.umiacs.ace.util.ThrottledInputStream;
import edu.umiacs.io.IO;
import edu.umiacs.irods.api.pi.GenQueryEnum;
import edu.umiacs.irods.operation.ConnectOperation;
import edu.umiacs.irods.operation.IrodsProxyInputStream;
import edu.umiacs.irods.operation.QueryBuilder;
import edu.umiacs.irods.operation.QueryResult;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 *
 * @author toaster
 */
public final class IrodsThreadedDirectoryIterator implements Iterator<FileBean> {

    private DriverStateBean[] statebeans;
    private static final int MAX_THREADS = 5;
    private static final int RETRY = 5;
    private static final int BLOCK_SIZE = 1048576;
    private static final Logger LOG = Logger.getLogger(
            IrodsThreadedDirectoryIterator.class);
    private boolean finished = false;
    private LinkedBlockingQueue<FileBean> readyList = new LinkedBlockingQueue<FileBean>();
    private Queue<String> dirsToProcess = new LinkedList<String>();
    private Queue<String> filesToProcess = new LinkedList<String>();
    private String root;
    private final List<ProcessFileThread> threads = new LinkedList<ProcessFileThread>();
    private PathFilter filter;
    private Thread takingThread = null;
    private double lastDelay = 0;
    private Lock loadLoack = new ReentrantLock();

    public IrodsThreadedDirectoryIterator(Collection c,
            MonitoredItem[] basePath, PathFilter filter, ConnectOperation co) {
        this.root = c.getDirectory();
        this.filter = filter;

        try {

            if (basePath != null) {
                for (MonitoredItem mi : basePath) {
                    String startFile;
                    startFile = this.root + mi.getPath();
                    if (mi.isDirectory()) {
                        dirsToProcess.add(startFile);

                    } else {
                        filesToProcess.add(startFile);

                    }
                }
            } else {
                dirsToProcess.add(root);
            }

            statebeans = new DriverStateBean[MAX_THREADS];
            for (int i = 0; i < MAX_THREADS; i++) {
                DriverStateBean sb = new DriverStateBean();
                MessageDigest digest = MessageDigest.getInstance(c.getDigestAlgorithm());
                Thread t = new Thread(new ProcessFileThread(digest, i, sb, co.clone()));
                statebeans[i] = sb;
                t.setName("iRODS Reader " + i + " " + c.getName());
                t.start();

            }
            // ensure this doesn't return before thread has started
            // race condition in hasNext otherwise
            if (threads.isEmpty()) {
                synchronized (threads) {
                    threads.wait();
                }
            }

        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (Throwable ioe) {
            LOG.error("Unknown irods startup error", ioe);
            throw new RuntimeException("Could not connect to irods", ioe);
        }

    }

    public DriverStateBean[] getStatebeans() {
        return statebeans;
    }

    /**
     * Has next if it's not finished or queue isn't empty
     * @return
     */
    @Override
    public boolean hasNext() {
        return (!threads.isEmpty() || !readyList.isEmpty());
    }

    public void cancel() {
        LOG.debug("Cancel called on " + root);
        this.finished = true;

//        if (force) {
//            pool.poolShutdown();
//        }
    }

    @Override
    public FileBean next() {
        takingThread = Thread.currentThread();
        try {
            return readyList.take();
        } catch (InterruptedException ie) {
            LOG.error("Interrupted Exception", ie);

            if (readyList.isEmpty()) {
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

    class ProcessFileThread implements Runnable {

        private DriverStateBean stateBean;
//        private byte[] buffer = new byte[2097152];
        private MessageDigest digest;
        private int id;
        private ConnectOperation co;
        private ThreadedDigestStream reader;

        public ProcessFileThread(MessageDigest digest, int id, DriverStateBean bean, ConnectOperation co) {
            this.id = id;
            this.digest = digest;
            this.stateBean = bean;
            this.co = co;
            this.reader = new ThreadedDigestStream(digest, BLOCK_SIZE);
        }

        @Override
        public void run() {
            NDC.push("[iRODS" + id + "] ");
            stateBean.setRunningThread(Thread.currentThread());
            LOG.debug("iRODS Thread starting: " + Thread.currentThread().getName());
            synchronized (threads) {
                threads.add(this);
                threads.notify();
            }
            try {
                while (!finished) {
                    String nextFile = null;
                    int retry = 0;


                    while (retry < RETRY) {
                        stateBean.setStateAndReset(State.WAITING_ON_FILE);
                        loadLoack.lockInterruptibly();
                        try {
                            stateBean.setStateAndReset(State.LISTING);
                            nextFile = loadNext();
                            retry = RETRY;
                        } catch (Exception e) {
                            LOG.error("loadNext threw error ", e);
                        } finally {
                            loadLoack.unlock();
                        }
                        retry++;
                    }

                    FileBean fb;

                    if (nextFile == null) {
                        continue;
                    }

                    int count = 1;
                    fb = processFile(nextFile);
                    while (count < RETRY && fb.isError() && !finished) {
                        fb = processFile(nextFile);
                        stateBean.setStateAndReset(State.IDLE);
                        count++;
                    }
                    // if pool is down, errored items should not be returned
//                    if (!pool.isShutdown()) {
                    readyList.offer(fb);
//                    }
                }
            } catch (InterruptedException e) {
                LOG.error("Interrupted, exiting ", e);
            } finally {
                LOG.debug(
                        "iRODS Thread finishing: " + Thread.currentThread().
                        getName());
                co.shutdown();
                synchronized (threads) {
                    threads.remove(this);
                    if (threads.isEmpty()) {
                        if (readyList.isEmpty() && takingThread != null) {
                            takingThread.interrupt();
                        }
//                        LOG.debug("Final iRODS Thread shutdown, closing connection");

//                        pool.poolShutdown();
                    }
                }

                // Destroy the reader as well or else the digset thread has a 
                // chance of hanging
                reader.shutdown();
                reader = null;
            }
        }

        @SuppressWarnings("empty-statement")
        private FileBean processFile(String file) {
            stateBean.setStateAndReset(State.OPENING_FILE);
            stateBean.setFile(file);
            stateBean.setRead(0);


            LOG.trace("Processing file: " + file + " finished state: " + finished );
            DigestInputStream dis = null;
            FileBean fb = new FileBean();
            fb.setPathList(extractPathList(file));

            digest.reset();
//            SRBFileSystem sfs = null;

            try {
                QueryThrottle.waitToRun();
                IrodsProxyInputStream iis = new IrodsProxyInputStream(file, co.getConnection());
                ThrottledInputStream tis = new ThrottledInputStream(iis,
                        QueryThrottle.getMaxBps() / MAX_THREADS, lastDelay);
                reader.setListener(new StateBeanDigestListener(stateBean));
                long fileSize = reader.readStream(tis);
                byte[] hashValue = digest.digest();
                tis.close();
                String hash = HashValue.asHexString(hashValue);
                fb.setHash(hash);
                fb.setFileSize(fileSize);
                lastDelay = tis.getSleepTime();
                LOG.trace(
                        "Closed Stream: " + file + " read: " + fileSize + " hash: " + hash);

            } catch (Throwable e) {
//                if (sfs != null) {
//                    pool.brokenConnection(sfs);
//                }
                LOG.error("Error reading file: " + file, e);
                fb.setError(true);
                fb.setErrorMessage(Strings.exceptionAsString(e));
            } finally {
                IO.release(dis);
                if (fb.getHash() == null && !fb.isError()) {
                    fb.setError(true);
                    fb.setErrorMessage("Null digest");
                }
                return fb;
            }
        }

        private void scanForDirectories(String parent) throws IOException {

            QueryBuilder qb;
            QueryResult qr;
//            try {

            qb = new QueryBuilder(GenQueryEnum.COL_COLL_NAME);
            qb.eq(GenQueryEnum.COL_COLL_PARENT_NAME, parent);
//            testConnection();
            qr = qb.execute(co.getConnection());

            while (qr.next()) {
                if (qr.getValue(GenQueryEnum.COL_COLL_NAME).equals(parent)) {
                    continue;
                }
                String dir = qr.getValue(GenQueryEnum.COL_COLL_NAME);
                if (filter.process(extractPathList(dir), true)) {
                    dirsToProcess.add(dir);
                }
            }

//            } catch (IRodsRequestException ex) {
//                if (ex.getErrorCode() != ErrorEnum.CAT_NO_ROWS_FOUND) {
//                    //TODO: handle this
//                    LOG.error("irods error ", ex);
//                }
//                throe new IOException(ex);
//            }
//            } catch (IOException ex) {
//                LOG.error("IOException ", ex);
//                //TODO: handle this
//            }

        }

        private void scanForFiles(String parent) throws IOException {
            QueryBuilder qb;
            QueryResult qr;
//        try {
            qb =
                    new QueryBuilder(GenQueryEnum.COL_DATA_NAME);

            qb.eq(GenQueryEnum.COL_COLL_NAME, parent);
//            testConnection();
            qr = qb.execute(co.getConnection());

            while (qr.next()) {
                String file = parent + "/" + qr.getValue(GenQueryEnum.COL_DATA_NAME);
                if (filter.process(extractPathList(file), false)) {
                    filesToProcess.add(file);
                }
            }

//        } catch (IOException ex) {
//            LOG.error("Exception", ex);
//        }
        }

        private String[] extractPathList(String file) {
            int substrLength = root.length();

            // build directory path
            List<String> dirPathList = new ArrayList<String>();
            String currFile = file;
            while (!currFile.equals(root)) {
//            LOG.trace("Adding dir to path: " + currFile.substring(substrLength));
                dirPathList.add(currFile.substring(substrLength));
                currFile = currFile.substring(0, currFile.lastIndexOf('/'));
            }
            return dirPathList.toArray(new String[dirPathList.size()]);
        }

        private String loadNext() {
            while (filesToProcess.isEmpty() && !dirsToProcess.isEmpty() && !finished) {//&& !pool.isShutdown() ) {
                String directory = dirsToProcess.poll();
                LOG.trace("Popping directory dirsToProcess: " + directory);

                try {
                    scanForDirectories(directory);
                    scanForFiles(directory);
                } catch (IOException ioe) {
                    LOG.error("IOexception reading directories", ioe);
                    throw new RuntimeException(ioe);
                }

            }

            if (!filesToProcess.isEmpty()) {
                return filesToProcess.poll();
            } else {
                LOG.trace("No more files left in queue, setting finished");
                finished = true;
                return null;
            }

        }
    }
}
