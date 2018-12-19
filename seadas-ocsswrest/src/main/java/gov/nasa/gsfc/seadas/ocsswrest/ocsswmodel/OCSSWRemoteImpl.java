package gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;
import gov.nasa.gsfc.seadas.ocsswrest.process.ORSProcessObserver;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.MissionInfo;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.MissionInfoFinder;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.ServerSideFileUtilities;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.swing.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWServerModel.*;
import static gov.nasa.gsfc.seadas.ocsswrest.utilities.ServerSideFileUtilities.debug;

/**
 * Created by aabduraz on 5/15/17.
 */
public class OCSSWRemoteImpl {

    final static String OCSSW_ROOT_PROPERTY = "ocsswroot";
    final static String SERVER_WORKING_DIRECTORY_PROPERTY = "serverWorkingDirectory";

    private static String NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME = "next_level_name.py";
    private static String NEXT_LEVEL_FILE_NAME_TOKEN = "Output Name:";
    public static final String GET_OBPG_FILE_TYPE_PROGRAM_NAME = "get_obpg_file_type.py";
    public static String OCSSW_INSTALLER_PROGRAM = "install_ocssw.py";
    public static String MLP_PROGRAM_NAME = "multilevel_processor.py";
    public static String MLP_PAR_FILE_NAME = "multilevel_processor_parFile.par";
    public static String MLP_OUTPUT_DIR_NAME = "mlpOutputDir";
    public static String ANC_FILE_LIST_FILE_NAME = "anc_file_list.txt";

    public static final String FILE_TYPE_VAR_NAME = "fileType";
    public static final String MISSION_NAME_VAR_NAME = "missionName";

    public static final String US_ASCII_CHAR_SET = "us-ascii";

    public static String PROCESS_STDOUT_FILE_NAME_EXTENSION = ".log";

    public static String TMP_OCSSW_INSTALLER_PROGRAM_PATH = (new File(System.getProperty("java.io.tmpdir"), "install_ocssw.py")).getPath();

    private static final String DEFAULTS_FILE_PREFIX = "msl12_defaults_",
            AQUARIUS_DEFAULTS_FILE_PREFIX = "l2gen_aquarius_defaults_",
            L3GEN_DEFAULTS_FILE_PREFIX = "msl12_defaults_";

    private String defaultsFilePrefix;

    private final static String L2GEN_PROGRAM_NAME = "l2gen",
            AQUARIUS_PROGRAM_NAME = "l2gen_aquarius",
            L3GEN_PROGRAM_NAME = "l3gen";


