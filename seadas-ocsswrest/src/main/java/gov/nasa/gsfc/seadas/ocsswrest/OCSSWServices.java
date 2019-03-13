package gov.nasa.gsfc.seadas.ocsswrest;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;
import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWRemoteImpl;
import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWServerModel;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import static gov.nasa.gsfc.seadas.ocsswrest.OCSSWRestServer.SERVER_WORKING_DIRECTORY_PROPERTY;
import static gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWRemoteImpl.*;
import static gov.nasa.gsfc.seadas.ocsswrest.process.ORSProcessObserver.PROCESS_ERROR_STREAM_FILE_NAME;
import static gov.nasa.gsfc.seadas.ocsswrest.process.ORSProcessObserver.PROCESS_INPUT_STREAM_FILE_NAME;
import static gov.nasa.gsfc.seadas.ocsswrest.utilities.ServerSideFileUtilities.debug;


/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 1/8/15
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */

@Path("/ocssw")
public class OCSSWServices {

    private static final String GET_OBPG_FILE_TYPE_PROGRAM_NAME = "get_obpg_file_type.py";
    private static String NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME = "next_level_name.py";
    private static String NEXT_LEVEL_FILE_NAME_TOKEN = "Output Name:";
    private static String FILE_TABLE_NAME = "FILE_TABLE";
    private static String MISSION_TABLE_NAME = "MISSION_TABLE";
    final static String CLIENT_SERVER_SHARED_DIR_PROPERTY = "clientServerSharedDir";

    private HashMap<String, Boolean> missionDataStatus;

    /**
     * This service empties client working directory on the server for each new connection from seadas application, then
     * returns ocssw information.
     * ocsswScriptsDirPath = ocsswRoot + File.separator + OCSSW_SCRIPTS_DIR_SUFFIX;
     * ocsswDataDirPath = ocsswRoot + File.separator +OCSSW_DATA_DIR_SUFFIX;
     * ocsswInstallerScriptPath = ocsswScriptsDirPath + System.getProperty("file.separator") + OCSSW_INSTALLER_PROGRAM;
     * ocsswRunnerScriptPath = ocsswScriptsDirPath + System.getProperty("file.separator") + OCSSW_RUNNER_SCRIPT;
     * ocsswBinDirPath
     *
     * @return
     */
    @GET
    @Path("/ocsswInfo/{seadasVersion}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getOcsswInfo(@PathParam("seadasVersion") String seadasVersion) {
        OCSSWServerModel.setSeadasVersion(seadasVersion);
        OCSSWServerModel.initiliaze();
        JsonObject ocsswInstallStatus = null;
        try {
            ocsswInstallStatus = Json.createObjectBuilder().add("ocsswExists", OCSSWServerModel.isOCSSWExist())
                    .add("ocsswRoot", OCSSWServerModel.getOcsswRoot())
                    .add("ocsswScriptsDirPath", OCSSWServerModel.getOcsswScriptsDirPath())
                    .add("ocsswDataDirPath", OCSSWServerModel.getOcsswDataDirPath())
                    .add("ocsswInstallerScriptPath", OCSSWServerModel.getOcsswInstallerScriptPath())
                    .add("ocsswRunnerScriptPath", OCSSWServerModel.getOcsswRunnerScriptPath())
                    .add("ocsswBinDirPath", OCSSWServerModel.getOcsswBinDirPath())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ocsswInstallStatus;
    }


    /**
     * This service manages client working directory on server.
     * If the client working directory, "System.getProperty(SERVER_WORKING_DIRECTORY_PROPERTY) + File.separator + clientId" exists on server and
     * the flag "keepFilesOnServer is set to "false", the client working directory is purged ;  if the flag "keepFilesOnServer is set to "true"
     * the directory is left intact.
     * If the client working directory doesn't exist, it will be created at this time.
     *
     * @return
     */
    @PUT
    @Path("/manageClientWorkingDirectory/{clientId}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response manageClientWorkingDirectory(@PathParam("clientId") String clientId, String keepFilesOnServer) {

        String workingDirPath = System.getProperty(SERVER_WORKING_DIRECTORY_PROPERTY) + File.separator + clientId;
        String responseMessage = ServerSideFileUtilities.manageDirectory(workingDirPath, new Boolean(keepFilesOnServer).booleanValue());

        Response response = Response.status(200).type("text/plain")
                .entity(responseMessage).build();
        return response;
    }


    /**
     * This method uploads client id and saves it in the file table. It also decides the working directory for the client and saves it in the table for later requests.
     *
     * @param jobId    jobId is specific to each request from the a SeaDAS client
     * @param clientId clientId identifies one SeaDAS client
     * @return
     */
    @PUT
    @Path("/ocsswSetClientId/{jobId}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response setClientIdWithJobId(@PathParam("jobId") String jobId, String clientId) {
        Response.Status respStatus = Response.Status.OK;
        SQLiteJDBC.updateItem(FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.CLIENT_ID_NAME.getFieldName(), clientId);

        String workingDirPath;

        boolean isClientServerSharedDir = new Boolean(System.getProperty(CLIENT_SERVER_SHARED_DIR_PROPERTY)).booleanValue();
        if (isClientServerSharedDir) {
            workingDirPath = System.getProperty(SERVER_WORKING_DIRECTORY_PROPERTY);
        } else {
            workingDirPath = System.getProperty(SERVER_WORKING_DIRECTORY_PROPERTY) + File.separator + clientId;
            //ServerSideFileUtilities.createDirectory(workingDirPath + File.separator + jobId);
        }
        SQLiteJDBC.updateItem(FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName(), workingDirPath);
        return Response.status(respStatus).build();
    }

    @PUT
    @Path("/ocsswSetProgramName/{jobId}/{programName}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response setOCSSWProgramName(@PathParam("jobId") String jobId, String programName) {
        Response.Status respStatus = Response.Status.OK;
        if (OCSSWServerModel.isProgramValid(programName)) {
            SQLiteJDBC.updateItem(FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.PROGRAM_NAME.getFieldName(), programName);
        } else {
            respStatus = Response.Status.BAD_REQUEST;

        }
        return Response.status(respStatus).build();
    }

    @GET
    @Path("/getOfileName/{jobId}")
    @Consumes(MediaType.TEXT_XML)
    public String getOfileName(@PathParam("jobId") String jobId) {
        String ofileName = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.O_FILE_NAME.getFieldName());
        System.out.println("Ofile name = " + ofileName);
        return ofileName;
    }


