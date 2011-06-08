package gov.nasa.obpg.seadas.sandbox.toolwindow;

/**
 * todo - Javadoc me!
 *
 * @author Danny Knowles
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class PixelInfo implements ActionListener {

    private JTextField jtfPixelLat;
    private JTextField jtfPixelLon;
    private JTextField jtfPixelVal;
    private JPanel contentPane;
    private JLabel jlabCoord;

    public PixelInfo() {
        JLabel jlabPixelLat = new JLabel("Lat: ");
        JLabel jlabPixelLon = new JLabel("Lon: ");
        JLabel jlabPixelVal = new JLabel("Val: ");
        JLabel jlabCoordLabel = new JLabel("Coordinates: ");
        jlabCoord = new JLabel("");

        jlabCoord.setBorder(BorderFactory.createEtchedBorder());

        jtfPixelLat = new JTextField(7);
        jtfPixelLon = new JTextField(7);
        jtfPixelVal = new JTextField(3);

        jtfPixelLat.setActionCommand("ChangeCoord");
        jtfPixelLon.setActionCommand("ChangeCoord");
        jtfPixelVal.setActionCommand("ChangeCoord");

        jtfPixelLat.addActionListener(this);
        jtfPixelLon.addActionListener(this);
        jtfPixelVal.addActionListener(this);

        contentPane = new JPanel();
        contentPane.setLayout(new FlowLayout());

        contentPane.add(jlabPixelLat);
        contentPane.add(jtfPixelLat);
        contentPane.add(jlabPixelLon);
        contentPane.add(jtfPixelLon);
        contentPane.add(jlabPixelVal);
        contentPane.add(jtfPixelVal);
        contentPane.add(jlabCoordLabel);
        contentPane.add(jlabCoord);
    }


    public void actionPerformed(ActionEvent ae) {

        if (ae.getActionCommand().equals("ChangeCoord")) {
            jlabCoord.setText("<html>Lat: " + jtfPixelLat.getText() + " <br>Lon: " + jtfPixelLon.getText() + "<br>Val: " + jtfPixelVal.getText());
        }


    }

    public static void main(String args[]) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                PixelInfo pixelInfo = new PixelInfo();
                JFrame jfrm = new JFrame("Pixel Info");

                jfrm.setLayout(new FlowLayout());

                jfrm.setSize(340, 120);

                jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                jfrm.setContentPane(pixelInfo.getContentPane());
                jfrm.setVisible(true);
            }
        });
    }

    public JPanel getContentPane() {
        return contentPane;
    }

}




