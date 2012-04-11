package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.l2gen.ParamInfo;
import org.esa.beam.visat.VisatApp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 3/16/12
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessorModel {

    private String programName;
    private String programLocation;
    private ArrayList<ParamInfo> paramList;
    private boolean acceptsParFile;
    private String[] processorEnv;
    private String errorMessage;
    private File programRoot;
    private File inputFile;
    private File outputFile;
    private String outputFileName;
    private File outputFileDir;
    private File parFile;
    private String parString;


    public ProcessorModel(String name) {
        this(name, null);
    }

    public ProcessorModel(String name, String parXMLFileName) {
        this.programName = name;
        computeProcessorEnv();
        if (parXMLFileName != null) {
            paramList = ParamUtils.computeParamList(parXMLFileName);
            acceptsParFile = ParamUtils.getParFilePreference(parXMLFileName);

        }

    }


    public boolean isValidProcessor() {
        return programLocation != null;
    }

    public String getProgramName() {
        return programName;
    }


    public String getProgramLocation() {
        return programLocation;
    }

    public File getProgramRoot() {
        return programRoot;
    }

    public ArrayList getProgramParamList() {

        return paramList;
    }

    public void setParFile(File parFile) {
        this.parFile = parFile;
    }

    public File getParFile() {
        return parFile;
    }

    public void setParString(String parString){
        this.parString = parString;

    }

    public void setAcceptsParFile(boolean acceptsParFile) {
        this.acceptsParFile = acceptsParFile;
    }

    public boolean acceptsParFile() {
        return acceptsParFile;
    }

    public String getProgramErrorMessage() {
        return errorMessage;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
        outputFileDir = inputFile.getParentFile();
        System.out.println(this.inputFile.toString() + " ~~~~~~~~ " + outputFileDir.toString());
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void setOutputFileDir(File outputFileDir) {
        if (outputFileDir != null) {
            this.outputFileDir = outputFileDir;
        }
    }

    public File getOutputFile() {
        if (outputFile == null) {
            if (outputFileName == null) {
                //default output file name
                outputFile = new File(outputFileDir, programName + "-out-" + Long.toHexString(System.nanoTime()));
            } else {
                //construct output file  based on output dir and file name
                outputFile = new File(outputFileDir, outputFileName);
            }
        }
        return outputFile;
    }

    public void updateParamInfo(ParamInfo currentOption, String newValue) {
        Iterator<ParamInfo> itr = paramList.iterator();
        ParamInfo option;
        while (itr.hasNext()) {

            option = itr.next();
            if (option.getName().equals(currentOption.getName())) {
                option.setValue(newValue);
            }
        }


    }

    private void computeProcessorEnv() {

        final File ocsswRoot;
        try {
            ocsswRoot = OCSSW.getOcsswRoot();
        } catch (IOException e) {
            errorMessage = e.getMessage();
            VisatApp.getApp().showErrorDialog(programName, e.getMessage());
            return;
        }

        final String ocsswArch;
        try {
            ocsswArch = OCSSW.getOcsswArch();
        } catch (IOException e) {
            errorMessage = e.getMessage();
            VisatApp.getApp().showErrorDialog(programName, e.getMessage());
            return;
        }

        final String location = ocsswRoot.getPath() + "/run/bin/" + ocsswArch + "/";

        final String[] envp = {
                "OCSSWROOT=" + ocsswRoot.getPath(),
                "OCSSW_ARCH=" + ocsswArch,
                "OCDATAROOT=" + ocsswRoot.getPath() + "/run/data",
        };

        processorEnv = envp;
        this.programLocation = location;
        this.programRoot = ocsswRoot;
    }


    private String[] getCmdArrayWithParFile() {
        final String[] cmdArray = {
                programLocation + programName,
                "ifile=" + inputFile,
                "ofile=" + outputFile,
                "par=" + parFile
        };

        for (int i = 0; i < cmdArray.length; i++) {
            System.out.println("i = " + i + " " + cmdArray[i]);
        }

        return cmdArray;
    }

    private String[] getCmdArrayWithArguments() {
        final String[] cmdArray = new String[paramList.size() + 1];
        cmdArray[0] = programLocation + programName;


        Iterator itr = paramList.iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();
            cmdArray[option.getOrder()] = option.getValue();
            System.out.println("order: " + option.getOrder() + "   value = " + option.getValue());
        }

        cmdArray[1] = inputFile.toString();
        cmdArray[cmdArray.length - 1] = outputFile.toString();

        for (int i = 0; i < cmdArray.length; i++) {
            System.out.println("i = " + i + " " + cmdArray[i]);
        }
        return cmdArray;
    }

    /**
     * this method returns a command array for execution.
     * the array is constructed using the paramList data and input/output files.
     * the command array structure is: full pathname of the program to be executed, input file name, params in the required order and finally the output file name.
     * assumption: order starts with 1
     *
     * @return
     */
    public String[] getProgramCmdArray() {

        if (acceptsParFile) {
            return getCmdArrayWithParFile();
        } else {

            return getCmdArrayWithArguments();
        }
    }

    public String[] getProgramEnv() {
        return processorEnv;

    }

    private void createParFile(File outputDir, final String parameterText) {
        try {
            final File tempFile = File.createTempFile(programName, ".par", outputDir);
            tempFile.deleteOnExit();
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(tempFile);
                fileWriter.write(parameterText);
            } finally {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }

//                   if (tempFile == null) {
//           JOptionPane.showMessageDialog(this.getClass().ge  ,
//                   "Unable to create parameter file '" + parFile + "'.",
//                   "Error",
//                   JOptionPane.ERROR_MESSAGE);
//            return;
//        }
            parFile = tempFile;
        } catch (IOException e) {
            return;
        }
    }

    public Process executeProcess() throws IOException {

        System.out.println("executing par file for l2gen ...");

        return Runtime.getRuntime().exec(getProgramCmdArray(), getProgramEnv(), getProgramRoot());

    }

    private void computeParFile() {
//                      final String[] cmdarray = {
//                        ocsswRoot.getPath() + "/run/bin/" + ocsswArch + "/" + programName,
//                        "ifile=" + inputFile,
//                        "ofile=" + outputFile,
//                        "par=" + parFile
//                };
    }
}
