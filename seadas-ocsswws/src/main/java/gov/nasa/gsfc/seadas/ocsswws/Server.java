package gov.nasa.gsfc.seadas.ocsswws;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/11/13
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import gov.nasa.gsfc.seadas.ocsswws.auth.SecurityFilter;
import gov.nasa.gsfc.seadas.ocsswws.utilities.OCSSWRuntimeConfig;
import gov.nasa.gsfc.seadas.ocsswws.utilities.RuntimeConfigException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static HttpServer webServer;

    public static final URI BASE_URI = getBaseURI();
    public static final String CONTENT = "JERSEY HTTPS EXAMPLE\n";
    public static final String CONFIG_FILE = "ocsswws.config";
    public static final String JOBS_ROOT_DIR = getJobsRootDir();

    public static ConcurrentHashMap<String, ArrayList<String>> jobMap;

    private static URI getBaseURI() {
        return UriBuilder.fromUri("https://localhost/").port(getPort(4463)).build();
    }

    private static int getPort(int defaultPort) {
        String port = System.getProperty("jersey.test.port");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;
    }

    private static String getJobsRootDir() {
        OCSSWRuntimeConfig runtimeConfig;
        try {

            File configFile = new File("config/ocsswws.config");
            String dir = configFile.getParent();
            String path = configFile.getPath();
            //InputStream stream1 = new FileInputStream(configFile.getParent() + "seadas-ocsswws/src/main/config/ocsswws.config");
            runtimeConfig = new OCSSWRuntimeConfig();
            return runtimeConfig.getContextProperty("jobs.root");
            //String clientListFile = runtimeConfig.getContextProperty("clients.fileName");
        } catch (RuntimeConfigException runtimeConfigException) {
            System.out.println(runtimeConfigException.getMessage());
        } finally {
            return System.getProperty("user.dir");
        }

    }

    protected static void startServer() {

        // add Jersey resource servlet
        WebappContext context = new WebappContext("context");
        ServletRegistration registration =
                context.addServlet("ServletContainer", ServletContainer.class);
        registration.setInitParameter("com.sun.jersey.config.property.packages",
                "gov.nasa.gsfc.seadas.ocsswws.resource;gov.nasa.gsfc.seadas.ocsswws.auth");

        // add security filter (which handles http basic authentication)
        registration.setInitParameter(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                SecurityFilter.class.getName());

        // Grizzly ssl configuration
        SSLContextConfigurator sslContext = new SSLContextConfigurator();

        // set up security context
        sslContext.setKeyStoreFile(System.getProperty("user.dir") + "/seadas/seadas-ocsswws/keystore_server"); // contains server keypair
        sslContext.setKeyStorePass("seadas7");
        sslContext.setTrustStoreFile(System.getProperty("user.dir") + "/seadas/seadas-ocsswws/truststore_server"); // contains client certificate
        sslContext.setTrustStorePass("seadas7");

        initJobMap();
        try {

            webServer = GrizzlyServerFactory.createHttpServer(
                    getBaseURI(),
                    null,
                    true,
                    new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(true)
            );

            // start Grizzly embedded server //
            System.out.println("Jersey app started. Try out " + BASE_URI + "\nHit CTRL + C to stop it...");
            context.deploy(webServer);
            webServer.start();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void initJobMap() {

        jobMap = new ConcurrentHashMap<String, ArrayList<String>>();
        File jobRootDir = new File(JOBS_ROOT_DIR);
        File[] clients = jobRootDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        for (File client : clients) {
            ArrayList<String> jobsList = new ArrayList<String>();
            File[] jobs = client.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    }
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }
            });
            for (File job : jobs) {
                jobsList.add(job.getName());
            }
            jobMap.put(client.getName(), jobsList);
        }
    }

    protected static void stopServer() {
        webServer.stop();
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        startServer();

        System.in.read();
    }
}
