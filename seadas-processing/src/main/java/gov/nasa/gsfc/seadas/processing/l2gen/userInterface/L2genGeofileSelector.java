package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.general.FileSelector;
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

    final private L2genData l2genData;

    final private FileSelector fileSelector;
    private boolean controlHandlerEnabled = true;

    public L2genGeofileSelector(L2genData l2genData) {
        this.l2genData = l2genData;

        fileSelector = new FileSelector(VisatApp.getApp(), FileSelector.Type.IFILE, L2genData.GEOFILE);
        addControlListeners();
        addEventListeners();
    }

    private void addControlListeners() {
        fileSelector.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (isControlHandlerEnabled()) {
                    l2genData.setParamValue(L2genData.GEOFILE, fileSelector.getFileName());
                }
            }
        });
    }

    private void addEventListeners() {
        l2genData.addPropertyChangeListener(L2genData.GEOFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                disableControlHandler();
                fileSelector.setFilename(l2genData.getParamValue(L2genData.GEOFILE));
                enableControlHandler();
            }
        });

        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                fileSelector.setEnabled(l2genData.isValidIfile() && l2genData.isRequiresGeofile());
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
