package gov.nasa.obpg.seadas.sandbox.seawifs;

//package geonav;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.esa.beam.util.math.DoubleList;
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

    private boolean debug = false;
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
         *   - redirects stdout and stderr to files.
         *   - processes and checks the command line input.
         *   - provides help if requested and then ends execution.
         *   - instantiates a ReaderTest object if help is not requested and
         *     then calls processFile.
         *  If problems are found, an appropriate exception is thrown and the 
         *  program exits with an error.
         */


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
                        System.out.println("Processing " + inFile.getName() + " ...");
                        PrintStream oriOut = System.out;
                        PrintStream oriErr = System.err;

                        File outFile = new File("test_out.txt");
                        File errFile = new File("test_err.txt");

                        PrintStream outStr = new PrintStream(outFile);
                        PrintStream errStr = new PrintStream(errFile);

                        System.setOut(outStr);
                        System.setErr(errStr);
                        ReaderTest rt = new ReaderTest();
                        rt.processFile(inFile);
                        System.setOut(oriOut);
                        System.setErr(oriErr);
                        System.out.println("Processing compleat.");
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
            }
        } else {
            System.out.println("No input file given.");
            System.exit(99);
        }
    }

    private void outputHeader() {
        /**
         * Print header to match what the sample file from Fortran output contains.
         */
        // Output a header like what Fred provided.
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

            ArrayFloat orbitData = readData("orb_vec", navGroup);
            ArrayFloat sensorData = readData("sen_mat", navGroup);
            ArrayFloat sunData = readData("sun_ref", navGroup);
            ArrayFloat attAngleData = readData("att_ang", navGroup);
            ArrayFloat scanTrackEllipseCoefData = readData("scan_ell", navGroup);
            ArrayFloat tiltData = readData("tilt", scanLineAttrGroup);

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

                float[] scanTrackEllipseCoef = new float[6];
                scanTrackEllipseCoef[0] = scanTrackEllipseCoefData.getFloat(6 * line);
                scanTrackEllipseCoef[1] = scanTrackEllipseCoefData.getFloat(6 * line + 1);
                scanTrackEllipseCoef[2] = scanTrackEllipseCoefData.getFloat(6 * line + 2);
                scanTrackEllipseCoef[3] = scanTrackEllipseCoefData.getFloat(6 * line + 3);
                scanTrackEllipseCoef[4] = scanTrackEllipseCoefData.getFloat(6 * line + 4);
                scanTrackEllipseCoef[5] = scanTrackEllipseCoefData.getFloat(6 * line + 5);

                float tilt = tiltData.getFloat(line);

                //Geonav geonavCalculator = new Geonav(orbVect, sensorMat, sunUnitVect, attAngleVect, tilt, ncFile);
                Geonav geonavCalculator = new Geonav(orbVect, sensorMat, scanTrackEllipseCoef, sunUnitVect,
                                                     attAngleVect, tilt, ncFile);
                geonavCalculator.doComputations();

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
/*
        } catch(InvalidRangeException ire) {
            System.out.println("Encountered InvalidRangeException reading a data array.");
            System.out.println(ire.getMessage());
            ire.printStackTrace();
            System.out.println();
            System.exit(-43);
*/
        } finally {
            ncFile.close();
        }
    }

    private ArrayFloat readData(String varName, Group group) {
        ArrayFloat dataArray = null;
        int[] startPts;
        Variable varToRead = group.findVariable(varName);
if (debug) {
    System.out.print(varName + ": ");
    dispVarDetails(varToRead);
}
        if (varToRead.getRank() == 1) {
            try {
                dataArray = (ArrayFloat) varToRead.read();
            } catch(IOException ioe) {
                System.out.println("Encountered IOException reading the data array: " + varToRead.getShortName());
                System.out.println(ioe.getMessage());
                ioe.printStackTrace();
                System.out.println();
                System.exit(-43);
            }
        } else {
            if (varToRead.getRank() == 2) {
                startPts = new int[2];
                startPts[0] = 0;
                startPts[1] = 0;
            } else {
                // Assuming nothing with more than rank 3.
                startPts = new int[3];
                startPts[0] = 0;
                startPts[1] = 0;
                startPts[2] = 0;
            }
            try {
                dataArray = (ArrayFloat) varToRead.read(startPts, varToRead.getShape());
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
        }
        return dataArray;
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
