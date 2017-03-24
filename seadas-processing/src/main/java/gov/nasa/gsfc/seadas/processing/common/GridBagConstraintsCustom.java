package gov.nasa.gsfc.seadas.processing.common;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 4/24/12
 * Time: 9:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class GridBagConstraintsCustom extends GridBagConstraints {

    // Creates a GridBagConstraints class with modifications to some of the defaults
    // This class is usefull because the GridBagConstraints class does not contain many constructor choices
    // This class enables setting several params conveniently and cleanly without a lot of lines of code

    // To best know which constructor to use here are the default values:
    // DEFAULT VALUES
    // gridx = 0
    // gridy = 0
    // weightx = 0
    // weighty = 0
    // anchor = CENTER
    // fill = NONE
    // insets = new Insets(0, 0, 0, 0)  which means for this class: pad = 0
    // gridwidth = 1
    // gridheight = 1

    // NOTE that the order is consistent relative to each constructor however the order of the GridBagConstraints
    // full constructor differs slightly for this custom version.  This is because this custom class is built based
    // on the order of usefullness of each param.  This is a subjective decision based on our current coding.  Essentially
    // gridwidth and gridheight have been shifted to be a later param in the list in this custom version.  The rest
    // of the params are maintained in the GridBagConstraints order.


    // identical to GridBagConstraints
    // just a convenience constructor for consistency
    // EXAMPLE:
    // myPanel.add(myComponent, new GridBagConstraintsCustom());
    public GridBagConstraintsCustom() {
    }

   // EXAMPLE:
   // myPanel.add(myComponent, new GridBagConstraintsCustom(0, 2));
    public GridBagConstraintsCustom(int gridx, int gridy) {
        this.gridx = gridx;
        this.gridy = gridy;
    }


    // EXAMPLE:
    // myPanel.add(myComponent, new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.CENTER));
    public GridBagConstraintsCustom(int gridx, int gridy, double weightx, double weighty, int anchor) {
        this(gridx, gridy);
        this.weightx = weightx;
        this.weighty = weighty;
        this.anchor = anchor;
    }

    // EXAMPLE:
    // myPanel.add(myComponent, new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
    public GridBagConstraintsCustom(int gridx, int gridy, double weightx, double weighty, int anchor, int fill) {
        this(gridx, gridy, weightx, weighty, anchor);
        this.fill = fill;
    }

    // Enables uniform cell padding (left, top, right, bottom) with one variable
    // EXAMPLE:
    // myPanel.add(myComponent,new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2));
    public GridBagConstraintsCustom(int gridx, int gridy, double weightx, double weighty, int anchor, int fill, int pad) {
        this(gridx, gridy, weightx, weighty, anchor, fill);
        this.insets = new Insets(pad, pad, pad, pad);
    }

    // Cell padding (left, top, right, bottom) is not uniform so needs to use Insets
    // The example below shows how to make a cell's contents potentially expand outside of the cell
    // this example is useful in the case of a table type layout where the length of the column title would normally
    // cause the column to expand wider than is desired
    // EXAMPLE:
    // myPanel.add(myComponent,new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, -6, 0, -6)));
    public GridBagConstraintsCustom(int gridx, int gridy, double weightx, double weighty, int anchor, int fill, Insets insets) {
        this(gridx, gridy, weightx, weighty, anchor, fill);
        this.insets = insets;
    }

    // EXAMPLE:
    // myPanel.add(myComponent,new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 3));
    public GridBagConstraintsCustom(int gridx, int gridy, double weightx, double weighty, int anchor, int fill, int pad, int gridwidth) {
        this(gridx, gridy, weightx, weighty, anchor, fill, pad);
        this.gridwidth = gridwidth;
    }

    // EXAMPLE:
    // myPanel.add(myComponent,new GridBagConstraintsCustom(0, 2, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 3, 3));
    public GridBagConstraintsCustom(int gridx, int gridy, double weightx, double weighty, int anchor, int fill, int pad, int gridwidth, int gridheight) {
        this(gridx, gridy, weightx, weighty, anchor, fill, pad, gridwidth);
        this.gridheight = gridheight;
    }

}
