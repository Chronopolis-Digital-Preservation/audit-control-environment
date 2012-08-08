/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.monitor.settings;

import edu.umiacs.ace.util.EntityManagerServlet;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, EntityManager em) throws ServletException, IOException {
        HashMap <String, String> customSettings = new HashMap<String, String>();

        ServletFileUpload su = new ServletFileUpload();
        try {
            FileItemIterator iter = su.getItemIterator(request);
            while ( iter.hasNext() ) {
                FileItemStream name = iter.next();

                InputStream nameStream = name.openStream();
                String paramName = Streams.asString(nameStream);
                FileItemStream value = null;
                if ( iter.hasNext() ) {
                    value = iter.next();

                    InputStream valueStream = value.openStream();
                    if ( name.isFormField() && value.isFormField()) {
                        String paramValue = Streams.asString(valueStream);
                        customSettings.put(paramName, paramValue);
                    }
                }

            }
        } catch (FileUploadException ex) {
            //            Logger.getLogger(SettingsServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        SettingsUtil.updateSettings(customSettings, true);

        RequestDispatcher dispatcher = request.getRequestDispatcher("UpdateSettings");
        dispatcher.forward(request, response);

    }

}
