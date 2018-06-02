package gov.nasa.gsfc.seadas.processing.common;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.BindingContext;
import gov.nasa.gsfc.seadas.processing.core.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.ModalDialog;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/7/12
 * Time: 9:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class ParamUIFactory {

    ProcessorModel processorModel;
    SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);
    private String emptySpace = "  ";

    public ParamUIFactory(ProcessorModel pm) {
        this.processorModel = pm;
    }

    public JPanel createParamPanel() {
        //final JScrollPane textScrollPane = new JScrollPane(parameterTextArea);
        final JScrollPane textScrollPane = new JScrollPane(createParamPanel(processorModel));

        textScrollPane.setPreferredSize(new Dimension(700, 400));

        final JPanel parameterComponent = new JPanel(new BorderLayout());

        parameterComponent.add(textScrollPane, BorderLayout.CENTER);


        parameterComponent.setPreferredSize(parameterComponent.getPreferredSize());

        if (processorModel.getProgramName().indexOf("smigen") != -1) {
            SMItoPPMUI smItoPPMUI = new SMItoPPMUI(processorModel);
            JPanel smitoppmPanel = smItoPPMUI.getSMItoPPMPanel();
            parameterComponent.add(smitoppmPanel, BorderLayout.SOUTH);
            smitoppmPanel.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    parameterComponent.validate();
                    parameterComponent.repaint();
                }
            });
        }
        parameterComponent.setMaximumSize(parameterComponent.getPreferredSize());
        parameterComponent.setMinimumSize(parameterComponent.getPreferredSize());
        return parameterComponent;
    }

    protected JPanel createParamPanel(ProcessorModel processorModel) {
        ArrayList<ParamInfo> paramList = processorModel.getProgramParamList();
        JPanel paramPanel = new JPanel();
        paramPanel.setName("param panel");
        JPanel textFieldPanel = new JPanel();
        textFieldPanel.setName("text field panel");
        JPanel booleanParamPanel = new JPanel();
        booleanParamPanel.setName("boolean field panel");
        JPanel fileParamPanel = new JPanel();
        fileParamPanel.setName("file parameter panel");

        TableLayout booleanParamLayout = new TableLayout(3);
        booleanParamPanel.setLayout(booleanParamLayout);

        TableLayout fileParamLayout = new TableLayout(1);
        fileParamLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        fileParamPanel.setLayout(fileParamLayout);

        int numberOfOptionsPerLine = paramList.size() % 4 < paramList.size() % 5 ? 4 : 5;
        TableLayout textFieldPanelLayout = new TableLayout(numberOfOptionsPerLine);
        textFieldPanel.setLayout(textFieldPanelLayout);

        Iterator itr = paramList.iterator();
        while (itr.hasNext()) {
            final ParamInfo pi = (ParamInfo) itr.next();
            if (!(pi.getName().equals(processorModel.getPrimaryInputFileOptionName()) ||
                    pi.getName().equals(processorModel.getPrimaryOutputFileOptionName()) ||
                    pi.getName().equals(L2genData.GEOFILE) ||
                    pi.getName().equals("verbose") ||
                    pi.getName().equals("--verbose"))) {
                if (pi.hasValidValueInfos() && pi.getType() != ParamInfo.Type.FLAGS) {
                    textFieldPanel.add(makeComboBoxOptionPanel(pi));
                } else {
                    switch (pi.getType()) {
                        case BOOLEAN:
                            booleanParamPanel.add(makeBooleanOptionField(pi));
                            break;
                        case IFILE:
                            fileParamPanel.add(createIOFileOptionField(pi));
                            break;
                        case OFILE:
                            fileParamPanel.add(createIOFileOptionField(pi));
                            break;
                        case DIR:
                            fileParamPanel.add(createIOFileOptionField(pi));
                            break;
                        case STRING:
                            textFieldPanel.add(makeOptionField(pi));
                            break;
                        case INT:
                            textFieldPanel.add(makeOptionField(pi));
                            break;
                        case FLOAT:
                            textFieldPanel.add(makeOptionField(pi));
                            break;
                        case FLAGS:
                            textFieldPanel.add(makeButtonOptionPanel(pi));
                            break;
                    }
                    //paramPanel.add(makeOptionField(pi));
                }
            }
        }

        TableLayout paramLayout = new TableLayout(1);

        paramPanel.setLayout(paramLayout);
        paramPanel.add(fileParamPanel);
        paramPanel.add(textFieldPanel);
        paramPanel.add(booleanParamPanel);

        return paramPanel;
    }

    protected JPanel makeOptionField(final ParamInfo pi) {

        final String optionName = ParamUtils.removePreceedingDashes(pi.getName());
        final JPanel optionPanel = new JPanel();
        optionPanel.setName(optionName);
        TableLayout fieldLayout = new TableLayout(1);
        fieldLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        optionPanel.setLayout(fieldLayout);
        optionPanel.setName(optionName);
        optionPanel.add(new JLabel(ParamUtils.removePreceedingDashes(optionName)));


        if (pi.getValue() == null || pi.getValue().length() == 0) {
            if (pi.getDefaultValue() != null) {
                processorModel.updateParamInfo(pi, pi.getDefaultValue());
            }
        }

        final PropertyContainer vc = new PropertyContainer();
        vc.addProperty(Property.create(optionName, pi.getValue()));
        vc.getDescriptor(optionName).setDisplayName(optionName);
        final BindingContext ctx = new BindingContext(vc);
        final JTextField field = new JTextField();
        field.setColumns(optionName.length() > 12 ? 12 : 8);
        field.setPreferredSize(field.getPreferredSize());
        field.setMaximumSize(field.getPreferredSize());
        field.setMinimumSize(field.getPreferredSize());
        field.setName(pi.getName());

        if (pi.getDescription() != null) {
            field.setToolTipText(pi.getDescription().replaceAll("\\s+", " "));
        }
        ctx.bind(optionName, field);

        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (!field.getText().trim().equals(pi.getValue().trim()))
                    processorModel.updateParamInfo(pi, field.getText());
            }
        });
        processorModel.addPropertyChangeListener(pi.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (!field.getText().trim().equals(pi.getValue().trim()))
                    field.setText(pi.getValue());
            }
        });
        optionPanel.add(field);

        return optionPanel;
    }

    private JPanel makeBooleanOptionField(final ParamInfo pi) {

        final String optionName = pi.getName();
        final boolean optionValue = pi.getValue().equals("true") || pi.getValue().equals("1") ? true : false;

        final JPanel optionPanel = new JPanel();
        optionPanel.setName(optionName);
        TableLayout booleanLayout = new TableLayout(1);
        //booleanLayout.setTableFill(TableLayout.Fill.HORIZONTAL);

        optionPanel.setLayout(booleanLayout);
        optionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        optionPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        optionPanel.add(new JLabel(emptySpace + ParamUtils.removePreceedingDashes(optionName) + emptySpace));


        final PropertySet vc = new PropertyContainer();
        vc.addProperty(Property.create(optionName, optionValue));
        vc.getDescriptor(optionName).setDisplayName(optionName);

        final BindingContext ctx = new BindingContext(vc);
        final JCheckBox field = new JCheckBox();
        field.setHorizontalAlignment(JFormattedTextField.LEFT);
        field.setName(pi.getName());
        if (pi.getDescription() != null) {
            field.setToolTipText(pi.getDescription().replaceAll("\\s+", " "));
        }
        SeadasLogger.getLogger().finest(optionName + "  " + pi.getValue());
        ctx.bind(optionName, field);

        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                processorModel.updateParamInfo(pi, (new Boolean(field.isSelected())).toString());
                SeadasLogger.getLogger().info((new Boolean(field.isSelected())).toString() + "  " + field.getText());

            }
        });

        processorModel.addPropertyChangeListener(pi.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                field.setSelected(pi.getValue().equals("true") || pi.getValue().equals("1") ? true : false);
                field.validate();
                field.repaint();
                //optionValue = field.isSelected();

            }
        });

        optionPanel.add(field);

        return optionPanel;

    }

    private JPanel makeComboBoxOptionPanel(final ParamInfo pi) {
        final JPanel singlePanel = new JPanel();

        TableLayout comboParamLayout = new TableLayout(1);
        comboParamLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        singlePanel.setLayout(comboParamLayout);

        final JLabel optionNameLabel = new JLabel(ParamUtils.removePreceedingDashes(pi.getName()));

        singlePanel.add(optionNameLabel);
        singlePanel.setName(pi.getName());

        String optionDefaultValue = pi.getValue();


        final ArrayList<ParamValidValueInfo> validValues = pi.getValidValueInfos();
        final String[] values = new String[validValues.size()];
        ArrayList<String> toolTips = new ArrayList<String>();

        Iterator itr = validValues.iterator();
        int i = 0;
        ParamValidValueInfo paramValidValueInfo;
        while (itr.hasNext()) {
            paramValidValueInfo = (ParamValidValueInfo) itr.next();
            values[i] = paramValidValueInfo.getValue();
            toolTips.add(paramValidValueInfo.getDescription());
            i++;
        }

        final JComboBox inputList = new JComboBox(values);
        ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
        inputList.setRenderer(renderer);
        renderer.setTooltips(toolTips);
        inputList.setEditable(true);
        inputList.setName(pi.getName());
        inputList.setPreferredSize(new Dimension(inputList.getPreferredSize().width,
                inputList.getPreferredSize().height));
        if (pi.getDescription() != null) {
            inputList.setToolTipText(pi.getDescription());
        }
        int defaultValuePosition = new ArrayList(Arrays.asList(values)).indexOf(optionDefaultValue);

        if (defaultValuePosition != -1) {
            inputList.setSelectedIndex(defaultValuePosition);
        }

        String optionName = pi.getName();


        final PropertyContainer vc = new PropertyContainer();
        vc.addProperty(Property.create(optionName, pi.getValue()));
        vc.getDescriptor(optionName).setDisplayName(optionName);

        final BindingContext ctx = new BindingContext(vc);

        ctx.bind(optionName, inputList);

        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {

                String newValue = (String) inputList.getSelectedItem();
                processorModel.updateParamInfo(pi, newValue);
            }
        });

        processorModel.addPropertyChangeListener(pi.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                //values = updateValidValues(pi);
                int currentChoicePosition = new ArrayList(Arrays.asList(values)).indexOf(pi.getValue());
                if (currentChoicePosition != -1) {
                    inputList.setSelectedIndex(currentChoicePosition);
                }
            }
        });
        singlePanel.add(inputList);
        return singlePanel;
    }

    private JPanel makeButtonOptionPanel(final ParamInfo pi) {
        final JPanel singlePanel = new JPanel();

        TableLayout comboParamLayout = new TableLayout(1);
        comboParamLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        singlePanel.setLayout(comboParamLayout);

        //final JLabel optionNameLabel = new JLabel(ParamUtils.removePreceedingDashes(pi.getName()));
        final JButton optionNameButton = new JButton(ParamUtils.removePreceedingDashes(pi.getName()));
        optionNameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String selectedFlags = chooseValidValues(pi);
                processorModel.updateParamInfo(pi, selectedFlags);
            }
        });
        //singlePanel.add(optionNameLabel);
        singlePanel.add(optionNameButton);
        singlePanel.setName(pi.getName());

        //String optionDefaultValue = pi.getValue();

        final JTextField field = new JTextField();
        field.setText(pi.getValue());
        field.setColumns(8);
        processorModel.addPropertyChangeListener(pi.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                field.setText(pi.getValue());
            }
        });
        //field.setEditable(false);
        singlePanel.add(field);
        return singlePanel;
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }

    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(name, listener);
    }

    public SwingPropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public void appendPropertyChangeSupport(SwingPropertyChangeSupport propertyChangeSupport) {
        PropertyChangeListener[] pr = propertyChangeSupport.getPropertyChangeListeners();
        for (int i = 0; i < pr.length; i++) {
            this.propertyChangeSupport.addPropertyChangeListener(pr[i]);
        }
    }

    public void clearPropertyChangeSupport() {
        propertyChangeSupport = new SwingPropertyChangeSupport(this);
    }

    private String chooseValidValues(ParamInfo pi) {
        JPanel validValuesPanel = new JPanel();
        validValuesPanel.setLayout(new TableLayout(3));
        String choosenValues = "";
        final ArrayList<ParamValidValueInfo> validValues = pi.getValidValueInfos();

        Iterator itr = validValues.iterator();
        ParamValidValueInfo paramValidValueInfo;
        while (itr.hasNext()) {
            paramValidValueInfo = (ParamValidValueInfo) itr.next();
            if (!paramValidValueInfo.getValue().trim().equals("SPARE")) {
                validValuesPanel.add(makeValidValueCheckbox(paramValidValueInfo));
            }
        }
        validValuesPanel.repaint();
        validValuesPanel.validate();

        final Window parent = SnapApp.getDefault().getMainFrame();
        String dialogTitle = null;
        final ModalDialog modalDialog = new ModalDialog(parent, dialogTitle, validValuesPanel, ModalDialog.ID_OK, "test");
        final int dialogResult = modalDialog.show();
        if (dialogResult != ModalDialog.ID_OK) {

        }

        itr = validValues.iterator();

        while (itr.hasNext()) {
            paramValidValueInfo = (ParamValidValueInfo) itr.next();
            if (paramValidValueInfo.isSelected()) {
                choosenValues = choosenValues + paramValidValueInfo.getValue() + ",";
            }
        }
        if (choosenValues.indexOf(",") != -1) {
            choosenValues = choosenValues.substring(0, choosenValues.lastIndexOf(","));
        }
        return choosenValues;
    }

    private JPanel makeValidValueCheckbox(final ParamValidValueInfo paramValidValueInfo) {

        final String optionName = paramValidValueInfo.getValue();
        final boolean optionValue = paramValidValueInfo.isSelected();

        final JPanel optionPanel = new JPanel();
        optionPanel.setName(optionName);
        optionPanel.setBorder(new EtchedBorder());
        optionPanel.setPreferredSize(new Dimension(100, 40));
        TableLayout booleanLayout = new TableLayout(1);
        //booleanLayout.setTableFill(TableLayout.Fill.HORIZONTAL);

        optionPanel.setLayout(booleanLayout);
        optionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        optionPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        optionPanel.add(new JLabel(emptySpace + ParamUtils.removePreceedingDashes(optionName) + emptySpace));


        final PropertySet vc = new PropertyContainer();
        vc.addProperty(Property.create(optionName, optionValue));
        vc.getDescriptor(optionName).setDisplayName(optionName);

        final BindingContext ctx = new BindingContext(vc);
        final JCheckBox field = new JCheckBox();
        field.setHorizontalAlignment(JFormattedTextField.LEFT);
        field.setName(optionName);
        field.setSelected(paramValidValueInfo.isSelected());
        if (paramValidValueInfo.getDescription() != null) {
            field.setToolTipText(paramValidValueInfo.getDescription().replaceAll("\\s+", " "));
        }

        ctx.bind(optionName, field);

        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                //processorModel.updateParamInfo(pi, (new Boolean(field.isSelected())).toString());
                //SeadasLogger.getLogger().info((new Boolean(field.isSelected())).toString() + "  " + field.getText());
                paramValidValueInfo.setSelected(field.isSelected());
            }
        });

        processorModel.addPropertyChangeListener(paramValidValueInfo.getValue(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                field.setSelected(paramValidValueInfo.isSelected());
                //optionValue = field.isSelected();

            }
        });

        optionPanel.add(field);

        return optionPanel;

    }


    private String[] updateValidValues(ParamInfo pi) {
        final ArrayList<ParamValidValueInfo> validValues = pi.getValidValueInfos();
        final String[] values = new String[validValues.size()];
        ArrayList<String> toolTips = new ArrayList<String>();

        Iterator itr = validValues.iterator();
        int i = 0;
        ParamValidValueInfo paramValidValueInfo;
        while (itr.hasNext()) {
            paramValidValueInfo = (ParamValidValueInfo) itr.next();
            values[i] = paramValidValueInfo.getValue();
            toolTips.add(paramValidValueInfo.getDescription());
            i++;
        }
        return values;
    }

    private boolean controlHandlerEnabled = true, eventHandlerEnabled = true;

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

    private JPanel createIOFileOptionField(final ParamInfo pi) {


        final FileSelector ioFileSelector = new FileSelector((AppContext) SnapApp.getDefault(), pi.getType(), ParamUtils.removePreceedingDashes(pi.getName()));
        ioFileSelector.getFileTextField().setColumns(40);

        processorModel.addPropertyChangeListener(pi.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                disableControlHandler();
                if (isEventHandlerEnabled() || pi.getName().isEmpty()) {
                    ioFileSelector.setFilename(pi.getValue());
                }
                enableControlHandler();
            }
        });

        ioFileSelector.addPropertyChangeListener(ioFileSelector.getPropertyName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                disableEventHandler();
                if (isControlHandlerEnabled()) {
                    String iofileName;
                    if (ioFileSelector.getFileName() != null) {
                        iofileName = ioFileSelector.getFileName();
                        processorModel.updateParamInfo(pi, iofileName);
                    }
                }
                enableEventHandler();
            }
        });



        ioFileSelector.getjPanel().setName(pi.getName());
        return ioFileSelector.getjPanel();
    }

    private class ComboboxToolTipRenderer extends DefaultListCellRenderer {
        ArrayList tooltips;

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            JComponent comp = (JComponent) super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);

            if (-1 < index && null != value && null != tooltips) {
                list.setToolTipText((String) tooltips.get(index));
            }
            return comp;
        }

        public void setTooltips(ArrayList tooltips) {
            this.tooltips = tooltips;
        }
    }

    private class ValidValueChooser extends JPanel {

        String selectedBoxes;
        JPanel valuesPanel;

        ValidValueChooser(ParamInfo paramInfo) {

        }


    }

    private class ValidValuesButtonAction implements ActionListener {

        final JPanel valuesPanel;

        String selectedValues;

        ValidValuesButtonAction(JPanel panel) {
            valuesPanel = panel;
        }

        public void actionPerformed(ActionEvent actionEvent) {
            final Window parent = SnapApp.getDefault().getMainFrame();
            String dialogTitle = null;
            final ModalDialog modalDialog = new ModalDialog(parent, dialogTitle, valuesPanel, ModalDialog.ID_OK, "test");
            final int dialogResult = modalDialog.show();
            if (dialogResult != ModalDialog.ID_OK) {

            }

        }

        void setSelectedValued(String selectedValues) {
            this.selectedValues = selectedValues;

        }

        String getSelectedValues() {
            return selectedValues;
        }
    }

}
