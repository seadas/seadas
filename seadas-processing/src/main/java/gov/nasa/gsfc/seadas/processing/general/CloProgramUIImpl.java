/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.l2gen.ParamInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.ParamValidValueInfo;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.BeamFileChooser;
import org.esa.beam.util.io.FileUtils;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;

public class CloProgramUIImpl extends JPanel implements CloProgramUI {

    private final JTextArea parameterTextArea;
    private final SourceProductFileSelector sourceProductSelector;
    private final OutputFileSelector outputFileSelector;
    private File selectedFile;
    private String programName;
    private final JPanel parameterPanel;
    private ProcessorModel processorModel;
    private File defaultOutputDir;


    public CloProgramUIImpl(String programName, String xmlFileName) {
        super(new BorderLayout());

        this.programName = programName;
        processorModel = new ProcessorModel(programName, xmlFileName);

        parameterTextArea = new JTextArea(getDefaultText());

        parameterPanel = createParameterPanel(processorModel.getProgramParamList());

        sourceProductSelector = new SourceProductFileSelector(VisatApp.getApp(), "");
        sourceProductSelector.setProcessorModel(processorModel);
        //sourceProductSelector.initProducts();

        outputFileSelector = new OutputFileSelector(VisatApp.getApp(), "Output File");

        initUI();
    }


    //private void


    public void updateProcessorModel() {

        Product selectedProduct = sourceProductSelector.getSelectedProduct();
        if (sourceProductSelector.getSelectedProduct() != null) {
            final File inputFile = selectedProduct.getFileLocation();
            System.out.println("update processors model " + inputFile.toString());
            processorModel.setInputFile(inputFile);
        }

        OutputFileSelectorModel outputFileSelectorModel = outputFileSelector.getModel();
        if (outputFileSelectorModel != null) {
            processorModel.setOutputFileDir(outputFileSelectorModel.getProductDir());
            processorModel.setOutputFileName(outputFileSelectorModel.getProductFileName());
        }
    }

    public ProcessorModel getProcessorModel() {
        updateProcessorModel();
        return processorModel;
    }

    public Product getSelectedSourceProduct() {

        return sourceProductSelector.getSelectedProduct();
    }

    public File getOutputFile() {
        return outputFileSelector.getModel().getProductFile();
    }


//    public String getProcessingParameters() {
//        return parameterTextArea.getText();
//    }

    public String getProcessingParameters() {
        String parameterString = new String("\n");
        ArrayList<ParamInfo> paramInfos = processorModel.getProgramParamList();
        for (ParamInfo paramInfo : paramInfos) {
            if (!paramInfo.getName().equals(ParamUtils.IFILE) && !paramInfo.getName().equals(ParamUtils.OFILE)) {
                parameterString = parameterString + paramInfo.getName() + "=" + paramInfo.getValue() + "\n";
            }
        }
        parameterString = parameterString + "programName=" + programName + "\n";
        System.out.println(parameterString);
        return parameterString;
    }

