package gov.nasa.gsfc.seadas;


import com.bc.ceres.core.runtime.RuntimeContext;
import org.esa.beam.visat.VisatApp;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.json.stream.JsonGenerator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 12/9/14
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class OCSSWClient {

    boolean ocsswExist;
    String ocsswRoot;

    public static final String OCSSW_LOCATION_PROPERTY = "ocssw.location";
    public static final String OCSSW_LOCATION_PROPERTY_VALUE_LOCAL ="local";

    final static String BASE_URI_PORT_NUMBER_PROPERTY = "baseUriPortNumber";

    //public static final String RESOURCE_BASE_URI = RuntimeContext.getConfig().getContextProperty(OCSSW.OCSSW_LOCATION_PROPERTY);//"http://localhost:6400/ocsswws/";
    public static final String defaultServer ="localhost";
    public static final String defaultPort = "6400";
    final static String OCSSW_REST_SERVICES_CONTEXT_PATH = "ocsswws";

    private WebTarget target;
    private String resourceBaseUri;

    public OCSSWClient(String serverIPAddress, String portNumber) {
        resourceBaseUri = getResourceBaseUri(serverIPAddress, portNumber);
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(MultiPartFeature.class);
        clientConfig.register(JsonProcessingFeature.class).property(JsonGenerator.PRETTY_PRINTING, true);
        Client c = ClientBuilder.newClient(clientConfig);
        target = c.target(resourceBaseUri);
    }

    public OCSSWClient(){
      this(defaultServer, defaultPort);
    }

    public WebTarget getOcsswWebTarget() {
        return target;
    }

    private String getResourceBaseUri(String serverIPAddress, String portNumber){
        String resourceBaseUri = "http://" + serverIPAddress + ":" + portNumber + "/" + OCSSW_REST_SERVICES_CONTEXT_PATH + "/";
        return  resourceBaseUri;
    }


    private boolean isOCSSWExist() {

        String ocsswLocation = RuntimeContext.getConfig().getContextProperty(OCSSW_LOCATION_PROPERTY);
        if (ocsswLocation.equals(OCSSW_LOCATION_PROPERTY_VALUE_LOCAL)) {
           return isLocalOCSSWExist();
        } else {
            return isRemoteOCSSWExist();
        }

    }

    private boolean isLocalOCSSWExist(){
        String OCSSW_SCRIPTS_DIR_SUFFIX =  File.separator + "run" +  File.separator + "scripts";

            String dirPath = RuntimeContext.getConfig().getContextProperty("ocssw.root", System.getenv("OCSSWROOT"));

            if (dirPath == null) {
                dirPath = RuntimeContext.getConfig().getContextProperty("home", System.getProperty("user.home") + File.separator + "ocssw");
            }
            if (dirPath != null) {
                final File dir = new File(dirPath  + OCSSW_SCRIPTS_DIR_SUFFIX);
                if (dir.isDirectory()) {
                    return true;
                }
            }

        return false;
    }

    private boolean isRemoteOCSSWExist(){
        String baseUriPortNumber = System.getProperty(BASE_URI_PORT_NUMBER_PROPERTY, "localhost");
        String serverAPI = System.getProperty("serverAPI");
        String baseUri = "http://"+ serverAPI + ":" + baseUriPortNumber + "/" + OCSSW_REST_SERVICES_CONTEXT_PATH + "/";
        return true;
    }
}
