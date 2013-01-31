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

package edu.umiacs.ace.driver.filter;

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * regex = regular expression of filter
 * type = int 1 file, 2 directories, 3 both
 * teststring = string to test filter regex against (optional)
 * @author toaster
 */
public class FilterServlet extends EntityManagerServlet {

    public static final String PARAM_MODIFY = "modify";
    public static final String PARAM_REGEX_PREFIX = "regex-";
    public static final String PARAM_AFFECTED_PREFIX = "affected-";
    public static final String PAGE_REGEX_LIST = "regexlist";
    public static final String PAGE_COLLECTION = "collection";
    private static final Logger LOG = Logger.getLogger(FilterServlet.class);

    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em ) throws ServletException, IOException {
        RequestDispatcher dispatcher;
        Collection c;
        ErrorList<FilterEntry> regexResults = null;
        if ( (c = getCollection(request, em)) == null ) {
            throw new ServletException("No valid collection specified");
        }

        regexResults = getRegexList(request);

        if ( regexResults != null ) {
            em = PersistUtil.getEntityManager();
            EntityTransaction trans = em.getTransaction();
            trans.begin();
            Query q = em.createNamedQuery("FilterEntry.dropByCollection");
            q.setParameter("coll", c);
            q.executeUpdate();
            for (FilterEntry fe : regexResults ) {
                System.out.println(fe.getRegex() + " :: "  + fe.getAffectedItem() + " :: " + fe.getCollection());
                fe.setCollection(c);
                em.persist(fe);
            }
            trans.commit();
        } else { // just loading page
            Query q = em.createNamedQuery("FilterEntry.listByCollection");
            q.setParameter("coll", c);
            regexResults = new ErrorList();
            for ( Object o : q.getResultList() ) {
                regexResults.add((FilterEntry) o);
            }
        }

        request.setAttribute(PAGE_REGEX_LIST, regexResults);
        request.setAttribute(PAGE_COLLECTION, c);
        dispatcher = request.getRequestDispatcher("managefilters.jsp");
        dispatcher.forward(request, response);
    }

    private ErrorList<FilterEntry> getRegexList( HttpServletRequest request) {
        ErrorList<FilterEntry> retList = new ErrorList<FilterEntry>();
        Map paramMap = request.getParameterMap();

        if ( !Strings.isEmpty(paramMap.get(PARAM_MODIFY)) ) {
            for ( Object o : paramMap.entrySet() ) {
                Map.Entry entry = (Map.Entry) o;
                String name = (String) entry.getKey();
                if ( name.startsWith(PARAM_REGEX_PREFIX) && !Strings.isEmpty(entry.getValue()) ) {
                    String[] regex = (String[]) entry.getValue();
                    String count = name.substring(PARAM_REGEX_PREFIX.length());
                    int type = Integer.parseInt(((String[]) paramMap.get(
                            PARAM_AFFECTED_PREFIX + count))[0]);
                    FilterEntry fe = new FilterEntry();
                    fe.setRegex(regex[0]);
                    fe.setAffectedItem(type);

                    if ( !Strings.isEmpty(fe.getRegex()) ) {
                        retList.add(fe);
                        LOG.trace("Adding filter: " + fe + " index " + count);
                    }
                }
            }

            return retList;
        } else {
            return null;
        }
    }

    public static class ErrorList<T> extends ArrayList<T> {

        private boolean error = false;

        private ErrorList() {
        }

        public ErrorList( List l ) {
            super(l);
        }

        public boolean isError() {
            return error;
        }
    }

}
