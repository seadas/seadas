package gov.nasa.gsfc.seadas.bathymetry.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 6/14/13
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class MaskMaxDepthTextfield {
    private BathymetryData bathymetryData;

    private JLabel jLabel;
    private JTextField jTextField = new JTextField();

    public MaskMaxDepthTextfield(BathymetryData bathymetryData) {

        this.bathymetryData = bathymetryData;

        jLabel = new JLabel("Max Depth");
        jLabel.setToolTipText("set maximum depth");

        getjTextField().setText(bathymetryData.getMaskMaxDepthString());
        getjTextField().setPreferredSize(getjTextField().getPreferredSize());
        getjTextField().setMinimumSize(getjTextField().getPreferredSize());
        getjTextField().setMaximumSize(getjTextField().getPreferredSize());

        addControlListeners();
    }


    private void addControlListeners() {
        jTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                bathymetryData.setMaskMaxDepth(jTextField.getText().toString());
            }
        });

        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bathymetryData.setMaskMaxDepth(jTextField.getText().toString());
            }
        });
    }


    public JLabel getjLabel() {
        return jLabel;
    }


    public JTextField getjTextField() {
        return jTextField;
    }
}
