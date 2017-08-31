package gov.nasa.gsfc.seadas;

import com.bc.ceres.core.runtime.RuntimeContext;
import org.esa.beam.visat.VisatApp;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.File;

import java.util.regex.Pattern;

/**
 * Created by aabduraz on 5/25/17.
 */
public class OCSSWInfo {

    public static final String OS_64BIT_ARCHITECTURE = "_64";
    public static final String OS_32BIT_ARCHITECTURE = "_32";

    public static final String OCSSW_LOCATION_PROPERTY = "ocssw.location";
    private static final Pattern PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static String OCSSW_BIN_DIR_SUFFIX = "run" + File.separator + "bin" + File.separator + getOSName();
    public static String OCSSW_SCRIPTS_DIR_SUFFIX = "run" + File.separator + "scripts";
    public static String OCSSW_DATA_DIR_SUFFIX = "run" + File.separator + "data";

    public static String OCSSW_INSTALLER_PROGRAM = "install_ocssw.py";
    public static String OCSSW_RUNNER_SCRIPT = "ocssw_runner";

    private static boolean ocsswExist;
    private static String ocsswRoot;
    private static String ocsswDataDirPath;
    private static String ocsswScriptsDirPath;
    private static String ocsswInstallerScriptPath;
    private static String ocsswRunnerScriptPath;
    private static String ocsswBinDirPath;

    private static String ocsswLocation;

    public static String getOcsswLocation() {
        return ocsswLocation;
    }

    public static void setOcsswLocation(String ocsswLocation) {
        OCSSWInfo.ocsswLocation = ocsswLocation;
    }

    public enum OCSSWLocationProperties {
        OCSSW_LOCATION_LOCAL("local"),
        OCSSW_LOCATION_VIRTUALMACHINE("virtualMachine"),
        OCSSW_LOCATION_REMOTESERVER("remoteServer");

        String ocsswLocation;

        OCSSWLocationProperties(String ocsswLocation) {
            this.ocsswLocation = ocsswLocation;
        }

        public String getOcsswLocation() {
            return ocsswLocation;
        }

    }

    public static boolean isOCSSWExist() {
        return ocsswExist;
    }

    public static void detectOcssw() {
        String ocsswLocationPropertyValue = RuntimeContext.getConfig().getContextProperty(OCSSW_LOCATION_PROPERTY);

        setOcsswLocation(null);

        boolean isValidOcsswPropertyValue = isValidOcsswLocationProperty(ocsswLocationPropertyValue);

        if ((ocsswLocationPropertyValue.equalsIgnoreCase(OCSSWLocationProperties.OCSSW_LOCATION_LOCAL.getOcsswLocation()) && OsUtils.getOperatingSystemType() != OsUtils.OSType.Windows)
             || (!isValidOcsswPropertyValue && (OsUtils.getOperatingSystemType() == OsUtils.OSType.Linux || OsUtils.getOperatingSystemType() == OsUtils.OSType.MacOS)) ) {
            setOcsswLocation(OCSSWLocationProperties.OCSSW_LOCATION_LOCAL.getOcsswLocation());
            initializeLocalOCSSW();
        } else {
            if (ocsswLocationPropertyValue.equalsIgnoreCase(OCSSWLocationProperties.OCSSW_LOCATION_VIRTUALMACHINE.getOcsswLocation())) {
                setOcsswLocation(OCSSWLocationProperties.OCSSW_LOCATION_VIRTUALMACHINE.getOcsswLocation());
            } else if (validate(ocsswLocationPropertyValue)) {
                setOcsswLocation(OCSSWLocationProperties.OCSSW_LOCATION_REMOTESERVER.getOcsswLocation());
            }
            initializeRemoteOCSSW();
        }

        if (ocsswLocationPropertyValue == null) {
            VisatApp.getApp().showInfoDialog(null, "Please provide OCSSW server location in $SEADAS_HOME/config/seadas.config");
            return;
        }
    }

