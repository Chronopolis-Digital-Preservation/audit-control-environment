package edu.umiacs.ace.stats;

import edu.umiacs.ace.util.EntityManagerServlet;

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
    private static final Object[] HEADER = {"date", "collection", "group", "total_count", "size"};

    private static final String AFTER = "after";
    private static final String BEFORE = "before";
    private static final String GROUP = "group";
    private static final String COLLECTION = "collection";
    private static final String CSV = "csv";


    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, EntityManager em) throws ServletException, IOException {
        String after = getParameter(request, AFTER, null);
        String before = getParameter(request, BEFORE, null);
        String group = getParameter(request, GROUP, null);
        String collection = getParameter(request, COLLECTION, null);
        String csv = getParameter(request, CSV, null);

        SummaryQuery q = new SummaryQuery(group, after, before, collection);

        // We only output to csv for now, so only check true/false
        // Might be better to have a radio w/ json/csv/none or smth
        if (csv == null) {
            List<IngestSummary> summary = q.getSummary();
            request.setAttribute("summary", summary);

            RequestDispatcher dispatcher = request.getRequestDispatcher("statistics.jsp");
            dispatcher.forward(request, response);
        } else {
            try {
                response.setContentType("text/plain");
                // Download instead of load a page
                response.setHeader("Content-Disposition", "attachment; filename=ingest-summary.csv");
                q.writeToCsv(response.getOutputStream());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
