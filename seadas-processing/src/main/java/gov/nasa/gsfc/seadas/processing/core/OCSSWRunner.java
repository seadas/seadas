package gov.nasa.gsfc.seadas.processing.core;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.processing.general.*;
import org.esa.beam.visat.VisatApp;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.getProperty;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 10/3/12
 * Time: 12:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class OCSSWRunner {

    //private static ProcessBuilder processBuilder;
    private static HashMap environment = new HashMap();
    private static final String OCSSW_ROOT_VAR = "OCSSWROOT";
    private static final String SEADAS_OCSSW_LOCATION = "ocssw.location";

    private static final String LOCAL = "local";
    private static final String REMOTE = "remote";
    private static boolean monitorProgress = false;
    static ProcessorModel processorModel;
    static OcsswCommandArrayManager commandArrayManager;

    public OCSSWRunner() {

        environment.put(OCSSW_ROOT_VAR, OCSSW.getOcsswEnvArray());
    }

    public static Process execute(ProcessorModel pm) {
        processorModel = pm;
        if (OCSSW.isLocal()) {
            commandArrayManager = new LocalOcsswCommandArrayManager(processorModel);
            return executeLocal(commandArrayManager.getProgramCommandArray(), commandArrayManager.getIfileDir());
        } else {
            commandArrayManager = new RemoteOcsswCommandArrayManager(processorModel);
            return executeRemote(commandArrayManager.getProgramCommandArray());
        }
    }


    public static Process executeLocal(String[] cmdArray, File ifileDir) {
        //System.out.println("local execution!" + " "  + Arrays.toString(cmdArray) );
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
        Map<String, String> env = processBuilder.environment();
        if (!env.containsKey(OCSSW_ROOT_VAR) && OCSSW.isOCSSWExist()) {
            env.put(OCSSW_ROOT_VAR, OCSSW.getOcsswEnv());
        }
        if (ifileDir != null) {
            processBuilder.directory(ifileDir);
        } else {
            processBuilder.directory(getDefaultDir());
        }

        Process process = null;
        try {
            process = processBuilder.start();
            if (process != null) {
                SeadasFileUtils.debug("Running the program " + cmdArray.toString());
//                int exitValue = process.waitFor();
//                SeadasFileUtils.debug("process exit value = " + exitValue);
            }
        } catch (Exception e) {
            //VisatApp.getApp().showErrorDialog("OCSSW execution error from SeaDAS application! \n" + cmdArray[3] + "  program is not executed correctly.");
            e.printStackTrace();
        }
        return process;
    }

    public static Process executeRemote(String[] cmdArray) {

        SeadasProcess process = new SeadasProcess();
        ArrayList<String> cmdArrayList = (ArrayList<String>) Arrays.asList(cmdArray);

        JsonArrayBuilder jab = Json.createArrayBuilder();
        for (String s : cmdArrayList) {
            jab.add(s);
        }
        JsonArray remoteCmdArray = jab.build();

        OCSSWClient ocsswClient = new OCSSWClient();
        WebTarget target = ocsswClient.getOcsswWebTarget();

        //todo: merge these two

        if (processorModel.getProgramName().equals(OCSSW.OCSSW_INSTALLER)) {
            final Response response = target.path("ocssw").path("installOcssw").request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(remoteCmdArray, MediaType.APPLICATION_JSON_TYPE));
        } else {
            final Response response = target.path("ocssw").path("executeOcsswProgram").request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(remoteCmdArray, MediaType.APPLICATION_JSON_TYPE));
        }

        return process;

    }

    /**
     * @param cmdArray - command array for executing  "next_level_name.py INPUT_FILE TARGET_PROGRAM"
     * @param ifileDir - command execution directory
     * @return OUTPUT_FILE_NAME. It will be extracted from the standard output.
     */
    public static String executeLocalNameFinder(String[] cmdArray, File ifileDir) {
        Process process = OCSSWRunner.execute(cmdArray, ifileDir);

        if (process == null) {
            return "output";
        }
        int exitCode = process.exitValue();
        InputStream is;
        if (exitCode == 0) {
            is = process.getInputStream();
        } else {
            is = process.getErrorStream();
        }

        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        try {

            if (exitCode == 0) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(NextLevelNameFinder.NEXT_LEVEL_FILE_NAME_TOKEN)) {
                        return (line.substring(NextLevelNameFinder.NEXT_LEVEL_FILE_NAME_TOKEN.length())).trim();
                    }
                }

            } else {
                VisatApp.getApp().showErrorDialog("Failed exit code on program '" + NextLevelNameFinder.NEXT_LEVEL_FILE_NAME_TOKEN + "'");
            }

        } catch (IOException ioe) {

            VisatApp.getApp().showErrorDialog(ioe.getMessage());
        }
        return "output";
    }

    public static String executeRemoteNameFinder(String[] cmdArray) {
        JsonArrayBuilder jab = Json.createArrayBuilder();
        for (String s : cmdArray) {
            jab.add(s);
        }

        //add jobId for server side database
        //jab.add(OCSSW.getJobId());
        JsonArray remoteCmdArray = jab.build();

        OCSSWClient ocsswClient = new OCSSWClient();
        WebTarget target = ocsswClient.getOcsswWebTarget();
        final Response response = target.path("ocssw").path("computeNextLevelFileName").path(OCSSW.getJobId()).request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(remoteCmdArray, MediaType.APPLICATION_JSON_TYPE));

        String ofileName = target.path("ocssw").path("retrieveNextLevelFileName").path(OCSSW.getJobId()).request(MediaType.TEXT_PLAIN).get(String.class);
        if (ofileName != null) {
            return ofileName;
        } else {
            return "output";
        }
    }

    public static HashMap executeLocalGetOBPGFileInfo(String[] cmdArray, File ifileDir) {
        HashMap<String, String> fileInfos = new HashMap();
        try {

            Process process = OCSSWRunner.execute(cmdArray, ifileDir);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = stdInput.readLine();
            if (line != null) {
                String splitLine[] = line.split(":");
                if (splitLine.length == 3) {
                    String missionName = splitLine[1].toString().trim();
                    String fileType = splitLine[2].toString().trim();

                    if (fileType.length() > 0) {
                        fileInfos.put(FileInfoFinder.FILE_TYPE_ID_STRING, fileType);
                    }

                    if (missionName.length() > 0) {
                        fileInfos.put(FileInfoFinder.MISSION_NAME_ID_STRING, missionName);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR - Problem running " + FileInfoFinder.FILE_INFO_SYSTEM_CALL);
            System.out.println(e.getMessage());
        }
        return fileInfos;
    }

    public static HashMap executeRemoteGetOBPGFileInfo(String[] cmdArray) {

        HashMap<String, String> fileInfos = new HashMap();
        OCSSWClient ocsswClient = new OCSSWClient();
        WebTarget target = ocsswClient.getOcsswWebTarget();
        JsonArrayBuilder jab = Json.createArrayBuilder();
        for (String s : cmdArray) {
            jab.add(s);
        }
        JsonArray remoteCmdArray = jab.build();

        Response response = target.path("ocssw").path("findIFileTypeAndMissionName").path(OCSSW.getJobId()).request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(remoteCmdArray, MediaType.APPLICATION_JSON_TYPE));

        String fileType = target.path("ocssw").path("retrieveIFileType").path(OCSSW.getJobId()).request(MediaType.TEXT_PLAIN_TYPE).get(String.class);
        String missionName = target.path("ocssw").path("retrieveMissionName").path(OCSSW.getJobId()).request(MediaType.TEXT_PLAIN_TYPE).get(String.class);
        String missionDirName = target.path("ocssw").path("retrieveMissionDirName").path(OCSSW.getJobId()).request(MediaType.TEXT_PLAIN_TYPE).get(String.class);
        if (fileType.length() > 0) {
            fileInfos.put(FileInfoFinder.FILE_TYPE_ID_STRING, fileType);
        }

        if (missionName.length() > 0) {
            fileInfos.put(FileInfoFinder.MISSION_NAME_ID_STRING, missionName);
        }

        if (missionDirName.length() > 0) {
            fileInfos.put(FileInfoFinder.MISSION_DIR_NAME_ID_STRING, missionDirName);
        }
        return fileInfos;
    }


    public static HashMap executeLocalLonLat2Pixel(String[] cmdArray, File ifileDir) {
        HashMap<String, String> pixels = new HashMap();
        try {

            Process process = OCSSWRunner.execute(cmdArray, ifileDir);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            String[] tmp;
            while ((line = stdInput.readLine()) != null) {
                SeadasLogger.getLogger().info(line);
                if (line.indexOf("=") != -1) {
                    tmp = line.split("=");
                    pixels.put(tmp[0], tmp[1]);
                }
            }

        } catch (IOException ioe) {
            SeadasLogger.getLogger().severe("Execution exception: " + ioe.getMessage());
        }
        return pixels;
    }

    /**
     *
     * @param cmdArray
     * @return
     *
     */
    public static HashMap executeRemoteLonLat2Pixel(String[] cmdArray) {

        HashMap<String, String> pixels = new HashMap();
        OCSSWClient ocsswClient = new OCSSWClient();
        WebTarget target = ocsswClient.getOcsswWebTarget();
        JsonArrayBuilder jab = Json.createArrayBuilder();
        for (String s : cmdArray) {
            jab.add(s);
        }
        JsonArray remoteCmdArray = jab.build();

        //todo write new methods on the server side.
        // need to receive a jason object that has all four parameters.
        //{"spixl":"value1", "epixl":"value2", "sline":"value3", "eline":"value4"}

        Response response = target.path("ocssw").path("lonlat2pixel").request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(remoteCmdArray, MediaType.APPLICATION_JSON_TYPE));

        JsonObject jo = (JsonObject)response.getEntity();
        pixels.put(LonLat2PixlineConverter.START_PIXEL_PARAM_NAME, jo.getString(LonLat2PixlineConverter.START_PIXEL_PARAM_NAME));
        pixels.put(LonLat2PixlineConverter.END_PIXEL_PARAM_NAME, jo.getString(LonLat2PixlineConverter.END_PIXEL_PARAM_NAME));
        pixels.put(LonLat2PixlineConverter.START_LINE_PARAM_NAME, jo.getString(LonLat2PixlineConverter.START_LINE_PARAM_NAME));
        pixels.put(LonLat2PixlineConverter.END_LINE_PARAM_NAME, jo.getString(LonLat2PixlineConverter.END_LINE_PARAM_NAME));
        return pixels;
    }

    public static Process execute(String[] cmdArray, File ifileDir) {
        String ocsswLocation = RuntimeContext.getConfig().getContextProperty(SEADAS_OCSSW_LOCATION);
        if (ocsswLocation == null || ocsswLocation.trim().equals(LOCAL)) {
            return executeLocal(cmdArray, ifileDir);
        } else {
            return executeRemote(cmdArray);
        }
    }

    public static Process execute(String[] cmdArray) {
        return execute(cmdArray, getDefaultDir());
    }

    private static File getDefaultDir() {
        File rootDir = new File(getProperty("user.dir"));
        return rootDir;
    }

    public static boolean isMonitorProgress() {
        return monitorProgress;
    }

    public static void setMonitorProgress(boolean mProgress) {
        monitorProgress = mProgress;
    }

    public static String[] getCurrentCmdArray(){
        return commandArrayManager.getProgramCommandArray();
    }
}
