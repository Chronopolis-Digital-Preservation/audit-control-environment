package edu.umiacs.ace.rest;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.CollectionState;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.util.PersistUtil;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Get a listing of groups/collections in ACE
 *
 * Created by shake on 10/22/14.
 */
@Path("/")
public class ListController {
    private static final Logger LOG = Logger.getLogger(ListController.class);

    /**
     * Get a list of all the groups registered in ACE
     *
     * @return A JSONArray containing each group name
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("groups")
    public List<String> getGroups() {
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("Collection.listGroups");
        return q.getResultList();
    }

    /**
     * Get a list of all collections within a group
     *
     * @param group the group to search in
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("collections/by-group/{group}")
    public List<Collection> getCollectionsInGroup(@PathParam("group") String group) {
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("Collection.getCollectionsInGroup");
        q.setParameter("group", group);
        return q.getResultList();
    }

    /**
     * Get a list of collections not associated with any group
     *
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("collections/by-group")
    public List<Collection> getCollectionsNoGroup() {
        // We can search on group == null if we do where group is null
        // Because we can't search on group == NULL, we need to do processing here
        EntityManager em = PersistUtil.getEntityManager();

        // Get all
        Query q = em.createNamedQuery("Collection.listAllCollections");
        List<Collection> results = q.getResultList();
        List<Collection> groupless = new ArrayList<>();

        // Filter if the group == null
        for (Collection result : results) {
            if (result.getGroup() == null) {
                groupless.add(result);
            }
        }
        return groupless;
    }

    /**
     * New API method to get all collections with query parameters
     *
     * @return
     */
    @GET
    @Path("collections")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Collection> getCollections(@QueryParam("group") String group,
                                           @QueryParam("active") Boolean active,
                                           @QueryParam("corrupt") Boolean corrupt) {
        EntityManager entityManager = PersistUtil.getEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Collection> cq = cb.createQuery(Collection.class);
        Root<Collection> coll = cq.from(Collection.class);
        cq.select(coll);
        if (active != null && active) {
            cq.where(cb.equal(coll.get("state"), CollectionState.ACTIVE));
        }

        if (corrupt != null && corrupt) {
            cq.where(cb.equal(coll.get("state"), CollectionState.ERROR));
        }
        cq.orderBy(cb.asc(coll.get("group")));

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * API for getting items in a collection. We can offer more query parameters (path, last seen, etc),
     * but for now just query based on state
     *
     * @param id The id of the collection
     * @param state The state of the monitored items
     * @return a list of monitored items for the collection
     */
    @GET
    @Path("collections/{id}/items")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MonitoredItemBean> getCollectionItems(@PathParam("id") Long id,
                                                      @QueryParam("state") String state) {
        EntityManager entityManager = PersistUtil.getEntityManager();
        Collection c = entityManager.find(Collection.class, id);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<MonitoredItemBean> cq = cb.createQuery(MonitoredItemBean.class);
        Root<MonitoredItem> mi = cq.from(MonitoredItem.class);

        cq.select(cb.construct(MonitoredItemBean.class,
                mi.get("id"), mi.get("path"),
                mi.get("state"), mi.get("fileDigest"),
                mi.get("size"), mi.get("lastSeen"),
                mi.get("stateChange"), mi.get("lastVisited")));

        Predicate predicate = cb.and(cb.equal(mi.get("directory"), false), cb.equal(mi.get("parentCollection"), c));

        if (state != null && !state.isEmpty()) {
            predicate = cb.and(predicate, cb.equal(mi.get("state"), state));
            // predicates.add(cb.equal(mi.get("parentCollection"), c));
        }

        cq.where(predicate);
        cq.orderBy(cb.asc(mi.get("path")));

        return entityManager.createQuery(cq).getResultList();
    }

}
