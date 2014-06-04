package gov.nasa.gsfc.seadas.contour.ui;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.Binding;
import com.bc.ceres.swing.binding.BindingContext;
import com.jidesoft.combobox.ColorComboBox;
import gov.nasa.gsfc.seadas.contour.util.CommonUtilities;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.framework.ui.ColorComboBoxAdapter;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.tool.ToolButtonFactory;

import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 9/9/13
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContourDialog extends JDialog {
    static final String CONTOUR_LINE_COLOR_PROPERTY = "contourLineColor";
    private ContourData contourData;
    private Component helpButton = null;
    private HelpBroker helpBroker = null;

    private final static String HELP_ID = "contourLines";
    private final static String HELP_ICON = "icons/Help24.gif";

    private Product product;

    Band selectedBand;
    Double startValue, minValue;
    Double endValue, maxValue;
    int numberOfLevels;

    //UI components
    JTextField minValueField, maxValueField, numLevelsField;
    JComboBox bandComboBox;
    JCheckBox logCheckBox;
    DecimalFormat decimalFormat = new DecimalFormat("##.###");

    public ContourDialog(Product product) {
        this.product = product;
        contourData = new ContourData();
        initHelpBroker();

        if (helpBroker != null) {
            //helpButton = getHelpButton(HELP_ID);
        }

        selectedBand = product.getBandAt(0);
        updateContourData();
//        setMaxValue(new Double(CommonUtilities.round(selectedBand.getStx().getMax(), 3)));
//        setMinValue(new Double(CommonUtilities.round(selectedBand.getStx().getMin(), 3)));
        numberOfLevels = 1;
        createContourUI();
    }

    protected Component getHelpButton(String helpId) {
        if (helpId != null) {

            final AbstractButton helpButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon(HELP_ICON),
                    false);

            HelpSet helpSet = helpBroker.getHelpSet();
            helpBroker.setCurrentID(helpId);

            if (helpButton != null) {
                helpButton.setToolTipText("Help");
                helpButton.setName("helpButton");
                helpBroker.enableHelpKey(helpButton, helpId, helpSet);
                helpBroker.enableHelpOnButton(helpButton, helpId, helpSet);
            }

            return helpButton;
        }

        return null;
    }


    private void initHelpBroker() {
        HelpSet helpSet = HelpSys.getHelpSet();
        if (helpSet != null) {
            helpBroker = helpSet.createHelpBroker();
            if (helpBroker instanceof DefaultHelpBroker) {
                DefaultHelpBroker defaultHelpBroker = (DefaultHelpBroker) helpBroker;
                defaultHelpBroker.setActivationWindow(this);
            }
        }
    }


    public final void createContourUI() {


        final int rightInset = 5;

        JPanel contourPanel = new JPanel(new GridBagLayout());
        contourPanel.setBorder(BorderFactory.createTitledBorder(""));

        JPanel contourContainerPanel = new JPanel(new GridBagLayout());

        final JPanel basicPanel = getBasicPanel();

        contourContainerPanel.add(basicPanel,
                        new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));
        JButton addButton = new JButton("+");
        addButton.setPreferredSize(addButton.getPreferredSize());
        addButton.setMinimumSize(addButton.getPreferredSize());
        addButton.setMaximumSize(addButton.getPreferredSize());
        addButton.setName("addButton");

        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                JPanel addedPanel = getBasicPanel();
                ((JButton)event.getSource()).getParent().add(addedPanel);
                JPanel c = (JPanel)((JButton)event.getSource()).getParent();
                JPanel jPanel = (JPanel)c.getComponents()[0];
                int numPanels = jPanel.getComponents().length;
                jPanel.add(addedPanel,
                        new ExGridBagConstraints(0, numPanels, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));
                repaint();
                pack();
            }
        });

        JPanel mainPanel = new JPanel(new GridBagLayout());


        contourPanel.add(contourContainerPanel,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));
        contourPanel.add(addButton,
                new ExGridBagConstraints(0, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));


        mainPanel.add(getBandPanel(),
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));

        mainPanel.add(contourPanel,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));

        mainPanel.add(getControllerPanel(),
                new ExGridBagConstraints(0, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));

        mainPanel.addPropertyChangeListener("contourPanel", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                setPreferredSize(getPreferredSize());
                setMinimumSize(getPreferredSize());
                setMaximumSize(getPreferredSize());
                setSize(getPreferredSize());
                repaint();
                pack();
            }
        });


        add(mainPanel);


        setModalityType(ModalityType.APPLICATION_MODAL);


        setTitle("Contour Lines");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();


        setPreferredSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setSize(getPreferredSize());
    }

    private JPanel getBandPanel() {
        final int rightInset = 5;

        JPanel bandPanel = new JPanel(new GridBagLayout());

        String[] productList = product.getBandNames();
        JLabel bandLabel = new JLabel("Product:");
        bandComboBox = new JComboBox(productList);
        bandComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                selectedBand = product.getBand((String) bandComboBox.getSelectedItem());
                updateContourData();
                minValueField.setText(new Double(getMinValue()).toString());
                maxValueField.setText(new Double(getMaxValue()).toString());
            }
        });

        JLabel filler = new JLabel("                                              ");

        bandPanel.add(filler,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        bandPanel.add(bandLabel,
                new ExGridBagConstraints(1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        bandPanel.add(bandComboBox,
                new ExGridBagConstraints(2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));
        return bandPanel;
    }


    private JPanel getControllerPanel() {
        JPanel controllerPanel = new JPanel(new GridBagLayout());

        JButton createContourLines = new JButton("Create Contour Lines");
        createContourLines.setPreferredSize(createContourLines.getPreferredSize());
        createContourLines.setMinimumSize(createContourLines.getPreferredSize());
        createContourLines.setMaximumSize(createContourLines.getPreferredSize());
        createContourLines.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (contourData.getContourIntervals().size() == 0) {
                    contourData.createContourLevels(getMinValue(), getMaxValue(), getNumberOfLevels(), logCheckBox.isSelected());
                }
                dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(cancelButton.getPreferredSize());
        cancelButton.setMinimumSize(cancelButton.getPreferredSize());
        cancelButton.setMaximumSize(cancelButton.getPreferredSize());
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                contourData = null;
                dispose();
            }
        });


        JLabel filler = new JLabel("                                        ");

        controllerPanel.add(filler,
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        controllerPanel.add(cancelButton,
                new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        controllerPanel.add(createContourLines,
                new ExGridBagConstraints(3, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
//        controllerPanel.add(helpButton,
//                new ExGridBagConstraints(5, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        //createContourLines.setAlignmentX(0.5f);
        return controllerPanel;
    }


    private JPanel getBasicPanel() {

        final int rightInset = 5;
        JPanel contourPanel = new JPanel(new GridBagLayout());
        contourPanel.setBorder(BorderFactory.createTitledBorder(""));

        final DecimalFormat df = new DecimalFormat("##.###");
        minValueField = new JFormattedTextField(df);
        minValueField.setColumns(4);
        JLabel minValueLabel = new JLabel("Start Value:");


        maxValueField = new JFormattedTextField(df);
        maxValueField.setColumns(4);

        JLabel maxValueLabel = new JLabel("End Value:");

        numLevelsField = new JFormattedTextField(new DecimalFormat("##"));
        numLevelsField.setColumns(2);

        PropertyContainer propertyContainer = new PropertyContainer();
        propertyContainer.addProperty(Property.create("minValueField", minValue));
        propertyContainer.addProperty(Property.create("maxValueField", maxValue));
        propertyContainer.addProperty(Property.create("numLevelsField", numberOfLevels));

        final BindingContext bindingContext = new BindingContext(propertyContainer);
        final PropertyChangeListener pcl_min = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                minValue = (Double) bindingContext.getBinding("minValueField").getPropertyValue();
//                maxValue = (Double) bindingContext.getBinding("maxValueField").getPropertyValue();
//                numberOfLevels = (Integer) bindingContext.getBinding("numLevelsField").getPropertyValue();
            }
        };
        final PropertyChangeListener pcl_max = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //minValue = (Double) bindingContext.getBinding("minValueField").getPropertyValue();
                maxValue = (Double) bindingContext.getBinding("maxValueField").getPropertyValue();
                //numberOfLevels = (Integer) bindingContext.getBinding("numLevelsField").getPropertyValue();
            }
        };
        final PropertyChangeListener pcl_num = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // minValue = (Double) bindingContext.getBinding("minValueField").getPropertyValue();
                //maxValue = (Double) bindingContext.getBinding("maxValueField").getPropertyValue();
                numberOfLevels = (Integer) bindingContext.getBinding("numLevelsField").getPropertyValue();
            }
        };
        JLabel numLevelsLabel = new JLabel("# of Levels:");

        Binding minValueBinding = bindingContext.bind("minValueField", minValueField);
        minValueBinding.addComponent(minValueLabel);
        bindingContext.addPropertyChangeListener("minValueField", pcl_min);

        Binding maxValueBinding = bindingContext.bind("maxValueField", maxValueField);
        maxValueBinding.addComponent(maxValueLabel);
        bindingContext.addPropertyChangeListener("maxValueField", pcl_max);

        Binding numLevelsBinding = bindingContext.bind("numLevelsField", numLevelsField);
        numLevelsBinding.addComponent(numLevelsLabel);
        bindingContext.addPropertyChangeListener("numLevelsField", pcl_num);

        logCheckBox = new JCheckBox();
        logCheckBox.setName("log checkbox");
        logCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (getNumberOfLevels() == contourData.getNumOfLevels()) {
                    contourData.setKeepColors(true);
                } else {
                    contourData.setKeepColors(false);
                }
                contourData.createContourLevels(getMinValue(), getMaxValue(), getNumberOfLevels(), logCheckBox.isSelected());
            }
        });

        JLabel filler = new JLabel("      ");
        contourPanel.add(minValueLabel,
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        contourPanel.add(minValueField,
                new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        contourPanel.add(filler,
                new ExGridBagConstraints(2, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        contourPanel.add(numLevelsLabel,
                new ExGridBagConstraints(3, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        contourPanel.add(maxValueField,
                new ExGridBagConstraints(4, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        contourPanel.add(filler,
                new ExGridBagConstraints(5, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        contourPanel.add(numLevelsLabel,
                new ExGridBagConstraints(6, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        contourPanel.add(numLevelsField,
                new ExGridBagConstraints(7, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        contourPanel.add(filler,
                new ExGridBagConstraints(8, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        contourPanel.add(new JLabel("Log"),
                new ExGridBagConstraints(9, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        contourPanel.add(logCheckBox,
                new ExGridBagConstraints(10, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        JButton customize = new JButton("Customize");
        customize.setPreferredSize(customize.getPreferredSize());
        customize.setMinimumSize(customize.getPreferredSize());
        customize.setMaximumSize(customize.getPreferredSize());
        customize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (contourData.getLevels().size() == 0 ||
                        minValue != contourData.getStartValue() ||
                        maxValue != contourData.getEndValue() ||
                        numberOfLevels != contourData.getNumOfLevels()) {
                    System.out.print("---change!---");
                    contourData.createContourLevels(getMinValue(), getMaxValue(), getNumberOfLevels(), logCheckBox.isSelected());
                }
                System.out.print("--- no change!---");
                customizeContourLevels(contourData);
            }
        });

        contourPanel.add(filler,
                new ExGridBagConstraints(11, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        contourPanel.add(customize,
                new ExGridBagConstraints(12, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        return contourPanel;
    }

    private void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    private void updateContourData() {
        setMaxValue(new Double(CommonUtilities.round(selectedBand.getStx().getMax(), 3)));
        setMinValue(new Double(CommonUtilities.round(selectedBand.getStx().getMin(), 3)));
        contourData.setBand(selectedBand);
        contourData.setBandIndex(product.getBandIndex(selectedBand.getName()));
        //contourData.setStartValue(getMinValue());
        //contourData.setEndValue(getMaxValue());
    }

    private Double getMinValue() {
//        Double minValue;
//
//        if (minValueField.getText().trim().isEmpty()) {
//            minValue = selectedBand.getStx().getMinimum();
//        } else {
//            minValue = new Double(minValueField.getText());
//        }
//        this.minValue = minValue;
        return minValue;
    }

    private void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    private Double getMaxValue() {
//        Double maxValue;
//        if (maxValueField.getText().trim().isEmpty()) {
//            maxValue = selectedBand.getStx().getMaximum();
//        } else {
//            maxValue = new Double(maxValueField.getText());
//        }
//        this.maxValue = maxValue;
        return maxValue;
    }

    private void setNumberOfLevels(int numberOfLevels) {
        this.numberOfLevels = numberOfLevels;
    }

    private int getNumberOfLevels() {
//        if (!numLevelsField.getText().trim().isEmpty()) {
//            numberOfLevels = new Integer(numLevelsField.getText());
//        }
        return numberOfLevels;
    }

    private void customizeContourLevels(ContourData contourData) {
        final String contourNamePropertyName = "contourName";
        final String contourValuePropertyName = "contourValue";
        final String contourColorPropertyName = "contourColor";
        ArrayList<ContourInterval> contourIntervalsClone = contourData.cloneContourIntervals();
        JPanel customPanel = new JPanel();
        customPanel.setLayout(new BoxLayout(customPanel, BoxLayout.Y_AXIS));
        for (final ContourInterval interval : contourIntervalsClone) {
            JPanel contourLevelPanel = new JPanel(new TableLayout(7));
            JLabel contourNameLabel = new JLabel("Name: ");
            JLabel contourValueLabel = new JLabel("Value: ");
            JLabel contourColorLabel = new JLabel("Color: ");
            JTextField contourLevelName = new JTextField();
            contourLevelName.setColumns(20);
            contourLevelName.setText(interval.getContourLevelName());
            JTextField contourLevelValue = new JTextField();
            contourLevelValue.setColumns(5);
            contourLevelValue.setText(new Double(interval.getContourLevelValue()).toString());
            PropertyContainer propertyContainer = new PropertyContainer();
            propertyContainer.addProperty(Property.create(contourNamePropertyName, interval.getContourLevelName()));
            propertyContainer.addProperty(Property.create(contourValuePropertyName, interval.getContourLevelValue()));
            propertyContainer.addProperty(Property.create(contourColorPropertyName, interval.getLineColor()));
            final BindingContext bindingContext = new BindingContext(propertyContainer);
            final PropertyChangeListener pcl_name = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    interval.setContourLevelName((String) bindingContext.getBinding(contourNamePropertyName).getPropertyValue());
                    //interval.setContourLevelValue((Double) bindingContext.getBinding(contourValuePropertyName).getPropertyValue());
                    //interval.setLineColor((Color) bindingContext.getBinding(contourColorPropertyName).getPropertyValue());
                }
            };
            final PropertyChangeListener pcl_value = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    //interval.setContourLevelName((String) bindingContext.getBinding(contourNamePropertyName).getPropertyValue());
                    interval.setContourLevelValue((Double) bindingContext.getBinding(contourValuePropertyName).getPropertyValue());
                    //interval.setLineColor((Color) bindingContext.getBinding(contourColorPropertyName).getPropertyValue());
                }
            };
            final PropertyChangeListener pcl_color = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    //interval.setContourLevelName((String) bindingContext.getBinding(contourNamePropertyName).getPropertyValue());
                    //interval.setContourLevelValue((Double) bindingContext.getBinding(contourValuePropertyName).getPropertyValue());
                    interval.setLineColor((Color) bindingContext.getBinding(contourColorPropertyName).getPropertyValue());
                }
            };
            ColorComboBox contourLineColorComboBox = new ColorComboBox();
            contourLineColorComboBox.setColorValueVisible(false);
            contourLineColorComboBox.setAllowDefaultColor(true);
            Binding contourLineColorBinding = bindingContext.bind(contourColorPropertyName, new ColorComboBoxAdapter(contourLineColorComboBox));
            contourLineColorBinding.addComponent(contourColorLabel);
            bindingContext.addPropertyChangeListener(contourColorPropertyName, pcl_color);

            Binding contourNameBinding = bindingContext.bind(contourNamePropertyName, contourLevelName);
            contourNameBinding.addComponent(contourNameLabel);
            bindingContext.addPropertyChangeListener(contourNamePropertyName, pcl_name);

            Binding contourValueBinding = bindingContext.bind(contourValuePropertyName, contourLevelValue);
            contourValueBinding.addComponent(contourValueLabel);
            bindingContext.addPropertyChangeListener(contourValuePropertyName, pcl_value);

            contourLevelPanel.add(contourNameLabel);
            contourLevelPanel.add(contourLevelName);
            contourLevelPanel.add(contourValueLabel);
            contourLevelPanel.add(contourLevelValue);
            contourLevelPanel.add(contourColorLabel);
            contourLevelPanel.add(contourLineColorComboBox);
            customPanel.add(contourLevelPanel);
        }

        Object[] options = {"Save",
                "Cancel"};
        int n = JOptionPane.showOptionDialog(this, customPanel,
                "Customize Contour Levels",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[1]);
        if (n == JOptionPane.YES_OPTION) {
            contourData.setContourIntervals(contourIntervalsClone);
            minValueField.setText(new Double(contourIntervalsClone.get(0).getContourLevelValue()).toString());
            maxValueField.setText(new Double(contourIntervalsClone.get(contourIntervalsClone.size() - 1).getContourLevelValue()).toString());
            minValue = contourIntervalsClone.get(0).getContourLevelValue();
            maxValue = contourIntervalsClone.get(contourIntervalsClone.size() - 1).getContourLevelValue();
            contourData.setStartValue(minValue);
            contourData.setEndValue(maxValue);
            //minValueField.firePropertyChange("maxValueField", minValue, contourIntervalsClone.get(0).getContourLevelValue());
            //maxValueField.firePropertyChange("maxValueField", maxValue, contourIntervalsClone.get(contourIntervalsClone.size() - 1).getContourLevelValue());
        }
    }

    public ContourData getContourData() {
        return contourData;
    }

    public void setContourData(ContourData contourData) {
        this.contourData = contourData;
    }
}
