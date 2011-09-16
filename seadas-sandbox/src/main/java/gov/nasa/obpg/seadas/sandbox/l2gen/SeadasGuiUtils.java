package gov.nasa.obpg.seadas.sandbox.l2gen;

import java.awt.*;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class SeadasGuiUtils {

    public SeadasGuiUtils() {
    }


    public static GridBagConstraints makeConstraints (int gridx, int gridy) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = gridy;

        return c;
    }

    public static GridBagConstraints makeConstraints (int gridx, int gridy, int anchor) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = gridy;
        c.anchor = anchor;

        return c;
    }
}
