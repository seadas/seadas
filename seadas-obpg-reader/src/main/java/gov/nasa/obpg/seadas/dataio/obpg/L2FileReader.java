package gov.nasa.obpg.seadas.dataio.obpg;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.*;
import ucar.ma2.Array;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: seadas
 * Date: 11/14/11
 * Time: 2:23 PM
  */
public class L2FileReader extends SeadasFileReader {

    L2FileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {

        int sceneWidth = getIntAttribute("Pixels per Scan Line");
        int sceneHeight = getIntAttribute("Number of Scan Lines");
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
                product.setEndTime(utcStart);
            }
        }

        product.setFileLocation(productReader.getInputFile());
        product.setProductReader(productReader);

        addGlobalMetadata(product);
        addScientificMetadata(product);

        variableMap = addBands(product, ncFile.getVariables());

        addGeocoding(product);

        addFlagsAndMasks(product);
        product.setAutoGrouping("Rrs:nLw:Lt:La:Lr:Lw:Es:TLg:rhom:rhos:rhot:Taua:Kd:aot:adg:aph:bbp:vgain:BT");

        return product;
    }

    public void addGeocoding(final Product product) throws ProductIOException {
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

    }
    private void addFlagsAndMasks(Product product) {
        Band QFBand = product.getBand("l2_flags");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("L2Flags");
            flagCoding.addFlag("ATMFAIL", 0x01, "Atmospheric correction failure");
            flagCoding.addFlag("LAND", 0x02, "Land");
            flagCoding.addFlag("PRODWARN", 0x04, "One (or more) product algorithms generated a warning");
            flagCoding.addFlag("HIGLINT", 0x08, "High glint determined");
            flagCoding.addFlag("HILT", 0x10, "High (or saturating) TOA radiance");
            flagCoding.addFlag("HISATZEN", 0x20, "Large satellite zenith angle");
            flagCoding.addFlag("COASTZ", 0x40, "Shallow water (<30m)");
            flagCoding.addFlag("SPARE8", 0x80, "Unused");
            flagCoding.addFlag("STRAYLIGHT", 0x100, "Straylight determined");
            flagCoding.addFlag("CLDICE", 0x200, "Cloud/Ice determined");
            flagCoding.addFlag("COCCOLITH", 0x400, "Coccolithophores detected");
            flagCoding.addFlag("TURBIDW", 0x800, "Turbid water determined");
            flagCoding.addFlag("HISOLZEN", 0x1000, "High solar zenith angle");
            flagCoding.addFlag("SPARE14", 0x2000, "Unused");
            flagCoding.addFlag("LOWLW", 0x4000, "Low Lw @ 555nm (possible cloud shadow)");
            flagCoding.addFlag("CHLFAIL", 0x8000, "Chlorophyll algorithm failure");
            flagCoding.addFlag("NAVWARN", 0x10000, "Navigation suspect");
            flagCoding.addFlag("ABSAER", 0x20000, "Absorbing Aerosols determined");
            flagCoding.addFlag("SPARE19", 0x40000, "Unused");
            flagCoding.addFlag("MAXAERITER", 0x80000, "Maximum iterations reached for NIR iteration");
            flagCoding.addFlag("MODGLINT", 0x100000, "Moderate glint determined");
            flagCoding.addFlag("CHLWARN", 0x200000, "Chlorophyll out-of-bounds (<0.01 or >100 mg m^-3)");
            flagCoding.addFlag("ATMWARN", 0x400000, "Atmospheric correction warning; Epsilon out-of-bounds");
            flagCoding.addFlag("SPARE24", 0x800000, "Unused");
            flagCoding.addFlag("SEAICE", 0x1000000, "Sea ice determined");
            flagCoding.addFlag("NAVFAIL", 0x2000000, "Navigation failure");
            flagCoding.addFlag("FILTER", 0x4000000, "Insufficient data for smoothing filter");
            flagCoding.addFlag("SSTWARN", 0x8000000, "Sea surface temperature suspect");
            flagCoding.addFlag("SSTFAIL", 0x10000000, "Sea surface temperature algorithm failure");
            flagCoding.addFlag("HIPOL", 0x20000000, "High degree of polariztion determined");
            flagCoding.addFlag("PRODFAIL", 0x40000000, "One (or more) product algorithms produced a failure");
            flagCoding.addFlag("SPARE32", 0x80000000, "Unused");


            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);


            product.getMaskGroup().add(Mask.BandMathsType.create("ATMFAIL", "Atmospheric correction failure",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.ATMFAIL",
                                                                 Color.RED, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("LAND", "Land",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.LAND",
                                                                 Color.GREEN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("PRODWARN", "One (or more) product algorithms generated a warning",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.PRODWARN",
                                                                 Color.LIGHT_GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("HILT", "High (or saturating) TOA radiance",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.HILT",
                                                                 Color.WHITE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("HIGLINT", "High glint determined",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.HIGLINT",
                                                                 Color.PINK, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("HISATZEN", "Large satellite zenith angle",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.HISATZEN",
                                                                 Color.BLACK, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("COASTZ", "Shallow water (<30m)",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.COASTZ",
                                                                 Color.ORANGE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("STRAYLIGHT", "Straylight determined",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.STRAYLIGHT",
                                                                 Color.YELLOW, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("CLDICE", "Cloud/Ice determined",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.CLDICE",
                                                                 Color.WHITE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("COCCOLITH", "Coccolithophores detected",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.COCCOLITH",
                                                                 Color.CYAN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("TURBIDW", "Turbid water determined",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.TURBIDW",
                                                                 Color.DARK_GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("HISOLZEN", "High solar zenith angle",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.HISOLZEN",
                                                                 Color.BLACK, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("LOWLW", "Low Lw @ 555nm (possible cloud shadow)",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.LOWLW",
                                                                 Color.BLUE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("CHLFAIL", "Chlorophyll algorithm failure",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.CHLFAIL",
                                                                 Color.RED, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("NAVWARN", "Navigation suspect",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.NAVWARN",
                                                                 Color.MAGENTA, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("ABSAER", "Absorbing Aerosols determined",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.ABSAER",
                                                                 Color.ORANGE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("MAXAERITER", "Maximum iterations reached for NIR correction",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.MAXAERITER",
                                                                 Color.GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("MODGLINT", "Moderate glint determined",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.MODGLINT",
                                                                 Color.MAGENTA, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("CHLWARN", "Chlorophyll out-of-bounds (<0.01 or >100 mg m^-3)",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.CHLWARN",
                                                                 Color.LIGHT_GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("ATMWARN", "Atmospheric correction warning; Epsilon out-of-bounds",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.ATMWARN",
                                                                 Color.MAGENTA, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("SEAICE", "Sea ice determined",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.SEAICE",
                                                                 Color.DARK_GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("NAVFAIL", "Navigation failure",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.NAVFAIL",
                                                                 Color.RED, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("FILTER", "Insufficient data for smoothing filter",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.FILTER",
                                                                 Color.LIGHT_GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("SSTWARN", "Sea surface temperature suspect",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.SSTWARN",
                                                                 Color.MAGENTA, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("SSTFAIL", "Sea surface temperature algorithm failure",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.SSTFAIL",
                                                                 Color.RED, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("HIPOL", "High degree of polariztion determined",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.HIPOL",
                                                                 Color.PINK, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("PRODFAIL", "One (or more) product algorithms produced a failure",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "l2_flags.PRODFAIL",
                                                                 Color.RED, 0.2));

        }

    }
}