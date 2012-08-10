/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.monitor.settings;

import edu.umiacs.ace.util.EntityManagerServlet;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

/**
 *
 * @author shake
 */
public class AddSettingServlet extends EntityManagerServlet{

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse
            response, EntityManager em) throws ServletException, IOException {
        List<SettingsParameter> customSettings = new ArrayList<SettingsParameter>();

        ServletFileUpload su = new ServletFileUpload();
        try {
            FileItemIterator iter = su.getItemIterator(request);

            while ( iter.hasNext() ) {
                FileItemStream name = iter.next();
                InputStream nameStream = name.openStream();
                String paramName = Streams.asString(nameStream);
                FileItemStream value = null;

                // Our form has pairs of 2 (attribute name & value),
                // so grab the next item as well
                if ( iter.hasNext() ) {
                    value = iter.next();
                    InputStream valueStream = value.openStream();

                    if ( name.isFormField() && value.isFormField()) {
                        String paramValue = Streams.asString(valueStream);
                        customSettings.add(new SettingsParameter(paramName,
                                paramValue, true));
                    }
                }

            }
        } catch (FileUploadException ex) {
            //            Logger.getLogger(SettingsServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        SettingsUtil.updateSettings(customSettings);

        RequestDispatcher dispatcher = request.getRequestDispatcher("UpdateSettings");
        dispatcher.forward(request, response);

    }

}
