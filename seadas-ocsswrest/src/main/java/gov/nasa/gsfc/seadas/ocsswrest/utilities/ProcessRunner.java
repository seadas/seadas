package gov.nasa.gsfc.seadas.ocsswrest.utilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/24/13
 * Time: 2:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessRunner {
    public static Process execute(String[] cmdArray) {
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
        Map<String, String> env = processBuilder.environment();
        HashMap environment = new HashMap();

        environment.put("OCSSWROOT", OCSSW.getOcsswEnv());

        env.putAll(environment);

        processBuilder.directory(new File(ServerSideFileUtilities.FILE_UPLOAD_PATH));
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException ioe) {

        }
        return process;
    }
}
