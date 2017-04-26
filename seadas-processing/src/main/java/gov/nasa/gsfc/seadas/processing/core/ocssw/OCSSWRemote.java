package gov.nasa.gsfc.seadas.processing.core.ocssw;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ParamList;

import javax.json.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Iterator;

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
        JsonObject jsonObject = target.path("ocssw").path("ocsswInstallStatus").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        ocsswExist = jsonObject.getBoolean("ocsswExists");
        ocsswRoot = jsonObject.getString("ocsswRoot");
    }

    @Override
    public void setProgramName(String programName) {
        this.programName = programName;
        target.path("ocssw").path("ocsswSetProgramName").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        setCommandArrayPrefix();
        setCommandArraySuffix();
    }

    @Override
    public String getOfileName(String ifileName) {
        JsonObject jsonObject = target.path("ocssw").path("getOfileName").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        String ofileName = jsonObject.getString("ofileName");
        missionName = jsonObject.getString("missionName");
        fileType = jsonObject.getString("fileType");
        return ofileName;
    }

    @Override
    public String getOfileName(String ifileName, String[] options) {
        JsonObject jsonObject = target.path("ocssw").path("ocsswInstallStatus").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        String ofileName = jsonObject.getString("ocsswExists");
        return ofileName;
    }

    private JsonArray transformToJsonArray(String[] commandArray){

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (String option:commandArray){
            jsonArrayBuilder.add(option);
        }

        return jsonArrayBuilder.build();
    }

    private String getOfileName(JsonArray jsonArray) {
        JsonObject jsonObject = target.path("ocssw").path("getOfileName").path(OCSSWOldModel.getJobId()).request(MediaType.APPLICATION_JSON).put(Entity.entity(jsonArray, MediaType.APPLICATION_JSON),JsonObject.class);
        String ofileName = jsonObject.getString("ofileName");
        missionName = jsonObject.getString("missionName");
        fileType = jsonObject.getString("fileType");
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
    public Process execute(ParamList paramListl) {
            return  target.path("executeCommandArray").request(MediaType.APPLICATION_XML).put(Entity.xml(paramListl), Process.class);
    }

    private JsonArray getJsonFromParamList(ParamList paramList) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        Iterator itr = paramList.getParamArray().iterator();

        ParamInfo option;
        String optionValue;
        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();
            optionValue = option.getValue();
            if (option.getType() != ParamInfo.Type.HELP) {
                if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_ARGUMENT)) {
                    if (option.getValue() != null && option.getValue().length() > 0) {
                        jsonArrayBuilder.add(optionValue);
                    }
                } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_OPTION) && !option.getDefaultValue().equals(option.getValue())) {
                    jsonArrayBuilder.add(option.getName() + "=" + optionValue);
                } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_FLAG) && (option.getValue().equals("true") || option.getValue().equals("1"))) {
                    if (option.getName() != null && option.getName().length() > 0) {
                        jsonArrayBuilder.add(option.getName());
                    }
                }
            }
        }
        JsonArray jsonCommandArray = jsonArrayBuilder.build();
        return jsonCommandArray;
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

