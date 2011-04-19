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
// $Id: UsersServlet.java 46 2011-01-12 19:32:51Z toaster $
package edu.umiacs.ace.monitor.users;

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.util.Strings;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public class ChangePasswordServlet extends EntityManagerServlet {

    private static final Logger LOG = Logger.getLogger(ChangePasswordServlet.class);
    public static final String PARAM_NEWPASSWORD = "newpassword";
    public static final String PARAM_USER = "user";
    public static final String PARAM_OLDPASSWORD = "oldpassword";
    public static final String PAGE_STATUS = "status";
    public static final String PAGE_SUCCESS = "success";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {
        
        request.setAttribute(PAGE_SUCCESS, false);
        long id;
        Users user = null;
        String status = null;
        boolean refresh = false;

        if (!(Boolean) getServletContext().getAttribute("authmanagement")) {
            status = "Local password management disabled, contact your system administrator";
        } else {
            String username = request.getParameter(PARAM_USER);
            String newPass = request.getParameter(PARAM_NEWPASSWORD);
            String oldPass = request.getParameter(PARAM_OLDPASSWORD);

            if ((user = getUser(username, em)) != null) {
                if (Strings.isEmpty(user.getPassword()) && Strings.isEmpty(oldPass)
                        || user.getPassword().equals(oldPass)) {
                    // if password is set, update, otherwise we just filled in user
                    if (!Strings.isEmpty(newPass)) {
                        user.setPassword(newPass);
                        EntityTransaction et = em.getTransaction();
                        et.begin();
                        em.merge(user);
                        et.commit();
                        refresh = true;
                        status = "Password Successfully Changed";
                        request.setAttribute(PAGE_SUCCESS, true);
                    } else {
                        status = "New password cannot be empty";
                    }
                } else {
                    status = "Bad username or password";
                }

            } else {
                status = "Bad username or password";
            }
        }
        request.setAttribute(PAGE_STATUS, status);
        RequestDispatcher dispatch = request.getRequestDispatcher(
                "passwordchange.jsp");
        dispatch.forward(request, response);
        if (refresh) {
            UserSessionTracker.refreshAll();
        }
    }

    private Users getUser(String username, EntityManager em) {
        Query q = em.createNamedQuery("Users.getUser");
        q.setParameter("user", username);
        try {
            return (Users) q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }

    }
}
