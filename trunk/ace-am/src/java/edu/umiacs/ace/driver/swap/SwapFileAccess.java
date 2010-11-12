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

import edu.umiacs.ace.driver.AuditIterable;
import edu.umiacs.ace.driver.DriverStateBean;
import edu.umiacs.ace.driver.DriverStateBean.State;
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.driver.QueryThrottle;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.util.ThrottledInputStream;
import edu.umiacs.io.IO;
import edu.umiacs.srb.util.StringUtil;
import edu.umiacs.util.Strings;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import org.swap.client.ConnectionListener;
import org.swap.client.ConnectionListener.Operation;
import org.swap.client.FileGroup;
import org.swap.client.SwapClient;
import org.swap.client.SwapFile;
import org.swap.node.communication.security.StoredCredCallback;

/**
 * Storage driver for accessing files stored in a swap storage entironemnt 
 * 
 * @author toaster
 */
public class SwapFileAccess extends StorageDriver {

    private static final String PARAM_SERVERS = "servers";
    private static final String PARAM_USER = "username";
    private static final String PARAM_PASS = "password";
    private static final String PARAM_PREFIX = "prefix";
    private static final String PARAM_PORT = "port";
    private static final Logger LOG = Logger.getLogger(SwapFileAccess.class);
    private boolean cancel = false;
    private double lastDelay = 0;
    private SwapSettings settings;
    private EntityManager em;

