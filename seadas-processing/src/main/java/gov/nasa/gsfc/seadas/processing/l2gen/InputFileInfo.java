package gov.nasa.gsfc.seadas.processing.l2gen;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/17/12
 * Time: 11:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class InputFileInfo {

    private File file;
    private MissionInfo missionInfo = new MissionInfo();
    private FileLevelInfo.Level level;



    public InputFileInfo() {
        setFile(null);
    }

    public InputFileInfo(File file) {
        setFile(file);
    }


    public void setFile(File file) {
        if (file != null && file.getAbsolutePath().toString().length() == 0) {
            file = null;
        }
        this.file = file;

        // todo run script to get mission and level
        String tmp_level = "1B";
        String tmp_mission = "SeaWiFS";

        setLevel(FileLevelInfo.getLevel(tmp_level));
        setMissionInfo(file);
    }


    private void setMissionInfo(File file) {
        missionInfo.setName(file);
    }


    //-------------------------- Direct Get Methods ----------------------------

    public File getFile() {
        return file;
    }

    public MissionInfo getMissionInfo() {
        return missionInfo;
    }

    //-------------------------- Indirect Get Methods ----------------------------

    public boolean isFileExists() {
        if (file != null) {
            return file.exists();
        }
        return false;
    }

    public String getMission() {
        return missionInfo.getName();
    }

    public boolean isMission(String missionName) {
        return missionInfo.isName(missionName);
    }

    public File getOFile() {
        return FilenamePatterns.getOFile(file, missionInfo);
    }

    public String getOFileName() {
        return FilenamePatterns.getOFileName(file, missionInfo);
    }

    public boolean isRequiresGeofile() {
        return missionInfo.isRequiresGeofile();
    }

    public File getGeoFile() {
        return FilenamePatterns.getGeoFile(file, missionInfo);
    }

    public String getGeoFileName() {
        return FilenamePatterns.getGeoFileName(file, missionInfo);
    }

    public String getMissionDirectory() {
        return missionInfo.getDirectory();
    }

    public FileLevelInfo.Level getLevel() {
        return level;
    }

    public void setLevel(FileLevelInfo.Level level) {
        this.level = level;
    }

    public boolean isLevel(FileLevelInfo.Level level) {
        if (this.level != null && this.level == level) {
            return true;
        } else {
            return false;
        }
    }
}
