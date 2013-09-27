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
                    disableEventHandler();
                    l2genDataProcessorModel.updateParamValues(sourceProductSelector.getSelectedProduct());
                    //l2genDataProcessorModel.updateParamValues(sourceProductSelector.getSelectedProduct().getFileLocation());
                    l2genDataProcessorModel.setParamValue(l2genDataProcessorModel.getPrimaryInputFileOptionName(), getSelectedIFileName());
                    enableEventHandler();
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
                disableControlHandler();
                if (isEventHandlerEnabled() || ifileName.isEmpty()) {
                    if (iFile.exists()) {
                        sourceProductSelector.setSelectedFile(iFile);
                    } else {
                        sourceProductSelector.setSelectedFile(null);
                    }
                }
                enableControlHandler();
            }
        }
        );
        l2genDataProcessorModel.addPropertyChangeListener("cancel", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                //l2genDataProcessorModel = null;
                sourceProductSelector = null;
                //To change body of implemented methods use File | Settings | File Templates.
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

    /**
     *  This method derives uncompressed file name in case the product is compressed.
     * @return  Selected product file name   after uncompressed.
     */
    public String getSelectedIFileName() {
        if (sourceProductSelector == null) {
            return null;
        }
        if (sourceProductSelector.getSelectedProduct() == null) {
            return null;
        }
        return sourceProductSelector.getSelectedProduct().getFileLocation().toString();
//        ProductReader productReader = sourceProductSelector.getSelectedProduct().getProductReader();
//        ProductReaderPlugIn netcdfFile = productReader.getReaderPlugIn();
//        Class[] c = netcdfFile.createReaderInstance().getReaderPlugIn().getInputTypes();
//        File sourceProductFileLocation =  sourceProductSelector.getSelectedProduct().getFileLocation();
//        String sourceProductFileAbsolutePathName = sourceProductFileLocation.getParent() + System.getProperty("file.separator") + sourceProductSelector.getSelectedProduct().getName();
//        return sourceProductFileAbsolutePathName;
    }



    public JPanel getJPanel() {
        return sourceProductSelector.createDefaultPanel();
    }

    public SourceProductFileSelector getSourceProductSelector() {
        return sourceProductSelector;
    }
}
