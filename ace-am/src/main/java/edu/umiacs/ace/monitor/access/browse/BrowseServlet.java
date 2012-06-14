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

package edu.umiacs.ace.monitor.access.browse;

import edu.umiacs.ace.hashtree.Proof;
import edu.umiacs.ace.hashtree.ProofValidator;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.monitor.audit.AuditThreadFactory;
import edu.umiacs.ace.monitor.audit.AuditTokens;
import edu.umiacs.ace.monitor.access.browse.DirectoryTree.DirectoryNode;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.ace.util.TokenUtil;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 * Servlet to manage browsing a collection. This will store a directorytree in
 * a users session
 *
 * @author toaster
 */
public class BrowseServlet extends EntityManagerServlet {

    private static final Logger LOG = Logger.getLogger(BrowseServlet.class);
    public static final String PARAM_RELOAD = "reload";
    public static final String PARAM_ITEM = "item";
    public static final String PARAM_COLLECTION = "collection";
    public static final String SESSION_FILE = "selectedFile";
    public static final String PAGE_COLLECTION = "collection";
    public static final String SESSION_DIRECTORY_TREE = "directoryTree";
    public static final String PAGE_ISAUDITING = "auditing";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em )
            throws ServletException, IOException {

        long itemId = getParameter(request, PARAM_ITEM, 0);
        long collectionId = getParameter(request, PARAM_COLLECTION, 0);
        HttpSession session = request.getSession();
        boolean isRunning = false;


        DirectoryTree dt;

        // clear, use clicked on a new Browse?collection=?
        if ( collectionId > 0 ) {
            LOG.trace("browse collection: " + collectionId);
            session.removeAttribute(SESSION_FILE);
            session.removeAttribute(SESSION_DIRECTORY_TREE);

            request.setAttribute(PAGE_COLLECTION, collectionId);

            Collection c = em.getReference(Collection.class, collectionId);
            if ( AuditThreadFactory.isRunning(c) || AuditTokens.isRunning(c) ) {
                isRunning = true;
            }
            dt = new DirectoryTree(c);
            session.setAttribute(SESSION_DIRECTORY_TREE, dt);
        } else if ( itemId > 0 ) {
            LOG.trace("Toggling item: " + itemId);
            dt = (DirectoryTree) session.getAttribute(SESSION_DIRECTORY_TREE);
            if ( dt == null ) {
                throw new ServletException("No directory tree in context, servlet must be called with collection first");
            }
            Collection c = dt.getCollection();
            if ( AuditThreadFactory.isRunning(c) || AuditTokens.isRunning(c) ) {
                isRunning = true;
            }
            session.setAttribute(SESSION_FILE,
                    loadFileBean(dt.getDirectoryNode(itemId), em,c));
            if ( dt.getDirectoryNode(itemId).isDirectory() ) {
                dt.toggleItem(itemId);

            }
            //            else
            //            {
            //                session.setAttribute(SESSION_FILE,
            //                        loadFileBean(dt.getDirectoryNode(itemId)));
            //            }
        }
        request.setAttribute(PAGE_ISAUDITING, isRunning);
        RequestDispatcher dispatcher = request.getRequestDispatcher("browse.jsp");
        dispatcher.forward(request, response);
    }

    private FileBean loadFileBean( DirectoryNode node, EntityManager em,Collection c ) {
        FileBean retBean = new FileBean();

        try {
            MonitoredItem master = em.getReference(MonitoredItem.class,
                    node.getId());
            retBean.root = master;
            retBean.name = node.getName();

            if (master.getToken() != null) {
                //            TokenResponse resp = (TokenResponse)master.getToken().getToken();
                MessageDigest digest = MessageDigest.getInstance(master.getToken().getProofAlgorithm());

                ProofValidator pv = new ProofValidator();
                Proof proof = TokenUtil.extractProof(master.getToken());
                String fileDigest = master.getFileDigest();
                if ( fileDigest != null ){
                    byte[] root = pv.rootHash(digest, proof, HashValue.asBytes(master.getFileDigest()));
                    retBean.itemProof = HashValue.asHexString(root);
                }else {
                    retBean.itemProof = null;
                }
            }
            return retBean;
        } catch ( EntityNotFoundException e ) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Cannot create ald",e);
            return null;
        }


    }

    public static class FileBean {

        MonitoredItem root;
        String name;
        String itemProof;

        private FileBean() {
        }

        public String getItemProof() {
            return itemProof;
        }

        public String getName() {
            return name;
        }

        public MonitoredItem getRoot() {
            return root;
        }
    }
}
