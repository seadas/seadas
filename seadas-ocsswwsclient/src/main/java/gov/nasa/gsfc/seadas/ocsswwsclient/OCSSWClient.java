package gov.nasa.gsfc.seadas.ocsswwsclient;


import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 12/9/14
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class OCSSWClient {

    public static final String RESOURCE_BASE_URI = "http://localhost:6400/ocsswws/";


    private HttpServer server;
    private WebTarget target;

    public WebTarget getOcsswWebTarget() {

        // create the client
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(MultiPartFeature.class);

        Client c = ClientBuilder.newClient(clientConfig);

        // uncomment the following line if you want to enable
        // support for JSON in the client (you also have to uncomment
        // dependency on jersey-media-json module in pom.xml and Main.startServer())
        // --
        // c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());

        target = c.target(RESOURCE_BASE_URI);
        return target;
    }


    public void uploadCmdArray(File ifile, String[] cmdArray){

        final File fileToUpload = new File("/Users/Shared/ocssw-dev/test/l2gen/aqua/A2002365234500.L2_LAC");
        System.out.println(fileToUpload.getAbsolutePath() + " " + fileToUpload.exists());

        final FormDataMultiPart multipart = new FormDataMultiPart(); //.field("foo", "bar").bodyPart(filePart);

        if (fileToUpload != null) {
            // MediaType of the body part will be derived from the file.
            final FileDataBodyPart filePart = new FileDataBodyPart("file", fileToUpload, MediaType.MULTIPART_FORM_DATA_TYPE);
            multipart.bodyPart(filePart);
        }
        multipart.field("cmdArray", cmdArray, MediaType.APPLICATION_JSON_TYPE);
        //final Response response = target.path("file").path("upload").request()
        //        .post(Entity.entity(multipart, multipart.getMediaType()));
        final Response response = target.path("ocssw").path("cmdArray").request()
                .post(Entity.entity(cmdArray, MediaType.APPLICATION_JSON_TYPE));
    }

    public static void main(String[] args) {
        OCSSWClient ocsswwsClient = new OCSSWClient();
        WebTarget newTarget = ocsswwsClient.getOcsswWebTarget();

    }

    private static URI getBaseURI() {

        //return UriBuilder.fromUri("http://localhost:8080/com.vogella.jersey.first").build();
        return UriBuilder.fromUri(RESOURCE_BASE_URI).build();

    }

}
