package gov.nasa.gsfc.seadas.ocsswrest.utilities;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/16/13
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class OCSSW {

    public static final String OCSSWROOT_ENVVAR = "OCSSWROOT";
    public static final String OCSSWROOT_PROPERTY = "ocssw.root";

    public static File getOcsswRoot() throws IOException {
        //String dirPath = RuntimeContext.getConfig().getContextProperty(OCSSWROOT_PROPERTY, System.getenv(OCSSWROOT_ENVVAR));
        String dirPath = System.getProperty(OCSSWROOT_PROPERTY, System.getenv(OCSSWROOT_ENVVAR));
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


    public static String getOcsswScriptPath() {
        final File ocsswRoot = getOcsswRootFile();
        if (ocsswRoot != null) {
            return ocsswRoot.getPath() + "/run/scripts/ocssw_runner";
        } else {
            return null;
        }

    }

    public static String[] getOcsswEnv() {
        final File ocsswRoot = getOcsswRootFile();
        if (ocsswRoot != null) {
            final String[] envp = {"OCSSWROOT=" + ocsswRoot.getPath()};
            return envp;
        } else {
            return null;
        }
    }

    private static File getOcsswRootFile() {
        final File ocsswRoot;
        try {
            ocsswRoot = OCSSW.getOcsswRoot();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return ocsswRoot;
    }


}
