package gov.nasa.gsfc.seadas.ocsswrest;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;
import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWRemote;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.OCSSWServerPropertyValues;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.ServerSideFileUtilities;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWRemote.MLP_OUTPUT_DIR_NAME;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 12/29/14
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/fileServices")
public class OCSSWFileServices {

    final static String SERVER_WORKING_DIRECTORY_PROPERTY = "serverWorkingDirectory";

    private static final String OCSSW_PROCESSING_DIR = "ocsswfiles";
    private static final String FILE_UPLOAD_PATH = System.getProperty("user.home") + System.getProperty("file.separator") + OCSSW_PROCESSING_DIR + System.getProperty("file.separator") + "ifiles";
    private static final String FILE_DOWNLOAD_PATH = System.getProperty("user.home") + System.getProperty("file.separator") + OCSSW_PROCESSING_DIR + System.getProperty("file.separator") + "ofiles";
    private static final String OCSSW_SERVER_DEFAULT_WORKING_DIR = System.getProperty("user.dir") + System.getProperty("file.separator") + "ocsswIntermediateFiles";
    private static final String OCSSW_OUTPUT_COMPRESSED_FILE_NAME = "ocssw_output.zip";
    private static final int BUFFER_SIZE = 1024;

    @GET
    @Path("/serverSharedFileDir")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSharedFileDirName() {
        System.out.println("Shared dir name:" + OCSSWServerPropertyValues.getServerSharedDirName());
        return OCSSWServerPropertyValues.getServerSharedDirName();
    }



   /**
     * Method for uploading a file.
     * handling HTTP POST requests.      *
     * @return String that will be returned as a text/plain response.
     */
    @POST
    @Path("/uploadFile/{jobId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response fileUpload(
            @PathParam("jobId") String jobId,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileInfo)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        String fileName = fileInfo.getFileName();
        if (fileName == null) {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        } else {
            String currentWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName()) + File.separator + jobId;
            File newFile = new File(currentWorkingDir);
            Files.createDirectories(newFile.toPath());
            boolean isDirCreated = new File(currentWorkingDir).isDirectory();
            String ifileFullPathName = currentWorkingDir + File.separator + fileName;

            System.out.println(ifileFullPathName + " is created " + isDirCreated);
            System.out.println(System.getProperty("user.home"));
            System.out.println(new File(currentWorkingDir).getAbsolutePath());

