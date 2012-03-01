package gov.nasa.gsfc.seadas.dataio;

import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.CrsGeoCoding;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: seadas
 * Date: 11/14/11
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class SMIFileReader extends SeadasFileReader {

    SMIFileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {
        //todo incoroprate the SMI product table info to replace the getL2BandInfoMap stuff.
        int sceneWidth = getIntAttribute("Number of Columns");
        int sceneHeight = getIntAttribute("Number of Lines");
        String productName = getStringAttribute("Product Name");

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

    public void addGeocoding(Product product) {
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

    public void addSmiMetadata(final Product product ) {
        Variable l3mvar = ncFile.findVariable("l3m_data");
        List<Attribute> variableAttributes = l3mvar.getAttributes();
        final MetadataElement smiElement = new MetadataElement("SMI Product Parameters");
        addAttributesToElement(variableAttributes, smiElement);

        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(smiElement);
    }
}