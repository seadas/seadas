package gov.nasa.gsfc.seadas.ocsswrest;

import gov.nasa.gsfc.seadas.ocsswrest.utilities.MissionInfo;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.OCSSWInfo;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.OCSSWServerModel;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.ProcessRunner;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 1/8/15
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */

@Path("/ocssw")
public class OCSSWServices {

    private static String NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME = "next_level_name.py";
    private static String NEXT_LEVEL_FILE_NAME_TOKEN = "Output Name:";
    private HashMap<String, Boolean> missionDataStatus;

    @GET
    @Path("/installDir")
    @Produces(MediaType.TEXT_PLAIN)
    public String getOCSSWInstallDir() {

        return OCSSWServerModel.OCSSW_INSTALL_DIR;
    }

    @GET
    @Path("/ocsswInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getOcsswInstallStatus() {
        final File ocsswScriptsDir = new File(OCSSWServerModel.OCSSW_INSTALL_DIR + System.getProperty("file.separator") + "run" + System.getProperty("file.separator") + "scripts");
        System.out.println("ocsswExists");
        JsonObject ocsswInstallStatus = Json.createObjectBuilder().add("ocsswExists", ocsswScriptsDir.isDirectory()).build();
        return ocsswInstallStatus;
    }

    @GET
    @Path("missions")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getMissionDataStatus() {
        return new MissionInfo().getMissions();
    }

    @GET
    @Path("downloadInstaller")
    @Produces(MediaType.TEXT_XML)
    public boolean getOCSSWInstallerDownloadStatus() {
        return OCSSWServerModel.downloadOCSSWInstaller();
    }


    @GET
    @Path("/evalDirInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getOCSSWEvalDirInfo() {
        JsonObject evalDirStatus = Json.createObjectBuilder().add("eval", new File(OCSSWServerModel.missionDataDir + "eval").exists()).build();
        return evalDirStatus;
    }

    @GET
    @Path("/srcDirInfo")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public JsonObject getOCSSWSrcDirInfo() {
        JsonObject srcDirStatus = Json.createObjectBuilder().add("build", new File(OCSSWServerModel.missionDataDir + "build").exists()).build();
        return srcDirStatus;
    }

    @GET
    @Path("/ocsswEnv")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OCSSWInfo getOCSSWInfo() {
        OCSSWInfo ocsswInfo = new OCSSWInfo();
        ocsswInfo.setInstalled(true);
        ocsswInfo.setOcsswDir(System.getProperty("user.home") + "/ocssw");
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
//        if (fileInfo == null) {
//            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
//        } else {
//            final String fileName = fileInfo.getFileName();
//            String uploadedFileLocation = File.separator
//                    + fileName;
//            System.out.println(uploadedFileLocation);
//            System.out.println(System.getProperty("user.dir"));
//            try {
//                //writeToFile(uploadedInputStream, uploadedFileLocation);
//                //getFileInfo();
//            } catch (Exception e) {
//                respStatus = Response.Status.INTERNAL_SERVER_ERROR;
//                e.printStackTrace();
//            }
//        }
        return Response.status(respStatus).build();
    }

