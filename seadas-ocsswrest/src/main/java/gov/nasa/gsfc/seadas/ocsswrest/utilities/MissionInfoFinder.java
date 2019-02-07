package gov.nasa.gsfc.seadas.ocsswrest.utilities;

import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWServerModel;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import static gov.nasa.gsfc.seadas.ocsswrest.utilities.ServerSideFileUtilities.debug;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 1/16/15
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */

public class MissionInfoFinder {

    public HashMap<String, Boolean> missionDataStatus;
    private static final String DEFAULTS_FILE_PREFIX = "msl12_defaults_",
            AQUARIUS_DEFAULTS_FILE_PREFIX = "l2gen_aquarius_defaults_",
            L3GEN_DEFAULTS_FILE_PREFIX = "msl12_defaults_";
    private String defaultsFilePrefix;

    private final static String L2GEN_PROGRAM_NAME = "l2gen",
            AQUARIUS_PROGRAM_NAME = "l2gen_aquarius",
            L3GEN_PROGRAM_NAME = "l3gen";

    public enum MissionNames {
        AQUARIUS("AQUARIUS"),
        AVHRR("AVHRR"),
        CZCS("CZCS"),
        GOCI("GOCI"),
        HICO("HICO"),
        MERIS("MERIS"),
        AQUA("AQUA"),
        TERRA("TERRA"),
        MOS("MOS"),
        MSI("MSI"),
        OCM1("OCM1"),
        OCM2("OCM2"),
        OCTS("OCTS"),
        OLI("OLI"),
        OLCI("OLCI"),
        OSMI("OSMI"),
        SEAWIFS("SEAWIFS"),
        VIIRSN("VIIRSN"),
        VIIRSJ1("VIIRSJ1"),
        UNKNOWN("UNKNOWN");

        String missionName;

        MissionNames(String fieldName) {
            this.missionName = fieldName;
        }

        public String getMissionName() {
            return missionName;
        }

    }

    public enum MissionDirs {
        AQUARIUS("aquarius"),
        AVHRR("avhrr"),
        CZCS("czcs"),
        GOCI("goci"),
        HICO("hico"),
        MERIS("meris"),
        AQUA("modis/aqua"),
        TERRA("modis/terra"),
        MOS("mos"),
        MSI("msi"),
        OCM1("ocm1"),
        OCM2("ocm2"),
        OCTS("octs"),
        OLI("oli"),
        OLCI("olci"),
        OSMI("osmi"),
        SEAWIFS("seawifs"),
        VIIRSN("viirs/npp"),
        VIIRSJ1("viirs/j1"),
        UNKNOWN("unknown");

        String missionDir;

        MissionDirs(String fieldName) {
            this.missionDir = fieldName;
        }

        public String getMissionDir() {
            return missionDir;
        }

    }

    public MissionInfoFinder() {
        initDirectoriesHashMap();
        initNamesHashMap();
        updateMissionStatus();
    }

