package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import gov.nasa.gsfc.seadas.processing.l2gen.userInterface.L2genPrimaryIOFilesSelector;
import org.esa.beam.framework.datamodel.Product;

import javax.swing.*;
import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
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

    private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    ProcessorModel processorModel;

    private ParFileUI parFileUI;

    public ProgramUIFactory(String programName, String xmlFileName) {
        processorModel = new ProcessorModel(programName, xmlFileName);
        parFileUI = new ParFileUI(processorModel);
        ioFilesSelector = new L2genPrimaryIOFilesSelector(processorModel);

        createUserInterface();
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

    public String getFileList() {
        File[] selectedFiles = ioFilesSelector.getIfileSelector().getSourceProductSelector().getSelectedMultiFiles();
        StringBuilder fileNames = new StringBuilder();
        for (File file : selectedFiles) {
            fileNames.append(file.getAbsolutePath() + "\n");
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(new File(ioFilesSelector.getIfileSelector().getSourceProductSelector().getCurrentDirectory(), processorModel.getProgramName() + "_ifiles.lst"));
            fileWriter.write(fileNames.toString());
            if (fileWriter != null) {
                fileWriter.close();
                return processorModel.getProgramName() + "_ifiles.lst";
            }
        } catch (IOException ioe) {
        }
        return null;
    }

    protected void createUserInterface() {
        JPanel ioPanel = ioFilesSelector.getjPanel();
        if (!processorModel.hasGeoFile()) {
            ioPanel.remove(1);
        } else if (!processorModel.hasPrimaryOutputFile()) {
            ioPanel.remove(2);
        }

        ioPanel.repaint();
        ioPanel.validate();

        final JPanel parFilePanel = parFileUI.getParStringPanel();

        this.setLayout(new GridBagLayout());

        add(ioPanel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        add(getParamPanel(),
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));

        add(parFilePanel,
                new GridBagConstraintsCustom(0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        setPreferredSize(getPreferredSize());
        setSize(getPreferredSize().width, getPreferredSize().height + 200);
    }

    protected JPanel getParamPanel() {
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


}
