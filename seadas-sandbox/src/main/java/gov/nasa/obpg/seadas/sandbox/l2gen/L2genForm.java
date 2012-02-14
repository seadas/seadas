/*
Author: Danny Knowles
    Don Shea
*/

package gov.nasa.obpg.seadas.sandbox.l2gen;

import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.selection.AbstractSelectionChangeListener;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.ui.SourceProductSelector;
import org.esa.beam.framework.gpf.ui.TargetProductSelector;
import org.esa.beam.framework.ui.AppContext;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;


class L2genForm extends JTabbedPane {

    private final AppContext appContext;
    private final SourceProductSelector sourceProductSelector;
    private final TargetProductSelector targetProductSelector;

    private ArrayList<JCheckBox> wavelengthsJCheckboxArrayList = null;
    private JPanel wavelengthsJPanel;
    private JPanel productsLimiterJPanel;
//    private JList waveDependentProductsJList;
//    private JList waveIndependentProductsJList;

    private JTable productsCartJTable;
    private SelectedProductsTableModel productsCartTableModel;
    private JPanel productsCartJPanel;
    private JComboBox ofileJComboBox;

//    private boolean setWaveIndependentProductsJListEnabled = true;
//    private boolean setWaveDependentProductsJListEnabled = true;

//    private boolean handleWaveIndependentProductsJListEnabled = true;
//    private boolean handleWaveDependentProductsJListEnabled = true;

    private boolean wavelengthCheckboxControlHandlersEnabled = true;

    private boolean handleIfileJComboBoxEnabled = true;

    private JTextArea selectedProductsJTextArea;

    private JTextArea parfileTextEntryName;
    private JTextArea parfileTextEntryValue;
    private JButton parfileTextEntrySubmit;

    private JTextField spixlJTextField;
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

    private JCheckBox wavelengthTypeIiiCheckbox;
    private JCheckBox wavelengthTypeVvvCheckbox;

    private JFileChooser parfileChooser = new JFileChooser();

    private DefaultMutableTreeNode rootNode;

    private String SELECTED_PRODUCTS_JTEXT_AREA_DEFAULT = "No products currently selected";
    private String SEADAS_PRODUCTS_FILE = "productList.xml";

    private static final int PRODUCT_SELECTOR_TAB_INDEX = 3;
    private static final int SUB_SAMPLE_TAB_INDEX = 2;

    private String WAVELENGTH_TYPE_INFRARED = "iii";
    private String WAVELENGTH_TYPE_VISIBLE = "vvv";

    private String WAVE_INDEPENDENT_PRODUCTS_JPANEL_TITLE = "Products (Wavelength Independent)";
    private String WAVE_DEPENDENT_PRODUCTS_JPANEL_TITLE = "Products (Wavelength Dependent)";

    private JButton selectedProductsDefaultsButton;
    private JButton selectedProductsEditLoadButton;
    private JButton selectedProductsCancelButton;

    private JTree productJTree;
    private boolean ignoreTreeCheck = false;

    private L2genData l2genData = new L2genData();
    private L2genReader l2genReader = new L2genReader(l2genData);

    enum DisplayMode {STANDARD_MODE, EDIT_MODE}


    private String EDIT_LOAD_BUTTON_TEXT_STANDARD_MODE = "Edit";
    private String EDIT_LOAD_BUTTON_TEXT_EDIT_MODE = "Load";


    L2genForm(TargetProductSelector targetProductSelector, AppContext appContext) {
        this.targetProductSelector = targetProductSelector;
        this.appContext = appContext;
        this.sourceProductSelector = new SourceProductSelector(appContext, "Source Product:");


        // add event listener
        addL2genDataListeners();
        createUserInterface();
        //    loadDefaults();
    }


    //----------------------------------------------------------------------------------------
    // Create tabs within the main panel
    //----------------------------------------------------------------------------------------

    private void createUserInterface() {
        createIOParametersTab("Input/Output");
        createParfileTab("Parameters");
        createSubsampleTab("Sub Sampling");
        //    createProductTab("Product");
        //  createProductSelectorSubTab("OLD Product Selector");

        createTreeSubTab("Products");
        createSelectedProductsSubTab("Selected Products Cart");


        this.setEnabledAt(PRODUCT_SELECTOR_TAB_INDEX, false);
        this.setEnabledAt(SUB_SAMPLE_TAB_INDEX, false);
    }


    //----------------------------------------------------------------------------------------
    // Methods to create each of the main  and sub tabs
    //----------------------------------------------------------------------------------------

    private void createIOParametersTab(String myTabname) {
        final TableLayout tableLayout = new TableLayout(1);
        tableLayout.setTableWeightX(1.0);
        tableLayout.setTableWeightY(0);
        tableLayout.setTableFill(TableLayout.Fill.BOTH);
        tableLayout.setTablePadding(3, 3);

        final JPanel ioPanel = new JPanel(tableLayout);
        ioPanel.add(createSourceProductPanel());
        ioPanel.add(createOfileJPanel());
        ioPanel.add(targetProductSelector.createDefaultPanel());
        ioPanel.add(tableLayout.createVerticalSpacer());

        addTab(myTabname, ioPanel);

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
        parfileJTextArea.setEditable(true);
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

            scrollTextArea.createHorizontalScrollBar();
            scrollTextArea.setMaximumSize(new Dimension(400, 400));

            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.fill = GridBagConstraints.BOTH;
            c.gridwidth = 2;
            c.weightx = 1;
            c.weighty = 1;
            mainPanel.add(scrollTextArea, c);
        }


        final JPanel finalMainPanel;
        finalMainPanel = SeadasGuiUtils.addPaddedWrapperPanel(mainPanel, 3);


