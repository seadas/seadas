package gov.nasa.gsfc.seadas.bathymetry.ui;

import gov.nasa.gsfc.seadas.bathymetry.operator.BathymetryOp;
import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.tool.ToolButtonFactory;

import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 9/5/12
 * Time: 8:46 AM
 * To change this template use File | Settings | File Templates.
 */


class BathymetryDialog extends JDialog {

    private BathymetryData bathymetryData = null;
    private Component helpButton = null;
    private HelpBroker helpBroker = null;

    private final static String HELP_ID = "bathymetry";
    private final static String HELP_ICON = "icons/Help24.gif";


    public BathymetryDialog(BathymetryData bathymetryData, boolean masksCreated, boolean bandCreated) {
        this.bathymetryData = bathymetryData;

        initHelpBroker();

        if (helpBroker != null) {
            helpButton = getHelpButton(HELP_ID);
        }

        if (masksCreated) {
            createNotificationUI();
        } else {
            createBathymetryUI(bandCreated);
        }
    }


    protected Component getHelpButton(String helpId) {
        if (helpId != null) {

            final AbstractButton helpButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon(HELP_ICON),
                    false);

            HelpSet helpSet = helpBroker.getHelpSet();
            helpBroker.setCurrentID(helpId);

            if (helpButton != null) {
                helpButton.setToolTipText("Help");
                helpButton.setName("helpButton");
                helpBroker.enableHelpKey(helpButton, helpId, helpSet);
                helpBroker.enableHelpOnButton(helpButton, helpId, helpSet);
            }

            return helpButton;
        }

