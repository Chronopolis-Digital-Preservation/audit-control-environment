package edu.umiacs.ace.rest;

import com.google.common.collect.ImmutableList;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.rest.models.CompareFile;
import edu.umiacs.ace.rest.models.CompareRequest;
import edu.umiacs.ace.rest.models.CompareResponse;
import edu.umiacs.ace.util.PersistUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

/**
 * Compare files to what ACE knows about a collection
 *
 * Created by shake on 3/9/17.
 */
@Path("/")
public class CompareController {

    /**
     * API method for comparing files and their digests to a given collection
     *
     * todo: It would be nice to not have "compare/id" and instead be more along the lines of "collection/id/compare"
     * todo: BadRequest if a collection == null?
     *
     * @param id the id of the collection
     * @param request the request containing the comparisons
     * @return the result of the comparison
     */
    @POST
    @Path("compare/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CompareResponse compare(@PathParam("id") Long id, CompareRequest request) {
        if (request == null) {
            request = new CompareRequest();
            request.setComparisons(ImmutableList.of());
        }

        EntityManager em = PersistUtil.getEntityManager();
        CompareResponse response = new CompareResponse();
        Collection collection = em.find(Collection.class, id); // can this throw an exception?
        for (CompareFile file : request.getComparisons()) {
            TypedQuery<MonitoredItem> query = em.createNamedQuery("MonitoredItem.getItemByPath", MonitoredItem.class);
            query.setParameter("path", file.getPath());
            query.setParameter("coll", collection);
            try {
                MonitoredItem result = query.getSingleResult();
                if (Objects.equals(result.getFileDigest(), file.getDigest())) {
                    response.addMatch(file.getPath());
                } else {
                    response.addDiff(file.getPath());
                }
            } catch (Exception e) {
                response.addNotFound(file.getPath());
            }
        }

        return response;
    }

}
