package gov.nasa.gsfc.seadas.ocsswrest.utilities;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/16/13
 * Time: 10:56 AM
 * To change this template use File | Settings | File Templates.
 */

import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static gov.nasa.gsfc.seadas.ocsswrest.OCSSWRestServer.SERVER_WORKING_DIRECTORY_PROPERTY;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 8/7/12
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerSideFileUtilities {

    private static boolean debug = true;
    public static final String FILE_UPLOAD_PATH = System.getProperty("user.dir") + System.getProperty("file.separator") + "ifiles";
    public static final int BUFFER_SIZE = 1024;


    public static void saveToDisc(final InputStream fileInputStream,
                                  final String fileUploadPath) throws IOException

    {
        final OutputStream out = new FileOutputStream(new File(fileUploadPath));
        int read = 0;
        byte[] bytes = new byte[BUFFER_SIZE];
        while ((read = fileInputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }

    public static void saveStringToDisc(final String params,
                                        final String filePath) throws IOException

    {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(new File(filePath));
            fileWriter.write(params);
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    // save uploaded file to new location
    public static void writeToFile(InputStream inputStream,
                                   String fileLocation) {

        try {
            File outputFile = new File(fileLocation);
            OutputStream outputStream = new FileOutputStream(outputFile);
            int read = 0;
            byte[] bytes = new byte[8192];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // save uploaded file to new location
    public static void writeToFile(File inputFile,
                                   String uploadedFileLocation) {

        try {
            File outputFile = new File(uploadedFileLocation);
            OutputStream outputStream = new FileOutputStream(outputFile);
            InputStream uploadedInputStream = new FileInputStream(inputFile);
            int read = 0;
            byte[] bytes = new byte[8192];

            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            uploadedInputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {

            e.printStackTrace();
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


    public static void debug(String message) {
        if (debug) {
            System.out.println("Debugging: " + message);
        }
    }

    public static ArrayList<String> getFilesList(String dirPath) {
        ArrayList<String> results = new ArrayList<String>();


        File[] files = new File(dirPath).listFiles();
        //If this pathname does not denote a directory, then listFiles() returns null.

        for (File file : files) {
            if (file.isFile()) {
                results.add(file.getName());
            }
        }
        return results;
    }

    public static String getLogFileName(String dirPath) {

        File[] files = new File(dirPath).listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().indexOf("Processor") != -1 && file.getName().endsWith(".log")) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public static String manageDirectory(String filePath, boolean keepFiles) {
        String message = "Client directory is left intact. All files are reusable.";
        try {
            File workingDir = new File(filePath);
            if (workingDir.exists() && workingDir.isDirectory()) {
                if (!keepFiles) {
                    ServerSideFileUtilities.purgeDirectory(workingDir);
                    message = "Client directory is emptied";
                }
            } else {
                Files.createDirectories(new File(filePath).toPath());
                message = "Client directory is created";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    public static void createDirectory(String dirPath) {
        File newDir = new File(dirPath);
        if (! (newDir.exists() && newDir.isDirectory() )) {
            try {
                Files.createDirectories(newDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void purgeDirectory(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) purgeDirectory(file);
            file.delete();
        }
    }

    public static String getlastLine(String fileName) {
        BufferedReader input = null;
        String last = null, line;
        try {
            input = new BufferedReader(new FileReader(fileName));
            while ((line = input.readLine()) != null) {
                last = line;
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return last;
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