        addTab(myTabname, finalMainPanel);
    }


    private void createSubsampleTab(String tabnameSubsample) {

        final JTabbedPane tabbedPane = new JTabbedPane();

        final JPanel latlonJPanel = new JPanel();
        final JPanel pixelLinesJPanel = new JPanel();

        createLatLonPane(latlonJPanel);
        createPixelsLinesPane(pixelLinesJPanel);


        // Declare mainPanel and set it's attributes
        final JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(""));
        mainPanel.setLayout(new GridBagLayout());


        // Add Swing controls to mainPanel grid cells
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 0;
            c.weighty = 0;
            mainPanel.add(latlonJPanel, c);
        }


        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1;
            c.weighty = 1;
            mainPanel.add(pixelLinesJPanel, c);
        }


        final JPanel paddedMainPanel;
        paddedMainPanel = SeadasGuiUtils.addPaddedWrapperPanel(mainPanel, 6);

        addTab(tabnameSubsample, paddedMainPanel);
    }


    private void createProductTab(String tabnameProductSelector) {

        final JTabbedPane tabbedPane = new JTabbedPane();


        //    createWavelengthsSubTab(tabbedPane, "Wavelength Grouping Tool");
//        createProductSelectorSubTab(tabbedPane, "Product Selector");
        //       createSelectedProductsSubTab(tabbedPane, "Selected Products Cart");
        //       createTreeSubTab(tabbedPane, "Tree");

        //    tabbedPane.setEnabledAt(0, false);
        //  tabbedPane.setEnabledAt(2, false);

        tabbedPane.setSelectedIndex(2);


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
        paddedMainPanel = SeadasGuiUtils.addPaddedWrapperPanel(mainPanel, 6);

        addTab(tabnameProductSelector, paddedMainPanel);


    }


    // private void createSelectedProductsSubTab(JTabbedPane tabbedPane, String myTabname) {
    private void createSelectedProductsSubTab(String myTabname) {

        final String COORDINATES_PANEL_TITLE = "Selected Products";

        productsCartJPanel = new JPanel();
        productsCartJTable = new JTable();

        productsCartJTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedProductsJTableMouseClickedHandler(e);
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        JButton selectedProductsRemoveButton = new JButton("Remove");

        // ----------------------------------------------------------------------------------------
        // Create mainPanel to hold all controls
        // ----------------------------------------------------------------------------------------

        final JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(COORDINATES_PANEL_TITLE));
        mainPanel.setLayout(new GridBagLayout());

        productsCartJPanel.add(new JScrollPane(productsCartJTable));
        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(productsCartJPanel, c);


        selectedProductsRemoveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSelectedProductsDelete();
            }
        });

        //
        //

        c = SeadasGuiUtils.makeConstraints(0, 1);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(selectedProductsRemoveButton, c);


        // ----------------------------------------------------------------------------------------
        // Create wrappedMainPanel to hold mainPanel: this is a formatting wrapper panel
        // ----------------------------------------------------------------------------------------

        final JPanel wrappedMainPanel = SeadasGuiUtils.addPaddedWrapperPanel(
                mainPanel,
                3,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE);


        // ----------------------------------------------------------------------------------------
        // Add wrappedMainPanel to tabbedPane
        // ----------------------------------------------------------------------------------------

        addTab(myTabname, wrappedMainPanel);
    }

    private void createSelectedProductsCartJPanelNew(JPanel inJPanel) {

        final String COORDINATES_PANEL_TITLE = "Selected Products";

        productsCartJPanel = new JPanel();
        productsCartJTable = new JTable();

        productsCartJTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedProductsJTableMouseClickedHandler(e);
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        JButton selectedProductsRemoveButton = new JButton("Remove");

        // ----------------------------------------------------------------------------------------
        // Create mainPanel to hold all controls
        // ----------------------------------------------------------------------------------------

        final JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(COORDINATES_PANEL_TITLE));
        mainPanel.setLayout(new GridBagLayout());

        productsCartJPanel.add(new JScrollPane(productsCartJTable));
        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(productsCartJPanel, c);


        selectedProductsRemoveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSelectedProductsDelete();
            }
        });

        //
        //

        c = SeadasGuiUtils.makeConstraints(0, 1);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(selectedProductsRemoveButton, c);


        // ----------------------------------------------------------------------------------------
        // Create wrappedMainPanel to hold mainPanel: this is a formatting wrapper panel
        // ----------------------------------------------------------------------------------------

        final JPanel wrappedMainPanel = SeadasGuiUtils.addPaddedWrapperPanel(
                mainPanel,
                3,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE);


        // ----------------------------------------------------------------------------------------
        // Add wrappedMainPanel to tabbedPane
        // ----------------------------------------------------------------------------------------

        //tabbedPane.addTab(myTabname, wrappedMainPanel);
        inJPanel.add(mainPanel);
    }


    private JPanel createSelectedProductsCartJPanel() {

        final String COORDINATES_PANEL_TITLE = "Selected Products";

        productsCartJPanel = new JPanel();
        productsCartJTable = new JTable();

        productsCartJTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedProductsJTableMouseClickedHandler(e);
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        JButton selectedProductsRemoveButton = new JButton("Remove");

        // ----------------------------------------------------------------------------------------
        // Create mainPanel to hold all controls
        // ----------------------------------------------------------------------------------------

        final JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(COORDINATES_PANEL_TITLE));
        mainPanel.setLayout(new GridBagLayout());

        productsCartJPanel.add(new JScrollPane(productsCartJTable));
        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(productsCartJPanel, c);


        selectedProductsRemoveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSelectedProductsDelete();
            }
        });

        //
        //

        c = SeadasGuiUtils.makeConstraints(0, 1);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(selectedProductsRemoveButton, c);


        // ----------------------------------------------------------------------------------------
        // Create wrappedMainPanel to hold mainPanel: this is a formatting wrapper panel
        // ----------------------------------------------------------------------------------------

        final JPanel wrappedMainPanel = SeadasGuiUtils.addPaddedWrapperPanel(
                mainPanel,
                3,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE);


        // ----------------------------------------------------------------------------------------
        // Add wrappedMainPanel to tabbedPane
        // ----------------------------------------------------------------------------------------

        // tabbedPane.addTab(myTabname, wrappedMainPanel);
        return wrappedMainPanel;
    }

    private void selectedProductsJTableMouseClickedHandler(MouseEvent e) {
        int row = productsCartJTable.rowAtPoint(e.getPoint());
        int col = productsCartJTable.columnAtPoint(e.getPoint());


        if (col == 0) {
            if (productsCartJTable.getValueAt(row, col).equals(true)) {
                productsCartJTable.setValueAt(new Boolean(false), row, col);
            } else {
                productsCartJTable.setValueAt(new Boolean(true), row, col);
            }
        }

    }


    private JPanel createWavelengthsJPanel() {

        // ----------------------------------------------------------------------------------------
        // Create all Swing controls used on this tabbed panel
        // ----------------------------------------------------------------------------------------


        JTextArea explanationJTextArea = new JTextArea("Here is where we tell you all about this tool Here is where we tell you all about this tool Here is where we tell you all about this tool Here is where we tell you all about this tool Here is where we tell you all about this tool Here is where we tell you all about this tool");
        explanationJTextArea.setEditable(false);
        explanationJTextArea.setLineWrap(true);
        explanationJTextArea.setColumns(50);
        explanationJTextArea.setBackground(Color.decode("#dddddd"));


        wavelengthTypeIiiCheckbox = new JCheckBox(WAVELENGTH_TYPE_INFRARED);
        wavelengthTypeIiiCheckbox.setName(WAVELENGTH_TYPE_INFRARED);
        wavelengthTypeIiiCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (wavelengthCheckboxControlHandlersEnabled) {
                    l2genData.setSelectedWavelengthTypeIii(wavelengthTypeIiiCheckbox.isSelected());
                }
            }
        });


        wavelengthTypeVvvCheckbox = new JCheckBox(WAVELENGTH_TYPE_VISIBLE);
        wavelengthTypeVvvCheckbox.setName(WAVELENGTH_TYPE_VISIBLE);
        wavelengthTypeVvvCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (wavelengthCheckboxControlHandlersEnabled) {
                    l2genData.setSelectedWavelengthTypeVvv(wavelengthTypeVvvCheckbox.isSelected());
                }
            }
        });

        wavelengthsJPanel = new JPanel();
        wavelengthsJPanel.setBorder(BorderFactory.createTitledBorder("Wavelength Limiter"));
        wavelengthsJPanel.setLayout(new GridBagLayout());


        productsLimiterJPanel = new JPanel();
        productsLimiterJPanel.setBorder(BorderFactory.createTitledBorder("Products"));
        productsLimiterJPanel.setLayout(new GridBagLayout());


        // ----------------------------------------------------------------------------------------
        // Create mainPanel to hold all controls
        // ----------------------------------------------------------------------------------------

        final JPanel mainPanel = new JPanel();

        mainPanel.setLayout(new GridBagLayout());

        // Add to mainPanel grid cell

        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(wavelengthsJPanel, c);

//        c = SeadasGuiUtils.makeConstraints(0, 1);
//        c.fill = GridBagConstraints.BOTH;
//        c.anchor = GridBagConstraints.NORTH;
//        c.weightx = 1;
//        c.weighty = 1;
//        mainPanel.add(productsLimiterJPanel, c);


        // ----------------------------------------------------------------------------------------
        // Create wrappedMainPanel to hold mainPanel: this is a formatting wrapper panel
        // ----------------------------------------------------------------------------------------

        JPanel wrappedMainPanel = SeadasGuiUtils.addWrapperPanel(mainPanel);


        // ----------------------------------------------------------------------------------------
        // Add wrappedMainPanel to tabbedPane
        // ----------------------------------------------------------------------------------------

        // tabbedPane.addTab(myTabname, wrappedMainPanel);
        return mainPanel;

    }


    private void createWavelengthsSubTab(JTabbedPane tabbedPane, String myTabname) {

        // ----------------------------------------------------------------------------------------
        // Set all constants for this tabbed pane
        // ----------------------------------------------------------------------------------------


        // ----------------------------------------------------------------------------------------
        // Create all Swing controls used on this tabbed panel
        // ----------------------------------------------------------------------------------------


        JTextArea explanationJTextArea = new JTextArea("Here is where we tell you all about this tool Here is where we tell you all about this tool Here is where we tell you all about this tool Here is where we tell you all about this tool Here is where we tell you all about this tool Here is where we tell you all about this tool");
        explanationJTextArea.setEditable(false);
        explanationJTextArea.setLineWrap(true);
        explanationJTextArea.setColumns(50);
        explanationJTextArea.setBackground(Color.decode("#dddddd"));


        wavelengthTypeIiiCheckbox = new JCheckBox(WAVELENGTH_TYPE_INFRARED);
        wavelengthTypeIiiCheckbox.setName(WAVELENGTH_TYPE_INFRARED);
        wavelengthTypeIiiCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (wavelengthCheckboxControlHandlersEnabled) {
                    l2genData.setSelectedWavelengthTypeIii(wavelengthTypeIiiCheckbox.isSelected());
                }
            }
        });


        wavelengthTypeVvvCheckbox = new JCheckBox(WAVELENGTH_TYPE_VISIBLE);
        wavelengthTypeVvvCheckbox.setName(WAVELENGTH_TYPE_VISIBLE);
        wavelengthTypeVvvCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (wavelengthCheckboxControlHandlersEnabled) {
                    l2genData.setSelectedWavelengthTypeVvv(wavelengthTypeVvvCheckbox.isSelected());
                }
            }
        });

        wavelengthsJPanel = new JPanel();
        wavelengthsJPanel.setBorder(BorderFactory.createTitledBorder("Wavelengths"));
        wavelengthsJPanel.setLayout(new GridBagLayout());


        productsLimiterJPanel = new JPanel();
        productsLimiterJPanel.setBorder(BorderFactory.createTitledBorder("Products"));
        productsLimiterJPanel.setLayout(new GridBagLayout());


        // ----------------------------------------------------------------------------------------
        // Create mainPanel to hold all controls
        // ----------------------------------------------------------------------------------------

        final JPanel mainPanel = new JPanel();

        mainPanel.setLayout(new GridBagLayout());

        // Add to mainPanel grid cell

        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 0;
        c.weighty = 0;
        mainPanel.add(wavelengthsJPanel, c);

        c = SeadasGuiUtils.makeConstraints(0, 1);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(productsLimiterJPanel, c);


        // ----------------------------------------------------------------------------------------
        // Create wrappedMainPanel to hold mainPanel: this is a formatting wrapper panel
        // ----------------------------------------------------------------------------------------

        JPanel wrappedMainPanel = SeadasGuiUtils.addWrapperPanel(mainPanel);


        // ----------------------------------------------------------------------------------------
        // Add wrappedMainPanel to tabbedPane
        // ----------------------------------------------------------------------------------------

        tabbedPane.addTab(myTabname, wrappedMainPanel);
    }

    //  private void createProductSelectorSubTab(JTabbedPane tabbedPane, String myTabname) {
