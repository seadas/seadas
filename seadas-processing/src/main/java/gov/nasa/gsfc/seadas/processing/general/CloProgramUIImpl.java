/*
Author:
       Aynur Abdurazik
*/

package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.ValueRange;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.selection.AbstractSelectionChangeListener;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import gov.nasa.gsfc.seadas.processing.l2gen.GridBagConstraintsCustom;
import gov.nasa.gsfc.seadas.processing.l2gen.L2genData;
import gov.nasa.gsfc.seadas.processing.l2gen.ParamInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.ParamValidValueInfo;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.BeamFileChooser;
import org.esa.beam.util.io.FileUtils;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;


public class CloProgramUIImpl extends JPanel implements CloProgramUI {

    protected final String programName;
    protected final String xmlFileName;
    protected final SourceProductFileSelector sourceProductSelector;
    protected final SourceProductFileSelector geoFileSelector;
    protected final OutputFileSelector outputFileSelector;


    private JPanel mainPanel;

    private File selectedFile;

    private double ioPanelWidth;

    private ProcessorModel processorModel;

    int outputFilePanelHeight;
    int inputFilePanelHeight;

    private boolean handleIfileJComboBoxEnabled = true;
    private boolean handleOfileSelecterEnabled = true;


    CloProgramUIImpl(String programName, String xmlFileName) {
        this.programName = programName;
        this.xmlFileName = xmlFileName;
        processorModel = new ProcessorModel(programName, xmlFileName);
        sourceProductSelector = new SourceProductFileSelector(VisatApp.getApp(), "");
        sourceProductSelector.initProducts();
        outputFileSelector = new OutputFileSelector(VisatApp.getApp(), "Output File");
        geoFileSelector = new SourceProductFileSelector(VisatApp.getApp(), "");
        inputFilePanelHeight = 0;
        outputFilePanelHeight = 0;
        createUserInterface();
    }


    public ProcessorModel getProcessorModel() {
        return processorModel;

    }

    public Product getSelectedSourceProduct() {
        return sourceProductSelector.getSelectedProduct();
    }

    private void createUserInterface() {

        if (mainPanel != null) {
            this.remove(mainPanel);
        }
        mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(createInputOutputPanel(),
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));

