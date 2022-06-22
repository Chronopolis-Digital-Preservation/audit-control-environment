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
package edu.umiacs.ace.monitor.access;

import com.google.common.collect.ImmutableList;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.CollectionState;
import edu.umiacs.ace.monitor.support.CStateBean;
import edu.umiacs.ace.monitor.support.PageBean;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.util.Strings;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author toaster
 */
public class StatusServlet extends EntityManagerServlet {

    private static final Logger LOG = Logger.getLogger(StatusServlet.class);

    private static final String PAGE_COLLECTIONS = "collections";
    private static final String ERROR_COLLECTIONS = "errorCollections";
    private static final String NO_GROUP_COLLECTIONS = "noGroupCollections";
    private static final String PAGE_STATES = "states";
    private static final String PAGE_COUNT = "count";
    private static final String PAGE_NUMBER = "page";
    private static final String ACTION_SEARCH = "search";

    private static final long DEFAULT_PAGE = 0;
    private static final int DEFAULT_COUNT = 100;

    // TODO: Add session fields for pagination and search params
    public static final String SESSION_WORKINGCOLLECTION = "workingCollection";
    public static final String SESSION_COLLECTION_URI = "collectionUri";

    private static final String PARAM_CSV = "csv";

    // Pagination stuff
    private static final String PARAM_COUNT = "count";
    private static final String PARAM_PAGE = "page";

    // Search Params
    private static final String PARAM_GROUP = "status_group";
    private static final String PARAM_STATE = "status_state";
    private static final String PARAM_STATE_ERROR = "status_state_error";
    private static final String PARAM_STATE_INTERRUPTED = "status_state_interrupted";
    private static final String PARAM_COLLECTION_LIKE = "status_collection";
    private static final String PARAM_AUDIT_DATE = "audit";
    private static final String PARAM_ACTION = "action";

