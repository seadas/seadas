package gov.nasa.obpg.seadas.sandbox.seawifs;

//package geonav;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import ucar.ma2.*;
import ucar.nc2.*;

import org.apache.commons.cli.*;

public class ReaderTest {

    // Constants:
    private static final String HDF_HEADER = Character.toString((char) 14) + 
                                             Character.toString((char) 3) + 
                                             Character.toString((char) 19) + 
                                             Character.toString((char) 1);

    int numScanLines = 0;

    private boolean debug = true;
    private static Logger errorLogger;

    //public ReaderTest() { }  // an explicit constructor may be needed ...

    private void dispVarDetails(Variable v) {
        /* Display debugging data about a Netcdf Variable */
        if (v != null) {
            System.out.print(v.getShortName() + ":");
            System.out.print("  rank: " + v.getRank());
            System.out.print("; size: " + v.getSize());
            //orbitVar.getShape(): ");
            if (v.getRank() > 1) {
                System.out.print(";  shape: ");
                for (int i = 0; i < v.getShape().length; i++) {
                    System.out.print(" " + v.getShape()[i]);
                    if (i < (v.getShape().length - 1)) {
                        System.out.print(" x");
                    }
                }
            }
            System.out.println();
        } else {
            System.out.println(" variable is null.");
        }
    }

    public static boolean isHDF(File f) {
    /**
	 *  Determine if file is an HDF file by seeing if the first 
	 *  four characters of the file contain the HDF header.
	 */
        // Header for HDF files: Ctrl-N Ctrl-C Ctrl-S Ctrl-A (no spaces)
        String fileHeader;
        try {
            DataInputStream inStr = new DataInputStream(new FileInputStream(f));
            fileHeader = Character.toString((char) inStr.readByte()) + 
                         Character.toString((char) inStr.readByte()) + 
                         Character.toString((char) inStr.readByte()) + 
                         Character.toString((char) inStr.readByte());
            inStr.close();
        } catch(IOException ioe) {
            fileHeader = "";
        }
        return fileHeader.equals(HDF_HEADER);
    }

    private static boolean isInputFileOk(File theFile) 
                           throws FileNotHdfException, FileNotFoundException {
        /**
         * Verify that the file is suitable for processing by
         * checking that it exists and is an HDF.
         */
        boolean fGood = false;
        if (theFile.exists()) {
            if (isHDF(theFile)) {
                fGood = true;
	    } else {
                throw new FileNotHdfException(theFile.getName());
            }
        } else {
            throw new FileNotFoundException(theFile.getName()
                                            + " could not be found.");
        }
        return fGood;
    }

    public static void main(String[] args) {
        /**
         *  This main method:
         *   - redirects stderr to a logging file.
         *   - processes and checks the command line input.
         *   - provides help if requested and then ends execution.
         *   - instantiates a ReaderTest object if help is not requested and
         *     then calls processFile.
         *  If problems are found, an appropriate exception is thrown and the 
         *  program exits with an error.
         */

        redirectStderr();

        if (args.length > 0) {
            Options options = new Options();
            options.addOption("h", false, "help message");
            CommandLineParser parser = new PosixParser();
            try {
                CommandLine cmdLine = parser.parse(options, args);
                if (cmdLine.hasOption("h")) {
                    printUsage();
    	        } else {
    		    File inFile = new File(args[0]);
                    if (isInputFileOk(inFile)) {
                        ReaderTest rt = new ReaderTest();
                        rt.processFile(inFile);
                    }
                }
            } catch(FileNotHdfException fnhe) { 
                System.out.println("Caught FileNotHdfException: " 
                                   + fnhe.getMessage());
                System.exit(84);
            } catch(FileNotFoundException fnfe) { 
                System.out.println("Caught FileNotFoundException: " 
                                   + fnfe.getMessage());
                System.exit(87);
            } catch(IOException ioe) {
                System.out.println("Caught IOException: " + ioe.getMessage());
                System.exit(90);
            } catch(ParseException pe) {
                System.out.println("Caught ParseException: " + pe.getMessage());
                System.exit(93);
/*
            } catch(Exception e) {
                System.out.println("Caught Exception: " + e.getMessage());
                System.out.println(e.getStackTrace());
                System.exit(96);
*/
            }
        } else {
            System.out.println("No input file given.");
            System.exit(99);
        }
    }

    private void outputHeader() {
        /*
         * Output header to match what Fred provided
         */
        //String header = String.format();
        System.out.println("     Line   Pixel  Latitude   Longitude           Solar                  Sensor");
        System.out.println("                                            Zenith     Azimuth      Zenith     Azimuth");
    }

    private static void printUsage() {
        /**
	     *  Prints a usage message to wherever System.out is printing.
	     */
        System.out.println("usage:");
        System.out.println("   java ReaderTest -h");
        System.out.println("      help (this message)");
        System.out.println("   java ReaderTest INPUT_FILE");
        System.out.println("      run the reader test, using INPUT_FILE");
    }

