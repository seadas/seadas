package gov.nasa.obpg.seadas.sandbox.toolwindow;

import org.esa.beam.framework.ui.application.support.AbstractToolView;

import javax.swing.*;

/**
 * todo - Javadoc me!
 *
 * @author Danny Knowles
 */
public class PixelInfoToolView extends AbstractToolView {

    public PixelInfoToolView() {
    }

    @Override
    protected JComponent createControl() {
        PixelInfo pixelInfo = new PixelInfo();
        return pixelInfo.getContentPane();
    }

}
