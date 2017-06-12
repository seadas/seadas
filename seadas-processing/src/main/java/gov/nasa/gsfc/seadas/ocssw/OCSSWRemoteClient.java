package gov.nasa.gsfc.seadas.ocssw;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.processing.common.SeadasProcess;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ParamList;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.json.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Iterator;

/**
 * Created by aabduraz on 3/27/17.
 */
public class OCSSWRemoteClient extends OCSSW {

    public static final String OCSSW_SERVER_PORT_NUMBER = "6401";
    public static final String SEADAS_CLIENT_ID_PROPERTY = "client.id";

    WebTarget target;
    String jobId;
    boolean ifileUploadSuccess;
    String ofileName;


    public OCSSWRemoteClient() {
        initiliaze();
    }

    private void initiliaze() {
        String remoteServerIPAddress = RuntimeContext.getConfig().getContextProperty(OCSSW_LOCATION_PROPERTY, "localhost");
        String remoteServerPortNumber = OCSSW_SERVER_PORT_NUMBER;
        OCSSWClient ocsswClient = new OCSSWClient(remoteServerIPAddress, remoteServerPortNumber);
        target = ocsswClient.getOcsswWebTarget();
        JsonObject jsonObject = target.path("ocssw").path("ocsswInfo").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        ocsswExist = jsonObject.getBoolean("ocsswExists");
        ocsswRoot = jsonObject.getString("ocsswRoot");
        if (ocsswExist) {
            jobId = target.path("jobs").path("newJobId").request(MediaType.TEXT_PLAIN_TYPE).get(String.class);
            String clientId = RuntimeContext.getConfig().getContextProperty(SEADAS_CLIENT_ID_PROPERTY, System.getProperty("user.home"));
            target.path("ocssw").path("ocsswSetClientId").path(jobId).request().put(Entity.entity(clientId, MediaType.TEXT_PLAIN_TYPE));
        }
    }

    @Override
    public void setProgramName(String programName) {

        this.programName = programName;
        Response response  = target.path("ocssw").path("ocsswSetProgramName").path(jobId).request().put(Entity.entity(programName, MediaType.TEXT_PLAIN_TYPE));
    }


    @Override
    public void setIfileName(String ifileName) {
        this.ifileName = ifileName;
        if (uploadIFile(ifileName)){
            ifileUploadSuccess = true;
        }
        else {
            ifileUploadSuccess = false;
        }

    }

    private void updateProgramName(String programName){
        this.programName = programName;
        setXmlFileName(programName+".xml");
    }


    public boolean uploadIFile(String ifileName){
        final FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file", new File(ifileName));
        final MultiPart multiPart = new FormDataMultiPart()
                //.field("ifileName", ifileName)
                .bodyPart(fileDataBodyPart);
        Response response = target.path("fileServices").path("uploadFile").path(jobId).request().post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));
        if (response.getStatus() == Response.ok().build().getStatus()) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public String getOfileName(String ifileName) {

        if (programName.equals("l3bindump")) {
            return ifileName + ".xml";
        }
        this.setIfileName(ifileName);

        if (ifileUploadSuccess) {
            JsonObject jsonObject = target.path("ocssw").path("getOfileName").path(jobId).request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
            ofileName = jsonObject.getString("ofileName");
            missionName = jsonObject.getString("missionName");
            fileType = jsonObject.getString("fileType");
            updateProgramName( jsonObject.getString("programName") );
            ofileName = ifileName.substring( 0, ifileName.lastIndexOf(File.separator) +1 ) + ofileName;
            return ofileName;
        } else {
            return null;
        }

    }

    @Override
    public String getOfileName(String ifileName, String[] options) {
        target.path("ocssw").path("updateOFileAdditionalOptions").request(MediaType.APPLICATION_JSON).put(Entity.entity(options, MediaType.APPLICATION_JSON));
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
    public Process execute(ParamList paramListl) {
        JsonObject commandArrayJsonObject = getJsonFromParamList(paramListl);
        Response response = target.path("ocssw").path("executeOcsswProgram").path(jobId).request().put(Entity.entity(commandArrayJsonObject, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            Response output = target.path("fileServices").path("downloadFile").path(jobId).request().get(Response.class);
            final InputStream responseStream = target.path("ocssw").path("downloadFile").path(jobId).request().get(InputStream.class);
            writeToFile(responseStream, ofileName);
        }
        Process seadasProcess = new SeadasProcess();
        return  seadasProcess;
    }

    // save uploaded file to new location
    private void writeToFile(InputStream uploadedInputStream,
                             String uploadedFileLocation) {

        try {
            File file = new File(uploadedFileLocation);
            OutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            byte[] bytes = new byte[8192];

            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            uploadedInputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private JsonObject getJsonFromParamList(ParamList paramList) {

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();


        Iterator itr = paramList.getParamArray().iterator();

        ParamInfo option;
        String commandItem;
        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();
            commandItem = null;
            if (option.getType() != ParamInfo.Type.HELP) {
                if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_ARGUMENT)) {
                    if (option.getValue() != null && option.getValue().length() > 0) {
                        commandItem = option.getValue();
                    }
                } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_OPTION) && !option.getDefaultValue().equals(option.getValue())) {
                    commandItem = option.getName() + "=" + option.getValue();
                } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_FLAG) && (option.getValue().equals("true") || option.getValue().equals("1"))) {
                    if (option.getName() != null && option.getName().length() > 0) {
                        commandItem = option.getName();
                    }
                }
            }
            //need to send both item name and its type to accurately construct the command array on the server
            if (commandItem != null) {
                jsonObjectBuilder.add(option.getName() + "_" + option.getType(), commandItem);
            }

        }
        return jsonObjectBuilder.build();
    }

    private JsonArray getJsonFromParamListOld(ParamList paramList) {
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

