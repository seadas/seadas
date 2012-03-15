package gov.nasa.gsfc.seadas.processing.l2gen;

import javax.swing.*;
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


    public static GridBagConstraints makeConstraints(int gridx, int gridy) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = gridy;

        return c;
    }

    public static GridBagConstraints makeConstraints(int gridx, int gridy, int anchor) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = gridy;
        c.anchor = anchor;

        return c;
    }

    public static JPanel addWrapperPanel(Object myMainPanel) {
        JPanel myWrapperPanel = new JPanel();
        myWrapperPanel.setLayout(new GridBagLayout());

        final GridBagConstraints c;
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1;
        c.weighty = 1;

        myWrapperPanel.add((Component) myMainPanel, c);

        return myWrapperPanel;
    }


    public static JPanel addPaddedWrapperPanel(Object myMainPanel, int pad, int anchor) {

        JPanel myWrapperPanel = new JPanel();
     //   myWrapperPanel.setBorder(BorderFactory.createTitledBorder(""));
        myWrapperPanel.setLayout(new GridBagLayout());

        final GridBagConstraints c;
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = anchor;
        c.insets = new Insets(pad, pad, pad, pad);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1;
        c.weighty = 1;

        myWrapperPanel.add((Component) myMainPanel, c);

        return myWrapperPanel;
    }


    public static JPanel addPaddedWrapperPanel(Object myMainPanel, int pad, int anchor, int fill) {

        JPanel myWrapperPanel = new JPanel();
        myWrapperPanel.setBorder(BorderFactory.createTitledBorder(""));
        myWrapperPanel.setLayout(new GridBagLayout());

        final GridBagConstraints c;
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = anchor;
        c.insets = new Insets(pad, pad, pad, pad);
        c.fill = fill;
        c.weightx = 1;
        c.weighty = 1;

        myWrapperPanel.add((Component) myMainPanel, c);

        return myWrapperPanel;
    }



   public static void padPanel(Object innerPanel, JPanel outerPanel, int pad) {

        final GridBagConstraints c;
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(pad, pad, pad, pad);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        outerPanel.add((Component) innerPanel, c);
    }



    public static JPanel addPaddedWrapperPanel(Object myMainPanel, int pad) {

        JPanel myWrapperPanel = new JPanel();

        myWrapperPanel.setLayout(new GridBagLayout());

        final GridBagConstraints c;
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(pad, pad, pad, pad);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        myWrapperPanel.add((Component) myMainPanel, c);

        return myWrapperPanel;
    }

}
