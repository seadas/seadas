package gov.nasa.gsfc.seadas.ocsswrest;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;
import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWConfig;
import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWServerModel;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.ProcessMessageBodyWriter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.json.stream.JsonGenerator;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Main class.
 */
public class OCSSWRestServer extends ResourceConfig {
    final static String BASE_URI_PORT_NUMBER_PROPERTY = "baseUriPortNumber";
    final static String OCSSW_ROOT_PROPERTY ="ocsswroot";
    final static String OCSSW_REST_SERVICES_CONTEXT_PATH = "ocsswws";
    public final static String SERVER_WORKING_DIRECTORY_PROPERTY = "serverWorkingDirectory";
    final static String KEEP_INTERMEDIATE_FILES_ON_SERVER_PROPERTY ="keepIntermediateFilesOnServer";
    final static String SERVER_API="0.0.0.0";

    static String configFilePath="./config/ocsswservertest.config";
    static String baseUriPortNumber;
    static String ocsswroot;
    static String serverWorkingDirectory;
    static String keepIntermediateFilesOnServer;

    // Base URI the Grizzly HTTP server will listen on
    public static String BASE_URI = "http://0.0.0.0:6401/ocsswws/";
    private static final Logger LOGGER = Logger.getLogger(OCSSWRestServer.class.getName());


    public OCSSWRestServer(String ocsswroot) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ocsswroot", ocsswroot);
        setProperties(properties);
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     *
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig(MultiPartResource.class);
        resourceConfig.register(MultiPartFeature.class);
        resourceConfig.register(InputStream.class);
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
        String fileName = args[0];
        System.out.println("argument: " + fileName);
        OCSSWConfig ocsswConfig = new OCSSWConfig(fileName);
        baseUriPortNumber = System.getProperty(BASE_URI_PORT_NUMBER_PROPERTY);
        BASE_URI = "http://"+ SERVER_API + ":" + baseUriPortNumber + "/" + OCSSW_REST_SERVICES_CONTEXT_PATH + "/";
        SQLiteJDBC.createTables();
        OCSSWServerModel.initiliaze();
        System.out.println(String.format("ORS is starting at ", BASE_URI));
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey new app started with WADL available at "
                + "%sapplication.wadl\nPress 'Ctrl' + 'C'  to stop it...", BASE_URI));
       // System.in.read();
       //server.shutdown();
    }
}

