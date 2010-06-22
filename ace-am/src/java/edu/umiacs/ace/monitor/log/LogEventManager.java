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
// $Id: LogEventManager.java 3187 2010-06-22 14:41:19Z toaster $
package edu.umiacs.ace.monitor.log;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.peers.PeerCollection;
import edu.umiacs.util.Strings;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Utility class for managing and persisting log event.
 * @author toaster
 */
public class LogEventManager {

//    private EntityManager em;
    private long session;
    private Collection collection;

//    public LogEventManager( EntityManager em, long session ) {
//        this.em = em;
//        this.session = session;
//    }

    public LogEventManager( long session, Collection collection ) {
//        this.em = em;
        this.session = session;
        this.collection = collection;
    }



    /**
     * Start auding of a collection
     * @param c
     */
//    public void fileAuditStart( Collection c, String message ) {
//        LogEvent event =
//                getBaseEvent(c.getDirectory(), c);
//        event.setLogType(LogEnum.FILE_AUDIT_START.getType());
//        event.setDescription(message);
//        persist(event);
//    }

//    public void tokenAuditStart( Collection c ) {
//        LogEvent event =
//                getBaseEvent(c.getDirectory(), c);
//        event.setLogType(LogEnum.TOKEN_AUDIT_START.getType());
//        persist(event);
//    }

    /**
     * Log finish colleciton audit
     * 
     * @param c collection that finished
     * @param message descriptive message, optional
     */
//    public void finishFileAudit( Collection c, String message ) {
//        LogEvent event =
//                getBaseEvent(c.getDirectory(), c);
//        event.setDescription(message);
//        event.setLogType(LogEnum.FILE_AUDIT_FINISH.getType());
//        persist(event);
//    }

    /**
     * Log finish colleciton token audit
     * 
     * @param c collection that finished
     * @param message descriptive message, optional
     */
//    public void finishTokenAudit( Collection c, String message ) {
//        LogEvent event =
//                getBaseEvent(c.getDirectory(), c);
//        event.setDescription(message);
//        event.setLogType(LogEnum.TOKEN_AUDIT_FINISH.getType());
//        persist(event);
//    }

    //public voi
    /**
     * Register a new file found
     * 
     * @param path
     * @param c
     */
//    public void foundNewFile( String path, Collection c ) {
//        LogEvent event = getBaseEvent(path, c);
////        event.setDescription(c.getDirectory() + path);
//
//        event.setLogType(LogEnum.FILE_NEW.getType());
//
//        persist(event);
//
//    }

//    public void removeFile( String path, Collection c ) {
//        LogEvent event = getBaseEvent(path, c);
//        event.setLogType(LogEnum.REMOVE_ITEM.getType());
//        persist(event);
//    }

    /**
     * Mark a file as transitioning to online.
     * 
     * @param path
     * @param c
     */
//    public void fileOnline( String path, Collection c ) {
//        LogEvent event = getBaseEvent(path, c);
////        event.setDescription(c.getDirectory() + path);
//
//        event.setLogType(LogEnum.FILE_ONLINE.getType());
//
//        persist(event);
//
//    }

