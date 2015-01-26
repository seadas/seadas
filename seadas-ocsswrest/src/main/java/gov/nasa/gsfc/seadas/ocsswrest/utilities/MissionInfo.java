package gov.nasa.gsfc.seadas.ocsswrest.utilities;

import gov.nasa.gsfc.seadas.ocsswrest.OCSSWServices;

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

    public static final String missionDataDir = OCSSWServices.OCSSW_DIR + System.getProperty("file.separator") + "run"
                + System.getProperty("file.separator") + "data"
                + System.getProperty("file.separator");

    public  HashMap<String, Boolean> missionDataStatus;

    public MissionInfo() {
        updateMissionStatus();
    }

    void updateMissionStatus() {
        missionDataStatus = new HashMap<String, Boolean>();
        missionDataStatus.put("SEAWIFS", new File(missionDataDir + "seawifs").exists());
        missionDataStatus.put("AQUA", new File(missionDataDir + "modisa").exists());
        missionDataStatus.put("TERRA", new File(missionDataDir + "modist").exists());
        missionDataStatus.put("VIIRSN", new File(missionDataDir + "viirsn").exists());
        missionDataStatus.put("MERIS", new File(missionDataDir + "meris").exists());
        missionDataStatus.put("CZCS", new File(missionDataDir + "czcs").exists());
        missionDataStatus.put("AQUARIUS", new File(missionDataDir + "aquarius").exists());
        missionDataStatus.put("OCTS", new File(missionDataDir + "octs").exists());
        missionDataStatus.put("OSMI", new File(missionDataDir + "osmi").exists());
        missionDataStatus.put("MOS", new File(missionDataDir + "mos").exists());
        missionDataStatus.put("OCM2", new File(missionDataDir + "ocm2").exists());
        missionDataStatus.put("OCM1", new File(missionDataDir + "ocm1").exists());
        missionDataStatus.put("AVHRR", new File(missionDataDir + "avhrr").exists());
        missionDataStatus.put("HICO", new File(missionDataDir + "hico").exists());
        missionDataStatus.put("GOCI", new File(missionDataDir + "goci").exists());
    }

    public JsonObject getMissions(){

        JsonObject missions = Json.createObjectBuilder().add("SEAWIFS", new File(missionDataDir + "seawifs").exists())
                                                        .add("AQUA", new File(missionDataDir + "modisa").exists())
                                                        .add("TERRA", new File(missionDataDir + "modist").exists())
                                                        .add("VIIRSN", new File(missionDataDir + "viirsn").exists())
                                                        .add("MERIS", new File(missionDataDir + "meris").exists())
                                                        .add("CZCS", new File(missionDataDir + "czcs").exists())
                                                        .add("AQUARIUS", new File(missionDataDir + "aquarius").exists())
                                                        .add("OCTS", new File(missionDataDir + "octs").exists())
                                                        .add("OSMI", new File(missionDataDir + "osmi").exists())
                                                        .add("MOS", new File(missionDataDir + "mos").exists())
                                                        .add("OCM2", new File(missionDataDir + "ocm2").exists())
                                                        .add("OCM1", new File(missionDataDir + "ocm1").exists())
                                                        .add("AVHRR", new File(missionDataDir + "avhrr").exists())
                                                        .add("HICO", new File(missionDataDir + "hico").exists())
                                                        .add("GOCI", new File(missionDataDir + "goci").exists())
                                                        .build();
        return missions;

    }
}
