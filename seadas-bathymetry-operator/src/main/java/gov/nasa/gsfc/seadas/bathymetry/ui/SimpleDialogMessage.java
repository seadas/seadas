package gov.nasa.gsfc.seadas.bathymetry.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 1/16/13
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */


public class SimpleDialogMessage extends JDialog {


    public SimpleDialogMessage(String title, String message) {

        JButton okayButton = new JButton("Okay");
        okayButton.setPreferredSize(okayButton.getPreferredSize());
        okayButton.setMinimumSize(okayButton.getPreferredSize());
        okayButton.setMaximumSize(okayButton.getPreferredSize());
        okayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });

        JLabel jLabel = new JLabel(message);


        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.add(jLabel,
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        jPanel.add(okayButton,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));


        add(jPanel);

        setModalityType(ModalityType.APPLICATION_MODAL);


        setTitle(title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();


        setPreferredSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setSize(getPreferredSize());

    }
}