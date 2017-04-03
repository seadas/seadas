package gov.nasa.gsfc.seadas.processing.core.ocssw;

import gov.nasa.gsfc.seadas.processing.core.ParamList;

import java.io.File;

/**
 * Created by aabduraz on 3/27/17.
 */
public abstract class OCSSW {
    public static final String OCSSW_LOCATION_PROPERTY = "ocssw.location";
    public static final String OCSSW_LOCATION_PROPERTY_VALUE_LOCAL ="local";
    public static final String OCSSW_LOCATION_PROPERTY_VALUE_VIRTUAL="localhost";
    public static final String OCSSW_VIRTUAL_SERVER_PORT_FORWWARD_NUMBER_FOR_CLIENT="6400";
    public static final String OCSSW_SERVER_PORT_NUMBER="6401";



    public static String OCSSW_INSTALLER_PROGRAM_NAME = "install_ocssw.py";
    public static String OCSSW_RUNNER_NAME = "ocssw_runner";
    public static String TMP_OCSSW_INSTALLER_PROGRAM_PATH = (new File(System.getProperty("java.io.tmpdir"), "install_ocssw.py")).getPath();
    public static String OCSSW_INSTALLER_PROGRAM_URL = "https://oceandata.sci.gsfc.nasa.gov/ocssw/install_ocssw.py";

    public static String OCSSW_SCRIPTS_DIR_PATH_SUFFIX =  "run" +  System.getProperty("file.separator") + "scripts";
    public static String OCSSW_DATA_DIR_PATH_SUFFIX =   "run" +  System.getProperty("file.separator") + "data";


    private String ocsswInstallDirPath;
    private String ocsswDataDirPath;
    private String ocsswScriptsDirPath;
    private String ocsswInstallerScriptPath;


    String programName;
    String ifileName;
    String missionName;
    private String fileType;

    String[] commandArrayPrefix;
    String[] commandArraySuffix;

    public abstract boolean isOCSSWExist();
    public abstract String getOcsswDataRoot();
    public abstract String getOcsswScriptPath();
    public abstract String getOcsswRunnerScriptPath();

    public abstract void execute(ParamList paramList);
    public abstract Process execute(String[] commandArray);
    public abstract String getOfileName(String ifileName);
    public abstract String getOfileName(String ifileName, String[] options);
    public abstract String getFileType(String ifileName);

    public abstract String getOcsswDataDirPath();

    public abstract void setOcsswDataDirPath(String ocsswDataDirPath);

    public abstract String getOcsswInstallDirPath();

    public abstract void setOcsswInstallDirPath(String ocsswInstallDirPath) ;
    public abstract String getOcsswScriptsDirPath() ;

    public abstract void setOcsswScriptsDirPath(String ocsswScriptsDirPath);

    public abstract String getOcsswInstallerScriptPath();

    public abstract void setOcsswInstallerScriptPath(String ocsswInstallerScriptPath);

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public void setCommandArrayPrefix() {

        if (programName.equals(OCSSW_INSTALLER_PROGRAM_NAME)) {
            commandArrayPrefix = new String[1];
            commandArrayPrefix[0] = programName;
            if (!isOCSSWExist()) {
                commandArrayPrefix[0] = TMP_OCSSW_INSTALLER_PROGRAM_PATH ;
            } else {
                commandArrayPrefix[0] = getOcsswInstallerScriptPath();
            }
        } else {
            commandArrayPrefix = new String[3];
            commandArrayPrefix[0] = getOcsswRunnerScriptPath();
            commandArrayPrefix[1] = "--ocsswroot";
            commandArrayPrefix[2] = getOcsswInstallDirPath();
        }
    }

    public void setCommandArraySuffix(){

    }

    public String getMissionName() {
        return missionName;
    }

    public abstract void setMissionName(String missionName);

    public String getFileType() {
        return fileType;
    }

    public abstract void setFileType(String fileType) ;

    public String[] getCommandArraySuffix() {
        return commandArraySuffix;
    }

    public void setCommandArraySuffix(String[] commandArraySuffix) {
        this.commandArraySuffix = commandArraySuffix;
    }
}
