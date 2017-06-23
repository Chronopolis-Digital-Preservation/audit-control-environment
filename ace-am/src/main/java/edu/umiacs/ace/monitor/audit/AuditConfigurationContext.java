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
package edu.umiacs.ace.monitor.audit;

import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.driver.StorageDriverFactory;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.ConfigConstants;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.settings.SettingsConstants;
import edu.umiacs.ace.monitor.settings.SettingsParameter;
import edu.umiacs.ace.monitor.settings.SettingsUtil;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.util.Strings;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Set the IMS for the AuditThread to use. Also, startup a background task
 * to handle firing off monitor tasks.
 * 
 * TODO: Handle token validation
 * 
 * @author toaster
 */
public final class AuditConfigurationContext implements ServletContextListener {

    //private static final String PARAM_IMS = "ims";
    //private static final String PARAM_IMS_PORT = "ims.port";
    //private static final String PARAM_IMS_TOKEN_CLASS = "ims.tokenclass";
    //private static final String PARAM_IMS_SSL = "ims.ssl";
    //private static final String PARAM_DISABLE_AUTO_AUDIT = "auto.audit.disable";
    //private static final String PARAM_THROTTLE_MAXAUDIT = "throttle.maxaudit";
    //private static final String PARAM_AUDIT_ONLY = "audit.only";
    public static final String ATTRIBUTE_PAUSE = "pause";
    private static final long HOUR = 1000 * 60 * 60;
    private Timer checkTimer;
    private static final Logger LOG = Logger.getLogger(AuditConfigurationContext.class);

    @Override
    public void contextInitialized( ServletContextEvent arg0 ) {
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("SettingsParameter.getAttr");
        SettingsParameter s = null;

        ServletContext ctx = arg0.getServletContext();
        // set IMS for audit Thread from server parameter
        q.setParameter("attr", SettingsConstants.PARAM_IMS);
        s = (SettingsParameter) q.getSingleResult();
        AuditThreadFactory.setIMS(s.getValue());
        if ( Strings.isEmpty(AuditThreadFactory.getIMS()) ) {
            throw new RuntimeException("IMS is empty");
        }

        q.setParameter("attr", SettingsConstants.PARAM_IMS_TOKEN_CLASS);
        s = (SettingsParameter) q.getSingleResult();
        if ( !Strings.isEmpty(s.getValue()) ) {
            String tokenClass = s.getValue();
            AuditThreadFactory.setTokenClass(tokenClass);
        }


        q.setParameter("attr", SettingsConstants.PARAM_IMS_PORT);
        s = (SettingsParameter) q.getSingleResult();
        if ( Strings.isValidInt(s.getValue()) ) {
            int port = Integer.parseInt(s.getValue());
            if ( port > 1 && port < 32768 ) {
                AuditThreadFactory.setImsPort(port);
            } else {
                throw new RuntimeException("ims.port must be between 1 and 32768");
            }
        }

        q.setParameter("attr", SettingsConstants.PARAM_AUDIT_ONLY);
        try {
            s = (SettingsParameter) q.getSingleResult();
            AuditThreadFactory.setAuditOnly(Boolean.valueOf(s.getValue()));
        }catch ( NoResultException ex ) {
            EntityTransaction trans = em.getTransaction();
            trans.begin();
            em.persist(new SettingsParameter(SettingsConstants.PARAM_AUDIT_ONLY,
                    SettingsConstants.auditOnly, false));
            trans.commit();
            AuditThreadFactory.setAuditOnly(false);
        }

        q.setParameter("attr", SettingsConstants.PARAM_AUDIT_SAMPLE);
        try {
            s = (SettingsParameter) q.getSingleResult();
            AuditThreadFactory.setAuditSampling(Boolean.valueOf(s.getValue()));
        } catch (NoResultException ex) {
            EntityTransaction trans = em.getTransaction();
            trans.begin();
            em.persist(new SettingsParameter(SettingsConstants.PARAM_AUDIT_SAMPLE,
                    SettingsConstants.auditSample, false));
            trans.commit();
            AuditThreadFactory.setAuditSampling(false);
        }

        q.setParameter("attr", SettingsConstants.PARAM_IMS_SSL);
        try {
            s = (SettingsParameter) q.getSingleResult();
            AuditThreadFactory.setSSL(Boolean.valueOf(s.getValue()));
        } catch (NoResultException ex) {
            EntityTransaction trans = em.getTransaction();
            trans.begin();
            em.persist(new SettingsParameter(SettingsConstants.PARAM_IMS_SSL,
                    SettingsConstants.imsSSL, false));
            trans.commit();
            AuditThreadFactory.setSSL(false);
        }

        q.setParameter("attr", SettingsConstants.PARAM_AUDIT_BLOCKING);
        try {
            s = (SettingsParameter) q.getSingleResult();
            AuditThreadFactory.setBlocking(Boolean.valueOf(s.getValue()));
        } catch (NoResultException ex) {
            EntityTransaction trans = em.getTransaction();
            trans.begin();
            em.persist(new SettingsParameter(SettingsConstants.PARAM_AUDIT_BLOCKING,
                                             SettingsConstants.auditBlocking,
                                             false));
            trans.commit();
            AuditThreadFactory.setBlocking(false);
        }

        q.setParameter("attr", SettingsConstants.PARAM_AUDIT_MAX_BLOCK_TIME);
        try {
            s = (SettingsParameter) q.getSingleResult();
            int blockTime = 0;
            String val = s.getValue();
            if (Strings.isValidInt(val)) {
                blockTime = Integer.parseInt(val);
            }

            // Just in case...
            if ( blockTime < 0 ) {
                blockTime = 0;
            }
            AuditThreadFactory.setMaxBlockTime(blockTime);
        } catch (NoResultException ex) {
            EntityTransaction trans = em.getTransaction();
            trans.begin();
            em.persist(new SettingsParameter(SettingsConstants.PARAM_AUDIT_MAX_BLOCK_TIME,
                    SettingsConstants.auditMaxBlockTime,
                    false));
            trans.commit();
            AuditThreadFactory.setBlocking(false);
        }

        PauseBean pb = new PauseBean();
        ctx.setAttribute(ATTRIBUTE_PAUSE, pb);

        q.setParameter("attr", SettingsConstants.PARAM_AUTO_AUDIT_ENABLE);
        s = (SettingsParameter) q.getSingleResult();
        // Invert the boolean because the PB checks if we're paused, not enabled
        String enableAudits = s.getValue();
        pb.setPaused(!Boolean.valueOf(enableAudits));

        q.setParameter("attr", SettingsConstants.PARAM_THROTTLE_MAXAUDIT);
        s = (SettingsParameter) q.getSingleResult();
        String maxRun = s.getValue();
        if ( Strings.isValidInt(maxRun) ) {
            int audit = Integer.parseInt(maxRun);
            if ( audit > 0 ) {
                AuditThreadFactory.setMaxAudits(audit);
            }
        }

        // q.setParam(attr, continuous audit)
        // s = q.getSingleResult
        // seed = s.getValue
        //bgAudit = new BackgroundAuditorFactory();
        //BackgroundAuditorFactory.start();

        em.close();
        checkTimer = new Timer("Audit Check Timer");
        checkTimer.schedule(new MyTimerTask(pb), 0, HOUR);

    }

