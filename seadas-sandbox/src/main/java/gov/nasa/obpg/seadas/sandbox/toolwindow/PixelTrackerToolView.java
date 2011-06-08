package gov.nasa.obpg.seadas.sandbox.toolwindow;

import org.esa.beam.framework.ui.application.support.AbstractToolView;

import javax.swing.*;

/**
 * todo - Javadoc me!
 *
 * @author Danny Knowles
 */
public class PixelTrackerToolView extends AbstractToolView {

    public PixelTrackerToolView() {
    }

    @Override
    protected JComponent createControl() {
        PixelTracker pixelTracker = new PixelTracker();
        return pixelTracker.getContentPane();
    }

}
