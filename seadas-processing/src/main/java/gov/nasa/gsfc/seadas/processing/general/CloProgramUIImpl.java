/*
Author: Danny Knowles
    Don Shea
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
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.BeamFileChooser;
import org.esa.beam.util.io.FileUtils;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;


public class CloProgramUIImpl extends JPanel implements CloProgramUI {

    private final String programName;
    private final SourceProductFileSelector sourceProductSelector;
    private final SourceProductFileSelector geoFileSelector;
    private final OutputFileSelector outputFileSelector;
    //private final OptionalFileSelector geoFileSelector;

    private JFileChooser parfileChooser = new JFileChooser();
    private JFileChooser geofileChooser = new JFileChooser();

    private DefaultMutableTreeNode rootNode;
    private File selectedFile;

    private int tabCount = 0;

    private static final String MAIN_TAB_NAME = "Main";


    private ProcessorModel processorModel;

    int outputFilePanelHeight;
    int inputFilePanelHeight;

    private boolean handleIfileJComboBoxEnabled = true;
    private boolean handleOfileSelecterEnabled = true;


    CloProgramUIImpl(String programName, String xmlFileName) {
        this.programName = programName;

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

        final JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(createInputOutputPanel(),
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));

        mainPanel.add(createParamPanel(),
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 3));

        mainPanel.add(outputFileSelector.getOpenInAppCheckBox(),
                new GridBagConstraintsCustom(0, 2, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        add(mainPanel);
    }


    private JPanel createInputOutputPanel() {

        final JPanel inputOutputPanel = new JPanel(new GridBagLayout());
        inputOutputPanel.setBorder(BorderFactory.createTitledBorder("Primary Input/Output Files"));

        inputOutputPanel.add(createSourceProductPanel(),
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        if (processorModel.hasGeoFile()) {
            inputOutputPanel.add(createGeoFilePanel(),
                    new GridBagConstraintsCustom(0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        }

        inputOutputPanel.add(createOutputFilePanel(),
                new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));


        return inputOutputPanel;
    }


    private JPanel createParamPanel() {
        //final JScrollPane textScrollPane = new JScrollPane(parameterTextArea);
        final JScrollPane textScrollPane = new JScrollPane(createParamPanel(processorModel.getProgramParamList()));

        textScrollPane.setPreferredSize(new Dimension(600, 300));

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
                final BeamFileChooser beamFileChooser = new BeamFileChooser();
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
                        while (line != null) {
                            System.out.printf("%1$d %2$s %n", reader.getLineNumber(), line);
                            line = reader.readLine();

                            //System.out.println(line);
                            //System.out.println(reader.getLineNumber());
                        }
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
            //fileWriter.write(parameterTextArea.getText());
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

    private File getGeoFile() {
        final Product selectedProduct = sourceProductSelector.getSelectedProduct();
        if (selectedProduct == null
                || selectedProduct.getFileLocation() == null) {
            return null;
        }
        final File productFile = selectedProduct.getFileLocation();
        return FileUtils.exchangeExtension(productFile, ".GEO");
    }

    private void showErrorMessage(JComponent parent) {
        JOptionPane.showMessageDialog(parent,
                "Unable to create parameter file:\n'" + selectedFile + "'",
                "",
                JOptionPane.ERROR);
    }


    private JPanel createParamPanel(ArrayList<ParamInfo> paramList) {

        JPanel paramPanel = new JPanel();
        JPanel booleanParamPanel = new JPanel();
        JPanel fileParamPanel = new JPanel();
        //Dimension paramPanelDimension = new Dimension(1000, 800);
        TableLayout paramLayout = new TableLayout(4);
        paramPanel.setLayout(paramLayout);
        TableLayout booelanParamLayout = new TableLayout(3);
        booleanParamPanel.setLayout(booelanParamLayout);

        TableLayout fileParamLayout = new TableLayout(1);
        fileParamPanel.setLayout(fileParamLayout);

        //paramPanel.setBorder(new EmptyBorder(null));
        //paramPanel.setPreferredSize(paramPanelDimension);

        Iterator itr = paramList.iterator();
        while (itr.hasNext()) {
            final ParamInfo pi = (ParamInfo) itr.next();
            if (!(pi.getName().equals(ParamUtils.IFILE) || pi.getName().equals("infile") || pi.getName().equals(ParamUtils.OFILE) || pi.getName().equals("geofile"))) {
                debug(pi.getName());
                switch (pi.getType()) {
                    case BOOLEAN:
                        booleanParamPanel.add(makeBooleanOptionField(pi));
                        break;
                    case OFILE:
                        fileParamPanel.add(makeFileOptionField(pi));
                        break;
                    case IFILE:
                        fileParamPanel.add(makeFileOptionField(pi));
                        break;
                    case STRING:
                        paramPanel.add(makeOptionField(pi));
                }
                //paramPanel.add(makeOptionField(pi));
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
        paramPanel.add(fileParamPanel);
        paramPanel.add(booleanParamPanel);
        return paramPanel;
    }

    private JPanel makeOptionField(final ParamInfo pi) {

        final String optionName = pi.getName();
        final String optionValue = pi.getValue();
        final JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionPanel.add(new JLabel(optionName));


        final PropertyContainer vc = new PropertyContainer();
        vc.addProperty(Property.create(optionName, optionValue));
        vc.getDescriptor(optionName).setDisplayName(optionName);

        final ValueRange valueRange = new ValueRange(-180, 180);


        vc.getDescriptor(optionName).setValueRange(valueRange);

        final BindingContext ctx = new BindingContext(vc);
        final JTextField field = new JTextField();
        field.setColumns(8);
        field.setHorizontalAlignment(JFormattedTextField.LEFT);
        System.out.println(optionName + "  " + optionValue);
        field.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                String propertyName = propertyChangeEvent.getPropertyName();
                if ("focusOwner".equals(propertyName)) {

                } else if ("focusedWindow".equals(propertyName)) {

                }
                Object source = propertyChangeEvent.getSource();
                // if (source == amountField) {
                //    amount = ((Number)amountField.getValue()).doubleValue();
                //    ...
            }
            //// ...//re-compute payment and update field..

        });
        ctx.bind(optionName, field);

        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {

                pi.setValue(field.getText());

            }
        });

        optionPanel.add(field);

        return optionPanel;

    }

    private JPanel makeBooleanOptionField(final ParamInfo pi) {

        final String optionName = pi.getName();
        final boolean optionValue = new Boolean(pi.getValue());
        final JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
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
        debug(optionName + "  " + optionValue);
        ctx.bind(optionName, field);

        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {

                pi.setValue((new Boolean(field.isSelected())).toString());
                pi.setValue(field.getText());

                System.out.printf("%s %s %n", (new Boolean(field.isSelected())).toString(), field.getText());

            }
        });

        optionPanel.add(field);

        return optionPanel;

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
        debug(optionName + "  " + optionValue);
        ctx.bind(optionName, ofs.getFileNameField());

        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {

                pi.setValue(ofs.getCurrentFileName());
                //pi.setValue(field.getText());
                debugf("%s %s %n", new String[]{ofs.getCurrentFileName(), pi.getValue()});
                //System.out.printf("%s %s %n", (new Boolean(field.isSelected())).toString(), field.getText() );

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
        sourceProductSelector.setProductNameLabel(new JLabel(L2genData.IFILE));
        final JPanel panel = sourceProductSelector.createDefaultPanel();

        //     sourceProductSelector.getProductNameLabel().setText("Name:");
        sourceProductSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "MER_RR__1PPBCM20030730_071000_000003972018_00321_07389_0000.N1");

        sourceProductSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {
                Product sourceProduct = getSourceProduct();


                if (sourceProduct != null && sourceProductSelector.getSelectedProduct() != null
                        && sourceProductSelector.getSelectedProduct().getFileLocation() != null) {
                    if (handleIfileJComboBoxEnabled) {
                        //   l2genData.setParamValue(l2genData.IFILE, sourceProductSelector.getSelectedProduct().getProgramName());
                        processorModel.updateParamInfo(L2genData.IFILE, sourceProductSelector.getSelectedProduct().getFileLocation().toString());
                        File geoFile = getGeoFile();
                        //geoFileSelector;
                        if (geoFile.exists()) {
                            geoFileSelector.setSelectedFile(geoFile);
                            processorModel.updateParamInfo("geofile", geoFile.toString());
                        }


                    }
                }
            }
        });
        inputFilePanelHeight = panel.getHeight();
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

        File ifile = processorModel.getInputFile();

        if (sourceProductSelector != null) {
            handleIfileJComboBoxEnabled = false;
            sourceProductSelector.setSelectedFile(ifile);
            handleIfileJComboBoxEnabled = true;
        }


    }

    private JPanel createOutputFilePanel() {
        outputFileSelector.setOutputFileNameLabel(new JLabel(L2genData.OFILE + " (name)"));
        outputFileSelector.setOutputFileDirLabel(new JLabel(L2genData.OFILE + " (directory)"));
        final JPanel panel = outputFileSelector.createDefaultPanel();

//        outputFileSelector.getModel().getProductNameComboBox().setPrototypeDisplayValue(
//                "MER_RR__1PPBCM20030730_071000_000003972018_00321_07389_0000.N1");
        outputFileSelector.getModel().getValueContainer().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                String ofile = null;
                if (outputFileSelector.getModel().getProductFile() != null) {
                    ofile = outputFileSelector.getModel().getProductFile().getAbsolutePath();
                    System.out.println("ofile: " + ofile);
                }

                if (ofile != null) {
                    if (handleOfileSelecterEnabled) {
                        processorModel.updateParamInfo(L2genData.OFILE, ofile);
                    }
                }
            }
        });
        outputFilePanelHeight = panel.getHeight();
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
        System.out.println(string);
    }

    private void debugf(String formatString, Object[] messages) {
        System.out.printf(formatString, messages);
    }


}