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

import edu.umiacs.ace.monitor.log.LogEnum;
import edu.umiacs.ace.monitor.log.LogEventManager;
import edu.umiacs.ace.util.PersistUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author toaster
 */
public class MonitoredItemManager {

    private static Lock lock = new ReentrantLock();
    private EntityManager em;
    // TODO: This may leak...
    private Set<String> existingParents = new HashSet<>();

    public MonitoredItemManager(EntityManager em) {
        this.em = em;
    }

    public void createDirectory(String directory, String root, Collection c) {
        MonitoredItem mi;
        if (existingParents.contains(directory) || directory == null) {
            return;
        }
        if ((mi = getItemByPath(directory, c)) != null) {
            EntityTransaction trans = em.getTransaction();
            trans.begin();
            Date d = new Date();
            mi.setLastSeen(d);
            mi.setLastVisited(d);
            mi.setState('A');
            em.merge(mi);
            trans.commit();
            existingParents.add(directory);
        } else {
            addItem(directory, root, true, c, 'A', 0);
            existingParents.add(directory);
        }
    }

    /**
     * @param path path to look for
     * @return MonitoredItem if exist, null otherwise
     */
    public MonitoredItem getItemByPath(String path, Collection c) {
        lock.lock();
        try {
            MonitoredItem tmp = new MonitoredItem();
            tmp.setPath(path);

            TypedQuery<MonitoredItem> q =
                    em.createNamedQuery("MonitoredItem.getItemByPath", MonitoredItem.class);
            q.setParameter("path", path);
            q.setParameter("coll", c);

            // Make sure we have the correct item
            List<MonitoredItem> li = q.getResultList();
            li.sort(new PathComparator());
            int idx = Collections.binarySearch(li, tmp, new PathComparator());
            return (idx >= 0 ? li.get(idx) : null);
        } finally {
            lock.unlock();
        }
    }

    /*
     *
     * @param id to get
     * @return the monitored item
     */
    public MonitoredItem getItemById(String id, Collection c) {
        EntityManager pem = PersistUtil.getEntityManager();
        return pem.find(MonitoredItem.class, Long.parseLong(id));
    }

    /**
     * Set the state on all items that contain no tokens to false
     *
     * @param c
     */
    public void setMissingTokensInvalid(Collection c, long session) {
        Query q = em.createNamedQuery("MonitoredItem.listNullTokens");
        q.setParameter("coll", c);

        LogEventManager lem = new LogEventManager(session, c);

        for (Object o : q.getResultList()) {
            MonitoredItem mi = (MonitoredItem) o;
            mi.setState('T');
            EntityTransaction et = em.getTransaction();
            et.begin();
            em.merge(mi);
            lem.createItemEvent(LogEnum.MISSING_TOKEN, mi.getPath());
            et.commit();
        }
    }

    /**
     * Create new master item, master item has no replicas.
     *
     * @param path             relative path to this item (w/o collection prefix)
     * @param parentDir        relative parent path to this item, null if parent is collection
     * @param directory        true if this is a directory
     * @param parentCollection collection this item belongs to
     * @param initialState     the initial state to set
     * @param size             the size to set
     * @return
     */
    public MonitoredItem addItem(String path,
                                 String parentDir,
                                 boolean directory,
                                 Collection parentCollection,
                                 char initialState,
                                 long size) {
        lock.lock();
        try {
            MonitoredItem mi = new MonitoredItem();
            mi.setDirectory(directory);
            mi.setLastSeen(new Date());
            mi.setLastVisited(new Date());
            mi.setStateChange(new Date());
            mi.setParentCollection(parentCollection);
            mi.setParentPath(parentDir);
            mi.setPath(path);
            mi.setState(initialState);
            mi.setSize(size);

            EntityTransaction trans = em.getTransaction();
            trans.begin();
            em.persist(mi);
            trans.commit();

            return mi;
        } finally {
            lock.unlock();
        }
    }

    public List<MonitoredItem> getCollectionRoots(Collection parent) {
        lock.lock();
        try {
            TypedQuery<MonitoredItem> q =
                    em.createNamedQuery("MonitoredItem.listRoots", MonitoredItem.class);
            q.setParameter("coll", parent);
            return q.getResultList();
        } finally {
            lock.unlock();
        }
    }

    /**
     * List all files that are in state P or D
     *
     * @param coll
     * @return
     */
    public List<MonitoredItem> listRemoteErrors(Collection coll) {
        lock.lock();
        try {
            TypedQuery<MonitoredItem> q =
                    em.createNamedQuery("MonitoredItem.listRemoteErrors", MonitoredItem.class);
            q.setParameter("coll", coll);
            return q.getResultList();
        } finally {
            lock.unlock();
        }
    }

    public List<MonitoredItem> listChildren(Collection coll, String parentPath) {
        lock.lock();
        try {
            TypedQuery<MonitoredItem> q =
                    em.createNamedQuery("MonitoredItem.listChildren", MonitoredItem.class);
            q.setParameter("coll", coll);
            q.setParameter("parent", parentPath);
            return q.getResultList();
        } finally {
            lock.unlock();
        }
    }

    public Long countErrorsInCollection(Collection c) {
        Query q = em.createNamedQuery("MonitoredItem.countErrorsInCollection");
        q.setParameter("coll", c);
        return (Long) q.getSingleResult();
    }

    // To compare MonitoredItems based on paths
    private class PathComparator implements Comparator<MonitoredItem> {
        public int compare(MonitoredItem m1, MonitoredItem m2) {
            return m1.getPath().compareTo(m2.getPath());
        }
    }
}
