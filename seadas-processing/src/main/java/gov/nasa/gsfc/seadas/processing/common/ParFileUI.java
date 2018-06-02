package gov.nasa.gsfc.seadas.processing.common;

import static gov.nasa.gsfc.seadas.processing.common.FileSelector.PROPERTY_KEY_APP_LAST_OPEN_DIR;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.logging.Logger;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.SnapFileChooser;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/7/12
 * Time: 9:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class ParFileUI {
    private ProcessorModel processorModel;

    private final JCheckBox openInAppCheckBox;
    private final JCheckBox showDefaultCheckBox;

    private JPanel parStringPanel;

    public ParFileUI(ProcessorModel pm) {
        processorModel = pm;
        openInAppCheckBox = new JCheckBox("Open in " + SnapApp.getDefault().getAppContext().getApplicationName());
        openInAppCheckBox.setSelected(pm.isOpenInSeadas());
        parStringPanel = new JPanel(new GridBagLayout());
        showDefaultCheckBox = new JCheckBox("Show Default Values");
        createParStringButtonPanel();
    }


    public JPanel getParStringPanel() {
        return parStringPanel;
    }

    private JTextArea createParStringEditor(String parString) {
        JTextArea parStringEditor = new JTextArea(parString);
        parStringEditor.setPreferredSize(parStringEditor.getPreferredSize());

        return parStringEditor;

    }

    public boolean isOpenOutputInApp() {
        return openInAppCheckBox.isSelected();
    }

    private void createParStringButtonPanel() {

        final JButton saveParameterFileButton = new JButton("Save Parameters...");
        final JButton loadParameterButton = new JButton("Load Parameters...");

        //The above two buttons are only active if an ocssw processor accepts par file.
        if (processorModel.acceptsParFile()) {
            saveParameterFileButton.addActionListener(createSafeAsAction());
            loadParameterButton.addActionListener(createLoadParameterAction());
        } else {
            saveParameterFileButton.setEnabled(false);
            loadParameterButton.setEnabled(false);
        }


        showDefaultCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        parStringPanel.add(loadParameterButton,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        parStringPanel.add(saveParameterFileButton,
                new GridBagConstraintsCustom(1, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        //TODO: add a checkbox to show default  values of params
//        parStringPanel.add(showDefaultCheckBox,
//                new GridBagConstraintsCustom(2, 0, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        parStringPanel.add(openInAppCheckBox,
                new GridBagConstraintsCustom(3, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        parStringPanel.setMaximumSize(parStringPanel.getPreferredSize());
        parStringPanel.setMinimumSize(parStringPanel.getPreferredSize());
    }

    private ActionListener createLoadParameterAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Component parent = (Component) e.getSource();
                Logger.getGlobal().info("current working directory: " + processorModel.getRootDir().getAbsolutePath());

                String homeDirPath = SystemUtils.getUserHomeDir().getPath();
                String openDir =((AppContext) SnapApp.getDefault()).getPreferences().getPropertyString(PROPERTY_KEY_APP_LAST_OPEN_DIR,
                       homeDirPath);
                final SnapFileChooser beamFileChooser = new SnapFileChooser(new File(openDir));
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
                        String[] option;
                        ParamInfo pi;
                        while (line != null) {
                            if (line.indexOf("=") != -1) {
                                option = line.split("=", 2);
                                option[0] = option[0].trim();
                                option[1] = option[1].trim();
                                if(!option[0].substring(0,1).equals("#")) { // ignore comments with = signs
                                     Logger.getGlobal().info("option1: " + option[0] + "   option2: " + option[1])  ;
                                    pi = processorModel.getParamInfo(option[0]);
                                    if (pi == null) {
                                        JOptionPane.showMessageDialog(parent,
                                                file.getName() + " is not a correct par file for " + processorModel.getProgramName() + "'.\n",
                                                "Error",
                                                JOptionPane.ERROR_MESSAGE);
                                        return;
                                    } else if (option[0].equals(processorModel.getPrimaryInputFileOptionName())) {
                                        // make a relative ifile relative to the par file
                                        File tmpFile = SeadasFileUtils.createFile(file.getParentFile(), option[1]);
                                        option[1] = tmpFile.getAbsolutePath();

                                        if (!processorModel.updateIFileInfo(option[1])) {
                                            JOptionPane.showMessageDialog(parent,
                                                "ifile " + option[1] + " is not found. Please include absolute path in the ifile name or select ifile through file chooser.",
                                                "Error",
                                                JOptionPane.ERROR_MESSAGE);
                                            //showErrorMessage(parent, "ifile is not found. Please include absolute path in the ifile name or select ifile through file chooser.");
                                            return;
                                        }
                                    } else if (option[0].equals(processorModel.getPrimaryOutputFileOptionName())) {
                                        String ofileName = option[1];
                                        if ( ! ofileName.startsWith(System.getProperty("file.separator")))  {
                                            ofileName = processorModel.getIFileDir().getAbsolutePath() + ofileName;
                                        }
                                        if (!processorModel.updateOFileInfo(ofileName)) {
                                            showErrorMessage(parent, "ofile directory does not exist!");
                                            return;
                                        }
                                    } else {
                                        processorModel.updateParamInfo(pi, option[1]);
                                    }
                                } // not comment line
                            }
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
                File selectedFile = getParameterFile();
                try {
                    saveParameterFile(parent, selectedFile);
                } catch (IOException e1) {
                    showErrorMessage(parent, selectedFile.getName());
                }
            }
        };
    }

    private File saveParameterFile(JComponent parent, File selectedFile) throws IOException {
        final SnapFileChooser beamFileChooser = new SnapFileChooser();
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
                    saveParameterFile(parent, null);
                }
            } else if (selectedFile.createNewFile()) {
                return writeParameterFileTo(selectedFile);
            } else {
                showErrorMessage(parent, selectedFile.getName());
            }
        }
        return null;
    }

    private File writeParameterFileTo(File parameterFile) throws IOException {
        FileWriter fileWriter = null;
        ParFileManager parFileManager = new ParFileManager(processorModel);
        try {
            fileWriter = new FileWriter(parameterFile);
            fileWriter.write(parFileManager.getParString());
            return parameterFile;
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    private File getParameterFile() {
        final File productFile = new File(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
        if (productFile !=null){
            return FileUtils.exchangeExtension(productFile, ".par");
        }

        else {
            return null;
        }
    }

    private void showErrorMessage(Component parent, String parFileName) {
        JOptionPane.showMessageDialog(parent,
                "Unable to create parameter file:\n'" + parFileName + "'",
                "",
                JOptionPane.ERROR_MESSAGE);
    }
}