    @GET
    @Path("/getFileInfo/{jobId}/{ifileName}")
    @Consumes(MediaType.TEXT_XML)
    public JsonObject getFileInfo(@PathParam("jobId") String jobId, @PathParam("ifileName") String ifileName) {
        String currentWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String ifileFullPathName = currentWorkingDir + File.separator + ifileName;
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
        HashMap<String, String> fileInfoMap = ocsswRemote.getFileInfo(ifileFullPathName, jobId);
        JsonObject fileInfo = Json.createObjectBuilder().add(MISSION_NAME_VAR_NAME, fileInfoMap.get(MISSION_NAME_VAR_NAME))
                .add(FILE_TYPE_VAR_NAME, fileInfoMap.get(FILE_TYPE_VAR_NAME))
                .build();
        return fileInfo;
    }


    @GET
    @Path("/getFileCharSet/{jobId}/{fileName}")
    @Consumes(MediaType.TEXT_XML)
    public String getFileCharSet(@PathParam("jobId") String jobId, @PathParam("fileName") String fileName) {
        String currentWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String ifileFullPathName = currentWorkingDir + File.separator + fileName;
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
        String fileCarSet = ocsswRemote.getFileCharset(ifileFullPathName);
        return fileCarSet;
    }

    @GET
    @Path("/getOfileName/{jobId}/{ifileName}/{programName}")
    @Consumes(MediaType.TEXT_XML)
    public String getOfileNameWithIfileAndProgramNameParams(@PathParam("jobId") String jobId,
                                                            @PathParam("ifileName") String ifileName,
                                                            @PathParam("programName") String programName) {

        String currentWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String ifileFullPathName = currentWorkingDir + File.separator + ifileName;
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
        String ofileName = ocsswRemote.getOfileName(jobId, ifileFullPathName, programName);
        return ofileName;
    }

