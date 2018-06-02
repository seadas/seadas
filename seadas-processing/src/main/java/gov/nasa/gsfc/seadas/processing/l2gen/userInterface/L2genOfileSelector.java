package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.SeaDASProcessorModel;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.common.FileSelector;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.esa.snap.rcp.SnapApp;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/6/12
 * Time: 11:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genOfileSelector {
    public static final String DEFAULT_OUTPUT_FILE_OPTION_NAME = "ofile";

    final private SeaDASProcessorModel seaDASProcessorModel;
    final private FileSelector fileSelector;
    private boolean controlHandlerEnabled = true;
    private String outputFileOptionName;

    public L2genOfileSelector(SeaDASProcessorModel seaDASProcessorModel) {
        this.seaDASProcessorModel = seaDASProcessorModel;
        outputFileOptionName = seaDASProcessorModel.getPrimaryOutputFileOptionName();
        if(outputFileOptionName == null) {
            outputFileOptionName = DEFAULT_OUTPUT_FILE_OPTION_NAME;
        } else {
            outputFileOptionName = outputFileOptionName.replaceAll("--", "");
        }
        fileSelector = new FileSelector(SnapApp.getDefault().getAppContext(), ParamInfo.Type.OFILE, outputFileOptionName);

        addControlListeners();
        addEventListeners();
    }

    private void addControlListeners() {
        fileSelector.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (isControlHandlerEnabled()) {
                    seaDASProcessorModel.setParamValue(seaDASProcessorModel.getPrimaryOutputFileOptionName(), fileSelector.getFileName());
                }
            }
        });

    }

    private void addEventListeners() {
        seaDASProcessorModel.addPropertyChangeListener(seaDASProcessorModel.getPrimaryOutputFileOptionName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                disableControlHandler();
                fileSelector.setFilename(seaDASProcessorModel.getParamValue(seaDASProcessorModel.getPrimaryOutputFileOptionName()));
                enableControlHandler();
            }
        });

        seaDASProcessorModel.addPropertyChangeListener(seaDASProcessorModel.getPrimaryOutputFileOptionName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                fileSelector.setEnabled(seaDASProcessorModel.isValidIfile());
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


    public JPanel getJPanel() {
        return fileSelector.getjPanel();
    }

    public FileSelector getFileSelector() {
        return fileSelector;
    }
}
