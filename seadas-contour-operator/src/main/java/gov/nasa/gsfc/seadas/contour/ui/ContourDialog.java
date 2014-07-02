package gov.nasa.gsfc.seadas.contour.ui;

import gov.nasa.gsfc.seadas.contour.data.ContourData;
import gov.nasa.gsfc.seadas.contour.data.ContourInterval;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.tool.ToolButtonFactory;

import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.*;
import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
    static final String NEW_BAND_SELECTED_PROPERTY = "newBandSelected";
    static final String DELETE_BUTTON_PRESSED_PROPERTY = "deleteButtonPressed";
    private ContourData contourData;
    private Component helpButton = null;
    private HelpBroker helpBroker = null;

    private final static String HELP_ID = "contourLines";
    private final static String HELP_ICON = "icons/Help24.gif";

    private Product product;

    Band selectedBand;
    int numberOfLevels;

    JComboBox bandComboBox;
    ArrayList<ContourData> contours;

    private SwingPropertyChangeSupport propertyChangeSupport;

    JPanel contourPanel;
    boolean filterBand;
    private boolean contourCanceled;

    public ContourDialog(Product product, String selectedBandName) {
        this.product = product;

        initHelpBroker();

        propertyChangeSupport = new SwingPropertyChangeSupport(this);

        if (helpBroker != null) {
            helpButton = getHelpButton(HELP_ID);
        }

        //selectedBand = product.getBandAt(0);
        selectedBand = product.getBand(selectedBandName);
        contourData = new ContourData(selectedBand);
        //updateContourData();
//        setMaxValue(new Double(CommonUtilities.round(selectedBand.getStx().getMax(), 3)));
//        setMinValue(new Double(CommonUtilities.round(selectedBand.getStx().getMin(), 3)));
        numberOfLevels = 1;
        contours = new ArrayList<ContourData>();
        propertyChangeSupport.addPropertyChangeListener(NEW_BAND_SELECTED_PROPERTY, getBandPropertyListener());
        propertyChangeSupport.addPropertyChangeListener(DELETE_BUTTON_PRESSED_PROPERTY, getDeleteButtonPropertyListener());
        filterBand = selectedBandName.indexOf("filtered") == -1 ? true : false;
        createContourUI();
        contourCanceled = false;
    }

    private PropertyChangeListener getBandPropertyListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                for (ContourData contourData1:contours) {
                    contourData1.setBand(selectedBand);
                }
            }
        };
    }

    private PropertyChangeListener getDeleteButtonPropertyListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                Component[] components = contourPanel.getComponents();
                for (Component component : components) {
                    if (component instanceof JPanel) {

                        Component[] jPanelComponents = ((JPanel) component).getComponents();
                        for (Component jPanelComponent : jPanelComponents) {
                            if (component instanceof JPanel && ((JPanel) jPanelComponent).getComponents().length == 0) {
                                ((JPanel) component).remove(jPanelComponent);
                            }
                        }
                    }
                    contourPanel.validate();
                    contourPanel.repaint();
                }
            }
        };
    }

        @Override
        public void addPropertyChangeListener (String name, PropertyChangeListener listener){
            propertyChangeSupport.addPropertyChangeListener(name, listener);
        }

        @Override
        public void removePropertyChangeListener (String name, PropertyChangeListener listener){
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


    public final JPanel createContourUI() {


        final int rightInset = 5;

        contourPanel = new JPanel(new GridBagLayout());

        contourPanel.setBorder(BorderFactory.createTitledBorder(""));

        final JPanel contourContainerPanel = new JPanel(new GridBagLayout());

        final JPanel basicPanel = getContourPanel();

        contourContainerPanel.add(basicPanel,
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));
        JButton addButton = new JButton("+");
        addButton.setPreferredSize(addButton.getPreferredSize());
        addButton.setMinimumSize(addButton.getPreferredSize());
        addButton.setMaximumSize(addButton.getPreferredSize());
        addButton.setName("addButton");

        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                JPanel addedPanel = getContourPanel();
                ((JButton) event.getSource()).getParent().add(addedPanel);
                JPanel c = (JPanel) ((JButton) event.getSource()).getParent();
                JPanel jPanel = (JPanel) c.getComponents()[0];
                int numPanels = jPanel.getComponents().length;
                jPanel.add(addedPanel,
                        new ExGridBagConstraints(0, numPanels, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));
                repaint();
                pack();
            }
        });

        contourContainerPanel.addPropertyChangeListener("deleteButtonPressed", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                Component[] components = contourContainerPanel.getComponents();
                for (Component component : components) {
                    if (((JPanel) component).getComponents().length == 0) {
                        contourContainerPanel.remove(component);
                    }
                }
                contourContainerPanel.validate();
                contourContainerPanel.repaint();
            }
        });
        JPanel mainPanel = new JPanel(new GridBagLayout());


        contourPanel.add(contourContainerPanel,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));
        contourPanel.add(addButton,
                new ExGridBagConstraints(0, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));
        mainPanel.add(getFilterPanel(),
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));

        mainPanel.add(contourPanel,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));

        mainPanel.add(getControllerPanel(),
                new ExGridBagConstraints(0, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));

        add(mainPanel);


        setModalityType(ModalityType.APPLICATION_MODAL);


        setTitle("Contour Lines for " + selectedBand.getName() );
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();

        return mainPanel;
    }

    private JPanel getBandPanel() {
        final int rightInset = 5;

        JPanel bandPanel = new JPanel(new GridBagLayout());

        final JCheckBox filterBox = new JCheckBox("Filter 5x5");
        String[] productList = product.getBandNames();
        JLabel bandLabel = new JLabel("Product:");
        bandComboBox = new JComboBox(productList);
        bandComboBox.setSelectedIndex(product.getBandIndex(selectedBand.getName()));
        bandComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String oldBandName = selectedBand.getName();
                selectedBand = product.getBand((String) bandComboBox.getSelectedItem());
                propertyChangeSupport.firePropertyChange(NEW_BAND_SELECTED_PROPERTY, oldBandName, selectedBand.getName());
                filterBand =  selectedBand.getName().indexOf("filtered") == -1 ? true : false;
                filterBox.setSelected(filterBand);
                filterBox.setEnabled(filterBand);
            }
        });

        filterBox.setSelected(filterBand);
        filterBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                filterBand = filterBox.isSelected();
                //contourData.setFiltered(filterBox.isSelected());
            }
        });
        JLabel filler = new JLabel("                                              ");

        bandPanel.add(filler,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        bandPanel.add(bandLabel,
                new ExGridBagConstraints(1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        bandPanel.add(bandComboBox,
                new ExGridBagConstraints(2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));
        bandPanel.add(filterBox,
                        new ExGridBagConstraints(3, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));
        return bandPanel;
    }


    private JPanel getFilterPanel(){
        final JCheckBox filterBox = new JCheckBox("Filter 5x5");
        filterBox.setSelected(true);
        filterBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                filterBand = filterBox.isSelected();
                //contourData.setFiltered(filterBox.isSelected());
            }
        });

        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.add(filterBox, BorderLayout.CENTER);
        return filterPanel;
    }

    private JPanel getControllerPanel() {
        JPanel controllerPanel = new JPanel(new GridBagLayout());

        JButton createContourLines = new JButton("Create Contour Lines");
        createContourLines.setPreferredSize(createContourLines.getPreferredSize());
        createContourLines.setMinimumSize(createContourLines.getPreferredSize());
        createContourLines.setMaximumSize(createContourLines.getPreferredSize());
        createContourLines.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
//                if (contourData.getContourIntervals().size() == 0) {
//                    contourData.createContourLevels(getMinValue(), getMaxValue(), getNumberOfLevels(), logCheckBox.isSelected());
//                }
                dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(cancelButton.getPreferredSize());
        cancelButton.setMinimumSize(cancelButton.getPreferredSize());
        cancelButton.setMaximumSize(cancelButton.getPreferredSize());
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                contourCanceled = true;
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
        controllerPanel.add(helpButton,
                new ExGridBagConstraints(5, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        return controllerPanel;
    }

    private JPanel getContourPanel() {
        ContourIntervalDialog contourIntervalDialog = new ContourIntervalDialog(selectedBand);
        contourIntervalDialog.addPropertyChangeListener(DELETE_BUTTON_PRESSED_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                propertyChangeSupport.firePropertyChange(DELETE_BUTTON_PRESSED_PROPERTY, true, false);
            }
        });
        contourIntervalDialog.addPropertyChangeListener(ContourIntervalDialog.CONTOUR_DATA_CHANGED_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                for (ContourData contourData : contours) {
                    contourData.createContourLevels();
                }
            }
        });
        contours.add(contourIntervalDialog.getContourData());
        return contourIntervalDialog.getBasicPanel();
    }


    public ContourData getContourData() {

        return getContourData(contours);
    }

    public ContourData getContourData(ArrayList<ContourData> contours) {
        ContourData mergedContourData = new ContourData(selectedBand);
        ArrayList<ContourInterval> contourIntervals = new ArrayList<ContourInterval>();
        for (ContourData contourData : contours) {
            contourIntervals.addAll(contourData.getContourIntervals());
        }
        mergedContourData.setContourIntervals(contourIntervals);
        mergedContourData.setFiltered(filterBand);
        return mergedContourData;
    }


    public boolean isContourCanceled() {
        return contourCanceled;
    }

    public void setContourCanceled(boolean contourCanceled) {
        this.contourCanceled = contourCanceled;
    }
}
