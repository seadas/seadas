package gov.nasa.gsfc.seadas.ocsswrest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.regexp.internal.RE;
import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;
import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWRemote;
import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWServerModel;
import gov.nasa.gsfc.seadas.ocsswrest.process.ORSProcessObserver;
import gov.nasa.gsfc.seadas.ocsswrest.process.ProcessRunner;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.*;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import static gov.nasa.gsfc.seadas.ocsswrest.OCSSWRestServer.SERVER_WORKING_DIRECTORY_PROPERTY;
import static gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWRemote.ANC_FILE_LIST_FILE_NAME;
import static gov.nasa.gsfc.seadas.ocsswrest.process.ORSProcessObserver.PROCESS_ERROR_STREAM_FILE_NAME;
import static gov.nasa.gsfc.seadas.ocsswrest.process.ORSProcessObserver.PROCESS_INPUT_STREAM_FILE_NAME;


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

    private HashMap<String, Boolean> missionDataStatus;

    @GET
    @Path("/installDir")
    @Produces(MediaType.TEXT_PLAIN)
    public String getOCSSWInstallDir() {
        System.out.println("ocssw install dir: " + OCSSWServerModelOld.OCSSW_INSTALL_DIR);
        return OCSSWServerModelOld.OCSSW_INSTALL_DIR;
    }

    /**
     * ocsswScriptsDirPath = ocsswRoot + File.separator + OCSSW_SCRIPTS_DIR_SUFFIX;
     * ocsswDataDirPath = ocsswRoot + File.separator +OCSSW_DATA_DIR_SUFFIX;
     * ocsswInstallerScriptPath = ocsswScriptsDirPath + System.getProperty("file.separator") + OCSSW_INSTALLER_PROGRAM;
     * ocsswRunnerScriptPath = ocsswScriptsDirPath + System.getProperty("file.separator") + OCSSW_RUNNER_SCRIPT;
     * ocsswBinDirPath
     *
     * @return
     */
    @GET
    @Path("/ocsswInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getOcsswInfo() {
        JsonObject ocsswInstallStatus = Json.createObjectBuilder().add("ocsswExists", OCSSWServerModel.isOCSSWExist())
                .add("ocsswRoot", OCSSWServerModel.getOcsswRoot())
                .add("ocsswScriptsDirPath", OCSSWServerModel.getOcsswScriptsDirPath())
                .add("ocsswDataDirPath", OCSSWServerModel.getOcsswDataDirPath())
                .add("ocsswInstallerScriptPath", OCSSWServerModel.getOcsswInstallerScriptPath())
                .add("ocsswRunnerScriptPath", OCSSWServerModel.getOcsswRunnerScriptPath())
                .add("ocsswBinDirPath", OCSSWServerModel.getOcsswBinDirPath())
                .build();
        return ocsswInstallStatus;
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
    public Response setClientId(@PathParam("jobId") String jobId, String clientId) {
        Response.Status respStatus = Response.Status.OK;
        System.out.println("client : " + clientId);
        SQLiteJDBC.updateItem(FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.CLIENT_ID_NAME.getFieldName(), clientId);
        String workingDirPath = System.getProperty(SERVER_WORKING_DIRECTORY_PROPERTY) + File.separator + clientId;
        System.out.println("client and working directory: " + clientId + "   " + workingDirPath);
        //todo cleanup client directory before starting a new set of tasks
        try {
            File workingDir = new File(workingDirPath);
            if (workingDir.exists() && workingDir.isDirectory()) {
                ServerSideFileUtilities.purgeDirectory(workingDir);
            } else {
                Files.createDirectories(new File(workingDirPath).toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        SQLiteJDBC.updateItem(FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName(), workingDirPath);
        return Response.status(respStatus).build();
    }

    @PUT
    @Path("/ocsswSetProgramName/{jobId}")
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
    public JsonObject getOfileName(@PathParam("jobId") String jobId) {

        String missionName = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.MISSION_NAME.getFieldName());
        String fileType = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.I_FILE_TYPE.getFieldName());
        String programName = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.PROGRAM_NAME.getFieldName());
        String ofileName = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.O_FILE_NAME.getFieldName());
        ofileName = ofileName.substring(ofileName.lastIndexOf(File.separator) + 1);
        JsonObject fileInfo = Json.createObjectBuilder().add("missionName", missionName)
                .add("fileType", fileType)
                .add("programName", programName)
                .add("ofileName", ofileName)
                .build();
        return fileInfo;
    }

    @PUT
    @Path("executeOcsswProgram/{jobId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executeOcsswProgram(@PathParam("jobId") String jobId, JsonObject jsonObject)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        Process process = null;
        if (jsonObject == null) {
            respStatus = Response.Status.BAD_REQUEST;
        } else {

            OCSSWRemote ocsswRemote = new OCSSWRemote();
            ocsswRemote.executeProgram(jobId, jsonObject);
        }
        return Response.status(respStatus).build();
    }

    @PUT
    @Path("executeOcsswProgramOnDemand/{jobId}/{programName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executeOcsswProgramOnDemand(@PathParam("jobId") String jobId,
                                                @PathParam("programName") String programName,
                                                JsonObject jsonObject)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        if (jsonObject == null) {
            respStatus = Response.Status.BAD_REQUEST;
        } else {

            OCSSWRemote ocsswRemote = new OCSSWRemote();
            ocsswRemote.executeProgramOnDemand(jobId, programName, jsonObject);
        }
        return Response.status(respStatus).build();
    }


    @PUT
    @Path("executeOcsswProgramAndGetStdout/{jobId}/{programName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executeOcsswProgramAndGetStdout(@PathParam("jobId") String jobId,
                                                    @PathParam("programName") String programName,
                                                    JsonObject jsonObject)
            throws IOException {

        String serverWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName()) + File.separator + jobId;
        if (jsonObject == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {

            OCSSWRemote ocsswRemote = new OCSSWRemote();
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
                                              JsonObject jsonObject)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        if (jsonObject == null) {
            respStatus = Response.Status.BAD_REQUEST;
        } else {

            OCSSWRemote ocsswRemote = new OCSSWRemote();
            ocsswRemote.executeProgramSimple(jobId, programName, jsonObject);
        }
        return Response.status(respStatus).build();
    }


    @PUT
    @Path("uploadMLPParFile/{jobId}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response uploadMLPParFile(@PathParam("jobId") String jobId,
                                     @PathParam("programName") String programName,
                                     File parFile)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        System.out.println("par file path: " + parFile.getAbsolutePath());
        if (parFile == null) {
            respStatus = Response.Status.BAD_REQUEST;
        } else {

            SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.NONEXIST.getValue());
            OCSSWRemote ocsswRemote = new OCSSWRemote();
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
    @Path("getMLPOutputFiles/{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject executeMLP(@PathParam("jobId") String jobId) {
        OCSSWRemote ocsswRemote = new OCSSWRemote();
        return ocsswRemote.getMLPOutputFilesJsonList(jobId);
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
        return new MissionInfo().getMissions();
    }

    @GET
    @Path("retrieveMissionDirName")
    @Produces(MediaType.TEXT_PLAIN)
    public String getMissionSuitesArray() {
        return OCSSWServerModelOld.missionDataDir;
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
        return new MissionInfo().getL2BinSuites(missionName);
    }

    @GET
    @Path("/missionSuites/{missionName}/{programName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getL2genMissionSuites(@PathParam("missionName") String missionName, @PathParam("programName") String programName) {
        if (OCSSWServerModel.isMissionDirExist(missionName)) {
            return new MissionInfo().getMissionSuiteList(missionName, programName);
        } else {
            return null;
        }
    }


    @GET
    @Path("downloadInstaller")
    @Produces(MediaType.TEXT_XML)
    public boolean getOCSSWInstallerDownloadStatus() {
        return OCSSWServerModelOld.downloadOCSSWInstaller();
    }


    @GET
    @Path("/evalDirInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getOCSSWEvalDirInfo() {
        JsonObject evalDirStatus = Json.createObjectBuilder().add("eval", new File(OCSSWServerModelOld.missionDataDir + "eval").exists()).build();
        return evalDirStatus;
    }

    @GET
    @Path("/srcDirInfo")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public JsonObject getOCSSWSrcDirInfo() {
        JsonObject srcDirStatus = Json.createObjectBuilder().add("build", new File(OCSSWServerModelOld.missionDataDir + "build").exists()).build();
        return srcDirStatus;
    }

    @GET
    @Path("/ocsswEnv")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OCSSWInfo getOCSSWInfo() {
        OCSSWInfo ocsswInfo = new OCSSWInfo();
        //ocsswInfo.setInstalled(true);
        //ocsswInfo.setOcsswDir(System.getProperty("user.home") + "/ocssw");
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
    @Path("/updateProgressMonitorFlag/{progressMonitorFlag}")
    @Consumes(MediaType.TEXT_PLAIN)
    public void updateProgressMonitorFlag(@PathParam("progressMonitorFlag") String progressMonitorFlag) {
        System.out.println("Shared dir name:" + OCSSWServerPropertyValues.getServerSharedDirName());
        OCSSWServerModelOld.setProgressMonitorFlag(progressMonitorFlag);
    }

    @POST
    @Path("/uploadNextLevelNameParams/{jobId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public int uploadNextLevelNameParams(@PathParam("jobId") String jobId, JsonObject jsonObject) {
        Response.Status responseStatus = Response.Status.ACCEPTED;
        OCSSWRemote ocsswRemote = new OCSSWRemote();
        ocsswRemote.getOfileName(jobId, jsonObject);
        return responseStatus.getStatusCode();
    }

    @GET
    @Path("/lonlat2pixel")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject lonlat2pixelConverter(JsonArray jsonArray) {
        Process process = null;
        String programName = "lonlat2pixel";
        HashMap<String, String> pixels = new HashMap<>();
        String[] cmdArray = getCmdArray(jsonArray);
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        cmdArray[0] = OCSSWServerModelOld.getOcsswScriptPath();
        cmdArray[1] = "--ocsswroot";
        cmdArray[2] = OCSSWServerModelOld.getOcsswEnv();
        process = ProcessRunner.executeCmdArray(cmdArray);
        String jsonString = new String();
        try {
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = br.readLine();
                String[] tmp;
                while ((line = br.readLine()) != null) {
                    if (jsonString.length() > 0) {
                        jsonString = jsonString + ",";
                    }
                    if (line.indexOf("=") != -1) {
                        tmp = line.split("=");
                        pixels.put(tmp[0], tmp[1]);
                        jsonString = jsonString + (jsonString.length() > 0 ? "," : "") + tmp[0] + " : " + tmp[1];
                        jsonObjectBuilder.add(tmp[0], tmp[1]);
                    }
                }
            }

        } catch (IOException ioe) {

        } catch (InterruptedException ie) {

        }
        JsonObject jo = jsonObjectBuilder.build();
        return jo;
    }

    @POST
    @Path("install")
    @Consumes(MediaType.APPLICATION_JSON)
    public void installOcssw() {

    }

    @POST
    @Path("cmdArray")
    @Consumes(MediaType.APPLICATION_JSON)
    public String uploadCommandArray(JsonArray jsonArray)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        Process process = null;
        if (jsonArray == null) {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        } else {
            writeToFile(jsonArray.getString(0));
            downloadOCSSWInstaller();

            String[] cmdArray = getCmdArray(jsonArray);

            cmdArray[0] = OCSSWServerModelOld.OCSSW_INSTALLER_FILE_LOCATION;

            process = ProcessRunner.executeInstaller(cmdArray);
        }
        int exitValue = process.exitValue();
        SQLiteJDBC.updateItem("PROCESS_TABLE", OCSSWServerModelOld.getCurrentJobId(), "EXIT_VALUE", new Integer(exitValue).toString());
        return new Integer(exitValue).toString();
    }

    @POST
    @Path("installOcssw")
    @Consumes(MediaType.APPLICATION_JSON)
    public String installOcssw(JsonArray jsonArray)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        Process process = null;
        if (jsonArray == null) {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        } else {
            writeToFile(jsonArray.getString(0));
            System.out.println("download installer: ");
            System.out.println(downloadOCSSWInstaller());

            String[] cmdArray = getCmdArray(jsonArray);

            cmdArray[0] = OCSSWServerModelOld.OCSSW_INSTALLER_FILE_LOCATION;

            for (String str : cmdArray) {
                System.out.println(str);
            }

            process = ProcessRunner.executeInstaller(cmdArray);
        }
        int exitValue = process.exitValue();
        SQLiteJDBC.updateItem("PROCESS_TABLE", OCSSWServerModelOld.getCurrentJobId(), "EXIT_VALUE", new Integer(exitValue).toString());
        return new Integer(exitValue).toString(); //Response.status(respStatus).build();
    }

    @POST
    @Path("runOcsswProcessor")
    @Consumes(MediaType.APPLICATION_JSON)
    public String runOcsswProcessor(JsonArray jsonArray)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        Process process = null;
        if (jsonArray == null) {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        } else {

            String[] cmdArray = getCmdArray(jsonArray);

            cmdArray[0] = OCSSWServerModelOld.getOcsswScriptPath();
            cmdArray[1] = "--ocsswroot";
            cmdArray[2] = OCSSWServerModelOld.getOcsswEnv();

            process = ProcessRunner.executeInstaller(cmdArray);
        }
        int exitValue = process.exitValue();
        SQLiteJDBC.updateItem("PROCESS_TABLE", OCSSWServerModelOld.getCurrentJobId(), "EXIT_VALUE", new Integer(exitValue).toString());
        return new Integer(exitValue).toString();//Response.status(respStatus).build();
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
        OCSSWRemote ocsswRemote = new OCSSWRemote();
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


    private static String[] getCmdArrayForNextLevelNameFinder(String ifileName, String programName) {
        String[] cmdArray = new String[6];
        cmdArray[0] = OCSSWServerModelOld.getOcsswScriptPath();
        cmdArray[1] = "--ocsswroot";
        cmdArray[2] = OCSSWServerModelOld.getOcsswEnv();
        cmdArray[3] = NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME;
        cmdArray[4] = ifileName;
        cmdArray[5] = programName;
        return cmdArray;

    }

    private String[] getCmdArray(JsonArray jsonArray) {
        String text = "cmdArray: ";
        String str;
        ArrayList<String> list = new ArrayList<String>();
        if (jsonArray != null) {
            int len = jsonArray.size();
            for (int i = 0; i < len; i++) {
                str = jsonArray.get(i).toString();
                str = str.replace('"', ' ');
                str = str.trim();
                list.add(str);
                text = text + str;
            }
        }
        writeToFile(text);

        String[] cmdArray = list.toArray(new String[list.size()]);
        return cmdArray;
    }

    public static boolean downloadOCSSWInstaller() {

        try {

            URL website = new URL(OCSSWServerModelOld.OCSSW_INSTALLER_URL);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(OCSSWServerModelOld.OCSSW_INSTALLER_FILE_LOCATION);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            fos.close();
            (new File(OCSSWServerModelOld.OCSSW_INSTALLER_FILE_LOCATION)).setExecutable(true);
            ocsswInstalScriptDownloadSuccessful = true;
        } catch (MalformedURLException malformedURLException) {
            System.out.println("URL for downloading install_ocssw.py is not correct!");
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("ocssw installation script failed to download. \n" +
                    "Please check network connection or 'seadas.ocssw.root' variable in the 'seadas.config' file. \n" +
                    "possible cause of error: " + fileNotFoundException.getMessage());
        } catch (IOException ioe) {
            System.out.println("ocssw installation script failed to download. \n" +
                    "Please check network connection or 'seadas.ocssw.root' variable in the \"seadas.config\" file. \n" +
                    "possible cause of error: " + ioe.getLocalizedMessage());
        } finally {

            return ocsswInstalScriptDownloadSuccessful;
        }
    }

    private static boolean ocsswInstalScriptDownloadSuccessful = false;

    private void writeToFile(String content) {
        FileOutputStream fop = null;
        File file;
        try {

            file = new File("/home/aynur/cmdArray.txt");
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
