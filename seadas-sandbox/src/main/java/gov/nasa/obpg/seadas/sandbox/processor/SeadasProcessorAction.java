/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.obpg.seadas.sandbox.processor;

import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.visat.VisatApp;

/**
 *
 * @author dshea
 */
public class SeadasProcessorAction extends ExecCommand {

    private SeadasProcessorFrame seadasProcessorFrame;

    @Override
    public void actionPerformed(final CommandEvent event) {
        ProductSceneView view = VisatApp.getApp().getSelectedProductSceneView(); // Get current view

        if (seadasProcessorFrame == null) {
            seadasProcessorFrame = new SeadasProcessorFrame();
        }
        seadasProcessorFrame.setVisible(true);


    }

    @Override
    public void updateState(final CommandEvent event) {
//        ProductSceneView view = VisatApp.getApp().getSelectedProductSceneView();
//        setEnabled(view != null);
          setEnabled(true);
    }
}
