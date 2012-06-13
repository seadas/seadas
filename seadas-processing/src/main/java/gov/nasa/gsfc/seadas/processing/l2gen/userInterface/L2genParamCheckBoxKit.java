package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/13/12
 * Time: 10:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genParamCheckBoxKit extends AbstractComponentKit {

    private L2genData l2genData;
    private JCheckBox jCheckBox = new JCheckBox();

    public L2genParamCheckBoxKit(L2genData l2genData, String name, String toolTip) {
        super(name, toolTip);
        addComponent(jCheckBox);
        
        this.l2genData = l2genData;
        addEventListeners();
    }


    public void controlHandler() {
        if (l2genData != null) {
            l2genData.setParamValue(getName(), isComponentSelected());
        }
    }

    private void addEventListeners() {
        if (l2genData != null) {
            l2genData.addPropertyChangeListener(getName(), new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    setComponentSelected(l2genData.getBooleanParamValue(getName()), false);
                }
            });
        }
    }
}



