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
package edu.umiacs.ace.monitor.core;

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.driver.StorageDriverFactory;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * add,modify,remove the settings of a collection
 * @author toaster
 */
public class ManageCollectionServlet extends EntityManagerServlet {

    private static final Logger LOG = Logger.getLogger(ManageCollectionServlet.class);
    private static final String PARAM_REMOVE = "remove";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_DIR = "directory";
    private static final String PARAM_DRIVER = "driver";
    private static final String PARAM_PROXY_DATA = "proxy";
    private static final String PARAM_CHECKPERIOD = "checkperiod";
    private static final String PARAM_AUDIT_TOKENS = "audittokens";
    private static final String PAGE_COLLECTION = "collection";
    private static final String PAGE_DRIVER = "driver";
    private static final String PAGE_AVAILDRIVERS = "availdrivers";
    private static final String PAGE_ERROR = "error";
    private static final String PARAM_GROUP = "group";
    private static final String PAGE_GROUP_LIST = "grouplist";
    private static final String PARAM_EMAILLIST = "emaillist";
    private static final String PARAM_DIGEST = "digest";
//    private static final String PARAM_PEER_PREFIX = "peer_id";
//    private static final String PARAM_PEER_COLLECTION_PREFIX = "peer_col_id";

    /**
     *
     * @param request
     * @param response
     * @param em
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        EntityTransaction trans;
        RequestDispatcher dispatcher;
        Collection collection;
        StorageDriver storage = null;
        String paramCheckResponse = null;


        /**
         * Modification, view, or removal of an existing collection       
         *  if we have an int, and its > 0, and it is the key for a resource
         */
        if ((collection = getCollection(request, em)) != null) {
            populateCollection(request, collection);

            if (collection.getStorage() != null) {
                storage = StorageDriverFactory.createStorageAccess(collection, em);

            }


            /**
             * Tst to see if we're removing a collection
             */
            if (!Strings.isEmpty(request.getParameter(PARAM_REMOVE))
                    && request.getParameter(PARAM_REMOVE).toLowerCase().equals("yes")) {
                LOG.debug("removing collection" + collection.getName());
                removeCollection(em, collection, storage);
                response.sendRedirect("Status?collectionid=-1");
                return;
            } /**
             * otherwise, are we updating?
             */
            else if (checkParameters(request)
                    && ((paramCheckResponse = checkStorage(storage, request, collection)) == null)) {
                LOG.debug("updating collection: " + collection.getName());
                trans = em.getTransaction();
                trans.begin();
                em.merge(collection);
                if (storage != null) {
                    storage.setParameters(request.getParameterMap());
                }
                trans.commit();
                dispatcher = request.getRequestDispatcher("collectionfinish.jsp");
            } /**
             * ok, we're just opening an existing collection for modification
             *  - this should display the storage page since storage will be set
             */
            else {
                LOG.debug("loading existing collection: " + collection.getName());
                dispatcher = request.getRequestDispatcher("collectionmodify.jsp");
            }
        } /**
         * its either a new submission, or requesting a blank page.
         *  We shouldn't set storage information here yet, just create the 
         *  new collection and create a blank storage
         */
        else {
            collection = new Collection();
            collection.setState('N');
            populateCollection(request, collection);

            // is all the collection params match, commit and create a storage
            if (checkParameters(request) && hasDigest(request)) {
                LOG.debug("creating collection, empty driver: " + collection.getName());
                PersistUtil.persist(collection);
                storage = StorageDriverFactory.createStorageAccess(collection,
                        em);
            }

            dispatcher = request.getRequestDispatcher("collectionmodify.jsp");
        }
        request.setAttribute(PAGE_ERROR, paramCheckResponse);
        Query q = em.createNamedQuery("Collection.listGroups");
        request.setAttribute(PAGE_GROUP_LIST, q.getResultList());
        request.setAttribute(PAGE_COLLECTION, collection);
        request.setAttribute(PAGE_AVAILDRIVERS,
                StorageDriverFactory.listResources());
        request.setAttribute(PAGE_DRIVER, storage);
        dispatcher.forward(request, response);

    }

    private String checkStorage(StorageDriver sd, ServletRequest request, Collection collection) {
        String result = null;
        if (sd == null) {
            //result = "No Storage Configured";
        } else {
            result = sd.checkParameters(request.getParameterMap(), collection.getDirectory());
        }
        return result;
    }

    private void removeCollection(EntityManager em, Collection collection, StorageDriver storage) {
        EntityTransaction trans;
        Query q;
        trans = em.getTransaction();
        trans.begin();
        q = em.createNamedQuery("LogEvent.deleteByCollection");
        q.setParameter("coll", collection);
        q.executeUpdate();
        q = em.createNamedQuery("Token.deleteByCollection");
        q.setParameter("coll", collection);
        q.executeUpdate();
        q = em.createNamedQuery("MonitoredItem.deleteByCollection");
        q.setParameter("coll", collection);
        q.executeUpdate();
        q = em.createNamedQuery("FilterEntry.dropByCollection");
        q.setParameter("coll", collection);
        q.executeUpdate();
        q = em.createNamedQuery("ReportSummary.deleteByCollection");
        q.setParameter("coll", collection);
        q.executeUpdate();
        q = em.createNamedQuery("PeerCollection.deleteByCollection");
        q.setParameter("coll", collection);
        q.executeUpdate();
        if (storage != null) {
            storage.remove(em);
        }
        em.remove(collection);
        trans.commit();
    }

    private boolean hasDigest(HttpServletRequest req) {
        String digest = req.getParameter(PARAM_DIGEST);
        if (!Strings.isEmpty(digest)) {
            try {
                MessageDigest.getInstance(digest);
                return true;
            } catch (NoSuchAlgorithmException e) {
                LOG.error("Attempt to register unfound provider: " + digest, e);
            }
        }
        return false;
    }

    public void populateCollection(HttpServletRequest req, Collection col) {
        if (!Strings.isEmpty(req.getParameter(PARAM_NAME))) {
            col.setName(req.getParameter(PARAM_NAME));
        }
        if (!Strings.isEmpty(req.getParameter(PARAM_DIR))) {
            col.setDirectory(req.getParameter(PARAM_DIR));
        }
        if (!Strings.isEmpty(req.getParameter(PARAM_DRIVER))) {
            col.setStorage(req.getParameter(PARAM_DRIVER));
        }
        if (!Strings.isEmpty(req.getParameter(PARAM_DIGEST)) && col.getId() < 1) {
            // do not allow changes
            col.setDigestAlgorithm(req.getParameter(PARAM_DIGEST));
        }

        if (!Strings.isEmpty(req.getParameter(PARAM_GROUP))) {
            col.setGroup(req.getParameter(PARAM_GROUP));
        }

        if (!Strings.isEmpty(req.getParameter(PARAM_EMAILLIST))) {
//            col.setEmailList(req.getParameter(PARAM_EMAILLIST));
            col.getSettings().put(ConfigConstants.ATTR_EMAIL_RECIPIENTS, req.getParameter(PARAM_EMAILLIST));
        }
//
        if (Strings.isValidInt(req.getParameter(PARAM_CHECKPERIOD))) {
            col.getSettings().put(ConfigConstants.ATTR_AUDIT_PERIOD, req.getParameter(PARAM_CHECKPERIOD));
//            col.setCheckPeriod(Integer.parseInt(req.getParameter(PARAM_CHECKPERIOD)));
        }
//
        if (req.getParameter(PARAM_PROXY_DATA) != null) {
            col.getSettings().put(ConfigConstants.ATTR_PROXY_DATA, req.getParameter(PARAM_PROXY_DATA));
//            col.setProxyData("true".equals(req.getParameter(PARAM_PROXY_DATA).toLowerCase()));
        }
//
        if (req.getParameter(PARAM_AUDIT_TOKENS) != null) {
            col.getSettings().put(ConfigConstants.ATTR_AUDIT_TOKENS, req.getParameter(PARAM_AUDIT_TOKENS));
//            col.setAuditTokens("true".equals(req.getParameter(PARAM_AUDIT_TOKENS).toLowerCase()));
        }


    }

    public boolean checkParameters(HttpServletRequest req) {
        return (!Strings.isEmpty(req.getParameter(PARAM_NAME))
                && !Strings.isEmpty(req.getParameter(PARAM_DIR))
                && StorageDriverFactory.listResources().contains(req.getParameter(PARAM_DRIVER)));
    }
}
