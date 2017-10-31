package gov.nasa.gsfc.seadas.ocssw;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.processing.common.SeadasFileUtils;
import gov.nasa.gsfc.seadas.processing.common.SeadasProcess;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by aabduraz on 3/27/17.
 */
public class OCSSWVM extends OCSSWRemote {
    public final static String OCSSW_VM_SERVER_SHARED_DIR_PROPERTY = "ocssw.sharedDir";
    public final static String OCSSW_VM_SERVER_SHARED_DIR_PROPERTY_DEFAULT_VALUE = System.getProperty("user.home") + File.separator + "ocsswVMServerSharedDir";

    String sharedDirPath;
    String workingDir;



    public OCSSWVM() {

        this.initialize();

    }

    private void initialize() {
        sharedDirPath = RuntimeContext.getConfig().getContextProperty(OCSSW_VM_SERVER_SHARED_DIR_PROPERTY, OCSSW_VM_SERVER_SHARED_DIR_PROPERTY_DEFAULT_VALUE);
//        String remoteServerIPAddress = RuntimeContext.getConfig().getContextProperty(OCSSW_LOCATION_PROPERTY, "localhost");
//        String remoteServerPortNumber = RuntimeContext.getConfig().getContextProperty(OCSSW_SERVER_PORT_PROPERTY, OCSSW_VIRTUAL_SERVER_PORT_FORWWARD_NUMBER_FOR_CLIENT);
        OCSSWClient ocsswClient = new OCSSWClient(ocsswInfo.getResourceBaseUri());
        target = ocsswClient.getOcsswWebTarget();
        if (ocsswInfo.isOcsswExist()) {
            jobId = target.path("jobs").path("newJobId").request(MediaType.TEXT_PLAIN_TYPE).get(String.class);
            clientId = RuntimeContext.getConfig().getContextProperty(SEADAS_CLIENT_ID_PROPERTY, System.getProperty("user.home"));
            target.path("ocssw").path("ocsswSetClientId").path(jobId).request().put(Entity.entity(clientId, MediaType.TEXT_PLAIN_TYPE));
            workingDir = sharedDirPath + File.separator + clientId + File.separator + jobId + File.separator;
        }
    }

    public boolean uploadIFile(String ifileName) {

        if (true) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method copies the client file into the shared directory between the host and the virtual machine.
     * The shared directory is specified in the seadas.config file.
     *
     * @param fileName
     * @return
     */
    @Override

    public boolean uploadClientFile(String fileName) {

        copyFile(fileName);
        ProgressMonitor pm = null;

        try {
            String fileTypeString = Files.probeContentType(new File(fileName).toPath());
            if (fileTypeString.equals(MediaType.TEXT_PLAIN)) {
                String listOfFiles = uploadListedFiles(pm, fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * This method uploads list of files provided in the text file.
     *
     * @param fileName is the name of the text file that contains list of input files.
     * @return true if all files uploaded successfully.
     */
    @Override
    public String uploadListedFiles(ProgressMonitor pm, String fileName) {

        File file = new File(fileName);
        StringBuilder sb = new StringBuilder();
        Scanner input = null;
        try {
            input = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String nextFileName;
        boolean fileUploadSuccess = true;

        while (input.hasNext()) {
            nextFileName = input.nextLine();
            copyFile(nextFileName);
        }
        String fileNames = sb.toString();

        input.close();
        if (fileUploadSuccess) {
            return fileNames;
        } else {
            return null;
        }


    }

    private void copyFile(String sourceFilePath) {
        File sourceFile = new File(sourceFilePath);
        String targetFilePathName = workingDir + sourceFilePath.substring(sourceFilePath.lastIndexOf(File.separator) + 1);
        File targetFile = new File(targetFilePathName);
        targetFile.getParentFile().mkdirs();

        try {
            SeadasFileUtils.copyFileUsingStream(sourceFile, targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyMLPFiles(String sourceFilePath) {
        File sourceFile = new File(sourceFilePath);
        String targetFilePathName = workingDir + sourceFilePath.substring(sourceFilePath.lastIndexOf(File.separator) + 1);
        File targetFile = new File(targetFilePathName);
        targetFile.getParentFile().mkdirs();

        try {
            SeadasFileUtils.copyFileUsingStream(sourceFile, targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyOutputFiles(String outputFilesList) {

    }


    /**
     * this method returns a command array for execution.
     * the array is constructed using the paramList data and input/output files.
     * the command array structure is: full pathname of the program to be executed, input file name, params in the required order and finally the output file name.
     * assumption: order starts with 1
     *
     * @return
     */
    @Override
    public Process execute(ProcessorModel processorModel) {
        Process Process = new SeadasProcess(ocsswInfo, jobId);

        JsonObject commandArrayJsonObject = null;

        String programName = processorModel.getProgramName();

        if (processorModel.acceptsParFile() && programName.equals(MLP_PROGRAM_NAME)) {
            String parString = processorModel.getParamList().getParamString("\n");
            File parFile = writeMLPParFile(convertParStringForRemoteServer(parString));
            target.path("ocssw").path("uploadMLPParFile").path(jobId).request().put(Entity.entity(parFile, MediaType.APPLICATION_OCTET_STREAM_TYPE));
            //copyMLPFiles(parFile.getAbsolutePath());
            //target.path("ocssw").path("executeMLPParFile").path(jobId).request().get(String.class);
            JsonObject outputFilesList = target.path("ocssw").path("getMLPOutputFiles").path(jobId).request().get(JsonObject.class);
            downloadFiles(outputFilesList);
        } else {
            commandArrayJsonObject = getJsonFromParamList(processorModel.getParamList());
            Response response = target.path("ocssw").path("executeOcsswProgramOnDemand").path(jobId).path(programName).request().put(Entity.entity(commandArrayJsonObject, MediaType.APPLICATION_JSON_TYPE));
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                downloadFiles(commandArrayJsonObject);
            }
        }


        return Process;
    }


    //to do: change to copy files instead of uploading
    private void downloadFiles(JsonObject paramJsonObject) {
        Set commandArrayKeys = paramJsonObject.keySet();
        String param;
        String ofileFullPathName, ofileName;
        try {
            Object[] array = (Object[]) commandArrayKeys.toArray();
            int i = 0;
            String[] commandArray = new String[commandArrayKeys.size() + 1];
            commandArray[i++] = programName;
            for (Object element : array) {
                String elementName = (String) element;
                param = paramJsonObject.getString((String) element);
                if (elementName.contains("OFILE")) {
                    if (param.indexOf("=") != -1) {
                        StringTokenizer st = new StringTokenizer(param, "=");
                        String paramName = st.nextToken();
                        String paramValue = st.nextToken();
                        ofileFullPathName = paramValue;

                    } else {
                        ofileFullPathName = param;
                    }
                    ofileName = ofileFullPathName.substring(ofileFullPathName.lastIndexOf(File.separator) + 1);
                    Response response = target.path("fileServices").path("downloadFile").path(jobId).path(ofileName).request().get(Response.class);
                    InputStream responceStream = (InputStream) response.getEntity();
                    SeadasFileUtils.writeToFile(responceStream, ofileFullPathName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
