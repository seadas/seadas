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

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.*;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObpgUtils {

    static final String MODIS_L1B_TYPE = "MODIS_SWATH_Type_L1B";
    static final String KEY_NAME = "Product Name";
    static final String KEY_TYPE = "Title";
    static final String KEY_WIDTH = "Pixels per Scan Line";
    static final String KEY_HEIGHT = "Number of Scan Lines";
    static final String KEY_WIDTH_MODISL1 = "Max Earth View Frames";
    static final String KEY_HEIGHT_MODISL1 = "Number of Scans";
    static final String KEY_START_NODE = "Start Node";
    static final String KEY_END_NODE = "End Node";
    static final String KEY_START_TIME = "Start Time";
    static final String KEY_END_TIME = "End Time";
    static final String SENSOR_BAND_PARAMETERS = "Sensor_Band_Parameters";
    static final String SENSOR_BAND_PARAMETERS_GROUP = "Sensor Band Parameters";
    static final String SCAN_LINE_ATTRIBUTES = "Scan_Line_Attributes";
    static final String SCAN_LINE_ATTRIBUTES_GROUP = "Scan-Line Attributes";

    static final String KEY_L3SMI_HEIGHT = "Number of Lines";
    static final String KEY_L3SMI_WIDTH = "Number of Columns";
    static final String SMI_PRODUCT_PARAMETERS = "SMI Product Parameters";

    MetadataAttribute attributeToMetadata(Attribute attribute) {
        final int productDataType = getProductDataType(attribute.getDataType(), false, false);
        if (productDataType != -1) {
            ProductData productData;
            if (attribute.isString()) {
                productData = ProductData.createInstance(attribute.getStringValue());
            } else if (attribute.isArray()) {
                productData = ProductData.createInstance(productDataType, attribute.getLength());
                productData.setElems(attribute.getValues().getStorage());
            } else {
                productData = ProductData.createInstance(productDataType, 1);
                productData.setElems(attribute.getValues().getStorage());
            }
            return new MetadataAttribute(attribute.getName(), productData, true);
        }
        return null;
    }

    public static int getProductDataType(Variable variable) {
        return getProductDataType(variable.getDataType(), variable.isUnsigned(), true);
    }

    public static int getProductDataType(DataType dataType, boolean unsigned, boolean rasterDataOnly) {
        if (dataType == DataType.BYTE) {
            return unsigned ? ProductData.TYPE_UINT8 : ProductData.TYPE_INT8;
        } else if (dataType == DataType.SHORT) {
            return unsigned ? ProductData.TYPE_UINT16 : ProductData.TYPE_INT16;
        } else if (dataType == DataType.INT) {
            return unsigned ? ProductData.TYPE_UINT32 : ProductData.TYPE_INT32;
        } else if (dataType == DataType.FLOAT) {
            return ProductData.TYPE_FLOAT32;
        } else if (dataType == DataType.DOUBLE) {
            return ProductData.TYPE_FLOAT64;
        } else if (!rasterDataOnly) {
            if (dataType == DataType.CHAR) {
                // return ProductData.TYPE_ASCII; TODO - handle this case
            } else if (dataType == DataType.STRING) {
                return ProductData.TYPE_ASCII;
            }
        }
        return -1;
    }

    public static File getInputFile(final Object o) {
        final File inputFile;
        if (o instanceof File) {
            inputFile = (File) o;
        } else if (o instanceof String) {
            inputFile = new File((String) o);
        } else {
            throw new IllegalArgumentException("unsupported input source: " + o);
        }
        return inputFile;
    }

    public Product createProductBody(List<Attribute> globalAttributes, String productType) throws ProductIOException {
        int sceneWidth;
        int sceneHeight;
        String keyWidth;
        String keyHeight;
        if (productType.contains(MODIS_L1B_TYPE)){
            keyWidth = KEY_WIDTH_MODISL1;
            keyHeight = KEY_HEIGHT_MODISL1;
            sceneWidth = getIntAttribute(keyWidth, globalAttributes);
            sceneHeight = getIntAttribute(keyHeight, globalAttributes) * 10;
        } else {
            keyWidth = getWidthKey(getStringAttribute(KEY_TYPE, globalAttributes));
            keyHeight = getHeightKey(getStringAttribute(KEY_TYPE, globalAttributes));
            sceneWidth = getIntAttribute(keyWidth, globalAttributes);
            sceneHeight = getIntAttribute(keyHeight, globalAttributes);
        }

        return createProduct(productType, globalAttributes, sceneWidth, sceneHeight);
    }

    private Product createProduct(String productType, List<Attribute> globalAttributes, int sceneWidth, int sceneHeight) throws ProductIOException {
        String productName;
        if (productType.contains(MODIS_L1B_TYPE)){
            productName = "MODIS L1B";
        } else {
            productName = getStringAttribute(KEY_NAME, globalAttributes);
        }

        final Product product = new Product(productName, productType, sceneWidth, sceneHeight);
        product.setDescription(productName);

        ProductData.UTC utcStart = getUTCAttribute(KEY_START_TIME, globalAttributes);
        if (utcStart != null) {
            product.setStartTime(utcStart);
        }
        ProductData.UTC utcEnd = getUTCAttribute(KEY_END_TIME, globalAttributes);
        if (utcEnd != null) {
            product.setEndTime(utcEnd);
        }
        return product;
    }

    private String getHeightKey(String title) {
        if (title.contains("Level-2")) {
            return KEY_HEIGHT;
        } else {
            return KEY_L3SMI_HEIGHT;
        }
    }

    private String getWidthKey(String title) {
        if (title.contains("Level-2")) {
            return KEY_WIDTH;
        } else {
            return KEY_L3SMI_WIDTH;
        }
    }

    public String getProductType(final NetcdfFile ncfile) throws ProductIOException {

        Attribute titleAttribute = ncfile.findGlobalAttribute("Title");
        Group modisl1bGroup = ncfile.findGroup("MODIS_SWATH_Type_L1B");

        if (titleAttribute != null){
            List<Attribute> globalAttributes = ncfile.getGlobalAttributes();
            return "OBPG " + getStringAttribute(KEY_TYPE, globalAttributes);
        } else if (modisl1bGroup != null) {
            return  modisl1bGroup.getShortName();
        } else {
            throw new ProductIOException("Unrecognized file!");
        }
    }

    private ProductData.UTC getUTCAttribute(String key, List<Attribute> globalAttributes) {
        Attribute attribute = findAttribute(key, globalAttributes);
        if (attribute != null) {
            String timeString = attribute.getStringValue().trim();
            final DateFormat dateFormat = ProductData.UTC.createDateFormat("yyyyDDDHHmmssSSS");
            try {
                final Date date = dateFormat.parse(timeString);
                String milliSeconds = timeString.substring(timeString.length() - 3);
                return ProductData.UTC.create(date, Long.parseLong(milliSeconds) * 1000);
            } catch (ParseException e) {
            }
        }
        return null;
    }

    private String getStringAttribute(String key, List<Attribute> globalAttributes) throws ProductIOException {
        Attribute attribute = findAttribute(key, globalAttributes);
        if (attribute == null || attribute.getLength() != 1) {
            throw new ProductIOException("Global attribute '" + key + "' is missing.");
        } else {
            return attribute.getStringValue().trim();
        }
    }

    private int getIntAttribute(String key, List<Attribute> globalAttributes) throws ProductIOException {
        Attribute attribute = findAttribute(key, globalAttributes);
        if (attribute == null) {
            throw new ProductIOException("Global attribute '" + key + "' is missing.");
        } else {
            return attribute.getNumericValue(0).intValue();
        }
    }

    private Attribute findAttribute(String name, List<Attribute> attributesList) {
        for (Attribute a : attributesList) {
            if (name.equals(a.getName()))
                return a;
        }
        return null;
    }

    public boolean mustFlip(final NetcdfFile ncfile) throws ProductIOException {
        Attribute startAttr = ncfile.findGlobalAttributeIgnoreCase(KEY_START_NODE);
        boolean startNodeAscending = false;
        if (startAttr != null) {
            startNodeAscending = "Ascending".equalsIgnoreCase(startAttr.getStringValue().trim());
        }
        Attribute endAttr = ncfile.findGlobalAttributeIgnoreCase(KEY_END_NODE);
        boolean endNodeAscending = false;
        if (endAttr != null) {
            endNodeAscending = "Ascending".equalsIgnoreCase(endAttr.getStringValue().trim());
        }

        return (startNodeAscending && endNodeAscending);
    }

    public void addGlobalMetadata(final Product product, List<Attribute> globalAttributes) {
        final MetadataElement globalElement = new MetadataElement("Global_Attributes");
        addAttributesToElement(globalAttributes, globalElement);

        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(globalElement);
    }

    public void addSmiMetadata(final Product product, Variable variable ) {
        List<Attribute> variableAttributes = variable.getAttributes();
        final MetadataElement smiElement = new MetadataElement(SMI_PRODUCT_PARAMETERS);
        addAttributesToElement(variableAttributes, smiElement);

        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(smiElement);
    }

    public void addScientificMetadata(Product product, NetcdfFile ncFile) throws IOException {

        Group group = ncFile.findGroup(SCAN_LINE_ATTRIBUTES_GROUP);
        if (group != null) {
            final MetadataElement scanLineAttrib = getMetadataElementSave(product, SCAN_LINE_ATTRIBUTES);
            handleMetadataGroup(group, scanLineAttrib);
        }

        group = ncFile.findGroup(SENSOR_BAND_PARAMETERS_GROUP);
        if (group != null) {
            final MetadataElement sensorBandParam = getMetadataElementSave(product, SENSOR_BAND_PARAMETERS);
            handleMetadataGroup(group, sensorBandParam);
        }

        Variable l3mvar = ncFile.findVariable("l3m_data");
        if (l3mvar != null) {
            addSmiMetadata(product, l3mvar);
        }
    }

    private void handleMetadataGroup(Group group, MetadataElement metadataElement) throws IOException {
        List<Variable> variables = group.getVariables();
        for (Variable variable : variables) {
            final String name = variable.getShortName();
            final int dataType = getProductDataType(variable);
            Array array = variable.read();
            final ProductData data = ProductData.createInstance(dataType, array.getStorage());
            final MetadataAttribute attribute = new MetadataAttribute("data", data, true);

            final MetadataElement sdsElement = new MetadataElement(name);
            sdsElement.addAttribute(attribute);
            metadataElement.addElement(sdsElement);

            final List<Attribute> list = variable.getAttributes();
            for (Attribute hdfAttribute : list) {
                final String attribName = hdfAttribute.getName();
                if ("units".equals(attribName)) {
                    attribute.setUnit(hdfAttribute.getStringValue());
                } else if ("long_name".equals(attribName)) {
                    attribute.setDescription(hdfAttribute.getStringValue());
                } else {
                    addAttributeToElement(sdsElement, hdfAttribute);
                }
            }
        }
    }


    private MetadataElement getMetadataElementSave(Product product, String name) {
        final MetadataElement metadataElement = product.getMetadataRoot().getElement(name);
        final MetadataElement namedElem;
        if (metadataElement == null) {
            namedElem = new MetadataElement(name);
            product.getMetadataRoot().addElement(namedElem);
        } else {
            namedElem = metadataElement;
        }
        return namedElem;
    }

    public Map<Band, Variable> addBands(Product product,
                                         List<Variable> variables,
                                         Map<String, String> bandInfoMap,
                                         Map<String, String> flagsInfoMap) {
        final Map<Band, Variable> bandToVariableMap = new HashMap<Band, Variable>();
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        int spectralBandIndex = 0;
        for (Variable variable : variables) {
            if (variable.getRank() == 2) {
                final int[] dimensions = variable.getShape();
                final int height = dimensions[0];
                final int width = dimensions[1];
                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    final String name = variable.getShortName();
                    final int dataType = getProductDataType(variable);
                    final Band band = new Band(name, dataType, width, height);
                    final String validExpression = bandInfoMap.get(name);
                    if (validExpression != null && !validExpression.equals("")) {
                        band.setValidPixelExpression(validExpression);
                    }
                    product.addBand(band);
                    if (name.matches("\\w+_\\d{3,}")) {
                        final float wavelength = Float.parseFloat(name.split("_")[1]);
                        band.setSpectralWavelength(wavelength);
                        band.setSpectralBandIndex(spectralBandIndex++);
                    }

                    bandToVariableMap.put(band, variable);
                    final List<Attribute> list = variable.getAttributes();
                    FlagCoding flagCoding = null;
                    for (Attribute hdfAttribute : list) {
                        final String attribName = hdfAttribute.getName();
                        if ("units".equals(attribName)) {
                            band.setUnit(hdfAttribute.getStringValue());
                        } else if ("long_name".equals(attribName)) {
                            band.setDescription(hdfAttribute.getStringValue());
                        } else if ("slope".equals(attribName)) {
                            band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("intercept".equals(attribName)) {
                            band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if (attribName.matches("f\\d\\d_name")) {
                            if (flagCoding == null) {
                                flagCoding = new FlagCoding(name);
                            }
                            final String flagName = hdfAttribute.getStringValue();
                            final int flagMask = convertToFlagMask(attribName);
                            flagCoding.addFlag(flagName, flagMask, flagsInfoMap.get(flagName));
                        }
                    }
                    if (flagCoding != null) {
                        band.setSampleCoding(flagCoding);
                        product.getFlagCodingGroup().add(flagCoding);
                    }
                }
            }
             if (variable.getRank() == 3) {
                final int[] dimensions = variable.getShape();
                final int bands = dimensions[0];
                final int height = dimensions[1];
                final int width = dimensions[2];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    final List<Attribute> list = variable.getAttributes();
                    Attribute band_names = findAttribute("band_names", list);
                    if (band_names != null){
                        String bnames = band_names.getStringValue();
                        String[] bname_array = bnames.split(",");
                        String units = null;
                        String description = null;
                        Array slope = null;
                        Array intercept = null;
                        FlagCoding flagCoding = null;
                        for (Attribute hdfAttribute : list) {
                            final String attribName = hdfAttribute.getName();
                            if ("units".equals(attribName)) {
                                units = hdfAttribute.getStringValue();
                            } else if ("long_name".equals(attribName)) {
                                description = hdfAttribute.getStringValue();
                            } else if ("reflectance_scales".equals(attribName)) {
                                slope = hdfAttribute.getValues();
                            } else if ("reflectance_offsets".equals(attribName)) {
                                intercept = hdfAttribute.getValues();
                            } else if (slope == null && "radiance_scales".equals(attribName)){
                                slope = hdfAttribute.getValues();
                            } else if (intercept == null && "radiance_offsets".equals(attribName)){
                                intercept = hdfAttribute.getValues();
                            }
                        }
                        int i = 0;
                        for (String bandname: bname_array){
                            final String shortname = variable.getShortName();
                            StringBuilder longname = new StringBuilder(shortname);
                            longname.append("_");
                            longname.append(bandname);
                            String name = longname.toString();
                            final int dataType = getProductDataType(variable);
                            final Band band = new Band(name, dataType, width, height);
                            final String validExpression = bandInfoMap.get(name);
                            if (validExpression != null && !validExpression.equals("")) {
                                band.setValidPixelExpression(validExpression);
                            }
                            product.addBand(band);
                            if (name.matches("\\w+_\\d{3,}")) {
                                final float wavelength = Float.parseFloat(name.split("_")[1]);
                                band.setSpectralWavelength(wavelength);
                                band.setSpectralBandIndex(spectralBandIndex++);
                            }
                            Variable sliced = null;
                            try {
                                sliced = variable.slice(0, i);
                            } catch (InvalidRangeException e) {
                                e.printStackTrace();  //Todo change body of catch statement.
                            }
                            bandToVariableMap.put(band, sliced);
                            band.setUnit(units);
                            band.setDescription(description);
                            if (slope != null){
                                band.setScalingFactor(slope.getDouble(i));

                                if (intercept != null)
                                    band.setScalingOffset(intercept.getDouble(i)*slope.getDouble(i));
                            }
                            if (flagCoding != null) {
                                band.setSampleCoding(flagCoding);
                                product.getFlagCodingGroup().add(flagCoding);
                            }
                            i++;
                        }
                    }
                }
            }
        }
        return bandToVariableMap;
    }

    public void addGeocoding(final Product product, NetcdfFile ncfile, boolean mustFlip) throws IOException {
        final String navGroup = "Navigation Data";
        final String longitude = "longitude";
        final String latitude = "latitude";
        String cntlPoints = "cntl_pt_cols";
        Band latBand = null;
        Band lonBand = null;
        if (product.containsBand(latitude) && product.containsBand(longitude)) {
            latBand = product.getBand(latitude);
            lonBand = product.getBand(longitude);
        } else {
            Variable latVar = ncfile.findVariable(navGroup + "/" + latitude);
            Variable lonVar = ncfile.findVariable(navGroup + "/" + longitude);
            Variable cntlPointVar = ncfile.findVariable(navGroup + "/" + cntlPoints);
            if (latVar != null && lonVar != null && cntlPointVar != null) {
                final ProductData lonRawData = readData(lonVar);
                final ProductData latRawData = readData(latVar);
                latBand = product.addBand(latVar.getShortName(), ProductData.TYPE_FLOAT32);
                lonBand = product.addBand(lonVar.getShortName(), ProductData.TYPE_FLOAT32);

                Array cntArray = cntlPointVar.read();
                int[] colPoints = (int[]) cntArray.getStorage();
                computeLatLonBandData(latBand, lonBand, latRawData, lonRawData, colPoints, mustFlip);
            }
        }
        if (latBand != null && lonBand != null) {
            product.setGeoCoding(new PixelGeoCoding(latBand, lonBand, null, 5, ProgressMonitor.NULL));
        }
    }

    public void addBitmaskDefinitions(Product product, BitmaskDef[] defaultBitmaskDefs) {
        for (BitmaskDef defaultBitmaskDef : defaultBitmaskDefs) {
            product.addBitmaskDef(defaultBitmaskDef);
        }
    }

    private void computeLatLonBandData(final Band latBand, final Band lonBand,
                                       final ProductData latRawData, final ProductData lonRawData,
                                       final int[] colPoints, boolean mustFlip) {
        latBand.ensureRasterData();
        lonBand.ensureRasterData();

        final float[] latRawFloats = (float[]) latRawData.getElems();
        final float[] lonRawFloats = (float[]) lonRawData.getElems();
        final float[] latFloats = (float[]) latBand.getDataElems();
        final float[] lonFloats = (float[]) lonBand.getDataElems();
        final int rawWidth = colPoints.length;
        final int width = latBand.getRasterWidth();
        final int height = latBand.getRasterHeight();

        int colPointIdx = 0;
        int p1 = colPoints[colPointIdx] - 1;
        int p2 = colPoints[++colPointIdx] - 1;
        for (int x = 0; x < width; x++) {
            if (x == p2 && colPointIdx < rawWidth - 1) {
                p1 = p2;
                p2 = colPoints[++colPointIdx] - 1;
            }
            final int steps = p2 - p1;
            final double step = 1.0 / steps;
            final double weight = step * (x - p1);
            for (int y = 0; y < height; y++) {
                final int rawPos2 = y * rawWidth + colPointIdx;
                final int rawPos1 = rawPos2 - 1;
                final int pos = y * width + x;
                latFloats[pos] = computePixel(latRawFloats[rawPos1], latRawFloats[rawPos2], weight);
                lonFloats[pos] = computePixel(lonRawFloats[rawPos1], lonRawFloats[rawPos2], weight);
            }
        }

        if (mustFlip) {
            ObpgProductReader.reverse(latFloats);
            ObpgProductReader.reverse(lonFloats);
        }

        latBand.setSynthetic(true);
        lonBand.setSynthetic(true);
        latBand.getSourceImage();
        lonBand.getSourceImage();
    }

    private float computePixel(final float a, final float b, final double weight) {
        if ((b - a) > 180) {
            final float b2 = b - 360;
            final double v = a + (b2 - a) * weight;
            if (v >= -180) {
                return (float) v;
            } else {
                return (float) (v + 360);
            }
        } else {
            return (float) (a + (b - a) * weight);
        }
    }

    private ProductData readData(Variable variable) throws IOException {
        final int dataType = getProductDataType(variable);
        Array array = variable.read();
        return ProductData.createInstance(dataType, array.getStorage());
    }

    int convertToFlagMask(String name) {
        if (name.matches("f\\d\\d_name")) {
            final String number = name.substring(1, 3);
            final int i = Integer.parseInt(number) - 1;
            if (i >= 0) {
                return 1 << i;
            }
        }
        return 0;
    }

    private void addAttributesToElement(List<Attribute> globalAttributes, final MetadataElement element) {
        for (Attribute attribute : globalAttributes) {
            addAttributeToElement(element, attribute);
        }
    }

    private void addAttributeToElement(final MetadataElement element, final Attribute attribute) {
        final MetadataAttribute metadataAttribute = attributeToMetadata(attribute);
        element.addAttribute(metadataAttribute);
    }
}
