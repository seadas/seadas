package gov.nasa.gsfc.seadas.processing.core.ocssw;

/**
 * Created by aabduraz on 3/27/17.
 */
public abstract class OCSSWDataInfo {
    private String filetype;
    private String missionInfo;
    private String nextLevelFileName;

    public abstract String getFiletype();

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public abstract String getMissionInfo() ;

    public void setMissionInfo(String missionInfo) {
        this.missionInfo = missionInfo;
    }

    public abstract String getNextLevelFileName() ;

    public void setNextLevelFileName(String nextLevelFileName) {
        this.nextLevelFileName = nextLevelFileName;
    }
}
