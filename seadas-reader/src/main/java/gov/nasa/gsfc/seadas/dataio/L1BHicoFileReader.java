package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.PixelGeoCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
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
public class L1BHicoFileReader extends SeadasFileReader {

    L1BHicoFileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    Array wavlengths = ncFile.findVariable("products/Lt").findAttribute("wavelengths").getValues();

    @Override
    public Product createProduct() throws ProductIOException {

        int[] dims = ncFile.findVariable("products/Lt").getShape();
        int sceneWidth = dims[1];
        int sceneHeight = dims[0];

        String navGroup = "navigation";
        final String latitude = "latitudes";

//        final float[] wvls = new float[wavlengths];
//        for (int i=0; i < strings.length; i++) {
//            ints[i] = Integer.parseInt(strings[i]);
//        }
//        final int[] HICO_WVL = ncFile.findVariable("products/Lt").findAttribute("wavelengths");
//        String productName = getStringAttribute("Dataset_Identifier");
        String productName = getStringAttribute("metadata/FGDC/Identification_Information/Dataset_Identifier");

        mustFlipX = mustFlipY = getDefaultFlip();
        SeadasProductReader.ProductType productType = productReader.getProductType();

        Product product = new Product(productName, productType.toString(), sceneWidth, sceneHeight);
        product.setDescription(productName);
//todo create method to populate time attributes
//        ProductData.UTC utcStart = getUTCAttribute("Start Time");
//        if (utcStart != null) {
//            if (mustFlipY){
//                product.setEndTime(utcStart);
//            } else {
//                product.setStartTime(utcStart);
//            }
//        }
//        ProductData.UTC utcEnd = getUTCAttribute("End Time");
//        if (utcEnd != null) {
//            if (mustFlipY) {
//                product.setStartTime(utcEnd);
//            } else {
//                product.setEndTime(utcEnd);
//            }
//        }

        product.setFileLocation(productReader.getInputFile());
        product.setProductReader(productReader);

        addGlobalMetadata(product);
        variableMap = addHicoBands(product, ncFile.getVariables());

        addGeocoding(product);
        addBandMetadata(product);
        // todo create method to add metadata from the various groups (data, navigation, images, etc)
        // todo add ability to read the true_color image inculded in the file
        //todo read in the flag bit variable

//        addFlagsAndMasks(product);
        product.setAutoGrouping("Lt");

        return product;
    }

    private Map<Band, Variable> addHicoBands(Product product, List<Variable> variables) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        Band band = null;

        Map<Band, Variable> bandToVariableMap = new HashMap<Band, Variable>();
        int spectralBandIndex = 0;
        for (Variable variable : variables) {
            if ((variable.getShortName().equals("latitudes")) || (variable.getShortName().equals("longitudes")))
                continue;
            int variableRank = variable.getRank();
            if (variableRank == 2) {
                final int[] dimensions = variable.getShape();
                final int height = dimensions[0] - leadLineSkip - tailLineSkip;
                final int width = dimensions[1];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    final String name = variable.getShortName();
                    final int dataType = getProductDataType(variable);
                    band = new Band(name, dataType, width, height);
                    final String validExpression = bandInfoMap.get(name);
                    if (validExpression != null && !validExpression.equals("")) {
                        band.setValidPixelExpression(validExpression);
                    }
                    product.addBand(band);

                    try {
                        band.setNoDataValue((double) variable.findAttribute("bad_value_scaled").getNumericValue().floatValue());
                        band.setNoDataValueUsed(true);
                    } catch (Exception ignored) { }

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
            if (variableRank == 3) {
                final int[] dimensions = variable.getShape();
                final int bands = dimensions[2];
                final int height = dimensions[0];
                final int width = dimensions[1];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    // final List<Attribute> list = variable.getAttributes();

                    String units = variable.getUnitsString();
                    String description = variable.getShortName();

                    for (int i = 0; i < bands; i++) {
                        final float wavelength = getHicoWvl(i);
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
                            sliced = variable.slice(2, i);
                        } catch (InvalidRangeException e) {
                            e.printStackTrace();  //Todo change body of catch statement.
                        }
                        try {
                            band.setNoDataValue((double) variable.findAttribute("bad_value_scaled").getNumericValue().floatValue());
                            band.setNoDataValueUsed(true);
                        } catch (Exception ignored) { }

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
                        bandToVariableMap.put(band, sliced);
                        band.setUnit(units);
                        band.setDescription(description);

                    }
                }
            }
        }
        return bandToVariableMap;
    }

    float getHicoWvl(int index) {
        return wavlengths.getFloat(index);
    }

    public void addGeocoding(final Product product) throws ProductIOException {
        final String longitude = "longitudes";
        final String latitude = "latitudes";
        String navGroup = "navigation";

        Variable latVar = ncFile.findVariable(navGroup + "/" + latitude);
        Variable lonVar = ncFile.findVariable(navGroup + "/" + longitude);
        if (latVar != null && lonVar != null ) {
            final ProductData lonRawData = readData(lonVar);
            final ProductData latRawData = readData(latVar);
            Band latBand = null;
            Band lonBand = null;
            latBand = product.addBand(latVar.getShortName(), ProductData.TYPE_FLOAT32);
            lonBand = product.addBand(lonVar.getShortName(), ProductData.TYPE_FLOAT32);
            latBand.setNoDataValue(-999.);
            lonBand.setNoDataValue(-999.);
            latBand.setNoDataValueUsed(true);
            lonBand.setNoDataValueUsed(true);
            latBand.setData(latRawData);
            lonBand.setData(lonRawData);

            try {
                if (latBand != null && lonBand != null) {
                    product.setGeoCoding(new PixelGeoCoding(latBand, lonBand, null, 5, ProgressMonitor.NULL));
                }
            } catch (IOException e) {
                throw new ProductIOException(e.getMessage());
            }
        }
    }
}