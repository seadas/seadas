package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/6/12
 * Time: 4:37 PM
 * To change this template use File | Settings | File Templates.
 */

public class L2genGetAncillaryFilesSpecifier {

    private JButton jButton;
    private L2genData l2genData;

    public L2genGetAncillaryFilesSpecifier(L2genData l2genData) {

        this.l2genData = l2genData;
        String NAME = "Get Ancillary";

        jButton = new JButton(NAME);

        addControlListeners();
        addEventListeners();
    }

    private void addControlListeners() {
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.setAncillaryFiles(false,false,false);
            }
        });

    }

    private void addEventListeners() {
        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jButton.setEnabled(l2genData.isValidIfile());
            }
        });

    }

    public JButton getjButton() {
        return jButton;
    }
}
