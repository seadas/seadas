package gov.nasa.gsfc.seadas.watermask.ui;

import gov.nasa.gsfc.seadas.watermask.operator.WatermaskClassifier;
import gov.nasa.gsfc.seadas.watermask.util.ResourceInstallationUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 1/16/13
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */


class InstallResolutionFileDialog extends JDialog {

    public static enum Step {
        INSTALLATION,
        CONFIRMATION
    }

    public SourceFileInfo sourceFileInfo;
    private JLabel jLabel;
    private LandMasksData landMasksData;


    public InstallResolutionFileDialog(LandMasksData landMasksData, SourceFileInfo sourceFileInfo, Step step) {
        this.landMasksData = landMasksData;
        this.sourceFileInfo = sourceFileInfo;


        if (step == Step.INSTALLATION) {
            installationRequestUI();
        } else if (step == Step.CONFIRMATION) {
            installationResultsUI();
        }
    }


    public final void installationRequestUI() {
        JButton installButton = new JButton("Install File");
        installButton.setPreferredSize(installButton.getPreferredSize());
        installButton.setMinimumSize(installButton.getPreferredSize());
        installButton.setMaximumSize(installButton.getPreferredSize());


        installButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {


                dispose();

                //  acquire in example: "https://oceandata.sci.gsfc.nasa.gov/SeaDAS/installer/landmask/50m.zip"
                try {
                    landMasksData.fireEvent(LandMasksData.CONFIRMED_REQUEST_TO_INSTALL_FILE_EVENT);

                    final String filename = sourceFileInfo.getFile().getName().toString();

                    final URL sourceUrl = new URL(LandMasksData.LANDMASK_URL + "/" + filename);

                    File targetFile = ResourceInstallationUtils.getTargetFile(filename);

                    if (!targetFile.exists()) {
                        Thread t = new Thread(new FileInstallRunnable(sourceUrl, filename, sourceFileInfo, landMasksData));
                        t.start();
                    }


//                    if (sourceFileInfo.getMode() == WatermaskClassifier.Mode.SRTM_GC) {
//                        File gcFile = ResourceInstallationUtils.getTargetFile(WatermaskClassifier.GC_WATER_MASK_FILE);
//
//                        if (!gcFile.exists()) {
//                            final URL northSourceUrl = new URL(LandMasksData.LANDMASK_URL + "/" + gcFile.getName());
//
//                            Thread t2 = new Thread(new FileInstallRunnable(northSourceUrl, gcFile.getName(), sourceFileInfo, landMasksData));
//                            t2.start();
//                        }
//                    }


//                    File targetDir = ResourceInstallationUtils.getTargetDir();
//                    ProcessBuilder pb = new ProcessBuilder("wget.py", sourceUrl.toString(), targetDir.getAbsolutePath());
//                    pb.start();


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

//        this.addPropertyChangeListener(LandMasksData.FILE_INSTALLED_EVENT, new PropertyChangeListener() {
//            @Override
//            public void propertyChange(PropertyChangeEvent evt) {
//                SourceFileInfo sourceFileInfo = (SourceFileInfo) evt.getNewValue();
//
//                InstallResolutionFileDialog dialog = new InstallResolutionFileDialog(this, sourceFileInfo, InstallResolutionFileDialog.Step.CONFIRMATION);
//                dialog.setVisible(true);
//                dialog.setEnabled(true);
//
//                if (sourceFileInfo.isEnabled()) {
//                    jLabel = new JLabel("File " + sourceFileInfo.getFile().getName().toString() + " has been installed");
//                    landMasksData.fireEvent(LandMasksData.FILE_INSTALLED_EVENT2);
//                } else {
//                    jLabel = new JLabel("File " + sourceFileInfo.getFile().getName().toString() + " installation failure");
//                }
//
//                landMasksData.removePropertyChangeListener(LandMasksData.FILE_INSTALLED_EVENT, this);
//            }
//        });
//
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
        buttonsJPanel.add(installButton,
                new ExGridBagConstraints(2, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
//        buttonsJPanel.add(helpButton,
//                new ExGridBagConstraints(3, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));


        jLabel = new JLabel("Do you want to install file " + sourceFileInfo.getFile().getName().toString() + " ?");

        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.add(jLabel,
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        jPanel.add(buttonsJPanel,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));


        add(jPanel);

        setModalityType(ModalityType.APPLICATION_MODAL);


        setTitle("File Installation");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();


        setPreferredSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setSize(getPreferredSize());

    }


    public final void installationResultsUI() {
        JButton okayButton = new JButton("Okay");
        okayButton.setPreferredSize(okayButton.getPreferredSize());
        okayButton.setMinimumSize(okayButton.getPreferredSize());
        okayButton.setMaximumSize(okayButton.getPreferredSize());


        okayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });


        if (sourceFileInfo.isEnabled()) {
            jLabel = new JLabel("File " + sourceFileInfo.getFile().getName().toString() + " has been installed");
            landMasksData.fireEvent(LandMasksData.FILE_INSTALLED_EVENT2);
        } else {
            jLabel = new JLabel("File " + sourceFileInfo.getFile().getName().toString() + " installation failure");
        }

        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.add(jLabel,
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        jPanel.add(okayButton,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));


        add(jPanel);

        setModalityType(ModalityType.APPLICATION_MODAL);


        setTitle("File Installation");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();


        setPreferredSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setSize(getPreferredSize());

    }
}

