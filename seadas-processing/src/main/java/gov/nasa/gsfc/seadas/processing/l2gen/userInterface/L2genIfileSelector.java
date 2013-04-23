package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import com.bc.ceres.swing.selection.AbstractSelectionChangeListener;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import gov.nasa.gsfc.seadas.processing.core.L2genDataProcessorModel;
import gov.nasa.gsfc.seadas.processing.general.SourceProductFileSelector;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/6/12
 * Time: 11:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genIfileSelector {

    final private L2genDataProcessorModel l2genDataProcessorModel;

    private SourceProductFileSelector sourceProductSelector;
    private boolean controlHandlerEnabled = true;
    private boolean eventHandlerEnabled = true;

    public L2genIfileSelector(L2genDataProcessorModel l2genDataProcessorModel) {
        this.l2genDataProcessorModel = l2genDataProcessorModel;

        sourceProductSelector = new SourceProductFileSelector(VisatApp.getApp(), l2genDataProcessorModel.getPrimaryInputFileOptionName(), l2genDataProcessorModel.isMultipleInputFiles());
        sourceProductSelector.initProducts();
        sourceProductSelector.setProductNameLabel(new JLabel(l2genDataProcessorModel.getPrimaryInputFileOptionName()));
        sourceProductSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "123456789 123456789 123456789 123456789 123456789 ");

        addControlListeners();
        addEventListeners();
    }

    private void addControlListeners() {
        sourceProductSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {
                File iFile = getSelectedIFile();
                if (isControlHandlerEnabled() && iFile != null) {
                    //if (l2genDataProcessorModel.getParamValue(l2genDataProcessorModel.getPrimaryInputFileOptionName()).equals(iFile.getAbsolutePath())) {
                    disableEventHandler();
                    //}
                    l2genDataProcessorModel.setParamValue(l2genDataProcessorModel.getPrimaryInputFileOptionName(), iFile.getAbsolutePath());
                    if (!l2genDataProcessorModel.getParamValue(l2genDataProcessorModel.getPrimaryInputFileOptionName()).equals(iFile.getAbsolutePath())) {
                        enableEventHandler();
                    }
                    l2genDataProcessorModel.updateParamValues(sourceProductSelector.getSelectedProduct());
                }
            }
        });
    }

    private void addEventListeners() {
        l2genDataProcessorModel.addPropertyChangeListener(l2genDataProcessorModel.getPrimaryInputFileOptionName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String ifileName = l2genDataProcessorModel.getParamValue(l2genDataProcessorModel.getPrimaryInputFileOptionName());
                File iFile = new File(ifileName);
                String selectedIfileName = getSelectedIFile().getAbsolutePath();
                disableControlHandler();
                if (isEventHandlerEnabled() ||
                        !l2genDataProcessorModel.getParamValue(l2genDataProcessorModel.getPrimaryInputFileOptionName()).equals(selectedIfileName)) {
                    if (iFile != null && iFile.exists()) {
                        if (!l2genDataProcessorModel.getParamValue(l2genDataProcessorModel.getPrimaryInputFileOptionName()).equals(getSelectedIFile().getAbsolutePath())) {
                            sourceProductSelector.setSelectedFile(iFile);
                        }
                    } else {
                        sourceProductSelector.setSelectedFile(null);
                    }
                }
                enableControlHandler();
            }
        }
        );
    }

    private boolean isControlHandlerEnabled() {
        return controlHandlerEnabled;
    }

    private boolean isEventHandlerEnabled() {
        return eventHandlerEnabled;
    }

    private void enableControlHandler() {
        controlHandlerEnabled = true;
    }

    private void disableControlHandler() {
        controlHandlerEnabled = false;
    }

    private void enableEventHandler() {
        eventHandlerEnabled = true;
    }

    private void disableEventHandler() {
        eventHandlerEnabled = false;
    }

    public File getSelectedIFile() {
        if (sourceProductSelector == null) {
            return null;
        }
        if (sourceProductSelector.getSelectedProduct() == null) {
            return null;
        }

        return sourceProductSelector.getSelectedProduct().getFileLocation();
    }

    public JPanel getJPanel() {
        return sourceProductSelector.createDefaultPanel();
    }

    public SourceProductFileSelector getSourceProductSelector() {
        return sourceProductSelector;
    }
}
