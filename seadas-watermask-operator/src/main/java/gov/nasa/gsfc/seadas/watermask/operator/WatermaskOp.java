/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package gov.nasa.gsfc.seadas.watermask.operator;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.util.ProductUtils;

import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * The watermask operator is a GPF-Operator. It takes the geographic bounds of the input product and creates a new
 * product with the same bounds. The output product contains a single band, which is a land/water fraction mask.
 * For each pixel, it contains the fraction of water; a value of 0.0 indicates land, a value of 100.0 indicates water,
 * and every value in between indicates a mixed pixel.
 * <br/>
 * The water mask is based on data given by SRTM-shapefiles between 60° north and 60° south, and by the GlobCover world
 * map above 60° north.
 * Since the base data may exhibit a higher resolution than the input product, a subsampling &ge;1 may be specified;
 * therefore, mixed pixels may occur.
 *
 * @author Daniel Knowles, Marco Peters, Thomas Storm
 */
@SuppressWarnings({"FieldCanBeLocal"})
@OperatorMetadata(alias = "LandWaterMask",
        version = "1.0",
        internal = false,
        authors = "Daniel Knowles, Marco Peters",
        copyright = "(c) 2011 by Brockmann Consult",
        description = "Operator creating a target product with a single band containing a land/water-mask," +
                " which is based on SRTM-shapefiles (between 60° north and 60° south) and the " +
                "GlobCover world map (above 60° north) and therefore very accurate.")
public class WatermaskOp extends Operator {

    public static final String LAND_WATER_FRACTION_BAND_NAME = "land_water_fraction";
    public static final String LAND_WATER_FRACTION_SMOOTHED_BAND_NAME = "land_water_fraction_smoothed";
    public static final String COAST_BAND_NAME = "coast";
    @SourceProduct(alias = "source", description = "The Product the land/water-mask shall be computed for.",
            label = "Name")
    private Product sourceProduct;

    @Parameter(description = "Specifies on which resolution the water mask shall be based.", unit = "m/pixel",
            label = "Resolution", defaultValue = "1000", valueSet = {"50", "150", "1000", "10000"})
    private int resolution;

    @Parameter(description = "Water mask filename",
            label = "Filename", defaultValue = "50m.zip",
            valueSet = {"50m.zip", "150m.zip", "GSHHS_water_mask_250m.zip", "GSHHS_water_mask_250m.zip", "GSHHS_water_mask_1km.zip", "GSHHS_water_mask_10km.zip"})
    private String filename;


    @Parameter(description = "Specifies the factor between the resolution of the source product and the watermask in " +
            "x direction. A value of '1' means no subsampling at all.",
            label = "Subsampling factor x", defaultValue = "3", notNull = true)
    private int subSamplingFactorX;

    @Parameter(description = "Specifies the factor between the resolution of the source product and the watermask in" +
            "y direction. A value of '1' means no subsampling at all.",
            label = "Subsampling factor y", defaultValue = "3", notNull = true)
    private int subSamplingFactorY;


//    @Parameter(description = "Specifies the watermaskClassifier mode",
//            label = "Mode", defaultValue = "2", notNull = true)
//    private int mode;

    @Parameter(description = "Specifies the watermaskClassifier mode",
            label = "Mode", defaultValue = "GSHHS", notNull = true)
    private WatermaskClassifier.Mode mode;


//    @Parameter(description = "Specifies the resolutionInfo which contains resolution, mode",
//            label = "Resolution Info", defaultValue = "1 km GSHHS", notNull = true)
//    private SourceFileInfo resolutionInfo;


    @TargetProduct
    private Product targetProduct;
    private WatermaskClassifier classifier;

    @Override
    public void initialize() throws OperatorException {
        validateParameter();
        validateSourceProduct();
        initTargetProduct();

        try {
            classifier = new WatermaskClassifier(resolution, mode, filename);
        } catch (IOException e) {
            throw new OperatorException("Error creating class WatermaskClassifier.", e);
        }
    }

