package gov.nasa.gsfc.seadas.colorbar.layer;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerContext;
import com.bc.ceres.glayer.annotations.LayerTypeMetadata;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelModel;
import com.bc.ceres.glevel.support.DefaultMultiLevelSource;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.visat.VisatApp;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import javax.media.jai.operator.FileLoadDescriptor;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 7/14/14
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */
@LayerTypeMetadata(name = "ColorBarLayerType",
        aliasNames = {"gov.nasa.gsfc.seadas.colorbar.layer.ColorBarLayerType"})
public class ColorBarLayerType extends ImageLayer.Type {

    public static final String PROPERTY_NAME_IMAGE_FILE = "filePath";
    public static final String PROPERTY_NAME_COLORBAR_TRANSFORM = "colorBarTransform";
    private static final String COLOR_BAR_LABEL = "Color Bar";

    @Override
    public boolean isValidFor(LayerContext ctx) {
        if (ctx.getCoordinateReferenceSystem() instanceof AbstractIdentifiedObject) {
            AbstractIdentifiedObject crs = (AbstractIdentifiedObject) ctx.getCoordinateReferenceSystem();
            return DefaultGeographicCRS.WGS84.equals(crs, false);
        }
        return false;
    }

    @Override
    public Layer createLayer(LayerContext ctx, PropertySet configuration) {
        final File file = (File) configuration.getValue(PROPERTY_NAME_IMAGE_FILE);
        RenderedImage image = FileLoadDescriptor.create(file.getPath(), null, true, null);

//        configuration.setValue(PROPERTY_NAME_COLORBAR_TRANSFORM, createTransform(image));
//        final AffineTransform transform = (AffineTransform) configuration.getValue(PROPERTY_NAME_COLORBAR_TRANSFORM);

        final AffineTransform transform = createTransform(image);
        final Rectangle2D modelBounds = DefaultMultiLevelModel.getModelBounds(transform, image);
        final DefaultMultiLevelModel model = new DefaultMultiLevelModel(1, transform, modelBounds);
        final MultiLevelSource multiLevelSource = new DefaultMultiLevelSource(image, model);
        return new ImageLayer(this, multiLevelSource, configuration);
    }

    @Override
    public PropertySet createLayerConfig(LayerContext ctx) {
        final PropertyContainer template = new PropertyContainer();

        final Property filePathModel = Property.create(PROPERTY_NAME_IMAGE_FILE, File.class);
        filePathModel.getDescriptor().setNotNull(true);
        template.addProperty(filePathModel);

        final Property colorBarTransformModel = Property.create(PROPERTY_NAME_COLORBAR_TRANSFORM, AffineTransform.class);
        colorBarTransformModel.getDescriptor().setNotNull(true);
        template.addProperty(colorBarTransformModel);

        return template;
    }

    private AffineTransform createTransform(RenderedImage image) {

        VisatApp visatApp = VisatApp.getApp();
        ProductSceneView sceneView = visatApp.getSelectedProductSceneView();
        RasterDataNode raster = sceneView.getSceneImage().getRasters()[0];
        AffineTransform transform = raster.getSourceImage().getModel().getImageToModelTransform(0);
        transform.concatenate(createTransform(raster, image));
        return transform;
    }

    private AffineTransform getRotationTransform() {
        AffineTransform rotation = new AffineTransform();
        rotation.setToQuadrantRotation(3);
        return rotation;
    }

    private AffineTransform getTranslateTransform() {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-VisatApp.getApp().getSelectedProductSceneView().getBaseImageLayer().getImage().getWidth(), 0);
        return tx;
    }

    private AffineTransform createTransform(RasterDataNode raster, RenderedImage image) {

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int rasterWidth = raster.getRasterWidth();
        int rasterHeight = raster.getRasterHeight();

        double scaleX = (image.getHeight() < image.getWidth()) ? (double) rasterWidth / imageWidth : 1.0;
        double scaleY = (image.getHeight() > image.getWidth()) ?(double) rasterHeight / imageHeight : 1.0;
        if (scaleX > 1) scaleY = scaleY +1;
        if (scaleY > 1) scaleX = scaleX +1;
        int y_axis_translation =  (image.getHeight() < image.getWidth()) ? -image.getHeight() : 0;
        int x_axis_translation = (image.getHeight() < image.getWidth()) ? 0 : raster.getRasterWidth();
        double[] flatmatrix = {scaleX, 0.0, 0.0, scaleY, x_axis_translation, y_axis_translation};
        AffineTransform i2mTransform = new AffineTransform(flatmatrix);
        return i2mTransform;
    }

    public static BufferedImage getScaledImage(BufferedImage image, int width, int height) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        double scaleX = (double) width / imageWidth;
        double scaleY = (double) height / imageHeight;
        AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
        AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);

        return bilinearScaleOp.filter(
                image,
                new BufferedImage(width, height, image.getType()));
    }

}
