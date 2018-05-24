package edu.umiacs.ace.monitor.register;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.EntityManagerServlet;
import edu.umiacs.ace.util.KSFuture;
import edu.umiacs.ace.util.PersistUtil;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ImportStatusServlet extends EntityManagerServlet {
    private static final Logger LOG = Logger.getLogger(ImportStatusServlet.class);

    private static final String ACTIVE_PARAM = "active";

    @Override
    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response,
                                  EntityManager em) throws ServletException, IOException {
        IngestThreadPool pool = IngestThreadPool.getInstance();
        long id = getParameter(request, ACTIVE_PARAM, -1);

        if (id > 0) {
            EntityManager manager = PersistUtil.getEntityManager();
            Collection collection = manager.find(Collection.class, id);
            if (collection != null) {
                KSFuture<IngestSupervisor> ksSupervisor = pool.getCache().get(collection);

                if (ksSupervisor != null) {
                    request.setAttribute(ACTIVE_PARAM, ksSupervisor.getKnownResult());
                }
            } else {
                LOG.debug("Collection " + id + " does not exist");
            }
        }

        request.setAttribute("results", pool);
        RequestDispatcher dispatcher = request.getRequestDispatcher("ingeststatus.jsp");
        dispatcher.forward(request, response);
    }
}
