package gov.nasa.gsfc.seadas.processing.core;

import java.io.IOException;
import java.util.Map;

/**
 * Created by aabduraz on 6/15/16.
 */
public class LocalOCSSWRunner extends OCSSWRunner {
    OcsswCommandArrayManager commandArrayManager;
    private static final String OCSSW_ROOT_VAR = "OCSSWROOT";
    public static Process execute(ProcessorModel processorModel) {
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



}