    @POST
    @Path("installOcssw")
    @Consumes(MediaType.APPLICATION_JSON)
    public Process installOcssw(JsonArray jsonArray)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        Process process = null;
        if (jsonArray == null) {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        } else {
            writeToFile(jsonArray.getString(0));
            downloadOCSSWInstaller();

            String[] cmdArray = getCmdArray(jsonArray);

            cmdArray[0] = OCSSWServerModel.OCSSW_INSTALLER_FILE_LOCATION;

            process = ProcessRunner.executeInstaller(cmdArray);
        }
        return process; //Response.status(respStatus).build();
    }

    @POST
    @Path("runOcsswProcessor")
    @Consumes(MediaType.APPLICATION_JSON)
    public Process runOcsswProcessor(JsonArray jsonArray)
            throws IOException {
        Response.Status respStatus = Response.Status.OK;
        Process process = null;
        if (jsonArray == null) {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        } else {

            String[] cmdArray = getCmdArray(jsonArray);

            cmdArray[0] = OCSSWServerModel.OCSSW_INSTALLER_FILE_LOCATION;

            process = ProcessRunner.executeInstaller(cmdArray);
        }
        return process; //Response.status(respStatus).build();
    }

    @GET
    @Path("nextLevelFileName")
    @Consumes(MediaType.TEXT_PLAIN)
    public static String findNextLevelFileName(String ifileName, String programName) {
         if (ifileName == null || programName == null) {
             return null;
         }
         if (programName.equals("l3bindump")) {
             return ifileName + ".xml";
         }

         String[] cmdArray = new String[6];
         cmdArray[0] = OCSSWServerModel.getOcsswScriptPath();
         cmdArray[1] = "--ocsswroot";
         cmdArray[2] = OCSSWServerModel.getOcsswEnv();
         cmdArray[3] = NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME;
         cmdArray[4] = ifileName;
         cmdArray[5] = programName;


         String ifileDir = ifileName.substring(0, ifileName.lastIndexOf(System.getProperty("file.separator")));
         Process process = null;//OCSSWRunner.execute(cmdArray, new File(ifileDir));

         int exitCode = process.exitValue();
         InputStream is;
         if (exitCode == 0) {
             is = process.getInputStream();
         } else {
             is = process.getErrorStream();
         }

         InputStreamReader isr = new InputStreamReader(is);
         BufferedReader br = new BufferedReader(isr);

         try {

             if (exitCode == 0) {
                 String line;
                 while ((line = br.readLine()) != null) {
                     if (line.startsWith(NEXT_LEVEL_FILE_NAME_TOKEN)) {
                         return (line.substring(NEXT_LEVEL_FILE_NAME_TOKEN.length())).trim();
                     }
                 }

             } else {
                 System.out.println("Failed exit code on program '" + programName + "'");
             }

         } catch (IOException ioe) {

             System.out.println(ioe.getMessage());
         }

         return null;
     }

//    public static String findNextLevelFileName(String ifileName, String programName) {
//        if (ifileName == null || programName == null) {
//            return null;
//        }
//        if (programName.equals("l3bindump")) {
//            return ifileName + ".xml";
//        }
//
//        String[] cmdArray = new String[6];
//
//        cmdArray[0] = OCSSWServerModel.getOcsswScriptPath();
//        cmdArray[1] = "--ocsswroot";
//        //cmdArray[2] = OCSSWServerModel.getOcsswEnv();
//        cmdArray[3] = NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME;
//        cmdArray[4] = ifileName;
//        cmdArray[5] = programName;
//
//        String ifileDir = ifileName.substring(0, ifileName.lastIndexOf(System.getProperty("file.separator")));
//        Process process = null; //OCSSWRunner.execute(cmdArray, new File(ifileDir));
//
//        if (process ==null ) {
//            return "output";
//        }
//        int exitCode = process.exitValue();
//        InputStream is;
//        if (exitCode == 0) {
//            is = process.getInputStream();
//        } else {
//            is = process.getErrorStream();
//        }
//
//        InputStreamReader isr = new InputStreamReader(is);
//        BufferedReader br = new BufferedReader(isr);
//
//        try {
//
//            if (exitCode == 0) {
//                String line;
//                while ((line = br.readLine()) != null) {
//                    if (line.startsWith(NEXT_LEVEL_FILE_NAME_TOKEN)) {
//                        return (line.substring(NEXT_LEVEL_FILE_NAME_TOKEN.length())).trim();
//                    }
//                }
//
//            } else {
//                System.out.println("Failed exit code on program '" + programName + "'");
//            }
//
//        } catch (IOException ioe) {
//
//            System.out.println(ioe.getMessage());
//        }
//        return "output";
//    }
//
    private static String[] getCmdArrayForNextLevelNameFinder(String ifileName, String programName){
        String[] cmdArray = new String[6];
        cmdArray[0] = OCSSWServerModel.getOcsswScriptPath();
        cmdArray[1] = "--ocsswroot";
        cmdArray[2] = OCSSWServerModel.getOcsswEnv();
        cmdArray[3] = NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME;
        cmdArray[4] = ifileName;
        cmdArray[5] = programName;
        return cmdArray;

    }

