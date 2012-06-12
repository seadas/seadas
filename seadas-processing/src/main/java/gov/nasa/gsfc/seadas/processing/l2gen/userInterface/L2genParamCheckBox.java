package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/12/12
 * Time: 4:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genParamCheckBox {

    private ParamInfo paramInfo;
    private L2genData l2genData;

    private JLabel jLabel;
    private JCheckBox jCheckBox = new JCheckBox();

    public L2genParamCheckBox(L2genData l2genData, ParamInfo paramInfo) {

        this.l2genData = l2genData;
        this.paramInfo = paramInfo;

        jLabel = new JLabel(paramInfo.getName());
        jLabel.setToolTipText(paramInfo.getDescription());
        jCheckBox.setName(paramInfo.getName());

        addControlListeners();
        addEventListeners();
    }

    private void addControlListeners() {
        jCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                l2genData.setParamValue(paramInfo.getName(), jCheckBox.isSelected());
            }
        });
    }

    private void addEventListeners() {
        l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jCheckBox.setSelected(l2genData.getBooleanParamValue(paramInfo.getName()));
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
