package gov.nasa.gsfc.seadas.bathymetry.ui;

import com.jidesoft.combobox.ColorExComboBox;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 9/4/12
 * Time: 10:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class MaskColorComboBox {
    private BathymetryData bathymetryData;

    private JLabel jLabel;
    private ColorExComboBox colorExComboBox = new ColorExComboBox();

    public MaskColorComboBox(BathymetryData bathymetryData) {

        this.bathymetryData = bathymetryData;

        jLabel = new JLabel("Mask Color");
        jLabel.setToolTipText("set mask color");

        colorExComboBox.setSelectedColor(bathymetryData.getMaskColor());
        colorExComboBox.setPreferredSize(colorExComboBox.getPreferredSize());

        addControlListeners();
    }


    private void addControlListeners() {

        colorExComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                bathymetryData.setMaskColor(colorExComboBox.getSelectedColor());
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
