package gov.nasa.gsfc.seadas.processing.general;

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

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 8/7/12
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class RSClient {


    public RSClient() {
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:9998").build();
    }

    public WebResource getOCSSWService(){
        final ClientConfig config = new DefaultClientConfig();
        final  Client client = Client.create(config);
        final WebResource resource = client.resource(getBaseURI())
                .path("file")
                .path("upload");
        return resource;
    }

    public boolean uploadFile(String[] filesToUpload) {
        final ClientConfig config = new DefaultClientConfig();
        final  Client client = Client.create(config);
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
            if (!clientResp.getClientResponseStatus().equals(ClientResponse.Status.OK)) {
                System.out.println("Not accepted Response: " + clientResp.getClientResponseStatus());
                System.out.println("Not accepted Response: " +  clientResp.toString());
                return false;
            }
            System.out.println("Response: " + clientResp.getClientResponseStatus() + " " + clientResp.toString());
        }

        client.destroy();

        return true;
    }

    public boolean uploadParam(String params) {
        final ClientConfig config = new DefaultClientConfig();
        final Client client = Client.create(config);
        final WebResource resource = client.resource(getBaseURI())
                .path("file")
                .path("params");

         final ClientResponse clientResp = resource.type(MediaType.TEXT_PLAIN_TYPE)
                .post(ClientResponse.class, params);
        System.out.println("Response: " + clientResp.getClientResponseStatus() + " " + clientResp.toString());
        return clientResp.getClientResponseStatus().equals(ClientResponse.Status.ACCEPTED);
    }

    public boolean uploadCmdArrayString(String cmdArrayString) {
        final ClientConfig config = new DefaultClientConfig();
        final Client client = Client.create(config);
        final WebResource resource = client.resource(getBaseURI())
                .path("file")
                .path("cmdArrayString");

         final ClientResponse clientResp = resource.type(MediaType.TEXT_PLAIN_TYPE)
                .post(ClientResponse.class, cmdArrayString);
        System.out.println("Response: " + clientResp.getClientResponseStatus() + " " + clientResp.toString());
        return clientResp.getClientResponseStatus().equals(ClientResponse.Status.ACCEPTED);
    }
    public boolean uploadCmdArray(String[] cmdArray) {
        final ClientConfig config = new DefaultClientConfig();
        final Client client = Client.create(config);
        final WebResource resource = client.resource(getBaseURI())
                .path("file")
                .path("cmdArray");
        final ClientResponse clientResp = resource.type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, cmdArray);
        return clientResp.getClientResponseStatus().equals(ClientResponse.Status.ACCEPTED);
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

    public boolean uploadParFile(String parString) {
        final ClientConfig config = new DefaultClientConfig();
        final Client client = Client.create(config);
        final WebResource resource = client.resource(getBaseURI())
                .path("file")
                .path("parFile");
        final ClientResponse clientResp = resource.type(MediaType.TEXT_PLAIN_TYPE)
                   .post(ClientResponse.class, parString);
           System.out.println("Response: " + clientResp.getClientResponseStatus() + " " + clientResp.toString());
           return clientResp.getClientResponseStatus().equals(ClientResponse.Status.ACCEPTED);
    }


    public Process runOCSSW() {
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
        String[] cmdTestArray = new String[]{"/Users/aabduraz/get_obpg_file_type.py", "/Users/aabduraz/soapui-settings.xml"}; //, "/Users/aabduraz/soapui-settings.xml"

        //uploadFile(cmdTestArray);
//        final ClientConfig config = new DefaultClientConfig();
//        final Client client = Client.create(config);
//        final WebResource resource = client.resource(getBaseURI())
//                .path("file")
//                .path("upload");
//
//        final File fileToUpload = new File("/Users/Shared/ocssw/test/l2gen/A2003080085000.L1B_LAC");
//        final FormDataMultiPart multiPart = new FormDataMultiPart();
//
//        if (fileToUpload != null)
//
//        {
//            final FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file", fileToUpload, MediaType.APPLICATION_OCTET_STREAM_TYPE);
//            multiPart.bodyPart(fileDataBodyPart);
//        }
//
//        final ClientResponse clientResp = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE)
//                .post(ClientResponse.class, multiPart);
//
//
//       System.out.println("Response: " + clientResp.getClientResponseStatus() + " " + clientResp.toString());
//        client.destroy();
    }
}
