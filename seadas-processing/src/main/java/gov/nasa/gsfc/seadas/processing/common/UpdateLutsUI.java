package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class UpdateLutsUI extends JPanel implements CloProgramUI  {

    ProcessorModel processorModel;

    JPanel paramPanel;
    OCSSW ocssw;
    public UpdateLutsUI(String programName, String xmlFileName, OCSSW ocssw) {
        this.ocssw = ocssw;
        processorModel = ProcessorModel.valueOf(programName, xmlFileName, ocssw);
        createUserInterface();
    }

    protected void createUserInterface() {

        paramPanel = getParamPanel();

        this.setLayout(new GridBagLayout());

        add(paramPanel,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 3));


        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        getProcessorModel().setReadyToRun(true);
    }

    @Override
    public JPanel getParamPanel() {
        return new ParamUIFactory(processorModel).createParamPanel();
    }

    @Override
    public ProcessorModel getProcessorModel() {
        return processorModel;
    }

    @Override
    public File getSelectedSourceProduct() {
        return null;
    }

    @Override
    public boolean isOpenOutputInApp() {
        return false;
    }

    @Override
    public String getParamString() {
        return null;
    }

    @Override
    public void setParamString(String paramString) {

    }
}
