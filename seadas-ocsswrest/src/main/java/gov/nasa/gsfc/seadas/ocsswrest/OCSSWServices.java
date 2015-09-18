package gov.nasa.gsfc.seadas.ocsswrest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.*;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 1/8/15
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */

@Path("/ocssw")
public class OCSSWServices {

    private static String NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME = "next_level_name.py";
    private static String NEXT_LEVEL_FILE_NAME_TOKEN = "Output Name:";
    private static String FILE_TABLE_NAME = "FILE_TABLE";
    private static String MISSION_TABLE_NAME = "MISSION_TABLE";

    private HashMap<String, Boolean> missionDataStatus;

    @GET
    @Path("/installDir")
    @Produces(MediaType.TEXT_PLAIN)
    public String getOCSSWInstallDir() {

        return OCSSWServerModel.OCSSW_INSTALL_DIR;
    }

    @GET
    @Path("/ocsswInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getOcsswInstallStatus() {
        final File ocsswScriptsDir = new File(OCSSWServerModel.OCSSW_INSTALL_DIR + System.getProperty("file.separator") + "run" + System.getProperty("file.separator") + "scripts");
        System.out.println("ocsswExists");
        JsonObject ocsswInstallStatus = Json.createObjectBuilder().add("ocsswExists", ocsswScriptsDir.isDirectory()).build();
        return ocsswInstallStatus;
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
        return OCSSWServerModel.missionDataDir;
    }

    @GET
    @Path("/l2bin_suites/{missionName}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getMissionSuites(@PathParam("missionName") String missionName) {
        return new MissionInfo().getL2BinSuites(missionName);
    }


    @GET
    @Path("downloadInstaller")
    @Produces(MediaType.TEXT_XML)
    public boolean getOCSSWInstallerDownloadStatus() {
        return OCSSWServerModel.downloadOCSSWInstaller();
    }


    @GET
    @Path("/evalDirInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getOCSSWEvalDirInfo() {
        JsonObject evalDirStatus = Json.createObjectBuilder().add("eval", new File(OCSSWServerModel.missionDataDir + "eval").exists()).build();
        return evalDirStatus;
    }

    @GET
    @Path("/srcDirInfo")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public JsonObject getOCSSWSrcDirInfo() {
        JsonObject srcDirStatus = Json.createObjectBuilder().add("build", new File(OCSSWServerModel.missionDataDir + "build").exists()).build();
        return srcDirStatus;
    }

    @GET
    @Path("/ocsswEnv")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OCSSWInfo getOCSSWInfo() {
        OCSSWInfo ocsswInfo = new OCSSWInfo();
        ocsswInfo.setInstalled(true);
        ocsswInfo.setOcsswDir(System.getProperty("user.home") + "/ocssw");
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
    @Path("/findIFileTypeAndMissionName/{jobId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String findFileTypeAndMissionName(@PathParam("jobId") String jobId, JsonArray jsonArray) {
        Response.Status respStatus = Response.Status.OK;
        Process process = null;
        String missionName = null;
        String fileType = null;

        if (jsonArray == null) {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        } else {
            System.out.println("finding file type and mission name  ");

            String[] cmdArray = getCmdArray(jsonArray);

            cmdArray[0] = OCSSWServerModel.getOcsswScriptPath();
            cmdArray[1] = "--ocsswroot";
            cmdArray[2] = OCSSWServerModel.getOcsswEnv();

            for (String str : cmdArray) {
                System.out.println(str);
            }

            process = ProcessRunner.executeCmdArray(cmdArray);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                String line = stdInput.readLine();
                System.out.println("Line = " + line);
                if (line != null) {
                    String splitLine[] = line.split(":");
                    if (splitLine.length == 3) {
                        missionName = splitLine[1].toString().trim();
                        fileType = splitLine[2].toString().trim();
                        System.out.println("Mission Name = " + missionName);
                        System.out.println("File Type = " + fileType);
                    }
                }
            } catch (IOException ioe) {
                System.out.println(ioe.getStackTrace());
            }
        }
        if (jobId != null) {
            //insert or update mission name
            if (SQLiteJDBC.isJobIdExist(jobId)) {
                SQLiteJDBC.updateMissionName(jobId, missionName);
            } else {
                SQLiteJDBC.insertMissionName(jobId, missionName);
            }

            //insert or update file type
            if (SQLiteJDBC.isJobIdExist(jobId)) {
                SQLiteJDBC.updateFileType(jobId, fileType);

            } else {
                SQLiteJDBC.insertFileType(jobId, fileType);
            }


        }
        return "ok";
    }


    @POST
    @Path("install")
    @Consumes(MediaType.APPLICATION_JSON)
    public void installOcssw() {

    }