    void updateMissionStatus() {
        missionDataStatus = new HashMap<String, Boolean>();
        try {
            for (MissionNames missionName : MissionNames.values()) {
                missionDataStatus.put(missionName.getMissionName(), new File(OCSSWServerModel.getOcsswDataDirPath() + File.separator + MissionDirs.valueOf(missionName.getMissionName()).getMissionDir()).exists());
                //System.out.println(MissionDirs.valueOf(missionName.getMissionName()) + " status: " + OCSSWServerModel.getOcsswDataDirPath() + File.separator + MissionDirs.valueOf(missionName.getMissionName()).getMissionDir());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public JsonObject getMissions() {

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        for (MissionNames  missionName : MissionNames.values()) {
            jsonObjectBuilder.add(missionName.getMissionName(), new File(OCSSWServerModel.getOcsswDataDirPath() + File.separator + MissionDirs.valueOf(missionName.getMissionName()).getMissionDir()).exists());
            //System.out.println(MissionDirs.valueOf(missionName.getMissionName()) + " status: " + new File(OCSSWServerModel.getOcsswDataDirPath() + File.separator + MissionDirs.valueOf(missionName.getMissionName()).getMissionDir()).exists());
        }
        return jsonObjectBuilder.build();

    }

    public JsonObject getL2BinSuites(String missionName) {
        setName(missionName);
        //File missionDir = new File(OCSSWServerModel.getOcsswDataDirPath() + File.separator + getDirectory());
        String[] suites = directory.list(new FilenameFilter() {
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

    public String[] getMissionSuiteList(String missionName, String mode) {
        setName(missionName);

        //File missionDir = new File(OCSSWServerModel.getOcsswDataDirPath() + File.separator + missionName);

        File missionDir = directory;

        System.out.println("mission directory: " + missionDir);

        if (missionDir.exists()) {

            ArrayList<String> suitesArrayList = new ArrayList<String>();

            File missionDirectoryFiles[] = missionDir.listFiles();

            for (File file : missionDirectoryFiles) {
                String filename = file.getName();

                debug("fileName: " + filename);

                if (filename.startsWith(getDefaultsFilePrefix(mode)) && filename.endsWith(".par")) {
                    String filenameTrimmed = filename.replaceFirst(getDefaultsFilePrefix(mode), "");
                    filenameTrimmed = filenameTrimmed.replaceAll("[\\.][p][a][r]$", "");
                    suitesArrayList.add(filenameTrimmed);
                }
            }

            final String[] suitesArray = new String[suitesArrayList.size()];

            int i = 0;
            for (String suite : suitesArrayList) {
                suitesArray[i] = suite;
                debug("suite " + suite);
                i++;
            }

            java.util.Arrays.sort(suitesArray);
            if(suitesArray.length > 0) {
                debug("mission suite: " + suitesArray[0] + suitesArray[1] + suitesArray);
            }

            return suitesArray;

        } else {
            return null;
        }
    }

    public String getDefaultsFilePrefix(String programName) {

        defaultsFilePrefix = DEFAULTS_FILE_PREFIX;

        if (programName.equals(L3GEN_PROGRAM_NAME)) {
            defaultsFilePrefix = L3GEN_DEFAULTS_FILE_PREFIX;
        } else if (programName.equals(AQUARIUS_PROGRAM_NAME)) {
            defaultsFilePrefix = AQUARIUS_DEFAULTS_FILE_PREFIX;
        }
        return defaultsFilePrefix;
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

    public enum Id {

        AQUARIUS,
        CZCS,
        HICO,
        GOCI,
        MERIS,
        MODISA,
        MODIST,
        MOS,
        MSI,
        OCTS,
        OSMI,
        SEAWIFS,
        VIIRSN,
        VIIRSJ1,
        OCM1,
        OCM2,
        OLI,
        OLCI,
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
            Id.MSI,
            Id.OCTS,
            Id.OSMI,
            Id.SEAWIFS,
            Id.VIIRSN,
            Id.VIIRSJ1,
            Id.OCM1,
            Id.OCM2,
            Id.OLI,
            Id.OLCI
    };

    public final static String[] SEAWIFS_NAMES = {"SeaWiFS"};
    public final static String SEAWIFS_DIRECTORY = "seawifs";

    public final static String[] MODISA_NAMES = {"MODIS Aqua", "Aqua", "MODISA"};
    public final static String MODISA_DIRECTORY = "modis/aqua";

    public final static String[] MODIST_NAMES = {"MODIS Terra", "TERRA", "MODIST"};
    public final static String MODIST_DIRECTORY = "modis/terrra";

    public final static String[] VIIRSN_NAMES = {"VIIRSN", "VIIRS"};
    public final static String VIIRSN_DIRECTORY = "viirs/npp";

    public final static String[] VIIRSJ1_NAMES = {"VIIRSJ1"};
    public final static String VIIRSJ1_DIRECTORY = "viirs/j1";

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

    public final static String[] MSI_NAMES = {"MSI"};
    public final static String MSI_DIRECTORY = "msi";

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

    public final static String[] OLCI_NAMES = {"OLCI"};
    public final static String OLCI_DIRECTORY = "olci";

    private final HashMap<Id, String[]> names = new HashMap<>();
    private final HashMap<Id, String> directories = new HashMap<>();

    private Id id;

    private boolean geofileRequired;
    private File directory;
    private File subsensorDirectory;


    private String[] suites;

    public void clear() {
        id = null;
        geofileRequired = false;
        directory = null;
        subsensorDirectory = null;
    }

    private void initDirectoriesHashMap() {
        directories.put(Id.SEAWIFS, SEAWIFS_DIRECTORY);
        directories.put(Id.MODISA, MODISA_DIRECTORY);
        directories.put(Id.MODIST, MODIST_DIRECTORY);
        directories.put(Id.VIIRSN, VIIRSN_DIRECTORY);
        directories.put(Id.VIIRSJ1, VIIRSJ1_DIRECTORY);
        directories.put(Id.MERIS, MERIS_DIRECTORY);
        directories.put(Id.CZCS, CZCS_DIRECTORY);
        directories.put(Id.AQUARIUS, AQUARIUS_DIRECTORY);
        directories.put(Id.OCTS, OCTS_DIRECTORY);
        directories.put(Id.OSMI, OSMI_DIRECTORY);
        directories.put(Id.MOS, MOS_DIRECTORY);
        directories.put(Id.MSI, MSI_DIRECTORY);
        directories.put(Id.OCM1, OCM1_DIRECTORY);
        directories.put(Id.OCM2, OCM2_DIRECTORY);
        directories.put(Id.HICO, HICO_DIRECTORY);
        directories.put(Id.GOCI, GOCI_DIRECTORY);
        directories.put(Id.OLI, OLI_DIRECTORY);
        directories.put(Id.OLCI, OLCI_DIRECTORY);
    }

    private void initNamesHashMap() {
        names.put(Id.SEAWIFS, SEAWIFS_NAMES);
        names.put(Id.MODISA, MODISA_NAMES);
        names.put(Id.MODIST, MODIST_NAMES);
        names.put(Id.VIIRSN, VIIRSN_NAMES);
        names.put(Id.VIIRSJ1, VIIRSJ1_NAMES);
        names.put(Id.MERIS, MERIS_NAMES);
        names.put(Id.CZCS, CZCS_NAMES);
        names.put(Id.AQUARIUS, AQUARIUS_NAMES);
        names.put(Id.OCTS, OCTS_NAMES);
        names.put(Id.OSMI, OSMI_NAMES);
        names.put(Id.MOS, MOS_NAMES);
        names.put(Id.MSI, MSI_NAMES);
        names.put(Id.OCM1, OCM1_NAMES);
        names.put(Id.OCM2, OCM2_NAMES);
        names.put(Id.HICO, HICO_NAMES);
        names.put(Id.GOCI, GOCI_NAMES);
        names.put(Id.OLI, OLI_NAMES);
        names.put(Id.OLCI, OLCI_NAMES);
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
        return id == getId();
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

        if (isId(Id.MODISA) || isId(Id.MODIST) || isId(Id.VIIRSN) || isId(Id.VIIRSJ1)) {
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
            String[] words = missionPiece.split("/");
            try {
                if(words.length == 2) {
                    setDirectory(new File(OCSSWServerModel.getOcsswDataDirPath(), words[0]));
                    setSubsensorDirectory(new File(OCSSWServerModel.getOcsswDataDirPath(), missionPiece));
                    return;
                } else {
                    setDirectory(new File(OCSSWServerModel.getOcsswDataDirPath(), missionPiece));
                    setSubsensorDirectory(null);
                    return;
                }
            } catch (Exception e) { }
        }

        setDirectory(null);
        setSubsensorDirectory(null);
    }

//    private void setMissionDirectoryName() {
//        if (directories.containsKey(id)) {
//            String missionPiece = directories.get(id);
//            setDirectory(new File(OCSSWServerModel.getOcsswDataDirPath(), missionPiece));
//        } else {
//            setDirectory(null);
//        }
//    }

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

    public File getSubsensorDirectory() {
        return subsensorDirectory;
    }

    private void setSubsensorDirectory(File directory) {
        this.subsensorDirectory = directory;
    }

}
