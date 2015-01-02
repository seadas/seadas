package gov.nasa.gsfc.seadas.ocsswrest.utilities;


/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 8/7/12
 * Time: 1:10 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
    static final int BUFFER = 2048;
    private static final String FILE_DOWNLOAD_PATH = System.getProperty("user.dir") + System.getProperty("file.separator") + "ofiles";

    public static void main(String argv[]) {
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(FILE_DOWNLOAD_PATH + File.separator + "ofiles.zip");
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];

            File f = new File(".");
            String files[] = f.list();

            for (int i = 0; i < files.length; i++) {
                System.out.println("Adding: " + files[i]);
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(files[i]);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0,
                        BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}