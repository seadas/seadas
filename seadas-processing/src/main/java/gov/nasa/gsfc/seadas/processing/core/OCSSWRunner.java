package gov.nasa.gsfc.seadas.processing.core;

import com.bc.ceres.core.runtime.RuntimeContext;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import gov.nasa.gsfc.seadas.processing.general.RSClient;
import org.esa.beam.visat.VisatApp;

import java.io.File;
import java.io.IOException;
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

    public OCSSWRunner() {

        environment.put(OCSSW_ROOT_VAR, OCSSW.getOcsswEnvArray());
    }

    public static Process execute(ProcessorModel processorModel) {

        String ocsswLocation = RuntimeContext.getConfig().getContextProperty(SEADAS_OCSSW_LOCATION);
        if (ocsswLocation == null || ocsswLocation.trim().equals(LOCAL)) {
            return executeLocal(processorModel);
        } else {
            return executeRemote(processorModel);
        }
    }

    public static Process execute(String[] cmdArray, File ifileDir) {
        String ocsswLocation = RuntimeContext.getConfig().getContextProperty(SEADAS_OCSSW_LOCATION);
        if (ocsswLocation == null || ocsswLocation.trim().equals(LOCAL)) {
            return executeLocal(cmdArray, ifileDir);
        } else {
            return executeRemote(cmdArray, ifileDir);
        }
    }

    public static Process executeLocal(ProcessorModel processorModel) {
        //System.out.println("local execution!" + " " + Arrays.toString(processorModel.getProgramCmdArray()));
        ProcessBuilder processBuilder = new ProcessBuilder(processorModel.getProgramCmdArray());
        Map<String, String> env = processBuilder.environment();

        if (!env.containsKey(OCSSW_ROOT_VAR) && OCSSW.isOCSSWExist()) {
            //System.out.println("error checkpoint!");
            env.put(OCSSW_ROOT_VAR, OCSSW.getOcsswEnv());
        }

        if (processorModel.getIFileDir() != null) {
            processBuilder.directory(processorModel.getIFileDir());
        } else {
            //processBuilder.directory(getDefaultDir());
        }
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException ioe) {

        }
        return process;
    }

    public static Process executeRemote(ProcessorModel processorModel) {
        //System.out.println("remote execution!");
        Process process = null;

        Gson gson = new Gson();
        String json = gson.toJson(processorModel.getProgramCmdArray());

        return process;
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
            int exitValue = process.waitFor();
        } catch (Exception e) {
            VisatApp.getApp().showErrorDialog("OCSSW execution error from SeaDAS application! \n" + cmdArray[0] + "  program is not executed correctly.");
            e.printStackTrace();
        }
        return process;
    }

    public static Process executeRemote(String[] cmdArray, File ifileDir) {

        Gson gson = new Gson();
        String json = gson.toJson(cmdArray);
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(json).getAsJsonArray();

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);

        Map<String, String> env = processBuilder.environment();
        if (environment != null)
            env.putAll(environment);

        if (ifileDir != null) {
            processBuilder.directory(ifileDir);
        } else {
            //processBuilder.directory(getDefaultDir());
        }
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException ioe) {

        }
        return process;
    }

    public static Process execute(String[] cmdArray) {
        return execute(cmdArray, getDefaultDir());
    }

    private static File getDefaultDir() {
        File rootDir = new File(getProperty("user.dir"));
        return rootDir;
    }

    public void remoteExecuteProgram(ProcessorModel pm) {

        RSClient ocsswClient = new RSClient();
        pm.getProgramCmdArray();

        String paramString = pm.getCmdArrayString();
        String[] filesToUpload = pm.getFilesToUpload();

        boolean fileUploadSuccess = ocsswClient.uploadFile(filesToUpload);

        if (fileUploadSuccess) {
            //System.out.println("file upload is successful!");

            ocsswClient.uploadParFile(pm.getParStringForRemoteServer());
            ocsswClient.uploadParam(paramString);
            //ocsswClient.runOCSSW();
        } else {
            //System.out.println("file upload failed!");
        }
    }
}
