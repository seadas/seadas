package gov.nasa.gsfc.seadas.watermask.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 9/4/12
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class LandColorComboBox {
    private LandMasksData landMasksData;

    private JLabel jLabel;
    private JComboBox colorExComboBox = new JComboBox();

    public LandColorComboBox(LandMasksData landMasksData) {

        this.landMasksData = landMasksData;

        jLabel = new JLabel("Color");
        jLabel.setToolTipText("Land mask color");

        colorExComboBox.getEditor().getEditorComponent().setBackground((landMasksData.getLandMaskColor()));
        colorExComboBox.setPreferredSize(colorExComboBox.getPreferredSize());

        addControlListeners();
    }


    private void addControlListeners() {

        colorExComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                landMasksData.setLandMaskColor(colorExComboBox.getEditor().getEditorComponent().getBackground());
            }
        });
    }


    public JLabel getjLabel() {
        return jLabel;
    }

    public JComboBox getColorExComboBox() {
        return colorExComboBox;
    }
}
