package gov.nasa.gsfc.seadas.processing.core.ocssw;


import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.json.stream.JsonGenerator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 12/9/14
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class OCSSWClient {

    public static final String RESOURCE_BASE_URI = "http://localhost:6400/ocsswws/";


    private WebTarget target;

    public OCSSWClient(){
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(MultiPartFeature.class);
        clientConfig.register(JsonProcessingFeature.class).property(JsonGenerator.PRETTY_PRINTING, true);
        Client c = ClientBuilder.newClient(clientConfig);
        target = c.target(RESOURCE_BASE_URI);
    }

    public WebTarget getOcsswWebTarget() {

        return target;
    }

    public static void main(String[] args) {
        OCSSWClient ocsswwsClient = new OCSSWClient();
        WebTarget newTarget = ocsswwsClient.getOcsswWebTarget();

    }
}
