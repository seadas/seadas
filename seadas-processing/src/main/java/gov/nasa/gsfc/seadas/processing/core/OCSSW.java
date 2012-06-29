package gov.nasa.gsfc.seadas.processing.core;

import com.bc.ceres.core.runtime.RuntimeConfig;
import com.bc.ceres.core.runtime.RuntimeContext;
import org.esa.beam.util.SystemUtils;
import sun.plugin2.util.SystemUtil;
import sun.security.krb5.Config;

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

}
