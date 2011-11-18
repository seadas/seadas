package gov.nasa.obpg.seadas.dataio.obpg;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.util.io.CsvReader;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

public abstract class SeadasFileReader {
    protected boolean mustFlipX;
    protected boolean mustFlipY;
    protected List<Attribute> globalAttributes;
    protected Map<Band, Variable> variableMap;
    protected NetcdfFile ncFile;
    protected SeadasProductReader productReader;

    public SeadasFileReader(SeadasProductReader productReader) {
        this.productReader = productReader;
        ncFile = productReader.getNcfile();
        globalAttributes = ncFile.getGlobalAttributes();
    }

    protected synchronized static HashMap<String, String> getL2BandInfoMap() {
        return readTwoColumnTable("l2-band-info.csv");
    }

    protected synchronized static HashMap<String, String> getL2FlagsInfoMap() {
        return readTwoColumnTable("l2-flags-info.csv");
    }

    public abstract Product createProduct() throws ProductIOException;

    public void readBandData(Band destBand, int sourceOffsetX, int sourceOffsetY, int sourceWidth,
                             int sourceHeight, ProductData destBuffer,
                             ProgressMonitor pm) throws IOException, InvalidRangeException {

        if (mustFlipY) {
            sourceOffsetY = destBand.getSceneRasterHeight() - (sourceOffsetY + sourceHeight);
        }
        if (mustFlipX) {
            sourceOffsetX = destBand.getSceneRasterWidth() - (sourceOffsetX + sourceWidth);
        }

        final int[] start = new int[]{sourceOffsetY, sourceOffsetX};
        final int[] stride = new int[]{1, 1};
        final int[] count = new int[]{1, sourceWidth};
        Object buffer = destBuffer.getElems();
        Variable variable = variableMap.get(destBand);

        int targetIndex = 0;
        pm.beginTask("Reading band '" + variable.getShortName() + "'...", sourceHeight);
        // loop over lines

        if (mustFlipY) {
            start[0] += sourceHeight - 1;
            try {
                for (int y = sourceHeight - 1; y >= 0; y--) {
                    if (pm.isCanceled()) {
                        break;
                    }
                    Section section = new Section(start, count, stride);
                    Array array;
                    int [] newshape= {sourceHeight,sourceWidth};
                    synchronized (ncFile) {
                        array = variable.read(section);
                    }
                    if (array.getRank() == 3){
                        array = array.reshapeNoCopy(newshape);
                    }
                    Object storage;

                    if (mustFlipX) {
                        storage = array.flip(1).copyTo1DJavaArray();
                    } else {
                        storage = array.copyTo1DJavaArray();
                    }

                    System.arraycopy(storage, 0, buffer, targetIndex, sourceWidth);
                    start[0]--;
                    targetIndex += sourceWidth;
                    pm.worked(1);
                }
            } finally {
                pm.done();
            }
        } else {
            try {
                for (int y = 0; y < sourceHeight; y++) {
                    if (pm.isCanceled()) {
                        break;
                    }
                    Section section = new Section(start, count, stride);
                    Array array;
                    int [] newshape= {sourceHeight,sourceWidth};

                    synchronized (ncFile) {
                        array = variable.read(section);
                    }
                    if (array.getRank() == 3){
                        array = array.reshapeNoCopy(newshape);
                    }
                    Object storage;

                    if (mustFlipX) {
                        storage = array.flip(1).copyTo1DJavaArray();
                    } else {
                        storage = array.copyTo1DJavaArray();
                    }

                    System.arraycopy(storage, 0, buffer, targetIndex, sourceWidth);
                    start[0]++;
                    targetIndex += sourceWidth;
                    pm.worked(1);
                }
            } finally {
                pm.done();
            }
        }

    }





