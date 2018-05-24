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
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Set the IMS for the AuditThread to use. Also, startup a background task
 * to handle firing off monitor tasks.
 * <p>
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
    public void contextInitialized(ServletContextEvent arg0) {
        EntityManager em = PersistUtil.getEntityManager();
        TypedQuery<SettingsParameter> nq = em.createNamedQuery(
                "SettingsParameter.getCurrentSettings",
                SettingsParameter.class);
        List<SettingsParameter> resultList = nq.getResultList();
        Map<String, String> resultMap = resultList.stream()
                .collect(Collectors.toMap((SettingsParameter::getName),
                        (SettingsParameter::getValue)));
        em.close();
        if (resultList.isEmpty()) {
            SettingsUtil.updateSettings(SettingsUtil.getDefaultSettings());
        }

        // PauseBean first because of dependencies
        ServletContext ctx = arg0.getServletContext();
        PauseBean pb = new PauseBean();
        ctx.setAttribute(ATTRIBUTE_PAUSE, pb);

        // Invert the boolean because the PB checks if we're paused, not enabled
        String enableAudits = resultMap.getOrDefault(
                SettingsConstants.PARAM_AUTO_AUDIT_ENABLE,
                SettingsConstants.autoAudit);
        pb.setPaused(!Boolean.valueOf(enableAudits));

        checkTimer = new Timer("Audit Check Timer");
        checkTimer.schedule(new MyTimerTask(pb), 0, HOUR);

        // set IMS for audit Thread from server parameter
        AuditThreadFactory.setIMS(resultMap.getOrDefault(
                SettingsConstants.PARAM_IMS,
                SettingsConstants.ims));

        String tokenClass = resultMap.getOrDefault(
                SettingsConstants.PARAM_IMS_TOKEN_CLASS,
                SettingsConstants.imsTokenClass);
        AuditThreadFactory.setTokenClass(tokenClass);

        String port = resultMap.getOrDefault(
                SettingsConstants.PARAM_IMS_PORT,
                SettingsConstants.imsPort);
        if (Strings.isValidInt(port)) {
           AuditThreadFactory.setImsPort(Integer.parseInt(port));
        }

        String auditOnly = resultMap.getOrDefault(
                SettingsConstants.PARAM_AUDIT_ONLY,
                SettingsConstants.auditOnly);
        AuditThreadFactory.setAuditOnly(Boolean.valueOf(auditOnly));

        // keep this off for now even though it isn't used
        AuditThreadFactory.setAuditSampling(false);

        String imsSsl = resultMap.getOrDefault(
                SettingsConstants.PARAM_IMS_SSL,
                SettingsConstants.imsSSL);
        AuditThreadFactory.setSSL(Boolean.valueOf(imsSsl));

        String blocking = resultMap.getOrDefault(
                SettingsConstants.PARAM_AUDIT_BLOCKING,
                SettingsConstants.auditBlocking);
        AuditThreadFactory.setBlocking(Boolean.valueOf(blocking));

        String blockTimeS = resultMap.getOrDefault(
                SettingsConstants.PARAM_AUDIT_MAX_BLOCK_TIME,
                SettingsConstants.auditMaxBlockTime);
        int blockTime = 0;
        if (Strings.isValidInt(blockTimeS)) {
            blockTime = Integer.parseInt(blockTimeS);
        }

        // Just in case...
        if (blockTime < 0) {
            blockTime = 0;
        }
        AuditThreadFactory.setMaxBlockTime(blockTime);

        String maxAudit = resultMap.getOrDefault(
                SettingsConstants.PARAM_THROTTLE_MAXAUDIT,
                SettingsConstants.maxAudit);
        if (Strings.isValidInt(maxAudit)) {
            int audit = Integer.parseInt(maxAudit);
            if (audit > 0) {
                AuditThreadFactory.setMaxAudits(audit);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        if (checkTimer != null) {
            checkTimer.cancel();
        }
        AuditThreadFactory.cancellAll();
        AuditTokens.cancellAll();
    }

    public static class PauseBean {

        private boolean paused = false;

        private PauseBean() {
        }

        public void setPaused(boolean paused) {
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

        private MyTimerTask(PauseBean pb) {
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
                if (pb.isPaused()) {
                    return;
                }

                Query query =
                        em.createNamedQuery("Collection.listAllCollections");

                items = query.getResultList();

                for (Collection c : items) {
                    StorageDriver sa;
                    int checkperiod = SettingsUtil.getInt(c, ConfigConstants.ATTR_AUDIT_PERIOD, 0);
                    if (checkperiod < 1) {
                        LOG.trace("Skipping auditing for collection: " + c.getName()
                                + " check period: " + checkperiod);
                        continue;
                    }
                    // if last sync is null, fire away since we haven't run yet
                    if (c.getLastSync() == null) {
                        LOG.debug("No last sync for " + c.getName() + " running");
                        sa = StorageDriverFactory.createStorageAccess(c,
                                em);
                        AuditThreadFactory.createThread(c, sa, true, (MonitoredItem[]) null);
                    } else {
                        long syncTime = c.getLastSync().getTime();
                        long currTime = System.currentTimeMillis();
                        // if the next audit is due and we haven't already started the audit
                        long lastSyncTime = currTime - syncTime;
                        if (lastSyncTime > ((long) (checkperiod * HOUR * 24)) &&
                                !auditing(c)) {

                            LOG.debug("last sync difference: " + (currTime - syncTime)
                                    + " greater than " + (checkperiod * HOUR * 24));
                            sa = StorageDriverFactory.createStorageAccess(c, em);

                            AuditThreadFactory.createThread(c, sa, true, (MonitoredItem[]) null);
                        } else {
                            LOG.trace("No Sync on " + c.getName());
                        }
                    }
                }
            } catch (Throwable t) {
                LOG.error("Error testing to see if collections need auditing", t);
            } finally {
                LOG.trace("FINISH - checking for required audit");
                NDC.pop();
                em.close();
            }
        }
    }
}
