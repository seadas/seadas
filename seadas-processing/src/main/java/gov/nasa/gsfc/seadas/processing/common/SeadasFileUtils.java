package gov.nasa.gsfc.seadas.processing.common;


import gov.nasa.gsfc.seadas.OsUtils;
import gov.nasa.gsfc.seadas.processing.core.*;
import gov.nasa.gsfc.seadas.processing.utilities.SeadasArrayUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.out;
import org.esa.snap.rcp.util.Dialogs;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/20/12
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class SeadasFileUtils {

    private static boolean debug = true;

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


    public static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
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

    public static void writeToFile(InputStream downloadedInputStream,
                                   String downloadedFileLocation) {

        try {
            File file = new File(downloadedFileLocation);
            OutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            byte[] bytes = new byte[8192];

            while ((read = downloadedInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            downloadedInputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public static Process cloFileCopy(String sourceFilePathName, String targetFilePathName) {

        String[] commandArrayParams = new String[2];
        commandArrayParams[0] = sourceFilePathName;
        commandArrayParams[1] = targetFilePathName;

        String[] copyCommandArray = SeadasArrayUtils.concatAll(OsUtils.getCopyCommandSyntax(), commandArrayParams);

        StringBuilder sb = new StringBuilder();
        for (String item : copyCommandArray) {
            sb.append(item + " ");
        }
        SeadasLogger.getLogger().info("command array content: " + sb.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(copyCommandArray);
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return process;
    }


    /**
     * Guess whether given file is binary. Just checks for anything under 0x09.
     */
    public static boolean isBinaryFile(File f) throws IOException {
        FileInputStream in = new FileInputStream(f);
        int size = in.available();
        if (size > 1024) size = 1024;
        byte[] data = new byte[size];
        in.read(data);
        in.close();

        int ascii = 0;
        int other = 0;

        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            if (b < 0x09) return true;

            if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D) ascii++;
            else if (b >= 0x20 && b <= 0x7E) ascii++;
            else other++;
        }

        if (other == 0) return false;

        return 100 * other / (ascii + other) > 95;
    }

    public static boolean isTextFileOld(String fileName) {
        try {
            return !isBinaryFile(new File(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isTextFile(String fileName){
        String fileType = identifyFileTypeUsingMimetypesFileTypeMap(fileName);
        if (fileType.startsWith("text")) {
           return true;
        }  else {
            //type isn't text or type couldn't be determined, assume binary
            return false;
        }
    }

    /**

     * Identify file type of file with provided path and name

     * using JDK 7's Files.probeContentType(Path).

     *

     * @param fileName Name of file whose type is desired.

     * @return String representing identified type of file with provided name.

     */

    public static String identifyFileTypeUsingFilesProbeContentType(final String fileName)

    {

        String fileType = "Undetermined";
        final File file = new File(fileName);
        try
        {
            fileType = Files.probeContentType(file.toPath());
        }
        catch (IOException ioException)
        {
            out.println(

                    "ERROR: Unable to determine file type for " + fileName

                            + " due to exception " + ioException);
        }
        return fileType;
    }

    /**

     * Identify file type of file with provided name using

     * JDK 6's MimetypesFileTypeMap.

     *

     * See Javadoc documentation for MimetypesFileTypeMap class

     * (http://docs.oracle.com/javase/8/docs/api/javax/activation/MimetypesFileTypeMap.html)

     * for details on how to configure mapping of file types or extensions.

     */

    public static String identifyFileTypeUsingMimetypesFileTypeMap(final String fileName)

    {

        final MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

        return fileTypeMap.getContentType(fileName);

    }

    /**

     * Identify file type of file with provided path and name

     * using JDK's URLConnection.getContentType().

     *

     * @param fileName Name of file whose type is desired.

     * @return Type of file for which name was provided.

     */

    public static String identifyFileTypeUsingUrlConnectionGetContentType(final String fileName)

    {

        String fileType = "Undetermined";

        try

        {

            final URL url = new URL("file://" + fileName);

            final URLConnection connection = url.openConnection();

            fileType = connection.getContentType();

        }

        catch (MalformedURLException badUrlEx)

        {

            out.println("ERROR: Bad URL - " + badUrlEx);

        }

        catch (IOException ioEx)

        {

            out.println("Cannot access URLConnection - " + ioEx);

        }

        return fileType;

    }


    /**

     * Identify file type of file with provided path and name

     * using JDK's URLConnection.guessContentTypeFromName(String).

     *

     * @param fileName Name of file whose type is desired.

     * @return Type of file for which name was provided.

     */

    public static String identifyFileTypeUsingUrlConnectionGuessContentTypeFromName(final String fileName)

    {

        return URLConnection.guessContentTypeFromName(fileName);

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
            Dialogs.showError(ifileName + " requires a GEO file to be extracted. " + geoFileName + " does not exist.");
            return null;
        }
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
            out.println("Debugging: " + message);
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

    public String tail(File file) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer == fileLength) {
                        continue;
                    }
                    break;

                } else if (readByte == 0xD) {
                    if (filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }

                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null)
                try {
                    fileHandler.close();
                } catch (IOException e) {
                /* ignore */
                }
        }
    }

    public String tail2(File file, int lines) {
        java.io.RandomAccessFile fileHandler = null;
        try {
            fileHandler =
                    new java.io.RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();
            int line = 0;

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer < fileLength) {
                        line = line + 1;
                    }
                } else if (readByte == 0xD) {
                    if (filePointer < fileLength - 1) {
                        line = line + 1;
                    }
                }
                if (line >= lines) {
                    break;
                }
                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null)
                try {
                    fileHandler.close();
                } catch (IOException e) {
                }
        }
    }

    public static File writeStringToFile(String fileContent, String fileLocation) {

        try {

            final File parFile = new File(fileLocation);
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(parFile);
                fileWriter.write(fileContent);
            } finally {
                if (fileWriter != null) {

                    fileWriter.close();
                }
            }
            return parFile;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
