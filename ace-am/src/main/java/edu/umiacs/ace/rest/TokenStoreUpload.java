/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.rest;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.register.IngestThreadPool;
import edu.umiacs.ace.token.TokenStoreReader;
import edu.umiacs.ace.util.PersistUtil;
import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

/**
 * Service for manipulating whole token stores
 *
 * @author toaster
 */
@Path("tokenstore")
public class TokenStoreUpload {
    
    private static final Logger LOG = Logger.getLogger(TokenStoreUpload.class);
    @POST
    @Path("{collectionid}")
    public Response loadTokenStore(@PathParam("collectionid") long collectionId,
                                   @Context HttpServletRequest request) {
        EntityManager em = PersistUtil.getEntityManager();
        LOG.info("Form Field: " + collectionId);
        if (ServletFileUpload.isMultipartContent(request)) { 
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            
            List<FileItem> items = null;
            try {
                items = upload.parseRequest(request);
                LOG.info("item size: " + items.size());
                for (FileItem item : items) {
                    LOG.info("item " + item.getSize() + " " + item.getName() + " " + item.getContentType() ) ;
                    Collection coll = em.find(Collection.class, collectionId);
                    TokenStoreReader reader = new TokenStoreReader(item.getInputStream());
                    IngestThreadPool.submitTokenStore(reader, coll);
                }
                return Response.ok().build();
            }
            catch (FileUploadException e) {
                LOG.error("Error parsing token store upload",e);
                return Response.serverError().build();
            } catch (IOException ex) {
                LOG.error("Error reading token store", ex);
                return Response.serverError().build();
            }
            
        }
        LOG.info("No file included, returning error");
        return Response.status(Status.NO_CONTENT).build();
    }
}
