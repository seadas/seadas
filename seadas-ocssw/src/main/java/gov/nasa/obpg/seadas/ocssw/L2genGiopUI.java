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

package gov.nasa.obpg.seadas.ocssw;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.ui.SourceProductSelector;
import org.esa.beam.util.io.BeamFileChooser;
import org.esa.beam.util.io.FileUtils;
import org.esa.beam.visat.VisatApp;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;

class L2genGiopUI extends JPanel {

    private final JTextArea parameterTextArea;
    private final SourceProductSelector sourceProductSelector;
    private File selectedFile;

    public L2genGiopUI() {
        super(new BorderLayout());

        parameterTextArea = new JTextArea(getDefaultText());

        sourceProductSelector = new SourceProductSelector(VisatApp.getApp(), "Source Product");
        sourceProductSelector.initProducts();

        initUI();
    }

    public Product getSelectedSourceProduct() {
        return sourceProductSelector.getSelectedProduct();
    }

    public String getProcessingParameters() {
        return parameterTextArea.getText();
    }

    private void initUI() {
        final JScrollPane textScrollPane = new JScrollPane(parameterTextArea);
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

        add(sourceProductSelector.createDefaultPanel(), BorderLayout.NORTH);
        add(parameterComponent, BorderLayout.CENTER);
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
                "giop_bbp_opt=3";
    }

}
