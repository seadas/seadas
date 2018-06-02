package gov.nasa.gsfc.seadas.ocssw;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.runtime.RuntimeContext;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import gov.nasa.gsfc.seadas.processing.common.SeadasFileUtils;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import gov.nasa.gsfc.seadas.processing.utilities.FileCompare;

import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.esa.snap.rcp.SnapApp;

/**
 * Created by aabduraz on 3/27/17.
 */
public class OCSSWVM extends OCSSWRemote {
    public final static String OCSSW_VM_SERVER_SHARED_DIR_PROPERTY = "ocssw.sharedDir";
    public final static String OCSSW_VM_SERVER_SHARED_DIR_PROPERTY_DEFAULT_VALUE = System.getProperty("user.home") + File.separator + "ocsswVMServerSharedDir";

    String workingDir;


    public OCSSWVM() {
        super();
        workingDir = RuntimeContext.getConfig().getContextProperty(OCSSW_VM_SERVER_SHARED_DIR_PROPERTY, OCSSW_VM_SERVER_SHARED_DIR_PROPERTY_DEFAULT_VALUE);
    }

    /**
     * This method copies the client file into the shared directory between the host and the virtual machine.
     * The shared directory is specified in the seadas.config file.
     *
     * @param sourceFilePathName
     * @return
     */
    @Override

    public boolean uploadClientFile(String sourceFilePathName) {
        ifileUploadSuccess = false;
        if (!isAncFile(sourceFilePathName) && !fileExistsOnServer(sourceFilePathName)) {
            if (needToUplaodFileContent(programName, sourceFilePathName)) {
                uploadListedFiles(null, sourceFilePathName);
                updateFileListFileContent(sourceFilePathName);
            }
            copyFileC2S(sourceFilePathName);
        }
        ifileUploadSuccess = true;
        return ifileUploadSuccess;
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
            if (!fileExistsOnServer(nextFileName)) {
                copyFileC2S(nextFileName);
            }
        }
        String fileNames = sb.toString();

