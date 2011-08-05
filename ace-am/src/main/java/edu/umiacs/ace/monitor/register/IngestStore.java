package edu.umiacs.ace.monitor.register;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.util.EntityManagerServlet;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import org.apache.tomcat.util.http.fileupload.FileUploadBase;

/**
 * TODO Servlet to ingest token stores
 * @author toaster
 */
public class IngestStore extends EntityManagerServlet {
    
    @Override
    protected void processRequest( HttpServletRequest request, HttpServletResponse response,
            EntityManager em ) throws ServletException, IOException {

        Collection coll = getCollection(request, em);
        MonitoredItem item = getItem(request, em);

        if (item != null && !item.isDirectory())
            throw new ServletException("Selected item is not a directory " + item.getPath());

//        request.
    }
   
}
