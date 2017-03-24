package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.core.L2genParamCategoryInfo;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.common.GridBagConstraintsCustom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/12/12
 * Time: 9:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genCategorizedParamsPanel extends JPanel {

    private L2genData l2genData;
    private L2genParamCategoryInfo paramCategoryInfo;
    private JPanel paramsPanel;
    private JButton restoreDefaultsButton;


    L2genCategorizedParamsPanel(L2genData l2genData, L2genParamCategoryInfo paramCategoryInfo) {

        this.l2genData = l2genData;
        this.paramCategoryInfo = paramCategoryInfo;


        initComponents();
        addComponents();
    }


    public void initComponents() {

        paramsPanel = new JPanel();
        paramsPanel.setLayout(new GridBagLayout());


        restoreDefaultsButton = new JButton("Restore Defaults ("+paramCategoryInfo.getName()+ " only)");
        restoreDefaultsButton.setEnabled(!l2genData.isParamCategoryDefault(paramCategoryInfo));

        restoreDefaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l2genData.setToDefaults(paramCategoryInfo);
            }
        });


        int gridy = 0;


        for (ParamInfo paramInfo : paramCategoryInfo.getParamInfos()) {
            JLabel jLabel;
            JLabel defaultIndicator;
            Component rightSideComponent;
            int rightSideComponentFill;

            if (paramInfo.hasValidValueInfos()) {
                if (paramInfo.isBit()) {
                    L2genParamBitwiseCheckboxGroup paramBitwiseCheckboxGroup = new L2genParamBitwiseCheckboxGroup(l2genData, paramInfo);
                    jLabel = paramBitwiseCheckboxGroup.getjLabel();
                    rightSideComponent = paramBitwiseCheckboxGroup.getjScrollPane();
                    rightSideComponentFill = GridBagConstraints.NONE;
                } else {
                    L2genParamComboBox paramComboBox = new L2genParamComboBox(l2genData, paramInfo);
                    jLabel = paramComboBox.getjLabel();
                    rightSideComponent = paramComboBox.getjComboBox();
                    rightSideComponentFill = GridBagConstraints.NONE;
                }
            } else {
                if (paramInfo.getType() == ParamInfo.Type.BOOLEAN) {
                    L2genParamCheckBox paramCheckBox = new L2genParamCheckBox(l2genData, paramInfo);
                    //  L2genParamCheckBoxKit paramCheckBox = new L2genParamCheckBoxKit(l2genData, paramInfo.getName(), paramInfo.getDescription());
                    jLabel = paramCheckBox.getjLabel();
                    rightSideComponent = paramCheckBox.getjCheckBox();
                    //  rightSideComponent = paramCheckBox.getComponent();
                    rightSideComponentFill = GridBagConstraints.NONE;
                } else if (paramInfo.getType() == ParamInfo.Type.IFILE || paramInfo.getType() == ParamInfo.Type.OFILE) {
                    L2genParamFileSelector paramFileSelector = new L2genParamFileSelector(l2genData, paramInfo);
                    jLabel = paramFileSelector.getjLabel();
                    rightSideComponent = paramFileSelector.getjPanel();
                    rightSideComponentFill = GridBagConstraints.HORIZONTAL;
                } else {
                    L2genParamTextfield paramTextfield = new L2genParamTextfield(l2genData, paramInfo);
                    jLabel = paramTextfield.getjLabel();
                    rightSideComponent = paramTextfield.getjTextField();
                    rightSideComponentFill = paramTextfield.getFill();
                }
            }


            L2genParamDefaultIndicator paramDefaultIndicator = new L2genParamDefaultIndicator(l2genData, paramInfo);
            defaultIndicator = paramDefaultIndicator.getjLabel();


            paramsPanel.add(jLabel,
                    new GridBagConstraintsCustom(0, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

            paramsPanel.add(defaultIndicator,
                    new GridBagConstraintsCustom(1, gridy, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

            paramsPanel.add(rightSideComponent,
                    new GridBagConstraintsCustom(2, gridy, 1, 0, GridBagConstraints.WEST, rightSideComponentFill));

            gridy++;

            l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (l2genData.isParamCategoryDefault(paramCategoryInfo)) {
                        restoreDefaultsButton.setEnabled(false);
                    } else {
                        restoreDefaultsButton.setEnabled(true);
                    }

                }
            });


        }


        /**
         * Add a blank filler panel to the bottom of paramsPanel
         * This serves the purpose of expanding at the bottom of the paramsPanel in order to fill the
         * space so that the rest of the param controls do not expand
         */

        paramsPanel.add(new JPanel(),
                new GridBagConstraintsCustom(2, gridy, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH));


    }

    public void addComponents() {


        final JScrollPane paramsScroll = new JScrollPane(paramsPanel);
        paramsScroll.setBorder(null);


        JPanel innerPanel = new JPanel(new GridBagLayout());
        innerPanel.setBorder(BorderFactory.createTitledBorder(paramCategoryInfo.getName()));

        innerPanel.add(paramsScroll,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH));

        innerPanel.add(restoreDefaultsButton,
                new GridBagConstraintsCustom(0, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));


        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(1000, 800));

        add(innerPanel,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 3));
    }

}
