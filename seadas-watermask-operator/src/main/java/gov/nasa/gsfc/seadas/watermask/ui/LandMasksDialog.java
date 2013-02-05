package gov.nasa.gsfc.seadas.watermask.ui;

import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.tool.ToolButtonFactory;

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

// BEAM COMMENTS
//

//        JPanel lwcPanel = GridBagUtils.createPanel();
//        JPanel coastlinePanel = GridBagUtils.createPanel();
//        GridBagConstraints coastlineConstraints = new GridBagConstraints();
//        int rightInset = 10;
//
//        SpinnerModel transparencyModel = new SpinnerNumberModel(0.4, 0.0, 1.0, 0.1);
//        JSpinner transparencySpinner = new JSpinner(transparencyModel);
//
//        SpinnerModel samplingModel = new SpinnerNumberModel(1, 1, 10, 1);
//        JSpinner xSamplingSpinner = new JSpinner(samplingModel);
//        JSpinner ySamplingSpinner = new JSpinner(samplingModel);
//
//        Integer[] resolutions = {50, 150};
//        JComboBox resolutionComboBox = new JComboBox(resolutions);
//
//        GridBagUtils.addToPanel(coastlinePanel, new JCheckBox("Coastline"), coastlineConstraints, "anchor=WEST, gridx=0, gridy=0");
//        GridBagUtils.addToPanel(coastlinePanel, new JLabel("Mask name: "), coastlineConstraints, "gridy=1, insets.right="+ rightInset);
//        GridBagUtils.addToPanel(coastlinePanel, new JTextField("Coastline"), coastlineConstraints, "gridx=1, insets.right=0");
//        GridBagUtils.addToPanel(coastlinePanel, new JLabel("Line color: "), coastlineConstraints, "gridx=0, gridy=2, insets.right=" + rightInset);
//        GridBagUtils.addToPanel(coastlinePanel, new ColorExComboBox(), coastlineConstraints, "gridx=1, insets.right=0");
//        GridBagUtils.addToPanel(coastlinePanel, new JLabel("Transparency: "), coastlineConstraints, "gridy=2, insets.right="+ rightInset);
//        GridBagUtils.addToPanel(coastlinePanel, transparencySpinner, coastlineConstraints, "gridx=1, insets.right=0");
//        GridBagUtils.addToPanel(coastlinePanel, new JLabel("Resolution: "), coastlineConstraints, "gridy=3, insets.right="+ rightInset);
//        GridBagUtils.addToPanel(coastlinePanel, resolutionComboBox, coastlineConstraints, "gridx=1, insets.right=0");
//        GridBagUtils.addToPanel(coastlinePanel, new JLabel("Supersampling factor x: "), coastlineConstraints, "gridy=4, insets.right="+ rightInset);
//        GridBagUtils.addToPanel(coastlinePanel, xSamplingSpinner, coastlineConstraints, "gridx=1, insets.right=0");
//        GridBagUtils.addToPanel(coastlinePanel, new JLabel("Supersampling factor y: "), coastlineConstraints, "gridy=5, insets.right="+ rightInset);
//        GridBagUtils.addToPanel(coastlinePanel, ySamplingSpinner, coastlineConstraints, "gridx=1, insets.right=0");

class LandMasksDialog extends JDialog {

    public LandMasksData landMasksData = null;
    private JButton helpButton = null;
    private String HELP_ID = "coastlineLandMasks";


    public LandMasksDialog(LandMasksData landMasksData, boolean masksCreated) {
        this.landMasksData = landMasksData;

        helpButton = (JButton) getHelpButton(HELP_ID);

        if (masksCreated) {
            createNotificationUI();
        } else {
            createLandMasksUI();
        }
    }


    // HELP EXAMPLE

//        final JPanel helpPanel = new JPanel(new GridBagLayout());
//        helpPanel.add(getHelpButton("l2gen"), BorderLayout.EAST);
//
//        mainPanel.add(helpPanel,
//                new GridBagConstraintsCustom(0, 2, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0, 2));

    protected AbstractButton getHelpButton(String helpId) {
        if (helpId != null) {
            final AbstractButton helpButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/Help24.gif"),
                    false);
            helpButton.setToolTipText("Help");
            helpButton.setName("helpButton");
            HelpSys.enableHelpOnButton(helpButton, helpId);
            //  HelpSys.enableHelpKey(getParentDialogContentPane(), getHelpId());
            return helpButton;
        }

