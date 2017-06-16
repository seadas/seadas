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

    public ModisGEO_L1B_UI(String programName, String xmlFileName, OCSSW ocssw) {
        super(programName, xmlFileName, ocssw);
        this.ocssw = ocssw;
    }

    private void initMissionNameMap() {

    }

    @Override
    public JPanel getParamPanel() {
        initMissionNameMap();
        final LUTManager lutManager = new LUTManager(ocssw);
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
        JPanel paramPanel = super.getParamPanel();
        JScrollPane scrollPane = (JScrollPane) paramPanel.getComponent(0);
        ((JPanel) findJPanel(scrollPane, paramPanel.getName())).add(lutManager.getLUTPanel());
        return paramPanel;
    }

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
}
