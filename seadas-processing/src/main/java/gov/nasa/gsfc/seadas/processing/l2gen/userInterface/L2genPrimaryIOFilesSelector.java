package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;


import gov.nasa.gsfc.seadas.processing.core.L2genDataProcessorModel;
import gov.nasa.gsfc.seadas.processing.common.GridBagConstraintsCustom;

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
        geofileSelector = new L2genGeofileSelector(l2genDataProcessorModel);
        ofileSelector = new L2genOfileSelector(l2genDataProcessorModel);
        createJPanel(l2genDataProcessorModel);
    }


    public void createJPanel(L2genDataProcessorModel l2genDataProcessorModel) {
        jPanel = new JPanel(new GridBagLayout());
        jPanel.setBorder(BorderFactory.createTitledBorder("Primary I/O Files"));

        int gridy = 0;

        jPanel.add(ifileSelector.getJPanel(),
                new GridBagConstraintsCustom(0, gridy, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        gridy++;

        jPanel.add(geofileSelector.getJPanel(),
                new GridBagConstraintsCustom(0, gridy, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        gridy++;

        jPanel.add(ofileSelector.getJPanel(),
                new GridBagConstraintsCustom(0, gridy, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
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
