package gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Created by aabduraz on 3/27/17.
 */
public class OCSSWServerModel {

    public static final String OS_64BIT_ARCHITECTURE = "_64";
    public static final String OS_32BIT_ARCHITECTURE = "_32";

    public static final String OCSSW_LOCATION_PROPERTY = "ocssw.location";
    public static final String OCSSW_LOCATION_PROPERTY_VALUE_LOCAL ="local";
    public static final String OCSSW_LOCATION_PROPERTY_VALUE_VIRTUAL ="localhost";
    public static final String OCSSW_VIRTUAL_SERVER_PORT_FORWWARD_NUMBER_FOR_CLIENT="6400";

    public static final String OCSSWROOT_PROPERTY = "ocssw.root";
    public static final String OCSSWROOT_ENVVAR = "OCSSWROOT";
    public static final String SEADASHOME_PROPERTY = "home";

    private static String NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME = "next_level_name.py";
    private static String NEXT_LEVEL_FILE_NAME_TOKEN = "Output Name:";
    public static final String GET_OBPG_FILE_TYPE_PROGRAM_NAME = "get_obpg_file_type.py";
    public static String OCSSW_BIN_DIR_SUFFIX = "run" + File.separator + "bin" +  File.separator + getOSName();
    public static String OCSSW_SCRIPTS_DIR_SUFFIX = "run" + File.separator + "scripts";
    public static String OCSSW_DATA_DIR_SUFFIX = "run" + File.separator + "data";

    public static String OCSSW_INSTALLER_PROGRAM = "install_ocssw.py";
    public static String OCSSW_RUNNER_SCRIPT = "ocssw_runner";

    public static String TMP_OCSSW_INSTALLER_PROGRAM_PATH = (new File(System.getProperty("java.io.tmpdir"), "install_ocssw.py")).getPath();

    public enum ExtractorPrograms{
        L1AEXTRACT_MODIS("l1aextract_modis"),
        L1AEXTRACT_SEAWIFS("l1extract_seawifs"),
        L1AEXTRACT__VIIRS("l1aextract_viirs"),
        L2EXTRACT("l2extract");

        String extractorProgramName;

        ExtractorPrograms(String programName) {
            extractorProgramName = programName;
        }

        public String getExtractorProgramName(){
            return extractorProgramName;
        }
    }

    public enum ExtractorProgramsXMLFiles{
        l1aextract_modis_xml,
        l1aextract_seawifs_xml,
        l1aextract_viirs_xml,
        l2extract_xml
    }

    public enum OCSSWDirectories{
        OCSSW_ROOT(ocsswRoot),
        OCSSW_BIN_DIR_SUFFIX("run" + File.separator + "bin" +  File.separator + getOSName()),
        OCSSW_SCRIPTS_DIR_SUFFIX("run" + File.separator + "scripts"),
        OCSSW_DATA_DIR_SUFFIX( "run" + File.separator + "data"),
        OCSSW_INSTALLER_TMP_DIR((new File(System.getProperty("java.io.tmpdir"), "install_ocssw.py")).getPath());

        String dirPath;
        OCSSWDirectories(String dirPath){
           this.dirPath = dirPath;
        }

        public String getDirPath(){
            return dirPath;
        }
    }

    public enum ProgramsScripts{
        OCSSW_RUNNER_SCRIPT,
        NEXT_LEVEL_NAME,
        GET_OBPG_FILE_TYPE,
        OCSSW_INSTALLER;

    }
    final String L1AEXTRACT_MODIS = "l1aextract_modis",
            L1AEXTRACT_MODIS_XML_FILE = "l1aextract_modis.xml",
            L1AEXTRACT_SEAWIFS = "l1aextract_seawifs",
            L1AEXTRACT_SEAWIFS_XML_FILE = "l1aextract_seawifs.xml",
            L1AEXTRACT_VIIRS = "l1aextract_viirs",
            L1AEXTRACT_VIIRS_XML_FILE = "l1aextract_viirs.xml",
            L2EXTRACT = "l2extract",
            L2EXTRACT_XML_FILE = "l2extract.xml";

