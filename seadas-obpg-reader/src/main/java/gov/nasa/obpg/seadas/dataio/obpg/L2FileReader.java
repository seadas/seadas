package gov.nasa.obpg.seadas.dataio.obpg;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.*;
import ucar.ma2.Array;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.HashMap;

import static gov.nasa.obpg.seadas.dataio.obpg.ObpgUtils.getInputFile;

/**
 * Created by IntelliJ IDEA.
 * User: seadas
 * Date: 11/14/11
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2FileReader extends SeadasFileReader {

    L2FileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {
        final HashMap<String, String> l2BandInfoMap = getL2BandInfoMap();
        final HashMap<String, String> l2FlagsInfoMap = getL2FlagsInfoMap();
        final BitmaskDef[] defs = getDefaultBitmaskDefs(l2FlagsInfoMap);

        int sceneWidth = getIntAttribute("Pixels per Scan Line");
        int sceneHeight = getIntAttribute("Number of Scan Lines");
        String productName = getStringAttribute("Product Name");

        mustFlipX = mustFlipY = getDefaultFlip();
        SeadasProductReader.ProductType productType = productReader.getProductType();
        if (productType == SeadasProductReader.ProductType.Level1A_CZCS)
            mustFlipX = false;

        Product product = new Product(productName, productType.toString(), sceneWidth, sceneHeight);
        product.setDescription(productName);

        ProductData.UTC utcStart = getUTCAttribute("Start Time");
        if (utcStart != null) {
            product.setStartTime(utcStart);
        }
        ProductData.UTC utcEnd = getUTCAttribute("End Time");
        if (utcEnd != null) {
            product.setEndTime(utcEnd);
        }

        product.setFileLocation(getInputFile(productReader.getInput()));
        product.setProductReader(productReader);

        addGlobalMetadata(product);
        addScientificMetadata(product);

        variableMap = addBands(product, ncFile.getVariables(), l2BandInfoMap, l2FlagsInfoMap);

        addGeocoding(product);

        addBitmaskDefinitions(product, defs);
        product.setAutoGrouping("Rrs:nLw:Lt:La:Lr:Lw:Es:TLg:rhom:rhos:rhot:Taua:Kd:aot:adg:aph:bbp:vgain:BT");

        return product;
    }

    public void addGeocoding(final Product product) throws ProductIOException {
        String navGroup = "Navigation Data";
        final String longitude = "longitude";
        final String latitude = "latitude";
        final String cntlPoints = "cntl_pt_cols";
        Band latBand;
        Band lonBand;

        if (ncFile.findGroup(navGroup) == null) {
            if (ncFile.findGroup("Navigation") != null) {
                navGroup = "Navigation";
            }
        }

        Variable latVar = ncFile.findVariable(navGroup + "/" + latitude);
        Variable lonVar = ncFile.findVariable(navGroup + "/" + longitude);
        Variable cntlPointVar = ncFile.findVariable(navGroup + "/" + cntlPoints);
        if (latVar != null && lonVar != null && cntlPointVar != null) {
            final ProductData lonRawData = readData(lonVar);
            final ProductData latRawData = readData(latVar);

            latBand = product.addBand(latVar.getShortName(), ProductData.TYPE_FLOAT32);
            lonBand = product.addBand(lonVar.getShortName(), ProductData.TYPE_FLOAT32);

            try {
                Array cntArray = cntlPointVar.read();
                int[] colPoints = (int[]) cntArray.getStorage();
                computeLatLonBandData(latBand, lonBand, latRawData, lonRawData, colPoints);
                if (latBand != null && lonBand != null) {
                    product.setGeoCoding(new PixelGeoCoding(latBand, lonBand, null, 5, ProgressMonitor.NULL));
                }
            } catch (IOException e) {
                throw new ProductIOException(e.getMessage());
            }
        }

    }

    private synchronized static HashMap<String, String> getL2BandInfoMap() {
        return readTwoColumnTable("l2-band-info.csv");
    }

    private synchronized static HashMap<String, String> getL2FlagsInfoMap() {
        return readTwoColumnTable("l2-flags-info.csv");
    }

}
