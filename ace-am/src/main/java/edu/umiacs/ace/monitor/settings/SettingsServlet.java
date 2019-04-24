package edu.umiacs.ace.monitor.settings;

import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.util.Strings;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author shake
 */
public class SettingsServlet extends EntityManagerServlet {

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response,
                                  EntityManager em) throws ServletException, IOException {
        boolean update = false;
        List<SettingsParameter> settings = new ArrayList<>();

        // See if we have a multipart/form and take care of it
        if (!Strings.isEmpty(request.getHeader("content-type"))) {
            ServletFileUpload su = new ServletFileUpload();
            try {
                FileItemIterator iter = su.getItemIterator(request);
                while (iter.hasNext()) {
                    FileItemStream item = iter.next();
                    InputStream stream = item.openStream();

                    if (item.isFormField()) {
                        String name = item.getFieldName();
                        String value = Streams.asString(stream);
                        if (name.equals("update")) {
                            update = true;
                        } else if (!value.isEmpty()) {
                            settings.add(new SettingsParameter(name, value));
                        }
                    }
                }
            } catch (FileUploadException ex) {
                Logger.getLogger(SettingsServlet.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (update) {
                SettingsUtil.updateSettings(settings);
            } else {
                SettingsUtil.updateSettings(SettingsUtil.getDefaultSettings());
            }
        }

        Map<String, String> settingsMap = settingsToMap(SettingsUtil.getCurrentSettings());
        Map<String, String> customMap = settingsToMap(SettingsUtil.getCustomSettings());

        request.setAttribute("currSettings", settingsMap);
        request.setAttribute("customSettings", customMap);

        final String LOG_CLASS = "org.apache.log4j.FileAppender";
        String logAppender = settingsMap.get(SettingsConstants.PARAM_4J_APPENDER);
        if (logAppender != null && logAppender.equals(LOG_CLASS)) {
            request.setAttribute("fileAppender", true);
        } else {
            request.setAttribute("fileAppender", false);
        }


        RequestDispatcher dispatcher = request.getRequestDispatcher("settings.jsp");
        dispatcher.forward(request, response);
    }

    private Map<String, String> settingsToMap(List<SettingsParameter> settings) {
        HashMap<String, String> settingsMap = new HashMap<>();

        for (SettingsParameter s : settings) {
            settingsMap.put(s.getName(), s.getValue());
        }

        return settingsMap;
    }
}
