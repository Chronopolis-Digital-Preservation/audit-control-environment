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
package edu.umiacs.ace.monitor.users;

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class UsersServlet extends EntityManagerServlet {

    private static final Logger LOG = Logger.getLogger(UsersServlet.class);
    public static final String PARAM_ID = "id";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_ROLE = "role";
    public static final String PARAM_COMMIT = "commit";
    public static final String PAGE_USER = "user";
    public static final String PAGE_ERROR = "error";
    public static final String PAGE_USERLIST = "userList";
    public static final String PAGE_SELECTED_ROLES = "roles";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void processRequest( HttpServletRequest request,
            HttpServletResponse response, EntityManager em )
            throws ServletException, IOException {

        long id;
        Users user = null;
        String error = null;
        boolean refresh = false;
        if ( !(Boolean) getServletContext().getAttribute("authmanagement") ) {
            return;
        }
        
        String paramPass = request.getParameter(PARAM_PASSWORD);

        // existing userId, lets load and update password
        if ( (id = getParameter(request, PARAM_ID, 0)) > 0 && (user = em.find(
                Users.class, id)) != null ) {
            // if password is set, update, otherwise we just filled in user
            if ( !Strings.isEmpty(paramPass) ) {
                user.setPassword(request.getParameter(PARAM_PASSWORD));
                EntityTransaction et = em.getTransaction();
                et.begin();
                em.merge(user);
                et.commit();
                refresh = true;
            }


        } // new User, lets create
        else if ( !Strings.isEmpty(request.getParameter(PARAM_COMMIT)) ) {
            String paramUser = request.getParameter(PARAM_USERNAME);
            if ( !Strings.isEmpty(paramUser) && !userExists(paramUser, em) ) {
                if ( !Strings.isEmpty(paramPass) ) {
                    LOG.trace("Adding new user: " + paramUser);
                    user = new Users();
                    user.setUsername(paramUser);
                    user.setPassword(paramPass);
                    EntityTransaction et = em.getTransaction();
                    et.begin();
                    em.persist(user);
                    et.commit();
                    refresh = true;
                } else {
                    error = "Please enter a password";
                }
            } else {
                error = "Username is empty or already exists.";
            }
        }

        request.setAttribute(PAGE_ERROR, error);
        request.setAttribute(PAGE_USER, user);

        // update roles for this user
        if ( user != null && !Strings.isEmpty(request.getParameter(PARAM_COMMIT)) ) {
            String[] selectedRoles = request.getParameterValues(PARAM_ROLE);
            setUsers(user.getUsername(), selectedRoles, em);
            refresh = true;
        }

        // load roles into page context
        if ( user != null ) {
            Map<String, String> selectedRoles = new HashMap<String, String>();
            for ( UserRoles ur : this.listRolesForUser(user.getUsername(), em) ) {
                selectedRoles.put(ur.getRolename(), "checked");
            }
            request.setAttribute(PAGE_SELECTED_ROLES, selectedRoles);
        }


        // now load all users into page
        // map of Users to List<UserRoles>
        Map<Users, List<UserRoles>> allUsers = getAllUsers(em);
        request.setAttribute(PAGE_USERLIST, allUsers);

        RequestDispatcher dispatch = request.getRequestDispatcher(
                "usermodify.jsp");
        dispatch.forward(request, response);
        if ( refresh ) {
            UserSessionTracker.refreshAll();
        }
    }

    private Map<Users, List<UserRoles>> getAllUsers( EntityManager em ) {
        Map<Users, List<UserRoles>> users = new HashMap<Users, List<UserRoles>>();

        Query q = em.createNamedQuery("Users.listAllUsers");

        for ( Object o : q.getResultList() ) {
            Users u = (Users) o;

            users.put(u, listRolesForUser(u.getUsername(), em));
        }

        return users;
    }

    private List<UserRoles> listRolesForUser( String username, EntityManager em ) {
        Query roleQuery = em.createNamedQuery("UserRoles.listRolesForUser");
        roleQuery.setParameter("user", username);
        return roleQuery.getResultList();
    }

    private void setUsers( String username, String[] roles, EntityManager em ) {
        if ( roles == null || roles.length < 1 ) {
            return;
        }

        List<UserRoles> userRoleList = new ArrayList<UserRoles>();
        for ( String role : roles ) {
            UserRoles ur = new UserRoles();
            ur.setRolename(role);
            ur.setUsername(username);
            userRoleList.add(ur);
        }

        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            Query q = em.createNamedQuery("UserRoles.removeUser");
            q.setParameter("user", username);
            q.executeUpdate();
            for ( UserRoles ur : userRoleList ) {
                em.persist(ur);
            }
            trans.commit();
        } catch ( Exception e ) {
            LOG.error("Error persisting ", e);
        }
    }

    private boolean userExists( String username, EntityManager em ) {
        Query q = em.createNamedQuery("Users.getUser");
        q.setParameter("user", username);
        try {
            q.getSingleResult();
            return true;
        } catch ( NoResultException ex ) {
            return false;
        }

    }
}
