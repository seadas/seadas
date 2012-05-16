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
public class SeadasMission {

    private static final String OCDATAROOT = System.getenv("OCDATAROOT");


    public static enum Mission {
        NCEP,
        ADTOMS,
        EPTOMS,
        TOVS,
        OISST,
        SeaWiFS,
        MODISA,
        MODIST,
        OCTS,
        TOAST,
        CZCS,
        Glory,
        N7TOMS,
        VIIRS,
        Aquarius,
        GOME,
        SCIA,
        OMI,
        MERIS,
        SSMI,
        OCM2,
        CDDIS,
        UNKNOWN,
        NULL
    }

    private Mission mission = Mission.NULL;
    private boolean requiresGeofile = false;
    private String directory = null;

    public SeadasMission() {

    }

    public SeadasMission(File iFile) {
        setMission(iFile);
    }


    public void setMission(File iFile) {

        if (iFile != null && iFile.exists()) {
            String ifile = iFile.getName().toString();
            String VIIRS_IFILE_PREFIX = "SVM01";
            String MODISA_IFILE_PREFIX = "A";
            String MODIST_IFILE_PREFIX = "T";

            if (ifile.toUpperCase().startsWith(VIIRS_IFILE_PREFIX)) {
                this.setMission(Mission.VIIRS);
            } else if (ifile.toUpperCase().startsWith(MODISA_IFILE_PREFIX)) {
                this.setMission(Mission.MODISA);
            } else if (ifile.toUpperCase().startsWith(MODIST_IFILE_PREFIX)) {
                this.setMission(Mission.MODIST);
            } else {
                this.setMission(Mission.UNKNOWN);
            }

        } else {
            this.setMission(Mission.NULL);
        }

        setRequiresGeofile();
        setMissionDirectoryName();
    }


    public Mission getMission() {
        return mission;
    }

    private void setMission(Mission mission) {
        this.mission = mission;
    }


    public void setRequiresGeofile() {

        if (getMission() == Mission.MODISA || getMission() == Mission.MODIST || getMission() == Mission.VIIRS) {
            setRequiresGeofile(true);
        } else {
            setRequiresGeofile(false);
        }
    }


    public boolean isRequiresGeofile() {
        return requiresGeofile;
    }

    public void setRequiresGeofile(boolean requiresGeofile) {
        this.requiresGeofile = requiresGeofile;
    }


    public void setMissionDirectoryName() {
        final HashMap<Mission, String> missionDirectoryNameHashMap = new HashMap();
        missionDirectoryNameHashMap.put(Mission.SeaWiFS, "seawifs");
        missionDirectoryNameHashMap.put(Mission.MODISA, "hmodisa");
        missionDirectoryNameHashMap.put(Mission.MODIST, "hmodist");

        String missionPiece = null;

        if (missionDirectoryNameHashMap.containsKey(mission)) {
            missionPiece = missionDirectoryNameHashMap.get(mission);
        }

        if (missionPiece != null) {

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

    public void setDirectory(String directory) {
        this.directory = directory;
    }

}
