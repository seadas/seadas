package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.core.L2genDataProcessorModel;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.common.FileSelector;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/6/12
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genGeofileSelector {

    final private L2genDataProcessorModel l2genDataProcessorModel;

    final private FileSelector fileSelector;
    private boolean controlHandlerEnabled = true;

    public L2genGeofileSelector(L2genDataProcessorModel  l2genDataProcessorModel) {
        this.l2genDataProcessorModel = l2genDataProcessorModel;

        fileSelector = new FileSelector(VisatApp.getApp(), ParamInfo.Type.IFILE, L2genData.GEOFILE);
        addControlListeners();
        addEventListeners();
    }

    private void addControlListeners() {
        fileSelector.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (isControlHandlerEnabled()) {
                    l2genDataProcessorModel.setParamValue(L2genData.GEOFILE, fileSelector.getFileName());
                }
            }
        });
    }

    private void addEventListeners() {
        l2genDataProcessorModel.addPropertyChangeListener(L2genData.GEOFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                disableControlHandler();
                fileSelector.setFilename(l2genDataProcessorModel.getParamValue(L2genData.GEOFILE));
                enableControlHandler();
            }
        });

        l2genDataProcessorModel.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                fileSelector.setEnabled(l2genDataProcessorModel.isValidIfile() && l2genDataProcessorModel.isGeofileRequired());
                fileSelector.setVisible(l2genDataProcessorModel.isValidIfile() && l2genDataProcessorModel.isGeofileRequired());
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
