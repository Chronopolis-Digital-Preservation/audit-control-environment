package edu.umiacs.ace.rest;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.PersistUtil;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
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
        // Because we can't search on group == NULL, we need to do processing here
        EntityManager em = PersistUtil.getEntityManager();

        // Get all
        Query q = em.createNamedQuery("Collection.listAllCollections");
        List<Collection> results = q.getResultList();
        List<Collection> groupless = new ArrayList<Collection>();

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
            cq.where(cb.equal(coll.get("state"), 'A'));
        }

        if (corrupt != null && corrupt) {
            cq.where(cb.equal(coll.get("state"), 'E'));
        }

        return entityManager.createQuery(cq).getResultList();
    }

}
