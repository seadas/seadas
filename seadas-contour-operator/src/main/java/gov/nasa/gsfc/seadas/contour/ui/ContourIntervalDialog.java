package gov.nasa.gsfc.seadas.contour.ui;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.Binding;
import com.bc.ceres.swing.binding.BindingContext;
import com.jidesoft.combobox.ColorComboBox;
import gov.nasa.gsfc.seadas.contour.data.ContourData;
import gov.nasa.gsfc.seadas.contour.data.ContourInterval;
import gov.nasa.gsfc.seadas.contour.util.CommonUtilities;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.ui.ColorComboBoxAdapter;

import javax.swing.*;
import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/4/14
 * Time: 3:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContourIntervalDialog extends JDialog {
    private static final String DELETE_BUTTON_IMAGE_FILE_NAME = "delete_button.png";
    static final String CONTOUR_DATA_CHANGED_PROPERTY = "contourDataChanged";
    private Double minValue;
    private Double maxValue;
    int numberOfLevels;

    //UI components
    JTextField minValueField, maxValueField, numLevelsField;
    JCheckBox logCheckBox;

    ContourData contourData;

    ContourIntervalDialog(Band selectedBand, String unfilteredBandName, String filterName, double ptsToPixelsMultiplier) {
        contourData = new ContourData(selectedBand, unfilteredBandName, filterName, ptsToPixelsMultiplier);
        numberOfLevels = 1;
        contourData.setNumOfLevels(numberOfLevels);

        contourData.addPropertyChangeListener(ContourDialog.NEW_BAND_SELECTED_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                setBand(contourData.getBand());
                propertyChangeSupport.firePropertyChange(CONTOUR_DATA_CHANGED_PROPERTY, true, false);
            }
        });
        propertyChangeSupport.addPropertyChangeListener(CONTOUR_DATA_CHANGED_PROPERTY, getDataChangedPropertyListener());
        setMaxValue(new Double(CommonUtilities.round(selectedBand.getStx().getMaximum(), 7)));
        setMinValue(new Double(CommonUtilities.round(selectedBand.getStx().getMinimum(), 7)));
        contourData.createContourLevels();
    }

    SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    public void setBand(Band newBand) {
        numberOfLevels = 1;
        numLevelsField.setText("1");
        setMaxValue(new Double(CommonUtilities.round(newBand.getStx().getMaximum(), 7)));
        setMinValue(new Double(CommonUtilities.round(newBand.getStx().getMinimum(), 7)));
        minValueField.setText(new Double(getMinValue()).toString());
        maxValueField.setText(new Double(getMaxValue()).toString());
        contourData.setBand(newBand);
    }

    private PropertyChangeListener getDataChangedPropertyListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                contourData.createContourLevels();
            }
        };
    }

    protected JPanel getBasicPanel() {

        final int rightInset = 5;
        final JPanel contourPanel = new JPanel(new GridBagLayout());
        contourPanel.setBorder(BorderFactory.createTitledBorder(""));

        //final DecimalFormat decimalFormatBig = new DecimalFormat("##.###");
        final DecimalFormat decimalFormatSmall = new DecimalFormat("##.#######");

        minValueField = new JFormattedTextField(decimalFormatSmall);
//        if (getMinValue() > 1) {
//        minValueField = new JFormattedTextField(decimalFormatBig);
//        } else {
//            minValueField = new JFormattedTextField(decimalFormatSmall);
//        }
        minValueField.setColumns(10);
        JLabel minValueLabel = new JLabel("Start Value:");

        maxValueField = new JFormattedTextField(decimalFormatSmall);
//        if (getMaxValue() > 1 ) {
//        maxValueField = new JFormattedTextField(decimalFormatBig);
//        } else {
//            maxValueField = new JFormattedTextField(decimalFormatSmall);
//        }

        maxValueField.setColumns(10);

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
                Double originalMinValue = minValue;
                minValue = (Double) bindingContext.getBinding("minValueField").getPropertyValue();
                if (minValue == null) {
                    if (maxValue != null) {
                        minValue = maxValue;
                    } else {
                        minValue = originalMinValue;
                    }
                }
                contourData.setStartValue(minValue);
                contourData.setContourValuesChanged(true);
                contourData.setLog(logCheckBox.isSelected() && minValue > 0 && maxValue > 0);
                propertyChangeSupport.firePropertyChange(CONTOUR_DATA_CHANGED_PROPERTY, true, false);
                logCheckBox.setSelected(logCheckBox.isSelected() && minValue > 0 && maxValue > 0);
            }
        };
        final PropertyChangeListener pcl_max = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Double originalMaxValue = maxValue;
                maxValue = (Double) bindingContext.getBinding("maxValueField").getPropertyValue();

                if (maxValue == null) {
                    if (minValue != null) {
                        maxValue = minValue;
                    } else {
                        maxValue = originalMaxValue;
                    }
                }
                contourData.setEndValue(maxValue);
                contourData.setContourValuesChanged(true);
                contourData.setLog(logCheckBox.isSelected() && minValue > 0 && maxValue > 0);
                propertyChangeSupport.firePropertyChange(CONTOUR_DATA_CHANGED_PROPERTY, true, false);
                logCheckBox.setSelected(logCheckBox.isSelected() && minValue > 0 && maxValue > 0);
            }
        };
        final PropertyChangeListener pcl_num = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                numberOfLevels = (Integer) bindingContext.getBinding("numLevelsField").getPropertyValue();
                contourData.setNumOfLevels(numberOfLevels);
                contourData.setContourValuesChanged(true);
                propertyChangeSupport.firePropertyChange(CONTOUR_DATA_CHANGED_PROPERTY, true, false);
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
//                if (getNumberOfLevels() == contourData.getNumOfLevels()) {
//                    contourData.setKeepColors(true);
//                } else {
//                    contourData.setKeepColors(false);
//                }
                contourData.setLog(logCheckBox.isSelected() && minValue > 0 && maxValue > 0);
                propertyChangeSupport.firePropertyChange(CONTOUR_DATA_CHANGED_PROPERTY, true, false);
                //contourData.createContourLevels(getMinValue(), getMaxValue(), getNumberOfLevels(), logCheckBox.isSelected());
                logCheckBox.setSelected(logCheckBox.isSelected() && minValue > 0 && maxValue > 0);
            }
        });

        JLabel filler = new JLabel("      ");
        contourPanel.add(minValueLabel,
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        contourPanel.add(minValueField,
                new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        contourPanel.add(filler,
                new ExGridBagConstraints(2, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        contourPanel.add(maxValueLabel,
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

        JButton customize = new JButton("Preview/Edit");
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
                //System.out.print("--- no change!---");
                customizeContourLevels(contourData);
                // disable min, max, level, log fields of a single contour panel
                if (contourData.isContourCustomized()) {
                    contourPanel.getComponent(0).setEnabled(false);
                    contourPanel.getComponent(1).setEnabled(false);
                    contourPanel.getComponent(2).setEnabled(false);
                    contourPanel.getComponent(3).setEnabled(false);
                    contourPanel.getComponent(4).setEnabled(false);
                    contourPanel.getComponent(5).setEnabled(false);
                    contourPanel.getComponent(6).setEnabled(false);
                    contourPanel.getComponent(7).setEnabled(false);

                }
            }
        });

        contourPanel.add(filler,
                new ExGridBagConstraints(11, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        contourPanel.add(customize,
                new ExGridBagConstraints(12, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        contourPanel.add(filler,
                new ExGridBagConstraints(13, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        JButton deleteButton = new JButton();
        ImageIcon imageIcon = new ImageIcon();

        java.net.URL imgURL = getClass().getResource(DELETE_BUTTON_IMAGE_FILE_NAME);
        if (imgURL != null) {
            imageIcon = new ImageIcon(imgURL, "Delete current row");
        } else {
            System.err.println("Couldn't find file: " + DELETE_BUTTON_IMAGE_FILE_NAME);
        }
        deleteButton.setIcon(imageIcon);
        deleteButton.setToolTipText("Delete current row");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                contourPanel.removeAll();
                contourPanel.validate();
                contourPanel.repaint();
                contourData.setDeleted(true);
                propertyChangeSupport.firePropertyChange("deleteButtonPressed", true, false);
                //propertyChangeSupport.firePropertyChange("deleteButtonPressed", true, false);
            }
        });

        contourPanel.add(deleteButton,
                new ExGridBagConstraints(14, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        return contourPanel;
    }

    protected void setMinValue(Double minValue) {
        this.minValue = minValue;
        contourData.setStartValue(minValue);
    }

    private Double getMinValue() {
        return minValue;
    }

    protected void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
        contourData.setEndValue(maxValue);
    }

    private Double getMaxValue() {
        return maxValue;
    }

    private void setNumberOfLevels(int numberOfLevels) {
        this.numberOfLevels = numberOfLevels;
        contourData.setNumOfLevels(numberOfLevels);
    }

    private int getNumberOfLevels() {
        return numberOfLevels;
    }

    private void customizeContourLevels(ContourData contourData) {
        final String contourNamePropertyName = "contourName";
        final String contourValuePropertyName = "contourValue";
        final String contourColorPropertyName = "contourColor";
        final String contourLineStylePropertyName = "contourLineStyle";
        final String contourLineDashLengthPropertyName = "contourLineDashLength";
        final String contourLineSpaceLengthPropertyName = "contourLineSpaceLength";
        ArrayList<ContourInterval> contourIntervalsClone = contourData.cloneContourIntervals();
        JPanel customPanel = new JPanel();
        customPanel.setLayout(new BoxLayout(customPanel, BoxLayout.Y_AXIS));
        for (final ContourInterval interval : contourIntervalsClone) {
            JPanel contourLevelPanel = new JPanel(new TableLayout(12));
            JLabel contourNameLabel = new JLabel("Name: ");
            JLabel contourValueLabel = new JLabel("Value: ");
            JLabel contourColorLabel = new JLabel("Color: ");
            JLabel contourLineStyleLabel = new JLabel("Line Style: ");
            JLabel contourLineDashLengthLabel = new JLabel("Line Dash Length: ");
            JLabel contourLineSpaceLengthLabel = new JLabel("Line Space Length: ");

            JPanel contourLineStylePanel = new JPanel();
            contourLineStylePanel.setLayout(new TableLayout(2));

            JTextField contourLevelName = new JTextField();
            contourLevelName.setColumns(25);
            contourLevelName.setText(interval.getContourLevelName());
            JTextField contourLevelValue = new JTextField();
            contourLevelValue.setColumns(10);
            contourLevelValue.setText(new Double(interval.getContourLevelValue()).toString());
            JTextField contourLineStyleValue = new JTextField();
            contourLineStyleValue.setColumns(10);
            contourLineStyleValue.setText(interval.getContourLineStyleValue());
            contourLineStyleValue.setToolTipText("Enter two numeric values. First number defines the dash length, the second number defines the space the length for dashed lines.");

            JTextField dashLengthValue = new JTextField();
            dashLengthValue.setColumns(4);
            dashLengthValue.setText(new Double(interval.getDashLength()).toString());
            dashLengthValue.setToolTipText("Enter a value greater than 0.");

            JTextField spaceLengthValue = new JTextField();
            spaceLengthValue.setColumns(4);
            spaceLengthValue.setText(new Double(interval.getSpaceLength()).toString());
            spaceLengthValue.setToolTipText("Enter 0 for solid lines. Enter a value greater than 0 for dashed lines.");

            PropertyContainer propertyContainer = new PropertyContainer();
            propertyContainer.addProperty(Property.create(contourNamePropertyName, interval.getContourLevelName()));
            propertyContainer.addProperty(Property.create(contourValuePropertyName, interval.getContourLevelValue()));
            propertyContainer.addProperty(Property.create(contourColorPropertyName, interval.getLineColor()));
            propertyContainer.addProperty(Property.create(contourLineStylePropertyName, interval.getContourLineStyleValue()));
            propertyContainer.addProperty(Property.create(contourLineDashLengthPropertyName, interval.getDashLength()));
            propertyContainer.addProperty(Property.create(contourLineSpaceLengthPropertyName, interval.getSpaceLength()));

            final BindingContext bindingContext = new BindingContext(propertyContainer);
            final PropertyChangeListener pcl_name = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    interval.setContourLevelName((String) bindingContext.getBinding(contourNamePropertyName).getPropertyValue());
                }
            };
            final PropertyChangeListener pcl_value = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    interval.setContourLevelValue((Double) bindingContext.getBinding(contourValuePropertyName).getPropertyValue());
                }
            };
            final PropertyChangeListener pcl_color = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    interval.setLineColor((Color) bindingContext.getBinding(contourColorPropertyName).getPropertyValue());
                }
            };
            final PropertyChangeListener pcl_lineStyle = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    interval.setContourLineStyleValue((String) bindingContext.getBinding(contourLineStylePropertyName).getPropertyValue());
                }
            };

            final PropertyChangeListener pcl_lineDashLength = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    interval.setDashLength((Double) bindingContext.getBinding(contourLineDashLengthPropertyName).getPropertyValue());
                }
            };
            final PropertyChangeListener pcl_lineSpaceLength = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    interval.setSpaceLength((Double) bindingContext.getBinding(contourLineSpaceLengthPropertyName).getPropertyValue());
                }
            };

            ColorComboBox contourLineColorComboBox = new ColorComboBox();
            contourLineColorComboBox.setColorValueVisible(false);
            contourLineColorComboBox.setAllowDefaultColor(true);
            contourLineColorComboBox.setSelectedColor(interval.getLineColor());
            Binding contourLineColorBinding = bindingContext.bind(contourColorPropertyName, new ColorComboBoxAdapter(contourLineColorComboBox));
            contourLineColorBinding.addComponent(contourColorLabel);
            bindingContext.addPropertyChangeListener(contourColorPropertyName, pcl_color);

            Binding contourNameBinding = bindingContext.bind(contourNamePropertyName, contourLevelName);
            contourNameBinding.addComponent(contourNameLabel);
            bindingContext.addPropertyChangeListener(contourNamePropertyName, pcl_name);

            Binding contourValueBinding = bindingContext.bind(contourValuePropertyName, contourLevelValue);
            contourValueBinding.addComponent(contourValueLabel);
            bindingContext.addPropertyChangeListener(contourValuePropertyName, pcl_value);

            Binding contourLineBinding = bindingContext.bind(contourLineStylePropertyName, contourLineStyleValue);
            contourLineBinding.addComponent(contourLineStyleLabel);
            bindingContext.addPropertyChangeListener(contourLineStylePropertyName, pcl_lineStyle);

            Binding contourLineDashLengthBinding = bindingContext.bind(contourLineDashLengthPropertyName, dashLengthValue);
            contourLineDashLengthBinding.addComponent(contourLineDashLengthLabel);
            bindingContext.addPropertyChangeListener(contourLineDashLengthPropertyName, pcl_lineDashLength);

            Binding contourLineSpaceLengthBinding = bindingContext.bind(contourLineSpaceLengthPropertyName, spaceLengthValue);
            contourLineSpaceLengthBinding.addComponent(contourLineSpaceLengthLabel);
            bindingContext.addPropertyChangeListener(contourLineSpaceLengthPropertyName, pcl_lineSpaceLength);


            contourLevelPanel.add(contourNameLabel);
            contourLevelPanel.add(contourLevelName);
            contourLevelPanel.add(contourValueLabel);
            contourLevelPanel.add(contourLevelValue);
            contourLevelPanel.add(contourColorLabel);
            contourLevelPanel.add(contourLineColorComboBox);