    String programName;
    String missionName;
    String fileType;

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
            debug("commandArrayPrefix: " + item);
        }
        return commandArrayPrefix;
    }

    public String[] getCommandArraySuffix(String programName) {
        String[] commandArraySuffix = null;
        if (programName.equals(OCSSW_INSTALLER_PROGRAM)) {
            commandArraySuffix = new String[1];
            String[] parts = OCSSWServerModel.getSeadasVersion().split("\\.");
            commandArraySuffix[0] = "--git-branch=v" + parts[0] + "." + parts[1];
        }
        if (commandArraySuffix != null) {
            for (String item : commandArraySuffix) {
                debug("commandArraySuffix " + item);
            }
        }
        return commandArraySuffix;
    }

    public void executeProgram(String jobId, JsonObject jsonObject) {
        programName = SQLiteJDBC.getProgramName(jobId);

        String[] commandArray = transformCommandArray(jobId, jsonObject, programName);

        executeProcess(ServerSideFileUtilities.concatAll(getCommandArrayPrefix(programName), commandArray, getCommandArraySuffix(programName)), jobId);
    }

    public void executeProgramOnDemand(String jobId, String programName, JsonObject jsonObject) {

        String[] commandArray = transformCommandArray(jobId, jsonObject, programName);

        executeProcess(ServerSideFileUtilities.concatAll(getCommandArrayPrefix(programName), commandArray, getCommandArraySuffix(programName)), jobId);
    }

    private String[] transformCommandArray(String jobId, JsonObject jsonObject, String programName) {

        String serverWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        Set commandArrayKeys = jsonObject.keySet();

        debug(" programName = " + programName);

        Object[] array = (Object[]) commandArrayKeys.toArray();
        int i = 0;

        String[] commandArray;

        if (!programName.equals(OCSSW_INSTALLER_PROGRAM)) {
            commandArray = new String[commandArrayKeys.size() + 1];
            commandArray[i++] = programName;
        } else {
            commandArray = new String[commandArrayKeys.size()];
        }
        String commandArrayElement;
        for (Object element : array) {
            debug(" element = " + element);
            String elementName = (String) element;
            commandArrayElement = jsonObject.getString((String) element);
            if ((elementName.contains("IFILE") && !isAncFile(commandArrayElement)) || elementName.contains("OFILE")) {

                if (!(commandArrayElement.contains(System.getProperty(SERVER_WORKING_DIRECTORY_PROPERTY)) || commandArrayElement.contains(System.getProperty(OCSSW_ROOT_PROPERTY)))) {
                    if (commandArrayElement.indexOf("=") != -1) {
                        StringTokenizer st = new StringTokenizer(commandArrayElement, "=");
                        String paramName = st.nextToken();
                        String paramValue = st.nextToken();
                        commandArrayElement = paramName + "=" + serverWorkingDir + File.separator + paramValue.substring(paramValue.lastIndexOf(File.separator) + 1);

                    } else {
                        commandArrayElement = serverWorkingDir + File.separator + commandArrayElement.substring(commandArrayElement.lastIndexOf(File.separator) + 1);
                    }
                }
            }
            debug("command array element = " + commandArrayElement);
            commandArray[i++] = commandArrayElement;
        }
        return commandArray;
    }

    private boolean isAncFile(String fileName) {
        boolean isAncFile = fileName.contains("/var/anc/");
        return isAncFile;
    }

    public InputStream executeProgramAndGetStdout(String jobId, String programName, JsonObject jsonObject) {

        String[] commandArray = ServerSideFileUtilities.concatAll(getCommandArrayPrefix(programName), transformCommandArray(jobId, jsonObject, programName));

        debug("command array content to get stdout: ");
        for (int j = 0; j < commandArray.length; j++) {
            System.out.print(commandArray[j] + " ");
        }

        Process process = null;
        InputStream processInputStream = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
            process = processBuilder.start();
            process.waitFor();
            processInputStream = process.getInputStream();
        } catch (IOException ioe) {
            ioe.printStackTrace();

        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (process == null) {
            debug(programName + " failed to create process.");
        }
        return processInputStream;
    }

    public void executeProgramSimple(String jobId, String programName, JsonObject jsonObject) {

        String[] commandArray = transformCommandArray(jobId, jsonObject, programName);
        executeProcessSimple(ServerSideFileUtilities.concatAll(getCommandArrayPrefix(programName), commandArray, getCommandArraySuffix(programName)), jobId, programName);
    }

    public void executeMLP(String jobId, File parFile) {
        try {
            debug("par file path: " + parFile.getAbsolutePath());
            String workingFileDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
            String parFileNewLocation = workingFileDir + File.separator + MLP_PAR_FILE_NAME;
            debug("par file new path: " + parFileNewLocation);
            String parFileContent = convertClientMLPParFilForRemoteServer(parFile, jobId);
            ServerSideFileUtilities.writeStringToFile(parFileContent, parFileNewLocation);
            String[] commandArray = {MLP_PROGRAM_NAME, parFileNewLocation};
            execute(ServerSideFileUtilities.concatAll(getCommandArrayPrefix(MLP_PROGRAM_NAME), commandArray), new File(parFileNewLocation).getParent(), jobId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JsonObject getMLPOutputFilesList(String jobId) {
        String workingFileDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String mlpDir = workingFileDir;
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
        String workingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
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
        String mlpOutputDir = workingFileDir + File.separator + MLP_OUTPUT_DIR_NAME;
        Collection<String> filesInMLPDir = ServerSideFileUtilities.getFilesList(mlpOutputDir);
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        Iterator itr = filesInMLPDir.iterator();
        int i = 0;
        while (itr.hasNext()) {
            jsonObjectBuilder.add("OFILE" + i++, (String) itr.next());
        }
        return jsonObjectBuilder.build();
    }

    private String convertClientMLPParFilForRemoteServer(File parFile, String jobId) {
        String parString = readFile(parFile.getAbsolutePath(), StandardCharsets.UTF_8);
        StringTokenizer st1 = new StringTokenizer(parString, "\n");
        StringTokenizer st2;
        StringBuilder stringBuilder = new StringBuilder();
        String token;
        String key, value;
        boolean isOdirdefined = true;

        int odirStringPositioninParFile = 6;

        String workingFileDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String mlpDir = workingFileDir;
        String mlpOutputDir = workingFileDir + File.separator + MLP_OUTPUT_DIR_NAME;

        //delete files from the MLP_OUTPUT_DIR_NAME before executing new mlp command.
        ServerSideFileUtilities.purgeDirectory(new File(mlpOutputDir));

        while (st1.hasMoreTokens()) {
            token = st1.nextToken();
            if (token.contains("=")) {
                st2 = new StringTokenizer(token, "=");
                key = st2.nextToken();
                value = st2.nextToken();
                if (new File(mlpDir + File.separator + value).exists()) {
                    value = mlpDir + File.separator + value;
                    debug("mlp file: " + value);
                    isOdirdefined = false;
                    try {
                        if (isTextFile(value)) {
                            debug("File is a text file. Need to upload the content.");
                            updateFileListFileContent(value, mlpDir);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // make sure the par file contains "odir=mlpOutputDir" element.
                } else if (!isOdirdefined) {
                    if (key.equals("odir")) {
                        value = mlpOutputDir;
                    } else {
                        stringBuilder.insert(odirStringPositioninParFile, "odir=" + mlpOutputDir + "\n");
                    }
                    isOdirdefined = true;
                }
                token = key + "=" + value;
            }
            stringBuilder.append(token);
            stringBuilder.append("\n");
            if (token.indexOf("ifile") != -1) {
                odirStringPositioninParFile = stringBuilder.indexOf(token) + token.length() + 1;
            }
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
                    debug("file name in the file list: " + fileName);
                    fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
                    stringBuilder.append(mlpDir + File.separator + fileName + "\n");
                }
            }
            String fileContent = stringBuilder.toString();
            debug(fileContent);
            ServerSideFileUtilities.writeStringToFile(fileContent, fileListFileName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void executeProcess(String[] commandArray, String jobId) {

        debug("command array content: ");
        for (int j = 0; j < commandArray.length; j++) {
            System.out.print(commandArray[j] + " ");
        }
        debug("\n" + "command array content ended ");

        SwingWorker swingWorker = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
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
                if (process.isAlive()) {
                    debug("process is alive: ");
                    final ORSProcessObserver processObserver = new ORSProcessObserver(process, programName, jobId);
                    processObserver.startAndWait();
                } else {
                    if (process.exitValue() == 0) {
                        SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.COMPLETED.getValue());
                    } else {
                        SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.FAILED.getValue());
                    }
                }
                return null;
            }
        };
        swingWorker.execute();
    }

    public void executeProcessSimple(String[] commandArray, String jobId, String programName) {


        StringBuilder sb = new StringBuilder();
        for (String item : commandArray) {
            sb.append(item + " ");
        }

        debug("command array content: " + sb.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
        Process process = null;
        try {
            process = processBuilder.start();
            SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.STARTED.getValue());
            if (process == null) {
                throw new IOException(programName + " failed to create process.");
            }
            process.waitFor();
            if (process.exitValue() == 0) {
                SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.COMPLETED.getValue());
            } else {
                SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.FAILED.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        debug("process exited! exit value = " + process.exitValue());
        debug(process.getInputStream().toString());
    }

    public Process executeSimple(String[] commandArray) {

        StringBuilder sb = new StringBuilder();
        for (String item : commandArray) {
            sb.append(item + " ");
        }
        debug("command array content: " + sb.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
        Process process = null;
        try {
            process = processBuilder.start();
            if (process != null) {
                debug("Running the program " + sb.toString());
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

                debug("command array: " + sb.toString());
                ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
                Map<String, String> env = processBuilder.environment();

                env.put("PWD", workingDir);

                processBuilder.directory(new File(workingDir));
                Process process = null;
                try {
                    process = processBuilder.start();
                    SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobID, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.STARTED.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final ORSProcessObserver processObserver = new ORSProcessObserver(process, programName, jobIdParam);
                processObserver.startAndWait();
                return process;
            }
        };
        swingWorker.execute();
    }

    public HashMap<String, String> computePixelsFromLonLat(String jobId, String programName, JsonObject jsonObject) {

        HashMap<String, String> pixels = new HashMap();
        try {
            String[] commandArray = transformCommandArray(jobId, jsonObject, programName);
            Process process = executeSimple(ServerSideFileUtilities.concatAll(getCommandArrayPrefix(programName), commandArray));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            String[] tmp;
            while ((line = stdInput.readLine()) != null) {
                if (line.indexOf("=") != -1) {
                    tmp = line.split("=");
                    pixels.put(tmp[0], tmp[1]);
                    SQLiteJDBC.updateItem(SQLiteJDBC.LONLAT_TABLE_NAME, jobId, tmp[0], tmp[1]);
                    debug("pixels are not null: " + tmp[0] + "=" + tmp[1]);
                }
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pixels;
    }

    public String getDefaultsFilePrefix(String programName) {

        defaultsFilePrefix = DEFAULTS_FILE_PREFIX;

        if (programName.equals(L3GEN_PROGRAM_NAME)) {
            defaultsFilePrefix = L3GEN_DEFAULTS_FILE_PREFIX;
        } else if (programName.equals(AQUARIUS_PROGRAM_NAME)) {
            defaultsFilePrefix = AQUARIUS_DEFAULTS_FILE_PREFIX;
        }
        return defaultsFilePrefix;
    }

    private void addSuites(ArrayList<String> suites, File dir, String prefix) {
        if (dir != null && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                String filename = file.getName();

                if (filename.startsWith(prefix) && filename.endsWith(".par")) {
                    String suiteName = filename.substring(prefix.length(), filename.length() - 4);
                    if (!suites.contains(suiteName)) {
                        suites.add(suiteName);
                        debug("mission suite name: " + suiteName);
                    }
                }
            }

        }
    }

    public String[] getMissionSuites(String missionName, String programName) {
        ArrayList<String> suitesArrayList = new ArrayList<String>();
        MissionInfo missionInfo = new MissionInfo(missionName);
        String prefix = getDefaultsFilePrefix(programName);

        // first look in the common directory
        File dir = new File(OCSSWServerModel.getOcsswDataDirPath(), "common");
        debug("mission suites dir: " + dir.getAbsolutePath());
        addSuites(suitesArrayList, dir, prefix);

        // look in sensor dir
        addSuites(suitesArrayList, missionInfo.getDirectory(), prefix);

        // look in subsensor dir
        addSuites(suitesArrayList, missionInfo.getSubsensorDirectory(), prefix);

        if (suitesArrayList.size() > 0) {

            final String[] suitesArray = new String[suitesArrayList.size()];

            int i = 0;
            for (String suite : suitesArrayList) {
                suitesArray[i] = suite;
                i++;
            }

            java.util.Arrays.sort(suitesArray);

            return suitesArray;

        } else {
            return null;
        }
    }


    public String getOfileName(String jobId, String ifileName, String programName) {

        if (ifileName == null || programName == null) {
            return null;
        }
        if (programName.equals("l3bindump")) {
            return ifileName + ".xml";
        }

        String[] commandArrayParams = {NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME, ifileName, programName};

        return getOfileName(ServerSideFileUtilities.concatAll(getCommandArrayPrefix(NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME), commandArrayParams));

    }

    public String getOfileName(String jobId, JsonObject jsonObject) {
        debug("jobId = " + jobId);
        try {
            String ifileName = jsonObject.getString("ifileName");
            String programName = jsonObject.getString("programName");
            String additionalOptionsString = jsonObject.getString("additionalOptions");
            String serverWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());

            String ifileNameWithFullPath = serverWorkingDir + File.separator + ifileName;

            debug("finding ofile name for  " + programName + " with input file " + ifileNameWithFullPath);

            if (ifileName == null || programName == null) {
                return null;
            }
            if (programName.equals("l3bindump")) {
                return ifileName + ".xml";
            }

            StringTokenizer st = new StringTokenizer(additionalOptionsString, ";");
            int i = 0;
            String[] additionalOptions = new String[st.countTokens()];

            while (st.hasMoreTokens()) {
                additionalOptions[i++] = (String) st.nextToken();
            }

            String[] commandArrayParams = {NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME, ifileNameWithFullPath, programName};
            String ofileName = getOfileName(ServerSideFileUtilities.concatAll(getCommandArrayPrefix(NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME), commandArrayParams, additionalOptions));

            SQLiteJDBC.updateItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.OFILE_NAME_FIELD_NAME, ofileName);

            return ofileName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    protected void extractFileInfo(String ifileName, String jobId) {

        String[] fileTypeCommandArrayParams = {GET_OBPG_FILE_TYPE_PROGRAM_NAME, ifileName};

        Process process = executeSimple((String[]) ServerSideFileUtilities.concatAll(getCommandArrayPrefix(GET_OBPG_FILE_TYPE_PROGRAM_NAME), fileTypeCommandArrayParams));

        try {

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = stdInput.readLine();
            debug("line : " + line);
            if (line != null) {
                String splitLine[] = line.split(":");
                if (splitLine.length == 3) {
                    String missionName = splitLine[1].toString().trim();
                    String fileType = splitLine[2].toString().trim();
                    debug("mission name : " + missionName);
                    debug("file type : " + fileType);
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


    public JsonObject getSensorFileIntoArrayList(String missionName) {

        MissionInfo missionInfo = new MissionInfo(missionName);
        debug("mission name: " + missionName);
        //missionInfo.setName(missionName);
        File file = getSensorInfoFilename(missionInfo);
        debug("mission sensor file path : " + file.getAbsolutePath());
        String lineData;
        BufferedReader moFile = null;
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        int i = 0;
        try {
            moFile = new BufferedReader(new FileReader(file));
            while ((lineData = moFile.readLine()) != null) {
                jsonObjectBuilder.add("sensorInfo" + i++, lineData);
            }
        } catch (IOException e) {
            ;
        } finally {
            try {
                moFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jsonObjectBuilder.build();
    }

    private File getSensorInfoFilename(MissionInfo missionInfo) {
        if (missionInfo != null) {
            // determine the filename which contains the wavelength
            File dir = missionInfo.getSubsensorDirectory();
            if (dir == null) {
                dir = missionInfo.getDirectory();
            }
            if (dir != null) {
                File filename = new File(dir.getAbsolutePath(), "msl12_sensor_info.dat");
                return filename;
            }
        }
        return null;
    }

    public HashMap<String, String> getFileInfo(String ifileName, String jobId) {

        HashMap<String, String> fileInfoMap = new HashMap<>();
        String[] fileTypeCommandArrayParams = {GET_OBPG_FILE_TYPE_PROGRAM_NAME, ifileName};
        Process process = executeSimple((String[]) ServerSideFileUtilities.concatAll(getCommandArrayPrefix(GET_OBPG_FILE_TYPE_PROGRAM_NAME), fileTypeCommandArrayParams));
        try {
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = stdInput.readLine();
            if (line != null) {
                String splitLine[] = line.split(":");
                if (splitLine.length == 3) {
                    String missionName = splitLine[1].toString().trim();
                    String fileType = splitLine[2].toString().trim();
                    if (fileType.length() > 0) {
                        fileInfoMap.put(FILE_TYPE_VAR_NAME, fileType);
                    }
                    if (missionName.length() > 0) {
                        fileInfoMap.put(MISSION_NAME_VAR_NAME, missionName);
                    }
                }
            }
        } catch (IOException ioe) {

            ioe.printStackTrace();
        }
        return fileInfoMap;
    }

    public String getFileCharset(String fileName) {
        String charset = null;
        String charSetKeyword = "charset";
        String[] systemFileTypeCommandArraysParams = {"file", "-i", fileName};
        Process process = executeSimple(systemFileTypeCommandArraysParams);

        try {
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = stdInput.readLine();
            if (line != null && line.indexOf(charSetKeyword) != -1) {
                charset = line.substring(line.lastIndexOf("=") + 1).trim();
            }
        } catch (IOException ioe) {

            ioe.printStackTrace();
        }
        debug("system file type = " + charset);
        return charset;
    }


    public boolean isTextFile(String fileName) {

        String charSet = getFileCharset(fileName);
        debug("file type is: " + charSet);

        if (charSet.trim().equals(US_ASCII_CHAR_SET)) {
            return true;
        } else {
            return false;
        }
    }

    private String getOfileName(String[] commandArray) {

        Process process = executeSimple(commandArray);

        if (process == null) {
            return null;
        }
        int exitCode = 100;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        debug("Finding ofile name; process exit value =   '" + exitCode);
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
                debug("Failed exit code on program '" + NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME + "'");
            }

        } catch (IOException ioe) {

            ioe.printStackTrace();
        }

        return null;
    }
}
