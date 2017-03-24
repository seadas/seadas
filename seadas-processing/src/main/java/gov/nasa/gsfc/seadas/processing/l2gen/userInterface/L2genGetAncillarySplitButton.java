package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import com.jidesoft.swing.JideSwingUtilities;
import gov.nasa.gsfc.seadas.processing.core.L2genData;

import javax.swing.*;
//import javax.swing.border.BevelBorder;
//import javax.swing.BorderFactory;
//
//import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
//import javax.swing.plaf.ButtonUI;


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
    JMenuItem refreshAncillaryJMenuItem = new JMenuItem("Refresh");
    JMenuItem nearRealTimeNO2AncillaryJMenuItem = new JMenuItem("Near Real-Time NO2");
    JMenuItem forceDownloadAncillaryJMenuItem = new JMenuItem("Force Download");

    public L2genGetAncillarySplitButton(L2genData l2genData) {

        this.l2genData = l2genData;

        ancillarySplitButton = new JideSplitButton();
        ancillarySplitButton.setFont((Font) JideSwingUtilities.getMenuFont(Toolkit.getDefaultToolkit(), UIManager.getDefaults()));
    //    ancillarySplitButton.setBorder(BorderFactory.createTitledBorder(""));
        ancillarySplitButton.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
   //     ancillarySplitButton.setButtonStyle(ButtonStyle.TOOLBOX_STYLE);



        ancillarySplitButton.setBorderPainted(true);
        ancillarySplitButton.setHorizontalAlignment(SwingConstants.CENTER);


        ancillarySplitButton.add(getAncillaryJMenuItem);
        ancillarySplitButton.add(refreshAncillaryJMenuItem);
        ancillarySplitButton.add(nearRealTimeNO2AncillaryJMenuItem);
        ancillarySplitButton.add(forceDownloadAncillaryJMenuItem);


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

                l2genData.setAncillaryFiles(false,false,false);
            }
        });


        getAncillaryJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                l2genData.setAncillaryFiles(false,false,false);
            }
        });



        refreshAncillaryJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                l2genData.setAncillaryFiles(true,false,false);
            }
        });

        forceDownloadAncillaryJMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                l2genData.setAncillaryFiles(false,true,false);
            }
        });

        nearRealTimeNO2AncillaryJMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                l2genData.setAncillaryFiles(false,false,true);
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