    /**
     * TODO: Use this to eliminate a lot of the boilerplate above with this
     *
    private void doDbStuff(EntityManager em, String param, String defaultVal, Method method) throws InvocationTargetException, IllegalAccessException {
        SettingsParameter s = null;
        Query q = em.createNamedQuery("SettingsParameter.getAttr");

        q.setParameter("attr", param);
        try {
            s = (SettingsParameter) q.getSingleResult();
            method.invoke(null, s.getValue());
        } catch (NoResultException ex) {
            EntityTransaction trans = em.getTransaction();
            trans.begin();
            em.persist(new SettingsParameter(param,
                    defaultVal,
                    false));
            trans.commit();
            method.invoke(null, defaultVal);
        }

    }
    */

    @Override
    public void contextDestroyed( ServletContextEvent arg0 ) {
        if ( checkTimer != null ) {
            checkTimer.cancel();
        }
        AuditThreadFactory.cancellAll();
        AuditTokens.cancellAll();
    }

    public static class PauseBean {

        private boolean paused = false;

        private PauseBean() {
        }

        public void setPaused( boolean paused ) {
            this.paused = paused;
        }

        public boolean isPaused() {
            return paused;
        }

        @Override
        public String toString() {
            return Boolean.toString(paused);
        }
    }

    static class MyTimerTask extends TimerTask {

        private PauseBean pb;

        private MyTimerTask( PauseBean pb ) {
            this.pb = pb;
        }

        private boolean auditing(Collection c) {
            return AuditThreadFactory.isQueued(c) || AuditThreadFactory.isRunning(c);
        }

        @Override
        public void run() {
            NDC.push("[Audit Timer]");
            EntityManager em = PersistUtil.getEntityManager();
            // List all collections
            List<Collection> items;
            LOG.trace("START - checking for required audit");
            try {
                if ( pb.isPaused() ) {
                    return;
                }

                Query query =
                        em.createNamedQuery("Collection.listAllCollections");

                items = query.getResultList();

                for ( Collection c : items ) {
                    StorageDriver sa;
                    int checkperiod = SettingsUtil.getInt(c, ConfigConstants.ATTR_AUDIT_PERIOD, 0);
                    if ( checkperiod < 1 ) {
                        LOG.trace("Skipping auditing for collection: " + c.getName()
                                + " check period: " + checkperiod);
                        continue;
                    }
                    // if last sync is null, fire away since we haven't run yet
                    if ( c.getLastSync() == null ) {
                        LOG.debug("No last sync for " + c.getName() + " running");
                        sa = StorageDriverFactory.createStorageAccess(c,
                                em);
                        AuditThreadFactory.createThread(c, sa, true, (MonitoredItem[])null);
                    } else {
                        long syncTime = c.getLastSync().getTime();
                        long currTime = System.currentTimeMillis();
                        // if the next audit is due and we haven't already started the audit
                        long lastSyncTime = currTime - syncTime;
                        if ( lastSyncTime > ((long) (checkperiod * HOUR * 24)) && 
                             !auditing(c) ) {
                                
                            LOG.debug("last sync difference: " + (currTime - syncTime)
                                    + " greater than " + (checkperiod * HOUR * 24));
                            sa = StorageDriverFactory.createStorageAccess(c, em);
                                    
                            AuditThreadFactory.createThread(c, sa, true, (MonitoredItem[]) null);
                        } else {
                            LOG.trace("No Sync on " + c.getName());
                        }
                    }
                }
            } catch ( Throwable t ) {
                LOG.error("Error testing to see if collections need auditing", t);
            } finally {
                LOG.trace("FINISH - checking for required audit");
                NDC.pop();
                em.close();
            }
        }
    }
}
