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
import org.esa.beam.framework.dataop.maptransf.Datum;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.hdf4.ODLparser;

import javax.swing.tree.FixedHeightLayoutCache;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

// import org.esa.beam.dataio.netcdf.metadata.profiles.hdfeos.HdfEosUtils;

// import java.security.PrivateKey;

public class ObpgUtils {

    static final String MODIS_L1B_TYPE = "MODIS_SWATH_Type_L1B";
    static final String MODIS_PLATFORM = "MODIS Platform";
    static final String MODIS_L1B_PARAM = "MODIS Resolution";

    static final String SEAWIFS_L1A_TYPE = "SeaWiFS Level-1A Data";

    static final String KEY_NAME = "Product Name";
    static final String KEY_TYPE = "Title";

    static final String KEY_WIDTH = "Pixels per Scan Line";
    static final String KEY_HEIGHT = "Number of Scan Lines";
    static final String KEY_WIDTH_MODISL1 = "Max Earth View Frames";
    static final String KEY_HEIGHT_MODISL1 = "Number of Scans";
    static final String KEY_WIDTH_AQUARIUS ="Number of Beams";
    static final String KEY_HEIGHT_AQUARIUS = "Number of Blocks";
    static final String KEY_SEADAS_MAPPED_WIDTH =  "Scene Pixels";
    static final String KEY_SEADAS_MAPPED_HEIGHT =  "Scene Lines";
    //static final String KEY_SEAWIFS_L1A_WIDTH = "Pixels per Scan Line";

    static final String KEY_START_NODE = "Start Node";
    static final String KEY_END_NODE = "End Node";
    static final String KEY_START_TIME = "Start Time";
    static final String KEY_END_TIME = "End Time";
    static final String KEY_DNF = "DayNightFlag";
    static final String SENSOR_BAND_PARAMETERS = "Sensor_Band_Parameters";
    static final String SENSOR_BAND_PARAMETERS_GROUP = "Sensor Band Parameters";
    static final String SCAN_LINE_ATTRIBUTES = "Scan_Line_Attributes";
    static final String SCAN_LINE_ATTRIBUTES_GROUP = "Scan-Line Attributes";

    static final String KEY_L3SMI_HEIGHT = "Number of Lines";
    static final String KEY_L3SMI_WIDTH = "Number of Columns";
    static final String SMI_PRODUCT_PARAMETERS = "SMI Product Parameters";

    static final String STRUCT_METADATA = "StructMetadata";
    static final String CORE_METADATA = "CoreMetadata";
    static final String ARCHIVE_METADATA = "ArchiveMetadata";

    static final int[] MODIS_WVL = new int[]{645, 859, 469, 555, 1240, 1640, 2130, 412, 443, 488, 531, 547, 667, 678,
            748, 869, 905, 936, 940, 3750, 3959, 3959, 4050, 4465, 4515, 1375, 6715, 7325, 8550, 9730, 11030, 12020,
            13335, 13635, 13935, 14235};

    static final int[] SEAWIFS_WVL = new int[]{412, 443, 490, 510, 555, 670, 765, 865};

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
        int pixelmultiplier = 1;
        int scanmultiplier = 10;
        if (productType.contains(MODIS_L1B_TYPE)){
            keyWidth = KEY_WIDTH_MODISL1;
            keyHeight = KEY_HEIGHT_MODISL1;
            for (Attribute attribute : globalAttributes) {
                if (attribute.getName().equals(MODIS_L1B_PARAM)){
                    String resolution = attribute.getStringValue();
                    if (resolution.equals("500m")){
                        pixelmultiplier = 2;
                        scanmultiplier = 20;
                    } else if (resolution.equals("250m")){
                        pixelmultiplier = 4;
                        scanmultiplier = 40;
                    }
                }
            }
            sceneWidth = getIntAttribute(keyWidth, globalAttributes) * pixelmultiplier;
            sceneHeight = getIntAttribute(keyHeight, globalAttributes) * scanmultiplier;
        } else if (productType.equalsIgnoreCase("SeaDAS Mapped")){
            sceneWidth = getIntAttribute(KEY_SEADAS_MAPPED_WIDTH, globalAttributes);
            sceneHeight = getIntAttribute(KEY_SEADAS_MAPPED_HEIGHT, globalAttributes);
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

        productName = getStringAttribute(KEY_NAME, globalAttributes);

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
        if (title.contains("Aquarius")){
            return KEY_HEIGHT_AQUARIUS;
        } else if (title.contains("Level-2") || title.contains("Level-1B") || title.contains("Browse")
                   || title.contains(SEAWIFS_L1A_TYPE)) {
            return KEY_HEIGHT;
        } else if (title.contains("Level-3 Mapped")) {
            return KEY_L3SMI_HEIGHT;
        } else {
            return KEY_L3SMI_HEIGHT;
            // TODO: Throw exception or default to KEY_HEIGHT (or a different) return value.
        }
    }

