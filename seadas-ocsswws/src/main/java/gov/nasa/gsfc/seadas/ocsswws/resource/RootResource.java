package gov.nasa.gsfc.seadas.ocsswws.resource;

import com.sun.jersey.core.util.Base64;
import gov.nasa.gsfc.seadas.ocsswws.Server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.*;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/12/13
 * Time: 9:30 AM
 * To change this template use File | Settings | File Templates.
 */
@Path("/")
public class RootResource {

    @Context
    SecurityContext securityContext;

    @Context
    UriInfo uriInfo;

//    public RootResource(@Context SecurityContext securityContext) {
//            // this is ok too: the proxy of SecurityContext will be injected
//        }
    @GET
    public Response get1(@Context HttpHeaders headers) {
        // you can get username form HttpHeaders
        System.out.println("Service: GET / User: " + getUser(headers));
         headers.getRequestHeaders();
        Properties systemProperties = System.getProperties();
        return Response.ok(Server.CONTENT).type(MediaType.TEXT_HTML).build();
    }

    private String getUser(HttpHeaders headers) {

        // this is a very minimalistic and "naive" code; if you plan to use it
        // add necessary checks (see com.sun.jersey.samples.https_grizzly.auth.SecurityFilter)
        String auth = headers.getRequestHeader("authorization").get(0);

        auth = auth.substring("Basic ".length());
        String[] values = new String(Base64.base64Decode(auth)).split(":");

        // String username = values[0];
        // String password = values[1];

        return values[0];
    }
}
