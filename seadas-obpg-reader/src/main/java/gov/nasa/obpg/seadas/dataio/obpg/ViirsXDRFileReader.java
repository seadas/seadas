package gov.nasa.obpg.seadas.dataio.obpg;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.*;
import ucar.ma2.Array;
import ucar.nc2.*;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: seadas
 * Date: 11/14/11
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViirsXDRFileReader extends SeadasFileReader {
    private NetcdfFile geofile;

    ViirsXDRFileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {
        final HashMap<String, String> l2BandInfoMap = getL2BandInfoMap();
        final HashMap<String, String> l2FlagsInfoMap = getL2FlagsInfoMap();
        final BitmaskDef[] defs = getDefaultBitmaskDefs(l2FlagsInfoMap);

        try {
            Group allData = ncFile.findGroup("All_Data");
            List<Dimension> dims;
            if (productReader.getProductType() == SeadasProductReader.ProductType.VIIRS_EDR) {
                dims = allData.getGroups().get(0).getVariables().get(0).getDimensions();
            } else {
                dims = allData.getGroups().get(0).getVariables().get(14).getDimensions();
            }

            int sceneHeight = dims.get(0).getLength();
            int sceneWidth = dims.get(1).getLength();

            String productName = productReader.getInputFile().getName();

            mustFlipX = mustFlipY = mustFlipVIIRS();
            SeadasProductReader.ProductType productType = productReader.getProductType();

            Product product = new Product(productName, productType.toString(), sceneWidth, sceneHeight);
            product.setDescription(productName);

            setStartEndTime(product);

            product.setFileLocation(productReader.getInputFile());
            product.setProductReader(productReader);

            addGlobalAttributeVIIRS();
            addGlobalMetadata(product);

            variableMap = addBands(product, ncFile.getVariables());

            addGeocoding(product);

            addBitmaskDefinitions(product, defs);
            product.setAutoGrouping("IOP:QF:nLw");

            return product;
        } catch (Exception e) {
            throw new ProductIOException(e.getMessage());
        }
    }

    @Override
    protected Band addNewBand(Product product, Variable variable) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        Band band = null;
        String[] factors = {"Radiance", "Reflectance", "BulkSST", "SkinSST"};
        int variableRank = variable.getRank();
        if (variableRank == 2) {
            final int[] dimensions = variable.getShape();
            final int height = dimensions[0];
            final int width = dimensions[1];
            if (height == sceneRasterHeight && width == sceneRasterWidth) {
                final String name = variable.getShortName();
                final int dataType = getProductDataType(variable);
                band = new Band(name, dataType, width, height);

                product.addBand(band);

                try {
                    String varname = variable.getShortName();

                    for (String v : factors) {
                        if (v.equals(varname)) {
                            String facvar = v + "Factors";
                            Group group = ncFile.getRootGroup().findGroup("All_Data").getGroups().get(0);
                            Variable factor = group.findVariable(facvar);

                            Array slpoff = factor.read();
                            float slope = slpoff.getFloat(0);

                            float intercept = slpoff.getFloat(1);

                            band.setScalingFactor((double) slope);
                            band.setScalingOffset((double) intercept);
                        }
                    }
                    band.setNoDataValue((double) variable.findAttribute("_FillValue").getNumericValue().floatValue());
                } catch (Exception e) {

                }
//todo: think about interpreting QF fields for flag coding

//                    FlagCoding flagCoding = null;
//
//                    if (flagCoding == null) {
//                        flagCoding = new FlagCoding(name);
//                    }
//                    final int flagMask = convertToFlagMask(attribName);
//                    flagCoding.addFlag(flagName, flagMask, flagsInfoMap.get(flagName));
//
//                    if (flagCoding != null) {
//                        band.setSampleCoding(flagCoding);
//                        product.getFlagCodingGroup().add(flagCoding);
//                    }
            }
        }
        return band;
    }

    public void addGeocoding(final Product product) throws ProductIOException {
        try {
            //todo: refine logic to get correct navGroup
            File inputFile = productReader.getInputFile();
            String navGroup = "All_Data/VIIRS-MOD-GEO-TC_All";
            String geoFileName = getStringAttribute("N_GEO_Ref");
            String path = inputFile.getParent();
            File geocheck = new File(path, geoFileName);
            if (!geocheck.exists()) {
                geoFileName = geoFileName.replaceFirst("GMODO", "GMTCO");
                geocheck = new File(path, geoFileName);
                if (!geocheck.exists()) {
                    if (!inputFile.getName().matches("_c\\d{20}_")) {
                        geoFileName = geoFileName.replaceFirst("_c\\d{20}_", "_");
                    }
                    geocheck = new File(path, geoFileName);
                    if (!geocheck.exists()) {
                        return;
                    }
                }
            }

            geofile = NetcdfFile.open(geocheck.getPath());

            final String longitude = "Longitude";
            final String latitude = "Latitude";

            Band latBand = new Band("latitude", ProductData.TYPE_FLOAT32, product.getSceneRasterWidth(), product.getSceneRasterHeight());
            Band lonBand = new Band("longitude", ProductData.TYPE_FLOAT32, product.getSceneRasterWidth(), product.getSceneRasterHeight());
            product.addBand(latBand);
            product.addBand(lonBand);


            Array latarr = geofile.findVariable(navGroup + "/" + latitude).read();

            Array lonarr = geofile.findVariable(navGroup + "/" + longitude).read();
            float[] latitudes;
            float[] longitudes;
            if (mustFlipX && mustFlipY) {
                latitudes = (float[]) latarr.flip(0).flip(1).copyTo1DJavaArray();
                longitudes = (float[]) lonarr.flip(0).flip(1).copyTo1DJavaArray();
            } else {
                latitudes = (float[]) latarr.getStorage();
                longitudes = (float[]) lonarr.getStorage();
            }

            ProductData lats = ProductData.createInstance(latitudes);
            latBand.setData(lats);
            ProductData lons = ProductData.createInstance(longitudes);
            lonBand.setData(lons);
            product.setGeoCoding(new PixelGeoCoding(latBand, lonBand, null, 5, ProgressMonitor.NULL));

        } catch (Exception e) {
            throw new ProductIOException(e.getMessage());
        }
    }

    public boolean mustFlipVIIRS() throws ProductIOException {
        List<Variable> vars = ncFile.getVariables();
        for (Variable var : vars) {
            if (var.getShortName().contains("DR_Gran_")) {
                List<Attribute> attrs = var.getAttributes();
                for (Attribute attr : attrs) {
                    if (attr.getName().equals("Ascending/Descending_Indicator")) {
                        if (attr.getNumericValue().longValue() == 0) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        throw new ProductIOException("Cannot find Ascending/Decending_Indicator");
    }


    private void setStartEndTime(Product product) throws ProductIOException {
        List<Variable> dataProductList = ncFile.getRootGroup().findGroup("Data_Products").getGroups().get(0).getVariables();
        for (Variable var : dataProductList) {
            if (var.getShortName().contains("DR_Aggr")) {
                String startDate = var.findAttribute("AggregateBeginningDate").getStringValue().trim();
                String startTime = var.findAttribute("AggregateBeginningTime").getStringValue().trim();
                String endDate = var.findAttribute("AggregateEndingDate").getStringValue().trim();
                String endTime = var.findAttribute("AggregateEndingTime").getStringValue().trim();

                final DateFormat dateFormat = ProductData.UTC.createDateFormat("yyyyMMddHHmmss");
                try {
                    String startTimeString = startDate + startTime.substring(0, 6);
                    String endTimeString = endDate + endTime.substring(0, 6);
                    final Date startdate = dateFormat.parse(startTimeString);
                    String startmicroSeconds = startTime.substring(startTimeString.length() - 7, startTimeString.length() - 1);

                    final Date enddate = dateFormat.parse(endTimeString);
                    String endmicroSeconds = endTime.substring(endTimeString.length() - 7, startTimeString.length() - 1);

                    if (mustFlipY) {
                        product.setStartTime(ProductData.UTC.create(enddate, Long.parseLong(endmicroSeconds)));
                        product.setEndTime(ProductData.UTC.create(startdate, Long.parseLong(startmicroSeconds)));
                    } else {
                        product.setStartTime(ProductData.UTC.create(startdate, Long.parseLong(startmicroSeconds)));
                        product.setEndTime(ProductData.UTC.create(enddate, Long.parseLong(endmicroSeconds)));
                    }

                } catch (ParseException e) {
                    throw new ProductIOException("Unable to parse start/end time attributes");
                }

            }
        }
    }

    public void addGlobalAttributeVIIRS() {
        List<Group> DataProductGroups = ncFile.getRootGroup().findGroup("Data_Products").getGroups();

        for (Group dpgroup : DataProductGroups) {
            String groupname = dpgroup.getShortName();
            if (groupname.matches("VIIRS-.*DR$")) {
                List<Variable> vars = dpgroup.getVariables();
                for (Variable var : vars) {
                    String varname = var.getShortName();
                    if (varname.matches(".*_(Aggr|Gran_0)$")) {
                        List<Attribute> attrs = var.getAttributes();
                        for (Attribute attr : attrs) {
                            globalAttributes.add(attr);

                        }
                    }

                }
            }

        }
    }
}