    static boolean ocsswExist;
    static String ocsswRoot;
    static String ocsswDataDirPath;
    static String ocsswScriptsDirPath;
    static String ocsswInstallerScriptPath;
    static String ocsswRunnerScriptPath;
    static String ocsswBinDirPath;


    String programName;
    String xmlFileName;
    String ifileName;
    String missionName;
    String fileType;

    String[] commandArrayPrefix;
    String[] commandArraySuffix;
    String[] commandArray;

    String[] additionalOptionsForIfileName;

    static boolean isProgramValid;

    private static String FILE_TABLE_NAME = "FILE_TABLE";
    private static String MISSION_TABLE_NAME = "MISSION_TABLE";
    public static final String PROGRAM_NAME_FIELD_NAME = "PROGRAM_NAME";


    public OCSSWServerModel(){

        initiliaze();
        ocsswExist = isOCSSWExist();

    }

    public static void initiliaze(){
        String ocsswRootPath = System.getProperty("ocsswroot");
        if (ocsswRootPath != null) {
            final File dir = new File(ocsswRootPath + System.getProperty("file.separator") + OCSSW_SCRIPTS_DIR_SUFFIX);
            if (dir.isDirectory()) {
                ocsswExist = true;
                ocsswRoot = ocsswRootPath;
                ocsswScriptsDirPath = ocsswRoot + File.separator + OCSSW_SCRIPTS_DIR_SUFFIX;
                ocsswDataDirPath = ocsswRoot + File.separator +OCSSW_DATA_DIR_SUFFIX;
                ocsswInstallerScriptPath = ocsswScriptsDirPath + System.getProperty("file.separator") + OCSSW_INSTALLER_PROGRAM;
                ocsswRunnerScriptPath = ocsswScriptsDirPath + System.getProperty("file.separator") + OCSSW_RUNNER_SCRIPT;
                ocsswBinDirPath = ocsswRoot + System.getProperty("file.separator") + OCSSW_BIN_DIR_SUFFIX;
            }
        }
    }

    public static String getOSName(){
        String osName = System.getProperty("os.name").toLowerCase();
        if (System.getProperty("os.arch").indexOf("64") != -1 ) {
            return osName + OS_64BIT_ARCHITECTURE;
        } else {
            return osName + OS_32BIT_ARCHITECTURE;
        }
    }

    public static boolean isOCSSWExist(){
        return ocsswExist;
    }


    public String getFileType(String ifileName) {
        return fileType;
    }


    public boolean setProgramName(String programName) {
        this.programName = programName;
        isProgramValid = isProgramValid(programName);
        if (isProgramValid) {
            setCommandArrayPrefix();
            setCommandArraySuffix();
        }
        return isProgramValid;
    }

    /**
     * This method will validate the program name. Only programs exist in $OCSSWROOT/run/scripts and $OSSWROOT/run/bin/"os_name" can be executed on the server side.
     * @param programName
     * @return true if programName is found in the $OCSSWROOT/run/scripts or $OSSWROOT/run/bin/"os_name" directories. Otherwise return false.
     */
    public static boolean isProgramValid(String programName) {
        isProgramValid = false;
        File scriptsFolder = new File(ocsswScriptsDirPath);
        File[] listOfScripts = scriptsFolder.listFiles();
        File runFolder = new File(ocsswBinDirPath);
        File[] listOfPrograms = runFolder.listFiles();

        File[] executablePrograms = concatAll(listOfPrograms, listOfScripts);

        for (File file:executablePrograms) {
            if (file.isFile() && programName.equals(file.getName())) {
                isProgramValid = true;
                break;
            }
        }
        return isProgramValid;
    }

