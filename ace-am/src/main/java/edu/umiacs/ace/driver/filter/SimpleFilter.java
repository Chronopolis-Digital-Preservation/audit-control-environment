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

import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.monitor.core.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public class SimpleFilter implements PathFilter {

    private Collection coll;
    private List<Matcher> dirRegex = null;
    private List<Matcher> fileRegex = null;
    private static final Logger LOG = Logger.getLogger(SimpleFilter.class);

    public SimpleFilter( Collection coll ) {
        this.coll = coll;

        dirRegex = new ArrayList<Matcher>();
        fileRegex = new ArrayList<Matcher>();

        EntityManager em = PersistUtil.getEntityManager();
        try {
            Query q = em.createNamedQuery("FilterEntry.listByCollection");
            q.setParameter("coll", coll);

            List l = q.getResultList();
            if ( l != null && l.size() > 0 ) {
                for ( Object o : l ) {
                    FilterEntry fe = (FilterEntry) o;
                    if ( fe.getAffectedItem() == FilterEntry.ITEM_ALL || fe.getAffectedItem()
                            == FilterEntry.ITEM_FILE ) {
                        LOG.trace("Adding file pattern " + fe.getRegex());
                        fileRegex.add(Pattern.compile(fe.getRegex()).matcher(
                                ""));
                    }

                    if ( fe.getAffectedItem() == FilterEntry.ITEM_ALL || fe.getAffectedItem()
                            == FilterEntry.ITEM_DIRECTORY ) {
                        LOG.trace("Adding directory pattern " + fe.getRegex());
                        dirRegex.add(Pattern.compile(fe.getRegex()).matcher(
                                ""));
                    }
                }
            }

            if ( fileRegex.isEmpty() ) {
                fileRegex = null;
            }
            if ( dirRegex.isEmpty() ) {
                dirRegex = null;
            }
        } finally {
            em.close();
        }
    }

    @Override
    public boolean process( String[] pathList, boolean isDirectory ) {
        String path;
        if ( pathList == null ) {
            throw new IllegalArgumentException("pathList is null");
        }

        path = (pathList.length > 1 ? pathList[0].substring(pathList[1].length() + 1)
                : pathList[0].substring(1));
//        LOG.trace("orig " + pathList[0] + " pruned " + path);
        if ( !isDirectory && (fileRegex == null || checkList(fileRegex, path)) ) {
            return true;
        }

        if ( isDirectory && (dirRegex == null || checkList(dirRegex, path)) ) {
            return true;
        }
        return false;
    }

    private boolean checkList( List<Matcher> regexList, String fileName ) {
        for ( Matcher m : regexList ) {
//            LOG.trace("Checking file " + fileName + " against " + m.pattern());
            m.reset(fileName);
            if ( m.matches() ) {
                return false;
            }
        }
        return true;

    }
}
