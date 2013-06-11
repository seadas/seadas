package gov.nasa.gsfc.seadas.ocsswws.auth;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/11/13
 * Time: 12:45 PM
 * To change this template use File | Settings | File Templates.
 */
@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

    public Response toResponse(AuthenticationException e) {
        if (e.getRealm() != null) {
            return Response.
                    status(Response.Status.UNAUTHORIZED).
                    header("WWW-Authenticate", "Basic realm=\"" + e.getRealm() + "\"").
                    type("text/plain").
                    entity(e.getMessage()).
                    build();
        } else {
            return Response.
                    status(Response.Status.UNAUTHORIZED).
                    type("text/plain").
                    entity(e.getMessage()).
                    build();
        }
    }

}
