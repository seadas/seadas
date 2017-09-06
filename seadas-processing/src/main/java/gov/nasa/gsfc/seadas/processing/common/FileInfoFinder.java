package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.OCSSWInfo;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.core.*;
import gov.nasa.gsfc.seadas.ocssw.*;

import java.util.HashMap;

/**
 * Created by aabduraz on 8/21/15.
 */
public class FileInfoFinder {

    public static final String FILE_INFO_SYSTEM_CALL = "get_obpg_file_type.py";
    public static final String FILE_TYPE_ID_STRING = "fileType";
    public static final String MISSION_NAME_ID_STRING = "missionName";
    public static final String MISSION_DIR_NAME_ID_STRING = "missionDirName";

    private String fileType;
    private String missionName;
    private String missionDirName;


    OCSSW ocssw;

    public FileInfoFinder(String fileName, OCSSW ocssw){
        this.ocssw = ocssw;
        //ocssw.setIfileName(fileName);
        ocssw.getOfileName(fileName);
        setMissionName(ocssw.getMissionName());
        setFileType(ocssw.getFileType());
        setMissionDirName(OCSSWInfo.getInstance().getOcsswDataDirPath());
//        fileInfoFinderProcessorModel = new ProcessorModel(FILE_INFO_SYSTEM_CALL, ocssw);
//        fileInfoFinderProcessorModel.setAcceptsParFile(false);
//        addParamInfo("file", fileName, ParamInfo.Type.IFILE, ParamInfo.USED_IN_COMMAND_AS_ARGUMENT, 0);
//        fileInfos = new HashMap();
    }

//    private void addParamInfo(String paramName, String paramValue, ParamInfo.Type paramType, String usedAs, int order){
//        ParamInfo paramInfo = new ParamInfo(paramName, paramValue, paramType);
//        paramInfo.setOrder(order);
//        paramInfo.setUsedAs(usedAs);
//        fileInfoFinderProcessorModel.addParamInfo(paramInfo);
//        identifyFileInfo();
//    }
//
//    private void identifyFileInfo(){
//        if (OCSSWOldModel.isLocal()) {
//            commandArrayManager = new LocalOcsswCommandArrayManager(fileInfoFinderProcessorModel);
//            //fileInfos = OCSSWRunnerOld.executeLocalGetOBPGFileInfo(commandArrayManager.getProgramCommandArray(), commandArrayManager.getIfileDir());
//        } else {
//            commandArrayManager = new RemoteOcsswCommandArrayManager(fileInfoFinderProcessorModel);
//            //fileInfos = OCSSWRunnerOld.executeRemoteGetOBPGFileInfo(commandArrayManager.getProgramCommandArray());
//        }
//        setFileType(fileInfos.get(FILE_TYPE_ID_STRING));
//        setMissionName(fileInfos.get(MISSION_NAME_ID_STRING));
//        setMissionDirName(fileInfos.get(MISSION_DIR_NAME_ID_STRING));
//    }
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    public String getMissionDirName() {
        return missionDirName;
    }

    public void setMissionDirName(String missionDirName) {
        this.missionDirName = missionDirName;
    }
}
