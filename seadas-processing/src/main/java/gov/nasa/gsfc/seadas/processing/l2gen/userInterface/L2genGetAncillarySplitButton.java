package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import com.jidesoft.swing.JideSwingUtilities;
import gov.nasa.gsfc.seadas.processing.core.L2genData;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import com.jidesoft.swing.JideSplitButton;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 12/14/12
 * Time: 3:58 PM
 * To change this template use File | Settings | File Templates.
 */


// http://java.dzone.com/news/drop-down-buttons-swing-new-al


public class L2genGetAncillarySplitButton {

    private L2genData l2genData;

    private JideSplitButton ancillarySplitButton;

    JMenuItem getAncillaryJMenuItem = new JMenuItem("Get Ancillary");
    JMenuItem clearAncillaryJMenuItem = new JMenuItem("Clear Ancillary");
    JMenuItem refreshAncillaryJMenuItem = new JMenuItem("Refresh Ancillary");

    public L2genGetAncillarySplitButton(L2genData l2genData) {

        this.l2genData = l2genData;

        ancillarySplitButton = new JideSplitButton();
        ancillarySplitButton.setFont((Font) JideSwingUtilities.getMenuFont(Toolkit.getDefaultToolkit(), UIManager.getDefaults()));
        ancillarySplitButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        ancillarySplitButton.setBorderPainted(true);
        ancillarySplitButton.setHorizontalAlignment(SwingConstants.CENTER);


        ancillarySplitButton.add(getAncillaryJMenuItem);
        ancillarySplitButton.add(clearAncillaryJMenuItem);
        ancillarySplitButton.add(refreshAncillaryJMenuItem);


        ancillarySplitButton.setText(getAncillaryJMenuItem.getText() + " ");
        ancillarySplitButton.setPreferredSize(ancillarySplitButton.getPreferredSize());
        ancillarySplitButton.setMaximumSize(ancillarySplitButton.getPreferredSize());
        ancillarySplitButton.setMinimumSize(ancillarySplitButton.getPreferredSize());
        ancillarySplitButton.setText(getAncillaryJMenuItem.getText());

        addControlListeners();
        addEventListeners();
    }

    private void addControlListeners() {

        ancillarySplitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                l2genData.setAncillaryFiles();
            }
        });


        getAncillaryJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                l2genData.setAncillaryFiles();
            }
        });



        clearAncillaryJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                l2genData.clearAncillaryFiles();
            }
        });

        refreshAncillaryJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                l2genData.refreshAncillaryFiles();
            }
        });



    }

    private void addEventListeners() {
        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                ancillarySplitButton.setEnabled(l2genData.isValidIfile());
            }
        });

    }

    public JideSplitButton getAncillarySplitButton() {
        return ancillarySplitButton;
    }
}