        input.close();
        if (fileUploadSuccess) {
            return fileNames;
        } else {
            return null;
        }


    }


    @Override
    public void updateFileListFileContent(String fileListFileName) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileListFileName), StandardCharsets.UTF_8);
            Iterator<String> itr = lines.iterator();
            String fileName;
            while (itr.hasNext()) {
                fileName = itr.next();
                if (fileName.trim().length() > 0) {
                    System.out.println("file name in the file list: " + fileName);
                    fileName = workingDir + File.separator + fileName.substring(fileName.lastIndexOf(File.separator) + 1);
                    stringBuilder.append(fileName + "\n");
                }
            }
            String fileContent = stringBuilder.toString();
            System.out.println(fileContent);
            SeadasFileUtils.writeStringToFile(fileContent, fileListFileName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void copyFileC2S(String sourceFilePathName) {
        String targetFilePathName = workingDir + File.separator + sourceFilePathName.substring(sourceFilePathName.lastIndexOf(File.separator) + 1);
        try {
            //SeadasFileUtils.copyFile(sourceFilePathName, targetFilePathName);
            SeadasFileUtils.cloFileCopy(sourceFilePathName, targetFilePathName);
            ifileUploadSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean fileExistsOnServer(String fileName) {
        String sourceFileDir = fileName.substring(0, fileName.lastIndexOf(File.separator));
        String fileNameOnServer = workingDir + File.separator + fileName.substring(fileName.lastIndexOf(File.separator) + 1);
        if (sourceFileDir.equals(workingDir) || new File(fileNameOnServer).exists()) {
            ifileUploadSuccess = true;
            return true;
        } else {
            return false;
        }
    }

    private boolean compareFileContents(String file1Path, String file2Path) {
        String file1HashValue;
        String file2HashValue;
        boolean isTwoEqual = false;
        try {
            System.out.println("two file is equal: " + isTwoEqual);
            file1HashValue = FileCompare.MD5HashFile(file1Path);
            file2HashValue = FileCompare.MD5HashFile(file2Path);
            isTwoEqual = file1HashValue.equals(file2HashValue);
        } catch (Exception ioe) {
            System.out.println("exception in comparing two files");
            ioe.printStackTrace();
        }
        return isTwoEqual;
    }

    private void copyFileS2C(String fileName) {
        String sourceFilePathName = workingDir + File.separator + fileName.substring(fileName.lastIndexOf(File.separator) + 1);
        try {
            SeadasFileUtils.cloFileCopy(sourceFilePathName, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyMLPFiles(String sourceFilePath) {
        File sourceFile = new File(sourceFilePath);
        String targetFilePathName = workingDir + File.separator + sourceFilePath.substring(sourceFilePath.lastIndexOf(File.separator) + 1);
        File targetFile = new File(targetFilePathName);
        targetFile.getParentFile().mkdirs();

        try {
            SeadasFileUtils.copyFileUsingStream(sourceFile, targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFileFromServerToClient(String sourceFilePathName, String targetFilePathName) {
        File sourceFile = new File(sourceFilePathName);
        File targetFile = new File(targetFilePathName);
        targetFile.getParentFile().mkdirs();
        try {
            SeadasFileUtils.copyFileUsingStream(sourceFile, targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getOutputFiles(String outputFileNames) {

        SnapApp snapApp = SnapApp.getDefault();

        ProgressMonitorSwingWorker pmSwingWorker = new ProgressMonitorSwingWorker(snapApp.getMainFrame(),
                "OCSSW Remote Server File Download") {

            @Override
            protected Void doInBackground(ProgressMonitor pm) throws Exception {

                JsonObject commandArrayJsonObject = null;

                StringTokenizer st = new StringTokenizer(outputFileNames, "\n");
                String fileName;
                while (st.hasMoreTokens()) {
                    fileName = st.nextToken();
                    copyFileS2C(fileName);
                }
                return null;
            }
        };
        pmSwingWorker.execute();
    }


    @Override
    public boolean downloadCommonFiles(JsonObject paramJsonObject) {
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
                    if (!workingDir.equals(ofileDir)) {
                        String sourceFilePathName = workingDir + File.separator + ofileName;
                        String targetFilePathName = ofileDir + File.separator + ofileName;
                        SeadasFileUtils.cloFileCopy(sourceFilePathName, targetFilePathName);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void downloadMLPOutputFiles(ProcessorModel processorModel) {

        String mlpWorkingDir =  workingDir + File.separator + MLP_OUTPUT_DIR_NAME;
        String mlpOdir = processorModel.getParamValue(MLP_PAR_FILE_ODIR_KEY_NAME);
        final String ofileDir = isMLPOdirValid(mlpOdir) ?  mlpOdir : ifileDir;

        if (!mlpWorkingDir.equals(ofileDir)) {

            SnapApp snapApp = SnapApp.getDefault();

            ProgressMonitorSwingWorker pmSwingWorker = new ProgressMonitorSwingWorker(snapApp.getMainFrame(),
                    "OCSSW Remote Server File Upload") {

                @Override
                protected Void doInBackground(ProgressMonitor pm) throws Exception {

                    JsonObject mlpOfilesJsonObject = target.path("fileServices").path("getMLPOutputFilesList").path(jobId).request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
                    Set fileSetKeys = mlpOfilesJsonObject.keySet();
                    Object[] fileArray = (Object[]) fileSetKeys.toArray();

                    pm.beginTask("Copying output files from the shared folder " + workingDir + " to " + ofileDir, fileArray.length);
                    pm.worked(1);

                    String ofileName, ofileFullPathName;
                    int numberOfTasksWorked = 1;

                    String sourceFilePathName, targetFilePathName;
                    for (Object fileNameKey : fileArray) {
                        ofileName = mlpOfilesJsonObject.getString((String) fileNameKey);

                        pm.setSubTaskName("Copying file '" + ofileName + " to " + ofileDir);
                        sourceFilePathName = mlpWorkingDir + File.separator + ofileName;
                        targetFilePathName = ofileDir + File.separator + ofileName;
                        SeadasFileUtils.cloFileCopy(sourceFilePathName, targetFilePathName);
                        pm.worked(numberOfTasksWorked++);
                    }
                    return null;
                }
            };
            pmSwingWorker.executeWithBlocking();
        }
    }


}
