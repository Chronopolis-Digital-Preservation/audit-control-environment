package edu.umiacs.ace.stats;

import edu.umiacs.ace.util.EntityManagerServlet;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Just a summary of when collections were ingested for now
 *
 * Created by shake on 8/30/16.
 */
public class StatisticsServlet extends EntityManagerServlet {
    private static final Logger LOG = Logger.getLogger(StatisticsServlet.class);

    // General infos
    private static final String GET = "GET";
    private static final String SERVLET = "statistics.jsp";

    // Query params
    private static final String AFTER = "after";
    private static final String BEFORE = "before";
    private static final String GROUP = "group";
    private static final String COLLECTION = "collection";
    private static final String CSV = "csv";

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, EntityManager em) throws ServletException, IOException {
        // Differentiate on the request method
        // GET - no query, just forward
        // POST - query/csv handling
        if (GET.equals(request.getMethod())) {
            processGet(request, response);
        } else {
            processPost(request, response, em);
        }
    }

    private void processGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(SERVLET);
        dispatcher.forward(request, response);
    }

    private void processPost(HttpServletRequest request, HttpServletResponse response, EntityManager em) throws ServletException, IOException {
        String csv = getParameter(request, CSV, null);
        String group = getParameter(request, GROUP, null);
        String after = getParameter(request, AFTER, null);
        String before = getParameter(request, BEFORE, null);
        String collection = getParameter(request, COLLECTION, null);

        SummaryQuery q = new SummaryQuery(group, after, before, collection);

        // We only output to csv for now, so only check true/false
        // Might be better to have a radio w/ json/csv/none or smth
        if (csv == null) {
            List<IngestSummary> summary = q.getSummary();
            request.setAttribute("summary", summary);

            RequestDispatcher dispatcher = request.getRequestDispatcher(SERVLET);
            dispatcher.forward(request, response);
        } else {
            try {
                response.setContentType("text/plain");
                // Download instead of load a page
                response.setHeader("Content-Disposition", "attachment; filename=ingest-summary.csv");
                q.writeToCsv(response.getOutputStream());
            } catch (SQLException e) {
                LOG.error("Error with statistics query", e);
            }
        }
    }

}
