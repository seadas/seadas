package gov.nasa.gsfc.seadas.processing.core;

import com.bc.ceres.core.runtime.RuntimeContext;
import org.esa.beam.visat.VisatApp;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * A ...
 *
 * @author Norman Fomferra
 * @author Aynur Abdurazik
 * @since SeaDAS 7.0
 */
public class OCSSW {

    public static final String OCSSWROOT_ENVVAR = "OCSSWROOT";

    public static final String OCSSWROOT_PROPERTY = "ocssw.root";

    public static final String SEADAS_CONFIG_UPDATE_PROGRAM_NAME = "rewrite_seadas_config.py";
    public static String OCSSW_INSTALLER = "install_ocssw.py";
    public static String TMP_OCSSW_INSTALLER = "/tmp/install_ocssw.py";
    public static String OCSSW_INSTALLER_URL = "http://oceandata.sci.gsfc.nasa.gov/ocssw/install_ocssw.py";

    private static boolean ocsswExist = false;

    public static File getOcsswRoot() throws IOException {
        String dirPath = RuntimeContext.getConfig().getContextProperty(OCSSWROOT_PROPERTY, System.getenv(OCSSWROOT_ENVVAR));
        //String dirPath = System.getProperty(OCSSWROOT_PROPERTY, System.getenv(OCSSWROOT_ENVVAR));
        if (dirPath == null) {
            throw new IOException(String.format("Either environment variable '%s' or\n" +
                    "configuration parameter '%s' must be given.", OCSSWROOT_ENVVAR, OCSSWROOT_PROPERTY));
        }
        final File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            throw new IOException(String.format("The directory pointed to by the environment variable  '%s' or\n" +
                    "configuration parameter '%s' seems to be invalid.", OCSSWROOT_ENVVAR, OCSSWROOT_PROPERTY));
        }
        return dir;
    }

    public static boolean isOCSSWExist() {

//        if (ocsswExist) {
//            return ocsswExist;
//        }
//
//        String dirPath = RuntimeContext.getConfig().getContextProperty(OCSSWROOT_PROPERTY, System.getenv(OCSSWROOT_ENVVAR));
//
//        if (dirPath == null) {
//            return false;
//        }
//
//        // Check if ${ocssw.root}/run/scripts directory exists in the system.
//        // Precondition to detect the existing installation:
//        // the user needs to provide "seadas.ocssw.root" value in seadas.config
//        // or set OCSSWROOT in the system env.
//        final File dir = new File(dirPath + System.getProperty("file.separator") + "run" + System.getProperty("file.separator") + "scripts");
//
//        if (!dir.isDirectory()) {
//            return false;
//        }
//        ocsswExist = true;
        return ocsswExist;
    }

    public static void checkOCSSW() {
        String dirPath = RuntimeContext.getConfig().getContextProperty(OCSSWROOT_PROPERTY, System.getenv(OCSSWROOT_ENVVAR));


        if (!(dirPath == null)) {

            // Check if ${ocssw.root}/run/scripts directory exists in the system.
            // Precondition to detect the existing installation:
            // the user needs to provide "seadas.ocssw.root" value in seadas.config
            // or set OCSSWROOT in the system env.
            final File dir = new File(dirPath + System.getProperty("file.separator") + "run" + System.getProperty("file.separator") + "scripts");

            if (dir.isDirectory()) {
                ocsswExist = true;
                return;
            }
        }

        downloadOCSSWInstaller();
    }


    public static File getOcsswDataRoot() throws IOException {
        return new File(new File(getOcsswRoot(), "run"), "data");
    }


    public static String getOcsswScriptPath() {
        final File ocsswRoot = getOcsswRootFile();
        if (ocsswRoot != null) {
            return ocsswRoot.getPath() + "/run/scripts/ocssw_runner";
        } else {
            return null;
        }

    }

    public static String[] getOcsswEnvArray() {
        final File ocsswRoot = getOcsswRootFile();
        if (ocsswRoot != null) {
            //final String[] envp = {"OCSSWROOT=" + ocsswRoot.getPath()};
            final String[] envp = {ocsswRoot.getPath()};
            return envp;
        } else {
            return null;
        }
    }

    public static String getOcsswEnv() {
        final File ocsswRoot = getOcsswRootFile();
        if (ocsswRoot != null) {
            return ocsswRoot.getPath();
        } else {
            return null;
        }
    }

    private static File getOcsswRootFile() {
        final File ocsswRoot;
        try {
            ocsswRoot = OCSSW.getOcsswRoot();
        } catch (IOException e) {
            if (VisatApp.getApp() != null)
                VisatApp.getApp().showErrorDialog(e.getMessage());
            return null;
        }
        return ocsswRoot;
    }

    public static void downloadOCSSWInstaller() {
        try {
            URL website = new URL("http://oceandata.sci.gsfc.nasa.gov/ocssw/install_ocssw.py");
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(TMP_OCSSW_INSTALLER);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            fos.close();
            (new File(TMP_OCSSW_INSTALLER)).setExecutable(true);
        } catch (MalformedURLException malformedURLException) {
            VisatApp.getApp().showInfoDialog("URL for downloading install_ocssw.py is not correct!", null);
        } catch (FileNotFoundException fileNotFoundException) {
            VisatApp.getApp().showInfoDialog(fileNotFoundException.getMessage(), null);
        } catch (IOException ioe) {
            VisatApp.getApp().showInfoDialog(ioe.getMessage(), null);
        }
    }

    private static void handleException(String errorMessage) {
        VisatApp.getApp().showInfoDialog(errorMessage, null);
    }

    public static void updateOCSSWRoot(String installDir) {
        FileWriter fileWriter = null;
        try {
            final FileReader reader = new FileReader(new File(RuntimeContext.getConfig().getConfigFilePath()));
            final BufferedReader br = new BufferedReader(reader);

            StringBuilder text = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("seadas.ocssw.root")) {
                    line = "seadas.ocssw.root = " + installDir;
                }
                text.append(line);
                text.append("\n");
            }
            fileWriter = new FileWriter(new File(RuntimeContext.getConfig().getConfigFilePath()));
            fileWriter.write(text.toString());
            if (fileWriter != null) {
                fileWriter.close();
            }
        } catch (IOException ioe) {
            handleException(ioe.getMessage());
        }
    }
}
