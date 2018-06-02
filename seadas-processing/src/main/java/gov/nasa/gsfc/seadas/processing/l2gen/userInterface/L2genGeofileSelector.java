package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
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
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genGeofileSelector {

    final private SeaDASProcessorModel seaDASProcessorModel;

    final private FileSelector fileSelector;
    private boolean controlHandlerEnabled = true;

    public L2genGeofileSelector(SeaDASProcessorModel seaDASProcessorModel) {
        this.seaDASProcessorModel = seaDASProcessorModel;

        fileSelector = new FileSelector(SnapApp.getDefault().getAppContext(), ParamInfo.Type.IFILE, L2genData.GEOFILE);
        addControlListeners();
        addEventListeners();
    }

    private void addControlListeners() {
        fileSelector.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (isControlHandlerEnabled()) {
                    seaDASProcessorModel.setParamValue(L2genData.GEOFILE, fileSelector.getFileName());
                }
            }
        });
    }

    private void addEventListeners() {
        seaDASProcessorModel.addPropertyChangeListener(L2genData.GEOFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                disableControlHandler();
                fileSelector.setFilename(seaDASProcessorModel.getParamValue(L2genData.GEOFILE));
                enableControlHandler();
            }
        });

        seaDASProcessorModel.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                fileSelector.setEnabled(seaDASProcessorModel.isValidIfile() && seaDASProcessorModel.isGeofileRequired());
                fileSelector.setVisible(seaDASProcessorModel.isValidIfile() && seaDASProcessorModel.isGeofileRequired());
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
