package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.common.FileSelector;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.esa.snap.rcp.SnapApp;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/12/12
 * Time: 4:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genParamFileSelector {

    private ParamInfo paramInfo;
    private L2genData l2genData;

    private JLabel jLabel;
    private JPanel jPanel;
    private FileSelector fileSelectorPanel;

    private boolean controlHandlerEnabled = true;

    public L2genParamFileSelector(L2genData l2genData, ParamInfo paramInfo) {

        this.l2genData = l2genData;
        this.paramInfo = paramInfo;

        jLabel = new JLabel(paramInfo.getName());
        jLabel.setToolTipText(paramInfo.getDescription());

//        FileSelector.Type type = null;
//        if (paramInfo.getType() == ParamInfo.Type.IFILE) {
//            type = FileSelector.Type.IFILE;
//        } else if (paramInfo.getType() == ParamInfo.Type.OFILE) {
//            type = FileSelector.Type.OFILE;
//        }

        if (paramInfo.getType() != null) {
            fileSelectorPanel = new FileSelector(SnapApp.getDefault().getAppContext(), paramInfo.getType());
            jPanel = fileSelectorPanel.getjPanel();
        }

        addControlListeners();
        addEventListeners();
    }

    private void addControlListeners() {
        fileSelectorPanel.addPropertyChangeListener(fileSelectorPanel.getPropertyName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (isControlHandlerEnabled()) {
                    l2genData.setParamValue(paramInfo.getName(), fileSelectorPanel.getFileName());
                }
            }
        });
    }

    private void addEventListeners() {

        l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                disableControlHandler();
                fileSelectorPanel.setFilename(l2genData.getParamValue(paramInfo.getName()));
                enableControlHandler();
            }
        });
    }

    private boolean isControlHandlerEnabled() {
        return controlHandlerEnabled;
    }

    private void enableControlHandler() {
        controlHandlerEnabled = true;
    }

    private void disableControlHandler() {
        controlHandlerEnabled = false;
    }

    public JLabel getjLabel() {
        return jLabel;
    }

    public JPanel getjPanel() {
        return jPanel;
    }
}