    private String getWidthKey(String title) {
        if (title.contains("Aquarius")){
            return KEY_WIDTH_AQUARIUS;
        } else if (title.contains("Level-2") || title.contains("Level-1B") || title.contains("Browse")
                   || title.contains("SeaWiFS Level-1A Data")) {
            return KEY_WIDTH;
        } else if (title.contains("Level-3 Mapped")){
            return KEY_L3SMI_WIDTH;
        } else {
            return KEY_L3SMI_WIDTH;
            // TODO: Throw exception or default to KEY_WIDTH (or a different) return value.
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
            try {
                List<Variable> seadasMappedVariables = ncfile.getVariables();
                Boolean isSeadasMapped = false;

                isSeadasMapped = seadasMappedVariables.get(0).findAttribute("Projection Category").isString();
            } catch (Exception e) {
                throw new ProductIOException("Unrecognized file!");
            }
            return "SeaDAS Mapped";
        }
    }
    
    public void addGlobalAttributeSeadasMapped(final NetcdfFile ncfile, List<Attribute> globalAttributes){
        int [] dims = ncfile.getVariables().get(0).getShape();
        String [] prodname = ncfile.getLocation().split("/");
        String projname = ncfile.getVariables().get(0).findAttribute("Projection Name").getStringValue();
        Array projlimits = ncfile.getVariables().get(0).findAttribute("Limit").getValues();
        double north = projlimits.getDouble(2);
        double south = projlimits.getDouble(0);
        double east = projlimits.getDouble(3);
        double west = projlimits.getDouble(1);

        Attribute rasterWidth = new Attribute(KEY_SEADAS_MAPPED_WIDTH,dims[1]);
        Attribute rasterHeight = new Attribute(KEY_SEADAS_MAPPED_HEIGHT,dims[0]);
        Attribute productName = new Attribute(KEY_NAME,prodname[prodname.length-1]);
        Attribute projection = new Attribute("Projection Name",projname);
        Attribute northing = new Attribute("Northernmost Latitude",north);
        Attribute southing = new Attribute("Southernmost Latitude",south);
        Attribute easting = new Attribute("Easternmost Longitude",east);
        Attribute westing = new Attribute("Westernmost Longitude",west);
        globalAttributes.add(rasterHeight);
        globalAttributes.add(rasterWidth);
        globalAttributes.add(productName);
        globalAttributes.add(projection);
        globalAttributes.add(northing);
        globalAttributes.add(southing);
        globalAttributes.add(easting);
        globalAttributes.add(westing);
    }

