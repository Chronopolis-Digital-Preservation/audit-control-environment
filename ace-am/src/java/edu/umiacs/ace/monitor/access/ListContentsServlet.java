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
// $Id: ListContentsServlet.java 3182 2010-06-16 20:53:20Z toaster $
package edu.umiacs.ace.monitor.access;

import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.sql.SQL;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet to list items in a collection, either url list or digest list
 * @author toaster
 */
public class ListContentsServlet extends EntityManagerServlet {

    private static final Logger LOG = Logger.getLogger(ListContentsServlet.class);
    public static final String PARAM_WGET = "wget"; // display url paths that can be fed into wget
    public static final String PARAM_ITEM = "item"; // directory ID to start with

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em )
            throws ServletException, IOException {
        Collection c = getCollection(request, em);
        long rootId = getParameter(request, PARAM_ITEM, 0);
        boolean wgetFormet = getParameter(request, PARAM_WGET, false);
        DataSource db;
        MonitoredItem mi;
        Connection conn = null;
        PreparedStatement stmt = null;
        response.setContentType("text/plain");
        ServletOutputStream os = response.getOutputStream();

        try {
            try {
                mi = em.getReference(MonitoredItem.class, rootId);
            } catch ( EntityNotFoundException e ) {
                mi = null;
            }

            if ( mi != null && !c.equals(mi.getParentCollection()) ) {
                throw new ServletException("Item not in collection");
            }
            if ( !wgetFormet ) {
                StringBuffer header = new StringBuffer();
                header.append(c.getDigestAlgorithm());
                header.append(":");
                header.append(c.getName());
                if ( mi != null ) {
                    header.append(":");
                    header.append(mi.getPath());
                }
                os.println(header.toString());
            }

            try {
                db = PersistUtil.getDataSource();
                conn = db.getConnection();
                if ( mi == null ) {

                    stmt = conn.prepareStatement(
                            "SELECT monitored_item.PATH, monitored_item.FILEDIGEST "
                            + "FROM monitored_item "
                            + "WHERE monitored_item.PARENTCOLLECTION_ID = ? "
                            + "AND monitored_item.DIRECTORY = 0",
                            ResultSet.TYPE_FORWARD_ONLY,
                            ResultSet.CONCUR_READ_ONLY);
                } else {
                    stmt = conn.prepareStatement(
                            "SELECT monitored_item.PATH, monitored_item.FILEDIGEST "
                            + "FROM monitored_item "
                            + "WHERE monitored_item.PARENTCOLLECTION_ID = ? "
                            + "AND monitored_item.DIRECTORY = 0 " + "AND monitored_item.PATH like ?",
                            ResultSet.TYPE_FORWARD_ONLY,
                            ResultSet.CONCUR_READ_ONLY);
                    stmt.setString(2, mi.getPath() + "%");
                }
                stmt.setLong(1, c.getId());
                stmt.setFetchSize(Integer.MIN_VALUE);
                ResultSet rs = stmt.executeQuery();

                while ( rs.next() ) {
                    String line;
                    if ( wgetFormet ) {
                        String ctxPath = HttpUtils.getRequestURL(request).toString();
//                    System.out.println("ctx path " + ctxPath + " uri " + HttpUtils.getRequestURL(
//                            request));
                        String prefix = ctxPath.substring(
                                0, ctxPath.lastIndexOf("/Sum"));
                        line = prefix + "/Path/" + c.getName() + rs.getString(1).replaceAll(
                                "\\n", "\\n");
                    } else {
                        line = rs.getString(2) + "\t" + rs.getString(1).replaceAll(
                                "\\n", "\\n");
                    }
                    os.println(line);

                }
                rs.close();
                stmt.close();
                conn.close();
//        for ( Object o : q.getResultList() )
//        {
////            MonitoredItem mi = (MonitoredItem) o;
////            if ( mi.getToken() != null )
////            {
//            Vector v = (Vector) o;
//            String line = v.get(1) + "\t" + v.get(0) + "\n";
//            os.write(line.getBytes());
////                String line = mi.getToken().getFileDigest() + "\t" + mi.getPath() + "\n";
////                os.write(line.getBytes());
////            }
//        }
                os.close();
            } catch ( SQLException e ) {
                throw new ServletException(e);
            } finally {
                SQL.release(conn);
                SQL.release(stmt);
            }
        } catch ( Throwable t ) {
            t.printStackTrace();

        }

    }
}
