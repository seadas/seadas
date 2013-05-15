package gov.nasa.gsfc.seadas.ocssw_ws.services;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 8/7/12
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerSideFileUtilities {

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

}
