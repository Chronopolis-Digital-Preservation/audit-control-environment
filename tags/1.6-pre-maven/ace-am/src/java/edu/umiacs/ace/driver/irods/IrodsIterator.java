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
package edu.umiacs.ace.driver.irods;

import edu.umiacs.ace.driver.DriverStateBean;
import edu.umiacs.ace.driver.DriverStateBean.State;
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.irods.operation.BulkFileSaver;
import edu.umiacs.irods.operation.BulkTransferListener;
import edu.umiacs.irods.operation.ConnectOperation;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public class IrodsIterator implements Iterator<FileBean> {

    private DriverStateBean stateBean;
    private IrodsHandler handler = null;
    private LinkedBlockingQueue<FileBean> readyList = new LinkedBlockingQueue<FileBean>();
    private Thread takingThread;
    private static final Logger LOG = Logger.getLogger(IrodsIterator.class);
    private ConnectOperation co;
    private String root;
    private BulkFileSaver bfs;
    private Queue<BulkFileSaver> saverList = new LinkedList<BulkFileSaver>();

    public IrodsIterator( Collection c, IrodsSetting ic,
            MonitoredItem[] startPathList, PathFilter filter,
            String digestAlgorithm ) {
        this.co = new ConnectOperation(ic.getServer(), ic.getPort(),
                ic.getUsername(), ic.getPassword(), ic.getZone());
        this.stateBean = new DriverStateBean();
        try {

            co.getConnection().closeConnection();
            this.root = c.getDirectory();

            handler = new IrodsHandler(readyList, filter, digestAlgorithm);

            MyListener listener = new MyListener();
            if ( startPathList != null ) {
                String startPath;

                for ( MonitoredItem mi : startPathList ) {
                    startPath = root + mi.getPath();
                    bfs = new BulkFileSaver(co, handler, startPath);
                    bfs.addListener(listener);
                    saverList.add(bfs);
                }
            } else {
                bfs = new BulkFileSaver(co, handler, root);
                bfs.addListener(listener);
                saverList.add(bfs);
            }

            saverList.poll().execute(true);

        } catch ( IOException ioe ) {
            LOG.error("Could not connect to irods", ioe);
            throw new RuntimeException("Could not connect to irods", ioe);
        }
    }

    public void cancel() {
        bfs.cancel();
    }

    public DriverStateBean getStateBean() {
        return stateBean;
    }

    @Override
    public boolean hasNext() {
        return !readyList.isEmpty() || handler != null;
    }

    @Override
    public FileBean next() {
        takingThread = Thread.currentThread();
        try {
            return readyList.take();
        } catch ( InterruptedException ie ) {


            if ( readyList.isEmpty() ) {
                LOG.error("Interrupted Exception in next, return null");
                return null;
            }
            LOG.error("Interrupted Exception in next, readylist not empty, tossing runtime", ie);
            throw new RuntimeException(ie);
        } finally {
            takingThread = null;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not gonna happen!");
    }

    private class MyListener implements BulkTransferListener {

        private long bytes;

        @Override
        public void startTransfer() {
            stateBean.setStateAndReset(State.LISTING);
            stateBean.setRunningThread(Thread.currentThread());
        }

        @Override
        public void startFile( String fullPath ) {
            bytes = 0;
            stateBean.setStateAndReset(State.READING);
            stateBean.setFile(fullPath);
        }

        @Override
        public void bytesWritten( int bytesWritten ) {

            bytes += bytesWritten;
            stateBean.setRead(bytes);
            stateBean.updateLastChange();
        }

        @Override
        public void endFile( String fullPath ) {
            stateBean.setStateAndReset(State.LISTING);

        }

        @Override
        public void endTransfer() {
            stateBean.setRunningThread(null);
            LOG.debug("endTransfer called on irods iterator");
            if ( !saverList.isEmpty() ) {
                saverList.poll().execute(true);
                return;
            } else {
                handler = null;
                if ( takingThread != null && readyList.isEmpty() ) {
                    takingThread.interrupt();
                }
            }
        }

        @Override
        public void handleException( Throwable t ) {
            cancel();
            handler = null;
            if ( takingThread != null && readyList.isEmpty() ) {
                takingThread.interrupt();
            }
            LOG.error("Error in bulk saver", t);
        }
    }
}
