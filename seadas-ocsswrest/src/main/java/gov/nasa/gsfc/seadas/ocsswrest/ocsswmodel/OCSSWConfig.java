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
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("ocssw")
public class OCSSWConfig {


    public final static String OCSSW_PROCESS_INPUTSTREAM_SOCKET_PORT_NUMBER_PROPERTY = "processInputStreamPortNumber";
    public final static String OCSSW_PROCESS_ERRORSTREAM_SOCKET_PORT_NUMBER_PROPERTY = "processErrorStreamPortNumber";

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
                String key, value;
                Set<Object> keys = properties.keySet();
                Iterator itr = keys.iterator();
                while (itr.hasNext()) {
                    key = (String) itr.next();
                    value = properties.getProperty(key);
                    if (value.startsWith("$")) {
                        value = System.getProperty(value.substring(value.indexOf("{") + 1, value.indexOf("}"))) + value.substring(value.indexOf("}") + 1);
                        properties.setProperty(key, value);
                    }
                }
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


