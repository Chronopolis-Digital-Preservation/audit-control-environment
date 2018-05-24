/*
 *
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
package edu.umiacs.ace.monitor.users;

import edu.umiacs.ace.monitor.settings.SettingsConstants;
import edu.umiacs.ace.monitor.settings.SettingsParameter;
import edu.umiacs.ace.monitor.settings.SettingsUtil;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.util.Strings;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;

/**
 *
 * @author toaster
 */
public class DefaultAccountContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(DefaultAccountContextListener.class);
    private static final String PARAM_USER_AUTH = "auth.management";
    /**
     * Hack to add a default admin account if none is found
     * @param arg0
     */
    @Override
    public void contextInitialized( ServletContextEvent arg0 ) {
        ServletContext ctx = arg0.getServletContext();
        EntityManager em = PersistUtil.getEntityManager();

        Boolean isAuthMgt = true;
        SettingsParameter userAuth = SettingsUtil.getOrDefault(PARAM_USER_AUTH,
                SettingsConstants.authManagement, em);

        String authStr = userAuth.getValue();
        if (!Strings.isEmpty(authStr)) {
            isAuthMgt = Boolean.valueOf(authStr);
        }
        ctx.setAttribute("authmanagement", isAuthMgt);

        if ( isAuthMgt ) {
            try {
                //            LOG.trace("Testing for account setup");

                Query q = em.createNamedQuery("Users.listAllUsers");

                if ( q.getResultList() == null || q.getResultList().isEmpty() ) {
                    LOG.debug("No accounts found, creating default admin");
                    Users u = new Users();
                    u.setPassword("admin");
                    u.setUsername("admin");

                    UserRoles status = new UserRoles();
                    status.setRolename("Status");
                    status.setUsername("admin");

                    UserRoles collMod = new UserRoles();
                    collMod.setRolename("Collection Modify");
                    collMod.setUsername("admin");

                    UserRoles browse = new UserRoles();
                    browse.setRolename("Browse");
                    browse.setUsername("admin");

                    UserRoles log = new UserRoles();
                    log.setRolename("Log");
                    log.setUsername("admin");

                    UserRoles audit = new UserRoles();
                    audit.setRolename("Audit");
                    audit.setUsername("admin");

                    UserRoles removeItem = new UserRoles();
                    removeItem.setRolename("Remove Item");
                    removeItem.setUsername("admin");

                    UserRoles users = new UserRoles();
                    users.setRolename("Users");
                    users.setUsername("admin");

                    UserRoles report = new UserRoles();
                    report.setRolename("Report");
                    report.setUsername("admin");

                    UserRoles dloadtoken = new UserRoles();
                    dloadtoken.setRolename("Download Token");
                    dloadtoken.setUsername("admin");

                    UserRoles dloaditem = new UserRoles();
                    dloaditem.setRolename("Download Item");
                    dloaditem.setUsername("admin");

                    UserRoles summary = new UserRoles();
                    summary.setRolename("Summary");
                    summary.setUsername("admin");

                    UserRoles compare = new UserRoles();
                    compare.setRolename("Compare");
                    compare.setUsername("admin");

                    UserRoles duplicates = new UserRoles();
                    duplicates.setRolename("Show Duplicates");
                    duplicates.setUsername("admin");

                    UserRoles auditSummaries = new UserRoles();
                    auditSummaries.setRolename("View Audit Summaries");
                    auditSummaries.setUsername("admin");

                    UserRoles actReporting = new UserRoles();
                    actReporting.setRolename("Modify Activity Reporting");
                    actReporting.setUsername("admin");

                    UserRoles partnerSite = new UserRoles();
                    partnerSite.setRolename("Modify Partner Sites");
                    partnerSite.setUsername("admin");

                    UserRoles updateSysSettings = new UserRoles();
                    updateSysSettings.setRolename("Modify System Settings");
                    updateSysSettings.setUsername("admin");


                    EntityTransaction et = em.getTransaction();
                    et.begin();
                    em.persist(u);
                    em.persist(status);
                    em.persist(collMod);
                    em.persist(browse);
                    em.persist(log);
                    em.persist(audit);
                    em.persist(removeItem);
                    em.persist(users);
                    em.persist(report);
                    em.persist(dloadtoken);
                    em.persist(dloaditem);
                    em.persist(summary);
                    em.persist(duplicates);
                    em.persist(compare);
                    em.persist(auditSummaries);
                    em.persist(actReporting);
                    em.persist(partnerSite);
                    em.persist(updateSysSettings);
                    et.commit();

                }
            } catch ( Exception e ) {

                LOG.error("Error creating default account", e);
            }

            checkSystemSettings(em);
        }
    }

    // It's possible to already have an admin account and not have the
    // system settings option yet, so check that here
    private void checkSystemSettings(EntityManager em) {
        boolean settingsMigration = false;
        Query roleQuery = em.createNamedQuery("UserRoles.listRolesForUser");
        roleQuery.setParameter("user", "admin");

        List<UserRoles> adminRoles = roleQuery.getResultList();
        for ( UserRoles role: adminRoles ) {
            if ( role.getRolename().equals("Modify System Settings") ) {
                settingsMigration = true;
            }
        }

        if ( !settingsMigration ) {
            UserRoles updateSysSettings = new UserRoles();
            updateSysSettings.setRolename("Modify System Settings");
            updateSysSettings.setUsername("admin");
            EntityTransaction et = em.getTransaction();
            et.begin();
            em.persist(updateSysSettings);
            et.commit();
        }
    }

    @Override
    public void contextDestroyed( ServletContextEvent arg0 ) {
    }
}
