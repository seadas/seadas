package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/6/12
 * Time: 4:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genRetainIfileSpecifier {

        private final JCheckBox jCheckBox;
        private L2genData l2genData;
        private boolean controlHandlerEnabled = true;

        public L2genRetainIfileSpecifier(L2genData l2genData) {

            this.l2genData = l2genData;
            String NAME = "Retain Selected IFILE";
            String TOOL_TIP = "If an ifile is currently selected then any ifile entry in the parfile being opened will be ignored.";

            jCheckBox = new JCheckBox(NAME);

            jCheckBox.setSelected(l2genData.isRetainCurrentIfile());
            jCheckBox.setToolTipText(TOOL_TIP);


            addControlListeners();
            addEventListeners();
        }

        private void addControlListeners() {
            jCheckBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (isControlHandlerEnabled()) {
                        l2genData.setRetainCurrentIfile(jCheckBox.isSelected());
                    }
                }
            });
        }

        private void addEventListeners() {
            l2genData.addPropertyChangeListener(l2genData.RETAIN_IFILE, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    disableControlHandler();
                    jCheckBox.setSelected(l2genData.isRetainCurrentIfile());
                    enableControlHandler();
                }
            });

        }

        private boolean isControlHandlerEnabled() {
            return controlHandlerEnabled;
        }

        private void enableControlHandler() {
            controlHandlerEnabled = true;
        }

        private void disableControlHandler() {
            controlHandlerEnabled = false;
        }

        public JCheckBox getjCheckBox() {
            return jCheckBox;
        }
}
