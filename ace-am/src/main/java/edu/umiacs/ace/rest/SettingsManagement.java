/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.rest;

import com.sun.istack.logging.Logger;
import edu.umiacs.ace.driver.irods.IrodsSetting;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.PersistUtil;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author shake
 */
@Path("settings")
public class SettingsManagement {
    private static final Logger LOG = Logger.getLogger(SettingsManagement.class);
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("irods/{collId}")
    public Response iRODSModify(IrodsSetting is, @PathParam("collId") long collId) {
        EntityManager em = PersistUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        trans.begin();

        // check our collection first
        Collection coll = em.find(Collection.class, collId);
        if ( !coll.getStorage().equals("irods")) {
            coll.setStorage("irods");
            em.persist(coll);
        }

        Query q = em.createNamedQuery("IrodsSettings.getByCollection");
        q.setParameter("coll", coll);
        try {
            // Make sure we aren't changing the collection
            // and update the id just in case
            IrodsSetting oldSetting = (IrodsSetting) q.getSingleResult();
            if ( oldSetting.getCollection().getId() != collId ) {
                return Response.notModified().build();
            }

            is.setId(oldSetting.getId());
        } catch (NoResultException ex) {
            LOG.info("Adding new irods settings for collection " + coll.getName());
        } finally {
            is.setCollection(coll);
            em.merge(is);
            trans.commit();
        }
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("irods/{collId}")
    public IrodsSetting getIRODSSettings(@PathParam("collId") long collId) {
        EntityManager em = PersistUtil.getEntityManager();
        Collection coll = em.find(Collection.class, collId);
        Query q = em.createNamedQuery("IrodsSettings.getByCollection");
        q.setParameter("coll", coll);
        return (IrodsSetting) q.getSingleResult();
    }
}
