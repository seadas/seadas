package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.processing.core.*;
import org.apache.commons.lang.ArrayUtils;
import org.esa.beam.util.Debug;
import org.esa.beam.visat.VisatApp;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/20/12
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class SeadasFileUtils {

    private static boolean debug = true;
    private static String NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME = "next_level_name.py";
    private static String NEXT_LEVEL_FILE_NAME_TOKEN = "Output Name:";

    public static File createFile(String parent, String fileName) {
        File pFile;
        if (parent == null) {
            pFile = null;
        } else {
            pFile = new File(parent);
        }
        return createFile(pFile, fileName);
    }

    public static File createFile(File parent, String fileName) {
        if (fileName == null) {
            return null;
        }

        String expandedFilename = SeadasFileUtils.expandEnvironment(fileName);
        File file = new File(expandedFilename);
        if (!file.isAbsolute() && parent != null) {
            file = new File(parent, expandedFilename);
        }
        return file;
    }

    public static String getCurrentDate(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());

    }

    public static String getKeyValueFromParFile(File file, String key) {
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(file));
            String line;
            line = reader.readLine();
            String[] option;
            ParamInfo pi;
            while (line != null) {
                if (line.indexOf("=") != -1) {
                    option = line.split("=", 2);
                    option[0] = option[0].trim();
                    option[1] = option[1].trim();
                    if (option[0].trim().equals(key)) {
                        return option[1];
                    }
                }
                line = reader.readLine();
            }
        } catch (FileNotFoundException fnfe) {

        } catch (IOException ioe) {

        }
        return null;
    }

    public static String getGeoFileNameFromIFile(String ifileName) {

        String geoFileName = (ifileName.substring(0, ifileName.indexOf("."))).concat(".GEO");
        int pos1 = ifileName.indexOf(".");
        int pos2 = ifileName.lastIndexOf(".");

        if (pos2 > pos1) {
            geoFileName = ifileName.substring(0, pos1 + 1) + "GEO" + ifileName.substring(pos2);
        }

        if (new File(geoFileName).exists()) {
            return geoFileName;
        } else {
            VisatApp.getApp().showErrorDialog(ifileName + " requires a GEO file to be extracted. " + geoFileName + " does not exist.");
            return null;
        }
    }


    public static String findNextLevelFileName(String ifileName, String programName, String suite) {
        if (ifileName == null || programName == null) {
            return null;
        }
        if (programName.equals("l3bindump")) {
            return ifileName + ".xml";
        }
        debug("Program name is " + programName);
        Debug.assertNotNull(ifileName);
// todo Add suite, also check calling program the make sure ProcessorModel call is right

        String[] cmdArray = new String[6];
        cmdArray[0] = OCSSW.getOcsswRunnerScriptPath();
        cmdArray[1] = "--ocsswroot";
        cmdArray[2] = OCSSW.getOcsswEnv();
        cmdArray[3] = NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME;
        cmdArray[4] = ifileName;
        cmdArray[5] = programName;

        String ifileDir = ifileName.substring(0, ifileName.lastIndexOf(System.getProperty("file.separator")));

        if (RuntimeContext.getConfig().getContextProperty(OCSSW.OCSSW_LOCATION_PROPERTY).equals(OCSSW.SEADAS_OCSSW_LOCATION_LOCAL)) {
            return retrieveOFileNameLocal(cmdArray, ifileDir);
        } else {
            cmdArray[4] = getIfileNameforRemoteServer(SeadasFileUtils.copyFile(ifileName, OCSSW.getOCSSWClientSharedDirName()));
            return retrieveOFileNameRemote(cmdArray);
        }
    }

    public ProcessorModel getNextLevelNameFinderProcessorModel(String ifileName, String programName) {
        ProcessorModel nextLevelNamer = new ProcessorModel(NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME);
        final ArrayList<ParamInfo> paramInfos = new ArrayList<ParamInfo>();

        ParamInfo ifileParamInfo = new ParamInfo("ifile", ifileName, ParamInfo.Type.IFILE);
        ifileParamInfo.setOrder(0);
        ifileParamInfo.setUsedAs(ParamInfo.USED_IN_COMMAND_AS_ARGUMENT);
        paramInfos.add(ifileParamInfo);

        ParamInfo programNameParamInfo = new ParamInfo("programName", programName, ParamInfo.Type.IFILE);
        programNameParamInfo.setOrder(1);
        programNameParamInfo.setUsedAs(ParamInfo.USED_IN_COMMAND_AS_ARGUMENT);
        paramInfos.add(programNameParamInfo);

        nextLevelNamer.setParamList(paramInfos);

        return nextLevelNamer;
    }

    private static String retrieveOFileNameLocal(String[] cmdArray, String ifileDir) {
        Process process = OCSSWRunner.execute(cmdArray, new File(ifileDir));

        if (process == null) {
            return "output";
        }

        //wait for process to exit
        try {
            Field field = process.getClass().getDeclaredField("hasExited");
            field.setAccessible(true);
            while (!(Boolean) field.get(process)) {
            }
        }  catch (IllegalAccessException e) {
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
                debug("Failed exit code on program '" + NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME + "'");
            }

        } catch (IOException ioe) {

            VisatApp.getApp().showErrorDialog(ioe.getMessage());
        }