    public void addGlobalAttributeModisL1B(final NetcdfFile ncfile, List<Attribute> globalAttributes) {
        Element eosElement = null;
        try {
          eosElement = getEosElement(CORE_METADATA, ncfile.getRootGroup());
          //  eosElement = HdfEosUtils.getEosElement(CORE_METADATA, ncfile.getRootGroup());
        } catch (IOException e) {
          e.printStackTrace();  //Todo add a valid exception
          System.out.print("Whoops...");
        }

        //grab granuleID
        Element inventoryMetadata = eosElement.getChild("INVENTORYMETADATA");
        Element inventoryElem = (Element) inventoryMetadata.getChildren().get(0);
        Element ecsdataElem = inventoryElem.getChild("ECSDATAGRANULE");
        Element granIdElem = ecsdataElem.getChild("LOCALGRANULEID");
        String granId = granIdElem.getValue().substring(1);
        Attribute granIdAttribute = new Attribute(KEY_NAME,granId);
        globalAttributes.add(granIdAttribute);
        Element dnfElem = ecsdataElem.getChild("DAYNIGHTFLAG");
        String daynightflag = dnfElem.getValue().substring(1);
        Attribute dnfAttribute = new Attribute(KEY_DNF,daynightflag);
        globalAttributes.add(dnfAttribute);

        //grab granule date-time
        Element timeElem = inventoryElem.getChild("RANGEDATETIME");
        Element startTimeElem = timeElem.getChild("RANGEBEGINNINGTIME");
        String startTime = startTimeElem.getValue().substring(1);
        Element startDateElem = timeElem.getChild("RANGEBEGINNINGDATE");
        String startDate = startDateElem.getValue().substring(1);
        Attribute startTimeAttribute = new Attribute(KEY_START_TIME,startDate+' '+startTime);
        globalAttributes.add(startTimeAttribute);

        Element endTimeElem = timeElem.getChild("RANGEENDINGTIME");
        String endTime = endTimeElem.getValue().substring(1);
        Element endDateElem = timeElem.getChild("RANGEENDINGDATE");
        String endDate = endDateElem.getValue().substring(1);
        Attribute endTimeAttribute = new Attribute(KEY_END_TIME,endDate+' '+endTime);
        globalAttributes.add(endTimeAttribute);

        Element measuredParamElem = inventoryElem.getChild("MEASUREDPARAMETER");
        Element measuredContainerElem = measuredParamElem.getChild("MEASUREDPARAMETERCONTAINER");
        Element paramElem = measuredContainerElem.getChild("PARAMETERNAME");
        String param = paramElem.getValue().substring(1);
        String resolution = "1km";
        if (param.contains("EV_500")){
            resolution = "500m";
        } else if (param.contains("EV_250")){
            resolution = "250m";
        }
        Attribute paramAttribute = new Attribute(MODIS_L1B_PARAM,resolution);
        globalAttributes.add(paramAttribute);

        //grab Mission Name
        Element platformElem = inventoryElem.getChild("ASSOCIATEDPLATFORMINSTRUMENTSENSOR");
        Element containerElem = platformElem.getChild("ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER");
        Element shortNameElem = containerElem.getChild("ASSOCIATEDPLATFORMSHORTNAME");
        String shortName = shortNameElem.getValue().substring(2);
        Attribute shortNameAttribute = new Attribute(MODIS_PLATFORM,shortName);
        globalAttributes.add(shortNameAttribute);



    }

    private ProductData.UTC getUTCAttribute(String key, List<Attribute> globalAttributes) {
        Attribute attribute = findAttribute(key, globalAttributes);
        Boolean isModis = false;
        try {
            isModis = findAttribute("MODIS Resolution",globalAttributes).isString();
        } catch (Exception e) {
        }
        if (attribute != null) {
            String timeString = attribute.getStringValue().trim();
            final DateFormat dateFormat = ProductData.UTC.createDateFormat("yyyyDDDHHmmssSSS");
            final DateFormat dateFormatModis = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            try {
                if (isModis){
                    final Date date = dateFormatModis.parse(timeString);
                    String milliSeconds = timeString.substring(timeString.length() - 3);
                    return ProductData.UTC.create(date, Long.parseLong(milliSeconds) * 1000);
                } else {
                    final Date date = dateFormat.parse(timeString);
                    String milliSeconds = timeString.substring(timeString.length() - 3);

                    return ProductData.UTC.create(date, Long.parseLong(milliSeconds) * 1000);
                }
            } catch (ParseException e) {
            }
        }
        return null;
    }

