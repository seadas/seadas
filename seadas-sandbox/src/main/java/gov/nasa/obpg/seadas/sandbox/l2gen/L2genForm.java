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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;



class L2genForm extends JTabbedPane {


    private final int VISIBLE_UPPER_LIMIT = 3000;

    private final String targetProductSuffix = "L2";
    private final AppContext appContext;
    private final SourceProductSelector sourceProductSelector;
    private final TargetProductSelector targetProductSelector;

    private String OCDATAROOT = System.getenv("OCDATAROOT");
    private ArrayList<JCheckBox> wavelengthsCheckboxArrayList = null;

    private JPanel wavelengthsPanel;
    private JList waveDependentJList;
    private JList waveIndependentJList;

    private JTextArea selectedProductsJTextArea;

    private String SELECTED_PRODUCTS_JTEXT_AREA_DEFAULT = "No products currently selected";
    private String WAVELENGTHS_PANEL_MESSAGE_DEFAULT = "Wavelengths can be specified here once an input file is selected";

    private String SEADAS_PRODUCTS_FILE = "/home/knowles/SeaDAS/seadas/seadas-sandbox/productList.xml";


    L2genForm(TargetProductSelector targetProductSelector, AppContext appContext) {
        this.targetProductSelector = targetProductSelector;
        this.appContext = appContext;
        this.sourceProductSelector = new SourceProductSelector(appContext, "Source Product:");


        createUI();
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
    }


