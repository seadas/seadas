package gov.nasa.gsfc.seadas.processing.l2gen;

import com.bc.ceres.swing.selection.AbstractSelectionChangeListener;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import gov.nasa.gsfc.seadas.processing.general.OutputFileSelector;
import gov.nasa.gsfc.seadas.processing.general.SourceProductFileSelector;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/14/12
 * Time: 7:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genInputOutputPanel extends JPanel {

    private L2genData l2genData;

    private JPanel inputFilePanel;
    private JPanel geoFilePanel;
    private JPanel outputFilePanel;
    private SourceProductFileSelector sourceProductSelector;
    private OutputFileSelector outputFileSelector;

    L2genInputOutputPanel(L2genData l2genData) {

        this.l2genData = l2genData;

        initComponents();
        addComponents();
    }


    public void initComponents() {
        inputFilePanel = createInputFilePanel();
        geoFilePanel = createGeoFilePanel();
        outputFilePanel = createOutputFilePanel();
    }

    public void addComponents() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Primary Input/Output Files"));

        add(inputFilePanel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        add(geoFilePanel,
                new GridBagConstraintsCustom(0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        add(outputFilePanel,
                new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
    }


    private JPanel createInputFilePanel() {

        sourceProductSelector = new SourceProductFileSelector(VisatApp.getApp(), L2genData.IFILE);
        sourceProductSelector.initProducts();

        final boolean[] handlerEnabled = {true};

        sourceProductSelector.setProductNameLabel(new JLabel(L2genData.IFILE));
        sourceProductSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "123456789 123456789 123456789 123456789 123456789 ");

        final JPanel jPanel = sourceProductSelector.createDefaultPanel();

        sourceProductSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {
                File iFile = getSelectedIFile();
                if (handlerEnabled[0] && iFile != null) {
                    l2genData.setParamValue(L2genData.IFILE, iFile.toString());
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                File iFile = new File(l2genData.getParamValue(l2genData.IFILE));
                handlerEnabled[0] = false;
                if (iFile != null && iFile.exists()) {
                    sourceProductSelector.setSelectedFile(iFile);
                } else {
                    sourceProductSelector.releaseProducts();
                }
                handlerEnabled[0] = true;
            }
        });

        return jPanel;
    }


    private JPanel createGeoFilePanel() {
        final SourceProductFileSelector geofileSelector = new SourceProductFileSelector(VisatApp.getApp(), L2genData.GEOFILE);

        geofileSelector.setProductNameLabel(new JLabel(L2genData.GEOFILE));
        geofileSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "123456789 123456789 123456789 123456789 123456789 ");

        final JPanel jPanel = geofileSelector.createDefaultPanel();

        final boolean[] handlerEnabled = {true};

        geofileSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {
                if (handlerEnabled[0] &&
                        geofileSelector.getSelectedProduct() != null
                        && geofileSelector.getSelectedProduct().getFileLocation() != null) {
                    l2genData.setParamValue(L2genData.GEOFILE, geofileSelector.getSelectedProduct().getFileLocation().toString());
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.GEOFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                File geoFile = new File(l2genData.getParamValue(L2genData.GEOFILE));
                handlerEnabled[0] = false;
                if (geoFile.exists()) {
                    geofileSelector.setSelectedFile(geoFile);
                } else {
                    geofileSelector.releaseProducts();
                }
                handlerEnabled[0] = true;
            }
        });

        l2genData.addPropertyChangeListener(L2genData.IFILE_VALIDATION_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                geofileSelector.setEnabled(l2genData.isValidIfile());
            }
        });

        return jPanel;
    }


    private JPanel createOutputFilePanel() {

        outputFileSelector = new OutputFileSelector(VisatApp.getApp(), L2genData.OFILE);
        outputFileSelector.setOutputFileNameLabel(new JLabel(L2genData.OFILE + " (name)"));
        outputFileSelector.setOutputFileDirLabel(new JLabel(L2genData.OFILE + " (directory)"));
        final JPanel panel = outputFileSelector.createDefaultPanel();

        final boolean[] handlerEnabled = {true};


        outputFileSelector.getModel().getValueContainer().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                File oFile = outputFileSelector.getModel().getProductFile();

                if (oFile != null && handlerEnabled[0]) {
                    l2genData.setParamValue(L2genData.OFILE, oFile.toString());
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.OFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                handlerEnabled[0] = false;
                String ofile = l2genData.getParamValue(L2genData.OFILE);
                if (ofile.equals(ParamInfo.NULL_STRING)) {
//                    outputFileSelector.getModel().setProductDir(null);
//                    outputFileSelector.getModel().setProductName("");
                } else {
                    File oFile = new File(ofile);
                    if (oFile.getParentFile() != null) {
                        outputFileSelector.getModel().setProductDir(oFile.getParentFile());
                    }

                    outputFileSelector.getModel().setProductName(oFile.getName());


                }
                handlerEnabled[0] = true;
            }
        });


        l2genData.addPropertyChangeListener(L2genData.IFILE_VALIDATION_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                outputFileSelector.setEnabled(l2genData.isValidIfile());
            }
        });

        return panel;
    }


    public JCheckBox getOpenInAppCheckBox() {
        return getOutputFileSelector().getOpenInAppCheckBox();
    }

    public SourceProductFileSelector getSourceProductSelector() {
        return sourceProductSelector;
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

    public OutputFileSelector getOutputFileSelector() {
        return outputFileSelector;
    }
}