        return null;
    }


    private void initHelpBroker() {
        HelpSet helpSet = HelpSys.getHelpSet();
        if (helpSet != null) {
            helpBroker = helpSet.createHelpBroker();
            if (helpBroker instanceof DefaultHelpBroker) {
                DefaultHelpBroker defaultHelpBroker = (DefaultHelpBroker) helpBroker;
                defaultHelpBroker.setActivationWindow(this);
            }
        }
    }


    public final void createNotificationUI() {
        JButton createMasks = new JButton("Recreate Bathymetry Mask");
        createMasks.setPreferredSize(createMasks.getPreferredSize());
        createMasks.setMinimumSize(createMasks.getPreferredSize());
        createMasks.setMaximumSize(createMasks.getPreferredSize());


        createMasks.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                bathymetryData.setDeleteMasks(true);
                dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(cancelButton.getPreferredSize());
        cancelButton.setMinimumSize(cancelButton.getPreferredSize());
        cancelButton.setMaximumSize(cancelButton.getPreferredSize());

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });

        JLabel filler = new JLabel("                            ");


        JPanel buttonsJPanel = new JPanel(new GridBagLayout());
        buttonsJPanel.add(cancelButton,
                new ExGridBagConstraints(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        buttonsJPanel.add(filler,
                new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        buttonsJPanel.add(createMasks,
                new ExGridBagConstraints(2, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        buttonsJPanel.add(helpButton,
                new ExGridBagConstraints(3, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));


        JLabel jLabel = new JLabel("Bathymetry has already been created for this product");

        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.add(jLabel,
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        jPanel.add(buttonsJPanel,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));


        add(jPanel);

        setModalityType(ModalityType.APPLICATION_MODAL);


        setTitle("Create Bathymetry Mask and Elevation Bands");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();


        setPreferredSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setSize(getPreferredSize());

    }


    public final void createBathymetryUI(boolean bandCreated) {


        final int rightInset = 5;

        final MaskEnabledAllBandsCheckbox maskEnabledAllBandsCheckbox = new MaskEnabledAllBandsCheckbox(bathymetryData);
        final MaskTransparencySpinner maskTransparencySpinner = new MaskTransparencySpinner(bathymetryData);
        final MaskColorComboBox maskColorComboBox = new MaskColorComboBox(bathymetryData);
        final MaskMaxDepthTextfield maskMaxDepthTextfield = new MaskMaxDepthTextfield(bathymetryData);
        final MaskMinDepthTextfield maskMinDepthTextfield = new MaskMinDepthTextfield(bathymetryData);

        final boolean[] fileSelectorEnabled = {true};

        if (bandCreated) {
            fileSelectorEnabled[0] = false;
        } else {
            fileSelectorEnabled[0] = true;
        }


        final ResolutionComboBox resolutionComboBox = new ResolutionComboBox(bathymetryData);

        JPanel resolutionSamplingPanel = new JPanel(new GridBagLayout());
        resolutionSamplingPanel.setBorder(BorderFactory.createTitledBorder(""));

        if (fileSelectorEnabled[0]) {
            resolutionSamplingPanel.add(resolutionComboBox.getjLabel(),
                    new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

            JComboBox jComboBox = resolutionComboBox.getjComboBox();
            jComboBox.setEnabled(fileSelectorEnabled[0]);

            bathymetryData.addPropertyChangeListener(BathymetryData.PROMPT_REQUEST_TO_INSTALL_FILE_EVENT, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (!bathymetryData.isInstallingFile()) {
                        SourceFileInfo sourceFileInfo = (SourceFileInfo) resolutionComboBox.getjComboBox().getSelectedItem();

                        InstallBathymetryFileDialog dialog = new InstallBathymetryFileDialog(bathymetryData, sourceFileInfo, InstallBathymetryFileDialog.Step.INSTALLATION);
                        dialog.setVisible(true);
                        dialog.setEnabled(true);
                    }
                }
            });


            resolutionSamplingPanel.add(jComboBox,
                    new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        } else {
            resolutionSamplingPanel.add(new JLabel("Note: Cannot recreate a different band, only a different mask"),
                    new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        }


        JTextField maskNameTextfield = new JTextField(bathymetryData.getMaskName());
        maskNameTextfield.setEditable(false);
        maskNameTextfield.setEnabled(false);
        maskNameTextfield.setToolTipText("Name of the mask to be created (this field is not editable)");


        JTextField bathymetryBandNameTextfield = new JTextField(BathymetryOp.BATHYMETRY_BAND_NAME);
        bathymetryBandNameTextfield.setEditable(false);
        bathymetryBandNameTextfield.setEnabled(false);
        bathymetryBandNameTextfield.setToolTipText("Name of the band to be created (this field is not editable)");


        JTextField topographyBandNameTextfield = new JTextField(BathymetryOp.TOPOGRAPHY_BAND_NAME);
        topographyBandNameTextfield.setEditable(false);
        topographyBandNameTextfield.setEnabled(false);
        topographyBandNameTextfield.setToolTipText("Name of the band to be created (this field is not editable)");

        JTextField elevationBandNameTextfield = new JTextField(BathymetryOp.ELEVATION_BAND_NAME);
        elevationBandNameTextfield.setEditable(false);
        elevationBandNameTextfield.setEnabled(false);
        elevationBandNameTextfield.setToolTipText("Name of the band to be created (this field is not editable)");


        JPanel maskJPanel = new JPanel(new GridBagLayout());
        maskJPanel.setBorder(BorderFactory.createTitledBorder("Bathymetry Mask Parameters"));

        int gridy = 0;


        maskJPanel.add(new JLabel("Mask Name"),
                new ExGridBagConstraints(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        maskJPanel.add(maskNameTextfield,
                new ExGridBagConstraints(1, gridy, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));


        gridy++;
        maskJPanel.add(maskColorComboBox.getjLabel(),
                new ExGridBagConstraints(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        maskJPanel.add(maskColorComboBox.getColorExComboBox(),
                new ExGridBagConstraints(1, gridy, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        gridy++;
        maskJPanel.add(maskTransparencySpinner.getjLabel(),
                new ExGridBagConstraints(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        maskJPanel.add(maskTransparencySpinner.getjSpinner(),
                new ExGridBagConstraints(1, gridy, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        gridy++;
        maskJPanel.add(maskMinDepthTextfield.getjLabel(),
                new ExGridBagConstraints(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        maskJPanel.add(maskMinDepthTextfield.getjTextField(),
                new ExGridBagConstraints(1, gridy, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        gridy++;
        maskJPanel.add(maskMaxDepthTextfield.getjLabel(),
                new ExGridBagConstraints(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        maskJPanel.add(maskMaxDepthTextfield.getjTextField(),
                new ExGridBagConstraints(1, gridy, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        gridy++;
        maskJPanel.add(maskEnabledAllBandsCheckbox.getjLabel(),
                new ExGridBagConstraints(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        maskJPanel.add(maskEnabledAllBandsCheckbox.getjCheckBox(),
                new ExGridBagConstraints(1, gridy, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));


        JPanel bandsJPanel = new JPanel(new GridBagLayout());
        bandsJPanel.setBorder(BorderFactory.createTitledBorder("Bands"));

        gridy = 0;
        bandsJPanel.add(new JLabel("Bathymetry Band"),
                new ExGridBagConstraints(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        bandsJPanel.add(bathymetryBandNameTextfield,
                new ExGridBagConstraints(1, gridy, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));


        gridy++;
        bandsJPanel.add(new JLabel("Topography Band"),
                new ExGridBagConstraints(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        bandsJPanel.add(topographyBandNameTextfield,
                new ExGridBagConstraints(1, gridy, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));


        gridy++;
        bandsJPanel.add(new JLabel("Elevation Band"),
                new ExGridBagConstraints(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        bandsJPanel.add(elevationBandNameTextfield,
                new ExGridBagConstraints(1, gridy, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));





        JPanel mainPanel = new JPanel(new GridBagLayout());


        // todo if we want a file selector then here it is
        // I removed it because right now there is only one file - DANNY
        //
        //        mainPanel.add(resolutionSamplingPanel,
        //                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));


        mainPanel.add(maskJPanel,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));

        mainPanel.add(bandsJPanel,
                new ExGridBagConstraints(0, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));

        String label;
        if (bandCreated) {
            label = "Recreate Bathymetry Mask";
        } else {
            label = "Create Bands and Mask";
        }
        JButton createMasks = new JButton(label);
        createMasks.setPreferredSize(createMasks.getPreferredSize());
        createMasks.setMinimumSize(createMasks.getPreferredSize());
        createMasks.setMaximumSize(createMasks.getPreferredSize());


        createMasks.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {


                SourceFileInfo sourceFileInfo = (SourceFileInfo) resolutionComboBox.getjComboBox().getSelectedItem();
                if (!sourceFileInfo.isEnabled()) {
                    bathymetryData.fireEvent(BathymetryData.PROMPT_REQUEST_TO_INSTALL_FILE_EVENT);
                } else if (!bathymetryData.isInstallingFile()) {
                    bathymetryData.setCreateMasks(true);
                    dispose();
                }


            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(cancelButton.getPreferredSize());
        cancelButton.setMinimumSize(cancelButton.getPreferredSize());
        cancelButton.setMaximumSize(cancelButton.getPreferredSize());

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });

        JLabel filler = new JLabel("                            ");


        JPanel buttonsJPanel = new JPanel(new GridBagLayout());
        buttonsJPanel.add(cancelButton,
                new ExGridBagConstraints(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        buttonsJPanel.add(filler,
                new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        buttonsJPanel.add(createMasks,
                new ExGridBagConstraints(2, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        buttonsJPanel.add(helpButton,
                new ExGridBagConstraints(3, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        createMasks.setAlignmentX(0.5f);


        mainPanel.add(buttonsJPanel,
                new ExGridBagConstraints(0, 4, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));


        add(mainPanel);

        setModalityType(ModalityType.APPLICATION_MODAL);


        setTitle("Create Bathymetry Mask & Elevation Bands");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();


        setPreferredSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setSize(getPreferredSize());
    }
}


