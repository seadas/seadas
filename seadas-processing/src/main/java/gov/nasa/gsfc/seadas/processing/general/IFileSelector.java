package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.swing.selection.AbstractSelectionChangeListener;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 7/9/12
 * Time: 5:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class IFileSelector {

    final private ProcessorModel processorModel;

    private SourceProductFileSelector sourceProductSelector;
    private boolean controlHandlerEnabled = true;
    private boolean eventHandlerEnabled = true;

    public IFileSelector(ProcessorModel processorModel) {
        this.processorModel = processorModel;

        sourceProductSelector = new SourceProductFileSelector(VisatApp.getApp(), this.processorModel.getPrimaryInputFileOptionName());

        sourceProductSelector.initProducts();
        sourceProductSelector.setProductNameLabel(new JLabel(this.processorModel.getPrimaryInputFileOptionName()));
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
                    disableEventHandler();
                    processorModel.setParamValue(processorModel.getPrimaryInputFileOptionName(), iFile.getAbsolutePath());
                    enableEventHandler();
                }
            }
        });
    }

    private void addEventListeners() {
        processorModel.addPropertyChangeListener(processorModel.getPrimaryInputFileOptionName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                File iFile = new File(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));

                disableControlHandler();
                if (isEventHandlerEnabled()) {
                    if ( iFile != null && iFile.exists()) {
                        sourceProductSelector.setSelectedFile(iFile);
                    } else {
                        sourceProductSelector.releaseProducts();
                    }
                }
                enableControlHandler();

            }
        });
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

    private String computeIFileOptionValue() {
        String ifileOptionValue = "";
        if ( processorModel.isMultipleInputFiles() )  {
            ifileOptionValue = getSelectedFilesList();


        }   else {
        ifileOptionValue =  getSelectedIFile().getAbsolutePath();
        }

        return ifileOptionValue;
    }
    public String getSelectedFilesList() {
        File[] selectedFiles = getSourceProductSelector().getSelectedMultiFiles();
        StringBuilder fileNames = new StringBuilder();
        FileInfo fi;
        for (File file : selectedFiles) {

            fi = new FileInfo(file.getAbsolutePath());
            //System.out.println(fi.getTypeName());
            fileNames.append(file.getAbsolutePath() + "\n");
        }
        return fileNames.toString();
    }
}
