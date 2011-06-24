package gov.nasa.obpg.seadas.sandbox.toolwindow;

import javax.swing.*;
import java.awt.*;

/**
 * todo - Javadoc me!
 *
 * @author Danny Knowles
 */
public class TabbedPaneWithPanels {

    JCheckBox jcbDVD;
    JCheckBox jcbScanner;
    JCheckBox jcbNtwrkRdy;

    JCheckBox jcbWordProc;
    JCheckBox jcbCompiler;
    JCheckBox jcbDatabase;

    JRadioButton jrbTower;
    JRadioButton jrbNotebook;
    JRadioButton jrbHandheld;

    private JPanel contentPane;

    public TabbedPaneWithPanels() {

        jrbTower = new JRadioButton("Tower");
        jrbNotebook = new JRadioButton("NotebookNotebookNotebookNotebookNotebook");
        jrbHandheld = new JRadioButton("Handheld");
        ButtonGroup bg = new ButtonGroup();
        bg.add(jrbTower);
        bg.add(jrbNotebook);
        bg.add(jrbHandheld);

        JPanel jpnl = new JPanel();
        jpnl.setLayout(new GridLayout(3, 1));
        jpnl.setOpaque(true);

        jpnl.add(jrbTower);
        jpnl.add(jrbNotebook);
        jpnl.add(jrbHandheld);

        jcbDVD = new JCheckBox();
        jcbScanner = new JCheckBox("Scanner");
        jcbNtwrkRdy = new JCheckBox("Network Ready");

        jcbDVD.setText("DVD Burner");

        JPanel jpnl2 = new JPanel();
        jpnl2.setLayout(new GridLayout(3, 1));
        jpnl2.setOpaque(true);

        jpnl2.add(jcbDVD);
        jpnl2.add(jcbScanner);
        jpnl2.add(jcbNtwrkRdy);

        jcbWordProc = new JCheckBox("Word Processing");
        jcbCompiler = new JCheckBox("Program Development");
        jcbDatabase = new JCheckBox("Database");

        JPanel jpnl3 = new JPanel();
        jpnl3.setLayout(new GridLayout(3, 1));
        jpnl3.setOpaque(true);

        jpnl3.add(jcbWordProc);
        jpnl3.add(jcbCompiler);
        jpnl3.add(jcbDatabase);

        JTabbedPane jtp = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        jtp.addTab("Style", jpnl);
        jtp.addTab("Options", jpnl2);
        jtp.addTab("Software", jpnl3);

        contentPane = new JPanel();

        contentPane.add(jtp);
    }

    public JComponent getContentPane() {
        return contentPane;
    }


    public static void main(String args[]) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                TabbedPaneWithPanels tabbedFrame = new TabbedPaneWithPanels();
                JFrame jfrm = new JFrame("Tabbed Frame");

                jfrm.setSize(340, 120);

                jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                jfrm.setContentPane(tabbedFrame.getContentPane());
                jfrm.setVisible(true);
            }
        });
    }
}
