package gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel;

/**
 * Created by aabduraz on 4/19/17.
 */

import org.glassfish.grizzly.http.server.HttpServer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("ocssw")
public class OCSSWConfig {

    private static final String OCSSW_PROCESSING_DIR = "ocsswfiles";
    private static final String FILE_UPLOAD_PATH = System.getProperty("user.home") + System.getProperty("file.separator") + OCSSW_PROCESSING_DIR + System.getProperty("file.separator") + "ifiles";
    private static final String FILE_DOWNLOAD_PATH = System.getProperty("user.home") + System.getProperty("file.separator") + OCSSW_PROCESSING_DIR + System.getProperty("file.separator") + "ofiles";
    private static final String FILE_DIR = System.getProperty("user.dir") + System.getProperty("file.separator") + "files";
    private static final String OCSSW_OUTPUT_COMPRESSED_FILE_NAME = "ocssw_output.zip";

    //    public static String configFilePath="ocsswservertest.config";
//    public static final String PROPERTIES_FILE = "ocsswserver.config";
    public static Properties properties = new Properties(System.getProperties());
    private ResourceLoader rl;

    public OCSSWConfig(String configFilePath) {
        rl = getResourceLoader(configFilePath);
        readProperties(rl);
    }

    public Properties readProperties(ResourceLoader resourceLoader) {
        InputStream inputStream = resourceLoader.getInputStream();


        if (inputStream != null) {
            try {
                properties.load(inputStream);
                // set the system properties
                System.setProperties(properties);
                // display new properties
                System.getProperties().list(System.out);
            } catch (IOException e) {
                // TODO Add your custom fail-over code here
                e.printStackTrace();
            }
        }
        return properties;
    }

    private ResourceLoader getResourceLoader(String resourcePath) {
        return new FileResourceLoader(resourcePath);
    }
}

interface ResourceLoader {
    public InputStream getInputStream();
}

class ClassResourceLoader implements ResourceLoader {
    private final String resource;

    public ClassResourceLoader(String resource) {
        this.resource = resource;
    }

    @Override
    public InputStream getInputStream() {
        return HttpServer.class.getResourceAsStream(resource);
    }
}

class FileResourceLoader implements ResourceLoader {
    private final String resource;

    public FileResourceLoader(String resource) {
        this.resource = resource;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(resource);
        } catch (Exception x) {
            return null;
        }
    }
}


