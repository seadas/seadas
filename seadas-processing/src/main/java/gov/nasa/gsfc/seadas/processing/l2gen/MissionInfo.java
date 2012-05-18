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

    public static String SeaWiFS = "SeaWiFS";
    public static String SeaWiFS_DIRECTORY = "seawifs";

    public static String MODISA = "MODIS Aqua";
    public static String MODISA_DIRECTORY = "hmodisa";

    public static String MODIST = "MODIS Terra";
    public static String MODIST_DIRECTORY = "hmodist";

    public static String VIIRS = "VIIRSN";
    public static String VIIRS_DIRECTORY = "viirsn";

    public static String MERIS = "MERIS";
    public static String MERIS_DIRECTORY = "meris";

    public static String CZCS = "CZCS";
    public static String CZCS_DIRECTORY = "czcs";

    public static String AQUARIUS = "AQUARIUS";
    public static String AQUARIUS_DIRECTORY = "aquarius";

    public static String OCTS = "OCTS";
    public static String OCTS_DIRECTORY = "octs";


    public static String NULL_STRING = "";


    private HashMap<Id, String> nameLookup = new HashMap();
    private HashMap<Id, String> directoryLookup = new HashMap();


    private Id id = Id.UNKNOWN;
    private boolean geofileRequired = false;
    private String directory = NULL_STRING;


    public MissionInfo() {
        initDirectoryLookup();
        initNameLookup();
    }


    public void clear() {
        id = Id.UNKNOWN;
        geofileRequired = false;
        directory = NULL_STRING;

    }

    private void initDirectoryLookup() {
        directoryLookup.put(Id.SEAWIFS, SeaWiFS_DIRECTORY);
        directoryLookup.put(Id.MODISA, MODISA_DIRECTORY);
        directoryLookup.put(Id.MODIST, MODIST_DIRECTORY);
        directoryLookup.put(Id.VIIRS, VIIRS_DIRECTORY);
        directoryLookup.put(Id.MERIS, MERIS_DIRECTORY);
        directoryLookup.put(Id.CZCS, CZCS_DIRECTORY);
        directoryLookup.put(Id.AQUARIUS, AQUARIUS_DIRECTORY);
        directoryLookup.put(Id.OCTS, OCTS_DIRECTORY);
    }


    private void initNameLookup() {
        nameLookup.put(Id.SEAWIFS, SeaWiFS);
        nameLookup.put(Id.MODISA, MODISA);
        nameLookup.put(Id.MODIST, MODIST);
        nameLookup.put(Id.VIIRS, VIIRS);
        nameLookup.put(Id.MERIS, MERIS);
        nameLookup.put(Id.CZCS, CZCS);
        nameLookup.put(Id.AQUARIUS, AQUARIUS);
        nameLookup.put(Id.OCTS, OCTS);
    }


    public Id getId() {
        return id;
    }

    public void setId(Id id) {
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
            setId(Id.UNKNOWN);
            return;
        }

        Iterator itr = nameLookup.keySet().iterator();

        while (itr.hasNext()) {
            Object key = itr.next();
            if (nameLookup.get(key).toLowerCase().equals(nameString.toLowerCase())) {
                setId((Id) key);
                return;
            }
        }

        setId(Id.UNKNOWN);
        return;
    }


    public boolean isName(String name) {
        if (this.id != null && this.id.equals(name)) {
            return true;
        } else {
            return false;
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
            final StringBuilder directory = new StringBuilder("");
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


}
