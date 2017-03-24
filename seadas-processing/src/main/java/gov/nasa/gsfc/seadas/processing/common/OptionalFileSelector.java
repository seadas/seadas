package gov.nasa.gsfc.seadas.processing.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/9/12
 * Time: 9:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class OptionalFileSelector extends JPanel {

    private JFileChooser fileChooser = new JFileChooser();


    private String currentFileName;
    private JTextField jTextField;

    //private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    public OptionalFileSelector(String optionalFileName) {

        createFileChooserPanel(optionalFileName);
        // this.pm = processorModel;
    }

    protected File getFileName(File inputFile, String extension) {

        String fileName = inputFile.getName();
        fileName = fileName.substring(0, fileName.indexOf(".")).concat(extension);
        //System.out.println(" new file Name = " + fileName);
        return new File(inputFile.getParentFile(), fileName);
    }

    protected void setFileName(File optionalFile) {

        currentFileName = optionalFile.getName();
        jTextField.setText(currentFileName);
    }
    protected void createFileChooserPanel(String optionName) {

        //final JLabel jLabel = new JLabel(L2genData.GEOFILE);
        final JLabel jLabel = new JLabel(optionName);

        final JButton jButton = new JButton("...");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooserHandler(fileChooser);
            }
        });

        jTextField = new JTextField("123456789 123456789 12345");
        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentFileName = jTextField.getText();
            }
        });

        jTextField.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                currentFileName = jTextField.getText();
            }
        });

        jTextField.setPreferredSize(jTextField.getPreferredSize());
        jTextField.setMinimumSize(jTextField.getPreferredSize());
        jTextField.setMaximumSize(jTextField.getPreferredSize());
        jTextField.setText("");


        jButton.setMargin(new Insets(0, -7, 0, -7));
        final Dimension size = new Dimension(jButton.getPreferredSize().width,
                jTextField.getPreferredSize().height);
        jButton.setPreferredSize(size);
        jButton.setMinimumSize(size);
        jButton.setMaximumSize(size);


        add(jLabel,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
        add(jTextField,
                new GridBagConstraintsCustom(1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 2));
        add(jButton,
                new GridBagConstraintsCustom(2, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));

    }

    private void fileChooserHandler(JFileChooser jFileChooser) {
        int result = jFileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            //pm.updateParamInfo(L2genData.GEOFILE, jFileChooser.getSelectedFile().toString());
            jTextField.setText(jFileChooser.getSelectedFile().toString());
        }
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    protected JTextField getFileNameField(){
        return jTextField;
    }

//    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
//        //propertyChangeSupport.addPropertyChangeListener(l);
//        //propertyChangeSupport.addPropertyChangeListener(propertyName, l);
//        addPropertyChangeListener(propertyName, l);
//    }
//
//    public void removePropertyChangeListener(PropertyChangeListener l) {
//        propertyChangeSupport.removePropertyChangeListener(l);
//    }


}
