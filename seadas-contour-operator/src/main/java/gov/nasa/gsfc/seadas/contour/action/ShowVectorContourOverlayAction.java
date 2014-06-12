package gov.nasa.gsfc.seadas.contour.action;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import gov.nasa.gsfc.seadas.ContourDescriptor;
import gov.nasa.gsfc.seadas.contour.ui.ContourData;
import gov.nasa.gsfc.seadas.contour.ui.ContourDialog;
import gov.nasa.gsfc.seadas.contour.ui.ContourInterval;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.util.FeatureUtils;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.actions.AbstractShowOverlayAction;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 4/17/14
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowVectorContourOverlayAction extends AbstractShowOverlayAction {

    final String DEFAULT_STYLE_FORMAT = "fill:%s; fill-opacity:0.5; stroke:%s; stroke-opacity:1.0; stroke-width:1.0; symbol:cross";

    @Override
    public void actionPerformed(CommandEvent event) {
        VisatApp visatApp = VisatApp.getApp();
        final ProductSceneView sceneView = VisatApp.getApp().getSelectedProductSceneView();
        Product product = visatApp.getSelectedProduct();

        ContourDialog contourDialog = new ContourDialog(product);
        contourDialog.setVisible(true);
        contourDialog.dispose();

        ContourData contourData = contourDialog.getContourData();

        if (contourData == null) {
            return;
        }
        double scalingFactor = sceneView.getSceneImage().getRasters()[0].getScalingFactor();
        double scalingOffset = sceneView.getSceneImage().getRasters()[0].getScalingOffset();

        ArrayList<VectorDataNode> vectorDataNodes = createVectorDataNodesforContours(contourData, scalingFactor, scalingOffset);

        for (VectorDataNode vectorDataNode : vectorDataNodes) {
            product.getVectorDataGroup().add(vectorDataNode);
            if (sceneView != null) {
                sceneView.setLayersVisible(vectorDataNode);
            }
        }
    }

    @Override
    protected void updateEnableState(ProductSceneView view) {
        setEnabled(ProductUtils.canGetPixelPos(view.getRaster()));
    }

    @Override
    protected void updateSelectState(ProductSceneView view) {
        //setSelected(view.isGraticuleOverlayEnabled());
    }

    private ArrayList<VectorDataNode> createVectorDataNodesforContours(ContourData contourData, double scalingFactor, double scalingOffset) {
        ArrayList<ContourInterval> contourIntervals = contourData.getLevels();
        ArrayList<VectorDataNode> vectorDataNodes = new ArrayList<VectorDataNode>();

        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        pb.setSource("source0", contourData.getBand().getSourceImage());
        //pb.setSource("source0", getFilteredBand(contourData.getBand()).getSourceImage());
        pb.setParameter("levels", contourData.getLevels());

        for (ContourInterval interval : contourIntervals) {
            ArrayList<Double> contourInterval = new ArrayList<Double>();
            String vectorName = interval.getContourLevelName();
            double contourValue = (interval.getContourLevelValue() - scalingOffset) / scalingFactor;
            contourInterval.add(contourValue);
            pb.setParameter("levels", contourInterval);

            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;

            try {
                featureCollection = createContourFeatureCollection(pb);
            } catch (Exception e) {
                if (contourData.getLevels().size() != 0)
                    VisatApp.getApp().showErrorDialog("failed to create contour lines");
                System.out.println(e.getMessage());
                continue;
            }
            if (featureCollection.isEmpty()) {
                VisatApp.getApp().showErrorDialog("Contour Lines", "No records found.");
                continue;
            }

            final PlacemarkDescriptor placemarkDescriptor = PlacemarkDescriptorRegistry.getInstance().getPlacemarkDescriptor(featureCollection.getSchema());
            placemarkDescriptor.setUserDataOf(featureCollection.getSchema());
            VectorDataNode vectorDataNode = new VectorDataNode(vectorName, featureCollection, placemarkDescriptor);

            //convert RGB color to an hexadecimal value
            //String hex = "#"+Integer.toHexString(interval.getLineColor().getRGB()).substring(2);
            String hex = String.format("#%02x%02x%02x", interval.getLineColor().getRed(), interval.getLineColor().getGreen(), interval.getLineColor().getBlue());
            vectorDataNode.setDefaultStyleCss(String.format(DEFAULT_STYLE_FORMAT, hex, hex));
            vectorDataNodes.add(vectorDataNode);
        }
        return vectorDataNodes;
    }

    private FeatureCollection<SimpleFeatureType, SimpleFeature> createContourFeatureCollection(ParameterBlockJAI pb) {

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

    private FeatureCollection<SimpleFeatureType, SimpleFeature> createContourFeatureCollection(ContourData contourData) {
        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        //pb.setSource("source0", contourData.getBand().getSourceImage());
        pb.setSource("source0", getFilteredBand(contourData.getBand()).getSourceImage());
        pb.setParameter("levels", contourData.getLevels());
        pb.setParameter("smooth", Boolean.TRUE);

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


    Band getFilteredBand(Band selectedBand){
        Filter filter = new GeneralFilter("Mean 5x5", 5, 5, GeneralFilterBand.MEAN);
        Band filteredBand = createFilterBand(filter, selectedBand.getName()+"_filtered");
        return filteredBand;
    }

    private static abstract class Filter {

        private String name;

        public Filter(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public abstract boolean equals(Object obj);
    }

    private static class KernelFilter extends Filter {

        private Kernel kernel;

        public KernelFilter(String name, Kernel kernel) {
            super(name);
            this.kernel = kernel;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof KernelFilter) {
                KernelFilter other = (KernelFilter) obj;
                return toString().equals(other.toString()) && kernel.equals(other.kernel);
            }
            return false;
        }
    }

    private static class GeneralFilter extends Filter {

        int width;
        int height;
        GeneralFilterBand.Operator operator;

        public GeneralFilter(String name, int width, int height, GeneralFilterBand.Operator operator) {
            super(name);
            this.width = width;
            this.height = height;
            this.operator = operator;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof GeneralFilter) {
                GeneralFilter other = (GeneralFilter) obj;
                return toString().equals(other.toString()) && operator == other.operator;
            }
            return false;
        }
    }

    private static FilterBand createFilterBand(Filter filter, String bandName) {
        final RasterDataNode raster = (RasterDataNode) VisatApp.getApp().getSelectedProductNode();

        final FilterBand filterBand;
        if (filter instanceof KernelFilter) {
            final KernelFilter kernelFilter = (KernelFilter) filter;
            filterBand = new ConvolutionFilterBand(bandName, raster, kernelFilter.kernel);
        } else {
            final GeneralFilter generalFilter = (GeneralFilter) filter;
            filterBand = new GeneralFilterBand(bandName, raster, generalFilter.width, generalFilter.operator);
        }
        final String descr = MessageFormat.format("Filter ''{0}'' applied to ''{1}''",
                filter.toString(),
                raster.getName());
        filterBand.setDescription(descr);
        raster.getProduct().addBand(filterBand);
        filterBand.fireProductNodeDataChanged();
        return filterBand;
    }
}

