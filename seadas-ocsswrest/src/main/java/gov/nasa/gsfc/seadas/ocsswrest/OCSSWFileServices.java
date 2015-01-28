package gov.nasa.gsfc.seadas.ocsswrest;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 12/29/14
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/file")
public class OCSSWFileServices {

    private static final String FILE_UPLOAD_PATH = System.getProperty("user.dir") + System.getProperty("file.separator") + "ifiles";
    private static final String FILE_DOWNLOAD_PATH = System.getProperty("user.dir") + System.getProperty("file.separator") + "ofiles";
    private static final String FILE_DIR = System.getProperty("user.dir") + System.getProperty("file.separator") + "files";
    private static final String OCSSW_OUTPUT_COMPRESSED_FILE_NAME = "ocssw_output.zip";
    private static final int BUFFER_SIZE = 1024;

    String[] cmdArray;

    /**
     * Method for uploading a file.
     * handling HTTP POST requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @POST
     @Path("/upload")
     @Consumes(MediaType.MULTIPART_FORM_DATA)
     public Response fileUpload(
             @FormDataParam("file") InputStream uploadedInputStream,
             @FormDataParam("file") FormDataContentDisposition fileInfo,
             @QueryParam("clientId") String clientID)
             throws IOException {
         Response.Status respStatus = Response.Status.OK;
         if (fileInfo == null) {
             respStatus = Response.Status.INTERNAL_SERVER_ERROR;
         } else {
             final String fileName = fileInfo.getFileName();
             String uploadedFileLocation = FILE_UPLOAD_PATH + File.separator
                     + fileName;
             System.out.println(uploadedFileLocation);
             System.out.println(System.getProperty("user.dir"));
             try {
                 writeToFile(uploadedInputStream, uploadedFileLocation);
                 //getFileInfo();
             } catch (Exception e) {
                 respStatus = Response.Status.INTERNAL_SERVER_ERROR;
                 e.printStackTrace();
             }
         }
         return Response.status(respStatus).build();
     }

    // save uploaded file to new location
    private void writeToFile(InputStream uploadedInputStream,
                             String uploadedFileLocation) {

        try {
            OutputStream out = new FileOutputStream(new File(
                    uploadedFileLocation));
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }
    @GET
    @Path("download")
    //@Produces("application/octet-stream")
    public File downloadFile(@PathParam("fileName") String fileName) {
        File file = new File(FILE_UPLOAD_PATH + "/A2002365234500.L2_LAC");

                //Put some validations here such as invalid file name or missing file name
        if(!file.exists())
        {
            Response.ResponseBuilder response = Response.status(Response.Status.BAD_REQUEST);
            return null;
        }
        System.out.println(file.getAbsolutePath());
        return file;
        //Response.ResponseBuilder responseBuilder = Response.ok((Object) file);
        //responseBuilder.header("Content-Disposition", "attachment; filename=`howtodoinjava.txt'");
        //return responseBuilder.build();
    }
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it! \n";
    }

}

