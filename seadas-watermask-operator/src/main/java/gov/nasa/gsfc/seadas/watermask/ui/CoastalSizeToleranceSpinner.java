package gov.nasa.gsfc.seadas.watermask.ui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Created by knowles on 5/25/17.
 */
public class CoastalSizeToleranceSpinner {
    private LandMasksData landMasksData;

    private JLabel jLabel;
    private JSpinner jSpinner = new JSpinner();

    public CoastalSizeToleranceSpinner(LandMasksData landMasksData) {

        this.landMasksData = landMasksData;

        jLabel = new JLabel("Coastal Size Tolerance");

        jLabel.setToolTipText("Coastal pixel grid size tolerance");

        jSpinner.setModel(new SpinnerNumberModel(100, 1, 100, 5));

        jSpinner.setPreferredSize(jSpinner.getPreferredSize());
        jSpinner.setSize(jSpinner.getPreferredSize());

        jSpinner.setModel(new SpinnerNumberModel(landMasksData.getCoastalSizeTolerance(), 1, 100, 5));

//        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) jSpinner.getEditor();
//        DecimalFormat format = editor.getFormat();
//        format.setMinimumFractionDigits(1);

        addControlListeners();
    }


    private void addControlListeners() {
        jSpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                int value =Integer.parseInt(jSpinner.getValue().toString() );
                landMasksData.setCoastalSizeTolerance(value);
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
