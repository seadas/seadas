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
package gov.nasa.obpg.seadas.dataio.obpg;

import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.util.io.BeamFileFilter;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SeadasProductReaderPlugIn implements ProductReaderPlugIn {

    // Set to "true" to output debugging information.
    // Don't forget to setback to "false" in production code!
    //
    private static final boolean DEBUG = false;

    private static final String DEFAULT_FILE_EXTENSION = ".hdf";
    /*
    private static final String DEFAULT_FILE_EXTENSION_L1A_GAC = ".L1A_GAC";
    private static final String DEFAULT_FILE_EXTENSION_L1A_HNSG = ".L1A_HNSG";
     */
    private static final String DEFAULT_FILE_EXTENSION_L2 = ".L2";
    private static final String DEFAULT_FILE_EXTENSION_L2_LAC = DEFAULT_FILE_EXTENSION_L2 + "_LAC";
    private static final String DEFAULT_FILE_EXTENSION_L2_LAC_OC = DEFAULT_FILE_EXTENSION_L2_LAC + "_OC";
    private static final String DEFAULT_FILE_EXTENSION_L2_LAC_SST = DEFAULT_FILE_EXTENSION_L2_LAC + "_SST";
    private static final String DEFAULT_FILE_EXTENSION_L2_LAC_SST4 = DEFAULT_FILE_EXTENSION_L2_LAC + "_SST4";
    private static final String DEFAULT_FILE_EXTENSION_L2_MLAC = DEFAULT_FILE_EXTENSION_L2 + "_MLAC";
    private static final String DEFAULT_FILE_EXTENSION_L2_MLAC_OC = DEFAULT_FILE_EXTENSION_L2_MLAC + "_OC";

    public static final String READER_DESCRIPTION = "NASA Ocean Color (OBPG) Products";
    public static final String FORMAT_NAME = "NASA-OBPG";

    private static final String[] supportedProductTypes = {
            "Aquarius Level 1A Data",
            "Aquarius Level 2 Data",
            "CZCS Level-1A Data",
            "CZCS Level-1B",
            "CZCS Level-2 Data",
            "CZCS Level-3 Standard Mapped Image",
            "HMODISA Level-2 Data",
            "HMODISA Level-3 Standard Mapped Image",
            "HMODIST Level-3 Standard Mapped Image",
            "MERIS Level-2 Data",
            "MERIS Level-1 Browse Data",
            "MERIS Level-2 Browse Data",
            "MODIS_SWATH_Type_L1B",
            "MODISA Level-1 Browse Data",
            "HMODISA Level-1 Browse Data",
            "MODISA Level-2 Browse Data",
            "HMODISA Level-2 Browse Data",
            "MODISA Level-2 Data",
            "MODIST Level-2 Data",
            "MOS Level-1B",
            "MOS Level-2 Data",
            "OSMI Level-1A Data",
            "OSMI Level-1B",
            "OSMI Level-2 Data",
            "OCM2 Level-3 Standard Mapped Image",
            "OCTS Level-1A GAC Data",
            "OCTS Level-2 Data",
            "OCTS Level-3 Standard Mapped Image",
            "SeaWiFS Near Real-Time Ancillary Data",
            "SeaWiFS Level-1B",
            "SeaWiFS Level-1A Data",
            "SeaWiFS Level-2 Data",
            "SeaWiFS Level-3 Standard Mapped Image",
            "VIIRS Level-3 Standard Mapped Image",
            "Level-3 Standard Mapped Image",
            "SeaWiFS Level-3 Binned Data",
            "CZCS Level-3 Binned Data",
            "OCTS Level-3 Binned Data",
            "HMODISA Level-3 Binned Data",
            "HMODIST Level-3 Binned Data",
            "MERIS Level-3 Binned Data",
            "MODIS Level-3 Binned Data",
            "OSMI Level-3 Binned Data",
            "OCM2 Level-3 Binned Data",
            "Aquarius Level-3 Binned Data",
            "Level-3 Binned Data",
            "VIIRS Level-3 Binned Data",
            "VIIRSN Level-2 Data",
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
                Attribute platformShortName = ncfile.findGlobalAttribute("Platform_Short_Name");

                Group modisl1bGroup = ncfile.findGroup("MODIS_SWATH_Type_L1B");
                List<Variable> seadasMappedVariables = ncfile.getVariables();
                Boolean isSeadasMapped = false;
                try {
                    isSeadasMapped = seadasMappedVariables.get(0).findAttribute("Projection Category").isString();
                } catch (Exception e) {
                }

                if (titleAttribute != null || modisl1bGroup != null || platformShortName != null) {
                    if (titleAttribute != null){
                        final String title = titleAttribute.getStringValue();
                        if (title != null) {
                            if (supportedProductTypeSet.contains(title.trim())) {
                                if (DEBUG) {
                                    System.out.println(file);
                                }
                                return DecodeQualification.INTENDED;
                            } else {
                                if (DEBUG) {
                                    System.out.println("# Unrecognized attribute Title=[" + title + "]: " + file);
                                }
                            }
                        }
                    } else if (modisl1bGroup != null) {
                        final String shortname = modisl1bGroup.getShortName();
                        if (shortname != null) {
                            if (supportedProductTypeSet.contains(shortname.trim())) {
                                if (DEBUG) {
                                    System.out.println(file);
                                }
                                return DecodeQualification.INTENDED;
                            } else {
                                if (DEBUG) {
                                    System.out.println("# Unrecognized attribute group=[" + shortname + "]: " + file);
                                }
                            }
                        }
                    } else {
//                        must be NPP
                        String platformName = platformShortName.getStringValue();
                        if (platformName.equals("NPP")){
                            Group dataProduct = ncfile.findGroup("Data_Products");
                            String dataProductList0 = dataProduct.getGroups().get(0).getShortName();
                            if (dataProductList0.matches("VIIRS.*DR")) {
                                return DecodeQualification.INTENDED;
                            }
                        }else {
                            if (DEBUG) {
                                    System.out.println("# Unrecognized platform=[" + platformName + "]: " + file);
                                }
                        }

                    }
                } else if (isSeadasMapped) {
                    return DecodeQualification.INTENDED;
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
        } catch (IOException ignore) {
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
                DEFAULT_FILE_EXTENSION_L2,
                DEFAULT_FILE_EXTENSION_L2_LAC,
                DEFAULT_FILE_EXTENSION_L2_LAC_OC,
                DEFAULT_FILE_EXTENSION_L2_LAC_SST,
                DEFAULT_FILE_EXTENSION_L2_LAC_SST4,
                DEFAULT_FILE_EXTENSION_L2_MLAC,
                DEFAULT_FILE_EXTENSION_L2_MLAC_OC
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
