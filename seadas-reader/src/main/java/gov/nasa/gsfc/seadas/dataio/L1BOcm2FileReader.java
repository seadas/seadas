package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.PixelGeoCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.nc2.Variable;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 2/8/13
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class L1BOcm2FileReader extends SeadasFileReader {

    L1BOcm2FileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {

        int sceneWidth = getIntAttribute("Pixels per Scan Line");
        int sceneHeight = getIntAttribute("Number of Scan Lines");
        try {
            String navGroup = "variables";
            final String latitude = "latitude(Scans, Pixels)";
            final Variable variable = ncFile.findVariable("Pixels");
            invalidateLines(LAT_SKIP_BAD_NAV,variable);

            sceneHeight -= leadLineSkip;
            sceneHeight -= tailLineSkip;

        } catch (IOException ignore) {

        }
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
        addInputParamMetadata(product);
        addBandMetadata(product);
        addScientificMetadata(product);

        variableMap = addBands(product, ncFile.getVariables());

        addGeocoding(product);

        addFlagsAndMasks(product);
        product.setAutoGrouping("Rrs:nLw:Lt:La:Lr:Lw:L_q:L_u:Es:TLg:rhom:rhos:rhot:Taua:Kd:aot:adg:aph:bbp:vgain:BT:tg_sol:tg_sen");

        return product;
    }

    public void addGeocoding(final Product product) throws ProductIOException {
        // see if bowtie geocoding is needed
        String res = null;
        String sensor = null;
        try {
            sensor = product.getMetadataRoot().getElement("Global_Attributes").getAttribute("Sensor").getData().getElemString();
            res = product.getMetadataRoot().getElement("Input_Parameters").getAttribute("RESOLUTION").getData().getElemString();
        } catch(Exception e) {}

        if(sensor != null) {
            sensor = sensor.toLowerCase();
            if(sensor.contains("viirs")) {
                addBowtieGeocoding(product, 16);
                return;
            } else if(sensor.contains("modis")) {
                int scanHeight = 10;
                if(res != null) {
                    if(res.equals("500")) {
                        scanHeight = 20;
                    } else if(res.equals("250")) {
                        scanHeight = 40;
                    }
                }
                addBowtieGeocoding(product, scanHeight);
                return;
            } // modis
        }
        addPixelGeocoding(product);
    }

    public void addBowtieGeocoding(final Product product, int scanHeight) throws ProductIOException {
        final String longitude = "longitude";
        final String latitude = "latitude";
        Band latBand;
        Band lonBand;

        if (product.containsBand(latitude) && product.containsBand(longitude)) {
            latBand = product.getBand(latitude);
            lonBand = product.getBand(longitude);
            latBand.setNoDataValue(-999.);
            lonBand.setNoDataValue(-999.);
            latBand.setNoDataValueUsed(true);
            lonBand.setNoDataValueUsed(true);
            product.setGeoCoding(new BowtiePixelGeoCoding(latBand, lonBand, scanHeight, 0));
        }

    }

    public void addPixelGeocoding(final Product product) throws ProductIOException {
        String navGroup = "Navigation Data";
        final String longitude = "longitude";
        final String latitude = "latitude";
        final String cntlPoints = "cntl_pt_cols";
        Band latBand = null;
        Band lonBand = null;

        if (product.containsBand(latitude) && product.containsBand(longitude)) {
            latBand = product.getBand(latitude);
            lonBand = product.getBand(longitude);
            latBand.setNoDataValue(-999.);
            lonBand.setNoDataValue(-999.);
            latBand.setNoDataValueUsed(true);
            lonBand.setNoDataValueUsed(true);
        } else {
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
                latBand.setNoDataValue(-999.);
                lonBand.setNoDataValue(-999.);
                latBand.setNoDataValueUsed(true);
                lonBand.setNoDataValueUsed(true);

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

    }

}