    public String getStringAttribute(String key, List<Attribute> globalAttributes) throws ProductIOException {
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

    public boolean mustFlipMODIS(String platform, String dnflag) {

        boolean startNodeAscending = false;
        boolean endNodeAscending = false;
        if (platform.contains("Aqua")) {
            if (dnflag.contains("Day")){
                startNodeAscending = true;
                endNodeAscending = true;
            }
        } else if (platform.contains("Terra")){
             if (dnflag.contains("Night")){
                startNodeAscending = true;
                endNodeAscending = true;
            }
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
            int variableRank = variable.getRank();
            if (variableRank == 2) {
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
            if (variableRank == 3) {
                if (product.getProductType().contains(MODIS_L1B_TYPE)) {
                    spectralBandIndex = addModisBands(product, bandInfoMap, bandToVariableMap, sceneRasterWidth, sceneRasterHeight, spectralBandIndex, variable);
                } else if (product.getProductType().contains(SEAWIFS_L1A_TYPE)) {
                    spectralBandIndex = addSeawifsBands(product, bandInfoMap, bandToVariableMap, sceneRasterWidth, sceneRasterHeight, spectralBandIndex, variable);
                }
            }
        }

        return bandToVariableMap;
    }

    private int addSeawifsBands(Product product, Map<String, String> bandInfoMap, Map<Band, Variable> bandToVariableMap, int sceneRasterWidth, int sceneRasterHeight, int spectralBandIndex, Variable variable) {
        final int[] dimensions = variable.getShape();
        final int bands = dimensions[2];
        final int height = dimensions[0];
        final int width = dimensions[1];

        if (height == sceneRasterHeight && width == sceneRasterWidth) {
            final List<Attribute> list = variable.getAttributes();

            String units = "radiance counts";
            String description = "Level-1A data";

            FlagCoding flagCoding = null;

            //int i;
            //for (String bandname: bname_array){
            for (int i=0;i<bands;i++){
                final String shortname = "L1A";
                StringBuilder longname = new StringBuilder(shortname);
                longname.append("_");
                longname.append(SEAWIFS_WVL[i]);
                String name = longname.toString();
                final int dataType = getProductDataType(variable);
                final Band band = new Band(name, dataType, width, height);
                final String validExpression = bandInfoMap.get(name);
                if (validExpression != null && !validExpression.equals("")) {
                    band.setValidPixelExpression(validExpression);
                }
                product.addBand(band);

                final float wavelength = Float.valueOf(SEAWIFS_WVL[i]);
                band.setSpectralWavelength(wavelength);
                band.setSpectralBandIndex(spectralBandIndex++);

                Variable sliced = null;
                try {
                    sliced = variable.slice(2, i);
                } catch (InvalidRangeException e) {
                    e.printStackTrace();  //Todo change body of catch statement.
                }
                bandToVariableMap.put(band, sliced);
                band.setUnit(units);
                band.setDescription(description);

                if (flagCoding != null) {
                    band.setSampleCoding(flagCoding);
                    product.getFlagCodingGroup().add(flagCoding);
                }
                addSeawifsGeonav(product);
            }
        }
        return spectralBandIndex;
    }

    void addSeawifsGeonav(Product product) {
        ObpgGeonav geonavData;
        //geonavData = new ObpgGeonav(product.get);
    }

    private int addModisBands(Product product, Map<String, String> bandInfoMap, Map<Band, Variable> bandToVariableMap, int sceneRasterWidth, int sceneRasterHeight, int spectralBandIndex, Variable variable) {
        final int[] dimensions = variable.getShape();
        final int bands = dimensions[0];
        final int height = dimensions[1];
        final int width = dimensions[2];

        if (height == sceneRasterHeight && width == sceneRasterWidth) {
            final List<Attribute> list = variable.getAttributes();
            Attribute band_names = findAttribute("band_names", list);
            if (band_names != null){ // Then treat as a MODIS L1B...
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
                //int i;
                //for (String bandname: bname_array){
                for (int i=0;i<bands;i++){
                    final String shortname = variable.getShortName();
                    StringBuilder longname = new StringBuilder(shortname);
                    longname.append("_");
                    longname.append(bname_array[i]);
                    String name = longname.toString();
                    final int dataType = getProductDataType(variable);
                    final Band band = new Band(name, dataType, width, height);
                    final String validExpression = bandInfoMap.get(name);
                    if (validExpression != null && !validExpression.equals("")) {
                        band.setValidPixelExpression(validExpression);
                    }
                    product.addBand(band);

                    int wvlidx;

                    if (bname_array[i].contains("lo") || bname_array[i].contains("hi")){
                        wvlidx = Integer.parseInt(bname_array[i].substring(0,1)) - 1;
                    } else {
                        wvlidx = Integer.parseInt(bname_array[i]) -1;
                    }

                    final float wavelength = Float.valueOf(MODIS_WVL[wvlidx]);
                    band.setSpectralWavelength(wavelength);
                    band.setSpectralBandIndex(spectralBandIndex++);

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
                }
            }
        }
        return spectralBandIndex;
    }

    public void addGeocoding(final Product product, NetcdfFile ncfile, boolean mustFlip) throws IOException {
        final String navGroup = "Navigation Data";
        final String navGroupMODIS = "MODIS_SWATH_Type_L1B/Geolocation Fields";
        final String longitude = "longitude";
        final String latitude = "latitude";
        final String beam_longitude = "beam_clon";
        final String beam_latitude = "beam_clat";
        String cntlPoints = "cntl_pt_cols";

        Band latBand = null;
        Band lonBand = null;
        if (product.getProductType().contains(MODIS_L1B_TYPE)){

            // read latitudes and longitudes
            int cntl_lat_ix = 5;
            int cntl_lon_ix = 5;
           String resolution = product.getMetadataRoot().getElement("Global_Attributes").getAttribute(MODIS_L1B_PARAM).getData().getElemString();
            if (resolution.equals("500m")){
                cntl_lat_ix = 2;
                cntl_lon_ix = 2;
            } else if (resolution.equals("250m")){
                cntl_lat_ix = 4;
                cntl_lon_ix = 4;
            } else {
                cntl_lat_ix = 5;
                cntl_lon_ix = 5;
            }

            Variable lats = ncfile.findVariable(navGroupMODIS + "/" + "Latitude");
            Variable lons = ncfile.findVariable(navGroupMODIS + "/" + "Longitude");
            int[] dims = lats.getShape();

            float[] latTiePoints;
            latTiePoints = new float[dims[0]*dims[1]];
            float[] lonTiePoints;
            lonTiePoints = new float[dims[0]*dims[1]];

            Array latarr = lats.read();
            Array lonarr = lons.read();
            if (mustFlip){
                latTiePoints = (float[]) latarr.flip(0).flip(1).copyTo1DJavaArray();
                lonTiePoints = (float[]) lonarr.flip(0).flip(1).copyTo1DJavaArray();
            } else {
                latTiePoints = (float[]) latarr.getStorage();
                lonTiePoints = (float[]) lonarr.getStorage();
            }

            final TiePointGrid latGrid = new TiePointGrid("latitude", dims[1],dims[0], 0, 0,
                    cntl_lat_ix, cntl_lon_ix, latTiePoints);

            product.addTiePointGrid(latGrid);

            final TiePointGrid lonGrid = new TiePointGrid("longitude", dims[1],dims[0], 0, 0,
                    cntl_lat_ix, cntl_lon_ix, lonTiePoints);

            product.addTiePointGrid(lonGrid);

            product.setGeoCoding(new TiePointGeoCoding(latGrid, lonGrid, Datum.WGS_84));
        } else if (product.containsBand(latitude) && product.containsBand(longitude)) {
            latBand = product.getBand(latitude);
            lonBand = product.getBand(longitude);
        } else if (product.containsBand(beam_latitude) && product.containsBand(beam_longitude)) {
            latBand = product.getBand(beam_latitude);
            lonBand = product.getBand(beam_longitude);
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
            //if (attribute.getName().contains("EV")) {
            if (attribute.getName().matches(".*(EV|Value|Bad|Nois|Electronics|Dead|Detector).*")) {
                continue;
            } else {
                addAttributeToElement(element, attribute);
            }
        }
    }

    private void addAttributeToElement(final MetadataElement element, final Attribute attribute) {
        final MetadataAttribute metadataAttribute = attributeToMetadata(attribute);
        element.addAttribute(metadataAttribute);
    }

    /* COPIED FROM org.esa.beam.dataio.netcdf.metadata.profiles.hdfeos:HdfEosUtils */

    static Element getEosElement(String name, Group eosGroup) throws IOException {
        String smeta = getEosMetadata(name, eosGroup);
        if (smeta == null) {
            return null;
        }
        smeta = smeta.replaceAll("\\s+=\\s+", "=");
        smeta = smeta.replaceAll("\\?", "_"); // XML names cannot contain the character "?".

        StringBuilder sb = new StringBuilder(smeta.length());
        StringTokenizer lineFinder = new StringTokenizer(smeta, "\t\n\r\f");
        while (lineFinder.hasMoreTokens()) {
            String line = lineFinder.nextToken().trim();
            sb.append(line);
            sb.append("\n");
        }

        ODLparser parser = new ODLparser();
        return parser.parseFromString(sb.toString());// now we have the ODL in JDOM elements


    }

   public static String getEosMetadata(String name, Group eosGroup) throws IOException {
      StringBuilder sbuff = null;
      String structMetadata = null;

      int n = 0;
      while (true) {
        Variable structMetadataVar = eosGroup.findVariable(name + "." + n);
        if (structMetadataVar == null) {
            break;
        }
        if ((structMetadata != null) && (sbuff == null)) { // more than 1 StructMetadata
          sbuff = new StringBuilder(64000);
          sbuff.append(structMetadata);
        }

        Array metadataArray = structMetadataVar.read();
        structMetadata = ((ArrayChar) metadataArray).getString(); // common case only StructMetadata.0, avoid extra copy

        if (sbuff != null) {
          sbuff.append(structMetadata);
        }
        n++;
      }
      return (sbuff != null) ? sbuff.toString() : structMetadata;
    }

    // look for a group with the given name. recurse into subgroups if needed. breadth first
    static Group findGroupNested(Group parent, String name) {

      for (Group g : parent.getGroups()) {
        if (g.getShortName().equals(name))
          return g;
      }
      for (Group g : parent.getGroups()) {
        Group result = findGroupNested(g, name);
        if (result != null)
          return result;
      }
      return null;
    }

    static String getValue(Element root, String... childs) {
        Element element = root;
        int index = 0;
        while (element != null && index < childs.length) {
            String childName = childs[index++];
            element = element.getChild(childName);
        }
        return element != null ? element.getValue() : null;

    }

}
