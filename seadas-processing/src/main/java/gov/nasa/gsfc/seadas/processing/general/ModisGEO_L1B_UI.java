package gov.nasa.gsfc.seadas.processing.general;

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

    public ModisGEO_L1B_UI(String programName, String xmlFileName) {
        super(programName, xmlFileName);
    }

    private void initMissionNameMap() {

    }

    @Override
    public JPanel getParamPanel() {
        initMissionNameMap();
        final LUTManager lutManager = new LUTManager();
        processorModel.addPropertyChangeListener(processorModel.getPrimaryInputFileOptionName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                String missionName = getMissionName(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
                if (missionName != null && missionName.trim().length() > 0) {
                    System.out.println("mission name: " + missionName);
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

    private String getMissionName(String ifilePath) {
        FileInfo fileInfo = new FileInfo(ifilePath);
        String missionName = fileInfo.getMissionName().toLowerCase();

        if (missionName.contains("aqua")) {
            missionName = "aqua";
        } else if (missionName.contains("terra")) {
            missionName = "terra";
        } else if (missionName.contains("seawifs")) {
            missionName = "seawifs";
        } else if (missionName.contains("aquarius")) {
            missionName = "aquarius";
        }
        return missionName;
    }

    private Component findJPanel(Component comp, String panelName) {
        System.out.println(comp.getClass() + "  " + panelName);
        if (comp.getClass() == JPanel.class) return comp;
        if (comp instanceof Container) {
            Component[] components = ((Container) comp).getComponents();
            System.out.println("number of comps: " + components.length);
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
