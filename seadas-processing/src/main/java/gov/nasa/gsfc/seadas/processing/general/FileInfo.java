package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.processing.core.*;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;

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

        ProcessorModel processorModel = new ProcessorModel(FILE_INFO_SYSTEM_CALL);
        processorModel.setAcceptsParFile(false);
        processorModel.addParamInfo("file", file.getAbsolutePath(), ParamInfo.Type.IFILE, 0);
        processorModel.getParamInfo("file").setUsedAs(ParamInfo.USED_IN_COMMAND_AS_ARGUMENT);

        if (RuntimeContext.getConfig().getContextProperty(OCSSW.OCSSW_LOCATION_PROPERTY).equals(OCSSW.SEADAS_OCSSW_LOCATION_LOCAL)) {
            try {

                //TODO execute this from remote server
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
        } else {
            OCSSWClient ocsswClient = new OCSSWClient();
            WebTarget target = ocsswClient.getOcsswWebTarget();
            JsonArrayBuilder jab = Json.createArrayBuilder();
            for (String s : processorModel.getProgramCmdArray()) {
                jab.add(s);
            }
            JsonArray remoteCmdArray = jab.build();

            Response response = target.path("ocssw").path("findIFileTypeAndMissionName").path(OCSSW.getJobId()).request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(remoteCmdArray, MediaType.APPLICATION_JSON_TYPE));

            String fileType = target.path("ocssw").path("retrieveIFileType").path(OCSSW.getJobId()).request(MediaType.TEXT_PLAIN_TYPE).get(String.class);
            String missionName = target.path("ocssw").path("retrieveMissionName").path(OCSSW.getJobId()).request(MediaType.TEXT_PLAIN_TYPE).get(String.class);
            if (fileType.length() > 0) {
                fileTypeInfo.setName(fileType);
            }

            if (missionName.length() > 0) {
                missionInfo.setName(missionName);
            }
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
