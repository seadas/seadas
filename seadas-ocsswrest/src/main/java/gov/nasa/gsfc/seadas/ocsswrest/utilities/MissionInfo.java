package gov.nasa.gsfc.seadas.ocsswrest.utilities;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 1/16/15
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */

public class MissionInfo {

    public HashMap<String, Boolean> missionDataStatus;

    public MissionInfo() {
        initDirectoriesHashMap();
        initNamesHashMap();
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

    public JsonObject getMissions() {

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

    public JsonObject getL2BinSuites(String missionName) {
        File missionDir = new File(OCSSWServerModel.missionDataDir + missionName);
        String[] suites = missionDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.contains("l2bin_defaults_");
            }
        });
        JsonObjectBuilder job = Json.createObjectBuilder();
        for (String str : suites) {
            job.add(str, true);
        }
        JsonObject missionSuites = job.build();
        return missionSuites;
    }

    public String findMissionId(String missionName) {
        Id missionId = Id.UNKNOWN;
        Iterator itr = names.keySet().iterator();

        while (itr.hasNext()) {
            Object key = itr.next();
            for (String name : names.get(key)) {
                if (name.toLowerCase().equals(missionName.toLowerCase())) {
                    setId((Id) key);
                    missionId = (Id) key;
                }
            }
        }
        return missionId.name();
    }

    public static enum Id {

        AQUARIUS,
        CZCS,
        HICO,
        GOCI,
        MERIS,
        MODISA,
        MODIST,
        MOS,
        OCTS,
        OSMI,
        SEAWIFS,
        VIIRS,
        OCM1,
        OCM2,
        OLI,
        UNKNOWN
    }

    public final static Id[] SUPPORTED_IDS = {
            Id.AQUARIUS,
            Id.CZCS,
            Id.HICO,
            Id.GOCI,
            Id.MERIS,
            Id.MODISA,
            Id.MODIST,
            Id.MOS,
            Id.OCTS,
            Id.OSMI,
            Id.SEAWIFS,
            Id.VIIRS,
            Id.OCM1,
            Id.OCM2,
            Id.OLI
    };

    public final static String[] SEAWIFS_NAMES = {"SeaWiFS"};
    public final static String SEAWIFS_DIRECTORY = "seawifs";

    public final static String[] MODISA_NAMES = {"MODIS Aqua", "Aqua", "MODISA"};
    public final static String MODISA_DIRECTORY = "hmodisa";

    public final static String[] MODIST_NAMES = {"MODIS Terra", "TERRA", "MODIST"};
    public final static String MODIST_DIRECTORY = "hmodist";

    public final static String[] VIIRS_NAMES = {"VIIRSN", "VIIRS"};
    public final static String VIIRS_DIRECTORY = "viirsn";

    public final static String[] MERIS_NAMES = {"MERIS"};
    public final static String MERIS_DIRECTORY = "meris";

    public final static String[] CZCS_NAMES = {"CZCS"};
    public final static String CZCS_DIRECTORY = "czcs";

    public final static String[] AQUARIUS_NAMES = {"AQUARIUS"};
    public final static String AQUARIUS_DIRECTORY = "aquarius";

    public final static String[] OCTS_NAMES = {"OCTS"};
    public final static String OCTS_DIRECTORY = "octs";

    public final static String[] OSMI_NAMES = {"OSMI"};
    public final static String OSMI_DIRECTORY = "osmi";

    public final static String[] MOS_NAMES = {"MOS"};
    public final static String MOS_DIRECTORY = "mos";

    public final static String[] OCM1_NAMES = {"OCM1"};
    public final static String OCM1_DIRECTORY = "ocm1";

    public final static String[] OCM2_NAMES = {"OCM2"};
    public final static String OCM2_DIRECTORY = "ocm2";

    public final static String[] HICO_NAMES = {"HICO"};
    public final static String HICO_DIRECTORY = "hico";

    public final static String[] GOCI_NAMES = {"GOCI"};
    public final static String GOCI_DIRECTORY = "goci";

    public final static String[] OLI_NAMES = {"OLI"};
    public final static String OLI_DIRECTORY = "oli";

    private final HashMap<Id, String[]> names = new HashMap<>();
    private final HashMap<Id, String> directories = new HashMap<>();

    private Id id;

    private boolean geofileRequired;
    private File directory;

    private String[] suites;

    public void clear() {
        id = null;
        geofileRequired = false;
        directory = null;
    }

    private void initDirectoriesHashMap() {
        directories.put(Id.SEAWIFS, SEAWIFS_DIRECTORY);
        directories.put(Id.MODISA, MODISA_DIRECTORY);
        directories.put(Id.MODIST, MODIST_DIRECTORY);
        directories.put(Id.VIIRS, VIIRS_DIRECTORY);
        directories.put(Id.MERIS, MERIS_DIRECTORY);
        directories.put(Id.CZCS, CZCS_DIRECTORY);
        directories.put(Id.AQUARIUS, AQUARIUS_DIRECTORY);
        directories.put(Id.OCTS, OCTS_DIRECTORY);
        directories.put(Id.OSMI, OSMI_DIRECTORY);
        directories.put(Id.MOS, MOS_DIRECTORY);
        directories.put(Id.OCM1, OCM1_DIRECTORY);
        directories.put(Id.OCM2, OCM2_DIRECTORY);
        directories.put(Id.HICO, HICO_DIRECTORY);
        directories.put(Id.GOCI, GOCI_DIRECTORY);
        directories.put(Id.OLI, OLI_DIRECTORY);
    }

    private void initNamesHashMap() {
        names.put(Id.SEAWIFS, SEAWIFS_NAMES);
        names.put(Id.MODISA, MODISA_NAMES);
        names.put(Id.MODIST, MODIST_NAMES);
        names.put(Id.VIIRS, VIIRS_NAMES);
        names.put(Id.MERIS, MERIS_NAMES);
        names.put(Id.CZCS, CZCS_NAMES);
        names.put(Id.AQUARIUS, AQUARIUS_NAMES);
        names.put(Id.OCTS, OCTS_NAMES);
        names.put(Id.OSMI, OSMI_NAMES);
        names.put(Id.MOS, MOS_NAMES);
        names.put(Id.OCM1, OCM1_NAMES);
        names.put(Id.OCM2, OCM2_NAMES);
        names.put(Id.HICO, HICO_NAMES);
        names.put(Id.GOCI, GOCI_NAMES);
        names.put(Id.OLI, OLI_NAMES);
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        clear();
        this.id = id;
        if (isSupported()) {
            setRequiresGeofile();
            setMissionDirectoryName();
        }
    }

    public boolean isId(Id id) {
        if (id == getId()) {
            return true;
        } else {
            return false;
        }
    }

    public void setName(String nameString) {
        if (nameString == null) {
            setId(null);
            return;
        }

        Iterator itr = names.keySet().iterator();

        while (itr.hasNext()) {
            Object key = itr.next();
            for (String name : names.get(key)) {
                if (name.toLowerCase().equals(nameString.toLowerCase())) {
                    setId((Id) key);
                    return;
                }
            }
        }

        setId(Id.UNKNOWN);
        return;
    }

    public String getName() {
        if (id == null) {
            return null;
        }

        if (names.containsKey(id)) {
            return names.get(id)[0];
        } else {
            return null;
        }
    }

    private void setRequiresGeofile() {
        if (id == null) {
            return;
        }

        if (isId(Id.MODISA) || isId(Id.MODIST) || isId(Id.VIIRS)) {
            setGeofileRequired(true);
        } else {
            setGeofileRequired(false);
        }
    }

    public boolean isGeofileRequired() {
        return geofileRequired;
    }

    private void setGeofileRequired(boolean geofileRequired) {
        this.geofileRequired = geofileRequired;
    }

    private void setMissionDirectoryName() {
        if (directories.containsKey(id)) {
            String missionPiece = directories.get(id);
            setDirectory(new File(OCSSWServerModel.missionDataDir, missionPiece));
        } else {
            setDirectory(null);
        }
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public boolean isSupported() {
        if (id == null) {
            return false;
        }
        for (Id supportedId : SUPPORTED_IDS) {
            if (supportedId == this.id) {
                return true;
            }
        }
        return false;
    }

    public String[] getSuites() {
        return suites;
    }

    public void setSuites(String[] suites) {
        this.suites = suites;
    }
}
