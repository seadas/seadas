/*
Author: Danny Knowles
    Don Shea
*/

package gov.nasa.gsfc.seadas.processing.l2gen;

import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.selection.AbstractSelectionChangeListener;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.ui.SourceProductSelector;
import org.esa.beam.framework.gpf.ui.TargetProductSelector;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.util.math.Array;

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


class L2genForm extends JTabbedPane {
    // note: line count=2865 before cleanup  now 1868
    private final AppContext appContext;
    private final SourceProductSelector sourceProductSelector;
    private final TargetProductSelector targetProductSelector;

    private ArrayList<JCheckBox> wavelengthsJCheckboxArrayList = null;
    //  private ArrayList<JCheckBox> paramJCheckboxes = null;
    private ArrayList<JPanel> paramOptionsJPanels = new ArrayList<JPanel>();

    private JPanel waveLimiterJPanel;

    final int PARAM_STRING_TEXTLEN = 15;
    final int PARAM_FILESTRING_TEXTLEN = 45;
    final int PARAM_INT_TEXTLEN = 15;
    final int PARAM_FLOAT_TEXTLEN = 15;

    //private JTable productsCartJTable;
    //   private SelectedProductsTableModel productsCartTableModel;
    //private JPanel productsCartJPanel;
    private JComboBox ofileJComboBox;

    private boolean waveLimiterControlHandlersEnabled = true;

    private boolean handleIfileJComboBoxEnabled = true;

    private JTextArea selectedProductsJTextArea;

    private JTextArea parfileTextEntryName;
    private JTextArea parfileTextEntryValue;
    private JButton parfileTextEntrySubmit;

    private JTextField spixlJTextField;
    private JTextField epixlJTextField;
    private JTextField dpixlJTextField;
    private JTextField slineJTextField;
    private JTextField elineJTextField;
    private JTextField dlineJTextField;

    private JTextField northJTextField;
    private JTextField southJTextField;
    private JTextField westJTextField;
    private JTextField eastJTextField;

    private JTextArea parfileJTextArea;

    private JFileChooser parfileChooser = new JFileChooser();

    private DefaultMutableTreeNode rootNode;

    private String SELECTED_PRODUCTS_JTEXT_AREA_DEFAULT = "No products currently selected";

    private int tabCount = 0;

    private static final String INPUT_OUTPUT_FILE_TAB_NAME = "Input/Output";
    private static final String PARFILE_TAB_NAME = "Parameters";
    private static final String SUB_SETTING_TAB_NAME = "Subsetting Options";
    private static final String PRODUCTS_TAB_NAME = "Products";


    private String WAVE_LIMITER_SELECT_ALL_INFRARED = "Select All Infrared";
    private String WAVE_LIMITER_DESELECT_ALL_INFRARED = "Deselect All Infrared";
    private String WAVE_LIMITER_SELECT_ALL_VISIBLE = "Select All Visible";
    private String WAVE_LIMITER_DESELECT_ALL_VISIBLE = "Deselect All Visible";


    private JButton selectedProductsDefaultsButton;
    private JButton selectedProductsEditLoadButton;
    private JButton selectedProductsCancelButton;
    private JButton waveLimiterSelectAllInfrared;
    private JButton waveLimiterSelectAllVisible;

    private JTree productJTree;

    private L2genData l2genData = new L2genData();
    private L2genReader l2genReader = new L2genReader(l2genData);

    enum DisplayMode {STANDARD_MODE, EDIT_MODE}

    private String EDIT_LOAD_BUTTON_TEXT_STANDARD_MODE = "Edit";
    private String EDIT_LOAD_BUTTON_TEXT_EDIT_MODE = "Load";


    L2genForm(TargetProductSelector targetProductSelector, AppContext appContext) {
        this.targetProductSelector = targetProductSelector;
        this.appContext = appContext;
        this.sourceProductSelector = new SourceProductSelector(appContext, "Source Product:");

        // determine whether ifile has been set prior to launching l2gen
        String ifile = null;
        if (sourceProductSelector.getSelectedProduct() != null
                && sourceProductSelector.getSelectedProduct().getFileLocation() != null) {
            ifile = sourceProductSelector.getSelectedProduct().getFileLocation().toString();
        }

        l2genData.initXmlBasedObjects();
        createUserInterface();
        addL2genDataListeners();

        if (ifile != null) {
            l2genData.setParamValue(l2genData.IFILE, ifile);
        }
    }


    //----------------------------------------------------------------------------------------
    // Create tabs within the main panel
    //----------------------------------------------------------------------------------------

    private void createUserInterface() {

        int currTabIndex = 0;
        createIOParametersTab(INPUT_OUTPUT_FILE_TAB_NAME);
        this.setEnabledAt(currTabIndex, true);

        currTabIndex++;
        createParfileTab(PARFILE_TAB_NAME);
        this.setEnabledAt(currTabIndex, false);

        currTabIndex++;
        createProductsTab(PRODUCTS_TAB_NAME);
        this.setEnabledAt(currTabIndex, false);

        currTabIndex++;
        createSubsampleTab(SUB_SETTING_TAB_NAME);
        this.setEnabledAt(currTabIndex, false);

        for (ParamCategoryInfo paramCategoryInfo : l2genData.getParamCategoryInfos()) {
            if (paramCategoryInfo.isVisible() && (paramCategoryInfo.getParamInfos().size() > 0)) {
                currTabIndex++;
                JPanel currJPanel = new JPanel();
                paramOptionsJPanels.add(currJPanel);
                createParamsTab(paramCategoryInfo, currJPanel);
                this.setEnabledAt(currTabIndex, false);
            }
        }

        tabCount = currTabIndex + 1;
    }


    //----------------------------------------------------------------------------------------
    // Methods to create each of the main  and sub tabs
    //----------------------------------------------------------------------------------------

