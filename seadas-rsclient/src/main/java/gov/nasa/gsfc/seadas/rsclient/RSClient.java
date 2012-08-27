package gov.nasa.gsfc.seadas.rsclient;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 8/7/12
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

public class RSClient

{
    WebResource resource;

    public RSClient() {

    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:9998").build();
    }


    public boolean uploadFile(String[] filesToUpload) {
        final ClientConfig config = new DefaultClientConfig();
        final Client client = Client.create(config);
        final WebResource resource = client.resource(getBaseURI())
                .path("file")
                .path("upload");

        for (String fileName : filesToUpload) {
            final File fileToUpload = new File(fileName);
            final FormDataMultiPart multiPart = new FormDataMultiPart();
            if (fileToUpload != null) {
                final FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file", fileToUpload, MediaType.APPLICATION_OCTET_STREAM_TYPE);
                multiPart.bodyPart(fileDataBodyPart);
            }
            final ClientResponse clientResp = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE)
                    .post(ClientResponse.class, multiPart);
            if (!clientResp.getClientResponseStatus().equals(ClientResponse.Status.ACCEPTED)) {
                return false;
            }

        }
        return true;
    }


    public boolean uploadFile(String fileName) {
        final ClientConfig config = new DefaultClientConfig();
        final Client client = Client.create(config);
        final WebResource resource = client.resource(getBaseURI())
                .path("file")
                .path("upload");
        final File fileToUpload = new File(fileName);
        final FormDataMultiPart multiPart = new FormDataMultiPart();
        if (fileToUpload != null) {
            final FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file", fileToUpload, MediaType.APPLICATION_OCTET_STREAM_TYPE);
            multiPart.bodyPart(fileDataBodyPart);
        }
        final ClientResponse clientResp = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .post(ClientResponse.class, multiPart);
        return clientResp.getClientResponseStatus().equals(ClientResponse.Status.ACCEPTED);
    }

    public Process runOCSSW(){
        final ClientConfig config = new DefaultClientConfig();
         final Client client = Client.create(config);
         final WebResource ocsswService = client.resource(getBaseURI())
                 .path("ocssw")
                 .path("output");
        ClientResponse response = ocsswService.accept(MediaType.MULTIPART_FORM_DATA_TYPE)
                .get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }

        Process process = null;
        try {
            process = response.getEntity(Process.class);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.destroy();
        }
        return process;
    }

    public boolean downloadFile(String fileName) {

        Client client = Client.create();
        WebResource webResource = client.resource(getBaseURI())
                .path("file")
                .path("download");
        ClientResponse response = webResource.accept(MediaType.MULTIPART_FORM_DATA_TYPE)
                .get(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }
        try {
            File output = response.getEntity(File.class);
            System.out.println("Output from Server .... \n");
            System.out.println(output);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.destroy();
        }
        return true;
    }

    public static void main(String[] args) throws FileNotFoundException {
        final ClientConfig config = new DefaultClientConfig();
        final Client client = Client.create(config);
        final WebResource resource = client.resource(getBaseURI())
                .path("upload")
                .path("file");

        final File fileToUpload = new File("/Users/Shared/ocssw/test/l2gen/A2003080085000.L1B_LAC");
        final FormDataMultiPart multiPart = new FormDataMultiPart();

        if (fileToUpload != null)

        {
            final FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file", fileToUpload, MediaType.APPLICATION_OCTET_STREAM_TYPE);
            multiPart.bodyPart(fileDataBodyPart);
        }

        final ClientResponse clientResp = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .post(ClientResponse.class, multiPart);


        System.out.println("Response: " + clientResp.getClientResponseStatus() + " " + clientResp.toString());
        client.destroy();
    }
}

