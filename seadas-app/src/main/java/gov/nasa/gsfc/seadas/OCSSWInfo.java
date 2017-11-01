package gov.nasa.gsfc.seadas;

import com.bc.ceres.core.runtime.RuntimeContext;
import org.esa.beam.util.SystemUtils;
import org.esa.beam.visat.VisatApp;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

import java.util.regex.Pattern;

/**
 * Created by aabduraz on 5/25/17.
 */

/**
 * OCSSWInfo is an Singleton class.
 */
public class OCSSWInfo {

    public static final String SEADAS_CLIENT_ID_PROPERTY = "client.id";
    public static final String OCSSW_KEEP_FILES_ON_SERVER_PROPERTY = "ocssw.keepFilesOnServer";
    public static final String OS_64BIT_ARCHITECTURE = "_64";
    public static final String OS_32BIT_ARCHITECTURE = "_32";

    public static final String OCSSW_LOCATION_PROPERTY = "ocssw.location";
    public static final String OCSSW_LOCATION_LOCAL ="local";
    public static final String OCSSW_LOCATION_VIRTUAL_MACHINE ="virtualMachine";
    public static final String OCSSW_LOCATION_REMOTE_SERVER ="remoteServer";
    public static final String OCSSW_PROCESS_INPUT_STREAM_PORT = "ocssw.processInputStreamPort";
    public static final String OCSSW_PROCESS_ERROR_STREAM_PORT = "ocssw.processErrorStreamPort";
    private static final Pattern PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static final String OCSSW_SCRIPTS_DIR_SUFFIX = "scripts";
    public static final String OCSSW_DATA_DIR_SUFFIX = "share";
    public static final String OCSSW_BIN_DIR_SUFFIX = "bin";

    public static final String OCSSW_INSTALLER_PROGRAM_NAME = "install_ocssw.py";
    public static final String OCSSW_RUNNER_SCRIPT = "ocssw_runner";

    public static final String VIRTUAL_MACHINE_SERVER_API = "localhost";

    private static OCSSWInfo ocsswInfo = null;

    private int processInputStreamPort;
    private int processErrorStreamPort;

    private boolean ocsswExist;
    private String ocsswRoot;
    private String ocsswDataDirPath;
    private String ocsswScriptsDirPath;
    private String ocsswInstallerScriptPath;
    private String ocsswRunnerScriptPath;
    private String ocsswBinDirPath;

    private String ocsswLocation;
    private String resourceBaseUri;

    public String getOcsswLocation() {
        return ocsswLocation;
    }

    private void setOcsswLocation(String location) { ocsswLocation = location; }

    String clientId;
    private OCSSWInfo() {}

    public static OCSSWInfo getInstance(){
        if (ocsswInfo == null) {
            ocsswInfo = new OCSSWInfo();
            ocsswInfo.detectOcssw();
        }

        return ocsswInfo;
    }

    public  int getProcessInputStreamPort() {
        return processInputStreamPort;
    }

    public  void setProcessInputStreamPort(int processInputStreamPort) {
        processInputStreamPort = processInputStreamPort;
    }

    public  int getProcessErrorStreamPort() {
        return processErrorStreamPort;
    }

    public  void setProcessErrorStreamPort(int processErrorStreamPort) {
        processErrorStreamPort = processErrorStreamPort;
    }

    public  boolean isOCSSWExist() {
        return ocsswExist;
    }