    @Override
    public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {
        final Rectangle rectangle = targetTile.getRectangle();
        try {
            final String targetBandName = targetBand.getName();
            final PixelPos pixelPos = new PixelPos();
            final GeoCoding geoCoding = sourceProduct.getGeoCoding();
            for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
                for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                    pixelPos.x = x;
                    pixelPos.y = y;
                    int dataValue = 0;
                    if (targetBandName.equals(LAND_WATER_FRACTION_BAND_NAME)) {
                        dataValue = classifier.getWaterMaskFraction(geoCoding, pixelPos,
                                subSamplingFactorX,
                                subSamplingFactorY);
                    } else if (targetBandName.equals(COAST_BAND_NAME)) {
                        final boolean coastline = isCoastline(geoCoding, pixelPos,
                                subSamplingFactorX,
                                subSamplingFactorY);
                        dataValue = coastline ? 1 : 0;
                    }
                    targetTile.setSample(x, y, dataValue);
                }
            }

        } catch (Exception e) {
            throw new OperatorException("Error computing tile '" + targetTile.getRectangle().toString() + "'.", e);
        }
    }

    private boolean isCoastline(GeoCoding geoCoding, PixelPos pixelPos, int superSamplingX, int superSamplingY) {
        double xStep = 1.0 / superSamplingX;
        double yStep = 1.0 / superSamplingY;
        final GeoPos geoPos = new GeoPos();
        final PixelPos currentPos = new PixelPos();
        for (int sx = 0; sx < superSamplingX; sx++) {
            currentPos.x = (float) (pixelPos.x + sx * xStep);
            for (int sy = 0; sy < superSamplingY; sy++) {
                currentPos.y = (float) (pixelPos.y + sy * yStep);
                geoCoding.getGeoPos(currentPos, geoPos);
                // Todo: Implement coastline algorithm here
                //
            }
        }
        return false;
    }

    private void validateParameter() {
        if (resolution != WatermaskClassifier.RESOLUTION_50m &&
                resolution != WatermaskClassifier.RESOLUTION_150m &&
                resolution != WatermaskClassifier.RESOLUTION_1km &&
                resolution != WatermaskClassifier.RESOLUTION_10km) {
            throw new OperatorException(String.format("Resolution needs to be either %d, %d, %d or %d.",
                    WatermaskClassifier.RESOLUTION_50m,
                    WatermaskClassifier.RESOLUTION_150m,
                    WatermaskClassifier.RESOLUTION_1km,
                    WatermaskClassifier.RESOLUTION_10km));
        }
        if (subSamplingFactorX < 1) {
            String message = MessageFormat.format(
                    "Subsampling factor needs to be greater than or equal to 1; was: ''{0}''.", subSamplingFactorX);
            throw new OperatorException(message);
        }
    }

    private void validateSourceProduct() {
        final GeoCoding geoCoding = sourceProduct.getGeoCoding();
        if (geoCoding == null) {
            throw new OperatorException("The source product must be geo-coded.");
        }
        if (!geoCoding.canGetGeoPos()) {
            throw new OperatorException("The geo-coding of the source product can not be used.\n" +
                    "It does not provide the geo-position for a pixel position.");
        }
    }

    private void initTargetProduct() {
        targetProduct = new Product("LW-Mask", ProductData.TYPESTRING_UINT8, sourceProduct.getSceneRasterWidth(),
                sourceProduct.getSceneRasterHeight());

        final Band waterBand = targetProduct.addBand(LAND_WATER_FRACTION_BAND_NAME, ProductData.TYPE_FLOAT32);
        waterBand.setNoDataValue(WatermaskClassifier.INVALID_VALUE);
        waterBand.setNoDataValueUsed(true);

        final Kernel arithmeticMean3x3Kernel = new Kernel(3, 3, 1.0 / 9.0,
                new double[]{
                        +1, +1, +1,
                        +1, +1, +1,
                        +1, +1, +1,
                });

        int count = 1;
        final ConvolutionFilterBand filteredCoastlineBand = new ConvolutionFilterBand(
                "mask_data_water_fraction_smoothed",
                waterBand,
                arithmeticMean3x3Kernel, count);

        targetProduct.addBand(filteredCoastlineBand);

        final ProductNodeGroup<Mask> maskGroup = targetProduct.getMaskGroup();
        Mask landMask = Mask.BandMathsType.create(
                "LandMask",
                "Land masked pixels",
                targetProduct.getSceneRasterWidth(),
                targetProduct.getSceneRasterHeight(),
                LAND_WATER_FRACTION_BAND_NAME + "== 0",
                new Color(51, 51, 51),
                0.0);

        maskGroup.add(landMask);

        Mask waterMask = Mask.BandMathsType.create(
                "WaterMask",
                "Water masked pixels",
                targetProduct.getSceneRasterWidth(),
                targetProduct.getSceneRasterHeight(),
                LAND_WATER_FRACTION_BAND_NAME + "> 0",
                new Color(0, 125, 255),
                0.5);
        maskGroup.add(waterMask);

        Mask coastlineMask = Mask.BandMathsType.create(
                "CoastMask",
                "Coast masked pixels",
                targetProduct.getSceneRasterWidth(),
                targetProduct.getSceneRasterHeight(),
                LAND_WATER_FRACTION_SMOOTHED_BAND_NAME + " > 25 and " + LAND_WATER_FRACTION_SMOOTHED_BAND_NAME + " < 75",
                new Color(0, 0, 0),
                0.0);
        maskGroup.add(coastlineMask);




//        final Band coastBand = targetProduct.addBand(COAST_BAND_NAME, ProductData.TYPE_FLOAT32);
//        coastBand.setNoDataValue(0);
//        coastBand.setNoDataValueUsed(true);

        ProductUtils.copyGeoCoding(sourceProduct, targetProduct);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static class Spi extends OperatorSpi {

        public Spi() {
            super(WatermaskOp.class);
        }
    }
}
