package gov.nasa.gsfc.seadas.processing.general;

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


public class FileSelector {


    private JPanel jPanel = new JPanel(new GridBagLayout());



    public enum Type {
        IFILE,
        OFILE
    }

    private String propertyName = "FILE_SELECTOR_PANEL_CHANGED";

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

    private final JPanel filterPane = new JPanel(new GridBagLayout());


    private final SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);


    public FileSelector(AppContext appContext) {
        this(appContext, null);
    }


    public FileSelector(AppContext appContext, Type type) {
        this.appContext = appContext;
        setType(type);

        initComponents();
        addComponents();
    }


    public FileSelector(AppContext appContext, Type type, String name) {
        this(appContext, type);
        setName(name);
    }


    private void initComponents() {

        fileTextfield = createFileTextfield();

        nameLabel = new JLabel(name);

        fileChooserButton = new JButton(new FileChooserAction());

    }


    private void addComponents() {


       jPanel.add(nameLabel,
               new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));

        if (name == null) {
            nameLabel.setVisible(false);
        }

       jPanel.add(fileTextfield,
               new GridBagConstraintsCustom(1, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 2));
       jPanel.add(fileChooserButton,
               new GridBagConstraintsCustom(2, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));


       jPanel.add(filterPane,
               new GridBagConstraintsCustom(3, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));

        if (type != Type.IFILE) {
            filterPane.setVisible(false);
        }
    }


    public void setEnabled(boolean enabled) {
        nameLabel.setEnabled(enabled);
        fileChooserButton.setEnabled(enabled);

        if (type == Type.IFILE) {
            filterRegexField.setEnabled(enabled);
            filterRegexLabel.setEnabled(enabled);
            fileTextfield.setEnabled(enabled);
        }
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



    public JTextField getFileTextField() {
        return fileTextfield;
    }


    public void setFilename(String filename) {
        fileTextfield.setText(filename);
        fileTextfield.postActionEvent();
        handleFileTextfield();
    }


    private void handleFileTextfield() {

        String currentFilename = fileTextfield.getText();

        boolean filenameChanged = false;
        if (currentFilename != null) {
            if (!currentFilename.equals(lastFilename)) {
                filenameChanged = true;
            }
        } else {
            if (lastFilename != null) {
                filenameChanged = true;
            }
        }

        if (filenameChanged) {
            fileTextfield.setFocusable(true);
            String tmpLastFilename = lastFilename;
            lastFilename = currentFilename;
            fireEvent(propertyName, tmpLastFilename, currentFilename);
        }
    }


    public JTextField createFileTextfield() {

        final JTextField jTextField = new JTextField();

        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleFileTextfield();
            }
        });


        jTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                handleFileTextfield();
            }
        });

        return jTextField;
    }


    private void createFilterPane(JPanel mainPanel) {

        mainPanel.removeAll();

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


        mainPanel.add(filterRegexLabel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        mainPanel.add(filterRegexField,
                new GridBagConstraintsCustom(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));


    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;


        if (type == Type.IFILE) {
            regexFileFilter = new RegexFileFilter();
            createFilterPane(filterPane);
            filterPane.setVisible(true);
        } else {
            regexFileFilter = null;
            filterPane.removeAll();
            filterPane.setVisible(false);
        }
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

        private FileChooserAction(String dialogTitle) {
            super("...");
            fileChooser = new BeamFileChooser();

            fileChooser.setDialogTitle(dialogTitle);

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

            if (type == Type.IFILE) {
                fileChooser.addChoosableFileFilter(regexFileFilter);
            }

            if (fileChooser.showDialog(window, APPROVE_BUTTON_TEXT) == JFileChooser.APPROVE_OPTION) {
                final File file = fileChooser.getSelectedFile();
                currentDirectory = fileChooser.getCurrentDirectory();
                appContext.getPreferences().setPropertyString(BasicApp.PROPERTY_KEY_APP_LAST_OPEN_DIR,
                        currentDirectory.getAbsolutePath());


                String filename = null;
                if (file != null) {
                    filename = file.getAbsolutePath();
                }


                setFilename(filename);
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


    public JPanel getjPanel() {
        return jPanel;
    }


    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }


    public void fireEvent(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }
}
