package gov.nasa.gsfc.seadas.dataio;

import gov.nasa.gsfc.seadas.dataio.SeadasProductReader.ProductType;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.CrsGeoCoding;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: seadas
 * Date: 11/14/11
 * Time: 2:23 PM
 */
public class SMIFileReader extends SeadasFileReader {

    SMIFileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {
        //todo incoroprate the SMI product table info to replace the getL2BandInfoMap stuff.

        int [] dims = ncFile.getVariables().get(0).getShape();
        int sceneHeight = dims[0];
        int sceneWidth = dims[1];

        String [] nameparts = ncFile.getLocation().split(File.separator);
        String productName = nameparts[nameparts.length-1];
        try {
                productName = getStringAttribute("Product Name");
        } catch (Exception e) {

        }

        SeadasProductReader.ProductType productType = productReader.getProductType();

        Product product = new Product(productName, productType.toString(), sceneWidth, sceneHeight);
        product.setDescription(productName);

        product.setFileLocation(productReader.getInputFile());
        product.setProductReader(productReader);

        addGlobalMetadata(product);
        addSmiMetadata(product);
        variableMap = addBands(product, ncFile.getVariables());

        addGeocoding(product);
        return product;
    }

    @Override
    protected Band addNewBand(Product product, Variable variable) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        Band band = null;

        int variableRank = variable.getRank();
            if (variableRank == 2) {
                final int[] dimensions = variable.getShape();
                final int height = dimensions[0];
                final int width = dimensions[1];
                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    String name = variable.getShortName();
                    if (name.equals("l3m_data")){
                        try {
                            name = new StringBuilder().append(getStringAttribute("Parameter")).append(" ").append(getStringAttribute("Measure")).toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    final int dataType = getProductDataType(variable);
                    band = new Band(name, dataType, width, height);

                    product.addBand(band);

                    try {
                        if (productReader.getProductType() == ProductType.MEaSUREs){
                            band.setNoDataValue((double) variable.findAttribute("_FillValue").getNumericValue().floatValue());
                        } else {
                            band.setNoDataValue((double) variable.findAttribute("Fill").getNumericValue().floatValue());
                        }
                        band.setNoDataValueUsed(true);
                    } catch (Exception e) {

                    }
                    // Set units, if defined
                    try {
                        band.setUnit(getStringAttribute("Units"));
                    }  catch (Exception e){

                    }

                    final List<Attribute> list = variable.getAttributes();
                    for (Attribute hdfAttribute : list) {
                        final String attribName = hdfAttribute.getName();
                         if ("Slope".equals(attribName)) {
                            band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("Intercept".equals(attribName)) {
                            band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                        }
                    }
                }
            }
        return band;
    }

    public void addGeocoding(Product product) {
        //float pixelX = 0.0f;
        //float pixelY = 0.0f;
        // Changed after conversation w/ Sean, Norman F., et al.
        float pixelX = 0.5f;
        float pixelY = 0.5f;
        String east = "Easternmost Longitude";
        String west = "Westernmost Longitude";
        String north = "Northernmost Latitude";
        String south = "Southernmost Latitude";
        if (productReader.getProductType() == ProductType.MEaSUREs){
            east = "Easternmost_Longitude";
            west = "Westernmost_Longitude";
            north = "Northernmost_Latitude";
            south = "Southernmost_Latitude";
        }

        float easting = (float) product.getMetadataRoot().getElement("Global_Attributes").getAttribute(east).getData().getElemDouble();
        float westing = (float) product.getMetadataRoot().getElement("Global_Attributes").getAttribute(west).getData().getElemDouble();
        float pixelSizeX = (easting - westing) / product.getSceneRasterWidth();
        float northing = (float) product.getMetadataRoot().getElement("Global_Attributes").getAttribute(north).getData().getElemDouble();
        float southing = (float) product.getMetadataRoot().getElement("Global_Attributes").getAttribute(south).getData().getElemDouble();
        float pixelSizeY = (northing - southing) / product.getSceneRasterHeight();

        try {
            product.setGeoCoding(new CrsGeoCoding(DefaultGeographicCRS.WGS84,
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(),
                    westing, northing,
                    pixelSizeX, pixelSizeY,
                    pixelX, pixelY));
        } catch (FactoryException e) {
            throw new IllegalStateException(e);
        } catch (TransformException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addSmiMetadata(final Product product) {
//        Variable l3mvar = ncFile.findVariable("l3m_data");
        Variable l3mvar = ncFile.getVariables().get(0);
        List<Attribute> variableAttributes = l3mvar.getAttributes();
        final MetadataElement smiElement = new MetadataElement("SMI Product Parameters");
        addAttributesToElement(variableAttributes, smiElement);

        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(smiElement);
    }
}