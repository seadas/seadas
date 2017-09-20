package gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;
import gov.nasa.gsfc.seadas.ocsswrest.process.ORSProcessObserver;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.ServerSideFileUtilities;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.swing.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWServerModel.*;

/**
 * Created by aabduraz on 5/15/17.
 */
public class OCSSWRemote {

    final static String OCSSW_ROOT_PROPERTY = "ocsswroot";
    final static String OCSSW_REST_SERVICES_CONTEXT_PATH = "ocsswws";
    final static String SERVER_WORKING_DIRECTORY_PROPERTY = "serverWorkingDirectory";
    final static String KEEP_INTERMEDIATE_FILES_ON_SERVER_PROPERTY = "keepIntermediateFilesOnServer";

    final static String DEFAULT_OCSSW_ROOT = System.getProperty("user.home") + System.getProperty("file.seperator") + "ocssw";
    final static String DEFAULT_WORKING_DIR = System.getProperty("user.home") + System.getProperty("file.seperator") + "clientFiles";

    private static String NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME = "next_level_name.py";
    private static String NEXT_LEVEL_FILE_NAME_TOKEN = "Output Name:";
    public static final String GET_OBPG_FILE_TYPE_PROGRAM_NAME = "get_obpg_file_type.py";
    public static String OCSSW_INSTALLER_PROGRAM = "install_ocssw.py";
    public static String MLP_PROGRAM_NAME = "multilevel_processor.py";
    public static String MLP_PAR_FILE_NAME = "multilevel_processor_parFile.txt";
    public static String MLP_OUTPUT_DIR_NAME = "mlpOutputDir";

    public static String PROCESS_STDOUT_FILE_NAME_EXTENSION = ".log";

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

    public void fileOp() {

    }


    public String[] getCommandArrayPrefix(String programName) {
        String[] commandArrayPrefix;
        if (programName.equals(OCSSW_INSTALLER_PROGRAM)) {
            commandArrayPrefix = new String[1];
            if (!isOCSSWExist()) {
                commandArrayPrefix[0] = TMP_OCSSW_INSTALLER_PROGRAM_PATH;
            } else {
                commandArrayPrefix[0] = getOcsswInstallerScriptPath();
            }
        } else {
            commandArrayPrefix = new String[3];
            commandArrayPrefix[0] = getOcsswRunnerScriptPath();
            commandArrayPrefix[1] = "--ocsswroot";
            commandArrayPrefix[2] = getOcsswRoot();
        }
        for (String item : commandArrayPrefix) {
            System.out.println("commandArrayPrefix: " + item);
        }
        return commandArrayPrefix;
    }

    public void executeProgram(String jobId, String[] commandArray) {

        programName = SQLiteJDBC.getProgramName(jobId);
        executeProcess(concatAll(getCommandArrayPrefix(programName), commandArray), jobId);
    }

