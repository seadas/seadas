package gov.nasa.gsfc.seadas.ocsswrest;

import gov.nasa.gsfc.seadas.ocsswrest.utilities.OCSSWInfo;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.OCSSWServerModel;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 1/8/15
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */

@Path("/ocssw")
public class OCSSWServices {

    @GET
    @Path("/installDir")
    @Produces(MediaType.TEXT_PLAIN)
    public String getOCSSWInstallDir() {
        return OCSSWServerModel.OCSSW_INSTALL_DIR;
    }

    @GET
    @Path("downloadInstaller")
    @Produces(MediaType.TEXT_XML)
    public boolean getOCSSWInstallerDownloadStatus() {
        return OCSSWServerModel.downloadOCSSWInstaller();
    }

    @GET
    @Path("/ocsswEnv")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OCSSWInfo getOCSSWInfo() {
        OCSSWInfo ocsswInfo = new OCSSWInfo();
        ocsswInfo.setInstalled(true);
        ocsswInfo.setOcsswDir(System.getProperty("user.home" + "/ocssw"));
        return ocsswInfo;
    }

    @POST
    @Path("install")
    @Consumes(MediaType.APPLICATION_JSON)
    public void installOcssw() {

    }

    @POST
    @Path("cmdArray")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response uploadCommandArray(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileInfo,
            @FormDataParam("cmdArray") String[] cmdArray,
            @QueryParam("clientId") String clientID)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        if (fileInfo == null) {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        } else {
            final String fileName = fileInfo.getFileName();
            String uploadedFileLocation =  File.separator
                    + fileName;
            System.out.println(uploadedFileLocation);
            System.out.println(System.getProperty("user.dir"));
            try {
                //writeToFile(uploadedInputStream, uploadedFileLocation);
                //getFileInfo();
            } catch (Exception e) {
                respStatus = Response.Status.INTERNAL_SERVER_ERROR;
                e.printStackTrace();
            }
        }
        return Response.status(respStatus).build();
    }

    @POST
    @Path("installOcssw")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response uploadCommandArray(
            @FormDataParam("installer_URL") String installerURL,
            @FormDataParam("cmdArray") String[] cmdArray,
            @QueryParam("clientId") String clientID)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        if (cmdArray == null) {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        } else {

        }
        return Response.status(respStatus).build();
    }


}
