package gov.nasa.gsfc.seadas.sandbox.l1gen;

import com.bc.ceres.swing.selection.AbstractSelectionChangeListener;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.ui.SourceProductSelector;
import org.esa.beam.framework.gpf.ui.TargetProductSelector;
import org.esa.beam.framework.ui.AppContext;

import javax.swing.*;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class L1genForm extends JPanel {

    private final AppContext appContext;
    private JButton button;

    public L1genForm(AppContext appContext) {
        this.appContext = appContext;


        createUI();
    }

    private void createUI() {
        button = new JButton("The Button");
        add(button);

    }


    public Object getSourceProduct() {
        return null;
    }
}
