package gov.nasa.gsfc.seadas.ocssw;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.OCSSWInfo;
import gov.nasa.gsfc.seadas.processing.common.Mission;
import gov.nasa.gsfc.seadas.processing.common.MissionInfo;
import gov.nasa.gsfc.seadas.processing.core.ParamList;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

import java.io.*;
import java.util.HashMap;

/**
 * Created by aabduraz on 3/27/17.
 */
public abstract class OCSSW {
    public static final String OCSSW_LOCATION_PROPERTY = "ocssw.location";

    public static final String OCSSW_VIRTUAL_SERVER_PORT_FORWWARD_NUMBER_FOR_CLIENT = "6400";

    public static final String OCSSWROOT_PROPERTY = "ocssw.root";
    public static final String OCSSW_SERVER_PORT_PROPERTY = "ocssw.port";
    public static final String OCSSWROOT_ENVVAR = "OCSSWROOT";
    public static final String SEADASHOME_PROPERTY = "home";


    public static String OCSSW_INSTALLER_PROGRAM = "install_ocssw.py";

    public static String MLP_PAR_FILE_NAME = "mlp_par_file";



    final String L1AEXTRACT_MODIS = "l1aextract_modis",
            L1AEXTRACT_MODIS_XML_FILE = "l1aextract_modis.xml",
            L1AEXTRACT_SEAWIFS = "l1aextract_seawifs",
            L1AEXTRACT_SEAWIFS_XML_FILE = "l1aextract_seawifs.xml",
            L1AEXTRACT_VIIRS = "l1aextract_viirs",
            L1AEXTRACT_VIIRS_XML_FILE = "l1aextract_viirs.xml",
            L2EXTRACT = "l2extract",
            L2EXTRACT_XML_FILE = "l2extract.xml";

    private static boolean monitorProgress = false;


    String programName;
    private String xmlFileName;
    String ifileName;
    public String missionName;
    public String fileType;

    public MissionInfo missionInfo;

    String[] commandArrayPrefix;
    String[] commandArraySuffix;
    String[] commandArray;

    HashMap<String, Mission> missions;

    OCSSWInfo ocsswInfo = OCSSWInfo.getInstance();

    public boolean isOCSSWExist() {
        return ocsswInfo.isOCSSWExist();
    }


    public boolean isProgramValid() {
        return true;
    }

    public boolean isMissionDirExist(String missionName){
        return missions.get(missionName).isMissionExist();
    }
    public String[] getMissionSuites(String missionName) {
        return missions.get(missionName).getMissionSuites();
    }

    public abstract String[] getMissionSuites(String missionName, String programName);

    public abstract Process execute(ProcessorModel processorModel);
    public abstract Process execute(ParamList paramList);

    public abstract Process execute(String[] commandArray);

    public abstract Process execute(String programName, String[] commandArrayParams);

    public abstract String getOfileName(String ifileName);

    public abstract String getOfileName(String ifileName, String[] options);
    public abstract String getOfileName(String ifileName, String programName, String suiteValue );

    void selectExtractorProgram() {
        if (missionName != null && fileType != null) {
            if (missionName.indexOf("MODIS") != -1 && fileType.indexOf("1A") != -1) {
                programName = L1AEXTRACT_MODIS;
                setXmlFileName(L1AEXTRACT_MODIS_XML_FILE);
            } else if (missionName.indexOf("SeaWiFS") != -1 && fileType.indexOf("1A") != -1 || missionName.indexOf("CZCS") != -1) {
                programName = L1AEXTRACT_SEAWIFS;
                setXmlFileName(L1AEXTRACT_SEAWIFS_XML_FILE);
            } else if (missionName.indexOf("VIIRS") != -1 && fileType.indexOf("1A") != -1) {
                programName = L1AEXTRACT_VIIRS;
                setXmlFileName(L1AEXTRACT_VIIRS_XML_FILE);
            } else if ((fileType.indexOf("L2") != -1 || fileType.indexOf("Level 2") != -1) ||
                    (missionName.indexOf("OCTS") != -1 && (fileType.indexOf("L1") != -1 || fileType.indexOf("Level 1") != -1))) {
                programName = L2EXTRACT;
                setXmlFileName(L2EXTRACT_XML_FILE);
            }
        }
        setProgramName(programName);
    }


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

    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
        missionInfo.setName(missionName);

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
            ocsswInfo.setOcsswRoot(installDir);
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

    public String getXmlFileName() {
        return xmlFileName;
    }

    public void setXmlFileName(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }

    public static boolean isMonitorProgress() {
        return monitorProgress;
    }

    public static void setMonitorProgress(boolean mProgress) {
        monitorProgress = mProgress;
    }

    public HashMap<String, String> computePixelsFromLonLat(){
        return null;
    }
}