        return null;
    }


    public final void createNotificationUI() {
        JButton createMasks = new JButton("Create New Masks");
        createMasks.setPreferredSize(createMasks.getPreferredSize());
        createMasks.setMinimumSize(createMasks.getPreferredSize());
        createMasks.setMaximumSize(createMasks.getPreferredSize());


        createMasks.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                landMasksData.setDeleteMasks(true);
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


        JLabel jLabel = new JLabel("Masks have already been created for this product");

        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.add(jLabel,
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        jPanel.add(buttonsJPanel,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));


        add(jPanel);

        setModalityType(ModalityType.APPLICATION_MODAL);


        setTitle("Land Masks");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();


        setPreferredSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setSize(getPreferredSize());

    }

    public final void createLandMasksUI() {


        final int rightInset = 5;

        final CoastlineEnabledAllBandsCheckbox coastlineEnabledAllBandsCheckbox = new CoastlineEnabledAllBandsCheckbox(landMasksData);
        final WaterEnabledAllBandsCheckbox waterEnabledAllBandsCheckbox = new WaterEnabledAllBandsCheckbox(landMasksData);
        final LandEnabledAllBandsCheckbox landEnabledAllBandsCheckbox = new LandEnabledAllBandsCheckbox(landMasksData);

        final CoastlineTransparencySpinner coastlineTransparencySpinner = new CoastlineTransparencySpinner(landMasksData);
        final WaterTransparencySpinner waterTransparencySpinner = new WaterTransparencySpinner(landMasksData);
        final LandTransparencySpinner landTransparencySpinner = new LandTransparencySpinner(landMasksData);


        final CoastlineColorComboBox coastlineColorComboBox = new CoastlineColorComboBox(landMasksData);
        final WaterColorComboBox waterColorComboBox = new WaterColorComboBox(landMasksData);
        final LandColorComboBox landColorComboBox = new LandColorComboBox(landMasksData);

        final ResolutionComboBox resolutionComboBox = new ResolutionComboBox(landMasksData);
        final SuperSamplingSpinner superSamplingSpinner = new SuperSamplingSpinner(landMasksData);


        JPanel resolutionSamplingPanel = new JPanel(new GridBagLayout());
        resolutionSamplingPanel.setBorder(BorderFactory.createTitledBorder(""));

        resolutionSamplingPanel.add(resolutionComboBox.getjLabel(),
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        JComboBox jComboBox = resolutionComboBox.getjComboBox();

        landMasksData.addPropertyChangeListener(LandMasksData.FILE_INSTALLED_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                    resolutionComboBox.updateJComboBox();
            }
        });


        landMasksData.addPropertyChangeListener(LandMasksData.CONFIRMED_REQUEST_TO_INSTALL_FILE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                SourceFileInfo sourceFileInfo = (SourceFileInfo) resolutionComboBox.getjComboBox().getSelectedItem();
                resolutionComboBox.getjComboBox().setSelectedIndex(resolutionComboBox.getValidSelectedIndex());
            }
        });


        landMasksData.addPropertyChangeListener(LandMasksData.PROMPT_REQUEST_TO_INSTALL_FILE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                SourceFileInfo sourceFileInfo = (SourceFileInfo) resolutionComboBox.getjComboBox().getSelectedItem();

                InstallResolutionFileDialog dialog = new InstallResolutionFileDialog(landMasksData, sourceFileInfo, InstallResolutionFileDialog.Step.INSTALLATION);
                dialog.setVisible(true);
                dialog.setEnabled(true);
            }
        });


        resolutionSamplingPanel.add(jComboBox,
                new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        resolutionSamplingPanel.add(superSamplingSpinner.getjLabel(),
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        resolutionSamplingPanel.add(superSamplingSpinner.getjSpinner(),
                new ExGridBagConstraints(1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));


        JPanel coastlineJPanel = new JPanel(new GridBagLayout());
        coastlineJPanel.setBorder(BorderFactory.createTitledBorder(""));


        JTextField coastlineNameTextfield = new JTextField(landMasksData.getCoastlineMaskName());
        coastlineNameTextfield.setEditable(false);
        coastlineNameTextfield.setToolTipText("Name of the mask (this field is not editable)");


        coastlineJPanel.add(new JLabel("Mask Name"),
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        coastlineJPanel.add(coastlineNameTextfield,
                new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        coastlineJPanel.add(coastlineColorComboBox.getjLabel(),
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        coastlineJPanel.add(coastlineColorComboBox.getColorExComboBox(),
                new ExGridBagConstraints(1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        coastlineJPanel.add(coastlineTransparencySpinner.getjLabel(),
                new ExGridBagConstraints(0, 2, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        coastlineJPanel.add(coastlineTransparencySpinner.getjSpinner(),
                new ExGridBagConstraints(1, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        coastlineJPanel.add(coastlineEnabledAllBandsCheckbox.getjLabel(),
                new ExGridBagConstraints(0, 3, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        coastlineJPanel.add(coastlineEnabledAllBandsCheckbox.getjCheckBox(),
                new ExGridBagConstraints(1, 3, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));


        JPanel waterJPanel = new JPanel(new GridBagLayout());
        waterJPanel.setBorder(BorderFactory.createTitledBorder(""));

        JTextField waterNameTextfield = new JTextField(landMasksData.getWaterMaskName());
        waterNameTextfield.setEditable(false);
        waterNameTextfield.setToolTipText("Name of the mask (this field is not editable)");

        waterJPanel.add(new JLabel("Mask Name"),
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        waterJPanel.add(waterNameTextfield,
                new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        waterJPanel.add(waterColorComboBox.getjLabel(),
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        waterJPanel.add(waterColorComboBox.getColorExComboBox(),
                new ExGridBagConstraints(1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        waterJPanel.add(waterTransparencySpinner.getjLabel(),
                new ExGridBagConstraints(0, 2, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        waterJPanel.add(waterTransparencySpinner.getjSpinner(),
                new ExGridBagConstraints(1, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        waterJPanel.add(waterEnabledAllBandsCheckbox.getjLabel(),
                new ExGridBagConstraints(0, 3, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        waterJPanel.add(waterEnabledAllBandsCheckbox.getjCheckBox(),
                new ExGridBagConstraints(1, 3, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));


        JPanel landJPanel = new JPanel(new GridBagLayout());
        landJPanel.setBorder(BorderFactory.createTitledBorder(""));

        JTextField landNameTextfield = new JTextField(landMasksData.getLandMaskName());
        landNameTextfield.setEditable(false);
        landNameTextfield.setToolTipText("Name of the mask (this field is not editable)");

        landJPanel.add(new JLabel("Mask Name"),
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        landJPanel.add(landNameTextfield,
                new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        landJPanel.add(landColorComboBox.getjLabel(),
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        landJPanel.add(landColorComboBox.getColorExComboBox(),
                new ExGridBagConstraints(1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        landJPanel.add(landTransparencySpinner.getjLabel(),
                new ExGridBagConstraints(0, 2, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        landJPanel.add(landTransparencySpinner.getjSpinner(),
                new ExGridBagConstraints(1, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        landJPanel.add(landEnabledAllBandsCheckbox.getjLabel(),
                new ExGridBagConstraints(0, 3, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        landJPanel.add(landEnabledAllBandsCheckbox.getjCheckBox(),
                new ExGridBagConstraints(1, 3, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));


        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(resolutionSamplingPanel,
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        mainPanel.add(coastlineJPanel,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        mainPanel.add(landJPanel,
                new ExGridBagConstraints(0, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        mainPanel.add(waterJPanel,
                new ExGridBagConstraints(0, 3, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));


        JButton createMasks = new JButton("Create Masks");
        createMasks.setPreferredSize(createMasks.getPreferredSize());
        createMasks.setMinimumSize(createMasks.getPreferredSize());
        createMasks.setMaximumSize(createMasks.getPreferredSize());


        createMasks.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                landMasksData.setCreateMasks(true);
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

        createMasks.setAlignmentX(0.5f);


        mainPanel.add(buttonsJPanel,
                new ExGridBagConstraints(0, 4, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));


        add(mainPanel);

        setModalityType(ModalityType.APPLICATION_MODAL);


        setTitle("Coastline and Land Masks");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();


        setPreferredSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setSize(getPreferredSize());
    }
}


