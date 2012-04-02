package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.l2gen.ParamInfo;
import org.esa.beam.visat.VisatApp;

import java.io.File;
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
    private File outputFileDir;

    public ProcessorModel(String name, String paramXmlFileName) {
        this.programName = name;
        computeProcessorEnv();
        paramList = ParamUtils.computeParamList(paramXmlFileName);
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

    public void setAcceptsParFile(boolean acceptsParFile) {
        this.acceptsParFile = acceptsParFile;
    }

    public boolean acceptsParFile() {
        return acceptsParFile;
    }

    public String getProgramErrorMessage() {
        return errorMessage;
    }

    public void setInputFile(File inputFile ) {
        this.inputFile = inputFile;
    }

    public File getInputFile() {
        return inputFile;
    }
    public void setOutputFile(File outputFile ) {
        this.outputFile = outputFile;
    }
    public void setOutputFileDir(File outputFileDir ) {
        this.outputFileDir = outputFileDir;
        outputFile = new File(outputFileDir, programName + "-out-" + Long.toHexString(System.nanoTime()));
    }
    public File getOutputFile() {
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

    /**
     * this method returns a command array for execution.
     * the array is constructed using the paramList data and input/output files.
     * the command array structure is: full pathname of the program to be executed, input file name, params in the required order and finally the output file name.
     * assumption: order starts with 1
     * @return
     */
    public String[] getProgramCmdArray() {

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
        cmdArray[cmdArray.length -1] = outputFile.toString();
//        final String[] cmdarray = {
//                programLocation + programName,
//                inputFile.toString(),
//                "100",
//                "300",
//                "1",
//                "21",
//                outputFile.toString()
//                //"par=" + parameterFile
//        };

        return cmdArray;
    }

    public String[] getProgramEnv() {
        return processorEnv;

    }

    private void computeParFile() {

    }
}