//    private void createProductSelectorSubTab(String myTabname) {
//
//        //   L2genReader l2genReader = new L2genReader(l2genData);
////
////        InputStream stream = L2genForm.class.getResourceAsStream(SEADAS_PRODUCTS_FILE);
////
////        l2genReader.readProductsXmlFile(stream);
//
//        JPanel wavelengthIndependentProductsJPanel = new JPanel();
//        createWaveIndependentProductsJPanel(wavelengthIndependentProductsJPanel);
//
//        JPanel wavelengthDependentProductsJPanel = new JPanel();
//        createWaveDependentProductsJPanel(wavelengthDependentProductsJPanel);
////
////        final JPanel selectedProductsJPanel = new JPanel();
////        createSelectedProductsJPanel(selectedProductsJPanel);
//
//
//        // Declare mainPanel and set it's attributes
//        final JPanel mainPanel = new JPanel();
//        mainPanel.setLayout(new GridBagLayout());
//
//
//        // Add to mainPanel grid cell
//        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
//        c.fill = GridBagConstraints.BOTH;
//        c.anchor = GridBagConstraints.NORTH;
//        c.weightx = 1;
//        c.weighty = 1;
//        mainPanel.add(wavelengthIndependentProductsJPanel, c);
//
//
//        // Add to mainPanel grid cell
//
//        c = SeadasGuiUtils.makeConstraints(1, 0);
//        c.fill = GridBagConstraints.BOTH;
//        c.anchor = GridBagConstraints.NORTH;
//        c.weightx = 1;
//        c.weighty = 1;
//        mainPanel.add(wavelengthDependentProductsJPanel, c);
//
//
//        // Add to mainPanel grid cell
//
////        c = SeadasGuiUtils.makeConstraints(0, 1);
////        c.fill = GridBagConstraints.BOTH;
////        c.anchor = GridBagConstraints.NORTH;
////        c.weightx = 0;
////        c.weighty = 0;
////        c.gridwidth = 2;
////        mainPanel.add(selectedProductsJPanel, c);
//
//
//        final JPanel finalMainPanel = new JPanel();
//        finalMainPanel.setLayout(new GridBagLayout());
//
//        c = SeadasGuiUtils.makeConstraints(0, 0);
//        c.anchor = GridBagConstraints.NORTHWEST;
//        c.insets = new Insets(3, 3, 3, 3);
//        c.fill = GridBagConstraints.BOTH;
//        c.weightx = 1;
//        c.weighty = 1;
//
//        finalMainPanel.add(mainPanel, c);
//
//
////        addTab(myTabname, finalMainPanel);
//
//    }

    private TristateCheckBox.State getCheckboxState(BaseInfo.State state) {
        switch (state) {
            case SELECTED:
                return TristateCheckBox.SELECTED;
            case PARTIAL:
                return TristateCheckBox.PARTIAL;
            default:
                return TristateCheckBox.NOT_SELECTED;
        }
    }

    private BaseInfo.State getInfoState(TristateCheckBox.State state) {
        if (state == TristateCheckBox.SELECTED) {
            return BaseInfo.State.SELECTED;
        }
        if (state == TristateCheckBox.PARTIAL) {
            return BaseInfo.State.PARTIAL;
        }
        return BaseInfo.State.NOT_SELECTED;

    }

    class CheckBoxNodeRenderer implements TreeCellRenderer {
        private JPanel nodeRenderer = new JPanel();
        private JLabel label = new JLabel();
        private TristateCheckBox check = new TristateCheckBox();

        Color selectionBorderColor, selectionForeground, selectionBackground,
                textForeground, textBackground;

        protected TristateCheckBox getJCheckBox() {
            return check;
        }

        public CheckBoxNodeRenderer() {
            Insets inset0 = new Insets(0, 0, 0, 0);
            check.setMargin(inset0);
            nodeRenderer.setLayout(new BorderLayout());
            nodeRenderer.add(check, BorderLayout.WEST);
            nodeRenderer.add(label, BorderLayout.CENTER);

            Font fontValue;
            fontValue = UIManager.getFont("Tree.font");
            if (fontValue != null) {
                check.setFont(fontValue);
                label.setFont(fontValue);
            }
            Boolean booleanValue = (Boolean) UIManager
                    .get("Tree.drawsFocusBorderAroundIcon");
            check.setFocusPainted((booleanValue != null)
                    && (booleanValue.booleanValue()));

            selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
            selectionForeground = UIManager.getColor("Tree.selectionForeground");
            selectionBackground = UIManager.getColor("Tree.selectionBackground");
            textForeground = UIManager.getColor("Tree.textForeground");
            textBackground = UIManager.getColor("Tree.textBackground");
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {

            // System.out.print("getTreeCellRendererComponent (" + Integer.toString(row) + ") ");


            String stringValue = null;
            BaseInfo.State state = BaseInfo.State.NOT_SELECTED;

            if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
                Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                if (userObject instanceof BaseInfo) {
                    BaseInfo info = (BaseInfo) userObject;
                    state = info.getState();
                    stringValue = info.getFullName();


                    tree.setToolTipText(info.getDescription());


                    //  System.out.print(node.getFullName() + " - " + state.toString());


                }
            }


            //System.out.print("\n");


            if (stringValue == null) {
                stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, false);
            }

            label.setText(stringValue);
            check.setState(getCheckboxState(state));
            check.setEnabled(tree.isEnabled());

            if (selected) {
                label.setForeground(selectionForeground);
                check.setForeground(selectionForeground);
                nodeRenderer.setForeground(selectionForeground);
                label.setBackground(selectionBackground);
                check.setBackground(selectionBackground);
                nodeRenderer.setBackground(selectionBackground);
            } else {
                label.setForeground(textForeground);
                check.setForeground(textForeground);
                nodeRenderer.setForeground(textForeground);
                label.setBackground(textBackground);
                check.setBackground(textBackground);
                nodeRenderer.setBackground(textBackground);
            }

            return nodeRenderer;
        }
    }

    class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {

        CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
        JTree tree;
        DefaultMutableTreeNode currentNode;

        public CheckBoxNodeEditor(JTree tree) {
            this.tree = tree;

            // add a listener fo the check box
            ItemListener itemListener = new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    TristateCheckBox.State state = renderer.getJCheckBox().getState();

                    System.out.print("********** listener ***********\n");
                    System.out.print("checkbox - " + renderer.label.getText() + " - " + state.toString() + "\n");
                    //     System.out.print("    val - " + currentValue.getFullName() + " - " + currentValue.getState().toString() + "\n");

                    if (stopCellEditing()) {
                        fireEditingStopped();
                    }
                }
            };
            //renderer.getJCheckBox().addItemListener(itemListener);
            renderer.getJCheckBox().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    System.out.print("--changeListener\n");
                    if (stopCellEditing()) {
                        fireEditingStopped();
                    }
                }
            });
            renderer.getJCheckBox().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.print("--actionListener\n");
                }
            });


        }

        public Object getCellEditorValue() {

            TristateCheckBox.State state = renderer.getJCheckBox().getState();
//
//            System.out.print("-----getCellEditorValue - " + renderer.label.getText() + " - " + state.toString() + "\n");
//            System.out.print("    val - " + currentValue.getFullName() + " - " + currentValue.getState().toString() + "\n");
//            l2genData.setSelectedInfo(currentValue, getInfoState(state));
//            System.out.print("   aval - " + currentValue.getFullName() + " - " + currentValue.getState().toString() + "\n");

            setNodeState(currentNode, getInfoState(state));

            return currentNode.getUserObject();
        }

        public boolean isCellEditable(EventObject event) {

            return true;

        }

        public Component getTreeCellEditorComponent(JTree tree, Object value,
                                                    boolean selected, boolean expanded, boolean leaf, int row) {

            System.out.print("getTreeCellEditorComponent (" + Integer.toString(row) + ") " +
                    ((BaseInfo) ((DefaultMutableTreeNode) value).getUserObject()).getFullName() + "\n");

            if (value instanceof DefaultMutableTreeNode) {
                currentNode = (DefaultMutableTreeNode) value;
            }
            Component editor = renderer.getTreeCellRendererComponent(tree, value,
                    true, expanded, leaf, row, true);

            return editor;
        }
    }


    private TreeNode createTree() {

        DefaultMutableTreeNode product, oldAlgorithm, algorithm = null, wavelength;

        oldAlgorithm = new DefaultMutableTreeNode();
        BaseInfo oldAInfo = null;

        rootNode = new DefaultMutableTreeNode(new BaseInfo());

        for (ProductInfo productInfo : l2genData.getProductInfoArray()) {
            product = new DefaultMutableTreeNode(productInfo);
            for (BaseInfo aInfo : productInfo.getChildren()) {
                algorithm = new DefaultMutableTreeNode(aInfo);

//
//                product.add(algorithm);

                if (algorithm.toString().equals(oldAlgorithm.toString())) {
                    if (oldAInfo.hasChildren()) {
                        if (aInfo.hasChildren()) {
                            algorithm = oldAlgorithm;
                        } else {
                            oldAlgorithm.add(algorithm);
                        }
                    } else {
                        if (aInfo.hasChildren()) {
                            product.remove(oldAlgorithm);
                            algorithm.add(oldAlgorithm);
                            product.add(algorithm);
                        }
                    }
                } else {
                    product.add(algorithm);
                }

                for (BaseInfo wInfo : aInfo.getChildren()) {
                    wavelength = new DefaultMutableTreeNode(wInfo);
                    algorithm.add(wavelength);
                }

                oldAInfo = aInfo;
                oldAlgorithm = algorithm;
            }
            if (productInfo.getChildren().size() == 1) {
                rootNode.add(algorithm);
            } else {
                rootNode.add(product);
            }
        }


        //if (productInfo.getChildren().size() > 0) {


        return rootNode;
    }


    public void checkTreeState(DefaultMutableTreeNode node) {

        l2genData.disableEvent(l2genData.PRODUCT_CHANGED_EVENT);
        BaseInfo info = (BaseInfo) node.getUserObject();
        BaseInfo.State newState = info.getState();

        if (node.getChildCount() > 0) {
            Enumeration<DefaultMutableTreeNode> enumeration = node.children();
            DefaultMutableTreeNode kid;
            boolean selectedFound = false;
            boolean notSelectedFound = false;
            while (enumeration.hasMoreElements()) {
                kid = enumeration.nextElement();
                checkTreeState(kid);

                BaseInfo childInfo = (BaseInfo) kid.getUserObject();

                switch (childInfo.getState()) {
                    case SELECTED:
                        selectedFound = true;
                        break;
                    case PARTIAL:
                        selectedFound = true;
                        notSelectedFound = true;
                        break;
                    case NOT_SELECTED:
                        notSelectedFound = true;
                        break;
                }
            }

            if (selectedFound && !notSelectedFound) {
                newState = BaseInfo.State.SELECTED;
            } else if (!selectedFound && notSelectedFound) {
                newState = BaseInfo.State.NOT_SELECTED;
            } else if (selectedFound && notSelectedFound) {
                newState = BaseInfo.State.PARTIAL;
            }

        } else {
            if (newState == BaseInfo.State.PARTIAL) {
                newState = BaseInfo.State.SELECTED;
                debug("in checkAlgorithmState converted newState to " + newState);
            }
        }

        if (newState != info.getState()) {
            l2genData.setSelectedInfo(info, newState);

        }

        l2genData.enableEvent(l2genData.PRODUCT_CHANGED_EVENT);
    }


    public void setNodeState(DefaultMutableTreeNode node, BaseInfo.State state) {

        debug("setNodeState called with state = " + state);

        if (node == null) {
            return;
        }

        BaseInfo info = (BaseInfo) node.getUserObject();

        if (state == info.getState()) {
            return;
        }

        l2genData.disableEvent(l2genData.PRODUCT_CHANGED_EVENT);

        if (node.getChildCount() > 0) {
            l2genData.setSelectedInfo(info, state);

            Enumeration<DefaultMutableTreeNode> enumeration = node.children();
            DefaultMutableTreeNode childNode;

            BaseInfo.State newState = state;

            while (enumeration.hasMoreElements()) {
                childNode = enumeration.nextElement();

                BaseInfo childInfo = (BaseInfo) childNode.getUserObject();

                if (childInfo instanceof WavelengthInfo) {
                    if (state == BaseInfo.State.PARTIAL) {
                        if (l2genData.compareWavelengthLimiter((WavelengthInfo) childInfo)) {
                            newState = BaseInfo.State.SELECTED;
                        } else {
                            newState = BaseInfo.State.NOT_SELECTED;
                        }
                    }
                }

                setNodeState(childNode, newState);
            }

            DefaultMutableTreeNode ancestorNode;
            DefaultMutableTreeNode targetNode = node;
            ancestorNode = (DefaultMutableTreeNode) node.getParent();

            while (ancestorNode.getParent() != null) {
                targetNode = ancestorNode;
                ancestorNode = (DefaultMutableTreeNode) ancestorNode.getParent();
            }

            checkTreeState(targetNode);

        } else {
            if (state == BaseInfo.State.PARTIAL) {
                l2genData.setSelectedInfo(info, BaseInfo.State.SELECTED);
            } else {
                l2genData.setSelectedInfo(info, state);
            }
        }

        l2genData.enableEvent(l2genData.PRODUCT_CHANGED_EVENT);
    }


    private void updateProductTreePanel() {

        TreeNode rootNode = createTree();
        productJTree.setModel(new DefaultTreeModel(rootNode, false));

    }


    //    private void createTreeSubTab(JTabbedPane tabbedPane, String myTabname) {
    private void createTreeSubTab(String myTabname) {


        InputStream stream = L2genForm.class.getResourceAsStream(SEADAS_PRODUCTS_FILE);

        l2genReader.readProductsXmlFile(stream);


        JPanel wavelengthsLimitorJPanel = createWavelengthsJPanel();
        //    JPanel selectedProductsCartJPanel = new JPanel();
        //   createSelectedProductsCartJPanelNew(selectedProductsCartJPanel);

        TreeNode rootNode = createTree();
        productJTree = new JTree(rootNode);

        CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
        productJTree.setCellRenderer(renderer);

        productJTree.setCellEditor(new CheckBoxNodeEditor(productJTree));
        productJTree.setEditable(true);

        productJTree.setRootVisible(false);


        final JPanel selectedProductsJPanel = new JPanel();
        createSelectedProductsJPanel(selectedProductsJPanel);


        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints c;


        JPanel treeBorderJPanel = new JPanel();
        treeBorderJPanel.setBorder(BorderFactory.createTitledBorder("Product Selector"));
        treeBorderJPanel.setLayout(new GridBagLayout());

        c = SeadasGuiUtils.makeConstraints(0, 0);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        treeBorderJPanel.add(new JScrollPane(productJTree), c);

        c = SeadasGuiUtils.makeConstraints(0, 0);
        c.anchor = GridBagConstraints.NORTHWEST;
        // c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
//        mainPanel.add(new JScrollPane(treeTable), c);
        mainPanel.add(treeBorderJPanel, c);


        c = SeadasGuiUtils.makeConstraints(1, 0);
        c.anchor = GridBagConstraints.NORTHEAST;
        // c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
//        mainPanel.add(new JScrollPane(treeTable), c);
        mainPanel.add(wavelengthsLimitorJPanel, c);


        // Add to mainPanel grid cell

        c = SeadasGuiUtils.makeConstraints(0, 1);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 2;
        mainPanel.add(selectedProductsJPanel, c);


//        c = SeadasGuiUtils.makeConstraints(1, 0);
//        c.anchor = GridBagConstraints.NORTHEAST;
//        // c.insets = new Insets(3, 3, 3, 3);
//        c.fill = GridBagConstraints.BOTH;
//        c.weightx = 1;
//        c.weighty = 1;
////        mainPanel.add(new JScrollPane(treeTable), c);
//        mainPanel.add(selectedProductsCartJPanel, c);
//


        addTab(myTabname, mainPanel);

    }


    //----------------------------------------------------------------------------------------
    // Methods involving Panels
    //----------------------------------------------------------------------------------------

    private JPanel createSourceProductPanel() {
        final JPanel panel = sourceProductSelector.createDefaultPanel();

        sourceProductSelector.getProductNameLabel().setText("Name:");
        sourceProductSelector.getProductNameComboBox().setPrototypeDisplayValue(
                "MER_RR__1PPBCM20030730_071000_000003972018_00321_07389_0000.N1");

        sourceProductSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent event) {
                Product sourceProduct = getSourceProduct();

                if (handleIfileJComboBoxEnabled) {
                    //   updateTargetProductName(sourceProduct);
                }


                if (sourceProduct != null && sourceProductSelector.getSelectedProduct() != null
                        && sourceProductSelector.getSelectedProduct().getFileLocation() != null) {
                    if (handleIfileJComboBoxEnabled) {
                        //   l2genData.setParamValue(l2genData.IFILE, sourceProductSelector.getSelectedProduct().getName());
                        l2genData.setParamValue(l2genData.IFILE, sourceProductSelector.getSelectedProduct().getFileLocation().toString());
                    }
                }
            }
        });
        return panel;
    }


    private void createLatLonPane(JPanel inPanel) {

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

        //     final JPanel inPanel = new JPanel();
        inPanel.setBorder(BorderFactory.createTitledBorder(COORDINATES_PANEL_TITLE));
        inPanel.setLayout(new GridBagLayout());

        inPanel.add(northJTextField,
                SeadasGuiUtils.makeConstraints(2, 1, GridBagConstraints.NORTH));

        inPanel.add(southJTextField,
                SeadasGuiUtils.makeConstraints(2, 3, GridBagConstraints.SOUTH));

        inPanel.add(eastJTextField,
                SeadasGuiUtils.makeConstraints(3, 2, GridBagConstraints.EAST));

        inPanel.add(westJTextField,
                SeadasGuiUtils.makeConstraints(1, 2, GridBagConstraints.WEST));

        inPanel.add(northLabel,
                SeadasGuiUtils.makeConstraints(2, 0, GridBagConstraints.SOUTH));

        inPanel.add(southLabel,
                SeadasGuiUtils.makeConstraints(2, 4, GridBagConstraints.NORTH));

        inPanel.add(eastLabel,
                SeadasGuiUtils.makeConstraints(4, 2, GridBagConstraints.WEST));

        inPanel.add(westLabel,
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

        wrappedMainPanel.add(inPanel, c);


        // ----------------------------------------------------------------------------------------
        // Add wrappedMainPanel to tabbedPane
        // ----------------------------------------------------------------------------------------

    }


    private void createPixelsLinesPane(JPanel mainPanel) {

        // ---------------------------------------------------------------------------------------- 
        // Set all constants for this tabbed pane
        // ---------------------------------------------------------------------------------------- 

        final int PIXELS_JTEXTFIELD_LENGTH = 5;
        final int LINES_JTEXTFIELD_LENGTH = 5;

        final String PIXELS_PANEL_TITLE = "";
        final String LINES_PANEL_TITLE = "";

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
        //     pixelsPanel.setBorder(BorderFactory.createTitledBorder(PIXELS_PANEL_TITLE));
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
        //   linesPanel.setBorder(BorderFactory.createTitledBorder(LINES_PANEL_TITLE));
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


        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Pixel-Lines"));

        GridBagConstraints constraints = SeadasGuiUtils.makeConstraints(0, 0);
        constraints.insets = new Insets(3, 3, 3, 3);
        mainPanel.add(pixelsPanel, constraints);

        constraints = SeadasGuiUtils.makeConstraints(1, 0);
        constraints.insets = new Insets(3, 3, 3, 3);
        mainPanel.add(linesPanel, constraints);


        // ---------------------------------------------------------------------------------------- 
        // Create wrappedMainPanel to hold mainPanel: this is a formatting wrapper panel
        // ---------------------------------------------------------------------------------------- 

        final JPanel wrappedMainPanel = new JPanel();
        wrappedMainPanel.setLayout(new GridBagLayout());

        constraints = SeadasGuiUtils.makeConstraints(0, 0);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(3, 3, 3, 3);
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 1;
        constraints.weighty = 1;

        wrappedMainPanel.add(mainPanel, constraints);


        // ---------------------------------------------------------------------------------------- 
        // Add wrappedMainPanel to tabbedPane
        // ---------------------------------------------------------------------------------------- 


    }

//    private void createWaveIndependentProductsJPanel(JPanel waveIndependentProductsJPanel) {
//
//        createWaveIndependentProductsJList();
//
//        JScrollPane waveIndependentProductsJScrollPane = new JScrollPane(waveIndependentProductsJList);
//        waveIndependentProductsJScrollPane.setMinimumSize(new Dimension(400, 400));
//        waveIndependentProductsJScrollPane.setMaximumSize(new Dimension(400, 400));
//        waveIndependentProductsJScrollPane.setPreferredSize(new Dimension(400, 400));
//
//        waveIndependentProductsJPanel.setBorder(BorderFactory.createTitledBorder(WAVE_INDEPENDENT_PRODUCTS_JPANEL_TITLE));
//        waveIndependentProductsJPanel.setLayout(new GridBagLayout());
//
//        // Add to waveIndependentProductsJPanel grid cell
//        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
//        c.fill = GridBagConstraints.BOTH;
//        c.weightx = 1;
//        c.weighty = 1;
//        waveIndependentProductsJPanel.add(waveIndependentProductsJScrollPane, c);
//    }

//    private void createWaveDependentProductsJPanel(JPanel waveDependentProductsJPanel) {
//
//        createWaveDependentProductsJList();
//
//        JScrollPane waveDependentProductsJScrollPane = new JScrollPane(waveDependentProductsJList);
//        waveDependentProductsJScrollPane.setMinimumSize(new Dimension(400, 400));
//        waveDependentProductsJScrollPane.setMaximumSize(new Dimension(400, 400));
//        waveDependentProductsJScrollPane.setPreferredSize(new Dimension(400, 400));
//
//        waveDependentProductsJPanel.setBorder(BorderFactory.createTitledBorder(WAVE_DEPENDENT_PRODUCTS_JPANEL_TITLE));
//        waveDependentProductsJPanel.setLayout(new GridBagLayout());
//
//        // Add to waveDependentProductsJPanel grid cell
//        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
//        c.fill = GridBagConstraints.BOTH;
//        c.weightx = 1;
//        c.weighty = 1;
//        waveDependentProductsJPanel.add(waveDependentProductsJScrollPane, c);
//    }

    private void createSelectedProductsJPanel(JPanel selectedProductsPanel) {


        selectedProductsPanel.setBorder(BorderFactory.createTitledBorder("Selected Products"));
        selectedProductsPanel.setLayout(new GridBagLayout());

        selectedProductsDefaultsButton = new JButton("Apply Defaults");
        selectedProductsEditLoadButton = new JButton();
        selectedProductsCancelButton = new JButton("Cancel");


        selectedProductsJTextArea = new JTextArea(SELECTED_PRODUCTS_JTEXT_AREA_DEFAULT);
        selectedProductsJTextArea.setLineWrap(true);
        selectedProductsJTextArea.setWrapStyleWord(true);
        selectedProductsJTextArea.setColumns(20);
        selectedProductsJTextArea.setRows(5);

        setDisplayModeSelectedProducts(DisplayMode.STANDARD_MODE);

        JPanel panel1 = SeadasGuiUtils.addPaddedWrapperPanel(selectedProductsDefaultsButton, 3, GridBagConstraints.WEST);
        JPanel panel2 = SeadasGuiUtils.addPaddedWrapperPanel(selectedProductsEditLoadButton, 3, GridBagConstraints.CENTER);
        JPanel panel3 = SeadasGuiUtils.addPaddedWrapperPanel(selectedProductsCancelButton, 3, GridBagConstraints.EAST);


        selectedProductsDefaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.applyProductDefaults();
            }
        });


        selectedProductsEditLoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (EDIT_LOAD_BUTTON_TEXT_EDIT_MODE.equals(selectedProductsEditLoadButton.getText())) {
                    l2genData.setParamValue(l2genData.PROD, selectedProductsJTextArea.getText());
                    selectedProductsJTextArea.setText(l2genData.getProd());

                    setDisplayModeSelectedProducts(DisplayMode.STANDARD_MODE);

                } else {
                    setDisplayModeSelectedProducts(DisplayMode.EDIT_MODE);
                }
            }
        });


        selectedProductsCancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedProductsJTextArea.setText(l2genData.getProd());

                setDisplayModeSelectedProducts(DisplayMode.STANDARD_MODE);
            }
        });


        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = 3;
        selectedProductsPanel.add(selectedProductsJTextArea, c);

