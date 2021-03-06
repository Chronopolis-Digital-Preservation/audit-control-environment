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
import edu.umiacs.ace.monitor.core.Token;
import edu.umiacs.ace.monitor.log.LogEnum;
import edu.umiacs.ace.monitor.log.LogEvent;
import edu.umiacs.ace.monitor.log.LogEventManager;
import edu.umiacs.ace.token.AceToken;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.util.TokenUtil;
import edu.umiacs.util.Strings;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveAction;

/**
 * A recursive action for fork join pools. Splits its work up to worker threads
 * until less than 7000 transactions are made.
 *
 * @author shake
 */
public class IngestThread extends RecursiveAction {
    private static final Logger LOG = Logger.getLogger(IngestThread.class);

    // These only get read from, never written to
    private Map<String, Token> tokens;
    private Collection coll;
    private List<String> identifiers;

    // Writable map for updating the state of items
    private ConcurrentMap<IngestState, ConcurrentSkipListSet<String>> states;

    private long session;
    private int numTransactions = 0;
    private LogEventManager logManager;

    // May cause problems
    private EntityManager em;

    public IngestThread(Map<String, Token> tokens,
                        Collection coll,
                        List<String> subList,
                        ConcurrentMap<IngestState, ConcurrentSkipListSet<String>> states) {
        this.coll = coll;
        this.tokens = tokens;
        this.identifiers = subList;
        this.states = states;
    }

    @Override
    protected void compute() {
        if (identifiers == null || coll == null) {
            return;
        }

        if (identifiers.size() < 2500) {
            // TODO: I still want to play around with rollbacks in case of failure
            em = PersistUtil.getEntityManager();
            EntityTransaction trans = em.getTransaction();
            trans.begin();
            try {
                run();
            } finally {
                trans.commit();
                em.close();
            }
        } else {
            int mid = identifiers.size() >>> 1;
            invokeAll(new IngestThread(tokens, coll, identifiers.subList(0, mid), states),
                    new IngestThread(tokens, coll, identifiers.subList(mid, identifiers.size()), states));
        }
    }

    public void run() {
        MonitoredItem item;
        session = System.currentTimeMillis();
        logManager = new LogEventManager(session, coll);

        // Cycle through all items read in and add/update tokens
        // Commit only if there are no errors in all transactions
        ConcurrentSkipListSet<String> queued = states.get(IngestState.QUEUED);
        for (String identifier : identifiers) {
            queued.remove(identifier);
            Token token = tokens.get(identifier);
            item = getItemByPath(identifier, coll);
            if (item == null) {
                LOG.debug("[Ingest Thread " + Thread.currentThread().getId()
                        + "] Adding new item " + identifier);
                LogEvent[] event = new LogEvent[2];
                // LOG.trace does not exist
                event[0] = logManager.createItemEvent(LogEnum.FILE_REGISTER,
                        identifier, coll.getDirectory() + identifier);
                event[1] = logManager.createItemEvent(LogEnum.ADD_TOKEN,
                        identifier, coll.getDirectory() + identifier);

                String parent;
                parent = extractParent(identifier);

                item = addItem(identifier, parent, false, coll, 'R', 0);

                token.setParentCollection(coll);

                // Token
                item.setToken(token);

                //Finish adding the item
                em.persist(event[0]);
                em.persist(event[1]);
                em.persist(item);
                numTransactions += 3;

                states.get(IngestState.NEW).add(identifier);
            } else {
                LOG.debug("[Ingest Thread " + Thread.currentThread().getId()
                        + "] Updating existing item " + identifier);
                updateToken(em, token, item, coll, identifier);
            }

            // With large Token Stores, we get a large number of transactions
            // Flushing and Clearing the EM helps to clear some memory
            if (numTransactions > 30) {
                em.flush();
                em.clear();
                numTransactions = 0;
            }
        }
    }

    // If we have a monitored item already in the database, check against the
    // new token and update if necessary
    private void updateToken(EntityManager em,
                             Token token,
                             MonitoredItem item,
                             Collection coll,
                             String identifier) {
        boolean update = false;
        Token registeredToken = item.getToken();
        if (registeredToken != null) {
            token.setParentCollection(coll);

            // TODO: Find a way to compare tokens w/o converting to AceTokens
            // Opted not to use token.equals because we want to compare the proof text
            AceToken registeredAceToken = TokenUtil.convertToAceToken(registeredToken);
            AceToken aceToken = TokenUtil.convertToAceToken(token);

            if (!registeredAceToken.getProof().equals(aceToken.getProof())) {
                update = true;
            }
        } else {
            update = true;
        }

        if (update) {
            LogEvent event = logManager.createItemEvent(LogEnum.TOKEN_INGEST_UPDATE,
                    identifier, coll.getDirectory() + identifier);
            item.setToken(token);
            // TODO: Why set 'I'? It's not necessarily invalid, maybe 'R' would be better
            //       or even better yet 'UpdatedToken'!
            item.setState('I');
            em.merge(item);
            em.persist(event);
            numTransactions += 2;

            states.get(IngestState.UPDATED).add(identifier);
        } else {
            states.get(IngestState.MATCH).add(identifier);
        }
    }


    // From MonitoredItemManager, but without any registration
    // Can probably be trimmed down
    private String extractParent(String path) {
        // We don't have a FileBean, so build the pathList ourselves
        StringBuilder fullPath = new StringBuilder(path);
        List<String> pathList = new LinkedList<>();
        int index;
        if (fullPath.charAt(0) != '/') {
            fullPath.insert(0, "/");
        }
        while ((index = fullPath.lastIndexOf("/")) != 0) {
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
        }

        return parentName;
    }

    // MIM method without transaction
    private MonitoredItem addItem(String path,
                                  String parentDir,
                                  boolean directory,
                                  Collection parentCollection,
                                  char initialState,
                                  long size) {
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
        return mi;
    }


    /**
     * From MIM but without locking
     *
     * @param path path to look for
     * @return MonitoredItem if exist, null otherwise
     */
    public MonitoredItem getItemByPath(String path, Collection c) {
        MonitoredItem tmp = new MonitoredItem();
        tmp.setPath(path);

        TypedQuery<MonitoredItem> q =
                em.createNamedQuery("MonitoredItem.getItemByPath", MonitoredItem.class);
        q.setParameter("path", path);
        q.setParameter("coll", c);

        // Make sure we have the correct item
        List<MonitoredItem> li = q.getResultList();
        li.sort(Comparator.comparing(MonitoredItem::getPath));
        int idx = Collections.binarySearch(li, tmp, Comparator.comparing(MonitoredItem::getPath));
        return (idx >= 0 ? li.get(idx) : null);
    }

}