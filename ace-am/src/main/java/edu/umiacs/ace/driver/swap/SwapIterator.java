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
// $Id: LocalFileAccess.java 19 2010-10-22 18:45:19Z toaster $
package edu.umiacs.ace.driver.swap;

import edu.umiacs.ace.driver.DriverStateBean;
import edu.umiacs.ace.driver.DriverStateBean.State;
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.QueryThrottle;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.ace.util.ThrottledInputStream;
import edu.umiacs.io.IO;
import edu.umiacs.util.Strings;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
import org.swap.client.SwapClient;
import org.swap.client.SwapFile;

/**
 *
 * @author toaster
 */
public final class SwapIterator implements Iterator<FileBean> {

    private Queue<SwapFile> dirsToProcess = new LinkedList<SwapFile>();
    private LinkedBlockingQueue<SwapFile> filesToProcess = new LinkedBlockingQueue<SwapFile>();
    private boolean finished = false;
    private SwapFile rootFile;
    private PathFilter filter;
    private DriverStateBean[] statebeans;
    private static final int MAX_THREADS = 5;
    private double lastDelay = 0;
    private final List<FileWorker> threads = new LinkedList<FileWorker>();
    private Thread takingThread = null;
    private LinkedBlockingQueue<FileBean> resultQueue = new LinkedBlockingQueue<FileBean>();
    private static final Logger LOG = Logger.getLogger(SwapIterator.class);
    private SwapClient client;
    private Lock loadLoack = new ReentrantLock();

