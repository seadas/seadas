package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.BindingContext;
import org.esa.beam.framework.dataio.ProductIOPlugInManager;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.util.io.FileChooserFactory;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Aynur Abdurazik
 * Date: 3/30/12
 * Time: 11:12 AM
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


    public OutputFileSelector(VisatApp app, String outputFrameLabel) {

        this(app, outputFrameLabel, new OutputFileSelectorModel());
    }

    public OutputFileSelector(VisatApp app, String outputFrameLabel, OutputFileSelectorModel model) {
        this.model = model;
        appContext = app;
        this.outFrameLabel = outputFrameLabel;
        initComponents();
        bindComponents();
        updateUIState();
    }

    private void initComponents() {
        outputFileNameLabel = new JLabel("Name: ");
        outputFileNameTextField = new JTextField(25);
        outputFileDirLabel = new JLabel("Directory:");
        outputFileDirTextField = new JTextField(25);
        outputFileDirChooserButton = new JButton(new ProductDirChooserAction());
        openInAppCheckBox = new JCheckBox("Open in " + appContext.getApplicationName());

        final Dimension size = new Dimension(26, 16);
        outputFileDirChooserButton.setPreferredSize(size);
        outputFileDirChooserButton.setMinimumSize(size);
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

    public JPanel createDefaultPanel() {
        final JPanel subPanel1 = new JPanel(new BorderLayout(3, 3));
        subPanel1.add(getOutputFileNameLabel(), BorderLayout.NORTH);
        subPanel1.add(getOutputFileNameTextField(), BorderLayout.CENTER);

        final JPanel subPanel3 = new JPanel(new BorderLayout(3, 3));
        subPanel3.add(getOutputFileDirLabel(), BorderLayout.NORTH);
        subPanel3.add(getOutputFileDirTextField(), BorderLayout.CENTER);
        subPanel3.add(getOutputFileDirChooserButton(), BorderLayout.EAST);

        final TableLayout tableLayout = new TableLayout(1);
        tableLayout.setTableAnchor(TableLayout.Anchor.WEST);
        tableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        tableLayout.setTableWeightX(1.0);

        tableLayout.setCellPadding(0, 0, new Insets(3, 3, 3, 3));
        tableLayout.setCellPadding(1, 0, new Insets(3, 3, 3, 3));
        tableLayout.setCellPadding(2, 0, new Insets(0, 24, 3, 3));
        tableLayout.setCellPadding(3, 0, new Insets(3, 3, 3, 3));

        final JPanel panel = new JPanel(tableLayout);
        panel.setBorder(BorderFactory.createTitledBorder(outFrameLabel));
        panel.add(subPanel1);
        panel.add(subPanel3);
        panel.add(getOpenInAppCheckBox());

        return panel;
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
                    if (selectedDir.canWrite() ) {
                        model.setProductDir(selectedDir);
                    }  else {
                        JOptionPane.showMessageDialog( chooser,
                                                        "Directory " + selectedDir.toString() +  "has no write permission",
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
