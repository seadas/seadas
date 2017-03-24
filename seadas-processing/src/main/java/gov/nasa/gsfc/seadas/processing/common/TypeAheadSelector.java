package gov.nasa.gsfc.seadas.processing.common;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicDirectoryModel;
import javax.swing.plaf.basic.BasicFileChooserUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 9/6/12
 * Time: 12:05 PM
 * This code is taken from http://www.javaworld.com/jw-02-2001/jw-0216-jfile.html
 */
public class TypeAheadSelector extends KeyAdapter
                               implements PropertyChangeListener {
    private JFileChooser chooser;
    private StringBuffer partialName = new StringBuffer();
    private Vector files;
    private boolean resetPartialName = true;
    public TypeAheadSelector(JFileChooser chooser) {
        try {
               UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
           } catch (Exception e) {
               e.printStackTrace();
           }
        this.chooser = chooser;
        //Component comp = findJList(chooser);
        Component comp = findJTextField(chooser);
        comp.addKeyListener(this);
        setListDataListener();
        chooser.addPropertyChangeListener(this);
    }
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if (prop.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            if (resetPartialName) partialName.setLength(0);
            resetPartialName = true;
        }
    }
    private void setListDataListener() {
        final BasicDirectoryModel model =
            ((BasicFileChooserUI)chooser.getUI()).getModel();
        model.addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent lde) {
                Vector buffer = model.getFiles();
                if (buffer.size() > 0) {
                    files = buffer;
                }
            }
            public void intervalAdded(ListDataEvent lde) {}
            public void intervalRemoved(ListDataEvent lde) {}
        });
    }
    public void keyTyped(KeyEvent ke) {
        if (ke.getKeyChar() == KeyEvent.VK_ENTER) {
            if (chooser.getSelectedFile().isFile()) chooser.approveSelection();
        }
        partialName.append(ke.getKeyChar());
        String upperCasePartialName = partialName.toString().toUpperCase();
        for(int i = 0; i < files.size(); i++) {
            File item = (File)files.get(i);
            String name = item.getName().toUpperCase();
            if (name.startsWith(upperCasePartialName)) {
                resetPartialName = false;
                chooser.setSelectedFile(item);
                return;
            }
        }
    }
    private Component findJList(Component comp) {
        //System.out.println(comp.getClass());
        if (comp.getClass() == JList.class) return comp;
        if (comp instanceof Container) {
            Component[] components = ((Container) comp).getComponents();
            for (int i = 0; i < components.length; i++) {
                Component child = findJList(components[i]);
                if (child != null)
                    return child;
            }
        }
        return null;
    }

    private Component findJTextField(Component comp) {
         //System.out.println(comp.getClass() + "     " + JTextField.class);
         if (comp.getClass().equals(JTextField.class)) return comp;
         if (comp instanceof Container) {
             Component[] components = ((Container) comp).getComponents();
             for (int i = 0; i < components.length; i++) {
                 Component child = findJTextField(components[i]);
                 if (child != null)
                     return child;
             }
         }
         return null;
     }
}
