package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.OCSSW;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/16/12
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class MissionInfo {


    public static enum Id {
        AQUARIUS,
        CZCS,
        HICO,
        MERIS,
        MODISA,
        MODIST,
        MOS,
        OCTS,
        OSMI,
        SEAWIFS,
        VIIRS,
        OCM2,
        UNKNOWN
    }

    public final static Id[] SUPPORTED_IDS = {
            Id.AQUARIUS,
            Id.CZCS,
            Id.HICO,
            Id.MERIS,
            Id.MODISA,
            Id.MODIST,
            Id.MOS,
            Id.OCTS,
            Id.OSMI,
            Id.SEAWIFS,
            Id.VIIRS,
            Id.OCM2
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

    public final static String[] OCM2_NAMES = {"OCM2"};
    public final static String OCM2_DIRECTORY = "ocm2";

    public final static String[] HICO_NAMES = {"HICO"};
    public final static String HICO_DIRECTORY = "hico";

    private final HashMap<Id, String[]> names = new HashMap<Id, String[]>();
    private final HashMap<Id, String> directories = new HashMap<Id, String>();


    private Id id;

    private boolean geofileRequired;
    private File directory;


    public MissionInfo() {
        initDirectoriesHashMap();
        initNamesHashMap();
    }


    public MissionInfo(Id id) {
        this();
        setId(id);
    }


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
        directories.put(Id.OCM2, OCM2_DIRECTORY);
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
        names.put(Id.OCM2, OCM2_NAMES);
        names.put(Id.HICO, HICO_NAMES);
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
            for (String name : names.get(key))
                if (name.toLowerCase().equals(nameString.toLowerCase())) {
                    setId((Id) key);
                    return;
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
            try {
                setDirectory(new File(OCSSW.getOcsswDataRoot(), missionPiece));
            } catch (IOException e) {
                setDirectory(null);
            }
        } else {
            setDirectory(null);
        }
    }

    public File getDirectory() {
        return directory;
    }

    private void setDirectory(File directory) {
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

}
