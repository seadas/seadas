package gov.nasa.gsfc.seadas.ocsswws;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/16/13
 * Time: 11:55 AM
 * To change this template use File | Settings | File Templates.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/15/13
 * Time: 12:34 PM
 * To change this template use File | Settings | File Templates.
 */

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class OCSSWServiceLauncher {


    private static int getPort(int defaultPort) {
        String port = System.getenv("JERSEY_HTTP_PORT");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/").port(getPort(9998)).build();
    }

    public static final URI BASE_URI = getBaseURI();

//    protected static HttpServer startServer() throws IOException {
//        System.out.println("Starting grizzly...");
//        ResourceConfig rc = new PackagesResourceConfig("gov.nasa.gsfc.seadas.ocssw_ws.services");
//        return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
//    }

//    public static void main(String[] args) throws IOException {
//        HttpServer httpServer = startServer();
//        System.out.println(String.format("Jersey app started with WADL available at "
//                + "%sapplication.wadl\nTry out %supload file\nHit enter to stop it...",
//                BASE_URI, BASE_URI));
//        System.in.read();
//        httpServer.stop();
//    }

//    public static void main(String[] args) throws IOException, ConfigurationException, DBException {
//
//
//        // Create a WebServer that listen on port 8085
//        // @param port The port opened
//        // @param webResourcesPath the path to the web resource (ex: /var/www)
//        // SeaDAS does not have any web resource, so "/var/www" is extra for now
//
//        GrizzlyWebServer gws = new GrizzlyWebServer(8085, "/var/www");
//        ServletAdapter jerseyAdapter = new ServletAdapter();
//
//        jerseyAdapter.addInitParameter(
//                PackagesResourceConfig.PROPERTY_PACKAGES, "gov.nasa.gsfc.seadas.ocsswws.services");
//        jerseyAdapter.setServletInstance(new ServletContainer());
//
//        gws.addGrizzlyAdapter(jerseyAdapter, new String[]{"/"});
//        //start Grizzly server
//        gws.start();
//    }

//    protected static SelectorThread startServer() throws IOException {
//	        final Map<String, String> initParams = new HashMap<String, String>();
//
//	        initParams.put("com.sun.jersey.config.property.packages",
//	                "YOUR.NAMESPACE.HERE.resources");
//
//	        System.out.println("Starting grizzly...");
//	        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(BASE_URI, initParams);
//	        return threadSelector;
//	    }
//
//	    public static void main(String[] args) throws IOException {
//	        SelectorThread threadSelector = startServer();
//	        System.out.println("Jersey started");
//	        System.in.read();
//	        threadSelector.stopEndpoint();
//	        System.exit(0);
//	    }

}
