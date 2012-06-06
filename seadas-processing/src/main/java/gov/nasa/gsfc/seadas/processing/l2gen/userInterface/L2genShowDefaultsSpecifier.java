package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/6/12
 * Time: 4:38 PM
 * To change this template use File | Settings | File Templates.
 */

public class L2genShowDefaultsSpecifier {

    private JCheckBox jCheckBox;
    private L2genData l2genData;
    private boolean controlHandlerEnabled = true;

    L2genShowDefaultsSpecifier(L2genData l2genData) {

        this.l2genData = l2genData;
        createJCheckBox();

        addControlListeners();
        addEventListeners();
    }


    private void createJCheckBox() {
        String NAME = "Show Defaults";
        String TOOL_TIP = "Displays all the defaults with the parfile text region";

        jCheckBox = new JCheckBox(NAME);

        jCheckBox.setSelected(l2genData.isShowDefaultsInParString());
        jCheckBox.setToolTipText(TOOL_TIP);
    }


    private void addControlListeners() {
        jCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (isControlHandlerEnabled()) {
                    l2genData.setShowDefaultsInParString(jCheckBox.isSelected());
                }
            }
        });
    }

    private void addEventListeners() {
        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jCheckBox.setEnabled(l2genData.isValidIfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.SHOW_DEFAULTS, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                disableControlHandler();
                jCheckBox.setSelected(l2genData.isShowDefaultsInParString());
                enableControlHandler();
            }
        });
    }

    private boolean isControlHandlerEnabled() {
        return controlHandlerEnabled;
    }

    private void enableControlHandler() {
        controlHandlerEnabled = true;
    }

    private void disableControlHandler() {
        controlHandlerEnabled = false;
    }

    public JCheckBox getjCheckBox() {
        return jCheckBox;
    }
}