    @PUT
    @Path("executeOcsswProgram/{jobId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executeOcsswProgram(@PathParam("jobId") String jobId, JsonObject jsonObject) {
        Response.Status respStatus = Response.Status.OK;
        Process process = null;
        if (jsonObject == null) {
            respStatus = Response.Status.BAD_REQUEST;
        } else {

            OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
            ocsswRemote.executeProgram(jobId, jsonObject);
        }
        return Response.status(respStatus).build();
    }

    @PUT
    @Path("executeUpdateLutsProgram/{jobId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executeUpdateLutsProgram(@PathParam("jobId") String jobId, JsonObject jsonObject) {
        Response.Status respStatus = Response.Status.BAD_REQUEST;
        Process process = null;
        if (jsonObject != null) {
            OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
            process = ocsswRemote.executeUpdateLutsProgram(jobId, jsonObject);
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            debug("exit value = " + process.exitValue());
            if (process.exitValue() == 0) {
                respStatus = Response.Status.OK;
            } else {
                respStatus = Response.Status.INTERNAL_SERVER_ERROR;
            }
        }
        return Response.status(respStatus).build();
    }

    @PUT
    @Path("executeOcsswProgramOnDemand/{jobId}/{programName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executeOcsswProgramOnDemand(@PathParam("jobId") String jobId,
                                                @PathParam("programName") String programName,
                                                JsonObject jsonObject) {
        SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.NONEXIST.getValue());
        Response.Status respStatus = Response.Status.OK;
        if (jsonObject == null) {
            respStatus = Response.Status.BAD_REQUEST;
        } else {

            OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
            ocsswRemote.executeProgramOnDemand(jobId, programName, jsonObject);
        }
        Response response = Response.status(respStatus).type("text/plain").entity(SQLiteJDBC.retrieveItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName())).build();
        System.out.println("process status on server = " + SQLiteJDBC.retrieveItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName()));
        return response;
        // return Response.status(respStatus).build();
    }


    @PUT
    @Path("executeOcsswProgramAndGetStdout/{jobId}/{programName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executeOcsswProgramAndGetStdout(@PathParam("jobId") String jobId,
                                                    @PathParam("programName") String programName,
                                                    JsonObject jsonObject) {

        String serverWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        if (jsonObject == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {

            OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
            InputStream processInputStream = ocsswRemote.executeProgramAndGetStdout(jobId, programName, jsonObject);
            ServerSideFileUtilities.writeToFile(processInputStream, serverWorkingDir + File.separator + ANC_FILE_LIST_FILE_NAME);
            return Response
                    .ok()
                    .build();
        }
    }

    @PUT
    @Path("executeOcsswProgramSimple/{jobId}/{programName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executeOcsswProgramSimple(@PathParam("jobId") String jobId,
                                              @PathParam("programName") String programName,
                                              JsonObject jsonObject) {
        Response.Status respStatus = Response.Status.OK;
        if (jsonObject == null) {
            respStatus = Response.Status.BAD_REQUEST;
        } else {

            OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
            ocsswRemote.executeProgramSimple(jobId, programName, jsonObject);
        }
        return Response.status(respStatus).build();
    }

    @PUT
    @Path("convertLonLat2Pixels/{jobId}/{programName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response convertLonLat2Pixels(@PathParam("jobId") String jobId,
                                         @PathParam("programName") String programName,
                                         JsonObject jsonObject) {
        Response.Status respStatus = Response.Status.OK;
        HashMap<String, String> pixels = new HashMap();
        JsonObject pixelsJson = null;
        if (jsonObject == null) {
            respStatus = Response.Status.BAD_REQUEST;
        } else {
            OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
            pixels = ocsswRemote.computePixelsFromLonLat(jobId, programName, jsonObject);
            if (pixels != null) {
                respStatus = Response.Status.OK;

            } else {
                respStatus = Response.Status.EXPECTATION_FAILED;
            }
        }
        return Response.status(respStatus).build();
    }

    @GET
    @Path("getConvertedPixels/{jobId}")
    @Consumes(MediaType.TEXT_PLAIN)
    public JsonObject getConvertedPixels(@PathParam("jobId") String jobId) {
        try {
            JsonObject pixelsJsonObject = Json.createObjectBuilder()
                    .add(SQLiteJDBC.LonLatTableFields.SLINE_FIELD_NAME.getValue(), SQLiteJDBC.retrieveItem(SQLiteJDBC.LONLAT_TABLE_NAME, jobId, SQLiteJDBC.LonLatTableFields.SLINE_FIELD_NAME.getValue()))
                    .add(SQLiteJDBC.LonLatTableFields.ELINE_FIELD_NAME.getValue(), SQLiteJDBC.retrieveItem(SQLiteJDBC.LONLAT_TABLE_NAME, jobId, SQLiteJDBC.LonLatTableFields.ELINE_FIELD_NAME.getValue()))
                    .add(SQLiteJDBC.LonLatTableFields.SPIXL_FIELD_NAME.getValue(), SQLiteJDBC.retrieveItem(SQLiteJDBC.LONLAT_TABLE_NAME, jobId, SQLiteJDBC.LonLatTableFields.SPIXL_FIELD_NAME.getValue()))
                    .add(SQLiteJDBC.LonLatTableFields.EPIXL_FIELD_NAME.getValue(), SQLiteJDBC.retrieveItem(SQLiteJDBC.LONLAT_TABLE_NAME, jobId, SQLiteJDBC.LonLatTableFields.EPIXL_FIELD_NAME.getValue()))
                    .build();
            return pixelsJsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @GET
    @Path("/getSensorInfoFileContent/{jobId}/{missionName}")
    @Consumes(MediaType.TEXT_XML)
    public JsonObject getSensorInfoFileContent(@PathParam("jobId") String jobId, @PathParam("missionName") String missionName) {

        System.out.println("missionName = " + missionName);
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
        JsonObject fileContents = ocsswRemote.getSensorFileIntoArrayList(missionName);
        return fileContents;
    }


    @PUT
    @Path("uploadMLPParFile/{jobId}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response uploadMLPParFile(@PathParam("jobId") String jobId,
                                     @PathParam("programName") String programName,
                                     File parFile) {
        Response.Status respStatus = Response.Status.OK;
        System.out.println("par file path: " + parFile.getAbsolutePath());
        if (parFile == null) {
            respStatus = Response.Status.BAD_REQUEST;
        } else {

            SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.NONEXIST.getValue());
            OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
            ocsswRemote.executeMLP(jobId, parFile);
        }
        return Response.status(respStatus).build();
    }

    @GET
    @Path("executeMLPParFile/{jobId}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response executeMLPParFile(@PathParam("jobId") String jobId) {
        Response.Status respStatus = Response.Status.OK;
        return Response.status(respStatus).build();
    }

    @GET
    @Path("processExitValue")
    @Produces(MediaType.TEXT_PLAIN)
    public String getProcessExitValue(@PathParam("jobId") String jobId) {
        return SQLiteJDBC.retrieveItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.EXIT_VALUE_NAME.getFieldName());
    }

    @GET
    @Path("processStatus/{jobId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getProcessStatus(@PathParam("jobId") String jobId) {
        String processStatus = SQLiteJDBC.retrieveItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName());
        System.out.println("process status: " + processStatus);
        return processStatus;
    }

    @GET
    @Path("missions")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getMissionDataStatus() {
        return new MissionInfoFinder().getMissions();
    }

    @GET
    @Path("isMissionDirExist/{missionName}")
    @Produces(MediaType.TEXT_PLAIN)
    public Boolean isMissionDirExist(@PathParam("missionName") String missionName) {
        return OCSSWServerModel.isMissionDirExist(missionName);
    }

    @GET
    @Path("/l2bin_suites/{missionName}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getMissionSuites(@PathParam("missionName") String missionName) {
        return new MissionInfoFinder().getL2BinSuites(missionName);
    }
