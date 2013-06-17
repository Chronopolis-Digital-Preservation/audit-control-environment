/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.rest;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.PersistUtil;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

/**
 * REST Service for adding and viewing collections
 *
 * @author shake
 */
@Path("collection")
public class CollectionManagement {
    private static final Logger LOG =
            Logger.getLogger(CollectionManagement.class);

    // Make sure we have required attributes set for the collections
    private void checkCollection(Collection coll) {
        if ( coll.getDigestAlgorithm() == null ) {
            coll.setDigestAlgorithm("SHA-256");
        }
        if ( coll.getStorage() == null ) {
            coll.setStorage("local");
        }
    }

    // Note: There should be a requirement that all the collections in the list
    // share the same name. I'll add it in the conditional just in case 
    private boolean checkGroupCollision(List<Collection> colls, Collection coll) {
        for ( Collection c : colls ) {
            if ( coll.getGroup().equals(c.getGroup()) &&
                 coll.getName().equals(c.getName())) {
                return true;
            }
        } 
        return false;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @RolesAllowed("Collection Modify")
    public Response addCollection(Collection coll){
        checkCollection(coll);
        EntityManager em = PersistUtil.getEntityManager();

        // Check against the name collection name in the group. This could 
        // make things very confusing in the interface
        Query q = em.createNamedQuery("Collection.getCollectionByName");
        q.setParameter("name", coll.getName());
        List<Collection> colls = q.getResultList();
        if ( checkGroupCollision(colls, coll)) {
            em.close();
            return Response.status(Status.CONFLICT).build();
        }
    
        EntityTransaction trans = em.getTransaction();
        trans.begin();
        em.persist(coll);
        trans.commit();
        return Response.status(Status.OK).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("xml/{id}")
    @RolesAllowed("Browse")
    public Collection getCollectionXML(@PathParam("id") long collId){
        EntityManager em = PersistUtil.getEntityManager();
        return em.find(Collection.class, collId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("json/{id}")
    @RolesAllowed("Browse")
    public Collection getCollectionJSON(@PathParam("id") long collId){
        EntityManager em = PersistUtil.getEntityManager();
        return em.find(Collection.class, collId);
    }


    /*
    @DELETE
    @Path("remove/{id}")
    public void removeCollection(@PathParam("id") long collId) {
        EntityManager em = PersistUtil.getEntityManager();
        em.remove(em.find(Collection.class, collId));
        em.close();
    }
     *
     */
}
