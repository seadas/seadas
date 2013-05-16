package gov.nasa.gsfc.seadas.ocsswws.services;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/16/13
 * Time: 10:53 AM
 * To change this template use File | Settings | File Templates.
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import gov.nasa.gsfc.seadas.ocsswws.utilities.OCSSW;
import gov.nasa.gsfc.seadas.ocsswws.utilities.ServerSideFileUtilities;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 8/6/12
 * Time: 12:48 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/ocssw")

public class OCSSWService {

    public static final String OCSSWROOT_ENVVAR = "OCSSWROOT";
    public static final String OCSSWROOT_PROPERTY = "ocssw.root";

    private static final String PARAM_FILE = System.getProperty("user.dir") + System.getProperty("file.separator") + "params" + File.separator + "param.txt";

    private String currentProgramName;
    private String paramList;


    public static File getOcsswRoot() throws IOException {
        //String dirPath = RuntimeContext.getConfig().getContextProperty(OCSSWROOT_PROPERTY, System.getenv(OCSSWROOT_ENVVAR));
        String dirPath = System.getProperty(OCSSWROOT_PROPERTY, System.getenv(OCSSWROOT_ENVVAR));
        if (dirPath == null) {
            throw new IOException(String.format("Either environment variable '%s' or\n" +
                    "configuration parameter '%s' must be given.", OCSSWROOT_ENVVAR, OCSSWROOT_PROPERTY));
        }
        final File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            throw new IOException(String.format("The directory pointed to by the environment variable  '%s' or\n" +
                    "configuration parameter '%s' seems to be invalid.", OCSSWROOT_ENVVAR, OCSSWROOT_PROPERTY));
        }
        return dir;
    }

    @GET
    @Path("/output")
    @Produces(MediaType.MULTIPART_FORM_DATA)
    public Response runOCSSW() {
        String[] cmdArray = null;
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(new File(ServerSideFileUtilities.PARAM_FILE)));
            String params = reader.readLine();
            StringTokenizer st = new StringTokenizer(params, ";");
            cmdArray = new String[st.countTokens()];
            int i = 0;
            while (st.hasMoreTokens()) {
                cmdArray[i++] = st.nextToken();
            }
        } catch (FileNotFoundException fnfe) {

        } catch (IOException ioe) {

        }

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
        processBuilder.directory(new File(ServerSideFileUtilities.FILE_UPLOAD_PATH));

        int exitValue = -1;
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException ioe) {

        }

        javax.ws.rs.core.Response.ResponseBuilder response = Response.ok((Object) process);
        //Response.Status respStatus = Response.Status.OK;
        //response.header("Content-Disposition",
        //        "attachment; filename=process");
        return (Response) response.build();
        //return Response.status(respStatus).build();
    }

    @GET
    @Path("/fileinfo")
    @Produces(MediaType.MULTIPART_FORM_DATA)
    public Response getFileInfo() {
        String[] cmdArray = null;
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(new File(ServerSideFileUtilities.PARAM_FILE)));
            String params = reader.readLine();
            StringTokenizer st = new StringTokenizer(params, ";");
            cmdArray = new String[st.countTokens()];
            int i = 0;
            while (st.hasMoreTokens()) {
                cmdArray[i++] = st.nextToken();
            }
        } catch (FileNotFoundException fnfe) {

        } catch (IOException ioe) {

        }

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
        processBuilder.directory(new File(ServerSideFileUtilities.FILE_UPLOAD_PATH));

        int exitValue = -1;
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException ioe) {

        }

        //Response.ResponseBuilder response = Response.ok((Object) process);
        Response.Status respStatus = Response.Status.OK;
        //response.header("Content-Disposition",
        //        "attachment; filename=process");
        //return response.build();
        return Response.status(respStatus).build();
    }

    @POST
    @Path("/cmdArray")
    @Consumes({MediaType.TEXT_PLAIN})
    public Response uploadCmdArray(String cmdArrayString)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        JsonParser parser = new JsonParser();
        JsonArray cmdArray = parser.parse(cmdArrayString).getAsJsonArray();

        //cmdArray[0] = OCSSWService.getProgramLocation();
        Process p;
        //p = executeProgram(cmdArray, OCSSWService.getProgramEnv());
        //if (p.exitValue() ==1  )
        // System.out.println(p.exitValue());
        //return Response.status(respStatus).build();
        return Response.ok().build();
    }

    protected static String[] getProgramEnv() {
        final String[] envp = new String[1];
        try {
            envp[0] = "OCSSWROOT=" + getOcsswRoot().getPath();
        } catch (IOException e) {

        }
        return envp;
    }

    protected static String getProgramLocation() {
        String programLocation = null;
        try {
            programLocation = getOcsswRoot().getPath() + "/run/scripts/ocssw_runner";
        } catch (IOException e) {

        }
        return programLocation;
    }


    public String getCurrentProgramName() {
        return currentProgramName;
    }

    public void setCurrentProgramName(String currentProgramName) {
        this.currentProgramName = currentProgramName;
    }

    public String getParamList() {
        return paramList;
    }

    public void setParamList(String paramList) {
        this.paramList = paramList;
    }

    public static Process execute(String[] cmdArray) {
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
        Map<String, String> env = processBuilder.environment();
        HashMap environment = new HashMap();

        environment.put("OCSSWROOT", OCSSW.getOcsswEnv());

        env.putAll(environment);

        processBuilder.directory(new File(ServerSideFileUtilities.FILE_UPLOAD_PATH));
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException ioe) {

        }
        return process;
    }
}

