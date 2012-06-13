package gov.nasa.gsfc.seadas.processing.general;

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
public class FileInfoNew {

    private File file;

    private static final String FILE_INFO_SYSTEM_CALL = "get_obpg_file_type.py";

    private final MissionInfo missionInfo = new MissionInfo();
    private final FileTypeInfo fileTypeInfo = new FileTypeInfo();


    public FileInfoNew(String defaultParent, String child) {
        defaultParent.trim();
        child.trim();

        String filename = null;

        if (isAbsolute(child)) {
            filename = child;
        } else if (isAbsolute(defaultParent)) {
            filename = defaultParent + System.getProperty("file.separator") + filename;
        }

        if (filename != null) {
            file = new File(filename);
            if (new File(filename).exists()) {
                init();
            }
        }
    }

    public FileInfoNew(String filename) {
        filename.trim();

        if (filename != null) {
            file = new File(filename);
            if (new File(filename).exists()) {
                init();
            }
        }
    }

    private boolean isAbsolute(String filename) {
        if (filename.indexOf(System.getProperty("file.separator")) == 0) {
            return true;
        } else {
            return false;
        }
    }


    public void clear() {

        missionInfo.clear();
        fileTypeInfo.clear();
    }

    public void init() {
        clear();

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


    //-------------------------- Indirect Get Methods ----------------------------


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


    public boolean isGeofileRequired() {
        return missionInfo.isGeofileRequired();
    }


    public File getFile() {
        return file;
    }
}
