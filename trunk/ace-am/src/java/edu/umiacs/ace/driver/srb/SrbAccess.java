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
// $Id: SrbAccess.java 3181 2010-06-16 20:46:16Z toaster $

package edu.umiacs.ace.driver.srb;

import edu.sdsc.grid.io.srb.SRBAccount;
import edu.sdsc.grid.io.srb.SRBFile;
import edu.sdsc.grid.io.srb.SRBFileInputStream;
import edu.sdsc.grid.io.srb.SRBFileSystem;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.monitor.audit.AuditIterable;
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.srb.connection.ConnectionPool;
import edu.umiacs.util.Strings;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public class SrbAccess extends StorageDriver {

    private static final Logger LOG = Logger.getLogger(SrbAccess.class);
    private static final String PARAM_SERVER = "server";
    private static final String PARAM_ZONE = "zone";
    private static final String PARAM_USER = "username";
    private static final String PARAM_PASS = "password";
    private static final String PARAM_DOMAIN = "domain";
    private static final String PARAM_PORT = "port";
    private EntityManager em;
    private SrbSettings settings;

    public SrbAccess( Collection c, EntityManager em ) {

        super(c);
        try {
            this.em = em;
            Query q = em.createNamedQuery("SrbSettings.getByCollection");

            q.setParameter("coll", c);
            try {
                settings = (SrbSettings) q.getSingleResult();
                LOG.debug("Loaded existing srbSettings");
            } catch ( NoResultException e ) {
                settings = new SrbSettings();
                settings.setZone("tempZone");
                settings.setPassword("");
                settings.setUsername("");
                settings.setPort(5544);
                settings.setServer("localhost");
                settings.setCollection(c);
                settings.setDomain("");
                EntityTransaction et = em.getTransaction();
                et.begin();
                em.persist(settings);
                et.commit();
                LOG.trace("Persisted new SrbSetting");
            }
        } catch ( Exception e ) {
            LOG.error("Error loading settings: " + e);
            throw new RuntimeException(e);
        }

    }

    public SrbSettings getSettings() {
        return settings;
    }

    @Override
    public void setParameters( Map m ) {
        boolean newTx;
        LOG.trace("Setting srb parameters");

        settings.setServer(getSingleParam(m, PARAM_SERVER));
        settings.setPort(Integer.parseInt(getSingleParam(m, PARAM_PORT)));
        settings.setUsername(getSingleParam(m, PARAM_USER));
        settings.setPassword(getSingleParam(m, PARAM_PASS));
        settings.setZone(getSingleParam(m, PARAM_ZONE));
        settings.setDomain(getSingleParam(m, PARAM_DOMAIN));
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
        String server, username, password, zone, domain;
        int port;

        server = getSingleParam(m, PARAM_SERVER);
        username = getSingleParam(m, PARAM_USER);
        password = getSingleParam(m, PARAM_PASS);
        zone = getSingleParam(m, PARAM_ZONE);
        domain = getSingleParam(m, PARAM_DOMAIN);

        LOG.trace("SRB checkParameters");
        good = (!Strings.isEmpty(server) && !Strings.isEmpty(zone) && Strings.isValidInt(getSingleParam(m,
                PARAM_PORT)) && !Strings.isEmpty(username) && !Strings.isEmpty(domain)
                && !Strings.isEmpty(password));
        if ( !good ) {
            return "Please check all SRB parameters, they have not been completely filled out";
        }
        port = Integer.parseInt(getSingleParam(m, PARAM_PORT));

        LOG.debug("srb checkParameters: all params filled: " + good + " svr: " + server + " user: "
                + username + " zone: " + zone);

        SRBAccount account = new SRBAccount(server, port, username,
                password, "/", domain, "none", zone);

        ConnectionPool pool = null;
        SRBFileSystem sfs = null;
        try {

            SRBFile sFile;
            LOG.trace("Attempting to connect to srb");
            pool = new ConnectionPool(account);


            sfs = pool.getConnection();
            sFile = new SRBFile(sfs, path);

            if ( !sFile.exists() ) {
                return "Directory does not exist in the SRB";
            } else if ( !sFile.isDirectory() ) {
                return "Directory exists, but is not a directory in the SRB";
            }
            pool.freeConnection(sfs);
            return null;

        } catch ( Throwable ioe ) {
            if ( pool != null && sfs != null ) {
                pool.brokenConnection(sfs);
            }
            LOG.info("Exception thrown attempting to validate settings", ioe);
            return "Error connecting to the SRB: " + ioe.getMessage();
        } finally {
            if ( pool != null ) {
                pool.poolShutdown();
            }
        }


    }

    @Override
    public void remove( EntityManager em ) {
        LOG.trace("Removing srb parameters");
        em.remove(settings);
    }

    @Override
    public String getPage() {
        return "srb.jsp";
    }

    @Override
    public AuditIterable<FileBean> getWorkList( final String digestAlgorithm,
            final PathFilter filter, final MonitoredItem[] startPathList ) {

        final SrbDirectoryIterator srbIterator = new SrbDirectoryIterator(getCollection(), settings,
                startPathList, filter, digestAlgorithm);

        return new AuditIterable<FileBean>() {

            @Override
            public Iterator<FileBean> iterator() {
                return srbIterator;
            }

            @Override
            public void cancel() {
                if ( srbIterator != null ) {
                    srbIterator.cancel(true);
                }
            }
        };

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
    public InputStream getItemInputStream( String itemPath ) throws IOException {
        ConnectionPool cp = settings.createPool();
        String fullPath = getCollection().getDirectory() + itemPath;
        SRBFileSystem sfs = cp.getConnection();
        SRBFileInputStream sfis = new SRBFileInputStream(sfs, fullPath);
        return new WrappedInputStream(sfis, sfs, cp);
    }

    class WrappedInputStream extends FilterInputStream {

        private ConnectionPool cp;
        private SRBFileSystem sfs;

        public WrappedInputStream( SRBFileInputStream sis, SRBFileSystem sfs, ConnectionPool cp ) {
            super(sis);
            this.cp = cp;
            this.sfs = sfs;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                cp.freeConnection(sfs);
                cp.poolShutdown();
            }
        }
    }
}
