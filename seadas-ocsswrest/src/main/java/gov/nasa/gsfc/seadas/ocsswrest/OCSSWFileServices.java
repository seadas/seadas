package gov.nasa.gsfc.seadas.ocsswrest;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;
import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWRemoteImpl;
import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWServerModel;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.OCSSWServerPropertyValues;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.ServerSideFileUtilities;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.*;
import java.io.*;
import java.nio.file.*;

import static gov.nasa.gsfc.seadas.ocsswrest.OCSSWRestServer.OCSSW_ROOT_PROPERTY;
import static gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWRemoteImpl.MLP_OUTPUT_DIR_NAME;
import static gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWRemoteImpl.MLP_PROGRAM_NAME;
import static gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWRemoteImpl.PROCESS_STDOUT_FILE_NAME_EXTENSION;

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

    @GET
    @Path("/fileVerification/{jobId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response fileVerification(@PathParam("jobId") String jobId,
                                     @QueryParam("fileName") String fileName) {
        Response.Status respStatus = Response.Status.NOT_FOUND;
        java.nio.file.Path path1, path2;

        String fileNameWithoutPath = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
        String workingFileDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String fileNameOnServer = workingFileDir + File.separator + fileNameWithoutPath;
        path1 = Paths.get(fileNameOnServer);
        path2 = Paths.get(fileName);

        try {
            System.out.println("path1 = " + path1);
            System.out.println("path2 = " + path2);

            //scenario one: file is in the client working directory
            if (Files.exists(path1)) {
                respStatus = Response.Status.FOUND;
                return Response.status(respStatus).build();
            }

            //scenario two: file is in the ocssw subdirectories
            //if (fileName.contains(System.getProperty(OCSSW_ROOT_PROPERTY)))
            if (fileName.indexOf(File.separator) != -1 && Files.exists(path2)) {
                if ((fileName.contains(System.getProperty(SERVER_WORKING_DIRECTORY_PROPERTY)) || fileName.contains(System.getProperty(OCSSW_ROOT_PROPERTY)))) {

                    respStatus = Response.Status.FOUND;
                } else {
                    System.out.println("file can not be on the server");
                    System.out.println("fileName.indexOf(File.separator) != -1 " + (fileName.indexOf(File.separator) != -1));
                    System.out.println("fileName.contains(System.getProperty(SERVER_WORKING_DIRECTORY_PROPERTY)) " + (fileName.contains(System.getProperty(SERVER_WORKING_DIRECTORY_PROPERTY))));
                    System.out.println("fileName.contains(System.getProperty(OCSSW_ROOT_PROPERTY))" + (fileName.contains(System.getProperty(OCSSW_ROOT_PROPERTY))));
                    respStatus = Response.Status.NOT_FOUND;
                }
            }
            return Response.status(respStatus).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    /**
     * Method for uploading a file.
     * handling HTTP POST requests.      *
     *
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
            String currentWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
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

    @GET
    @Path("/downloadFile/{jobId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("jobId") String jobId) {
        String ofileName = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.O_FILE_NAME.getFieldName());
        StreamingOutput fileStream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws WebApplicationException {
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
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; fileName = " + ofileName)
                .build();
    }

    @GET
    @Path("downloadAncFileList/{jobId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadAncFileList(@PathParam("jobId") String jobId) {

        String serverWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String fileToDownload = serverWorkingDir + File.separator + OCSSWRemoteImpl.ANC_FILE_LIST_FILE_NAME;
        StreamingOutput fileStream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws WebApplicationException {
                try {
                    System.out.println("anc file name = " + fileToDownload);
                    java.nio.file.Path path = Paths.get(fileToDownload);
                    byte[] data = Files.readAllBytes(path);
                    outputStream.write(data);
                    outputStream.flush();
                } catch (Exception e) {
                    throw new WebApplicationException("File Not Found !!");
                }
            }
        };
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; fileName = " + fileToDownload)
                .build();
    }


    @GET
    @Path("getMLPOutputFilesList/{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getMLPOutputFilesList(@PathParam("jobId") String jobId) {
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
        return ocsswRemote.getMLPOutputFilesJsonList(jobId);
    }

    @GET
    @Path("/downloadLogFile/{jobId}/{programName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadLogFile(@PathParam("jobId") String jobId, @PathParam("programName") String programName) {
        String workingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String processStdoutFileName = workingDir + File.separator + programName + PROCESS_STDOUT_FILE_NAME_EXTENSION;
        if (programName.equals(MLP_PROGRAM_NAME)) {
            processStdoutFileName = ServerSideFileUtilities.getLogFileName(workingDir);
        }
        String finalProcessStdoutFileName = processStdoutFileName;
        StreamingOutput fileStream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws WebApplicationException {
                try {
                    java.nio.file.Path path = Paths.get(finalProcessStdoutFileName);
                    byte[] data = Files.readAllBytes(path);
                    outputStream.write(data);
                    outputStream.flush();
                } catch (Exception e) {
                    throw new WebApplicationException("File Not Found !!");
                }
            }
        };
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; fileName = " + finalProcessStdoutFileName)
                .build();
    }

    @GET
    @Path("/downloadMLPOutputFile/{jobId}/{ofileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadMLPOutputFile(@PathParam("jobId") String jobId,
                                          @PathParam("ofileName") String clientOfileName) {
        String workingFileDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        String mlpOutputDir = workingFileDir + File.separator + MLP_OUTPUT_DIR_NAME;
        String ofileName = mlpOutputDir + File.separator + clientOfileName;
        File file = new File(ofileName);
        if (file.exists()) {
            StreamingOutput fileStream = new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream) throws WebApplicationException {
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
        } else {
            System.out.println(ofileName + " does not exist");
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
        StreamingOutput fileStream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws WebApplicationException {
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
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; fileName = " + ofileName)
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
        return OCSSWServerPropertyValues.getServerSharedDirName();
    }


    @GET
    @Path("/missionInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getFileMissionInfo() {
        JsonObject jsonObject = Json.createObjectBuilder().build();
        return jsonObject;
    }

}

