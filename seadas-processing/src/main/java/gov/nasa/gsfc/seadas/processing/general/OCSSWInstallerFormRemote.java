package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.OCSSWClient;
import org.esa.beam.framework.ui.AppContext;

import javax.ws.rs.client.WebTarget;
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
        ocsswClient = new OCSSWClient();
        target = ocsswClient.getOcsswWebTarget();
    }

    private void updateMissionValues() {
        Response response = target.path("ocssw").path("missions").request().get();
        missionDataStatus = (HashMap<String, Boolean>) response.getEntity();
        for (Map.Entry<String, Boolean> entry : missionDataStatus.entrySet()) {
            String missionName = entry.getKey();
            Boolean missionStatus = entry.getValue();

            if (missionStatus) {
                processorModel.setParamValue("--" + missionName.toLowerCase(), "1");
            }

        }
        if ((Boolean) target.path("ocssw").path("eval").request().get().getEntity()) {
            processorModel.setParamValue("--eval", "1");
        }
        if ((Boolean) target.path("ocssw").path("eval").request().get().getEntity()) {
            processorModel.setParamValue("--src", "1");
        }
    }

    private String getInstallDir() {
        return (String) target.path("ocssw").path("installDir").request().get().getEntity();
        //return "${user.home}/ocssw";
    }
}
