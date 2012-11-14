/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package gov.nasa.gsfc.seadas.dataio;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.util.FeatureUtils;
import org.esa.beam.util.io.CsvReader;
import org.esa.beam.util.io.FileUtils;
import org.esa.beam.visat.VisatApp;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


/**
 * Action that lets a user load a SeaBASS file
 *
 * @author Don Shea
 * @since SeaDAS 7.0
 */
public class ImportSeabassAction extends ExecCommand {

    public static final String TITLE = "Open SeaBASS File";

    @Override
    public void actionPerformed(final CommandEvent event) {
        VisatApp visatApp = VisatApp.getApp();

        File file = visatApp.showFileOpenDialog(TITLE, false, null, "importSeabass.lastDir");
        if (file == null) {
            return;
        }

        Product product = visatApp.getSelectedProduct();

        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection;
        try {
            featureCollection = readTrack(file, product.getGeoCoding());
        } catch (IOException e) {
            visatApp.showErrorDialog(TITLE, "Failed to load SeaBASS file:\n" + e.getMessage());
            return;
        }

        if (featureCollection.isEmpty()) {
            visatApp.showErrorDialog(TITLE, "No records found.");
            return;
        }

        String name = FileUtils.getFilenameWithoutExtension(file);
        final PlacemarkDescriptor placemarkDescriptor = PlacemarkDescriptorRegistry.getInstance().getPlacemarkDescriptor(featureCollection.getSchema());
        placemarkDescriptor.setUserDataOf(featureCollection.getSchema());
        VectorDataNode vectorDataNode = new VectorDataNode(name, featureCollection, placemarkDescriptor);

        product.getVectorDataGroup().add(vectorDataNode);

        final ProductSceneView view = visatApp.getSelectedProductSceneView();
        if (view != null) {
            view.setLayersVisible(vectorDataNode);
        }
    }

    @Override
    public void updateState(final CommandEvent event) {
        setEnabled(VisatApp.getApp().getSelectedProduct() != null
                && VisatApp.getApp().getSelectedProduct().getGeoCoding() != null);
    }

    private static FeatureCollection<SimpleFeatureType, SimpleFeature> readTrack(File file, GeoCoding geoCoding) throws IOException {
        Reader reader = new FileReader(file);
        try {
            return readTrack(reader, geoCoding);
        } finally {
            reader.close();
        }
    }

    static FeatureCollection<SimpleFeatureType, SimpleFeature> readTrack(Reader reader, GeoCoding geoCoding) throws IOException {
        CsvReader csvReader = new CsvReader(reader, new char[]{'\t', ' '}, true, "#");
        SimpleFeatureType trackFeatureType = createTrackFeatureType(geoCoding);
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = new ListFeatureCollection(trackFeatureType);
        double[] record;
        int pointIndex = 0;
        while ((record = csvReader.readDoubleRecord()) != null) {
            if (record.length < 4) {
                throw new IOException("Illegal track file format.\n" +
                        "Expecting tab-separated lines containing 4 values: lat, lon, data, data2.");
            }

            float lat = (float) record[0];
            float lon = (float) record[1];
            double data = record[2];
            double data2 = record[3];

            final SimpleFeature feature = createFeature(trackFeatureType, geoCoding, pointIndex, lat, lon, data, data2);
            if (feature != null) {
                featureCollection.add(feature);
            }

            pointIndex++;
        }

        if (featureCollection.isEmpty()) {
            throw new IOException("No track point found or all of them are located outside the scene boundaries.");
        }

        final CoordinateReferenceSystem mapCRS = geoCoding.getMapCRS();
        if (!mapCRS.equals(DefaultGeographicCRS.WGS84)) {
            try {
                transformFeatureCollection(featureCollection, mapCRS);
            } catch (TransformException e) {
                throw new IOException("Cannot transform the ship track onto CRS '" + mapCRS.toWKT() + "'.", e);
            }
        }

        return featureCollection;
    }

    private static void transformFeatureCollection(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection, CoordinateReferenceSystem targetCRS) throws TransformException {
        final GeometryCoordinateSequenceTransformer transform = FeatureUtils.getTransform(DefaultGeographicCRS.WGS84, targetCRS);
        final FeatureIterator<SimpleFeature> features = featureCollection.features();
        final GeometryFactory geometryFactory = new GeometryFactory();
        while (features.hasNext()) {
            final SimpleFeature simpleFeature = features.next();
            final Point sourcePoint = (Point) simpleFeature.getDefaultGeometry();
            final Point targetPoint = transform.transformPoint(sourcePoint, geometryFactory);
            simpleFeature.setDefaultGeometry(targetPoint);
        }
    }

    private static SimpleFeatureType createTrackFeatureType(GeoCoding geoCoding) {
        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName("org.esa.beam.TrackPoint1");
        /*0*/
        ftb.add("pixelPos", Point.class, geoCoding.getImageCRS());
        /*1*/
        ftb.add("geoPos", Point.class, DefaultGeographicCRS.WGS84);
        /*2*/
        ftb.add("data", Double.class);
        /*3*/
        ftb.add("data2", Double.class);
        ftb.setDefaultGeometry(geoCoding instanceof CrsGeoCoding ? "geoPos" : "pixelPos");
        // GeoTools Bug: this doesn't work
        // ftb.userData("trackPoints", "true");
        final SimpleFeatureType ft = ftb.buildFeatureType();
        ft.getUserData().put("trackPoints", "true");
        return ft;
    }

    private static SimpleFeature createFeature(SimpleFeatureType type, GeoCoding geoCoding, int pointIndex, float lat, float lon, double data, double data2) {
        PixelPos pixelPos = geoCoding.getPixelPos(new GeoPos(lat, lon), null);
        if (!pixelPos.isValid()) {
            return null;
        }
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(type);
        GeometryFactory gf = new GeometryFactory();
        /*0*/
        fb.add(gf.createPoint(new Coordinate(pixelPos.x, pixelPos.y)));
        /*1*/
        fb.add(gf.createPoint(new Coordinate(lon, lat)));
        /*2*/
        fb.add(data);
        /*3*/
        fb.add(data2);
        return fb.buildFeature(String.format("ID%08d", pointIndex));
    }


}