    protected static BitmaskDef[] getDefaultBitmaskDefs(HashMap<String, String> l2FlagsInfoMap) {
        final InputStream stream = SeadasProductReader.class.getResourceAsStream("l2-bitmask-definitions.xml");
        if (stream != null) {
            try {
                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder builder = factory.newDocumentBuilder();
                final org.w3c.dom.Document w3cDocument = builder.parse(stream);
                final Document document = new DOMBuilder().build(w3cDocument);
                final List<Element> children = document.getRootElement().getChildren("Bitmask_Definition");
                final ArrayList<BitmaskDef> bitmaskDefList = new ArrayList<BitmaskDef>(children.size());
                for (Element element : children) {
                    final BitmaskDef bitmaskDef = BitmaskDef.createBitmaskDef(element);
                    final String description = l2FlagsInfoMap.get(bitmaskDef.getName());
                    bitmaskDef.setDescription(description);
                    bitmaskDefList.add(bitmaskDef);
                }
                return bitmaskDefList.toArray(new BitmaskDef[bitmaskDefList.size()]);
            } catch (Exception e) {
                // ?
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ?
                }
            }
        }
        return new BitmaskDef[0];
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
                    try {
                        band.setNoDataValue((double) variable.findAttribute("bad_value_unscaled").getNumericValue().floatValue());
                    } catch (Exception e) {

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
        }

