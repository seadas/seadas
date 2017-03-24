package gov.nasa.gsfc.seadas.processing.common;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class SeadasGuiUtils {

    public SeadasGuiUtils() {
    }


    public static String importFile(JFileChooser jFileChooser) {

        StringBuilder stringBuilder;

        int result = jFileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            final ArrayList<String> parfileTextLines = myReadDataFile(jFileChooser.getSelectedFile().toString());

            stringBuilder = new StringBuilder();

            for (String currLine : parfileTextLines) {
                stringBuilder.append(currLine);
                stringBuilder.append("\n");
            }

            return stringBuilder.toString();
        }

        return null;
    }


    public static void exportFile(JFileChooser fileChooser, String contents) {
        int result = fileChooser.showSaveDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Create file
                FileWriter fstream = new FileWriter(fileChooser.getSelectedFile().toString());
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(contents);
                //Close the output stream
                out.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }
        }
    }


    private static ArrayList<String> myReadDataFile
            (String
                     fileName) {
        String lineData;
        ArrayList<String> fileContents = new ArrayList<String>();
        BufferedReader moFile = null;
        try {
            moFile = new BufferedReader(new FileReader(new File(fileName)));
            while ((lineData = moFile.readLine()) != null) {

                fileContents.add(lineData);
            }
        } catch (IOException e) {
            ;
        } finally {
            try {
                moFile.close();
            } catch (Exception e) {
                //Ignore
            }
        }
        return fileContents;
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
