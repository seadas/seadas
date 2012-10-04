package gov.nasa.gsfc.seadas.processing.core;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 10/3/12
 * Time: 12:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class OCSSWRunner {

    //private static ProcessBuilder processBuilder;

    public OCSSWRunner() {

    }

    public static Process execute(String[] cmdArray, File ifileDir) {
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
        if (ifileDir != null) {
            processBuilder.directory(ifileDir);
        } else  {
            processBuilder.directory(getDefaultDir());
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
