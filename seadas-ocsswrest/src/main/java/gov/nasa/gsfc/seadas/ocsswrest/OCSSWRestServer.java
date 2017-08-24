package gov.nasa.gsfc.seadas.ocsswrest;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;
import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWConfig;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Main class.
 */
public class OCSSWRestServer {
    final static String BASE_URI_PORT_NUMBER_PROPERTY = "baseUriPortNumber";
    final static String OCSSW_ROOT_PROPERTY ="ocsswroot";
    final static String OCSSW_REST_SERVICES_CONTEXT_PATH = "ocsswws";
    final static String SERVER_WORKING_DIRECTORY_PROPERTY = "serverWorkingDirectory";
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

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     *
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig(MultiPartResource.class);
        resourceConfig.registerInstances(new LoggingFilter(LOGGER, true));
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
        OCSSWConfig ocsswConfig = new OCSSWConfig(fileName);
        baseUriPortNumber = System.getProperty(BASE_URI_PORT_NUMBER_PROPERTY);
        BASE_URI = "http://"+ SERVER_API + ":" + baseUriPortNumber + "/" + OCSSW_REST_SERVICES_CONTEXT_PATH + "/";
        SQLiteJDBC.createTables();
        OCSSWServerModel.initiliaze();
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey new app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.shutdown();
    }

    private static void readConfigFile(){
        //load application properties
        Properties configProperties = new Properties(System.getProperties());

        try {
            //load the file handle for main.properties
            FileInputStream fileInputStream = new FileInputStream(configFilePath);
            configProperties.load(fileInputStream);
            fileInputStream.close();
            // set the system properties
            System.setProperties(configProperties);
            // display new properties
            System.getProperties().list(System.out);
            baseUriPortNumber = configProperties.getProperty(BASE_URI_PORT_NUMBER_PROPERTY);
            ocsswroot = configProperties.getProperty(OCSSW_ROOT_PROPERTY);
            serverWorkingDirectory = configProperties.getProperty(SERVER_WORKING_DIRECTORY_PROPERTY);
            keepIntermediateFilesOnServer = configProperties.getProperty(KEEP_INTERMEDIATE_FILES_ON_SERVER_PROPERTY);

        } catch (FileNotFoundException fnfe){

        } catch (IOException ioe){

        }

    }
}

