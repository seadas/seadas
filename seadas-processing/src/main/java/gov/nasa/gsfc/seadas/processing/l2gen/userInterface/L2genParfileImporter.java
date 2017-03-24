package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.common.SeadasGuiUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/6/12
 * Time: 4:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genParfileImporter {

        final private JButton jButton;
        private L2genData l2genData;
        final JFileChooser jFileChooser;

        L2genParfileImporter(L2genData l2genData) {
            this.l2genData = l2genData;

            String NAME = "Load Parameters";

            jButton = new JButton(NAME);

            jFileChooser = new JFileChooser();

            addControlListeners();
        }

        private void addControlListeners() {
            jButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String contents = SeadasGuiUtils.importFile(jFileChooser);
                    File parFileDir;
                    if(contents == null) {
                        parFileDir = null;
                    } else {
                        parFileDir = jFileChooser.getSelectedFile().getParentFile();
                    }
                    l2genData.setParString(contents, l2genData.isRetainCurrentIfile(), false, parFileDir);
                }
            });
        }

        public JButton getjButton() {
            return jButton;
        }
}
