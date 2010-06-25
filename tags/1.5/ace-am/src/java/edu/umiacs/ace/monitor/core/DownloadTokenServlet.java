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

import edu.umiacs.ace.ims.api.XMLTokenWriter;
import edu.umiacs.ace.ims.ws.TokenResponse;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.util.Strings;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Retrieve the xml form of a token
 * tokenid = db ID or token to download
 * @author toaster
 */
public class DownloadTokenServlet extends EntityManagerServlet {

    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em )
            throws ServletException, IOException {
        String idStr = request.getParameter(PARAM_TOKEN_ID);
        if ( !Strings.isValidLong(idStr) ) {
            throw new ServletException("Must supply id");
        }
        long id = Long.parseLong(idStr);
        Token token = em.getReference(Token.class, id);

        if ( token == null ) {
            throw new ServletException("No token for id: " + id);
        }

        XMLTokenWriter writer = new XMLTokenWriter();

        response.setContentType("text/xml");
        writer.write(response.getOutputStream(), (TokenResponse) token.getToken());
    }
}
