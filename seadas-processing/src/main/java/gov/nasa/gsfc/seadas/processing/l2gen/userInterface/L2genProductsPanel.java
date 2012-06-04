package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.general.GridBagConstraintsCustom;

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
    JButton defaultsButton;


    L2genProductsPanel(L2genData l2genData) {

        this.l2genData = l2genData;


        initComponents();
        addComponents();
    }

    public void initComponents() {
        productSelectorJPanel = new L2genProductTreeSelectorPanel(l2genData);

        wavelengthsLimitorJPanel = new L2genWavelengthLimiterPanel(l2genData);

        selectedProductsJTextArea = createSelectedProductsJTextArea();
        defaultsButton = createDefaultsButton();

        selectedProductsJPanel = createSelectedProductsJPanel();

    }

    public void addComponents() {
        setLayout(new GridBagLayout());

        add(productSelectorJPanel,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH));

        add(wavelengthsLimitorJPanel,
                new GridBagConstraintsCustom(1, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

        add(selectedProductsJPanel,
                new GridBagConstraintsCustom(0, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 0, 2));

    }


    private JPanel createSelectedProductsJPanel() {

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Selected Products"));

        mainPanel.add(selectedProductsJTextArea,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 3));

        mainPanel.add(defaultsButton,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE));

        return mainPanel;
    }


    private JTextArea createSelectedProductsJTextArea() {

        final JTextArea jTextArea = new JTextArea();
        jTextArea.setLineWrap(true);
        jTextArea.setWrapStyleWord(true);
        jTextArea.setColumns(20);
        jTextArea.setRows(5);
        jTextArea.setEditable(true);



        jTextArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                    String l2prod = l2genData.sortStringList(jTextArea.getText());
                    l2genData.setParamValue(L2genData.L2PROD, l2prod);
                    jTextArea.setText(l2genData.getParamValue(L2genData.L2PROD));
            }
        });


        l2genData.addPropertyChangeListener(L2genData.L2PROD, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jTextArea.setText(l2genData.getParamValue(L2genData.L2PROD));
            }
        });

        return jTextArea;
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
