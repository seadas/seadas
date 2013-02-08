package gov.nasa.gsfc.seadas.watermask.ui;

import gov.nasa.gsfc.seadas.watermask.util.ResourceInstallationUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
            installationUI();
        } else if (step == Step.CONFIRMATION) {
            confirmationUI();
        }
    }

//    private static class InstallationThread
//            implements   Runnable {
//
//        public void run() {
//
//
//            ResourceInstallationUtils.installAuxdata(sourceUrl, filename);
//
//        }
//    }


    public final void installationUI() {
        JButton installButton = new JButton("Install File");
        installButton.setPreferredSize(installButton.getPreferredSize());
        installButton.setMinimumSize(installButton.getPreferredSize());
        installButton.setMaximumSize(installButton.getPreferredSize());


        installButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {


                dispose();

                //  acquire in example: "http://oceandata.sci.gsfc.nasa.gov/SeaDAS/installer/landmask/50m.zip"
                try {
                    landMasksData.fireEvent(LandMasksData.CONFIRMED_REQUEST_TO_INSTALL_FILE_EVENT);

                    String filename = sourceFileInfo.getFile().getName().toString();
                    URL sourceUrl = new URL(LandMasksData.LANDMASK_URL + "/" + filename);
   //                 ResourceInstallationUtils.installAuxdata(sourceUrl, filename);


                    FileInstallationThread fileInstallationThread = new FileInstallationThread(sourceUrl, filename);

                    fileInstallationThread.run();

                    InstallResolutionFileDialog dialog = new InstallResolutionFileDialog(landMasksData, sourceFileInfo, Step.CONFIRMATION);
                    dialog.setVisible(true);
                    dialog.setEnabled(true);

                    if (sourceFileInfo.isEnabled()) {
                        jLabel = new JLabel("File " + sourceFileInfo.getFile().getName().toString() + " has been installed");
                    } else {
                        jLabel = new JLabel("File " + sourceFileInfo.getFile().getName().toString() + " installation failure");
                    }

                    landMasksData.fireEvent(LandMasksData.FILE_INSTALLED_EVENT);
                } catch (Exception e) {
                    e.printStackTrace();
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


    public final void confirmationUI() {
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

