package edu.umiacs.ace.monitor.access;

import com.google.common.collect.ImmutableList;
import edu.umiacs.ace.util.PersistUtil;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet context listener which acts as a cache for Summary information about
 * groups in ACE
 * <p>
 * If there are many groups, it's conceivable that this could be a strain on memory
 * <p>
 * Created by shake on 4/4/17.
 */
public class GroupSummaryContext implements ServletContextListener {
    private static final Logger log = Logger.getLogger(GroupSummaryContext.class);

    public static Map<String, GroupSummary> summaries;
    public static Map<String, GroupSummary> preservationSummaries;

    /**
     * Query to get ALL group summaries
     */
    private static final String SUMMARY_QUERY_ALL =
            "SELECT c.colgroup, sum(m.size) AS size, sum(m.count) AS count " +
                    "FROM collection c " +
                    "JOIN (  " +
                    "SELECT sum(size) AS size, count(id) AS count, parentcollection_id   " +
                    "FROM monitored_item   " +
                    "WHERE directory = 0   " +
                    "GROUP BY parentcollection_id " +
                    ") AS m ON c.id = m.parentcollection_id " +
                    "WHERE c.colgroup IS NOT NULL " +
                    "GROUP BY c.colgroup";

    /**
     * Query to get group summaries for preservation storages with REMOVE state collections excluded
     */
    private static final String SUMMARY_QUERY_PRESERVATION_STORAGES =
            "SELECT c.colgroup, sum(m.size) AS size, sum(m.count) AS count FROM " +
                    "(SELECT * FROM collection " +
                    "WHERE state <> 'R') as c " +
                    "JOIN (  " +
                    "SELECT sum(size) AS size, count(id) AS count, parentcollection_id   " +
                    "FROM monitored_item   " +
                    "WHERE directory = 0   " +
                    "GROUP BY parentcollection_id " +
                    ") AS m ON c.id = m.parentcollection_id " +
                    "WHERE c.colgroup IS NOT NULL " +
                    "GROUP BY c.colgroup";

    /**
     * Query to get the group summary for a single group
     */
    private static final String SUMMARY_QUERY_GROUP =
            "select c.colgroup, sum(m.size) AS size, count(m.id) AS count " +
                    "FROM monitored_item m " +
                    "JOIN ( " +
                    "  select colgroup, id " +
                    "  FROM collection " +
                    "  WHERE colgroup = ? " +
                    ") c ON c.id = m.parentcollection_id WHERE directory = 0";

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        summaries = new HashMap<>();
        updateSummaries(ImmutableList.of(SUMMARY_QUERY_ALL),
                ImmutableList.of(),
                summaries);

        preservationSummaries = new HashMap<>();
        updateSummaries(ImmutableList.of(SUMMARY_QUERY_PRESERVATION_STORAGES),
                ImmutableList.of(),
                preservationSummaries);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        summaries.clear();
        preservationSummaries.clear();
    }

    /**
     * Public api method to update a single group
     *
     * @param group the group to update
     */
    @SuppressWarnings("WeakerAccess")
    public static void updateGroup(String group) {
        if (group != null) {
            log.debug("Updating group summary for " + group);
            updateSummaries(
                    ImmutableList.of(SUMMARY_QUERY_GROUP),
                    ImmutableList.of(group),
                    summaries);
            updateSummaries(
                    ImmutableList.of(SUMMARY_QUERY_PRESERVATION_STORAGES),
                    ImmutableList.of(group),
                    preservationSummaries);
        }
    }

    /**
     * Static method to allow us to update the summary of a group
     *
     * @param sql    the sql query to build
     * @param params the parameters to pass along to the query
     */
    private static void updateSummaries(List<String> sql, List<String> params, Map<String, GroupSummary> summaries) {
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
        try {
	        List<GroupSummary> results = (List<GroupSummary>) groupSummary.getResultList();
	        for (GroupSummary result : results) {
	            log.info("Result: group " + result.getGroup() + ", size: " + result.getSize() + ", count: " + result.getCount());
	            summaries.put(result.getGroup(), result);
	        }
        } catch (PersistenceException e) {
        	log.error("Error qury GroupSummy.", e);
        }
    }
}
