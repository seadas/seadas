package gov.nasa.gsfc.seadas.processing.core.ocssw;

import gov.nasa.gsfc.seadas.processing.core.ParamList;

import java.io.File;

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


    public static String OCSSW_INSTALLER_PROGRAM_NAME = "install_ocssw.py";
    public static String OCSSW_RUNNER_NAME = "ocssw_runner";
    public static String TMP_OCSSW_INSTALLER_PROGRAM_PATH = (new File(System.getProperty("java.io.tmpdir"), "install_ocssw.py")).getPath();
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
    String ifileName;
    String missionName;
    String fileType;

    String[] commandArrayPrefix;
    String[] commandArraySuffix;

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

    public abstract void execute(ParamList paramList);
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
}
