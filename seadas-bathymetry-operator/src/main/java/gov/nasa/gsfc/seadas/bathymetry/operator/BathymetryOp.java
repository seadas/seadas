package gov.nasa.gsfc.seadas.bathymetry.operator;

import com.bc.ceres.core.ProgressMonitor;
import gov.nasa.gsfc.seadas.bathymetry.ui.BathymetryData;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.Tile;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.util.ProductUtils;
import ucar.ma2.Array;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * The bathymetry operator is a GPF-Operator. It takes the geographic bounds of the input product and creates a new
 * product with the same bounds. The output product contains a single band, which is a land/water fraction mask.
 * For each pixel, it contains the fraction of water; a value of 0.0 indicates land, a value of 100.0 indicates water,
 * and every value in between indicates a mixed pixel.
 * <br/>
 * Since the base data may exhibit a higher resolution than the input product, a subsampling &ge;1 may be specified;
 * therefore, mixed pixels may occur.
 *
 * @author Danny Knowles
 */
@SuppressWarnings({"FieldCanBeLocal"})
@OperatorMetadata(alias = "BathymetryOp",
        version = "1.0",
        internal = true,
        authors = "Danny Knowles",
        copyright = "",
        description = "Operator creating a bathymetry band, elevation band, topography band and bathymetry mask")
public class BathymetryOp extends Operator {

    public static final String BATHYMETRY_BAND_NAME = "bathymetry";
    public static final String ELEVATION_BAND_NAME = "elevation";
    public static final String TOPOGRAPHY_BAND_NAME = "topography";

    @SourceProduct(alias = "source", description = "The Product the land/water-mask shall be computed for.",
            label = "Name")
    private Product sourceProduct;

    @Parameter(description = "Specifies on which resolution the water mask shall be based.", unit = "m/pixel",
            label = "Resolution", defaultValue = "1855", valueSet = {"1855"})
    private int resolution;

    @Parameter(description = "Bathymetry filename",
            label = "Filename", defaultValue = "ETOPO1_ocssw.nc",
            valueSet = {"ETOPO1_ocssw.nc"})
    private String filename;




    @TargetProduct
    private Product targetProduct;

    private BathymetryReader bathymetryReader;


    @Override
    public void initialize() throws OperatorException {

        File bathymetryFile = BathymetryData.getBathymetryFile(filename);

        if (bathymetryFile != null) {
            if (bathymetryFile.exists()) {
                System.out.print("Reading bathymetry source file " + bathymetryFile.getAbsolutePath()+"\n");
            } else {
                System.out.print("Bathymetry source file does not exist " + bathymetryFile.getAbsolutePath()+"\n");
            }
        } else {
            System.out.print("Reading bathymetry source file " + filename+"\n");
        }


        try {
            bathymetryReader = new BathymetryReader(bathymetryFile);
        } catch (IOException e) {
            if (bathymetryFile != null) {
                if (bathymetryFile.exists()) {
                    System.out.print("Error: Reading bathymetry source file " + bathymetryFile.getAbsolutePath()+"\n");
                } else {
                    System.out.print("Error: Bathymetry source file does not exist " + bathymetryFile.getAbsolutePath()+"\n");
                }
            } else {
                System.out.print("Error: Reading bathymetry source file " + filename+"\n");
            }

//            if (bathymetryFile != null) {
//                if (bathymetryFile.exists()) {
//                    SeadasLogger.getLogger().warning("Error reading bathymetry source file '" + bathymetryFile.getAbsolutePath());
//                } else {
//                    SeadasLogger.getLogger().warning("Bathymetry source file does not exist '" + bathymetryFile.getAbsolutePath());
//                }
//            } else {
//                SeadasLogger.getLogger().warning("Error reading bathymetry source file '" + filename);
//            }
        }


        validateParameter();
        validateSourceProduct();

        initTargetProduct();

    }


