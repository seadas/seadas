package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.core.runtime.RuntimeContext;
import com.bc.ceres.swing.TableLayout;
import gov.nasa.gsfc.seadas.processing.core.OCSSW;
import gov.nasa.gsfc.seadas.processing.core.ParamUtils;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.AppContext;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;

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

    public static final String missionDataDir = OCSSW.getOcsswEnv() + System.getProperty("file.separator") + "run"
            + System.getProperty("file.separator") + "data"
            + System.getProperty("file.separator");

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

    private static final HashMap<String, String> MISSION_DIRECTORIES;

    static {
        MISSION_DIRECTORIES = new HashMap<String, String>();
        MISSION_DIRECTORIES.put("SEAWIFS", "seawifs");
        MISSION_DIRECTORIES.put("AQUA", "modisa");
        MISSION_DIRECTORIES.put("TERRA", "modist");
        MISSION_DIRECTORIES.put("VIIRSN", "viirsn");
        MISSION_DIRECTORIES.put("MERIS", "meris");
        MISSION_DIRECTORIES.put("CZCS", "czcs");
        MISSION_DIRECTORIES.put("AQUARIUS", "aquarius");
        MISSION_DIRECTORIES.put("OCTS", "octs");
        MISSION_DIRECTORIES.put("OLI", "oli");
        MISSION_DIRECTORIES.put("OSMI", "osmi");
        MISSION_DIRECTORIES.put("MOS", "mos");
        MISSION_DIRECTORIES.put("OCM2", "ocm2");
        MISSION_DIRECTORIES.put("OCM1", "ocm1");
        MISSION_DIRECTORIES.put("AVHRR", "avhrr");
        MISSION_DIRECTORIES.put("HICO", "hico");
        MISSION_DIRECTORIES.put("GOCI", "goci");

    }

    HashMap<String, Boolean> missionDataStatus;

    public OCSSWInstallerForm(AppContext appContext, String programName, String xmlFileName) {
        this.appContext = appContext;
        processorModel = ProcessorModel.valueOf(programName, xmlFileName);
        processorModel.setReadyToRun(true);
        init();
        updateMissionValues();
        createUserInterface();
        processorModel.updateParamInfo("--install-dir", getInstallDir());
    }


    abstract void updateMissionValues();

    abstract String getInstallDir();

    abstract void init();

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

    private void reorganizePanel(JPanel paramPanel) {
        dirPanel = new JPanel();

        missionPanel = new JPanel(new TableLayout(5));
        missionPanel.setBorder(BorderFactory.createTitledBorder("Mission Data"));

        otherPanel = new JPanel();
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
                        if (!DEFAULT_MISSIONS.contains(tmpString)) {
                            if (new File(missionDataDir + MISSION_DIRECTORIES.get(tmpString)).exists() ||
                                    missionDataStatus.get(tmpString)) {
                                ((JPanel) c).getComponents()[0].setEnabled(false);
                            }
                            missionPanel.add(c);
                        }
                    } else {
                        if (tmpString.equals("SRC")) {
                            ((JLabel) ((JPanel) c).getComponent(0)).setText("Source Code");
                            if (new File(OCSSW.getOcsswEnv() + System.getProperty("file.separator") + "build").exists()) {
                                ((JPanel) c).getComponents()[0].setEnabled(false);
                            }
                        } else if (tmpString.equals("EVAL")) {
                            ((JLabel) ((JPanel) c).getComponent(0)).setText("Evaluation Data Files");
                            if (new File(missionDataDir + "eval").exists()) {
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
                if (!RuntimeContext.getConfig().getContextProperty(OCSSW.OCSSW_LOCATION_PROPERTY).equals(OCSSW.SEADAS_OCSSW_LOCATION_LOCAL)) {
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