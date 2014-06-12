package gov.nasa.gsfc.seadas.contour.util;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 9/5/13
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.util.ResourceInstaller;
import org.esa.beam.util.SystemUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 1/17/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceInstallationUtils {

    public static String CONTOUR_MODULE_NAME = "seadas-contour-operator";
    public static String AUXDIR = "auxdata";
    public static String CONTOUR_PATH = "gov/nasa/gsfc/seadas/contour/";
    public static String AUXPATH = CONTOUR_PATH + "operator/" + AUXDIR + "/";
    public static String ICON_PATH = CONTOUR_PATH + "ui/action/";

    public static String getIconFilename(String icon, Class sourceClass) {

        URL sourceUrl = ResourceInstaller.getSourceUrl(sourceClass);
        String iconFilename = sourceUrl.toString() + ICON_PATH + icon;

        return iconFilename;
    }


    public static void writeFileFromUrlOld(URL sourceUrl, File targetFile) throws IOException {

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(sourceUrl.openStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            boolean exist = targetFile.createNewFile();

            if (!exist) {
                // file already exists
            } else {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(targetFile));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    bufferedWriter.write(line);
                }

                bufferedWriter.close();
            }


            bufferedReader.close();

        } catch (Exception e) {
            throw new IOException("failed to write file from url: " + e.getMessage());
        }
    }


    public static void writeFileFromUrl(URL sourceUrl, File targetFile) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) sourceUrl.openConnection();
        connection.setRequestMethod("GET");
        InputStream in = connection.getInputStream();
        FileOutputStream out = new FileOutputStream(targetFile);
        FileCopy(in, out, 1024);
        out.close();
    }

    public static void FileCopy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int n = input.read(buf);
        while (n >= 0) {
            output.write(buf, 0, n);
            n = input.read(buf);
        }
        output.flush();
    }

    public static File getTargetDir() {
        File targetModuleDir = new File(SystemUtils.getApplicationDataDir(), CONTOUR_MODULE_NAME);
        File targetDir = new File(targetModuleDir, AUXDIR);

        return targetDir;
    }


    public static File getModuleDir(String moduleName) {
        if (moduleName == null) {
            return null;
        }

        return new File(SystemUtils.getApplicationDataDir(), moduleName);
    }




    public static File getTargetFile(String filename) {
        File targetModuleDir = new File(SystemUtils.getApplicationDataDir(), CONTOUR_MODULE_NAME);
        File targetDir = new File(targetModuleDir, AUXDIR);
        File targetFile = new File(targetDir, filename);

        return targetFile;
    }


    public static void installAuxdata(URL sourceUrl, String filename) throws IOException {
        File targetFile = getTargetFile(filename);

        try {
        writeFileFromUrl(sourceUrl, targetFile);
        } catch (IOException e) {
            targetFile.delete();
            throw new IOException();
        }

    }

    public static File installAuxdata(Class sourceClass, String filename) throws IOException {
        File targetFile = getTargetFile(filename);

        if (!targetFile.canRead()) {
            URL sourceUrl = ResourceInstaller.getSourceUrl(sourceClass);
            ResourceInstaller resourceInstaller = new ResourceInstaller(sourceUrl, AUXPATH, targetFile.getParentFile());
            try {
                resourceInstaller.install(filename, ProgressMonitor.NULL);
            } catch (Exception e) {
                throw new IOException("file failed: " + e.getMessage());
            }
        }
        return targetFile;
    }

}
