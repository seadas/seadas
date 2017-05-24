package gov.nasa.gsfc.seadas.ocssw;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.processing.core.ParamList;

import java.io.*;

/**
 * Created by aabduraz on 3/27/17.
 */
public abstract class OCSSW {
    public static final String OCSSW_LOCATION_PROPERTY = "ocssw.location";
    public static final String OCSSW_LOCATION_PROPERTY_VALUE_LOCAL ="local";
    public static final String OCSSW_LOCATION_PROPERTY_VALUE_VIRTUAL ="localhost";
    public static final String OCSSW_VIRTUAL_SERVER_PORT_FORWWARD_NUMBER_FOR_CLIENT="6400";

    public static final String OCSSWROOT_PROPERTY = "ocssw.root";
    public static final String OCSSWROOT_ENVVAR = "OCSSWROOT";
    public static final String SEADASHOME_PROPERTY = "home";


    public static String OCSSW_INSTALLER_PROGRAM = "install_ocssw.py";
    public static String OCSSW_RUNNER_SCRIPT = "ocssw_runner";

    public static String OCSSW_INSTALLER_PROGRAM_URL = "https://oceandata.sci.gsfc.nasa.gov/ocssw/install_ocssw.py";

    public static String OCSSW_SCRIPTS_DIR_PATH_SUFFIX =  "run" +  System.getProperty("file.separator") + "scripts";
    public static String OCSSW_DATA_DIR_PATH_SUFFIX =   "run" +  System.getProperty("file.separator") + "data";

     boolean ocsswExist;
     String ocsswRoot;
     String ocsswDataDirPath;
     String ocsswScriptsDirPath;
     String ocsswInstallerScriptPath;
     String ocsswRunnerScriptPath;


    String programName;
    String xmlFileName;
    String ifileName;
    String missionName;
    String fileType;

    String[] commandArrayPrefix;
    String[] commandArraySuffix;
    String[] commandArray;

    public boolean isOCSSWExist(){
        return ocsswExist;
    }

    public String getOcsswInstallDirPath(){
        return ocsswRoot;
    }

    public String getOcsswScriptsDirPath() {
        return ocsswScriptsDirPath;
    }

    public String getOcsswDataDirPath() {
        return ocsswDataDirPath;
    }

    public String getOcsswInstallerScriptPath() {
        return ocsswInstallerScriptPath;
    }

    public String getOcsswRunnerScriptPath() {
        return ocsswRunnerScriptPath;
    }

    public abstract Process execute(ParamList paramList);
    public abstract Process execute(String[] commandArray);
    public abstract String getOfileName(String ifileName);
    public abstract String getOfileName(String ifileName, String[] options);
    public abstract String getFileType(String ifileName);



    public abstract void setOcsswDataDirPath(String ocsswDataDirPath);



    public abstract void setOcsswInstallDirPath(String ocsswInstallDirPath) ;


    public abstract void setOcsswScriptsDirPath(String ocsswScriptsDirPath);



    public abstract void setOcsswInstallerScriptPath(String ocsswInstallerScriptPath);

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
        setCommandArrayPrefix();
        setCommandArraySuffix();
    }


    public abstract void setCommandArrayPrefix();

    public abstract void setCommandArraySuffix();

    public String getMissionName(String ifileName) {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public String[] getCommandArraySuffix() {
        return commandArraySuffix;
    }

    public void setCommandArraySuffix(String[] commandArraySuffix) {
        this.commandArraySuffix = commandArraySuffix;
    }

    public boolean isOcsswExist() {
        return ocsswExist;
    }

    public void setOcsswExist(boolean ocsswExist) {
        this.ocsswExist = ocsswExist;
    }
    public void updateOCSSWRoot(String installDir) {
        FileWriter fileWriter = null;
        try {
            final FileReader reader = new FileReader(new File(RuntimeContext.getConfig().getConfigFilePath()));
            final BufferedReader br = new BufferedReader(reader);

            StringBuilder text = new StringBuilder();
            String line;
            boolean isOCSSWRootSpecified = false;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("seadas.ocssw.root")) {
                    line = "seadas.ocssw.root = " + installDir;
                    isOCSSWRootSpecified = true;
                }
                text.append(line);
                text.append("\n");
            }
            //Append "seadas.ocssw.root = " + installDir + "\n" to the runtime config file if it is not exist
            if (!isOCSSWRootSpecified) {
                text.append("seadas.ocssw.root = " + installDir + "\n");
            }
            fileWriter = new FileWriter(new File(RuntimeContext.getConfig().getConfigFilePath()));
            fileWriter.write(text.toString());
            if (fileWriter != null) {
                fileWriter.close();
            }
            ocsswRoot = installDir;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public String[] getCommandArray() {
        return commandArray;
    }

    public void setCommandArray(String[] commandArray) {
        this.commandArray = commandArray;
    }

    public String getIfileName() {
        return ifileName;
    }

    public void setIfileName(String ifileName) {
        this.ifileName = ifileName;
    }
}
