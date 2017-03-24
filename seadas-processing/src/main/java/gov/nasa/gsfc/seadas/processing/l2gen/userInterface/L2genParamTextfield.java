package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/12/12
 * Time: 4:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genParamTextfield {

    private ParamInfo paramInfo;
    private L2genData l2genData;

    private JLabel jLabel;
    private JTextField jTextField;

    private int fill;

    public L2genParamTextfield(L2genData l2genData, ParamInfo paramInfo) {

        this.l2genData = l2genData;
        this.paramInfo = paramInfo;


        final String PROTOTYPE_70 = buildStringPrototype(70);
        final String PROTOTYPE_60 = buildStringPrototype(60);
        final String PROTOTYPE_15 = buildStringPrototype(15);

        String textfieldPrototype;

        if (paramInfo.getType() == ParamInfo.Type.STRING) {
            textfieldPrototype = PROTOTYPE_60;
            fill = GridBagConstraints.NONE;
        } else if (paramInfo.getType() == ParamInfo.Type.INT) {
            textfieldPrototype = PROTOTYPE_15;
            fill = GridBagConstraints.NONE;
        } else if (paramInfo.getType() == ParamInfo.Type.FLOAT) {
            textfieldPrototype = buildStringPrototype(60);
            fill = GridBagConstraints.NONE;
        } else if (paramInfo.getType() == ParamInfo.Type.IFILE) {
            textfieldPrototype = PROTOTYPE_70;
            fill = GridBagConstraints.HORIZONTAL;
        } else if (paramInfo.getType() == ParamInfo.Type.OFILE) {
            textfieldPrototype = PROTOTYPE_70;
            fill = GridBagConstraints.HORIZONTAL;
        } else {
            textfieldPrototype = PROTOTYPE_70;
            fill = GridBagConstraints.NONE;
        }


        jTextField = new JTextField(textfieldPrototype);
        jTextField.setPreferredSize(jTextField.getPreferredSize());
        jTextField.setMaximumSize(jTextField.getPreferredSize());
        jTextField.setMinimumSize(jTextField.getPreferredSize());
        jTextField.setText("");

        jLabel = new JLabel(paramInfo.getName());
        jLabel.setToolTipText(paramInfo.getDescription());

        addControlListeners();
        addEventListeners();
    }

    private void addControlListeners() {
        jTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                l2genData.setParamValue(paramInfo.getName(), jTextField.getText().toString());
            }
        });

        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.setParamValue(paramInfo.getName(), jTextField.getText().toString());
            }
        });
    }

    private void addEventListeners() {
        l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jTextField.setText(l2genData.getParamValue(paramInfo.getName()));
            }
        });
    }

    private String buildStringPrototype(int size) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < size; i++) {
            stringBuilder.append("p");
        }

        return stringBuilder.toString();
    }

    public JLabel getjLabel() {
        return jLabel;
    }

    public JTextField getjTextField() {
        return jTextField;
    }

    public int getFill() {
        return fill;
    }
}
