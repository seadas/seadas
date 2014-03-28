package gov.nasa.gsfc.seadas.contour.extensions;

import org.esa.beam.framework.ui.product.ProductSceneImage;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.glayer.GraticuleLayer;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 3/27/14
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class SeadasProductSceneImage extends ProductSceneImage {

    ContourLayer getContourLayer(boolean create) {
        ContourLayer layer = (ContourLayer) getLayer(ProductSceneView.GRATICULE_LAYER_ID);
        if (layer == null && create) {
            layer = createGraticuleLayer(getImageToModelTransform());
            addLayer(0, layer);
        }
        return layer;
    }
}