//
//    @GET
//    @Path("/missionSuites/{missionName}/{programName}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String[] getL2genMissionSuites(@PathParam("missionName") String missionName, @PathParam("programName") String programName) {
//        try {
//            missionName.replaceAll("_", " ");
//            if (OCSSWServerModel.isMissionDirExist(missionName)) {
//                return new MissionInfoFinder().getMissionSuiteList(missionName, programName);
//            } else {
//                return null;
//            }
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    @GET
    @Path("/missionSuites/{missionName}/{programName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getMissionSuites(@PathParam("missionName") String missionName, @PathParam("programName") String programName) {
        missionName = missionName.replaceAll("_", " ");
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
        return ocsswRemote.getMissionSuites(missionName, programName);
    }

    @GET
    @Path("/srcDirInfo")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public JsonObject getOCSSWSrcDirInfo() {
        JsonObject srcDirStatus = Json.createObjectBuilder().add("ocssw-src", new File(OCSSWServerModel.getOcsswSrcDirPath()).exists()).build();
        return srcDirStatus;
    }

    @GET
    @Path("/viirsDemInfo")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public JsonObject getOCSSWViirsDemInfo() {
        JsonObject viirsDemStatus = Json.createObjectBuilder().add("viirs-dem", new File(OCSSWServerModel.getOcsswViirsDemPath()).exists()).build();
        return viirsDemStatus;
    }

    @GET
    @Path("/ocsswEnv")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OCSSWInfo getOCSSWInfo() {
        OCSSWInfo ocsswInfo = new OCSSWInfo();
        return ocsswInfo;
    }

    @GET
    @Path("/serverSharedFileDir")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSharedFileDirName() {
        System.out.println("Shared dir name:" + OCSSWServerPropertyValues.getServerSharedDirName());
        return OCSSWServerPropertyValues.getServerSharedDirName();
    }

    @POST
    @Path("/uploadNextLevelNameParams/{jobId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response uploadNextLevelNameParams(@PathParam("jobId") String jobId, JsonObject jsonObject) {
        Response.Status responseStatus = Response.Status.ACCEPTED;
        System.out.println("params uploaded!");
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
        String ofileName = ocsswRemote.getOfileName(jobId, jsonObject);
        System.out.println("ofileName = " + ofileName);
        return Response.ok(ofileName).build();
    }

    @GET
    @Path("retrieveNextLevelFileName/{jobId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String findNextLevelFileName(@PathParam("jobId") String jobId) {
        return SQLiteJDBC.retrieveItem(FILE_TABLE_NAME, jobId, "O_FILE_NAME");
    }

    @GET
    @Path("retrieveIFileType/{jobId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String findIFileType(@PathParam("jobId") String jobId) {
        return SQLiteJDBC.retrieveItem(FILE_TABLE_NAME, jobId, "I_FILE_TYPE");
    }

    @GET
    @Path("retrieveMissionName/{jobId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String findMissionName(@PathParam("jobId") String jobId) {
        return SQLiteJDBC.retrieveItem(FILE_TABLE_NAME, jobId, "MISSION_NAME");
    }


    @GET
    @Path("retrieveMissionDirName/{jobId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String findMissionDirName(@PathParam("jobId") String jobId) {
        return SQLiteJDBC.retrieveItem(FILE_TABLE_NAME, jobId, "MISSION_DIR");
    }


    @GET
    @Path("retrieveProcessStdoutFile/{jobId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream retrieveProcessStdoutFile(@PathParam("jobId") String jobId) {
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
        InputStream inputStream1 = ocsswRemote.getProcessStdoutFile(jobId);
        InputStream inputStream = SQLiteJDBC.retrieveInputStreamItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STD_OUT_NAME.getFieldName());
        return inputStream;
    }

    @GET
    @Path("retrieveProcessInputStreamLine/{jobId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String retrieveProcessInputStreamLine(@PathParam("jobId") String jobId) {
        String serverWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String processInputStreamFileName = serverWorkingDir + File.separator + jobId + File.separator + PROCESS_INPUT_STREAM_FILE_NAME;
        String inputStreamLine = ServerSideFileUtilities.getlastLine(processInputStreamFileName);
        System.out.println("process input stream last line = " + inputStreamLine + "  filename = " + processInputStreamFileName);
        return inputStreamLine;
    }

    @GET
    @Path("retrieveProcessErrorStreamLine/{jobId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String retrieveProcessErrorStreamLine(@PathParam("jobId") String jobId) {
        String serverWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String processErrorStreamFileName = serverWorkingDir + File.separator + jobId + File.separator + PROCESS_ERROR_STREAM_FILE_NAME;
        String errorStreamLine = ServerSideFileUtilities.getlastLine(processErrorStreamFileName);
        System.out.println("process error stream last line = " + errorStreamLine + "  filename = " + processErrorStreamFileName);
        return errorStreamLine;
    }
}
