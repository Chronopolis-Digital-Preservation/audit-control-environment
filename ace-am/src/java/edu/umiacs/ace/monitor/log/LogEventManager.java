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
import edu.umiacs.util.Strings;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Utility class for managing and persisting log event.
 * @author toaster
 */
public class LogEventManager {

    private long session;
    private Collection collection;


    public LogEventManager( long session, Collection collection ) {
        this.session = session;
        this.collection = collection;
    }


    public void persistItemEvent( LogEnum logType,  String path,
            String msg, EntityManager em )
    {
        LogEvent le = createItemEvent(logType, path,msg);
        EntityTransaction trans = em.getTransaction();
        trans.begin();
        em.persist(le);
        trans.commit();
    }

    public LogEvent createItemEvent( LogEnum logType,  String path )
    {
        return createItemEvent(logType, path, (String) null);
    }

    public LogEvent createItemEvent( LogEnum logType,  String path, Throwable msg )
    {
        return createItemEvent(logType, path, Strings.exceptionAsString(msg));
    }

    public LogEvent createItemEvent( LogEnum logType,  String path, String msg )
    {
        LogEvent event = new LogEvent();

        event.setDate(new Date());
        event.setSession(session);
        event.setCollection(collection);
        event.setPath(path);
        event.setLogType(logType.getType());
        event.setDescription(msg);
        return event;
    }
    
    public void persistCollectionEvent( LogEnum logType, String message, EntityManager em ) {
        persistItemEvent(logType, null, message, em);
    }

    public LogEvent createCollectionEvent( LogEnum logType, String message ) {
        return createItemEvent(logType, null, message);
    }

    public LogEvent createCollectionEvent( LogEnum logType, Throwable message ) {
        return createItemEvent(logType, null, message);
    }

}