    private void validateParameter() {
        if (resolution != BathymetryData.RESOLUTION_BATHYMETRY_FILE) {
            throw new OperatorException(String.format("Resolution needs to be %d ",
                    BathymetryData.RESOLUTION_BATHYMETRY_FILE));
        }
    }

    private void validateSourceProduct() {
        final GeoCoding geoCoding = sourceProduct.getSceneGeoCoding();
        if (geoCoding == null) {
            throw new OperatorException("The source product must be geo-coded.");
        }
        if (!geoCoding.canGetGeoPos()) {
            throw new OperatorException("The geo-coding of the source product can not be used.\n" +
                    "It does not provide the geo-position for a pixel position.");
        }
    }

    private void initTargetProduct() {
        targetProduct = new Product("BathymetryProductTemporary", ProductData.TYPESTRING_UINT8, sourceProduct.getSceneRasterWidth(),
                sourceProduct.getSceneRasterHeight());

        final Band bathymetryBand = targetProduct.addBand(BATHYMETRY_BAND_NAME, ProductData.TYPE_FLOAT32);
        bathymetryBand.setNoDataValue(bathymetryReader.getMissingValue());
        bathymetryBand.setNoDataValueUsed(true);

        final Band topographyBand = targetProduct.addBand(TOPOGRAPHY_BAND_NAME, ProductData.TYPE_FLOAT32);
        topographyBand.setNoDataValue(bathymetryReader.getMissingValue());
        topographyBand.setNoDataValueUsed(true);

        final Band elevationBand = targetProduct.addBand(ELEVATION_BAND_NAME, ProductData.TYPE_FLOAT32);
        elevationBand.setNoDataValue(bathymetryReader.getMissingValue());
        elevationBand.setNoDataValueUsed(true);

        ProductUtils.copyGeoCoding(sourceProduct, targetProduct);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static class Spi extends OperatorSpi {
        public Spi() {
            super(BathymetryOp.class);
        }
    }


    @Override
    public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {

        final String targetBandName = targetBand.getName();

        final Rectangle rectangle = targetTile.getRectangle();

        // not sure if this is really needed but just in case
        if (!targetBandName.equals(BATHYMETRY_BAND_NAME)
                && !targetBandName.equals(TOPOGRAPHY_BAND_NAME)
                && !targetBandName.equals(ELEVATION_BAND_NAME)) {
            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
                    int dataValue = 0;
                    targetTile.setSample(x, y, dataValue);
                }
            }

            return;
        }


