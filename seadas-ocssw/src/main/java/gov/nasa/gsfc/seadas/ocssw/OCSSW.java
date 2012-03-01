package gov.nasa.gsfc.seadas.ocssw;

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
    public static final String OCSSWROOT_PROPERTY = "seadas.ocssw.root";
    public static final String OCSSW_ARCH_ENVVAR = "OCSSW_ARCH";
    public static final String OCSSW_ARCH_PROPERTY = "seadas.ocssw.arch";

    public static File getOcsswRoot() throws IOException {
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

    public static String getOcsswArch() throws IOException {
        String arch = System.getProperty(OCSSW_ARCH_PROPERTY, System.getenv(OCSSW_ARCH_ENVVAR));
        if (arch == null) {
            throw new IOException(String.format("Either environment variable '%s' or\n" +
                                                        "configuration parameter '%s' must be given.", OCSSWROOT_ENVVAR, OCSSWROOT_PROPERTY));
        }
        return arch;
    }
}
