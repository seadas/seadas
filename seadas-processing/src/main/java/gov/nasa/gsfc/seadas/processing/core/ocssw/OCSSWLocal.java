package gov.nasa.gsfc.seadas.processing.core.ocssw;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.processing.common.FileInfoFinder;
import gov.nasa.gsfc.seadas.processing.common.SeadasFileUtils;
import gov.nasa.gsfc.seadas.processing.core.ParamList;
import gov.nasa.gsfc.seadas.processing.utilities.SeadasArrayUtils;
import org.apache.commons.lang.ArrayUtils;
import org.esa.beam.util.Debug;
import org.esa.beam.visat.VisatApp;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by aabduraz on 3/27/17.
 */
public class OCSSWLocal extends OCSSW {


    private static String NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME = "next_level_name.py";
    private static String NEXT_LEVEL_FILE_NAME_TOKEN = "Output Name:";
    public static final String GET_OBPG_FILE_TYPE_PROGRAM_NAME = "get_obpg_file_type.py";

    @Override
    public boolean isOCSSWExist() {
        return false;
    }

    @Override
    public String getOcsswDataRoot() {
        return null;
    }

    @Override
    public String getOcsswScriptPath() {
        return null;
    }

    @Override
    public String getOcsswRunnerScriptPath() {
        return null;
    }

    @Override
    public void execute(ParamList paramListl) {

    }

    @Override
    public Process execute(String[] commandArray) {

        //String ifileDir = ifileName.substring(0, ifileName.lastIndexOf(System.getProperty("file.separator")));

        return null;
    }

    @Override
    public String getOfileName(String ifileName) {
        if (ifileName == null || programName == null) {
            return null;
        }
        if (programName.equals("l3bindump")) {
            return ifileName + ".xml";
        }

        String[] commandArrayParams = {NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME, ifileName, programName};

        return getOfileName(SeadasArrayUtils.concatAll(commandArrayPrefix, commandArrayParams));


    }

    @Override
    public String getOfileName(String ifileName, String[] options) {
        if (ifileName == null || programName == null) {
            return null;
        }
        if (programName.equals("l3bindump")) {
            return ifileName + ".xml";
        }

        String[] commandArrayParams = {NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME, ifileName, programName};

        return getOfileName(SeadasArrayUtils.concatAll(commandArrayPrefix, commandArrayParams, options));
    }


    private String getOfileName(String[] commandArray){

        Process process = execute(commandArray);

        if (process == null) {
            return null;
        }

        //wait for process to exit
        try {
            Field field = process.getClass().getDeclaredField("hasExited");
            field.setAccessible(true);
            while (!(Boolean) field.get(process)) {
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
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
                String line = br.readLine();
                while (line != null) {
                    if (line.startsWith(NEXT_LEVEL_FILE_NAME_TOKEN)) {
                        return (line.substring(NEXT_LEVEL_FILE_NAME_TOKEN.length())).trim();
                    }
                    line = br.readLine();
                }

            } else {
                Debug.trace("Failed exit code on program '" + NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME + "'");
            }

        } catch (IOException ioe) {

            VisatApp.getApp().showErrorDialog(ioe.getMessage());
        }


        String[] fileTypeCommandArrayParams = {GET_OBPG_FILE_TYPE_PROGRAM_NAME, ifileName};

        process = execute((String[]) ArrayUtils.addAll(commandArrayPrefix, fileTypeCommandArrayParams));

        try {

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = stdInput.readLine();
            if (line != null) {
                String splitLine[] = line.split(":");
                if (splitLine.length == 3) {
                    String missionName = splitLine[1].toString().trim();
                    String fileType = splitLine[2].toString().trim();

                    if (fileType.length() > 0) {
                        setFileType(fileType);
                    }

                    if (missionName.length() > 0) {
                        setMissionName(missionName);
                    }
                }
            }
        } catch (IOException ioe) {

            VisatApp.getApp().showErrorDialog(ioe.getMessage());
        }
        return null;
    }

    @Override
    public String getFileType(String ifileName) {
        return null;
    }

    @Override
    public String getOcsswDataDirPath() {
        return null;
    }

    @Override
    public void setOcsswDataDirPath(String ocsswDataDirPath) {

    }

    @Override
    public String getOcsswInstallDirPath() {
        return null;
    }

    @Override
    public void setOcsswInstallDirPath(String ocsswInstallDirPath) {

    }

    @Override
    public String getOcsswScriptsDirPath() {
        return null;
    }

    @Override
    public void setOcsswScriptsDirPath(String ocsswScriptsDirPath) {

    }

    @Override
    public String getOcsswInstallerScriptPath() {
        return null;
    }

    @Override
    public void setOcsswInstallerScriptPath(String ocsswInstallerScriptPath) {

    }

    @Override
    public void setMissionName(String missionName) {
        this.missionName = missionName;

    }

    @Override
    public void setFileType(String fileType) {

    }
}
