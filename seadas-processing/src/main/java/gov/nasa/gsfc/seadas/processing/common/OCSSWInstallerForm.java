package gov.nasa.gsfc.seadas.processing.common;

import com.bc.ceres.core.runtime.RuntimeContext;
import com.bc.ceres.swing.TableLayout;
import gov.nasa.gsfc.seadas.OCSSWInfo;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.core.ParamUtils;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import org.esa.snap.ui.AppContext;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 3/13/13
 * Time: 11:11 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class OCSSWInstallerForm extends JPanel implements CloProgramUI {


    //private FileSelector ocsswDirSelector;
    JTextField fileTextField;
    //private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    ProcessorModel processorModel;
    private AppContext appContext;
    private JPanel dirPanel;
    private JPanel missionPanel;
    private JPanel otherPanel;

    //private JPanel superParamPanel;

    public static final String INSTALL_DIR_OPTION_NAME = "--install-dir";

    public String missionDataDir;
    public OCSSW ocssw;

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
                    "OLI",
                    "OSMI",
                    "SEAWIFS",
                    "VIIRSN"}
    ));
    private static final Set<String> DEFAULT_MISSIONS = new HashSet<String>(Arrays.asList(
            new String[]{
                    //"GOCI",
                    //"HICO",
                    "OCRVC"
            }
    ));


    HashMap<String, Boolean> missionDataStatus;


    public OCSSWInstallerForm(AppContext appContext, String programName, String xmlFileName, OCSSW ocssw) {
        this.appContext = appContext;
        this.ocssw = ocssw;
        processorModel = ProcessorModel.valueOf(programName, xmlFileName, ocssw);
        processorModel.setReadyToRun(true);
        setMissionDataDir(OCSSWInfo.getInstance().getOcsswDataDirPath());
        init();
        updateMissionValues();
        createUserInterface();
        processorModel.updateParamInfo(INSTALL_DIR_OPTION_NAME, getInstallDir());
        processorModel.addPropertyChangeListener(INSTALL_DIR_OPTION_NAME, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                setMissionDataDir(processorModel.getParamValue(INSTALL_DIR_OPTION_NAME) + File.separator + OCSSWInfo.OCSSW_DATA_DIR_SUFFIX);
                updateMissionStatus();
                updateMissionValues();
                createUserInterface();
                //reorganizePanel(getSuperParamPanel());
            }
        });
    }


    String getMissionDataDir(){
       return missionDataDir;
    }

    void setMissionDataDir(String currentMissionDataDir) {
        missionDataDir = currentMissionDataDir;
    }

    abstract void updateMissionStatus();
    abstract void updateMissionValues();

    String getInstallDir() {
        return OCSSWInfo.getInstance().getOcsswRoot();
    }

    abstract void init();

    public ProcessorModel getProcessorModel() {
        return processorModel;
    }

    public File getSelectedSourceProduct() {
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

        JPanel paramPanel = new ParamUIFactory(processorModel).createParamPanel();
        reorganizePanel(paramPanel);

        add(dirPanel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        add(missionPanel,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        add(otherPanel,
                new GridBagConstraintsCustom(0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));

        //setSize(getPreferredSize().width, getPreferredSize().height + 200);
        //setPreferredSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
    }

    public JPanel getParamPanel() {
        JPanel newPanel = new JPanel(new GridBagLayout());
        newPanel.add(missionPanel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 3));
        newPanel.add(otherPanel,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 2));
        return newPanel;
    }
    //ToDo: missionDataDir test should be differentiated for local and remote servers

    protected void reorganizePanel(JPanel paramPanel) {
        dirPanel = new JPanel();

        missionPanel = new JPanel(new TableLayout(5));
        missionPanel.setBorder(BorderFactory.createTitledBorder("Mission Data"));

        otherPanel = new JPanel();
        TableLayout otherPanelLayout = new TableLayout(3);
        otherPanelLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        otherPanel.setLayout(otherPanelLayout);
        otherPanel.setBorder(BorderFactory.createTitledBorder("Others"));
        OCSSWInfo ocsswInfo = OCSSWInfo.getInstance();

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
                        if (!DEFAULT_MISSIONS.contains(tmpString)) {
                            if (ocssw.isMissionDirExist(tmpString) ||
                                    missionDataStatus.get(tmpString)) {
                                ((JPanel) c).getComponents()[0].setEnabled(false);
                            } else {
                                ((JPanel) c).getComponents()[0].setEnabled(true);
                            }
                            missionPanel.add(c);
                        }
                    } else {
                        if (tmpString.equals("SRC")) {
                            ((JLabel) ((JPanel) c).getComponent(0)).setText("Source Code");
                            if (new File(ocsswInfo.getOcsswRoot() + System.getProperty("file.separator") + "ocssw-src").exists()) {
                                ((JPanel) c).getComponents()[0].setEnabled(false);
                            }
                        } else if (tmpString.equals("CLEAN")) {
                            ((JLabel) ((JPanel) c).getComponent(0)).setText("Clean Install");
                            ((JPanel) c).getComponents()[0].setEnabled(true);
                        }
                        otherPanel.add(c);
                        otherPanel.add(new JLabel("      "));
                    }
                }
            } else if (option.getName().equals("file parameter panel")) {
                Component[] bps = ((JPanel) option).getComponents();
                for (Component c : bps) {
                    dirPanel = (JPanel) c;

                }
                if (! ocsswInfo.getOcsswLocation().equals(ocsswInfo.OCSSW_LOCATION_LOCAL)) {
                    //if ocssw is not local, then disable the button to choose ocssw installation directory
                    ((JLabel)dirPanel.getComponent(0)).setText("Remote install-dir");
                    dirPanel.getComponent(1).setEnabled(false);
                    dirPanel.getComponent(2).setEnabled(false);
                    //dirPanel.getComponent(2).setVisible(false);
                } else {
                    ((JLabel)dirPanel.getComponent(0)).setText("Local install-dir");
                }
            }
        }

    }

    private void refreshMissionPanel(){

    }

    private Component findJPanel(Component comp, String panelName) {
        if (comp.getClass() == JPanel.class) return comp;
        if (comp instanceof Container) {
            Component[] components = ((Container) comp).getComponents();
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