    private void createIOParametersTab(String myTabname) {
        final TableLayout tableLayout = new TableLayout(1);
        tableLayout.setTableWeightX(1.0);
        tableLayout.setTableWeightY(0);
        tableLayout.setTableFill(TableLayout.Fill.BOTH);
        tableLayout.setTablePadding(3, 3);

        final JPanel ioPanel = new JPanel(tableLayout);
        ioPanel.add(createSourceProductPanel());
        ioPanel.add(createOfileJPanel());
        ioPanel.add(targetProductSelector.createDefaultPanel());
        ioPanel.add(tableLayout.createVerticalSpacer());

        addTab(myTabname, ioPanel);

    }


    private void createParfileTab(String myTabname) {

        // Define all Swing controls used on this tab page
        final JButton loadParfileButton = new JButton("Open");
        final JButton saveParfileButton = new JButton("Save");

        saveParfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = parfileChooser.showSaveDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    writeParfile();
                }
            }
        });

        loadParfileButton.addActionListener(new ActionListener() {
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

        parfileJTextArea = new JTextArea();
        parfileJTextArea.setEditable(true);
        parfileJTextArea.setBackground(Color.decode("#ffffff"));

        parfileJTextArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                parfileLostFocus();
            }
        });


        // Declare mainPanel and set it's attributes
        final JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder("Parfile"));
        mainPanel.setLayout(new GridBagLayout());

        // Add openButton control to a mainPanel grid cell
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
            mainPanel.add(loadParfileButton, c);
        }

        // Add saveButton control to a mainPanel grid cell
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 0;
            c.anchor = GridBagConstraints.EAST;
            mainPanel.add(saveParfileButton, c);
        }

        // Add textArea control to a mainPanel grid cell
        {
            JScrollPane scrollTextArea = new JScrollPane(parfileJTextArea);

            scrollTextArea.createHorizontalScrollBar();
            scrollTextArea.setMaximumSize(new Dimension(400, 400));

            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.fill = GridBagConstraints.BOTH;
            c.gridwidth = 2;
            c.weightx = 1;
            c.weighty = 1;
            mainPanel.add(scrollTextArea, c);
        }


        final JPanel finalMainPanel;
        finalMainPanel = SeadasGuiUtils.addPaddedWrapperPanel(mainPanel, 3);


        addTab(myTabname, finalMainPanel);
    }


    private void createParamsTab(ParamCategoryInfo paramCategoryInfo, JPanel paddedMainPanel) {

        final JPanel mainPanel = new JPanel();

        mainPanel.setLayout(new GridBagLayout());
        //    mainPanel.setBorder(BorderFactory.createTitledBorder("mainPanel"));

        int gridy = 0;
        for (ParamInfo paramInfo : paramCategoryInfo.getParamInfos()) {

//            if (paramInfo.hasValidValueInfos()) {
//                createParamComboBox(paramInfo, mainPanel, gridy);
//            } else {
//                if (paramInfo.getType() == ParamInfo.Type.BOOLEAN) {
//                    createParamCheckBox(paramInfo, mainPanel, gridy);
//                } else {
//                    createParamTextfield(paramInfo, mainPanel, gridy);
//                }
//            }

            gridy++;
        }

        final JPanel outerMainPanel = new JPanel();
        final JScrollPane jScrollPane = new JScrollPane(mainPanel);

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.NORTH;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 1;


            outerMainPanel.setLayout(new GridBagLayout());
            outerMainPanel.setBorder(BorderFactory.createTitledBorder(paramCategoryInfo.getName()));
            outerMainPanel.add(jScrollPane, c);
        }


        final GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = gridy;
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        final JPanel blankPanel = new JPanel();
        final JLabel jLabel = new JLabel("");
        blankPanel.add(jLabel);
        blankPanel.setLayout(new GridBagLayout());
        mainPanel.add(blankPanel, c);

        paddedMainPanel = SeadasGuiUtils.addPaddedWrapperPanel(outerMainPanel, 6);
        addTab(paramCategoryInfo.getName(), paddedMainPanel);
    }


    private void createParamTextfield(ParamInfo paramInfo, JPanel jPanel, int gridy) {


        final String param = paramInfo.getName();
        int jTextFieldLen = 15;


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


        JTextField jTextField = new JTextField(jTextFieldLen);
        final JTextField finalJTextField = jTextField;


        final JLabel jLabel = new JLabel(paramInfo.getName());
        jLabel.setToolTipText(paramInfo.getDescription());


        {
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = gridy;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.NORTHEAST;
            constraints.weightx = 0;
            constraints.weighty = 0;
            jPanel.add(jLabel, constraints);
        }

        {
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.gridy = gridy;

            if (jTextFieldLen == PARAM_FILESTRING_TEXTLEN) {
                constraints.fill = GridBagConstraints.HORIZONTAL;
            } else {
                constraints.fill = GridBagConstraints.NONE;
            }
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.weightx = 1;
            constraints.weighty = 0;

            jPanel.add(jTextField, constraints);
        }


        jTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                l2genData.setParamValue(param, finalJTextField.getText().toString());
            }
        });

        l2genData.addPropertyChangeListener(param, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                finalJTextField.setText(l2genData.getParamValue(param));
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
                if (-1 < index) {
                    list.setToolTipText(tooltips[index]);
                }
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
            return this;
        }

        public void setTooltips(String[] tooltips) {
            this.tooltips = tooltips;
        }
    }

    private void createParamComboBox(ParamInfo paramInfo, JPanel jPanel, int gridy) {

        final String param = paramInfo.getName();

        ArrayList<ParamValidValueInfo> validValuesArrayList = new ArrayList<ParamValidValueInfo>();
        ArrayList<String> validValuesToolTipsArrayList = new ArrayList<String>();

        for (ParamValidValueInfo paramValidValueInfo : paramInfo.getValidValueInfos()) {
            if (paramValidValueInfo.getValue() != null && paramValidValueInfo.getValue().length() > 0) {
                validValuesArrayList.add(paramValidValueInfo);
                validValuesToolTipsArrayList.add(paramValidValueInfo.getDescription());
            }
        }

//        final ParamValidValueInfo validValuesInfosArray[] = (ParamValidValueInfo[]) validValuesArrayList.toArray();

        final ParamValidValueInfo[] validValueInfosArray;
        validValueInfosArray = new ParamValidValueInfo[validValuesArrayList.size()];

        int i = 0;
        for (ParamValidValueInfo paramValidValueInfo : validValuesArrayList) {
            validValueInfosArray[i] = paramValidValueInfo;
            i++;
        }


        final String[] validValuesToolTipsArray;
        validValuesToolTipsArray = new String[validValuesArrayList.size()];

        int j = 0;
        for (String validValuesToolTip : validValuesToolTipsArrayList) {
            validValuesToolTipsArray[j] = validValuesToolTip;
            j++;
        }


        final JComboBox jComboBox = new JComboBox(validValueInfosArray);

        MyComboBoxRenderer myComboBoxRenderer = new MyComboBoxRenderer();
        myComboBoxRenderer.setTooltips(validValuesToolTipsArray);
        jComboBox.setRenderer(myComboBoxRenderer);

        final JLabel jLabel = new JLabel(paramInfo.getName());
        jLabel.setToolTipText(paramInfo.getDescription());


        {
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = gridy;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.NORTHEAST;
            constraints.weightx = 0;
            constraints.weighty = 0;
            jPanel.add(jLabel, constraints);
        }

        {
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.gridy = gridy;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.weightx = 1;
            constraints.weighty = 0;
            jPanel.add(jComboBox, constraints);
        }

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.setParamValue(param, jComboBox.getSelectedItem().toString());
            }
        });


        l2genData.addPropertyChangeListener(param, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                debug("receiving eventName " + param);
                for (ParamValidValueInfo paramValidValueInfo : validValueInfosArray) {
                    if (l2genData.getParamValue(param).equals(paramValidValueInfo.getValue())) {
                        jComboBox.setSelectedItem(paramValidValueInfo);
                    }
                }
            }
        });

    }


    private void createParamCheckBox(ParamInfo paramInfo, JPanel jPanel, int gridy) {
        final JCheckBox jCheckBox = new JCheckBox();
        final String paramName = paramInfo.getName();

        jCheckBox.setName(paramInfo.getName());

        //   paramJCheckboxes.add(jCheckBox);

        if (paramInfo.getValue().equals(ParamInfo.BOOLEAN_TRUE)) {
            jCheckBox.setSelected(true);
        } else {
            jCheckBox.setSelected(false);
        }

        final JLabel jLabel = new JLabel(paramInfo.getName());
        jLabel.setToolTipText(paramInfo.getDescription());


        {
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = gridy;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.NORTHEAST;
            constraints.weightx = 0;
            constraints.weighty = 0;
            jPanel.add(jLabel, constraints);
        }

        {
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.gridy = gridy;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.weightx = 0;
            constraints.weighty = 0;


            jPanel.add(jCheckBox, constraints);
        }


        // add listener for current checkbox
        jCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                l2genData.setParamValue(paramName, jCheckBox.isSelected());
            }
        });

        l2genData.addPropertyChangeListener(paramName, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                debug("receiving eventName " + paramName);
                jCheckBox.setSelected(l2genData.getBooleanParamValue(paramName));
            }
        });
    }


    private void createSubsampleTab(String tabnameSubsample) {

        final JTabbedPane tabbedPane = new JTabbedPane();

        final JPanel latlonJPanel = new JPanel();
        final JPanel pixelLinesJPanel = new JPanel();

        createLatLonPane(latlonJPanel);
        createPixelsLinesPane(pixelLinesJPanel);


        // Declare mainPanel and set it's attributes
        final JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(""));
        mainPanel.setLayout(new GridBagLayout());


        // Add Swing controls to mainPanel grid cells
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 0;
            c.weighty = 0;
            mainPanel.add(latlonJPanel, c);
        }


        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1;
            c.weighty = 1;
            mainPanel.add(pixelLinesJPanel, c);
        }


        final JPanel paddedMainPanel;
        paddedMainPanel = SeadasGuiUtils.addPaddedWrapperPanel(mainPanel, 6);

        addTab(tabnameSubsample, paddedMainPanel);
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


        waveLimiterJPanel = new JPanel();
        // wavelengthsJPanel.setBorder(BorderFactory.createTitledBorder("Wavelength Limiter"));
        waveLimiterJPanel.setLayout(new GridBagLayout());


        // ----------------------------------------------------------------------------------------
        // Create mainPanel to hold all controls
        // ----------------------------------------------------------------------------------------

        final JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder("Wavelength Limiter"));

        mainPanel.setLayout(new GridBagLayout());

        // Add to mainPanel grid cell

        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(waveLimiterSelectAllVisible, c);

        c = SeadasGuiUtils.makeConstraints(0, 1);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(waveLimiterSelectAllInfrared, c);

        c = SeadasGuiUtils.makeConstraints(0, 2);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(waveLimiterJPanel, c);


        // ----------------------------------------------------------------------------------------
        // Create wrappedMainPanel to hold mainPanel: this is a formatting wrapper panel
        // ----------------------------------------------------------------------------------------

        JPanel wrappedMainPanel = SeadasGuiUtils.addWrapperPanel(mainPanel);


        // ----------------------------------------------------------------------------------------
        // Add wrappedMainPanel to tabbedPane
        // ----------------------------------------------------------------------------------------

        // tabbedPane.addTab(myTabname, wrappedMainPanel);
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

        DefaultMutableTreeNode product, oldAlgorithm, algorithm = null, wavelength;

        oldAlgorithm = new DefaultMutableTreeNode();
        BaseInfo oldAInfo = null;

        rootNode = new DefaultMutableTreeNode(new BaseInfo());

        for (ProductInfo productInfo : l2genData.getProductInfos()) {
            product = new DefaultMutableTreeNode(productInfo);
            for (BaseInfo aInfo : productInfo.getChildren()) {
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
            if (productInfo.getChildren().size() == 1) {
                rootNode.add(algorithm);
            } else {
                rootNode.add(product);
            }
        }

        return rootNode;
    }


    public void checkTreeState(DefaultMutableTreeNode node) {

        l2genData.disableEvent(l2genData.L2PROD);
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

        l2genData.enableEvent(l2genData.L2PROD);
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

        l2genData.disableEvent(l2genData.L2PROD);

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

        l2genData.enableEvent(l2genData.L2PROD);
    }


    private void updateProductTreePanel() {

        TreeNode rootNode = createTree();
        productJTree.setModel(new DefaultTreeModel(rootNode, false));

    }


    private void createProductsTab(String myTabname) {


        JPanel wavelengthsLimitorJPanel = createWaveLimiterJPanel();
        //    JPanel selectedProductsCartJPanel = new JPanel();
        //   createSelectedProductsCartJPanelNew(selectedProductsCartJPanel);

        TreeNode rootNode = createTree();
        productJTree = new JTree(rootNode);

        CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
        productJTree.setCellRenderer(renderer);

        productJTree.setCellEditor(new CheckBoxNodeEditor(productJTree));
        productJTree.setEditable(true);
        productJTree.setShowsRootHandles(true);
        productJTree.setRootVisible(false);


        final JPanel selectedProductsJPanel = new JPanel();
        createSelectedProductsJPanel(selectedProductsJPanel);


        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints c;


        JPanel treeBorderJPanel = new JPanel();
        treeBorderJPanel.setBorder(BorderFactory.createTitledBorder("Product Selector"));
        treeBorderJPanel.setLayout(new GridBagLayout());

        c = SeadasGuiUtils.makeConstraints(0, 0);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        treeBorderJPanel.add(new JScrollPane(productJTree), c);

        c = SeadasGuiUtils.makeConstraints(0, 0);
        c.anchor = GridBagConstraints.NORTHWEST;
        // c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
//        mainPanel.add(new JScrollPane(treeTable), c);
        mainPanel.add(treeBorderJPanel, c);


        c = SeadasGuiUtils.makeConstraints(1, 0);
        c.anchor = GridBagConstraints.NORTHEAST;
        // c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1;
        c.weighty = 1;
//        mainPanel.add(new JScrollPane(treeTable), c);
        mainPanel.add(wavelengthsLimitorJPanel, c);


        // Add to mainPanel grid cell

        c = SeadasGuiUtils.makeConstraints(0, 1);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 2;
        mainPanel.add(selectedProductsJPanel, c);


//        c = SeadasGuiUtils.makeConstraints(1, 0);
//        c.anchor = GridBagConstraints.NORTHEAST;
//        // c.insets = new Insets(3, 3, 3, 3);
//        c.fill = GridBagConstraints.BOTH;
//        c.weightx = 1;
//        c.weighty = 1;
////        mainPanel.add(new JScrollPane(treeTable), c);
//        mainPanel.add(selectedProductsCartJPanel, c);
//


        addTab(myTabname, mainPanel);

    }


    //----------------------------------------------------------------------------------------
    // Methods involving Panels
    //----------------------------------------------------------------------------------------

    private JPanel createSourceProductPanel() {
        final JPanel panel = sourceProductSelector.createDefaultPanel();

        sourceProductSelector.getProductNameLabel().setText("Name:");
        sourceProductSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "MER_RR__1PPBCM20030730_071000_000003972018_00321_07389_0000.N1");

        sourceProductSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {
                Product sourceProduct = getSourceProduct();

                if (handleIfileJComboBoxEnabled) {
                    //   updateTargetProductName(sourceProduct);
                }


                if (sourceProduct != null && sourceProductSelector.getSelectedProduct() != null
                        && sourceProductSelector.getSelectedProduct().getFileLocation() != null) {
                    if (handleIfileJComboBoxEnabled) {
                        //   l2genData.setParamValue(l2genData.IFILE, sourceProductSelector.getSelectedProduct().getName());
                        l2genData.setParamValue(l2genData.IFILE, sourceProductSelector.getSelectedProduct().getFileLocation().toString());
                    }
                }
            }
        });
        return panel;
    }


    private void createLatLonPane(JPanel inPanel) {

        // ----------------------------------------------------------------------------------------
        // Set all constants for this tabbed pane
        // ----------------------------------------------------------------------------------------

        final int COORDINATES_JTEXTFIELD_LENGTH = 5;

        final String COORDINATES_PANEL_TITLE = "Coordinates";

        final String NORTH_LABEL = "N";
        final String SOUTH_LABEL = "S";
        final String EAST_LABEL = "E";
        final String WEST_LABEL = "W";


        // ----------------------------------------------------------------------------------------
        // Create all Swing controls used on this tabbed panel
        // ----------------------------------------------------------------------------------------

        northJTextField = new JTextField(COORDINATES_JTEXTFIELD_LENGTH);
        southJTextField = new JTextField(COORDINATES_JTEXTFIELD_LENGTH);
        westJTextField = new JTextField(COORDINATES_JTEXTFIELD_LENGTH);
        eastJTextField = new JTextField(COORDINATES_JTEXTFIELD_LENGTH);


        // ----------------------------------------------------------------------------------------
        // Add lose focus listeners to all JTextField components
        // ----------------------------------------------------------------------------------------

        northJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                northLostFocus();
            }
        });

        southJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                southLostFocus();
            }
        });

        westJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                westLostFocus();
            }
        });

        eastJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                eastLostFocus();
            }
        });


        // ----------------------------------------------------------------------------------------
        // Create labels for all Swing controls used on this tabbed panel
        // ----------------------------------------------------------------------------------------

        final JLabel northLabel = new JLabel(NORTH_LABEL);
        final JLabel southLabel = new JLabel(SOUTH_LABEL);
        final JLabel westLabel = new JLabel(WEST_LABEL);
        final JLabel eastLabel = new JLabel(EAST_LABEL);


        // ----------------------------------------------------------------------------------------
        // Create mainPanel to hold all controls
        // ----------------------------------------------------------------------------------------

        //     final JPanel inPanel = new JPanel();
        inPanel.setBorder(BorderFactory.createTitledBorder(COORDINATES_PANEL_TITLE));
        inPanel.setLayout(new GridBagLayout());

        inPanel.add(northJTextField,
                SeadasGuiUtils.makeConstraints(2, 1, GridBagConstraints.NORTH));

        inPanel.add(southJTextField,
                SeadasGuiUtils.makeConstraints(2, 3, GridBagConstraints.SOUTH));

        inPanel.add(eastJTextField,
                SeadasGuiUtils.makeConstraints(3, 2, GridBagConstraints.EAST));

        inPanel.add(westJTextField,
                SeadasGuiUtils.makeConstraints(1, 2, GridBagConstraints.WEST));

        inPanel.add(northLabel,
                SeadasGuiUtils.makeConstraints(2, 0, GridBagConstraints.SOUTH));

        inPanel.add(southLabel,
                SeadasGuiUtils.makeConstraints(2, 4, GridBagConstraints.NORTH));

        inPanel.add(eastLabel,
                SeadasGuiUtils.makeConstraints(4, 2, GridBagConstraints.WEST));

        inPanel.add(westLabel,
                SeadasGuiUtils.makeConstraints(0, 2, GridBagConstraints.EAST));


        // ----------------------------------------------------------------------------------------
        // Create wrappedMainPanel to hold mainPanel: this is a formatting wrapper panel
        // ----------------------------------------------------------------------------------------

        final JPanel wrappedMainPanel = new JPanel();
        wrappedMainPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1;
        c.weighty = 1;

        wrappedMainPanel.add(inPanel, c);


        // ----------------------------------------------------------------------------------------
        // Add wrappedMainPanel to tabbedPane
        // ----------------------------------------------------------------------------------------

    }


    private void createPixelsLinesPane(JPanel mainPanel) {

        // ---------------------------------------------------------------------------------------- 
        // Set all constants for this tabbed pane
        // ---------------------------------------------------------------------------------------- 

        final int PIXELS_JTEXTFIELD_LENGTH = 5;
        final int LINES_JTEXTFIELD_LENGTH = 5;

        final String PIXELS_PANEL_TITLE = "";
        final String LINES_PANEL_TITLE = "";

        final String SPIXL_LABEL = "start pix";
        final String EPIXL_LABEL = "end pix";
        final String DPIXL_LABEL = "delta pix";
        final String SLINE_LABEL = "start line";
        final String ELINE_LABEL = "end line";
        final String DLINE_LABEL = "delta line";


        // ---------------------------------------------------------------------------------------- 
        // Create all Swing controls used on this tabbed panel
        // ---------------------------------------------------------------------------------------- 

        spixlJTextField = new JTextField(PIXELS_JTEXTFIELD_LENGTH);
        epixlJTextField = new JTextField(PIXELS_JTEXTFIELD_LENGTH);
        dpixlJTextField = new JTextField(PIXELS_JTEXTFIELD_LENGTH);

        slineJTextField = new JTextField(LINES_JTEXTFIELD_LENGTH);
        elineJTextField = new JTextField(LINES_JTEXTFIELD_LENGTH);
        dlineJTextField = new JTextField(LINES_JTEXTFIELD_LENGTH);


        // ---------------------------------------------------------------------------------------- 
        // Add lose focus listeners to all JTextField components
        // ---------------------------------------------------------------------------------------- 

        spixlJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                spixlLostFocus();
            }
        });


        epixlJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                epixlLostFocus();
            }
        });

        dpixlJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                dpixlLostFocus();
            }
        });

        slineJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                slineLostFocus();
            }
        });

        elineJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                elineLostFocus();
            }
        });

        dlineJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                dlineLostFocus();
            }
        });


        // ---------------------------------------------------------------------------------------- 
        // Create labels for all Swing controls used on this tabbed panel
        // ---------------------------------------------------------------------------------------- 

        final JLabel spixlJLabel = new JLabel(SPIXL_LABEL);
        final JLabel epixlJLabel = new JLabel(EPIXL_LABEL);
        final JLabel dpixlJLabel = new JLabel(DPIXL_LABEL);

        final JLabel slineJLabel = new JLabel(SLINE_LABEL);
        final JLabel elineJLabel = new JLabel(ELINE_LABEL);
        final JLabel dlineJLabel = new JLabel(DLINE_LABEL);


        // ---------------------------------------------------------------------------------------- 
        // Create pixelsPanel to hold all pixel controls
        // ---------------------------------------------------------------------------------------- 

        final JPanel pixelsPanel = new JPanel();
        //     pixelsPanel.setBorder(BorderFactory.createTitledBorder(PIXELS_PANEL_TITLE));
        pixelsPanel.setLayout(new GridBagLayout());

        pixelsPanel.add(spixlJLabel,
                SeadasGuiUtils.makeConstraints(0, 0, GridBagConstraints.EAST));
        pixelsPanel.add(spixlJTextField,
                SeadasGuiUtils.makeConstraints(1, 0));

        pixelsPanel.add(epixlJLabel,
                SeadasGuiUtils.makeConstraints(0, 1, GridBagConstraints.EAST));
        pixelsPanel.add(epixlJTextField,
                SeadasGuiUtils.makeConstraints(1, 1));

        pixelsPanel.add(dpixlJLabel,
                SeadasGuiUtils.makeConstraints(0, 2, GridBagConstraints.EAST));
        pixelsPanel.add(dpixlJTextField,
                SeadasGuiUtils.makeConstraints(1, 2));


        // ----------------------------------------------------------------------------------------
        // Create linesPanel to hold all lines controls
        // ----------------------------------------------------------------------------------------         

        final JPanel linesPanel = new JPanel();
        //   linesPanel.setBorder(BorderFactory.createTitledBorder(LINES_PANEL_TITLE));
        linesPanel.setLayout(new GridBagLayout());

        linesPanel.add(slineJLabel,
                SeadasGuiUtils.makeConstraints(0, 0, GridBagConstraints.EAST));
        linesPanel.add(slineJTextField,
                SeadasGuiUtils.makeConstraints(1, 0));

        linesPanel.add(elineJLabel,
                SeadasGuiUtils.makeConstraints(0, 1, GridBagConstraints.EAST));
        linesPanel.add(elineJTextField,
                SeadasGuiUtils.makeConstraints(1, 1));

        linesPanel.add(dlineJLabel,
                SeadasGuiUtils.makeConstraints(0, 2, GridBagConstraints.EAST));
        linesPanel.add(dlineJTextField,
                SeadasGuiUtils.makeConstraints(1, 2));


        // ---------------------------------------------------------------------------------------- 
        // Create mainPanel to hold pixelsPanel and linesPanel
        // ---------------------------------------------------------------------------------------- 


        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Pixel-Lines"));

        GridBagConstraints constraints = SeadasGuiUtils.makeConstraints(0, 0);
        constraints.insets = new Insets(3, 3, 3, 3);
        mainPanel.add(pixelsPanel, constraints);

        constraints = SeadasGuiUtils.makeConstraints(1, 0);
        constraints.insets = new Insets(3, 3, 3, 3);
        mainPanel.add(linesPanel, constraints);


        // ---------------------------------------------------------------------------------------- 
        // Create wrappedMainPanel to hold mainPanel: this is a formatting wrapper panel
        // ---------------------------------------------------------------------------------------- 

        final JPanel wrappedMainPanel = new JPanel();
        wrappedMainPanel.setLayout(new GridBagLayout());

        constraints = SeadasGuiUtils.makeConstraints(0, 0);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(3, 3, 3, 3);
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 1;
        constraints.weighty = 1;

        wrappedMainPanel.add(mainPanel, constraints);


        // ---------------------------------------------------------------------------------------- 
        // Add wrappedMainPanel to tabbedPane
        // ---------------------------------------------------------------------------------------- 


    }


    private void createSelectedProductsJPanel(JPanel selectedProductsPanel) {


        selectedProductsPanel.setBorder(BorderFactory.createTitledBorder("Selected Products"));
        selectedProductsPanel.setLayout(new GridBagLayout());

        selectedProductsDefaultsButton = new JButton("Apply Defaults");
        selectedProductsEditLoadButton = new JButton();
        selectedProductsCancelButton = new JButton("Cancel");


        selectedProductsJTextArea = new JTextArea(SELECTED_PRODUCTS_JTEXT_AREA_DEFAULT);
        selectedProductsJTextArea.setLineWrap(true);
        selectedProductsJTextArea.setWrapStyleWord(true);
        selectedProductsJTextArea.setColumns(20);
        selectedProductsJTextArea.setRows(5);

        setDisplayModeSelectedProducts(DisplayMode.STANDARD_MODE);

        JPanel panel1 = SeadasGuiUtils.addPaddedWrapperPanel(selectedProductsDefaultsButton, 3, GridBagConstraints.WEST);
        JPanel panel2 = SeadasGuiUtils.addPaddedWrapperPanel(selectedProductsEditLoadButton, 3, GridBagConstraints.CENTER);
        JPanel panel3 = SeadasGuiUtils.addPaddedWrapperPanel(selectedProductsCancelButton, 3, GridBagConstraints.EAST);


        selectedProductsDefaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.copyFromProductDefaults();
            }
        });


        selectedProductsEditLoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (EDIT_LOAD_BUTTON_TEXT_EDIT_MODE.equals(selectedProductsEditLoadButton.getText())) {
                    l2genData.setParamValue(l2genData.L2PROD, selectedProductsJTextArea.getText());
                    selectedProductsJTextArea.setText(l2genData.getParamValue(l2genData.L2PROD));
                    setDisplayModeSelectedProducts(DisplayMode.STANDARD_MODE);
                } else {
                    setDisplayModeSelectedProducts(DisplayMode.EDIT_MODE);
                }
            }
        });


        selectedProductsCancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedProductsJTextArea.setText(l2genData.getParamValue(l2genData.L2PROD));

                setDisplayModeSelectedProducts(DisplayMode.STANDARD_MODE);
            }
        });


        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = 3;
        selectedProductsPanel.add(selectedProductsJTextArea, c);


        c = SeadasGuiUtils.makeConstraints(0, 1);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        selectedProductsPanel.add(panel1, c);

        c = SeadasGuiUtils.makeConstraints(1, 1);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        selectedProductsPanel.add(panel2, c);

        c = SeadasGuiUtils.makeConstraints(2, 1);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        selectedProductsPanel.add(panel3, c);
    }

    private JPanel createOfileJPanel() {

        JPanel selectedProductsPanel = new JPanel();
        selectedProductsPanel.setBorder(BorderFactory.createTitledBorder("Output File"));
        selectedProductsPanel.setLayout(new GridBagLayout());

        ofileJComboBox = new JComboBox();
// todo add listener to this

        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        selectedProductsPanel.add(ofileJComboBox, c);

        return selectedProductsPanel;
    }


    //----------------------------------------------------------------------------------------
    // Methods involved with the Parfile Tab
    //----------------------------------------------------------------------------------------

    public void loadParfileEntry() {
        System.out.println(parfileTextEntryName.getText() + "=" + parfileTextEntryValue.getText());
        l2genData.setParamValue(parfileTextEntryName.getText(), parfileTextEntryValue.getText());
        System.out.println("ifile=" + l2genData.getParamValue(l2genData.IFILE));
    }


    //----------------------------------------------------------------------------------------
    // Methods involved with the Product Tab
    //----------------------------------------------------------------------------------------


    private void setDisplayModeSelectedProducts(DisplayMode displayMode) {

        if (displayMode == DisplayMode.STANDARD_MODE) {
            selectedProductsJTextArea.setEditable(false);
            selectedProductsJTextArea.setBackground(Color.decode("#dddddd"));
            selectedProductsEditLoadButton.setText(EDIT_LOAD_BUTTON_TEXT_STANDARD_MODE);
            selectedProductsDefaultsButton.setEnabled(true);
            selectedProductsCancelButton.setVisible(false);
        } else if (displayMode == DisplayMode.EDIT_MODE) {
            selectedProductsJTextArea.setEditable(true);
            selectedProductsJTextArea.setBackground(Color.decode("#ffffff"));
            selectedProductsEditLoadButton.setText(EDIT_LOAD_BUTTON_TEXT_EDIT_MODE);
            selectedProductsDefaultsButton.setEnabled(false);
            selectedProductsCancelButton.setVisible(true);
        }
    }


    //----------------------------------------------------------------------------------------
    // Swing Control Handlers
    //----------------------------------------------------------------------------------------

    private void spixlLostFocus() {
        l2genData.setParamValue(l2genData.SPIXL, spixlJTextField.getText().toString());
    }

    private void epixlLostFocus() {
        l2genData.setParamValue(l2genData.EPIXL, epixlJTextField.getText().toString());
    }

    private void dpixlLostFocus() {
        l2genData.setParamValue(l2genData.DPIXL, dpixlJTextField.getText().toString());
    }

    private void slineLostFocus() {
        l2genData.setParamValue(l2genData.SLINE, slineJTextField.getText().toString());
    }

    private void elineLostFocus() {
        l2genData.setParamValue(l2genData.ELINE, elineJTextField.getText().toString());
    }

    private void dlineLostFocus() {
        l2genData.setParamValue(l2genData.DLINE, dlineJTextField.getText().toString());
    }

    private void northLostFocus() {
        l2genData.setParamValue(l2genData.NORTH, northJTextField.getText().toString());
    }

    private void southLostFocus() {
        l2genData.setParamValue(l2genData.SOUTH, southJTextField.getText().toString());
    }

    private void westLostFocus() {
        l2genData.setParamValue(l2genData.WEST, westJTextField.getText().toString());
    }

    private void eastLostFocus() {
        l2genData.setParamValue(l2genData.EAST, eastJTextField.getText().toString());
    }

    private void parfileLostFocus() {
        l2genData.setParamsFromParfile(parfileJTextArea.getText().toString());
    }


    public void uploadParfile() {

        final ArrayList<String> parfileTextLines = myReadDataFile(parfileChooser.getSelectedFile().toString());

        StringBuilder parfileText = new StringBuilder();

        for (String currLine : parfileTextLines) {
            debug(currLine);
            parfileText.append(currLine);
            parfileText.append("\n");
        }

        l2genData.setParamsFromParfile(parfileText.toString());
        parfileJTextArea.setEditable(true);
        //  parfileJTextArea.setText(parfileText.toString());
    }

    public void writeParfile() {

        try {
            // Create file
            FileWriter fstream = new FileWriter(parfileChooser.getSelectedFile().toString());
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(l2genData.getParfile());
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

        for (ParamCategoryInfo paramCategoryInfo : l2genData.getParamCategoryInfos()) {
            if (paramCategoryInfo.isVisible()) {
                for (ParamInfo paramInfo : paramCategoryInfo.getParamInfos()) {
                    final String eventName = paramInfo.getName();

                    debug("Making listener for " + eventName);
                    l2genData.addPropertyChangeListener(eventName, new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            debug("receiving eventName " + eventName);
                            parfileJTextArea.setText(l2genData.getParfile());
//                            for (JCheckBox jCheckBox : paramJCheckboxes) {
//                                if (jCheckBox.getName().equals(eventName)) {
//                                    jCheckBox.setSelected(l2genData.getBooleanParamValue(eventName));
//                                }
//                            }
                        }
                    });
                }
            }
        }


