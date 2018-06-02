package gov.nasa.gsfc.seadas.processing.common;

import com.bc.ceres.swing.binding.BindingContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.esa.snap.core.dataio.ProductIOPlugInManager;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.FileChooserFactory;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Aynur Abdurazik Date: 3/30/12 Time: 11:12 AM
 */
public class OutputFileSelector {

    private JLabel outputFileNameLabel;
    private JTextField outputFileNameTextField;
    private JLabel outputFileDirLabel;
    private JTextField outputFileDirTextField;
    private JButton outputFileDirChooserButton;

    private JCheckBox openInAppCheckBox;
    private OutputFileSelectorModel model;

    private AppContext appContext;
    private String outFrameLabel;

    public OutputFileSelector(SnapApp app, String outputFrameLabel) {

        this(app, outputFrameLabel, new OutputFileSelectorModel());
    }

    public OutputFileSelector(SnapApp app, String outputFrameLabel, OutputFileSelectorModel model) {
        this.model = model;
        appContext = (AppContext) app;
        this.outFrameLabel = outputFrameLabel;
        initComponents();
        bindComponents();
        updateUIState();
    }

    private void initComponents() {

        outputFileNameLabel = new JLabel("Name: ");
        outputFileNameLabel.setPreferredSize(outputFileNameLabel.getPreferredSize());
        outputFileNameLabel.setMinimumSize(outputFileNameLabel.getPreferredSize());
        outputFileNameLabel.setMaximumSize(outputFileNameLabel.getPreferredSize());

        outputFileNameTextField = new JTextField("123456789 123456789 12345");
        outputFileNameTextField.setPreferredSize(outputFileNameTextField.getPreferredSize());
        outputFileNameTextField.setMaximumSize(outputFileNameTextField.getPreferredSize());
        outputFileNameTextField.setMinimumSize(outputFileNameTextField.getPreferredSize());
        outputFileNameTextField.setText("");

        outputFileDirLabel = new JLabel("Directory:");
        outputFileDirLabel.setPreferredSize(outputFileDirLabel.getPreferredSize());
        outputFileDirLabel.setMinimumSize(outputFileDirLabel.getPreferredSize());
        outputFileDirLabel.setMaximumSize(outputFileDirLabel.getPreferredSize());

        outputFileDirTextField = new JTextField("123456789 123456789 12345");
        outputFileDirTextField.setPreferredSize(outputFileDirTextField.getPreferredSize());
        outputFileDirTextField.setMinimumSize(outputFileDirTextField.getPreferredSize());
        outputFileDirTextField.setMaximumSize(outputFileDirTextField.getPreferredSize());
        outputFileDirTextField.setText("");

        outputFileDirChooserButton = new JButton(new ProductDirChooserAction());

        outputFileDirChooserButton.setMargin(new Insets(0, -7, 0, -7));
        final Dimension size = new Dimension(outputFileDirChooserButton.getPreferredSize().width,
                outputFileDirTextField.getPreferredSize().height);
        outputFileDirChooserButton.setPreferredSize(size);
        outputFileDirChooserButton.setMinimumSize(size);
        outputFileDirChooserButton.setMaximumSize(size);

        openInAppCheckBox = new JCheckBox("Open in " + appContext.getApplicationName());

    }

    private void bindComponents() {
        final BindingContext bc = new BindingContext(model.getValueContainer());

        bc.bind("productName", outputFileNameTextField);
        bc.bind("openInAppSelected", openInAppCheckBox);
        bc.bind("productDir", outputFileDirTextField);

        model.getValueContainer().addPropertyChangeListener("productDir", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                outputFileDirTextField.setToolTipText(model.getProductDir().getPath());
            }
        });

    }

    public OutputFileSelectorModel getModel() {
        return model;
    }

    public JLabel getOutputFileNameLabel() {
        return outputFileNameLabel;
    }

    public JTextField getOutputFileNameTextField() {
        return outputFileNameTextField;
    }

    public JTextField getOutputFileNameTextField(int length) {
        outputFileNameTextField.setColumns(length);
        return outputFileNameTextField;
    }

    public JLabel getOutputFileDirLabel() {
        return outputFileDirLabel;
    }

    public JTextField getOutputFileDirTextField() {
        return outputFileDirTextField;
    }

    public JButton getOutputFileDirChooserButton() {
        return outputFileDirChooserButton;
    }

    public JCheckBox getOpenInAppCheckBox() {
        return openInAppCheckBox;
    }

    public void setOutputFileNameLabel(JLabel jLabel) {
        this.outputFileNameLabel = jLabel;
    }

    public void setOutputFileDirLabel(JLabel jLabel) {
        this.outputFileDirLabel = jLabel;
    }

    public JPanel createDefaultPanel() {

        final JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(getOutputFileNameLabel(),
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
        mainPanel.add(getOutputFileNameTextField(),
                new GridBagConstraintsCustom(1, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 2));
        mainPanel.add(getOutputFileDirLabel(),
                new GridBagConstraintsCustom(2, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
        mainPanel.add(getOutputFileDirTextField(),
                new GridBagConstraintsCustom(3, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 2));
        mainPanel.add(getOutputFileDirChooserButton(),
                new GridBagConstraintsCustom(4, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));

        return mainPanel;
    }

    private void updateUIState() {
        if (model.isSaveToFileSelected()) {
            openInAppCheckBox.setEnabled(canReadOutputFormat(model.getFormatName()));
            outputFileDirLabel.setEnabled(true);
            outputFileDirTextField.setEnabled(true);
            outputFileDirChooserButton.setEnabled(true);
        } else {
            openInAppCheckBox.setEnabled(false);
            outputFileDirTextField.setEnabled(false);
            outputFileDirTextField.setEnabled(false);
            outputFileDirChooserButton.setEnabled(false);
        }
    }

    public void setEnabled(boolean enabled) {
        outputFileNameLabel.setEnabled(enabled);
        outputFileNameTextField.setEnabled(enabled);
        outputFileDirLabel.setEnabled(enabled);
        outputFileDirTextField.setEnabled(enabled);
        outputFileDirChooserButton.setEnabled(enabled);
        openInAppCheckBox.setEnabled(enabled);
    }

    private static boolean canReadOutputFormat(String formatName) {
        return ProductIOPlugInManager.getInstance().getReaderPlugIns(formatName).hasNext();
    }

    private class UIStateUpdater implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!canReadOutputFormat(model.getFormatName())) {
                model.setOpenInAppSelected(false);
            }
            updateUIState();
        }
    }

    private class ProductDirChooserAction extends AbstractAction {

        private static final String APPROVE_BUTTON_TEXT = "Select";

        public ProductDirChooserAction() {
            super("...");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            Window windowAncestor = null;
            if (event.getSource() instanceof JComponent) {
                JButton button = (JButton) event.getSource();
                if (button != null) {
                    windowAncestor = SwingUtilities.getWindowAncestor(button);
                }
            }
            final JFileChooser chooser = FileChooserFactory.getInstance().createDirChooser(model.getProductDir());
            chooser.setDialogTitle("Select Output File Directory");
            if (chooser.showDialog(windowAncestor, APPROVE_BUTTON_TEXT) == JFileChooser.APPROVE_OPTION) {
                final File selectedDir = chooser.getSelectedFile();
                if (selectedDir != null) {
                    if (selectedDir.canWrite()) {
                        model.setProductDir(selectedDir);
                    } else {
                        JOptionPane.showMessageDialog(chooser,
                                "Directory " + selectedDir.toString() + "has no write permission",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        chooser.requestFocus();
                    }

                } else {
                    model.setProductDir(new File("."));
                }
            }
        }
    }
}
