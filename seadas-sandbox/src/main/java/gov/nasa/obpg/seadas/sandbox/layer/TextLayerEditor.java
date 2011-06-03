package gov.nasa.obpg.seadas.sandbox.layer;

import org.esa.beam.framework.ui.layer.AbstractLayerEditor;

import javax.swing.*;

/**
 * An editor for {@link TextLayer}s.
 *
 * @author Norman Fomferra
 * @since SeaDAS 7.0
 */
public class TextLayerEditor extends AbstractLayerEditor {
    @Override
    protected JComponent createControl() {
        return new JLabel("Dear SeaDAS user, you are looking at the TextLayer-editor.");
    }
}
