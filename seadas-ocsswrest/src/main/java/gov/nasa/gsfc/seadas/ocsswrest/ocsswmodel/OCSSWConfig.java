package gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel;

/**
 * Created by aabduraz on 4/19/17.
 */


import gov.nasa.gsfc.seadas.ocsswrest.OCSSWServices;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("ocssw")
public class OCSSWConfig extends Application {

    public static final String PROPERTIES_FILE = "config.properties";
    public static Properties properties = new Properties();

    private Properties readProperties() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                // TODO Add your custom fail-over code here
                e.printStackTrace();
            }
        }
        return properties;
    }

    @Override
    public Set<Class<?>> getClasses() {
        readProperties();
        Set<Class<?>> rootResources = new HashSet<Class<?>>();
        rootResources.add(OCSSWServices.class);
        return rootResources;
    }

}

