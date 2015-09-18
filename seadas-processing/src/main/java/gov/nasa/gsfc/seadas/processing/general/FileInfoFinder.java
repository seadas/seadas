package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.processing.core.OCSSW;
import gov.nasa.gsfc.seadas.processing.core.OCSSWClient;
import gov.nasa.gsfc.seadas.processing.core.OCSSWRunner;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by aabduraz on 8/21/15.
 */
public class FileInfoFinder {

    private static final String FILE_INFO_SYSTEM_CALL = "get_obpg_file_type.py";

    private String fileType;
    private String missionName;
    private String missionDirName;

    public void computeFileInfo(ProcessorModel processorModel) {
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
                            setFileType(fileType);
                        }

                        if (missionName.length() > 0) {
                            setMissionName(missionName);
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
            String missionDirName = target.path("ocssw").path("retrieveMissionDirName").path(OCSSW.getJobId()).request(MediaType.TEXT_PLAIN_TYPE).get(String.class);
            if (fileType.length() > 0) {
                setFileType(fileType);
            }

            if (missionName.length() > 0) {
                setMissionName(missionName);
            }

            if (missionDirName.length() > 0) {
                setMissionDirName(missionDirName);
            }
        }

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
