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

package edu.umiacs.ace.monitor.peers;

import edu.umiacs.ace.remote.JsonGateway;
import edu.umiacs.ace.remote.PeerAuthenticator;
import edu.umiacs.ace.remote.StatusBean.CollectionBean;
import edu.umiacs.ace.util.PersistUtil;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.List;

/**
 * Add a partner list to the application context.
 * 
 * @author toaster
 */
public class PartnerSiteContextListener implements ServletContextListener {

    public static final String PAGE_PARTNER_LIST = "partnerList";

    @Override
    public void contextInitialized( ServletContextEvent arg0 ) {
        Authenticator.setDefault(new PeerAuthenticator());

        arg0.getServletContext().setAttribute(PAGE_PARTNER_LIST,
                new PartnerList());
    }

    @Override
    public void contextDestroyed( ServletContextEvent arg0 ) {
    }

    public static class PartnerList {

        private PartnerList() {
        }

        public List<ExtendedPartnerSite> getSites() {
            EntityManager em = PersistUtil.getEntityManager();
            Query q = em.createNamedQuery("PartnerSite.listAll");
            List l = q.getResultList();
            if ( l != null ) {
                List<ExtendedPartnerSite> ret = new ArrayList<>();
                for ( Object o : l ) {
                    PartnerSite ps = (PartnerSite) o;
                    ret.add(new ExtendedPartnerSite(ps));
                }
                return ret;
            }
            return null;
        }
    }

    public static class ExtendedPartnerSite extends PartnerSite {

        private PartnerSite site;

        private ExtendedPartnerSite( PartnerSite site ) {
            this.site = site;
        }

        public boolean isOnline() {
            return JsonGateway.getGateway().getStatusBean(site) != null;
        }

        public List<CollectionBean> getCollections() {
            if ( JsonGateway.getGateway().getStatusBean(site) == null ) {
                return null;
            }
            return JsonGateway.getGateway().getStatusBean(site).getCollections();
        }

        @Override
        public Long getId() {
            return site.getId();
        }

        @Override
        public String getPass() {
            return site.getPass();
        }

        @Override
        public String getRemoteURL() {
            return site.getRemoteURL();
        }

        @Override
        public String getUser() {
            return site.getUser();
        }
    }
}
