/*
 * Copyright (C) 2014 Brockmann Consult GmbH (info@brockmann-consult.de)
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

import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.*;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class L1BPaceOciFileReader extends SeadasFileReader {

    L1BPaceOciFileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    enum WvlType {
        RED("red_wavelengths"),
        BLUE("blue_wavelengths"),
        SWIR("swir_wavelenghts");

        private String name;

        private WvlType(String nm) {
            name = nm;
        }

        public String toString() {
            return name;
        }
    }

    Array blue_wavlengths = null;
    Array red_wavlengths = null;
    Array swir_wavlengths = null;


    @Override
    public Product createProduct() throws ProductIOException {

        int sceneHeight = ncFile.findDimension("number_of_scans").getLength();
        int sceneWidth = ncFile.findDimension("ccd_pixels").getLength();

        String productName;

        try {
            productName = getStringAttribute("product_name");
        } catch (Exception ignored) {
            productName = productReader.getInputFile().getName();
        }

        Variable blueWvl = ncFile.findVariable("sensor_band_parameters/blue_wavelength");
        Variable redWvl = ncFile.findVariable("sensor_band_parameters/red_wavelength");
        Variable swirWvl = ncFile.findVariable("sensor_band_parameters/SWIR_wavelength");
        try {
            blue_wavlengths = blueWvl.read();
            red_wavlengths = redWvl.read();
            //swir_wavlengths = swirWvl.read();


            // somehow there are duplicate bands in the test file.
            // fixme
            swir_wavlengths = Array.factory(new float[]{940, 1038, 1250, 1251, 1378, 1615, 1616, 2130, 2260});



        } catch (IOException e) {
            throw new ProductIOException(e.getMessage(), e);
        }
        mustFlipX = mustFlipY = getDefaultFlip();

        SeadasProductReader.ProductType productType = productReader.getProductType();

        Product product = new Product(productName, productType.toString(), sceneWidth, sceneHeight);
        product.setDescription(productName);

        Attribute startTime = findAttribute("time_coverage_start");
        ProductData.UTC utcStart = getUTCAttribute("time_coverage_start");
        ProductData.UTC utcEnd = getUTCAttribute("time_coverage_end");
        if (startTime == null) {
            utcStart = getUTCAttribute("Start_Time");
            utcEnd = getUTCAttribute("End_Time");
        }
        // only needed as a stop-gap to handle an intermediate version of l2gen metadata
        if (utcEnd == null) {
            utcEnd = getUTCAttribute("time_coverage_stop");
        }

        product.setFileLocation(productReader.getInputFile());
        product.setProductReader(productReader);

        addGlobalMetadata(product);
        variableMap = addOciBands(product, ncFile.getVariables());

        addGeocoding(product);
        addMetadata(product, "products", "Band_Metadata");
        addMetadata(product, "navigation", "Navigation_Metadata");

        product.setAutoGrouping("Lt");

        return product;
    }


    public void addMetadata(Product product, String groupname, String meta_element) throws ProductIOException {
        Group group = ncFile.findGroup(groupname);

        if (group != null) {
            final MetadataElement bandAttributes = new MetadataElement(meta_element);
            List<Variable> variables = group.getVariables();
            for (Variable variable : variables) {
                final String name = variable.getShortName();
                final MetadataElement sdsElement = new MetadataElement(name + ".attributes");
                final int dataType = getProductDataType(variable);
                final MetadataAttribute prodtypeattr = new MetadataAttribute("data_type", dataType);

                sdsElement.addAttribute(prodtypeattr);
                bandAttributes.addElement(sdsElement);

                final List<Attribute> list = variable.getAttributes();
                for (Attribute varAttribute : list) {
                    addAttributeToElement(sdsElement, varAttribute);
                }
            }
            final MetadataElement metadataRoot = product.getMetadataRoot();
            metadataRoot.addElement(bandAttributes);
        }
    }

    private Map<Band, Variable> addOciBands(Product product, List<Variable> variables) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        Band band;

        Map<Band, Variable> bandToVariableMap = new HashMap<Band, Variable>();
        int spectralBandIndex = 0;
        for (Variable variable : variables) {
            if (variable.getParentGroup().equals("sensor_band_parameters") || variable.getParentGroup().equals("scan_line_attributes"))
                continue;
            if ((variable.getShortName().equals("latitude")) || (variable.getShortName().equals("longitude")))
                continue;
            int variableRank = variable.getRank();

            if (variableRank == 2) {
                final int[] dimensions = variable.getShape();
                final int height = dimensions[0];
                final int width = dimensions[1];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    // final List<Attribute> list = variable.getAttributes();

                    String units = variable.getUnitsString();
                    String name = variable.getShortName();
                    final int dataType = getProductDataType(variable);
                    band = new Band(name, dataType, width, height);
                    product.addBand(band);

                    final List<Attribute> list = variable.getAttributes();
                    for (Attribute hdfAttribute : list) {
                        final String attribName = hdfAttribute.getShortName();
                        if ("units".equals(attribName)) {
                            band.setUnit(hdfAttribute.getStringValue());
                        } else if ("long_name".equals(attribName)) {
                            band.setDescription(hdfAttribute.getStringValue());
                        } else if ("slope".equals(attribName)) {
                            band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("intercept".equals(attribName)) {
                            band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("scale_factor".equals(attribName)) {
                            band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("add_offset".equals(attribName)) {
                            band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("bad_value_scaled".equals(attribName)) {
                            band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                            band.setNoDataValueUsed(true);
                        }
                    }
                    bandToVariableMap.put(band, variable);
                    band.setUnit(units);
                    band.setDescription(variable.getDescription());
                }
            } else if (variableRank == 3) {
                final int[] dimensions = variable.getShape();
                final int bands = dimensions[0];
                final int height = dimensions[1];
                final int width = dimensions[2];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    // final List<Attribute> list = variable.getAttributes();

                    String units = variable.getUnitsString();
                    String description = variable.getShortName();

                    for (int i = 0; i < bands; i++) {
                        final float wavelength = getOciWvl(i, getWvlType(variable.getShortName()));
                        StringBuilder longname = new StringBuilder(description);
                        longname.append("_");
                        longname.append(wavelength);
                        String name = longname.toString();
                        final int dataType = getProductDataType(variable);
                        band = new Band(name, dataType, width, height);
                        product.addBand(band);

                        band.setSpectralWavelength(wavelength);
                        band.setSpectralBandIndex(spectralBandIndex++);

                        Variable sliced = null;
                        try {
                            sliced = variable.slice(0, i);
                        } catch (InvalidRangeException e) {
                            e.printStackTrace();  //Todo change body of catch statement.
                        }

                        final List<Attribute> list = variable.getAttributes();
                        for (Attribute hdfAttribute : list) {
                            final String attribName = hdfAttribute.getShortName();
                            if ("units".equals(attribName)) {
                                band.setUnit(hdfAttribute.getStringValue());
                            } else if ("long_name".equals(attribName)) {
                                band.setDescription(hdfAttribute.getStringValue());
                            } else if ("slope".equals(attribName)) {
                                band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                            } else if ("intercept".equals(attribName)) {
                                band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                            } else if ("scale_factor".equals(attribName)) {
                                band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                            } else if ("add_offset".equals(attribName)) {
                                band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                            } else if ("bad_value_scaled".equals(attribName)) {
                                band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                                band.setNoDataValueUsed(true);
                            }
                        }
                        bandToVariableMap.put(band, sliced);
                        band.setUnit(units);
                        band.setDescription(description);

                    }
                }
            }


        }
        return bandToVariableMap;
    }

    private WvlType getWvlType(String productName) {
        WvlType wvltype = null;
        if (productName.equals("Lt_blue")) {
            wvltype = WvlType.BLUE;
        } else if (productName.equals("Lt_red")) {
            wvltype = WvlType.RED;
        } else if (productName.equals("Lt_SWIR")) {
            wvltype = WvlType.SWIR;
        }
        return wvltype;
    }

    private float getOciWvl(int index, WvlType wvlEnum) {
        float wvl;
        switch (wvlEnum) {
            case RED:
                wvl = red_wavlengths.getFloat(index);
                break;
            case BLUE:
                wvl = blue_wavlengths.getFloat(index);
                break;
            case SWIR:
                wvl = swir_wavlengths.getFloat(index);
                break;
            default:
                wvl = 0;
        }
        return wvl;
    }

    public ProductData readDataFlip(Variable variable) throws ProductIOException {
        final int dataType = getProductDataType(variable);
        Array array;
        Object storage;
        try {
            array = variable.read();
            storage = array.flip(0).copyTo1DJavaArray();
        } catch (IOException e) {
            throw new ProductIOException(e.getMessage(), e);
        }
        return ProductData.createInstance(dataType, storage);
    }

    public void addGeocoding(final Product product) throws ProductIOException {
        final String longitude = "longitude";
        final String latitude = "latitude";
        String navGroup = "geolocation_data";

        Variable latVar = ncFile.findVariable(navGroup + "/" + latitude);
        Variable lonVar = ncFile.findVariable(navGroup + "/" + longitude);

        if (latVar != null && lonVar != null) {
            final ProductData lonRawData;
            final ProductData latRawData;
            if (mustFlipY) {
                lonRawData = readDataFlip(lonVar);
                latRawData = readDataFlip(latVar);
            } else {
                lonRawData = readData(lonVar);
                latRawData = readData(latVar);
            }

            Band latBand = product.addBand(latVar.getShortName(), ProductData.TYPE_FLOAT32);
            Band lonBand = product.addBand(lonVar.getShortName(), ProductData.TYPE_FLOAT32);
            latBand.setNoDataValue(-999.);
            lonBand.setNoDataValue(-999.);
            latBand.setNoDataValueUsed(true);
            lonBand.setNoDataValueUsed(true);
            latBand.setData(latRawData);
            lonBand.setData(lonRawData);

            product.setGeoCoding(GeoCodingFactory.createPixelGeoCoding(latBand, lonBand, null, 5));

        }
    }
}