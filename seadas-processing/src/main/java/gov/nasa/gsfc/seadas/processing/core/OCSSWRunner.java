package gov.nasa.gsfc.seadas.processing.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    private final String OCSSW_ROOT_VAR = "OCSSWROOT";
    public OCSSWRunner() {

       environment.put(OCSSW_ROOT_VAR, OCSSW.getOcsswEnv());
    }

    public static Process execute(String[] cmdArray, File ifileDir) {
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);

        Map<String, String> env = processBuilder.environment();
         if (environment != null)
            env.putAll(environment);

        if (ifileDir != null) {
            processBuilder.directory(ifileDir);
        } else  {
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
        File rootDir = new File(System.getProperty("user.dir"));
        return rootDir;
    }
}
