package gov.nasa.obpg.seadas.sandbox.seawifs;

//package geonav;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import ucar.ma2.*;
import ucar.nc2.*;

import org.apache.commons.cli.*;

import gov.nasa.obpg.seadas.dataio.obpg.*;

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
         *  four characters of the file contain the HDF header:
         *       Ctrl-N Ctrl-C Ctrl-S Ctrl-A (no spaces).
         *
         */
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
        boolean fGood;// = false;
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
                        Calendar now = GregorianCalendar.getInstance();
                        String startingMessage = String.format("Processing of %s started at: %04d-%02d-%02dT%02d:%02d:%02d",
                                                               inFile.getName(), now.get(Calendar.YEAR),
                                                               now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH),
                                                               now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
                                                               now.get(Calendar.SECOND)
                                                              );
                        System.out.println(startingMessage);

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
        // Output a header identical to the output from Fred's Fortran.
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
            ObpgGeonav geonavCalculator = new ObpgGeonav(ncFile);
            int numLines = geonavCalculator.getNumberScanLines();
            int numPixels = geonavCalculator.getNumberPixels();
            float[][] latitudes = geonavCalculator.getLatitudes();
            float[][] longitudes = geonavCalculator.getLongitudes();
            float[][] solarAzimuths = geonavCalculator.getSolarAzimuths();
            float[][] solarZeniths = geonavCalculator.getSolarZeniths();
            float[][] sensorAzimuths = geonavCalculator.getSensorAzimuths();
            float[][] sensorZeniths = geonavCalculator.getSensorZeniths();

            System.out.println("numLines = " + numLines + ", numPixels = " + numPixels);
            outputHeader();
            for (int line = 0; line < numLines; line ++) {
                for (int pix = 0; pix < numPixels; pix ++) {
                    String outLine = String.format("    %4d    %4d %11.6f %11.6f%11.6f %11.6f %11.6f %11.6f",
                                                   line, pix,
                                                   latitudes[line][pix], longitudes[line][pix],
                                                   solarZeniths[line][pix], solarAzimuths[line][pix],
                                                   sensorZeniths[line][pix], sensorAzimuths[line][pix]);
                    System.out.println(outLine);
                }
            }
        } finally {
            ncFile.close();
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