        return bandToVariableMap;
    }

    public void addGlobalMetadata(Product product) {
        final MetadataElement globalElement = new MetadataElement("Global_Attributes");
        addAttributesToElement(globalAttributes, globalElement);

        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(globalElement);
    }

    public void addScientificMetadata(Product product) throws ProductIOException {

        Group group = ncFile.findGroup("Scan-Line Attributes");
        if (group != null) {
            final MetadataElement scanLineAttrib = getMetadataElementSave(product, "Scan_Line_Attributes");
            handleMetadataGroup(group, scanLineAttrib);
        }

        group = ncFile.findGroup("Sensor Band Parameters");
        if (group != null) {
            final MetadataElement sensorBandParam = getMetadataElementSave(product, "Sensor_Band_Parameters");
            handleMetadataGroup(group, sensorBandParam);
        }
    }

    public void addBitmaskDefinitions(Product product, BitmaskDef[] defaultBitmaskDefs) {
        for (BitmaskDef defaultBitmaskDef : defaultBitmaskDefs) {
            product.addBitmaskDef(defaultBitmaskDef);
        }
    }

    public void computeLatLonBandData(final Band latBand, final Band lonBand,
                                       final ProductData latRawData, final ProductData lonRawData,
                                       final int[] colPoints) {
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

        if (mustFlipY) {
            reverse(latFloats);
        }
        if (mustFlipX) {
            reverse(lonFloats);
        }

        latBand.setSynthetic(true);
        lonBand.setSynthetic(true);
        latBand.getSourceImage();
        lonBand.getSourceImage();
    }

    public float computePixel(final float a, final float b, final double weight) {
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


    public boolean getDefaultFlip() throws ProductIOException {
        String startAttr = getStringAttribute("Start Node");
        boolean startNodeAscending = false;
        if (startAttr != null) {
            startNodeAscending = startAttr.equalsIgnoreCase("Ascending");
        }
        String endAttr = getStringAttribute("End Node");
        boolean endNodeAscending = false;
        if (endAttr != null) {
            endNodeAscending = endAttr.equalsIgnoreCase("Ascending");
        }

        return (startNodeAscending && endNodeAscending);
    }



    protected static HashMap<String, String> readTwoColumnTable(String resourceName) {
        final InputStream stream = SeadasProductReader.class.getResourceAsStream(resourceName);
        if (stream != null) {
            try {
                HashMap<String, String> validExpressionMap = new HashMap<String, String>(32);
                final CsvReader csvReader = new CsvReader(new InputStreamReader(stream), new char[]{';'});
                final List<String[]> table = csvReader.readStringRecords();
                for (String[] strings : table) {
                    if (strings.length == 2) {
                        validExpressionMap.put(strings[0], strings[1]);
                    }
                }
                return validExpressionMap;
            } catch (IOException e) {
                // ?
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ok
                }
            }
        }
        return new HashMap<String, String>(0);
    }

    public static void reverse(float[] data) {
        final int n = data.length;
        final int nc = n / 2;
        for (int i1 = 0; i1 < nc; i1++) {
            int i2 = n - 1 - i1;
            float temp = data[i1];
            data[i1] = data[i2];
            data[i2] = temp;
        }
    }

    public Attribute findAttribute(String name, List<Attribute> attributesList) {
        for (Attribute a : attributesList) {
            if (name.equals(a.getName()))
                return a;
        }
        return null;
    }

    public Attribute findAttribute(String name) {
        return findAttribute(name, globalAttributes);
    }

    public String getStringAttribute(String key, List<Attribute> attributeList) throws ProductIOException {
        Attribute attribute = findAttribute(key, attributeList);
        if (attribute == null || attribute.getLength() != 1) {
            throw new ProductIOException("Global attribute '" + key + "' is missing.");
        } else {
            return attribute.getStringValue().trim();
        }
    }

    public String getStringAttribute(String key) throws ProductIOException {
        return getStringAttribute(key, globalAttributes);
    }

    public int getIntAttribute(String key, List<Attribute> globalAttributes) throws ProductIOException {
        Attribute attribute = findAttribute(key, globalAttributes);
        if (attribute == null) {
            throw new ProductIOException("Global attribute '" + key + "' is missing.");
        } else {
            return attribute.getNumericValue(0).intValue();
        }
    }

    public int getIntAttribute(String key) throws ProductIOException {
        return getIntAttribute(key, globalAttributes);
    }

    public float getFloatAttribute(String key, List<Attribute> globalAttributes) throws ProductIOException {
        Attribute attribute = findAttribute(key, globalAttributes);
        if (attribute == null) {
            throw new ProductIOException("Global attribute '" + key + "' is missing.");
        } else {
            return attribute.getNumericValue(0).floatValue();
        }
    }

    public float getFloatAttribute(String key) throws ProductIOException {
        return getIntAttribute(key, globalAttributes);
    }

    private ProductData.UTC getUTCAttribute(String key, List<Attribute> globalAttributes) {
        Attribute attribute = findAttribute(key, globalAttributes);
        Boolean isModis = false;
        try {
            isModis = findAttribute("MODIS Resolution", globalAttributes).isString();
        } catch (Exception e) {
        }
        if (attribute != null) {
            String timeString = attribute.getStringValue().trim();
            final DateFormat dateFormat = ProductData.UTC.createDateFormat("yyyyDDDHHmmssSSS");
            final DateFormat dateFormatModis = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            try {
                if (isModis) {
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

    public ProductData.UTC getUTCAttribute(String key) {
        return getUTCAttribute(key, globalAttributes);
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

    private void handleMetadataGroup(Group group, MetadataElement metadataElement) throws ProductIOException {
        List<Variable> variables = group.getVariables();
        for (Variable variable : variables) {
            final String name = variable.getShortName();
            final int dataType = getProductDataType(variable);
            Array array;
            try {
                array = variable.read();
            } catch (IOException e) {
                throw new ProductIOException(e.getMessage());
            }
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

    protected void addAttributesToElement(List<Attribute> globalAttributes, final MetadataElement element) {
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

    public int convertToFlagMask(String name) {
        if (name.matches("f\\d\\d_name")) {
            final String number = name.substring(1, 3);
            final int i = Integer.parseInt(number) - 1;
            if (i >= 0) {
                return 1 << i;
            }
        }
        return 0;
    }

    public ProductData readData(Variable variable) throws ProductIOException {
        final int dataType = getProductDataType(variable);
        Array array;
        try {
            array = variable.read();
        } catch (IOException e) {
            throw new ProductIOException(e.getMessage());
        }
        return ProductData.createInstance(dataType, array.getStorage());
    }


    float[] flatten2DimArray(float[][] twoDimArray) {
        // Converts an array of two dimensions into a single dimension array row by row.
        float[] flatArray = new float[twoDimArray.length * twoDimArray[0].length];
        for (int row = 0; row < twoDimArray.length; row++) {
            int offset = row * twoDimArray[row].length;
            System.arraycopy(twoDimArray[row], 0, flatArray, offset, twoDimArray[row].length);
        }
        return flatArray;
    }

}
