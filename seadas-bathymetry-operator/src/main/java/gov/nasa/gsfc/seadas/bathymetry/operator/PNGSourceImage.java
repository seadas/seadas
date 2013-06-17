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

import org.esa.beam.jai.ImageHeader;
import org.esa.beam.util.ImageUtils;
import org.esa.beam.util.jai.SingleBandedSampleModel;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.SourcelessOpImage;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * OpImage to read from GlobCover-based water mask images.
 *
 * @author Thomas Storm
 */
public class PNGSourceImage extends SourcelessOpImage {

    private final ZipFile zipFile;
    private int resolution;

    static PNGSourceImage create(Properties properties, File zipFile, int resolution) throws IOException {
        final ImageHeader imageHeader = ImageHeader.load(properties, null);
        return new PNGSourceImage(imageHeader, zipFile, resolution);
    }

    private PNGSourceImage(ImageHeader imageHeader, File zipFile, int resolution) throws IOException {
        super(imageHeader.getImageLayout(),
                null,
                ImageUtils.createSingleBandedSampleModel(DataBuffer.TYPE_BYTE,
                        imageHeader.getImageLayout().getSampleModel(null).getWidth(),
                        imageHeader.getImageLayout().getSampleModel(null).getHeight()),
                imageHeader.getImageLayout().getMinX(null),
                imageHeader.getImageLayout().getMinY(null),
                imageHeader.getImageLayout().getWidth(null),
                imageHeader.getImageLayout().getHeight(null));
        this.zipFile = new ZipFile(zipFile);
        this.resolution = resolution;
        // this image uses its own tile cache in order not to disturb the GPF tile cache.
        setTileCache(JAI.createTileCache(50L * 1024 * 1024));
    }

    @Override
    public Raster computeTile(int tileX, int tileY) {
        Raster raster;
        try {
            raster = computeRawRaster(tileX, tileY);
        } catch (IOException e) {
            throw new RuntimeException(MessageFormat.format("Failed to read image tile ''{0} | {1}''.", tileX, tileY), e);
        }
        return raster;

    }

    private Raster computeRawRaster(int tileX, int tileY) throws IOException {
        final String fileName = getFileName(tileX, tileY);
        final WritableRaster targetRaster = createWritableRaster(tileX, tileY);
        final ZipEntry zipEntry = zipFile.getEntry(fileName);

        InputStream inputStream = null;
        try {
            inputStream = zipFile.getInputStream(zipEntry);
            BufferedImage image = ImageIO.read(inputStream);
            Raster imageData = image.getData();
            for (int y = 0; y < imageData.getHeight(); y++) {
                int yPos = tileYToY(tileY) + y;
                for (int x = 0; x < imageData.getWidth(); x++) {
                    byte sample = (byte) imageData.getSample(x, y, 0);
                    sample = (byte) Math.abs(sample - 1);
                    int xPos = tileXToX(tileX) + x;
                    targetRaster.setSample(xPos, yPos, 0, sample);
                }
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return targetRaster;
    }

    private WritableRaster createWritableRaster(int tileX, int tileY) {
        final Point location = new Point(tileXToX(tileX), tileYToY(tileY));
        final SampleModel sampleModel = new SingleBandedSampleModel(DataBuffer.TYPE_BYTE, getTileWidth(), getTileHeight());
        return createWritableRaster(sampleModel, location);
    }

    private String getFileName(int tileX, int tileY) {

        String res = String.valueOf(resolution / 1000);
        return String.format("gshhs_%s_%02d_%02d.png", res, tileY, tileX);

    }
}
