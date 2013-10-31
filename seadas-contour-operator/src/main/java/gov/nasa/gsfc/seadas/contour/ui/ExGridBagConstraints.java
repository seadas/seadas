package gov.nasa.gsfc.seadas.contour.ui;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 9/4/12
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExGridBagConstraints extends GridBagConstraints {

    public ExGridBagConstraints(int gridx, int gridy) {
        this.gridx = gridx;
        this.gridy = gridy;
    }

    public ExGridBagConstraints(int gridx, int gridy, double weightx, double weighty, int anchor, int fill) {
        this.gridx = gridx;
        this.gridy = gridy;
        this.weightx = weightx;
        this.weighty = weighty;
        this.anchor = anchor;
        this.fill = fill;
    }

    public ExGridBagConstraints(int gridx, int gridy, double weightx, double weighty, int anchor, int fill, int pad) {
        this.gridx = gridx;
        this.gridy = gridy;
        this.weightx = weightx;
        this.weighty = weighty;
        this.anchor = anchor;
        this.fill = fill;
        this.insets = new Insets(pad, pad, pad, pad);
    }

    public ExGridBagConstraints(int gridx, int gridy, double weightx, double weighty, int anchor, int fill, Insets insets) {
        this.gridx = gridx;
        this.gridy = gridy;
        this.weightx = weightx;
        this.weighty = weighty;
        this.anchor = anchor;
        this.fill = fill;
        this.insets = insets;
    }

    public ExGridBagConstraints(int gridx, int gridy, double weightx, double weighty, int anchor, int fill, int pad, int gridwidth) {
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