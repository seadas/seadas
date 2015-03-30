package gov.nasa.gsfc.seadas.ocsswrest.utilities;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.File;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 1/16/15
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */

public class MissionInfo {

    public  HashMap<String, Boolean> missionDataStatus;

    public MissionInfo() {
        updateMissionStatus();
    }

    void updateMissionStatus() {
        missionDataStatus = new HashMap<String, Boolean>();
        missionDataStatus.put("SEAWIFS", new File(OCSSWServerModel.missionDataDir + "seawifs").exists());
        missionDataStatus.put("AQUA", new File(OCSSWServerModel.missionDataDir + "modisa").exists());
        missionDataStatus.put("TERRA", new File(OCSSWServerModel.missionDataDir + "modist").exists());
        missionDataStatus.put("VIIRSN", new File(OCSSWServerModel.missionDataDir + "viirsn").exists());
        missionDataStatus.put("MERIS", new File(OCSSWServerModel.missionDataDir + "meris").exists());
        missionDataStatus.put("CZCS", new File(OCSSWServerModel.missionDataDir + "czcs").exists());
        missionDataStatus.put("AQUARIUS", new File(OCSSWServerModel.missionDataDir + "aquarius").exists());
        missionDataStatus.put("OCTS", new File(OCSSWServerModel.missionDataDir + "octs").exists());
        missionDataStatus.put("OLI", new File(OCSSWServerModel.missionDataDir + "oli").exists());
        missionDataStatus.put("OSMI", new File(OCSSWServerModel.missionDataDir + "osmi").exists());
        missionDataStatus.put("MOS", new File(OCSSWServerModel.missionDataDir + "mos").exists());
        missionDataStatus.put("OCM2", new File(OCSSWServerModel.missionDataDir + "ocm2").exists());
        missionDataStatus.put("OCM1", new File(OCSSWServerModel.missionDataDir + "ocm1").exists());
        missionDataStatus.put("AVHRR", new File(OCSSWServerModel.missionDataDir + "avhrr").exists());
        missionDataStatus.put("HICO", new File(OCSSWServerModel.missionDataDir + "hico").exists());
        missionDataStatus.put("GOCI", new File(OCSSWServerModel.missionDataDir + "goci").exists());

        System.out.println("aqua status: " + new File(OCSSWServerModel.missionDataDir + "modisa").exists());
    }

    public JsonObject getMissions(){

        JsonObject missions = Json.createObjectBuilder().add("SEAWIFS", new File(OCSSWServerModel.missionDataDir + "seawifs").exists())
                                                        .add("AQUA", new File(OCSSWServerModel.missionDataDir + "modisa").exists())
                                                        .add("TERRA", new File(OCSSWServerModel.missionDataDir + "modist").exists())
                                                        .add("VIIRSN", new File(OCSSWServerModel.missionDataDir + "viirsn").exists())
                                                        .add("MERIS", new File(OCSSWServerModel.missionDataDir + "meris").exists())
                                                        .add("CZCS", new File(OCSSWServerModel.missionDataDir + "czcs").exists())
                                                        .add("AQUARIUS", new File(OCSSWServerModel.missionDataDir + "aquarius").exists())
                                                        .add("OCTS", new File(OCSSWServerModel.missionDataDir + "octs").exists())
                                                        .add("OLI", new File(OCSSWServerModel.missionDataDir + "oli").exists())
                                                        .add("OSMI", new File(OCSSWServerModel.missionDataDir + "osmi").exists())
                                                        .add("MOS", new File(OCSSWServerModel.missionDataDir + "mos").exists())
                                                        .add("OCM2", new File(OCSSWServerModel.missionDataDir + "ocm2").exists())
                                                        .add("OCM1", new File(OCSSWServerModel.missionDataDir + "ocm1").exists())
                                                        .add("AVHRR", new File(OCSSWServerModel.missionDataDir + "avhrr").exists())
                                                        .add("HICO", new File(OCSSWServerModel.missionDataDir + "hico").exists())
                                                        .add("GOCI", new File(OCSSWServerModel.missionDataDir + "goci").exists())
                                                        .build();
        System.out.println("aqua status: " + new File(OCSSWServerModel.missionDataDir + "modisa").exists());
        return missions;

    }
}
