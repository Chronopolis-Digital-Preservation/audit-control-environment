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

package edu.umiacs.ace.monitor.register;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.util.Strings;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * Class to register directories from a token store
 *
 * @author shake
 */
public class IngestDirectory extends Thread{
    private Collection coll;
    private Set<String> identifiers;
    private Set<String> existingParents = new HashSet<String>();
    private EntityManager em = PersistUtil.getEntityManager();
    private int numTransactions = 0;

    public IngestDirectory(Set<String> identifiers , Collection coll){
        this.identifiers = identifiers;
        this.coll = coll;
    }

    @Override
    public void run() {
        if ( identifiers == null || coll == null ) {
            return;
        }

        EntityTransaction trans = em.getTransaction();
        trans.begin();
        for ( String identifier : identifiers ) {
            extractAndRegisterParentDirs(identifier);
        }
        trans.commit();

    }

    private void extractAndRegisterParentDirs(String path) {
        // We don't have a FileBean, so build the pathList ourselves
        StringBuilder fullPath = new StringBuilder(path);
        List <String> pathList = new LinkedList<String>();
        int index = 0;
        while( (index = fullPath.lastIndexOf("/")) != 0 ) {
            pathList.add(fullPath.toString());
            fullPath.delete(index, fullPath.length());
        }
        pathList.add(fullPath.toString());

        // Same as AuditThread, but with our pathList
        String parentName = (pathList.size() > 1
                ? pathList.get(1) : null);

        // 1. make sure directory path is registered
        if (parentName != null) {
            parentName = Strings.cleanStringForXml(parentName, '_');
            for ( int i = 1; i < pathList.size(); i++) {
                String parent = (pathList.size() > i + 1 ? pathList.get(i+1) : null);
                parent = Strings.cleanStringForXml(parent, '_');
                createDirectory(pathList.get(i), parent);

                if ( numTransactions > 10000 ) {
                    em.flush();
                    em.clear();
                }
            }
        }
    }

    private void createDirectory(String directory, String root) {
        MonitoredItem mi;
        if ( existingParents.contains(directory) || directory == null ) {
            return;
        }
        if ( (mi = getItemByPath(directory)) != null ) {
            Date d = new Date();
            mi.setLastSeen(d);
            mi.setLastVisited(d);
            mi.setState('A');
            em.merge(mi);
            numTransactions++;
            existingParents.add(directory);
        } else {
            addItem(directory, root, true, 'A', 0);
        }
    }


    public MonitoredItem getItemByPath( String path ) {
        Query q = em.createNamedQuery("MonitoredItem.getItemByPath");
        q.setParameter("path", path);
        q.setParameter("coll", coll);
        try {
            return (MonitoredItem) q.getSingleResult();
        } catch ( NoResultException ex ) {
            return null;
        }

    }

    public MonitoredItem addItem( String path, String parentDir,boolean directory,
            char initialState, long size ) {

        MonitoredItem mi = new MonitoredItem();
        mi.setDirectory(directory);
        mi.setLastSeen(new Date());
        mi.setLastVisited(new Date());
        mi.setStateChange(new Date());
        mi.setParentCollection(coll);
        mi.setParentPath(parentDir);
        mi.setPath(path);
        mi.setState(initialState);
        mi.setSize(size);
        em.persist(mi);
        numTransactions++;

        return mi;
    }

}
