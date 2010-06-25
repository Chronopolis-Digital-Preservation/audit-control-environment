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

import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.monitor.audit.AuditIterable;
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.irods.api.IRodsConnection;
import edu.umiacs.irods.api.pi.ObjTypeEnum;
import edu.umiacs.irods.api.pi.RodsObjStat_PI;
import edu.umiacs.irods.operation.ConnectOperation;
import edu.umiacs.irods.operation.IrodsInputStream;
import edu.umiacs.irods.operation.IrodsOperations;
import edu.umiacs.util.Strings;
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
 * irods storage driver
 * @author toaster
 */
public class IrodsAccess extends StorageDriver {

    private static final String PARAM_SERVER = "server";
    private static final String PARAM_ZONE = "zone";
    private static final String PARAM_USER = "username";
    private static final String PARAM_PASS = "password";
    private static final String PARAM_PORT = "port";
    private static final Logger LOG = Logger.getLogger(IrodsAccess.class);
    private IrodsSetting ic;
    private EntityManager em;

    public IrodsAccess( Collection c, EntityManager em ) {
        super(c);
        try {
            this.em = em;
            Query q = em.createNamedQuery("IrodsSettings.getByCollection");

            q.setParameter("coll", c);
            try {
                ic = (IrodsSetting) q.getSingleResult();
                LOG.trace("Loaded existing irodsSetting");
            } catch ( NoResultException e ) {
                ic = new IrodsSetting();
                ic.setZone("tempZone");
                ic.setPassword("rods");
                ic.setUsername("rods");
                ic.setPort(1247);
                ic.setServer("localhost");
                ic.setCollection(c);
                EntityTransaction et = em.getTransaction();
                et.begin();
                em.persist(ic);
                et.commit();
                LOG.trace("Persisted new irodsSetting");
            }
        } catch ( Exception e ) {
            LOG.error("Error loading settings: " + e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public AuditIterable<FileBean> getWorkList( final String digestAlgorithm,
            final PathFilter filter, final MonitoredItem[] startPathList ) {
        final IrodsIterator it = new IrodsIterator(getCollection(),
                ic, startPathList, filter, digestAlgorithm);

        return new AuditIterable<FileBean>() {

            @Override
            public Iterator<FileBean> iterator() {
                return it;
            }

            @Override
            public void cancel() {
                it.cancel();
            }
        };

    }

    public IrodsSetting getSettings() {
        return ic;
    }

    @Override
    public void setParameters( Map m ) {
        boolean newTx;
        LOG.trace("Setting irods parameters");

        ic.setServer(getSingleParam(m, PARAM_SERVER));
        ic.setPort(Integer.parseInt(getSingleParam(m, PARAM_PORT)));
        ic.setUsername(getSingleParam(m, PARAM_USER));
        ic.setPassword(getSingleParam(m, PARAM_PASS));
        ic.setZone(getSingleParam(m, PARAM_ZONE));
        EntityTransaction et = em.getTransaction();
        newTx = et.isActive();
        if ( !newTx ) {
            et.begin();
        }
        PersistUtil.getEntityManager().merge(ic);
        if ( !newTx ) {
            et.commit();
        }
    }

    @Override
    public String checkParameters( Map m, String path ) {
        boolean good = false;
        String server, username, password, zone;
        int port;

        server = getSingleParam(m, PARAM_SERVER);//String) m.get(PARAM_SERVER);
        username = getSingleParam(m, PARAM_USER);
        password = getSingleParam(m, PARAM_PASS);
        zone = getSingleParam(m, PARAM_ZONE);

        LOG.trace("IRODS checkParameters");
        good = (!Strings.isEmpty(server) && !Strings.isEmpty(zone) && Strings.isValidInt(getSingleParam(m,
                PARAM_PORT)) && !Strings.isEmpty(username) && !Strings.isEmpty(password));
        if ( !good ) {
            return "Settings empty, check settings";
        }
        port = Integer.parseInt(getSingleParam(m, PARAM_PORT));

        LOG.debug("IRODS checkParameters: all params filled: " + good + " svr: " + server
                + " user: " + username + " zone: " + zone);

        ConnectOperation co = new ConnectOperation(server, port, username,
                password, zone);
        try {
            LOG.trace("Attempting to connect to irods");

            IRodsConnection connect = co.getConnection();

            IrodsOperations ops = new IrodsOperations(co);
            RodsObjStat_PI stat = ops.stat(path);
            if ( stat == null || stat.getObjType() != ObjTypeEnum.COLL_OBJ_T ) {
                return "Directory listed does not exist in IRODS";
            }

            connect.closeConnection();

            return null;
        } catch ( IOException ioe ) {
            LOG.info("Exception thrown attempting to validate settings", ioe);
            return "Error connecting to irods: " + ioe.getMessage();

        } finally {
            co.shutdown();
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
        LOG.trace("Removing irods parameters");
        em.remove(ic);
    }

    @Override
    public String getPage() {
        return "irods.jsp";
    }

    @Override
    public InputStream getItemInputStream( String itemPath ) throws IOException {
        //TODO: iRODS getItemInputStream
//        throw new UnsupportedOperationException("Not supported yet.");
        ConnectOperation co = new ConnectOperation(ic.getServer(), ic.getPort(),
                ic.getUsername(), ic.getPassword(), ic.getZone());
        String fullPath = getCollection().getDirectory() + itemPath;
        return new IrodsInputStream(fullPath, co.getConnection());
    }
}