//        int choice = VisatApp.getApp().showQuestionDialog("ofile computation", "ofile name is not found", true, "continue");
//
//        return String.valueOf(choice);
        return "output";
    }

    private static String retrieveOFileNameRemote(String[] cmdArray) {
        JsonArrayBuilder jab = Json.createArrayBuilder();
        for (String s : cmdArray) {
            jab.add(s);
        }

        //add jobId for server side database
        //jab.add(OCSSW.getJobId());
        JsonArray remoteCmdArray = jab.build();

        OCSSWClient ocsswClient = new OCSSWClient();
        WebTarget target = ocsswClient.getOcsswWebTarget();
        final Response response = target.path("ocssw").path("computeNextLevelFileName").path(OCSSW.getJobId()).request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(remoteCmdArray, MediaType.APPLICATION_JSON_TYPE));

        String ofileName = target.path("ocssw").path("retrieveNextLevelFileName").path(OCSSW.getJobId()).request(MediaType.TEXT_PLAIN).get(String.class);
        if (ofileName != null) {
            return ofileName;
        } else {
            return "output";
        }
    }

    private static String[] getCmdArrayForNextLevelNameFinder(String ifileName, String programName) {
        String[] cmdArray = new String[6];
        cmdArray[0] = OCSSW.getOcsswRunnerScriptPath();
        cmdArray[1] = "--ocsswroot";
        cmdArray[2] = OCSSW.getOcsswEnv();
        cmdArray[3] = NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME;
        cmdArray[4] = RuntimeContext.getConfig().getContextProperty(OCSSW.OCSSW_LOCATION_PROPERTY).equals(OCSSW.SEADAS_OCSSW_LOCATION_LOCAL) ? ifileName : getIfileNameforRemoteServer(ifileName);

        cmdArray[5] = programName;
        return cmdArray;
    }

    private String getIfileNameforCmdArray(String ifileName){
        if (RuntimeContext.getConfig().getContextProperty(OCSSW.OCSSW_LOCATION_PROPERTY).equals(OCSSW.SEADAS_OCSSW_LOCATION_LOCAL)){
            return ifileName;
        } else {
            return ifileName;
        }
    }

    private static String getNextLevelFileName(String ifileName, String[] cmdArray) {

        String ifileDir = ifileName.substring(0, ifileName.lastIndexOf(System.getProperty("file.separator")));
        Process process = OCSSWRunner.execute(cmdArray, new File(ifileDir));
        if (process == null) {
            return null;
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
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(NEXT_LEVEL_FILE_NAME_TOKEN)) {
                        return (line.substring(NEXT_LEVEL_FILE_NAME_TOKEN.length())).trim();
                    }
                }

            } else {
                debug("Failed exit code on program '" + cmdArray[5] + "'");
            }

        } catch (IOException ioe) {

            VisatApp.getApp().showErrorDialog(ioe.getMessage());
        }
        return null;
    }

    public static String findNextLevelFileName(String ifileName, String programName, String[] additionalOptions) {

        if (ifileName == null || programName == null) {
            return null;
        }
        if (programName.equals("l3bindump")) {
            return ifileName + ".xml";
        }
        debug("Program name is " + programName);
        Debug.assertNotNull(ifileName);

        String[] cmdArray = (String[]) ArrayUtils.addAll(getCmdArrayForNextLevelNameFinder(ifileName, programName), additionalOptions);

        String ifileDir = ifileName.substring(0, ifileName.lastIndexOf(System.getProperty("file.separator")));

        String ofileName;

        if (RuntimeContext.getConfig().getContextProperty(OCSSW.OCSSW_LOCATION_PROPERTY).equals(OCSSW.SEADAS_OCSSW_LOCATION_LOCAL)) {
            ofileName = retrieveOFileNameLocal(cmdArray, ifileDir);
        } else {

            ofileName = retrieveOFileNameRemote(cmdArray);
        }

        if (ofileName == null) {
            ofileName = "output";
        }
        return ofileName;
    }

    private static String getIfileNameforRemoteServer(String ifileName) {
        String newIfileName = ifileName.replace(OCSSW.getOCSSWClientSharedDirName(), OCSSW.getServerSharedDirName());
        return newIfileName;
    }

    public static String getDefaultOFileNameFromIFile(String ifileName, String programName) {
        debug("Program name is " + programName);
        Debug.assertNotNull(ifileName);
        ProcessorTypeInfo.ProcessorID processorID = ProcessorTypeInfo.getProcessorID(programName);
        String ofileName = ifileName + "_" + programName + ".out";
        switch (processorID) {
            case EXTRACTOR:
                ofileName = ifileName + ".sub";
                break;
            case MODIS_L1A_PY:
                //FileUtils.exchangeExtension(ifileName, "GEO") ;
                break;
            case MODIS_GEO_PY:
                ofileName = ifileName.replaceAll("L1A_LAC", "GEO");
                break;
            case L1BGEN:
                ofileName = ifileName.replaceAll("L1A", "L1B");
                break;
            case MODIS_L1B_PY:
                ofileName = ifileName.replaceAll("L1A", "L1B");
                break;
            case L1BRSGEN:
                ofileName = ifileName + ".BRS";
                break;
            case L2BRSGEN:
                ofileName = ifileName + ".BRS";
                break;
            case L1MAPGEN:
                ofileName = ifileName + "_" + programName + ".out";
                break;
            case L2MAPGEN:
                ofileName = ifileName + "_" + programName + ".out";
                break;
            case L2BIN:
                ofileName = ifileName.replaceAll("L2_.{3,}", "L3b_DAY");
                break;
            case L3BIN:
                ofileName = ifileName.replaceAll("L2_.{3,}", "L3b_DAY");
                break;
            case SMIGEN:
                ofileName = ifileName.replaceAll("L3B", "L3m");
                ofileName = ofileName.replaceAll("L3b", "L3m");
                ofileName = ofileName.replaceAll(".main", "");
                break;
            case SMITOPPM:
                ofileName = ifileName.trim().length() > 0 ? ifileName + ".ppm" : "";
                break;
        }
        return ofileName;
    }

    public static void main(String arg[]) {
        System.out.println(getCurrentDate("dd MMMMM yyyy"));
        System.out.println(getCurrentDate("yyyyMMdd"));
        System.out.println(getCurrentDate("dd.MM.yy"));
        System.out.println(getCurrentDate("MM/dd/yy"));
        System.out.println(getCurrentDate("yyyy.MM.dd G 'at' hh:mm:ss z"));
        System.out.println(getCurrentDate("EEE, MMM d, ''yy"));
        System.out.println(getCurrentDate("h:mm a"));
        System.out.println(getCurrentDate("H:mm:ss:SSS"));
        System.out.println(getCurrentDate("K:mm a,z"));
        System.out.println(getCurrentDate("yyyy.MMMMM.dd GGG hh:mm aaa"));
    }

    public static String expandEnvironment(String string1) {

        if (string1 == null) {
            return string1;
        }

        String environmentPattern = "([A-Za-z0-9_]+)";
        Pattern pattern1 = Pattern.compile("\\$\\{" + environmentPattern + "\\}");
        Pattern pattern2 = Pattern.compile("\\$" + environmentPattern);


        String string2 = null;

        while (!string1.equals(string2)) {
            if (string2 != null) {
                string1 = string2;
            }

            string2 = expandEnvironment(pattern1, string1);
            string2 = expandEnvironment(pattern2, string2);

            if (string2 == null) {
                return string2;
            }
        }

        return string2;
    }


    private static String expandEnvironment(Pattern pattern, String string) {


        if (string == null || pattern == null) {
            return string;
        }

        Matcher matcher = pattern.matcher(string);
        Map<String, String> envMap = null;

        while (matcher.find()) {

            // Retrieve environment variables
            if (envMap == null) {
                envMap = System.getenv();
            }

            String envNameOnly = matcher.group(1);

            if (envMap.containsKey(envNameOnly)) {
                String envValue = envMap.get(envNameOnly);

                if (envValue != null) {
                    String escapeStrings[] = {"\\", "$", "{", "}"};
                    for (String escapeString : escapeStrings) {
                        envValue = envValue.replace(escapeString, "\\" + escapeString);
                    }

                    Pattern envNameClausePattern = Pattern.compile(Pattern.quote(matcher.group(0)));
                    string = envNameClausePattern.matcher(string).replaceAll(envValue);
                }
            }
        }

        return string;
    }


    public static void debug(String message) {
        if (debug) {
            System.out.println("Debugging: " + message);
        }
    }

    public static void writeToDisk(String fileName, String log) throws IOException {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(new File(fileName));
            fileWriter.write(log);
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    /**
     * Copies sourceFile into targetDir.
     *
     * @param sourceFile the full path name of the source file to be copied
     * @param targetDir  the full path name of the target directory
     * @return the full path of the file in the new location, if the "copy" operation is successfull. Otherwise, it returns null.
     */
    public static String copyFile(String sourceFile, String targetDir) {
        String[] cmdArray = new String[3];
        cmdArray[0] = "cp";
        cmdArray[1] = sourceFile;
        cmdArray[2] = targetDir + System.getProperty("file.separator") + ".";
        Process p = OCSSWRunner.executeLocal(cmdArray, new File(targetDir));
        try{
            p.waitFor();
        }catch(InterruptedException ie){
            System.out.println("Process interrupted!");
        }
        if (p.exitValue() == 0) {
            return targetDir + sourceFile.substring(sourceFile.lastIndexOf(System.getProperty("file.separator")));
        } else {
            System.out.println("This should not happen!");
            return null;
        }
    }

    /**
     * Deletes a file from a directory
     *
     * @param fileFullPathName the full path name of the file to be deleted
     */
    public static void deleteFile(String fileFullPathName) {
        String[] cmdArray = new String[2];
        cmdArray[0] = "rm";
        cmdArray[1] = fileFullPathName;
        OCSSWRunner.executeLocal(cmdArray, new File(fileFullPathName));
    }

    public void updateDiskFile(String fileFullPath, String varName, String varValue) {
        File targetFile = new File(fileFullPath);
        StringBuilder targetFileContent = new StringBuilder();
        try {
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(targetFile));
            String readLine;
            String[] configVar;

            readLine = lineNumberReader.readLine();

            while (readLine != null) {
                if (readLine.indexOf("=") != -1) {
                    configVar = readLine.split("=", 2);
                    configVar[0] = configVar[0].trim();
                    configVar[1] = configVar[1].trim();
                    if (configVar[0].trim().equalsIgnoreCase(varName.trim())) {
                        targetFileContent.append(configVar[0] + " = " + varValue + System.getProperty("line.separator"));
                    } else {
                        targetFileContent.append(readLine + System.getProperty("line.separator"));
                    }
                }
                readLine = lineNumberReader.readLine();
            }
            writeToDisk(fileFullPath, targetFileContent.toString());

        } catch (FileNotFoundException fnfe) {

        } catch (IOException ioe) {

        }
    }

}