    /**
     * Create communication error event. Returns the event rather than persisting
     * for use in a larger transaction.
     * 
     * @param path
     * @param c
     * @param reason
     * @return
     */
//    public LogEvent imsCommError( String path, Collection c, Throwable reason ) {
//        LogEvent event = getBaseEvent(path, c);
//        event.setLogType(LogEnum.UNKNOWN_IMS_COMMUNICATION_ERROR.getType());
//        event.setDescription(Strings.exceptionAsString(reason));
//        return event;
////        persist(event);
//
//    }

//    public LogEvent missingRemoteFile( String path, Collection c, PeerCollection pc ) {
//        LogEvent event = getBaseEvent(path, c);
//        event.setLogType(LogEnum.REMOTE_FILE_MISSING.getType());
//        event.setDescription(pc.getSite().getRemoteURL());
//        return event;
////        persist(event);
//
//    }

//    public LogEvent onlineRemoteFile( String path, Collection c ) {
//        LogEvent event = getBaseEvent(path, c);
//        event.setLogType(LogEnum.REMOTE_FILE_ONLINE.getType());
//        return event;
////        persist(event);
//
//    }

//    public LogEvent missingFile( String path, Collection c ) {
//        LogEvent event = getBaseEvent(path, c);
//        event.setLogType(LogEnum.FILE_MISSING.getType());
//        return event;
////        persist(event);
//
//    }

//    public void corruptFile( String path, Collection c, String expected,
//            String seenHash ) {
//        LogEvent event = getBaseEvent(path, c);
//        event.setLogType(LogEnum.FILE_CORRUPT.getType());
//        event.setDescription(
//                "Expected digest: " + expected + " Saw: " + seenHash);
//
//        persist(event);
//
//    }

//    public LogEvent corruptRemoteFile( String path, Collection c, String expected,
//            String seenHash, PeerCollection pc ) {
//        LogEvent event = getBaseEvent(path, c);
//        event.setLogType(LogEnum.REMOTE_FILE_CORRUPT.getType());
//        event.setDescription(
//                "Expected digest: " + expected + " Saw: " + seenHash + " site: "
//                + pc.getSite().getRemoteURL());
//        return event;
////        persist(event);
//
//    }

//    public LogEvent validToken( String path, Collection c ) {
//        LogEvent event = getBaseEvent(path, c);
//        event.setLogType(LogEnum.TOKEN_VALID.getType());
//        return event;
//    }

//    public LogEvent invalidToken( String path, Collection c, String message ) {
//        LogEvent event = getBaseEvent(path, c);
//        event.setLogType(LogEnum.TOKEN_INVALID.getType());
//        event.setDescription(message);
//        return event;
//    }

    /**
     * persists
     * 
     * @param path
     * @param c
     */
//    public void missingToken( String path, Collection c ) {
//        LogEvent event = getBaseEvent(path, c);
//        event.setLogType(LogEnum.MISSING_TOKEN.getType());
//    }
    /**
     * presists
     * 
     * @param path
     * @param c
     * @param message
     */
//    public void errorReading( String path, Collection c, String message ) {
//        LogEvent event = getBaseEvent(path, c);
//        event.setLogType(LogEnum.ERROR_READING.getType());
//        event.setDescription(message);
//
//        persist(event);
//    }

//    public void systemError( Collection coll, Throwable reason ) {
//        systemError(coll, Strings.exceptionAsString(reason));
////        LogEvent event = getBaseEvent(null, coll);
////        event.setLogType(LogEnum.SYSTEM_ERROR.getType());
////        event.setDescription(Strings.exceptionAsString(reason));
////
////        persist(event);
//    }

//    public void systemError( Collection coll, String reason ) {
//        LogEvent event = getBaseEvent(null, coll);
//        event.setLogType(LogEnum.SYSTEM_ERROR.getType());
//        event.setDescription(reason);
//
//        persist(event);
//    }

    /**
     * Log abort replication. Results in both a system error and a finish log event
     * 
     * @param coll
     * @param message
     * @param reason
     */
//    public void abortSite( Collection coll, String message,
//            Throwable reason ) {
//        LogEvent event = getBaseEvent(null, coll);
//        event.setLogType(LogEnum.SYSTEM_ERROR.getType());
//        event.setDescription(message + Strings.exceptionAsString(reason));
//
//        persist(event);
////        finishFileAudit(coll, "Audit aborted: " + message);
//    }

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

//    private void persist( LogEvent event ) {
////        EntityTransaction trans = em.getTransaction();
////        trans.begin();
////        em.persist(event);
////        trans.commit();
//    }

//    private LogEvent getBaseEvent( String path, Collection c ) {
//        LogEvent event = new LogEvent();
//
//        event.setDate(new Date());
//        event.setSession(session);
//        event.setCollection(c);
//        event.setPath(path);
//
//        return event;
//    }
}
