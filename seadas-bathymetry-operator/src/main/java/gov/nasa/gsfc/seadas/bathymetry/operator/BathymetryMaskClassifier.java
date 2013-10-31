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
import gov.nasa.gsfc.seadas.bathymetry.util.ImageDescriptor;

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

    public static final int RESOLUTION_BATHYMETRY_FILE = 1855;

    public static final String FILENAME_BATHYMETRY = "ETOPO1_ocssw.nc";

    static final int BATHYMETRY_TILE_WIDTH = 500;
    static final int BATHYMETRY_TILE_HEIGHT = 250;
    static final int BATHYMETRY_IMAGE_WIDTH = 21601;
    static final int BATHYMETRY_IMAGE_HEIGHT = 10801;

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
     *                   <code>RESOLUTION_BATHYMETRY_FILE</code> or <code>RESOLUTION_10km</code>
     * @throws java.io.IOException If some IO-error occurs creating the sources.
     */
    public BathymetryMaskClassifier(int resolution, String filename) throws IOException {
        if (resolution != RESOLUTION_BATHYMETRY_FILE) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Resolution needs to be {0}.", RESOLUTION_BATHYMETRY_FILE));
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
        if (resolution == RESOLUTION_BATHYMETRY_FILE) {
            imageDescriptor = new ImageDescriptorBuilder()
                    .width(BATHYMETRY_IMAGE_WIDTH)
                    .height(BATHYMETRY_IMAGE_HEIGHT)
                    .tileWidth(BATHYMETRY_TILE_WIDTH)
                    .tileHeight(BATHYMETRY_TILE_HEIGHT)
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




}