    public SwapIterator( MonitoredItem[] basePath, PathFilter filter,
            String digestAlgorithm, String collectionName,
            SwapFile rootFile, SwapClient client ) {

        this.filter = filter;
        this.rootFile = rootFile;
        this.client = client;
        try {
            if ( basePath != null ) {
                for ( MonitoredItem mi : basePath ) {
                    String startFile;
                    if (rootFile.getFullPath().equals("/"))
                    startFile = mi.getPath();
                    else
                    startFile = rootFile.getFullPath() + mi.getPath();
                    LOG.trace("Supplied file for processing: " + startFile);
                    SwapFile sf = rootFile.getFileGroup().getFileDetails(startFile);

                    if ( mi.isDirectory() ) {
                        dirsToProcess.add(sf);

                    } else {
                        filesToProcess.add(sf);

                    }
                }
            } else {
                dirsToProcess.add(rootFile);
            }

            statebeans = new DriverStateBean[MAX_THREADS];
            for ( int i = 0; i < MAX_THREADS; i++ ) {
                DriverStateBean sb = new DriverStateBean();
                MessageDigest digest = MessageDigest.getInstance(digestAlgorithm);
                Thread t = new Thread(new FileWorker(sb, i, digest));
                statebeans[i] = sb;
                t.setName("SWAP Reader " + i + " " + collectionName);
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
            LOG.error("Could not connect to the srb", ioe);
            throw new RuntimeException("Could not connect to the srb", ioe);
        }
    }

    public void cancel() {
        this.finished = true;
    }

    public DriverStateBean[] getStateBeans() {
        return statebeans;
    }

    @Override
    public boolean hasNext() {

        return (!threads.isEmpty() || !resultQueue.isEmpty());
    }

    @Override
    public FileBean next() {
        takingThread = Thread.currentThread();
        try {
            return resultQueue.take();
        } catch ( InterruptedException ie ) {
            LOG.error("Interrupted Exception", ie);

            if ( resultQueue.isEmpty() ) {
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

    private String[] extractPathList( SwapFile file ) throws InterruptedException {
        int substrLength = rootFile.getFullPath().length();
if (rootFile.getFullPath().equals("/"))
    substrLength = 0;
        
        // build directory path
        List<String> dirPathList = new ArrayList<String>();
        String currFile = file.getFullPath();
        while ( !currFile.equals(rootFile.getFullPath()) ) {
            LOG.trace("Adding dir to path: " + currFile.substring(substrLength) + " subsr " + substrLength + " root " + rootFile.getFullPath() + " full " + currFile);
            dirPathList.add(currFile.substring(substrLength));
            currFile = currFile.substring(0, currFile.lastIndexOf('/'));
            if (Strings.isEmpty(currFile))
                currFile = "/";
        }
        return dirPathList.toArray(new String[dirPathList.size()]);

    }

    private class FileWorker implements Runnable {

        private int id;
        private DriverStateBean statebean;
        private byte[] buffer = new byte[2097152];
        private MessageDigest digest;

        public FileWorker( DriverStateBean statebean, int id, MessageDigest digest ) {
            this.statebean = statebean;
            this.id = id;
            this.digest = digest;
        }

        @Override
        @SuppressWarnings("empty-statement")
        public void run() {
            NDC.push("[SWAP-" + id + "] ");
            statebean.setRunningThread(Thread.currentThread());

            synchronized ( threads ) {
                threads.add(this);
                threads.notify();
            }
            try {
                while ( !finished ) {
                    loadLoack.lockInterruptibly();
                    SwapFile file = null;
                    try {
                        file = loadNext();
                    }
                    catch (Exception e)
                    {
                        LOG.error("Uncaught listing exception ",e);
                    } finally {
                        loadLoack.unlock();
                    }
                    FileBean fb = null;
                    if ( file != null ) {
                        fb = processFile(file);
                    }
                    if ( fb != null ) {
                        resultQueue.offer(fb);
                    }



                }
            } catch ( InterruptedException e ) {
                LOG.error("Interrupted, exiting ", e);
            } finally {
                LOG.debug(
                        "SWAP Thread finishing: " + Thread.currentThread().
                        getName());

                synchronized ( threads ) {
                    threads.remove(this);
                    if ( threads.isEmpty() ) {
                        if ( resultQueue.isEmpty() && takingThread != null ) {
                            takingThread.interrupt();
                        }
                        LOG.debug("Final SWAP Thread shutdown, closing client");
                        client.close();
                        //rootFile.getFileGroup().
                    }
                }
            }
        }

        private FileBean processFile( SwapFile file ) throws InterruptedException {


            DigestInputStream dis = null;
            FileBean fb = new FileBean();

            LOG.trace("Processing file: " + file);
            statebean.setStateAndReset(State.OPENING_FILE);
            statebean.setRead(0);
            statebean.setTotalSize(file.getSize());
            statebean.setFile(file.getFullPath());


            digest.reset();

            try {
                fb.setPathList(extractPathList(file));
                statebean.setState(State.THROTTLE_WAIT);
                QueryThrottle.waitToRun();
                statebean.setState(State.READING);
                statebean.updateLastChange();

                long fileSize = 0;

                URL outputURL = file.getURL(null);
                HttpURLConnection connection;
                connection = (HttpURLConnection) outputURL.openConnection();
                HttpURLConnection.setFollowRedirects(true);
                connection.setDoInput(true);

                ThrottledInputStream tis =
                        new ThrottledInputStream(connection.getInputStream(), QueryThrottle.getMaxBps(), lastDelay);
                dis = new DigestInputStream(tis, digest);
                int read = 0;
                while ( (read = dis.read(buffer)) != -1) {
                    fileSize += read;
                    statebean.updateLastChange();
                    statebean.setRead(fileSize);
                }
                lastDelay = tis.getSleepTime();

                dis.close();
                byte[] hashValue = digest.digest();
                fb.setHash(HashValue.asHexString(hashValue));
                fb.setFileSize(fileSize);
                connection.disconnect();

            } catch ( IOException ie ) {
                LOG.error("Error reading file: " + file, ie);
                fb.setError(true);
                fb.setErrorMessage(Strings.exceptionAsString(ie));
            } finally {
                IO.release(dis);
                statebean.setStateAndReset(State.IDLE);
                if ( fb.getHash() == null && !fb.isError() ) {
                    fb.setError(true);
                    fb.setErrorMessage("Null digest");
                }
                return fb;

            }
        }

        private SwapFile loadNext() throws InterruptedException {
            statebean.setStateAndReset(State.LISTING);
            // see if wee need to process a directory or if there are files in queue
            while ( filesToProcess.isEmpty() && !dirsToProcess.isEmpty() ) {

                SwapFile directory = dirsToProcess.poll();
                statebean.setFile(directory.getFullPath());
                LOG.trace("Popping directory: " + directory);
                SwapFile[] fileList = directory.listFiles();
                if ( fileList == null ) {
                    LOG.info("Could not read directory, skipping: " + directory);
                } else {
                    for ( SwapFile f : fileList ) {
                        LOG.trace("Testing item " + f);
                        if ( f.isDirectory() && filter.process(
                                extractPathList(f), true) ) {
                            LOG.trace("Adding matching directory: " + f);
                            dirsToProcess.add(f);
                        } else if ( f.isFile() && filter.process(
                                extractPathList(f), false) ) {
                            LOG.trace("Adding matching file: " + f);
                            filesToProcess.add(f);
                        }
                    }
                }
            }

            // now see why we ended loop
            statebean.setStateAndReset(State.IDLE);
            if ( !filesToProcess.isEmpty() ) {
                return filesToProcess.poll();
            } else {
                LOG.trace("No more files left in queue, setting finished");
                finished = true;
                return null;
            }


        }
    }
}
