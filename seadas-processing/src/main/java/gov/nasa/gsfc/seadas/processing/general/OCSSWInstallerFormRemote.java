package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.OCSSWClient;
import org.esa.beam.framework.ui.AppContext;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 1/20/15
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class OCSSWInstallerFormRemote extends OCSSWInstallerForm {

    OCSSWClient ocsswClient;
    WebTarget target;

    OCSSWInstallerFormRemote(AppContext appContext, String programName, String xmlFileName) {
        super(appContext, programName, xmlFileName);
    }

    void init(){
        ocsswClient = new OCSSWClient();
        final ResourceConfig rc = new ResourceConfig();

        target = new OCSSWClient().getOcsswWebTarget();
    }

    @Override
    void updateMissionStatus(){

    }

    @Override
    void updateMissionValues() {

        Response response0 = target.path("file").path("test").request().get();
        response0 = target.path("ocssw").path("missions").request().get();
        missionDataStatus = target.path("ocssw").path("missions").request(MediaType.APPLICATION_JSON)
                                          .get(new GenericType<HashMap<String, Boolean>>() {});
        //missionDataStatus = (HashMap<String, Boolean>) response.getEntity();
        for (Map.Entry<String, Boolean> entry : missionDataStatus.entrySet()) {
            String missionName = entry.getKey();
            Boolean missionStatus = entry.getValue();

            if (missionStatus) {
                processorModel.setParamValue("--" + missionName.toLowerCase(), "1");
            }

        }

        HashMap<String, Boolean> ocsswStatus = target.path("ocssw").path("evalDirInfo").request(MediaType.APPLICATION_JSON).get(new GenericType<HashMap<String, Boolean>>() {});
        if ( target.path("ocssw").path("evalDirInfo").request(MediaType.APPLICATION_JSON).get(new GenericType<HashMap<String, Boolean>>() {}).get("eval")) {
            processorModel.setParamValue("--eval", "1");
        }
        if (target.path("ocssw").path("srcDirInfo").request(MediaType.APPLICATION_JSON).get(new GenericType<HashMap<String, Boolean>>() {}).get("build")) {
            processorModel.setParamValue("--src", "1");
        }
    }

    @Override
    String getInstallDir() {
        return target.path("ocssw").path("installDir").request(MediaType.TEXT_PLAIN).get(String.class);
    }
}
