package gov.nasa.gsfc.seadas.ocsswrest.utilities;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/16/13
 * Time: 10:56 AM
 * To change this template use File | Settings | File Templates.
 */

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public static final String FILE_DOWNLOAD_PATH = System.getProperty("user.dir") + System.getProperty("file.separator") + "ofiles";
    public static final String OCSSW_OUTPUT_COMPRESSED_FILE_NAME = "ocssw_output.zip";
    public static final int BUFFER_SIZE = 1024;
    public static final String PARAM_FILE = System.getProperty("user.dir") + System.getProperty("file.separator") + "params" + File.separator + "param.txt";


    public void writeToFile() {

    }

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
    public static void writeToFile(InputStream uploadedInputStream,
                                   String uploadedFileLocation) {

        try {
            File outputFile = new File(uploadedFileLocation);
            OutputStream outputStream = new FileOutputStream(outputFile);
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
                fileWriter.write(fileContent );
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

//    public static boolean makeNewJobDirectory(String clientID, String jobID) {
//        String newDirPath = OCSSWRestServer.JOBS_ROOT_DIR + System.getProperty("file.separator") + clientID + System.getProperty("file.separator") + jobID;
//        ProcessBuilder processBuilder = new ProcessBuilder(new String[]{"mkdir", newDirPath});
//        Process process = null;
//        try {
//            process = processBuilder.start();
//        } catch (IOException ioe) {
//            return false;
//        } finally {
//            if (process.exitValue() == 0) {
//                return true;
//            }
//            return false;
//        }
//    }

    public static String generateNewJobID() {
        return hashJobID(new Long(new Date().getTime()).toString());
    }

    private static String hashJobID(String jobID) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        md.update(jobID.getBytes());

        byte byteData[] = md.digest();
        //convert the byte to hex format
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
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
}

