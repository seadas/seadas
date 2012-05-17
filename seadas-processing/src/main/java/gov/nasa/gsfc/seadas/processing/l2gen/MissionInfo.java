package gov.nasa.gsfc.seadas.processing.l2gen;

import java.io.File;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/16/12
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class MissionInfo {

    private static final String OCDATAROOT = System.getenv("OCDATAROOT");


    public static String SeaWiFS = "SeaWiFS";
    public static String SeaWiFS_DIRECTORY = "seawifs";

    public static String MODISA = "MODISA";
    public static String MODISA_DIRECTORY = "hmodisa";

    public static String MODIST = "MODIST";
    public static String MODIST_DIRECTORY = "hmodist";

    public static String VIIRS = "VIIRS";
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


    private HashMap<String, String> directoryLookup = new HashMap();


    private String name = NULL_STRING;
    private boolean requiresGeofile = false;
    private String directory = NULL_STRING;


    public MissionInfo() {
        initDirectoryLookup();
    }

    public MissionInfo(File iFile) {
        initDirectoryLookup();

        if (iFile != null) {
            setName(iFile);
        }
    }


    private void initDirectoryLookup() {
        directoryLookup.put(SeaWiFS, SeaWiFS_DIRECTORY);
        directoryLookup.put(MODISA, MODISA_DIRECTORY);
        directoryLookup.put(MODIST, MODIST_DIRECTORY);
        directoryLookup.put(VIIRS, VIIRS_DIRECTORY);
        directoryLookup.put(MERIS, MERIS_DIRECTORY);
        directoryLookup.put(CZCS, CZCS_DIRECTORY);
        directoryLookup.put(AQUARIUS, AQUARIUS_DIRECTORY);
        directoryLookup.put(OCTS, OCTS_DIRECTORY);
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        if (name == null) {
            this.name = NULL_STRING;
        }
        this.name = name;
    }

    public boolean isName(String name) {
        if (this.name != null && this.name.equals(name)) {
            return true;
        } else {
            return false;
        }
    }

    public void setName(File iFile) {

        if (iFile != null && iFile.exists()) {
            String ifile = iFile.getName().toString();
            String VIIRS_IFILE_PREFIX = "SVM01";
            String MODISA_IFILE_PREFIX = "A";
            String MODIST_IFILE_PREFIX = "T";

            if (ifile.toUpperCase().startsWith(VIIRS_IFILE_PREFIX)) {
                this.setName(VIIRS);
            } else if (ifile.toUpperCase().startsWith(MODISA_IFILE_PREFIX)) {
                this.setName(MODISA);
            } else if (ifile.toUpperCase().startsWith(MODIST_IFILE_PREFIX)) {
                this.setName(MODIST);
            } else {
                this.setName(NULL_STRING);
            }

        } else {
            this.setName(NULL_STRING);
        }

        setRequiresGeofile();
        setMissionDirectoryName();
    }


    private void setRequiresGeofile() {

        if (isName(MODISA) || isName(MODIST) || isName(VIIRS)) {
            setRequiresGeofile(true);
        } else {
            setRequiresGeofile(false);
        }
    }


    public boolean isRequiresGeofile() {
        return requiresGeofile;
    }

    private void setRequiresGeofile(boolean requiresGeofile) {
        this.requiresGeofile = requiresGeofile;
    }


    private void setMissionDirectoryName() {

        if (directoryLookup.containsKey(name)) {
            String missionPiece = directoryLookup.get(name);

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
