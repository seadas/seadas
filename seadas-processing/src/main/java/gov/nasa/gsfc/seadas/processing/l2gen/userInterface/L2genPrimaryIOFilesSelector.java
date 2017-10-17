package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;


import gov.nasa.gsfc.seadas.processing.core.SeaDASProcessorModel;
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


    public L2genPrimaryIOFilesSelector(SeaDASProcessorModel seaDASProcessorModel) {
        ifileSelector = new L2genIfileSelector(seaDASProcessorModel);
        geofileSelector = new L2genGeofileSelector(seaDASProcessorModel);
        ofileSelector = new L2genOfileSelector(seaDASProcessorModel);
        createJPanel(seaDASProcessorModel);
    }


    public void createJPanel(SeaDASProcessorModel seaDASProcessorModel) {
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
