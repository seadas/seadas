package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genWavelengthInfo;
import gov.nasa.gsfc.seadas.processing.common.GridBagConstraintsCustom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/11/12
 * Time: 3:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genWavelengthLimiterPanel extends JPanel {

    private L2genData l2genData;

    private JPanel waveLimiterJPanel;

    private ArrayList<JCheckBox> wavelengthsJCheckboxArrayList = new ArrayList<JCheckBox>();

    private boolean waveLimiterControlHandlersEnabled = false;

    private JButton
            infraredButton,
            visibleButton,
            nearInfraredButton;

    private final static String
            SELECT_ALL_INFRARED = "Select All Infrared",
            DESELECT_ALL_INFRARED = "Deselect All Infrared",
            SELECT_ALL_NEAR_INFRARED = "Select All Near-Infrared",
            DESELECT_ALL_NEAR_INFRARED = "Deselect All Near-Infrared",
            SELECT_ALL_VISIBLE = "Select All Visible",
            DESELECT_ALL_VISIBLE = "Deselect All Visible";

    //  private InfraredButton infraredButton = new InfraredButton();


    L2genWavelengthLimiterPanel(L2genData l2genData) {
        this.l2genData = l2genData;
        initComponents();
        addComponents();

    }

    public void initComponents() {
        waveLimiterJPanel = createWaveLimiterJPanel();
        infraredButton = createInfraredButton();
        nearInfraredButton = createNearInfraredButton();
        visibleButton = createVisibleButton();
    }

    public void addComponents() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Wavelength Limiter"));
        setToolTipText("The wavelengths selected here are applied when you check a wavelength dependent product.  Not that any subsequent change ...");

        JPanel innerPanel = new JPanel(new GridBagLayout());

        innerPanel.add(visibleButton,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

        innerPanel.add(nearInfraredButton,
                new GridBagConstraintsCustom(0, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

        innerPanel.add(infraredButton,
                new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

        innerPanel.add(waveLimiterJPanel,
                new GridBagConstraintsCustom(0, 3, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

                innerPanel.add(new JPanel(),
                new GridBagConstraintsCustom(0, 4, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH));

        JScrollPane innerScroll = new JScrollPane(innerPanel);
        innerScroll.setBorder(null);

        add(innerScroll,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH));
    }


    private JButton createInfraredButton() {

        final JButton jButton = new JButton(SELECT_ALL_INFRARED);

        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jButton.getText().equals(SELECT_ALL_INFRARED)) {
                    l2genData.setSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.INFRARED, true);
                } else if (jButton.getText().equals(DESELECT_ALL_INFRARED)) {
                    l2genData.setSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.INFRARED, false);
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.WAVE_LIMITER, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateInfraredButton();
            }
        });

        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateInfraredButton();
            }
        });

        return jButton;
    }


    private void updateInfraredButton() {

        // Set INFRARED 'Select All' toggle to appropriate text and enabled
        if (l2genData.hasWaveType(L2genWavelengthInfo.WaveType.INFRARED)) {
            nearInfraredButton.setEnabled(true);
            if (l2genData.isSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.INFRARED)) {
                if (!infraredButton.getText().equals(DESELECT_ALL_INFRARED)) {
                    infraredButton.setText(DESELECT_ALL_INFRARED);
                }
            } else {
                if (!infraredButton.getText().equals(SELECT_ALL_INFRARED)) {
                    infraredButton.setText(SELECT_ALL_INFRARED);
                }
            }
        } else {
            nearInfraredButton.setEnabled(false);
        }
    }

    private class InfraredButton {


        private static final String selectAll = SELECT_ALL_INFRARED;
        private static final String deselectAll = DESELECT_ALL_INFRARED;


        InfraredButton() {
        }

        private JButton createInfraredButton() {

            final JButton jButton = new JButton(selectAll);

            jButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (jButton.getText().equals(selectAll)) {
                        l2genData.setSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.INFRARED, true);
                    } else if (jButton.getText().equals(deselectAll)) {
                        l2genData.setSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.INFRARED, false);
                    }
                }
            });


            l2genData.addPropertyChangeListener(L2genData.WAVE_LIMITER, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    updateInfraredButton();
                }
            });

            l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    updateInfraredButton();
                }
            });

            return jButton;
        }


        private void updateInfraredButton() {

            // Set INFRARED 'Select All' toggle to appropriate text and enabled
            if (l2genData.hasWaveType(L2genWavelengthInfo.WaveType.INFRARED)) {
                nearInfraredButton.setEnabled(true);
                if (l2genData.isSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.INFRARED)) {
                    if (!infraredButton.getText().equals(deselectAll)) {
                        infraredButton.setText(deselectAll);
                    }
                } else {
                    if (!infraredButton.getText().equals(selectAll)) {
                        infraredButton.setText(selectAll);
                    }
                }
            } else {
                nearInfraredButton.setEnabled(false);
            }
        }
    }


    private JButton createNearInfraredButton() {

        final JButton jButton = new JButton(SELECT_ALL_NEAR_INFRARED);

        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jButton.getText().equals(SELECT_ALL_NEAR_INFRARED)) {
                    l2genData.setSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.NEAR_INFRARED, true);
                } else if (jButton.getText().equals(DESELECT_ALL_NEAR_INFRARED)) {
                    l2genData.setSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.NEAR_INFRARED, false);
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.WAVE_LIMITER, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateNearInfraredButton();
            }
        });

        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateNearInfraredButton();
            }
        });

        return jButton;
    }


    private void updateNearInfraredButton() {
        // Set NEAR_INFRARED 'Select All' toggle to appropriate text and enabled
        if (l2genData.hasWaveType(L2genWavelengthInfo.WaveType.NEAR_INFRARED)) {
            nearInfraredButton.setEnabled(true);
            if (l2genData.isSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.NEAR_INFRARED)) {
                if (!nearInfraredButton.getText().equals(DESELECT_ALL_NEAR_INFRARED)) {
                    nearInfraredButton.setText(DESELECT_ALL_NEAR_INFRARED);
                }
            } else {
                if (!nearInfraredButton.getText().equals(SELECT_ALL_NEAR_INFRARED)) {
                    nearInfraredButton.setText(SELECT_ALL_NEAR_INFRARED);
                }
            }
        } else {
            nearInfraredButton.setEnabled(true);
        }
    }


    private JButton createVisibleButton() {

        final JButton jButton = new JButton(SELECT_ALL_VISIBLE);

        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jButton.getText().equals(SELECT_ALL_VISIBLE)) {
                    l2genData.setSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.VISIBLE, true);
                } else if (jButton.getText().equals(DESELECT_ALL_VISIBLE)) {
                    l2genData.setSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.VISIBLE, false);
                }
            }
        });

        l2genData.addPropertyChangeListener(L2genData.WAVE_LIMITER, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateVisibleButton();
            }
        });

        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateVisibleButton();
            }
        });

        return jButton;
    }


    private void updateVisibleButton() {

        // Set VISIBLE 'Select All' toggle to appropriate text and enabled
        if (l2genData.hasWaveType(L2genWavelengthInfo.WaveType.VISIBLE)) {
            visibleButton.setEnabled(true);
            if (l2genData.isSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.VISIBLE)) {
                if (!visibleButton.getText().equals(DESELECT_ALL_VISIBLE)) {
                    visibleButton.setText(DESELECT_ALL_VISIBLE);
                }
            } else {
                if (!visibleButton.getText().equals(SELECT_ALL_VISIBLE)) {
                    visibleButton.setText(SELECT_ALL_VISIBLE);
                }
            }
        } else {
            visibleButton.setEnabled(false);
        }
    }


    private JPanel createWaveLimiterJPanel() {

        final JPanel jPanel = new JPanel(new GridBagLayout());

        l2genData.addPropertyChangeListener(L2genData.WAVE_LIMITER, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateWaveLimiterSelectionStates();
            }
        });

        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateWavelengthLimiterPanel();
                updateWaveLimiterSelectionStates();
            }
        });

        return jPanel;
    }


    private void updateWavelengthLimiterPanel() {

        waveLimiterJPanel.removeAll();

        // clear this because we dynamically rebuild it when input file selection is made or changed
        wavelengthsJCheckboxArrayList.clear();

        ArrayList<JCheckBox> wavelengthGroupCheckboxes = new ArrayList<JCheckBox>();

        for (L2genWavelengthInfo waveLimiterInfo : l2genData.getWaveLimiterInfos()) {

            final String currWavelength = waveLimiterInfo.getWavelengthString();
            final JCheckBox currJCheckBox = new JCheckBox(currWavelength);

            currJCheckBox.setName(currWavelength);

            // add current JCheckBox to the externally accessible arrayList
            wavelengthsJCheckboxArrayList.add(currJCheckBox);

            // add listener for current checkbox
            currJCheckBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (waveLimiterControlHandlersEnabled) {
                        l2genData.setSelectedWaveLimiter(currWavelength, currJCheckBox.isSelected());
                    }
                }
            });

            wavelengthGroupCheckboxes.add(currJCheckBox);
        }


        if (l2genData.hasWaveType(L2genWavelengthInfo.WaveType.INFRARED)) {
            l2genData.setSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.INFRARED, true);
        }

        if (l2genData.hasWaveType(L2genWavelengthInfo.WaveType.VISIBLE)) {
            l2genData.setSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.VISIBLE, true);
        }

        if (l2genData.hasWaveType(L2genWavelengthInfo.WaveType.NEAR_INFRARED)) {
            l2genData.setSelectedAllWaveLimiter(L2genWavelengthInfo.WaveType.NEAR_INFRARED, true);
        }


        // some GridBagLayout formatting variables
        int gridyCnt = 0;
        int gridxCnt = 0;
        int NUMBER_OF_COLUMNS = 2;


        for (JCheckBox wavelengthGroupCheckbox : wavelengthGroupCheckboxes) {
            // add current JCheckBox to the panel

            waveLimiterJPanel.add(wavelengthGroupCheckbox,
                    new GridBagConstraintsCustom(gridxCnt, gridyCnt, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));


            // increment GridBag coordinates
            if (gridxCnt < (NUMBER_OF_COLUMNS - 1)) {
                gridxCnt++;
            } else {
                gridxCnt = 0;
                gridyCnt++;
            }
        }

        // just in case
        l2genData.fireEvent(l2genData.WAVE_LIMITER);
        // updateWaveLimiterSelectionStates();
    }


    /**
     * Set all waveLimiterInfos controls to agree with l2genData
     */
    private void updateWaveLimiterSelectionStates() {

        // Turn off control handlers until all controls are set
        waveLimiterControlHandlersEnabled = false;

        // Set all checkboxes to agree with l2genData
        for (L2genWavelengthInfo waveLimiterInfo : l2genData.getWaveLimiterInfos()) {
            for (JCheckBox currJCheckbox : wavelengthsJCheckboxArrayList) {
                if (waveLimiterInfo.getWavelengthString().equals(currJCheckbox.getName())) {
                    if (waveLimiterInfo.isSelected() != currJCheckbox.isSelected()) {
                        currJCheckbox.setSelected(waveLimiterInfo.isSelected());
                    }
                }
            }
        }


        // Turn on control handlers now that all controls are set
        waveLimiterControlHandlersEnabled = true;
    }


}
