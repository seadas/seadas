package gov.nasa.gsfc.seadas.processing.l2gen;

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

    L2genData l2genData;
    JPanel waveLimiterJPanel;
    private ArrayList<JCheckBox> wavelengthsJCheckboxArrayList = null;
    boolean waveLimiterControlHandlersEnabled = false;

    private String WAVE_LIMITER_SELECT_ALL_INFRARED = "Select All Infrared";
    private String WAVE_LIMITER_DESELECT_ALL_INFRARED = "Deselect All Infrared";
    private String WAVE_LIMITER_SELECT_ALL_NEAR_INFRARED = "Select All Near-Infrared";
    private String WAVE_LIMITER_DESELECT_ALL_NEAR_INFRARED = "Deselect All Near-Infrared";
    private String WAVE_LIMITER_SELECT_ALL_VISIBLE = "Select All Visible";
    private String WAVE_LIMITER_DESELECT_ALL_VISIBLE = "Deselect All Visible";

    private JButton waveLimiterSelectAllInfrared;
    private JButton waveLimiterSelectAllVisible;
    private JButton waveLimiterSelectAllNearInfrared;


    L2genWavelengthLimiterPanel(L2genData l2genData) {

        this.l2genData = l2genData;

        initComponents();
        addComponents();
    }

    public void initComponents() {
        waveLimiterJPanel = createWaveLimiterJPanel();

        l2genData.addPropertyChangeListener(L2genData.WAVE_LIMITER_CHANGE_EVENT, new PropertyChangeListener() {
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
    }

    public void addComponents() {

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Wavelength Limiter"));
        setToolTipText("The wavelengths selected here are applied when you check a wavelength dependent product.  Not that any subsequent change ...");

        add(waveLimiterSelectAllVisible,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

        add(waveLimiterSelectAllNearInfrared,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

        add(waveLimiterSelectAllInfrared,
                new GridBagConstraintsCustom(0, 2, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

        add(waveLimiterJPanel,
                new GridBagConstraintsCustom(0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH));
    }


    private JPanel createWaveLimiterJPanel() {

        // ----------------------------------------------------------------------------------------
        // Create all Swing controls used on this tabbed panel
        // ----------------------------------------------------------------------------------------

        waveLimiterSelectAllInfrared = new JButton(WAVE_LIMITER_SELECT_ALL_INFRARED);

        waveLimiterSelectAllInfrared.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_SELECT_ALL_INFRARED)) {
                    l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.INFRARED, true);
                } else if (waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_DESELECT_ALL_INFRARED)) {
                    l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.INFRARED, false);
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.WAVE_LIMITER_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // Set INFRARED 'Select All' toggle to appropriate text
                if (l2genData.hasWaveType(WavelengthInfo.WaveType.INFRARED)) {
                    if (l2genData.isSelectedAllWaveLimiter(WavelengthInfo.WaveType.INFRARED)) {
                        if (!waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_DESELECT_ALL_INFRARED)) {
                            waveLimiterSelectAllInfrared.setText(WAVE_LIMITER_DESELECT_ALL_INFRARED);
                        }
                    } else {
                        if (!waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_SELECT_ALL_INFRARED)) {
                            waveLimiterSelectAllInfrared.setText(WAVE_LIMITER_SELECT_ALL_INFRARED);
                        }
                    }
                }
            }
        });


        waveLimiterSelectAllNearInfrared = new JButton(WAVE_LIMITER_SELECT_ALL_NEAR_INFRARED);

        waveLimiterSelectAllNearInfrared.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (waveLimiterSelectAllNearInfrared.getText().equals(WAVE_LIMITER_SELECT_ALL_NEAR_INFRARED)) {
                    l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.NEAR_INFRARED, true);
                } else if (waveLimiterSelectAllNearInfrared.getText().equals(WAVE_LIMITER_DESELECT_ALL_NEAR_INFRARED)) {
                    l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.NEAR_INFRARED, false);
                }
            }
        });

        waveLimiterSelectAllVisible = new JButton(WAVE_LIMITER_SELECT_ALL_VISIBLE);

        waveLimiterSelectAllVisible.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (waveLimiterSelectAllVisible.getText().equals(WAVE_LIMITER_SELECT_ALL_VISIBLE)) {
                    l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.VISIBLE, true);
                } else if (waveLimiterSelectAllVisible.getText().equals(WAVE_LIMITER_DESELECT_ALL_VISIBLE)) {
                    l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.VISIBLE, false);
                }
            }
        });


        waveLimiterJPanel = new JPanel(new GridBagLayout());


        return waveLimiterJPanel;

    }


    private void updateWavelengthLimiterPanel() {

        wavelengthsJCheckboxArrayList = new ArrayList<JCheckBox>();

        waveLimiterJPanel.removeAll();

        // clear this because we dynamically rebuild it when input file selection is made or changed
        wavelengthsJCheckboxArrayList.clear();

        ArrayList<JCheckBox> wavelengthGroupCheckboxes = new ArrayList<JCheckBox>();

        for (WavelengthInfo waveLimiterInfo : l2genData.getWaveLimiterInfos()) {

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

        waveLimiterSelectAllInfrared.setText(WAVE_LIMITER_SELECT_ALL_INFRARED);
        waveLimiterSelectAllNearInfrared.setText(WAVE_LIMITER_SELECT_ALL_NEAR_INFRARED);
        waveLimiterSelectAllVisible.setText(WAVE_LIMITER_SELECT_ALL_VISIBLE);

        if (l2genData.hasWaveType(WavelengthInfo.WaveType.INFRARED)) {
            waveLimiterSelectAllInfrared.setEnabled(true);
            l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.INFRARED, true);
        } else {
            waveLimiterSelectAllInfrared.setEnabled(false);
        }

        if (l2genData.hasWaveType(WavelengthInfo.WaveType.VISIBLE)) {
            waveLimiterSelectAllVisible.setEnabled(true);

            l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.VISIBLE, true);
        } else {
            waveLimiterSelectAllVisible.setEnabled(false);
        }

        if (l2genData.hasWaveType(WavelengthInfo.WaveType.NEAR_INFRARED)) {
            waveLimiterSelectAllNearInfrared.setEnabled(true);

            l2genData.setSelectedAllWaveLimiter(WavelengthInfo.WaveType.NEAR_INFRARED, true);
        } else {
            waveLimiterSelectAllNearInfrared.setEnabled(false);
        }


        // some GridBagLayout formatting variables
        int gridyCnt = 0;
        int gridxCnt = 0;
        int NUMBER_OF_COLUMNS = 1;


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

        // updateWaveLimiterSelectionStates();
    }


    /**
     * Set all waveLimiterInfos controls to agree with l2genData
     */
    private void updateWaveLimiterSelectionStates() {

        // Turn off control handlers until all controls are set
        waveLimiterControlHandlersEnabled = false;

        // Set all checkboxes to agree with l2genData
        for (WavelengthInfo waveLimiterInfo : l2genData.getWaveLimiterInfos()) {
            for (JCheckBox currJCheckbox : wavelengthsJCheckboxArrayList) {
                if (waveLimiterInfo.getWavelengthString().equals(currJCheckbox.getName())) {
                    if (waveLimiterInfo.isSelected() != currJCheckbox.isSelected()) {
                        currJCheckbox.setSelected(waveLimiterInfo.isSelected());
                    }
                }
            }
        }

        // Set INFRARED 'Select All' toggle to appropriate text
        if (l2genData.hasWaveType(WavelengthInfo.WaveType.INFRARED)) {
            if (l2genData.isSelectedAllWaveLimiter(WavelengthInfo.WaveType.INFRARED)) {
                if (!waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_DESELECT_ALL_INFRARED)) {
                    waveLimiterSelectAllInfrared.setText(WAVE_LIMITER_DESELECT_ALL_INFRARED);
                }
            } else {
                if (!waveLimiterSelectAllInfrared.getText().equals(WAVE_LIMITER_SELECT_ALL_INFRARED)) {
                    waveLimiterSelectAllInfrared.setText(WAVE_LIMITER_SELECT_ALL_INFRARED);
                }
            }
        }

        // Set NEAR_INFRARED 'Select All' toggle to appropriate text
        if (l2genData.hasWaveType(WavelengthInfo.WaveType.NEAR_INFRARED)) {
            if (l2genData.isSelectedAllWaveLimiter(WavelengthInfo.WaveType.NEAR_INFRARED)) {
                if (!waveLimiterSelectAllNearInfrared.getText().equals(WAVE_LIMITER_DESELECT_ALL_NEAR_INFRARED)) {
                    waveLimiterSelectAllNearInfrared.setText(WAVE_LIMITER_DESELECT_ALL_NEAR_INFRARED);
                }
            } else {
                if (!waveLimiterSelectAllNearInfrared.getText().equals(WAVE_LIMITER_SELECT_ALL_NEAR_INFRARED)) {
                    waveLimiterSelectAllNearInfrared.setText(WAVE_LIMITER_SELECT_ALL_NEAR_INFRARED);
                }
            }
        }


        // Set VISIBLE 'Select All' toggle to appropriate text
        if (l2genData.hasWaveType(WavelengthInfo.WaveType.VISIBLE)) {
            if (l2genData.isSelectedAllWaveLimiter(WavelengthInfo.WaveType.VISIBLE)) {
                if (!waveLimiterSelectAllVisible.getText().equals(WAVE_LIMITER_DESELECT_ALL_VISIBLE)) {
                    waveLimiterSelectAllVisible.setText(WAVE_LIMITER_DESELECT_ALL_VISIBLE);
                }
            } else {
                if (!waveLimiterSelectAllVisible.getText().equals(WAVE_LIMITER_SELECT_ALL_VISIBLE)) {
                    waveLimiterSelectAllVisible.setText(WAVE_LIMITER_SELECT_ALL_VISIBLE);
                }
            }
        }


        // Turn on control handlers now that all controls are set
        waveLimiterControlHandlersEnabled = true;
    }


}
