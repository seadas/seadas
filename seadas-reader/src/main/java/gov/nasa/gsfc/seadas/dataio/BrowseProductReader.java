package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.PixelGeoCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: seadas
 * Date: 11/14/11
 * Time: 2:23 PM
  */
public class BrowseProductReader extends SeadasFileReader {

    BrowseProductReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {

        int sceneWidth = getIntAttribute("Pixels per Scan Line");
        int sceneHeight = getIntAttribute("Number of Scan Lines");
//        try {
//            String navGroup = "Navigation";
//            final String latitude = "latitude";
//            if (ncFile.findGroup(navGroup) == null) {
//                if (ncFile.findGroup("Navigation") != null) {
//                    navGroup = "Navigation";
//                }
//            }
//            final Variable variable = ncFile.findVariable(navGroup + "/" + latitude);
//            invalidateLines(LAT_SKIP_BAD_NAV,variable);
//
//            sceneHeight -= leadLineSkip;
//            sceneHeight -= tailLineSkip;
//
//        } catch (IOException ignore) {
//
//       }
        String productName = getStringAttribute("Product Name");

        mustFlipX = mustFlipY = getDefaultFlip();
        SeadasProductReader.ProductType productType = productReader.getProductType();
        if (productType == SeadasProductReader.ProductType.Level1A_CZCS ||
                productType == SeadasProductReader.ProductType.Level2_CZCS)
            mustFlipX = false;

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
                product.setEndTime(utcEnd);
            }
        }

        product.setFileLocation(productReader.getInputFile());
        product.setProductReader(productReader);

        addGlobalMetadata(product);
//        addBandMetadata(product);

        variableMap = addBands(product, ncFile.getVariables());

        addGeocoding(product);

        return product;
    }

    @Override
    public Map<Band, Variable> addBands(Product product,
                                        List<Variable> variables) {
        final Map<Band, Variable> bandToVariableMap = new HashMap<Band, Variable>();
        for (Variable variable : variables) {
            Band band = addNewBand(product, variable);
            if (band != null) {
                bandToVariableMap.put(band, variable);
            }
        }

        return bandToVariableMap;
    }
    @Override
    protected Band addNewBand(Product product, Variable variable) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        Band band = null;

        int variableRank = variable.getRank();
        if (variableRank == 2) {
            final int[] dimensions = variable.getShape();
            final int height = dimensions[0] - leadLineSkip - tailLineSkip;
            final int width = dimensions[1];
            if (height == sceneRasterHeight && width == sceneRasterWidth) {
                String name = variable.getShortName();
                boolean isBrs = false;
                try {
                    if (variable.findAttribute("long_name").getStringValue().contains("brs_data")) isBrs = true;
                    else isBrs = false;
                } catch (Exception ignored) { }
                if (isBrs){
                    try {
                        name = getStringAttribute("Parameter");
                    } catch (Exception ignore) { }
                }
                final int dataType = getProductDataType(variable);
                band = new Band(name, dataType, width, height);
                final String validExpression = bandInfoMap.get(name);
                if (validExpression != null && !validExpression.equals("")) {
                    band.setValidPixelExpression(validExpression);
                }
                product.addBand(band);


                if (isBrs){
                    try {
                        band.setScalingFactor(getFloatAttribute("Slope"));
                        band.setScalingOffset(getFloatAttribute("Intercept"));
                        band.setNoDataValue(253.);// * band.getScalingFactor() + band.getScalingOffset()));
                        band.setNoDataValueUsed(true);
                    } catch (Exception ignored) { }
                } else {
                    final List<Attribute> list = variable.getAttributes();
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
                        }
                    }
                }
            }
        }
        return band;
    }
    public void addGeocoding(final Product product) throws ProductIOException {
        final String longitude = "longitude";
        final String latitude = "latitude";
        Band latBand = null;
        Band lonBand = null;

        latBand = product.getBand(latitude);
        lonBand = product.getBand(longitude);
        latBand.setNoDataValue(-999.);
        lonBand.setNoDataValue(-999.);
        latBand.setNoDataValueUsed(true);
        lonBand.setNoDataValueUsed(true);

        try {
            if (latBand != null && lonBand != null) {
                    product.setGeoCoding(new PixelGeoCoding(latBand, lonBand, null, 5, ProgressMonitor.NULL));
            }
        } catch (IOException e) {
                throw new ProductIOException(e.getMessage());
        }
    }
}