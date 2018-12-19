package gov.nasa.gsfc.seadas.processing.common;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/8/12
 * Time: 11:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class SeadasProcessorInfo {


    public static enum Id {
        L1AEXTRACT_MODIS,
        L1AEXTRACT_SEAWIFS,
        L1AEXTRACT,
        L1AGEN,
        GEOGEN,
        L1BGEN,
        L1MAPGEN,
        L1BRSGEN,
        L2EXTRACT,
        L2GEN,
        L2GEN_AQUARIUS,
        L2MAPGEN,
        L2BRSGEN,
        L2BIN,
        L3BIN,
        L3GEN,
        SMIGEN
    }

    private Id id;
    private FileInfo fileInfo;
    private String executable;
    private boolean validIfile = false;

    public SeadasProcessorInfo(Id id) {
        this.id = id;
    }

    public SeadasProcessorInfo(Id id, FileInfo fileInfo) {
        this.id = id;
        this.fileInfo = fileInfo;

        setExecutable();
        setValidIfile();
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        setExecutable();
        setValidIfile();
    }


    private void setExecutable() {
        executable = getExecutable(fileInfo, id);
    }

    static public String getExecutable(FileInfo iFileInfo, SeadasProcessorInfo.Id processorId) {
        if (processorId == null || iFileInfo == null) {
            return null;
        }

        if (iFileInfo.isSupportedMission()) {

            switch (processorId) {
                case L1AEXTRACT:
                    if (iFileInfo.isMissionId(MissionInfo.Id.MODISA) ||
                            iFileInfo.isMissionId(MissionInfo.Id.MODIST)) {
                        return "l1aextract_modis";
                    } else if (iFileInfo.isMissionId(MissionInfo.Id.SEAWIFS)) {
                        return "l1aextract_seawifs";

                    } else {
                        return "l2extract";
                    }

                case L1AGEN:
                    if (iFileInfo.isMissionId(MissionInfo.Id.MODISA) ||
                            iFileInfo.isMissionId(MissionInfo.Id.MODIST)) {
                        return "modis_L1A.py";
                    } else if (iFileInfo.isMissionId(MissionInfo.Id.SEAWIFS)) {
                        return "l1aextract_seawifs";

                    } else {
                        return "modis_L1A.py";
                    }

                case GEOGEN:
                    if (iFileInfo.isGeofileRequired()) {
                        return "modis_GEO.py";
                    } else {
                        return null;
                    }

                case L1BGEN:
                    if (iFileInfo.isMissionId(MissionInfo.Id.MODISA) ||
                            iFileInfo.isMissionId(MissionInfo.Id.MODIST)) {
                        return "modis_L1B.py";
                    } else {
                        return "l1bgen";
                    }

                case L1MAPGEN:
                    return "l1mapgen";
                case L1BRSGEN:
                    return "l1brsgen";
                case L2EXTRACT:
                    return "l2extract";
                case L2GEN:
                    return "l2gen";
                case L2GEN_AQUARIUS:
                    return "l2gen_aquarius";
                case L2MAPGEN:
                    return "l2mapgen";
                case L2BRSGEN:
                    return "l2brsgen";
                case L2BIN:
                    return "l2bin";
                case L3BIN:
                    return "l3bin";
                case L3GEN:
                    return "l3gen";
                case SMIGEN:
                    return "smigen";
                default:
                    return null;
            }
        } else {
            return null;
        }
    }


    static public boolean isSupportedMission(FileInfo iFileInfo, SeadasProcessorInfo.Id processorId) {
        if (processorId == null || iFileInfo == null) {
            return false;
        }

        switch (processorId) {
            case L1AEXTRACT_MODIS:
                return iFileInfo.isSupportedMission() &&
                        !iFileInfo.isMissionId(MissionInfo.Id.SEAWIFS);
            case L1AEXTRACT_SEAWIFS:
                return iFileInfo.isMissionId(MissionInfo.Id.SEAWIFS);
            case L1AEXTRACT:
                return iFileInfo.isSupportedMission();
            case L1AGEN:
                return iFileInfo.isSupportedMission();
            case GEOGEN:
                return iFileInfo.isSupportedMission() &&
                        iFileInfo.isGeofileRequired();
            case L1BGEN:
                return iFileInfo.isSupportedMission();
            case L1MAPGEN:
                return iFileInfo.isSupportedMission();
            case L1BRSGEN:
                return iFileInfo.isSupportedMission();
            case L2EXTRACT:
                return iFileInfo.isSupportedMission();
            case L2GEN:
                return iFileInfo.isSupportedMission();
            case L2GEN_AQUARIUS:
                return iFileInfo.isSupportedMission();
            case L2MAPGEN:
                return iFileInfo.isSupportedMission();
            case L2BRSGEN:
                return iFileInfo.isSupportedMission();
            case L2BIN:
                return iFileInfo.isSupportedMission();
            case L3BIN:
                return iFileInfo.isSupportedMission();
            case L3GEN:
                return iFileInfo.isSupportedMission();
            case SMIGEN:
                return iFileInfo.isSupportedMission();
            default:
                return false;
        }

    }


    static public boolean isValidFileType(FileInfo iFileInfo, SeadasProcessorInfo.Id processorId) {
        if (processorId == null || iFileInfo == null) {
            return false;
        }

        switch (processorId) {
            case L1AEXTRACT_MODIS:
                return iFileInfo.isTypeId(FileTypeInfo.Id.L1A);
            case L1AEXTRACT_SEAWIFS:
                return iFileInfo.isTypeId(FileTypeInfo.Id.L1A);
            case L1AEXTRACT:
                return iFileInfo.isTypeId(FileTypeInfo.Id.L1A);
            case L1AGEN:
                return iFileInfo.isTypeId(FileTypeInfo.Id.L0);
            case GEOGEN:
                return iFileInfo.isTypeId(FileTypeInfo.Id.L1A);
            case L1BGEN:
                return isValidL2genFileType(iFileInfo); // same requirements as L2GEN
            case L1MAPGEN:
                return isValidL2genFileType(iFileInfo); // same requirements as L2GEN
            case L1BRSGEN:
                return isValidL2genFileType(iFileInfo); // same requirements as L2GEN
            case L2EXTRACT:
                return iFileInfo.isTypeId(FileTypeInfo.Id.L2);
            case L2GEN:
                return isValidL2genFileType(iFileInfo);
            case L2GEN_AQUARIUS:
                return isValidL2genFileType(iFileInfo);
            case L2MAPGEN:
                return iFileInfo.isTypeId(FileTypeInfo.Id.L2);
            case L2BRSGEN:
                return iFileInfo.isTypeId(FileTypeInfo.Id.L2);
            case L2BIN:
                return iFileInfo.isTypeId(FileTypeInfo.Id.L2);
            case L3BIN:
                return iFileInfo.isTypeId(FileTypeInfo.Id.L3);
            case L3GEN:
                return iFileInfo.isTypeId(FileTypeInfo.Id.L3BIN);
            case SMIGEN:
                return iFileInfo.isTypeId(FileTypeInfo.Id.L3BIN);
            default:
                return false;
        }

    }

    static public boolean isValidIfile(FileInfo iFileInfo, SeadasProcessorInfo.Id processorId) {

        if (iFileInfo != null && iFileInfo.getFile().exists() && iFileInfo.getFile().isAbsolute()) {
            return isValidFileType(iFileInfo, processorId) && isSupportedMission(iFileInfo, processorId);
        }

        return false;
    }


    private void setValidIfile() {
        validIfile = isValidIfile(fileInfo, id);
    }


    static private boolean isValidL2genFileType(FileInfo iFileInfo) {
        if (iFileInfo == null) {
            return false;
        }

        if (iFileInfo.isMissionId(MissionInfo.Id.VIIRSN) || iFileInfo.isMissionId(MissionInfo.Id.VIIRSJ1)) {
            if (iFileInfo.isTypeId(FileTypeInfo.Id.SDR) ||
                iFileInfo.isTypeId(FileTypeInfo.Id.L1A) ||
                iFileInfo.isTypeId(FileTypeInfo.Id.L1B)
                ) {
                return true;
            }
        } else if (iFileInfo.isMissionId(MissionInfo.Id.MODISA) ||
                iFileInfo.isMissionId(MissionInfo.Id.MODIST) ||
                iFileInfo.isMissionId(MissionInfo.Id.MERIS)
                ) {
            if (iFileInfo.isTypeId(FileTypeInfo.Id.L1B)) {
                return true;
            }

        } else {
            if (iFileInfo.isTypeId(FileTypeInfo.Id.L1A) ||
                    iFileInfo.isTypeId(FileTypeInfo.Id.L1B)) {
                return true;
            }
        }

        return false;
    }


    public FileInfo getFileInfo() {
        return fileInfo;
    }


    public String getExecutable() {
        return executable;
    }


    public boolean isValidIfile() {
        return validIfile;
    }

}
