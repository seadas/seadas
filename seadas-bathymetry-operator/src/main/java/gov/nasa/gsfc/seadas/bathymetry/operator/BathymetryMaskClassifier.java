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

package gov.nasa.gsfc.seadas.bathymetry.operator;

import gov.nasa.gsfc.seadas.bathymetry.util.ImageDescriptorBuilder;
import gov.nasa.gsfc.seadas.bathymetry.ui.BathymetryData;
import org.esa.beam.framework.datamodel.GeoCoding;
import org.esa.beam.framework.datamodel.GeoPos;
import org.esa.beam.framework.datamodel.PixelPos;
import gov.nasa.gsfc.seadas.bathymetry.util.ImageDescriptor;

import javax.media.jai.OpImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * Classifies a pixel given by its geo-coordinate as water pixel.
 */
@SuppressWarnings({"ResultOfMethodCallIgnored"})
public class BathymetryMaskClassifier {

    public static final int WATER_VALUE = 1;
    public static final int INVALID_VALUE = 127;
    public static final int LAND_VALUE = 0;


    public static final int RESOLUTION_1km = 1000;
    public static final int RESOLUTION_10km = 10000;

    public static final String FILENAME_BATHYMETRY = "ETOPO1_ocssw.nc";
    public static final String FILENAME_GSHHS_10km = "GSHHS_water_mask_10km.zip";

    static final int GSHHS_1_TILE_WIDTH = 500;
    static final int GSHHS_1_TILE_HEIGHT = 250;
    static final int GSHHS_1_IMAGE_WIDTH = 36000;
    static final int GSHHS_1_IMAGE_HEIGHT = 18000;

    static final int GSHHS_10_TILE_WIDTH = 250;
    static final int GSHHS_10_TILE_HEIGHT = 125;
    static final int GSHHS_10_IMAGE_WIDTH = 3600;
    static final int GSHHS_10_IMAGE_HEIGHT = 1800;


    private PNGSourceImage gshhsImage;

    private int resolution;
    private String filename;
    private BathymetryReader bathymetryReader;

    /**
     * Creates a new classifier instance on the given resolution.
     * The classifier uses a tiled image in background to determine the if a
     * given geo-position is over land or over water.
     * Tiles do not exist if the whole region of the tile would only cover land or water.
     * Where a tile does not exist a so called fill algorithm can be performed.
     * In this case the next existing tile is searched and the nearest classification value
     * for the given geo-position is returned.
     * If the fill algorithm is not performed a value indicating invalid is returned.
     *
     * @param resolution The resolution specifying the source data which is to be queried. The units are in meters.
     *                   Needs to be <code>RESOLUTION_50m</code>, <code>RESOLUTION_150m</code>,
     *                   <code>RESOLUTION_1km</code> or <code>RESOLUTION_10km</code>
     * @throws java.io.IOException If some IO-error occurs creating the sources.
     */
    public BathymetryMaskClassifier(int resolution, String filename) throws IOException {
        if (resolution != RESOLUTION_1km) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Resolution needs to be {0}.", RESOLUTION_1km));
        }

        this.resolution = resolution;
        this.filename = filename;

