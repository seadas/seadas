package gov.nasa.gsfc.seadas.processing.general;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/20/12
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessorTypeInfo {

    public static String L1EXTRACT_MODIS_PROGRAM_NAME = "l1extract_modis";
    public static String L1EXTRACT_SEAWIFS_PROGRAM_NAME = "l1extract_seawifs";
    public static String L2EXTRACT_PROGRAM_NAME = "l2extract";
    public static String MODIS_L1A_PY_PROGRAM_NAME = "modis_L1A.py";
    public static String MODIS_GEO_PY_PROGRAM_NAME = "modis_GEO.py";
    public static String GEOLOCATE_VIIRS_PROGRAM_NAME = "geolocate_viirs";
    public static String L1BGEN_PROGRAM_NAME = "l1bgen";
    public static String MODIS_L1B_PY_PROGRAM_NAME = "modis_L1B.py";
    public static String CALIBRATE_VIIRS_PROGRAM_NAME = "calibrate_viirs";
    public static String L1BRSGEN_PROGRAM_NAME = "l1brsgen";
    public static String L2BRSGEN_PROGRAM_NAME = "l2brsgen";
    public static String L1MAPGEN_PROGRAM_NAME = "l1mapgen";
    public static String L2MAPGEN_PROGRAM_NAME = "l2mapgen";
    public static String L2BIN_PROGRAM_NAME = "l2bin";
    public static String L2BIN_AQUARIUS_PROGRAM_NAME = "l2bin_aquarius";
    public static String L2GEN_PROGRAM_NAME = "l2gen";
    public static String L2GEN_AQUARIUS_PROGRAM_NAME = "l2gen_aquarius";
    public static String L3BIN_PROGRAM_NAME = "l3bin";
    public static String L3MAPGEN_PROGRAM_NAME = "l3mapgen";
    public static String SMIGEN_PROGRAM_NAME = "smigen";
    public static String SMITOPPM_PROGRAM_NAME = "smitoppm";
    public static String LONLAT2PIXLINE_PROGRAM_NAME = "lonlat2pixline";
    public static String MULTILEVEL_PROCESSOR_PY_PROGRAM_NAME = "multilevel_processor.py";
    public static String MULTILEVEL_PROCESSOR_PROGRAM_NAME = "multilevel_processor";
    public static String INSTALL_OCSSW_PY_PROGRAM_NAME = "install_ocssw.py";
    public static String L3BINDUMP_PROGRAM_NAME = "l3bindump";
    public static String GET_OBPG_FILE_TYPE_PY_PROGRAM_NAME = "get_obpg_file_type.py";
    public static String NEXT_LEVEL_NAME_PY_PROGRAM_NAME = "next_level_name.py";

    public static enum ProcessorID {
        EXTRACTOR,
        MODIS_L1A_PY,
        MODIS_GEO_PY,
        GEOLOCATE_VIIRS,
        L1BGEN,
        MODIS_L1B_PY,
        CALIBRATE_VIIRS,
        L1BRSGEN,
        L2BRSGEN,
        L1MAPGEN,
        L2MAPGEN,
        L2BIN,
        L2BIN_AQUARIUS,
        L3BIN,
        L3MAPGEN,
        SMIGEN,
        SMITOPPM,
        LONLAT2PIXLINE,
        MULTILEVEL_PROCESSOR_PY,
        MULTILEVEL_PROCESSOR,
        OCSSW_INSTALLER,
        L2GEN,
        L2GEN_AQUARIUS,
        L3BINDUMP,
        GET_OBPG_FILE_TYPE_PY,
        NEXT_LEVEL_NAME_PY,
        NOID
    }

    private static final Map<String, ProcessorID> processorHashMap = new HashMap<String, ProcessorID>() {{

        put("l1aextract_modis", ProcessorID.EXTRACTOR);
        put("l1aextract_seawifs", ProcessorID.EXTRACTOR);
        put("l2extract", ProcessorID.EXTRACTOR);
        put("extractor", ProcessorID.EXTRACTOR);
        put("modis_L1A.py", ProcessorID.MODIS_L1A_PY);
        put("modis_GEO.py", ProcessorID.MODIS_GEO_PY);
        put("geolocate_viirs", ProcessorID.GEOLOCATE_VIIRS);
        put("l1bgen", ProcessorID.L1BGEN);
        put("modis_L1B.py", ProcessorID.MODIS_L1B_PY);
        put("calibrate_viirs", ProcessorID.CALIBRATE_VIIRS);
        put("l1brsgen", ProcessorID.L1BRSGEN);
        put("l2brsgen", ProcessorID.L2BRSGEN);
        put("l1mapgen", ProcessorID.L1MAPGEN);
        put("l2mapgen", ProcessorID.L2MAPGEN);
        put("l2bin", ProcessorID.L2BIN);
        put("l2bin_aquarius", ProcessorID.L2BIN_AQUARIUS);
        put("l2gen", ProcessorID.L2GEN);
        put("l2gen_aquarius", ProcessorID.L2GEN_AQUARIUS);
        put("l3bin", ProcessorID.L3BIN);
        put("l3mapgen", ProcessorID.L3MAPGEN);
        put("smigen", ProcessorID.SMIGEN);
        put("smitoppm", ProcessorID.SMITOPPM);
        put("lonlat2pixline", ProcessorID.LONLAT2PIXLINE);
        put("multilevel_processor.py", ProcessorID.MULTILEVEL_PROCESSOR_PY);
        put("multilevel_processor", ProcessorID.MULTILEVEL_PROCESSOR);
        put("install_ocssw.py", ProcessorID.OCSSW_INSTALLER);
        put("l3bindump", ProcessorID.L3BINDUMP);
        put("get_obpg_file_type.py", ProcessorID.GET_OBPG_FILE_TYPE_PY);
        put("next_level_name.py", ProcessorID.NEXT_LEVEL_NAME_PY);

    }};

    protected static String getExcludedProcessorNames() {
       return     "multilevel_processor" +
                  "smitoppm" +
                  "l1aextract_modis" +
                  "l1aextract_seawifs" +
                  "l2extract" +
                  "next_level_name.py" +
                  "get_obpg_file_type.py"+
                  "lonlat2pixline";
    }


    protected static Set<String> getProcessorNames() {
        return processorHashMap.keySet();
    }

    public static ProcessorID getProcessorID(String processorName) {
        return processorHashMap.get(processorName);
    }
}
