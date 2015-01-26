package gov.nasa.gsfc.seadas.processing.core;

import org.glassfish.grizzly.http.server.HttpServer;

import javax.ws.rs.client.WebTarget;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 1/15/15
 * Time: 1:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class OCSSWWSClient {

    public static final String RESOURCE_BASE_URI = "http://localhost:6400/ocsswws/";

        private HttpServer server;
        private WebTarget target;


}
