package gov.nasa.gsfc.seadas.processing.common;

/**
 * Created by aabduraz on 6/16/17.
 */
public class Mission {

    private String missionName;
    private boolean isMissionExist;
    private String[] missionSuites;

    public Mission(String missionName){
        this.missionName = missionName;
    }


    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    public boolean isMissionExist() {
        return isMissionExist;
    }

    public void setMissionExist(boolean missionExist) {
        isMissionExist = missionExist;
    }

    public String[] getMissionSuites() {
        return missionSuites;
    }

    public void setMissionSuites(String[] missionSuites) {
        this.missionSuites = missionSuites;
    }
}
