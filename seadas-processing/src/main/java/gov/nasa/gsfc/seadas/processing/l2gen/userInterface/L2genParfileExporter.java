package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.common.SeadasGuiUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/6/12
 * Time: 4:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genParfileExporter {

        final private JButton jButton;
        private L2genData l2genData;
        final JFileChooser jFileChooser;

        public L2genParfileExporter(L2genData l2genData) {
            this.l2genData = l2genData;

            String NAME = "Save Parameters";
            jButton = new JButton(NAME);
            jFileChooser = new JFileChooser();

            addControlListeners();
            addEventListeners();
        }

        private void addControlListeners() {
            jButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String contents = l2genData.getParString(false);
                    SeadasGuiUtils.exportFile(jFileChooser, contents);
                }
            });

        }


        private void addEventListeners() {
            l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    jButton.setEnabled(l2genData.isValidIfile());
                }
            });
        }

        public JButton getjButton() {
            return jButton;
        }
}
