package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 12/13/12
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genSuiteComboBox {
    private L2genData l2genData;

    private JLabel jLabel;
    private JComboBox jComboBox;

    private boolean controlHandlerEnabled = true;

    public L2genSuiteComboBox(L2genData l2genData) {

        this.l2genData = l2genData;

        jComboBox = new JComboBox();

        jLabel = new JLabel("Suite");

        addControlListeners();
        addEventListeners();
    }


    private void addControlListeners() {
        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jComboBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isControlHandlerEnabled()) {
                            l2genData.setParamValue(L2genData.SUITE, (String) jComboBox.getSelectedItem());
                        }
                    }
                });
            }
        });
    }

    private void addEventListeners() {
        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                disableControlHandler();
                String[] suiteList = l2genData.getSuiteList();
                if (suiteList != null) {
                    jLabel.setEnabled(true);
                    jComboBox.setEnabled(true);

                    jComboBox.setModel(new DefaultComboBoxModel(l2genData.getSuiteList()));
                } else {
                    jLabel.setEnabled(false);
                    jComboBox.setEnabled(false);

                }
                jComboBox.setSelectedItem(l2genData.getParamValue(L2genData.SUITE));
                enableControlHandler();

            }
        });
    }


    public JLabel getjLabel() {
        return jLabel;
    }

    public JComboBox getjComboBox() {
        return jComboBox;
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


}
