/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.rest;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
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
            @Context HttpServletRequest request)
    {
        LOG.info("Form Field: " + collectionId);
        if (ServletFileUpload.isMultipartContent(request))
        {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            List<FileItem> items = null;
            try {
                items = upload.parseRequest(request);
                LOG.info("item size: " + items.size());
                for (FileItem item : items)
                {
                    LOG.info("item " + item.getSize() + " " + item.getName() + " " + item.getContentType() ) ;
                }
                return Response.ok().build();
            }
            catch (FileUploadException e)
            {
              LOG.error("Error parsing token store upload",e);
              return Response.serverError().build();
            }

        }
        LOG.info("No file included, returning error");
        return Response.status(Status.OK).build();
    }
}