    /**
     * OCSSW_LOCATION_PROPERTY takes only one of the following three values: local, virtualMachine, IP address of a remote server
     * @return
     */
    private static boolean isValidOcsswLocationProperty(String ocsswLocationPropertyValue){

        if( ocsswLocationPropertyValue.equalsIgnoreCase(OCSSWLocationProperties.OCSSW_LOCATION_LOCAL.getOcsswLocation())
                || ocsswLocationPropertyValue.equalsIgnoreCase(OCSSWLocationProperties.OCSSW_LOCATION_VIRTUALMACHINE.getOcsswLocation())
                || ocsswLocationPropertyValue.equalsIgnoreCase(OCSSWLocationProperties.OCSSW_LOCATION_REMOTESERVER.getOcsswLocation())) {
            return true;
        }
        return false;
    }
    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }

    public static void initializeLocalOCSSW() {
        String ocsswRootPath = RuntimeContext.getConfig().getContextProperty("ocssw.root", System.getenv("OCSSWROOT"));
        if (ocsswRootPath == null) {
            ocsswRootPath = RuntimeContext.getConfig().getContextProperty("home", System.getProperty("user.home") + File.separator + "ocssw");
        }
        if (ocsswRootPath != null) {
            final File dir = new File(ocsswRootPath + File.separator + OCSSW_SCRIPTS_DIR_SUFFIX);
            System.out.println("server ocssw root path: " + dir.getAbsoluteFile());
            if (dir.isDirectory()) {
                ocsswExist = true;
                ocsswRoot = ocsswRootPath;
                ocsswScriptsDirPath = ocsswRoot + File.separator + OCSSW_SCRIPTS_DIR_SUFFIX;
                ocsswDataDirPath = ocsswRoot + File.separator + OCSSW_DATA_DIR_SUFFIX;
                ocsswInstallerScriptPath = ocsswScriptsDirPath + System.getProperty("file.separator") + OCSSW_INSTALLER_PROGRAM;
                ocsswRunnerScriptPath = ocsswScriptsDirPath + System.getProperty("file.separator") + OCSSW_RUNNER_SCRIPT;
                ocsswBinDirPath = ocsswRoot + System.getProperty("file.separator") + OCSSW_BIN_DIR_SUFFIX;
            }
        }
    }

    private static boolean initializeRemoteOCSSW() {
        final String BASE_URI_PORT_NUMBER_PROPERTY = "ocssw.port";
        final String OCSSW_REST_SERVICES_CONTEXT_PATH = "ocsswws";
        String baseUriPortNumber = RuntimeContext.getConfig().getContextProperty(BASE_URI_PORT_NUMBER_PROPERTY, "6400");
        String serverAPI = RuntimeContext.getConfig().getContextProperty("ocssw.location", "locahost");
        String resourceBaseUri = "http://" + serverAPI + ":" + baseUriPortNumber + "/" + OCSSW_REST_SERVICES_CONTEXT_PATH + "/";
        System.out.println("server URL:" + resourceBaseUri);
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(MultiPartFeature.class);
        clientConfig.register(JsonProcessingFeature.class).property(JsonGenerator.PRETTY_PRINTING, true);
        Client c = ClientBuilder.newClient(clientConfig);
        WebTarget target = c.target(resourceBaseUri);
        JsonObject jsonObject = target.path("ocssw").path("ocsswInfo").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        boolean ocsswExist = jsonObject.getBoolean("ocsswExists");
        if (ocsswExist) {
            ocsswDataDirPath = jsonObject.getString("ocsswDataDirPath");
            ocsswInstallerScriptPath = jsonObject.getString("ocsswInstallerScriptPath");
            ocsswRunnerScriptPath = jsonObject.getString("ocsswRunnerScriptPath");
            ocsswScriptsDirPath = jsonObject.getString("ocsswScriptsDirPath");
        }
        return ocsswExist;
    }

    public static String getOSName() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (System.getProperty("os.arch").indexOf("64") != -1) {
            return osName + OS_64BIT_ARCHITECTURE;
        } else {
            return osName + OS_32BIT_ARCHITECTURE;
        }
    }

    public static boolean isOcsswExist() {
        return ocsswExist;
    }

    public static String getOcsswRoot() {
        return ocsswRoot;
    }

    public static void setOcsswRoot(String ocsswRootNewValue) {
         ocsswRoot = ocsswRootNewValue;
    }

    public static String getOcsswDataDirPath() {
        return ocsswDataDirPath;
    }

    public static String getOcsswScriptsDirPath() {
        return ocsswScriptsDirPath;
    }

    public static String getOcsswInstallerScriptPath() {
        return ocsswInstallerScriptPath;
    }

    public static String getOcsswRunnerScriptPath() {
        return ocsswRunnerScriptPath;
    }

    public static String getOcsswBinDirPath() {
        return ocsswBinDirPath;
    }
}
