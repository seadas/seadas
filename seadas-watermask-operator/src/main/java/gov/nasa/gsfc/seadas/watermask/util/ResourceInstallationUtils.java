package gov.nasa.gsfc.seadas.watermask.util;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.util.ResourceInstaller;
import org.esa.beam.util.SystemUtils;

import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 1/17/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceInstallationUtils {

    public static String MODULE_NAME = "seadas-watermask-operator";
    public static String AUXDIR = "auxdata";
    public static String WATERMASK_PATH = "gov/nasa/gsfc/seadas/watermask/";
    public static String AUXPATH = WATERMASK_PATH + "operator/" + AUXDIR + "/";
    public static String ICON_PATH = WATERMASK_PATH + "ui/icons/";

public static String getIconFilename(String icon, Class sourceClass) {

    URL sourceUrl = ResourceInstaller.getSourceUrl(sourceClass);
    String iconFilename =  sourceUrl.toString() + ICON_PATH + icon;

    return iconFilename;
}



    public static void writeFileFromUrl(URL sourceUrl, File targetFile) throws IOException {

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
            throw new IOException("failed to write file from url: " + e.getMessage() );
        }
    }



    public static File getTargetDir() {
        File targetModuleDir = new File(SystemUtils.getApplicationDataDir(), MODULE_NAME);
        File targetDir = new File(targetModuleDir, AUXDIR);

        return targetDir;
    }


    public static File getTargetFile(String filename) {
        File targetModuleDir = new File(SystemUtils.getApplicationDataDir(), MODULE_NAME);
        File targetDir = new File(targetModuleDir, AUXDIR);
        File targetFile = new File(targetDir, filename);

        return targetFile;
    }


    public static void installAuxdata(URL sourceUrl, String filename) throws IOException {
        File targetFile = getTargetFile(filename);

            writeFileFromUrl(sourceUrl, targetFile);

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
