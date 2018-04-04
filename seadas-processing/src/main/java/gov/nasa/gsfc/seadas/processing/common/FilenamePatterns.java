package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.ocssw.OCSSW;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/15/12
 * Time: 11:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class FilenamePatterns {


//    static public FileInfo getOFileInfo(FileInfo fileInfo) {
//        return new FileInfo(fileInfo.getFile().getParent(), getOFile(fileInfo).getAbsolutePath(), false);
//    }
//
//    static public FileInfo getAquariusOFileInfo(FileInfo fileInfo, String suite) {
//        return new FileInfo(fileInfo.getFile().getParent(), getAquariusOFile(fileInfo, suite).getAbsolutePath(), false);
//    }


    static public File getAquariusOFile(FileInfo iFileInfo, String suite) {
        if (iFileInfo == null
                || iFileInfo.getFile() == null
                || iFileInfo.getFile().getAbsolutePath() == null
                || iFileInfo.getFile().getAbsolutePath().length() == 0) {
            return null;
        }


        File oFile = getL2genOfile(iFileInfo.getFile());

        // add on the suite
        StringBuilder ofile = new StringBuilder(oFile.getAbsolutePath());

        ofile.append("_").append(suite);

        oFile = new File(ofile.toString());

        return oFile;
    }


    static public File getOFile(FileInfo fileInfo) {
        if (fileInfo == null) {
            return null;
        }

        if (fileInfo.getFile().getAbsolutePath().length() == 0) {
            return null;
        }


        File oFile;
        if (fileInfo.isMissionId(MissionInfo.Id.VIIRS)) {
            oFile = getViirsOfilename(fileInfo.getFile());
        } else {
            if (fileInfo.getTypeId() == FileTypeInfo.Id.L3BIN) {
                oFile = getL3genOfile(fileInfo.getFile());
            } else {
                oFile = getL2genOfile(fileInfo.getFile());
            }

        }


        return oFile;
    }


    static public FileInfo getGeoFileInfo(FileInfo fileInfo, OCSSW ocssw) {
        if (fileInfo == null) {
            return null;
        }

        File geoFile = getGeoFile(fileInfo);

        if (geoFile == null) {
            return null;
        }

        return new FileInfo(fileInfo.getFile().getParent(), getGeoFile(fileInfo).getAbsolutePath(), false, ocssw);
    }


    static public File getGeoFile(FileInfo iFileInfo) {
        if (iFileInfo == null) {
            return null;
        }

        if (iFileInfo.getFile().getAbsolutePath().length() == 0) {
            return null;
        }

        String VIIRS_IFILE_PREFIX = "SVM01";

        StringBuilder geofileDirectory = new StringBuilder(iFileInfo.getFile().getParent() + "/");
        StringBuilder geofileBasename = new StringBuilder();
        StringBuilder geofile = new StringBuilder();
        File geoFile = null;

        if (iFileInfo.isMissionId(MissionInfo.Id.VIIRS) && iFileInfo.isTypeId(FileTypeInfo.Id.SDR)) {
            String VIIRS_GEOFILE_PREFIX = "GMTCO";
            geofileBasename.append(VIIRS_GEOFILE_PREFIX);
            geofileBasename.append(iFileInfo.getFile().getName().substring(VIIRS_IFILE_PREFIX.length()));
            geofile.append(geofileDirectory.toString() + geofileBasename.toString());
            File possibleGeoFile = new File(geofile.toString());
            if (possibleGeoFile.exists()) {
                geoFile = possibleGeoFile;
            }

        } else {
            ArrayList<File> possibleGeoFiles = new ArrayList<File>();

            String STRING_TO_BE_REPLACED[] = {"L1A_LAC", "L1B_LAC", "L1A.LAC", "L1B.LAC", "L1A_SNPP", "L1B-M_SNPP" };
            String STRING_TO_INSERT[] = {"GEO", "geo", "GEO-M_SNPP" };

            /**
             * replace last occurrence of instance of STRING_TO_BE_REPLACED[]
             */
            for (String string_to_be_replaced : STRING_TO_BE_REPLACED) {
                if (iFileInfo.getFile().getName().contains(string_to_be_replaced)) {

                    int index = iFileInfo.getFile().getName().lastIndexOf(string_to_be_replaced);
                    String start = iFileInfo.getFile().getName().substring(0, index);
                    String end = iFileInfo.getFile().getName().substring((index + string_to_be_replaced.length()), iFileInfo.getFile().getName().length());

                    for (String string_to_insert : STRING_TO_INSERT) {
                        StringBuilder possibleGeofile = new StringBuilder(geofileDirectory + start + string_to_insert + end);
                        possibleGeoFiles.add(new File(possibleGeofile.toString()));
                    }

                    break;
                }
            }

            for (String string_to_insert : STRING_TO_INSERT) {
                StringBuilder possibleGeofile = new StringBuilder(geofileDirectory + iFileInfo.getFile().getName() + "." + string_to_insert);
                possibleGeoFiles.add(new File(possibleGeofile.toString()));
            }

            for (File possibleGeoFile : possibleGeoFiles) {
                if (possibleGeoFile.exists()) {
                    geoFile = possibleGeoFile;
                    break;
                }
            }

            if (geoFile == null) {
                if (possibleGeoFiles.size() > 0) {
                    geoFile = possibleGeoFiles.get(0);
                } else {
                    geoFile = new File("YourGeoFilename.GEO");
                }
            }
        }

        return geoFile;
    }


    static private File getViirsOfilename(File iFile) {
        if (iFile == null || iFile.getAbsoluteFile().length() == 0) {
            return null;
        }


        StringBuilder ofile = new StringBuilder(iFile.getParent() + "/");

        String yearString = iFile.getName().substring(11, 15);
        String monthString = iFile.getName().substring(15, 17);
        String dayOfMonthString = iFile.getName().substring(17, 19);

        String formattedDateString = getFormattedDateString(yearString, monthString, dayOfMonthString);

        String timeString = iFile.getName().substring(21, 27);
        ofile.append("V");
        ofile.append(formattedDateString);
        ofile.append(timeString);

        ofile.append(".");
        ofile.append("L2_NPP");

        return new File(ofile.toString());
    }


    static private File getL2genOfile(File iFile) {
        if (iFile == null || iFile.getAbsoluteFile().length() == 0) {
            return null;
        }

        String OFILE_REPLACEMENT_STRING = "L2";
        String IFILE_STRING_TO_BE_REPLACED[] = {"L1A", "L1B"};

        StringBuilder ofileBasename = new StringBuilder();

        /**
         * replace last occurrence of instance of IFILE_STRING_TO_BE_REPLACED[]
         */
        for (String string_to_be_replaced : IFILE_STRING_TO_BE_REPLACED) {
            if (iFile.getName().toUpperCase().contains(string_to_be_replaced)) {
                int index = iFile.getName().toUpperCase().lastIndexOf(string_to_be_replaced);
                ofileBasename.append(iFile.getName().substring(0, index));
                ofileBasename.append(OFILE_REPLACEMENT_STRING);
                ofileBasename.append(iFile.getName().substring((index + string_to_be_replaced.length()), iFile.getName().length()));
                break;
            }
        }

        /**
         * Not found so append it
         */
        if (ofileBasename.toString().length() == 0) {
            ofileBasename.append(iFile.getName());
            ofileBasename.append("." + OFILE_REPLACEMENT_STRING);

        }

        StringBuilder ofile = new StringBuilder(iFile.getParent() + "/" + ofileBasename.toString());

        return new File(ofile.toString());
    }

    static private File getL3genOfile(File iFile) {
        if (iFile == null || iFile.getAbsoluteFile().length() == 0) {
            return null;
        }

        StringBuilder ofileBasename = new StringBuilder();


        if (ofileBasename.toString().length() == 0) {
            ofileBasename.append(iFile.getName());
            ofileBasename.append(".out");

        }

        StringBuilder ofile = new StringBuilder(iFile.getParent() + "/" + ofileBasename.toString());

        return new File(ofile.toString());
    }



    /**
     * Given standard Gregorian date return day of year (Jan 1=1, Feb 1=32, etc)
     *
     * @param year
     * @param month      1-based Jan=1, etc.
     * @param dayOfMonth
     * @return
     */

    static private int getDayOfYear(int year, int month, int dayOfMonth) {
        GregorianCalendar gc = new GregorianCalendar(year, month - 1, dayOfMonth);
        return gc.get(GregorianCalendar.DAY_OF_YEAR);
    }


    static private String getFormattedDateString(String yearString, String monthString, String dayOfMonthString) {
        int year = Integer.parseInt(yearString);
        int month = Integer.parseInt(monthString);
        int dayOfMonth = Integer.parseInt(dayOfMonthString);
        return getFormattedDateString(year, month, dayOfMonth);
    }


    static private String getFormattedDateString(int year, int month, int dayOfMonth) {

        StringBuilder formattedDateString = new StringBuilder(Integer.toString(year));

        int dayOfYear = getDayOfYear(year, month, dayOfMonth);

        StringBuilder dayOfYearString = new StringBuilder(Integer.toString(dayOfYear));

        while (dayOfYearString.toString().length() < 3) {
            dayOfYearString.insert(0, "0");
        }

        formattedDateString.append(dayOfYearString);

        return formattedDateString.toString();
    }


}
