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

package edu.umiacs.ace.util;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.JNDIConnector;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.server.ServerSession;

import javax.naming.Context;
import javax.naming.InitialContext;


/**
 * Note: As of eclipselink 2.6.3 tomcat starts with NoServerPlatform, which defaults to COMPOSITE_NAME_LOOKUP
 *
 * register jndi connection
 * @author toaster
 */
public class DataSourceSessionCustomizer implements SessionCustomizer {

    @Override
    public void customize( Session session ) throws Exception {
    JNDIConnector connector = null;
    Context context = null;
   // try {
      context = new InitialContext();
      if(null != context) {
        connector = (JNDIConnector)session.getLogin().getConnector(); // possible CCE
        // Change from COMPOSITE_NAME_LOOKUP to STRING_LOOKUP
        // Note: if both jta and non-jta elements exist this will only change the first one - and may still result in
        // the COMPOSITE_NAME_LOOKUP being set
        // Make sure only jta-data-source is in persistence.xml with no non-jta-data-source property set
        connector.setLookupType(JNDIConnector.STRING_LOOKUP);
 
        // Or, if you are specifying both JTA and non-JTA in your persistence.xml then set both connectors to be safe
        JNDIConnector writeConnector = (JNDIConnector)session.getLogin().getConnector();
        writeConnector.setLookupType(JNDIConnector.STRING_LOOKUP);
        JNDIConnector readConnector =
            (JNDIConnector)((DatabaseLogin)((ServerSession)session).getReadConnectionPool().getLogin()).getConnector();
        readConnector.setLookupType(JNDIConnector.STRING_LOOKUP);
 
        System.out.println("_JPAEclipseLinkSessionCustomizer: configured " + connector.getName());
      }
      else {
        throw new Exception("_JPAEclipseLinkSessionCustomizer: Context is null");
      }
    //}
    //catch(Exception e) {
    //  e.printStackTrace();
    //}

        JNDIConnector conn = (JNDIConnector) session.getLogin().getConnector();
        conn.setLookupType(JNDIConnector.STRING_LOOKUP);
    }
}
