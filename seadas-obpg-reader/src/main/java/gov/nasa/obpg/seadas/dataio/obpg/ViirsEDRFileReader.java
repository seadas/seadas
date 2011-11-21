package gov.nasa.obpg.seadas.dataio.obpg;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.jexp.EvalException;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.*;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.Group;
import ucar.nc2.Dimension;

import java.io.IOException;
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
public class ViirsEDRFileReader extends SeadasFileReader {

    ViirsEDRFileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {
        final HashMap<String, String> l2BandInfoMap = getL2BandInfoMap();
        final HashMap<String, String> l2FlagsInfoMap = getL2FlagsInfoMap();
        final BitmaskDef[] defs = getDefaultBitmaskDefs(l2FlagsInfoMap);

        try {
            Group allData = ncFile.findGroup("All_Data");
            List<Dimension> dims = allData.getGroups().get(0).getVariables().get(0).getDimensions();
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

            addGlobalMetadata(product);
//            addScientificMetadata(product);

            variableMap = addBands(product, ncFile.getVariables(), l2BandInfoMap, l2FlagsInfoMap);

//            addGeocoding(product);

            addBitmaskDefinitions(product, defs);
            product.setAutoGrouping("IOP:QF:nLw");

            return product;
        } catch (Exception e) {
            throw new ProductIOException(e.getMessage());
        }
    }

    public void addGeocoding(final Product product) throws ProductIOException {
        try {
            String geoFileName = getStringAttribute("N_GEO_Ref");


            String navGroup = "Navigation Data";
            final String longitude = "longitude";
            final String latitude = "latitude";
            final String cntlPoints = "cntl_pt_cols";
            Band latBand = null;
            Band lonBand = null;

            if (ncFile.findGroup(navGroup) == null) {
                if (ncFile.findGroup("Navigation") != null) {
                    navGroup = "Navigation";
                }
            }

            if (product.containsBand(latitude) && product.containsBand(longitude)) {
                latBand = product.getBand(latitude);
                lonBand = product.getBand(longitude);
            } else {
                Variable latVar = ncFile.findVariable(navGroup + "/" + latitude);
                Variable lonVar = ncFile.findVariable(navGroup + "/" + longitude);
                Variable cntlPointVar = ncFile.findVariable(navGroup + "/" + cntlPoints);
                if (latVar != null && lonVar != null && cntlPointVar != null) {
                    final ProductData lonRawData = readData(lonVar);
                    final ProductData latRawData = readData(latVar);

                    latBand = product.addBand(latVar.getShortName(), ProductData.TYPE_FLOAT32);
                    lonBand = product.addBand(lonVar.getShortName(), ProductData.TYPE_FLOAT32);


                    Array cntArray;
                    try {
                        cntArray = cntlPointVar.read();
                        int[] colPoints = (int[]) cntArray.getStorage();
                        computeLatLonBandData(latBand, lonBand, latRawData, lonRawData, colPoints);
                    } catch (IOException e) {
                        throw new ProductIOException(e.getMessage());
                    }
                }
            }
            try {
                if (latBand != null && lonBand != null) {
                    product.setGeoCoding(new PixelGeoCoding(latBand, lonBand, null, 5, ProgressMonitor.NULL));
                }
            } catch (IOException e) {
                throw new ProductIOException(e.getMessage());
            }

        } catch (Exception e) {
        }
    }

    public boolean mustFlipVIIRS() throws ProductIOException {
        List<Variable> vars = ncFile.getVariables();
        for (Variable var : vars) {
            if (var.getShortName().contains("EDR_Gran_")) {
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
            if (var.getShortName().contains("EDR_Aggr")) {
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
}
