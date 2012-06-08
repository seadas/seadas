package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;


import gov.nasa.gsfc.seadas.processing.core.L2genDataProcessorModel;
import gov.nasa.gsfc.seadas.processing.general.GridBagConstraintsCustom;

import javax.swing.*;
import java.awt.*;


/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/14/12
 * Time: 7:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genPrimaryIOFilesSelector {

    private JPanel jPanel;

    private L2genIfileSelector ifileSelector;
    private L2genGeofileSelector geofileSelector;
    private L2genOfileSelector ofileSelector;

    public L2genPrimaryIOFilesSelector(L2genDataProcessorModel l2genDataProcessorModel) {
        ifileSelector = new L2genIfileSelector(l2genDataProcessorModel);
        if (l2genDataProcessorModel.isRequiresGeofile()) {
            geofileSelector = new L2genGeofileSelector(l2genDataProcessorModel);
        }
        if (l2genDataProcessorModel.getPrimaryOutputFileOptionName() != null) {
            ofileSelector = new L2genOfileSelector(l2genDataProcessorModel);
        }
        createJPanel();
    }


    public void createJPanel() {
        jPanel = new JPanel(new GridBagLayout());
        jPanel.setBorder(BorderFactory.createTitledBorder("Primary I/O Files"));

        jPanel.add(ifileSelector.getJPanel(),
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        if (geofileSelector != null) {
            jPanel.add(geofileSelector.getJPanel(),
                    new GridBagConstraintsCustom(0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        }
        if (ofileSelector != null) {
            jPanel.add(ofileSelector.getJPanel(),
                    new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        }
    }


    public L2genIfileSelector getIfileSelector() {
        return ifileSelector;
    }

    public L2genGeofileSelector getGeofileSelector() {
        return geofileSelector;
    }

    public L2genOfileSelector getOfileSelector() {
        return ofileSelector;
    }

    public JPanel getjPanel() {
        return jPanel;
    }
}