        mainPanel.add(createParamPanel(),
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 3));

        mainPanel.add(outputFileSelector.getOpenInAppCheckBox(),
                new GridBagConstraintsCustom(0, 2, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        add(mainPanel);
        validate();
        repaint();
    }

    //    private JCheckBox makeOpenInAppCheckBox(){
//        JCheckBox openInAppCheckBox = outputFileSelector.getOpenInAppCheckBox();
//        openInApp = outputFileSelector.getOpenInAppCheckBox().isSelected();
//        outputFileSelector.getModel().getValueContainer().addPropertyChangeListener(new PropertyChangeListener() {
//            @Override
//            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
//                //To change body of implemented methods use File | Settings | File Templates.
//                openInApp = outputFileSelector.getOpenInAppCheckBox().isSelected();
//            }
//        } );
//
//        return openInAppCheckBox;
//    }
    private JPanel createInputOutputPanel() {

        final JPanel inputOutputPanel = new JPanel(new GridBagLayout());
        inputOutputPanel.setBorder(BorderFactory.createTitledBorder("Primary Input/Output Files"));

        inputOutputPanel.add(createSourceProductPanel(),
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        if (processorModel.hasGeoFile()) {
            inputOutputPanel.add(createGeoFilePanel(),
                    new GridBagConstraintsCustom(0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        }

        if (processorModel.hasPrimaryOutputFile()) {
            inputOutputPanel.add(createOutputFilePanel(),
                    new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        }

        ioPanelWidth = inputOutputPanel.getSize().getWidth();


        return inputOutputPanel;
    }


    protected JPanel createParamPanel() {
        //final JScrollPane textScrollPane = new JScrollPane(parameterTextArea);
        final JScrollPane textScrollPane = new JScrollPane(createParamPanel(processorModel));

        textScrollPane.setPreferredSize(new Dimension(700, 400));

        final JButton saveParameterFileButton = new JButton("Store Parameters...");
        saveParameterFileButton.addActionListener(createSafeAsAction());

        final JButton loadParameterButton = new JButton("Load Parameters...");
        loadParameterButton.addActionListener(createLoadParameterAction());

        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(loadParameterButton);
        buttonPanel.add(saveParameterFileButton);

        final JPanel parameterComponent = new JPanel(new BorderLayout());
        parameterComponent.add(textScrollPane, BorderLayout.CENTER);
        parameterComponent.add(buttonPanel, BorderLayout.SOUTH);

        return parameterComponent;
    }

    private ActionListener createLoadParameterAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Component parent = (Component) e.getSource();
                System.out.print(processorModel.getRootDir().getAbsolutePath());
                final BeamFileChooser beamFileChooser = new BeamFileChooser(processorModel.getRootDir());
                final int status = beamFileChooser.showOpenDialog(parent);
                if (status == JFileChooser.APPROVE_OPTION) {
                    final File file = beamFileChooser.getSelectedFile();
                    if (!file.exists()) {
                        JOptionPane.showMessageDialog(parent,
                                "Unable to load parameter file '" + file + "'.\n" +
                                        "The file does not exist.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    LineNumberReader reader = null;
                    try {
                        reader = new LineNumberReader(new FileReader(file));
                        String line;
                        line = reader.readLine();
                        String[] option = new String[2];
                        while (line != null) {
                            System.out.printf("%1$d %2$s %n", reader.getLineNumber(), line);
                            option = line.split("=", 2);
                            processorModel.updateParamInfo(option[0], option[1]);
                            if (option[0].trim().equals(processorModel.getPrimaryInputFileOptionName())) {
                                processorModel.addPropertyChangeListener(processorModel.getPrimaryInputFileOptionName(), new PropertyChangeListener() {
                                    @Override
                                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                                        {
                                           // sourceProductSelector.getIfileTextfield().setText();
                                           sourceProductSelector.setSelectedFile(new File(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName())));
                                        }
                                    }
                                });
                            }
                            line = reader.readLine();
                        }
                        //mainPanel.validate();
                        //mainPanel.repaint();
                        createUserInterface();
                        System.out.println("par String: " + processorModel.getParString());
                    } catch (IOException e1) {
                        System.err.println(e1.getMessage());
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(parent,
                                "Unable to load parameter file '" + file + "'.\n" +
                                        "Error reading file.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        //    parameterTextArea.setText(getDefaultText());
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException ignored) {
                            }
                        }
                    }
                }
            }
        };
    }

    private ActionListener createSafeAsAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JComponent parent = (JComponent) e.getSource();
                selectedFile = getParameterFile();
                try {
                    saveParameterFile(parent);
                } catch (IOException e1) {
                    showErrorMessage(parent);
                }
            }
        };
    }

    private File saveParameterFile(JComponent parent) throws IOException {
        final BeamFileChooser beamFileChooser = new BeamFileChooser();
        if (selectedFile != null) {
            beamFileChooser.setSelectedFile(selectedFile);
        }
        final int status = beamFileChooser.showSaveDialog(parent);
        if (JFileChooser.APPROVE_OPTION == status) {
            selectedFile = beamFileChooser.getSelectedFile();
            if (selectedFile.canWrite()) {
                final int i = JOptionPane.showConfirmDialog(parent, "The file exists.\nDo you really want to overwrite the existing file?");
                if (i == JOptionPane.OK_OPTION) {
                    return writeParameterFileTo(selectedFile);
                } else {
                    saveParameterFile(parent);
                }
            } else if (selectedFile.createNewFile()) {
                return writeParameterFileTo(selectedFile);
            } else {
                showErrorMessage(parent);
            }
        }
        return null;
    }

    private File writeParameterFileTo(File parameterFile) throws IOException {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(parameterFile);
            fileWriter.write(processorModel.getParString());
            return parameterFile;
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    private File getParameterFile() {
        final Product selectedProduct = sourceProductSelector.getSelectedProduct();
        if (selectedProduct == null
                || selectedProduct.getFileLocation() == null) {
            return null;
        }
        final File productFile = selectedProduct.getFileLocation();
        return FileUtils.exchangeExtension(productFile, ".par");
    }

    private File getGeoFile(File selectedProduct) {
        //final Product selectedProduct = sourceProductSelector.getSelectedProduct();
        if (selectedProduct == null) {
            return null;
        }
        return FileUtils.exchangeExtension(selectedProduct, ".GEO");
    }

    private void showErrorMessage(JComponent parent) {
        JOptionPane.showMessageDialog(parent,
                "Unable to create parameter file:\n'" + selectedFile + "'",
                "",
                JOptionPane.ERROR_MESSAGE);
    }

    public boolean isOpenOutputInApp() {
        return outputFileSelector.getOpenInAppCheckBox().isSelected();
    }

    protected JPanel createParamPanel(ProcessorModel processorModel) {
        ArrayList<ParamInfo> paramList = processorModel.getProgramParamList();
        JPanel paramPanel = new JPanel();
        JPanel textFieldPanel = new JPanel();
        JPanel booleanParamPanel = new JPanel();
        JPanel fileParamPanel = new JPanel();
        Dimension paramPanelDimension = new Dimension();
        paramPanelDimension.setSize(ioPanelWidth, 1000);

        //int iopanelSize;


        TableLayout booelanParamLayout = new TableLayout(3);
        booleanParamPanel.setLayout(booelanParamLayout);

        TableLayout fileParamLayout = new TableLayout(1);
        fileParamPanel.setLayout(fileParamLayout);

        int numberOfOptionsPerLine = paramList.size() % 4 < paramList.size() % 5 ? 4 : 5;
        TableLayout textFieldPanelLayout = new TableLayout(numberOfOptionsPerLine);
        textFieldPanel.setLayout(textFieldPanelLayout);
        //paramPanel.setBorder(new EmptyBorder(null));
        //paramPanel.setPreferredSize(paramPanelDimension);

        Iterator itr = paramList.iterator();
        while (itr.hasNext()) {
            final ParamInfo pi = (ParamInfo) itr.next();
            if (!(pi.getName().equals(processorModel.getPrimaryInputFileOptionName()) ||
                    pi.getName().equals(processorModel.getPrimaryOutputFileOptionName()) ||
                    pi.getName().equals(L2genData.GEOFILE))) {

                SeadasLogger.getLogger().fine(pi.getName());
                if (pi.hasValidValueInfos()) {

                    textFieldPanel.add(makeComboBoxOptionPanel(pi));

                } else {
                    switch (pi.getType()) {
                        case BOOLEAN:
                            booleanParamPanel.add(makeBooleanOptionField(pi));
                            break;
                        case IFILE:
                            fileParamPanel.add(makeInputFileOptionField(pi));
                            break;
                        case OFILE:
                            fileParamPanel.add(makeOutputFileOptionField(pi));
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
                    }
                    //paramPanel.add(makeOptionField(pi));
                }
            }
        }

        paramPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                //To change body of implemented methods use File | Settings | File Templates.

                validateParams();
            }
        });
        SeadasLogger.getLogger().info("boolean panel components: " + booleanParamPanel.getComponentCount());
        SeadasLogger.getLogger().info("file panel components: " + fileParamPanel.getComponentCount());
        SeadasLogger.getLogger().info("textfield panel components: " + textFieldPanel.getComponentCount());

        TableLayout paramLayout = new TableLayout(1);

        paramPanel.setLayout(paramLayout);
        paramPanel.add(fileParamPanel);
        paramPanel.add(textFieldPanel);
        paramPanel.add(booleanParamPanel);

        return paramPanel;
    }

    protected JPanel makeOptionField(final ParamInfo pi) {

        final String optionName = pi.getName();
        //final String optionValue = pi.getValue();
        final JPanel optionPanel = new JPanel();
        TableLayout fieldLayout = new TableLayout(1);
        fieldLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        optionPanel.setLayout(fieldLayout);
        optionPanel.add(new JLabel(optionName));


        if (pi.getValue() == null || pi.getValue().length() == 0) {
            if (pi.getDefaultValue() != null) {
                pi.setValue(pi.getDefaultValue());
            }
        }
        final PropertyContainer vc = new PropertyContainer();
        vc.addProperty(Property.create(optionName, pi.getValue()));
        vc.getDescriptor(optionName).setDisplayName(optionName);

        //final ValueRange valueRange = new ValueRange(-180, 180);


        //vc.getDescriptor(optionName).setValueRange(valueRange);

        final BindingContext ctx = new BindingContext(vc);
        final JTextField field = new JTextField();
        field.setColumns(8);
        field.setPreferredSize(field.getPreferredSize());
        field.setMaximumSize(field.getPreferredSize());
        field.setMinimumSize(field.getPreferredSize());

        if (pi.getDescription() != null) {
            field.setToolTipText(pi.getDescription());
        }
        ctx.bind(optionName, field);

        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                pi.setValue(field.getText());
                handleParamChanged();
            }
        });

        optionPanel.add(field);

        return optionPanel;

    }

    private JPanel makeBooleanOptionField(final ParamInfo pi) {

        final String optionName = pi.getName();
        final boolean optionValue = pi.getValue() == "true" || pi.getValue() == "1" ? true : false;
        final JPanel optionPanel = new JPanel();
        TableLayout booleanLayout = new TableLayout(1);
        booleanLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        optionPanel.setLayout(booleanLayout);
        optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionPanel.add(new JLabel(optionName));


        final PropertyContainer vc = new PropertyContainer();
        vc.addProperty(Property.create(optionName, optionValue));
        vc.getDescriptor(optionName).setDisplayName(optionName);

        final ValueRange valueRange = new ValueRange(0, 1);


        vc.getDescriptor(optionName).setValueRange(valueRange);

        final BindingContext ctx = new BindingContext(vc);
        final JCheckBox field = new JCheckBox();
        field.setHorizontalAlignment(JFormattedTextField.LEFT);
        if (pi.getDescription() != null) {
            field.setToolTipText(pi.getDescription());
        }
        SeadasLogger.getLogger().finest(optionName + "  " + pi.getValue());
        ctx.bind(optionName, field);

        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {

                pi.setValue((new Boolean(field.isSelected())).toString());

                SeadasLogger.getLogger().info((new Boolean(field.isSelected())).toString() + "  " + field.getText());

            }
        });

        optionPanel.add(field);

        return optionPanel;

    }

    private JPanel makeComboBoxOptionPanel(final ParamInfo paramInfo) {
        final JPanel singlePanel = new JPanel();

        TableLayout comboParamLayout = new TableLayout(1);
        comboParamLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        singlePanel.setLayout(comboParamLayout);

        final JLabel optionNameLabel = new JLabel(paramInfo.getName());

        singlePanel.add(optionNameLabel);


        String optionDefaultValue = paramInfo.getValue();


        final ArrayList<ParamValidValueInfo> validValues = paramInfo.getValidValueInfos();
        String[] values = new String[validValues.size()];

        Iterator itr = validValues.iterator();
        int i = 0;
        while (itr.hasNext()) {
            values[i] = ((ParamValidValueInfo) itr.next()).getValue();
            i++;
        }

        final JComboBox inputList = new JComboBox(values);
        inputList.setEditable(true);
        inputList.setPreferredSize(new Dimension(inputList.getPreferredSize().width,
                inputList.getPreferredSize().height));
        if (paramInfo.getDescription() != null) {
            inputList.setToolTipText(paramInfo.getDescription());
        }
        int defaultValuePosition = validValues.indexOf(optionDefaultValue);

        if (defaultValuePosition != -1) {
            inputList.setSelectedIndex(defaultValuePosition);
        }

        String optionName = paramInfo.getName();
        //String optionValue = (String)inputList.getSelectedItem();
//        inputList.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent actionEvent) {
//                String newValue = (String) inputList.getSelectedItem();
//                processorModel.updateParamInfo(paramInfo, newValue);
//            }
//        });
//        inputList.addPropertyChangeListener(new PropertyChangeListener() {
//            @Override
//            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//        });

        final PropertyContainer vc = new PropertyContainer();
        vc.addProperty(Property.create(optionName, paramInfo.getValue()));
        vc.getDescriptor(optionName).setDisplayName(optionName);

        //final ValueRange valueRange = new ValueRange(-180, 180);


        //vc.getDescriptor(optionName).setValueRange(valueRange);

        final BindingContext ctx = new BindingContext(vc);

//        processorModel.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
//            @Override
//            public void propertyChange(PropertyChangeEvent evt) {
//                int newValuePosition = validValues.indexOf(paramInfo.getValue());
//                if (newValuePosition != -1) {
//                    inputList.setSelectedIndex(newValuePosition);
//                }
//
//            }
//        });
        ctx.bind(optionName, inputList);

        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {

                String newValue = (String) inputList.getSelectedItem();
                paramInfo.setValue(newValue);
            }
        });

        singlePanel.add(inputList);


        switch (paramInfo.getType()) {
            case STRING:
                break;
            case INT:
                break;
            case FLOAT:
                break;
        }

        return singlePanel;
    }

    private JPanel makeFileOptionField(final ParamInfo pi) {

        final String optionName = pi.getName();
        final String optionValue = pi.getValue();
        //final JPanel optionPanel = new JPanel();
        // optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        //optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        //optionPanel.add(new JLabel(optionName));


        final PropertyContainer vc = new PropertyContainer();
        vc.addProperty(Property.create(optionName, optionValue));
        vc.getDescriptor(optionName).setDisplayName(optionName);

        final OptionalFileSelector ofs = new OptionalFileSelector(optionName);
        ofs.addPropertyChangeListener(optionName, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        // /final ValueRange valueRange = new ValueRange(-180, 180);
        //vc.getDescriptor(optionName).setValueRange(valueRange);

        final BindingContext ctx = new BindingContext(vc);
        SeadasLogger.getLogger().finest(optionName + "  " + optionValue);
        ctx.bind(optionName, ofs.getFileNameField());

        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {

                pi.setValue(ofs.getCurrentFileName());
                debugf("%s %s %n", new String[]{ofs.getCurrentFileName(), pi.getValue()});
            }
        });

        //optionPanel.add(ofs);

        return ofs;

    }

    private void validateParams() {

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


    private JPanel createSourceProductPanel() {

        final String primaryInputFileOptionName = processorModel.getPrimaryInputFileOptionName();
        String primaryInputFileName = processorModel.getParamValue(primaryInputFileOptionName);

        //String ifileNamePad = StringUtil.repeat(" ", ParamUtils.getLongestIFileNameLength());
        sourceProductSelector.setProductNameLabel(new JLabel(L2genData.IFILE));
//        if (processorModel.getInputFile() != null ) {
//             sourceProductSelector.setSelectedFile(processorModel.getInputFile());
//        }


        System.out.println("primary input file name:" + processorModel.getParamValue(primaryInputFileOptionName));
        if (primaryInputFileName != null) {
            sourceProductSelector.getIfileTextfield().setText(primaryInputFileName);
            sourceProductSelector.setSelectedFile(new File(primaryInputFileName));
        }

        final JPanel panel = sourceProductSelector.createDefaultPanel();

        sourceProductSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "MER_RR__1PPBCM20030730_071000_000003972018_00321_07389_0000.N1");

        sourceProductSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {
                File sourceProduct = getSourceProduct().getFileLocation();


                if (sourceProduct != null) {
                    if (handleIfileJComboBoxEnabled) {
                        processorModel.updateParamInfo(primaryInputFileOptionName, sourceProduct.toString());
                        outputFileSelector.getModel().setProductDir(sourceProduct.getParentFile());
                        outputFileSelector.getModel().setProductName(sourceProduct.getName() + "_out");

                        File geoFile = getGeoFile(sourceProduct);
                        if (geoFile.exists()) {
                            geoFileSelector.setSelectedFile(geoFile);
                            processorModel.updateParamInfo(L2genData.GEOFILE, geoFile.toString());
                        }


                    }
                }
            }
        });