        try {
            final GeoCoding geoCoding = sourceProduct.getSceneGeoCoding();
            final PixelPos pixelPos = new PixelPos();
            final GeoPos geoPos = new GeoPos();


            // loop through tile, adding each pixel geolocation to it's appropriate earthBox via motherEarthBox
            // at this point the earthBoxes will adjust their mins and maxes based on the given lats and lons.

            MotherEarthBox motherEarthBox = new MotherEarthBox();

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
                    pixelPos.setLocation(x, y);
                    geoCoding.getGeoPos(pixelPos, geoPos);
                    motherEarthBox.add(geoPos);
                }
            }


            // for each of the earthBoxes, add in the bathymetry height array which is obtained at
            // this point from the source in a single chunk call.

            for (EarthBox earthBox : motherEarthBox.getFilledEarthBoxes()) {

                // add dimensions to the earthBox
                int minLatIndex = bathymetryReader.getLatIndex(earthBox.getMinLat());
                int maxLatIndex = bathymetryReader.getLatIndex(earthBox.getMaxLat());

                int minLonIndex = bathymetryReader.getLonIndex(earthBox.getMinLon());
                int maxLonIndex = bathymetryReader.getLonIndex(earthBox.getMaxLon());

                if (minLatIndex > 0) {
                    minLatIndex--;
                }

                if (maxLatIndex < bathymetryReader.dimensionLat - 1) {
                    maxLatIndex++;
                }

                if (minLonIndex > 0) {
                    minLonIndex--;
                }

                if (maxLonIndex < bathymetryReader.dimensionLon - 1) {
                    maxLonIndex++;
                }


                // determine length of each dimension for the chunk array to be pulled out of the netcdf source
                int latDimensionLength = maxLatIndex - minLatIndex + 1;
                int lonDimensionLength = maxLonIndex - minLonIndex + 1;

                // get the bathymetry height array from the netcdf source
                int[] origin = new int[]{minLatIndex, minLonIndex};
                int[] shape = new int[]{latDimensionLength, lonDimensionLength};

                // retrieve the bathymetry height array from netcdf
                Array heightArray = bathymetryReader.getHeightArray(origin, shape);
                Array waterSurfaceheightArray = bathymetryReader.getWaterSurfaceHeightArray(origin, shape);

                // convert heightArray from ucar.ma2.Array format to regular java array
                short heights[][] = (short[][]) heightArray.copyToNDJavaArray();
                short waterSurfaceHeights[][] = (short[][]) waterSurfaceheightArray.copyToNDJavaArray();

                // add the value array to the earthBox

                float minLat = bathymetryReader.getLat(minLatIndex);
                float maxLat = bathymetryReader.getLat(maxLatIndex);
                float minLon = bathymetryReader.getLon(minLonIndex);
                float maxLon = bathymetryReader.getLon(maxLonIndex);
                short missingValue = bathymetryReader.getMissingValue();

                earthBox.setValues(minLat, maxLat, minLon, maxLon, heights, waterSurfaceHeights, missingValue);
                earthBox.setGetValueAverage(true);
            }


            // loop through all the tile pixels, geolocate them, and get their bathymetry height from motherEarthBox.
            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
                    pixelPos.setLocation(x, y);
                    geoCoding.getGeoPos(pixelPos, geoPos);

                    int value;
                    int waterSurfaceHeight;
                    int height;
                    if (geoPos.isValid()) {

                        height = motherEarthBox.getValue(geoPos);
                        waterSurfaceHeight = motherEarthBox.getWaterSurfaceValue(geoPos);

                        if (targetBandName.equals(ELEVATION_BAND_NAME)) {
                            value = height;
                        } else if (targetBandName.equals(TOPOGRAPHY_BAND_NAME)) {
                            int valueSurface = height - waterSurfaceHeight;

                            if (valueSurface > 0) {
                                value = motherEarthBox.getValue(geoPos);
                            } else {
                                value = bathymetryReader.getMissingValue();
                            }
                        } else if (targetBandName.equals(BATHYMETRY_BAND_NAME)) {
                          int  valueSurface = height - waterSurfaceHeight;

                            if (valueSurface <= 0) {
                                value = valueSurface;
                            } else {
                                value = bathymetryReader.getMissingValue();
                            }

                            // convert  to positive if not NaN
                            if (value > -32000) {
                                value = -value;
                            }
                        } else {
                            value = bathymetryReader.getMissingValue();
                        }

                    } else {
                        value = bathymetryReader.getMissingValue();
                    }


                    targetTile.setSample(x, y, value);
                }
            }

        } catch (Exception e) {
            throw new OperatorException("Error computing tile '" + targetTile.getRectangle().toString() + "'.", e);
        }
    }