            OCSSWRemote ocsswRemote = new OCSSWRemote();
            try {
                ServerSideFileUtilities.writeToFile(uploadedInputStream, ifileFullPathName);
                SQLiteJDBC.updateItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.IFILE_NAME_FIELD_NAME, ifileFullPathName);
                SQLiteJDBC.updateInputFilesList(jobId, ifileFullPathName);
                String ofileName = ocsswRemote.getOfileName(ifileFullPathName, jobId);
                System.out.println("ofile name = " + ofileName);
                SQLiteJDBC.updateItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.OFILE_NAME_FIELD_NAME, currentWorkingDir + File.separator + ofileName);
            } catch (Exception e) {
                respStatus = Response.Status.INTERNAL_SERVER_ERROR;
                e.printStackTrace();
            }
        }
        return Response.status(respStatus).build();
    }

    /**
     * Method for uploading a file.
     * handling HTTP POST requests.      *
     * @return String that will be returned as a text/plain response.
     */
    @POST
    @Path("/uploadClientFile/{jobId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadClientFile(
            @PathParam("jobId") String jobId,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileInfo)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        Response response;
        String fileInfoString;
        String fileName = fileInfo.getFileName();
        System.out.println("file info " + " is  " + fileName);
        if (fileName == null) {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        } else {
            String currentWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName()) + File.separator + jobId;
            System.out.println("current working directory " + " is  " + currentWorkingDir);
            File newFile = new File(currentWorkingDir);
            Files.createDirectories(newFile.toPath());
            boolean isDirCreated = new File(currentWorkingDir).isDirectory();
            String clientfileFullPathName = currentWorkingDir + File.separator + fileName;

            System.out.println(clientfileFullPathName + " is created " + isDirCreated);
            System.out.println(System.getProperty("user.home"));
            System.out.println(new File(currentWorkingDir).getAbsolutePath());
            try {
                ServerSideFileUtilities.writeToFile(uploadedInputStream, clientfileFullPathName);
                SQLiteJDBC.updateInputFilesList(jobId, clientfileFullPathName);
            } catch (Exception e) {
                respStatus = Response.Status.INTERNAL_SERVER_ERROR;
                e.printStackTrace();
            }
        }
        return Response.status(respStatus).build();
    }

    @POST
    @Path("/upload/{clientId}/{processorId}/{jobId}/{fileName}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response largeFileUpload(
            @PathParam("clientId") String clientId,
            @PathParam("processorId") String processorId,
            @PathParam("jobId") String jobId,
            @PathParam("fileName") String fileName,
            InputStream uploadedInputStream)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        if (fileName == null) {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        } else {

            String uploadedFileDir = FILE_UPLOAD_PATH + File.separator + clientId + File.separator + processorId + File.separator + jobId;
            File newFile = new File(uploadedFileDir);
            Files.createDirectories(newFile.toPath());

            boolean isDirCreated = new File(uploadedFileDir).isDirectory();
            String uploadedFileLocation = uploadedFileDir + File.separator + fileName;

            System.out.println(uploadedFileLocation + " is created " + isDirCreated);
            System.out.println(System.getProperty("user.home"));
            System.out.println(new File(uploadedFileDir).getAbsolutePath());
            try {
                ServerSideFileUtilities.writeToFile(uploadedInputStream, uploadedFileLocation);
                //getFileInfo();
            } catch (Exception e) {
                respStatus = Response.Status.INTERNAL_SERVER_ERROR;
                e.printStackTrace();
            }
        }
        return Response.status(respStatus).build();
    }

    @GET
    @Path("/downloadFile/{jobId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("jobId") String jobId) {
        String ofileName = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.O_FILE_NAME.getFieldName() );
        File file = new File(ofileName);
        StreamingOutput fileStream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                try
                {
                    java.nio.file.Path path = Paths.get(ofileName);
                    byte[] data = Files.readAllBytes(path);
                    outputStream.write(data);
                    outputStream.flush();
                }
                catch (Exception e)
                {
                    throw new WebApplicationException("File Not Found !!");
                }
            }
        };
        System.out.println(file.getAbsolutePath());
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition","attachment; fileName = " + ofileName)
                .build();
    }

    @GET
    @Path("/downloadMLPOutputFile/{jobId}/{ofileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadMLPOutputFile(@PathParam("jobId") String jobId,
                                          @PathParam("ofileName") String clientOfileName) {
        String workingFileDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String mlpOutputDir = workingFileDir + File.separator + jobId + File.separator + MLP_OUTPUT_DIR_NAME;
        String ofileName = mlpOutputDir + File.separator + clientOfileName;
        File file = new File(ofileName);
        if (file.exists() ) {
            StreamingOutput fileStream = new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                    try {
                        java.nio.file.Path path = Paths.get(ofileName);
                        byte[] data = Files.readAllBytes(path);
                        outputStream.write(data);
                        outputStream.flush();
                    } catch (Exception e) {
                        throw new WebApplicationException("File Not Found !!");
                    }
                }
            };
            System.out.println(file.getAbsolutePath());

            return Response
                    .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                    .header("content-disposition", "attachment; fileName = " + ofileName)
                    .build();
        }
        else {
            System.out.println( ofileName + " does not exist");
            return null;
        }
    }


    @GET
    @Path("/downloadFile/{jobId}/{ofileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFileOnDemand(@PathParam("jobId") String jobId,
                                         @PathParam("ofileName") String clientOfileName) {
        String workingFileDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String ofileName = workingFileDir + File.separator + clientOfileName;
        File file = new File(ofileName);
        StreamingOutput fileStream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                try
                {
                    java.nio.file.Path path = Paths.get(ofileName);
                    byte[] data = Files.readAllBytes(path);
                    outputStream.write(data);
                    outputStream.flush();
                }
                catch (Exception e)
                {
                    throw new WebApplicationException("File Not Found !!");
                }
            }
        };
        System.out.println(file.getAbsolutePath());
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition","attachment; fileName = " + ofileName)
                .build();
    }


    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        //return "Got it! \n";
        System.out.println("getting ocssw shared server name");
        OCSSWServerPropertyValues propertyValues = new OCSSWServerPropertyValues();
        return propertyValues.getServerSharedDirName();
    }


    @GET
    @Path("/missionInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getFileMissionInfo() {
        JsonObject jsonObject = Json.createObjectBuilder().build();
        return jsonObject;
    }

}