//        sourceProductSelector.addFocusListener(new FocusListener() {
//            @Override
//            public void focusGained(FocusEvent focusEvent) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public void focusLost(FocusEvent focusEvent) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//        });
//
//         processorModel.addPropertyChangeListener(primaryInputFileOptionName, new PropertyChangeListener() {
//             @Override
//             public void propertyChange(PropertyChangeEvent evt) {
//                 File iFile = new File(processorModel.getParamValue(primaryInputFileOptionName));
//                 //handlerEnabled[0] = false;
//                 if (iFile != null && iFile.exists()) {
//                     sourceProductSelector.setSelectedFile(iFile);
//                 } else {
//                     sourceProductSelector.releaseProducts();
//                 }
//                 //handlerEnabled[0] = true;
//             }
//         });
//        Component[] panelCs =  panel.getComponents();
//        for (Component c :panelCs ) {
//            System.out.println(c.getClass());
//            if (c.getClass().toString().indexOf("JComboBox") != -1 )
//            {
//                ((JComboBox)c).setSelectedItem(primaryInputFileName);
//            }
//        }
//        final PropertyContainer vc = new PropertyContainer();
//        vc.addProperty(Property.create(primaryInputFileOptionName, primaryInputFileName));
//        vc.getDescriptor(primaryInputFileOptionName).setDisplayName(primaryInputFileOptionName);
//        final BindingContext ctx = new BindingContext(vc);
//        System.out.println("property changed in parfile:" + processorModel.getParamValue(primaryInputFileOptionName) );
//        ctx.bind(primaryInputFileOptionName, sourceProductSelector.getIfileTextfield());
//
//        ctx.addPropertyChangeListener(primaryInputFileOptionName, new PropertyChangeListener() {
//
//            @Override
//            public void propertyChange(PropertyChangeEvent pce) {
//
//                System.out.println("property changed:" + sourceProductSelector.getIfileTextfield().getText() + " "  + sourceProductSelector.getSelectedProduct().getName());
//                processorModel.updateParamInfo(primaryInputFileOptionName, sourceProductSelector.getSelectedProduct().getName());
//            }
//        });


        inputFilePanelHeight = panel.getHeight();

        panel.validate();
        panel.repaint();
        return panel;
    }

    private JPanel createGeoFilePanel() {
        geoFileSelector.setProductNameLabel(new JLabel(L2genData.GEOFILE));
        final JPanel panel = geoFileSelector.createDefaultPanel();

        //     sourceProductSelector.getProductNameLabel().setText("Name:");
        geoFileSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "MER_RR__1PPBCM20030730_071000_000003972018_00321_07389_0000.N1");

        geoFileSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {


                if (geoFileSelector.getSelectedProduct() != null
                        && geoFileSelector.getSelectedProduct().getFileLocation() != null) {
                    if (handleIfileJComboBoxEnabled) {
                        //   l2genData.setParamValue(l2genData.IFILE, sourceProductSelector.getSelectedProduct().getProgramName());
                        processorModel.updateParamInfo(L2genData.GEOFILE, geoFileSelector.getSelectedProduct().getFileLocation().toString());
                    }
                }
            }
        });
        inputFilePanelHeight = inputFilePanelHeight + panel.getHeight();
        return panel;
    }

    private void ifileChangedEventHandler() {

        File ifile = new File(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));

        if (sourceProductSelector != null) {
            handleIfileJComboBoxEnabled = false;
            sourceProductSelector.setSelectedFile(ifile);
            handleIfileJComboBoxEnabled = true;
        }


    }

    protected void handleParamChanged() {

    }

    private JPanel createOutputFilePanel() {
//        if (! processorModel.hasPrimaryOutputFile() ) {
//            return null;
//        }
        outputFileSelector.setOutputFileNameLabel(new JLabel(L2genData.OFILE));
        outputFileSelector.setOutputFileDirLabel(new JLabel("Save in:"));
        final JPanel panel = outputFileSelector.createDefaultPanel();

//        outputFileSelector.getModel().getProductNameComboBox().setPrototypeDisplayValue(
//                "MER_RR__1PPBCM20030730_071000_000003972018_00321_07389_0000.N1");
        outputFileSelector.getModel().getValueContainer().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                String ofile = null;
                if (outputFileSelector.getModel().getProductFile() != null) {
                    ofile = outputFileSelector.getModel().getProductFile().getAbsolutePath();
                }

                if (ofile != null) {
                    if (handleOfileSelecterEnabled) {

                        processorModel.updateParamInfo(processorModel.getPrimaryOutputFileOptionName(), ofile);
                    }
                }
            }
        });
        outputFilePanelHeight = panel.getHeight();
        return panel;
    }

    private JPanel makeOutputFileOptionField(final ParamInfo pi) {

        final OutputFileSelector outputFileSelector = new OutputFileSelector(VisatApp.getApp(), "ofile");
        outputFileSelector.setOutputFileNameLabel(new JLabel(pi.getName()));
        outputFileSelector.setOutputFileDirLabel(new JLabel(" Save in: "));
        final JPanel panel = outputFileSelector.createDefaultPanel();

//        outputFileSelector.getModel().getProductNameComboBox().setPrototypeDisplayValue(
//                "MER_RR__1PPBCM20030730_071000_000003972018_00321_07389_0000.N1");
        outputFileSelector.getModel().getValueContainer().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                String ofile = null;
                if (outputFileSelector.getModel().getProductFile() != null) {
                    ofile = outputFileSelector.getModel().getProductFile().getAbsolutePath();
                    //System.out.println("ofile: " + ofile);
                }

                if (ofile != null) {
                    if (handleOfileSelecterEnabled) {
                        processorModel.updateParamInfo(pi, ofile);
                    }
                }
            }
        });
        outputFilePanelHeight = panel.getHeight();
        return panel;
    }

    private JPanel makeInputFileOptionField(final ParamInfo pi) {
        final SourceProductFileSelector inputFileSelector = new SourceProductFileSelector(VisatApp.getApp(), "ifile");
        inputFileSelector.setProductNameLabel(new JLabel(pi.getName()));
        final JPanel panel = inputFileSelector.createDefaultPanel();

        //     sourceProductSelector.getProductNameLabel().setText("Name:");
        inputFileSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "MER_RR__1PPBCM20030730_071000_000003972018_00321_07389_0000.N1");

        inputFileSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {

                if (inputFileSelector.getSelectedProduct() != null
                        && inputFileSelector.getSelectedProduct().getFileLocation() != null) {
                    if (handleIfileJComboBoxEnabled) {
                        processorModel.updateParamInfo(pi, inputFileSelector.getSelectedProduct().getFileLocation().toString());
                    }
                }
            }
        });
        // inputFilePanelHeight = panel.getHeight();
        return panel;
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
        //System.out.println(string);
    }

    private void debugf(String formatString, Object[] messages) {
        //System.out.printf(formatString, messages);
    }


}