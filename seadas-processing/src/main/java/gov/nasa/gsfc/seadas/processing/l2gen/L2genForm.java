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

    final Color DEFAULT_INDICATOR_COLOR = new Color(0, 0, 120);

    final String DEFAULT_INDICATOR_TOOLTIP = "* Identicates that the selection is not the default value";
    final String DEFAULT_INDICATOR_LABEL_ON = " *  ";
    final String DEFAULT_INDICATOR_LABEL_OFF = "     ";
    final int PARAM_STRING_TEXTLEN = 60;
    final int PARAM_FILESTRING_TEXTLEN = 70;
    final int PARAM_INT_TEXTLEN = 15;
    final int PARAM_FLOAT_TEXTLEN = 15;

    private boolean swingSentEventsDisabled = false;
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

        int currTabIndex = 1;

        for (ParamCategoryInfo paramCategoryInfo : l2genData.getParamCategoryInfos()) {
            if (paramCategoryInfo.isVisible() && (paramCategoryInfo.getParamInfos().size() > 0)) {
                currTabIndex++;
                createParamsTab(paramCategoryInfo, currTabIndex);
                this.setEnabledAt(currTabIndex, false);
            }
        }

        myTabCount = currTabIndex + 1;
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


    /**
     * Creates a tab containing Swing controls for all params within a given paramCategor <p>
     * <p/>
     * Adds in the listeners needed to detect to each control<br>
     * Adds in the listeners needed to update each control when an event has been fired<br>
     * <p/>
     * For Swing panel layout diagram see:
     * <a href="https://github.com/seadas/seadas/blob/master/seadas-processing/src/main/resources/gov/nasa/gsfc/seadas/processing/l2gen/paramDiagram1.jpg">
     * paramDiagram1.jpg</a>
     *
     * @param paramCategoryInfo
     * @param currTabIndex
     */

    private void createParamsTab(final ParamCategoryInfo paramCategoryInfo, final int currTabIndex) {


        final JPanel paramsPanel = new JPanel();
        paramsPanel.setLayout(new GridBagLayout());


        final JButton restoreDefaultsButton = new JButton("Restore Defaults (this tab only)");
        restoreDefaultsButton.setEnabled(!l2genData.isParamCategoryDefault(paramCategoryInfo));

        restoreDefaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.setToDefaults(paramCategoryInfo);
            }
        });


        int gridy = 0;
        for (ParamInfo paramInfo : paramCategoryInfo.getParamInfos()) {
            if (paramInfo.hasValidValueInfos()) {
                if (paramInfo.isBit()) {
                    createParamBitwiseComboBox(paramInfo, paramsPanel, gridy);
                } else {
                    createParamComboBox(paramInfo, paramsPanel, gridy);
                }
            } else {
                if (paramInfo.getType() == ParamInfo.Type.BOOLEAN) {
                    createParamCheckBox(paramInfo, paramsPanel, gridy);
                } else {
                    createParamTextfield(paramInfo, paramsPanel, gridy);
                }
            }

            gridy++;

            l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    StringBuilder stringBuilder = new StringBuilder(paramCategoryInfo.getName());

                    if (l2genData.isParamCategoryDefault(paramCategoryInfo)) {
                        restoreDefaultsButton.setEnabled(false);
                        setTabName(currTabIndex, stringBuilder.toString());
                    } else {
                        restoreDefaultsButton.setEnabled(true);
                        setTabName(currTabIndex, stringBuilder.append("*").toString());
                    }

                }
            });
        }


        /**
         * Add a blank filler panel to the bottom of paramsPanel
         * This serves the purpose of expanding at the bottom of the paramsPanel in order to fill the
         * space so that the rest of the param controls do not expand
         */

        paramsPanel.add(new JPanel(),
                new GridBagConstraintsCustom(0, gridy, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH));


        final JScrollPane paramsScroll = new JScrollPane(paramsPanel);

        final JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder(paramCategoryInfo.getName()));

        mainPanel.add(paramsScroll,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH));

        mainPanel.add(restoreDefaultsButton,
                new GridBagConstraintsCustom(0, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));


        //     final JPanel paddedMainPanel = SeadasGuiUtils.addPaddedWrapperPanel(mainPanel, 6);

        final JPanel paddedMainPanel = new JPanel(new GridBagLayout());
        paddedMainPanel.setPreferredSize(new Dimension(1000, 800));

        paddedMainPanel.add(mainPanel,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 6));


        addTab(paramCategoryInfo.getName(), paddedMainPanel);
    }


    private void createParamTextfield(ParamInfo paramInfo, JPanel jPanel, int gridy) {


        final String param = paramInfo.getName();
        int jTextFieldLen = 0;


        if (paramInfo.getType() == ParamInfo.Type.STRING) {
            if (paramInfo.getName().contains("file")) {
                jTextFieldLen = PARAM_FILESTRING_TEXTLEN;
            } else {
                jTextFieldLen = PARAM_STRING_TEXTLEN;
            }
        } else if (paramInfo.getType() == ParamInfo.Type.INT) {
            jTextFieldLen = PARAM_INT_TEXTLEN;
        } else if (paramInfo.getType() == ParamInfo.Type.FLOAT) {
            jTextFieldLen = PARAM_FLOAT_TEXTLEN;
        }


        final JTextField jTextField = new JTextField();
        if (jTextFieldLen > 0) {
            jTextField.setColumns(jTextFieldLen);
        }
        jTextField.setText(paramInfo.getValue());

        final JLabel jLabel = new JLabel(paramInfo.getName());
        jLabel.setToolTipText(paramInfo.getDescription());

        final JLabel defaultIndicator = new JLabel(DEFAULT_INDICATOR_LABEL_OFF);
        defaultIndicator.setForeground(DEFAULT_INDICATOR_COLOR);
        defaultIndicator.setToolTipText(DEFAULT_INDICATOR_TOOLTIP);


        jPanel.add(jLabel,
                new GridBagConstraintsCustom(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        jPanel.add(defaultIndicator,
                new GridBagConstraintsCustom(1, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        int fill;
        if (jTextFieldLen > 0) {
            fill = GridBagConstraints.NONE;
        } else {
            fill = GridBagConstraints.HORIZONTAL;
        }

        jPanel.add(jTextField,
                new GridBagConstraintsCustom(2, gridy, 1, 0, GridBagConstraints.WEST, fill));


        jTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                l2genData.setParamValue(param, jTextField.getText().toString());
            }
        });

        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.setParamValue(param, jTextField.getText().toString());
            }
        });

        l2genData.addPropertyChangeListener(param, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jTextField.setText(l2genData.getParamValue(param));
                if (l2genData.isParamDefault(param)) {
                    defaultIndicator.setText(DEFAULT_INDICATOR_LABEL_OFF);
                    defaultIndicator.setToolTipText("");
                } else {
                    defaultIndicator.setText(DEFAULT_INDICATOR_LABEL_ON);
                    defaultIndicator.setToolTipText(DEFAULT_INDICATOR_TOOLTIP);
                }
            }
        });
    }


    class MyComboBoxRenderer extends BasicComboBoxRenderer {

        private String[] tooltips;

        public void MyComboBoxRenderer(String[] tooltips) {
            this.tooltips = tooltips;
        }


        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());

                if (-1 < index && index < tooltips.length) {
                    list.setToolTipText(tooltips[index]);
                }
            } else {
                setBackground(Color.white);
                setForeground(Color.black);
            }

            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
            return this;
        }

        public void setTooltips(String[] tooltips) {
            this.tooltips = tooltips;
        }
    }

    private void createParamComboBox(final ParamInfo paramInfo, JPanel jPanel, int gridy) {

        final String param = paramInfo.getName();

        ArrayList<ParamValidValueInfo> jComboBoxArrayList = new ArrayList<ParamValidValueInfo>();
        ArrayList<String> validValuesToolTipsArrayList = new ArrayList<String>();


        for (ParamValidValueInfo paramValidValueInfo : paramInfo.getValidValueInfos()) {
            if (paramValidValueInfo.getValue() != null && paramValidValueInfo.getValue().length() > 0) {
                jComboBoxArrayList.add(paramValidValueInfo);

                if (paramValidValueInfo.getDescription().length() > 70) {
                    validValuesToolTipsArrayList.add(paramValidValueInfo.getDescription());
                } else {
                    validValuesToolTipsArrayList.add(null);
                }
            }
        }

        final ParamValidValueInfo[] jComboBoxArray;
        jComboBoxArray = new ParamValidValueInfo[jComboBoxArrayList.size()];

        int i = 0;
        for (ParamValidValueInfo paramValidValueInfo : jComboBoxArrayList) {
            jComboBoxArray[i] = paramValidValueInfo;
            i++;
        }

        final String[] validValuesToolTipsArray = new String[jComboBoxArrayList.size()];

        int j = 0;
        for (String validValuesToolTip : validValuesToolTipsArrayList) {
            validValuesToolTipsArray[j] = validValuesToolTip;
            j++;
        }


        final JComboBox jComboBox = new JComboBox(jComboBoxArray);

        final MyComboBoxRenderer myComboBoxRenderer = new MyComboBoxRenderer();
        myComboBoxRenderer.setTooltips(validValuesToolTipsArray);
        jComboBox.setRenderer(myComboBoxRenderer);
        jComboBox.setEditable(false);


        for (ParamValidValueInfo paramValidValueInfo : jComboBoxArray) {
            if (l2genData.getParamValue(param).equals(paramValidValueInfo.getValue())) {
                jComboBox.setSelectedItem(paramValidValueInfo);
            }
        }


        final JLabel jLabel = new JLabel(paramInfo.getName());
        jLabel.setToolTipText(paramInfo.getDescription());

        final JLabel defaultIndicator = new JLabel(DEFAULT_INDICATOR_LABEL_OFF);
        defaultIndicator.setForeground(DEFAULT_INDICATOR_COLOR);
        defaultIndicator.setToolTipText(DEFAULT_INDICATOR_TOOLTIP);


        jPanel.add(jLabel,
                new GridBagConstraintsCustom(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));


        jPanel.add(defaultIndicator,
                new GridBagConstraintsCustom(1, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));


        jPanel.add(jComboBox,
                new GridBagConstraintsCustom(2, gridy, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.setParamValue(paramInfo, (ParamValidValueInfo) jComboBox.getSelectedItem());
            }
        });


        l2genData.addPropertyChangeListener(param, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                debug("receiving eventName " + param);
                boolean found = false;
                ComboBoxModel comboBoxModel = jComboBox.getModel();

                for (int i = 0; i < comboBoxModel.getSize(); i++) {
                    ParamValidValueInfo jComboBoxItem = (ParamValidValueInfo) comboBoxModel.getElementAt(i);
                    if (paramInfo.getValue().equals(jComboBoxItem.getValue())) {
                        jComboBox.setSelectedItem(jComboBoxItem);

                        if (l2genData.isParamDefault(paramInfo)) {
                            defaultIndicator.setText(DEFAULT_INDICATOR_LABEL_OFF);
                            defaultIndicator.setToolTipText("");
                        } else {
                            defaultIndicator.setText(DEFAULT_INDICATOR_LABEL_ON);
                            defaultIndicator.setToolTipText(DEFAULT_INDICATOR_TOOLTIP);
                        }
                        found = true;
                    }
                }

                if (!found) {
                    final ParamValidValueInfo newArray[] = new ParamValidValueInfo[comboBoxModel.getSize() + 1];
                    int i;
                    for (i = 0; i < comboBoxModel.getSize(); i++) {
                        newArray[i] = (ParamValidValueInfo) comboBoxModel.getElementAt(i);
                    }
                    newArray[i] = new ParamValidValueInfo(paramInfo.getValue());
                    newArray[i].setDescription("User defined value");
                    jComboBox.setModel(new DefaultComboBoxModel(newArray));
                    jComboBox.setSelectedItem(newArray[i]);
                }
            }
        });

    }

    private void createParamBitwiseComboBox(final ParamInfo paramInfo, JPanel jPanel, int gridy) {

        final JPanel valuePanel = new JPanel();
        valuePanel.setLayout(new GridBagLayout());
        int valuePanelGridy = 0;

        final JLabel defaultIndicator = new JLabel(DEFAULT_INDICATOR_LABEL_OFF);
        defaultIndicator.setForeground(DEFAULT_INDICATOR_COLOR);
        defaultIndicator.setToolTipText(DEFAULT_INDICATOR_TOOLTIP);


        for (ParamValidValueInfo paramValidValueInfo : paramInfo.getValidValueInfos()) {
            if (paramValidValueInfo.getValue() != null && paramValidValueInfo.getValue().length() > 0) {
                createParamBitwiseCheckbox(paramInfo, paramValidValueInfo, valuePanel, valuePanelGridy, defaultIndicator);

                valuePanelGridy++;
            }
        }

        final JScrollPane valuesScroll = new JScrollPane(valuePanel);

        final JLabel jLabel = new JLabel(paramInfo.getName());
        jLabel.setToolTipText(paramInfo.getDescription());

        jPanel.add(jLabel,
                new GridBagConstraintsCustom(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        jPanel.add(defaultIndicator,
                new GridBagConstraintsCustom(1, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        jPanel.add(valuesScroll,
                new GridBagConstraintsCustom(2, gridy, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
    }


    private void createParamBitwiseCheckbox(final ParamInfo paramInfo,
                                            final ParamValidValueInfo paramValidValueInfo,
                                            JPanel jPanel,
                                            int gridy,
                                            final JLabel defaultIndicatorLabel) {

        final JCheckBox jCheckBox = new JCheckBox();
        final String param = paramInfo.getName();


        jCheckBox.setSelected(paramInfo.isBitwiseSelected(paramValidValueInfo));

        final JLabel jLabel = new JLabel(paramValidValueInfo.getValue() + " - " + paramValidValueInfo.getDescription());
        jLabel.setToolTipText(paramValidValueInfo.getValue() + " - " + paramValidValueInfo.getDescription());

        jPanel.add(jCheckBox,
                new GridBagConstraintsCustom(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        jPanel.add(jLabel,
                new GridBagConstraintsCustom(1, gridy, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));


        // add listener for current checkbox
        jCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String currValueString = l2genData.getParamValue(paramInfo);
                int currValue = Integer.parseInt(currValueString);
                String currValidValueString = paramValidValueInfo.getValue();
                int currValidValue = Integer.parseInt(currValidValueString);
                int newValue = currValue;

                if (currValidValue > 0) {
                    if (jCheckBox.isSelected()) {

                        newValue = (currValue | currValidValue);
                    } else {

                        if ((currValue & currValidValue) > 0) {
                            newValue = currValue - currValidValue;
                        }
                    }
                } else {
                    if (jCheckBox.isSelected()) {
                        newValue = 0;
                    } else {
                        if (!swingSentEventsDisabled) {
                            swingSentEventsDisabled = true;
                            l2genData.setParamToDefaults(paramInfo);
                            swingSentEventsDisabled = false;
                        }
                        return;
                    }
                }


                String newValueString = Integer.toString(newValue);

                debug("I heard you click param=" + param + " currVV=" + currValidValueString + " origValue=" + currValueString + "newValue=" + newValueString);

                if (!swingSentEventsDisabled) {
                    swingSentEventsDisabled = true;
                    l2genData.setParamValue(param, newValueString);
                    swingSentEventsDisabled = false;
                }
            }
        }

        );

        l2genData.addPropertyChangeListener(param, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                debug("receiving eventName " + param);

                int value = Integer.parseInt(paramValidValueInfo.getValue());

                if (value > 0) {
                    jCheckBox.setSelected(paramInfo.isBitwiseSelected(paramValidValueInfo));
                } else {
                    if (paramValidValueInfo.getValue().equals(l2genData.getParamValue(paramInfo))) {
                        jCheckBox.setSelected(true);
                    } else {
                        jCheckBox.setSelected(false);
                    }
                }


                if (l2genData.isParamDefault(param)) {
                    defaultIndicatorLabel.setText(DEFAULT_INDICATOR_LABEL_OFF);
                    defaultIndicatorLabel.setToolTipText("");
                } else {
                    defaultIndicatorLabel.setText(DEFAULT_INDICATOR_LABEL_ON);
                    defaultIndicatorLabel.setToolTipText(DEFAULT_INDICATOR_TOOLTIP);
                }
            }
        }

        );
    }


    private void createParamCheckBox(ParamInfo paramInfo, JPanel jPanel, int gridy) {
        final JCheckBox jCheckBox = new JCheckBox();
        final String param = paramInfo.getName();

        jCheckBox.setName(paramInfo.getName());


        if (paramInfo.getValue().equals(ParamInfo.BOOLEAN_TRUE)) {
            jCheckBox.setSelected(true);
        } else {
            jCheckBox.setSelected(false);
        }

        final JLabel jLabel = new JLabel(paramInfo.getName());
        jLabel.setToolTipText(paramInfo.getDescription());

        final JLabel defaultIndicator = new JLabel(DEFAULT_INDICATOR_LABEL_OFF);
        defaultIndicator.setForeground(DEFAULT_INDICATOR_COLOR);
        defaultIndicator.setToolTipText(DEFAULT_INDICATOR_TOOLTIP);


        jPanel.add(jLabel,
                new GridBagConstraintsCustom(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        jPanel.add(defaultIndicator,
                new GridBagConstraintsCustom(1, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        jPanel.add(jCheckBox,
                new GridBagConstraintsCustom(2, gridy, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        // add listener for current checkbox
        jCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                l2genData.setParamValue(param, jCheckBox.isSelected());
            }
        });

        l2genData.addPropertyChangeListener(param, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                debug("receiving eventName " + param);
                jCheckBox.setSelected(l2genData.getBooleanParamValue(param));

                if (l2genData.isParamDefault(param)) {
                    defaultIndicator.setText(DEFAULT_INDICATOR_LABEL_OFF);
                    defaultIndicator.setToolTipText("");
                } else {
                    defaultIndicator.setText(DEFAULT_INDICATOR_LABEL_ON);
                    defaultIndicator.setToolTipText(DEFAULT_INDICATOR_TOOLTIP);
                }
            }
        });
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