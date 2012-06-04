package gov.nasa.gsfc.seadas.processing.l2gen;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.core.L2genParamCategoryInfo;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.general.FileSelectorPanel;
import gov.nasa.gsfc.seadas.processing.general.GridBagConstraintsCustom;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/12/12
 * Time: 9:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genCategorizedParamsPanel extends JPanel {

    private L2genData l2genData;
    private L2genParamCategoryInfo paramCategoryInfo;
    private JPanel paramsPanel;
    private JButton restoreDefaultsButton;

    private final Color DEFAULT_INDICATOR_COLOR = new Color(0, 0, 120);

    private final String DEFAULT_INDICATOR_TOOLTIP = "* Identicates that the selection is not the default value";
    private final String DEFAULT_INDICATOR_LABEL_ON = " *  ";
    private final String DEFAULT_INDICATOR_LABEL_OFF = "     ";

    private boolean swingSentEventsDisabled = false;


    L2genCategorizedParamsPanel(L2genData l2genData, L2genParamCategoryInfo paramCategoryInfo) {

        this.l2genData = l2genData;
        this.paramCategoryInfo = paramCategoryInfo;


        initComponents();
        addComponents();
    }


    public void initComponents() {

        paramsPanel = new JPanel();
        paramsPanel.setLayout(new GridBagLayout());


        restoreDefaultsButton = new JButton("Restore Defaults (this tab only)");
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
                } else if (paramInfo.getType() == ParamInfo.Type.IFILE || paramInfo.getType() == ParamInfo.Type.OFILE) {
                    createFileSelectorRow(paramInfo, paramsPanel, gridy);
                } else {
                    createParamTextfield(paramInfo, paramsPanel, gridy);
                }
            }

            gridy++;

            l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (l2genData.isParamCategoryDefault(paramCategoryInfo)) {
                        restoreDefaultsButton.setEnabled(false);
                    } else {
                        restoreDefaultsButton.setEnabled(true);
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


    }

    public void addComponents() {

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(paramCategoryInfo.getName()));
        setPreferredSize(new Dimension(1000, 800));


        final JScrollPane paramsScroll = new JScrollPane(paramsPanel);

        add(paramsScroll,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH));

        add(restoreDefaultsButton,
                new GridBagConstraintsCustom(0, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
    }

    private String buildStringPrototype(int size) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < size; i++) {
            stringBuilder.append("p");
        }

        return stringBuilder.toString();
    }

    private void createParamTextfield(final ParamInfo paramInfo, JPanel jPanel, int gridy) {

        final String PROTOTYPE_70 = buildStringPrototype(70);
        final String PROTOTYPE_60 = buildStringPrototype(60);
        final String PROTOTYPE_15 = buildStringPrototype(15);

        int fill;
        String textfieldPrototype;

        if (paramInfo.getType() == ParamInfo.Type.STRING) {
            textfieldPrototype = PROTOTYPE_60;
            fill = GridBagConstraints.NONE;
        } else if (paramInfo.getType() == ParamInfo.Type.INT) {
            textfieldPrototype = PROTOTYPE_15;
            fill = GridBagConstraints.NONE;
        } else if (paramInfo.getType() == ParamInfo.Type.FLOAT) {
            textfieldPrototype = buildStringPrototype(60);
            fill = GridBagConstraints.NONE;
        } else if (paramInfo.getType() == ParamInfo.Type.IFILE) {
            textfieldPrototype = PROTOTYPE_70;
            fill = GridBagConstraints.HORIZONTAL;
        } else if (paramInfo.getType() == ParamInfo.Type.OFILE) {
            textfieldPrototype = PROTOTYPE_70;
            fill = GridBagConstraints.HORIZONTAL;
        } else {
            textfieldPrototype = PROTOTYPE_70;
            fill = GridBagConstraints.NONE;
        }


        final JTextField jTextField = new JTextField(textfieldPrototype);
        jTextField.setPreferredSize(jTextField.getPreferredSize());
        jTextField.setMaximumSize(jTextField.getPreferredSize());
        jTextField.setMinimumSize(jTextField.getPreferredSize());

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


        jPanel.add(jTextField,
                new GridBagConstraintsCustom(2, gridy, 1, 0, GridBagConstraints.WEST, fill));


        jTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                l2genData.setParamValue(paramInfo.getName(), jTextField.getText().toString());
            }
        });

        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.setParamValue(paramInfo.getName(), jTextField.getText().toString());
            }
        });

        l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jTextField.setText(l2genData.getParamValue(paramInfo.getName()));
                if (l2genData.isParamDefault(paramInfo.getName())) {
                    defaultIndicator.setText(DEFAULT_INDICATOR_LABEL_OFF);
                    defaultIndicator.setToolTipText("");
                } else {
                    defaultIndicator.setText(DEFAULT_INDICATOR_LABEL_ON);
                    defaultIndicator.setToolTipText(DEFAULT_INDICATOR_TOOLTIP);
                }
            }
        });
    }


    private void createFileSelectorRow(final ParamInfo paramInfo, JPanel jPanel, int gridy) {


        final JLabel jLabel = new JLabel(paramInfo.getName());
        jLabel.setToolTipText(paramInfo.getDescription());

        final JLabel defaultIndicator = new JLabel(DEFAULT_INDICATOR_LABEL_OFF);

        l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (l2genData.isParamDefault(paramInfo.getName())) {
                    defaultIndicator.setText(DEFAULT_INDICATOR_LABEL_OFF);
                    defaultIndicator.setToolTipText("");
                } else {
                    defaultIndicator.setText(DEFAULT_INDICATOR_LABEL_ON);
                    defaultIndicator.setToolTipText(DEFAULT_INDICATOR_TOOLTIP);
                }
            }
        });


        defaultIndicator.setForeground(DEFAULT_INDICATOR_COLOR);
        defaultIndicator.setToolTipText(DEFAULT_INDICATOR_TOOLTIP);

        JPanel valuePanel = createFileSelectorPanel(paramInfo);


        jPanel.add(jLabel,
                new GridBagConstraintsCustom(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        jPanel.add(defaultIndicator,
                new GridBagConstraintsCustom(1, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        jPanel.add(valuePanel,
                new GridBagConstraintsCustom(2, gridy, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
    }


    private JPanel createFileSelectorPanel(final ParamInfo paramInfo) {

        FileSelectorPanel.Type type = null;
        if (paramInfo.getType() == ParamInfo.Type.IFILE) {
            type = FileSelectorPanel.Type.IFILE;
        } else if (paramInfo.getType() == ParamInfo.Type.OFILE) {
            type = FileSelectorPanel.Type.OFILE;
        }

        if (type == null) {
            return null;
        }

        final FileSelectorPanel fileSelectorPanel = new FileSelectorPanel(VisatApp.getApp(), type);

        final boolean[] handlerEnabled = {true};

        fileSelectorPanel.addPropertyChangeListener(fileSelectorPanel.getPropertyName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (handlerEnabled[0]) {
                    l2genData.setParamValue(paramInfo.getName(), fileSelectorPanel.getFileName());
                }
            }
        });


        l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                handlerEnabled[0] = false;
                fileSelectorPanel.setFilename(l2genData.getParamValue(paramInfo.getName()));
                handlerEnabled[0] = true;
            }
        });

        return fileSelectorPanel;


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

     //   final String param = paramInfo.getName();

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
            if (l2genData.getParamValue(paramInfo.getName()).equals(paramValidValueInfo.getValue())) {
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
                l2genData.setParamValue(paramInfo.getName(), (ParamValidValueInfo) jComboBox.getSelectedItem());
            }
        });


        l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean found = false;
                ComboBoxModel comboBoxModel = jComboBox.getModel();

                for (int i = 0; i < comboBoxModel.getSize(); i++) {
                    ParamValidValueInfo jComboBoxItem = (ParamValidValueInfo) comboBoxModel.getElementAt(i);
                    if (paramInfo.getValue().equals(jComboBoxItem.getValue())) {
                        jComboBox.setSelectedItem(jComboBoxItem);

                        if (l2genData.isParamDefault(paramInfo.getName())) {
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
                String currValueString = l2genData.getParamValue(paramInfo.getName());
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
                            l2genData.setParamToDefaults(paramInfo.getName());
                            swingSentEventsDisabled = false;
                        }
                        return;
                    }
                }


                String newValueString = Integer.toString(newValue);

                if (!swingSentEventsDisabled) {
                    swingSentEventsDisabled = true;
                    l2genData.setParamValue(paramInfo.getName(), newValueString);
                    swingSentEventsDisabled = false;
                }
            }
        }

        );

        l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                int value = Integer.parseInt(paramValidValueInfo.getValue());

                if (value > 0) {
                    jCheckBox.setSelected(paramInfo.isBitwiseSelected(paramValidValueInfo));
                } else {
                    if (paramValidValueInfo.getValue().equals(l2genData.getParamValue(paramInfo.getName()))) {
                        jCheckBox.setSelected(true);
                    } else {
                        jCheckBox.setSelected(false);
                    }
                }


                if (l2genData.isParamDefault(paramInfo.getName())) {
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


    private void createParamCheckBox(final ParamInfo paramInfo, JPanel jPanel, int gridy) {
        final JCheckBox jCheckBox = new JCheckBox();

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
                l2genData.setParamValue(paramInfo.getName(), jCheckBox.isSelected());
            }
        });

        l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jCheckBox.setSelected(l2genData.getBooleanParamValue(paramInfo.getName()));

                if (l2genData.isParamDefault(paramInfo.getName())) {
                    defaultIndicator.setText(DEFAULT_INDICATOR_LABEL_OFF);
                    defaultIndicator.setToolTipText("");
                } else {
                    defaultIndicator.setText(DEFAULT_INDICATOR_LABEL_ON);
                    defaultIndicator.setToolTipText(DEFAULT_INDICATOR_TOOLTIP);
                }
            }
        });
    }


    private void debug(String message) {

    }
}
