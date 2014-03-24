package gov.nasa.gsfc.seadas.contour.ui;

import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.glayer.CollectionLayer;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerType;
import com.bc.ceres.glayer.LayerTypeRegistry;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.grender.Viewport;
import com.bc.ceres.grender.support.BufferedImageRendering;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Mask;
import org.esa.beam.glayer.GraticuleLayer;
import org.esa.beam.glayer.GraticuleLayerType;
import org.esa.beam.glayer.MaskLayerType;

import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 3/6/14
 * Time: 12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContourRendering {


    public void render(Band band) {
        CollectionLayer collectionLayer = new CollectionLayer();
        List<Layer> layerChildren = collectionLayer.getChildren();

        ImageLayer imageLayer = new ImageLayer((RenderedImage) band.getSourceImage()); // or a RenderedImage

        LayerType graticuleType = LayerTypeRegistry.getLayerType(GraticuleLayerType.class);
        PropertySet template = graticuleType.createLayerConfig(null);
        template.setValue(GraticuleLayerType.PROPERTY_NAME_RASTER, band);
        GraticuleLayer graticuleLayer = (GraticuleLayer) graticuleType.createLayer(null, template);
        MaskLayerType type = LayerTypeRegistry.getLayerType(MaskLayerType.class);
        PropertySet configuration = type.createLayerConfig(null);
        //Mask.BandMathsType.create("coast", "", band.getSceneRasterWidth(), band.getSceneRasterHeight(), "l1_flags.COASTLINE");
        Mask coastlineMask = Mask.BandMathsType.create("coast", "", band.getSceneRasterWidth(), band.getSceneRasterHeight(), "l1_flags.COASTLINE", null, 0);
        configuration.setValue(MaskLayerType.PROPERTY_NAME_MASK, coastlineMask);
        com.bc.ceres.glayer.Layer coastlineLayer = type.createLayer(null, configuration);

        layerChildren.add(0, imageLayer);
        layerChildren.add(0, graticuleLayer);
        layerChildren.add(0, coastlineLayer);
        Rectangle2D modelBounds = collectionLayer.getModelBounds();
        Rectangle2D imageBounds = imageLayer.getModelToImageTransform().createTransformedShape(modelBounds).getBounds2D();
        BufferedImage bufferedImage = new BufferedImage((int) imageBounds.getWidth(),
                (int) imageBounds.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        BufferedImageRendering rendering = new BufferedImageRendering(bufferedImage);
        Viewport viewport = rendering.getViewport();
        viewport.setModelYAxisDown(imageLayer.getImageToModelTransform().getDeterminant() > 0.0);
        viewport.zoom(modelBounds);
        collectionLayer.render(rendering);
        RenderedImage outputImage = rendering.getImage();
    }
}
