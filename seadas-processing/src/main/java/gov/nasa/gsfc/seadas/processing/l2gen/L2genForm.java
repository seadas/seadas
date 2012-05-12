/*
Author: Danny Knowles
    Don Shea
*/

package gov.nasa.gsfc.seadas.processing.l2gen;

import com.bc.ceres.swing.selection.AbstractSelectionChangeListener;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import gov.nasa.gsfc.seadas.processing.general.CloProgramUI;
import gov.nasa.gsfc.seadas.processing.general.OutputFileSelector;
import gov.nasa.gsfc.seadas.processing.general.ProcessorModel;
import gov.nasa.gsfc.seadas.processing.general.SourceProductFileSelector;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;


public class L2genForm extends JTabbedPane implements CloProgramUI {

    private final AppContext appContext;
    private final SourceProductFileSelector sourceProductSelector;
    private final OutputFileSelector outputFileSelector;

    private boolean handleIfileJComboBoxEnabled = true;
    private boolean handleOfileSelecterEnabled = true;

    private int myTabCount = 0;

    private static final String MAIN_TAB_NAME = "Main";
    private static final String PRODUCTS_TAB_NAME = "Products";
    private static final int MAIN_TAB_INDEX = 0;
    private static final int PRODUCTS_TAB_INDEX = 1;

    private L2genData l2genData = new L2genData();

    private ProcessorModel processorModel;


    L2genForm(AppContext appContext, String xmlFileName) {
        this.appContext = appContext;

        processorModel = new ProcessorModel("l2gen", xmlFileName);

        sourceProductSelector = new SourceProductFileSelector(VisatApp.getApp(), L2genData.IFILE);
        sourceProductSelector.initProducts();
        outputFileSelector = new OutputFileSelector(VisatApp.getApp(), L2genData.OFILE);


        l2genData.initXmlBasedObjects();
        createUserInterface();
        addL2genDataListeners();

        // set ifile if it has been loaded into SeaDAS prior to launching l2gen
        if (sourceProductSelector.getSelectedProduct() != null
                && sourceProductSelector.getSelectedProduct().getFileLocation() != null) {
            String ifile = sourceProductSelector.getSelectedProduct().getFileLocation().toString();

            if (ifile != null && ifile.length() > 0) {
                l2genData.setParamValue(L2genData.IFILE, ifile);
            }
        }

        l2genData.fireAllParamEvents();
        l2genData.fireEvent(L2genData.IFILE_VALIDATION_CHANGE_EVENT, null, l2genData.isValidIfile());
    }
    //----------------------------------------------------------------------------------------
    // Create tabs within the main panel
    //----------------------------------------------------------------------------------------

    public ProcessorModel getProcessorModel() {
        processorModel.setParString(l2genData.getParString(false));
        processorModel.setOutputFile(new File(l2genData.getParamValue(L2genData.OFILE)));
        return processorModel;
    }

    public Product getSelectedSourceProduct() {
        return sourceProductSelector.getSelectedProduct();
    }

    private void createUserInterface() {

        createMainTab(MAIN_TAB_NAME);
        this.setEnabledAt(MAIN_TAB_INDEX, true);

        createProductsTab(PRODUCTS_TAB_NAME);
        this.setEnabledAt(PRODUCTS_TAB_INDEX, false);

        int tabIndex = 1;


        for (final ParamCategoryInfo paramCategoryInfo : l2genData.getParamCategoryInfos()) {
            if (paramCategoryInfo.isVisible() && (paramCategoryInfo.getParamInfos().size() > 0)) {
                tabIndex++;
                final int tabIndexFinal = tabIndex;
                //               createParamsTab(paramCategoryInfo, tabIndex);
                L2genCategorizedParamsPanel l2genCategorizedParamsPanel = new L2genCategorizedParamsPanel(l2genData, paramCategoryInfo);
                addTab(paramCategoryInfo.getName(), l2genCategorizedParamsPanel);

                for (ParamInfo paramInfo : paramCategoryInfo.getParamInfos()) {
                    l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            StringBuilder stringBuilder = new StringBuilder(paramCategoryInfo.getName());

                            if (l2genData.isParamCategoryDefault(paramCategoryInfo)) {
                                setTabName(tabIndexFinal, stringBuilder.toString());
                            } else {
                                setTabName(tabIndexFinal, stringBuilder.append("*").toString());
                            }

                        }
                    });
                }


