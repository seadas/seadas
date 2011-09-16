/*
Author: Danny Knowles
    Don Shea
*/

package gov.nasa.obpg.seadas.sandbox.l2gen;

import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.selection.AbstractSelectionChangeListener;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.ui.SourceProductSelector;
import org.esa.beam.framework.gpf.ui.TargetProductSelector;
import org.esa.beam.framework.gpf.ui.TargetProductSelectorModel;
import org.esa.beam.framework.ui.AppContext;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;


class L2genForm extends JTabbedPane {

//    private ArrayList<ProductInfo> waveIndependentProductInfoArray;
//    private ArrayList<ProductInfo> waveDependentProductInfoArray;

    private final AppContext appContext;
    private final SourceProductSelector sourceProductSelector;
    private final TargetProductSelector targetProductSelector;

    private ArrayList<JCheckBox> wavelengthsCheckboxArrayList = null;
    private JPanel wavelengthsJPanel;
    private JList waveDependentJList;
    private JList waveIndependentJList;
    private JTextArea selectedProductsJTextArea;


    private JTextArea parfileTextEntryName;
    private JTextArea parfileTextEntryValue;
    private JButton parfileTextEntrySubmit;

    public JTextField spixlJTextField;
    private JTextField epixlJTextField;
    private JTextField dpixlJTextField;
    private JTextField slineJTextField;
    private JTextField elineJTextField;
    private JTextField dlineJTextField;

    private JTextField northJTextField;
    private JTextField southJTextField;
    private JTextField westJTextField;
    private JTextField eastJTextField;

    private JTextArea parfileJTextArea;

    private String missionLetter = "";

    private JFileChooser parfileChooser = new JFileChooser();

    private String OCDATAROOT = System.getenv("OCDATAROOT");
    private final int VISIBLE_UPPER_LIMIT = 3000;
    private final String TARGET_PRODUCT_SUFFIX = "L2";
    private String SELECTED_PRODUCTS_JTEXT_AREA_DEFAULT = "No products currently selected";
    private String WAVELENGTHS_PANEL_MESSAGE_DEFAULT = "Wavelengths can be specified here once an input file is selected";

    private String SEADAS_PRODUCTS_FILE = "/home/knowles/SeaDAS/seadas/seadas-sandbox/productList.xml";


    private String currentIfile = "";

    // TEMP will read this from defaults file
    private String spixl_DEFAULT = "1";
    private String epixl_DEFAULT = "-1";
    private String dpixl_DEFAULT = "1";
    private String sline_DEFAULT = "1";
    private String eline_DEFAULT = "-1";
    private String dline_DEFAULT = "1";

    private L2genDataStructure l2genDataStructure = new L2genDataStructure();


    L2genForm(TargetProductSelector targetProductSelector, AppContext appContext) {
        this.targetProductSelector = targetProductSelector;
        this.appContext = appContext;
        this.sourceProductSelector = new SourceProductSelector(appContext, "Source Product:");

        // add event listener
        // addL2genDataListeners();
              createUI();
        //    loadDefaults();
    }


