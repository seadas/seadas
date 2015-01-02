package gov.nasa.gsfc.seadas.ocsswws;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/11/13
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */

import gov.nasa.gsfc.seadas.ocsswws.resource.JobResource;
import gov.nasa.gsfc.seadas.ocsswws.resource.RootResource;
import gov.nasa.gsfc.seadas.ocsswws.services.OCSSWService;
import gov.nasa.gsfc.seadas.ocsswws.utilities.OCSSWRuntimeConfig;
import gov.nasa.gsfc.seadas.ocsswws.utilities.RuntimeConfigException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

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
    protected static void startServer1() {

           // Grizzly ssl configuration
           SSLContextConfigurator sslContext = new SSLContextConfigurator();

           // set up security context
           //sslContext.setKeyStoreFile(System.getProperty("user.dir") + "/seadas/seadas-ocsswws/keystore_server"); // contains server keypair
           System.out.println(System.getProperty("user.dir"));
           sslContext.setKeyStoreFile("/Users/Shared/seadas71/seadas/seadas-ocsswws/server.jks");
           //sslContext.setKeyStoreFile(System.getProperty("user.dir") + "/ocsswsserver/server.jks"); // contains server keypair
           sslContext.setKeyStorePass("ocsswwsserver");
           //sslContext.setTrustStoreFile(System.getProperty("user.dir") + "/ocsswsserver/server.jks"); // contains client certificate
           sslContext.setTrustStoreFile("/Users/Shared/seadas71/seadas/seadas-ocsswws/server.jks");
           sslContext.setTrustStorePass("ocsswwsserver");

           ResourceConfig rc = new ResourceConfig();
           //rc.registerClasses(RootResource.class, SecurityFilter.class, AuthenticationExceptionMapper.class);
           rc.register(RootResource.class);
           rc.register(OCSSWService.class);
           rc.register(JobResource.class);

           try {
               webServer = GrizzlyHttpServerFactory.createHttpServer(
                       getBaseURI(),
                       rc,
                       true,
                       new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(false)
               );

               // start Grizzly embedded server //
               System.out.println("Jersey app started. Try out " + BASE_URI + "\nHit CTRL + C to stop it...");
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
            //Create an array of directories. The directories are named with job ids.
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

        Server server = new Server();

        server.startServer1();

        System.in.read();
    }
}
