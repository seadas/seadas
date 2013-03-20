package gov.nasa.gsfc.seadas.processing.core;

import com.bc.ceres.core.runtime.RuntimeContext;
import org.esa.beam.visat.VisatApp;

import java.io.File;
import java.io.IOException;

/**
 * A ...
 *
 * @author Norman Fomferra
 * @since SeaDAS 7.0
 */
public class OCSSW {

    public static final String OCSSWROOT_ENVVAR = "OCSSWROOT";
    public static final String OCSSWROOT_PROPERTY = "ocssw.root";

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

        if (ocsswExist) {
            return ocsswExist;
        }

        String dirPath = RuntimeContext.getConfig().getContextProperty(OCSSWROOT_PROPERTY, System.getenv(OCSSWROOT_ENVVAR));

        if (dirPath == null ) {
            return false;
        }

        final File dir = new File(dirPath);

        if (!dir.isDirectory()) {
            return false;
        }
        ocsswExist = true;
        return ocsswExist;
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


}
