package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/6/12
 * Time: 4:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genParStringSpecifier {

    private final JTextArea jTextArea = new JTextArea();
    private L2genData l2genData;
    private int tabIndex;


    L2genParStringSpecifier(L2genData l2genData, int tabIndex) {

        this.l2genData = l2genData;
        this.tabIndex = tabIndex;

        jTextArea.setEditable(true);
        jTextArea.setAutoscrolls(true);

        addControlListeners();
        addEventListeners();
    }

    private void addControlListeners() {
        jTextArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                controlHandler();
            }
        });
    }

    private void addEventListeners() {
        for (ParamInfo paramInfo : l2genData.getParamInfos()) {
            final String eventName = paramInfo.getName();
            l2genData.addPropertyChangeListener(eventName, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    // relay this to PARSTRING event in case it is currently disabled
                    l2genData.fireEvent(L2genData.PARSTRING);
                }
            });
        }

        l2genData.addPropertyChangeListener(L2genData.SHOW_DEFAULTS, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // relay this to PARSTRING event in case it is currently disabled
                l2genData.fireEvent(L2genData.PARSTRING);
            }
        });


        l2genData.addPropertyChangeListener(L2genData.PARSTRING, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                eventHandler();
            }
        });


        l2genData.addPropertyChangeListener(L2genData.TAB_CHANGE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                tabChangeEventHandler(evt);
            }
        });


    }


    private void tabChangeEventHandler(PropertyChangeEvent evt) {
        if (evt.getNewValue().equals(tabIndex) && !evt.getOldValue().equals(tabIndex)) {
            l2genData.enableEvent(L2genData.PARSTRING);
        } else if (!evt.getNewValue().equals(tabIndex) && evt.getOldValue().equals(tabIndex)) {
            l2genData.disableEvent(L2genData.PARSTRING);
        }
    }


    private void controlHandler() {

        l2genData.setParString(jTextArea.getText(), false);
    }


    private void eventHandler() {
        jTextArea.setText(l2genData.getParString());
    }

    public JTextArea getjTextArea() {
        return jTextArea;
    }
}

