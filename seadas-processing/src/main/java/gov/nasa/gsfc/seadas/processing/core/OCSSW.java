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
    public static final String SEADASHOME_PROPERTY = "home";

    public static String OCSSW_INSTALLER = "install_ocssw.py";
    public static String TMP_OCSSW_INSTALLER = "/tmp/install_ocssw.py";
    public static String OCSSW_INSTALLER_URL = "http://oceandata.sci.gsfc.nasa.gov/ocssw/install_ocssw.py";

    private static boolean ocsswExist = false;
    private static File ocsswRoot = null;


    public static File getOcsswRoot() throws IOException {
        return ocsswRoot;
    }

    public static boolean isOCSSWExist() {
        return ocsswExist;
    }

    public static void checkOCSSW() {
        String dirPath = RuntimeContext.getConfig().getContextProperty(OCSSWROOT_PROPERTY, System.getenv(OCSSWROOT_ENVVAR));

        if ( dirPath == null ) {
            dirPath = RuntimeContext.getConfig().getContextProperty(SEADASHOME_PROPERTY, System.getProperty("user.home") + System.getProperty("file.separator") + "ocssw");
        }
        if ( dirPath != null ) {
             // Check if ${ocssw.root}/run/scripts directory exists in the system.
            // Precondition to detect the existing installation:
            // the user needs to provide "seadas.ocssw.root" value in seadas.config
            // or set OCSSWROOT in the system env.
             ocsswRoot = new File(dirPath);
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
        //final File ocsswRoot = getOcsswRootFile();
        if (ocsswRoot != null) {
            return ocsswRoot.getPath() + "/run/scripts/ocssw_runner";
        } else {
            return null;
        }

    }

    public static String[] getOcsswEnvArray() {
        if (ocsswRoot != null) {
            final String[] envp = {ocsswRoot.getPath()};
            return envp;
        } else {
            return null;
        }
    }

    public static String getOcsswEnv() {
        if (ocsswRoot != null) {
            return ocsswRoot.getPath();
        } else {
            return null;
        }
    }

    private static File getOcsswRootFile() {
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
            boolean isOCSSWRootSpecified = false;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("seadas.ocssw.root")) {
                    line = "seadas.ocssw.root = " + installDir;
                    isOCSSWRootSpecified = true;
                }
                text.append(line);
                text.append("\n");
            }
            //Append "seadas.ocssw.root = " + installDir + "\n" to the runtime config file if it is not exist
            if (!isOCSSWRootSpecified) {
                text.append("seadas.ocssw.root = " + installDir + "\n");
            }
            fileWriter = new FileWriter(new File(RuntimeContext.getConfig().getConfigFilePath()));
            fileWriter.write(text.toString());
            if (fileWriter != null) {
                fileWriter.close();
            }
            ocsswRoot = new File(installDir);
        } catch (IOException ioe) {
            handleException(ioe.getMessage());
        }
    }
}


