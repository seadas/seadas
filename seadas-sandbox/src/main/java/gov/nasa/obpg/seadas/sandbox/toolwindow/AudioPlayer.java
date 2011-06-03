package gov.nasa.obpg.seadas.sandbox.toolwindow;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.text.*;
import java.util.*;

public class AudioPlayer implements ChangeListener {

    JLabel jlabBass;
    JLabel jlabMidrange;
    JLabel jlabTreble;
    JLabel jlabBalance;
    JLabel jlabVolume;
    JLabel jlabInfo;

    JSlider jsldrBass;
    JSlider jsldrMidrange;
    JSlider jsldrTreble;
    JSlider jsldrBalance;
    JSlider jsldrVolume;
    JSlider jsldrInfo;

    JRadioButton jrbPreset1;
    JRadioButton jrbPreset2;
    JRadioButton jrbDefaults;

    JButton jbtnStore;

    DecimalFormat df;

    Presets[] presets;
    private JPanel contentPane;

    AudioPlayer() {


        df = new DecimalFormat("+#;-#");

        setupPresets();

        setupSliders();

        setupLabels();

        setupRButtons();

        jbtnStore = new JButton("Store Settings");

        jsldrBass.addChangeListener(this);
        jsldrMidrange.addChangeListener(this);
        jsldrTreble.addChangeListener(this);
        jsldrBalance.addChangeListener(this);
        jsldrVolume.addChangeListener(this);

        jbtnStore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (jrbPreset1.isSelected())
                    storePreset(presets[1]);
                else if (jrbPreset2.isSelected())
                    storePreset(presets[2]);
            }
        });

        jrbDefaults.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                loadPreset(presets[0]);
            }
        });

        jrbPreset1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                loadPreset(presets[1]);
            }
        });

        jrbPreset2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                loadPreset(presets[2]);
            }
        });


        contentPane = new JPanel();
        contentPane.setLayout(new FlowLayout());

        contentPane.add(jlabBass);
        contentPane.add(jsldrBass);
        contentPane.add(jlabMidrange);
        contentPane.add(jsldrMidrange);
        contentPane.add(jlabTreble);
        contentPane.add(jsldrTreble);
        contentPane.add(jlabBalance);
        contentPane.add(jsldrBalance);
        contentPane.add(jlabVolume);
        contentPane.add(jsldrVolume);
        contentPane.add(jrbDefaults);
        contentPane.add(jrbPreset1);
        contentPane.add(jrbPreset2);
        contentPane.add(jbtnStore);
        contentPane.add(new JLabel(""));
        contentPane.add(jlabInfo);

    }

    public void stateChanged(ChangeEvent ce) {
        showSettings();
    }

    void setupSliders() {
        jsldrBass = new JSlider(-10, 10);
        jsldrMidrange = new JSlider(-10, 10);
        jsldrTreble = new JSlider(-10, 10);
        jsldrVolume = new JSlider(0, 10, 0);
        jsldrBalance = new JSlider(-5, 5);

        jsldrBass.setMajorTickSpacing(2);
        jsldrMidrange.setMajorTickSpacing(2);
        jsldrTreble.setMajorTickSpacing(2);
        jsldrVolume.setMajorTickSpacing(1);
        jsldrBalance.setMajorTickSpacing(1);

        jsldrBass.setMinorTickSpacing(1);
        jsldrMidrange.setMinorTickSpacing(1);
        jsldrTreble.setMinorTickSpacing(1);

        Hashtable table = new Hashtable();
        for (int i = -10; i <= 0; i += 2)
            table.put(new Integer(i), new JLabel("" + i));
        for (int i = 2; i <= 10; i += 2)
            table.put(new Integer(i), new JLabel("+" + i));
        jsldrBass.setLabelTable(table);
        jsldrMidrange.setLabelTable(table);
        jsldrTreble.setLabelTable(table);

        table = new Hashtable();
        table.put(new Integer(0), new JLabel("Center"));
        table.put(new Integer(-5), new JLabel("L"));
        table.put(new Integer(5), new JLabel("R"));
        jsldrBalance.setLabelTable(table);

        jsldrVolume.setLabelTable(jsldrVolume.createStandardLabels(1));

        jsldrBass.setPaintTicks(true);
        jsldrMidrange.setPaintTicks(true);
        jsldrTreble.setPaintTicks(true);
        jsldrVolume.setPaintTicks(true);
        jsldrBalance.setPaintTicks(true);

        jsldrBass.setPaintLabels(true);
        jsldrMidrange.setPaintLabels(true);
        jsldrTreble.setPaintLabels(true);
        jsldrVolume.setPaintLabels(true);
        jsldrBalance.setPaintLabels(true);

        jsldrBass.setSnapToTicks(true);
        jsldrMidrange.setSnapToTicks(true);
        jsldrTreble.setSnapToTicks(true);
        jsldrVolume.setSnapToTicks(true);
        jsldrBalance.setSnapToTicks(true);

        Dimension sldrSize = new Dimension(240, 60);
        jsldrBass.setPreferredSize(sldrSize);
        jsldrMidrange.setPreferredSize(sldrSize);
        jsldrTreble.setPreferredSize(sldrSize);
        jsldrVolume.setPreferredSize(sldrSize);
        jsldrBalance.setPreferredSize(sldrSize);

    }

    void setupLabels() {
        jlabBass = new JLabel("Bass");
        jlabMidrange = new JLabel("Midrange");
        jlabTreble = new JLabel("Treble");
        jlabVolume = new JLabel("Volume");
        jlabBalance = new JLabel("Balance");

        Dimension labSize = new Dimension(60, 25);
        jlabBass.setPreferredSize(labSize);
        jlabMidrange.setPreferredSize(labSize);
        jlabTreble.setPreferredSize(labSize);
        jlabVolume.setPreferredSize(labSize);
        jlabBalance.setPreferredSize(labSize);

        jlabInfo = new JLabel("");
        jlabInfo.setPreferredSize(new Dimension(110, 100));

        showSettings();
    }

    void setupRButtons() {
        jrbDefaults = new JRadioButton("Defaults");
        jrbPreset1 = new JRadioButton("Preset 1");
        jrbPreset2 = new JRadioButton("Preset 2");

        ButtonGroup bg = new ButtonGroup();
        bg.add(jrbDefaults);
        bg.add(jrbPreset1);
        bg.add(jrbPreset2);

        jrbDefaults.setSelected(true);
    }

    void showSettings() {
        String bal;

        int b = jsldrBalance.getValue();
        if (b > 0)
            bal = "Right " + df.format(jsldrBalance.getValue());
        else if (b == 0)
            bal = "Center";
        else
            bal = "Left " + df.format(-jsldrBalance.getValue());

        jlabInfo.setText("<html>Treble: " +
                df.format(jsldrTreble.getValue()) +
                "<br>Midrange: " +
                df.format(jsldrMidrange.getValue()) +
                "<br>Bass: " +
                df.format(jsldrBass.getValue()) +
                "<br>Balance: " + bal +
                "<br>Volume: " +
                jsldrVolume.getValue());
    }

    void setupPresets() {
        presets = new Presets[3];
        presets[0] = new Presets(0, 0, 0, 0, 0);
        presets[1] = new Presets(2, -4, 7, 0, 4);
        presets[2] = new Presets(3, 3, -2, 1, 7);
    }

    void storePreset(Presets info) {
        info.bass = jsldrBass.getValue();
        info.midrange = jsldrMidrange.getValue();
        info.treble = jsldrTreble.getValue();
        info.balance = jsldrBalance.getValue();
        info.volume = jsldrVolume.getValue();
    }

    void loadPreset(Presets info) {
        jsldrBass.setValue(info.bass);
        jsldrMidrange.setValue(info.midrange);
        jsldrTreble.setValue(info.treble);
        jsldrBalance.setValue(info.balance);
        jsldrVolume.setValue(info.volume);
    }

    public static void main(String args[]) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                AudioPlayer audioPlayer = new AudioPlayer();
                JFrame jfrm = new JFrame(" A Simple Audio Player Interface");
                 jfrm.setLayout(new FlowLayout());
                 jfrm.setSize(340, 520);
                 jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 jfrm.setContentPane(audioPlayer.getContentPane());
                 jfrm.setVisible(true);
             }
        });
    }

    public JPanel getContentPane() {
        return contentPane;
    }
}
