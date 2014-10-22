package edu.umiacs.ace.rest;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.PersistUtil;
import org.codehaus.jettison.json.JSONArray;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
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
    public JSONArray getGroups() {
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("Collection.listGroups");
        List<String> groups = q.getResultList();
        return new JSONArray(q.getResultList());
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

}
