package gov.nasa.obpg.seadas.dataio.obpg;

import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.dataop.maptransf.Datum;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.iosp.hdf4.ODLparser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

//import org.esa.beam.dataio.netcdf.metadata.profiles.hdfeos.H;

/**
 * Created by IntelliJ IDEA.
 * User: seadas
 * Date: 11/14/11
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class L1BModisFileReader extends SeadasFileReader {

    L1BModisFileReader(SeadasProductReader productReader) {
        super(productReader);
    }


    @Override
    public Product createProduct() throws ProductIOException {

        addGlobalAttributeModisL1B();
        String productName = getStringAttribute("Product Name");
        int pixelmultiplier = 1;
        int scanmultiplier = 10;

        for (Attribute attribute : globalAttributes) {
            if (attribute.getName().equals("MODIS Resolution")) {
                String resolution = attribute.getStringValue();
                if (resolution.equals("500m")) {
                    pixelmultiplier = 2;
                    scanmultiplier = 20;
                } else if (resolution.equals("250m")) {
                    pixelmultiplier = 4;
                    scanmultiplier = 40;
                }
            }
        }
        int sceneWidth = getIntAttribute("Max Earth View Frames", globalAttributes) * pixelmultiplier;
        int sceneHeight = getIntAttribute("Number of Scans", globalAttributes) * scanmultiplier;

        mustFlipX = mustFlipY = mustFlipMODIS();
        SeadasProductReader.ProductType productType = productReader.getProductType();

        Product product = new Product(productName, productType.toString(), sceneWidth, sceneHeight);
        product.setDescription(productName);

        ProductData.UTC utcStart = getUTCAttribute("Start Time");
        if (utcStart != null) {
            if (mustFlipY){
                product.setEndTime(utcStart);
            } else {
                product.setStartTime(utcStart);
            }
        }
        ProductData.UTC utcEnd = getUTCAttribute("End Time");
        if (utcEnd != null) {
            if (mustFlipY) {
                product.setStartTime(utcEnd);
            } else {
                product.setEndTime(utcStart);
            }
        }

        product.setFileLocation(productReader.getInputFile());
        product.setProductReader(productReader);

        addGlobalMetadata(product);
        addScientificMetadata(product);

        variableMap = addModisBands(product, ncFile.getVariables());
        addGeocoding(product);

        // todo - think about maybe possibly sometime creating a flag for questionable data
//        addFlagsAndMasks(product);
        product.setAutoGrouping("RefSB:Emissive");

        return product;

    }

    private Map<Band, Variable> addModisBands(Product product, List<Variable> variables) {

        final int[] MODIS_WVL = new int[]{645, 859, 469, 555, 1240, 1640, 2130, 412, 443, 488, 531, 547, 667, 678,
                748, 869, 905, 936, 940, 3750, 3959, 3959, 4050, 4465, 4515, 1375, 6715, 7325, 8550, 9730, 11030, 12020,
                13335, 13635, 13935, 14235};

        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();

        Map<Band, Variable> bandToVariableMap = new HashMap<Band, Variable>();
        int spectralBandIndex = 0;
        for (Variable variable : variables) {


            int variableRank = variable.getRank();
            if (variableRank == 3) {
                final int[] dimensions = variable.getShape();
                final int bands = dimensions[0];
                final int height = dimensions[1];
                final int width = dimensions[2];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    final List<Attribute> list = variable.getAttributes();
                    Attribute band_names = findAttribute("band_names", list);
                    if (band_names != null) {
                        String bnames = band_names.getStringValue();
                        String[] bname_array = bnames.split(",");
                        String units = null;
                        String description = null;
                        Array slope = null;
                        Array intercept = null;
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
                            } else if (slope == null && "radiance_scales".equals(attribName)) {
                                slope = hdfAttribute.getValues();
                            } else if (intercept == null && "radiance_offsets".equals(attribName)) {
                                intercept = hdfAttribute.getValues();
                            }
                        }

                        for (int i = 0; i < bands; i++) {
                            final String shortname = variable.getShortName();
                            StringBuilder longname = new StringBuilder(shortname);
                            longname.append("_");
                            longname.append(bname_array[i]);
                            String name = longname.toString();
                            final int dataType = getProductDataType(variable);
                            final Band band = new Band(name, dataType, width, height);

                            product.addBand(band);

                            int wvlidx;

                            if (bname_array[i].contains("lo") || bname_array[i].contains("hi")) {
                                wvlidx = Integer.parseInt(bname_array[i].substring(0, 1)) - 1;
                            } else {
                                wvlidx = Integer.parseInt(bname_array[i]) - 1;
                            }

                            final float wavelength = (float) MODIS_WVL[wvlidx];
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
                            if (slope != null) {
                                band.setScalingFactor(slope.getDouble(i));

                                if (intercept != null)
                                    band.setScalingOffset(intercept.getDouble(i) * slope.getDouble(i));
                            }
                        }
                    }
                }
            }

        }
        return bandToVariableMap;
    }

    public void addGeocoding(final Product product) throws ProductIOException {

        final String navGroupMODIS = "MODIS_SWATH_Type_L1B/Geolocation Fields";
        // read latitudes and longitudes
        int cntl_lat_ix;
        int cntl_lon_ix;
        String resolution = product.getMetadataRoot().getElement("Global_Attributes").getAttribute("MODIS Resolution").getData().getElemString();
        if (resolution.equals("500m")) {
            cntl_lat_ix = 2;
            cntl_lon_ix = 2;
        } else if (resolution.equals("250m")) {
            cntl_lat_ix = 4;
            cntl_lon_ix = 4;
        } else {
            cntl_lat_ix = 5;
            cntl_lon_ix = 5;
        }

        Variable lats = ncFile.findVariable(navGroupMODIS + "/" + "Latitude");
        Variable lons = ncFile.findVariable(navGroupMODIS + "/" + "Longitude");
        int[] dims = lats.getShape();

        float[] latTiePoints;
        float[] lonTiePoints;

        try {
            Array latarr = lats.read();

            Array lonarr = lons.read();
            if (mustFlipX && mustFlipY) {
                latTiePoints = (float[]) latarr.flip(0).flip(1).copyTo1DJavaArray();
                lonTiePoints = (float[]) lonarr.flip(0).flip(1).copyTo1DJavaArray();
            } else {
                latTiePoints = (float[]) latarr.getStorage();
                lonTiePoints = (float[]) lonarr.getStorage();
            }

            final TiePointGrid latGrid = new TiePointGrid("latitude", dims[1], dims[0], 0, 0,
                    cntl_lat_ix, cntl_lon_ix, latTiePoints);

            product.addTiePointGrid(latGrid);

            final TiePointGrid lonGrid = new TiePointGrid("longitude", dims[1], dims[0], 0, 0,
                    cntl_lat_ix, cntl_lon_ix, lonTiePoints);

            product.addTiePointGrid(lonGrid);

            product.setGeoCoding(new TiePointGeoCoding(latGrid, lonGrid, Datum.WGS_84));

        } catch (IOException e) {
            throw new ProductIOException(e.getMessage());
        }
    }

    public boolean mustFlipMODIS() throws ProductIOException {
        String platform = getStringAttribute("MODIS Platform");
        String dnflag = getStringAttribute("DayNightFlag");

        boolean startNodeAscending = false;
        boolean endNodeAscending = false;
        if (platform.contains("Aqua")) {
            if (dnflag.contains("Day")) {
                startNodeAscending = true;
                endNodeAscending = true;
            }
        } else if (platform.contains("Terra")) {
            if (dnflag.contains("Night")) {
                startNodeAscending = true;
                endNodeAscending = true;
            }
        }

        return (startNodeAscending && endNodeAscending);
    }

    public void addGlobalAttributeModisL1B() {
        Element eosElement = null;
        try {
            eosElement = getEosElement("CoreMetadata", ncFile.getRootGroup());
            //  eosElement = HdfEosUtils.getEosElement(CORE_METADATA, ncfile.getRootGroup());
        } catch (IOException e) {
            e.printStackTrace();  //Todo add a valid exception
            System.out.print("Whoops...");
        }

        //grab granuleID
        try {
            Element inventoryMetadata = eosElement.getChild("INVENTORYMETADATA");
            Element inventoryElem = (Element) inventoryMetadata.getChildren().get(0);
            Element ecsdataElem = inventoryElem.getChild("ECSDATAGRANULE");
            Element granIdElem = ecsdataElem.getChild("LOCALGRANULEID");
            String granId = granIdElem.getValue().substring(1);
            Attribute granIdAttribute = new Attribute("Product Name", granId);
            globalAttributes.add(granIdAttribute);
            Element dnfElem = ecsdataElem.getChild("DAYNIGHTFLAG");
            String daynightflag = dnfElem.getValue().substring(1);
            Attribute dnfAttribute = new Attribute("DayNightFlag", daynightflag);
            globalAttributes.add(dnfAttribute);

            //grab granule date-time
            Element timeElem = inventoryElem.getChild("RANGEDATETIME");
            Element startTimeElem = timeElem.getChild("RANGEBEGINNINGTIME");
            String startTime = startTimeElem.getValue().substring(1);
            Element startDateElem = timeElem.getChild("RANGEBEGINNINGDATE");
            String startDate = startDateElem.getValue().substring(1);
            Attribute startTimeAttribute = new Attribute("Start Time", startDate + ' ' + startTime);
            globalAttributes.add(startTimeAttribute);

            Element endTimeElem = timeElem.getChild("RANGEENDINGTIME");
            String endTime = endTimeElem.getValue().substring(1);
            Element endDateElem = timeElem.getChild("RANGEENDINGDATE");
            String endDate = endDateElem.getValue().substring(1);
            Attribute endTimeAttribute = new Attribute("End Time", endDate + ' ' + endTime);
            globalAttributes.add(endTimeAttribute);

            Element measuredParamElem = inventoryElem.getChild("MEASUREDPARAMETER");
            Element measuredContainerElem = measuredParamElem.getChild("MEASUREDPARAMETERCONTAINER");
            Element paramElem = measuredContainerElem.getChild("PARAMETERNAME");
            String param = paramElem.getValue().substring(1);
            String resolution = "1km";
            if (param.contains("EV_500")) {
                resolution = "500m";
            } else if (param.contains("EV_250")) {
                resolution = "250m";
            }
            Attribute paramAttribute = new Attribute("MODIS Resolution", resolution);
            globalAttributes.add(paramAttribute);

            //grab Mission Name
            Element platformElem = inventoryElem.getChild("ASSOCIATEDPLATFORMINSTRUMENTSENSOR");
            Element containerElem = platformElem.getChild("ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER");
            Element shortNameElem = containerElem.getChild("ASSOCIATEDPLATFORMSHORTNAME");
            String shortName = shortNameElem.getValue().substring(2);
            Attribute shortNameAttribute = new Attribute("MODIS Platform", shortName);
            globalAttributes.add(shortNameAttribute);
        } catch (Exception e) {

        }
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
}
