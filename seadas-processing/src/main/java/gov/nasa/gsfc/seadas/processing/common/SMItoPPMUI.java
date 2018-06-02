package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.AppContext;

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

        ppmFile = new FileSelector((AppContext) SnapApp.getDefault(), ParamInfo.Type.OFILE, "ppm file");
        ppmFile.getFileTextField().setColumns(20);

//        ppmFile.getFileTextField().addPropertyChangeListener(new PropertyChangeListener() {
//            @Override
//            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
//                ppmFile.getFileTextField().postActionEvent();
//            }
//        });

        ppmFile.addPropertyChangeListener(ppmFile.getPropertyName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (ppmFile.getFileName().trim().length() != 0) {
                    processorModel.setReadyToRun(processorModel.getParamValue(processorModel.getPrimaryOutputFileOptionName()).trim().length() > 0);
                } else {
                    processorModel.setReadyToRun(false);
                }
            }
        });

        processorModel.addPropertyChangeListener(processorModel.getPrimaryInputFileOptionName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (smitoppmCheckBox.isSelected()) {
                    ppmFile.setFilename(processorModel.getOcssw().getOfileName(processorModel.getParamValue(processorModel.getPrimaryOutputFileOptionName()), "smitoppm", null));
                    smitoppmPanel.add(ppmFile.getjPanel());
                    smitoppmPanel.validate();
                    smitoppmPanel.repaint();
                }
            }
        });

        processorModel.addPropertyChangeListener(processorModel.getPrimaryOutputFileOptionName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (smitoppmCheckBox.isSelected()) {
                    ppmFile.setFilename(processorModel.getOcssw().getOfileName(processorModel.getParamValue(processorModel.getPrimaryOutputFileOptionName()), "smitoppm", null));
                    smitoppmPanel.add(ppmFile.getjPanel());
                    smitoppmPanel.validate();
                    smitoppmPanel.repaint();
                }
            }
        });
        smitoppmPanel.add(smitoppmLabel);
        smitoppmPanel.add(smitoppmCheckBox);
        smitoppmCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (smitoppmCheckBox.isSelected()) {
                    ppmFile.setFilename(processorModel.getOcssw().getOfileName(processorModel.getParamValue(processorModel.getPrimaryOutputFileOptionName()), "smitoppm", null));
                    smitoppmPanel.add(ppmFile.getjPanel());
                    processorModel.setReadyToRun(processorModel.getParamValue(processorModel.getPrimaryOutputFileOptionName()).trim().length() > 0 && ppmFile.getFileName().trim().length() > 0);
                    processorModel.createsmitoppmProcessorModel(ppmFile.getFileName());
                    smitoppmPanel.validate();
                    smitoppmPanel.repaint();
                } else {
                    smitoppmPanel.remove(ppmFile.getjPanel());
                    smitoppmPanel.validate();
                    smitoppmPanel.repaint();
                    processorModel.setReadyToRun(processorModel.getParamValue(processorModel.getPrimaryOutputFileOptionName()).trim().length() > 0);
                    processorModel.setSecondaryProcessor(null);
                }

            }
        });

        return smitoppmPanel;
    }
}
