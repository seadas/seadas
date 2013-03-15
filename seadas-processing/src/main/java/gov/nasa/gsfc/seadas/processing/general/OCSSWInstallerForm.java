package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.swing.TableLayout;
import gov.nasa.gsfc.seadas.processing.core.ParamUtils;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.BasicApp;
import org.esa.beam.util.SystemUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 3/13/13
 * Time: 11:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class OCSSWInstallerForm extends JPanel implements CloProgramUI {


    //private FileSelector ocsswDirSelector;
    JTextField fileTextField;
    //private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    ProcessorModel processorModel;
    private AppContext appContext;

    private static final Set<String> MISSIONS = new HashSet<String>(Arrays.asList(
            new String[]{"AQUARIUS",
                    "AVHRR",
                    "CZCS",
                    "GOCI",
                    "HICO",
                    "MERIS",
                    "AQUA",
                    "TERRA",
                    "MOS",
                    "OCM1",
                    "OCM2",
                    "OCRVC",
                    "OCTS",
                    "OSMI",
                    "SEAWIFS",
                    "VIIRSN"}
    ));
    private static final Set<String> DEFAULT_MISSIONS = new HashSet<String>(Arrays.asList(
            new String[]{
                    "GOCI",
                    "HICO",
                    "OCRVC"
            }
    ));

    public OCSSWInstallerForm(AppContext appContext, String programName, String xmlFileName) {
        this.appContext = appContext;
        processorModel = ProcessorModel.valueOf(programName, xmlFileName);
        processorModel.setReadyToRun(true);
        //ocsswDirSelector = new FileSelector(appContext, FileSelector.Type.IFILE, "ocssw installation directory");
        createUserInterface();
    }

    public ProcessorModel getProcessorModel() {
        return processorModel;
    }

    public Product getSelectedSourceProduct() {
        return null;
    }

    public boolean isOpenOutputInApp() {
        return false;
    }

    public String getParamString() {
        return processorModel.getParamList().getParamString();
    }

    public void setParamString(String paramString) {
        processorModel.getParamList().setParamString(paramString);
    }

    protected void createUserInterface() {

        this.setLayout(new GridBagLayout());

        add(getDirPanel(),
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        add(getParamPanel(),
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));

        //setSize(getPreferredSize().width, getPreferredSize().height + 200);
        //setPreferredSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
    }

    public JPanel getParamPanel() {
        JPanel paramPanel = new ParamUIFactory(processorModel).createParamPanel();
        return reorganizePanel(paramPanel);
    }

    private JPanel reorganizePanel(JPanel paramPanel) {
        JPanel missionPanel = new JPanel(new TableLayout(5));
        missionPanel.setBorder(BorderFactory.createTitledBorder("Mission Data"));
        JPanel otherPanel = new JPanel();
        TableLayout otherPanelLayout = new TableLayout(3);
        otherPanelLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        otherPanel.setLayout(otherPanelLayout);
        otherPanel.setBorder(BorderFactory.createTitledBorder("Others"));

        JScrollPane jsp = (JScrollPane) paramPanel.getComponent(0);
        JPanel panel = (JPanel) findJPanel(jsp, "param panel");
        Component[] options = panel.getComponents();
        String tmpString;
        for (Component option : options) {
            if (option.getName().equals("boolean field panel")) {
                Component[] bps = ((JPanel) option).getComponents();
                for (Component c : bps) {
                    tmpString = ParamUtils.removePreceedingDashes(c.getName()).toUpperCase();
                    if (MISSIONS.contains(tmpString)) {
                        System.out.println(c.getName());
                        if (!DEFAULT_MISSIONS.contains(tmpString)) {
                            missionPanel.add(c);
                        }
                    } else {
                        if (tmpString.equals("SRC")) {
                            ((JLabel) ((JPanel) c).getComponent(0)).setText("Source Code");
                        } else if (tmpString.equals("EVAL")) {
                            ((JLabel) ((JPanel) c).getComponent(0)).setText("Evaluation Data Files");
                        }
                        otherPanel.add(c);
                        otherPanel.add(new JLabel("      "));
                    }
                }
            }
        }

        JPanel newPanel = new JPanel(new GridBagLayout());
//        TableLayout panelLayout = new TableLayout(2);
//        panelLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
//        newPanel.setLayout(panelLayout);
//        newPanel.setBorder(BorderFactory.createTitledBorder("Install Options"));
//        newPanel.add(missionPanel);
//        newPanel.add(otherPanel);

        newPanel.add(missionPanel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        newPanel.add(otherPanel,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        return newPanel;
    }

    private boolean isMission(String paramName) {
        return false;
    }

    protected JPanel getDirPanel() {
        final JPanel dirPanel = new JPanel();
//        dirPanel.add(new JLabel("ocssw dir"),
//                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
        fileTextField = new JTextField(System.getProperty("user.home") + System.getProperty("file.separator") + "ocssw");
        fileTextField.setPreferredSize(fileTextField.getPreferredSize());
        fileTextField.setMaximumSize(fileTextField.getPreferredSize());
        fileTextField.setMinimumSize(fileTextField.getPreferredSize());
        JButton fileChooserButton = new JButton(new FileChooserAction());

        dirPanel.add(new JLabel("OCSSW Installation Dir:"),
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
        dirPanel.add(fileTextField,
                new GridBagConstraintsCustom(1, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 4));
        dirPanel.add(fileChooserButton,
                new GridBagConstraintsCustom(2, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
        return dirPanel;
    }

    private class FileChooserAction extends AbstractAction {

        private String APPROVE_BUTTON_TEXT = "Select";
        private JFileChooser fileChooser;

        private FileChooserAction() {
            super("Browse");
            fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select OCSSW Installation Directory:");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        }

        @Override
        public void actionPerformed(ActionEvent event) {
            final Window window = SwingUtilities.getWindowAncestor((JComponent) event.getSource());

            String homeDirPath = SystemUtils.getUserHomeDir().getPath();
            String openDir = appContext.getPreferences().getPropertyString(BasicApp.PROPERTY_KEY_APP_LAST_OPEN_DIR,
                    homeDirPath);
            File currentDirectory = new File(openDir);
            fileChooser.setCurrentDirectory(currentDirectory);

            if (fileChooser.showDialog(window, APPROVE_BUTTON_TEXT) == JFileChooser.APPROVE_OPTION) {

                currentDirectory = fileChooser.getSelectedFile();
                if (!currentDirectory.isDirectory()) {
                    currentDirectory = fileChooser.getCurrentDirectory();
                }
                appContext.getPreferences().setPropertyString(BasicApp.PROPERTY_KEY_APP_LAST_OPEN_DIR,
                        currentDirectory.getAbsolutePath());
                fileTextField.setText(currentDirectory.getAbsolutePath());
            }

        }

    }

    private Component findJPanel(Component comp, String panelName) {
        System.out.println(comp.getClass() + "  " + panelName);
        if (comp.getClass() == JPanel.class) return comp;
        if (comp instanceof Container) {
            Component[] components = ((Container) comp).getComponents();
            System.out.println("number of comps: " + components.length);
            for (int i = 0; i < components.length; i++) {

                Component child = findJPanel(components[i], components[i].getName());
                if (child != null) {

                    return child;
                }
            }
        }
        return null;

    }
}