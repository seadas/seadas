package gov.nasa.obpg.seadas.sandbox.toolwindow;

import org.esa.beam.framework.ui.application.support.AbstractToolView;

import javax.swing.*;

/**
 * todo - Javadoc me!
 *
 * @author Danny Knowles
 */
public class TabbedPaneWithPanelsToolView extends AbstractToolView {

    public TabbedPaneWithPanelsToolView() {
    }

    @Override
    protected JComponent createControl() {
        TabbedPaneWithPanels tabbedPane = new TabbedPaneWithPanels();
        return tabbedPane.getContentPane();
    }
}
