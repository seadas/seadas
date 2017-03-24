package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ParamValidValueInfo;
import gov.nasa.gsfc.seadas.processing.common.GridBagConstraintsCustom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/12/12
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genParamBitwiseCheckboxGroup {
    private ParamInfo paramInfo;
    private L2genData l2genData;

    private JLabel jLabel;
    private JScrollPane jScrollPane;

    private boolean controlHandlerEnabled = true;

    public L2genParamBitwiseCheckboxGroup(L2genData l2genData, ParamInfo paramInfo) {

        this.l2genData = l2genData;
        this.paramInfo = paramInfo;

        jLabel = new JLabel(paramInfo.getName());
        jLabel.setToolTipText(paramInfo.getDescription());

        JPanel jPanel = new JPanel(new GridBagLayout());
        int gridy = 0;


        for (ParamValidValueInfo paramValidValueInfo : paramInfo.getValidValueInfos()) {
            if (paramValidValueInfo.getValue() != null && paramValidValueInfo.getValue().length() > 0) {

                L2genParamBitwiseCheckbox paramBitwiseCheckbox = new L2genParamBitwiseCheckbox(l2genData, paramInfo, paramValidValueInfo);

                jPanel.add(paramBitwiseCheckbox.getjCheckBox(),
                        new GridBagConstraintsCustom(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

                jPanel.add(paramBitwiseCheckbox.getjLabel(),
                        new GridBagConstraintsCustom(1, gridy, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

                gridy++;
            }
        }

        jScrollPane = new JScrollPane(jPanel);
    }

    public JLabel getjLabel() {
        return jLabel;
    }

    public JScrollPane getjScrollPane() {
        return jScrollPane;
    }


    private class L2genParamBitwiseCheckbox {

        private ParamInfo paramInfo;
        private L2genData l2genData;
        private ParamValidValueInfo paramValidValueInfo;

        private JLabel jLabel;
        private JCheckBox jCheckBox;


        public L2genParamBitwiseCheckbox(L2genData l2genData, ParamInfo paramInfo, ParamValidValueInfo paramValidValueInfo) {

            this.l2genData = l2genData;
            this.paramInfo = paramInfo;
            this.paramValidValueInfo = paramValidValueInfo;

            jCheckBox = new JCheckBox();


            jCheckBox.setSelected(paramInfo.isBitwiseSelected(paramValidValueInfo));

            jLabel = new JLabel(paramValidValueInfo.getValue() + " - " + paramValidValueInfo.getDescription());
            jLabel.setToolTipText(paramValidValueInfo.getValue() + " - " + paramValidValueInfo.getDescription());

            addControlListeners();
            addEventListeners();
        }

        private void addControlListeners() {
            jCheckBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    String currValueString = l2genData.getParamValue(paramInfo.getName());
                    int currValue = Integer.parseInt(currValueString);
                    String currValidValueString = paramValidValueInfo.getValue();
                    int currValidValue = Integer.parseInt(currValidValueString);
                    int newValue = currValue;

                    if (currValidValue > 0) {
                        if (jCheckBox.isSelected()) {

                            newValue = (currValue | currValidValue);
                        } else {

                            if ((currValue & currValidValue) > 0) {
                                newValue = currValue - currValidValue;
                            }
                        }
                    } else {
                        if (jCheckBox.isSelected()) {
                            newValue = 0;
                        } else {
                            if (isControlHandlerEnabled()) {
                                disableControlHandler();
                                l2genData.setParamToDefaults(paramInfo.getName());
                                enableControlHandler();
                            }
                            return;
                        }
                    }


                    String newValueString = Integer.toString(newValue);

                    if (isControlHandlerEnabled()) {
                        disableControlHandler();
                        l2genData.setParamValue(paramInfo.getName(), newValueString);
                        enableControlHandler();
                    }
                }
            });

        }

        private void addEventListeners() {
            l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {

                    int value = Integer.parseInt(paramValidValueInfo.getValue());

                    if (value > 0) {
                        jCheckBox.setSelected(paramInfo.isBitwiseSelected(paramValidValueInfo));
                    } else {
                        if (paramValidValueInfo.getValue().equals(l2genData.getParamValue(paramInfo.getName()))) {
                            jCheckBox.setSelected(true);
                        } else {
                            jCheckBox.setSelected(false);
                        }
                    }
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

        public JLabel getjLabel() {
            return jLabel;
        }

        public JCheckBox getjCheckBox() {
            return jCheckBox;
        }
    }
}
