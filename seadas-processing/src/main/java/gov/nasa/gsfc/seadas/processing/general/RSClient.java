package gov.nasa.gsfc.seadas.processing.general;

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.filter.LoggingFilter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
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

    public static final String RESOURCE_BASE_URI = "https://localhost:4463/";
    public static final String KEY_FILE_PATH =  "/Users/Shared/seadas7/seadas/seadas-ocsswws/";
    public RSClient() {
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:4463").build();
    }

//    public WebResource getOCSSWService(){
//        final ClientConfig config = new DefaultClientConfig();
//        final Client client = Client.create(config);
//        final WebResource resource = client.resource(getBaseURI())
//                .path("file")
//                .path("upload");
//        return resource;
//    }
//
//    public boolean uploadFile(String[] filesToUpload) {
//        final ClientConfig config = new DefaultClientConfig();
//        final  Client client = Client.create(config);
//        final WebResource resource = client.resource(getBaseURI())
//                .path("file")
//                .path("upload");
//
//        for (String fileName : filesToUpload) {
//            final File fileToUpload = new File(fileName);
//            final FormDataMultiPart multiPart = new FormDataMultiPart();
//            if (fileToUpload != null) {
//                final FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file", fileToUpload, MediaType.APPLICATION_OCTET_STREAM_TYPE);
//                multiPart.bodyPart(fileDataBodyPart);
//            }
//            final ClientResponse clientResp = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE)
//                                                       .post(ClientResponse.class, multiPart);
//            if (!clientResp.getClientResponseStatus().equals(ClientResponse.Status.OK)) {
//                System.out.println("Not accepted Response: " + clientResp.getClientResponseStatus());
//                System.out.println("Not accepted Response: " +  clientResp.toString());
//                return false;
//            }
//            System.out.println("Response: " + clientResp.getClientResponseStatus() + " " + clientResp.toString());
//        }
//
//        client.destroy();
//
//        return true;
//    }
//
//    public boolean uploadParam(String params) {
//        final ClientConfig config = new DefaultClientConfig();
//        final Client client = Client.create(config);
//        final WebResource resource = client.resource(getBaseURI())
//                .path("file")
//                .path("params");
//
//         final ClientResponse clientResp = resource.type(MediaType.TEXT_PLAIN_TYPE)
//                .post(ClientResponse.class, params);
//        System.out.println("Response: " + clientResp.getClientResponseStatus() + " " + clientResp.toString());
//        return clientResp.getClientResponseStatus().equals(ClientResponse.Status.ACCEPTED);
//    }
//
//    public boolean uploadCmdArrayString(String cmdArrayString) {
//        final ClientConfig config = new DefaultClientConfig();
//        final Client client = Client.create(config);
//        final WebResource resource = client.resource(getBaseURI())
//                .path("file")
//                .path("cmdArrayString");
//
//         final ClientResponse clientResp = resource.type(MediaType.TEXT_PLAIN_TYPE)
//                .post(ClientResponse.class, cmdArrayString);
//        System.out.println("Response: " + clientResp.getClientResponseStatus() + " " + clientResp.toString());
//        return clientResp.getClientResponseStatus().equals(ClientResponse.Status.ACCEPTED);
//    }
//    public boolean uploadCmdArray(String[] cmdArray) {
//        final ClientConfig config = new DefaultClientConfig();
//        final Client client = Client.create(config);
//        final WebResource resource = client.resource(getBaseURI())
//                .path("file")
//                .path("cmdArray");
//        final ClientResponse clientResp = resource.type(MediaType.APPLICATION_JSON_TYPE)
//                .post(ClientResponse.class, cmdArray);
//        return clientResp.getClientResponseStatus().equals(ClientResponse.Status.ACCEPTED);
//    }
//
//
//    public boolean uploadFile(String fileName) {
//        final ClientConfig config = new DefaultClientConfig();
//        final Client client = Client.create(config);
//        final WebResource resource = client.resource(getBaseURI())
//                .path("file")
//                .path("upload");
//        final File fileToUpload = new File(fileName);
//        final FormDataMultiPart multiPart = new FormDataMultiPart();
//        if (fileToUpload != null) {
//            final FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file", fileToUpload, MediaType.APPLICATION_OCTET_STREAM_TYPE);
//            multiPart.bodyPart(fileDataBodyPart);
//        }
//        final ClientResponse clientResp = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE)
//                .post(ClientResponse.class, multiPart);
//        return clientResp.getClientResponseStatus().equals(ClientResponse.Status.ACCEPTED);
//    }
//
//    public boolean uploadParFile(String parString) {
//        final ClientConfig config = new DefaultClientConfig();
//        final Client client = Client.create(config);
//        final WebResource resource = client.resource(getBaseURI())
//                .path("file")
//                .path("parFile");
//        final ClientResponse clientResp = resource.type(MediaType.TEXT_PLAIN_TYPE)
//                   .post(ClientResponse.class, parString);
//           System.out.println("Response: " + clientResp.getClientResponseStatus() + " " + clientResp.toString());
//           return clientResp.getClientResponseStatus().equals(ClientResponse.Status.ACCEPTED);
//    }
//
//
//    public Process runOCSSW() {
//        final ClientConfig config = new DefaultClientConfig();
//        final Client client = Client.create(config);
//        final WebResource ocsswService = client.resource(getBaseURI())
//                .path("ocssw")
//                .path("output");
//        ClientResponse response = ocsswService.accept(MediaType.MULTIPART_FORM_DATA_TYPE)
//                .get(ClientResponse.class);
//        if (response.getStatus() != 200) {
//            throw new RuntimeException("Failed : HTTP error code : "
//                    + response.getStatus());
//        }
//
//        Process process = null;
//        try {
//            process = response.getEntity(Process.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            client.destroy();
//        }
//        return process;
//    }
//
//    public boolean downloadFile(String fileName) {
//
//        Client client = Client.create();
//        WebResource webResource = client.resource(getBaseURI())
//                .path("file")
//                .path("download");
//        ClientResponse response = webResource.accept(MediaType.MULTIPART_FORM_DATA_TYPE)
//                .get(ClientResponse.class);
//
//        if (response.getStatus() != 200) {
//            throw new RuntimeException("Failed : HTTP error code : "
//                    + response.getStatus());
//        }
//        try {
//            File output = response.getEntity(File.class);
//            System.out.println("Output from Server .... \n");
//            System.out.println(output);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            client.destroy();
//        }
//        return true;
//    }

    public String requestNewJobId() {
        Client client = ClientBuilder.newClient();

        WebTarget webTarget = client.target(RESOURCE_BASE_URI);

        WebTarget jobIdWebTarget = webTarget.path("jobs");

        WebTarget jobIddWebTargetWithQueryParam =
                jobIdWebTarget.queryParam("gs616-seadas1", "12345", "get_obpg_file_type.py");

        return null;
    }

    public void testConnection(){
        SslConfigurator sslConfig = SslConfigurator.newInstance()
                .trustStoreFile(KEY_FILE_PATH +  "truststore_client")
                .trustStorePassword("seadas7")

                .keyStoreFile(KEY_FILE_PATH + "keystore_client")
                .keyPassword("seadas7");

        Client client = ClientBuilder.newBuilder().sslContext(sslConfig.createSSLContext()).build();

        System.out.println("Client: GET " + RESOURCE_BASE_URI);

        WebTarget target = client.target(RESOURCE_BASE_URI);
        target.register(new LoggingFilter());

        Response response;

        response = target.path("/").request().get(Response.class);
        System.out.println("response status: " + response.getStatus());
    }

    public static void main(String[] args) throws FileNotFoundException {

        RSClient rsClient = new RSClient();
        rsClient.testConnection();
//        String[] cmdTestArray = new String[]{"/Users/aabduraz/get_obpg_file_type.py", "/Users/aabduraz/soapui-settings.xml"}; //, "/Users/aabduraz/soapui-settings.xml"
//
//       ClientConfig clientConfig = new ClientConfig();
////        clientConfig.register(MyClientResponseFilter.class);
//
//        Client client = ClientBuilder.newClient(clientConfig);
//
//        WebTarget webTarget = client.target(RESOURCE_BASE_URI);
//
//        WebTarget jobIdWebTarget = webTarget.path("jobs");
//
//        WebTarget jobIdWebTargetWithQueryParam =
//                jobIdWebTarget.queryParam("gs616-seadas1", "12345", "get_obpg_file_type.py");
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
