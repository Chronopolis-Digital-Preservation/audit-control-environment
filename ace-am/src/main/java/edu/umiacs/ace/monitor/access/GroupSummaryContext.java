package edu.umiacs.ace.monitor.access;

import com.google.common.collect.ImmutableList;
import edu.umiacs.ace.util.PersistUtil;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by shake on 4/4/17.
 */
public class GroupSummaryContext implements ServletContextListener {
    private static final Logger log = Logger.getLogger(GroupSummaryContext.class);

    public static Map<String, GroupSummary> summaries;

    // Main query
    private static final String SUMMARY_QUERY = "SELECT c.colgroup, sum(m.size) AS size, sum(m.count) AS count " +
            "FROM collection c " +
            "JOIN (  " +
            "SELECT sum(size) AS size, count(id) AS count, parentcollection_id   " +
            "FROM monitored_item   " +
            "WHERE directory = 0   " +
            "GROUP BY parentcollection_id " +
            ") AS m ON c.id = m.parentcollection_id ";

    // clauses depending on if we're querying on a group or not
    private static final String SUMMARY_QUERY_NOT_NULL = "WHERE c.colgroup IS NOT NULL ";
    private static final String SUMMARY_QUERY_PARAM = "WHERE c.colgroup = ? ";

    // and the wrap up
    private static final String SUMMARY_QUERY_END = "GROUP BY c.colgroup";

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        summaries = new HashMap<>();
        updateSummaries(ImmutableList.of(SUMMARY_QUERY, SUMMARY_QUERY_NOT_NULL, SUMMARY_QUERY_END),
                ImmutableList.<String>of());
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        summaries.clear();
    }

    /**
     * Public api method to update a single group
     *
     * @param group the group to update
     */
    public static void updateGroup(String group) {
        if (group != null) {
            updateSummaries(
                    ImmutableList.of(SUMMARY_QUERY, SUMMARY_QUERY_PARAM, SUMMARY_QUERY_END),
                    ImmutableList.of(group));
        }
    }

    /**
     * Static method to allow us to update the summary of a group
     *
     * @param sql the sql query to build
     * @param params the parameters to pass along to the query
     */
    private static void updateSummaries(List<String> sql, List<String> params) {
        EntityManager em = PersistUtil.getEntityManager();

        StringBuilder query = new StringBuilder();
        for (String s : sql) {
            query.append(s);
        }

        Query groupSummary = em.createNativeQuery(query.toString(), "GroupSummaryMapping");
        int i = 1;
        for (String param : params) {
            groupSummary.setParameter(i, param);
            i++;
        }

        List<GroupSummary> results = (List<GroupSummary>)groupSummary.getResultList();
        for (GroupSummary result : results) {
            summaries.put(result.getGroup(), result);
        }
    }
}
