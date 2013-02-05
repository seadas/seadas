package gov.nasa.gsfc.seadas.watermask.ui;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 9/4/12
 * Time: 10:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class CoastlineEnabledAllBandsCheckbox {
    private LandMasksData landMasksData;

    private JLabel jLabel;
    private JCheckBox jCheckBox = new JCheckBox();

    private static String DEFAULT_NAME = "Enabled in All Bands";
    private static String DEFAULT_TOOLTIPS = "Set Coastline Mask Enabled in All Bands";

    public CoastlineEnabledAllBandsCheckbox(LandMasksData landMasksData) {


        this.landMasksData = landMasksData;

        jLabel = new JLabel(DEFAULT_NAME);
        jLabel.setToolTipText(DEFAULT_TOOLTIPS);
        jCheckBox.setSelected(landMasksData.isShowCoastlineMaskAllBands());

        addControlListeners();
    }

    private void addControlListeners() {
        jCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                landMasksData.setShowCoastlineMaskAllBands(jCheckBox.isSelected());

            }
        });
    }


    public JLabel getjLabel() {
        return jLabel;
    }

    public JCheckBox getjCheckBox() {
        return jCheckBox;
    }
}
