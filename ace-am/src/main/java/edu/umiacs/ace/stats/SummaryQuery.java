package edu.umiacs.ace.stats;

import edu.umiacs.ace.util.PersistUtil;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletOutputStream;
import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to create the SQL query and execute it for
 * an @{IngestSummary}
 *
 * I wonder if it would be better to use FILE_ONLINE (4) or FILE_NEW (1)
 * instead of FILE_AUDIT_FINISH (20). This would give us a better
 * idea of what a collection was like at a point in time, rather than
 * what it's like now.
 *
 * TODO: Paged for large summaries
 * TODO: Query for no groups
 *
 * Created by shake on 8/30/16.
 */
@SuppressWarnings("WeakerAccess")
public class SummaryQuery {

    // Query Parameters
    private String group;
    private String after;
    private String before;
    private String collection;

    // The start of our query
    final String SELECT = "SELECT l1.date, c.name, c.colgroup, m.count, m.size ";

    // LogEvent Join
    // Smallest n-per-group, on Date of logtype 20
    final String LE_JOIN = "FROM logevent l1 " +
            "JOIN ( " +
            "  SELECT collection_id, min(date) AS date " +
            "  FROM logevent " +
            "  WHERE logtype = 20 ";
    final String LE_JOIN_FINISH = "GROUP BY collection_id) AS l2 " +
            "ON l1.collection_id = l2.collection_id " +
            "AND l1.date = l2.date " +
            "AND l1.logtype = 20 ";

    // Optional parameters to query on for the log event
    final String LE_AFTER = "AND l1.date > ? ";
    final String LE_BEFORE = "AND l1.date < ? ";

    // Collection Join
    //Use coalesce in case the colgroup is null - prevents errors with the constructor
    // Gets the colgroup so that we can display it
    final String COL_JOIN = "JOIN ( " +
            " SELECT id, name, COALESCE(colgroup, '') AS colgroup FROM collection ";
    final String COL_JOIN_GROUP = "WHERE colgroup = ? ";
    final String COL_JOIN_NAME = "name LIKE ? ";
    final String COL_JOIN_END = ") AS c " +
            "ON l1.collection_id = c.id ";

    // Monitored Item Join
    // count number of items and sum based on the size
    final String MI_JOIN = "JOIN ( " +
            "  SELECT count(id) AS count, sum(size) AS size, parentcollection_id " +
            "  FROM monitored_item " +
            "  WHERE directory = 0 " +
            "  GROUP BY parentcollection_id) AS m " +
            "  ON m.parentcollection_id = c.id ";

    public SummaryQuery(String group, String after, String before, String collection) {
        this.group = group;
        this.after = after;
        this.before = before;
        this.collection = collection;
    }

    public List<IngestSummary> getSummary() {
        List<String> params = new ArrayList<>();
        EntityManager em = PersistUtil.getEntityManager();

        // IngestSummaryMapping defined in META-INF/orm.xml
        StringBuilder query = buildQuery(params);
        Query nq = em.createNativeQuery(query.toString(), "IngestSummaryMapping");

        int i = 1;
        for (String param : params) {
            nq.setParameter(i, param);
            i++;
        }

        return (List<IngestSummary>)nq.getResultList();
    }

    public void writeToCsv(ServletOutputStream os) throws SQLException, IOException {
        List<String> params = new ArrayList<>();
        DataSource db = PersistUtil.getDataSource();
        Connection conn = db.getConnection();
        PreparedStatement stmt = conn.prepareStatement(buildQuery(params).toString(),
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);

        int i = 1;
        for (String param : params) {
            // TODO: We could have a class which calls the appropriate method
            //       i.e. datetime -> setDate etc
            //       but this works so maybe not
            stmt.setString(i, param);
            i++;
        }

        Charset charset = Charset.forName("UTF-8");
        os.write("date_ingested,collection,group,total_items,size".getBytes(charset));
        os.println();

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Timestamp date = rs.getTimestamp(1);
            String collection = rs.getString(2);
            String group = rs.getString(3);
            long count = rs.getLong(4);
            BigDecimal size = rs.getBigDecimal(5);

            StringBuilder bldr = new StringBuilder();
            bldr.append(date).append(",");
            bldr.append(collection).append(",");
            bldr.append(group).append(",");
            bldr.append(count).append(",");
            bldr.append(size);

            os.write(bldr.toString().getBytes(charset));
            os.println();
        }

        rs.close();
        stmt.close();
        conn.close();
        os.close();
    }

    private StringBuilder buildQuery(List<String> params) {
        StringBuilder query = new StringBuilder(SELECT);

        // Start our LogEvent Join
        query.append(LE_JOIN);
        query.append(LE_JOIN_FINISH);
        updateParams(params, query, LE_AFTER, after);
        updateParams(params, query, LE_BEFORE, before);

        // Start our collection join
        query.append(COL_JOIN);

        // Setup our conditional clauses
        // todo: could wrap the two collection == null checks together
        String groupQuery = collection == null ? COL_JOIN_GROUP : COL_JOIN_GROUP + " AND ";
        String collVal = collection == null ? null : "%" + collection + "%";
        String collQuery = group == null ? "WHERE " + COL_JOIN_NAME : COL_JOIN_NAME;

        updateParams(params, query, groupQuery, group);
        updateParams(params, query, collQuery, collVal);

        // Finish + MonitoredItem join
        query.append(COL_JOIN_END).append(MI_JOIN);
        return query;
    }

    private void updateParams(List<String> params, StringBuilder query, String partial, String value) {
        if (value != null && !value.isEmpty()) {
            params.add(value);
            query.append(partial);
        }
    }

    public SummaryQuery setGroup(String group) {
        this.group = group;
        return this;
    }

    public SummaryQuery setAfter(String after) {
        this.after = after;
        return this;
    }

    public SummaryQuery setBefore(String before) {
        this.before = before;
        return this;
    }

    public SummaryQuery setCollection(String collection) {
        this.collection = collection;
        return this;
    }
}
