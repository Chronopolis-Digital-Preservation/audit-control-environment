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
package edu.umiacs.ace.monitor.audit;

import edu.umiacs.ace.exception.StatusCode;
import edu.umiacs.ace.ims.api.RequestBatchCallback;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.MonitoredItemManager;
import edu.umiacs.ace.monitor.core.Token;
import edu.umiacs.ace.monitor.log.LogEventManager;
import edu.umiacs.ace.ims.ws.TokenRequest;
import edu.umiacs.ace.ims.ws.TokenResponse;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.log.LogEnum;
import edu.umiacs.util.Strings;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public class FileAuditCallback implements RequestBatchCallback {

    private long tokensAdded = 0;
//    private long session = 0;
    private Collection coll;
    private static final Logger LOG = Logger.getLogger(FileAuditCallback.class);
    private long callbackErrors = 0;
    private CancelCallback callback;
    private LogEventManager logManager;

    public FileAuditCallback( Collection coll, long session, CancelCallback callback ) {
        this.coll = coll;
//        this.session = session;
        this.callback = callback;
        logManager = new LogEventManager(session, coll);
    }

    public long getTokensAdded() {
        return tokensAdded;
    }

    public long getCallbackErrors() {
        return callbackErrors;
    }

    @Override
    public void tokensReceived( List<TokenRequest> requests,
            List<TokenResponse> responses ) {
        EntityManager em = PersistUtil.getEntityManager();
        MonitoredItemManager mim = new MonitoredItemManager(em);
//        LogEventManager lem = new LogEventManager( session, coll);
        Map<String, String> map = new HashMap<String, String>();
        for ( TokenRequest tr : requests ) {
            map.put(tr.getName(), tr.getHashValue());
        }

        EntityTransaction trans = em.getTransaction();
        trans.begin();
        for ( TokenResponse tr : responses ) {
            tokensAdded++;
            if ( tr.getStatusCode() == StatusCode.SUCCESS ) {
                MonitoredItem item = null;
                item = mim.getItemByPath(tr.getName(), coll);

                Token token = new Token();
                token.setCreateDate(new Date());
                token.setValid(true);
                token.setLastValidated(new Date());
                token.setToken((Serializable) tr);
                token.setParentCollection(coll);
                if ( !map.containsKey(tr.getName()) || item == null ) {
                    LOG.error("No request for response: " + tr.getName() + " or item null, item: "
                            + item);
                }


                em.persist(token);
                item.setFileDigest(map.get(tr.getName()));
                item.setToken(token);
                item.setState('A');
                em.merge(item);
                em.persist(logManager.createItemEvent(LogEnum.ADD_TOKEN, tr.getName(),
                        "Round: " + tr.getRoundId()));
            } else {
                em.persist(logManager.createItemEvent(LogEnum.CREATE_TOKEN_ERROR,  tr.getName(),
                        "Code: " + tr.getStatusCode()));
                callbackErrors++;
            }
        }
        trans.commit();
        em.close();
    }

    @Override
    public void exceptionThrown( List<TokenRequest> requests,
            Throwable throwable ) {
        LOG.error("Exception throw registering", throwable);
        EntityManager em = PersistUtil.getEntityManager();
        MonitoredItemManager mim = new MonitoredItemManager(em);
        EntityTransaction trans = em.getTransaction();
        trans.begin();
        callbackErrors++;
        for ( TokenRequest tr : requests ) {

            LOG.debug(" Error item: " + tr.getName());

            em.persist(logManager.createItemEvent(LogEnum.UNKNOWN_IMS_COMMUNICATION_ERROR, tr.getName(), throwable));

            MonitoredItem item = null;
            item = mim.getItemByPath(tr.getName(), coll);
            if ( item == null ) {
                continue;
            }
            item.setState('M');


            em.merge(item);

        }
        trans.commit();
        em.close();
    }

    @Override
    public void unexpectedException( Throwable throwable ) {
        callbackErrors++;
        LOG.error("Unexpected Exception thrown during registration", throwable);
        
        EntityManager em = PersistUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        trans.begin();
        String msg = "Exception in batch thread" + Strings.exceptionAsString(throwable);
        logManager.persistCollectionEvent(LogEnum.SYSTEM_ERROR,msg,em);
        trans.commit();
        em.close();
        callback.cancel();
    }
}
