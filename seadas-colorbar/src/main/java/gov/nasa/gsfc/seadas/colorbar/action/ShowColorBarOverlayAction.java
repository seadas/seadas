package gov.nasa.gsfc.seadas.colorbar.action;

import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerFilter;
import com.bc.ceres.glayer.LayerType;
import com.bc.ceres.glayer.LayerTypeRegistry;
import com.bc.ceres.glayer.support.LayerUtils;
import gov.nasa.gsfc.seadas.colorbar.layer.ColorBarLayerType;
import gov.nasa.gsfc.seadas.colorbar.ui.ColorBarImportDialog;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.actions.AbstractShowOverlayAction;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 7/10/14
 * Time: 12:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowColorBarOverlayAction extends AbstractShowOverlayAction {

    private static final String COLORBAR_TYPE_PROPERTY_NAME = "colorbar.type";
    private static final String DEFAULT_LAYER_TYPE = "ColorBarLayerType";

    @Override
    public void actionPerformed(CommandEvent event) {
        final ProductSceneView view = VisatApp.getApp().getSelectedProductSceneView();
        if (view != null) {
            Layer rootLayer = view.getRootLayer();
            Layer colorBarLayer = findColorBarLayer(view);
            if (isSelected()) {
                if (colorBarLayer == null) {
                    ColorBarImportDialog importDialog = new ColorBarImportDialog();
                    File imageFile = importDialog.promptForFile();
                    if (imageFile != null) {
                        colorBarLayer = createColorBarLayer(imageFile);
                        colorBarLayer.setName("Color Bar");
                        rootLayer.getChildren().add(colorBarLayer);
                    } else {
                        return;
                    }
                }
                colorBarLayer.setVisible(true);
            } else {
                view.updateCurrentLayer(colorBarLayer, false);
            }
        }
    }

    private Layer createColorBarLayer(File imageFile) {
        final LayerType layerType = getColorBarLayerType();
        final PropertySet template = layerType.createLayerConfig(null);
        template.setValue(ColorBarLayerType.PROPERTY_NAME_IMAGE_FILE, imageFile);
        return layerType.createLayer(null, template);
    }


    @Override
    protected void updateEnableState(ProductSceneView view) {
        setEnabled(true);
    }
    @Override
    protected void updateSelectState(ProductSceneView view) {
        Layer colorBarLayer = findColorBarLayer(view);
        setSelected(colorBarLayer != null && colorBarLayer.isVisible());
    }

    private LayerType getColorBarLayerType() {
        final VisatApp visatApp = VisatApp.getApp();
        String layerTypeClassName = visatApp.getPreferences().getPropertyString(COLORBAR_TYPE_PROPERTY_NAME,
                DEFAULT_LAYER_TYPE);
        return LayerTypeRegistry.getLayerType(layerTypeClassName);
    }

    private Layer findColorBarLayer(ProductSceneView view) {
        return LayerUtils.getChildLayer(view.getRootLayer(), LayerUtils.SearchMode.DEEP, new LayerFilter() {
            public boolean accept(Layer layer) {
                return layer.getLayerType() instanceof ColorBarLayerType;
            }
        });
    }
}
