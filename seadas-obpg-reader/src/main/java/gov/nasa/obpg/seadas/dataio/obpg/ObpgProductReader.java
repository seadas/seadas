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
import org.esa.beam.framework.dataio.AbstractProductReader;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.util.io.CsvReader;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
// import org.opengis.filter.spatial.Equals;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObpgProductReader extends AbstractProductReader {

    private ObpgUtils obpgUtils = new ObpgUtils();
    private Map<Band, Variable> variableMap;
    private boolean mustFlip; //TODO separate into horizontal and vertical flip - CZCS...
    private NetcdfFile ncfile;

    /**
     * Constructs a new abstract product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be <code>null</code> for internal reader
     *                     implementations
     */
    protected ObpgProductReader(ObpgProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        try {
            final HashMap<String, String> l2BandInfoMap = getL2BandInfoMap();
            final HashMap<String, String> l2FlagsInfoMap = getL2FlagsInfoMap();
            final BitmaskDef[] defs = getDefaultBitmaskDefs(l2FlagsInfoMap);

            final File inFile = ObpgUtils.getInputFile(getInput());
            final String path = inFile.getPath();
            final Product product;

            ncfile = NetcdfFile.open(path);

            String productType = obpgUtils.getProductType(ncfile);
            mustFlip = obpgUtils.mustFlip(ncfile);

            List<Attribute> globalAttributes = ncfile.getGlobalAttributes();
            if (productType.contains("MODIS_SWATH_Type_L1B")){
                obpgUtils.addGlobalAttributeModisL1B(ncfile, globalAttributes);
                mustFlip = obpgUtils.mustFlipMODIS(obpgUtils.getStringAttribute("MODIS Platform",globalAttributes),
                        obpgUtils.getStringAttribute("DayNightFlag",globalAttributes));
            }
            if (productType.contains("SeaDAS Mapped")){
                 obpgUtils.addGlobalAttributeSeadasMapped(ncfile, globalAttributes);
            }
            product = obpgUtils.createProductBody(globalAttributes,productType);

            product.setFileLocation(inFile);
            product.setProductReader(this);

            obpgUtils.addGlobalMetadata(product, globalAttributes);
            obpgUtils.addScientificMetadata(product, ncfile);

            variableMap = obpgUtils.addBands(product, ncfile.getVariables(), l2BandInfoMap, l2FlagsInfoMap);
            if (productType.contains("Level-3")) {
                GeoCoding geoCoding = createGeoCoding(product, productType);
                product.setGeoCoding(geoCoding);
            } else if (productType.contains("SeaDAS Mapped")){
                GeoCoding geoCoding = createGeoCoding(product, productType);  //TODO Check on various IDL projections
                product.setGeoCoding(geoCoding);
            } else if (productType.contains("SeaWiFS  Level-1A Data")){
                // ToDo add SeaWiFS geocoding stuff, using Geonav results here
                ObpgGeonav geonavCalculator = readSeawifsGeonavData(path);
                float[] latitude = geonavCalculator.getLatitude();
       	        float[] longitude = geonavCalculator.getLongitude();
            } else {
                obpgUtils.addGeocoding(product, ncfile, mustFlip);
            }
            obpgUtils.addBitmaskDefinitions(product, defs);
            return product;
        } catch (IOException e) {
            throw new ProductIOException(e.getMessage());
        }
    }


    @Override
    public void close() throws IOException {
        if (ncfile != null) {
            ncfile.close();
        }
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth,
                                          int sourceHeight,
                                          int sourceStepX, int sourceStepY, Band destBand, int destOffsetX,
                                          int destOffsetY, int destWidth, int destHeight,
                                          ProductData destBuffer,
                                          ProgressMonitor pm) throws IOException {

        if (mustFlip) {
             sourceOffsetY = destBand.getSceneRasterHeight() - (sourceOffsetY + sourceHeight);
             sourceOffsetX = destBand.getSceneRasterWidth() - (sourceOffsetX + sourceWidth);
        }
        Variable variable = variableMap.get(destBand);
        try {
            readBandData(variable, sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight, destBuffer, pm);
            if (mustFlip) {
                reverse(destBuffer);
            }
        } catch (Exception e) {
            final ProductIOException exception = new ProductIOException(e.getMessage());
            exception.setStackTrace(e.getStackTrace());
            throw exception;
        }
    }

    private void readBandData(Variable variable, int sourceOffsetX, int sourceOffsetY, int sourceWidth,
                              int sourceHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException,
            InvalidRangeException {

        final int[] start = new int[]{sourceOffsetY, sourceOffsetX};
        final int[] stride = new int[]{1, 1};
        final int[] count = new int[]{1, sourceWidth};
        Object buffer = destBuffer.getElems();

        int targetIndex = 0;
        pm.beginTask("Reading band '" + variable.getShortName() + "'...", sourceHeight);
        // loop over lines
        try {
            for (int y = 0; y < sourceHeight; y++) {
                if (pm.isCanceled()) {
                    break;
                }
                Section section = new Section(start, count, stride);
                Array array;
                synchronized (ncfile) {
                    array = variable.read(section);
                }
                final Object storage = array.getStorage();
                System.arraycopy(storage, 0, buffer, targetIndex, sourceWidth);
                start[0]++;
                targetIndex += sourceWidth;
                pm.worked(1);
            }
        } finally {
            pm.done();
        }
    }

    public static void reverse(ProductData data) {
        final int n = data.getNumElems();
        final int nc = n / 2;
        for (int i1 = 0; i1 < nc; i1++) {
            int i2 = n - 1 - i1;
            double temp = data.getElemDoubleAt(i1);
            data.setElemDoubleAt(i1, data.getElemDoubleAt(i2));
            data.setElemDoubleAt(i2, temp);
        }
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

    private static BitmaskDef[] getDefaultBitmaskDefs(HashMap<String, String> l2FlagsInfoMap) {
        final InputStream stream = ObpgProductReader.class.getResourceAsStream("l2-bitmask-definitions.xml");
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

    private synchronized static HashMap<String, String> getL2BandInfoMap() {
        return readTwoColumnTable("l2-band-info.csv");
    }

    private synchronized static HashMap<String, String> getL2FlagsInfoMap() {
        return readTwoColumnTable("l2-flags-info.csv");
    }

    private static HashMap<String, String> readTwoColumnTable(String resourceName) {
        final InputStream stream = ObpgProductReader.class.getResourceAsStream(resourceName);
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

    private GeoCoding createGeoCoding(Product product, String productType) {
        //float pixelX = 0.0f;
        //float pixelY = 0.0f;
        // Changed after conversation w/ Sean, Norman F., et al.
        float pixelX = 0.5f;
        float pixelY = 0.5f;

        float easting = (float) product.getMetadataRoot().getElement("Global_Attributes").getAttribute("Easternmost Longitude").getData().getElemDouble();
        float westing = (float) product.getMetadataRoot().getElement("Global_Attributes").getAttribute("Westernmost Longitude").getData().getElemDouble();
        float pixelSizeX = (easting - westing) / product.getSceneRasterWidth();
        float northing = (float) product.getMetadataRoot().getElement("Global_Attributes").getAttribute("Northernmost Latitude").getData().getElemDouble();
        float southing = (float) product.getMetadataRoot().getElement("Global_Attributes").getAttribute("Southernmost Latitude").getData().getElemDouble();
        float pixelSizeY = (northing - southing) / product.getSceneRasterHeight();

        try {
            return new CrsGeoCoding(DefaultGeographicCRS.WGS84,
                                    product.getSceneRasterWidth(),
                                    product.getSceneRasterHeight(),
                                    westing, northing,
                                    pixelSizeX, pixelSizeY,
                                    pixelX, pixelY);
        } catch (FactoryException e) {
            throw new IllegalStateException(e);
        } catch (TransformException e) {
            throw new IllegalStateException(e);
        }
    }

    private float[] populateVector(ArrayFloat sourceData, int size, int lineNum) {
        float[] newVect = new float[size];
        for(int i = 0; i < size; i ++) {
            newVect[i] = sourceData.getFloat(size * lineNum + i);
        }
        return newVect;
    }

    private ArrayFloat readNetcdfDataArray(String varName, Group group) {
        ArrayFloat dataArray = null;
        int[] startPts;
        Variable varToRead = group.findVariable(varName);
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

    private ObpgGeonav readSeawifsGeonavData(String path) throws IOException {
        NetcdfFile ncFile = null;
        ObpgGeonav geonavCalculator = null;
        try {
            String prefix = "  ";
            ncFile = NetcdfFile.open(path);
            ObpgGeonav.DataType dataType = ObpgGeonav.getSeawifsDataType(ncFile);
            int numScanLines = ObpgGeonav.getNumberScanLines(ncFile);

            //Group rootGroup = ncFile.getRootGroup();
            Group navGroup = ncFile.findGroup("Navigation");
            Group scanLineAttrGroup = ncFile.findGroup("Scan-Line Attributes");

            ArrayFloat orbitData = readNetcdfDataArray("orb_vec", navGroup);
            ArrayFloat sensorData = readNetcdfDataArray("sen_mat", navGroup);
            ArrayFloat sunData = readNetcdfDataArray("sun_ref", navGroup);
            ArrayFloat attAngleData = readNetcdfDataArray("att_ang", navGroup);
            ArrayFloat scanTrackEllipseCoefData = readNetcdfDataArray("scan_ell", navGroup);
            ArrayFloat tiltData = readNetcdfDataArray("tilt", scanLineAttrGroup);

            for (int line = 0; line < numScanLines; line ++) {
                float[] orbVect = populateVector(orbitData, 3, line);
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
                float[] sunUnitVect = populateVector(sunData, 3, line);
                float[] attAngleVect = populateVector(attAngleData, 3, line);
                float[] scanTrackEllipseCoef = populateVector(scanTrackEllipseCoefData, 6, line);
                float tilt = tiltData.getFloat(line);

                geonavCalculator = new ObpgGeonav(orbVect, sensorMat, scanTrackEllipseCoef, sunUnitVect,
                                                     attAngleVect, tilt, ncFile);
                geonavCalculator.doComputations();

       	        //float[] sensorAzimuth = geonavCalculator.getSensorAzimuth();
       	        //float[] sensorZenith = geonavCalculator.getSensorZenith();
       	        //float[] solarAzimuth = geonavCalculator.getSolarAzimuth();
       	        //float[] solarZenith = geonavCalculator.getSolarZenith();
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
        } finally {
            ncFile.close();
        }
        return geonavCalculator;
    }
}
