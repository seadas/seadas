package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.common.GridBagConstraintsCustom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/14/12
 * Time: 8:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genProductsPanel extends JPanel {

    private L2genData l2genData;
    JPanel productSelectorJPanel;
    JPanel wavelengthsLimitorJPanel;
    JPanel selectedProductsJPanel;
    JTextArea selectedProductsJTextArea;
    private JButton restoreDefaultsButton;
    private JScrollPane selectedProductsJScrollPane;



    L2genProductsPanel(L2genData l2genData) {
        this.l2genData = l2genData;

        initComponents();
        addComponents();
    }

    public void initComponents() {
        productSelectorJPanel = new L2genProductTreeSelectorPanel(l2genData);

        wavelengthsLimitorJPanel = new L2genWavelengthLimiterPanel(l2genData);
        if (l2genData.isIfileIndependentMode()) {
            wavelengthsLimitorJPanel.setVisible(false);
        }

        createSelectedProductsJTextArea();


        restoreDefaultsButton = new JButton("Restore Defaults (Products only)");
        restoreDefaultsButton.setEnabled(!l2genData.isParamDefault(L2genData.L2PROD));

        restoreDefaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.setParamToDefaults(L2genData.L2PROD);
            }
        });

        l2genData.addPropertyChangeListener(L2genData.L2PROD, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (l2genData.isParamDefault(L2genData.L2PROD)) {
                    restoreDefaultsButton.setEnabled(false);
                } else {
                    restoreDefaultsButton.setEnabled(true);
                }

            }
        });

    }

    public void addComponents() {


        JPanel innerPanel = new JPanel(new GridBagLayout());

        innerPanel.add(productSelectorJPanel,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 3));

        if (l2genData.isWavelengthRequired()) {
            innerPanel.add(wavelengthsLimitorJPanel,
                    new GridBagConstraintsCustom(1, 0, 1, 0.3, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 3));
        }

        setLayout(new GridBagLayout());


        add(innerPanel,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH));

        add(selectedProductsJPanel,
                new GridBagConstraintsCustom(0, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 3));

        add(restoreDefaultsButton,
                new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));

    }


    private JPanel createSelectedProductsJPanel() {

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Selected Products"));

        mainPanel.add(selectedProductsJScrollPane,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 0, 3));

        return mainPanel;
    }


    private void createSelectedProductsJTextArea() {

        selectedProductsJTextArea = new JTextArea();
        selectedProductsJTextArea.setLineWrap(true);
        selectedProductsJTextArea.setWrapStyleWord(false);
        selectedProductsJTextArea.setRows(3);
        selectedProductsJTextArea.setEditable(true);


        selectedProductsJScrollPane = new JScrollPane(selectedProductsJTextArea);

        selectedProductsJPanel = createSelectedProductsJPanel();


        selectedProductsJScrollPane.setBorder(null);
        selectedProductsJScrollPane.setPreferredSize(selectedProductsJScrollPane.getPreferredSize());
        selectedProductsJScrollPane.setMinimumSize(selectedProductsJScrollPane.getPreferredSize());
        selectedProductsJScrollPane.setMaximumSize(selectedProductsJScrollPane.getPreferredSize());


        selectedProductsJTextArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                String l2prod = l2genData.sortStringList(selectedProductsJTextArea.getText());
                l2genData.setParamValue(L2genData.L2PROD, l2prod);
                selectedProductsJTextArea.setText(l2genData.getParamValue(L2genData.L2PROD));
            }
        });


        l2genData.addPropertyChangeListener(L2genData.L2PROD, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                selectedProductsJTextArea.setText(l2genData.getParamValue(L2genData.L2PROD));
            }
        });

    }


    private JButton createDefaultsButton() {
        final JButton jButton = new JButton("Apply Defaults");
        jButton.setEnabled(false);

        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.setProdToDefault();
            }
        });

        l2genData.addPropertyChangeListener(L2genData.L2PROD, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (l2genData.isParamDefault(L2genData.L2PROD)) {
                    jButton.setEnabled(false);
                } else {
                    jButton.setEnabled(true);
                }
            }
        });

        return jButton;

    }

}
