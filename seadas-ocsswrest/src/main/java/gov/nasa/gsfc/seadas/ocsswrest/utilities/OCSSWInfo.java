package gov.nasa.gsfc.seadas.ocsswrest.utilities;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 1/9/15
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
public class OCSSWInfo {
    public static final String _OCSSWROOT_ENVVAR = "OCSSWROOT";
    public static String OCSSW_INSTALLER = "install_ocssw.py";
    public static String OCSSW_RUNNER = "ocssw_runner";
    public static String TMP_OCSSW_INSTALLER = (new File(System.getProperty("java.io.tmpdir"), "install_ocssw.py")).getPath();
    public static String OCSSW_INSTALLER_URL = "https://oceandata.sci.gsfc.nasa.gov/ocssw/install_ocssw.py";

    public static String _OCSSW_SCRIPTS_DIR_SUFFIX =  System.getProperty("file.separator") + "run" +  System.getProperty("file.separator") + "scripts";
    public static String _OCSSW_DATA_DIR_SUFFIX =  System.getProperty("file.separator") + "run" +  System.getProperty("file.separator") + "data";
    private boolean installed;
    private String ocsswDir;

    public boolean isInstalled(){
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public String getOcsswInstallDir(){
        ocsswDir = System.getenv(_OCSSWROOT_ENVVAR);
        return ocsswDir;
    }

    public String getOcsswDataDir(){
        return ocsswDir + _OCSSW_DATA_DIR_SUFFIX;
    }

    public String getOcsswScriptsDir(){
        return ocsswDir + _OCSSW_SCRIPTS_DIR_SUFFIX;
    }

}
