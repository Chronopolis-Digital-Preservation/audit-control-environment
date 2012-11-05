/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.rest;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.PersistUtil;
import java.util.logging.Level;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;

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

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void addCollection(Collection coll){
        checkCollection(coll);
        EntityManager em = PersistUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        trans.begin();
        em.persist(coll);
        trans.commit();
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
    @RolesAllowed({"Browse"})
    public Collection getCollectionJSON(@PathParam("id") long collId){
        EntityManager em = PersistUtil.getEntityManager();
        return em.find(Collection.class, collId);
    }

}
