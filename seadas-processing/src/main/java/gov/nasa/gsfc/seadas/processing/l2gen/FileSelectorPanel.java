package gov.nasa.gsfc.seadas.processing.l2gen;

import gov.nasa.gsfc.seadas.processing.general.SeadasLogger;

import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.BasicApp;
import org.esa.beam.util.SystemUtils;
import org.esa.beam.util.io.BeamFileChooser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/25/12
 * Time: 10:29 AM
 * To change this template use File | Settings | File Templates.
 */


public class FileSelectorPanel extends JPanel {


    public enum Type {
        IFILE,
        OFILE
    }

    private  String propertyName = "FILE_SELECTOR_PANEL_CHANGED";

    private AppContext appContext;

    private Type type;
    private String name;
    private JLabel nameLabel;
    private JTextField fileTextfield;

    private JButton fileChooserButton;

    private File currentDirectory;

    private RegexFileFilter regexFileFilter;
    private JTextField filterRegexField;
    private JLabel filterRegexLabel;


    private String lastFilename = null;

    private final SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);


    public FileSelectorPanel(AppContext appContext, Type type) {
        this.appContext = appContext;
        this.setType(type);

        initComponents();
        addComponents();
    }


    public FileSelectorPanel(AppContext appContext, Type type, String name) {
        this(appContext, type);
        setName(name);
    }


    private void initComponents() {

        fileTextfield = createFileTextfield();

        nameLabel = new JLabel(name);

        fileChooserButton = new JButton(new FileChooserAction());

        regexFileFilter = new RegexFileFilter();
    }


    private void addComponents() {
        setLayout(new GridBagLayout());


        add(nameLabel,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));

        if (name == null) {
            nameLabel.setVisible(false);
        }

        add(fileTextfield,
                new GridBagConstraintsCustom(1, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 2));
        add(fileChooserButton,
                new GridBagConstraintsCustom(2, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
        add(createFilterPane(),
                new GridBagConstraintsCustom(3, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
    }


    public void setEnabled(boolean enabled) {
        nameLabel.setEnabled(enabled);
        fileChooserButton.setEnabled(enabled);
        filterRegexField.setEnabled(enabled);
        filterRegexLabel.setEnabled(enabled);
        fileTextfield.setEnabled(enabled);
    }


    public void setName(String name) {
        this.name = name;
        nameLabel.setText(name);
        if (name != null && name.length() > 0) {
            nameLabel.setVisible(true);
        }
    }

    public String getFileName() {
        return fileTextfield.getText();
    }


    public void setFilename(String filename) {
        boolean filenameChanged = false;
        if (filename != null) {
            if (!filename.equals(lastFilename)) {
                filenameChanged = true;
            }
        } else {
            if (lastFilename != null) {
                filenameChanged = true;
            }
        }

        if (filenameChanged) {
            fileTextfield.setText(filename);
            fireEvent(propertyName, lastFilename, filename);
            lastFilename = filename;
        }
    }


    public JTextField createFileTextfield() {

        final JTextField jTextField = new JTextField();

        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFilename(jTextField.getText());
            }
        });


        jTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                setFilename(jTextField.getText());
            }
        });

        return jTextField;
    }


    private JPanel createFilterPane() {

        filterRegexField = new JTextField("123456789 ");
        filterRegexField.setPreferredSize(filterRegexField.getPreferredSize());
        filterRegexField.setMinimumSize(filterRegexField.getPreferredSize());
        filterRegexField.setMaximumSize(filterRegexField.getPreferredSize());
        filterRegexField.setText("");

        filterRegexField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
                regexFileFilter = new RegexFileFilter(filterRegexField.getText());
                SeadasLogger.getLogger().warning(regexFileFilter.getDescription());
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        filterRegexLabel = new JLabel("filter:");
        filterRegexLabel.setPreferredSize(filterRegexLabel.getPreferredSize());
        filterRegexLabel.setMinimumSize(filterRegexLabel.getPreferredSize());
        filterRegexLabel.setMaximumSize(filterRegexLabel.getPreferredSize());
        filterRegexLabel.setToolTipText("Filter the chooser by regular expression");


        JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(filterRegexLabel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        mainPanel.add(filterRegexField,
                new GridBagConstraintsCustom(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        return mainPanel;

    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }


    private class FileChooserAction extends AbstractAction {

        private String APPROVE_BUTTON_TEXT = "Select";
        private JFileChooser fileChooser;

        private FileChooserAction() {
            super("...");
            fileChooser = new BeamFileChooser();

            fileChooser.setDialogTitle("Select Input File");

            fileChooser.setAcceptAllFileFilterUsed(true);
            fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            final Window window = SwingUtilities.getWindowAncestor((JComponent) event.getSource());

            String homeDirPath = SystemUtils.getUserHomeDir().getPath();
            String openDir = appContext.getPreferences().getPropertyString(BasicApp.PROPERTY_KEY_APP_LAST_OPEN_DIR,
                    homeDirPath);
            currentDirectory = new File(openDir);
            fileChooser.setCurrentDirectory(currentDirectory);

            fileChooser.addChoosableFileFilter(regexFileFilter);

            if (fileChooser.showDialog(window, APPROVE_BUTTON_TEXT) == JFileChooser.APPROVE_OPTION) {
                final File file = fileChooser.getSelectedFile();

                lastFilename = fileTextfield.getText();


                String filename;
                if (file != null) {
                    filename = file.getAbsolutePath();
                } else {
                    filename = null;
                }

                setFilename(filename);

                currentDirectory = fileChooser.getCurrentDirectory();
                appContext.getPreferences().setPropertyString(BasicApp.PROPERTY_KEY_APP_LAST_OPEN_DIR,
                        currentDirectory.getAbsolutePath());
            }
        }

    }


    private class RegexFileFilter extends FileFilter {

        private String regex;

        public RegexFileFilter() {
            this(null);
        }

        public RegexFileFilter(String regex) throws IllegalStateException {
            SeadasLogger.getLogger().info("regular expression: " + regex);
            if (regex == null || regex.trim().length() == 0) {

                //throw new IllegalStateException();
                return;
            }

            this.regex = ".*" + regex + ".*";

        }

        /* (non-Javadoc)
        * @see java.io.FileFilter#accept(java.io.File)
        */
        public boolean accept(File pathname) {

            if (regex == null) {
                return true;
            }
            return (pathname.isFile() && pathname.getName().matches(this.regex));
        }

        public String getDescription() {
            return "Files matching regular expression: '" + regex + "'";
        }
    }


    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }


    public void fireEvent(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }
}
