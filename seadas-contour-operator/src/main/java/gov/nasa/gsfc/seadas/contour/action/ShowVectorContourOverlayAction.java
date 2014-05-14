package gov.nasa.gsfc.seadas.contour.action;

import com.bc.ceres.glayer.LayerFilter;
import com.bc.ceres.swing.figure.FigureStyle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import gov.nasa.gsfc.seadas.ContourDescriptor;
import gov.nasa.gsfc.seadas.contour.layer.ContourVectorDataLayerFilterFactory;
import gov.nasa.gsfc.seadas.contour.ui.ContourData;
import gov.nasa.gsfc.seadas.contour.ui.ContourDialog;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.util.FeatureUtils;
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

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 4/17/14
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowVectorContourOverlayAction extends ExecCommand { //AbstractShowOverlayAction {

    private final LayerFilter contourFilter = ContourVectorDataLayerFilterFactory.createContourFilter();
    public static final String CONTOUR_LAYER_ID = "gov.nasa.gsfc.seadas.contour";

    @Override
    public void actionPerformed(CommandEvent event) {
        VisatApp visatApp = VisatApp.getApp();
        final ProductSceneView sceneView = VisatApp.getApp().getSelectedProductSceneView();
        Product product = visatApp.getSelectedProduct();
        ContourData contourData = new ContourData();

        ContourDialog contourDialog = new ContourDialog(contourData, product);
        contourDialog.setVisible(true);
        contourDialog.getContentPane().getComponents();
        contourDialog.dispose();

        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection;
        try {
            featureCollection = createContourFeatureCollection(product, contourData);
        } catch (Exception e) {
            visatApp.showErrorDialog("failed to create contour lines");
            return;
        }

        if (featureCollection.isEmpty()) {
            visatApp.showErrorDialog("Contour Lines", "No records found.");
            return;
        }

        String name = "Contour Lines";
        final PlacemarkDescriptor placemarkDescriptor = PlacemarkDescriptorRegistry.getInstance().getPlacemarkDescriptor(featureCollection.getSchema());
        placemarkDescriptor.setUserDataOf(featureCollection.getSchema());
        VectorDataNode vectorDataNode = new VectorDataNode(name, featureCollection, placemarkDescriptor);

        product.getVectorDataGroup().add(vectorDataNode);

        //final ProductSceneView view = visatApp.getSelectedProductSceneView();
        if (sceneView != null) {
            FigureStyle lineStyle = sceneView.getFigureEditor().getDefaultLineStyle();
            sceneView.setLayersVisible(vectorDataNode);
        }
    }

    @Override
    public void updateState(final CommandEvent event) {
        setEnabled(VisatApp.getApp().getSelectedProduct() != null
                && VisatApp.getApp().getSelectedProduct().getGeoCoding() != null);
    }

    private static void transformFeatureCollection(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) throws TransformException {
        final GeometryCoordinateSequenceTransformer transform = FeatureUtils.getTransform(sourceCRS, targetCRS);
        final FeatureIterator<SimpleFeature> features = featureCollection.features();
        final GeometryFactory geometryFactory = new GeometryFactory();
        while (features.hasNext()) {
            final SimpleFeature simpleFeature = features.next();
            final LineString sourceLine = (LineString) simpleFeature.getDefaultGeometry();
            final LineString targetLine = transform.transformLineString(sourceLine, geometryFactory);
            simpleFeature.setDefaultGeometry(targetLine);
        }
    }

    private SimpleFeatureType createFeatureType(GeoCoding geoCoding) throws IOException {
        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName("gov.nasa.gsfc.contour.contourVectorData");
        ftb.add("contour_lines", LineString.class, geoCoding.getImageCRS());
        ftb.setDefaultGeometry("contour_lines");
        final SimpleFeatureType ft = ftb.buildFeatureType();
        ft.getUserData().put("contourVectorData", "true");
        return ft;
    }

    private static SimpleFeature createFeature(SimpleFeatureType type, LineString lineString) {

        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(type);
        /*0*/
        fb.add(lineString);
        return fb.buildFeature(null);
    }

    private FeatureCollection<SimpleFeatureType, SimpleFeature> createContourFeatureCollection(Product product, ContourData contourData) {
//        ArrayList<Double> contourIntervals = new ArrayList<Double>();
//
//        for (double level = 0.9; level < 3; level += 0.5) {
//            contourIntervals.add(level);
//        }

        //Band band1 = product.getBand("chlor_a");
        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        pb.setSource("source0", contourData.getBand().getSourceImage());
        pb.setParameter("levels", contourData.getLevels());
        RenderedOp dest = JAI.create("Contour", pb);
        Collection<LineString> contours = (Collection<LineString>) dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);
        GeoCoding geoCoding = VisatApp.getApp().getSelectedProduct().getGeoCoding();
        SimpleFeatureType featureType = null;
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;
        try {
            featureType = createFeatureType(geoCoding);
            featureCollection = new ListFeatureCollection(featureType);
        } catch (IOException ioe) {

        }
        for (LineString lineString : contours) {
            Coordinate[] coordinates = lineString.getCoordinates();
            for (int i = 0; i < coordinates.length; i++) {
                coordinates[i].x = coordinates[i].x + 0.5;
                coordinates[i].y = coordinates[i].y + 0.5;
            }
            final SimpleFeature feature = createFeature(featureType, lineString);
            if (feature != null) {
                featureCollection.add(feature);
            }
        }

        final CoordinateReferenceSystem mapCRS = geoCoding.getMapCRS();
        if (!mapCRS.equals(DefaultGeographicCRS.WGS84)) {
            try {
                transformFeatureCollection(featureCollection, geoCoding.getImageCRS(), mapCRS);
            } catch (TransformException e) {
                VisatApp.getApp().showErrorDialog("transformation failed!");
            }
        }
        return featureCollection;
    }

}

