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
 * Time: 12:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class MaskMinDepthTextfield {
    private BathymetryData bathymetryData;

    private JLabel jLabel;
    private JTextField jTextField = new JTextField();

    public MaskMinDepthTextfield(BathymetryData bathymetryData) {

        this.bathymetryData = bathymetryData;

        jLabel = new JLabel("Min Depth");
        jLabel.setToolTipText("set minimum depth");

        getjTextField().setText(bathymetryData.getMaskMaxDepthString());
        getjTextField().setPreferredSize(getjTextField().getPreferredSize());
        getjTextField().setMinimumSize(getjTextField().getPreferredSize());
        getjTextField().setMaximumSize(getjTextField().getPreferredSize());
        getjTextField().setText(bathymetryData.getMaskMinDepthString());

        addControlListeners();
    }


    private void addControlListeners() {
        jTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                bathymetryData.setMaskMinDepth(jTextField.getText().toString());
            }
        });

        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bathymetryData.setMaskMinDepth(jTextField.getText().toString());
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