    public  void detectOcssw() {
        String ocsswLocationPropertyValue = RuntimeContext.getConfig().getContextProperty(OCSSW_LOCATION_PROPERTY);

        setOcsswLocation(null);

        boolean isValidOcsswPropertyValue = isValidOcsswLocationProperty(ocsswLocationPropertyValue);

        if ((ocsswLocationPropertyValue.equalsIgnoreCase(OCSSW_LOCATION_LOCAL) && OsUtils.getOperatingSystemType() != OsUtils.OSType.Windows)
             || (!isValidOcsswPropertyValue && (OsUtils.getOperatingSystemType() == OsUtils.OSType.Linux || OsUtils.getOperatingSystemType() == OsUtils.OSType.MacOS)) ) {
            setOcsswLocation(OCSSW_LOCATION_LOCAL);
            initializeLocalOCSSW();
        } else {
            if (ocsswLocationPropertyValue.equalsIgnoreCase(OCSSW_LOCATION_VIRTUAL_MACHINE)) {
                setOcsswLocation(OCSSW_LOCATION_VIRTUAL_MACHINE);
                initializeRemoteOCSSW(VIRTUAL_MACHINE_SERVER_API);

            } else if (validate(ocsswLocationPropertyValue)) {
                setOcsswLocation(OCSSW_LOCATION_REMOTE_SERVER);
                initializeRemoteOCSSW(ocsswLocationPropertyValue);
            }
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
    private  boolean isValidOcsswLocationProperty(String ocsswLocationPropertyValue){
        if( ocsswLocationPropertyValue.equalsIgnoreCase(OCSSW_LOCATION_LOCAL)
                || ocsswLocationPropertyValue.equalsIgnoreCase(OCSSW_LOCATION_VIRTUAL_MACHINE)
                || validate(ocsswLocationPropertyValue)) {
            return true;
        }
        return false;
    }
    public boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }

    public void initializeLocalOCSSW() {
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
                ocsswInstallerScriptPath = ocsswScriptsDirPath + System.getProperty("file.separator") + OCSSW_INSTALLER_PROGRAM_NAME;
                ocsswRunnerScriptPath = ocsswScriptsDirPath + System.getProperty("file.separator") + OCSSW_RUNNER_SCRIPT;
                ocsswBinDirPath = ocsswRoot + System.getProperty("file.separator") + OCSSW_BIN_DIR_SUFFIX;
            }
        }
    }

    private  boolean initializeRemoteOCSSW(String serverAPI) {


        final String BASE_URI_PORT_NUMBER_PROPERTY = "ocssw.port";
        final String OCSSW_REST_SERVICES_CONTEXT_PATH = "ocsswws";
        String baseUriPortNumber = RuntimeContext.getConfig().getContextProperty(BASE_URI_PORT_NUMBER_PROPERTY, "6400");
        resourceBaseUri = "http://" + serverAPI + ":" + baseUriPortNumber + "/" + OCSSW_REST_SERVICES_CONTEXT_PATH + "/";
        System.out.println("server URL:" + resourceBaseUri);
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(MultiPartFeature.class);
        clientConfig.register(JsonProcessingFeature.class).property(JsonGenerator.PRETTY_PRINTING, true);
        Client c = ClientBuilder.newClient(clientConfig);
        WebTarget target = c.target(resourceBaseUri);
        JsonObject jsonObject = target.path("ocssw").path("ocsswInfo").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
        ocsswExist = jsonObject.getBoolean("ocsswExists");
        if (ocsswExist) {
            ocsswRoot = jsonObject.getString("ocsswRoot");
            ocsswDataDirPath = jsonObject.getString("ocsswDataDirPath");
            ocsswInstallerScriptPath = jsonObject.getString("ocsswInstallerScriptPath");
            ocsswRunnerScriptPath = jsonObject.getString("ocsswRunnerScriptPath");
            ocsswScriptsDirPath = jsonObject.getString("ocsswScriptsDirPath");

            clientId = RuntimeContext.getConfig().getContextProperty(SEADAS_CLIENT_ID_PROPERTY, System.getProperty("user.name"));
            String keepFilesOnServer = RuntimeContext.getConfig().getContextProperty(OCSSW_KEEP_FILES_ON_SERVER_PROPERTY, "false");
            Response response = target.path("ocssw").path("manageClientWorkingDirectory").path(clientId).request().put(Entity.entity(keepFilesOnServer, MediaType.TEXT_PLAIN_TYPE));
        }
        processInputStreamPort = new Integer(RuntimeContext.getConfig().getContextProperty(OCSSW_PROCESS_INPUT_STREAM_PORT)).intValue();
        processErrorStreamPort = new Integer(RuntimeContext.getConfig().getContextProperty(OCSSW_PROCESS_ERROR_STREAM_PORT)).intValue();
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

    public  String getResourceBaseUri() {
        return resourceBaseUri;
    }

    private String getServerAPI(){
        return null;
    }

    public  boolean isOcsswExist() {
        return ocsswExist;
    }

    public  String getOcsswRoot() {
        return ocsswRoot;
    }

    public  void setOcsswRoot(String ocsswRootNewValue) {
         ocsswRoot = ocsswRootNewValue;
    }

    public  String getOcsswDataDirPath() {
        return ocsswDataDirPath;
    }

    public  String getOcsswScriptsDirPath() {
        return ocsswScriptsDirPath;
    }

    public String getOcsswInstallerScriptPath() {
        return ocsswInstallerScriptPath;
    }

    public  String getOcsswRunnerScriptPath() {
        return ocsswRunnerScriptPath;
    }

    public String getOcsswBinDirPath() {
        return ocsswBinDirPath;
    }
}