    // Filter params?
    // ...group
    // ...name (maybe some regex?)

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    @Override
    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {
        RequestDispatcher dispatcher;
        List<CollectionSummaryBean> collections;
        List<CollectionSummaryBean> errorCollections;
        List<CollectionSummaryBean> noGroupCollections;
        List<Collection> items;
        List<Collection> errorItems;
        List<Collection> noGroupItems;
        List<String> collectionGroups;

        long page = getParameter(request, PARAM_PAGE, DEFAULT_PAGE);
        int count = (int) getParameter(request, PARAM_COUNT, DEFAULT_COUNT);

        // local getParameter so that the session is checked as well
        String group = getParameter(request, PARAM_GROUP, null);
        String state = getParameter(request, PARAM_STATE, null);
        String collection = getParameter(request, PARAM_COLLECTION_LIKE, null);
        String action = getParameter(request, PARAM_ACTION, null);
        // String date = getParameter(request, PARAM_GROUP, null);
        PageBean pb = new PageBean((int) page, count, "");

        long offset = page * count;

        // our main query
        StringBuilder queryString = new StringBuilder();

        // for getting a count of items
        StringBuilder countString = new StringBuilder();

        // our parameters
        StringBuilder params = new StringBuilder();

        List<String> queries = new ArrayList<>();

        // TODO: Can probably tidy this up a bit
        if (!Strings.isEmpty(group)) {
            queries.add("c.group LIKE :status_group");
            pb.addParam(PARAM_GROUP, group);
            request.setAttribute(PARAM_GROUP, group);
        }

        if (!Strings.isEmpty(collection)) {
            queries.add("c.name LIKE :status_collection");
            pb.addParam(PARAM_COLLECTION_LIKE, collection);
            request.setAttribute(PARAM_COLLECTION_LIKE, collection);
        }

        // Enforce that the state is not empty, or larger than 1 character
        if (!Strings.isEmpty(state) && state.length() == 1) {
            queries.add("c.state = :status_state");
            pb.addParam(PARAM_STATE, state);
            request.setAttribute(PARAM_STATE, state);
        }

        errorItems = getErrorCollections(em);
        errorCollections = new ArrayList<>();
        for (Collection col : errorItems) {
            CollectionSummaryBean csb = createCollectionSummary(col);
            errorCollections.add(csb);
        }

        collections = new ArrayList<>();
        items = new ArrayList<>();

        collectionGroups = new ArrayList<>();
        noGroupCollections = new ArrayList<>();

        if (hasJson(request) || queries.size() > 0 || action != null && action.trim().equalsIgnoreCase(ACTION_SEARCH)) {
        	// Query collections
            queryString.append("SELECT c FROM Collection c");
            countString.append("SELECT COUNT(c.id) FROM Collection c");

            if (queries.size() > 0) {
                params.append(" WHERE");
            }

            Iterator<String> it = queries.iterator();
            while (it.hasNext()) {
                String query = it.next();
                params.append(" ")
                      .append(query);
                if (it.hasNext()) {
                    params.append(" AND");
                }
            }

            queryString.append(params);
            // allows us to keep a consistent order when displaying collections
            queryString.append(" ORDER BY c.group ASC, c.name ASC");
            countString.append(params);

            TypedQuery<Collection> query =
                    em.createQuery(queryString.toString(), Collection.class);       
            // em.createNamedQuery("Collection.listAllCollections");
            query.setFirstResult((int) offset);
            query.setMaxResults(count);

            Query countQuery = em.createQuery(countString.toString());

            // TODO: Can probably tidy this up a bit
            if (!Strings.isEmpty(group)) {
                query.setParameter(PARAM_GROUP, "%" + group + "%");
                countQuery.setParameter(PARAM_GROUP, "%" + group + "%");
            }

            if (!Strings.isEmpty(collection)) {
                query.setParameter(PARAM_COLLECTION_LIKE, "%" + collection + "%");
                countQuery.setParameter(PARAM_COLLECTION_LIKE, "%" + collection + "%");
            }

            if (!Strings.isEmpty(state) && state.length() == 1) {
                query.setParameter(PARAM_STATE, CollectionState.fromChar(state.charAt(0)));
                countQuery.setParameter(PARAM_STATE, CollectionState.fromChar(state.charAt(0)));
            }

            items = query.getResultList();

            // We only need to execute this query if we have parameters
            // and need to update the total count of collections
            if (queries.size() > 0) {
                long totalResults = (long) countQuery.getSingleResult();
                LOG.info("Total results from query: " + totalResults);
                pb.update(totalResults);
            }
            
            for (Collection col : items) {
                CollectionSummaryBean csb = createCollectionSummary(col);
                collections.add(csb);
            }
        }

        setWorkingCollection(request, em);

        if (action != null && action.trim().equalsIgnoreCase(ACTION_SEARCH)) {
        	// Search action
        	collectionGroups = getCollectionGroups(items);
            noGroupItems = getNoGroupCollections(items);      	
        } else {
        	// Browser groups action
        	collectionGroups = searchCollectionGroups(em);
        	noGroupItems = searchNoGroupCollections(em); 
        }

        for (Collection col : noGroupItems) {
            CollectionSummaryBean csb = createCollectionSummary(col);
            noGroupCollections.add(csb);
        }  

        request.setAttribute(PAGE_COLLECTIONS, collections);
        request.setAttribute(ERROR_COLLECTIONS, errorCollections);
        request.setAttribute(NO_GROUP_COLLECTIONS, noGroupCollections);
        request.setAttribute(PAGE_STATES, ImmutableList.copyOf(CStateBean.values()));
        request.setAttribute(PAGE_COUNT, count);
        request.setAttribute(PAGE_NUMBER, pb);
        request.setAttribute("action", action);
        request.setAttribute("colGroups", collectionGroups);
        request.setAttribute("groups", GroupSummaryContext.summaries);
        if (hasJson(request)) {
            dispatcher = request.getRequestDispatcher("status-json.jsp");
        } else if (hasCsv(request)) {
            dispatcher = request.getRequestDispatcher("status-csv.jsp");
        } else {
            dispatcher = request.getRequestDispatcher("status.jsp");
        }
        dispatcher.forward(request, response);
    }

    /**
     * Build error collections query
     * @return
     */
    private List<Collection> getErrorCollections(EntityManager em) {
    	StringBuilder errorQueryBuilder = new StringBuilder();
    	errorQueryBuilder.append("SELECT c FROM Collection c");
    	errorQueryBuilder.append(" WHERE c.state IN(:" + PARAM_STATE_ERROR + ", :" + PARAM_STATE_INTERRUPTED + ")");
    	errorQueryBuilder.append(" ORDER BY c.group ASC, c.name ASC");

        TypedQuery<Collection> query =
                em.createQuery(errorQueryBuilder.toString(), Collection.class);

        query.setParameter(PARAM_STATE_ERROR, CollectionState.ERROR);
        query.setParameter(PARAM_STATE_INTERRUPTED, CollectionState.INTERRUPTED);
        
    	return query.getResultList();
    }