    @POST
    @Path("cmdArray")
    @Consumes(MediaType.APPLICATION_JSON)
    public Process uploadCommandArray(JsonArray jsonArray)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        Process process = null;
        if (jsonArray == null) {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        } else {
            writeToFile(jsonArray.getString(0));
            downloadOCSSWInstaller();

            String[] cmdArray = getCmdArray(jsonArray);

            cmdArray[0] = OCSSWServerModel.OCSSW_INSTALLER_FILE_LOCATION;

            process = ProcessRunner.executeInstaller(cmdArray);
        }
        return process; //Response.status(respStatus).build();

        //return Response.status(respStatus).build();
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

            cmdArray[0] = OCSSWServerModel.OCSSW_INSTALLER_FILE_LOCATION;

            for (String str : cmdArray) {
                System.out.println(str);
            }

            process = ProcessRunner.executeInstaller(cmdArray);
        }
        return new Integer(process.exitValue()).toString(); //Response.status(respStatus).build();
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

            cmdArray[0] = OCSSWServerModel.getOcsswScriptPath();
            cmdArray[1] = "--ocsswroot";
            cmdArray[2] = OCSSWServerModel.getOcsswEnv();

            process = ProcessRunner.executeInstaller(cmdArray);
        }
        return new Integer(process.exitValue()).toString(); //Response.status(respStatus).build();
    }

    @POST
    @Path("executeOcsswProgram")
    @Consumes(MediaType.APPLICATION_JSON)
    public String executeOcsswProgram(JsonArray jsonArray)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        Process process = null;
        if (jsonArray == null) {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        } else {

            String[] cmdArray = getCmdArray(jsonArray);

            cmdArray[0] = OCSSWServerModel.getOcsswScriptPath();
            cmdArray[1] = "--ocsswroot";
            cmdArray[2] = OCSSWServerModel.getOcsswEnv();

            for (String str : cmdArray) {
                System.out.println(str);
            }

            process = ProcessRunner.executeCmdArray(cmdArray);
        }
        System.out.println("process execution completed.");
        System.out.println("exit code = " + process.exitValue());
        return new Integer(process.exitValue()).toString();
    }

    @POST
    @Path(value = "/computeNextLevelFileName/{jobId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String computeNextLevelFileName(@PathParam("jobId") String jobId, JsonArray jsonArray) {
        Process process = null;
        InputStream is = null;
        String ofileName = "output";
        if (jsonArray != null) {
            //jobId = jsonArray.getString(jsonArray.size() - 1);

            String[] cmdArray = getCmdArray(jsonArray);

            cmdArray[0] = OCSSWServerModel.getOcsswScriptPath();
            cmdArray[1] = "--ocsswroot";
            cmdArray[2] = OCSSWServerModel.getOcsswEnv();
            for (String str : cmdArray) {
                System.out.println(str);
            }
            process = ProcessRunner.executeCmdArray(cmdArray);
            is = process.getInputStream();

        }
        InputStreamReader isr = new InputStreamReader(is);

        BufferedReader br = new BufferedReader(isr);
        int exitCode = 0;
        try {

            if (exitCode == 0) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(NEXT_LEVEL_FILE_NAME_TOKEN)) {
                        ofileName = (line.substring(NEXT_LEVEL_FILE_NAME_TOKEN.length())).trim();
                        System.out.println(ofileName);
                    }
                }

            } else {
                System.out.println("Failed exit code on program '" + NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME + "'");
            }

        } catch (IOException ioe) {

            System.out.println(ioe.getMessage());
        }

        System.out.println("completed cmd Array execution!");
        System.out.println(is.toString());
        if (jobId != null) {
            if (SQLiteJDBC.isJobIdExist(jobId)) {
                SQLiteJDBC.updateOFileName(jobId, ofileName);
            } else {
                SQLiteJDBC.insertOFileName(jobId, ofileName);
            }
        }
        return new Integer(process.exitValue()).toString();
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
    @Path("retrieveProcess/{jobId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Process retrieveProcess(@PathParam("jobId") String jobId) {
        ObjectMapper om = new ObjectMapper();
        try {
            String processString = om.writeValueAsString(OCSSWServerModel.getProcess(jobId));
        } catch (JsonProcessingException jpe) {
            System.out.println(jpe.getStackTrace());
        }
        return OCSSWServerModel.getProcess(jobId);
    }

    @GET
    @Path("retrieveProcessResult/{jobId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream retrieveProcessResult(@PathParam("jobId") String jobId) {
        return OCSSWServerModel.getProcessResult(jobId);
    }


    private static String[] getCmdArrayForNextLevelNameFinder(String ifileName, String programName) {
        String[] cmdArray = new String[6];
        cmdArray[0] = OCSSWServerModel.getOcsswScriptPath();
        cmdArray[1] = "--ocsswroot";
        cmdArray[2] = OCSSWServerModel.getOcsswEnv();
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

            URL website = new URL(OCSSWServerModel.OCSSW_INSTALLER_URL);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(OCSSWServerModel.OCSSW_INSTALLER_FILE_LOCATION);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            fos.close();
            (new File(OCSSWServerModel.OCSSW_INSTALLER_FILE_LOCATION)).setExecutable(true);
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

            file = new File("/home/aabduraz/cmdArray.txt");
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
