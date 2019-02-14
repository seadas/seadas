package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.ocssw.OCSSW;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 10/4/12
 * Time: 3:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModisGEO_L1B_UI extends ProgramUIFactory {

    HashMap<String, String> validLutMissionNameMap;
    OCSSW ocssw;
    final LUTManager lutManager;

    public ModisGEO_L1B_UI(String programName, String xmlFileName, OCSSW ocssw) {
        super(programName, xmlFileName, ocssw);
        this.ocssw = ocssw;
        lutManager = new LUTManager(ocssw);
    }

    private void initMissionNameMap() {

    }

    @Override
    public JPanel getParamPanel() {
        initMissionNameMap();

        JPanel paramPanel = super.getParamPanel();
        processorModel.addPropertyChangeListener(processorModel.getPrimaryInputFileOptionName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                String missionName = getMissionName();
                if (missionName != null && missionName.trim().length() > 0) {
                    lutManager.enableLUTButton(missionName);
                } else {
                    lutManager.disableLUTButton();
                }

            }
        });

        JScrollPane scrollPane = (JScrollPane) paramPanel.getComponent(0);
        if (lutManager != null) {
            final JPanel lutPanel = (JPanel) findJButton(scrollPane, paramPanel.getName());
            lutPanel.remove(0);
            lutPanel.add(lutManager.getLUTButton());
            lutPanel.revalidate();
            lutPanel.repaint();
            paramPanel.revalidate();
            paramPanel.repaint();
        }

        return paramPanel;
    }


    /*
     * usage: update_luts.py [-h] [-e] [-v] [-n] [--timeout TIMEOUT] MISSION

     * Retrieve latest lookup tables for specified sensor.

     * positional arguments:
     * MISSION            sensor or platform to process; one of:
                     seawifs, aquarius, modisa, modist, viirsn, viirsj1, aqua, terra, npp, j1
     */
    private String getMissionName() {
        String missionName = processorModel.getOcssw().getMissionName();
        if (missionName != null) {
            missionName = missionName.toLowerCase();
            if (missionName.contains("aqua")) {
                missionName = "aqua";
            } else if (missionName.contains("terra")) {
                missionName = "terra";
            } else if (missionName.contains("seawifs")) {
                missionName = "seawifs";
            } else if (missionName.contains("aquarius")) {
                missionName = "aquarius";
            } else if (missionName.contains("viirsn") ) {

                missionName = "viirsn";
            } else if (missionName.contains("viirsji") ) {

                missionName = "viirsj1";
            }
        }
        return missionName;
    }

    private Component findJPanel(Component comp, String panelName) {

        if (comp.getClass() == JPanel.class) return comp;
        if (comp instanceof Container) {
            Component[] components = ((Container) comp).getComponents();
            for (int i = 0; i < components.length; i++) {
                Component child = findJPanel(components[i], components[i].getName());
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }

    private Component findJButton(Component comp, String panelName) {

        System.out.println(comp.getClass() + "  " + comp.getName());
        if (comp.getClass() == JButton.class && comp.getName() != null && comp.getName().contains("actionButton")) return comp.getParent();
        if (comp instanceof Container) {
            Component[] components = ((Container) comp).getComponents();
            for (int i = 0; i < components.length; i++) {
                Component child = findJButton(components[i], components[i].getName());
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }
}