//        panelTest = SeadasGuiUtils.addWrapperPanel(jtable, 3);
//
//        c = SeadasGuiUtils.makeConstraints(0, 1);
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.weightx = 1;
//        selectedProductsPanel.add(panelTest, c);

        c = SeadasGuiUtils.makeConstraints(0, 1);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        selectedProductsPanel.add(panel1, c);

        c = SeadasGuiUtils.makeConstraints(1, 1);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        selectedProductsPanel.add(panel2, c);

        c = SeadasGuiUtils.makeConstraints(2, 1);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        selectedProductsPanel.add(panel3, c);
    }

    private JPanel createOfileJPanel() {

        JPanel selectedProductsPanel = new JPanel();
        selectedProductsPanel.setBorder(BorderFactory.createTitledBorder("Output File"));
        selectedProductsPanel.setLayout(new GridBagLayout());

        ofileJComboBox = new JComboBox();
// todo add listener to this

        GridBagConstraints c = SeadasGuiUtils.makeConstraints(0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        selectedProductsPanel.add(ofileJComboBox, c);

        return selectedProductsPanel;
    }


    //----------------------------------------------------------------------------------------
    // Methods involved with the Parfile Tab
    //----------------------------------------------------------------------------------------

    public void loadParfileEntry() {
        System.out.println(parfileTextEntryName.getText() + "=" + parfileTextEntryValue.getText());
        l2genData.setParamValue(parfileTextEntryName.getText(), parfileTextEntryValue.getText());
        System.out.println("ifile=" + l2genData.getParamValue(l2genData.IFILE));
    }


    //----------------------------------------------------------------------------------------
    // Methods involved with the Product Tab
    //----------------------------------------------------------------------------------------


//    private void createWaveIndependentProductsJList() {
//
//        waveIndependentProductsJList = new JList();
//
//        setWaveIndependentProductsJList();
//
//        // add listener to control
//        waveIndependentProductsJList.addListSelectionListener(new ListSelectionListener() {
//            @Override
//            public void valueChanged(ListSelectionEvent e) {
//                if (handleWaveIndependentProductsJListEnabled) {
//                    handleWaveIndependentProductsJList();
//                }
//            }
//        });
//    }


//    private void setWaveIndependentProductsJList() {
//        // Create arrayList for all the algorithmInfo objects
//        ArrayList<AlgorithmInfo> algorithmInfoArrayList = new ArrayList<AlgorithmInfo>();
//
//        for (ProductInfo productInfo : l2genData.getWaveIndependentProductInfoArray()) {
//            for (BaseInfo algorithmInfo : productInfo.getChildren()) {
//                ((AlgorithmInfo) algorithmInfo).setToStringShowProductName(true);
//                algorithmInfoArrayList.add((AlgorithmInfo) algorithmInfo);
//            }
//        }
//
//        // Store the arrayList into an array which can then be fed into a JList control
//        AlgorithmInfo[] algorithmInfoArray = new AlgorithmInfo[algorithmInfoArrayList.size()];
//        algorithmInfoArrayList.toArray(algorithmInfoArray);
//
//        // format the JList control
//        waveIndependentProductsJList.setListData(algorithmInfoArray);
//
//        //     setSelectionStatesWaveIndependentProductsJList();
//    }


//    private void createWaveDependentProductsJList() {
//
//        waveDependentProductsJList = new JList();
//
//        setWaveDependentProductsJList();
//
//        // add listener to control
//        waveDependentProductsJList.addListSelectionListener(new ListSelectionListener() {
//            @Override
//
//            public void valueChanged(ListSelectionEvent e) {
//                if (handleWaveDependentProductsJListEnabled) {
//                    handleWaveDependentProductsJList();
//                }
//            }
//        });
//    }

//
//    private void setWaveDependentProductsJList() {
//        // Create arrayList for all the wavelengthInfo objects
//        ArrayList<WavelengthInfo> wavelengthInfoArrayList = new ArrayList<WavelengthInfo>();
//
//        for (ProductInfo productInfo : l2genData.getWaveDependentProductInfoArray()) {
//            for (BaseInfo algorithmInfo : productInfo.getChildren()) {
//                for (BaseInfo wInfo : algorithmInfo.getChildren()) {
//                    WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;
//                    wavelengthInfo.setToStringShowProductName(true);
//                    for (WavelengthInfo wavelengthInfoLimiter : l2genData.getWavelengthLimiterArray()) {
//                        if (wavelengthInfo.getWavelength() == wavelengthInfoLimiter.getWavelength()) {
//                            if (wavelengthInfoLimiter.isSelected()) {
//
//                                wavelengthInfoArrayList.add(wavelengthInfo);
//                            }
//                        }
//                    }
//
//                }
//            }
//        }
//
//
//        // Store the arrayList into an array which can then be fed into a JList control
//        WavelengthInfo[] wavelengthInfoArray = new WavelengthInfo[wavelengthInfoArrayList.size()];
//        wavelengthInfoArrayList.toArray(wavelengthInfoArray);
//
//        // format the JList control
//        waveDependentProductsJList.setListData(wavelengthInfoArray);
//
//        //    setSelectionStatesWaveDependentProductsJList();
//    }


    private void setDisplayModeSelectedProducts(DisplayMode displayMode) {

        if (displayMode == DisplayMode.STANDARD_MODE) {
            selectedProductsJTextArea.setEditable(false);
            selectedProductsJTextArea.setBackground(Color.decode("#dddddd"));
//
//            waveIndependentProductsJList.setEnabled(true);
//            waveDependentProductsJList.setEnabled(true);

            selectedProductsEditLoadButton.setText(EDIT_LOAD_BUTTON_TEXT_STANDARD_MODE);
            selectedProductsDefaultsButton.setEnabled(true);
            selectedProductsCancelButton.setVisible(false);
        } else if (displayMode == DisplayMode.EDIT_MODE) {
            selectedProductsJTextArea.setEditable(true);
            selectedProductsJTextArea.setBackground(Color.decode("#ffffff"));

//            waveDependentProductsJList.clearSelection();
//            waveIndependentProductsJList.clearSelection();
//            waveDependentProductsJList.setEnabled(false);
//            waveIndependentProductsJList.setEnabled(false);

            selectedProductsEditLoadButton.setText(EDIT_LOAD_BUTTON_TEXT_EDIT_MODE);
            selectedProductsDefaultsButton.setEnabled(false);
            selectedProductsCancelButton.setVisible(true);
        }
    }


    class SelectedProductsTableModel extends AbstractTableModel {


        String[] columnNames = {"Selected", "Product"};
        Object[][] data = null;


        public SelectedProductsTableModel(ArrayList<Object> selectedProductObjects) {

            if (selectedProductObjects != null) {
                data = new Object[selectedProductObjects.size()][2];

                int idx = 0;
                for (Object selectedProductObject : selectedProductObjects) {
                    data[idx][0] = new Boolean(false);
                    data[idx][1] = selectedProductObject;

                    idx++;
                }
            } else {
                data = new Object[1][2];
                data[0][0] = " ";
                data[0][1] = "No Products Selected";
            }
        }


        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return (String) columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }


        /*
        * JTable uses this method to determine the default renderer/
        * editor for each cell.  If we didn't implement this method,
        * then the last column would contain text ("true"/"false"),
        * rather than a check box.
        */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
        * Don't need to implement this method unless your table's
        * editable.
        */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 2) {
                return false;
            } else {
                return true;
            }
        }

        /*
        * Don't need to implement this method unless your table's
        * data can change.
        */
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }

    }


    //----------------------------------------------------------------------------------------
    // Swing Control Handlers
    //----------------------------------------------------------------------------------------

    private void spixlLostFocus() {
        l2genData.setParamValue(l2genData.SPIXL, spixlJTextField.getText().toString());
    }

    private void epixlLostFocus() {
        l2genData.setParamValue(l2genData.EPIXL, epixlJTextField.getText().toString());
    }

    private void dpixlLostFocus() {
        l2genData.setParamValue(l2genData.DPIXL, dpixlJTextField.getText().toString());
    }

    private void slineLostFocus() {
        l2genData.setParamValue(l2genData.SLINE, slineJTextField.getText().toString());
    }

    private void elineLostFocus() {
        l2genData.setParamValue(l2genData.ELINE, elineJTextField.getText().toString());
    }

    private void dlineLostFocus() {
        l2genData.setParamValue(l2genData.DLINE, dlineJTextField.getText().toString());
    }

    private void northLostFocus() {
        l2genData.setParamValue(l2genData.NORTH, northJTextField.getText().toString());
    }

    private void southLostFocus() {
        l2genData.setParamValue(l2genData.SOUTH, southJTextField.getText().toString());
    }

    private void westLostFocus() {
        l2genData.setParamValue(l2genData.WEST, westJTextField.getText().toString());
    }

    private void eastLostFocus() {
        l2genData.setParamValue(l2genData.EAST, eastJTextField.getText().toString());
    }

    private void parfileLostFocus() {
        l2genData.setParfile(parfileJTextArea.getText().toString());
    }