    private List<String> searchCollectionGroups(EntityManager em) {
    	StringBuilder queryBuilder = new StringBuilder();
    	queryBuilder.append("SELECT DISTINCT c.group FROM Collection c");
    	queryBuilder.append(" WHERE c.group IS NOT NULL AND  c.group <> '' ");
    	queryBuilder.append(" ORDER BY c.group ASC, c.name ASC");

        TypedQuery<String> query =
                em.createQuery(queryBuilder.toString(), String.class);

    	return query.getResultList();
    }

    private List<String> getCollectionGroups(List<Collection> collections) {
    	return collections.stream().filter(x -> x.getGroup() != null && x.getGroup().length() > 0).map(x -> {return x.getGroup();}).distinct().collect(Collectors.toList());
    }

    private List<Collection> getNoGroupCollections(List<Collection> collections) {
    	return collections.stream().filter(c -> c.getGroup() == null || c.getGroup().length() == 0).collect(Collectors.toList());
    }

    /**
     * 
     * @param em
     * @return
     */
    private List<Collection> searchNoGroupCollections(EntityManager em) {
    	StringBuilder queryBuilder = new StringBuilder();
    	queryBuilder.append("SELECT c FROM Collection c");
    	queryBuilder.append(" WHERE c.group IS NULL OR c.group = '' ");
    	queryBuilder.append(" ORDER BY c.name ASC");

        TypedQuery<Collection> query =
                em.createQuery(queryBuilder.toString(), Collection.class);

    	return query.getResultList();
    }
 
    private void setWorkingCollection(HttpServletRequest request, EntityManager em) {
        long collectionId;
        String idParam = request.getParameter(PARAM_COLLECTION_ID);
        collectionId = Strings.isValidLong(idParam) ? Long.parseLong(idParam) : -1;
        CollectionSummaryBean workingCollectionBean = (CollectionSummaryBean) request.getSession().getAttribute(SESSION_WORKINGCOLLECTION);

        if (Strings.isValidLong(idParam) && -1 == collectionId) {
            // clear the working collection
            request.getSession().removeAttribute(SESSION_WORKINGCOLLECTION);
            request.getSession().removeAttribute(SESSION_COLLECTION_URI);
        } else if (Strings.isValidLong(idParam)                                            // valid param
                && (workingCollectionBean == null                                          // no working collection
                || !workingCollectionBean.getCollection().getId().equals(collectionId))) { // or one which does not equal our requested collection
            // Get the requested collection from the db and set it as our working collection
            Collection workingCollection = em.find(Collection.class, collectionId);
            if (workingCollection != null) {
                workingCollectionBean = createCollectionSummary(workingCollection);
                request.getSession().setAttribute(SESSION_WORKINGCOLLECTION, workingCollectionBean);

                String collectionUri = request.getRequestURL()+"?"+request.getQueryString();
                request.getSession().setAttribute(SESSION_COLLECTION_URI, collectionUri);
            }

        } else {
            // Continue using the current working collection
            request.getSession().setAttribute(SESSION_WORKINGCOLLECTION, workingCollectionBean);
        }
    }

    private CollectionSummaryBean createCollectionSummary(Collection col) {
        CollectionSummaryBean csb = new CollectionSummaryBean();
        csb.setCollection(col);
        csb.setTotalFiles(CollectionCountContext.getFileCount(col));
        csb.setActiveFiles(CollectionCountContext.getActiveCount(col));
        csb.setCorruptFiles(CollectionCountContext.getCorruptCount(col));
        csb.setInvalidDigests(CollectionCountContext.getTokenMismatchCount(
                col));
        csb.setMissingFiles(CollectionCountContext.getMissingCount(col));
        csb.setMissingTokens(CollectionCountContext.getMissingTokenCount(col));
        csb.setTotalSize(CollectionCountContext.getTotalSize(col));
        csb.setTotalErrors(CollectionCountContext.getTotalErrors(col));
        csb.setRemoteMissing(CollectionCountContext.getRemoteMissing(col));
        csb.setRemoteCorrupt(CollectionCountContext.getRemoteCorrupt(col));
        return csb;
    }

    private boolean hasCsv(HttpServletRequest request) {
        String value = request.getParameter(PARAM_CSV);
        return !Strings.isEmpty(value);
    }

    @Override
    public String getParameter(HttpServletRequest request, String paramName, String defaultValue) {
        String requestParam = request.getParameter(paramName);

        // We could remove the session attributes here, not sure if that's a good idea though

        // req param: not null and not empty -> use
        // session attr not null and req param null -> use
        // session attr not null and req param empty -> default
        if (!Strings.isEmpty(requestParam)) {
            return requestParam;
        } else {
            return defaultValue;
        }
    }
}
