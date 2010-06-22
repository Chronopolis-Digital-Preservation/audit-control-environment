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
// $Id: LocalFileAccess.java 3181 2010-06-16 20:46:16Z toaster $

package edu.umiacs.ace.driver.localfile;

import edu.umiacs.ace.monitor.audit.AuditIterable;
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.driver.QueryThrottle;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.ace.util.ThrottledInputStream;
import edu.umiacs.io.IO;
import edu.umiacs.util.Strings;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;

/**
 * Storage driver for accessing files stored on a local file system (ie, java.io.File)
 * 
 * @author toaster
 */
public class LocalFileAccess extends StorageDriver {

    private static final Logger LOG = Logger.getLogger(LocalFileAccess.class);
    private boolean cancel = false;
    private double lastDelay = 0;

    public LocalFileAccess( Collection c, EntityManager em ) {
        super(c);
    }

    @Override
    public void setParameters( Map m ) {
// Do Nothing
    }

    @Override
    public String checkParameters( Map m, String path ) {
        File f = new File(path);
        if ( f.isDirectory() ) {
            return null;
        } else if ( f.isFile() ) {
            return "Directory is a file, not directory";
        } else if ( !f.exists() ) {
            return "Directory does not exist";
        }
        return "Check directory on server";
    }

    @Override
    public void remove( EntityManager em ) {
// Do nothing
    }

    @Override
    public String getPage() {
        return null;
    }

    @Override
    public AuditIterable<FileBean> getWorkList( final String digestAlgorithm,
            final PathFilter filter, final MonitoredItem[] startPathList ) {
        return new AuditIterable<FileBean>() {

            @Override
            public Iterator<FileBean> iterator() {
                return new MyIterator(startPathList, filter, digestAlgorithm);
            }

            @Override
            public void cancel() {
                cancel = true;
            }
        };

    }

    class MyIterator implements Iterator<FileBean> {

        private FileBean next;
        private Queue<File> dirsToProcess = new LinkedList<File>();
        private Queue<File> filesToProcess = new LinkedList<File>();
        private MessageDigest digest;
        private byte[] buffer = new byte[4096];
        private File rootFile;
        private PathFilter filter;

        public MyIterator( MonitoredItem[] startPath, PathFilter filter,
                String digestAlgorithm ) {
            this.filter = filter;
            try {
                digest = MessageDigest.getInstance(digestAlgorithm);
                rootFile = new File(getCollection().getDirectory());


                if ( startPath != null ) {
                    for ( MonitoredItem mi : startPath ) {
                        File startFile;
                        startFile = new File(
                                getCollection().getDirectory() + startPath[0].getPath());
                        if ( startFile.isDirectory() ) {
                            dirsToProcess.add(startFile);

                        } else if ( startFile.isFile() ) {
                            filesToProcess.add(startFile);

                        }
                    }
//                    if (startPath.length == 1 & startPath[0].isDirectory())
//                    {
//                        startFile = new File(
//                            getCollection().getDirectory() + startPath[0].getPath());
//                        dirsToProcess.add(startFile);
//                    }
//                    else
//                    {
//                        for ()
//                    }

                } else {
                    dirsToProcess.add(rootFile);
//                    startFile = rootFile;
                }

//                if ( startFile.isDirectory() )
//                {
//                    dirsToProcess.add(startFile);
//                }
//                else if ( startFile.isFile() )
//                {
//                    filesToProcess.add(startFile);
//                }
                loadNext();
            } catch ( NoSuchAlgorithmException ex ) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean hasNext() {

            return !cancel && next != null;
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
            // see if wee need to process a directory or if there are files in queue
            while ( filesToProcess.isEmpty() && !dirsToProcess.isEmpty() ) {
                File directory = dirsToProcess.poll();
                LOG.trace("Popping directory: " + directory);
                File[] fileList = directory.listFiles();
                if ( fileList == null ) {
                    LOG.info("Could not read directory, skipping: " + directory);
                } else {
                    for ( File f : directory.listFiles() ) {
                        LOG.trace("Found item " + f);
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
            // we have files
            if ( !filesToProcess.isEmpty() && !cancel ) {
                next = processFile(filesToProcess.poll());
            } else {
                next = null;
            }

        }

        private String[] extractPathList( File file ) {
            int substrLength = rootFile.getPath().length();

            // build directory path
            List<String> dirPathList = new ArrayList<String>();
            File currFile = file;
            while ( !currFile.equals(rootFile) ) {
//                LOG.trace("Adding dir to path: " + currFile.getPath().substring(
//                        substrLength));
                String pathToAdd = currFile.getPath().substring(substrLength);
                pathToAdd = pathToAdd.replace(File.separatorChar, '/');
                dirPathList.add(pathToAdd);
                currFile = currFile.getParentFile();
            }
            return dirPathList.toArray(new String[dirPathList.size()]);
        }

        @SuppressWarnings("empty-statement")
        private FileBean processFile( File file ) {
            DigestInputStream dis = null;
            FileBean fb = new FileBean();

            fb.setPathList(extractPathList(file));

            LOG.trace("Processing file: " + file);

            digest.reset();

            try {
                QueryThrottle.waitToRun();
                long fileSize = 0;
                ThrottledInputStream tis =
                        new ThrottledInputStream(new FileInputStream(file), QueryThrottle.getMaxBps(), lastDelay);
                dis = new DigestInputStream(tis, digest);
                int read = 0;
                while ( (read = dis.read(buffer)) >= 0 && !cancel ) {
                    fileSize += read;
                }
                lastDelay = tis.getSleepTime();

                byte[] hashValue = digest.digest();
                dis.close();
                fb.setHash(HashValue.asHexString(hashValue));
                fb.setFileSize(fileSize);

            } catch ( IOException ie ) {
                LOG.error("Error reading file: " + file, ie);
                fb.setError(true);
                fb.setErrorMessage(Strings.exceptionAsString(ie));
            } finally {
                IO.release(dis);
                if ( cancel ) {
                    return null;
                } else {
                    return fb;
                }
            }
        }
    }

    @Override
    public InputStream getItemInputStream( String itemPath ) throws IOException {
        File f = new File(getCollection().getDirectory() + itemPath);
        return new FileInputStream(f);
    }
}
