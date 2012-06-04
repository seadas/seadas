package gov.nasa.gsfc.seadas.processing.l2gen;

import com.bc.ceres.swing.selection.AbstractSelectionChangeListener;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.general.SourceProductFileSelector;
import org.esa.beam.framework.datamodel.Product;
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

    private JPanel ifilePanel;
    private JPanel geofilePanel;
    private JPanel ofilePanel;

    private SourceProductFileSelector sourceProductSelector;

    L2genInputOutputPanel(L2genData l2genData) {

        this.l2genData = l2genData;

        initComponents();
        addComponents();
    }


    public void initComponents() {
        ifilePanel = createIfilePanel();
        geofilePanel = createGeofilePanel();
        ofilePanel = createOfilePanel();
    }

    public void addComponents() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Primary I/O Files"));

        add(ifilePanel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        add(geofilePanel,
                new GridBagConstraintsCustom(0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        add(ofilePanel,
                new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
    }


    private JPanel createIfilePanel() {

        sourceProductSelector = new SourceProductFileSelector(VisatApp.getApp(), L2genData.IFILE);
        sourceProductSelector.initProducts();
        sourceProductSelector.setProductNameLabel(new JLabel(L2genData.IFILE));
        sourceProductSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "123456789 123456789 123456789 123456789 123456789 ");


        final JPanel jPanel = sourceProductSelector.createDefaultPanel();

        final boolean[] handlerEnabled = {true};

        sourceProductSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {
                File iFile = getSelectedIFile();
                if (handlerEnabled[0] && iFile != null) {
                    l2genData.setParamValue(L2genData.IFILE, iFile.getAbsolutePath());
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                File iFile = new File(l2genData.getParamValue(L2genData.IFILE));
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


    private JPanel createGeofilePanel() {


        final FileSelectorPanel jPanel = new FileSelectorPanel(VisatApp.getApp(),
                FileSelectorPanel.Type.IFILE, L2genData.GEOFILE);


        final boolean[] handlerEnabled = {true};


        jPanel.addPropertyChangeListener(jPanel.getPropertyName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (handlerEnabled[0]) {
                    l2genData.setParamValue(L2genData.GEOFILE, jPanel.getFileName());
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.GEOFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                handlerEnabled[0] = false;
                jPanel.setFilename(l2genData.getParamValue(L2genData.GEOFILE));
                handlerEnabled[0] = true;
            }
        });

        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jPanel.setEnabled(l2genData.isValidIfile() && l2genData.isRequiresGeofile());
            }
        });

        return jPanel;
    }


    private JPanel createOfilePanel() {

        final FileSelectorPanel jPanel = new FileSelectorPanel(VisatApp.getApp(),
                FileSelectorPanel.Type.OFILE, L2genData.OFILE);

        final boolean[] handlerEnabled = {true};


        jPanel.addPropertyChangeListener(jPanel.getPropertyName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (handlerEnabled[0]) {
                    l2genData.setParamValue(L2genData.OFILE, jPanel.getFileName());
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.OFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                handlerEnabled[0] = false;
                jPanel.setFilename(l2genData.getParamValue(L2genData.OFILE));
                handlerEnabled[0] = true;
            }
        });

        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jPanel.setEnabled(l2genData.isValidIfile());
            }
        });

        return jPanel;
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


    public Product getSelectedProduct() {
        if (sourceProductSelector == null) {
            return null;
        }

        return sourceProductSelector.getSelectedProduct();
    }


    public void prepareShow() {
        if (sourceProductSelector != null) {
            sourceProductSelector.initProducts();
        }
    }

    public void prepareHide() {
        if (sourceProductSelector != null) {
            sourceProductSelector.releaseProducts();
        }
    }
}
