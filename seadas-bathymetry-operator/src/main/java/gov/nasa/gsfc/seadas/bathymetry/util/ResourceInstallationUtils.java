package gov.nasa.gsfc.seadas.bathymetry.util;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import gov.nasa.gsfc.seadas.bathymetry.operator.BathymetryOp;
import org.esa.beam.BeamUiActivator;
import org.esa.beam.util.ResourceInstaller;
import org.esa.beam.util.SystemUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

import gov.nasa.gsfc.seadas.bathymetry.ui.BathymetryData;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 1/17/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceInstallationUtils {

    public static String BATHYMETRY_MODULE_NAME = "seadas-bathymetry-operator";
    public static String AUXDIR = "auxdata";
    public static String BATHYMETRY_PATH = "gov/nasa/gsfc/seadas/bathymetry/";
    public static String AUXPATH = BATHYMETRY_PATH + "operator/" + AUXDIR + "/";
    public static String ICON_PATH = BATHYMETRY_PATH + "ui/icons/";

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

        return BathymetryData.getOcsswRoot().getParentFile();
    }


    public static File getModuleDir(String moduleName) {
        if (moduleName == null) {
            return null;
        }

        return new File(SystemUtils.getApplicationDataDir(), moduleName);
    }




    public static File getTargetFile(String filename) {
   //     File targetFile = new File(getTargetDir(), filename);

        File targetModuleDir = new File(SystemUtils.getApplicationDataDir(), BATHYMETRY_MODULE_NAME);
        File targetDir = new File(targetModuleDir, AUXDIR);
        File targetFile = new File(targetDir, filename);

        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

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
