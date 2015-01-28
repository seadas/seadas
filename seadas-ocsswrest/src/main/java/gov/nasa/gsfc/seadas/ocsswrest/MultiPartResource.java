package gov.nasa.gsfc.seadas.ocsswrest;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 12/29/14
 * Time: 4:12 PM
 * To change this template use File | Settings | File Templates.
 */

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXB;
import java.io.File;
import java.io.InputStream;

/**
 * MultiPart resource
 *
 * @author Arul Dhesiaseelan (aruld@acm.org)
 */
@Path("multipart")
public class MultiPartResource {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    public Response processForm(@FormDataParam("xml") InputStream is, @FormDataParam("xml") FormDataContentDisposition header) {
        System.out.println("Processing file # " + header.getFileName());
        File entity = JAXB.unmarshal(is, File.class);
//        entity.setUid(UUID.randomUUID().toString());
        return Response.ok(entity).build();
    }

}