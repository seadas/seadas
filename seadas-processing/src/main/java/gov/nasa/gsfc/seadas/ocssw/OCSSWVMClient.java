package gov.nasa.gsfc.seadas.ocssw;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.core.ParamList;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * Created by aabduraz on 3/27/17.
 */
public class OCSSWVMClient extends OCSSWRemoteClient {
    public final static String OCSSW_VM_SERVER_SHARED_DIR_PROPERTY = "ocssw.sharedDir";
    public final static String OCSSW_VM_SERVER_SHARED_DIR_PROPERTY_DEFAULT_VALUE = System.getProperty("user.home") + File.separator + "ocsswVMServerSharedDir";

    String sharedDirPath;

    public OCSSWVMClient(){
        this.initialize();
    }

    private void initialize(){
        sharedDirPath = RuntimeContext.getConfig().getContextProperty(OCSSW_VM_SERVER_SHARED_DIR_PROPERTY, OCSSW_VM_SERVER_SHARED_DIR_PROPERTY_DEFAULT_VALUE);
        String remoteServerIPAddress = RuntimeContext.getConfig().getContextProperty(OCSSW_LOCATION_PROPERTY, "localhost");
        String remoteServerPortNumber = RuntimeContext.getConfig().getContextProperty(OCSSW_SERVER_PORT_PROPERTY, OCSSW_VIRTUAL_SERVER_PORT_FORWWARD_NUMBER_FOR_CLIENT);
        OCSSWClient ocsswClient = new OCSSWClient(remoteServerIPAddress, remoteServerPortNumber);
        target = ocsswClient.getOcsswWebTarget();
        JsonObject jsonObject = target.path("ocssw").path("ocsswInfo").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        ocsswExist = jsonObject.getBoolean("ocsswExists");
        ocsswRoot = jsonObject.getString("ocsswRoot");
        if (ocsswExist) {
            jobId = target.path("jobs").path("newJobId").request(MediaType.TEXT_PLAIN_TYPE).get(String.class);
            String clientId = RuntimeContext.getConfig().getContextProperty(SEADAS_CLIENT_ID_PROPERTY, System.getProperty("user.home"));
            target.path("ocssw").path("ocsswSetClientId").path(jobId).request().put(Entity.entity(clientId, MediaType.TEXT_PLAIN_TYPE));
            ocsswDataDirPath = jsonObject.getString("ocsswDataDirPath");
            ocsswInstallerScriptPath = jsonObject.getString("ocsswInstallerScriptPath");
            ocsswRunnerScriptPath = jsonObject.getString("ocsswRunnerScriptPath");
            ocsswScriptsDirPath = jsonObject.getString("ocsswScriptsDirPath");
        }
    }

    public boolean uploadIFile(String ifileName) {

        if (true) {
            return true;
        } else {
            return false;
        }
    }
}
