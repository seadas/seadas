package gov.nasa.gsfc.seadas.watermask.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 9/4/12
 * Time: 11:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class WaterColorComboBox {    private LandMasksData landMasksData;

    private JLabel jLabel;
    private JComboBox colorExComboBox = new JComboBox();

    public WaterColorComboBox(LandMasksData landMasksData) {

        this.landMasksData = landMasksData;

        jLabel = new JLabel("Color");
        jLabel.setToolTipText("Water mask color");
        colorExComboBox.getEditor().getEditorComponent().setBackground((landMasksData.getWaterMaskColor()));
        colorExComboBox.setPreferredSize(colorExComboBox.getPreferredSize());

        addControlListeners();
    }


    private void addControlListeners() {

        colorExComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                landMasksData.setWaterMaskColor(colorExComboBox.getEditor().getEditorComponent().getBackground());
            }
        });
    }


    public JLabel getjLabel() {
        return jLabel;
    }

    public JComboBox  getColorExComboBox() {
        return colorExComboBox;
    }
}
