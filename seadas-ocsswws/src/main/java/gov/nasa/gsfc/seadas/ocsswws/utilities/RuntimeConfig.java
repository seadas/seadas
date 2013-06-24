package gov.nasa.gsfc.seadas.ocsswws.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/20/13
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class RuntimeConfig {

    private String configFileKey;
    private String configFilePath;

    private final Properties properties;

    private String debugKey;
    private boolean debug;

    public RuntimeConfig(Properties properties) {
        this.properties = properties;
    }

    public RuntimeConfig() throws RuntimeConfigException {
         properties = System.getProperties();
         initAll();
     }

    private void initAll() throws RuntimeConfigException {
        //initContext();
        initDebug();
        //initHomeDirAndConfiguration();
        initDebug(); // yes, again
       // initClasspathPaths();
       // initModulesDir();
        //initLibDirs();
//        if (isUsingModuleRuntime()) {
//            initAppId();
//        }
//        initLogLevel();
//        initConsoleLog();
//        initLogger();
//        setAutoDetectProperties();
    }

    private void initDebug() {
        debug = Boolean.valueOf(System.getProperty(debugKey, Boolean.toString(debug)));
    }
    private void loadConfiguration() throws RuntimeConfigException {
        trace(String.format("Loading configuration from [%s]", configFilePath));
        try {
            InputStream stream = new FileInputStream(configFilePath);
            try {
                Properties fileProperties = new Properties();
                fileProperties.load(stream);
                // @todo check tests - code was not backward compatible with Java 5
                // so i changed it - but this is not the only place of uncompatibilty
                // add default properties so that they override file properties
                //Set<String> propertyNames = fileProperties.stringPropertyNames();
//                for (String propertyName : propertyNames) {
//                    String propertyValue = fileProperties.getProperty(propertyName);
//                    if (!isPropertySet(propertyName)) {
//                        setProperty(propertyName, propertyValue);
//                        trace(String.format("Configuration property [%s] added", propertyName));
//                    } else {
//                        trace(String.format("Configuration property [%s] ignored", propertyName));
//                    }
//                }

                Enumeration<?> enumeration = fileProperties.propertyNames();
                while (enumeration.hasMoreElements()) {
                    final Object key = enumeration.nextElement();
                    if (key instanceof String) {
                        final Object value = fileProperties.get(key);
                        if (value instanceof String) {
                            final String keyString = (String) key;
                            String propertyValue = fileProperties.getProperty(keyString);
                            if (!isPropertySet(keyString)) {
                                setProperty(keyString, propertyValue);
                                trace(String.format("Configuration property [%s] added", keyString));
                            } else {
                                trace(String.format("Configuration property [%s] ignored", keyString));
                            }
                        }
                    }
                }
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeConfigException(String.format("Failed to load configuration [%s]", configFilePath),
                    e);
        }
    }

    private void trace(String msg) {
        if (debug) {
            System.out.println(String.format("[DEBUG] ceres-config: %s", msg));
        }
    }

    private boolean isPropertySet(String key) {
        return properties.containsKey(key);
    }

    private void setProperty(String key, String value) {
        if (value != null) {
            properties.setProperty(key, value);
        } else {
            properties.remove(key);
        }
    }
}

/**
 * Indicates conditions that a reasonable Ceres application might want to catch.
 *
 * @author  Norman Fomferra
 * @version $Id: RuntimeConfigException.java,v 1.1 2007/03/28 11:39:11 norman Exp $
 */
class RuntimeConfigException extends Exception {
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public RuntimeConfigException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public RuntimeConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
