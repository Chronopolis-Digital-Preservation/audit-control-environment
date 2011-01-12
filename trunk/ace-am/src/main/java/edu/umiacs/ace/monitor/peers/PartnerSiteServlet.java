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

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import sun.misc.BASE64Encoder;

/**
 * Manage partner sites
 * partnerid = db id of partner site
 * remove = true/false, if true, remove supplied id
 * user = http user for remote;
 * pass = http pass for remote;
 * url = base url to ace installation
 * @author toaster
 */
public final class PartnerSiteServlet extends EntityManagerServlet {

    private static final String PARAM_ID = "partnerid";
    private static final String PARAM_USER = "user";
    private static final String PARAM_PASS = "pass";
    private static final String PARAM_URL = "url";
    private static final String PARAM_REMOVE = "remove";
    private static final String PAGE_PARTNER = "partner";
    private static final String PAGE_ERROR = "error";
    private static final String PAGE_PARTNER_LIST = "partnerList";
    private static final Logger LOG = Logger.getLogger(PartnerSiteServlet.class);

    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em ) throws ServletException, IOException {

        RequestDispatcher dispatcher;
        PartnerSite partner = null;
        long partnerId = getParameter(request, PARAM_ID, 0);
        String user = getParameter(request, PARAM_USER, "");
        String pass = getParameter(request, PARAM_PASS, "");
        String url = getParameter(request, PARAM_URL, "");

        if ( partnerId > 0 ) {
            partner = em.getReference(PartnerSite.class, partnerId);
        }
        boolean remove = getParameter(request, PARAM_REMOVE, false);

        FailType outcome = validParams(request);

        if ( partner != null && remove ) {
            // remove
            EntityTransaction et = em.getTransaction();
            et.begin();
            Query q = em.createNamedQuery("PeerCollection.deleteByPeerSite");
            q.setParameter("sire", partner);
            q.executeUpdate();
            em.remove(partner);

            et.commit();
            partner = null;
        } else if ( partner != null && outcome == FailType.SUCCESS ) {
            partner.setPass(pass);
            partner.setRemoteURL(url);
            partner.setUser(user);

            EntityTransaction et = em.getTransaction();
            et.begin();
            em.merge(partner);
            et.commit();
        } else if ( validParams(request) == FailType.SUCCESS ) {
            partner = new PartnerSite();
            partner.setPass(pass);
            partner.setRemoteURL(url);
            partner.setUser(user);

            EntityTransaction et = em.getTransaction();
            et.begin();
            em.persist(partner);
            et.commit();
        } else if ( !Strings.isEmpty(user) || !Strings.isEmpty(pass) || !Strings.isEmpty(
                url) ) {
            request.setAttribute(PAGE_ERROR, outcome);
        }

        request.setAttribute(PAGE_PARTNER, partner);

        dispatcher = request.getRequestDispatcher("partnersite.jsp");
        dispatcher.forward(request, response);
    }

    private FailType validParams( HttpServletRequest params ) {
        String user = getParameter(params, PARAM_USER, "");
        String pass = getParameter(params, PARAM_PASS, "");
        String url = getParameter(params, PARAM_URL, "");

        if ( Strings.isEmpty(user) ) {
            return FailType.MISSING_USER;
        }
        if ( Strings.isEmpty(pass) ) {
            return FailType.MISSING_PASSWORD;
        }
        if ( Strings.isEmpty(url) ) {
            return FailType.MISSING_URL;
        }

        try {
            URL encodedUrl = new URL(url);


            URLConnection uc = encodedUrl.openConnection();
            uc.setRequestProperty("Authorization", "Basic " + encode(
                    user + ":" + pass));
            uc.getInputStream().close();

            return FailType.SUCCESS;
        } catch ( MalformedURLException e ) {
            LOG.error("Bad url", e);

            return FailType.MALFORMED_URL;
        } catch ( Exception e ) {
            LOG.error("can't connect", e);
            return FailType.CANNOT_CONNECT;
        }
    }

    public static String encode( String source ) {
        BASE64Encoder enc = new sun.misc.BASE64Encoder();
        return (enc.encode(source.getBytes()));
    }

    public enum FailType {

        SUCCESS("Connection successful"),
        MISSING_USER("No username supplied"),
        MISSING_PASSWORD("No password supplied"),
        MISSING_URL("No url supplied"),
        MALFORMED_URL("Bad formart for URL"),
        CANNOT_CONNECT("Error connecting to url with user and password");
        private String msg;

        private FailType( String msg ) {
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }
    }
}
