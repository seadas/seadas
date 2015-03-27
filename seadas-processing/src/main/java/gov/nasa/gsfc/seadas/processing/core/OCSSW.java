package gov.nasa.gsfc.seadas.processing.core;

import com.bc.ceres.core.runtime.RuntimeContext;
import org.esa.beam.visat.VisatApp;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
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
    public static final String OCSSWLOCATION_PROPERTY = "ocssw.location";
    public static final String SEADASHOME_PROPERTY = "home";
    public static final String SEADAS_OCSSW_LOCATION_LOCAL = "local";
    public static final String SEADAS_OCSSW_LOCATION_REMOTE = "remote";

    public static String OCSSW_INSTALLER = "install_ocssw.py";
    public static String TMP_OCSSW_INSTALLER = (new File(System.getProperty("java.io.tmpdir"), "install_ocssw.py")).getPath();
    public static String OCSSW_INSTALLER_URL = "http://oceandata.sci.gsfc.nasa.gov/ocssw/install_ocssw.py";

    private static boolean ocsswExist = false;
    private static String ocsswRoot = null;
    private static boolean ocsswInstalScriptDownloadSuccessful = false;

    private static String clientId;
    private static String processorId;
    private static String jobId;

    public static String getOcsswRoot() throws IOException {
        return ocsswRoot;
    }

    public static boolean isOCSSWExist() {
        return ocsswExist;
    }


    public static void checkOCSSW() {

        String ocsswLocation = RuntimeContext.getConfig().getContextProperty(OCSSWLOCATION_PROPERTY);

        //ocssw installed local
        if (ocsswLocation == null || ocsswLocation.trim().equals(SEADAS_OCSSW_LOCATION_LOCAL)) {
            String dirPath = RuntimeContext.getConfig().getContextProperty(OCSSWROOT_PROPERTY, System.getenv(OCSSWROOT_ENVVAR));

            if (dirPath == null) {
                dirPath = RuntimeContext.getConfig().getContextProperty(SEADASHOME_PROPERTY, System.getProperty("user.home") + System.getProperty("file.separator") + "ocssw");
            }
            if (dirPath != null) {
                // Check if ${ocssw.root}/run/scripts directory exists in the system.
                // Precondition to detect the existing installation:
                // the user needs to provide "seadas.ocssw.root" value in seadas.config
                // or set OCSSWROOT in the system env.
                ocsswRoot = dirPath;
                final File dir = new File(dirPath + System.getProperty("file.separator") + "run" + System.getProperty("file.separator") + "scripts");
                if (dir.isDirectory()) {
                    ocsswExist = true;
                    return;
                }
            }
        } else {
            //ocssw installed in virtual box and needs be accessed through web services
            // need to access to two services: one for ocsswRoot, and the other for ocsswExist -this doesn't work. Because ocsswrest package is not available to SeadasMain at this time
//            OCSSWClient ocsswClient = new OCSSWClient();
//            WebTarget target = ocsswClient.getOcsswWebTarget();
//            ocsswRoot = target.path("ocssw").path("installDir").request(MediaType.TEXT_PLAIN).get(String.class);
//            ocsswExist = target.path("ocssw").path("ocsswInfo").request(MediaType.APPLICATION_JSON).get(new GenericType<HashMap<String, Boolean>>() {}).get("ocsswExists");
            //Assumption: ocssw is already installed on the virtual box and the location is ${remoteuser.home}/ocssw
            ocsswRoot = "${remoteuser.home}/ocssw";
            ocsswExist = true;
        }
    }


    public static String getOcsswDataRoot() throws IOException {
        //return new File(new File(getOcsswRoot(), "run"), "data");
        return ocsswRoot + "/run/data/";
    }


    public static String getOcsswScriptPath() {
        //final File ocsswRoot = getOcsswRootFile();
        if (ocsswRoot != null) {
            return ocsswRoot + "/run/scripts/ocssw_runner";
        } else {
            return null;
        }

    }

    public static String[] getOcsswEnvArray() {
        if (ocsswRoot != null) {
            final String[] envp = {ocsswRoot};
            return envp;
        } else {
            return null;
        }
    }

    public static String getOcsswEnv() {

        return ocsswRoot;
    }

    public static boolean downloadOCSSWInstaller() {

        if (isOcsswInstalScriptDownloadSuccessful()) {
            return ocsswInstalScriptDownloadSuccessful;
        }
        try {
            URL website = new URL(OCSSW_INSTALLER_URL);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(TMP_OCSSW_INSTALLER);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            fos.close();
            (new File(TMP_OCSSW_INSTALLER)).setExecutable(true);
            ocsswInstalScriptDownloadSuccessful = true;
        } catch (MalformedURLException malformedURLException) {
            handleException("URL for downloading install_ocssw.py is not correct!");
        } catch (FileNotFoundException fileNotFoundException) {
            handleException("ocssw installation script failed to download. \n" +
                    "Please check network connection or 'seadas.ocssw.root' variable in the 'seadas.config' file. \n" +
                    "possible cause of error: " + fileNotFoundException.getMessage());
        } catch (IOException ioe) {
            handleException("ocssw installation script failed to download. \n" +
                    "Please check network connection or 'seadas.ocssw.root' variable in the \"seadas.config\" file. \n" +
                    "possible cause of error: " + ioe.getLocalizedMessage());
        } finally {
            return ocsswInstalScriptDownloadSuccessful;
        }
    }

    private static void handleException(String errorMessage) {
        VisatApp.getApp().showErrorDialog(errorMessage);
    }

    public static boolean isOcsswInstalScriptDownloadSuccessful() {
        return ocsswInstalScriptDownloadSuccessful;
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
            ocsswRoot = installDir;
        } catch (IOException ioe) {
            handleException(ioe.getMessage());
        }
    }

    public static String getClientId() {
        return clientId;
    }

    public static void setClientId(String clientId) {
        OCSSW.clientId = clientId;
    }

    public static String getProcessorId() {
        return processorId;
    }

    public static void setProcessorId(String processorId) {
        OCSSW.processorId = processorId;
    }


    public static String getJobId() {
        return jobId;
    }

    public static void createJobId() {

        OCSSWClient ocsswClient = new OCSSWClient();
        WebTarget target = ocsswClient.getOcsswWebTarget();
        jobId = target.path("jobs").path("newJobId").request(MediaType.TEXT_PLAIN).get(String.class);
    }
}


