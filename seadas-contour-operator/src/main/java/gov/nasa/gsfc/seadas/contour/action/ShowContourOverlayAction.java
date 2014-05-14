package gov.nasa.gsfc.seadas.contour.action;

import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerFilter;
import com.bc.ceres.glayer.LayerType;
import com.bc.ceres.glayer.LayerTypeRegistry;
import com.bc.ceres.glayer.support.LayerUtils;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.swing.figure.FigureEditor;
import com.bc.ceres.swing.figure.support.DefaultFigureStyle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import gov.nasa.gsfc.seadas.ContourDescriptor;
import gov.nasa.gsfc.seadas.contour.layer.ContourLayer;
import gov.nasa.gsfc.seadas.contour.layer.ContourLayerType;
import gov.nasa.gsfc.seadas.contour.ui.ContourDialog;
import gov.nasa.gsfc.seadas.contour.util.Java2DConverter;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.dataop.maptransf.IdentityTransformDescriptor;
import org.esa.beam.framework.dataop.maptransf.MapTransformDescriptor;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.product.*;
import org.esa.beam.util.PropertyMap;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.actions.AbstractShowOverlayAction;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import javax.media.jai.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;

import static org.esa.beam.framework.datamodel.PlainFeatureFactory.createPlainFeature;
import static org.esa.beam.framework.datamodel.PlainFeatureFactory.createPlainFeatureType;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 9/3/13
 * Time: 1:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowContourOverlayAction extends AbstractShowOverlayAction {


    private static final String CONTOUR_TYPE_PROPERTY_NAME = "contour.type";
    private static final String DEFAULT_LAYER_TYPE = "VectorDataLayerType";
    public static final String CONTOUR_LAYER_ID = "gov.nasa.gsfc.seadas.contour";

    @Override
    public void actionPerformed(CommandEvent event) {
        final ProductSceneView view = VisatApp.getApp().getSelectedProductSceneView();
        VectorDataFigureEditor contourFigureEditor = new VectorDataFigureEditor(view);
//        //view.getSceneImage().getConfiguration();
        final Product product = VisatApp.getApp().getSelectedProduct();
        //view.getSceneImage().getImageToModelTransform();
        Band band = product.getBand("chlor_a");
        ContourDialog contourDialog = new ContourDialog(product);
        contourDialog.setVisible(true);
        contourDialog.dispose();
        band.setSourceImage(getContourImage(product, band));
//
////        //view.getSceneImage().getRasters()[0];
        if (view != null) {
            Layer rootLayer = view.getRootLayer();
            Layer contourLayer = findContourLayer(view);
            if (isSelected()) {
                if (contourLayer == null) {
                    contourLayer = createContourLayer(view, product);
                    rootLayer.getChildren().add(contourLayer);
                }
                contourLayer.setVisible(true);
            } else {
                contourLayer.getParent().getChildren().remove(contourLayer);
            }
        }
    }

    private PlanarImage getContourImage(Product product, Band band) {

        JAI.setDefaultTileSize(new Dimension(512, 512));
        ArrayList<Double> contourIntervals = new ArrayList<Double>();

        for (double level = 0.5; level < 1; level += 2) {
            contourIntervals.add(level);
        }

        //Contour1Spi cspi = new Contour1Spi();

        //cspi.updateRegistry(JAI.getDefaultInstance().getOperationRegistry());

        OperationRegistry or = JAI.getDefaultInstance().getOperationRegistry();
        String modeName = "rendered";
        String[] descriptorNames;

        for (String name : or.getDescriptorNames(modeName)) {
            System.out.println(name);
        }
        Band band1 = product.getBand("chlor_a");
        PlanarImage pi = product.getBand("chlor_a").getSourceImage();
        MultiLevelImage mli = band1.getSourceImage();
        TiledImage ti = new TiledImage(pi.getMinX(), pi.getMinY(), pi.getWidth(), pi.getHeight(), pi.getTileGridXOffset(), pi.getTileGridYOffset(), pi.getSampleModel(), pi.getColorModel());
        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        //pb.setSource("source0", band1.getGeophysicalImage());
        pb.setSource("source0", band1.getSourceImage());
        pb.setParameter("levels", contourIntervals);
        RenderedOp dest = JAI.create("Contour", pb);
        Collection<LineString> contours = (Collection<LineString>) dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);

        //SimpleFeatureType sft = createPlainFeatureType("Geometry", Geometry.class, DefaultGeographicCRS.WGS84);
        SimpleFeatureType sft = createPlainFeatureType("Contours", LineString.class, product.getGeoCoding().getGeoCRS());
        SimpleFeatureFigureFactory simpleFeatureFigureFactory = new SimpleFeatureFigureFactory(sft);
        Geometry geometry;
        SimpleFeature feature;
        SimpleFeatureShapeFigure figure;
        FigureEditor figureEditor = new VectorDataFigureEditor(VisatApp.getApp().getSelectedProductSceneView());
        final GeoCoding geoCoding = product.getGeoCoding();
        final PixelPos pixelPos = new PixelPos();
        final GeoPos geoPos = new GeoPos();
        VectorDataNode vectorDataNode = new VectorDataNode("contour_lines", sft);
        ProductNodeGroup<VectorDataNode> vectorDataNodeGroup = product.getVectorDataGroup();
        GeneralPath generalPath = null;
        for (LineString lineString : contours) {
            //geometry = createLineString();
            //geometry = lineString;
            Java2DConverter java2DConverter = new Java2DConverter(null);   //pi.getAsBufferedImage().createGraphics().getTransform()
            try {
                generalPath = (GeneralPath) java2DConverter.toShape(lineString);
            } catch (NoninvertibleTransformException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
//            Coordinate[] coordinates = lineString.getCoordinates();
//                        int numCoor = coordinates.length;
//                        for (int i = 0; i < numCoor - 1; i++) {
//                pixelPos.setLocation(coordinates[i].x, coordinates[i].y);
//                geoCoding.getGeoPos(pixelPos, geoPos);
//                coordinates[i].x = geoPos.lon;
//                coordinates[i].y = geoPos.lat;
//            }
            feature = createPlainFeature(sft, "Contours", lineString, "style_css");
            figure = new SimpleFeatureShapeFigure(feature, new DefaultFigureStyle());
            figure = (SimpleFeatureShapeFigure) simpleFeatureFigureFactory.createShapeFigure((Geometry)lineString, figureEditor.getDefaultLineStyle());
            figureEditor.getFigureCollection().addFigure(figure);
            //vectorDataNode = new VectorDataNode("contour_lines", sft);

            vectorDataNode.getFeatureCollection().add(feature);

            final ProductSceneView view = VisatApp.getApp().getSelectedProductSceneView();
            if (view != null) {
                view.setLayersVisible(vectorDataNode);
            }

            //vectorDataNode.getFeatureCollection().add(figure.getShape().getPathIterator(new AffineTransform()))
        }


        product.getVectorDataGroup().add(vectorDataNode);
        //BufferedImage g = ti.getAsBufferedImage();     //ti.createGraphics();
//        Graphics2D g2d = g.createGraphics();
//        //final Viewport vp = pi.getViewport();
//        final AffineTransform transformSave = g2d.getTransform();
//        try {
//            final AffineTransform transform = new AffineTransform();
//            transform.concatenate(transformSave);
//            //transform.concatenate(vp.getModelToViewTransform());
//            transform.concatenate(((MultiLevelImage) pi).getModel().getImageToModelTransform(0));
//            g2d.setTransform(transform);
//            g2d.setColor(Color.ORANGE);
//            for (LineString contour : contours) {
//                Coordinate[] coordinates = contour.getCoordinates();
//                int numCoor = coordinates.length;
//                for (int i = 0; i < numCoor - 1; i++) {
//                    g2d.draw(new Line2D.Double(coordinates[i].x, coordinates[i].y, coordinates[i+1].x, coordinates[i+1].y));
//                }
//            }
////            final GeneralPath[] linePaths = graticule.getLinePaths();
////            if (linePaths != null) {
////                drawLinePaths(g2d, linePaths);
////            }
//        } finally {
//            g2d.setTransform(transformSave);
//        }
//        JTSFrame jtsFrame = new JTSFrame("Contours from source image");
//        for (LineString contour : contours) {
//            jtsFrame.addGeometry((Geometry) contour, Color.BLUE);
//            com.vividsolutions.jts.geom.Point p = contour.getPointN(0);
//            contour.getLength();
//        }
//        ImageFrame imgFrame = new ImageFrame(dest.getRendering(), "Source image");
//        imgFrame.setLocation(100, 100);
//        imgFrame.setVisible(true);
//
//        Dimension size = imgFrame.getSize();
//        jtsFrame.setSize(size);
//        jtsFrame.setLocation(100 + size.width + 5, 100);
//        jtsFrame.setVisible(true);

//        jtsFrame.update(g2d);
//        jtsFrame.setLocation(100 + size.width + 15, 100);
//        jtsFrame.setVisible(true);
          dest.getCurrentRendering();

        return dest.getRendering();

    }

    private Layer createContourLayer(ProductSceneView view, Product product) {
//        final VectorDataLayerType layerType = getContourLayerType();
//        final PropertySet template = layerType.createLayerConfig(view.getLayerContext());
        final LayerType layerType = LayerTypeRegistry.getLayerType(ContourLayerType.class);
         final PropertySet template = layerType.createLayerConfig(null);
         template.setValue(ContourLayerType.PROPERTY_NAME_RASTER, view.getSceneImage().getRasters()[0] );
         final ContourLayer contourLayer = (ContourLayer) layerType.createLayer(null, template);
         contourLayer.setId(CONTOUR_LAYER_ID);
         contourLayer.setVisible(false);
         contourLayer.setName("Contour");
         setContourLayerStyle(view.getSceneImage().getConfiguration(), contourLayer);
         //return contourLayer;
        return VectorDataLayerType.createLayer(view.getLayerContext(), getContourData(product));
    }



    private VectorDataNode getContourData(Product product) {

        ArrayList<Double> contourIntervals = new ArrayList<Double>();

        for (double level = 1; level < 3; level += 2) {
            contourIntervals.add(level);
        }

        Band band1 = product.getBand("chlor_a");
        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        pb.setSource("source0", band1.getSourceImage());
        pb.setParameter("levels", contourIntervals);
        RenderedOp dest = JAI.create("Contour", pb);
        Collection<LineString> contours = (Collection<LineString>) dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);

        SimpleFeatureType sft = createPlainFeatureType("org.esa.beam.Geometry", Geometry.class, null);

        SimpleFeature feature;
        final GeoCoding geoCoding = product.getGeoCoding();
        final PixelPos pixelPos = new PixelPos();
        final GeoPos geoPos = new GeoPos();
        VectorDataNode vectorDataNode = new VectorDataNode("contour_lines", sft);
        GeneralPath generalPath = new GeneralPath();
        Java2DConverter java2DConverter = new Java2DConverter(null);

        for (LineString lineString : contours) {
            Coordinate[] coordinates = lineString.getCoordinates();
            int numCoor = coordinates.length;
            for (int i = 0; i < numCoor; i++) {
                pixelPos.setLocation(coordinates[i].x, coordinates[i].y);
                geoCoding.getGeoPos(pixelPos, geoPos);
                coordinates[i].x = geoPos.lon;
                coordinates[i].y = geoPos.lat;
            }

            try {

                generalPath = (GeneralPath) java2DConverter.toShape(lineString);
            } catch (NoninvertibleTransformException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            feature = createPlainFeature(sft, "Contours", lineString, "style_css");

            vectorDataNode.getFeatureCollection().add(feature);
            //vectorDataNode.getFeatureCollection().add(figure.getShape().getPathIterator(new AffineTransform()))
        }
        product.getVectorDataGroup().add(vectorDataNode);
        return vectorDataNode;
    }

//        private ContourLayer createContourLayer(AffineTransform i2mTransform) {
//            final LayerType layerType = LayerTypeRegistry.getLayerType(ContourLayerType.class);
//            final PropertySet template = layerType.createLayerConfig(null);
//            template.setValue(ContourLayerType.PROPERTY_NAME_RASTER, );
//            final ContourLayer contourLayer = (ContourLayer) layerType.createLayer(null, template);
//            contourLayer.setId(ProductSceneView.GRATICULE_LAYER_ID);
//            contourLayer.setVisible(false);
//            contourLayer.setName("Contour");
//            setContourLayerStyle(configuration, contourLayer);
//            return contourLayer;
//        }
    @Override
    protected void updateEnableState(ProductSceneView view) {
        RasterDataNode raster = view.getRaster();
        GeoCoding geoCoding = raster.getGeoCoding();
        setEnabled(isGeographicLatLon(geoCoding));
    }

    private boolean isGeographicLatLon(GeoCoding geoCoding) {
        if (geoCoding instanceof MapGeoCoding) {
            MapGeoCoding mapGeoCoding = (MapGeoCoding) geoCoding;
            MapTransformDescriptor transformDescriptor = mapGeoCoding.getMapInfo()
                    .getMapProjection().getMapTransform().getDescriptor();
            String typeID = transformDescriptor.getTypeID();
            if (typeID.equals(IdentityTransformDescriptor.TYPE_ID)) {
                return true;
            }
        } else if (geoCoding instanceof CrsGeoCoding) {
            return CRS.equalsIgnoreMetadata(geoCoding.getMapCRS(), DefaultGeographicCRS.WGS84);
        }
        return false;
    }

    @Override
    protected void updateSelectState(ProductSceneView view) {
        Layer blueMarbleLayer = findContourLayer(view);
        setSelected(blueMarbleLayer != null && blueMarbleLayer.isVisible());
    }

//    private LayerType getContourLayerType() {
//        final VisatApp visatApp = VisatApp.getApp();
//        String layerTypeClassName = visatApp.getPreferences().getPropertyString(CONTOUR_TYPE_PROPERTY_NAME,
//                DEFAULT_LAYER_TYPE);
//        return LayerTypeRegistry.getLayerType(layerTypeClassName);
//    }

    private Layer findContourLayer(ProductSceneView view) {
        return LayerUtils.getChildLayer(view.getRootLayer(), LayerUtils.SearchMode.DEEP, new LayerFilter() {
            @Override
            public boolean accept(Layer layer) {
                return layer.getLayerType() instanceof ContourLayerType;
            }
        });
    }
    
    static void setContourLayerStyle(PropertyMap configuration, Layer layer) {
        final PropertySet layerConfiguration = layer.getConfiguration();

        layerConfiguration.setValue(ContourLayerType.PROPERTY_NAME_RES_AUTO,
                                    configuration.getPropertyBool(ContourLayerType.PROPERTY_NAME_RES_AUTO,
                                                                  ContourLayerType.DEFAULT_RES_AUTO));
        layerConfiguration.setValue(ContourLayerType.PROPERTY_NAME_RES_PIXELS,
                                    configuration.getPropertyInt(ContourLayerType.PROPERTY_NAME_RES_PIXELS,
                                                                 ContourLayerType.DEFAULT_RES_PIXELS));
        layerConfiguration.setValue(ContourLayerType.PROPERTY_NAME_RES_LAT,
                                    configuration.getPropertyDouble(ContourLayerType.PROPERTY_NAME_RES_LAT,
                                                                    ContourLayerType.DEFAULT_RES_LAT));
        layerConfiguration.setValue(ContourLayerType.PROPERTY_NAME_RES_LON,
                                    configuration.getPropertyDouble(ContourLayerType.PROPERTY_NAME_RES_LON,
                                                                    ContourLayerType.DEFAULT_RES_LON));

        layerConfiguration.setValue(ContourLayerType.PROPERTY_NAME_LINE_COLOR,
                                    configuration.getPropertyColor(ContourLayerType.PROPERTY_NAME_LINE_COLOR,
                                                                   ContourLayerType.DEFAULT_LINE_COLOR));
        layerConfiguration.setValue(ContourLayerType.PROPERTY_NAME_LINE_WIDTH,
                                    configuration.getPropertyDouble(ContourLayerType.PROPERTY_NAME_LINE_WIDTH,
                                                                    ContourLayerType.DEFAULT_LINE_WIDTH));
        layerConfiguration.setValue(ContourLayerType.PROPERTY_NAME_LINE_TRANSPARENCY,
                                    configuration.getPropertyDouble(
                                            ContourLayerType.PROPERTY_NAME_LINE_TRANSPARENCY,
                                            ContourLayerType.DEFAULT_LINE_TRANSPARENCY));
        layerConfiguration.setValue(ContourLayerType.PROPERTY_NAME_TEXT_ENABLED,
                                    configuration.getPropertyBool(ContourLayerType.PROPERTY_NAME_TEXT_ENABLED,
                                                                  ContourLayerType.DEFAULT_TEXT_ENABLED));
        layerConfiguration.setValue(ContourLayerType.PROPERTY_NAME_TEXT_FG_COLOR,
                                    configuration.getPropertyColor(ContourLayerType.PROPERTY_NAME_TEXT_FG_COLOR,
                                                                   ContourLayerType.DEFAULT_TEXT_FG_COLOR));
        layerConfiguration.setValue(ContourLayerType.PROPERTY_NAME_TEXT_BG_COLOR,
                                    configuration.getPropertyColor(ContourLayerType.PROPERTY_NAME_TEXT_BG_COLOR,
                                                                   ContourLayerType.DEFAULT_TEXT_BG_COLOR));
        layerConfiguration.setValue(ContourLayerType.PROPERTY_NAME_TEXT_BG_TRANSPARENCY,
                                    configuration.getPropertyDouble(
                                            ContourLayerType.PROPERTY_NAME_TEXT_BG_TRANSPARENCY,
                                            ContourLayerType.DEFAULT_TEXT_BG_TRANSPARENCY));
    }


}