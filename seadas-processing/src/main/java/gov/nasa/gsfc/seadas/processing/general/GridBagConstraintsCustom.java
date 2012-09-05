package gov.nasa.gsfc.seadas.processing.general;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 4/24/12
 * Time: 9:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class GridBagConstraintsCustom extends GridBagConstraints {

    public GridBagConstraintsCustom(int gridx, int gridy) {
        this.gridx = gridx;
        this.gridy = gridy;
    }

    public GridBagConstraintsCustom(int gridx, int gridy, double weightx, double weighty, int anchor, int fill) {
        this.gridx = gridx;
        this.gridy = gridy;
        this.weightx = weightx;
        this.weighty = weighty;
        this.anchor = anchor;
        this.fill = fill;
    }

    public GridBagConstraintsCustom(int gridx, int gridy, double weightx, double weighty, int anchor, int fill, int pad) {
        this.gridx = gridx;
        this.gridy = gridy;
        this.weightx = weightx;
        this.weighty = weighty;
        this.anchor = anchor;
        this.fill = fill;
        this.insets = new Insets(pad, pad, pad, pad);
    }

    public GridBagConstraintsCustom(int gridx, int gridy, double weightx, double weighty, int anchor, int fill, int pad, int gridwidth) {
        this.gridx = gridx;
        this.gridy = gridy;
        this.weightx = weightx;
        this.weighty = weighty;
        this.anchor = anchor;
        this.fill = fill;
        this.insets = new Insets(pad, pad, pad, pad);
        this.gridwidth = gridwidth;
    }


}
