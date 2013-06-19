package gov.nasa.gsfc.seadas.bathymetry.util;

import gov.nasa.gsfc.seadas.bathymetry.operator.BathymetryUtils;
import junit.framework.TestCase;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipFile;

public class ShapeFileRasterizerTest extends TestCase {

    @Test
    public void testImageCreation() throws IOException {
        final File targetDir = new File("");
        final ShapeFileRasterizer rasterizer = new ShapeFileRasterizer(targetDir);
        final URL shapeUrl = getClass().getResource("e000n05f.shp");
        final File shapeFile = new File(shapeUrl.getFile());
        final int resolution = BathymetryUtils.computeSideLength(150);
        final BufferedImage image = rasterizer.createImage(shapeFile, resolution);

        // test some known values
        final Raster imageData = image.getData();
        assertEquals(1, imageData.getSample(526, 92, 0));
        assertEquals(0, imageData.getSample(312, 115, 0));
        assertEquals(1, imageData.getSample(141, 187, 0));
        assertEquals(0, imageData.getSample(197, 27, 0));
        assertEquals(1, imageData.getSample(308, 701, 0));
    }

     @Test
    public void testFromZip() throws IOException {
        final File targetDir = new File("");
        final ShapeFileRasterizer rasterizer = new ShapeFileRasterizer(targetDir);
        final URL shapeUrl = getClass().getResource("e000n05f.zip");
         final int tileSize = BathymetryUtils.computeSideLength(150);
         final List<File> tempFiles;
         final ZipFile zipFile = new ZipFile(shapeUrl.getFile());
         try {
             tempFiles = rasterizer.createTempFiles(zipFile);
         } finally {
             zipFile.close();
         }

         BufferedImage image = null;
         for (File file : tempFiles) {
             if (file.getName().endsWith("shp")) {
                 image = rasterizer.createImage(file, tileSize);
             }
         }
         assertNotNull(image);

//        // test some known values
        final Raster imageData = image.getData();
        assertEquals(1, imageData.getSample(526, 92, 0));
        assertEquals(0, imageData.getSample(312, 115, 0));
        assertEquals(1, imageData.getSample(141, 187, 0));
        assertEquals(0, imageData.getSample(197, 27, 0));
        assertEquals(1, imageData.getSample(308, 701, 0));

    }

}
