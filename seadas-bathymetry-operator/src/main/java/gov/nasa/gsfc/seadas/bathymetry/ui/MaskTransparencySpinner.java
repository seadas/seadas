package gov.nasa.gsfc.seadas.bathymetry.ui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.text.DecimalFormat;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 9/4/12
 * Time: 9:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class MaskTransparencySpinner {

    private BathymetryData bathymetryData;

    private JLabel jLabel;
    private JSpinner jSpinner = new JSpinner();

    public MaskTransparencySpinner(BathymetryData bathymetryData) {

        this.bathymetryData = bathymetryData;

        jLabel = new JLabel("Mask Transparency");
        jLabel.setToolTipText("set mask transparency");

        jSpinner.setModel(new SpinnerNumberModel(100, 0, 100, 100));

        jSpinner.setPreferredSize(jSpinner.getPreferredSize());
        jSpinner.setSize(jSpinner.getPreferredSize());

        jSpinner.setModel(new SpinnerNumberModel(bathymetryData.getMaskTransparency(), 0.0, 1.0, 0.1));

        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) jSpinner.getEditor();
        DecimalFormat format = editor.getFormat();
        format.setMinimumFractionDigits(1);

        addControlListeners();
    }


    private void addControlListeners() {
        jSpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                bathymetryData.setMaskTransparency((Double) jSpinner.getValue());
            }
        });
    }


    public JLabel getjLabel() {
        return jLabel;
    }

    public JSpinner getjSpinner() {
        return jSpinner;
    }
}

