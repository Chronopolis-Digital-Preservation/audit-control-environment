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
// $Id: FilterServlet.java 3181 2010-06-16 20:46:16Z toaster $

package edu.umiacs.ace.driver.filter;

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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

    public static final String PARAM_TEST = "teststring";
    public static final String PARAM_MODIFY = "modify";
    public static final String PARAM_REGEX_PREFIX = "regex";
    public static final String PARAM_TYPE_PREFIX = "type";
    public static final String PAGE_REGEX_LIST = "regexlist";
    public static final String PAGE_COLLECTION = "collection";
    public static final String PAGE_NEXT = "next";
    public static final String PAGE_ERROR = "errors";
    public static final String PAGE_TEST = "teststring";
    private static final Logger LOG = Logger.getLogger(FilterServlet.class);

    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em ) throws ServletException, IOException {
        RequestDispatcher dispatcher;
        Collection c;
        String testString;
        ErrorList<MyFilterEntry> regexResults;
        if ( (c = getCollection(request, em)) == null ) {
            throw new ServletException("No valid collection specified");
        }

        testString = request.getParameter(PARAM_TEST);

        regexResults = getRegexList(request, testString);

        if ( regexResults != null ) {


            if ( !regexResults.isError() ) {
                EntityTransaction et = em.getTransaction();
                et.begin();
                try {
                    // drop existing regex'
                    Query q = em.createNamedQuery("FilterEntry.dropByCollection");
                    q.setParameter("coll", c);
                    q.executeUpdate();

                    // add new regex
                    for ( FilterEntry fe : regexResults ) {
                        fe.setCollection(c);
                        em.persist(fe);
                    }
                    et.commit();

                } catch ( Exception e ) {
                    et.rollback();
                    LOG.error("Error saving regex list ", e);
                }
            }
        } else // just loading page
        {
            Query q = em.createNamedQuery("FilterEntry.listByCollection");
            q.setParameter("coll", c);
            regexResults = new ErrorList();
            for ( Object o : q.getResultList() ) {
                regexResults.add(new MyFilterEntry((FilterEntry) o));
            }


        }
        if ( !Strings.isEmpty(testString) ) {
            request.setAttribute(PAGE_TEST, testString);
        }
        request.setAttribute(PAGE_NEXT,
                (regexResults != null ? regexResults.size() : 1));
        request.setAttribute(PAGE_REGEX_LIST, regexResults);
        request.setAttribute(PAGE_ERROR, regexResults.isError());
        request.setAttribute(PAGE_COLLECTION, c);
        dispatcher = request.getRequestDispatcher("managefilters.jsp");
        dispatcher.forward(request, response);
    }

    private ErrorList<MyFilterEntry> getRegexList( HttpServletRequest request,
            String testString ) {

        ErrorList<MyFilterEntry> retList = new ErrorList<MyFilterEntry>();
        Map paramMap = request.getParameterMap();

        if ( !Strings.isEmpty(paramMap.get(PARAM_MODIFY)) ) {
            for ( Object param : paramMap.keySet() ) {
                String name = (String) param;
                if ( name.startsWith(PARAM_REGEX_PREFIX) && !Strings.isEmpty(paramMap.get(
                        param)) ) {
                    String[] regex = (String[]) paramMap.get(param);
                    String count = name.substring(PARAM_REGEX_PREFIX.length());
                    int type = Integer.parseInt(((String[]) paramMap.get(
                            PARAM_TYPE_PREFIX + count))[0]);
                    MyFilterEntry fe = new MyFilterEntry();
                    fe.setRegex(regex[0]);
                    fe.setAffectedItem(type);
                    if ( !testEntry(fe, testString) ) {
                        retList.error = true;
                    }

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

    private boolean testEntry( MyFilterEntry fe, String testString ) {
        try {
            Pattern p = Pattern.compile(fe.getRegex());
            if ( !Strings.isEmpty(testString) ) {
                fe.setMatchesTest(p.matcher(testString).matches());
            }
            return true;
        } catch ( PatternSyntaxException e ) {
            fe.setErrorMessage(e.getMessage() + " " + e.getDescription());
            LOG.debug("Bad user supplied pattern: ", e);
            return false;
        }
    }

    public class ErrorList<T> extends ArrayList<T> {

        private boolean error = false;

        public ErrorList() {
        }

        public ErrorList( List l ) {
            super(l);
        }

        public boolean isError() {
            return error;
        }
    }

    public class MyFilterEntry extends FilterEntry {

        private String error = null;
        private boolean matchesTest = false;

        public MyFilterEntry() {
        }

        public MyFilterEntry( FilterEntry fe ) {
            super.setId(fe.getId());
            super.setAffectedItem(fe.getAffectedItem());
            super.setRegex(fe.getRegex());
        }

        public boolean isError() {
            return error != null;
        }

        public String getErrorMessage() {
            return error;
        }

        public void setErrorMessage( String error ) {
            this.error = error;
        }

        public boolean isMatchesTest() {
            return matchesTest;
        }

        public void setMatchesTest( boolean matchesTest ) {
            this.matchesTest = matchesTest;
        }
    }
}
