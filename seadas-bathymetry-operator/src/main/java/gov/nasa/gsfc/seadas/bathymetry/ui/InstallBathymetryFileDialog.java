package gov.nasa.gsfc.seadas.bathymetry.ui;

import gov.nasa.gsfc.seadas.bathymetry.util.ResourceInstallationUtils;

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


class InstallBathymetryFileDialog extends JDialog {

    public static enum Step {
        INSTALLATION,
        CONFIRMATION
    }

    public SourceFileInfo sourceFileInfo;
    private JLabel jLabel;
    private BathymetryData bathymetryData;


    public InstallBathymetryFileDialog(BathymetryData bathymetryData, SourceFileInfo sourceFileInfo, Step step) {
        this.bathymetryData = bathymetryData;
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
                    bathymetryData.setInstallingFile(true);
                    bathymetryData.fireEvent(BathymetryData.CONFIRMED_REQUEST_TO_INSTALL_FILE_EVENT);

                    if (sourceFileInfo.getAltFile() != null) {
                        final String filename = sourceFileInfo.getAltFile().getName().toString();

                        final URL sourceUrl = new URL(BathymetryData.LANDMASK_URL + "/" + filename);

                        File targetFile = ResourceInstallationUtils.getTargetFile(filename);

                        if (!targetFile.exists()) {
                            Thread t = new Thread(new FileInstallRunnable(sourceUrl, filename, sourceFileInfo, bathymetryData));
                            t.start();
                        }
                    }


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


        jLabel = new JLabel("Do you want to install file " + sourceFileInfo.getAltFile().getName().toString() + " ?");

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
            jLabel = new JLabel("File " + sourceFileInfo.getAltFile().getName().toString() + " has been installed");
            bathymetryData.fireEvent(BathymetryData.FILE_INSTALLED_EVENT2);
        } else {
            jLabel = new JLabel("File " + sourceFileInfo.getAltFile().getName().toString() + " installation failure");
        }

        bathymetryData.setInstallingFile(false);

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