//            contourLevelPanel.add(contourLineStyleLabel);
//            contourLevelPanel.add(contourLineStyleValue);
            //contourLevelPanel.add(contourLineDashLengthLabel);
            //contourLevelPanel.add(dashLengthValue);
            //contourLevelPanel.add(contourLineSpaceLengthLabel);
            //contourLevelPanel.add(spaceLengthValue);
            customPanel.add(contourLevelPanel);
        }

        Object[] options = {"Save",
                "Cancel"};
//        JOptionPane jPane = new JOptionPane();
//
//        int n = JOptionPane.showOptionDialog(this, customPanel,
//                "Customize Contour Levels",
//                JOptionPane.YES_NO_OPTION,
//                JOptionPane.PLAIN_MESSAGE,
//                null,
//                options,
//                options[0]);
        //final JDialog dialog = new JDialog(this, "Customize Contour Levels", true);
        final JOptionPane optionPane = new JOptionPane(customPanel,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                javax.swing.UIManager.getIcon("OptionPane.informationIcon"),     //do not use a custom Icon
                options,  //the titles of buttons
                options[0]); //default button title

        final JDialog dialog = optionPane.createDialog(this, "Group Contour Levels");
        dialog.setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                //setLabel("Thwarted user attempt to close window.");
            }
        });
        optionPane.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        String prop = e.getPropertyName();

                        if (dialog.isVisible()
                                && (e.getSource() == optionPane)
                                && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                            //If you were going to check something
                            //before closing the window, you'd do
                            //it here.
                            dialog.setVisible(false);
                        }
                    }
                });
        Point dialogLoc = dialog.getLocation();
        Point parentLoc = this.getLocation();
        dialog.setLocation(parentLoc.x + dialogLoc.x, dialogLoc.y);
        dialog.pack();
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);


        //int value = ((Integer)optionPane.getValue()).intValue();
