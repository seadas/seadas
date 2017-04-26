package gov.nasa.gsfc.seadas.ocsswrest.utilities;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/16/13
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */

import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;


public class OCSSWServerModelOld {


    public static String OCSSW_INSTALL_DIR = System.getProperty("user.home") + "/ocssw";

    public static String OCSSW_SCRIPTS_DIR_SUFFIX =  "run" +  System.getProperty("file.separator") + "scripts";
    public static String OCSSW_DATA_DIR_SUFFIX =   "run" +  System.getProperty("file.separator") + "data";

    public static final String OCSSWROOT_PROPERTY = "ocssw.root";
    public static final String SEADASHOME_PROPERTY = "home";

    public static String OCSSW_INSTALLER = "install_ocssw.py";

    public static String OCSSW_INSTALLER_URL = "https://oceandata.sci.gsfc.nasa.gov/ocssw/install_ocssw.py";

    public static String OCSSW_INSTALLER_FILE_LOCATION = (new File(System.getProperty("java.io.tmpdir"), "install_ocssw.py")).getPath();
    public static String OCDATAROOT;
    public static String _OCSSW_SCRIPTS_DIR = OCSSW_INSTALL_DIR +  System.getProperty("file.separator") + "run" +  System.getProperty("file.separator") + "scripts";

    public static final String missionDataDir = OCSSW_INSTALL_DIR
            + System.getProperty("file.separator") + "run"
            + System.getProperty("file.separator") + "data"
            + System.getProperty("file.separator");

    private static boolean ocsswExist = false;
    private static File ocsswRoot = null;
    private static boolean ocsswInstalScriptDownloadSuccessful = false;

    private static HashMap<String, Process> processHashMap;

    private static String currentJobId;
    private static boolean progressMonitorFlag;


    /**
     * This method detects the existence of ocssw on server. The install directory should be user.home/ocssw
     * Existence criteria: $"user.home"/ocssw/run/scripts directory exists
     * @return Returns true if ocssw exists, else return false.
     */
    public static boolean isOCSSWExist() {
            final File dir = new File(OCSSW_INSTALL_DIR  + System.getProperty("file.separator") + OCSSW_SCRIPTS_DIR_SUFFIX);
            if (dir.isDirectory()) {
                return true;
            } else {
                return false;
            }
    }


    public static File getOcsswRoot() throws IOException {
        return ocsswRoot;
    }

    public static void init(){
        checkOCSSW();
        processHashMap = new HashMap<>();
    }

    public static void checkOCSSW() {

        // Check if ${ocssw.root}/run/scripts directory exists in the system.
        // Precondition to detect the existing installation:
        // the user needs to provide "seadas.ocssw.root" value in seadas.config
        // or set OCSSWROOT in the system env.
        ocsswRoot = new File(OCSSW_INSTALL_DIR);
        final File dir = new File(_OCSSW_SCRIPTS_DIR);
        if (dir.isDirectory()) {
            ocsswExist = true;
            return;
        }
    }

    public static String getOcsswScriptPath() {
        final File ocsswRoot = getOcsswRootFile();
        if (ocsswRoot != null) {
            return _OCSSW_SCRIPTS_DIR + System.getProperty("file.separator") +"ocssw_runner";
        } else {
            return null;
        }

    }

    public static String[] getCommandArrayPrefix() {
        String[] cmdArray = new String[3];
        cmdArray[0] = OCSSWServerModelOld.getOcsswScriptPath();
        cmdArray[1] = "--ocsswroot";
        cmdArray[2] = OCSSWServerModelOld.getOcsswEnv();
        return cmdArray;
    }

    public static String[] getOcsswEnvArray() {
        if (ocsswRoot != null) {
            final String[] envp = {OCSSW_INSTALL_DIR};
            return envp;
        } else {
            return null;
        }
    }

    public static String getOcsswEnv() {

        return OCSSW_INSTALL_DIR;
    }

    public static String[] getCmdArrayPrefix() {
        String[] cmdArrayPrefix = new String[3];
        cmdArrayPrefix[0] = OCSSWServerModelOld.getOcsswScriptPath();
        cmdArrayPrefix[1] = "--ocsswroot";
        cmdArrayPrefix[2] = OCSSWServerModelOld.getOcsswEnv();
        return cmdArrayPrefix;
    }

    public static String findIfileOnServer(String ifileName) {
        OCSSWServerPropertyValues propertyValues = new OCSSWServerPropertyValues();
        String ifileServerName = ifileName.replace(propertyValues.getClientSharedDirName(), propertyValues.getServerSharedDirName());
        return ifileServerName;
    }

    private static File getOcsswRootFile() {
        final File ocsswRoot;
        try {
            ocsswRoot = OCSSWServerModelOld.getOcsswRoot();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return ocsswRoot;
    }

    /**
     * this method doanloads the latest ocssw_install.py program from   "https://oceandata.sci.gsfc.nasa.gov/ocssw/install_ocssw.py" location.
     *
     * @return true if download is successful.
     */
    public static boolean downloadOCSSWInstaller() {

        if (isOcsswInstalScriptDownloadSuccessful()) {
            return ocsswInstalScriptDownloadSuccessful;
        }
        try {
            URL website = new URL(OCSSW_INSTALLER_URL);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(OCSSW_INSTALLER_FILE_LOCATION);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            fos.close();
            (new File(OCSSW_INSTALLER_FILE_LOCATION)).setExecutable(true);
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
        System.out.println(errorMessage);
    }

    public static boolean isOcsswInstalScriptDownloadSuccessful() {
        return ocsswInstalScriptDownloadSuccessful;
    }


    public static  Process getProcess(String jobId) {
        return processHashMap.get(jobId);
    }

    public static void addProcess(String jobId, Process process) {
        processHashMap.put(jobId, process);
    }

    public static String getCurrentJobId() {
        return currentJobId;
    }

    public static void setCurrentJobId(String currentJobId) {
        OCSSWServerModelOld.currentJobId = currentJobId;
    }

    public static boolean isProgressMonitorFlag() {
        return progressMonitorFlag;
    }

    public static void setProgressMonitorFlag(String progressMonitorFlag) {

        OCSSWServerModelOld.progressMonitorFlag = new Boolean(progressMonitorFlag).booleanValue();
    }
}
