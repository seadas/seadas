package gov.nasa.gsfc.seadas.processing.l2gen;

import gov.nasa.gsfc.seadas.processing.general.ProcessorModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/17/12
 * Time: 11:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileInfo {

    private static final String FILE_INFO_SYSTEM_CALL = "get_obpg_file_type.py";

    private final MissionInfo missionInfo = new MissionInfo();
    private final FileTypeInfo fileTypeInfo = new FileTypeInfo();

    private File file;


    public FileInfo() {
    }

    public FileInfo(File file) {
        this();
        setFile(file);
    }

    public void clear() {
        file = null;
        missionInfo.clear();
        fileTypeInfo.clear();
    }

    public void setFile(File file) {
        clear();

        if (file == null || file.getAbsolutePath().toString().length() == 0) {
            return;
        }

        this.file = file;

        ProcessorModel processorModel = new ProcessorModel(FILE_INFO_SYSTEM_CALL);
        processorModel.setAcceptsParFile(false);
        processorModel.addParamInfo("file", file.getAbsolutePath(), 1);

        try {
            Process p = processorModel.executeProcess();
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


    //-------------------------- Direct Get Methods ----------------------------

    public File getFile() {
        return file;
    }


    public MissionInfo getMissionInfo() {
        return missionInfo;
    }

    public String getFileName() {
        if (file != null) {
            return file.getAbsoluteFile().toString();
        } else {
            return null;
        }
    }

    //-------------------------- Indirect Get Methods ----------------------------

    public boolean isFileExists() {
        if (file != null) {
            return file.exists();
        }
        return false;
    }

    public boolean isFileAbsolute() {
        if (file != null) {
            return file.isAbsolute();
        }
        return false;
    }

    public File getParentFile() {
        if (file != null) {
            return file.getParentFile();
        }

        return null;
    }


    public MissionInfo.Id getMissionId() {
        return missionInfo.getId();
    }

    public String getMissionName() {
        return missionInfo.getName();
    }

    public String getMissionDirectory() {
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


    public FileInfo getOFileInfo() {
        return FilenamePatterns.getOFileInfo(this);
    }

    public File getOFile() {
        return getOFileInfo().getFile();
    }

    public String getOFileName() {
        return getOFileInfo().getFileName();
    }


    public boolean isGeofileRequired() {
        return missionInfo.isGeofileRequired();
    }


    public FileInfo getGeoFileInfo() {
        return FilenamePatterns.getGeoFileInfo(this);
    }

    public File getGeoFile() {
        return getGeoFileInfo().getFile();
    }

    public String getGeoFileName() {
        return getGeoFileInfo().getFileName();
    }


}
