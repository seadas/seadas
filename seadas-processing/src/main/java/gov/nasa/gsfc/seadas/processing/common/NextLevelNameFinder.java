package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.core.*;
import gov.nasa.gsfc.seadas.ocssw.*;

/**
 * Created by aabduraz on 6/28/16.
 */
public class NextLevelNameFinder {

    public static String NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME = "next_level_name.py";
    public static String NEXT_LEVEL_FILE_NAME_TOKEN = "Output Name:";
    ProcessorModel nextLevelNamerProcessorModel;

    String ifileName;
    String programName;
    private String ofileName;

    OcsswCommandArrayManager commandArrayManager;

    /**
     *This class encapsulates the next_level_name.py program. The following is the command line usage.
     * Usage: next_level_name.py INPUT_FILE TARGET_PROGRAM

     Options:
     --version             show program's version number and exit
     -h, --help            show this help message and exit
     --oformat=OFORMAT     output format
     --resolution=RESOLUTION
     resolution for smigen/l3mapgen
     --suite=SUITE         data type suite

     The sought output file name is displayed on the standard output.

     * @param ifileName is the INPUT_FILE argument on the command line
     * @param programName is the TARGET_OUTPUT on the command line
     */

    NextLevelNameFinder(String ifileName, String programName, OCSSW ocssw){
        this.ifileName = ifileName;
        this.programName= programName;
        this.setOfileName("output");
        nextLevelNamerProcessorModel = new ProcessorModel(NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME, ocssw);
        addIfileParamInfo();
        addProgramNameParamInfo();
    }

    public String getNextLevelFileName(){
        if (OCSSWOldModel.isLocal()) {
            commandArrayManager = new LocalOcsswCommandArrayManager(nextLevelNamerProcessorModel);
            ofileName = OCSSWRunnerOld.executeLocalNameFinder(commandArrayManager.getProgramCommandArray(), commandArrayManager.getIfileDir());
        } else {
            commandArrayManager = new RemoteOcsswCommandArrayManager(nextLevelNamerProcessorModel);
            ofileName = OCSSWRunnerOld.executeRemoteNameFinder(commandArrayManager.getProgramCommandArray());
        }
        return ofileName;
    }

    public void addParamInfo(String paramName, String paramValue, ParamInfo.Type paramType, String usedAs){
        addParamInfo(paramName, paramValue, paramType, usedAs, nextLevelNamerProcessorModel.getParamList().getParamArray().size());
    }

    private void addParamInfo(String paramName, String paramValue, ParamInfo.Type paramType, String usedAs, int order){
        ParamInfo paramInfo = new ParamInfo(paramName, paramValue, paramType);
        paramInfo.setOrder(order);
        paramInfo.setUsedAs(usedAs);
        nextLevelNamerProcessorModel.addParamInfo(paramInfo);
    }

    /**
     * Creates a ParamInfo object for the "INPUT_FILE" argument
     */
    private void addIfileParamInfo(){
        addParamInfo("ifile", ifileName, ParamInfo.Type.IFILE, ParamInfo.USED_IN_COMMAND_AS_ARGUMENT, 0);
    }

    /**
     * Creates a ParamInfo object for the "TARGET_PROGRAM" argument
     */
    private void addProgramNameParamInfo(){
        addParamInfo("proramName", programName, ParamInfo.Type.STRING, ParamInfo.USED_IN_COMMAND_AS_ARGUMENT, 1);
    }

    public String getOfileName() {
        return ofileName;
    }

    public void setOfileName(String ofileName) {
        this.ofileName = ofileName;
    }

}
