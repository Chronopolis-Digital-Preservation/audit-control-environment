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

package edu.umiacs.ace.monitor.log;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.util.Strings;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event log querying servlet
 * @author toaster
 */
public class LogServlet extends EntityManagerServlet {

    private static final Logger LOG = Logger.getLogger(LogServlet.class);
    private static final long DEFAULT_START = 0;
    private static final int DEFAULT_COUNT = 50;
    private static final long DEFAULT_SESSION = 0;
    private static final long DEFAULT_TOP = 0;
    // possible values for parameter toggletype
    public static final String PARAM_START = "start"; // count forwards from item, inclusive
    public static final String PARAM_COUNT = "count"; // show count items on a page
    public static final String PARAM_TOP = "top"; // count backwards starting at top (start not set)
    // the next three are filters for session, collection, and event type
    public static final String PARAM_SESSION = "sessionId"; // show only events for this session (0 for all)
    public static final String PARAM_PATH = "logpath"; // show only for given path
    public static final String PARAM_COLLECTION = "collection";
    public static final String PARAM_TOGGLE = "toggletype"; // toggle filtering for specified event
    public static final String PARAM_CLEAR = "clear"; // clear session. used for external links in
    // items thrown into a page context
    public static final String PAGE_SELECTS = "selects";
    public static final String PAGE_LOGLIST = "loglist"; // list containing log messages
    public static final String PAGE_COUNT = "count"; // currently displayed count
    public static final String PAGE_COLLECTION = "collectionbean";

    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em ) throws ServletException, IOException {

        testClear(request, PARAM_CLEAR);
        long start = getParameter(request, PARAM_START, DEFAULT_START);
        int count = getParameter(request, PARAM_COUNT, DEFAULT_COUNT);
        long session = getParameter(request, PARAM_SESSION, DEFAULT_SESSION);
        long top = getParameter(request, PARAM_TOP, DEFAULT_TOP);
        long collection = getParameter(request, PARAM_COLLECTION, 0L);
        String path = getParameter(request, PARAM_PATH, null);
        //boolean reverseResults = false;

        System.out.println(
                "session" + session + " start: " + start + " count: " + count + " top: " + top
                + " path: " + path);

        Map<String, String> typeMap = parseToggles(request);

        // <editor-fold defaultstate="collapsed" desc="where clause setting. Click on the + sign on the left to edit the code.">
        List<String> queries = new ArrayList<String>();
        //StringBuilder queryString = new StringBuilder();

        if ( session > 0 ) {
            queries.add("l.session = :session");
        }

        if ( collection > 0 ) {
            queries.add("l.collection = :collection");
        }


        if ( !Strings.isEmpty(path) ) {
            queries.add("l.path = :path");
        }

        if ( typeMap.size() > 0 ) {
            queries.add(generateTypeString(typeMap));
        }
        // </editor-fold>

        // Handling scrolling results, which is expected to return page count number of results
        List<LogEvent> results = getQueryResult(request, em, new ArrayList<>(queries), session, collection, path, start, top, count);

        if (results.size() < count && (start > 0 || top > 0)) {
        	// return max page count of results
        	long first = 0;
        	long last = 0;
        	if (results.size() > 0) {
        		first = results.get(0).getId();
        		last = results.get(results.size() - 1).getId();
        	}
        	start = start > 0 ? 0 : top > 0 ? last : start;
			top = start > 0 ? first + 1 : top > 0 ? 0 : top;

			HttpSession s = request.getSession();
			s.setAttribute(PARAM_START, start);
			s.setAttribute(PARAM_TOP, top);

			results = getQueryResult(request, em, new ArrayList<>(queries), session, collection, path, start, top, count);
        }

        request.setAttribute(PAGE_LOGLIST, results);
        request.setAttribute(PAGE_COUNT, count);

        // add in collection list
//        Query query =
//                em.createNamedQuery("Collection.listAllCollections");
//        request.setAttribute(PAGE_COLLECTIONLIST,query.getResultList());
        RequestDispatcher rd;
        if ( hasJson(request) ) {
            rd = request.getRequestDispatcher("eventlog-json.jsp");
        } else {
            rd = request.getRequestDispatcher("eventlog.jsp");
        }
        rd.forward(request, response);
    }

    private List<LogEvent> getQueryResult(HttpServletRequest request,
    		EntityManager em,
    		List<String> queries,
    		long session,
    		long collection,
    		String path,
    		long start,
    		long top,
    		int count) {
    	StringBuilder queryString = new StringBuilder();

        // handle start index
        // if start is set, select greater than start
        // else if top set, select less than top
        if ( start > 0 ) {
        	queries.add("l.id >= :start");
        } else if ( top > 0 ) {
        	queries.add("l.id <= :top");
        } else {
        	queries.add("l.id >= :start");
        }

    	// build query string
        queryString.append("SELECT l FROM LogEvent l");

        if ( queries.size() > 0 ) {
            queryString.append(" WHERE");
        }

        int i = 0;
        for ( String s : queries ) {
            queryString.append(" ");
            queryString.append(s);
            i++;
            if ( i < queries.size() ) {
                queryString.append(" AND");
            }
        }

        queryString.append(" ORDER BY l.id");

        // set order descending if we have no start and we have a count
        // otherwise we have a start, or blank page w/ no parameters given
        if (start < 1 && (top > 0 || count > 0)) {
        	queryString.append(" DESC");
        }

        LOG.debug("Log query: " + queryString);
        em = PersistUtil.getEntityManager();

        // Fill in query parameters
        Query q = em.createQuery(queryString.toString());
        q.setMaxResults(count);

        // <editor-fold defaultstate="collapsed" desc="mirror from above where clause setting. Click on the + sign on the left to edit the code.">
        if ( session > 0 ) {
            q.setParameter("session", session);
        }
        if ( collection > 0 ) {
            Collection c = em.getReference(Collection.class, collection);
            q.setParameter("collection", c);
            request.setAttribute(PAGE_COLLECTION, c);
        }
        if ( !Strings.isEmpty(path) ) {
            q.setParameter("path", path);
        }
        if ( start > 0 ) {
            q.setParameter("start", start);
        } else if ( top > 0 ) {
            q.setParameter("top", top);
        } else {
            q.setParameter("start", start);
        }

        List<LogEvent> results = q.getResultList();
        if ( !(start < 1 && (top > 0 || count > 0)) ) {
        	results = new ArrayList<>(results);
            Collections.reverse(results);
        }
        return results;
    }

    private void testClear( HttpServletRequest request, String paramName ) {
        HttpSession s = request.getSession();
        if ( request.getParameter(paramName) != null ) {
            s.removeAttribute(PARAM_START);
            s.removeAttribute(PARAM_CLEAR);
            s.removeAttribute(PARAM_COLLECTION);
            s.removeAttribute(PARAM_COUNT);
            s.removeAttribute(PARAM_PATH);
            s.removeAttribute(PARAM_SESSION);
            s.removeAttribute(PAGE_SELECTS);
            s.removeAttribute(PARAM_TOP);
        }
    }

    private int getParameter( HttpServletRequest request, String paramName,
            int defaultValue ) {
        HttpSession s = request.getSession();
        if ( Strings.isValidInt(request.getParameter(paramName)) ) {
            s.setAttribute(paramName,
                    Integer.parseInt(request.getParameter(paramName)));
            return Integer.parseInt(request.getParameter(paramName));
        } else if ( s.getAttribute(paramName) != null ) {
            return (Integer) s.getAttribute(paramName);
        } else {
            // s.setAttribute(paramName, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public long getParameter( HttpServletRequest request, String paramName,
            long defaultValue ) {
        HttpSession s = request.getSession();
        if ( Strings.isValidLong(request.getParameter(paramName)) ) {
            s.setAttribute(paramName,
                    Long.parseLong(request.getParameter(paramName)));
            return Long.parseLong(request.getParameter(paramName));
        } else if ( s.getAttribute(paramName) != null ) {
            return (Long) s.getAttribute(paramName);
        } else {
            // s.setAttribute(paramName, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public String getParameter( HttpServletRequest request, String paramName,
            String defaultValue ) {
        HttpSession s = request.getSession();
        String param = request.getParameter(paramName);
        if ( request.getParameterMap().containsKey(paramName) ) {

            s.setAttribute(paramName, param);
            return request.getParameter(paramName);

        } else if ( s.getAttribute(paramName) != null ) {
            return (String) s.getAttribute(paramName);
        } else {
            s.setAttribute(paramName, defaultValue);
            return defaultValue;
        }
    }

    /**
     * If any types are selected (selectedTypes.size() > 0) then create the query
     * string for the selected types
     * 
     * @param selectedTypes map from session of selected types
     * @return
     */
    private String generateTypeString( Map<String, String> selectedTypes ) {
        StringBuilder returnString = new StringBuilder();

        if ( selectedTypes.size() > 0 ) {
            returnString = new StringBuilder(" l.logType IN ( ");
        }
        for ( String key : selectedTypes.keySet() ) {
            if ( LogEvent.CHOICE_ERRORS.equals(key) ) {
                returnString.append(LogEnum.SYSTEM_ERROR.getType()).append(",");
                returnString.append(LogEnum.SITE_UNACCESSABLE.getType()).append(",");
                returnString.append(LogEnum.LOG_TYPE_UNKNOWN.getType()).append(",");
                returnString.append(LogEnum.CREATE_TOKEN_ERROR.getType()).append(",");
                returnString.append(LogEnum.ERROR_READING.getType()).append(",");
                returnString.append(LogEnum.UNKNOWN_IMS_COMMUNICATION_ERROR.getType()).append(",");
            } else if ( LogEvent.CHOICE_MISSING.equals(key) ) {
                returnString.append(LogEnum.FILE_MISSING.getType()).append(",");
                returnString.append(LogEnum.FILE_CORRUPT.getType()).append(",");
                returnString.append(LogEnum.MISSING_TOKEN.getType()).append(",");
            } else if ( LogEvent.CHOICE_NEWMASTER.equals(key) ) {
                returnString.append(LogEnum.FILE_NEW.getType()).append(",");
                returnString.append(LogEnum.ADD_TOKEN.getType()).append(",");
                returnString.append(LogEnum.FILE_ONLINE.getType()).append(",");
                returnString.append(LogEnum.FILE_REGISTER.getType()).append(",");
            } else if ( LogEvent.CHOICE_SYNC.equals(key) ) {
                returnString.append(LogEnum.FILE_AUDIT_FINISH.getType()).append(",");
                returnString.append(LogEnum.FILE_AUDIT_START.getType()).append(",");
                returnString.append(LogEnum.FILE_AUDIT_CANCEL.getType()).append(",");
                returnString.append(LogEnum.FILE_AUDIT_ABORT.getType()).append(",");
            } else if ( LogEvent.CHOICE_REMOVALS.equals(key) ) {
                returnString.append(LogEnum.COLLECTION_REMOVED.getType()).append(",");
            }
        }

        if ( returnString.length() > 0 ) {
            // remove last ','
            returnString = new StringBuilder(returnString.substring(0, returnString.length() - 1));
            returnString.append(")");
        }

        return returnString.toString();
    }

    /**
     * Return a map containing selected log types as keys and "checked" as the
     * value. To be used to fill in an <input> statement 
     * 
     * @param request request to parse
     * @return map of selected log types
     */
    private Map<String, String> parseToggles( HttpServletRequest request ) {
        String toggleType = request.getParameter(PARAM_TOGGLE);


        HttpSession s = request.getSession();
        if ( s.getAttribute(PAGE_SELECTS) == null ) {
            s.setAttribute(PAGE_SELECTS, new HashMap<String, String>());
        }

        HashMap<String, String> map = (HashMap<String, String>) s.getAttribute(
                PAGE_SELECTS);


        if ( Strings.isEmpty(toggleType) ) {
            return map;
        }

        if ( LogEvent.CHOICE_ERRORS.equals(toggleType) ) {
            if ( map.containsKey(LogEvent.CHOICE_ERRORS) ) {
                map.remove(LogEvent.CHOICE_ERRORS);
            } else {
                map.put(LogEvent.CHOICE_ERRORS, "checked");
            }
        } else if ( LogEvent.CHOICE_MISSING.equals(toggleType) ) {
            if ( map.containsKey(LogEvent.CHOICE_MISSING) ) {
                map.remove(LogEvent.CHOICE_MISSING);
            } else {
                map.put(LogEvent.CHOICE_MISSING, "checked");
            }
        } else if ( LogEvent.CHOICE_NEWMASTER.equals(toggleType) ) {
            if ( map.containsKey(LogEvent.CHOICE_NEWMASTER) ) {
                map.remove(LogEvent.CHOICE_NEWMASTER);
            } else {
                map.put(LogEvent.CHOICE_NEWMASTER, "checked");
            }
        } //        else if ( CHOICE_NEWREPLICA.equals(toggleType) )
        //        {
        //            if ( map.containsKey(CHOICE_NEWREPLICA) )
        //            {
        //                map.remove(CHOICE_NEWREPLICA);
        //            }
        //            else
        //            {
        //                map.put(CHOICE_NEWREPLICA, "checked");
        //            }
        //        }
        else if ( LogEvent.CHOICE_SYNC.equals(toggleType) ) {
            if ( map.containsKey(LogEvent.CHOICE_SYNC) ) {
                map.remove(LogEvent.CHOICE_SYNC);
            } else {
                map.put(LogEvent.CHOICE_SYNC, "checked");
            }
        }
        else if ( LogEvent.CHOICE_REMOVALS.equals(toggleType) ) {
            if ( map.containsKey(LogEvent.CHOICE_REMOVALS) ) {
                map.remove(LogEvent.CHOICE_REMOVALS);
            } else {
                map.put(LogEvent.CHOICE_REMOVALS, "checked");
            }
        }
        return map;
    }
}
