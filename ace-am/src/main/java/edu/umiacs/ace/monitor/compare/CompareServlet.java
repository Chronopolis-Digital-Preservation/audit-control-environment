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

package edu.umiacs.ace.monitor.compare;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.peers.PartnerSite;
import edu.umiacs.ace.remote.JsonGateway;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.util.Strings;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author toaster
 */
public class CompareServlet extends EntityManagerServlet {

    private static final Logger LOG = Logger.getLogger(CompareServlet.class);
    public static final String PARAM_FILE = "source";
    public static final String PAGE_RESULTS = "results";
    public static final String PARAM_FILTER = "filter";
    public static final String PARAM_REMOTESITEID = "partnerid";
    public static final String PARAM_REMOTRE_COLLECTIONID = "remotecollectionid";
    public static final String PARAM_SOURCE = "source";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    @Override
    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {
        Collection c = null;
        MonitoredItem monItem = null;
        RequestDispatcher dispatcher;
        String inputFilter = null;
        HttpSession session = request.getSession();
        boolean fileAttached = false;
        PartnerSite partner = null;
        long remoteCollection = 0;


        if (!ServletFileUpload.isMultipartContent(request)) {
            throw new ServletException("No file is attached");
        }

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload();

        // Parse the request
        try {
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                if (item.isFormField()) {
                    if (PARAM_COLLECTION_ID.equals(item.getFieldName())) {
                        String col = Streams.asString(item.openStream());
                        if (Strings.isValidLong(col)) {
                            long colId = Long.parseLong(col);
                            c = em.getReference(Collection.class, colId);

                            if (c == null) {
                                throw new ServletException(
                                        "Collection does not exist: " + colId);
                            }

                        }
                    } else if (PARAM_ITEM_ID.equals(item.getFieldName())) {
                        String itemString = Streams.asString(item.openStream());

                        long itemId = Long.parseLong(itemString);
                        try {
                            monItem = em.getReference(MonitoredItem.class,
                                    itemId);
                        } catch (EntityNotFoundException e) {
                            monItem = null;
                        }
                    } else if (PARAM_REMOTESITEID.equals(item.getFieldName())) {
                        String siteString = Streams.asString(item.openStream());
                        if (Strings.isValidLong(siteString)) {
                            long partnerId = Long.parseLong(siteString);
                            try {
                                partner = em.getReference(PartnerSite.class,
                                        partnerId);
                            } catch (EntityNotFoundException e) {
                                partner = null;
                            }
                        }
                    } else if (PARAM_REMOTRE_COLLECTIONID.equals(
                            item.getFieldName())) {
                        String collString = Streams.asString(item.openStream());
                        if (Strings.isValidLong(collString)) {
                            remoteCollection = Long.parseLong(collString);
                        }
                    } else if (PARAM_FILTER.equals(item.getFieldName())) {
                        inputFilter = Streams.asString(item.openStream());
                        if (Strings.isEmpty(inputFilter)) {
                            inputFilter = null;
                        }
                    } else if (PARAM_SOURCE.equals(item.getFieldName())) {
                        String isAttached = Streams.asString(item.openStream());

                        fileAttached = "upload".equals(isAttached);

                    }
                }
                if (!item.isFormField() && fileAttached) {
                    LOG.debug(
                            "item " + monItem + " filter " + inputFilter + " loading attached file");

                    CollectionCompare2 cc = new CollectionCompare2(
                            item.openStream(), inputFilter);
                    CompareResults cr = new CompareResults(cc);
                    Thread t = new Thread(new TableCompareRunnable(cr, cc, c,
                            monItem), "Compare Thread " + c.getName());
                    t.start();
                    session.setAttribute(PAGE_RESULTS, cr);
                }
            }

            LOG.debug(
                    "fileattached: " + fileAttached + " partner " + partner + " remote coll: "
                            + remoteCollection);
            // we have no attached file, load remote
            if (!fileAttached && partner != null && remoteCollection > 0) {
                LOG.debug("Remote digest request " + partner.getRemoteURL());
                CollectionCompare2 cc = new CollectionCompare2(
                        JsonGateway.getGateway().getDigestList(partner,
                                remoteCollection), inputFilter);
                CompareResults cr = new CompareResults(cc);

                Thread t = new Thread(new TableCompareRunnable(cr, cc, c,
                        monItem), "Compare Thread " + c.getName());
                t.start();
                session.setAttribute(PAGE_RESULTS, cr);
            }
        } catch (FileUploadException ful) {
            throw new ServletException(ful);
        }

        dispatcher = request.getRequestDispatcher("comparison.jsp");
        dispatcher.forward(request, response);

    }

    static class TableCompareRunnable implements Runnable {

        private CollectionCompare2 cc;
        private Collection c;
        private MonitoredItem baseItem;
        private CompareResults cr;

        private TableCompareRunnable(CompareResults cr, CollectionCompare2 cc, Collection c,
                                     MonitoredItem baseItem) {
            this.cr = cr;
            this.cc = cc;
            this.c = c;
            this.baseItem = baseItem;
        }

        @Override
        public void run() {
            cc.compareTo(cr, c, baseItem);
        }
    }
}
