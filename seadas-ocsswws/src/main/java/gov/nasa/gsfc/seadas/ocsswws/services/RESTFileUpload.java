package gov.nasa.gsfc.seadas.ocsswws.services;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/16/13
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 7/30/12
 * Time: 1:21 PM
 * To change this template use File | Settings | File Templates.
 */

import com.google.gson.Gson;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.StringTokenizer;

@Path("/file")

public class RESTFileUpload

{
    private static final String FILE_UPLOAD_PATH = System.getProperty("user.dir") + System.getProperty("file.separator") + "ifiles";
    private static final String FILE_DOWNLOAD_PATH = System.getProperty("user.dir") + System.getProperty("file.separator") + "ofiles";
    private static final String FILE_DIR = System.getProperty("user.dir") + System.getProperty("file.separator") + "files";
    private static final String OCSSW_OUTPUT_COMPRESSED_FILE_NAME = "ocssw_output.zip";
    private static final int BUFFER_SIZE = 1024;

    String[] cmdArray;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response fileUpload(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileInfo)
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
                saveToDisc(uploadedInputStream, uploadedFileLocation);
                //getFileInfo();
            } catch (Exception e) {
                respStatus = Response.Status.INTERNAL_SERVER_ERROR;
                e.printStackTrace();
            }
        }
        return Response.status(respStatus).build();
    }

    private void getFileInfo() {
        Process process = null;
        String ocsswDir = null;
        try {
            process = Runtime.getRuntime().exec("ls -l");
            ocsswDir = OCSSWService.getOcsswRoot().getPath();

        } catch (IOException ioe) {

        }
        if (ocsswDir != null) {
            System.out.println("ocssw root: " + ocsswDir);
        }
        if (process != null) {
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException ioe) {

            }
        }
    }

