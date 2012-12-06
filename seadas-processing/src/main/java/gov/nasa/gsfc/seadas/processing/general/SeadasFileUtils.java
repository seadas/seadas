package gov.nasa.gsfc.seadas.processing.general;

import org.esa.beam.util.Debug;
import org.esa.beam.visat.VisatApp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/20/12
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class SeadasFileUtils {

    private static boolean debug = false;

    public static File createFile(String parent, String fileName) {
        File pFile;
        if (parent == null) {
            pFile = null;
        } else {
            pFile = new File(parent);
        }
        return createFile(pFile, fileName);
    }

    public static File createFile(File parent, String fileName) {
        if (fileName == null) {
            return null;
        }

        String expandedFilename = SeadasFileUtils.expandEnvironment(fileName);
        File file = new File(expandedFilename);
        if (!file.isAbsolute() && parent != null) {
            file = new File(parent, expandedFilename);
        }
        return file;
    }

    public static String getCurrentDate(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());

    }


    public static String getGeoFileNameFromIFile(String ifileName) {

        String geoFileName = (ifileName.substring(0, ifileName.indexOf("."))).concat(".GEO");

        if (new File(geoFileName).exists()) {
            return geoFileName;
        } else {
            VisatApp.getApp().showErrorDialog(ifileName + " requires a GEO file to be extracted. " + geoFileName + " does not exist.");
            return null;
        }
    }

    public static String getDefaultOFileNameFromIFile(String ifileName, String programName) {
        debug("Program name is " + programName);
        Debug.assertNotNull(ifileName);
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
                ofileName = ifileName.replaceAll("L2_.{3,}", "L3b_DAY");
                break;
            case L3BIN:
                ofileName = ifileName.replaceAll("L2_.{3,}", "L3b_DAY");
                break;
            case SMIGEN:
                ofileName = ifileName.replaceAll("L3B", "L3m");
                ofileName = ofileName.replaceAll("L3b", "L3m");
                ofileName = ofileName.replaceAll(".main", "");
                break;
            case SMITOPPM:
                ofileName = ifileName.trim().length() > 0 ? ifileName + ".ppm" : "";
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


//    public static String expandEnvironment(String string) {
//
//
//        //     Pattern pattern = Pattern.compile("(.+)\\$\\{{0,1}([A-Za-z0-9_]+)\\}{0,1}(.+)");
//        Pattern pattern = Pattern.compile("([.]*)\\$\\{{0,1}([A-Za-z0-9_]+)\\}{0,1}([.]*)");
//        Matcher matcher = pattern.matcher(string);
//
//
//        if (matcher.find()) {
//            String s0 = matcher.group(0);
//            String s1 = matcher.group(1);
//            String s2 = matcher.group(2);
//            String s3 = s2;
//        }
//
//
//        return string;
//    }
//
//


    public static String expandEnvironment(String string1) {

        if (string1 == null) {
            return string1;
        }

        String environmentPattern = "([A-Za-z0-9_]+)";
        Pattern pattern1 = Pattern.compile("\\$\\{" + environmentPattern + "\\}");
        Pattern pattern2 = Pattern.compile("\\$" + environmentPattern);


        String string2 = null;

        while (!string1.equals(string2)) {
            if (string2 != null) {
                string1 = string2;
            }

            string2 = expandEnvironment(pattern1, string1);
            string2 = expandEnvironment(pattern2, string2);

            if (string2 == null) {
                return string2;
            }
        }

        return string2;
    }



    private static String expandEnvironment(Pattern pattern, String string) {


        if (string == null || pattern == null) {
            return string;
        }

        Matcher matcher = pattern.matcher(string);
        Map<String, String> envMap = null;

        while (matcher.find()) {

            // Retrieve environment variables
            if (envMap == null) {
                envMap = System.getenv();
            }

            String envNameOnly = matcher.group(1);

            if (envMap.containsKey(envNameOnly)) {
                String envValue = envMap.get(envNameOnly);

                if (envValue != null) {
                    String escapeStrings[] = {"\\", "$", "{", "}"};
                    for (String escapeString : escapeStrings) {
                        envValue = envValue.replace(escapeString, "\\"+escapeString);
                    }

                    Pattern envNameClausePattern = Pattern.compile(Pattern.quote(matcher.group(0)));
                    string = envNameClausePattern.matcher(string).replaceAll(envValue);
                }
            }
        }

        return string;
    }


    public static void debug(String message) {
        if (debug) {
            System.out.println("Debugging: " + message);
        }
    }
}
