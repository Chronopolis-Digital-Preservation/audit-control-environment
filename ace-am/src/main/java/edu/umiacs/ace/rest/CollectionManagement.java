/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.rest;

import atg.taglib.json.util.JSONException;
import atg.taglib.json.util.JSONObject;
import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.driver.StorageDriverFactory;
import edu.umiacs.ace.monitor.access.CollectionCountContext;
import edu.umiacs.ace.monitor.audit.AuditThreadFactory;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.CollectionState;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.util.PersistUtil;
import org.apache.log4j.Logger;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

/**
 * REST Service for adding and viewing collections
 *
 * @author shake
 */
@Path("collection")
public class CollectionManagement {
    private static final Logger LOG =
            Logger.getLogger(CollectionManagement.class);

    @Context
    private HttpServletRequest request;

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
    @Path("audit/{id}")
    public Response startAudit(@PathParam("id") long id, @DefaultValue("false") @QueryParam("corrupt") String corrupt) {
        boolean auditCorrupt = Boolean.getBoolean(corrupt);
        EntityManager em = PersistUtil.getEntityManager();
        Collection c = em.find(Collection.class, id);
        if ( c == null ) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LOG.trace("[REST] Request to start audit on collection " + c.getName()
                + " from " + request.getRemoteHost());

        StorageDriver driver = StorageDriverFactory.createStorageAccess(c, em);

        // We use a null item so that the entire collection gets audited
        MonitoredItem[] item = null;
        if (auditCorrupt) {
            // todo: might be able to put this in some type of service class
            Query query = em.createNamedQuery("MonitoredItem.listLocalErrors");
            query.setParameter("coll", c);
            List<MonitoredItem> resList = query.getResultList();
            item = resList.toArray(new MonitoredItem[resList.size()]);
        }
        AuditThreadFactory.createThread(c, driver, true, item);
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("Collection Modify")
    @Path("modify/{id}")
    public Response modifyCollection(Collection coll, @PathParam("id") long id) {
        // For my sanity (and yours), we're only going to allow certain settings
        // to be modified. 
        // These are:
        //   - storage
        //   - directory
        //   - group 
        //   - general settings (audit period, etc)
        EntityManager em = PersistUtil.getEntityManager();
        Collection orig = em.find(Collection.class, id);
        if ( !orig.getName().equals(coll.getName()) ) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        if (null != coll.getStorage() ) {
            orig.setStorage(coll.getStorage());
        }

        if ( null != coll.getDirectory() ) {
            orig.setDirectory(coll.getDirectory());
        }

        // TODO: Make sure no collection is contained in the group first
        if ( null != coll.getGroup() ) {
            orig.setGroup(coll.getGroup());
        }

        orig.getSettings().putAll(coll.getSettings());

        EntityTransaction trans = em.getTransaction();
        trans.begin();
        em.merge(orig);
        trans.commit();
        em.close();
        
        return Response.status(Status.OK).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("Collection Modify")
    public Response addCollection(Collection coll){
        checkCollection(coll);
        EntityManager em = PersistUtil.getEntityManager();

        // Check against the name collection name in the group. This could 
        // make things very confusing in the interface
        // TODO: Why not just query name + group?
        Query q = em.createNamedQuery("Collection.getCollectionByName");
        q.setParameter("name", coll.getName());
        List<Collection> colls = q.getResultList();
        if ( checkGroupCollision(colls, coll) ) {
            em.close();
            return Response.status(Status.BAD_REQUEST).build();
        }

        coll.setState(CollectionState.NEVER);
        EntityTransaction trans = em.getTransaction();
        trans.begin();
        em.persist(coll);
        CollectionCountContext.incrementTotalCollections();
        trans.commit();
        em.close();

        JSONObject id = new JSONObject();
        try {
            id.put("id", coll.getId());
        } catch (JSONException ex) {
            return Response.serverError().build();
        }
        return Response.ok(id.toString(), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("settings/by-id/{id}")
    @RolesAllowed("Browse")
    public Collection getCollectionJSON(@PathParam("id") long collId){
        EntityManager em = PersistUtil.getEntityManager();
        return em.find(Collection.class, collId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("settings/by-name/{name}")
    @RolesAllowed("Browse")
    public Collection getCollection(@PathParam("name") String name) {
        return findCollection(name, null);
    }

    /**
     *
     * @param name - Name of the collection
     * @param group - Group the collection is in
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("settings/by-name/{name}/{group}")
    @RolesAllowed("Browse")
    public Collection getCollection(@PathParam("name") String name, 
                                    @PathParam("group") String group) {
        return findCollection(name, group);
    }

    private Collection findCollection(String name, String group) {
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("Collection.getCollectionByName");
        q.setParameter("name", name);
        List<Collection> cols = q.getResultList();
        em.close();
        for (Collection c : cols) { 
            String collGroup = c.getGroup();
            if ( collGroup == null ) {
                // Check if what we want is also null
                // Note: Nested if to avoid NPE in the following statement
                if ( group == null || group.isEmpty() ) {
                    return c;
                }
            } else if (collGroup.equals(group)) {
                return c;
            }
        }
        return null;
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