    private void initUI() {
        //final JScrollPane textScrollPane = new JScrollPane(parameterTextArea);
        final JScrollPane textScrollPane = new JScrollPane(parameterPanel);

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
        //parameterComponent.add(parameterPanel, BorderLayout.CENTER);
        parameterComponent.add(buttonPanel, BorderLayout.SOUTH);

        add(sourceProductSelector.createDefaultPanel(), BorderLayout.NORTH);
        add(parameterComponent, BorderLayout.CENTER);
        add(outputFileSelector.createDefaultPanel(), BorderLayout.SOUTH);
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
                        parameterTextArea.setText("");
                        String line;
                        line = reader.readLine();
                        while (line != null) {
                            parameterTextArea.append(line);
                            parameterTextArea.append("\n");
                            line = reader.readLine();
                        }
                    } catch (IOException e1) {
                        System.err.println(e1.getMessage());
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(parent,
                                "Unable to load parameter file '" + file + "'.\n" +
                                        "Error reading file.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        parameterTextArea.setText(getDefaultText());
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
            fileWriter.write(parameterTextArea.getText());
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

    private void showErrorMessage(JComponent parent) {
        JOptionPane.showMessageDialog(parent,
                "Unable to create parameter file:\n'" + selectedFile + "'",
                "",
                JOptionPane.ERROR);
    }

    private JPanel createParameterPanel(ArrayList<ParamInfo> paramList) {

        final JPanel paramPanel = new JPanel();
        paramPanel.setBorder(new TitledBorder(" Parameters "));

        paramPanel.setLayout(new FlowLayout());

        ParamInfo.Type paramType;
        for (ParamInfo paramInfo : paramList) {
            paramType = paramInfo.getType();
            if (paramType == ParamInfo.Type.BOOLEAN) {
                final JCheckBox booleanCheckBox = new JCheckBox();
                paramPanel.add(booleanCheckBox);

            } else if (!(paramInfo.getName().equals(ParamUtils.IFILE) || paramInfo.getName().equals(ParamUtils.OFILE))) {
                if (paramInfo.hasValidValueInfos()) {

                    paramPanel.add(makeComboBoxOptionPanel(paramInfo));

                } else {
                    paramPanel.add(makeTextFieldOptionPanel(paramInfo));
                }
            }


        }
        return paramPanel;
    }

    private JPanel makeComboBoxOptionPanel(final ParamInfo paramInfo) {
        final JPanel singlePanel = new JPanel();
        singlePanel.setLayout(new FlowLayout());

        final JLabel optionNameLabel = new JLabel(paramInfo.getName());

        singlePanel.add(optionNameLabel);


        String optionDefaultValue = paramInfo.getValue();


        ArrayList<ParamValidValueInfo> validValues = paramInfo.getValidValueInfos();
        String[] values = new String[validValues.size()];
        validValues.toArray(values);

        final JComboBox inputList = new JComboBox(values);
        inputList.setEditable(true);
        inputList.setPreferredSize(new Dimension(inputList.getPreferredSize().width,
                                                 inputList.getPreferredSize().height));
        int defaultValuePosition = validValues.indexOf(optionDefaultValue);

        if (defaultValuePosition != -1) {
            inputList.setSelectedIndex(defaultValuePosition);
        }

        inputList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String newValue = (String) inputList.getSelectedItem();
                processorModel.updateParamInfo(paramInfo, newValue);
            }
        });
        inputList.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
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

    private JPanel makeTextFieldOptionPanel(final ParamInfo paramInfo) {
        final JPanel singlePanel = new JPanel();
        singlePanel.setLayout(new FlowLayout());

        final JLabel optionNameLabel = new JLabel(paramInfo.getName());

        singlePanel.add(optionNameLabel);


        String optionDefaultValue = paramInfo.getDefaultValue();
        final JTextField inputField = new JTextField(optionDefaultValue);
        inputField.setToolTipText(paramInfo.getDescription());
        inputField.setColumns(5);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String newValue = inputField.getText();
                processorModel.updateParamInfo(paramInfo, newValue);
            }
        });

        inputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                String newValue = inputField.getText();
                validateInput(newValue);
                processorModel.updateParamInfo(paramInfo, newValue);
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {

            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
            }
        });

        inputField.addMouseListener(new MouseListener() {
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
                double value;
                try {
                    String textValue = inputField.getText();
                    value = Double.parseDouble(textValue);
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(singlePanel, "Please enter valid number.");
                    inputField.requestFocusInWindow();
                    return;
                }

                boolean valid = validateInput(inputField.getText());

                if (!valid) {
                    inputField.requestFocusInWindow();
                    return;
                }
            }
        });
        singlePanel.add(inputField);

        switch (paramInfo.getType()) {
            case STRING:
                break;
            case INT:
                break;
            case BOOLEAN:
                final JCheckBox booleanCheckBox = new JCheckBox();
                singlePanel.add(booleanCheckBox);
                break;
            case FLOAT:
                break;
        }

        return singlePanel;
    }


    private boolean validateInput(String input) {
        //processorModel.updateParamInfo(paramInfo, newValue);
        return true;

    }

    private String getDefaultText() {
        // changed all products using wavelength 555 to 560
        // wavelength 555 is not valid for GIOP when using MERIS
        return "l2prod=chl_giop,a_443_giop,a_560_giop,bb_443_giop,bb_560_giop,aph_443_giop,aph_560_giop,adg_443_giop,adg_s_giop,bbp_443_giop,bbp_s_giop,rrsdiff_giop\n" +
                "\n" +
                "# L-M fit\n" +
                "giop_fit_opt=1\n" +
                "\n" +
                "# Morel f/Q to relate bb/(a+bb) to Rrs\n" +
                "giop_rrs_opt=1\n" +
                "\n" +
                "# Bricaud 1995 aph spectrum \n" +
                "giop_aph_opt=2\n" +
                "\n" +
                "# exponential adg function\n" +
                "giop_adg_opt=1\n" +
                "giop_adg_s=0.0145\n" +
                "\n" +
                "# power-law bbp with QAA adaptive exponent\n" +
                "giop_bbp_opt=3\n" +
                "programName=" + programName + "\n";
    }


}

