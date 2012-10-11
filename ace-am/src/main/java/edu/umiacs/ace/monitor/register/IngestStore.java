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
import edu.umiacs.ace.monitor.log.LogEventManager;
import edu.umiacs.ace.token.TokenStoreEntry;
import edu.umiacs.ace.token.TokenStoreReader;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.util.TokenUtil;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
//import org.apache.tomcat.util.http.fileupload.FileUploadBase;

/**
 * Servlet to ingest token stores
 * @author toaster
 */
public class IngestStore extends EntityManagerServlet {
    // private static final Logger LOG = Logger.getLogger(IngestStore.class);
    private LogEventManager logManager;
    // private long session;
    public static final String PAGE_RESULTS = "results";

    @Override
    protected void processRequest( HttpServletRequest request, HttpServletResponse response,
            EntityManager em ) throws ServletException, IOException {
        Collection coll = null; //getCollection(request, em);
        MonitoredItem item = getItem(request, em);
        TokenStoreReader tokenReader = null;
        RequestDispatcher dispatcher;
        Map<String, Token> batchTokens = new HashMap<String, Token>();

        if (item != null && !item.isDirectory())
            throw new ServletException( "Selected item is not a directory "
                    + item.getPath() );

        if (!ServletFileUpload.isMultipartContent(request)) {
            throw new ServletException("No file attached");
        }

        //        request.
        ServletFileUpload upload = new ServletFileUpload();
        try {
            FileItemIterator iter = upload.getItemIterator(request);
            while( iter.hasNext() ) {
                FileItemStream fileItem = iter.next();
                InputStream stream = fileItem.openStream();

                if (fileItem.isFormField()) {

                    // Basic find collection id and set the collection
                    if ( PARAM_COLLECTION_ID.equals(fileItem.getFieldName()) ) {
                        String collectionId = Streams.asString(stream);
                        if ( Strings.isValidLong(collectionId) ) {
                            long collId = Long.parseLong(collectionId);
                            coll = em.getReference(Collection.class, collId);

                            if ( coll == null ) {
                                throw new ServletException("Collection Id " + 
                                    collId + " not found.");
                            }
                        }
                    }
                } else {
                    // Read tokens in from input stream and add to AceToken set
                    // May want to log this stuff
                    tokenReader = new TokenStoreReader(fileItem.openStream());

                    if ( tokenReader == null ) {
                        throw new ServletException("Token file is corrupt");
                    }

                    while ( tokenReader.hasNext() ) {
                        TokenStoreEntry tokenEntry = tokenReader.next();
                        Token token = TokenUtil.convertFromAceToken(tokenEntry.getToken());

                        if ( !token.getProofAlgorithm().equals(coll.getDigestAlgorithm()) ) {
                            throw new ServletException("Token digest differs from"
                                    + " collection digest.");
                        }
                        batchTokens.put(tokenEntry.getIdentifiers().get(0), token);
                    }
                }

            }
        } catch (FileUploadException ex) {
            throw new ServletException(ex);
        }

        // Sanity check
        if ( coll == null || tokenReader == null ) {
            throw new ServletException("Bad upload parameters");
        }

        IngestThreadPool tPool = IngestThreadPool.getInstance();
        tPool.submitTokens(batchTokens, coll);
        HttpSession session = request.getSession();
        session.setAttribute(PAGE_RESULTS, tPool);

        dispatcher = request.getRequestDispatcher("ingeststatus.jsp");
        dispatcher.forward(request, response);
    }

}
