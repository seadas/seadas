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

    private ArrayList<JCheckBox> wavelengthsJCheckboxArrayList = null;

    private JPanel waveLimiterJPanel;
    private boolean initialConfiguration = true;

    final Color DEFAULT_INDICATOR_COLOR = new Color(0, 0, 120);

    final String DEFAULT_INDICATOR_TOOLTIP = "* Identicates that the selection is not the default value";
    final String DEFAULT_INDICATOR_LABEL_ON = " *  ";
    final String DEFAULT_INDICATOR_LABEL_OFF = "     ";
    final int PARAM_STRING_TEXTLEN = 60;
    final int PARAM_FILESTRING_TEXTLEN = 70;
    final int PARAM_INT_TEXTLEN = 15;
    final int PARAM_FLOAT_TEXTLEN = 15;

    private boolean waveLimiterControlHandlersEnabled = true;
    private boolean swingSentEventsDisabled = false;
    private boolean handleIfileJComboBoxEnabled = true;
    private boolean handleOfileSelecterEnabled = true;

    private JTextArea parStringTextArea;

    private JFileChooser parfileChooser = new JFileChooser();
    private JFileChooser geofileChooser = new JFileChooser();

    private DefaultMutableTreeNode rootNode;


    private int myTabCount = 0;

    private static final String MAIN_TAB_NAME = "Main";
    private static final String PRODUCTS_TAB_NAME = "Products";
    private static final int MAIN_TAB_INDEX = 0;
    private static final int PRODUCTS_TAB_INDEX = 1;

    private String WAVE_LIMITER_SELECT_ALL_INFRARED = "Select All Infrared";
    private String WAVE_LIMITER_DESELECT_ALL_INFRARED = "Deselect All Infrared";
    private String WAVE_LIMITER_SELECT_ALL_NEAR_INFRARED = "Select All Near-Infrared";
    private String WAVE_LIMITER_DESELECT_ALL_NEAR_INFRARED = "Deselect All Near-Infrared";
    private String WAVE_LIMITER_SELECT_ALL_VISIBLE = "Select All Visible";
    private String WAVE_LIMITER_DESELECT_ALL_VISIBLE = "Deselect All Visible";


    //  private JButton selectedProductsDefaultsButton;
    private JButton waveLimiterSelectAllInfrared;
    private JButton waveLimiterSelectAllVisible;
    private JButton waveLimiterSelectAllNearInfrared;

    private JTree productJTree;

    private L2genData l2genData = new L2genData();

    private ProcessorModel processorModel;

    int outputFilePanelHeight;
    int inputFilePanelHeight;


    L2genForm(AppContext appContext, String xmlFileName) {
        this.appContext = appContext;

        processorModel = new ProcessorModel("l2gen", xmlFileName);

        sourceProductSelector = new SourceProductFileSelector(VisatApp.getApp(), "");
        sourceProductSelector.initProducts();
        outputFileSelector = new OutputFileSelector(VisatApp.getApp(), "Output File");

        // determine whether ifile has been set prior to launching l2gen
        String ifile = null;
        if (sourceProductSelector.getSelectedProduct() != null
                && sourceProductSelector.getSelectedProduct().getFileLocation() != null) {
            ifile = sourceProductSelector.getSelectedProduct().getFileLocation().toString();

            if (ifile != null && ifile.length() > 0) {
                initialConfiguration = false;
            }
        }


        l2genData.initXmlBasedObjects();
        createUserInterface();
        addL2genDataListeners();

        initialConfiguration = false;

        if (ifile != null) {
            l2genData.setParamValue(L2genData.IFILE, ifile);
        } else {
            l2genData.fireAllParamEvents();
        }


    }
    //----------------------------------------------------------------------------------------
    // Create tabs within the main panel
    //----------------------------------------------------------------------------------------

    public ProcessorModel getProcessorModel() {
        processorModel.setParString(l2genData.getParString());
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

        mainPanel.add(createOutputFilePanel(),
                new GridBagConstraintsCustom(0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        mainPanel.add(createGeofileChooserPanel(),
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

        mainPanel.add(createParfilePanel(),
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 3));

        mainPanel.add(outputFileSelector.getOpenInAppCheckBox(),
                new GridBagConstraintsCustom(0, 2, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        addTab(tabname, mainPanel);
    }


    private JPanel createGeofileChooserPanel() {

        final JTextField jTextField = new JTextField("123456789 123456789 12345");
        jTextField.setPreferredSize(jTextField.getPreferredSize());
        jTextField.setMinimumSize(jTextField.getPreferredSize());
        jTextField.setMaximumSize(jTextField.getPreferredSize());
        jTextField.setText("");

        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                geofileTextfieldHandler(jTextField);
            }
        });

        jTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                geofileTextfieldHandler(jTextField);
            }
        });


        l2genData.addPropertyChangeListener(L2genData.GEOFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String geofileString = l2genData.getParamValue(L2genData.GEOFILE);
                if (geofileString == null || geofileString.equals(ParamInfo.NULL_STRING)) {
                    geofileChooser.setSelectedFile(null);

                } else {
                    geofileChooser.setSelectedFile(new File(geofileString));
                }

                jTextField.setText(geofileString);
            }
        });


        final JButton jButton = new JButton("...");

        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                geofileChooserHandler(geofileChooser);
            }
        });

        jButton.setMargin(new Insets(0, -7, 0, -7));
        Dimension size = new Dimension(jButton.getPreferredSize().width,
                jTextField.getPreferredSize().height);
        jButton.setPreferredSize(size);
        jButton.setMinimumSize(size);
        jButton.setMaximumSize(size);


        final JPanel mainPanel = new JPanel((new GridBagLayout()));
        mainPanel.add(new JLabel(L2genData.GEOFILE),
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
        mainPanel.add(jTextField,
                new GridBagConstraintsCustom(1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 2));
        mainPanel.add(jButton,
                new GridBagConstraintsCustom(2, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));


        Component[] geofilePanelComponents = mainPanel.getComponents();
        for (Component component : geofilePanelComponents) {
            component.setEnabled(false);
        }


        l2genData.addPropertyChangeListener(L2genData.IFILE_VALIDATION_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                boolean isSetNewValue = false;
                if (evt.getNewValue() != null) {
                    isSetNewValue = new Boolean((Boolean) evt.getNewValue());
                }

                Component[] geofilePanelComponents = mainPanel.getComponents();
                for (Component component : geofilePanelComponents) {
                    component.setEnabled(isSetNewValue);
                }
            }
        });

        return mainPanel;
    }


    private void geofileChooserHandler(JFileChooser jFileChooser) {
        int result = jFileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            l2genData.setParamValue(L2genData.GEOFILE, jFileChooser.getSelectedFile().toString());
        }
    }


    private void geofileTextfieldHandler(JTextField jTextField) {
        l2genData.setParamValue(L2genData.GEOFILE, jTextField.getText());
    }


    private JPanel createParfilePanel() {


        final JButton saveParfileButton = new JButton("Save");
        saveParfileButton.setEnabled(false);
        saveParfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = parfileChooser.showSaveDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    writeParfile();
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

                saveParfileButton.setEnabled(isSetNewValue);
            }
        });


        final JButton openParfileButton = new JButton("Open");
        openParfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = parfileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    uploadParfile();
                    debug("uploadParfile called");
                } else {
                    debug("uploadParfile not called");
                }

            }
        });


        final JCheckBox retainIfileOfileCheckbox = new JCheckBox("Retain Selected IFILE");
        retainIfileOfileCheckbox.setSelected(l2genData.isRetainCurrentIfile());
        retainIfileOfileCheckbox.setToolTipText("If an ifile is currently selected then any ifile entry in the parfile being opened will be ignored.");
        retainIfileOfileCheckbox.setEnabled(false);

        // add listener for current checkbox
        retainIfileOfileCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                l2genData.setRetainCurrentIfile(retainIfileOfileCheckbox.isSelected());
            }
        });

        l2genData.addPropertyChangeListener(l2genData.RETAIN_IFILE_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                retainIfileOfileCheckbox.setSelected(l2genData.isRetainCurrentIfile());
            }
        });


        l2genData.addPropertyChangeListener(L2genData.IFILE_VALIDATION_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                boolean isSetNewValue = false;
                if (evt.getNewValue() != null) {
                    isSetNewValue = new Boolean((Boolean) evt.getNewValue());
                }

                retainIfileOfileCheckbox.setEnabled(isSetNewValue);
            }
        });


        final JButton getAncButton = new JButton("Get Anc");
        getAncButton.setEnabled(false);
        getAncButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.setAncillaryFiles();
            }
        });


        l2genData.addPropertyChangeListener(L2genData.IFILE_VALIDATION_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                boolean isSetNewValue = false;
                if (evt.getNewValue() != null) {
                    isSetNewValue = new Boolean((Boolean) evt.getNewValue());
                }

                getAncButton.setEnabled(isSetNewValue);
            }
        });


        final JCheckBox showDefaultsCheckbox = new JCheckBox("Show Defaults");
        showDefaultsCheckbox.setSelected(l2genData.isShowDefaultsInParString());
        showDefaultsCheckbox.setToolTipText("Displays all the defaults with the parfile text region");
        showDefaultsCheckbox.setEnabled(false);

        // add listener for current checkbox
        showDefaultsCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                l2genData.setShowDefaultsInParString(showDefaultsCheckbox.isSelected());
            }
        });


        l2genData.addPropertyChangeListener(L2genData.IFILE_VALIDATION_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                boolean isSetNewValue = false;
                if (evt.getNewValue() != null) {
                    isSetNewValue = new Boolean((Boolean) evt.getNewValue());
                }

                showDefaultsCheckbox.setEnabled(isSetNewValue);
            }
        });


        l2genData.addPropertyChangeListener(l2genData.SHOW_DEFAULTS_IN_PARSTRING_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                showDefaultsCheckbox.setSelected(l2genData.isShowDefaultsInParString());
            }
        });


        parStringTextArea = new JTextArea();
        final JScrollPane parStringScrollArea = new JScrollPane(parStringTextArea);
        parStringTextArea.setEditable(true);
        parStringTextArea.setAutoscrolls(true);


        parStringTextArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                parStringTextAreaLostFocus();
            }
        });


        for (ParamInfo paramInfo : l2genData.getParamInfos()) {
            final String eventName = paramInfo.getName();
            l2genData.addPropertyChangeListener(eventName, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    parStringTextArea.setText(l2genData.getParString(l2genData.isShowDefaultsInParString()));
//                    parStringScrollArea.repaint();
//                    parStringScrollArea.updateUI();
//                    parStringScrollArea.getVerticalScrollBar().setValue(0);
//                    parStringScrollArea.getHorizontalScrollBar().setValue(0);
                }
            });
        }

        l2genData.addPropertyChangeListener(l2genData.SHOW_DEFAULTS_IN_PARSTRING_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                parStringTextArea.setText(l2genData.getParString(l2genData.isShowDefaultsInParString()));
//                parStringScrollArea.repaint();
//                parStringScrollArea.updateUI();
//                parStringScrollArea.getVerticalScrollBar().setValue(0);
//                parStringScrollArea.getHorizontalScrollBar().setValue(0);
            }
        });


        final JPanel buttonsSubPanel = new JPanel(new GridBagLayout());
        buttonsSubPanel.add(openParfileButton,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        buttonsSubPanel.add(retainIfileOfileCheckbox,
                new GridBagConstraintsCustom(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
        buttonsSubPanel.add(getAncButton,
                new GridBagConstraintsCustom(2, 0, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2));
        buttonsSubPanel.add(showDefaultsCheckbox,
                new GridBagConstraintsCustom(3, 0, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2));
        buttonsSubPanel.add(saveParfileButton,
                new GridBagConstraintsCustom(4, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));


        final JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Parfile"));
        mainPanel.add(buttonsSubPanel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        mainPanel.add(parStringScrollArea,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH));


        return mainPanel;
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

        l2genData.addPropertyChangeListener(param, new

                PropertyChangeListener() {
                    @Override
                    public void propertyChange
                            (PropertyChangeEvent
                                     evt) {
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


    private JPanel createWaveLimiterJPanel() {

        // ----------------------------------------------------------------------------------------
        // Create all Swing controls used on this tabbed panel
        // ----------------------------------------------------------------------------------------

        waveLimiterSelectAllInfrared = new JButton(WAVE_LIMITER_SELECT_ALL_INFRARED);

        waveLimiterSelectAllInfrared.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_SELECT_ALL_INFRARED)) {
                    l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.INFRARED, true);
                } else if (waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_DESELECT_ALL_INFRARED)) {
                    l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.INFRARED, false);
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.WAVE_LIMITER_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // Set INFRARED 'Select All' toggle to appropriate text
                if (l2genData.hasWaveType(WavelengthInfo.WaveType.INFRARED)) {
                    if (l2genData.isSelectedAllWaveLimiter(WavelengthInfo.WaveType.INFRARED)) {
                        if (!waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_DESELECT_ALL_INFRARED)) {
                            waveLimiterSelectAllInfrared.setText(WAVE_LIMITER_DESELECT_ALL_INFRARED);
                        }
                    } else {
                        if (!waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_SELECT_ALL_INFRARED)) {
                            waveLimiterSelectAllInfrared.setText(WAVE_LIMITER_SELECT_ALL_INFRARED);
                        }
                    }
                }
            }
        });


        waveLimiterSelectAllNearInfrared = new JButton(WAVE_LIMITER_SELECT_ALL_NEAR_INFRARED);

        waveLimiterSelectAllNearInfrared.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (waveLimiterSelectAllNearInfrared.getText().equals(WAVE_LIMITER_SELECT_ALL_NEAR_INFRARED)) {
                    l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.NEAR_INFRARED, true);
                } else if (waveLimiterSelectAllNearInfrared.getText().equals(WAVE_LIMITER_DESELECT_ALL_NEAR_INFRARED)) {
                    l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.NEAR_INFRARED, false);
                }
            }
        });

        waveLimiterSelectAllVisible = new JButton(WAVE_LIMITER_SELECT_ALL_VISIBLE);

        waveLimiterSelectAllVisible.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (waveLimiterSelectAllVisible.getText().equals(WAVE_LIMITER_SELECT_ALL_VISIBLE)) {
                    l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.VISIBLE, true);
                } else if (waveLimiterSelectAllVisible.getText().equals(WAVE_LIMITER_DESELECT_ALL_VISIBLE)) {
                    l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.VISIBLE, false);
                }
            }
        });


        waveLimiterJPanel = new JPanel(new GridBagLayout());


        // ----------------------------------------------------------------------------------------
        // Create mainPanel to hold all controls
        // ----------------------------------------------------------------------------------------

        final JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Wavelength Limiter"));
        mainPanel.setToolTipText("The wavelengths selected here are applied when you check a wavelength dependent product.  Not that any subsequent change ...");


        // Add to mainPanel grid cell

        mainPanel.add(waveLimiterSelectAllVisible,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

        mainPanel.add(waveLimiterSelectAllNearInfrared,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

        mainPanel.add(waveLimiterSelectAllInfrared,
                new GridBagConstraintsCustom(0, 2, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

        mainPanel.add(waveLimiterJPanel,
                new GridBagConstraintsCustom(0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH));


        return mainPanel;

    }


    private TristateCheckBox.State getCheckboxState(BaseInfo.State state) {
        switch (state) {
            case SELECTED:
                return TristateCheckBox.SELECTED;
            case PARTIAL:
                return TristateCheckBox.PARTIAL;
            default:
                return TristateCheckBox.NOT_SELECTED;
        }
    }

    private BaseInfo.State getInfoState(TristateCheckBox.State state) {
        if (state == TristateCheckBox.SELECTED) {
            return BaseInfo.State.SELECTED;
        }
        if (state == TristateCheckBox.PARTIAL) {
            return BaseInfo.State.PARTIAL;
        }
        return BaseInfo.State.NOT_SELECTED;

    }


    class CheckBoxNodeRenderer implements TreeCellRenderer {
        private JPanel nodeRenderer = new JPanel();
        private JLabel label = new JLabel();
        private TristateCheckBox check = new TristateCheckBox();

        Color selectionBorderColor, selectionForeground, selectionBackground,
                textForeground, textBackground;

        protected TristateCheckBox getJCheckBox() {
            return check;
        }

        public CheckBoxNodeRenderer() {
            Insets inset0 = new Insets(0, 0, 0, 0);
            check.setMargin(inset0);
            nodeRenderer.setLayout(new BorderLayout());
            nodeRenderer.add(check, BorderLayout.WEST);
            nodeRenderer.add(label, BorderLayout.CENTER);

            Font fontValue;
            fontValue = UIManager.getFont("Tree.font");
            if (fontValue != null) {
                check.setFont(fontValue);
                label.setFont(fontValue);
            }
            Boolean booleanValue = (Boolean) UIManager
                    .get("Tree.drawsFocusBorderAroundIcon");
            check.setFocusPainted((booleanValue != null)
                    && (booleanValue.booleanValue()));

            selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
            selectionForeground = UIManager.getColor("Tree.selectionForeground");
            selectionBackground = UIManager.getColor("Tree.selectionBackground");
            textForeground = UIManager.getColor("Tree.textForeground");
            textBackground = UIManager.getColor("Tree.textBackground");
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {

            String stringValue = null;
            BaseInfo.State state = BaseInfo.State.NOT_SELECTED;

            if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
                Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                if (userObject instanceof BaseInfo) {
                    BaseInfo info = (BaseInfo) userObject;
                    state = info.getState();
                    stringValue = info.getFullName();

                    tree.setToolTipText(info.getDescription());
                }
            }

            if (stringValue == null) {
                stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, false);
            }

            label.setText(stringValue);
            check.setState(getCheckboxState(state));
            check.setEnabled(tree.isEnabled());

            if (selected) {
                label.setForeground(selectionForeground);
                check.setForeground(selectionForeground);
                nodeRenderer.setForeground(selectionForeground);
                label.setBackground(selectionBackground);
                check.setBackground(selectionBackground);
                nodeRenderer.setBackground(selectionBackground);
            } else {
                label.setForeground(textForeground);
                check.setForeground(textForeground);
                nodeRenderer.setForeground(textForeground);
                label.setBackground(textBackground);
                check.setBackground(textBackground);
                nodeRenderer.setBackground(textBackground);
            }

//            if (((DefaultMutableTreeNode) value).getParent() == null) {
//                check.setVisible(false);
//            }

            BaseInfo baseInfo = (BaseInfo) ((DefaultMutableTreeNode) value).getUserObject();

            if (baseInfo instanceof ProductCategoryInfo) {
                check.setVisible(false);
            } else {
                check.setVisible(true);
            }

            return nodeRenderer;
        }
    }

    class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {

        CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
        JTree tree;
        DefaultMutableTreeNode currentNode;

        public CheckBoxNodeEditor(JTree tree) {
            this.tree = tree;

            // add a listener fo the check box
            ItemListener itemListener = new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    TristateCheckBox.State state = renderer.getJCheckBox().getState();

                    System.out.print("********** listener ***********\n");
                    System.out.print("checkbox - " + renderer.label.getText() + " - " + state.toString() + "\n");
                    //     System.out.print("    val - " + currentValue.getFullName() + " - " + currentValue.getState().toString() + "\n");

                    if (stopCellEditing()) {
                        fireEditingStopped();
                    }
                }
            };
            //renderer.getJCheckBox().addItemListener(itemListener);
            renderer.getJCheckBox().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    System.out.print("--changeListener\n");
                    if (stopCellEditing()) {
                        fireEditingStopped();
                    }
                }
            });
            renderer.getJCheckBox().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.print("--actionListener\n");
                }
            });


        }

        public Object getCellEditorValue() {

            TristateCheckBox.State state = renderer.getJCheckBox().getState();
//
//            System.out.print("-----getCellEditorValue - " + renderer.label.getText() + " - " + state.toString() + "\n");
//            System.out.print("    val - " + currentValue.getFullName() + " - " + currentValue.getState().toString() + "\n");
//            l2genData.setSelectedInfo(currentValue, getInfoState(state));
//            System.out.print("   aval - " + currentValue.getFullName() + " - " + currentValue.getState().toString() + "\n");

            setNodeState(currentNode, getInfoState(state));

            return currentNode.getUserObject();
        }

        public boolean isCellEditable(EventObject event) {

            return true;

        }

        public Component getTreeCellEditorComponent(JTree tree, Object value,
                                                    boolean selected, boolean expanded, boolean leaf, int row) {

            System.out.print("getTreeCellEditorComponent (" + Integer.toString(row) + ") " +
                    ((BaseInfo) ((DefaultMutableTreeNode) value).getUserObject()).getFullName() + "\n");

            if (value instanceof DefaultMutableTreeNode) {
                currentNode = (DefaultMutableTreeNode) value;
            }


            Component editor = renderer.getTreeCellRendererComponent(tree, value,
                    true, expanded, leaf, row, true);

            return editor;
        }

    }


    private TreeNode createTree() {

        DefaultMutableTreeNode productCategory, product, oldAlgorithm, algorithm = null, wavelength;

        oldAlgorithm = new DefaultMutableTreeNode();
        BaseInfo oldAInfo = null;

        rootNode = new DefaultMutableTreeNode(new BaseInfo());

        for (ProductCategoryInfo productCategoryInfo : l2genData.getProductCategoryInfos()) {
            if (productCategoryInfo.isVisible() && productCategoryInfo.hasChildren()) {
                productCategory = new DefaultMutableTreeNode(productCategoryInfo);
                rootNode.add(productCategory);
                for (BaseInfo pInfo : productCategoryInfo.getChildren()) {
                    product = new DefaultMutableTreeNode(pInfo);
                    for (BaseInfo aInfo : pInfo.getChildren()) {
                        algorithm = new DefaultMutableTreeNode(aInfo);

                        if (algorithm.toString().equals(oldAlgorithm.toString())) {
                            if (oldAInfo.hasChildren()) {
                                if (aInfo.hasChildren()) {
                                    algorithm = oldAlgorithm;
                                } else {
                                    oldAlgorithm.add(algorithm);
                                }
                            } else {
                                if (aInfo.hasChildren()) {
                                    product.remove(oldAlgorithm);
                                    algorithm.add(oldAlgorithm);
                                    product.add(algorithm);
                                }
                            }
                        } else {
                            product.add(algorithm);
                        }

                        for (BaseInfo wInfo : aInfo.getChildren()) {
                            wavelength = new DefaultMutableTreeNode(wInfo);
                            algorithm.add(wavelength);
                        }

                        oldAInfo = aInfo;
                        oldAlgorithm = algorithm;
                    }
                    if (pInfo.getChildren().size() == 1) {
                        productCategory.add(algorithm);
                    } else {
                        productCategory.add(product);
                    }
                }
            }
        }

        return rootNode;
    }


    public void checkTreeState(DefaultMutableTreeNode node) {

        l2genData.disableEvent(L2genData.L2PROD);
        BaseInfo info = (BaseInfo) node.getUserObject();
        BaseInfo.State newState = info.getState();

        if (node.getChildCount() > 0) {
            Enumeration<DefaultMutableTreeNode> enumeration = node.children();
            DefaultMutableTreeNode kid;
            boolean selectedFound = false;
            boolean notSelectedFound = false;
            while (enumeration.hasMoreElements()) {
                kid = enumeration.nextElement();
                checkTreeState(kid);

                BaseInfo childInfo = (BaseInfo) kid.getUserObject();

                switch (childInfo.getState()) {
                    case SELECTED:
                        selectedFound = true;
                        break;
                    case PARTIAL:
                        selectedFound = true;
                        notSelectedFound = true;
                        break;
                    case NOT_SELECTED:
                        notSelectedFound = true;
                        break;
                }
            }

            if (selectedFound && !notSelectedFound) {
                newState = BaseInfo.State.SELECTED;
            } else if (!selectedFound && notSelectedFound) {
                newState = BaseInfo.State.NOT_SELECTED;
            } else if (selectedFound && notSelectedFound) {
                newState = BaseInfo.State.PARTIAL;
            }

        } else {
            if (newState == BaseInfo.State.PARTIAL) {
                newState = BaseInfo.State.SELECTED;
                debug("in checkAlgorithmState converted newState to " + newState);
            }
        }

        if (newState != info.getState()) {
            l2genData.setSelectedInfo(info, newState);

        }

        l2genData.enableEvent(L2genData.L2PROD);
    }


    public void setNodeState(DefaultMutableTreeNode node, BaseInfo.State state) {

        debug("setNodeState called with state = " + state);

        if (node == null) {
            return;
        }

        BaseInfo info = (BaseInfo) node.getUserObject();

        if (state == info.getState()) {
            return;
        }

        l2genData.disableEvent(L2genData.L2PROD);

        if (node.getChildCount() > 0) {
            l2genData.setSelectedInfo(info, state);

            Enumeration<DefaultMutableTreeNode> enumeration = node.children();
            DefaultMutableTreeNode childNode;

            BaseInfo.State newState = state;

            while (enumeration.hasMoreElements()) {
                childNode = enumeration.nextElement();

                BaseInfo childInfo = (BaseInfo) childNode.getUserObject();

                if (childInfo instanceof WavelengthInfo) {
                    if (state == BaseInfo.State.PARTIAL) {
                        if (l2genData.compareWavelengthLimiter((WavelengthInfo) childInfo)) {
                            newState = BaseInfo.State.SELECTED;
                        } else {
                            newState = BaseInfo.State.NOT_SELECTED;
                        }
                    }
                }

                setNodeState(childNode, newState);
            }

            DefaultMutableTreeNode ancestorNode;
            DefaultMutableTreeNode targetNode = node;
            ancestorNode = (DefaultMutableTreeNode) node.getParent();

            while (ancestorNode.getParent() != null) {
                targetNode = ancestorNode;
                ancestorNode = (DefaultMutableTreeNode) ancestorNode.getParent();
            }

            checkTreeState(targetNode);

        } else {
            if (state == BaseInfo.State.PARTIAL) {
                l2genData.setSelectedInfo(info, BaseInfo.State.SELECTED);
            } else {
                l2genData.setSelectedInfo(info, state);
            }
        }

        l2genData.enableEvent(L2genData.L2PROD);
    }


    private void updateProductTreePanel() {

        TreeNode rootNode = createTree();
        productJTree.setModel(new DefaultTreeModel(rootNode, false));

    }


    private JPanel createProductSelectorJPanel() {

        TreeNode rootNode = createTree();
        productJTree = new JTree(rootNode);
        productJTree.setCellRenderer(new CheckBoxNodeRenderer());
        productJTree.setCellEditor(new CheckBoxNodeEditor(productJTree));
        productJTree.setEditable(true);
        productJTree.setShowsRootHandles(true);
        productJTree.setRootVisible(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Product Selector"));

        mainPanel.add(new JScrollPane(productJTree),
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH));

        return mainPanel;
    }


    private void createProductsTab(String tabname) {

        JPanel productSelectorJPanel = createProductSelectorJPanel();

        JPanel wavelengthsLimitorJPanel = createWaveLimiterJPanel();

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

        final JPanel panel = sourceProductSelector.createDefaultPanel();

        sourceProductSelector.setProductNameLabel(new JLabel(L2genData.IFILE));
        sourceProductSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "123456789 123456789 123456789 123456789 123456789 ");

        sourceProductSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {
                Product sourceProduct = getSourceProduct();


                if (sourceProduct != null && sourceProductSelector.getSelectedProduct() != null
                        && sourceProductSelector.getSelectedProduct().getFileLocation() != null) {
                    if (handleIfileJComboBoxEnabled) {
                        l2genData.setParamValue(L2genData.IFILE, sourceProductSelector.getSelectedProduct().getFileLocation().toString());
                    }
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


    private JPanel createOutputFilePanel() {

        outputFileSelector.setOutputFileNameLabel(new JLabel(L2genData.OFILE + " (name)"));
        outputFileSelector.setOutputFileDirLabel(new JLabel(L2genData.OFILE + " (directory)"));
        final JPanel panel = outputFileSelector.createDefaultPanel();


        outputFileSelector.setEnabled(false);


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
    // Swing Control Handlers
    //----------------------------------------------------------------------------------------


    private void parStringTextAreaLostFocus() {
        l2genData.setParString(parStringTextArea.getText().toString(), false);
        // reset the text
        // this is done here because events were fired only if params actually changed
        // changes to comments or param-case dont trigger an event
        // so setting the text here insures that this textarea is updated
        parStringTextArea.setText(l2genData.getParString(l2genData.isShowDefaultsInParString()));
    }


    public void uploadParfile() {

        final ArrayList<String> parfileTextLines = myReadDataFile(parfileChooser.getSelectedFile().toString());

        StringBuilder parfileText = new StringBuilder();

        for (String currLine : parfileTextLines) {
            debug(currLine);
            parfileText.append(currLine);
            parfileText.append("\n");
        }


        l2genData.setParString(parfileText.toString(), l2genData.isRetainCurrentIfile());
        parStringTextArea.setEditable(true);
        //  parStringTextArea.setText(parfileText.toString());
    }

    public void writeParfile() {

        try {
            // Create file
            FileWriter fstream = new FileWriter(parfileChooser.getSelectedFile().toString());
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(l2genData.getParString());
            //Close the output stream
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

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


        l2genData.addPropertyChangeListener(L2genData.WAVE_LIMITER_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateWaveLimiterSelectionStates();
            }
        });


        l2genData.addPropertyChangeListener(L2genData.L2PROD, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                productChangedHandler();
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


        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override

            public void propertyChange(PropertyChangeEvent evt) {
                //   if (enableIfileEvent) {

                System.out.println(L2genData.IFILE + " being handled");
                if (!initialConfiguration) {
                    ifileChangedEventHandler();
                }
                //   }
            }

        }

        );


    }

    private void enableTabs(boolean enabled) {
        for (int tabIndex = 1; tabIndex < myTabCount; tabIndex++) {
            this.setEnabledAt(tabIndex, enabled);
        }
    }

    private void updateWavelengthLimiterPanel() {

        wavelengthsJCheckboxArrayList = new ArrayList<JCheckBox>();

        waveLimiterJPanel.removeAll();

        // clear this because we dynamically rebuild it when input file selection is made or changed
        wavelengthsJCheckboxArrayList.clear();

        ArrayList<JCheckBox> wavelengthGroupCheckboxes = new ArrayList<JCheckBox>();

        for (WavelengthInfo waveLimiterInfo : l2genData.getWaveLimiterInfos()) {

            final String currWavelength = waveLimiterInfo.getWavelengthString();
            final JCheckBox currJCheckBox = new JCheckBox(currWavelength);

            currJCheckBox.setName(currWavelength);

            // add current JCheckBox to the externally accessible arrayList
            wavelengthsJCheckboxArrayList.add(currJCheckBox);

            // add listener for current checkbox
            currJCheckBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (waveLimiterControlHandlersEnabled) {
                        l2genData.setSelectedWaveLimiter(currWavelength, currJCheckBox.isSelected());
                    }
                }
            });

            wavelengthGroupCheckboxes.add(currJCheckBox);
        }

        waveLimiterSelectAllInfrared.setText(WAVE_LIMITER_SELECT_ALL_INFRARED);
        waveLimiterSelectAllNearInfrared.setText(WAVE_LIMITER_SELECT_ALL_NEAR_INFRARED);
        waveLimiterSelectAllVisible.setText(WAVE_LIMITER_SELECT_ALL_VISIBLE);

        if (l2genData.hasWaveType(WavelengthInfo.WaveType.INFRARED)) {
            waveLimiterSelectAllInfrared.setEnabled(true);
            l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.INFRARED, true);
        } else {
            waveLimiterSelectAllInfrared.setEnabled(false);
        }

        if (l2genData.hasWaveType(WavelengthInfo.WaveType.VISIBLE)) {
            waveLimiterSelectAllVisible.setEnabled(true);

            l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.VISIBLE, true);
        } else {
            waveLimiterSelectAllVisible.setEnabled(false);
        }

        if (l2genData.hasWaveType(WavelengthInfo.WaveType.NEAR_INFRARED)) {
            waveLimiterSelectAllNearInfrared.setEnabled(true);

            l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.NEAR_INFRARED, true);
        } else {
            waveLimiterSelectAllNearInfrared.setEnabled(false);
        }


        // some GridBagLayout formatting variables
        int gridyCnt = 0;
        int gridxCnt = 0;
        int NUMBER_OF_COLUMNS = 1;


        for (JCheckBox wavelengthGroupCheckbox : wavelengthGroupCheckboxes) {
            // add current JCheckBox to the panel

            waveLimiterJPanel.add(wavelengthGroupCheckbox,
                    new GridBagConstraintsCustom(gridxCnt, gridyCnt, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));


            // increment GridBag coordinates
            if (gridxCnt < (NUMBER_OF_COLUMNS - 1)) {
                gridxCnt++;
            } else {
                gridxCnt = 0;
                gridyCnt++;
            }
        }

        // updateWaveLimiterSelectionStates();
    }

    private void ifileChangedEventHandler() {

        updateWavelengthLimiterPanel();
        updateProductTreePanel();
        updateWaveLimiterSelectionStates();

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


    /**
     * Set all waveLimiterInfos controls to agree with l2genData
     */
    private void updateWaveLimiterSelectionStates() {

        // Turn off control handlers until all controls are set
        waveLimiterControlHandlersEnabled = false;

        // Set all checkboxes to agree with l2genData
        for (WavelengthInfo waveLimiterInfo : l2genData.getWaveLimiterInfos()) {
            for (JCheckBox currJCheckbox : wavelengthsJCheckboxArrayList) {
                if (waveLimiterInfo.getWavelengthString().equals(currJCheckbox.getName())) {
                    if (waveLimiterInfo.isSelected() != currJCheckbox.isSelected()) {
                        currJCheckbox.setSelected(waveLimiterInfo.isSelected());
                    }
                }
            }
        }

        // Set INFRARED 'Select All' toggle to appropriate text
        if (l2genData.hasWaveType(WavelengthInfo.WaveType.INFRARED)) {
            if (l2genData.isSelectedAllWaveLimiter(WavelengthInfo.WaveType.INFRARED)) {
                if (!waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_DESELECT_ALL_INFRARED)) {
                    waveLimiterSelectAllInfrared.setText(WAVE_LIMITER_DESELECT_ALL_INFRARED);
                }
            } else {
                if (!waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_SELECT_ALL_INFRARED)) {
                    waveLimiterSelectAllInfrared.setText(WAVE_LIMITER_SELECT_ALL_INFRARED);
                }
            }
        }

        // Set NEAR_INFRARED 'Select All' toggle to appropriate text
        if (l2genData.hasWaveType(WavelengthInfo.WaveType.NEAR_INFRARED)) {
            if (l2genData.isSelectedAllWaveLimiter(WavelengthInfo.WaveType.NEAR_INFRARED)) {
                if (!waveLimiterSelectAllNearInfrared.getText().equals(WAVE_LIMITER_DESELECT_ALL_NEAR_INFRARED)) {
                    waveLimiterSelectAllNearInfrared.setText(WAVE_LIMITER_DESELECT_ALL_NEAR_INFRARED);
                }
            } else {
                if (!waveLimiterSelectAllNearInfrared.getText().equals(WAVE_LIMITER_SELECT_ALL_NEAR_INFRARED)) {
                    waveLimiterSelectAllNearInfrared.setText(WAVE_LIMITER_SELECT_ALL_NEAR_INFRARED);
                }
            }
        }


        // Set VISIBLE 'Select All' toggle to appropriate text
        if (l2genData.hasWaveType(WavelengthInfo.WaveType.VISIBLE)) {
            if (l2genData.isSelectedAllWaveLimiter(WavelengthInfo.WaveType.VISIBLE)) {
                if (!waveLimiterSelectAllVisible.getText().equals(WAVE_LIMITER_DESELECT_ALL_VISIBLE)) {
                    waveLimiterSelectAllVisible.setText(WAVE_LIMITER_DESELECT_ALL_VISIBLE);
                }
            } else {
                if (!waveLimiterSelectAllVisible.getText().equals(WAVE_LIMITER_SELECT_ALL_VISIBLE)) {
                    waveLimiterSelectAllVisible.setText(WAVE_LIMITER_SELECT_ALL_VISIBLE);
                }
            }
        }


        // Turn on control handlers now that all controls are set
        waveLimiterControlHandlersEnabled = true;
    }


    private void productChangedHandler() {

        productJTree.treeDidChange();
        checkTreeState(rootNode);
    }


    //----------------------------------------------------------------------------------------
    // Some Generic stuff
    //----------------------------------------------------------------------------------------

    private ArrayList<String> myReadDataFile
            (String
                     fileName) {
        String lineData;
        ArrayList<String> fileContents = new ArrayList<String>();
        BufferedReader moFile = null;
        try {
            moFile = new BufferedReader(new FileReader(new File(fileName)));
            while ((lineData = moFile.readLine()) != null) {

                fileContents.add(lineData);
            }
        } catch (IOException e) {
            ;
        } finally {
            try {
                moFile.close();
            } catch (Exception e) {
                //Ignore
            }
        }
        return fileContents;
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