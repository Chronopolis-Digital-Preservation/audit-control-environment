package edu.umiacs.ace.monitor.settings;

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class SettingsServlet extends EntityManagerServlet {

    private final String LOG_CLASS = "org.apache.log4j.FileAppender";

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response,
            EntityManager em) throws ServletException, IOException {
        Set<String> paramSet = SettingsUtil.getParamSet();
        HashMap<String, String> settings = new HashMap<String, String>();
        boolean update = false;

        // See if we have a multipart/form and take care of it
        if (!Strings.isEmpty(request.getHeader("content-type"))) {
            ServletFileUpload su = new ServletFileUpload();
            try {
                FileItemIterator iter = su.getItemIterator(request);
                while ( iter.hasNext() ) {
                    FileItemStream item = iter.next();
                    InputStream stream = item.openStream();

                    if ( item.isFormField() ) {
                        String name = item.getFieldName();
                        String value = Streams.asString(stream);
                        if ( paramSet.contains(name) && !value.isEmpty() ) {
                            settings.put(name, value);
                        }

                        if ( name.equals("update") ) {
                            update = true;
                        }
                    }
                }
            } catch (FileUploadException ex) {
                Logger.getLogger(SettingsServlet.class.getName()).log(Level.SEVERE, null, ex);
            }

            if ( update ) {
                SettingsUtil.updateSettings(settings, false);
            } else {
                SettingsUtil.updateSettings(SettingsUtil.getDefaultMap(), false);
            }
        }

        Map<String, String> settingsMap =
                settingsToMap(SettingsUtil.getCurrentSettings());
        Map<String, String> customMap =
                settingsToMap(SettingsUtil.getCustomSettings());

        request.setAttribute("currSettings", settingsMap);
        request.setAttribute("customSettings", customMap);

        if ( settingsMap.get(SettingsConstants.PARAM_4J_APPENDER).equals(LOG_CLASS)) {
            request.setAttribute("fileAppender", true);
        }else {
            request.setAttribute("fileAppender", false);
        }


        RequestDispatcher dispatcher = request.getRequestDispatcher("settings.jsp");
        dispatcher.forward(request, response);

    }

    private Map<String, String> settingsToMap( List<SettingsParameter> settings) {
        HashMap<String, String> settingsMap = new HashMap<String, String>();

        for ( SettingsParameter s : settings ) {
            settingsMap.put(s.getName(), s.getValue());
        }

        return settingsMap;
    }
}
