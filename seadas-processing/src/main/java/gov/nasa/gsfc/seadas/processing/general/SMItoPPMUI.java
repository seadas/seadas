package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/7/12
 * Time: 9:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class SMItoPPMUI {

    private JCheckBox smitoppmCheckBox;
    private FileSelector ppmFile;
    private ProcessorModel processorModel;

    public SMItoPPMUI(ProcessorModel pm) {
        processorModel = pm;
    }

    public JPanel getSMItoPPMPanel() {
        return createSmiToPpmPanel();
    }

    private JPanel createSmiToPpmPanel() {
        final JPanel smitoppmPanel = new JPanel();
        smitoppmPanel.setLayout(new FlowLayout());

        final String smitoppmLabelName = "smitoppm";

        JLabel smitoppmLabel = new JLabel(smitoppmLabelName);

        smitoppmCheckBox = new JCheckBox();
        smitoppmCheckBox.setSelected(false);

        ppmFile = new FileSelector(VisatApp.getApp(), FileSelector.Type.OFILE, "ppm file");
        ppmFile.getFileTextField().setColumns(20);
        ppmFile.addPropertyChangeListener(ppmFile.getPropertyName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (ppmFile.getFileName() != null) {
                    processorModel.setReadyToRun(true);
                }
            }
        });
        smitoppmPanel.add(smitoppmLabel);
        smitoppmPanel.add(smitoppmCheckBox);
        smitoppmCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (smitoppmCheckBox.isSelected()) {
                    smitoppmPanel.add(ppmFile.getjPanel());
                    processorModel.setReadyToRun(false);
                    smitoppmPanel.validate();
                    smitoppmPanel.repaint();
                } else {
                    smitoppmPanel.remove(ppmFile.getjPanel());
                    smitoppmPanel.validate();
                    smitoppmPanel.repaint();
                }

            }
        });

        return smitoppmPanel;
    }
}