//    public void computeTileGood(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {
//
//        final String targetBandName = targetBand.getName();
//
//        final Rectangle rectangle = targetTile.getRectangle();
//
//        // not sure if this is really needed but just in case
//        if (!targetBandName.equals(BATHYMETRY_BAND_NAME)) {
//            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
//                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
//                    int dataValue = 0;
//                    targetTile.setSample(x, y, dataValue);
//                }
//            }
//
//            return;
//        }
//
//
//        try {
//            final GeoCoding geoCoding = sourceProduct.getGeoCoding();
//            final PixelPos pixelPos = new PixelPos();
//            final GeoPos geoPos = new GeoPos();
//
//            // establish 4 locations on the earth to minimize any effects of dateline and pole crossing
//            EarthBox earthBoxNW = new EarthBox();
//            EarthBox earthBoxNE = new EarthBox();
//            EarthBox earthBoxSW = new EarthBox();
//            EarthBox earthBoxSE = new EarthBox();
//
//            // loop through perimeter of tile, adding each pixel geolocation to it's appropriate earthBox
//            // at this point the earthBoxes will be adjusted their mins and maxes of the lats and lons.
//            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
//                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
//                    pixelPos.setLocation(x, y);
//
//                    geoCoding.getGeoPos(pixelPos, geoPos);
//
//                    if (geoPos.lat >= 0) {
//                        if (geoPos.lon >= 0) {
//                            earthBoxNE.add(geoPos);
//                        } else {
//                            earthBoxNW.add(geoPos);
//                        }
//                    } else {
//                        if (geoPos.lon >= 0) {
//                            earthBoxSE.add(geoPos);
//                        } else {
//                            earthBoxSW.add(geoPos);
//                        }
//                    }
//                }
//            }
//
//
//            // for all applicable earthBoxes add in the dimensions and bathymetry height array which is obtained at
//            // this point from the source in a single chunk call.
//            EarthBox earthBoxes[] = {earthBoxNE, earthBoxNW, earthBoxSE, earthBoxSW};
//
//            for (EarthBox earthBox : earthBoxes) {
//                if (earthBox.getMaxLat() != EarthBox.NULL_COORDINATE) {
//                    // add dimensions to the earthBox
//                    int minLatIndex = bathymetryReader.getLatIndex(earthBox.getMinLat());
//                    int maxLatIndex = bathymetryReader.getLatIndex(earthBox.getMaxLat());
//
//                    int minLonIndex = bathymetryReader.getLonIndex(earthBox.getMinLon());
//                    int maxLonIndex = bathymetryReader.getLonIndex(earthBox.getMaxLon());
//
//                    // determine length of each dimension for the chunk array to be pulled out of the netcdf source
//                    int latDimensionLength = maxLatIndex - minLatIndex + 1;
//                    int lonDimensionLength = maxLonIndex - minLonIndex + 1;
//
//                    // get the bathymetry height array from the netcdf source
//                    int[] origin = new int[]{minLatIndex, minLonIndex};
//                    int[] shape = new int[]{latDimensionLength, lonDimensionLength};
//
//                    // retrieve the bathymetry height array from netcdf
//                    Array heightArray = bathymetryReader.getHeightArray(origin, shape);
//
//                    // convert heightArray from ucar.ma2.Array format to regular java array
//                    short heights[][] = (short[][]) heightArray.copyToNDJavaArray();
//
//                    // add the value array to the earthBox
//                    //              earthBox.setValues(heights);
//                }
//            }
//
//
//            // loop through all the tile pixels, geolocate them, and get their bathymetry height from the
//            // appropriate earthBox.
//            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
//                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
//                    pixelPos.setLocation(x, y);
//                    geoCoding.getGeoPos(pixelPos, geoPos);
//
//                    final short bathymetryValue;
//                    if (geoPos.isValid()) {
//                        if (geoPos.lat >= 0) {
//                            if (geoPos.lon >= 0) {
//                                bathymetryValue = earthBoxNE.getValue(geoPos);
//                            } else {
//                                bathymetryValue = earthBoxNW.getValue(geoPos);
//                            }
//                        } else {
//                            if (geoPos.lon >= 0) {
//                                bathymetryValue = earthBoxSE.getValue(geoPos);
//                            } else {
//                                bathymetryValue = earthBoxSW.getValue(geoPos);
//                            }
//                        }
//                    } else {
//                        bathymetryValue = bathymetryReader.getMissingValue();
//                    }
//
//                    targetTile.setSample(x, y, bathymetryValue);
//                }
//            }
//
//        } catch (Exception e) {
//            throw new OperatorException("Error computing tile '" + targetTile.getRectangle().toString() + "'.", e);
//        }
//    }


}
