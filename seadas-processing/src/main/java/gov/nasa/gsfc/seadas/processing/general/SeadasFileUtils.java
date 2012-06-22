package gov.nasa.gsfc.seadas.processing.general;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/20/12
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class SeadasFileUtils {

    private static boolean debug = true;

    public static String getCurrentDate(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());

    }

    public static String getGeoFileNameFromIFile(String ifileName, String programName) {
        debug("Program name is " + programName);
        ProcessorTypeInfo.ProcessorID processorID = ProcessorTypeInfo.getProcessorID(programName);
        String ofileName = ifileName + "_" + programName + ".out";
        switch (processorID) {
            case EXTRACTOR:
                ofileName = ifileName + ".sub";
                break;
            case MODIS_L1A_PY:
                //FileUtils.exchangeExtension(ifileName, "GEO") ;
                break;
            case MODIS_GEO_PY:
                ofileName = ifileName.replaceAll("L1A_LAC", "GEO");
                break;
            case L1BGEN:
                ofileName = ifileName.replaceAll("L1A", "L1B");
                break;
            case MODIS_L1B_PY:
                ofileName = ifileName.replaceAll("L1A", "L1B");
                break;
            case L1BRSGEN:
                ofileName = ifileName + ".BRS";
                break;
            case L2BRSGEN:
                ofileName = ifileName + ".BRS";
                break;
            case L1MAPGEN:
                ofileName = ifileName + "_" + programName + ".out";
                break;
            case L2MAPGEN:
                ofileName = ifileName + "_" + programName + ".out";
                break;
            case L2BIN:
                ofileName = ifileName.replaceAll("L2_/?/?/?", "L3d_" + getCurrentDate("yyyyMMdd"));
                break;
            case L3BIN:
                ofileName = ifileName.replaceAll("L2_/?/?/?", "L3d_" + getCurrentDate("yyyyMMdd"));
                ;
                break;
            case SMIGEN:
                ofileName = ofileName.replaceAll("L3b", "L3m");
                ofileName = ofileName.replaceAll(".main", "");
                break;
            case SMITOPPM:
                ofileName = ifileName + ".ppm";
                break;
        }
        return ofileName;
    }

    public static String getDefaultOFileNameFromIFile(String ifileName, String programName) {
        debug("Program name is " + programName);
        ProcessorTypeInfo.ProcessorID processorID = ProcessorTypeInfo.getProcessorID(programName);
        String ofileName = ifileName + "_" + programName + ".out";
        switch (processorID) {
            case EXTRACTOR:
                ofileName = ifileName + ".sub";
                break;
            case MODIS_L1A_PY:
                //FileUtils.exchangeExtension(ifileName, "GEO") ;
                break;
            case MODIS_GEO_PY:
                ofileName = ifileName.replaceAll("L1A_LAC", "GEO");
                break;
            case L1BGEN:
                ofileName = ifileName.replaceAll("L1A", "L1B");
                break;
            case MODIS_L1B_PY:
                ofileName = ifileName.replaceAll("L1A", "L1B");
                break;
            case L1BRSGEN:
                ofileName = ifileName + ".BRS";
                break;
            case L2BRSGEN:
                ofileName = ifileName + ".BRS";
                break;
            case L1MAPGEN:
                ofileName = ifileName + "_" + programName + ".out";
                break;
            case L2MAPGEN:
                ofileName = ifileName + "_" + programName + ".out";
                break;
            case L2BIN:
                ofileName = ifileName.replaceAll("L2_/?/?/?", "L3d_" + getCurrentDate("yyyyMMdd"));
                break;
            case L3BIN:
                ofileName = ifileName.replaceAll("L2_/?/?/?", "L3d_" + getCurrentDate("yyyyMMdd"));
                ;
                break;
            case SMIGEN:
                ofileName = ofileName.replaceAll("L3b", "L3m");
                ofileName = ofileName.replaceAll(".main", "");
                break;
            case SMITOPPM:
                ofileName = ifileName + ".ppm";
                break;
        }
        return ofileName;
    }

    public static void main(String arg[]) {
        System.out.println(getCurrentDate("dd MMMMM yyyy"));
        System.out.println(getCurrentDate("yyyyMMdd"));
        System.out.println(getCurrentDate("dd.MM.yy"));
        System.out.println(getCurrentDate("MM/dd/yy"));
        System.out.println(getCurrentDate("yyyy.MM.dd G 'at' hh:mm:ss z"));
        System.out.println(getCurrentDate("EEE, MMM d, ''yy"));
        System.out.println(getCurrentDate("h:mm a"));
        System.out.println(getCurrentDate("H:mm:ss:SSS"));
        System.out.println(getCurrentDate("K:mm a,z"));
        System.out.println(getCurrentDate("yyyy.MMMMM.dd GGG hh:mm aaa"));
    }

    public static void debug(String message) {
        if (debug) {
            System.out.println("Debugging: " + message);
        }
    }
}
