package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ParamList;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.l2gen.userInterface.L2genPrimaryIOFilesSelector;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/6/12
 * Time: 5:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProgramUIFactory extends JPanel implements CloProgramUI {

    private L2genPrimaryIOFilesSelector ioFilesSelector;

    ProcessorModel processorModel;

    private ParFileUI parFileUI;
    private JPanel paramPanel;
    OCSSW ocssw;

    public ProgramUIFactory(String programName, String xmlFileName, OCSSW ocssw) {
        this.ocssw = ocssw;
        processorModel = ProcessorModel.valueOf(programName, xmlFileName, ocssw);
        parFileUI = new ParFileUI(processorModel);
        ioFilesSelector = new L2genPrimaryIOFilesSelector(processorModel);
        createUserInterface();
    }

    public ProcessorModel getProcessorModel() {
        return processorModel;
    }

    public File getSelectedSourceProduct() {
        return ioFilesSelector.getIfileSelector().getFileSelector().getSelectedFile();
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
        if (ioFilesSelector.getIfileSelector().getFileSelector().getSelectedFile() != null) {
            processorModel.updateIFileInfo(ioFilesSelector.getIfileSelector().getSelectedIFileName());
            processorModel.updateParamValues(ioFilesSelector.getIfileSelector().getSelectedIFile());
        }

        final JPanel parFilePanel = parFileUI.getParStringPanel();

        paramPanel = getParamPanel();

        ParamList paramList = processorModel.getParamList();
        ArrayList<ParamInfo> paramInfos = paramList.getParamArray();
        for (ParamInfo pi : paramInfos) {
            //when ifile or infile changes, the values of some parameters may change.
            //ofile and geofile should not affect the param values
            if (!(pi.getType().equals("ifile") || pi.getType().equals("infile") || pi.getType().equals("ofile") || pi.getType().equals("geofile"))) {
                processorModel.addPropertyChangeListener(pi.getName(), new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                        paramPanel = getParamPanel();
                        paramPanel.repaint();
                        paramPanel.validate();
                        remove(1);
                        add(paramPanel,
                                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
                        add(parFilePanel,
                                        new GridBagConstraintsCustom(0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));

                        revalidate();
                        repaint();
                    }
                });
            }
        }

        this.setLayout(new GridBagLayout());

        add(ioPanel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        add(paramPanel,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));

        add(parFilePanel,
                new GridBagConstraintsCustom(0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));

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
}
