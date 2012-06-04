package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.general.GridBagConstraintsCustom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/11/12
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */

public class L2genParfilePanel extends JPanel {

    private L2genForm l2genForm;
    private L2genData l2genData;

    private int tabIndex;

    private JButton openButton;
    private JButton saveButton;
    private JCheckBox retainIfileCheckbox;
    private JButton getAncButton;
    private JCheckBox showDefaultsCheckbox;
    private JTextArea parStringTextArea;


    L2genParfilePanel(L2genForm l2genForm, int tabIndex) {

        this.l2genForm = l2genForm;
        this.l2genData = l2genForm.getL2genData();
        this.tabIndex = tabIndex;

        initComponents();
        addComponents();
    }


    public void initComponents() {
        openButton = createOpenButton();
        saveButton = createSaveButton();
        retainIfileCheckbox = createRetainIfileCheckbox();
        getAncButton = createGetAncButton();
        showDefaultsCheckbox = createShowDefaultsCheckbox();
        parStringTextArea = new ParStringTextArea().getjTextArea();
    }


    public void addComponents() {

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Parfile"));


        final JPanel openButtonRetainPanel = new JPanel(new GridBagLayout());
        openButtonRetainPanel.setBorder(BorderFactory.createEtchedBorder());
        openButtonRetainPanel.add(openButton,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
        openButtonRetainPanel.add(retainIfileCheckbox,
                new GridBagConstraintsCustom(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));


        final JPanel subPanel = new JPanel(new GridBagLayout());
        subPanel.add(openButtonRetainPanel,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        subPanel.add(getAncButton,
                new GridBagConstraintsCustom(1, 0, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        subPanel.add(showDefaultsCheckbox,
                new GridBagConstraintsCustom(2, 0, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        subPanel.add(saveButton,
                new GridBagConstraintsCustom(3, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));


        add(subPanel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        add(new JScrollPane(parStringTextArea),
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH));
    }


    private JButton createOpenButton() {

        String NAME = "Open";

        final JButton jButton = new JButton(NAME);

        final JFileChooser jFileChooser = new JFileChooser();

        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadParfile(jFileChooser);
            }
        });

        return jButton;
    }


    private JButton createSaveButton() {

        String NAME = "Save";

        final JButton jButton = new JButton(NAME);

        final JFileChooser jFileChooser = new JFileChooser();

        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writeParfile(jFileChooser);
            }
        });


        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jButton.setEnabled(l2genData.isValidIfile());
            }
        });

        return jButton;
    }


    private JCheckBox createRetainIfileCheckbox() {

        String NAME = "Retain Selected IFILE";
        String TOOL_TIP = "If an ifile is currently selected then any ifile entry in the parfile being opened will be ignored.";

        final JCheckBox jCheckBox = new JCheckBox(NAME);

        jCheckBox.setSelected(l2genData.isRetainCurrentIfile());
        jCheckBox.setToolTipText(TOOL_TIP);

        final boolean[] handlerEnabled = {true};

        jCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (handlerEnabled[0]) {
                    l2genData.setRetainCurrentIfile(jCheckBox.isSelected());
                }
            }
        });

        l2genData.addPropertyChangeListener(l2genData.RETAIN_IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                handlerEnabled[0] = false;
                jCheckBox.setSelected(l2genData.isRetainCurrentIfile());
                handlerEnabled[0] = true;
            }
        });


        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jCheckBox.setEnabled(l2genData.isValidIfile());
            }
        });

        return jCheckBox;
    }


    private JButton createGetAncButton() {

        String NAME = "Get Ancillary";

        final JButton jButton = new JButton(NAME);

        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.setAncillaryFiles();
            }
        });


        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jButton.setEnabled(l2genData.isValidIfile());
            }
        });

        return jButton;
    }


    private JCheckBox createShowDefaultsCheckbox() {

        String NAME = "Show Defaults";
        String TOOL_TIP = "Displays all the defaults with the parfile text region";

        final JCheckBox jCheckBox = new JCheckBox(NAME);

        jCheckBox.setSelected(l2genData.isShowDefaultsInParString());
        jCheckBox.setToolTipText(TOOL_TIP);

        final boolean[] handlerEnabled = {true};

        jCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (handlerEnabled[0]) {
                    l2genData.setShowDefaultsInParString(jCheckBox.isSelected());
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jCheckBox.setEnabled(l2genData.isValidIfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.SHOW_DEFAULTS, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                handlerEnabled[0] = false;
                jCheckBox.setSelected(l2genData.isShowDefaultsInParString());
                handlerEnabled[0] = true;
            }
        });

        return jCheckBox;
    }


    private class ParStringTextArea {

        private final JTextArea jTextArea = new JTextArea();

        private boolean updatePending = false;

        ParStringTextArea() {

            jTextArea.setEditable(true);
            jTextArea.setAutoscrolls(true);

            jTextArea.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    controlHandler();
                }
            });


            for (ParamInfo paramInfo : l2genData.getParamInfos()) {
                final String eventName = paramInfo.getName();
                l2genData.addPropertyChangeListener(eventName, new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        // relay this to PARSTRING event in case it is currently disabled
                        l2genData.fireEvent(L2genData.PARSTRING);
                    }
                });
            }

            l2genData.addPropertyChangeListener(L2genData.SHOW_DEFAULTS, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    // relay this to PARSTRING event in case it is currently disabled
                    l2genData.fireEvent(L2genData.PARSTRING);
                }
            });


            l2genData.addPropertyChangeListener(L2genData.PARSTRING, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    eventHandler();
                }
            });


            l2genData.addPropertyChangeListener(L2genData.TAB_CHANGE, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    tabChangeEventHandler(evt);
                }
            });
        }


        private void tabChangeEventHandler(PropertyChangeEvent evt) {
            if (evt.getNewValue().equals(tabIndex) && !evt.getOldValue().equals(tabIndex)) {
                l2genData.enableEvent(L2genData.PARSTRING);
            } else if (!evt.getNewValue().equals(tabIndex) && evt.getOldValue().equals(tabIndex)) {
                l2genData.disableEvent(L2genData.PARSTRING);
            }
        }


        private void controlHandler() {
            l2genData.setParString(jTextArea.getText(), false);
        }


        private void eventHandler() {
            jTextArea.setText(l2genData.getParString());
        }

        public JTextArea getjTextArea() {
            return jTextArea;
        }
    }


    private void uploadParfile(JFileChooser parfileChooser) {

        int result = parfileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            final ArrayList<String> parfileTextLines = myReadDataFile(parfileChooser.getSelectedFile().toString());

            StringBuilder parfileText = new StringBuilder();

            for (String currLine : parfileTextLines) {
                debug(currLine);
                parfileText.append(currLine);
                parfileText.append("\n");
            }

            l2genData.setParString(parfileText.toString(), l2genData.isRetainCurrentIfile());
        }
    }


    private void writeParfile(JFileChooser parfileChooser) {
        int result = parfileChooser.showSaveDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Create file
                FileWriter fstream = new FileWriter(parfileChooser.getSelectedFile().toString());
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(l2genData.getParString(false));
                //Close the output stream
                out.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private ArrayList<String> myReadDataFile
            (String
                     fileName) {
        String lineData;
        ArrayList<String> fileContents = new ArrayList<String>();
        BufferedReader moFile = null;
        try {
            moFile = new BufferedReader(new FileReader(new File(fileName)));
            while ((lineData = moFile.readLine()) != null) {

                fileContents.add(lineData);
            }
        } catch (IOException e) {
            ;
        } finally {
            try {
                moFile.close();
            } catch (Exception e) {
                //Ignore
            }
        }
        return fileContents;
    }


    private void debug(String message) {

    }
}

