package gov.nasa.gsfc.seadas.processing.core.ocssw;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.processing.core.ParamList;

import javax.json.JsonObject;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * Created by aabduraz on 3/27/17.
 */
public class OCSSWRemote extends OCSSW {

    public static final String OCSSW_SERVER_PORT_NUMBER = "6401";

    WebTarget target;


    public OCSSWRemote() {
        initiliaze();
    }

    private void initiliaze() {
        String remoteServerIPAddress = RuntimeContext.getConfig().getContextProperty(OCSSW.OCSSW_LOCATION_PROPERTY);
        String remoteServerPortNumber = OCSSW_SERVER_PORT_NUMBER;
        OCSSWClient ocsswClient = new OCSSWClient(remoteServerIPAddress, remoteServerPortNumber);
        target = ocsswClient.getOcsswWebTarget();
    }


    @Override
    public boolean isOCSSWExist() {
        JsonObject jsonObject = target.path("ocssw").path("ocsswInstallStatus").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        boolean ocsswExist = jsonObject.getBoolean("ocsswExists");
        return ocsswExist;
    }


    @Override
    public String getOcsswInstallerScriptPath() {
        JsonObject jsonObject = target.path("ocssw").path("ocsswInstallStatus").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        String ocsswInstallScriptPath = jsonObject.getString("ocsswExists");
        return ocsswInstallScriptPath;
    }


    @Override
    public String getOcsswInstallDirPath() {
        JsonObject jsonObject = target.path("ocssw").path("ocsswInstallStatus").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        String ocsswInstallDirPath = jsonObject.getString("ocsswExists");
        return ocsswInstallDirPath;
    }

    @Override
    public String getOcsswRunnerScriptPath() {
        JsonObject jsonObject = target.path("ocssw").path("ocsswInstallStatus").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        String ocsswRunnerScriptPath = jsonObject.getString("ocsswExists");
        return ocsswRunnerScriptPath;
    }


    @Override
    public String getOcsswScriptsDirPath() {
        JsonObject jsonObject = target.path("ocssw").path("ocsswInstallStatus").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        String ocsswScriptsDirPath = jsonObject.getString("ocsswExists");
        return ocsswScriptsDirPath;
    }



    @Override
    public String getOcsswDataDirPath() {
        JsonObject jsonObject = target.path("ocssw").path("ocsswInstallStatus").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        String ocsswDataDirPath = jsonObject.getString("ocsswExists");
        return ocsswDataDirPath;
    }



    @Override
    public String getOfileName(String ifileName) {
        JsonObject jsonObject = target.path("ocssw").path("ocsswInstallStatus").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        String ofileName = jsonObject.getString("ocsswExists");
        return ofileName;
    }

    @Override
    public String getOfileName(String ifileName, String[] options) {
        JsonObject jsonObject = target.path("ocssw").path("ocsswInstallStatus").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        String ofileName = jsonObject.getString("ocsswExists");
        return ofileName;
    }

    @Override
    public String getFileType(String ifileName) {
        JsonObject jsonObject = target.path("ocssw").path("ocsswInstallStatus").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        String fileType = jsonObject.getString("ocsswExists");
        return fileType;
    }

    @Override
    public String getMissionName(String ifileName) {
        JsonObject jsonObject = target.path("ocssw").path("ocsswInstallStatus").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        String missionName = jsonObject.getString("ocsswExists");
        return missionName;
    }


    @Override
    public void execute(ParamList paramListl) {

    }

    @Override
    public Process execute(String[] commandArray) {
        return null;
    }

    @Override
    public void setOcsswDataDirPath(String ocsswDataDirPath) {

    }

    @Override
    public void setOcsswInstallDirPath(String ocsswInstallDirPath) {

    }


    @Override
    public void setOcsswScriptsDirPath(String ocsswScriptsDirPath) {

    }


    @Override
    public void setOcsswInstallerScriptPath(String ocsswInstallerScriptPath) {

    }

    @Override
    public void setCommandArrayPrefix() {

    }

    @Override
    public void setCommandArraySuffix() {

    }

    @Override
    public void setMissionName(String missionName) {

    }

    @Override
    public void setFileType(String fileType) {

    }
}

