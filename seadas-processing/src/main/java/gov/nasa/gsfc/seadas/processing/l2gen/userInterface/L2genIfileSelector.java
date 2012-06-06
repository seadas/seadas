package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import com.bc.ceres.swing.selection.AbstractSelectionChangeListener;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import gov.nasa.gsfc.seadas.processing.core.L2genData;
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

    final private L2genData l2genData;

    private SourceProductFileSelector sourceProductSelector;
    private boolean controlHandlerEnabled = true;

    public L2genIfileSelector(L2genData l2genData) {
        this.l2genData = l2genData;

        sourceProductSelector = new SourceProductFileSelector(VisatApp.getApp(), L2genData.IFILE);
        sourceProductSelector.initProducts();
        sourceProductSelector.setProductNameLabel(new JLabel(L2genData.IFILE));
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
                    l2genData.setParamValue(L2genData.IFILE, iFile.getAbsolutePath());
                }
            }
        });
    }

    private void addEventListeners() {
        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                File iFile = new File(l2genData.getParamValue(L2genData.IFILE));
                disableControlHandler();
                if (iFile != null && iFile.exists()) {
                    sourceProductSelector.setSelectedFile(iFile);
                } else {
                    sourceProductSelector.releaseProducts();
                }
                enableControlHandler();
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