//    private void handleWaveIndependentProductsJList() {
//
//        l2genData.disableEvent(l2genData.WAVE_INDEPENDENT_PRODUCT_CHANGED);
//
//        Object[] selectedElements = waveIndependentProductsJList.getSelectedValues();
//
//        for (Object object : selectedElements) {
//            AlgorithmInfo algorithmInfo = (AlgorithmInfo) object;
//
//            if (!algorithmInfo.isSelected()) {
//                l2genData.setSelectedWaveIndependentProduct(algorithmInfo, true);
//            }
//        }
//
//        waveIndependentProductsJList.clearSelection();
//        l2genData.enableEvent(l2genData.WAVE_INDEPENDENT_PRODUCT_CHANGED);
//    }


//    private void handleWaveDependentProductsJList() {
//
//        l2genData.disableEvent(l2genData.WAVE_DEPENDENT_PRODUCT_CHANGED);
//
//        Object[] selectedElements = waveDependentProductsJList.getSelectedValues();
//
//        for (Object object : selectedElements) {
//            WavelengthInfo wavelengthInfo = (WavelengthInfo) object;
//            if (!wavelengthInfo.isSelected()) {
//                l2genData.setSelectedWaveDependentProduct(wavelengthInfo, true);
//            }
//        }
//
////        waveDependentProductsJList.clearSelection();
//        l2genData.enableEvent(l2genData.WAVE_DEPENDENT_PRODUCT_CHANGED);
//    }

    private void handleSelectedProductsDelete() {

        ArrayList<Object> productsToDeleteArrayList = new ArrayList<Object>();


        for (int row = 0; row < productsCartJTable.getRowCount(); row++) {

            if (productsCartJTable.getValueAt(row, 0).equals(true)) {

                Object product = productsCartJTable.getValueAt(row, 1);

                productsToDeleteArrayList.add(product);

            }
        }


        for (Object productToDelete : productsToDeleteArrayList) {
            if (productToDelete instanceof AlgorithmInfo) {
                l2genData.setSelectedInfo((AlgorithmInfo) productToDelete, BaseInfo.State.NOT_SELECTED);
            } else if (productToDelete instanceof WavelengthInfo) {
                l2genData.setSelectedInfo((WavelengthInfo) productToDelete, BaseInfo.State.NOT_SELECTED);
            }
        }
    }


    private void handleSelectedProductsJTable() {

        final int CHECKBOX_COLUMN_INDEX = 0;

        productsCartJTable.clearSelection();

        if (productsCartJTable.getSelectedRowCount() > 0) {
            if (productsCartJTable.getSelectedColumn() == CHECKBOX_COLUMN_INDEX) {
                Object checkboxValue = productsCartJTable.getValueAt(productsCartJTable.getSelectedRow(),
                        productsCartJTable.getSelectedColumn());

                if (checkboxValue.equals(true)) {
                    productsCartJTable.setValueAt(false,
                            productsCartJTable.getSelectedRow(),
                            productsCartJTable.getSelectedColumn());
                } else {
                    productsCartJTable.setValueAt(true,
                            productsCartJTable.getSelectedRow(),
                            productsCartJTable.getSelectedColumn());
                }


            }
        }
    }

    public void uploadParfile() {

        final ArrayList<String> parfileTextLines = myReadDataFile(parfileChooser.getSelectedFile().toString());

        StringBuilder parfileText = new StringBuilder();

        for (String currLine : parfileTextLines) {
            parfileText.append(currLine);
            parfileText.append("\n");
        }

        l2genData.setParfile(parfileText.toString());
        parfileJTextArea.setEditable(true);
        parfileJTextArea.setEditable(false);
        //  parfileJTextArea.setText(parfileText.toString());
    }

    public void writeParfile() {

        try {
            // Create file
            FileWriter fstream = new FileWriter(parfileChooser.getSelectedFile().toString());
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(l2genData.getParfile());
            //Close the output stream
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }


    //----------------------------------------------------------------------------------------
    // Listeners and L2genData Handlers
    //----------------------------------------------------------------------------------------

    private void addL2genDataListeners() {

        l2genData.addPropertyChangeListener(l2genData.MISSION_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println(l2genData.MISSION_CHANGE_EVENT + " being handled");
                missionStringChangeEvent((String) evt.getNewValue());

            }
        });


        l2genData.addPropertyChangeListener(l2genData.SPIXL, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                spixlJTextField.setText(l2genData.getParamValue(l2genData.SPIXL));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });

        l2genData.addPropertyChangeListener(l2genData.EPIXL, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                epixlJTextField.setText(l2genData.getParamValue(l2genData.EPIXL));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });

        l2genData.addPropertyChangeListener(l2genData.DPIXL, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                dpixlJTextField.setText(l2genData.getParamValue(l2genData.DPIXL));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });

        l2genData.addPropertyChangeListener(l2genData.SLINE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                slineJTextField.setText(l2genData.getParamValue(l2genData.SLINE));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });

        l2genData.addPropertyChangeListener(l2genData.ELINE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                elineJTextField.setText(l2genData.getParamValue(l2genData.ELINE));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });

        l2genData.addPropertyChangeListener(l2genData.DLINE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                dlineJTextField.setText(l2genData.getParamValue(l2genData.DLINE));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.NORTH, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                northJTextField.setText(l2genData.getParamValue(l2genData.NORTH));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.SOUTH, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                southJTextField.setText(l2genData.getParamValue(l2genData.SOUTH));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.WEST, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                westJTextField.setText(l2genData.getParamValue(l2genData.WEST));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.EAST, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                eastJTextField.setText(l2genData.getParamValue(l2genData.EAST));
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.PARFILE_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("receiving PARFILE_TEXT_CHANGE_EVENT_NAME");
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.WAVELENGTH_LIMITER_CHANGE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("WAVELENGTH_LIMITER_CHANGE_EVENT fired");
                updateWavelengthCheckboxSelectionStateEvent();
                // setWaveDependentProductsJList();
            }
        });


        l2genData.addPropertyChangeListener(l2genData.PRODUCT_CHANGED_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                debug("productChangedHandler() being called");
                productChangedHandler();

            }
        });


        l2genData.addPropertyChangeListener(l2genData.IFILE, new PropertyChangeListener() {
            @Override

            public void propertyChange(PropertyChangeEvent evt) {
                //   if (enableIfileEvent) {
                ifileChangedEventHandler();
                //   }
            }

        });

