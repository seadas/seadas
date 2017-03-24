package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.processing.core.*;

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

    private String fileName;
    OcsswCommandArrayManager commandArrayManager;

    ProcessorModel fileInfoFinderProcessorModel;
    HashMap<String, String> fileInfos;

    public FileInfoFinder(String fileName){
        this.fileName = fileName;
        fileInfoFinderProcessorModel = new ProcessorModel(FILE_INFO_SYSTEM_CALL);
        fileInfoFinderProcessorModel.setAcceptsParFile(false);
        addParamInfo("file", fileName, ParamInfo.Type.IFILE, ParamInfo.USED_IN_COMMAND_AS_ARGUMENT, 0);
        fileInfos = new HashMap();
    }

    private void addParamInfo(String paramName, String paramValue, ParamInfo.Type paramType, String usedAs, int order){
        ParamInfo paramInfo = new ParamInfo(paramName, paramValue, paramType);
        paramInfo.setOrder(order);
        paramInfo.setUsedAs(usedAs);
        fileInfoFinderProcessorModel.addParamInfo(paramInfo);
        identifyFileInfo();
    }

    private void identifyFileInfo(){
        if (OCSSWOldModel.isLocal()) {
            commandArrayManager = new LocalOcsswCommandArrayManager(fileInfoFinderProcessorModel);
            fileInfos = OCSSWRunner.executeLocalGetOBPGFileInfo(commandArrayManager.getProgramCommandArray(), commandArrayManager.getIfileDir());
        } else {
            commandArrayManager = new RemoteOcsswCommandArrayManager(fileInfoFinderProcessorModel);
            fileInfos = OCSSWRunner.executeRemoteGetOBPGFileInfo(commandArrayManager.getProgramCommandArray());
        }
        setFileType(fileInfos.get(FILE_TYPE_ID_STRING));
        setMissionName(fileInfos.get(MISSION_NAME_ID_STRING));
        setMissionDirName(fileInfos.get(MISSION_DIR_NAME_ID_STRING));
    }
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
