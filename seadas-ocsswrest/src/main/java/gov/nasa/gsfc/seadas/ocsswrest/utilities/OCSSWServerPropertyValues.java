package gov.nasa.gsfc.seadas.ocsswrest.utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 4/27/15
 * Time: 2:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class OCSSWServerPropertyValues {

    static Properties properties;
    final static String CONFIG_FILE_NAME = "ocsswserver.config";
    final static String CLIENT_SHARED_DIR_NAME_PROPERTY = "clientSharedDirName";
    final static String SERVER_SHARED_DIR_NAME_PROPERTY = "serverSharedDirName";

    public OCSSWServerPropertyValues() {
        properties = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
        try {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (FileNotFoundException fnfe) {
            System.out.println("config file not found!");
        } catch (IOException ioe) {
            System.out.println("IO exception!");
        }
    }

    public String getPropValues(String propertyName) {
        return properties.getProperty(propertyName);
    }

    public static String getClientSharedDirName(){
        return properties.getProperty(CLIENT_SHARED_DIR_NAME_PROPERTY);
    }

    public static String getServerSharedDirName(){

        System.out.println("shared dir name: " + properties.getProperty(SERVER_SHARED_DIR_NAME_PROPERTY));
        return properties.getProperty(SERVER_SHARED_DIR_NAME_PROPERTY);
    }

    public void updateClientSharedDirName( String clientSharedDirName) {
        properties.setProperty(CLIENT_SHARED_DIR_NAME_PROPERTY, clientSharedDirName);
    }

    private static void loadProperties(){

    }

}