//        if (value == JOptionPane.YES_OPTION) {
//            setLabel("Good.");
//        } else if (value == JOptionPane.NO_OPTION) {
//            setLabel("Try using the window decorations "
//                     + "to close the non-auto-closing dialog. "
//                     + "You can't!");
//        }
        if (optionPane.getValue().equals(options[0])) {
            contourData.setContourIntervals(contourIntervalsClone);
            minValueField.setText(new Double(contourIntervalsClone.get(0).getContourLevelValue()).toString());
            maxValueField.setText(new Double(contourIntervalsClone.get(contourIntervalsClone.size() - 1).getContourLevelValue()).toString());
            minValue = contourIntervalsClone.get(0).getContourLevelValue();
            maxValue = contourIntervalsClone.get(contourIntervalsClone.size() - 1).getContourLevelValue();
            contourData.setStartValue(minValue);
            contourData.setEndValue(maxValue);
            contourData.setContourCustomized(true);
        }
    }

    public ContourData getContourData() {
        return contourData;
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }

    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(name, listener);
    }

    public SwingPropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public void appendPropertyChangeSupport(SwingPropertyChangeSupport propertyChangeSupport) {
        PropertyChangeListener[] pr = propertyChangeSupport.getPropertyChangeListeners();
        for (int i = 0; i < pr.length; i++) {
            this.propertyChangeSupport.addPropertyChangeListener(pr[i]);
        }
    }

    public void clearPropertyChangeSupport() {
        PropertyChangeListener[] pr = propertyChangeSupport.getPropertyChangeListeners();
        for (int i = 0; i < pr.length; i++) {
            this.propertyChangeSupport.removePropertyChangeListener(pr[i]);
        }

    }

//    public void updateContourNames(String newFilterName){
//            contourData.updateContourNamesForNewFilter(contourData.getFilterName(), newFilterName);
//    }
}
