package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import gov.nasa.gsfc.seadas.processing.l2gen.userInterface.L2genPrimaryIOFilesSelector;
import org.esa.beam.framework.datamodel.Product;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/6/12
 * Time: 5:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProgramUIFactory extends JPanel implements CloProgramUI {

    private L2genPrimaryIOFilesSelector ioFilesSelector;

    //private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    ProcessorModel processorModel;

    private ParFileUI parFileUI;

    public ProgramUIFactory(String programName, String xmlFileName, String multiIFile) {
        processorModel = ProcessorModel.valueOf(programName, xmlFileName);
        processorModel.setMultipleInputFiles(multiIFile.equals("true") ? true : false);
        parFileUI = new ParFileUI(processorModel);
        ioFilesSelector = new L2genPrimaryIOFilesSelector(processorModel);
        createUserInterface();
    }

    public ProgramUIFactory(String programName, String xmlFileName) {
        this(programName, xmlFileName, "false");
    }

    public ProcessorModel getProcessorModel() {
        return processorModel;
    }

    public Product getSelectedSourceProduct() {
        return ioFilesSelector.getIfileSelector().getSourceProductSelector().getSelectedProduct();
    }

    public boolean isOpenOutputInApp() {
        return parFileUI.isOpenOutputInApp();
    }

    public String getParamString() {
        return processorModel.getParamList().getParamString();
    }

    public void setParamString(String paramString) {
        processorModel.getParamList().setParamString(paramString);
    }

    protected void createUserInterface() {
        final JPanel ioPanel = ioFilesSelector.getjPanel();
        processorModel.addPropertyChangeListener("geofile", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (!processorModel.hasGeoFile() && ioPanel.getComponentCount() > 1) {
                    ioPanel.remove(1);
                } else {
                    ioPanel.add(ioFilesSelector.getGeofileSelector().getJPanel(),
                            new GridBagConstraintsCustom(0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL), 1);
                    Component[] c = ((JPanel) ioPanel.getComponent(1)).getComponents();
                    for (Component ci : c) {
                        ci.setEnabled(true);
                    }
                }
                ioPanel.repaint();
                ioPanel.validate();
            }
        });
        int primaryOutputIndex = 2;
        if (!processorModel.hasGeoFile()) {
            ioPanel.remove(1);
            primaryOutputIndex--;
        }
        if (!processorModel.hasPrimaryOutputFile()) {
            ioPanel.remove(primaryOutputIndex);
        }

        ioPanel.repaint();
        ioPanel.validate();

        //update processor model param info if there is an open product.
        if (ioFilesSelector.getIfileSelector().getSourceProductSelector().getSelectedProduct() != null) {
            processorModel.updateIFileInfo(ioFilesSelector.getIfileSelector().getSourceProductSelector().getSelectedProduct().getFileLocation().toString());
        }

        if (processorModel.getProgramName().indexOf("bin") != -1) {
            processorModel.addPropertyChangeListener(processorModel.getPrimaryInputFileOptionName(), new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    processorModel.updateIFileInfo(computeIFileOptionValue());
                }
            });
        }

        final JPanel parFilePanel = parFileUI.getParStringPanel();

        this.setLayout(new GridBagLayout());

        add(ioPanel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        add(getParamPanel(),
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));

        add(parFilePanel,
                new GridBagConstraintsCustom(0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));

        //setSize(getPreferredSize().width, getPreferredSize().height + 200);
        //setPreferredSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
    }

    public JPanel getParamPanel() {
        return new ParamUIFactory(processorModel).createParamPanel();
    }

    protected void disableJPanel(JPanel panel) {
        Component[] com = panel.getComponents();
        for (int a = 0; a < com.length; a++) {
            com[a].setEnabled(false);
        }
        panel.repaint();
        panel.validate();

    }

    protected void enableJPanel(JPanel panel) {
        Component[] com = panel.getComponents();
        for (int a = 0; a < com.length; a++) {
            com[a].setEnabled(true);
        }
    }

    private String computeIFileOptionValue() {
        String ifileOptionValue = "";
        if (hasMultipleFiles()) {
            ifileOptionValue = getSelectedFilesList();


        } else {
            ifileOptionValue = ioFilesSelector.getIfileSelector().getSelectedIFile().getAbsolutePath();
        }

        return ifileOptionValue;
    }

    private boolean hasMultipleFiles() {
        File[] selectedFiles = ioFilesSelector.getIfileSelector().getSourceProductSelector().getSelectedMultiFiles();
        if (selectedFiles != null && selectedFiles.length > 1) {
            return true;
        }
        return false;
    }

    public String getSelectedFilesList() {

        File fileListFile = new File(processorModel.getRootDir(), processorModel.getProgramName() + "_inputFiles.lst");

        File[] selectedFiles = ioFilesSelector.getIfileSelector().getSourceProductSelector().getSelectedMultiFiles();
        StringBuilder fileNames = new StringBuilder();
        for (File file : selectedFiles) {
            fileNames.append(file.getAbsolutePath() + "\n");
        }
        FileWriter fileWriter = null;
        try {

            fileWriter = new FileWriter(fileListFile);
            fileWriter.write(fileNames.toString());
            fileWriter.close();
            ioFilesSelector.getIfileSelector().getSourceProductSelector().setSelectedFile(fileListFile);
        } catch (IOException ioe) {
        }
        return fileListFile.getAbsolutePath();
    }

}
