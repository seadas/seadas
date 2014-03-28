package gov.nasa.gsfc.seadas.contour.extensions;

import gov.nasa.gsfc.seadas.contour.layer.ContourLayer;
import org.esa.beam.framework.ui.product.ProductSceneImage;
import org.esa.beam.framework.ui.product.ProductSceneView;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 3/27/14
 * Time: 3:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class SeadasProductSceneView extends ProductSceneView{

    SeadasProductSceneImage seadasProductSceneImage;

    public static final String CONTOUR_LAYER_ID = "gov.nasa.gsfc.seadas.contour.layer.contour";

    public SeadasProductSceneView(ProductSceneImage sceneImage) {
        super(sceneImage);
        seadasProductSceneImage = (SeadasProductSceneImage)sceneImage;

    }

    public boolean isContourOverlayEnabled() {
        final ContourLayer contourLayer = getContourLayer(false);
        return contourLayer != null && contourLayer.isVisible();
    }

    public void setContourOverlayEnabled(boolean enabled) {
        if (isGraticuleOverlayEnabled() != enabled) {
            getContourLayer(true).setVisible(enabled);
        }
    }

    protected SeadasProductSceneImage getSceneImage(){
        return  seadasProductSceneImage;

    }
    private ContourLayer getContourLayer(boolean create) {
        return getSceneImage().getContourLayer(create);
    }

}
