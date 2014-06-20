package gov.nasa.gsfc.seadas.contour.action;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import gov.nasa.gsfc.seadas.ContourDescriptor;
import gov.nasa.gsfc.seadas.contour.data.ContourData;
import gov.nasa.gsfc.seadas.contour.data.ContourInterval;
import gov.nasa.gsfc.seadas.contour.ui.ContourDialog;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.util.FeatureUtils;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.actions.AbstractShowOverlayAction;
import org.esa.beam.visat.actions.imgfilter.CreateFilteredBandDialog;
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
    Product product;

    @Override
    public void actionPerformed(CommandEvent event) {
        VisatApp visatApp = VisatApp.getApp();
        final ProductSceneView sceneView = VisatApp.getApp().getSelectedProductSceneView();
        product = visatApp.getSelectedProduct();
        ProductNode productNode = visatApp.getApp().getSelectedProductNode();
        ContourDialog contourDialog = new ContourDialog(product, productNode.getName());
        contourDialog.setVisible(true);
        contourDialog.dispose();

        if (contourDialog.isContourCanceled()) {
            return;
        }

        ContourData contourData = contourDialog.getContourData();
        if (contourData.isFiltered()) {
            Band newBand = getFilteredBand(contourData.getBand());
            if (newBand != null) {
                contourData.setBand(newBand);
            }
        }
        //double scalingFactor = sceneView.getSceneImage().getRasters()[0].getScalingFactor();
        //double scalingOffset = sceneView.getSceneImage().getRasters()[0].getScalingOffset();
        ArrayList<VectorDataNode> vectorDataNodes = createVectorDataNodesforContours(contourData);

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

    private ArrayList<VectorDataNode> createVectorDataNodesforContours(ContourData contourData) {


        double scalingFactor = contourData.getBand().getScalingFactor();
        double scalingOffset = contourData.getBand().getScalingOffset();

        ArrayList<ContourInterval> contourIntervals = contourData.getLevels();
        ArrayList<VectorDataNode> vectorDataNodes = new ArrayList<VectorDataNode>();

        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        pb.setSource("source0", contourData.getBand().getSourceImage());

//        if (contourData.isFiltered()) {
//            pb.setSource("source0", getFilteredBand(contourData.getBand()).getSourceImage());
//        } else {
//            pb.setSource("source0", contourData.getBand().getSourceImage());
//        }

        for (ContourInterval interval : contourIntervals) {
            ArrayList<Double> contourInterval = new ArrayList<Double>();
            String vectorName = interval.getContourLevelName();
            if (contourData.isFiltered()) {
                vectorName = vectorName + "_filtered";
            }
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
                VisatApp.getApp().showErrorDialog("Contour Lines", "No records found for ." + contourData.getBand().getName() + " at " + (contourValue * scalingFactor + scalingOffset));
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

    Band getFilteredBand(Band selectedBand) {
        String filteredBandName = selectedBand.getName() + "_filtered";
        if (product.getBand(filteredBandName) != null) {
            return product.getBand(filteredBandName);
        }
        final CreateFilteredBandDialog.DialogData dialogData = promptForFilter();
        if (dialogData == null) {
            return null;
        }
        final FilterBand filterBand = createFilterBand(dialogData.getFilter(), dialogData.getBandName(), dialogData.getIterationCount());
        return filterBand;
    }

//    private FilterBand createFilteredBand() {
//        final CreateFilteredBandDialog.DialogData dialogData = promptForFilter();
//        if (dialogData == null) {
//            return null;
//        }
//        final FilterBand filterBand = createFilterBand(dialogData.getFilter(), dialogData.getBandName(), dialogData.getIterationCount());
//        return filterBand;
//    }

    private static FilterBand createFilterBand(org.esa.beam.visat.actions.imgfilter.model.Filter filter, String bandName, int iterationCount) {
        RasterDataNode sourceRaster = (RasterDataNode) VisatApp.getApp().getSelectedProductNode();

        FilterBand targetBand;
        Product product = sourceRaster.getProduct();

        if (filter.getOperation() == org.esa.beam.visat.actions.imgfilter.model.Filter.Operation.CONVOLVE) {
            targetBand = new ConvolutionFilterBand(bandName, sourceRaster, getKernel(filter), iterationCount);
            if (sourceRaster instanceof Band) {
                ProductUtils.copySpectralBandProperties((Band) sourceRaster, targetBand);
            }
        } else {
            GeneralFilterBand.OpType opType = getOpType(filter.getOperation());
            targetBand = new GeneralFilterBand(bandName, sourceRaster, opType, getKernel(filter), iterationCount);
            if (sourceRaster instanceof Band) {
                ProductUtils.copySpectralBandProperties((Band) sourceRaster, targetBand);
            }
        }

        targetBand.setDescription(String.format("Filter '%s' (=%s) applied to '%s'", filter.getName(), filter.getOperation(), sourceRaster.getName()));
        if (sourceRaster instanceof Band) {
            ProductUtils.copySpectralBandProperties((Band) sourceRaster, targetBand);
        }
        product.addBand(targetBand);
        targetBand.fireProductNodeDataChanged();
        return targetBand;
    }

    private static Kernel getKernel(org.esa.beam.visat.actions.imgfilter.model.Filter filter) {
        return new Kernel(filter.getKernelWidth(),
                filter.getKernelHeight(),
                filter.getKernelOffsetX(),
                filter.getKernelOffsetY(),
                1.0 / filter.getKernelQuotient(),
                filter.getKernelElements());
    }

    static GeneralFilterBand.OpType getOpType(org.esa.beam.visat.actions.imgfilter.model.Filter.Operation operation) {
        if (operation == org.esa.beam.visat.actions.imgfilter.model.Filter.Operation.OPEN) {
            return GeneralFilterBand.OpType.OPENING;
        } else if (operation == org.esa.beam.visat.actions.imgfilter.model.Filter.Operation.CLOSE) {
            return GeneralFilterBand.OpType.CLOSING;
        } else if (operation == org.esa.beam.visat.actions.imgfilter.model.Filter.Operation.ERODE) {
            return GeneralFilterBand.OpType.EROSION;
        } else if (operation == org.esa.beam.visat.actions.imgfilter.model.Filter.Operation.DILATE) {
            return GeneralFilterBand.OpType.DILATION;
        } else if (operation == org.esa.beam.visat.actions.imgfilter.model.Filter.Operation.MIN) {
            return GeneralFilterBand.OpType.MIN;
        } else if (operation == org.esa.beam.visat.actions.imgfilter.model.Filter.Operation.MAX) {
            return GeneralFilterBand.OpType.MAX;
        } else if (operation == org.esa.beam.visat.actions.imgfilter.model.Filter.Operation.MEAN) {
            return GeneralFilterBand.OpType.MEAN;
        } else if (operation == org.esa.beam.visat.actions.imgfilter.model.Filter.Operation.MEDIAN) {
            return GeneralFilterBand.OpType.MEDIAN;
        } else if (operation == org.esa.beam.visat.actions.imgfilter.model.Filter.Operation.STDDEV) {
            return GeneralFilterBand.OpType.STDDEV;
        } else {
            throw new IllegalArgumentException("illegal operation: " + operation);
        }
    }

    private CreateFilteredBandDialog.DialogData promptForFilter() {
        final ProductNode selectedNode = VisatApp.getApp().getSelectedProductNode();
        final Product product = selectedNode.getProduct();
        final CreateFilteredBandDialog dialog = new CreateFilteredBandDialog(product, selectedNode.getName(), getHelpId());
        if (dialog.show() == ModalDialog.ID_OK) {
            return dialog.getDialogData();
        }
        return null;
    }
}

