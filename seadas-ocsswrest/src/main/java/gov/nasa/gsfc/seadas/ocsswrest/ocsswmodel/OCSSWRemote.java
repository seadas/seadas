package gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.ServerSideFileUtilities;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;

import static gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWServerModel.*;

/**
 * Created by aabduraz on 5/15/17.
 */
public class OCSSWRemote {

    final static String OCSSW_ROOT_PROPERTY ="ocsswroot";
    final static String OCSSW_REST_SERVICES_CONTEXT_PATH = "ocsswws";
    final static String SERVER_WORKING_DIRECTORY_PROPERTY = "serverWorkingDirectory";
    final static String KEEP_INTERMEDIATE_FILES_ON_SERVER_PROPERTY ="keepIntermediateFilesOnServer";

    final static String DEFAULT_OCSSW_ROOT = System.getProperty("user.home") + System.getProperty("file.seperator") + "ocssw";
    final static String DEFAULT_WORKING_DIR = System.getProperty("user.home") + System.getProperty("file.seperator") + "clientFiles";

    private static String NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME = "next_level_name.py";
    private static String NEXT_LEVEL_FILE_NAME_TOKEN = "Output Name:";
    public static final String GET_OBPG_FILE_TYPE_PROGRAM_NAME = "get_obpg_file_type.py";
    public static String OCSSW_INSTALLER_PROGRAM = "install_ocssw.py";

    final String L1AEXTRACT_MODIS = "l1aextract_modis",
            L1AEXTRACT_MODIS_XML_FILE = "l1aextract_modis.xml",
            L1AEXTRACT_SEAWIFS = "l1aextract_seawifs",
            L1AEXTRACT_SEAWIFS_XML_FILE = "l1aextract_seawifs.xml",
            L1AEXTRACT_VIIRS = "l1aextract_viirs",
            L1AEXTRACT_VIIRS_XML_FILE = "l1aextract_viirs.xml",
            L2EXTRACT = "l2extract",
            L2EXTRACT_XML_FILE = "l2extract.xml";


    public static String TMP_OCSSW_INSTALLER_PROGRAM_PATH = (new File(System.getProperty("java.io.tmpdir"), "install_ocssw.py")).getPath();

    String programName;
    String missionName;
    String fileType;
    String xmlFileName;

    public void fileOp(){

    }


    private String[] getCommandArrayPrefix(String programName){
        String[]  commandArrayPrefix;
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
        for (String item:commandArrayPrefix) {
            System.out.println("commandArrayPrefix: " + item);
        }
        return commandArrayPrefix;
    }

    public void executeProgram(String programName, String jobId, String[] commandArray) {

    }


    public Process execute(String[] commandArray) {

        StringBuilder sb = new StringBuilder();
        for (String item:commandArray) {
            sb.append(item + " ");
        }

        ProcessBuilder processBuilder = new ProcessBuilder(commandArray);

        //processBuilder.directory(new File(ifileDir));

        Process process = null;
        try {
            process = processBuilder.start();
            if (process != null) {
                ServerSideFileUtilities.debug("Running the program " + sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return process;
    }

    /**
     * For a given input file, this method finds the file type and mission name before it applies the output file naming convention to compute an output file name.
     * For the generic "extractor" program, it will identify the actual extractor program to be used.
     * The intermediate findings, fileType, missionName, and programName, are saved in the database for future use.
     * @param ifileName
     * @param jobId
     * @return ofilename
     */

    public String getOfileName(String ifileName, String jobId) {
        programName = SQLiteJDBC.getProgramName(jobId);
        System.out.println("finding ofile name for  "  + programName + " with input file " + ifileName );
        extractFileInfo(ifileName, jobId);
        if (programName.equals("extractor")) {
            selectExtractorProgram(jobId);

        }

        if (ifileName == null || programName == null) {
            return null;
        }
        if (programName.equals("l3bindump")) {
            return ifileName + ".xml";
        } else if(programName.equals("extractor")) {

        }

        String[] commandArrayParams = {NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME, ifileName, programName};

        return getOfileName(concatAll(getCommandArrayPrefix(NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME), commandArrayParams));


    }

    protected void extractFileInfo(String ifileName, String jobId){

        String[] fileTypeCommandArrayParams = {GET_OBPG_FILE_TYPE_PROGRAM_NAME, ifileName};

        Process process = execute((String[]) concatAll(getCommandArrayPrefix(GET_OBPG_FILE_TYPE_PROGRAM_NAME), fileTypeCommandArrayParams));

        try {

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = stdInput.readLine();
            System.out.println("line : "  + line);
            if (line != null) {
                String splitLine[] = line.split(":");
                if (splitLine.length == 3) {
                    String missionName = splitLine[1].toString().trim();
                    String fileType = splitLine[2].toString().trim();
System.out.println("mission name : "  + missionName);
                    System.out.println("file type : "  + fileType);
                    if (fileType.length() > 0) {
                        SQLiteJDBC.updateItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.I_FILE_TYPE.getFieldName(), fileType);
                        this.fileType = fileType;
                    }

                    if (missionName.length() > 0) {
                        SQLiteJDBC.updateItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.MISSION_NAME.getFieldName(), missionName);
                        this.missionName = missionName;
                    }
                }
            }
        } catch (IOException ioe) {

            ioe.printStackTrace();
        }
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
                ServerSideFileUtilities.debug("Failed exit code on program '" + NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME + "'");
            }

        } catch (IOException ioe) {

            ioe.printStackTrace();
        }

        return null;
    }


    private void selectExtractorProgram(String jobId) {
        if (missionName != null && fileType != null) {
            if (missionName.indexOf("MODIS") != -1 && fileType.indexOf("1A") != -1) {
                programName = ExtractorPrograms.L1AEXTRACT_MODIS.getExtractorProgramName();
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
        SQLiteJDBC.updateItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.PROGRAM_NAME.getFieldName(), programName);
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
