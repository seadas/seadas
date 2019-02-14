package gov.nasa.gsfc.seadas.processing.common;

import com.bc.ceres.swing.TableLayout;
import gov.nasa.gsfc.seadas.OCSSWInfo;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 10/4/12
 * Time: 3:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class LUTManager {
    private final String UPDATE_LUTS_PROCESSOR_NAME = "update_luts.py";
    private String missionName;
    private JButton lutButton;
    OCSSW ocssw;
    boolean enableUpdateLuts;

    public LUTManager(OCSSW ocssw) {
        this.ocssw = ocssw;
        lutButton = new JButton();
        lutButton.setEnabled(false);
        lutButton.setName("Update Luts");
        lutButton.setText("Update LUTS");
        lutButton.setToolTipText("Click to update Look Up Tables");
        lutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                updateLUT();
            }
        });
        enableUpdateLuts = false;
    }

    protected JButton getLUTButton() {
        return lutButton;
    }

    private void updateLUT() {
        String[] lutCmdArray = {OCSSWInfo.getInstance().getOcsswRunnerScriptPath(), UPDATE_LUTS_PROCESSOR_NAME, missionName};
        String[] lutCmdArrayParams = {missionName};
        Process process = ocssw.execute(lutCmdArray);
        process = ocssw.execute(UPDATE_LUTS_PROCESSOR_NAME, lutCmdArrayParams);
        try {
            int exitValue = process.waitFor();
            VisatApp.getApp().showMessageDialog("", "LUTs updated", ModalDialog.ID_OK, null);

        } catch (Exception e) {
            SeadasLogger.getLogger().severe("Execution exception 0 : " + e.getMessage());
            VisatApp.getApp().showMessageDialog("", "WARNING: LUTs update failed", ModalDialog.ID_OK, null);

        }
        //System.out.println("update_luts exit value = " + process.exitValue());
    }

    protected void enableLUTButton(String missionName) {
        this.missionName = missionName;
        lutButton.setEnabled(true);
    }

    protected void disableLUTButton() {
        lutButton.setEnabled(false);
    }

    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
        //enableLUTButton();
    }

    protected JPanel getLUTPanel() {
        JPanel lutPanel = new JPanel();
        lutPanel.setLayout(new TableLayout(1));
        lutButton.setEnabled(enableUpdateLuts);
        lutPanel.add(lutButton);
        return lutPanel;
    }
}
