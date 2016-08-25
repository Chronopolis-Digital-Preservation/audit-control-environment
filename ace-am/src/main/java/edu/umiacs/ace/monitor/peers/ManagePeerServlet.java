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

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.peers.PartnerSiteContextListener.PartnerList;
import edu.umiacs.ace.remote.StatusBean.CollectionBean;
import edu.umiacs.ace.util.EntityManagerServlet;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Add or remove peers from the list of available
 *
 *
 * @author toaster
 */
public class ManagePeerServlet extends EntityManagerServlet {

    public static final String PARAM_PEER_ID = "partnerid";
    public static final String PARAM_PEER_COLL_ID = "remotecollectionid";
    public static final String PARAM_REMOVE = "remove"; // long internal id of relation 
    private static final Logger LOG = Logger.getLogger(ManagePeerServlet.class);

    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em ) throws ServletException, IOException {
        long removeId = getParameter(request, PARAM_REMOVE, -1);
        PartnerSite peerSite = getPartner(request, em);
        long peerColl = getParameter(request, PARAM_PEER_COLL_ID, -1);
        Collection coll = getCollection(request, em);

        if ( removeId > 0 ) {
            LOG.trace("Removing peer with id: " + removeId);
            PeerCollection pc = null;
            try {
                pc = em.getReference(PeerCollection.class, removeId);
                EntityTransaction et = em.getTransaction();
                coll.getPeerCollections().remove(pc);
                et.begin();
                em.remove(pc);
                em.merge(coll);
                et.commit();
            } catch ( EntityNotFoundException e ) {
            }

        } else if ( coll != null && peerSite != null && validPeer(peerSite,
                peerColl) ) {
            boolean alreadyRegistered = false;

            for ( PeerCollection pc : coll.getPeerCollections() ) {
                if ( pc.getPeerId() == peerColl && pc.getSite().equals(peerSite) ) {
                    alreadyRegistered = true;
                }
            }

            if ( !alreadyRegistered ) {
                LOG.trace("Adding peer id: " + peerColl + " for " + coll.getName());
                PeerCollection pc = new PeerCollection();
                pc.setParent(coll);
                pc.setPeerId(peerColl);
                pc.setSite(peerSite);
                coll.getPeerCollections().add(pc);
                EntityTransaction et = em.getTransaction();
                et.begin();
                em.persist(pc);
                em.merge(coll);
                et.commit();
            } else {
                LOG.trace("Not adding already registered peer collection, localId: " + coll.getId()
                        + " remote id " + peerColl);
            }
        } else {
            LOG.trace("Manage peer doing nothing, remove: " + removeId + " site: " + peerSite
                    + " peer coll: " + peerColl + " local coll: " + coll);
        }

        RequestDispatcher dispatcher;
        if ( coll == null ) {
            dispatcher = request.getRequestDispatcher("Status");
        } else {
            dispatcher = request.getRequestDispatcher("ManageCollection?collectionid="
                    + coll.getId());
        }

        dispatcher.forward(request, response);
    }

    private boolean validPeer( PartnerSite site, long peerColl ) {
        PartnerList pl = (PartnerList) getServletContext().getAttribute(
                PartnerSiteContextListener.PAGE_PARTNER_LIST);
        List<PartnerSiteContextListener.ExtendedPartnerSite> partnerList = pl.getSites();

        for ( PartnerSiteContextListener.ExtendedPartnerSite partner : partnerList ) {
            if ( site.getId().equals(partner.getId()) ) {
                for ( CollectionBean cb : partner.getCollections() ) {
                    if ( peerColl == cb.getId() ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private PartnerSite getPartner( HttpServletRequest request, EntityManager em ) {
        long peer = getParameter(request, PARAM_PEER_ID, -1);
        if ( peer < 0 ) {
            return null;
        }
        try {
            return em.getReference(PartnerSite.class, peer);
        } catch ( EntityNotFoundException e ) {
            return null;
        }
    }
}