    public SwapFileAccess( Collection c, EntityManager em ) {
        super(c);
        try {
            this.em = em;
            Query q = em.createNamedQuery("SwapSettings.getByCollection");

            q.setParameter("coll", c);
            try {
                settings = (SwapSettings) q.getSingleResult();
                LOG.debug("Loaded existing SWAP Settings");
            } catch ( NoResultException e ) {
                settings = new SwapSettings();
                settings.setPassword("");
                settings.setUsername("");
                settings.setPort(9123);
                settings.setServers("localhost");
                settings.setCollection(c);
                settings.setPrefix("");
                EntityTransaction et = em.getTransaction();
                et.begin();
                em.persist(settings);
                et.commit();
                LOG.trace("Persisted new SWAP Setting");
            }
        } catch ( Exception e ) {
            LOG.error("Error loading settings: " + e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setParameters( Map m ) {
        boolean newTx;
        settings.setServers(getSingleParam(m, PARAM_SERVERS));
        settings.setUsername(getSingleParam(m, PARAM_USER));
        settings.setPassword(getSingleParam(m, PARAM_PASS));
        settings.setPrefix(getSingleParam(m, PARAM_PREFIX));
        settings.setPort(Integer.parseInt(getSingleParam(m, PARAM_PORT)));

        EntityTransaction et = em.getTransaction();
        newTx = et.isActive();
        if ( !newTx ) {
            et.begin();
        }
        PersistUtil.getEntityManager().merge(settings);
        if ( !newTx ) {
            et.commit();
        }
    }

    @Override
    public String checkParameters( Map m, String path ) {

        boolean good = false;
        String servers, username, password, prefix;
        int port;

        servers = getSingleParam(m, PARAM_SERVERS);
        username = getSingleParam(m, PARAM_USER);
        password = getSingleParam(m, PARAM_PASS);
        prefix = getSingleParam(m, PARAM_PREFIX);

        LOG.trace("SWAP checkParameters");
        good = (!Strings.isEmpty(servers) && Strings.isValidInt(getSingleParam(m,
                PARAM_PORT)) && !Strings.isEmpty(username) && !Strings.isEmpty(password));
        if ( !good ) {
            return "Please check all SWAP parameters, they have not been completely filled out";
        }
        port = Integer.parseInt(getSingleParam(m, PARAM_PORT));

        LOG.debug("swap checkParameters: all params filled: " + good + " svr: " + servers
                + " user: "
                + username);

        for ( String svr : servers.split(",") ) {
            if ( !svr.matches("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$") ) {
                return "Bad server name: " + svr;
            }
        }
        SwapClient client = new SwapClient(new StoredCredCallback(username, password));
        MyConnectListener mcl = new MyConnectListener();
        client.addConnectionListener(mcl);

        try {
            for ( String svr : servers.split(",") ) {
                client.addNode(new InetSocketAddress(svr, port), 2000);
            }

            if ( mcl.getResult() != Operation.LOGIN_SUCCESS ) {
                client.close();
                return "Authentication Failed";

            }

            Thread.sleep(2000);

            FileGroup group = groupByName(client, path);
            if ( group == null ) {
                return "File group " + path + " cannot be found";
            }

            if ( !Strings.isEmpty(prefix) ) {
                SwapFile sf = group.getFileDetails(prefix);
                if ( sf == null || !sf.isDirectory() ) {
                    return "Prefix cannot be found in group";
                }
            }
        } catch ( InterruptedException e ) {

            return "Interrupted trying to check settings";
        } finally {
            client.close();
        }
        return null;
    }

    private class MyConnectListener implements ConnectionListener {

        Operation result = null;

        @Override
        public synchronized void connectionEvent( InetSocketAddress isa, Operation oprtn ) {
            if ( result == null && oprtn != Operation.DISCONNECT ) {
                result = oprtn;
                notifyAll();
            }

        }
        // block till result

        public synchronized Operation getResult() throws InterruptedException {
            while ( result == null ) {
                wait();
            }
            return result;
        }
    }

    private String getSingleParam( Map m, String paramName ) {
        Object o = m.get(paramName);

        if ( o == null ) {
            return null;
        }

        String[] objString = (String[]) o;
        return objString[0];
    }

    @Override
    public void remove( EntityManager em ) {
        LOG.trace("Removing srb parameters");
        em.remove(settings);
    }

    @Override
    public String getPage() {
        return "swap.jsp";
    }

    public SwapSettings getSettings() {
        return settings;
    }

    private FileGroup groupByName( SwapClient client, String name ) {

        try {

            UUID id = UUID.fromString(name);
            LOG.trace("Path parses to uuid, testing " + id);
            return client.getFileGroupList().get(id);
        } catch ( IllegalArgumentException e ) {
            LOG.trace("Path does not parse to uuis, checking namespaces " + name);
            for ( FileGroup fg : client.getFileGroupList() ) {
                System.out.println("testing " + fg.getCombinedNameSpace());
                if ( fg.getCombinedNameSpace().equals(name) ) {
                    return fg;
                }
            }
        }
        return null;
    }

    @Override
    public AuditIterable<FileBean> getWorkList( final String digestAlgorithm,
            final PathFilter filter, final MonitoredItem[] startPathList ) {
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
                cancel = true;
            }

            @Override
            public DriverStateBean[] getState() {
                return new DriverStateBean[]{statebean};
            }
        };

    }

    private SwapClient createClient() {

        SwapClient client = new SwapClient(new StoredCredCallback(
                settings.getUsername(), settings.getPassword()));

        for ( String server : settings.getServers().split(",") ) {
            client.addNode(new InetSocketAddress(server, settings.getPort()), 2000);
        }

        return client;
    }

    class MyIterator implements Iterator<FileBean> {

        private FileBean next;
        private Queue<SwapFile> dirsToProcess = new LinkedList<SwapFile>();
        private Queue<SwapFile> filesToProcess = new LinkedList<SwapFile>();
        private MessageDigest digest;
        private byte[] buffer = new byte[32768];
        private SwapFile rootFile;
        private PathFilter filter;
        private DriverStateBean statebean;
        private SwapClient client;

        public MyIterator( MonitoredItem[] startPath, PathFilter filter,
                String digestAlgorithm, DriverStateBean statebean ) {
            this.statebean = statebean;
            this.filter = filter;
            try {
                client = createClient();
                Thread.sleep(2000);
                digest = MessageDigest.getInstance(digestAlgorithm);
                FileGroup group = groupByName(client, getCollection().getDirectory());
                
                if ( Strings.isEmpty(settings.getPrefix()) ) {
                    rootFile = group.getFileDetails("");
                } else {
                    rootFile = group.getFileDetails(settings.getPrefix());
                }

                if (rootFile == null)
                    throw new NullPointerException("Root file is null!");

                statebean.setRunningThread(Thread.currentThread());

                statebean.setStateAndReset(State.LISTING);
                if ( startPath != null ) {
                    for ( MonitoredItem mi : startPath ) {
                        SwapFile startFile;
                        String subpath = settings.getPrefix() + mi.getPath();

                        startFile = group.getFileDetails(subpath);
                        if ( startFile.isDirectory() ) {
                            dirsToProcess.add(startFile);

                        } else if ( startFile.isFile() ) {
                            filesToProcess.add(startFile);

                        }
                    }

                } else {
                    dirsToProcess.add(rootFile);
                }

                loadNext();
            } catch ( InterruptedException ex ) {
                throw new RuntimeException(ex);

            } catch ( NoSuchAlgorithmException ex ) {
                throw new RuntimeException(ex);
            }
            statebean.setStateAndReset(State.IDLE);
        }

        @Override
        public boolean hasNext() {

            return !cancel && next != null;
        }

        @Override
        public FileBean next() {
            FileBean retValue = next;

            try {
                loadNext();
            } catch ( InterruptedException e ) {
                throw new RuntimeException(e);
            }

            return retValue;
        }

        @Override
        public void remove() {
        }

        private void loadNext() throws InterruptedException {
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
            statebean.setStateAndReset(State.IDLE);

        }

        private String[] extractPathList( SwapFile file ) throws InterruptedException {
            int substrLength = rootFile.getFullPath().length();

            // build directory path
            List<String> dirPathList = new ArrayList<String>();
            SwapFile currFile = file;
            while ( currFile != null && !currFile.equals(rootFile) ) {
//                LOG.trace("Adding dir to path: " + currFile.getPath().substring(
//                        substrLength));
                String pathToAdd = currFile.getFullPath().substring(substrLength);
                pathToAdd = pathToAdd.replace(File.separatorChar, '/');
                dirPathList.add(pathToAdd);
                currFile = currFile.getParentFile();
            }
            return dirPathList.toArray(new String[dirPathList.size()]);
        }

        @SuppressWarnings("empty-statement")
        private FileBean processFile( SwapFile file ) throws InterruptedException {


            DigestInputStream dis = null;
            FileBean fb = new FileBean();

            fb.setPathList(extractPathList(file));

            LOG.trace("Processing file: " + file);
            statebean.setStateAndReset(State.OPENING_FILE);
            statebean.setRead(0);
            statebean.setTotalSize(file.getSize());
            statebean.setFile(file.getFullPath());

            digest.reset();

            try {
                statebean.setState(State.THROTTLE_WAIT);
                QueryThrottle.waitToRun();
                statebean.setState(State.READING);
                statebean.updateLastChange();

                long fileSize = 0;

                URL outputURL = file.getURL(null);
                HttpURLConnection connection;
                connection = (HttpURLConnection) outputURL.openConnection();
//                connection.setChunkedStreamingMode(buffer.length);
                HttpURLConnection.setFollowRedirects(true);
//                connection.setRequestMethod("GET");
                connection.setDoInput(true);
//                connection.setDoOutput(true);


                ThrottledInputStream tis =
                        new ThrottledInputStream(connection.getInputStream(), QueryThrottle.getMaxBps(), lastDelay);
                dis = new DigestInputStream(tis, digest);
                int read = 0;
                while ( (read = dis.read(buffer)) >= 0 && !cancel ) {
                    fileSize += read;
                    statebean.updateLastChange();
                    statebean.setRead(fileSize);
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
                statebean.setStateAndReset(State.IDLE);
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
