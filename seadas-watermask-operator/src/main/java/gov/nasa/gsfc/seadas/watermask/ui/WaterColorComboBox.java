package gov.nasa.gsfc.seadas.watermask.ui;

import com.jidesoft.combobox.ColorExComboBox;

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
    private ColorExComboBox colorExComboBox = new ColorExComboBox();

    public WaterColorComboBox(LandMasksData landMasksData) {

        this.landMasksData = landMasksData;

        jLabel = new JLabel("Color");
        jLabel.setToolTipText("Water mask color");

        colorExComboBox.setSelectedColor(landMasksData.getWaterMaskColor());
        colorExComboBox.setPreferredSize(colorExComboBox.getPreferredSize());

        addControlListeners();
    }


    private void addControlListeners() {

        colorExComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                landMasksData.setWaterMaskColor(colorExComboBox.getSelectedColor());
            }
        });
    }


    public JLabel getjLabel() {
        return jLabel;
    }

    public ColorExComboBox getColorExComboBox() {
        return colorExComboBox;
    }
}
