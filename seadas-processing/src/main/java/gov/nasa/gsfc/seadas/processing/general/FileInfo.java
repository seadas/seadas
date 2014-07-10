package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.OCSSWRunner;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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


    public FileInfo(String defaultParent, String child) {
         this(defaultParent, child, DEFAULT_MISSION_AND_FILE_TYPE_ENABLED);
    }

    public FileInfo(String defaultParent, String child, boolean missionAndFileTypeEnabled) {

        this.missionAndFileTypeEnabled = missionAndFileTypeEnabled;
        file = SeadasFileUtils.createFile(defaultParent, child);
        if(file != null && file.exists()) {
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

        ProcessorModel processorModel = new ProcessorModel(FILE_INFO_SYSTEM_CALL);
        processorModel.setAcceptsParFile(false);
        processorModel.addParamInfo("file", file.getAbsolutePath(), ParamInfo.Type.IFILE, 0);
        processorModel.getParamInfo("file").setUsedAs(ParamInfo.USED_IN_COMMAND_AS_ARGUMENT);

        try {
            Process p = OCSSWRunner.execute(processorModel.getProgramCmdArray());
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = stdInput.readLine();
            if (line != null) {
                String splitLine[] = line.split(":");
                if (splitLine.length == 3) {
                    String missionName = splitLine[1].toString().trim();
                    String fileType = splitLine[2].toString().trim();

                    if (fileType.length() > 0) {
                        fileTypeInfo.setName(fileType);
                    }

                    if (missionName.length() > 0) {
                        missionInfo.setName(missionName);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR - Problem running " + FILE_INFO_SYSTEM_CALL);
            System.out.println(e.getMessage());
        }
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

    public boolean isMissionId(MissionInfo.Id missionId) {
        return missionInfo.isId(missionId);
    }

    public boolean isSupportedMission() {
        return missionInfo.isSupported();
    }


    public FileTypeInfo.Id getTypeId() {
        return fileTypeInfo.getId();
    }

    public String getTypeName() {
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
