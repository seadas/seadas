package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.general.GridBagConstraintsCustom;
import gov.nasa.gsfc.seadas.processing.general.SeadasGuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/11/12
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */

public class L2genParfilePanel {

    private JPanel jPanel;

    private L2genData l2genData;
    private int tabIndex;

    private L2genParfileImporter parfileImporter;
    private L2genParfileExporter parfileExporter;
    private L2genRetainIfileSpecifier retainIfileSpecifier;
    private L2genGetAncillaryFilesSpecifier getAncillaryFilesSpecifier;
    private L2genShowDefaultsSpecifier showDefaultsSpecifier;
    private L2genParStringSpecifier parStringSpecifier;

    L2genParfilePanel(L2genData l2genData, int tabIndex) {

        this.l2genData = l2genData;
        this.tabIndex = tabIndex;

        parfileImporter = new L2genParfileImporter(l2genData);
        parfileExporter = new L2genParfileExporter(l2genData);
        retainIfileSpecifier = new L2genRetainIfileSpecifier(l2genData);
        getAncillaryFilesSpecifier = new L2genGetAncillaryFilesSpecifier(l2genData);
        showDefaultsSpecifier = new L2genShowDefaultsSpecifier(l2genData);
        parStringSpecifier = new L2genParStringSpecifier(l2genData, tabIndex);

        createJPanel();
    }


    public void createJPanel() {

        jPanel = new JPanel(new GridBagLayout());
        jPanel.setBorder(BorderFactory.createTitledBorder("Parfile"));


        final JPanel openButtonRetainPanel = new JPanel(new GridBagLayout());
        openButtonRetainPanel.setBorder(BorderFactory.createEtchedBorder());
        openButtonRetainPanel.add(parfileImporter.getjButton(),
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
        openButtonRetainPanel.add(retainIfileSpecifier.getjCheckBox(),
                new GridBagConstraintsCustom(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));


        final JPanel subPanel = new JPanel(new GridBagLayout());
        subPanel.add(openButtonRetainPanel,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        subPanel.add(getAncillaryFilesSpecifier.getjButton(),
                new GridBagConstraintsCustom(1, 0, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        subPanel.add(showDefaultsSpecifier.getjCheckBox(),
                new GridBagConstraintsCustom(2, 0, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        subPanel.add(parfileExporter.getjButton(),
                new GridBagConstraintsCustom(3, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));


        jPanel.add(subPanel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        jPanel.add(new JScrollPane(parStringSpecifier.getjTextArea()),
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH));
    }

    public JPanel getjPanel() {
        return jPanel;
    }
}

