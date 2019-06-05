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

import edu.umiacs.ace.ims.api.ValidationCallback;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Token;
import edu.umiacs.ace.monitor.log.LogEnum;
import edu.umiacs.ace.monitor.log.LogEventManager;
import edu.umiacs.ace.token.AceToken;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.util.Strings;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author toaster
 */
public final class TokenAuditCallback implements ValidationCallback {

    private static final Logger LOG = Logger.getLogger(ValidationCallback.class);
    private Map<AceToken, MonitoredItem> itemMap;
    private long totalErrors = 0;
    private long validTokens = 0;
    private CancelCallback cancel;
    private LogEventManager logManager;

    public TokenAuditCallback(Map<AceToken, MonitoredItem> itemMap,
                              CancelCallback callback,
                              Collection collection,
                              long session ) {
        this.itemMap = itemMap;
        this.cancel = callback;
        logManager = new LogEventManager(session, collection);
    }

    public long getTotalErrors() {
        return totalErrors;
    }

    public long getValidTokens() {
        return validTokens;
    }

    @Override
    public void unexpectedException( Throwable throwable ) {
        totalErrors++;
        LOG.error("Exception throw registering", throwable);
        EntityManager em = PersistUtil.getEntityManager();
        String msg = "Exception in batch thread" + Strings.exceptionAsString(throwable);
        logManager.persistCollectionEvent(LogEnum.SYSTEM_ERROR, msg, em);
        em.close();
        cancel.cancel();
    }

    @Override
    public void validToken( AceToken response ) {
        MonitoredItem item = itemMap.get(response);
        if ( item == null ) {
            return;
        }
        Token token = item.getToken();
        itemMap.remove(response);

        EntityManager em = PersistUtil.getEntityManager();

        token.setLastValidated(new Date());

        if ( !token.getValid() ) {
            token.setValid(true);
            em.persist(logManager.createItemEvent(LogEnum.TOKEN_VALID, item.getPath()));
        }

        if ( item.getState() == 'I' || item.getState() == 'R' ) {
            item.setState('A');
            item.setStateChange(new Date());
        }

        validTokens++;
        EntityTransaction trans = em.getTransaction();
        trans.begin();
        em.merge(token);
        em.merge(item);
        trans.commit();
        em.close();
        LOG.trace("Token valid: " + item.getPath());
    }

    @Override
    public void invalidToken( AceToken response, String correctCSI,
            String calculatedCSI ) {

        MonitoredItem item = itemMap.get(response);
        if ( item == null ) {
            return;
        }
        Token token = item.getToken();
        itemMap.remove(response);

        EntityManager em = PersistUtil.getEntityManager();

        if ( token.getValid() ) {
            token.setValid(false);

            String message = "Generated CSI: " + calculatedCSI + " IMS (correct) CSI: " + correctCSI;
            em.persist(logManager.createItemEvent(LogEnum.TOKEN_INVALID, item.getPath(), message));
        }

        if ( item.getState() == 'A' ) {
            item.setState('I');
            item.setStateChange(new Date());
        } else if (item.getState() == 'R') {
            //invalid digest
            item.setState('C');
            item.setStateChange(new Date());
        }

        validTokens++;
        EntityTransaction trans = em.getTransaction();
        trans.begin();
        em.merge(token);
        em.merge(item);
        trans.commit();
        em.close();

        totalErrors++;
        LOG.trace("Token invalid: " + item.getPath());
    }
}
