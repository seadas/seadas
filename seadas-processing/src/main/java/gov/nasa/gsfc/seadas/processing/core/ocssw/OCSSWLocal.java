package gov.nasa.gsfc.seadas.processing.core.ocssw;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.processing.common.SeadasFileUtils;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ParamList;
import gov.nasa.gsfc.seadas.processing.utilities.SeadasArrayUtils;
import org.apache.commons.lang.ArrayUtils;
import org.esa.beam.util.Debug;
import org.esa.beam.visat.VisatApp;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

import static java.lang.System.getProperty;

/**
 * Created by aabduraz on 3/27/17.
 */
public class OCSSWLocal extends OCSSW {

    private static String NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME = "next_level_name.py";
    private static String NEXT_LEVEL_FILE_NAME_TOKEN = "Output Name:";
    public static final String GET_OBPG_FILE_TYPE_PROGRAM_NAME = "get_obpg_file_type.py";
    public static String OCSSW_SCRIPTS_DIR_SUFFIX = "run" + System.getProperty("file.separator") + "scripts";
    public static String OCSSW_DATA_DIR_SUFFIX = "run" + System.getProperty("file.separator") + "data";


    public static String TMP_OCSSW_INSTALLER_PROGRAM_PATH = (new File(System.getProperty("java.io.tmpdir"), "install_ocssw.py")).getPath();


    final String L1AEXTRACT_MODIS = "l1aextract_modis",
            L1AEXTRACT_MODIS_XML_FILE = "l1aextract_modis.xml",
            L1AEXTRACT_SEAWIFS = "l1aextract_seawifs",
            L1AEXTRACT_SEAWIFS_XML_FILE = "l1aextract_seawifs.xml",
            L1AEXTRACT_VIIRS = "l1aextract_viirs",
            L1AEXTRACT_VIIRS_XML_FILE = "l1aextract_viirs.xml",
            L2EXTRACT = "l2extract",
            L2EXTRACT_XML_FILE = "l2extract.xml";


    public OCSSWLocal(){

        initiliaze();
        ocsswExist = isOCSSWExist();

    }

    private void initiliaze(){
        String dirPath = RuntimeContext.getConfig().getContextProperty(OCSSWROOT_PROPERTY, System.getenv(OCSSWROOT_ENVVAR));

        if (dirPath == null) {
            dirPath = RuntimeContext.getConfig().getContextProperty(SEADASHOME_PROPERTY, System.getProperty("user.home") + System.getProperty("file.separator") + "ocssw");
        }
        if (dirPath != null) {
            final File dir = new File(dirPath + System.getProperty("file.separator") + OCSSW_SCRIPTS_DIR_SUFFIX);
            if (dir.isDirectory()) {
                ocsswExist = true;
                ocsswRoot = dirPath;
                ocsswScriptsDirPath = ocsswRoot + System.getProperty("file.separator") + OCSSW_SCRIPTS_DIR_SUFFIX;
                ocsswDataDirPath = ocsswRoot + System.getProperty("file.separator") +OCSSW_DATA_DIR_SUFFIX;
                ocsswInstallerScriptPath = ocsswScriptsDirPath + System.getProperty("file.separator") + OCSSW_INSTALLER_PROGRAM;
                ocsswRunnerScriptPath = ocsswScriptsDirPath + System.getProperty("file.separator") + OCSSW_RUNNER_SCRIPT;
            }
        }
    }

    @Override
    public String getFileType(String ifileName) {
        return fileType;
    }



    @Override
    public Process execute(ParamList paramListl) {
        String[] programNameArray = {programName};
        commandArray = SeadasArrayUtils.concatAll(commandArrayPrefix, programNameArray, getCommandArrayParam(paramListl), commandArraySuffix);
        return execute(commandArray);
    }