    private void processFile(File inFile) throws IOException {
        //System.out.println("Processing " + inFile.getPath());
        NetcdfFile ncFile = null;
        try {
            String prefix = "  ";
            ncFile = NetcdfFile.open(inFile.getPath());
            Geonav.DataType dataType = Geonav.getSeawifsDataType(ncFile);
            numScanLines = Geonav.getNumberScanLines(ncFile);

            Group rootGroup = ncFile.getRootGroup();
            Group navGroup = ncFile.findGroup("Navigation");
            Group scanLineAttrGroup = ncFile.findGroup("Scan-Line Attributes");
            //Group tiltGroup = ncFile.findGroup("Sensor Tilt");

            outputHeader();

            int[] startPts1 = new int[1];
            int[] startPts2 = new int[2];
            int[] startPts3 = new int[3];
            Variable orbitVar = navGroup.findVariable("orb_vec");
            ArrayFloat orbitData = (ArrayFloat.D2) orbitVar.read(startPts2, orbitVar.getShape());

            Variable sensorVar = navGroup.findVariable("sen_mat");
            ArrayFloat sensorData = (ArrayFloat.D3) sensorVar.read(startPts3, sensorVar.getShape());

            Variable sunVar = navGroup.findVariable("sun_ref");
            ArrayFloat sunData = (ArrayFloat.D2) sunVar.read(startPts2, sunVar.getShape());

            Variable attAngleVar = navGroup.findVariable("att_ang");
            ArrayFloat attAngleData = (ArrayFloat) attAngleVar.read(startPts2, attAngleVar.getShape());

            Variable tiltVar = scanLineAttrGroup.findVariable("tilt");
            ArrayFloat tiltData = (ArrayFloat.D1) tiltVar.read();

if (debug) {
    System.out.print("orbitVar: ");
    dispVarDetails(orbitVar);
    System.out.print("sensoVar: ");
    dispVarDetails(sensorVar);
    System.out.print("sunVar: ");
    dispVarDetails(sunVar);
    System.out.print("attAngleVar: ");
    dispVarDetails(attAngleVar);
    System.out.print("tiltVar: ");
    dispVarDetails(tiltVar);
}

            for (int line = 0; line < numScanLines; line ++) {
                float[] orbVect = new float[3];
                orbVect[0] = orbitData.getFloat(3 * line);
                orbVect[1] = orbitData.getFloat(3 * line + 1);
                orbVect[2] = orbitData.getFloat(3 * line + 2);

                // Re-visit this if the results come out wrong:
                float[][] sensorMat = new float[3][3];
                sensorMat[0][0] = sensorData.getFloat(3 * line);
                sensorMat[0][1] = sensorData.getFloat(3 * line + 1);
                sensorMat[0][2] = sensorData.getFloat(3 * line + 2);
                sensorMat[1][0] = sensorData.getFloat(3 * line + 3);
                sensorMat[1][1] = sensorData.getFloat(3 * line + 4);
                sensorMat[1][2] = sensorData.getFloat(3 * line + 5);
                sensorMat[2][0] = sensorData.getFloat(3 * line + 6);
                sensorMat[2][1] = sensorData.getFloat(3 * line + 7);
                sensorMat[2][2] = sensorData.getFloat(3 * line + 8);

                float[] sunUnitVect = new float[3];
                sunUnitVect[0] = sunData.getFloat(3 * line);
                sunUnitVect[1] = sunData.getFloat(3 * line + 1);
                sunUnitVect[2] = sunData.getFloat(3 * line + 2);

                float[] attAngleVect = new float[3];
                attAngleVect[0] = attAngleData.getFloat(3 * line);
                attAngleVect[1] = attAngleData.getFloat(3 * line + 1);
                attAngleVect[2] = attAngleData.getFloat(3 * line + 2);

                float tilt = tiltData.getFloat(line);

                Geonav geonavCalculator = new Geonav(orbVect, sensorMat, sunUnitVect, attAngleVect, tilt, ncFile);

                float[] latitude = geonavCalculator.getLatitude();
       	        float[] longitude = geonavCalculator.getLongitude();
       	        float[] sensorAzimuth = geonavCalculator.getSensorAzimuth();
       	        float[] sensorZenith = geonavCalculator.getSensorZenith();
       	        float[] solarAzimuth = geonavCalculator.getSolarAzimuth();
       	        float[] solarZenith = geonavCalculator.getSolarZenith();

                for (int pix = 0; pix < latitude.length; pix ++) {
                    String outLine = String.format("    %4d    %4d %11.6f %11.6f %11.6f %11.6f %11.6f %11.6f", 
                                                   line, pix, latitude[pix], longitude[pix],
                                                   solarZenith[pix], solarAzimuth[pix],
                                                   sensorZenith[pix], sensorAzimuth[pix]);
                    System.out.println(outLine);
                }
            }

            int i = 0;
            while (i < numScanLines) {
                float[] orbPos = new float[3];
                float[][] sensorMatrix = new float[3][3];
                float[] sunRef = new float[3];
                orbPos[0] = orbitData.getFloat(i);
                orbPos[1] = orbitData.getFloat(i+1);
                orbPos[2] = orbitData.getFloat(i+2);
                i += 3;
            }
        } catch(InvalidRangeException ire) {
            System.out.println("Encountered InvalidRangeException reading a data array.");
            System.out.println(ire.getMessage());
            ire.printStackTrace();
            System.out.println();
            System.exit(-43);
        } finally {
            ncFile.close();
        }
    }

