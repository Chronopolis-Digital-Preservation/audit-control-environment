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
package edu.umiacs.ace.driver;

import edu.umiacs.ace.monitor.core.ConfigConstants;
import edu.umiacs.ace.monitor.settings.SettingsUtil;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.MonitoredItemManager;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * Return an item using its driver.
 * 
 * @author toaster
 */
public final class RetrieveItemServlet extends EntityManagerServlet {

    private static final Logger LOG = Logger.getLogger(RetrieveItemServlet.class);

    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em ) throws ServletException, IOException {
        MonitoredItemManager mim;
        Collection coll;
        MonitoredItem item = null;
        String urlPath;
        Long collection;
        String collectionPath = null;


        if ( !checkPathExistence(request.getPathInfo()) ) {
            //TODO: should this list collections, xml doc?
            return;
        }
        urlPath = request.getPathInfo().substring(1);

        if ( urlPath.contains("/") ) {
            collection =
                    Long.parseLong(urlPath.substring(0, urlPath.indexOf("/")));
            collectionPath = urlPath.substring(collection.toString().length());
        } else {
            collection = Long.parseLong(urlPath);
        }

        LOG.trace("Looking up item, collection: " + collection + " path: " + collectionPath);

        if ( (coll = em.find(Collection.class, collection)) == null ) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!SettingsUtil.getBoolean(coll, ConfigConstants.ATTR_PROXY_DATA)) {
        //if ( !coll.isProxyData() ) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        mim = new MonitoredItemManager(em);
        if ( collectionPath != null ) {
            item = mim.getItemByPath(collectionPath, coll);
        }

        // If we can't find the item, return a 404 not found
        if ( item == null ) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if ( item.isDirectory() ) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
            // return directory list, do a 404 for now, browse is the only way to get directories
        } else {
            writeItemToResponse(response, collectionPath, coll, em);

        }
        LOG.debug("Retrieved item, Collection: " + collection + " item: " + item.getPath());

    }

    private void writeItemToResponse( HttpServletResponse response,
            String collectionPath, Collection coll, EntityManager em ) throws IOException {
        DigestInputStream dis = null;
//        InputStream is = null;
        ServletContext sc = getServletContext();

        String shortName = collectionPath.substring(collectionPath.lastIndexOf("/") + 1,
                collectionPath.length());
        StorageDriver sa = StorageDriverFactory.createStorageAccess(coll, em);
        String mimeType = sc.getMimeType(shortName);

        if ( mimeType == null ) {
            mimeType = "application/unknown";
        }

        response.setContentType(mimeType);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            dis = new DigestInputStream(sa.getItemInputStream(collectionPath), digest);
//            is = sa.getItemInputStream(collectionPath);
            ServletOutputStream sos = response.getOutputStream();
            byte[] block = new byte[16384];
            int read = 0;
            while ( (read = dis.read(block)) > 0 ) {
                sos.write(block, 0, read);
            }
            dis.close();
            //TODO: Compare this 
        } catch ( IOException ioe ) {
            LOG.error("Error reading: " + collectionPath, ioe);
            if ( dis != null ) {
                dis.close();
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch ( NoSuchAlgorithmException e ) {
            LOG.error("Error creating digest " + collectionPath, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        }
    }

    private boolean checkPathExistence( String path ) {
        if ( path == null || path.length() == 0 ) {
            return false;
        }
        return true;
    }
}
