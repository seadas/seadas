package gov.nasa.gsfc.seadas.ocsswrest;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;
import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWServerModel;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.OCSSWServerModelOld;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.ProcessMessageBodyWriter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.json.stream.JsonGenerator;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Main class.
 */
public class OCSSWRestServer {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://0.0.0.0:6401/ocsswws/";
    private static final Logger LOGGER = Logger.getLogger(OCSSWRestServer.class.getName());

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     *
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig(MultiPartResource.class);
        resourceConfig.registerInstances(new LoggingFilter(LOGGER, true));
        resourceConfig.register(MultiPartFeature.class);
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(ProcessMessageBodyWriter.class);
        resourceConfig.register(JsonProcessingFeature.class).property(JsonGenerator.PRETTY_PRINTING, true);
        resourceConfig.packages("gov.nasa.gsfc.seadas.ocsswrest");
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), resourceConfig);
    }

    /**
     * Main method.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        SQLiteJDBC.createTables();
        OCSSWServerModelOld.init();
        OCSSWServerModel.initiliaze();
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey new app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.shutdown();
    }
}