    @Override
    public Process execute(String[] commandArray) {

        ProcessBuilder processBuilder = new ProcessBuilder(commandArray);

        String ifileDir = getIfileName().substring(0, getIfileName().lastIndexOf(System.getProperty("file.separator")));

        processBuilder.directory(new File(ifileDir));

        Process process = null;
        try {
            process = processBuilder.start();
            if (process != null) {
                SeadasFileUtils.debug("Running the program " + commandArray.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return process;
    }

    @Override
    public String getOfileName(String ifileName) {

        this.setIfileName(ifileName);
        extractFileInfo(ifileName);
        if (programName.equals("extractor")) {
            selectExtractorProgram();
        }

        if (ifileName == null || programName == null) {
            return null;
        }
        if (programName.equals("l3bindump")) {
            return ifileName + ".xml";
        } else if(programName.equals("extractor")) {

        }

        String[] commandArrayParams = {NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME, ifileName, programName};

        return getOfileName(SeadasArrayUtils.concatAll(commandArrayPrefix, commandArrayParams));


    }

    private void extractFileInfo(String ifileName){

        String[] fileTypeCommandArrayParams = {GET_OBPG_FILE_TYPE_PROGRAM_NAME, ifileName};

        Process process = execute((String[]) ArrayUtils.addAll(commandArrayPrefix, fileTypeCommandArrayParams));

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
    }

    private void selectExtractorProgram() {
        if (missionName != null && fileType != null) {
            if (missionName.indexOf("MODIS") != -1 && fileType.indexOf("1A") != -1) {
                programName = L1AEXTRACT_MODIS;
                xmlFileName = L1AEXTRACT_MODIS_XML_FILE;
            } else if (missionName.indexOf("SeaWiFS") != -1 && fileType.indexOf("1A") != -1 ||missionName.indexOf("CZCS") != -1) {
                programName = L1AEXTRACT_SEAWIFS;
                xmlFileName = L1AEXTRACT_SEAWIFS_XML_FILE;
            } else if (missionName.indexOf("VIIRS") != -1 && fileType.indexOf("1A") != -1) {
                programName = L1AEXTRACT_VIIRS;
                xmlFileName = L1AEXTRACT_VIIRS_XML_FILE;
            } else if ((fileType.indexOf("L2") != -1 || fileType.indexOf("Level 2") != -1) ||
                    (missionName.indexOf("OCTS") != -1 && (fileType.indexOf("L1") != -1 || fileType.indexOf("Level 1") != -1))) {
                programName = L2EXTRACT;
                xmlFileName = L2EXTRACT_XML_FILE;
            }
        }
        setProgramName(programName);
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


    private String getOfileName(String[] commandArray) {

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

        return null;
    }

    public void setCommandArrayPrefix() {

        if (programName.equals(OCSSW_INSTALLER_PROGRAM)) {
            commandArrayPrefix = new String[1];
            commandArrayPrefix[0] = programName;
            if (!isOCSSWExist()) {
                commandArrayPrefix[0] = TMP_OCSSW_INSTALLER_PROGRAM_PATH ;
            } else {
                commandArrayPrefix[0] = getOcsswInstallerScriptPath();
            }
        } else {
            commandArrayPrefix = new String[3];
            commandArrayPrefix[0] = ocsswRunnerScriptPath;
            commandArrayPrefix[1] = "--ocsswroot";
            commandArrayPrefix[2] = ocsswRoot;
        }
    }

    private String[] getCommandArrayParam(ParamList paramList) {

        ArrayList<String> commandArrayList = new ArrayList();

        Iterator itr = paramList.getParamArray().iterator();

        ParamInfo option;
        int optionOrder;
        String optionValue;
        int i = 0;
        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();
            optionOrder = option.getOrder();
            optionValue = option.getValue();
            if (option.getType() != ParamInfo.Type.HELP) {
                if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_ARGUMENT)) {
                    if (option.getValue() != null && option.getValue().length() > 0) {
                        commandArrayList.add(optionValue);
                    }
                } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_OPTION) && !option.getDefaultValue().equals(option.getValue())) {
                    commandArrayList.add(option.getName() + "=" + optionValue);
                } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_FLAG) && (option.getValue().equals("true") || option.getValue().equals("1"))) {
                    if (option.getName() != null && option.getName().length() > 0) {
                        commandArrayList.add(option.getName());
                    }
                }
            }
        }
        String[] commandArrayParam = new String[commandArrayList.size()];
        commandArrayParam = commandArrayList.toArray(commandArrayParam);
        return commandArrayParam;
    }

    @Override
    public void setCommandArraySuffix() {

    }

    @Override
    public void setOcsswDataDirPath(String ocsswDataDirPath) {

    }


    @Override
    public void setOcsswInstallDirPath(String ocsswInstallDirPath) {

    }


    @Override
    public void setOcsswScriptsDirPath(String ocsswScriptsDirPath) {

    }


    @Override
    public void setOcsswInstallerScriptPath(String ocsswInstallerScriptPath) {

    }
}
