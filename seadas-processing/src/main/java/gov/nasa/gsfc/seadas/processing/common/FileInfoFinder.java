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
        ocssw.getOfileName(fileName);
        setMissionName(ocssw.getMissionName());
        setFileType(ocssw.getFileType());
        setMissionDirName(OCSSWInfo.getInstance().getOcsswDataDirPath());
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
