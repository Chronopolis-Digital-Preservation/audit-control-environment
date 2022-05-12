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

import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.driver.StorageDriverFactory;
import edu.umiacs.ace.monitor.access.CollectionCountContext;
import edu.umiacs.ace.monitor.access.StatusServlet;
import edu.umiacs.ace.monitor.log.LogEnum;
import edu.umiacs.ace.monitor.log.LogEventManager;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.util.Strings;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.eclipse.persistence.exceptions.DatabaseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ForkJoinPool;

/**
 * add,modify,remove the settings of a collection
 *
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


        /*
         * Modification, view, or removal of an existing collection
         *  if we have an int, and its > 0, and it is the key for a resource
         */
        if ((collection = getCollection(request, em)) != null) {
            populateCollection(request, collection);

            if (collection.getStorage() != null) {
                storage = StorageDriverFactory.createStorageAccess(collection, em);
            }

            /*
             * Tst to see if we're removing a collection
             */
            if (!Strings.isEmpty(request.getParameter(PARAM_REMOVE))
                    && request.getParameter(PARAM_REMOVE).toLowerCase().equals("yes")) {
                LOG.debug("removing collection " + collection.getName());

                // update collection state to REMOVED
                collection.setState(CollectionState.REMOVED);
                trans = em.getTransaction();
                trans.begin();
                em.merge(collection);
                trans.commit();

                // Add COLLECTION_REMOVED event
                long eventSession = System.currentTimeMillis();
                addLogEvent(em, eventSession, null, new Date(), LogEnum.COLLECTION_REMOVED, collection, null);

                // remove from audit context
                CollectionCountContext.decrementTotalCollections(collection);
                // clear session of the working collection
                request.getSession().removeAttribute(StatusServlet.SESSION_WORKINGCOLLECTION);
 
                dispatcher = request.getRequestDispatcher("collectionfinish.jsp");
            } /*
             * otherwise, are we updating?
             */ else if (checkParameters(request)
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
            } /*
             * ok, we're just opening an existing collection for modification
             *  - this should display the storage page since storage will be set
             */ else {
                LOG.debug("loading existing collection: " + collection.getName());
                dispatcher = request.getRequestDispatcher("collectionmodify.jsp");
            }
        } /*
         * its either a new submission, or requesting a blank page.
         *  We shouldn't set storage information here yet, just create the
         *  new collection and create a blank storage
         */ else {
            collection = new Collection();
            collection.setState(CollectionState.NEVER);
            populateCollection(request, collection);

            // is all the collection params match, commit and create a storage
            if (checkParameters(request) && hasDigest(request)) {
                LOG.debug("creating collection, empty driver: " + collection.getName());
                PersistUtil.persist(collection);
                storage = StorageDriverFactory.createStorageAccess(collection, em);
                CollectionCountContext.incrementTotalCollections();
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

    /**
     * Persist log event.
     * @param em
     * @param session
     * @param path
     * @param date
     * @param logType
     * @param collection
     * @param msg
     */
    private void addLogEvent(EntityManager em, long session, String path, Date date, LogEnum logType, Collection collection, String msg) {
    	LogEventManager logEventManager = new LogEventManager(session, collection);
    	logEventManager.persistItemEvent(logType, path, msg, em);
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

    public static void populateCollection(HttpServletRequest req, Collection col) {
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

        if (col.getSettings() == null)
            col.setSettings(new HashMap<>());

        if (!Strings.isEmpty(req.getParameter(PARAM_EMAILLIST))) {
            col.getSettings().put(ConfigConstants.ATTR_EMAIL_RECIPIENTS, req.getParameter(PARAM_EMAILLIST));
        }

        if (Strings.isValidInt(req.getParameter(PARAM_CHECKPERIOD))) {
            col.getSettings().put(ConfigConstants.ATTR_AUDIT_PERIOD, req.getParameter(PARAM_CHECKPERIOD));
        }

        if (req.getParameter(PARAM_PROXY_DATA) != null) {
            col.getSettings().put(ConfigConstants.ATTR_PROXY_DATA, req.getParameter(PARAM_PROXY_DATA));
        }

        if (req.getParameter(PARAM_AUDIT_TOKENS) != null) {
            col.getSettings().put(ConfigConstants.ATTR_AUDIT_TOKENS, req.getParameter(PARAM_AUDIT_TOKENS));
        }
    }

    public boolean checkParameters(HttpServletRequest req) {
        return (!Strings.isEmpty(req.getParameter(PARAM_NAME))
                && !Strings.isEmpty(req.getParameter(PARAM_DIR))
                && StorageDriverFactory.listResources().contains(req.getParameter(PARAM_DRIVER)));
    }

    private class RemoveThread implements Runnable {
        private static final String COLL_ID = "collection_id";
        private static final String PARENT_ID = "parent_id";
        private static final String PARENT_COLL_ID = "parentcollection_id";

        private static final String LOG_EVENT = "logevent";
        private static final String LOG_EVENT_ID = COLL_ID;
        private static final String ACE_TOKEN = "acetoken";
        private static final String ACE_TOKEN_ID = PARENT_COLL_ID;
        private static final String MONITORED_ITEM = "monitored_item";
        private static final String MONITORED_ITEM_ID = PARENT_COLL_ID;
        private static final String FILTER_ENTRY = "filter_entry";
        private static final String FILTER_ENTRY_ID = COLL_ID;
        private static final String REPORT_SUMMARY = "report_summary";
        private static final String REPORT_SUMMARY_ID = COLL_ID;
        private static final String PEER_COLLECTION = "peer_collection";
        private static final String PEER_COLLECTION_ID = PARENT_ID;

        private Boolean abort = false;
        private Collection collection;
        private StorageDriver storage;

        private RemoveThread(Collection collection, StorageDriver storage) {
            this.collection = collection;
            this.storage = storage;
        }

        @Override
        public void run() {
            // this is on a separate thread so re-acquire the entity manager
            NDC.push("[Remove " + collection.getName() + "] ");
            EntityManager em = PersistUtil.getEntityManager();
            // batching to reduce contention on tables when deleting many rows
            // todo: native queries aren't the best for this, but deleting with jpql is... weird
            //   could possibly try to use a CriteriaBuilder and issue a subquery
            LOG.info("Starting remove");
            batchRm(em, collection, LOG_EVENT, LOG_EVENT_ID);
            batchRm(em, collection, ACE_TOKEN, ACE_TOKEN_ID);
            batchRm(em, collection, MONITORED_ITEM, MONITORED_ITEM_ID);
            batchRm(em, collection, FILTER_ENTRY, FILTER_ENTRY_ID);
            batchRm(em, collection, REPORT_SUMMARY, REPORT_SUMMARY_ID);
            batchRm(em, collection, PEER_COLLECTION, PEER_COLLECTION_ID);

            if (!abort) {
                // The collection and storage driver are detached at this point so they need to be
                // re-acquired
                collection = em.find(Collection.class, collection.getId());
                storage = StorageDriverFactory.createStorageAccess(collection, em);
                if (storage != null) {
                    storage.remove(em);
                }

                LOG.info("Finishing remove");
                EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                em.remove(collection);
                transaction.commit();
                CollectionCountContext.decrementTotalCollections(collection);
            }

            NDC.pop();
            NDC.clear();
        }

        private void batchRm(EntityManager em, Collection coll, String table, String row) {
            final String queryString = "DELETE FROM %s WHERE %s = %s ORDER BY id LIMIT 1000";
            boolean run = true;

            LOG.debug("Removing entries for " + table);
            Query q = em.createNativeQuery(String.format(queryString, table, row, coll.getId()));
            while (run && !abort) {
                EntityTransaction trans = em.getTransaction();
                trans.begin();
                try {
                    int affected = q.executeUpdate();
                    trans.commit();
                    run = (affected != 0);
                    // LOG.info("Removed " + affected + " rows setting run to " + run);
                } catch (DatabaseException e) {
                    LOG.warn("Caught exception when removing collection", e);
                    trans.rollback();
                    run = false;
                    abort = true;
                }
            }
        }
    }

}