    public void setCommandArrayPrefix() {

        if (programName.equals(OCSSW_INSTALLER_PROGRAM)) {
            commandArrayPrefix = new String[1];
            commandArrayPrefix[0] = programName;
            if (!isOCSSWExist()) {
                commandArrayPrefix[0] = TMP_OCSSW_INSTALLER_PROGRAM_PATH ;
            } else {
                commandArrayPrefix[0] = ocsswInstallerScriptPath;
            }
        } else {
            commandArrayPrefix = new String[3];
            commandArrayPrefix[0] = ocsswRunnerScriptPath;
            commandArrayPrefix[1] = "--ocsswroot";
            commandArrayPrefix[2] = ocsswRoot;
        }
    }

    public String[] getCommandArrayPrefix(String jobId) {
        programName = SQLiteJDBC.retrieveItem(FILE_TABLE_NAME, jobId, PROGRAM_NAME_FIELD_NAME);

        if (programName.equals(OCSSW_INSTALLER_PROGRAM)) {
            commandArrayPrefix = new String[1];
            if (!isOCSSWExist()) {
                commandArrayPrefix[0] = TMP_OCSSW_INSTALLER_PROGRAM_PATH ;
            } else {
                commandArrayPrefix[0] = ocsswInstallerScriptPath;
            }
        } else {
            commandArrayPrefix = new String[3];
            commandArrayPrefix[0] = ocsswRunnerScriptPath;
            commandArrayPrefix[1] = "--ocsswroot";
            commandArrayPrefix[2] = ocsswRoot;
        }
        return commandArrayPrefix;
    }



    public Process execute(String[] commandArray) {

        ProcessBuilder processBuilder = new ProcessBuilder(commandArray);

        String ifileDir = ifileName.substring(0, ifileName.lastIndexOf(System.getProperty("file.separator")));

        processBuilder.directory(new File(ifileDir));

        Process process = null;
        try {
            process = processBuilder.start();
            if (process != null) {
                System.out.println("Running the program " + commandArray.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return process;
    }


    public String getOfileName(String ifileName) {

        this.ifileName = ifileName;
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

        return getOfileName(concatAll(commandArrayPrefix, commandArrayParams));


    }

    private void extractFileInfo(String ifileName){

        String[] fileTypeCommandArrayParams = {GET_OBPG_FILE_TYPE_PROGRAM_NAME, ifileName};

        Process process = execute(concatAll(commandArrayPrefix, fileTypeCommandArrayParams));

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
                        //setMissionName(missionName);
                    }
                }
            }
        } catch (IOException ioe) {

           ioe.printStackTrace();
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


    public String getOfileName(String ifileName, String[] options) {
        if (ifileName == null || programName == null) {
            return null;
        }
        if (programName.equals("l3bindump")) {
            return ifileName + ".xml";
        }

        String[] commandArrayParams = {NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME, ifileName, programName};

        return getOfileName(concatAll(commandArrayPrefix, commandArrayParams, options));
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
                System.out.println("Failed exit code on program '" + NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME + "'");
            }

        } catch (IOException ioe) {

            ioe.printStackTrace();
        }


        String[] fileTypeCommandArrayParams = {GET_OBPG_FILE_TYPE_PROGRAM_NAME, ifileName};

        process = execute(concatAll(commandArrayPrefix, fileTypeCommandArrayParams));

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
                        //setMissionName(missionName);
                    }
                }
            }
        } catch (IOException ioe) {

            ioe.printStackTrace();
        }
        return null;
    }




    public String getProgramName() {
        return programName;
    }


    public void setCommandArraySuffix() {

    }

    public void setOcsswDataDirPath(String ocsswDataDirPath) {

    }



    public void setOcsswInstallDirPath(String ocsswInstallDirPath) {

    }



    public void setOcsswScriptsDirPath(String ocsswScriptsDirPath) {

    }


    public void setOcsswInstallerScriptPath(String ocsswInstallerScriptPath) {

    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * Concatenating an arbitrary number of arrays
     *
     * @param first First array in the list of arrays
     * @param rest  Rest of the arrays in the list to be concatenated
     * @param <T>
     * @return
     */
    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            if (array != null) {
                totalLength += array.length;
            }

        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        return result;
    }
}