//        l2genData.addPropertyChangeListener(l2genData.MISSION_CHANGE_EVENT, new PropertyChangeListener() {
//            @Override
//            public void propertyChange(PropertyChangeEvent evt) {
//
//            }
//        });


        l2genData.addPropertyChangeListener(l2genData.INVALID_IFILE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                invalidIfileEvent();
            }
        });


        l2genData.addPropertyChangeListener(l2genData.SPIXL, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                spixlJTextField.setText(l2genData.getParamValue(l2genData.SPIXL));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });

        l2genData.addPropertyChangeListener(l2genData.EPIXL, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                epixlJTextField.setText(l2genData.getParamValue(l2genData.EPIXL));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });

        l2genData.addPropertyChangeListener(l2genData.DPIXL, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                dpixlJTextField.setText(l2genData.getParamValue(l2genData.DPIXL));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });

        l2genData.addPropertyChangeListener(l2genData.SLINE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                slineJTextField.setText(l2genData.getParamValue(l2genData.SLINE));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });

        l2genData.addPropertyChangeListener(l2genData.ELINE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                elineJTextField.setText(l2genData.getParamValue(l2genData.ELINE));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });

        l2genData.addPropertyChangeListener(l2genData.DLINE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                dlineJTextField.setText(l2genData.getParamValue(l2genData.DLINE));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.NORTH, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                northJTextField.setText(l2genData.getParamValue(l2genData.NORTH));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.SOUTH, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                southJTextField.setText(l2genData.getParamValue(l2genData.SOUTH));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.WEST, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                westJTextField.setText(l2genData.getParamValue(l2genData.WEST));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.EAST, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                eastJTextField.setText(l2genData.getParamValue(l2genData.EAST));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.PARFILE_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("receiving PARFILE_TEXT_CHANGE_EVENT_NAME");
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.WAVE_LIMITER_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("WAVELENGTH_LIMITER_CHANGE_EVENT fired");
                updateWaveLimiterSelectionStates();
                // setWaveDependentProductsJList();
            }
        });


        l2genData.addPropertyChangeListener(l2genData.L2PROD, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                debug("productChangedHandler() being called");
                productChangedHandler();

            }
        });


        l2genData.addPropertyChangeListener(l2genData.IFILE, new PropertyChangeListener() {
            @Override

            public void propertyChange(PropertyChangeEvent evt) {
                //   if (enableIfileEvent) {
                System.out.println(l2genData.IFILE + " being handled");
                missionStringChangeEvent((String) evt.getNewValue());
                spixlJTextField.setText(l2genData.getParamValue(l2genData.SPIXL));
                epixlJTextField.setText(l2genData.getParamValue(l2genData.EPIXL));
                dpixlJTextField.setText(l2genData.getParamValue(l2genData.DPIXL));
                slineJTextField.setText(l2genData.getParamValue(l2genData.SLINE));
                elineJTextField.setText(l2genData.getParamValue(l2genData.ELINE));
                dlineJTextField.setText(l2genData.getParamValue(l2genData.DLINE));
                northJTextField.setText(l2genData.getParamValue(l2genData.NORTH));
                southJTextField.setText(l2genData.getParamValue(l2genData.SOUTH));
                eastJTextField.setText(l2genData.getParamValue(l2genData.EAST));
                westJTextField.setText(l2genData.getParamValue(l2genData.WEST));
                ifileChangedEventHandler();
                //   }
            }

        });


        l2genData.addPropertyChangeListener(l2genData.DEFAULTS_CHANGED_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.OFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                debug("EVENT RECEIVED ofile");
                debug(l2genData.getParamValue("ofile=" + l2genData.OFILE));
                parfileJTextArea.setText(l2genData.getParfile());
                updateOfileHandler();
            }
        });
    }


    private void updateWavelengthLimiterPanel() {

        wavelengthsJCheckboxArrayList = new ArrayList<JCheckBox>();

        waveLimiterJPanel.removeAll();

        // clear this because we dynamically rebuild it when input file selection is made or changed
        wavelengthsJCheckboxArrayList.clear();

        ArrayList<JCheckBox> wavelengthGroupCheckboxes = new ArrayList<JCheckBox>();

        for (WavelengthInfo wavelengthInfo : l2genData.getWaveLimiter()) {

            final String currWavelength = wavelengthInfo.getWavelengthString();
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


        // some GridBagLayout formatting variables
        int gridyCnt = 0;
        int gridxCnt = 0;
        int NUMBER_OF_COLUMNS = 1;


        for (JCheckBox wavelengthGroupCheckbox : wavelengthGroupCheckboxes) {
            // add current JCheckBox to the panel
            {
                final GridBagConstraints c = new GridBagConstraints();
                c.gridx = gridxCnt;
                c.gridy = gridyCnt;
                c.fill = GridBagConstraints.NONE;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.weightx = 1;
                waveLimiterJPanel.add(wavelengthGroupCheckbox, c);
            }

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

        if (sourceProductSelector != null) {
            boolean setSourceProductSelector = false;

            if (l2genData.getParamValue(l2genData.IFILE) != null &&
                    sourceProductSelector.getSelectedProduct() != null &&
                    sourceProductSelector.getSelectedProduct().getFileLocation() != null) {
                if (!l2genData.getParamValue(l2genData.IFILE).equals(sourceProductSelector.getSelectedProduct().getFileLocation().toString())) {
                    setSourceProductSelector = true;
                }
            } else {
                setSourceProductSelector = true;
            }

            if (setSourceProductSelector == true) {
                handleIfileJComboBoxEnabled = false;
                sourceProductSelector.setSelectedProduct(null);

//                final TargetProductSelectorModel selectorModel = targetProductSelector.getModel();
//                selectorModel.setProductName(null);

                handleIfileJComboBoxEnabled = true;
            }
        }
    }

    private void invalidIfileEvent() {
        for (int tabIndex = 2; tabIndex < tabCount; tabIndex++) {
            this.setEnabledAt(tabIndex, false);
        }
        //todo disable RUN button
    }

    private void missionStringChangeEvent(String newMissionString) {


        ifileChangedEventHandler();

        for (int tabIndex = 1; tabIndex < tabCount; tabIndex++) {
            this.setEnabledAt(tabIndex, true);
        }

        //       createProductSelectorWavelengthsPanel();

        updateWavelengthLimiterPanel();
        //   l2genData.applyParfileDefaults();
        updateProductTreePanel();

        // setWaveDependentProductsJList();
        updateWaveLimiterSelectionStates();


        parfileJTextArea.setText(l2genData.getParfile());
        selectedProductsJTextArea.setText(l2genData.getParamValue(l2genData.L2PROD));
    }


    /**
     * Set all waveLimiter controls to agree with l2genData
     */
    private void updateWaveLimiterSelectionStates() {

        // Turn off control handlers until all controls are set
        waveLimiterControlHandlersEnabled = false;

        // Set all checkboxes to agree with l2genData
        for (WavelengthInfo wavelengthInfo : l2genData.getWaveLimiter()) {
            for (JCheckBox currJCheckbox : wavelengthsJCheckboxArrayList) {
                if (wavelengthInfo.getWavelengthString().equals(currJCheckbox.getName())) {
                    if (wavelengthInfo.isSelected() != currJCheckbox.isSelected()) {
                        currJCheckbox.setSelected(wavelengthInfo.isSelected());
                    }
                }
            }
        }

        // Set INFRARED 'Select All' toggle to appropriate text
        if (l2genData.hasWaveType(WavelengthInfo.WaveType.INFRARED)) {
            if (l2genData.isSelectedAllWaveLimiter(WavelengthInfo.WaveType.INFRARED) && waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_SELECT_ALL_INFRARED)) {
                waveLimiterSelectAllInfrared.setText(WAVE_LIMITER_DESELECT_ALL_INFRARED);
            } else if (!l2genData.isSelectedAllWaveLimiter(WavelengthInfo.WaveType.INFRARED) && waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_DESELECT_ALL_INFRARED)) {
                waveLimiterSelectAllInfrared.setText(WAVE_LIMITER_SELECT_ALL_INFRARED);
            }
        }

        // Set INFRARED 'Select All' toggle to appropriate text
        if (l2genData.hasWaveType(WavelengthInfo.WaveType.VISIBLE)) {
            if (l2genData.isSelectedAllWaveLimiter(WavelengthInfo.WaveType.VISIBLE) && waveLimiterSelectAllVisible.getText().equals(WAVE_LIMITER_SELECT_ALL_VISIBLE)) {
                waveLimiterSelectAllVisible.setText(WAVE_LIMITER_DESELECT_ALL_VISIBLE);
            } else if (!l2genData.isSelectedAllWaveLimiter(WavelengthInfo.WaveType.VISIBLE) && waveLimiterSelectAllVisible.getText().equals(WAVE_LIMITER_DESELECT_ALL_VISIBLE)) {
                waveLimiterSelectAllVisible.setText(WAVE_LIMITER_SELECT_ALL_VISIBLE);
            }
        }

        // Turn on control handlers now that all controls are set
        waveLimiterControlHandlersEnabled = true;
    }


    private void productChangedHandler() {

        selectedProductsJTextArea.setText(l2genData.getParamValue(l2genData.L2PROD));
        parfileJTextArea.setText(l2genData.getParfile());
        productJTree.treeDidChange();
        checkTreeState(rootNode);
    }


    private void updateOfileHandler() {

        //todo
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
        System.out.println(string);
    }


}