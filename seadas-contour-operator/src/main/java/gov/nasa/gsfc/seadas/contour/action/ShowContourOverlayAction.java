package gov.nasa.gsfc.seadas.contour.action;

import gov.nasa.gsfc.seadas.contour.extensions.SeadasProductSceneView;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.visat.VisatApp;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 9/3/13
 * Time: 1:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowContourOverlayAction extends ExecCommand {

    @Override
    public void actionPerformed(CommandEvent event) {
        final SeadasProductSceneView view = (SeadasProductSceneView)VisatApp.getApp().getSelectedProductSceneView();

        if (view != null) {
            if (ProductUtils.canGetPixelPos(view.getRaster())) {
                view.setContourOverlayEnabled(isSelected());
            }
        }
    }

//    @Override
//    protected void updateEnableState(ProductSceneView view) {
//        setEnabled(ProductUtils.canGetPixelPos(view.getRaster()));
//    }
//
//    @Override
//    protected void updateSelectState(ProductSceneView view) {
//        setSelected(view.isGraticuleOverlayEnabled());
//    }
}