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

package gov.nasa.gsfc.seadas.bathymetry.util;

import gov.nasa.gsfc.seadas.bathymetry.operator.BathymetryUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Responsible for transferring shapefiles containing a land/water-mask into a rasterized image.
 *
 * @author Thomas Storm
 */
class ShapeFileRasterizer {

    private final File targetDir;

    private final String tempDir;

    ShapeFileRasterizer(File targetDir) {
        this.targetDir = targetDir;
        tempDir = System.getProperty("java.io.tmpdir", ".");
    }

    /**
     * The main method of this tool.
     *
     * @param args Three arguments are needed: 1) directory containing shapefiles. 2) target directory.
     *             3) resolution in meters / pixel.
     *
     * @throws java.io.IOException If some IO error occurs.
     */
    public static void main(String[] args) throws IOException {
        final File resourceDir = new File(args[0]);
        final File targetDir = new File(args[1]);
        int sideLength = BathymetryUtils.computeSideLength(Integer.parseInt(args[2]));
        boolean createImage = false;
        if(args.length == 4){
            createImage = Boolean.parseBoolean(args[3]);
        }
        final ShapeFileRasterizer rasterizer = new ShapeFileRasterizer(targetDir);
        rasterizer.rasterizeShapeFiles(resourceDir, sideLength, createImage);
    }