//        l2genData.addPropertyChangeListener(l2genData.PROD, new PropertyChangeListener() {
//            @Override
//
//            public void propertyChange(PropertyChangeEvent evt) {
//                debug("RECEIVING PROD EVENT FIRE");
//                productChangedHandler();
//            }
//
//        });


        l2genData.addPropertyChangeListener(l2genData.DEFAULTS_CHANGED_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                parfileJTextArea.setText(l2genData.getParfile());
            }
        });


        l2genData.addPropertyChangeListener(l2genData.OFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                debug("EVENT RECEIVED ofile");
                debug(l2genData.getParamValue("ofile=" + l2genData.OFILE));
                parfileJTextArea.setText(l2genData.getParfile());
                updateOfileHandler();
            }
        });
    }


    private void updateWavelengthLimiterPanel() {

        wavelengthsJCheckboxArrayList = new ArrayList<JCheckBox>();

        wavelengthsJPanel.removeAll();


        // clear this because we dynamically rebuild it when input file selection is made or changed
        wavelengthsJCheckboxArrayList.clear();


        ArrayList<JCheckBox> wavelengthGroupCheckboxes = new ArrayList<JCheckBox>();


        for (WavelengthInfo wavelengthInfo : l2genData.getWavelengthLimiterArray()) {

            final String currWavelength = wavelengthInfo.getWavelengthString();
            final JCheckBox currJCheckBox = new JCheckBox(currWavelength);

            currJCheckBox.setName(currWavelength);

            // add current JCheckBox to the externally accessible arrayList
            wavelengthsJCheckboxArrayList.add(currJCheckBox);


            // add listener for current checkbox
            currJCheckBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (wavelengthCheckboxControlHandlersEnabled) {
                        l2genData.setSelectedWavelengthLimiterArray(currWavelength, currJCheckBox.isSelected());
                    }
                }
            });

            wavelengthGroupCheckboxes.add(currJCheckBox);
        }

        if (l2genData.hasWavelengthLimiterTypeIii()) {
            wavelengthGroupCheckboxes.add(wavelengthTypeIiiCheckbox);
            wavelengthTypeIiiCheckbox.setSelected(true);
            l2genData.setSelectedWavelengthTypeIii(wavelengthTypeIiiCheckbox.isSelected());
        }

        if (l2genData.hasWavelengthLimiterTypeVvv()) {
            wavelengthGroupCheckboxes.add(wavelengthTypeVvvCheckbox);
            wavelengthTypeVvvCheckbox.setSelected(true);
            l2genData.setSelectedWavelengthTypeVvv(wavelengthTypeVvvCheckbox.isSelected());
        }


        // some GridBagLayout formatting variables
        int gridyCnt = 0;
        int gridxCnt = 0;
        int NUMBER_OF_COLUMNS = 1;


        for (JCheckBox wavelengthGroupCheckbox : wavelengthGroupCheckboxes) {
            // add current JCheckBox to the panel
            {
                final GridBagConstraints c = new GridBagConstraints();
                c.gridx = gridxCnt;
                c.gridy = gridyCnt;
                c.fill = GridBagConstraints.NONE;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.weightx = 1;
                wavelengthsJPanel.add(wavelengthGroupCheckbox, c);
            }

            // increment GridBag coordinates
            if (gridxCnt < (NUMBER_OF_COLUMNS - 1)) {
                gridxCnt++;
            } else {
                gridxCnt = 0;
                gridyCnt++;
            }

        }

        // updateWavelengthCheckboxSelectionStateEvent();
    }

    private void ifileChangedEventHandler() {

        if (sourceProductSelector != null) {
            boolean setSourceProductSelector = false;

            if (l2genData.getParamValue(l2genData.IFILE) != null &&
                    sourceProductSelector.getSelectedProduct() != null &&
                    sourceProductSelector.getSelectedProduct().getFileLocation() != null) {
                if (!l2genData.getParamValue(l2genData.IFILE).equals(sourceProductSelector.getSelectedProduct().getFileLocation().toString())) {
                    setSourceProductSelector = true;
                }
            } else {
                setSourceProductSelector = true;
            }

            if (setSourceProductSelector == true) {
                handleIfileJComboBoxEnabled = false;
                sourceProductSelector.setSelectedProduct(null);

//                final TargetProductSelectorModel selectorModel = targetProductSelector.getModel();
//                selectorModel.setProductName(null);

                handleIfileJComboBoxEnabled = true;
            }
        }
    }

    private void missionStringChangeEvent(String newMissionString) {

        ifileChangedEventHandler();

        this.setEnabledAt(PRODUCT_SELECTOR_TAB_INDEX, true);
        this.setEnabledAt(SUB_SAMPLE_TAB_INDEX, true);
        //       createProductSelectorWavelengthsPanel();

        updateWavelengthLimiterPanel();
        l2genData.applyParfileDefaults();
        updateProductTreePanel();

        // setWaveDependentProductsJList();
        updateWavelengthCheckboxSelectionStateEvent();


        parfileJTextArea.setText(l2genData.getParfile());
    }


    private void updateWavelengthCheckboxSelectionStateEvent() {

        wavelengthCheckboxControlHandlersEnabled = false;

        for (WavelengthInfo wavelengthInfo : l2genData.getWavelengthLimiterArray()) {
            for (JCheckBox currJCheckbox : wavelengthsJCheckboxArrayList) {
                //  debug("wave=" + wavelengthInfo.getWavelengthString() + " getName=" + currJCheckbox.getName());
                if (wavelengthInfo.getWavelengthString().equals(currJCheckbox.getName())) {
                    if (wavelengthInfo.isSelected() != currJCheckbox.isSelected()) {
                        currJCheckbox.setSelected(wavelengthInfo.isSelected());
                    }
                }
            }
        }

        if (l2genData.hasWavelengthLimiterTypeIii()) {
            if (wavelengthTypeIiiCheckbox.isSelected() != l2genData.isSelectedWavelengthLimiterTypeIii()) {
                wavelengthTypeIiiCheckbox.setSelected(l2genData.isSelectedWavelengthLimiterTypeIii());
            }
        }

        if (l2genData.hasWavelengthLimiterTypeVvv()) {
            if (wavelengthTypeVvvCheckbox.isSelected() != l2genData.isSelectedWavelengthLimiterTypeVvv()) {
                wavelengthTypeVvvCheckbox.setSelected(l2genData.isSelectedWavelengthLimiterTypeVvv());
            }
        }

        wavelengthCheckboxControlHandlersEnabled = true;
    }


    private void productChangedHandler() {

        selectedProductsJTextArea.setText(l2genData.getProd());
        parfileJTextArea.setText(l2genData.getParfile());

        if (l2genData.getSelectedProducts().size() > 0) {
            System.out.println("make new selectedProductsTableModel with data");
            productsCartTableModel = new SelectedProductsTableModel(l2genData.getSelectedProducts());

            productsCartJTable.setModel(productsCartTableModel);

        } else {
            System.out.println("make new selectedProductsTableModel WITH NO data");
            productsCartTableModel = new SelectedProductsTableModel(null);
            productsCartJTable.setModel(productsCartTableModel);
        }

        productsCartJTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        productsCartJTable.getColumnModel().getColumn(1).setPreferredWidth(200);


        productsCartJTable.setRowSelectionAllowed(false);
        productsCartJTable.setColumnSelectionAllowed(false);
        productsCartJTable.setCellSelectionEnabled(false);
        productsCartJPanel.add(new JScrollPane(productsCartJTable));


        // just reload the model to update the tree

        productJTree.treeDidChange();
    }


    private void updateOfileHandler() {

        //todo
    }


    //----------------------------------------------------------------------------------------
    // Some Generic stuff
    //----------------------------------------------------------------------------------------

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


    //----------------------------------------------------------------------------------------
    // Miscellaneous
    //----------------------------------------------------------------------------------------

    Product getSourceProduct() {
        return sourceProductSelector.getSelectedProduct();
    }


    void prepareShow() {
        sourceProductSelector.initProducts();
    }

    void prepareHide() {
        sourceProductSelector.releaseProducts();
    }


    private void debug(String string) {
        System.out.println(string);
    }


