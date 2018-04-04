package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.ocssw.OCSSW;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/13/12
 * Time: 4:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileInfo {

    private File file;

    private static final String FILE_INFO_SYSTEM_CALL = "get_obpg_file_type.py";

    private static final boolean DEFAULT_MISSION_AND_FILE_TYPE_ENABLED = true;

    private final MissionInfo missionInfo = new MissionInfo();
    private final FileTypeInfo fileTypeInfo = new FileTypeInfo();
    private boolean missionAndFileTypeEnabled = DEFAULT_MISSION_AND_FILE_TYPE_ENABLED;
    OCSSW ocssw;

    public FileInfo(String defaultParent, String child, OCSSW ocssw) {
        this(defaultParent, child, DEFAULT_MISSION_AND_FILE_TYPE_ENABLED, ocssw);
    }

    public FileInfo(String defaultParent, String child, boolean missionAndFileTypeEnabled, OCSSW ocssw) {
        this.ocssw = ocssw;
        this.missionAndFileTypeEnabled = missionAndFileTypeEnabled;
        file = SeadasFileUtils.createFile(defaultParent, child);
        if (file != null && file.exists()) {
            initMissionAndFileTypeInfos();
        }
    }

    public FileInfo(String filename) {
        if (filename != null) {
            file = new File(filename);
            if (file.exists()) {
                initMissionAndFileTypeInfos();
            }
        }
    }

    public void clear() {
        file = null;
        missionInfo.clear();
        fileTypeInfo.clear();
    }

    private void initMissionAndFileTypeInfos() {
        FileInfoFinder fileInfoFinder = new FileInfoFinder(file.getAbsolutePath(), ocssw);
        fileTypeInfo.setName(fileInfoFinder.getFileType());
        missionInfo.setName(fileInfoFinder.getMissionName());
    }


    //-------------------------- Indirect Get Methods ----------------------------


    public MissionInfo.Id getMissionId() {
        return missionInfo.getId();
    }

    public String getMissionName() {
        return missionInfo.getName();
    }

    public File getMissionDirectory() {
        return missionInfo.getDirectory();
    }

    public File getSubsensorDirectory() {
        return missionInfo.getSubsensorDirectory();
    }

    public boolean isMissionDirExist() {
        return ocssw.isMissionDirExist(getMissionName());
    }

    public boolean isMissionId(MissionInfo.Id missionId) {
        return missionInfo.isId(missionId);
    }

    public boolean isSupportedMission() {
        return missionInfo.isSupported();
    }


    public FileTypeInfo.Id getTypeId() {
        return fileTypeInfo.getId();
    }

    public String getFileTypeName() {
        return fileTypeInfo.getName();
    }

    public boolean isTypeId(FileTypeInfo.Id type) {
        return fileTypeInfo.isId(type);
    }


    public boolean isGeofileRequired() {
        return missionInfo.isGeofileRequired();
    }


    public File getFile() {
        return file;
    }

    public boolean isMissionAndFileTypeEnabled() {
        return missionAndFileTypeEnabled;
    }

    public void setMissionAndFileTypeEnabled(boolean missionAndFileTypeEnabled) {
        this.missionAndFileTypeEnabled = missionAndFileTypeEnabled;
    }
}