    void rasterizeShapeFiles(File directory, int tileSize, boolean createImage) throws IOException {
        File[] shapeFiles = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".zip");
            }
        });
        if (shapeFiles != null) {
            rasterizeShapeFiles(shapeFiles, tileSize, createImage);
        }
        File[] subdirs = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if (subdirs != null) {
            for (File subDir : subdirs) {
                rasterizeShapeFiles(subDir, tileSize, createImage);
            }
        }
    }

    void rasterizeShapeFiles(File[] zippedShapeFiles, int tileSize, boolean createImage) {
        final ExecutorService executorService = Executors.newFixedThreadPool(12);
        for (int i = 0; i < zippedShapeFiles.length; i++) {
            File shapeFile = zippedShapeFiles[i];
            executorService.submit(new ShapeFileRunnable(shapeFile, tileSize, i+1, zippedShapeFiles.length, createImage));
        }
        executorService.shutdown();
        while(!executorService.isTerminated()) {
            try {
                executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    BufferedImage createImage(File shapeFile, int tileSize) throws IOException {
        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        final String shapeFileName = shapeFile.getName();
        final ReferencedEnvelope referencedEnvelope = parseEnvelopeFromShapeFileName(shapeFileName, crs);
        MapContext context = new DefaultMapContext(crs);

        final URL shapeFileUrl = shapeFile.toURI().toURL();

        final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = getFeatureSource(shapeFileUrl);
        context.addLayer(featureSource, createPolygonStyle());

        BufferedImage landMaskImage = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D graphics = landMaskImage.createGraphics();


        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(context);
        renderer.paint(graphics, new Rectangle(0, 0, tileSize, tileSize),referencedEnvelope);

        return landMaskImage;
    }

    private ReferencedEnvelope parseEnvelopeFromShapeFileName(String shapeFileName, CoordinateReferenceSystem crs) {
        int lonMin = Integer.parseInt(shapeFileName.substring(1, 4));
        int lonMax;
        if (shapeFileName.startsWith("e")) {
            lonMax = lonMin + 1;
        } else if (shapeFileName.startsWith("w")) {
            lonMin--;
            lonMin = lonMin * -1;
            lonMax = lonMin--;
        } else {
            throw new IllegalStateException("Wrong shapefile-name: '" + shapeFileName + "'.");
        }

        int latMin = Integer.parseInt(shapeFileName.substring(5, 7));
        int latMax;
        if (shapeFileName.charAt(4) == 'n') {
            latMax = latMin + 1;
        } else if (shapeFileName.charAt(4) == 's') {
            latMin--;
            latMin = latMin * -1;
            latMax = latMin--;
        } else {
            throw new IllegalStateException("Wrong shapefile-name: '" + shapeFileName + "'.");
        }
        return new ReferencedEnvelope(lonMin, lonMax, latMin, latMax, crs);
    }

    private void writeToFile(BufferedImage image, String name, boolean createImage) throws IOException {
        String fileName = getFilenameWithoutExtension(name);
        fileName = fileName.substring(0, fileName.length() - 1);
        String imgFileName = fileName + ".img";
        File outputFile = new File(targetDir.getAbsolutePath(), imgFileName);

        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        try {
            byte[] data = ((DataBufferByte) image.getData().getDataBuffer()).getData();
            fileOutputStream.write(data);
            if (createImage) {
                ImageIO.write(image, "png", new File(targetDir.getAbsolutePath(), fileName + ".png"));
            }
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    List<File> createTempFiles(ZipFile zipFile) {
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        List<File> tempShapeFiles;
        try {
            tempShapeFiles = unzipTempFiles(zipFile, entries);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error generating temp files from shapefile '" + zipFile.getName() + "'.", e);
        }
        return tempShapeFiles;
    }

    private List<File> unzipTempFiles(ZipFile zipFile, Enumeration<? extends ZipEntry> entries) throws
                                                                                                   IOException {
        List<File> files = new ArrayList<File>();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            File file = readIntoTempFile(zipFile, entry);
            files.add(file);
        }
        return files;
    }

    private File readIntoTempFile(ZipFile zipFile, ZipEntry entry) throws IOException {
        File file = new File(tempDir, entry.getName());
        final FileOutputStream writer = new FileOutputStream(file);
        final InputStream reader = zipFile.getInputStream(entry);
        try {
            byte[] buffer = new byte[1024*1024];
            int bytesRead = reader.read(buffer);
            while (bytesRead != -1) {
                writer.write(buffer, 0, bytesRead);
                bytesRead = reader.read(buffer);
            }
        } finally {
            reader.close();
            writer.close();
        }
        return file;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private void deleteTempFiles(List<File> tempFiles) {
        for (File tempFile : tempFiles) {
            tempFile.delete();
        }
        tempFiles.clear();
    }

    private static String getFilenameWithoutExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1) {
            return fileName.substring(0, i);
        }
        return fileName;
    }

    private FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(URL url) throws IOException {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put(ShapefileDataStoreFactory.URLP.key, url);
        parameterMap.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.TRUE);
        DataStore shapefileStore = DataStoreFinder.getDataStore(parameterMap);
        String typeName = shapefileStore.getTypeNames()[0]; // Shape files do only have one type name
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
        featureSource = shapefileStore.getFeatureSource(typeName);
        return featureSource;
    }

    private Style createPolygonStyle() {
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
        FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);

        PolygonSymbolizer symbolizer = styleFactory.createPolygonSymbolizer();
        org.geotools.styling.Stroke stroke = styleFactory.createStroke(
                filterFactory.literal("#FFFFFF"),
                filterFactory.literal(0.0)
        );
        symbolizer.setStroke(stroke);
        Fill fill = styleFactory.createFill(
                filterFactory.literal("#FFFFFF"),
                filterFactory.literal(1.0)
        );
        symbolizer.setFill(fill);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(symbolizer);

        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle();
        fts.rules().add(rule);

        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

    private class ShapeFileRunnable implements Runnable {

        private final File shapeFile;
        private int tileSize;
        private int index;
        private int shapeFileCount;
        private boolean createImage;

        ShapeFileRunnable(File shapeFile, int tileSize, int shapeFileIndex, int shapeFileCount, boolean createImage) {
            this.shapeFile = shapeFile;
            this.tileSize = tileSize;
            this.index = shapeFileIndex;
            this.shapeFileCount = shapeFileCount;
            this.createImage = createImage;
        }

        @Override
        public void run() {
            try {
                ZipFile zipFile = new ZipFile(shapeFile);
                List<File> tempShapeFiles = createTempFiles(zipFile);
                zipFile.close();
                for (File file : tempShapeFiles) {
                    if (file.getName().endsWith("shp")) {
                        final BufferedImage image = createImage(file, tileSize);
                        writeToFile(image, shapeFile.getName(), createImage);
                    }
                }
                deleteTempFiles(tempShapeFiles);
                System.out.printf("File %d of %d%n",index, shapeFileCount);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}