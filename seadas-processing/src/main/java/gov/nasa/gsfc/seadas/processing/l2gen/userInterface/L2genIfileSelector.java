package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import com.bc.ceres.swing.selection.AbstractSelectionChangeListener;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import gov.nasa.gsfc.seadas.processing.core.SeaDASProcessorModel;
import gov.nasa.gsfc.seadas.processing.common.SeadasFileSelector;


import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.esa.snap.rcp.SnapApp;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/6/12
 * Time: 11:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genIfileSelector {

    final private SeaDASProcessorModel seaDASProcessorModel;

    private SeadasFileSelector fileSelector;
    private boolean controlHandlerEnabled = true;
    private boolean eventHandlerEnabled = true;

    public L2genIfileSelector(SeaDASProcessorModel seaDASProcessorModel) {
        this.seaDASProcessorModel = seaDASProcessorModel;

        fileSelector = new SeadasFileSelector(SnapApp.getDefault().getAppContext(), seaDASProcessorModel.getPrimaryInputFileOptionName(), seaDASProcessorModel.isMultipleInputFiles());
        fileSelector.initProducts();
        fileSelector.setFileNameLabel(new JLabel(seaDASProcessorModel.getPrimaryInputFileOptionName()));
        fileSelector.getFileNameComboBox().setPrototypeDisplayValue(
                "123456789 123456789 123456789 123456789 123456789 ");

        addControlListeners();
        addEventListeners();
    }

    private void addControlListeners() {
        fileSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {
                File iFile = getSelectedIFile();
                if (isControlHandlerEnabled() && iFile != null) {
                    disableEventHandler();
                    //disableControlHandler();
                    seaDASProcessorModel.setParamValue(seaDASProcessorModel.getPrimaryInputFileOptionName(), getSelectedIFileName());
                    enableEventHandler();
                }
            }
        });
    }

    private void addEventListeners() {
        seaDASProcessorModel.addPropertyChangeListener(seaDASProcessorModel.getPrimaryInputFileOptionName(), new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        String ifileName = seaDASProcessorModel.getParamValue(seaDASProcessorModel.getPrimaryInputFileOptionName());
                        System.out.println("processor model property changed! ifileName in file selector " + ifileName);
                        File iFile = new File(ifileName);
                        if (isEventHandlerEnabled() || ifileName.isEmpty()) {
                            //disableEventHandler();
                            disableControlHandler();
                            if (iFile.exists()) {
                                fileSelector.setSelectedFile(iFile);
                            } else {
                                fileSelector.setSelectedFile(null);
                            }
                            enableControlHandler();
                        }
                    }
                }
        );
        seaDASProcessorModel.addPropertyChangeListener("cancel", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                fileSelector = null;
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
        if (fileSelector == null) {
            return null;
        }
        if (fileSelector.getSelectedFile() == null) {
            return null;
        }
        return fileSelector.getSelectedFile();
    }

    /**
     * This method derives uncompressed file name in case the product is compressed.
     *
     * @return Selected product file name   after uncompressed.
     */
    public String getSelectedIFileName() {
        if (fileSelector == null) {
            return null;
        }
        if (fileSelector.getSelectedFile() == null) {
            return null;
        }
        return fileSelector.getSelectedFile().toString();
    }


    public JPanel getJPanel() {
        return fileSelector.createDefaultPanel();
    }

    public SeadasFileSelector getFileSelector() {
        return fileSelector;
    }
}
