package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/12/12
 * Time: 4:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genParamDefaultIndicator {

    private final String DEFAULT_INDICATOR_TOOLTIP = "* Identicates that the selection is not the default value";
    private final String DEFAULT_INDICATOR_LABEL_ON = " *  ";
    private final String DEFAULT_INDICATOR_LABEL_OFF = "     ";

    private L2genData l2genData;
    private ParamInfo paramInfo;

    private JLabel jLabel = new JLabel();

    public L2genParamDefaultIndicator(L2genData l2genData, ParamInfo paramInfo) {

        this.l2genData = l2genData;
        this.paramInfo = paramInfo;

        addEventListeners();
    }


    private void addEventListeners() {
        l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                setDefaultIndicator();
            }
        });
    }

    private void setDefaultIndicator() {
        if (l2genData.isParamDefault(paramInfo.getName())) {
            jLabel.setText(DEFAULT_INDICATOR_LABEL_OFF);
            jLabel.setToolTipText("");
        } else {
            jLabel.setText(DEFAULT_INDICATOR_LABEL_ON);
            jLabel.setToolTipText(DEFAULT_INDICATOR_TOOLTIP);
        }
    }

    public JLabel getjLabel() {
        return jLabel;
    }

}