    private void createSubsampleTab(String tabnameSubsample) {

        final JTabbedPane tabbedPane = new JTabbedPane();
        createLatLonSubTab(tabbedPane, "Lat-Lon");
        createPixlineSubTab(tabbedPane, "Pix-Line");


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


    private void createPixlineSubTab(JTabbedPane tabbedPane, String myTabname) {

        // Define all Swing controls used on this tab page
        final JTextField spixTextfield = new JTextField(5);
        final JTextField epixTextfield = new JTextField(5);
        final JTextField dpixTextfield = new JTextField(5);
        final JTextField slineTextfield = new JTextField(5);
        final JTextField elineTextfield = new JTextField(5);
        final JTextField dlineTextfield = new JTextField(5);

        final JLabel spixLabel = new JLabel("start pix");
        final JLabel epixLabel = new JLabel("end pix");
        final JLabel dpixLabel = new JLabel("delta pix");
        final JLabel slineLabel = new JLabel("start line");
        final JLabel elineLabel = new JLabel("end line");
        final JLabel dlineLabel = new JLabel("delta line");


        // Declare mainPanel and set it's attributes
        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());


        final JPanel innerPanel1 = new JPanel();
        innerPanel1.setBorder(BorderFactory.createTitledBorder("Pixels"));
        innerPanel1.setLayout(new GridBagLayout());


        // Add Swing controls to mainPanel grid cells
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.EAST;
            innerPanel1.add(spixLabel, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 0;
            innerPanel1.add(spixTextfield, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.anchor = GridBagConstraints.EAST;
            innerPanel1.add(epixLabel, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 1;
            innerPanel1.add(epixTextfield, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 2;
            c.anchor = GridBagConstraints.EAST;
            innerPanel1.add(dpixLabel, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 2;
            innerPanel1.add(dpixTextfield, c);
        }


        final JPanel innerPanel2 = new JPanel();
        innerPanel2.setBorder(BorderFactory.createTitledBorder("Lines"));
        innerPanel2.setLayout(new GridBagLayout());

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.EAST;
            innerPanel2.add(slineLabel, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 0;
            innerPanel2.add(slineTextfield, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.anchor = GridBagConstraints.EAST;
            innerPanel2.add(elineLabel, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 1;
            innerPanel2.add(elineTextfield, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 2;
            c.anchor = GridBagConstraints.EAST;
            innerPanel2.add(dlineLabel, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 2;
            innerPanel2.add(dlineTextfield, c);
        }


        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            mainPanel.add(innerPanel1, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 0;
            mainPanel.add(innerPanel2, c);
        }


        final JPanel finalMainPanel = new JPanel();
        finalMainPanel.setLayout(new GridBagLayout());

        {
            final GridBagConstraints c;
            c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.insets = new Insets(3, 3, 3, 3);
            c.fill = GridBagConstraints.NONE;
            c.weightx = 1;
            c.weighty = 1;

            finalMainPanel.add(mainPanel, c);
        }

        tabbedPane.addTab(myTabname, finalMainPanel);
    }


    private void createLatLonSubTab(JTabbedPane tabbedPane, String myTabname) {

        // Define all Swing controls used on this tab page
        final JTextField northTextfield = new JTextField(5);
        final JTextField southTextfield = new JTextField(5);
        final JTextField westTextfield = new JTextField(5);
        final JTextField eastTextfield = new JTextField(5);

        final JLabel northLabel = new JLabel("N");
        final JLabel southLabel = new JLabel("S");
        final JLabel westLabel = new JLabel("W");
        final JLabel eastLabel = new JLabel("E");


        // Declare mainPanel and set it's attributes
        final JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder("Coordinates"));
        mainPanel.setLayout(new GridBagLayout());


        // Add Swing controls to mainPanel grid cells
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 1;
            c.anchor = GridBagConstraints.NORTH;
            mainPanel.add(northTextfield, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 3;
            c.anchor = GridBagConstraints.SOUTH;
            mainPanel.add(southTextfield, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 3;
            c.gridy = 2;
            c.anchor = GridBagConstraints.EAST;
            mainPanel.add(eastTextfield, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 2;
            c.anchor = GridBagConstraints.WEST;
            mainPanel.add(westTextfield, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 0;
            c.anchor = GridBagConstraints.SOUTH;
            mainPanel.add(northLabel, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 4;
            c.anchor = GridBagConstraints.NORTH;
            mainPanel.add(southLabel, c);
        }

        {


            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 4;
            c.gridy = 2;
            c.anchor = GridBagConstraints.WEST;
            mainPanel.add(eastLabel, c);
        }

        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 2;
            c.anchor = GridBagConstraints.EAST;
            mainPanel.add(westLabel, c);
        }

        final JPanel finalMainPanel = new JPanel();
        finalMainPanel.setLayout(new GridBagLayout());

        {
            final GridBagConstraints c;
            c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.insets = new Insets(3, 3, 3, 3);
            c.fill = GridBagConstraints.NONE;
            c.weightx = 1;
            c.weighty = 1;

            finalMainPanel.add(mainPanel, c);
        }

//        return finalMainPanel;

        tabbedPane.addTab(myTabname, finalMainPanel);
    }


    private void createParfileTab(String myTabname) {

        // Define all Swing controls used on this tab page
        final JButton openButton = new JButton("Open");
        final JButton saveButton = new JButton("Save");
        final JTextArea textArea = new JTextArea();

        // Declare mainPanel and set it's attributes
        final JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder("Parfile"));
        mainPanel.setLayout(new GridBagLayout());

        sourceProductSelector.addSelectionChangeListener(new AbstractSelectionChangeListener() {

            @Override
            public void selectionChanged(SelectionChangeEvent event) {

                final Product sourceProduct = getSourceProduct();

                String myProduct = sourceProduct.getName();
                String myParfileLineIfile = "ifile=" + myProduct;

                textArea.setText(myParfileLineIfile);

//                sourceProductSelector.setSelectedProduct(sourceProduct);

                updateTargetProductName(sourceProduct);

            }
        });

        // Add openButton control to a mainPanel grid cell
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            mainPanel.add(openButton, c);
        }

        // Add saveButton control to a mainPanel grid cell
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 0;
            c.anchor = GridBagConstraints.EAST;
            mainPanel.add(saveButton, c);
        }

        // Add textArea control to a mainPanel grid cell
        {
            JScrollPane scrollTextArea = new JScrollPane(textArea);

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
        finalMainPanel = addPaddedWrapperPanel(mainPanel, 3);


        addTab(myTabname, finalMainPanel);
    }


    private void createProductSelectorTab(String myTabname) {

        wavelengthsPanel = new JPanel();
        final JPanel productWavelengthIndependentPanel = new JPanel();
        final JPanel productWavelengthDependentPanel = new JPanel();
        final JPanel selectedProductsPanel = new JPanel();

        ArrayList<ProductInfo> waveIndependentProductInfoArray;
        ArrayList<ProductInfo> waveDependentProductInfoArray;

        L2genXmlReader l2genXmlReader = new L2genXmlReader();

        l2genXmlReader.parseXmlFile(SEADAS_PRODUCTS_FILE);

        waveDependentProductInfoArray = l2genXmlReader.getWaveDependentProductInfoArray();
        waveIndependentProductInfoArray = l2genXmlReader.getWaveIndependentProductInfoArray();

        Collections.sort(waveIndependentProductInfoArray, ProductInfo.CASE_INSENSITIVE_ORDER);
        Collections.sort(waveDependentProductInfoArray, ProductInfo.CASE_INSENSITIVE_ORDER);

        waveIndependentJList = new JList();
        waveDependentJList = new JList();

        createProductSelectorWavelengthsPanel();

        createProductSelectorProductListPanel(productWavelengthIndependentPanel, waveIndependentProductInfoArray, "Products (Wavelength Independent)", waveIndependentJList);

        createProductSelectorProductListPanel(productWavelengthDependentPanel, waveDependentProductInfoArray, "Products (Wavelength Dependent)", waveDependentJList);


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
            mainPanel.add(wavelengthsPanel, c);
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
                                currentSelectedProductStringBuilder.append(algorithmInfo.toString());
                                currentSelectedProductStringBuilder.append("_");
                                currentSelectedProductStringBuilder.append(selectedWavelengthString);
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
                                currentSelectedProductStringBuilder.append(algorithmInfo.toString());
                                currentSelectedProductStringBuilder.append("_");
                                currentSelectedProductStringBuilder.append(selectedWavelengthString);
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
            if (selectedProductListStringBuilder.length() > 0) {
                selectedProductListStringBuilder.append(PRODUCT_LIST_DELIMITER);
            }

            selectedProductListStringBuilder.append(values[i].toString());
        }

        if (selectedProductListStringBuilder.length() > 0) {
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
        wavelengthsPanel.setBorder(BorderFactory.createTitledBorder("Wavelengths"));
        wavelengthsPanel.setLayout(new GridBagLayout());

        JLabel defaultMessageJLabel = new JLabel(WAVELENGTHS_PANEL_MESSAGE_DEFAULT);

        // add default message to the panel
        {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1;
            wavelengthsPanel.add(defaultMessageJLabel, c);
        }

    }


    private void updateProductSelectorWavelengthsPanel() {

        wavelengthsPanel.removeAll();

        // lookup hash relating mission letter with mission directory name
        final HashMap myMissionLetterHashMap = new HashMap();
        myMissionLetterHashMap.put("S", "seawifs");
        myMissionLetterHashMap.put("A", "modisa");
        myMissionLetterHashMap.put("T", "modist");

        // determine the mission letter and mission name from the selected product
        Product sourceProduct = getSourceProduct();
        String missionLetter = sourceProduct.getName().substring(0, 1);
        String missionDirectoryName = (String) myMissionLetterHashMap.get(missionLetter);

        // determine the filename which contains the wavelengths
        final StringBuilder myFilename = new StringBuilder("");
        myFilename.append(OCDATAROOT);
        myFilename.append("/");
        myFilename.append(missionDirectoryName);
        myFilename.append("/");
        myFilename.append("msl12_sensor_info.dat");

        // read in the mission's datafile which contains the wavelengths
        final ArrayList<String> myAsciiFileArrayList = myReadDataFile(myFilename.toString());

        // some GridBagLayout formatting variables
        int gridyCnt = 0;
        int gridxCnt = 0;
        int gridxColumns = 4;

        // clear this because we dynamically rebuild it when input file selection is made or changed
        wavelengthsCheckboxArrayList.clear();

        // loop through datafile
        for (String myLine : myAsciiFileArrayList) {

            // skip the comment lines in file
            if (!myLine.trim().startsWith("#")) {

                // just look at value pairs of the form Lambda(#) = #
                String splitLine[] = myLine.split("=");
                if (splitLine.length == 2 &&
                        splitLine[0].trim().startsWith("Lambda(") &&
                        splitLine[0].trim().endsWith(")")
                        ) {

                    // get current wavelength and add into in a JCheckBox
                    String currWavelength = splitLine[1].trim();
                    JCheckBox currJCheckBox = new JCheckBox(currWavelength);

                    currJCheckBox.setName(currWavelength.toString());

                    // add current JCheckBox to the externally accessible arrayList
                    wavelengthsCheckboxArrayList.add(currJCheckBox);


                    // add listener for current checkbox
                    currJCheckBox.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
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
                        wavelengthsPanel.add(currJCheckBox, c);
                    }

                    // increment GridBag coordinates
                    if (gridxCnt < (gridxColumns - 1)) {
                        gridxCnt++;
                    } else {
                        gridxCnt = 0;
                        gridyCnt++;
                    }

                }  //end if on value pairs of the form Lambda(#) = #

            }  // end if skipping comments lines

        }  // end for (String myLine : myAsciiFileArrayList)

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
            }
        });
        return panel;
    }


    private void updateTargetProductName(Product selectedProduct) {

        String productName = "output." + targetProductSuffix;

        final TargetProductSelectorModel selectorModel = targetProductSelector.getModel();

        if (selectedProduct != null) {
            int i = selectedProduct.getName().lastIndexOf('.');
            if (i != -1) {
                String baseName = selectedProduct.getName().substring(0, i);
                productName = baseName + "." + targetProductSuffix;
            } else {
                productName = selectedProduct.getName() + "." + targetProductSuffix;
            }
        }

        selectorModel.setProductName(productName);
   }


}