//    private void updateSelectedProductsJTextAre() {
//
//        final StringBuilder selectedProductListStringBuilder = new StringBuilder("");
//        final StringBuilder currentSelectedProductStringBuilder = new StringBuilder("");
//        final String PRODUCT_LIST_DELIMITER = " ";
//
//        if (wavelengthsJCheckboxArrayList != null) {
//            for (final JCheckBox currWavelengthCheckBox : wavelengthsJCheckboxArrayList) {
//
//                if (currWavelengthCheckBox.isSelected()) {
//
//                    String selectedWavelengthString = currWavelengthCheckBox.getName().toString();
//
//                    int wavelengthInteger = Integer.parseInt(selectedWavelengthString);
//
//                    Object values[] = waveDependentProductsJList.getSelectedValues();
//
//                    for (int i = 0; i < values.length; i++) {
//                        AlgorithmInfo algorithmInfo = (AlgorithmInfo) values[i];
//
//                        if (wavelengthInteger < WavelengthInfo.VISIBLE_UPPER_LIMIT) {
//                            if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.VISIBLE ||
//                                    algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
//                                currentSelectedProductStringBuilder.delete(0, currentSelectedProductStringBuilder.length());
//
//                                currentSelectedProductStringBuilder.append(algorithmInfo.getProductName());
//                                currentSelectedProductStringBuilder.append("_");
//                                currentSelectedProductStringBuilder.append(selectedWavelengthString);
//
//                                if (algorithmInfo.getName() != null) {
//                                    currentSelectedProductStringBuilder.append("_");
//                                    currentSelectedProductStringBuilder.append(algorithmInfo.getName());
//                                }
//
//                                //   currentSelectedProductStringBuilder.append(algorithmInfo.getParameterType());
//                                //    currentSelectedProductStringBuilder.append("VISIBLE");
//
//                                if (selectedProductListStringBuilder.length() > 0) {
//                                    selectedProductListStringBuilder.append(PRODUCT_LIST_DELIMITER);
//                                }
//                                selectedProductListStringBuilder.append(currentSelectedProductStringBuilder);
//                            }
//
//                        } else {
//                            if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.IR ||
//                                    algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
//                                currentSelectedProductStringBuilder.delete(0, currentSelectedProductStringBuilder.length());
//
//                                currentSelectedProductStringBuilder.append(algorithmInfo.getProductName());
//                                currentSelectedProductStringBuilder.append("_");
//                                currentSelectedProductStringBuilder.append(selectedWavelengthString);
//
//                                if (algorithmInfo.getName() != null) {
//                                    currentSelectedProductStringBuilder.append("_");
//                                    currentSelectedProductStringBuilder.append(algorithmInfo.getName());
//                                }
//
//                                //    currentSelectedProductStringBuilder.append(algorithmInfo.getParameterType());
//                                //    currentSelectedProductStringBuilder.append("IR");
//
//                                if (selectedProductListStringBuilder.length() > 0) {
//                                    selectedProductListStringBuilder.append(PRODUCT_LIST_DELIMITER);
//                                }
//                                selectedProductListStringBuilder.append(currentSelectedProductStringBuilder);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//
//        Object values[] = waveIndependentProductsJList.getSelectedValues();
//
//        for (int i = 0; i < values.length; i++) {
//            AlgorithmInfo algorithmInfo = (AlgorithmInfo) values[i];
//
//            if (selectedProductListStringBuilder.length() > 0) {
//                selectedProductListStringBuilder.append(PRODUCT_LIST_DELIMITER);
//            }
//
//
//            selectedProductListStringBuilder.append(algorithmInfo.getProductName());
//
//            if (algorithmInfo.getName() != null) {
//                selectedProductListStringBuilder.append("_");
//                selectedProductListStringBuilder.append(algorithmInfo.getName());
//            }
//
//        }
//
//
//        if (selectedProductListStringBuilder.length() > 0) {
//            if (!l2genData.getParamValue(l2genData.PROD).equals(selectedProductListStringBuilder.toString())) {
//                l2genData.setParamValue(l2genData.PROD, selectedProductListStringBuilder.toString());
//                selectedProductsJTextArea.setText(selectedProductListStringBuilder.toString());
//            }
//        } else {
//            selectedProductsJTextArea.setText(SELECTED_PRODUCTS_JTEXT_AREA_DEFAULT);
//        }
//
//
//    }

//    private void setSelectionStatesWaveIndependentProductsJList() {
//        waveIndependentProductsJList.clearSelection();
//
//        int idx = 0;
//
//        for (ProductInfo productInfo : l2genData.getWaveIndependentProductInfoArray()) {
//            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
//
//                if (algorithmInfo.isSelected() == true) {
//                    waveIndependentProductsJList.setSelectedIndex(idx);
//                }
//
//                idx++;
//            }
//        }
//    }


//
//    private void setSelectionStatesWaveDependentProductsJList() {
//        waveDependentProductsJList.clearSelection();
//
//        int idx = 0;
//
//        for (ProductInfo productInfo : l2genData.getWaveDependentProductInfoArray()) {
//            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
//                for (WavelengthInfo wavelengthInfo : algorithmInfo.getWavelengthInfoArray()) {
//                    if (wavelengthInfo.isSelected() == true) {
//                        waveDependentProductsJList.setSelectedIndex(idx);
//                    }
//
//                    idx++;
//                }
//            }
//        }
//    }
}
