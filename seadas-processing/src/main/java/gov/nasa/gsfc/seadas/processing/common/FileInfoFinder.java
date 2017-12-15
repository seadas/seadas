package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.OCSSWInfo;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;

/**
 * Created by aabduraz on 8/21/15.
 */
public class FileInfoFinder {

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