    private void processVariable(String prefix, Group parentGroup, String varName) {
        Variable theVar = parentGroup.findVariable(varName);
        int rank = theVar.getRank();
        int[] shape = theVar.getShape();
        System.out.println(prefix + "Variable: " + theVar.getShortName() 
                           + ", Type: " + theVar.getDataType()
                           + ", Rank: " + rank
                           + ", Size: " + theVar.getSize()
                           + ", Shape: " + shape[0] + " x "
                           + shape[1] + "\n"
                          );
        int[] startPts = new int[rank];
        ArrayFloat dataArray = null;
        switch (rank) {
            case 2:
                dataArray = readRank2Data(theVar);
                break;
            case 3:
                dataArray = readRank3Data(theVar);
                break;
            default:
                System.out.println("Error! Encountered unexpected rank " + rank +
                                   " reading " + theVar.getShortName());
        }

        double[] dataIn = new double[(int) theVar.getSize()];
        for (int i = 0; i < theVar.getSize(); i ++) {
            dataIn[i] = dataArray.getDouble(i);
        }
/*
        for (int i = 0; i < dataIn.length; i ++) {
            System.out.println(theVar.getShortName() + "[" + i + "] = " + dataIn[i]);
        }
*/
    }

    private void readData() {
    }

    private ArrayFloat.D2 readRank2Data(Variable varToRead) {
        int [] startPts = {0, 0};
        ArrayFloat.D2 dataArray = null;
        try {
            dataArray = (ArrayFloat.D2) varToRead.read(startPts, varToRead.getShape());
        } catch(IOException ioe) {
            System.out.println("Encountered IOException reading the data array: " + varToRead.getShortName());
            System.out.println(ioe.getMessage());
            ioe.printStackTrace();
            System.out.println();
            System.exit(-42);
        } catch(InvalidRangeException ire) {
            System.out.println("Encountered InvalidRangeException reading the data array: " + varToRead.getShortName());
            System.out.println(ire.getMessage());
            ire.printStackTrace();
            System.out.println();
            System.exit(-43);
        }
        return dataArray;
    }

    private ArrayFloat.D3 readRank3Data(Variable varToRead) {
        int []startPts = {0, 0, 0};
        ArrayFloat.D3 dataArray = null;
        try {
            dataArray = (ArrayFloat.D3) varToRead.read(startPts, varToRead.getShape());
            return dataArray;
        } catch(IOException ioe) {
            System.out.println("Encountered IOException reading the data array: " + varToRead.getShortName());
            System.out.println(ioe.getMessage());
            ioe.printStackTrace();
            System.out.println();
            System.exit(-44);
        } catch(InvalidRangeException ire) {
            System.out.println("Encountered InvalidRangeException reading the data array: " + varToRead.getShortName());
            System.out.println(ire.getMessage());
            ire.printStackTrace();
            System.out.println();
            System.exit(-45);
        }
        return dataArray;
    }

    private static void redirectStderr() {
        GregorianCalendar cal = new GregorianCalendar();  // Calendar.getInstance() didn't work
        String timeStamp = String.format("%04d-%02d-%02d_%02d%02d%02d", cal.get(Calendar.YEAR),
                                         cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                                         cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                                         cal.get(Calendar.SECOND));
        /*
         * Code (selected snippets) for redirecting of stderr stolen from: 
         *     http://blogs.oracle.com/nickstephen/entry/java_redirecting_system_out_and
         */
        Handler fileHandler;
        LogManager logManager;
        try {
            logManager = LogManager.getLogManager();
            logManager.reset();
            fileHandler = new FileHandler("reader_error_log" + "_" + timeStamp, 10000, 3, true);
            fileHandler.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(fileHandler);
        } catch(IOException ioe) {
            System.out.println("Encountered IOException setting up logging:\n" +
                               ioe.getMessage() + "\n");
            ioe.printStackTrace();
        } 
    }
}

class FileNotHdfException extends Exception {
    String name;

    public FileNotHdfException(String f) {
        super(f);
    }

    public String toString() {
        return "FileNotHdfException: " + name;
    }
}
