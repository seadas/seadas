/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package gov.nasa.gsfc.seadas.dataio;

import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.util.io.BeamFileFilter;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class L1ProductReaderPlugIn implements ProductReaderPlugIn {

    // Set to "true" to output debugging information.
    // Don't forget to setback to "false" in production code!
    //
    private static final boolean DEBUG = false;

    private static final String DEFAULT_FILE_EXTENSION = ".hdf";

    private static final String DEFAULT_FILE_EXTENSION_L1A_GAC = ".L1A_GAC";
    private static final String DEFAULT_FILE_EXTENSION_L1A_LAC = ".L1A_LAC";
    private static final String DEFAULT_FILE_EXTENSION_L1B_GAC = ".L1B_GAC";
    private static final String DEFAULT_FILE_EXTENSION_L1B_LAC = ".L1B_LAC";
    private static final String DEFAULT_FILE_EXTENSION_L1A_MLAC = ".L1A_MLAC";
    private static final String DEFAULT_FILE_EXTENSION_L1A_HRPT = ".L1A_H*";


    public static final String READER_DESCRIPTION = "SeaDAS-Supported Level 1 Products";
    public static final String FORMAT_NAME = "SeaDAS-L1";

    private static final String[] supportedProductTypes = {
            "CZCS Level-1A Data",
            "CZCS Level-1B",
            "MOS Level-1B",
            "OSMI Level-1A Data",
            "OSMI Level-1B",
            "OCTS Level-1A GAC Data",
//            "SeaWiFS Near Real-Time Ancillary Data",
            "SeaWiFS Level-1B",
            "SeaWiFS Level-1A Data",
    };
    private static final Set<String> supportedProductTypeSet = new HashSet<String>(Arrays.asList(supportedProductTypes));

    /**
     * Checks whether the given object is an acceptable input for this product reader and if so, the method checks if it
     * is capable of decoding the input's content.
     */
    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        final File file = getInputFile(input);
        if (file == null) {
            return DecodeQualification.UNABLE;
        }
        if (!file.exists()) {
            if (DEBUG) {
                System.out.println("# File not found: " + file);
            }
            return DecodeQualification.UNABLE;
        }
        if (!file.isFile()) {
            if (DEBUG) {
                System.out.println("# Not a file: " + file);
            }
            return DecodeQualification.UNABLE;
        }
        NetcdfFile ncfile = null;
        try {
            if (NetcdfFile.canOpen(file.getPath())) {
                ncfile = NetcdfFile.open(file.getPath());
                Attribute titleAttribute = ncfile.findGlobalAttribute("Title");

                if (titleAttribute != null) {

                    final String title = titleAttribute.getStringValue();
                    if (title != null) {
                        if (supportedProductTypeSet.contains(title.trim())) {
                            if (DEBUG) {
                                System.out.println(file);
                            }
                            ncfile.close();
                            return DecodeQualification.INTENDED;
                        } else {
                            if (DEBUG) {
                                System.out.println("# Unrecognized attribute Title=[" + title + "]: " + file);
                            }
                        }
                    }
                } else {
                    if (DEBUG) {
                        System.out.println("# Missing attribute 'Title': " + file);
                    }
                }
            } else {
                if (DEBUG) {
                    System.out.println("# Can't open as NetCDF: " + file);
                }
            }
        } catch (Exception ignore) {
            if (DEBUG) {
                System.out.println("# I/O exception caught: " + file);
            }
        } finally {
            if (ncfile != null) {
                try {
                    ncfile.close();
                } catch (IOException ignore) {
                }
            }
        }
        return DecodeQualification.UNABLE;
    }

    /**
     * Returns an array containing the classes that represent valid input types for this reader.
     * <p/>
     * <p> Intances of the classes returned in this array are valid objects for the <code>setInput</code> method of the
     * <code>ProductReader</code> interface (the method will not throw an <code>InvalidArgumentException</code> in this
     * case).
     *
     * @return an array containing valid input types, never <code>null</code>
     */
    @Override
    public Class[] getInputTypes() {
        return new Class[]{String.class, File.class};
    }

    /**
     * Creates an instance of the actual product reader class. This method should never return <code>null</code>.
     *
     * @return a new reader instance, never <code>null</code>
     */
    @Override
    public ProductReader createReaderInstance() {
        return new SeadasProductReader(this);
    }

    @Override
    public BeamFileFilter getProductFileFilter() {
        String[] formatNames = getFormatNames();
        String formatName = "";
        if (formatNames.length > 0) {
            formatName = formatNames[0];
        }
        return new BeamFileFilter(formatName, getDefaultFileExtensions(), getDescription(null));
    }

    /**
     * Gets the default file extensions associated with each of the format names returned by the <code>{@link
     * #getFormatNames}</code> method. <p>The string array returned shall always have the same length as the array
     * returned by the <code>{@link #getFormatNames}</code> method. <p>The extensions returned in the string array shall
     * always include a leading colon ('.') character, e.g. <code>".hdf"</code>
     *
     * @return the default file extensions for this product I/O plug-in, never <code>null</code>
     */
    @Override
    public String[] getDefaultFileExtensions() {
        // todo: return regular expression to clean up the extensions.
        return new String[]{
                DEFAULT_FILE_EXTENSION,
                DEFAULT_FILE_EXTENSION_L1A_GAC,
                DEFAULT_FILE_EXTENSION_L1A_LAC,
                DEFAULT_FILE_EXTENSION_L1B_GAC,
                DEFAULT_FILE_EXTENSION_L1B_LAC,
                DEFAULT_FILE_EXTENSION_L1A_MLAC,
                DEFAULT_FILE_EXTENSION_L1A_HRPT
        };
    }

    /**
     * Gets a short description of this plug-in. If the given locale is set to <code>null</code> the default locale is
     * used.
     * <p/>
     * <p> In a GUI, the description returned could be used as tool-tip text.
     *
     * @param locale the local for the given decription string, if <code>null</code> the default locale is used
     * @return a textual description of this product reader/writer
     */
    @Override
    public String getDescription(Locale locale) {
        return READER_DESCRIPTION;
    }

    /**
     * Gets the names of the product formats handled by this product I/O plug-in.
     *
     * @return the names of the product formats handled by this product I/O plug-in, never <code>null</code>
     */
    @Override
    public String[] getFormatNames() {
        return new String[]{FORMAT_NAME};
    }

    public File getInputFile(Object input) {
        File inputFile;
        if (input instanceof File) {
            inputFile = (File) input;
        } else if (input instanceof String) {
            inputFile = new File((String) input);
        } else {
            return null;
        }
        return inputFile;
    }


}