                this.setEnabledAt(tabIndex, false);
            }
        }

        myTabCount = tabIndex + 1;
    }


    private JPanel createInputOutputPanel() {

        final JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Primary Input/Output Files"));

        mainPanel.add(createInputFilePanel(),
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        mainPanel.add(createGeoFilePanel(),
                new GridBagConstraintsCustom(0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        mainPanel.add(createOutputFilePanel(),
                new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        return mainPanel;
    }


    //----------------------------------------------------------------------------------------
    // Methods to create each of the main  and sub tabs
    //----------------------------------------------------------------------------------------

    private void createMainTab(String tabname) {

        final JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(createInputOutputPanel(),
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));


        L2genParfilePanel l2genParfilePanel = new L2genParfilePanel(l2genData);


        mainPanel.add(l2genParfilePanel,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 3));

        mainPanel.add(outputFileSelector.getOpenInAppCheckBox(),
                new GridBagConstraintsCustom(0, 2, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        addTab(tabname, mainPanel);
    }


    private void createProductsTab(String tabname) {


        JPanel productSelectorJPanel = new L2genProductSelectorPanel(l2genData);

        JPanel wavelengthsLimitorJPanel = new L2genWavelengthLimiterPanel(l2genData);

        JPanel selectedProductsJPanel = createSelectedProductsJPanel();


        final JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(productSelectorJPanel,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH));

        mainPanel.add(wavelengthsLimitorJPanel,
                new GridBagConstraintsCustom(1, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

        mainPanel.add(selectedProductsJPanel,
                new GridBagConstraintsCustom(0, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 0, 2));


        addTab(tabname, mainPanel);

        l2genData.addPropertyChangeListener(L2genData.L2PROD, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                StringBuilder tabname = new StringBuilder(PRODUCTS_TAB_NAME);

                if (l2genData.isParamDefault(L2genData.L2PROD)) {
                    setTabName(PRODUCTS_TAB_INDEX, tabname.toString());
                } else {
                    setTabName(PRODUCTS_TAB_INDEX, tabname.append("*").toString());
                }
            }
        });
    }


    //----------------------------------------------------------------------------------------
    // Methods involving Panels
    //----------------------------------------------------------------------------------------

    private JPanel createInputFilePanel() {

        sourceProductSelector.setProductNameLabel(new JLabel(L2genData.IFILE));
        sourceProductSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "123456789 123456789 123456789 123456789 123456789 ");
        final JPanel panel = sourceProductSelector.createDefaultPanel();


        sourceProductSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {
                if (handleIfileJComboBoxEnabled &&
                        sourceProductSelector.getSelectedProduct() != null
                        && sourceProductSelector.getSelectedProduct().getFileLocation() != null) {
                    l2genData.setParamValue(L2genData.IFILE, sourceProductSelector.getSelectedProduct().getFileLocation().toString());
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                if (sourceProductSelector != null) {
                    File ifile = null;
                    if (evt.getNewValue() != null && evt.getNewValue().toString().length() > 0) {
                        ifile = new File(evt.getNewValue().toString());
                        handleIfileJComboBoxEnabled = false;
                        if (ifile.exists()) {
                            sourceProductSelector.setSelectedFile(ifile);
                        } else {
                            sourceProductSelector.releaseProducts();
                        }
                        handleIfileJComboBoxEnabled = true;

                    }

                    handleIfileJComboBoxEnabled = false;
                    sourceProductSelector.setSelectedFile(ifile);
                    handleIfileJComboBoxEnabled = true;
                }
            }
        });

        return panel;
    }


    private JPanel createGeoFilePanel() {
        final SourceProductFileSelector geofileSelector = new SourceProductFileSelector(VisatApp.getApp(), L2genData.GEOFILE);

        geofileSelector.setProductNameLabel(new JLabel(L2genData.GEOFILE));
        geofileSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "123456789 123456789 123456789 123456789 123456789 ");
        final JPanel panel = geofileSelector.createDefaultPanel();
//        geofileSelector.setEnabled(false);


        geofileSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {
                if (handleIfileJComboBoxEnabled &&
                        geofileSelector.getSelectedProduct() != null
                        && geofileSelector.getSelectedProduct().getFileLocation() != null) {
                    l2genData.setParamValue(L2genData.GEOFILE, geofileSelector.getSelectedProduct().getFileLocation().toString());
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.GEOFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                if (geofileSelector != null) {
                    File geofile = new File(l2genData.getParamValue(L2genData.GEOFILE));
                    handleIfileJComboBoxEnabled = false;
                    if (geofile.exists()) {
                        geofileSelector.setSelectedFile(geofile);
                    } else {
                        geofileSelector.releaseProducts();
                    }
                    handleIfileJComboBoxEnabled = true;
                }
            }
        });

        l2genData.addPropertyChangeListener(L2genData.IFILE_VALIDATION_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                boolean isSetNewValue = false;
                if (evt.getNewValue() != null) {
                    isSetNewValue = new Boolean((Boolean) evt.getNewValue());
                }

                geofileSelector.setEnabled(isSetNewValue);
            }
        });


        return panel;
    }


    private JPanel createOutputFilePanel() {

        outputFileSelector.setOutputFileNameLabel(new JLabel(L2genData.OFILE + " (name)"));
        outputFileSelector.setOutputFileDirLabel(new JLabel(L2genData.OFILE + " (directory)"));
        final JPanel panel = outputFileSelector.createDefaultPanel();


        //   outputFileSelector.setEnabled(false);


        outputFileSelector.getModel().getValueContainer().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                String ofile = null;
                if (outputFileSelector.getModel().getProductFile() != null) {
                    ofile = outputFileSelector.getModel().getProductFile().getAbsolutePath();

                    if (ofile != null && handleOfileSelecterEnabled) {
                        l2genData.setParamValue(L2genData.OFILE, ofile);
                    }
                }
            }
        });



        l2genData.addPropertyChangeListener(L2genData.OFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String ofileString = l2genData.getParamValue(L2genData.OFILE);
                if (ofileString.equals(ParamInfo.NULL_STRING)) {
                } else {
                    File ofile = new File(ofileString);

                    handleOfileSelecterEnabled = false;
                    outputFileSelector.getModel().setProductDir(ofile.getParentFile());
                    outputFileSelector.getModel().setProductName(ofile.getName());
                    handleOfileSelecterEnabled = true;
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.IFILE_VALIDATION_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                boolean isSetNewValue = false;
                if (evt.getNewValue() != null) {
                    isSetNewValue = new Boolean((Boolean) evt.getNewValue());
                }

                outputFileSelector.setEnabled(isSetNewValue);
            }
        });

        return panel;
    }


    private JPanel createSelectedProductsJPanel() {

        final JTextArea selectedProductsJTextArea = new JTextArea();
        selectedProductsJTextArea.setLineWrap(true);
        selectedProductsJTextArea.setWrapStyleWord(true);
        selectedProductsJTextArea.setColumns(20);
        selectedProductsJTextArea.setRows(5);
        selectedProductsJTextArea.setEditable(true);

        selectedProductsJTextArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                String l2prod = l2genData.sortStringList(selectedProductsJTextArea.getText());
                l2genData.setParamValue(L2genData.L2PROD, l2prod);
                selectedProductsJTextArea.setText(l2genData.getParamValue(L2genData.L2PROD));
            }
        });


        l2genData.addPropertyChangeListener(L2genData.L2PROD, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                selectedProductsJTextArea.setText(l2genData.getParamValue(L2genData.L2PROD));
            }
        });


        final JButton defaultsButton = new JButton("Apply Defaults");
        defaultsButton.setEnabled(false);

        defaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.setProdToDefault();
            }
        });

        l2genData.addPropertyChangeListener(L2genData.L2PROD, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (l2genData.isParamDefault(L2genData.L2PROD)) {
                    defaultsButton.setEnabled(false);
                } else {
                    defaultsButton.setEnabled(true);
                }
            }
        });


        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Selected Products"));

        mainPanel.add(selectedProductsJTextArea,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 3));

        mainPanel.add(defaultsButton,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE));

        return mainPanel;
    }


    //----------------------------------------------------------------------------------------
    // Listeners and L2genData Handlers
    //----------------------------------------------------------------------------------------

    private void addL2genDataListeners() {


        l2genData.addPropertyChangeListener(L2genData.INVALID_IFILE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //   invalidIfileEvent();
            }
        });



        l2genData.addPropertyChangeListener(L2genData.IFILE_VALIDATION_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                boolean isSetNewValue = false;
                if (evt.getNewValue() != null) {
                    isSetNewValue = new Boolean((Boolean) evt.getNewValue());
                }

                enableTabs(isSetNewValue);
            }
        });


    }

    private void enableTabs(boolean enabled) {
        for (int tabIndex = 1; tabIndex < myTabCount; tabIndex++) {
            this.setEnabledAt(tabIndex, enabled);
        }
    }


    private void invalidIfileEvent() {
        for (int tabIndex = 1; tabIndex < myTabCount; tabIndex++) {
            this.setEnabledAt(tabIndex, false);
        }
        //todo disable RUN button
    }

    private void setTabName(int tabIndex, String name) {
        debug("tabIndex=" + tabIndex + " myTabCount=" + this.getTabCount());
        if (tabIndex < (this.getTabCount() + 1)) {
            this.setTitleAt(tabIndex, name);
        }
    }


    //----------------------------------------------------------------------------------------
    // Miscellaneous
    //----------------------------------------------------------------------------------------

    Product getSourceProduct() {
        return sourceProductSelector.getSelectedProduct();
    }

    /*
    */
    void prepareShow() {
        sourceProductSelector.initProducts();
    }

    void prepareHide() {
        sourceProductSelector.releaseProducts();
    }


    private void debug(String string) {
        //  System.out.println(string);
    }


}