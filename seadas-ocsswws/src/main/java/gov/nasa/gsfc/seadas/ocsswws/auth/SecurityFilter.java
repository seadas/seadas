package gov.nasa.gsfc.seadas.ocsswws.auth;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.security.Principal;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/11/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SecurityFilter implements ContainerRequestFilter {

    @Context
    UriInfo uriInfo;
    private static final String REALM = "HTTPS Example authentization";

    public ContainerRequest filter(ContainerRequest request) {
        User user = authenticate(request);
        request.setSecurityContext(new Authorizer(user));
        return request;
    }

    private User authenticate(ContainerRequest request) {
        // Extract authentication credentials
        String authentication = request.getHeaderValue(ContainerRequest.AUTHORIZATION);
        if (authentication == null) {
            throw new MappableContainerException
                    (new AuthenticationException("Authentication credentials are required", REALM));
        }
        if (!authentication.startsWith("Basic ")) {
            return null;
            // additional checks should be done here
            // "Only HTTP Basic authentication is supported"
        }
        authentication = authentication.substring("Basic ".length());
        String[] values = new String(Base64.base64Decode(authentication)).split(":");
        if (values.length < 2) {
            throw new WebApplicationException(400);
            // "Invalid syntax for username and password"
        }
        String username = values[0];
        String password = values[1];
        if ((username == null) || (password == null)) {
            throw new WebApplicationException(400);
            // "Missing username or password"
        }

        // Validate the extracted credentials
        User user = null;

        if (username.equals("user") && password.equals("password")) {
            user = new User("user", "user");
            System.out.println("USER AUTHENTICATED");
        //        } else if (username.equals("admin") && password.equals("adminadmin")) {
        //            user = new User("admin", "admin");
        //            System.out.println("ADMIN AUTHENTICATED");
        } else {
            System.out.println("USER NOT AUTHENTICATED");
            throw new MappableContainerException(new AuthenticationException("Invalid username or password\r\n", REALM));
        }
        return user;
    }

    public class Authorizer implements SecurityContext {

        private User user;
        private Principal principal;

        public Authorizer(final User user) {
            this.user = user;
            this.principal = new Principal() {

                public String getName() {
                    return user.username;
                }
            };
        }

        public Principal getUserPrincipal() {
            return this.principal;
        }

        public boolean isUserInRole(String role) {
            return (role.equals(user.role));
        }

        public boolean isSecure() {
            return "https".equals(uriInfo.getRequestUri().getScheme());
        }

        public String getAuthenticationScheme() {
            return SecurityContext.BASIC_AUTH;
        }
    }

    public class User {

        public String username;
        public String role;

        public User(String username, String role) {
            this.username = username;
            this.role = role;
        }
    }
}