//    private static String getNextLevelFileName(String ifileName, String[] cmdArray) {
//
//        String ifileDir = ifileName.substring(0, ifileName.lastIndexOf(System.getProperty("file.separator")));
//        Process process = OCSSWRunner.execute(cmdArray, new File(ifileDir));
//        if (process == null) {
//            return null;
//        }
//
//        int exitCode = process.exitValue();
//        InputStream is;
//        if (exitCode == 0) {
//            is = process.getInputStream();
//        } else {
//            is = process.getErrorStream();
//        }
//
//        InputStreamReader isr = new InputStreamReader(is);
//        BufferedReader br = new BufferedReader(isr);
//
//        try {
//
//            if (exitCode == 0) {
//                String line;
//                while ((line = br.readLine()) != null) {
//                    if (line.startsWith(NEXT_LEVEL_FILE_NAME_TOKEN)) {
//                        return (line.substring(NEXT_LEVEL_FILE_NAME_TOKEN.length())).trim();
//                    }
//                }
//
//            } else {
//                debug("Failed exit code on program '" + cmdArray[5] + "'");
//            }
//
//        } catch (IOException ioe) {
//
//            VisatApp.getApp().showErrorDialog(ioe.getMessage());
//        }
//         return null;
//    }
//
//    public static String findNextLevelFileName(String ifileName, String programName, String[] additionalOptions) {
//
//        if (ifileName == null || programName == null) {
//            return null;
//        }
//        if (programName.equals("l3bindump")) {
//            return ifileName + ".xml";
//        }
//        debug("Program name is " + programName);
//        Debug.assertNotNull(ifileName);
//
//        String[] cmdArray = (String[])ArrayUtils.addAll(getCmdArrayForNextLevelNameFinder(ifileName, programName), additionalOptions);
//        String ofileName = getNextLevelFileName(ifileName, cmdArray);
//        if (ofileName == null) {
//            ofileName = "output";
//        }
//
//        return ofileName;
//    }

    private String[] getCmdArray(JsonArray jsonArray) {
        String text = "cmdArray: ";
        String str;
        ArrayList<String> list = new ArrayList<String>();
        if (jsonArray != null) {
            int len = jsonArray.size();
            for (int i = 0; i < len; i++) {
                str = jsonArray.get(i).toString();
                str = str.replace('"', ' ');
                str = str.trim();
                list.add(str);
                text = text + str;
            }
        }
        writeToFile(text);

        String[] cmdArray = list.toArray(new String[list.size()]);
        return cmdArray;
    }

    public static boolean downloadOCSSWInstaller() {

        try {

            URL website = new URL(OCSSWServerModel.OCSSW_INSTALLER_URL);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(OCSSWServerModel.OCSSW_INSTALLER_FILE_LOCATION);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            fos.close();
            (new File(OCSSWServerModel.OCSSW_INSTALLER_FILE_LOCATION)).setExecutable(true);
            ocsswInstalScriptDownloadSuccessful = true;
        } catch (MalformedURLException malformedURLException) {
            System.out.println("URL for downloading install_ocssw.py is not correct!");
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("ocssw installation script failed to download. \n" +
                    "Please check network connection or 'seadas.ocssw.root' variable in the 'seadas.config' file. \n" +
                    "possible cause of error: " + fileNotFoundException.getMessage());
        } catch (IOException ioe) {
            System.out.println("ocssw installation script failed to download. \n" +
                    "Please check network connection or 'seadas.ocssw.root' variable in the \"seadas.config\" file. \n" +
                    "possible cause of error: " + ioe.getLocalizedMessage());
        } finally {
            return ocsswInstalScriptDownloadSuccessful;
        }
    }

    private static boolean ocsswInstalScriptDownloadSuccessful = false;

    private void writeToFile(String content) {
        FileOutputStream fop = null;
        File file;
        try {

            file = new File("/home/aabduraz/cmdArray.txt");
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
