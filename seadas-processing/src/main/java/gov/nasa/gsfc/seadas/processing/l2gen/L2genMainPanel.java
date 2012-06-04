package gov.nasa.gsfc.seadas.processing.l2gen;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.general.GridBagConstraintsCustom;
import org.esa.beam.framework.datamodel.Product;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/14/12
 * Time: 9:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genMainPanel extends JPanel {

    private final L2genForm l2genForm;
    private final L2genData l2genData;

    private int tabIndex;

    private L2genInputOutputPanel l2genInputOutputPanel;
    private L2genParfilePanel l2genParfilePanel;


    L2genMainPanel(L2genForm l2genForm, int tabIndex) {

        this.l2genForm = l2genForm;
        this.l2genData = l2genForm.getL2genData();
        this.tabIndex = tabIndex;

        initComponents();
        addComponents();
    }

    private void initComponents() {
        l2genInputOutputPanel = new L2genInputOutputPanel(l2genData);
        l2genParfilePanel = new L2genParfilePanel(l2genForm, tabIndex);
    }

    private void addComponents() {

        setLayout(new GridBagLayout());

        add(l2genInputOutputPanel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));

        add(l2genParfilePanel,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 3));

    }


    public Product getSelectedProduct() {
        if (l2genInputOutputPanel != null) {
            return l2genInputOutputPanel.getSelectedProduct();
        }

        return null;
    }


    public void prepareShow() {
        if (l2genInputOutputPanel != null) {
            l2genInputOutputPanel.prepareShow();
        }
    }

    public void prepareHide() {
        if (l2genInputOutputPanel != null) {
            l2genInputOutputPanel.prepareHide();
        }
    }
}
