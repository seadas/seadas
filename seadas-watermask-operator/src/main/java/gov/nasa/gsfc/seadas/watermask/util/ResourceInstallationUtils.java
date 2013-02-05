package gov.nasa.gsfc.seadas.watermask.util;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.util.ResourceInstaller;
import org.esa.beam.util.SystemUtils;

import java.io.*;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 1/17/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceInstallationUtils {

    public static String MODULE_NAME = "beam-watermask-operator";
    public static String AUXDIR = "auxdata";
    public static String AUXPATH = "org/esa/beam/watermask/operator/"+AUXDIR+"/";





    public static void writeFileFromUrl(URL sourceUrl, File targetFile) throws Exception {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sourceUrl.openStream()));

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
    }


    public static File getTargetFile(String filename) {
        File targetModuleDir = new File(SystemUtils.getApplicationDataDir(), MODULE_NAME);
        File targetDir = new File(targetModuleDir, AUXDIR);
        File targetFile = new File(targetDir, filename);

        return targetFile;
    }


    public static void installAuxdata(URL sourceUrl, String filename) {
        File targetFile = getTargetFile(filename);

        try {
            writeFileFromUrl(sourceUrl, targetFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File installAuxdata(Class sourceClass, String filename) {
        File targetFile = getTargetFile(filename);

        if (!targetFile.canRead()) {
            URL sourceUrl = ResourceInstaller.getSourceUrl(sourceClass);
            ResourceInstaller resourceInstaller = new ResourceInstaller(sourceUrl, AUXPATH, targetFile.getParentFile());
            try {
                resourceInstaller.install(filename, ProgressMonitor.NULL);
            } catch (Exception e) {
                System.out.printf("resource not copied - %s", e.getMessage());
            }
        }
        return targetFile;
    }

}
