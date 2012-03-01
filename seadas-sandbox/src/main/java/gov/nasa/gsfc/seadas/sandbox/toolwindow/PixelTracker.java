package gov.nasa.gsfc.seadas.sandbox.toolwindow;

/**
 * todo - Javadoc me!
 *
 * @author Danny Knowles
 */



import com.bc.ceres.swing.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class PixelTracker implements ActionListener {

    private JTextField jtfPixelLat;
    private JTextField jtfPixelLon;
    private JTextField jtfPixelVal;
    private JPanel contentPane;
    private JLabel jlabCoord;

    private JButton element2;
    private JButton element4;
    private JLabel buttonResults;


    public PixelTracker() {
        ImageIcon myIcon = new ImageIcon("/home/knowles/SeaDAS/i_was_a_teenage_frankenstein.jpg");

        JLabel element0 = new JLabel(myIcon);
        element0.setSize(new Dimension(100, 100));
        JLabel jlabPixelLon = new JLabel("Lon: ");
        JLabel jlabPixelVal = new JLabel("Val: ");
        JLabel jlabCoordLabel = new JLabel("Coordinates: ");
        jlabCoord = new JLabel("hello");

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

        TableLayout layout = new TableLayout(3);
//        layout.setTableFill(TableLayout.Fill.BOTH);
        layout.setTableWeightX(1);
        layout.setTableWeightY(1);
//        layout.setCellAnchor(0, 0, TableLayout.Anchor.EAST);
//        layout.setCellAnchor(1, 0, TableLayout.Anchor.EAST);
//        layout.setCellAnchor(2,0, TableLayout.Anchor.EAST);


        layout.setCellRowspan(0, 0, 3);
        layout.setCellColspan(0, 1, 2);
        layout.setCellColspan(3, 0, 2);
        layout.setCellColspan(4, 1, 2);
        layout.setCellAnchor(0,1, TableLayout.Anchor.CENTER);
        layout.setCellFill(1,1, TableLayout.Fill.BOTH);



        JButton element1 = new JButton("Element1");
        JButton element3 = new JButton("Element3");
        element4 = new JButton("Element4");
                        element4.setActionCommand("ChangeCoord");
        element4.addActionListener(this);

        JButton element5 = new JButton("Element5");
        JButton element6 = new JButton("Element6");
        JButton element7 = new JButton("Element7");
        JButton element8 = new JButton("Element8");
        JButton element9 = new JButton("Element9");
        JButton element10 = new JButton("Element10");
        JButton element11 = new JButton("Element11");
        JButton element12 = new JButton("Element12");
        JButton element13 = new JButton("Element13");


        buttonResults = new JLabel("");
        JPanel innerPanel = myPanelButton(element2);








        contentPane = new JPanel();
        contentPane.setLayout(layout);

        contentPane.add(element0, new TableLayout.Cell(0, 0,3,1));
        contentPane.add(innerPanel, new TableLayout.Cell(0,1));
        contentPane.add(element3, new TableLayout.Cell(1, 1));
        contentPane.add(element4, new TableLayout.Cell(1, 2));
        contentPane.add(element5, new TableLayout.Cell(2, 1));
        contentPane.add(element6, new TableLayout.Cell(2, 2));
        contentPane.add(element7, new TableLayout.Cell(3, 0));
        contentPane.add(element8, new TableLayout.Cell(3, 2));
//        contentPane.add(element9, new TableLayout.Cell(4, 0));
        contentPane.add(element10, new TableLayout.Cell(4, 1));
        contentPane.add(element11, new TableLayout.Cell(5, 0));
        contentPane.add(jtfPixelLat, new TableLayout.Cell(5, 1));
        contentPane.add(jlabCoord, new TableLayout.Cell(5, 2));

    }


    public JPanel myPanelButton(JButton myButton) {

        JPanel myPanel = new JPanel();

        TableLayout myLayout = new TableLayout(2);
        myPanel.setLayout(myLayout);

        JLabel myLabel = new JLabel("Here is a Button: ");
        myButton = new JButton("Push ME!");
        myButton.setActionCommand("myButton");
        myButton.addActionListener(this);
        myPanel.add(myLabel);
        myPanel.add(myButton);

        return myPanel;
    }





    public void actionPerformed(ActionEvent ae) {

        if (ae.getActionCommand().equals("ChangeCoord")) {
            jlabCoord.setText("<html>Lat: " + jtfPixelLat.getText() + " <br>Lon: " + jtfPixelLon.getText() + "<br>Val: " + jtfPixelVal.getText());
        }

        if (ae.getActionCommand().equals("myButton")) {
            jlabCoord.setText("<html>button");
        }


    }

    public static void main(String args[]) {

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {

        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                PixelTracker pixelTracker = new PixelTracker();
                JFrame jfrm = new JFrame("Pixel Info");
                jfrm.setLayout(new FlowLayout());
                jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                jfrm.setContentPane(pixelTracker.getContentPane());
                jfrm.pack();
                jfrm.setVisible(true);
            }
        });
    }

    public JPanel getContentPane() {
        return contentPane;
    }

}




