package edu.umiacs.ace.monitor.settings;

import edu.umiacs.ace.util.EntityManagerServlet;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
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
public class SettingsServlet extends EntityManagerServlet{

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response,
            EntityManager em) throws ServletException, IOException {
        Set<String> paramSet = SettingsUtil.getParamSet();
        HashMap<String, String> settings = new HashMap<String, String>();

        ServletFileUpload su = new ServletFileUpload();
        try {
            FileItemIterator iter = su.getItemIterator(request);
            while ( iter.hasNext() ) {
                FileItemStream item = iter.next();
                InputStream stream = item.openStream();

                if ( item.isFormField() ) {
                    String name = item.getFieldName();
                    String value = Streams.asString(stream);
                    if ( paramSet.contains(name) && !value.isEmpty()) {
                        settings.put(name, value);
                    }else {
                        System.out.println("Ignoring " + name + " :: " + value);
                    }
                }
            }
        } catch (FileUploadException ex) {
            Logger.getLogger(SettingsServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        SettingsUtil.updateSettings(settings);
        response.sendRedirect("Status");
    }

}
