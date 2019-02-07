package gov.nasa.gsfc.seadas.ocsswrest.utilities;

import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWServerModel;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Created by IntelliJ IDEA. User: knowles Date: 5/16/12 Time: 3:13 PM To change
 * this template use File | Settings | File Templates.
 */
public class MissionInfo {

    public enum Id {
        AQUARIUS,
        AVHRR,
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
        VIIRSN,
        VIIRSJ1,
        OCM1,
        OCM2,
        OLI,
        OLCI,
        UNKNOWN
    }

    public final static String[] SEAWIFS_NAMES = {"SeaWiFS"};
    public final static String SEAWIFS_DIRECTORY = "seawifs";

    public final static String[] MODISA_NAMES = {"MODIS Aqua", "Aqua", "MODISA"};
    public final static String MODISA_DIRECTORY = "modis/aqua";

    public final static String[] MODIST_NAMES = {"MODIS Terra", "TERRA", "MODIST"};
    public final static String MODIST_DIRECTORY = "modis/terra";

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

    public final static String[] AVHRR_NAMES = {"AVHRR"};
    public final static String AVHRR_DIRECTORY = "avhrr";

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

    public final static String[] OLCI_NAMES = {"OLCI"};
    public final static String OLCI_DIRECTORY = "olci";

    private final HashMap<Id, String[]> names = new HashMap<>();
    private final HashMap<Id, String> directories = new HashMap<>();

    private Id id;

    private boolean geofileRequired;
    private File directory;
    private File subsensorDirectory;

    public MissionInfo() {
        initDirectoriesHashMap();
        initNamesHashMap();
    }

    public MissionInfo(Id id) {
        this();
        setId(id);
    }

    public MissionInfo(String name) {
        this();
        setName(name);
    }

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
        directories.put(Id.AVHRR, AVHRR_DIRECTORY);
        directories.put(Id.OCTS, OCTS_DIRECTORY);
        directories.put(Id.OSMI, OSMI_DIRECTORY);
        directories.put(Id.MOS, MOS_DIRECTORY);
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
        names.put(Id.AVHRR, AVHRR_NAMES);
        names.put(Id.OCTS, OCTS_NAMES);
        names.put(Id.OSMI, OSMI_NAMES);
        names.put(Id.MOS, MOS_NAMES);
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

    public File getDirectory() {
        return directory;
    }

    private void setDirectory(File directory) {
        this.directory = directory;
    }

    public File getSubsensorDirectory() {
        return subsensorDirectory;
    }

    private void setSubsensorDirectory(File directory) {
        this.subsensorDirectory = directory;
    }

    public boolean isSupported() {
        if (id == null) {
            return false;
        }
        return id != Id.UNKNOWN;
    }

}
