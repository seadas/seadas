package gov.nasa.gsfc.seadas.processing.general;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/20/12
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessorTypeInfo {

    public static enum ProcessorID {
        EXTRACTOR,
        MODIS_L1A_PY,
        MODIS_GEO_PY,
        L1BGEN,
        MODIS_L1B_PY,
        L1BRSGEN,
        L2BRSGEN,
        L1MAPGEN,
        L2MAPGEN,
        L2BIN,
        L2BIN_AQUARIUS,
        L3BIN,
        SMIGEN,
        SMITOPPM,
        LONLAT2PIXLINE,
        SEADAS_PROCESSOR_PY,
        SEADAS_PROCESSOR_MAIN,
        OCSSW_INSTALLER,
        NOID
    }

    private static final Map<String, ProcessorID> processorHashMap = new HashMap<String, ProcessorID>() {{

        put("l1aextract_modis", ProcessorID.EXTRACTOR);
        put("l1aextract_seawifs", ProcessorID.EXTRACTOR);
        put("l2extract", ProcessorID.EXTRACTOR);
        put("extractor", ProcessorID.EXTRACTOR);
        put("modis_L1A.py", ProcessorID.MODIS_L1A_PY);
        put("modis_GEO.py", ProcessorID.MODIS_GEO_PY);
        put("l1bgen", ProcessorID.L1BGEN);
        put("modis_L1B.py", ProcessorID.MODIS_L1B_PY);
        put("l1brsgen", ProcessorID.L1BRSGEN);
        put("l2brsgen", ProcessorID.L2BRSGEN);
        put("l1mapgen", ProcessorID.L1MAPGEN);
        put("l2mapgen", ProcessorID.L2MAPGEN);
        put("l2bin", ProcessorID.L2BIN);
        put("l2bin_aquarius", ProcessorID.L2BIN_AQUARIUS);
        put("l3bin", ProcessorID.L3BIN);
        put("smigen", ProcessorID.SMIGEN);
        put("smitoppm", ProcessorID.SMITOPPM);
        put("lonlat2pixline", ProcessorID.LONLAT2PIXLINE);
        put("seadas_processor.py", ProcessorID.SEADAS_PROCESSOR_PY);
        put("seadas_processor_main", ProcessorID.SEADAS_PROCESSOR_MAIN);
        put("install_ocssw.py", ProcessorID.OCSSW_INSTALLER);


    }};

    public static ProcessorID getProcessorID(String processorName) {
        return processorHashMap.get(processorName);
    }
}
