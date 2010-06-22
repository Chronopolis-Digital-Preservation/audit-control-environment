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

import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author toaster
 */
public class AuditThreadFactory {

    private static int max_audits = 3;
    private static final Map<Collection, AuditThread> runningThreads =
            new HashMap<Collection, AuditThread>();
    private static String imsHost = null;
    private static int imsPort = 8080;
    private static String tokenClass = "SHA-256";

    public static void setIMS( String ims ) {
        imsHost = ims;
    }

    public static String getTokenClass() {
        return tokenClass;
    }

    public static void setTokenClass( String tokenClass ) {
        AuditThreadFactory.tokenClass = tokenClass;
    }

    public static String getIMS() {
        return imsHost;
    }

    public static int getImsPort() {
        return imsPort;
    }

    public static void setImsPort( int imsPort ) {
        AuditThreadFactory.imsPort = imsPort;
    }

    /**
     * Return a new or existing thread if any room is available New threads will start replication
     * 
     * @param c
     * @param tri
     * @return
     */
    public static AuditThread createThread( Collection c, StorageDriver tri,
            MonitoredItem... startItem ) {
        synchronized ( runningThreads ) {
            if ( !runningThreads.containsKey(c) && runningThreads.size() < max_audits ) {
//                String[] pathList = null;
//                if (startItem != null)
//                {
//                    pathList = new String[startItem.size()];
//                }
                AuditThread newThread = new AuditThread(c, tri, startItem);
                newThread.setImsHost(imsHost);
                newThread.setImsport(imsPort);
                newThread.setTokenClassName(tokenClass);
                runningThreads.put(c, newThread);
            }
            return runningThreads.get(c);
        }
    }

//    public static AuditThread createThread(Collection c, StorageAccess tri,
//            MonitoredItem startItem)
//    {
//        synchronized ( runningThreads )
//        {
//            if ( !runningThreads.containsKey(c) && runningThreads.size() < max_audits)
//            {
//                String path = null;
//                if (startItem != null)
//                {
//                    path = startItem.getPath();
//                }
//                runningThreads.put(c, new AuditThread(c, tri, path));
//            }
//            return runningThreads.get(c);
//        }
//    }
    public static final boolean isRunning( Collection c ) {
        synchronized ( runningThreads ) {
            return runningThreads.containsKey(c);
        }
    }

    static void cancellAll() {
        for ( AuditThread at : runningThreads.values() ) {
            at.cancel();
        }
    }

    public static int getMaxAudits() {
        return max_audits;
    }

    public static void setMaxAudits( int max_audits ) {
        AuditThreadFactory.max_audits = max_audits;
    }

    /**
     * Return the current thread for a collection.
     * @param c collection to fetch
     * 
     * @return current running thread or null if nothing is running
     * 
     */
    public static AuditThread getThread( Collection c ) {
        synchronized ( runningThreads ) {
            if ( isRunning(c) ) {
                return runningThreads.get(c);
            }
        }
        return null;
    }

    /**
     * Method for AuditThread to notify its finished
     * @param c
     */
    static void finished( Collection c ) {
        synchronized ( runningThreads ) {
            runningThreads.remove(c);
        }
    }
}
