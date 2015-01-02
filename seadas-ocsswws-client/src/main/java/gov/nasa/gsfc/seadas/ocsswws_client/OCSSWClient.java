package gov.nasa.gsfc.seadas.ocsswws_client;


import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.filter.LoggingFilter;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 12/9/14
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class OCSSWClient {

    public static final String RESOURCE_BASE_URI = "https://localhost:4463";
    public static final String KEY_FILE_PATH = "/Users/Shared/seadas71/seadas/seadas-ocsswws/";

    public void testConnection(){
         SslConfigurator sslConfig = SslConfigurator.newInstance()
                 .trustStoreFile(KEY_FILE_PATH + "client.jks")
                 .trustStorePassword("ocsswwsclient")
                 .keyStoreFile(KEY_FILE_PATH + "client.jks")
                 .keyPassword("ocsswwsclient");

         SSLContext ssl = sslConfig.createSSLContext();
         Client client = ClientBuilder.newBuilder().sslContext(ssl).build();

         System.out.println("Client: GET " + RESOURCE_BASE_URI);

         WebTarget target = client.target(RESOURCE_BASE_URI);

        System.out.println(target.path("hello").request()

            .accept(MediaType.TEXT_PLAIN).get(Response.class)

            .toString());

         target.register(new LoggingFilter());

         Response response;
         response = target.path("/").request().get(Response.class);
         System.out.println("response status: " + response.getStatus());
     }
    public static void main(String[] args) {
        OCSSWClient ocsswwsClient = new OCSSWClient();
        ocsswwsClient.testConnection();
    }

    private static URI getBaseURI() {

        //return UriBuilder.fromUri("http://localhost:8080/com.vogella.jersey.first").build();
        return UriBuilder.fromUri("http://localhost:6400").build();

    }

}
