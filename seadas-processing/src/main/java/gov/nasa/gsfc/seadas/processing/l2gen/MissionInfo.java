package gov.nasa.gsfc.seadas.processing.l2gen;

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

    private static final String OCDATAROOT = System.getenv("OCDATAROOT");


    public static enum Id {
        SEAWIFS,
        MODISA,
        MODIST,
        VIIRS,
        MERIS,
        CZCS,
        AQUARIUS,
        OCTS,
        UNKNOWN
    }

    public final static Id[] SUPPORTED_IDS = {
            Id.SEAWIFS,
            Id.MODISA,
            Id.MODIST,
            Id.VIIRS,
            Id.MERIS,
            Id.CZCS,
            Id.AQUARIUS,
            Id.OCTS};


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


    private final HashMap<Id, String[]> namesLookup = new HashMap<Id, String[]>();
    private final HashMap<Id, String> directoryLookup = new HashMap<Id, String>();


    private Id id;

    private boolean geofileRequired;
    private String directory;


    public MissionInfo() {
        initDirectoryLookup();
        initNameLookup();
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

    private void initDirectoryLookup() {
        directoryLookup.put(Id.SEAWIFS, SEAWIFS_DIRECTORY);
        directoryLookup.put(Id.MODISA, MODISA_DIRECTORY);
        directoryLookup.put(Id.MODIST, MODIST_DIRECTORY);
        directoryLookup.put(Id.VIIRS, VIIRS_DIRECTORY);
        directoryLookup.put(Id.MERIS, MERIS_DIRECTORY);
        directoryLookup.put(Id.CZCS, CZCS_DIRECTORY);
        directoryLookup.put(Id.AQUARIUS, AQUARIUS_DIRECTORY);
        directoryLookup.put(Id.OCTS, OCTS_DIRECTORY);
    }


    private void initNameLookup() {
        namesLookup.put(Id.SEAWIFS, SEAWIFS_NAMES);
        namesLookup.put(Id.MODISA, MODISA_NAMES);
        namesLookup.put(Id.MODIST, MODIST_NAMES);
        namesLookup.put(Id.VIIRS, VIIRS_NAMES);
        namesLookup.put(Id.MERIS, MERIS_NAMES);
        namesLookup.put(Id.CZCS, CZCS_NAMES);
        namesLookup.put(Id.AQUARIUS, AQUARIUS_NAMES);
        namesLookup.put(Id.OCTS, OCTS_NAMES);
    }


    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        clear();
        this.id = id;
        setRequiresGeofile();
        setMissionDirectoryName();
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

        Iterator itr = namesLookup.keySet().iterator();

        while (itr.hasNext()) {
            Object key = itr.next();
            for (String name : namesLookup.get(key))
                if (name.toLowerCase().equals(nameString.toLowerCase())) {
                    setId((Id) key);
                    return;
                }
        }

        setId(Id.UNKNOWN);
        return;
    }

    public String getNameString() {
        if (id == null) {
            return null;
        }

        if (namesLookup.containsKey(id)) {
            return namesLookup.get(id)[0];
        } else {
            return namesLookup.get(Id.UNKNOWN)[0];
        }
    }


    private void setRequiresGeofile() {

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

        if (directoryLookup.containsKey(id)) {
            String missionPiece = directoryLookup.get(id);

            // determine the filename which contains the wavelengths
            final StringBuilder directory = new StringBuilder();
            directory.append(OCDATAROOT);
            directory.append("/");
            directory.append(missionPiece);

            setDirectory(directory.toString());
        } else {
            setDirectory(null);
        }
    }

    public String getDirectory() {
        return directory;
    }

    private void setDirectory(String directory) {
        this.directory = directory;
    }

    public boolean isSupported() {
        for (Id id : SUPPORTED_IDS) {
            if (id == this.id) {
                return true;
            }
        }

        return false;
    }
}
