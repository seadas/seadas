package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.general.GridBagConstraintsCustom;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/14/12
 * Time: 9:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genMainPanel {

    private JPanel jPanel;
    private int tabIndex;

    private L2genPrimaryIOFilesSelector primaryIOFilesSelector;
    private L2genParfilePanel parfileParStringSpecifier;

    L2genMainPanel(L2genData l2genData, int tabIndex) {

        this.tabIndex = tabIndex;
        primaryIOFilesSelector = new L2genPrimaryIOFilesSelector(l2genData);
        parfileParStringSpecifier = new L2genParfilePanel(l2genData, tabIndex);

        createJPanel();
    }


    private void createJPanel() {
        jPanel = new JPanel(new GridBagLayout());
        jPanel.add(primaryIOFilesSelector.getjPanel(),
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        jPanel.add(parfileParStringSpecifier.getjPanel(),
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 3));
    }


    public L2genPrimaryIOFilesSelector getPrimaryIOFilesSelector() {
        return primaryIOFilesSelector;
    }

    public L2genParfilePanel getParfileParStringSpecifier() {
        return parfileParStringSpecifier;
    }

    public JPanel getjPanel() {
        return jPanel;
    }

    public int getTabIndex() {
        return tabIndex;
    }
}
