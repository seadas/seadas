package gov.nasa.gsfc.seadas;

import com.bc.ceres.core.runtime.RuntimeContext;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.File;

/**
 * Created by aabduraz on 5/25/17.
 */
public class OCSSWInfo {
    static boolean ocsswExist;

    public static boolean isOCSSWExist(){
        return ocsswExist;
    }

    public static void checkOCSSW() {

        String ocsswLocation = RuntimeContext.getConfig().getContextProperty("ocssw.location");
        if (ocsswLocation.equals("local")) {
            ocsswExist = isLocalOCSSWExist();
        } else {
            ocsswExist = isRemoteOCSSWExist();
        }



    }

    private static boolean isLocalOCSSWExist(){
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

    private static boolean isRemoteOCSSWExist(){
        final String BASE_URI_PORT_NUMBER_PROPERTY = "ocssw.port";
        final String OCSSW_REST_SERVICES_CONTEXT_PATH = "ocsswws";
        String baseUriPortNumber = RuntimeContext.getConfig().getContextProperty(BASE_URI_PORT_NUMBER_PROPERTY, "6400");
        String serverAPI = RuntimeContext.getConfig().getContextProperty("ocssw.location", "locahost");
        String resourceBaseUri = "http://"+ serverAPI + ":" + baseUriPortNumber + "/" + OCSSW_REST_SERVICES_CONTEXT_PATH + "/";
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(MultiPartFeature.class);
        clientConfig.register(JsonProcessingFeature.class).property(JsonGenerator.PRETTY_PRINTING, true);
        Client c = ClientBuilder.newClient(clientConfig);
        WebTarget target = c.target(resourceBaseUri);
        JsonObject jsonObject = target.path("ocssw").path("ocsswInfo").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        boolean ocsswExist = jsonObject.getBoolean("ocsswExists");
        return ocsswExist;
    }
}