        try {


            final File auxdataDir = BathymetryData.getOcsswRoot();
            File runDir = new File(auxdataDir, "run");
            File dataDir = new File(runDir, "data");
            File commonDir = new File(dataDir, "common");
            File bathymetryFile = new File(commonDir, filename);

            bathymetryReader = new BathymetryReader(bathymetryFile);


            ImageDescriptor bathymetryDescriptor = getBathymetryDescriptor(auxdataDir);
            if (bathymetryDescriptor != null) {
                gshhsImage = createImage(auxdataDir, bathymetryDescriptor);
            }

            if (bathymetryReader != null) try {
                bathymetryReader.close();
            } catch (IOException ioe) {
                //
            }

        } catch (IOException ioe) {
            //
        } finally {

        }
    }

    private ImageDescriptor getBathymetryDescriptor(File auxdataDir) {

        ImageDescriptor imageDescriptor = null;

        String zipname = filename;
        if (resolution == RESOLUTION_1km) {
            imageDescriptor = new ImageDescriptorBuilder()
                    .width(GSHHS_1_IMAGE_WIDTH)
                    .height(GSHHS_1_IMAGE_HEIGHT)
                    .tileWidth(GSHHS_1_TILE_WIDTH)
                    .tileHeight(GSHHS_1_TILE_HEIGHT)
                    .auxdataDir(auxdataDir)
                    .zipFileName(zipname)
                    .build();
        }

        return imageDescriptor;
    }


    private PNGSourceImage createImage(File auxdataDir2, ImageDescriptor descriptor) throws IOException {
        int width = descriptor.getImageWidth();
        int tileWidth = descriptor.getTileWidth();
        int height = descriptor.getImageHeight();
        int tileHeight = descriptor.getTileHeight();
        final Properties properties = new Properties();
        properties.setProperty("width", String.valueOf(width));
        properties.setProperty("height", String.valueOf(height));
        properties.setProperty("tileWidth", String.valueOf(tileWidth));
        properties.setProperty("tileHeight", String.valueOf(tileHeight));
        final URL imageProperties = getClass().getResource("image.properties");
        properties.load(imageProperties.openStream());

        final File auxdataDir = descriptor.getAuxdataDir();
        final String zipFileName = descriptor.getZipFileName();
        File zipFile = new File(auxdataDir, zipFileName);
        return PNGSourceImage.create(properties, zipFile, resolution);
    }


    private int getSample(double lat, double lon, double latDiff, double lonDiff, double offset, OpImage image) {
        final double pixelSizeX = lonDiff / image.getWidth();
        final double pixelSizeY = latDiff / image.getHeight();
        final int x = (int) Math.round(lon / pixelSizeX);
        final int y = (int) (Math.round((lat - offset) / pixelSizeY));
        final Raster tile = image.getTile(image.XToTileX(x), image.YToTileY(y));
        if (tile == null) {
            return bathymetryReader.getMissingValue();
        }
        return tile.getSample(x, y, 0);
    }

    /**
     * Returns the fraction of water for the given region, considering a subsampling factor.
     *
     * @param geoCoding          The geo coding of the product the watermask fraction shall be computed for.
     * @param pixelPos           The pixel position the watermask fraction shall be computed for.
     * @param subsamplingFactorX The factor between the high resolution water mask and the - lower resolution -
     *                           source image in x direction. Only values in [1..M] are sensible,
     *                           with M = (source image resolution in m/pixel) / (50 m/pixel)
     * @param subsamplingFactorY The factor between the high resolution water mask and the - lower resolution -
     *                           source image in y direction. Only values in [1..M] are sensible,
     *                           with M = (source image resolution in m/pixel) / (50 m/pixel)
     * @return The fraction of water in the given geographic rectangle, in the range [0..100].
     */
    public byte getBathymetryAverage(GeoCoding geoCoding, PixelPos pixelPos, int subsamplingFactorX, int subsamplingFactorY) {
        float valueSum = 0;
        double xStep = 1.0 / subsamplingFactorX;
        double yStep = 1.0 / subsamplingFactorY;
        final GeoPos geoPos = new GeoPos();
        final PixelPos currentPos = new PixelPos();
        int invalidCount = 0;
        for (int sx = 0; sx < subsamplingFactorX; sx++) {
            currentPos.x = (float) (pixelPos.x + sx * xStep);
            for (int sy = 0; sy < subsamplingFactorY; sy++) {
                currentPos.y = (float) (pixelPos.y + sy * yStep);
                geoCoding.getGeoPos(currentPos, geoPos);
                int bathymetryPoint = getBathymetryPoint(geoPos);
                if (bathymetryPoint != bathymetryReader.getMissingValue()) {
                    valueSum += bathymetryPoint;
                } else {
                    invalidCount++;
                }
            }
        }


        return computeAverage(subsamplingFactorX, subsamplingFactorY, valueSum, invalidCount);
    }

    private byte computeAverage(int subsamplingFactorX, int subsamplingFactorY, float valueSum, int invalidCount) {
        final boolean allValuesInvalid = invalidCount == subsamplingFactorX * subsamplingFactorY;
        if (allValuesInvalid) {
            return (byte) bathymetryReader.getMissingValue();
        } else {
            return (byte) (100 * valueSum / (subsamplingFactorX * subsamplingFactorY));
        }
    }

    private int getBathymetryPoint(GeoPos geoPos) {
        final int bathymetryPoint;
        if (geoPos.isValid()) {
            int latIndex = bathymetryReader.getLatIndex(geoPos.lat);
            int lonIndex = bathymetryReader.getLonIndex(geoPos.lon);

            bathymetryPoint = bathymetryReader.getHeight(latIndex, lonIndex);
        } else {
            bathymetryPoint = bathymetryReader.getMissingValue();
        }
        return bathymetryPoint;
    }


    public short getMissingValue() {
        return bathymetryReader.getMissingValue();
    }


}
