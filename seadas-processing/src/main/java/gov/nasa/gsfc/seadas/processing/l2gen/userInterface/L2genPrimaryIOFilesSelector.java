package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;


import gov.nasa.gsfc.seadas.processing.core.L2genData;
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

    L2genPrimaryIOFilesSelector(L2genData l2genData) {
        ifileSelector = new L2genIfileSelector(l2genData);
        geofileSelector = new L2genGeofileSelector(l2genData);
        ofileSelector = new L2genOfileSelector(l2genData);

        createJPanel();
    }


    public void createJPanel() {
        jPanel = new JPanel(new GridBagLayout());
        jPanel.setBorder(BorderFactory.createTitledBorder("Primary I/O Files"));

        jPanel.add(ifileSelector.getJPanel(),
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        jPanel.add(geofileSelector.getJPanel(),
                new GridBagConstraintsCustom(0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        jPanel.add(ofileSelector.getJPanel(),
                new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
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
