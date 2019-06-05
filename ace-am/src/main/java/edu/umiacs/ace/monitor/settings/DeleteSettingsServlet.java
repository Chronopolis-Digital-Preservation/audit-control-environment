/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.monitor.settings;

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.util.Strings;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author shake
 */
public class DeleteSettingsServlet extends EntityManagerServlet{
    public static final String PARAM_SETTING = "setting";

    @Override
    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response,
                                  EntityManager em) throws ServletException, IOException {

        List<String> settingsToDelete = new ArrayList<>();
        RequestDispatcher dispatcher = null;

        if ( !Strings.isEmpty(request.getParameter(PARAM_SETTING)) ) {
            String[] selectedRoles = request.getParameterValues(PARAM_SETTING);
            settingsToDelete.addAll(Arrays.asList(selectedRoles));
            SettingsUtil.deleteSettings(settingsToDelete);
        }

        List<SettingsParameter> customSettings = SettingsUtil.getCustomSettings();
        request.setAttribute("customSettings", customSettings);
        dispatcher = request.getRequestDispatcher("deletesettings.jsp");

        dispatcher.forward(request, response);
    }

}