    public void executeProgram(String jobId, JsonObject jsonObject) {
        programName = SQLiteJDBC.getProgramName(jobId);
        String serverWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        Set commandArrayKeys = jsonObject.keySet();
        System.out.println(" array size = " + commandArrayKeys.size());
        String commandArrayElement;
        try {
            Object[] array = (Object[]) commandArrayKeys.toArray();
            int i = 0;
            String[] commandArray = new String[commandArrayKeys.size() + 1];
            commandArray[i++] = programName;
            for (Object element : array) {
                System.out.println(" element = " + element);
                String elementName = (String) element;
                commandArrayElement = jsonObject.getString((String) element);
                if (elementName.contains("IFILE") || elementName.contains("OFILE")) {
                    if (commandArrayElement.indexOf("=") != -1) {
                        StringTokenizer st = new StringTokenizer(commandArrayElement, "=");
                        String paramName = st.nextToken();
                        String paramValue = st.nextToken();
                        commandArrayElement = paramName + "=" + serverWorkingDir + paramValue.substring(paramValue.lastIndexOf(File.separator));

                    } else {
                        commandArrayElement = serverWorkingDir + commandArrayElement.substring(commandArrayElement.lastIndexOf(File.separator));
                    }
                }
                System.out.println("command array element = " + commandArrayElement);
                commandArray[i++] = commandArrayElement;
            }
            executeProcess(concatAll(getCommandArrayPrefix(programName), commandArray), jobId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeProgramOnDemand(String jobId, String programName, JsonObject jsonObject) {
        String serverWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        Set commandArrayKeys = jsonObject.keySet();
        System.out.println(" array size = " + commandArrayKeys.size());
        String commandArrayElement;
        try {
            Object[] array = (Object[]) commandArrayKeys.toArray();
            int i = 0;
            String[] commandArray = new String[commandArrayKeys.size() + 1];
            commandArray[i++] = programName;
            for (Object element : array) {
                System.out.println(" element = " + element);
                String elementName = (String) element;
                commandArrayElement = jsonObject.getString((String) element);
                if (elementName.contains("IFILE") || elementName.contains("OFILE")) {
                    if (commandArrayElement.indexOf("=") != -1) {
                        StringTokenizer st = new StringTokenizer(commandArrayElement, "=");
                        String paramName = st.nextToken();
                        String paramValue = st.nextToken();
                        commandArrayElement = paramName + "=" + serverWorkingDir + paramValue.substring(paramValue.lastIndexOf(File.separator));

                    } else {
                        commandArrayElement = serverWorkingDir + commandArrayElement.substring(commandArrayElement.lastIndexOf(File.separator));
                    }
                }
                System.out.println("command array element = " + commandArrayElement);
                commandArray[i++] = commandArrayElement;
            }
            executeProcess(concatAll(getCommandArrayPrefix(programName), commandArray), jobId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void executeMLP(String jobId, File parFile) {
        System.out.println("par file path: " + parFile.getAbsolutePath());
        String workingFileDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String parFileNewLocation = workingFileDir + File.separator + jobId + File.separator + MLP_PAR_FILE_NAME;
        System.out.println("par file new path: " + parFileNewLocation);
        String parFileContent = convertClientParFilForRemoteServer(parFile, jobId);
        ServerSideFileUtilities.writeStringToFile(parFileContent, parFileNewLocation);
        String[] commandArray = {MLP_PROGRAM_NAME, parFileNewLocation};
        execute(concatAll(getCommandArrayPrefix(MLP_PROGRAM_NAME), commandArray), new File(parFileNewLocation).getParent(), jobId);
    }

    public JsonObject getMLPOutputFilesList(String jobId) {
        String workingFileDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String mlpDir = workingFileDir + File.separator + jobId;
        Collection<String> filesInMLPDir = ServerSideFileUtilities.getFilesList(mlpDir);
        Collection<String> inputFiles = SQLiteJDBC.getInputFilesList(jobId);
        filesInMLPDir.removeAll(inputFiles);
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        Iterator itr = filesInMLPDir.iterator();
        while (itr.hasNext()) {
            jsonObjectBuilder.add("OFILE", (String) itr.next());
        }
        return jsonObjectBuilder.build();
    }

    public InputStream getProcessStdoutFile(String jobId) {
        String workingFileDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String workingDir = workingFileDir + File.separator + jobId;
        String processStdoutFileName = workingDir + File.separator + programName + PROCESS_STDOUT_FILE_NAME_EXTENSION;
        if (programName.equals(MLP_PROGRAM_NAME)) {
            processStdoutFileName = ServerSideFileUtilities.getLogFileName(workingDir);
        }
        File processStdoutFile = new File(processStdoutFileName);
        InputStream processStdoutStream = null;
        try {
             processStdoutStream = new FileInputStream(processStdoutFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return processStdoutStream;
    }


    public JsonObject getMLPOutputFilesJsonList(String jobId) {
        String workingFileDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String mlpOutputDir = workingFileDir + File.separator + jobId + File.separator + MLP_OUTPUT_DIR_NAME;
        Collection<String> filesInMLPDir = ServerSideFileUtilities.getFilesList(mlpOutputDir);
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        Iterator itr = filesInMLPDir.iterator();
        int i = 0;
        while (itr.hasNext()) {
            jsonObjectBuilder.add("OFILE" + i++, (String) itr.next());
        }
        return jsonObjectBuilder.build();
    }

    private String convertClientParFilForRemoteServer(File parFile, String jobId) {
        String parString = readFile(parFile.getAbsolutePath(), StandardCharsets.UTF_8);
        StringTokenizer st1 = new StringTokenizer(parString, "\n");
        StringTokenizer st2;
        StringBuilder stringBuilder = new StringBuilder();
        String token;
        String key, value;
        String fileTypeString;
        boolean isOdirdefined = true;


        String workingFileDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String mlpDir = workingFileDir + File.separator + jobId;
        String mlpOutputDir = mlpDir + File.separator + MLP_OUTPUT_DIR_NAME;

        while (st1.hasMoreTokens()) {
            token = st1.nextToken();
            if (token.contains("=")) {
                st2 = new StringTokenizer(token, "=");
                key = st2.nextToken();
                value = st2.nextToken();
                if (new File(mlpDir + File.separator + value).exists()) {
                    value = mlpDir + File.separator + value;
                    isOdirdefined = false;
                    try {
                        fileTypeString = Files.probeContentType(new File(value).toPath());
                        if (fileTypeString.equals(MediaType.TEXT_PLAIN)) {
                            updateFileListFileContent(value, mlpDir);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // make sure the par file contains "odir=mlpOutputDir" element.
                } else if (!isOdirdefined) {
                    if (key.equals("odir")) {
                        value = mlpOutputDir;
                    } else {
                        stringBuilder.append("odir=" + mlpOutputDir);
                        stringBuilder.append("\n");
                    }
                    isOdirdefined = true;
                }
                token = key + "=" + value;
            }
            stringBuilder.append(token);
            stringBuilder.append("\n");
        }

        //Create the mlp output dir; Otherwise mlp will throw exception
        try {
            Files.createDirectories(new File(mlpOutputDir).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String newParString = stringBuilder.toString();
        return newParString;
    }

    public String readFile(String path, Charset encoding) {
        String fileString = new String();
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            fileString = new String(encoded, encoding);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return fileString;
    }

    public void updateFileListFileContent(String fileListFileName, String mlpDir) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileListFileName), StandardCharsets.UTF_8);

            Iterator<String> itr = lines.iterator();
            String fileName;
            while (itr.hasNext()) {
                fileName = itr.next();
                if (fileName.trim().length() > 0) {
                    System.out.println("file name in the file list: " + fileName);
                    fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
                    stringBuilder.append(mlpDir + File.separator + fileName + "\n");
                }
            }
            String fileContent = stringBuilder.toString();
            System.out.println(fileContent);
            ServerSideFileUtilities.writeStringToFile(fileContent, fileListFileName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void executeProcess(String[] commandArray, String jobId) {

        SwingWorker swingWorker = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                StringBuilder sb = new StringBuilder();
                for (String item : commandArray) {
                    sb.append(item + " ");
                }

                ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
                Process process = null;
                try {
                    process = processBuilder.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (process == null) {
                    throw new IOException(programName + " failed to create process.");
                }
                final ORSProcessObserver processObserver = new ORSProcessObserver(process, programName, jobId);

                processObserver.startAndWait();

                return null;
            }

            @Override
            protected void done() {

            }
        };
        swingWorker.execute();
    }


    public Process executeSimple(String[] commandArray) {

        StringBuilder sb = new StringBuilder();
        for (String item : commandArray) {
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

    public void execute(String[] commandArrayParam, String workingDir, String jobIdParam) {
        String jobID = jobIdParam;
        String[] commandArray = commandArrayParam;
        SwingWorker swingWorker = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {

                StringBuilder sb = new StringBuilder();
                for (String item : commandArray) {
                    sb.append(item + " ");
                }

                 System.out.println("command array: " + sb.toString());
                ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
                Map<String, String> env = processBuilder.environment();

                String originalPWD = env.get("PWD");
                env.put("PWD", workingDir);

                processBuilder.directory(new File(workingDir));

                processBuilder.redirectErrorStream(true);
                //File log = new File("log");
                //processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
                Process process = null;
                try {
                    process = processBuilder.start();
                    SQLiteJDBC.updateItem(SQLiteJDBC.FILE_TABLE_NAME, jobID, SQLiteJDBC.FileTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.STARTED.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final ORSProcessObserver processObserver = new ORSProcessObserver(process, programName, jobIdParam);
                processObserver.startAndWait();
                System.out.println("process started successfully");
                return process;
            }

            @Override
            protected void done() {
                SQLiteJDBC.updateItem(SQLiteJDBC.FILE_TABLE_NAME, jobID, SQLiteJDBC.FileTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.COMPLETED.getValue());
            }
        };
        swingWorker.execute();
    }

    /**
     * For a given input file, this method finds the file type and mission name before it applies the output file naming convention to compute an output file name.
     * For the generic "extractor" program, it will identify the actual extractor program to be used.
     * The intermediate findings, fileType, missionName, and programName, are saved in the database for future use.
     *
     * @param ifileName
     * @param jobId
     * @return ofilename
     */

    public String getOfileName(String ifileName, String jobId) {
        programName = SQLiteJDBC.getProgramName(jobId);
        System.out.println("finding ofile name for  " + programName + " with input file " + ifileName);
        extractFileInfo(ifileName, jobId);
        if (programName.equals("extractor")) {
            selectExtractorProgram(jobId);

        }

        if (ifileName == null || programName == null) {
            return null;
        }
        if (programName.equals("l3bindump")) {
            return ifileName + ".xml";
        } else if (programName.equals("extractor")) {

        }

        String[] commandArrayParams = {NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME, ifileName, programName};

        return getOfileName(concatAll(getCommandArrayPrefix(NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME), commandArrayParams));


    }

    public String getOfileName(String jobId, JsonObject jsonObject) {
        String ifileName = jsonObject.getString("ifileName");
        String programName = jsonObject.getString("programName");
        String additionalOptionsString = jsonObject.getString("additionalOptionsString");

        StringTokenizer st = new StringTokenizer(additionalOptionsString, ";");
        int i = 0;
        String[] additionalOptions = new String[st.countTokens()];

        while (st.hasMoreTokens()) {
            additionalOptions[i++] = (String) st.nextToken();
        }

        System.out.println("finding ofile name for  " + programName + " with input file " + ifileName);
        extractFileInfo(ifileName, jobId);
        if (programName.equals("extractor")) {
            selectExtractorProgram(jobId);

        }

        if (ifileName == null || programName == null) {
            return null;
        }
        if (programName.equals("l3bindump")) {
            return ifileName + ".xml";
        } else if (programName.equals("extractor")) {

        }

        String[] commandArrayParams = {NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME, ifileName, programName};
        String ofileName = getOfileName(concatAll(getCommandArrayPrefix(NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME), commandArrayParams, additionalOptions));

        System.out.println("ofile name = " + ofileName);
        String uploadedFileDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        SQLiteJDBC.updateItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.OFILE_NAME_FIELD_NAME, uploadedFileDir + File.separator + ofileName);

        return ofileName;


    }


    protected void extractFileInfo(String ifileName, String jobId) {

        String[] fileTypeCommandArrayParams = {GET_OBPG_FILE_TYPE_PROGRAM_NAME, ifileName};

        Process process = executeSimple((String[]) concatAll(getCommandArrayPrefix(GET_OBPG_FILE_TYPE_PROGRAM_NAME), fileTypeCommandArrayParams));

        try {

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = stdInput.readLine();
            System.out.println("line : " + line);
            if (line != null) {
                String splitLine[] = line.split(":");
                if (splitLine.length == 3) {
                    String missionName = splitLine[1].toString().trim();
                    String fileType = splitLine[2].toString().trim();
                    System.out.println("mission name : " + missionName);
                    System.out.println("file type : " + fileType);
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

        Process process = executeSimple(commandArray);

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
            } else if (missionName.indexOf("SeaWiFS") != -1 && fileType.indexOf("1A") != -1 || missionName.indexOf("CZCS") != -1) {
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
