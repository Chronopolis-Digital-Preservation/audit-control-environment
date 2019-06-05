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
package edu.umiacs.ace.driver.localfile;

import edu.umiacs.ace.driver.AuditIterable;
import edu.umiacs.ace.driver.DriverStateBean;
import edu.umiacs.ace.driver.DriverStateBean.State;
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.QueryThrottle;
import edu.umiacs.ace.driver.StateBeanDigestListener;
import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.ace.util.ThreadedDigestStream;
import edu.umiacs.ace.util.ThrottledInputStream;
import edu.umiacs.util.Strings;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Storage driver for accessing files stored on a local file system (ie, java.io.File)
 *
 * @author toaster
 */
public class LocalFileAccess extends StorageDriver {

    private static final Logger LOG = Logger.getLogger(LocalFileAccess.class);
    private double lastDelay = 0;
    private static final int BLOCK_SIZE = 1048576;

    public LocalFileAccess(Collection c, EntityManager em) {
        super(c);
    }

    @Override
    public void setParameters(Map m) {
// Do Nothing
    }

    @Override
    public String checkParameters(Map m, String path) {
        File f = new File(path);
        if (f.isDirectory()) {
            return null;
        } else if (f.isFile()) {
            return "Directory is a file, not directory";
        } else if (!f.exists()) {
            return "Directory does not exist";
        }
        return "Check directory on server";
    }

    @Override
    public void remove(EntityManager em) {
// Do nothing
    }

    @Override
    public String getPage() {
        return null;
    }

    @Override
    public AuditIterable<FileBean> getWorkList(final String digestAlgorithm,
            final PathFilter filter, final MonitoredItem[] startPathList) {
        return new AuditIterable<FileBean>() {

            private DriverStateBean statebean = new DriverStateBean();
            private MyIterator it;

            @Override
            public Iterator<FileBean> iterator() {
                it = new MyIterator(startPathList, filter, digestAlgorithm, statebean);
                return it;
            }

            @Override
            public void cancel() {
                LOG.debug("Cancel called on localfile iterator");
                // believe it or not this npe has happened...
                if ( it != null ) {
                    it.cancel();
                }
            }

            @Override
            public DriverStateBean[] getState() {
                return new DriverStateBean[]{statebean};
            }
        };

    }

    class MyIterator implements Iterator<FileBean> {

        private FileBean next;
        private Queue<File> dirsToProcess = new LinkedList<>();
        private Queue<File> filesToProcess = new LinkedList<>();
        private MessageDigest digest;
        private File rootFile;
        private PathFilter filter;
        private DriverStateBean statebean;
        private ThreadedDigestStream reader;
        private boolean cancel = false;

        public MyIterator(MonitoredItem[] startPath,
                          PathFilter filter,
                          String digestAlgorithm,
                          DriverStateBean statebean) {
            this.statebean = statebean;
            this.filter = filter;
            try {
                digest = MessageDigest.getInstance(digestAlgorithm);
                reader = new ThreadedDigestStream(digest, BLOCK_SIZE);
                rootFile = new File(getCollection().getDirectory());

                statebean.setRunningThread(Thread.currentThread());

                statebean.setStateAndReset(State.LISTING);
                if (startPath != null) {
                    for (MonitoredItem mi : startPath) {
                        File startFile;
                        startFile = new File(getCollection().getDirectory() + mi.getPath());
                        if (startFile.isDirectory()) {
                            dirsToProcess.add(startFile);

                        } else if (startFile.isFile()) {
                            filesToProcess.add(startFile);

                        }
                    }

                } else {
                    dirsToProcess.add(rootFile);
                }

                loadNext();
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
            statebean.setStateAndReset(State.IDLE);
        }

        public void cancel() {
            this.cancel = true;
            reader.abort();
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = (!cancel && next != null);
            if (!hasNext) {
                reader.shutdown();
            }
            return hasNext;
        }

        @Override
        public FileBean next() {
            FileBean retValue = next;

            loadNext();

            return retValue;
        }

        @Override
        public void remove() {
        }

        private void loadNext() {
            statebean.setStateAndReset(State.LISTING);

            // see if wee need to process a directory or if there are files in queue
            while (filesToProcess.isEmpty() && !dirsToProcess.isEmpty()) {
                File directory = dirsToProcess.poll();
                statebean.setFile(directory.getPath());
                LOG.trace("Popping directory: " + directory);
                File[] fileList = directory.listFiles();
                if (fileList == null) {
                    LOG.info("Could not read directory, skipping: " + directory);
                } else {
                    for (File f : fileList) {
                        LOG.trace("Found item " + f);
                        if ( f.isDirectory() &&
                             filter.process(extractPathList(f), true)) {
                            LOG.trace("Adding matching directory: " + f);
                            dirsToProcess.add(f);
                        } else if ( f.isFile() &&
                                    filter.process(extractPathList(f), false)) {
                            LOG.trace("Adding matching file: " + f);
                            filesToProcess.add(f);
                        }
                    }
                }
            }

            // now see why we ended loop
            // we have files
            if (!filesToProcess.isEmpty() && !cancel) {
                next = processFile(filesToProcess.poll());
            } else {
                next = null;
            }
            statebean.setStateAndReset(State.IDLE);

        }

        private String[] extractPathList(File file) {
            int substrLength = rootFile.getPath().length();

            // build directory path
            List<String> dirPathList = new ArrayList<>();
            File currFile = file;
            while (!currFile.equals(rootFile)) {
                String pathToAdd = currFile.getPath().substring(substrLength);
                pathToAdd = pathToAdd.replace(File.separatorChar, '/');
                dirPathList.add(pathToAdd);
                currFile = currFile.getParentFile();
            }
            return dirPathList.toArray(new String[dirPathList.size()]);
        }

        @SuppressWarnings("empty-statement")
        private FileBean processFile(File file) {
            FileBean fb = new FileBean();
            fb.setPathList(extractPathList(file));

            LOG.trace("Processing file: " + file);
            statebean.setStateAndReset(State.OPENING_FILE);
            statebean.setRead(0);
            statebean.setTotalSize(file.length());
            statebean.setFile(file.getPath());

            digest.reset();

            try {
                statebean.setState(State.THROTTLE_WAIT);
                QueryThrottle.waitToRun();
                statebean.setState(State.READING);
                statebean.updateLastChange();

                ThrottledInputStream tis =
                        new ThrottledInputStream(new FileInputStream(file), QueryThrottle.getMaxBps(), lastDelay);

                reader.setListener(new StateBeanDigestListener(statebean));
                long fileSize = reader.readStream(tis);
                lastDelay = tis.getSleepTime();

                byte[] hashValue = digest.digest();
                tis.close();
                fb.setHash(HashValue.asHexString(hashValue));
                fb.setFileSize(fileSize);

            } catch (IOException ie) {
                LOG.error("Error reading file: " + file, ie);
                fb.setError(true);
                fb.setErrorMessage(Strings.exceptionAsString(ie));
            } finally {
                statebean.setStateAndReset(State.IDLE);
                if (cancel) {
                    return null;
                } else {
                    return fb;
                }
            }
        }
    }

    @Override
    public InputStream getItemInputStream(String itemPath) throws IOException {

        File f = new File(getCollection().getDirectory() + itemPath);
        return new FileInputStream(f);
    }
}