//    @POST
//    @Path("/params")
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response paramUpload(
//            @FormDataParam("param") InputStream uploadedInputStream,
//            @FormDataParam("param") FormDataContentDisposition fileInfo)
//            throws IOException {
//        Response.Status respStatus = Response.Status.OK;
//        if (fileInfo == null) {
//            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
//        } else {
//            final String fileName = fileInfo.getFileName();
//            String uploadedFileLocation = FILE_UPLOAD_PATH + File.separator
//                    + fileName;
//            System.out.println(uploadedFileLocation);
//            System.out.println(System.getProperty("user.dir"));
//            try {
//                saveToDisc(uploadedInputStream, uploadedFileLocation);
//            } catch (Exception e) {
//                respStatus = Response.Status.INTERNAL_SERVER_ERROR;
//                e.printStackTrace();
//            }
//        }
//        return Response.status(respStatus).build();
//    }

    //    @POST
//    @Path("/file")
//    @Consumes("application/octet-stream")
//    public Response putFile(@Context HttpServletRequest a_request,
//                             @PathParam("fileId") long a_fileId,
//                             InputStream a_fileInputStream) throws IOException
//    {
//        Response.Status respStatus = Response.Status.OK;
//        try
//             {
//                 saveToDisc(a_fileInputStream, "/Users/aabduraz/test");
//             }
//             catch (Exception e)
//             {
//                 respStatus = Response.Status.INTERNAL_SERVER_ERROR;
//                 e.printStackTrace();
//             }
//
//     return Response.status(respStatus).build();
//    }
    // save uploaded file to the specified location

    private void saveToDisc(final InputStream fileInputStream,
                            final String fileUploadPath) throws IOException

    {
        final OutputStream out = new FileOutputStream(new File(fileUploadPath));
        int read = 0;
        byte[] bytes = new byte[BUFFER_SIZE];
        while ((read = fileInputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }


    private Process executeProgram(String[] cmdArray, String[] programEnv) throws IOException {
        try {
            return Runtime.getRuntime().exec(cmdArray, programEnv, new File(FILE_UPLOAD_PATH));
        } catch (Exception e) {
            //return Runtime.getRuntime().exec(cmdArray, programEnv);
            System.out.println("Execution exception!");

        }
        return null;
    }

//    private Process executeProcess(String[] cmdArray, String[] programEnv, File rootDir) throws IOException {
//        return Runtime.getRuntime().exec(cmdArray, programEnv, rootDir);
//    }
//
//    private File getRootDir() {
//
//        return new File("test");
//    }

//    public static void main(String[] args) throws IOException {
//        if (!(new File(FILE_UPLOAD_PATH).isDirectory())) {
//            File testFile = new File(FILE_UPLOAD_PATH + System.getProperty("file.separator") + "README");
//            testFile.getParentFile().mkdir();
//        }
//
//        HttpServer server = HttpServerFactory.create("http://localhost:9998/");
//
//        server.start();
//
//        System.out.println("Server running");
//        System.out.println("Visit: http://localhost:9998/upload");
//        System.out.println("Hit return to stop...");
//        System.in.read();
//        System.out.println("Stopping server");
//        server.stop(0);
//        System.out.println("Server stopped");
//    }

    private static final String FILE_PATH = FILE_DOWNLOAD_PATH + File.separator + OCSSW_OUTPUT_COMPRESSED_FILE_NAME;

    @GET
    @Path("/download")
    @Produces(MediaType.MULTIPART_FORM_DATA)
    public Response getFile() {

        File file = new File(FILE_PATH);
        Response.ResponseBuilder response = Response.ok((Object) file);
        response.header("Content-Disposition",
                "attachment; filename=" + OCSSW_OUTPUT_COMPRESSED_FILE_NAME);
        return response.build();

    }

    @GET
    @Path("/{ofileName: [0-9]+}")
    @Produces(MediaType.MULTIPART_FORM_DATA)
    public Response getUser(@PathParam("ofileName") String ofileName) {
        File file = new File(FILE_DIR + File.separator + ofileName);

        Response.ResponseBuilder response = Response.ok((Object) file);
        response.header("Content-Disposition",
                "attachment; filename=" + ofileName);
        return response.build();
    }

    @POST
    @Path("/params")
    @Consumes({MediaType.TEXT_PLAIN})
    public Response paramUpload(String params)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        params = params.replaceAll("\n", ";");
        params = params.substring(0, params.lastIndexOf(";"));
        StringTokenizer st = new StringTokenizer(params, ";");
        cmdArray = new String[st.countTokens()];
        int i = 0;
        //String tmpString;
        StringTokenizer st1;
        while (st.hasMoreTokens()) {
            //tmpString = st.nextToken();
            st1 = new StringTokenizer(st.nextToken(), ":");
            st1.nextToken();
            cmdArray[i++] = st1.nextToken();
        }
        cmdArray[0] = OCSSWService.getProgramLocation();
        Process p;
        p = executeProgram(cmdArray, OCSSWService.getProgramEnv());
        //if (p.exitValue() ==1  )
        // System.out.println(p.exitValue());
        return Response.status(respStatus).build();
    }

    @POST
    @Path("/parFile")
    @Consumes({MediaType.TEXT_PLAIN})
    public Response parFileUpload(String params)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        params = params.replaceAll("\n", ";");
        params = params.substring(0, params.lastIndexOf(";"));
        StringTokenizer st = new StringTokenizer(params, ";");
        cmdArray = new String[st.countTokens() + 1];
        int i = 1;
        //String tmpString;
        StringTokenizer st1;
        while (st.hasMoreTokens()) {
            //tmpString = st.nextToken();
            st1 = new StringTokenizer(st.nextToken(), ":");
            st1.nextToken();
            cmdArray[i++] = st1.nextToken();
        }
        cmdArray[0] = OCSSWService.getProgramLocation();
        Process p;
        p = executeProgram(cmdArray, OCSSWService.getProgramEnv());
        //if (p.exitValue() ==1  )
        // System.out.println(p.exitValue());
        return Response.status(respStatus).build();
    }

    @GET
    @Path("/params")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getParams() {

        Response.ResponseBuilder response = Response.ok((Object) cmdArray);
        response.header("Content-Disposition",
                "attachment; cmdArray generated from params");
        return response.build();

    }

    @POST
    @Path("/cmdArray")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadCmdArray(String jsonCmdArray) {
        Response.Status respStatus = Response.Status.OK;
        Gson gson = new Gson();
        String[] cmdArray = gson.fromJson(jsonCmdArray, String[].class);
        for (String cmd : cmdArray) {
            System.out.println("cmdarray : " + cmdArray);
        }
        return Response.status(respStatus).build();

    }

//    @GET
//    @Consumes("multipart/form-data")
//    @Produces("text/plain")
//    @Path("submit/{client_id}/{doc_id}/{html}/{password}")
//    public Response submit(@PathParam("client_id") String clientID,
//                       @PathParam("doc_id") String docID,
//                       @PathParam("html") String html,
//                       @PathParam("password") String password,
//                       @PathParam("pdf") File pdf) {
//      return Response.ok("true").build();
//    }
}
