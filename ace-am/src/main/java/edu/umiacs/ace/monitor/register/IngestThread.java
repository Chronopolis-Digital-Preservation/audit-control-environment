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
import edu.umiacs.ace.monitor.core.MonitoredItemManager;
import edu.umiacs.ace.monitor.core.Token;
import edu.umiacs.ace.monitor.log.LogEnum;
import edu.umiacs.ace.monitor.log.LogEvent;
import edu.umiacs.ace.monitor.log.LogEventManager;
import edu.umiacs.ace.token.AceToken;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.util.TokenUtil;
import edu.umiacs.util.Strings;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Worker thread spawned from IngestStore. Probably not thread safe.
 * But who needs locks anyways?
 *
 * @author shake
 */
public class IngestThread extends Thread {
    // These only gets read from, never written to
    private Map<String, Token> tokens;
    private Collection coll;
    private List<String> identifiers;

    // Unique to each IngestThread
    private boolean running = true;
    private long session;
    private LogEventManager logManager;
    private Set<String> updatedTokens;
    private Set<String> newTokens;
    private Set<String> unchangedTokens;
    private int numTransactions = 0;

    // May cause problems
    private EntityManager em = PersistUtil.getEntityManager();

    IngestThread(Map<String, Token> tokens, Collection coll, List<String> subList) {
        this.tokens = tokens;
        this.coll = coll;
        this.identifiers = subList;
        updatedTokens = new HashSet<String>();
        newTokens = new HashSet<String>();
        unchangedTokens = new HashSet<String>();
    }

    private void finished() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    // This was for the jsp page, but has since been moved to ITF
    // Saving in case things with that get fubar'd
    public String getStatus() {
        if ( tokens.isEmpty() ) {
            return "Set of tokens to add is empty";
        }
        DecimalFormat format = new DecimalFormat("#.##");
        double percent =
                100*((updatedTokens.size() + newTokens.size() +
                unchangedTokens.size())/(double)tokens.size());
        return "Ingested " + format.format(percent) + "% of tokens";
    }

    public Set<String> getUpdatedTokens() {
        return updatedTokens;
    }

    public Set<String> getNewTokens() {
        return newTokens;
    }

    public int getUpdatedTokensSize() {
        return updatedTokens.size();
    }

    public int getNewTokensSize() {
        return newTokens.size();
    }

    public int getUnchangedSize() {
        return unchangedTokens.size();
    }

    @Override
    public void run() {
        MonitoredItemManager mim = new MonitoredItemManager(em);
        MonitoredItem item = null;
        session = System.currentTimeMillis();
        logManager = new LogEventManager(session, coll);

        // Cycle through all items read in and add/update tokens
        // Commit only if there are no errors in all transactions
        EntityTransaction trans = em.getTransaction();
        trans.begin();
        try{
            for(String identifier: identifiers) {
                Token token = tokens.get(identifier);
                item = mim.getItemByPath(identifier, coll);
                if ( item == null ) {
                    LogEvent[] event = new LogEvent[2];
                    // LOG.trace does not exist
                    event[0] = logManager.createItemEvent(LogEnum.FILE_REGISTER,
                            identifier, coll.getDirectory() + identifier);
                    event[1] = logManager.createItemEvent(LogEnum.ADD_TOKEN, identifier,
                            coll.getDirectory() + identifier);

                    String parent = null;
                    parent = extractParent(mim, identifier, coll);

                    item = addItem(identifier, parent, false, coll, 'R', 0);

                    token.setParentCollection(coll);

                    // Token 
                    em.persist(event[1]);
                    em.persist(token);
                    item.setToken(token);

                    //Finish adding the item
                    em.persist(event[0]);
                    em.merge(item);
                    numTransactions += 4;

                    newTokens.add(identifier);
                }else{
                    updateToken(em, token, item, coll, identifier);
                }

                // With large Token Stores, we get a large number of transactions
                // Flusing and Clearing the EM helps to clear some memory
                if ( numTransactions > 7000 ) {
                    em.flush();
                    em.clear();
                    numTransactions = 0;
                }
            }
        }finally{
            trans.commit();
            em.close();
            trans = null;
            em = null;
            IngestThreadFactory.release();
            finished();
        }
    }

    // If we have a monitored item already in the database, check against the
    // new token and update if necessary
    private void updateToken(EntityManager em, Token token, MonitoredItem item,
            Collection coll, String identifier) {
        boolean update = false;
        Token registeredToken = item.getToken();
        if ( registeredToken != null ) {
            token.setParentCollection(coll);

            // TODO: Find a way to compare tokens w/o converting to AceTokens
            // Opted not to use token.equals because we want to compare the
            // proof text
            AceToken registeredAceToken = TokenUtil.convertToAceToken(registeredToken);
            AceToken aceToken = TokenUtil.convertToAceToken(token);

            if ( !registeredAceToken.getProof().equals(aceToken.getProof()) ) {
                update = true;
            }
        }else{
            update = true;
        }


        if ( update ) {
            LogEvent event = logManager.createItemEvent(LogEnum.TOKEN_INGEST_UPDATE,
                    identifier, coll.getDirectory() + identifier);
            em.persist(token);
            item.setToken(token);
            item.setState('I');
            em.merge(item);
            em.persist(event);
            numTransactions += 3;
            updatedTokens.add(identifier);
        }else{
            unchangedTokens.add(identifier);
        }
    }


    // From MonitoredItemManager, but without any registration
    // Can probably be trimmed down
    private String extractParent(MonitoredItemManager mim,
            String path, Collection coll) {
        // We don't have a FileBean, so build the pathList ourselves
        StringBuilder fullPath = new StringBuilder(path);
        List <String> pathList = new LinkedList<String>();
        int index = 0;
        while( (index = fullPath.lastIndexOf("/")) != 0 ) {
            //System.out.println(fullPath);
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
    public MonitoredItem addItem( String path, String parentDir,boolean directory,
            Collection parentCollection, char initialState, long size ) {
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

        em.persist(mi);
        numTransactions++;
        return mi;
    }


}
