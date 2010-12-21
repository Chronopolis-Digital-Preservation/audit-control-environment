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
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.util.Strings;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
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

    static FileGroup groupByName( SwapClient client, String name ) {

        try {

            UUID id = UUID.fromString(name);
            LOG.trace("Path parses to uuid, testing " + id);
            return client.getFileGroupList().get(id);
        } catch ( IllegalArgumentException e ) {
            LOG.trace("Path does not parse to a uuid, checking namespaces for '" + name + "'");
            for ( FileGroup fg : client.getFileGroupList() ) {
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

            private SwapIterator it;

            @Override
            public Iterator<FileBean> iterator() {
                SwapFile rootFile;
                SwapClient client = createClient();
                try {
                    Thread.sleep(2000);
                    FileGroup group = groupByName(client, settings.getCollection().getDirectory());
                    if ( group == null ) {
                        LOG.error("Could not extract swap filegroup '"
                                + settings.getCollection().getDirectory() + "' ");

                        throw new RuntimeException("Could not find swap file group");
                    }

                    rootFile = group.getFileDetails(settings.getPrefix());
                } catch ( InterruptedException e ) {
                    LOG.error("Could not lookup swap file '" + settings.getPrefix() + "' ");

                    throw new RuntimeException("Could not open " + settings.getPrefix());
                }

                if ( rootFile == null ) {
                    LOG.error("Could not lookup swap file '" + settings.getPrefix() + "' ");
                    throw new RuntimeException("Could not open " + settings.getPrefix());
                }

                it =
                        new SwapIterator(startPathList, filter, digestAlgorithm, settings.getCollection().getName(), rootFile, client);
//                it = new MyIterator(startPathList, filter, digestAlgorithm, statebean);
                return it;
            }

            @Override
            public void cancel() {
                LOG.debug("Cancel called on swapfile iterator");
                it.cancel();
            }

            @Override
            public DriverStateBean[] getState() {
                return it.getStateBeans();
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

    @Override
    public InputStream getItemInputStream( String itemPath ) throws IOException {

        SwapClient client = createClient();
        FileGroup group;
        SwapFile file;
        
        try {
            Thread.sleep(2000);
            group = groupByName(client, settings.getCollection().getDirectory());
            if ( group == null ) {
                throw new IOException("Could not open swap file group");
            }
        } catch ( InterruptedException e ) {
            throw new IOException("Interrupted opening filegroup");
        }

        try {
            file = group.getFileDetails(settings.getPrefix() + itemPath);
        } catch ( InterruptedException e ) {
            throw new IOException("Could not open " + itemPath);
        }
        if ( file == null ) {
            throw new FileNotFoundException(itemPath);
        }
        URL outputURL = file.getURL(null);
        HttpURLConnection connection;
        connection = (HttpURLConnection) outputURL.openConnection();
        HttpURLConnection.setFollowRedirects(true);
        connection.setDoInput(true);
        return connection.getInputStream();

    }
}