    private void addL2genDataListeners() {
        l2genDataStructure.addPropertyChangeListener(l2genDataStructure.MISSION_STRING_CHANGE_EVENT_NAME, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                missionStringChangeEvent((String) evt.getNewValue());

            }
        });


        l2genDataStructure.addPropertyChangeListener(l2genDataStructure.SPIXL, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                spixlJTextField.setText(l2genDataStructure.getParamValue(l2genDataStructure.SPIXL));
            }
        });

        l2genDataStructure.addPropertyChangeListener(l2genDataStructure.EPIXL, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                epixlJTextField.setText(l2genDataStructure.getParamValue(l2genDataStructure.EPIXL));
            }
        });

        l2genDataStructure.addPropertyChangeListener(l2genDataStructure.DPIXL, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                dpixlJTextField.setText(l2genDataStructure.getParamValue(l2genDataStructure.DPIXL));
            }
        });

        l2genDataStructure.addPropertyChangeListener(l2genDataStructure.SLINE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                slineJTextField.setText(l2genDataStructure.getParamValue(l2genDataStructure.SLINE));
            }
        });

        l2genDataStructure.addPropertyChangeListener(l2genDataStructure.ELINE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                elineJTextField.setText(l2genDataStructure.getParamValue(l2genDataStructure.ELINE));
            }
        });

        l2genDataStructure.addPropertyChangeListener(l2genDataStructure.DLINE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                dlineJTextField.setText(l2genDataStructure.getParamValue(l2genDataStructure.DLINE));
            }
        });


        l2genDataStructure.addPropertyChangeListener(l2genDataStructure.NORTH, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                northJTextField.setText(l2genDataStructure.getParamValue(l2genDataStructure.NORTH));
            }
        });


        l2genDataStructure.addPropertyChangeListener(l2genDataStructure.SOUTH, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                southJTextField.setText(l2genDataStructure.getParamValue(l2genDataStructure.SOUTH));
            }
        });


        l2genDataStructure.addPropertyChangeListener(l2genDataStructure.WEST, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                westJTextField.setText(l2genDataStructure.getParamValue(l2genDataStructure.WEST));
            }
        });


        l2genDataStructure.addPropertyChangeListener(l2genDataStructure.EAST, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                eastJTextField.setText(l2genDataStructure.getParamValue(l2genDataStructure.EAST));
            }
        });


        l2genDataStructure.addPropertyChangeListener(l2genDataStructure.PARFILE_TEXT_CHANGE_EVENT_NAME, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("receiving PARFILE_TEXT_CHANGE_EVENT_NAME");
                parfileJTextArea.setText(l2genDataStructure.getParfile());
            }
        });


        l2genDataStructure.addPropertyChangeListener(l2genDataStructure.UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT fired");
                updateWavelengthCheckboxSelectionStateEvent();

            }
        });


    }


    private void missionStringChangeEvent(String newMissionString) {
        //todo
        System.out.println("This is cool " + newMissionString);

        updateProductSelectorWavelengthsPanel();

        updateWavelengthCheckboxSelectionStateEvent();
        updateSelectedProductsJTextArea();

    }


    private void updateWavelengthCheckboxSelectionStateEvent() {

        for (WavelengthInfo wavelengthInfo : l2genDataStructure.getWavelengthInfoArray()) {
            for (JCheckBox currJCheckbox : wavelengthsCheckboxArrayList) {
                if (wavelengthInfo.getWavelength().equals(currJCheckbox.getName())) {
                    if (wavelengthInfo.isSelected() != currJCheckbox.isSelected()) {
                        currJCheckbox.setSelected(wavelengthInfo.isSelected());
                    }
                }
            }
        }
    }


    private void loadDefaults() {
        spixlJTextField.setText(spixl_DEFAULT);
        epixlJTextField.setText(epixl_DEFAULT);
        dpixlJTextField.setText(dpixl_DEFAULT);
        slineJTextField.setText(sline_DEFAULT);
        elineJTextField.setText(eline_DEFAULT);
        dlineJTextField.setText(dline_DEFAULT);
    }

    Product getSourceProduct() {
        return sourceProductSelector.getSelectedProduct();
    }

    void prepareShow() {
        sourceProductSelector.initProducts();
    }

    void prepareHide() {
        sourceProductSelector.releaseProducts();
    }

    private void createUI() {
        createIOParametersTab("I/O Parameters");
        createParfileTab("Processing Parameters");
        createSubsampleTab("Sub Sample");
        createProductSelectorTab("Product Selector");
        System.out.println("OCDATAROOT=" + OCDATAROOT);
    }


    private void createSubsampleTab(String tabnameSubsample) {

        final JTabbedPane tabbedPane = new JTabbedPane();
        createLatLonSubTab(tabbedPane, "Lat-Lon");
        createPixelsLinesSubTab(tabbedPane, "Pix-Line");


        // Declare mainPanel and set it's attributes
        final JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(""));
        mainPanel.setLayout(new GridBagLayout());


        // Add Swing controls to mainPanel grid cells
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 1;
            mainPanel.add(tabbedPane, c);
        }


        final JPanel paddedMainPanel;
        paddedMainPanel = addPaddedWrapperPanel(mainPanel, 6);

        addTab(tabnameSubsample, paddedMainPanel);
    }




    private void spixlLostFocus() {
        l2genDataStructure.setParamValue(l2genDataStructure.SPIXL, spixlJTextField.getText().toString());
    }

    private void epixlLostFocus() {
        l2genDataStructure.setParamValue(l2genDataStructure.EPIXL, epixlJTextField.getText().toString());
    }

    private void dpixlLostFocus() {
        l2genDataStructure.setParamValue(l2genDataStructure.DPIXL, dpixlJTextField.getText().toString());
    }

    private void slineLostFocus() {
        l2genDataStructure.setParamValue(l2genDataStructure.SLINE, slineJTextField.getText().toString());
    }

    private void elineLostFocus() {
        l2genDataStructure.setParamValue(l2genDataStructure.ELINE, elineJTextField.getText().toString());
    }

    private void dlineLostFocus() {
        l2genDataStructure.setParamValue(l2genDataStructure.DLINE, dlineJTextField.getText().toString());
    }

    private void northLostFocus() {
        l2genDataStructure.setParamValue(l2genDataStructure.NORTH, northJTextField.getText().toString());
    }

    private void southLostFocus() {
        l2genDataStructure.setParamValue(l2genDataStructure.SOUTH, southJTextField.getText().toString());
    }

    private void westLostFocus() {
        l2genDataStructure.setParamValue(l2genDataStructure.WEST, westJTextField.getText().toString());
    }

    private void eastLostFocus() {
        l2genDataStructure.setParamValue(l2genDataStructure.EAST, eastJTextField.getText().toString());
    }

    private void parfileLostFocus() {
        l2genDataStructure.setParfile(parfileJTextArea.getText().toString());
    }


    private void createPixelsLinesSubTab(JTabbedPane tabbedPane, String myTabname) {

        // ---------------------------------------------------------------------------------------- 
        // Set all constants for this tabbed pane
        // ---------------------------------------------------------------------------------------- 

        final int PIXELS_JTEXTFIELD_LENGTH = 5;
        final int LINES_JTEXTFIELD_LENGTH = 5;

        final String PIXELS_PANEL_TITLE = "Pixels";
        final String LINES_PANEL_TITLE = "Lines";

        final String SPIXL_LABEL = "start pix";
        final String EPIXL_LABEL = "end pix";
        final String DPIXL_LABEL = "delta pix";
        final String SLINE_LABEL = "start line";
        final String ELINE_LABEL = "end line";
        final String DLINE_LABEL = "delta line";


        // ---------------------------------------------------------------------------------------- 
        // Create all Swing controls used on this tabbed panel
        // ---------------------------------------------------------------------------------------- 

        spixlJTextField = new JTextField(PIXELS_JTEXTFIELD_LENGTH);
        epixlJTextField = new JTextField(PIXELS_JTEXTFIELD_LENGTH);
        dpixlJTextField = new JTextField(PIXELS_JTEXTFIELD_LENGTH);

        slineJTextField = new JTextField(LINES_JTEXTFIELD_LENGTH);
        elineJTextField = new JTextField(LINES_JTEXTFIELD_LENGTH);
        dlineJTextField = new JTextField(LINES_JTEXTFIELD_LENGTH);


        // ---------------------------------------------------------------------------------------- 
        // Add lose focus listeners to all JTextField components
        // ---------------------------------------------------------------------------------------- 

        spixlJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                spixlLostFocus();
            }
        });


        epixlJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                epixlLostFocus();
            }
        });

        dpixlJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                dpixlLostFocus();
            }
        });

        slineJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                slineLostFocus();
            }
        });

        elineJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                elineLostFocus();
            }
        });

        dlineJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                dlineLostFocus();
            }
        });


        // ---------------------------------------------------------------------------------------- 
        // Create labels for all Swing controls used on this tabbed panel
        // ---------------------------------------------------------------------------------------- 

        final JLabel spixlJLabel = new JLabel(SPIXL_LABEL);
        final JLabel epixlJLabel = new JLabel(EPIXL_LABEL);
        final JLabel dpixlJLabel = new JLabel(DPIXL_LABEL);

        final JLabel slineJLabel = new JLabel(SLINE_LABEL);
        final JLabel elineJLabel = new JLabel(ELINE_LABEL);
        final JLabel dlineJLabel = new JLabel(DLINE_LABEL);


        // ---------------------------------------------------------------------------------------- 
        // Create pixelsPanel to hold all pixel controls
        // ---------------------------------------------------------------------------------------- 

        final JPanel pixelsPanel = new JPanel();
        pixelsPanel.setBorder(BorderFactory.createTitledBorder(PIXELS_PANEL_TITLE));
        pixelsPanel.setLayout(new GridBagLayout());

        pixelsPanel.add(spixlJLabel,
                SeadasGuiUtils.makeConstraints(0, 0, GridBagConstraints.EAST));
        pixelsPanel.add(spixlJTextField,
                SeadasGuiUtils.makeConstraints(1, 0));

        pixelsPanel.add(epixlJLabel,
                SeadasGuiUtils.makeConstraints(0, 1, GridBagConstraints.EAST));
        pixelsPanel.add(epixlJTextField,
                SeadasGuiUtils.makeConstraints(1, 1));

        pixelsPanel.add(dpixlJLabel,
                SeadasGuiUtils.makeConstraints(0, 2, GridBagConstraints.EAST));
        pixelsPanel.add(dpixlJTextField,
                SeadasGuiUtils.makeConstraints(1, 2));


        // ----------------------------------------------------------------------------------------
        // Create linesPanel to hold all lines controls
        // ----------------------------------------------------------------------------------------         

        final JPanel linesPanel = new JPanel();
        linesPanel.setBorder(BorderFactory.createTitledBorder(LINES_PANEL_TITLE));
        linesPanel.setLayout(new GridBagLayout());

        linesPanel.add(slineJLabel,
                SeadasGuiUtils.makeConstraints(0, 0, GridBagConstraints.EAST));
        linesPanel.add(slineJTextField,
                SeadasGuiUtils.makeConstraints(1, 0));

        linesPanel.add(elineJLabel,
                SeadasGuiUtils.makeConstraints(0, 1, GridBagConstraints.EAST));
        linesPanel.add(elineJTextField,
                SeadasGuiUtils.makeConstraints(1, 1));

        linesPanel.add(dlineJLabel,
                SeadasGuiUtils.makeConstraints(0, 2, GridBagConstraints.EAST));
        linesPanel.add(dlineJTextField,
                SeadasGuiUtils.makeConstraints(1, 2));


        // ---------------------------------------------------------------------------------------- 
        // Create mainPanel to hold pixelsPanel and linesPanel
        // ---------------------------------------------------------------------------------------- 

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        mainPanel.add(pixelsPanel,
                SeadasGuiUtils.makeConstraints(0, 0));
        mainPanel.add(linesPanel,
                SeadasGuiUtils.makeConstraints(1, 0));


        // ---------------------------------------------------------------------------------------- 
        // Create wrappedMainPanel to hold mainPanel: this is a formatting wrapper panel
        // ---------------------------------------------------------------------------------------- 

        final JPanel wrappedMainPanel = new JPanel();
        wrappedMainPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = SeadasGuiUtils.makeConstraints(0, 0);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(3, 3, 3, 3);
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 1;
        constraints.weighty = 1;

        wrappedMainPanel.add(mainPanel, constraints);


        // ---------------------------------------------------------------------------------------- 
        // Add wrappedMainPanel to tabbedPane
        // ---------------------------------------------------------------------------------------- 

        tabbedPane.addTab(myTabname, wrappedMainPanel);

    }


    private void createLatLonSubTab(JTabbedPane tabbedPane, String myTabname) {

        // ----------------------------------------------------------------------------------------
        // Set all constants for this tabbed pane
        // ----------------------------------------------------------------------------------------

        final int COORDINATES_JTEXTFIELD_LENGTH = 5;

        final String COORDINATES_PANEL_TITLE = "Coordinates";

        final String NORTH_LABEL = "N";
        final String SOUTH_LABEL = "S";
        final String EAST_LABEL = "E";
        final String WEST_LABEL = "W";


        // ----------------------------------------------------------------------------------------
        // Create all Swing controls used on this tabbed panel
        // ----------------------------------------------------------------------------------------

        northJTextField = new JTextField(COORDINATES_JTEXTFIELD_LENGTH);
        southJTextField = new JTextField(COORDINATES_JTEXTFIELD_LENGTH);
        westJTextField = new JTextField(COORDINATES_JTEXTFIELD_LENGTH);
        eastJTextField = new JTextField(COORDINATES_JTEXTFIELD_LENGTH);


        // ----------------------------------------------------------------------------------------
        // Add lose focus listeners to all JTextField components
        // ----------------------------------------------------------------------------------------

        northJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                northLostFocus();
            }
        });

        southJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                southLostFocus();
            }
        });

        westJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                westLostFocus();
            }
        });

        eastJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                eastLostFocus();
            }
        });


        // ----------------------------------------------------------------------------------------
        // Create labels for all Swing controls used on this tabbed panel
        // ----------------------------------------------------------------------------------------

        final JLabel northLabel = new JLabel(NORTH_LABEL);
        final JLabel southLabel = new JLabel(SOUTH_LABEL);
        final JLabel westLabel = new JLabel(WEST_LABEL);
        final JLabel eastLabel = new JLabel(EAST_LABEL);


        // ----------------------------------------------------------------------------------------
        // Create mainPanel to hold all controls
        // ----------------------------------------------------------------------------------------

        final JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(COORDINATES_PANEL_TITLE));
        mainPanel.setLayout(new GridBagLayout());

        mainPanel.add(northJTextField,
                SeadasGuiUtils.makeConstraints(2, 1, GridBagConstraints.NORTH));

        mainPanel.add(southJTextField,
                SeadasGuiUtils.makeConstraints(2, 3, GridBagConstraints.SOUTH));

        mainPanel.add(eastJTextField,
                SeadasGuiUtils.makeConstraints(3, 2, GridBagConstraints.EAST));

        mainPanel.add(westJTextField,
                SeadasGuiUtils.makeConstraints(1, 2, GridBagConstraints.WEST));

        mainPanel.add(northLabel,
                SeadasGuiUtils.makeConstraints(2, 0, GridBagConstraints.SOUTH));

        mainPanel.add(southLabel,
                SeadasGuiUtils.makeConstraints(2, 4, GridBagConstraints.NORTH));

        mainPanel.add(eastLabel,
                SeadasGuiUtils.makeConstraints(4, 2, GridBagConstraints.WEST));

        mainPanel.add(westLabel,
                SeadasGuiUtils.makeConstraints(0, 2, GridBagConstraints.EAST));


        // ----------------------------------------------------------------------------------------
        // Create wrappedMainPanel to hold mainPanel: this is a formatting wrapper panel
        // ----------------------------------------------------------------------------------------

        final JPanel wrappedMainPanel = new JPanel();
        wrappedMainPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1;
        c.weighty = 1;

        wrappedMainPanel.add(mainPanel, c);


        // ----------------------------------------------------------------------------------------
        // Add wrappedMainPanel to tabbedPane
        // ----------------------------------------------------------------------------------------

        tabbedPane.addTab(myTabname, wrappedMainPanel);
    }


    public void loadParfileEntry() {
        System.out.println(parfileTextEntryName.getText() + "=" + parfileTextEntryValue.getText());
        l2genDataStructure.setParamValue(parfileTextEntryName.getText(), parfileTextEntryValue.getText());
        System.out.println("ifile=" + l2genDataStructure.getParamValue(l2genDataStructure.IFILE));
    }

    public void uploadParfile() {

        final ArrayList<String> parfileTextLines = myReadDataFile(parfileChooser.getSelectedFile().toString());

        StringBuilder parfileText = new StringBuilder();

        for (String currLine : parfileTextLines) {
            parfileText.append(currLine);
            parfileText.append("\n");
        }

        l2genDataStructure.setParfile(parfileText.toString());
        parfileJTextArea.setEditable(true);
        parfileJTextArea.setEditable(false);
        //  parfileJTextArea.setText(parfileText.toString());
    }


    public void writeParfile() {

        try {
            // Create file
            FileWriter fstream = new FileWriter(parfileChooser.getSelectedFile().toString());
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(l2genDataStructure.getParfile());
            //Close the output stream
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }


    private void createParfileTab(String myTabname) {


        // Define all Swing controls used on this tab page
        final JButton loadParfileButton = new JButton("Open");
        final JButton saveParfileButton = new JButton("Save");

        saveParfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = parfileChooser.showSaveDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    writeParfile();
                }

            }
        });

        loadParfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = parfileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    uploadParfile();
                }

            }
        });

        parfileJTextArea = new JTextArea();
        parfileJTextArea.setEditable(false);
        parfileJTextArea.setBackground(Color.decode("#dddddd"));

        parfileJTextArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                parfileLostFocus();
            }
        });


        parfileTextEntryName = new JTextArea();
        parfileTextEntryName.setColumns(20);
        parfileTextEntryValue = new JTextArea();
        parfileTextEntryValue.setColumns(20);
        parfileTextEntrySubmit = new JButton("Load This Entry");
        JLabel equalsSign = new JLabel("=");


        parfileTextEntrySubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadParfileEntry();

            }
        });

        final JPanel parfileTextEntryPanel = new JPanel();
        parfileTextEntryPanel.setLayout(new FlowLayout());
        parfileTextEntryPanel.add(parfileTextEntryName);
        parfileTextEntryPanel.add(equalsSign);
        parfileTextEntryPanel.add(parfileTextEntryValue);
        parfileTextEntryPanel.add(parfileTextEntrySubmit);

        // Declare mainPanel and set it's attributes
        final JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder("Parfile"));
        mainPanel.setLayout(new GridBagLayout());

        // Add openButton control to a mainPanel grid cell
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
            mainPanel.add(loadParfileButton, c);
        }

        // Add saveButton control to a mainPanel grid cell
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 0;
            c.anchor = GridBagConstraints.EAST;
            mainPanel.add(saveParfileButton, c);
        }

        // Add textArea control to a mainPanel grid cell
        {
            JScrollPane scrollTextArea = new JScrollPane(parfileJTextArea);

            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.fill = GridBagConstraints.BOTH;
            c.gridwidth = 2;
            c.weightx = 1;
            c.weighty = 1;
            mainPanel.add(scrollTextArea, c);
        }

        // Add saveButton control to a mainPanel grid cell
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 2;
            mainPanel.add(parfileTextEntryPanel, c);
        }


        final JPanel finalMainPanel;
        finalMainPanel = addPaddedWrapperPanel(mainPanel, 3);


        addTab(myTabname, finalMainPanel);
    }


    private void createProductSelectorTab(String myTabname) {

        wavelengthsJPanel = new JPanel();
        final JPanel productWavelengthIndependentPanel = new JPanel();
        final JPanel productWavelengthDependentPanel = new JPanel();
        final JPanel selectedProductsPanel = new JPanel();

        l2genDataStructure.initProductInfoArrays(SEADAS_PRODUCTS_FILE);

        waveIndependentJList = new JList();
        waveDependentJList = new JList();

        createProductSelectorWavelengthsPanel();

        createProductSelectorProductListPanel(productWavelengthIndependentPanel, l2genDataStructure.getWaveIndependentProductInfoArray(), "Products (Wavelength Independent)", waveIndependentJList);

        createProductSelectorProductListPanel(productWavelengthDependentPanel, l2genDataStructure.getWaveDependentProductInfoArray(), "Products (Wavelength Dependent)", waveDependentJList);


        waveDependentJList.addListSelectionListener(new ListSelectionListener() {
            @Override

            public void valueChanged(ListSelectionEvent e) {
                updateSelectedProductsJTextArea();


            }
        });

        waveIndependentJList.addListSelectionListener(new ListSelectionListener() {
            @Override

            public void valueChanged(ListSelectionEvent e) {
                updateSelectedProductsJTextArea();


            }
        });


        createSelectedProductsPanel(selectedProductsPanel);


        // Declare mainPanel and set it's attributes
        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new

                GridBagLayout()

        );


        // Add to mainPanel grid cell
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTH;
            c.weightx = 0;
            c.weighty = 0;
            mainPanel.add(wavelengthsJPanel, c);
        }


        // Add to mainPanel grid cell
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.NORTH;
            c.weightx = 0;
            c.weighty = 0;
            mainPanel.add(productWavelengthDependentPanel, c);
        }


        // Add to mainPanel grid cell
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 2;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.NORTH;
            c.weightx = 0;
            c.weighty = 0;
            mainPanel.add(productWavelengthIndependentPanel, c);
        }

        // Add to mainPanel grid cell
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 3;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.NORTH;
            c.weightx = 1;
            c.weighty = 1;
            mainPanel.add(selectedProductsPanel, c);
        }


        final JPanel finalMainPanel = new JPanel();
        finalMainPanel.setLayout(new

                GridBagLayout()

        );

        {
            final GridBagConstraints c;
            c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.insets = new Insets(3, 3, 3, 3);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 1;

            finalMainPanel.add(mainPanel, c);
        }


        addTab(myTabname, finalMainPanel);
    }


    private void createProductSelectorProductListPanel(JPanel productPanel, ArrayList<ProductInfo> productInfoArrayList,
                                                       String myTitle, JList algorithmInfoJList) {

        // Create arrayList for all the algorithmInfo objects
        ArrayList<AlgorithmInfo> algorithmInfoArrayList = new ArrayList<AlgorithmInfo>();

        for (ProductInfo productInfo : productInfoArrayList) {

            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                algorithmInfo.setToStringShowProductName(true);
                algorithmInfoArrayList.add(algorithmInfo);
            }

        }

        // Store the arrayList into an array which can then be fed into a JList control
        AlgorithmInfo[] algorithmInfoArray = new AlgorithmInfo[algorithmInfoArrayList.size()];
        algorithmInfoArrayList.toArray(algorithmInfoArray);

        // format the JList control
        algorithmInfoJList.setListData(algorithmInfoArray);
        JScrollPane algorithmInfoJListScrollPane = new JScrollPane(algorithmInfoJList);
        algorithmInfoJListScrollPane.setMinimumSize(new Dimension(400, 100));
        algorithmInfoJListScrollPane.setMaximumSize(new Dimension(400, 100));
        algorithmInfoJListScrollPane.setPreferredSize(new Dimension(400, 100));

        productPanel.setBorder(BorderFactory.createTitledBorder(myTitle));
        productPanel.setLayout(new GridBagLayout());

        // Add to productPanel grid cell
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 1;
            productPanel.add(algorithmInfoJListScrollPane, c);
        }

    }


    private void updateSelectedProductsJTextArea() {

        final StringBuilder selectedProductListStringBuilder = new StringBuilder("");
        final StringBuilder currentSelectedProductStringBuilder = new StringBuilder("");
        final String PRODUCT_LIST_DELIMITER = " ";

        if (wavelengthsCheckboxArrayList != null) {

            for (final JCheckBox currWavelengthCheckBox : wavelengthsCheckboxArrayList) {

                if (currWavelengthCheckBox.isSelected()) {

                    String selectedWavelengthString = currWavelengthCheckBox.getName().toString();

                    int wavelengthInteger = Integer.parseInt(selectedWavelengthString);

                    Object values[] = waveDependentJList.getSelectedValues();

                    for (int i = 0; i < values.length; i++) {
                        AlgorithmInfo algorithmInfo = (AlgorithmInfo) values[i];

                        if (wavelengthInteger < VISIBLE_UPPER_LIMIT) {
                            if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.VISIBLE ||
                                    algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
                                currentSelectedProductStringBuilder.delete(0, currentSelectedProductStringBuilder.length());

                                currentSelectedProductStringBuilder.append(algorithmInfo.getProductName());
                                currentSelectedProductStringBuilder.append("_");
                                currentSelectedProductStringBuilder.append(selectedWavelengthString);

                                if (algorithmInfo.getName() != null) {
                                    currentSelectedProductStringBuilder.append("_");
                                    currentSelectedProductStringBuilder.append(algorithmInfo.getName());
                                }

                                //   currentSelectedProductStringBuilder.append(algorithmInfo.getParameterType());
                                //    currentSelectedProductStringBuilder.append("VISIBLE");

                                if (selectedProductListStringBuilder.length() > 0) {
                                    selectedProductListStringBuilder.append(PRODUCT_LIST_DELIMITER);
                                }
                                selectedProductListStringBuilder.append(currentSelectedProductStringBuilder);
                            }

                        } else {
                            if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.IR ||
                                    algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
                                currentSelectedProductStringBuilder.delete(0, currentSelectedProductStringBuilder.length());

                                currentSelectedProductStringBuilder.append(algorithmInfo.getProductName());
                                currentSelectedProductStringBuilder.append("_");
                                currentSelectedProductStringBuilder.append(selectedWavelengthString);

                                if (algorithmInfo.getName() != null) {
                                    currentSelectedProductStringBuilder.append("_");
                                    currentSelectedProductStringBuilder.append(algorithmInfo.getName());
                                }

                                //    currentSelectedProductStringBuilder.append(algorithmInfo.getParameterType());
                                //    currentSelectedProductStringBuilder.append("IR");

                                if (selectedProductListStringBuilder.length() > 0) {
                                    selectedProductListStringBuilder.append(PRODUCT_LIST_DELIMITER);
                                }
                                selectedProductListStringBuilder.append(currentSelectedProductStringBuilder);
                            }
                        }
                    }
                }
            }
        }


        Object values[] = waveIndependentJList.getSelectedValues();

        for (int i = 0; i < values.length; i++) {
            AlgorithmInfo algorithmInfo = (AlgorithmInfo) values[i];

            if (selectedProductListStringBuilder.length() > 0) {
                selectedProductListStringBuilder.append(PRODUCT_LIST_DELIMITER);
            }


            selectedProductListStringBuilder.append(algorithmInfo.getProductName());

            if (algorithmInfo.getName() != null) {
                selectedProductListStringBuilder.append("_");
                selectedProductListStringBuilder.append(algorithmInfo.getName());
            }

        }


        if (selectedProductListStringBuilder.length() > 0) {
            l2genDataStructure.setParamValue(l2genDataStructure.PROD, selectedProductListStringBuilder.toString());
            selectedProductsJTextArea.setText(selectedProductListStringBuilder.toString());
        } else {
            selectedProductsJTextArea.setText(SELECTED_PRODUCTS_JTEXT_AREA_DEFAULT);
        }


    }


    private void createSelectedProductsPanel(JPanel selectedProductsPanel) {


        selectedProductsPanel.setBorder(BorderFactory.createTitledBorder("Selected Products"));
        selectedProductsPanel.setLayout(new GridBagLayout());

        selectedProductsJTextArea = new JTextArea(SELECTED_PRODUCTS_JTEXT_AREA_DEFAULT);
        selectedProductsJTextArea.setEditable(false);
        selectedProductsJTextArea.setLineWrap(true);
        selectedProductsJTextArea.setWrapStyleWord(true);
        selectedProductsJTextArea.setColumns(20);
        selectedProductsJTextArea.setRows(5);

        updateSelectedProductsJTextArea();

        // Add openButton control to a mainPanel grid cell
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            selectedProductsPanel.add(selectedProductsJTextArea, c);
        }
    }


    private void createProductSelectorWavelengthsPanel() {

        wavelengthsCheckboxArrayList = new ArrayList<JCheckBox>();

        // config panel
        wavelengthsJPanel.setBorder(BorderFactory.createTitledBorder("Wavelengths"));
        wavelengthsJPanel.setLayout(new GridBagLayout());

        JLabel defaultMessageJLabel = new JLabel(WAVELENGTHS_PANEL_MESSAGE_DEFAULT);

        // add default message to the panel
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1;
            wavelengthsJPanel.add(defaultMessageJLabel, c);
        }

    }


    private void updateProductSelectorWavelengthsPanel() {

        wavelengthsJPanel.removeAll();

        // some GridBagLayout formatting variables
        int gridyCnt = 0;
        int gridxCnt = 0;
        int gridxColumns = 4;

        // clear this because we dynamically rebuild it when input file selection is made or changed
        wavelengthsCheckboxArrayList.clear();

        for (WavelengthInfo wavelengthInfo : l2genDataStructure.getWavelengthInfoArray()) {

            final String currWavelength = wavelengthInfo.getWavelength();
            final JCheckBox currJCheckBox = new JCheckBox(currWavelength);

            currJCheckBox.setName(currWavelength);

            // add current JCheckBox to the externally accessible arrayList
            wavelengthsCheckboxArrayList.add(currJCheckBox);

            // add listener for current checkbox
            currJCheckBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    l2genDataStructure.setIsSelectedWavelengthInfoArray(currWavelength, currJCheckBox.isSelected());
                    updateSelectedProductsJTextArea();

                }
            });


            // add current JCheckBox to the panel
            {
                final GridBagConstraints c = new GridBagConstraints();
                c.gridx = gridxCnt;
                c.gridy = gridyCnt;
                c.fill = GridBagConstraints.NONE;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.weightx = 1;
                wavelengthsJPanel.add(currJCheckBox, c);
            }

            // increment GridBag coordinates
            if (gridxCnt < (gridxColumns - 1)) {
                gridxCnt++;
            } else {
                gridxCnt = 0;
                gridyCnt++;
            }

        }


        updateWavelengthCheckboxSelectionStateEvent();
    }



    private JPanel addPaddedWrapperPanel(JPanel myMainPanel, int pad) {

        JPanel myWrapperPanel = new JPanel();

        myWrapperPanel.setLayout(new GridBagLayout());

        final GridBagConstraints c;
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(pad, pad, pad, pad);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        myWrapperPanel.add(myMainPanel, c);

        return myWrapperPanel;
    }


    private ArrayList<String> myReadDataFile(String fileName) {
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


    private void createIOParametersTab(String myTabname) {
        final TableLayout tableLayout = new TableLayout(1);
        tableLayout.setTableWeightX(1.0);
        tableLayout.setTableWeightY(0);
        tableLayout.setTableFill(TableLayout.Fill.BOTH);
        tableLayout.setTablePadding(3, 3);

        final JPanel ioPanel = new JPanel(tableLayout);
        ioPanel.add(createSourceProductPanel());
        ioPanel.add(targetProductSelector.createDefaultPanel());
        ioPanel.add(tableLayout.createVerticalSpacer());

        addTab(myTabname, ioPanel);

    }


    private JPanel createSourceProductPanel() {
        final JPanel panel = sourceProductSelector.createDefaultPanel();

        sourceProductSelector.getProductNameLabel().setText("Name:");
        sourceProductSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "MER_RR__1PPBCM20030730_071000_000003972018_00321_07389_0000.N1");
        sourceProductSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {
                Product sourceProduct = getSourceProduct();
                updateTargetProductName(sourceProduct);
                updateProductSelectorWavelengthsPanel();
                updateSelectedProductsJTextArea();
//                updateParfileJTextArea();
            }
        });
        return panel;
    }


    private void updateTargetProductName(Product selectedProduct) {

        String productName = "output." + TARGET_PRODUCT_SUFFIX;

        final TargetProductSelectorModel selectorModel = targetProductSelector.getModel();

        if (selectedProduct != null) {
            int i = selectedProduct.getName().lastIndexOf('.');
            if (i != -1) {
                String baseName = selectedProduct.getName().substring(0, i);
                productName = baseName + "." + TARGET_PRODUCT_SUFFIX;
            } else {
                productName = selectedProduct.getName() + "." + TARGET_PRODUCT_SUFFIX;
            }
        }

        selectorModel.setProductName(productName);
    }


}
