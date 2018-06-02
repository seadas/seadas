/*
Author: Danny Knowles
    Don Shea
*/

package gov.nasa.gsfc.seadas.processing.processor;

import com.bc.ceres.swing.selection.SelectionChangeEvent;
import com.bc.ceres.swing.selection.SelectionChangeListener;
import gov.nasa.gsfc.seadas.processing.core.MultiParamList;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ParamList;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import gov.nasa.gsfc.seadas.processing.common.*;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import static gov.nasa.gsfc.seadas.processing.common.FileSelector.PROPERTY_KEY_APP_LAST_OPEN_DIR;

import javax.swing.*;
import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.AppContext;


public class MultlevelProcessorForm extends JPanel implements CloProgramUI {

    public enum Processor {
        MAIN("main"),
        MODIS_L1A_PY("modis_L1A.py"),
        L1AEXTRACT_MODIS("l1aextract_modis"),
        L1AEXTRACT_SEAWIFS("l1aextract_seawifs"),
        L1MAPGEN("l1mapgen"),
        GEO("geo"),
        MODIS_L1B("modis_L1B.py"),
        L1BGEN("l1bgen"),
        L1BRSGEN("l1brsgen"),
        L2GEN("l2gen"),
        L2EXTRACT("l2extract"),
        L2BRSGEN("l2brsgen"),
        L2MAPGEN("l2mapgen"),
        L2BIN("l2bin"),
        L3BIN("l3bin"),
        //        SMIGEN("smigen"),
        L3MAPGEN("l3mapgen");

        private Processor(String name) {
            this.name = name;
        }

        private final String name;

        public String toString() {
            return name;
        }
    }


    /*
   MultlevelProcessorForm
       tabbedPane
           mainPanel
               primaryIOPanel
                   sourceProductFileSelector (ifile)
               parfilePanel
                   importPanel
                       importParfileButton
                       retainParfileCheckbox
                   exportParfileButton
                   parfileScrollPane
                       parfileTextArea
           chainScrollPane
               chainPanel
                   nameLabel
                   keepLabel
                   paramsLabel
                   configLabel
                   progRowPanel


    */

    private AppContext appContext;

    private JFileChooser jFileChooser;

    private final JTabbedPane tabbedPane;

    private JPanel mainPanel;
    private JPanel primaryIOPanel;
    private SeadasFileSelector sourceProductFileSelector;
    private JScrollPane parfileScrollPane;
    private JPanel parfilePanel;
    private JPanel importPanel;
    private JButton importParfileButton;
    private JCheckBox retainIFileCheckbox;
    private JButton exportParfileButton;
    private JTextArea parfileTextArea;
    private FileSelector odirSelectorOld;
    private ActiveFileSelector odirSelector;

    private JScrollPane chainScrollPane;
    private JPanel chainPanel;
    private JLabel nameLabel;
    private JLabel keepLabel;
    private JLabel paramsLabel;

    private JPanel spacer;

    public String MAIN_PARSTRING_EVENT = "MAIN_PARSTRING_EVENT";
    static public String ODIR_EVENT = "ODIR";
    static public final String IFILE = "ifile";

    private ArrayList<MultilevelProcessorRow> rows;

    String xmlFileName;
    ProcessorModel processorModel;
    private SwingPropertyChangeSupport propertyChangeSupport;

    OCSSW ocssw;

    MultlevelProcessorForm(AppContext appContext, String xmlFileName, OCSSW ocssw) {
        this.appContext = appContext;
        this.xmlFileName = xmlFileName;
        this.ocssw = ocssw;

        propertyChangeSupport = new SwingPropertyChangeSupport(this);

        jFileChooser = new JFileChooser();

        // create main panel
        sourceProductFileSelector = new SeadasFileSelector(SnapApp.getDefault().getAppContext(), IFILE, true);
        sourceProductFileSelector.initProducts();
        //sourceProductFileSelector.setProductNameLabel(new JLabel(IFILE));
        sourceProductFileSelector.getFileNameComboBox().setPrototypeDisplayValue(
                "123456789 123456789 123456789 123456789 123456789 ");
        sourceProductFileSelector.addSelectionChangeListener(new SelectionChangeListener() {
            @Override
            public void selectionChanged(SelectionChangeEvent selectionChangeEvent) {
                handleIFileChanged();
            }

            @Override
            public void selectionContextChanged(SelectionChangeEvent selectionChangeEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        odirSelector = new ActiveFileSelector(propertyChangeSupport, ODIR_EVENT, "odir", ParamInfo.Type.DIR);

//        odirSelector = new FileSelector(VisatApp.getApp(), ParamInfo.Type.DIR, "odir");
//
//        odirSelector.addPropertyChangeListener(new PropertyChangeListener() {
//            @Override
//            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
//                handleOdirChanged();
//            }
//        });

        odirSelector.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                handleOdirChanged();
            }
        });


        primaryIOPanel = new JPanel(new GridBagLayout());
        primaryIOPanel.setBorder(BorderFactory.createTitledBorder("Primary I/O Files"));
        primaryIOPanel.add(sourceProductFileSelector.createDefaultPanel(),
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        primaryIOPanel.add(odirSelector.getJPanel(),
                new GridBagConstraintsCustom(0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        retainIFileCheckbox = new JCheckBox("Retain Selected IFILE");

        importParfileButton = new JButton("Load Parameters");
        importParfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String contents = SeadasGuiUtils.importFile(jFileChooser);
                if (contents != null) {
                    File defaultIFileDirectory = null;
                    if (jFileChooser.getSelectedFile() != null) {
                        defaultIFileDirectory = jFileChooser.getSelectedFile().getParentFile();
                    }
                    setParamString(contents, retainIFileCheckbox.isSelected(), defaultIFileDirectory);
                }
            }
        });

        importPanel = new JPanel(new GridBagLayout());
        importPanel.setBorder(BorderFactory.createEtchedBorder());
        importPanel.add(importParfileButton,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        importPanel.add(retainIFileCheckbox,
                new GridBagConstraintsCustom(1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        exportParfileButton = new JButton("Save Parameters");
        exportParfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String contents = getParamString();
                SeadasGuiUtils.exportFile(jFileChooser, contents + "\n");
            }
        });

        parfileTextArea = new JTextArea();
        parfileTextArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void focusLost(FocusEvent e) {
                handleParamStringChange();
            }
        });
        parfileScrollPane = new JScrollPane(parfileTextArea);
        parfileScrollPane.setBorder(null);

        parfilePanel = new JPanel(new GridBagLayout());
        parfilePanel.setBorder(BorderFactory.createTitledBorder("Parfile"));
        parfilePanel.add(importPanel,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        parfilePanel.add(exportParfileButton,
                new GridBagConstraintsCustom(1, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        parfilePanel.add(parfileScrollPane,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 0, 2));

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(primaryIOPanel,
                new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        mainPanel.add(parfilePanel,
                new GridBagConstraintsCustom(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH));

        // create chain panel
        nameLabel = new JLabel("Processor");
        Font font = nameLabel.getFont().deriveFont(Font.BOLD);
        nameLabel.setFont(font);
        keepLabel = new JLabel("Keep");
        keepLabel.setToolTipText("Keep intermediate output files");
        keepLabel.setFont(font);
        paramsLabel = new JLabel("Parameters");
        paramsLabel.setFont(font);
        spacer = new JPanel();

        chainPanel = new JPanel(new GridBagLayout());

        chainPanel.add(nameLabel,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, -8)));
        chainPanel.add(keepLabel,
                new GridBagConstraintsCustom(1, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, -8, 2, -8)));
        chainPanel.add(paramsLabel,
                new GridBagConstraintsCustom(2, 0, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, -8, 2, 2)));
        createRows();
        int rowNum = 1;
        for (MultilevelProcessorRow row : rows) {
            row.attachComponents(chainPanel, rowNum);
            rowNum++;
        }
        chainPanel.add(spacer,
                new GridBagConstraintsCustom(0, rowNum, 0, 1, GridBagConstraints.WEST, GridBagConstraints.VERTICAL));

        chainScrollPane = new JScrollPane(chainPanel);
        chainScrollPane.setBorder(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.add("Main", mainPanel);
        tabbedPane.add("Processor Chain", chainScrollPane);

        // add the tabbed pane
        setLayout(new GridBagLayout());
        add(tabbedPane, new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH));

    }

    void createRows() {
        Processor[] rowNames = {
                Processor.MAIN,
                Processor.MODIS_L1A_PY,
                Processor.L1AEXTRACT_SEAWIFS,
                Processor.L1BRSGEN,
                Processor.L1MAPGEN,
                Processor.GEO,
                Processor.L1AEXTRACT_MODIS,
                Processor.MODIS_L1B,
                Processor.L1BGEN,
                Processor.L2GEN,
                Processor.L2EXTRACT,
                Processor.L2BIN,
                Processor.L2BRSGEN,
                Processor.L2MAPGEN,
                Processor.L3BIN,
//                Processor.SMIGEN,
                Processor.L3MAPGEN
        };
        rows = new ArrayList<MultilevelProcessorRow>();

        for (Processor processor : rowNames) {
            MultilevelProcessorRow row = new MultilevelProcessorRow(processor.toString(), this, ocssw);
            row.addPropertyChangeListener(MultilevelProcessorRow.PARAM_STRING_EVENT, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    updateParamString();
                }
            });
            rows.add(row);

        }
    }


    public AppContext getAppContext() {
        return appContext;
    }

    @Override
    public JPanel getParamPanel() {
        return this;
    }

    public ParamList getParamList() {
        MultiParamList paramList = new MultiParamList();
        for (MultilevelProcessorRow row : rows) {
            String name = row.getName();
            if (name.equals("modis_GEO.py")) {
                name = "geo";
            }
            paramList.addParamList(name, row.getParamList());
        }
        return paramList;
    }

    @Override
    public ProcessorModel getProcessorModel() {
        if (processorModel == null) {
            processorModel = new MultilevelProcessorModel("multilevel_processor.py", xmlFileName, ocssw);
            processorModel.setReadyToRun(true);
        }
        processorModel.setParamList(getParamList());
        return processorModel;
    }

    @Override
    public File getSelectedSourceProduct() {
        if (getSourceProductFileSelector() != null) {
            return getSourceProductFileSelector().getSelectedFile();
        }
        return null;
    }

    @Override
    public boolean isOpenOutputInApp() {
        return false;
    }

    public SeadasFileSelector getSourceProductFileSelector() {
        return sourceProductFileSelector;
    }

    public void prepareShow() {
        if (getSourceProductFileSelector() != null) {
            getSourceProductFileSelector().initProducts();
        }
    }

    public void prepareHide() {
        if (getSourceProductFileSelector() != null) {
            getSourceProductFileSelector().releaseFiles();
        }
    }

    private MultilevelProcessorRow getRow(String name) {
        for (MultilevelProcessorRow row : rows) {
            if (row.getName().equals(name)) {
                return row;
            }
        }
        return null;
    }

    public String getParamString() {
        String paramString = getParamList().getParamString("\n");

        // This rigged up thingy adds in some comments
        String[] lines = paramString.split("\n");
        StringBuilder stringBuilder = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            stringBuilder.append(line).append("\n");

            if (line.toLowerCase().startsWith(IFILE)) {
                String iFilename = line.substring(IFILE.length() + 1, line.length()).trim();
                File iFile = new File(iFilename);

                if (!iFile.exists()) {
                    stringBuilder.append("## WARNING: ifile '").append(iFilename).append("' does not exist").append("\n");
                }
            }
        }

        return stringBuilder.toString();
    }

    public void setParamString(String str) {
        setParamString(str, false, null);
    }


    public void setParamString(String str, boolean retainIFile, File defaultIFileDirectory) {

        String oldODir = getRow(Processor.MAIN.toString()).getParamList().getValue("odir");


        String[] lines = str.split("\n");

        String section = Processor.MAIN.toString();
        StringBuilder stringBuilder = new StringBuilder();

        for (String line : lines) {
            line = line.trim();

            // get rid of comment lines
            if (line.length() > 0 && line.charAt(0) != '#') {

                // locate new section line
                if (line.charAt(0) == '[' && line.contains("]")) {

                    // determine next section
                    int endIndex = line.indexOf(']');
                    String nextSection = line.substring(1, endIndex).trim();


                    if (nextSection.length() > 0) {

                        // set the params for this section
                        if (stringBuilder.length() > 0) {
                            MultilevelProcessorRow row = getRow(section);
                            if (row != null) {
                                row.setParamString(stringBuilder.toString(), retainIFile);
                            }
                            stringBuilder.setLength(0);
                        }

                        section = nextSection;
                    }


//                    line = line.substring(1).trim();
//                    String[] words = line.split("\\s+", 2);
//                    section = words[0];
//                    int i = section.indexOf(']');
//                    if (i != -1) {
//                        section = section.substring(0, i).trim();
//                    }


                } else {
                    if (line.toLowerCase().startsWith(IFILE)) {

                        String originalIFilename = line.substring(IFILE.length() + 1, line.length()).trim();
                        File originalIFile = new File(originalIFilename);

                        String absoluteIFilename;
                        if (originalIFile.isAbsolute()) {
                            absoluteIFilename = originalIFilename;
                        } else {
                            File absoluteFile = new File(defaultIFileDirectory, originalIFilename);
                            absoluteIFilename = absoluteFile.getAbsolutePath();
                        }

                        stringBuilder.append(IFILE).append("=").append(absoluteIFilename).append("\n");

                    } else {
                        stringBuilder.append(line).append("\n");
                    }
                }
            }
        }

        if (stringBuilder.length() > 0) {
            MultilevelProcessorRow row = getRow(section);
            if (row != null) {
                row.setParamString(stringBuilder.toString(), retainIFile);
            }
        }

        String newODir = getRow(Processor.MAIN.toString()).getParamList().getValue("odir");
        propertyChangeSupport.firePropertyChange(ODIR_EVENT, oldODir, newODir);


        updateParamString();
    }

    private void updateParamString() {

        ArrayList<File> fileArrayList = new ArrayList<File>();
        String fileList = getRow(Processor.MAIN.toString()).getParamList().getValue(IFILE);
        String filenameArray[] = fileList.split(",");
        for (String filename : filenameArray) {
            fileArrayList.add(new File(filename));
        }

        String oldIfileName = null;
        if (sourceProductFileSelector.getSelectedFile() != null) {
            oldIfileName = sourceProductFileSelector.getSelectedFile().getAbsolutePath();
        }
        String newIfileName = getRow(Processor.MAIN.toString()).getParamList().getValue(IFILE);

        if (newIfileName != null && !newIfileName.equals(oldIfileName) || oldIfileName != null && !oldIfileName.equals(newIfileName)) {
            if (fileArrayList.size() > 1) {
                File listFile = setSelectedMultiFileList(fileArrayList);

                sourceProductFileSelector.setSelectedFile(listFile);
            } else {
                File file = new File(getRow(Processor.MAIN.toString()).getParamList().getValue(IFILE));
                sourceProductFileSelector.setSelectedFile(file);
            }
        }

        //todo this should not be needed but is needed at the moment because parfileTextArea doesn't trigger event in this case
        String newODir = getRow(Processor.MAIN.toString()).getParamList().getValue("odir");
        propertyChangeSupport.firePropertyChange(ODIR_EVENT, null, newODir);


        parfileTextArea.setText(getParamString());
    }


    public File setSelectedMultiFileList(ArrayList<File> tmpArrayList) {
        File[] files = new File[tmpArrayList.size()];
        tmpArrayList.toArray(files);

        String homeDirPath = SystemUtils.getUserHomeDir().getPath();
        String openDir = appContext.getPreferences().getPropertyString(PROPERTY_KEY_APP_LAST_OPEN_DIR,
                homeDirPath);
        File currentDirectory = new File(openDir);
        File fileListFile = new File(currentDirectory, "_inputFiles.lst");

        StringBuilder fileNames = new StringBuilder();
        for (File file : files) {
            fileNames.append(file.getAbsolutePath() + "\n");
        }
        FileWriter fileWriter = null;
        try {

            fileWriter = new FileWriter(fileListFile);
            fileWriter.write(fileNames.toString());
            fileWriter.close();
        } catch (IOException ioe) {
        }

        return fileListFile;

    }


    private void handleParamStringChange() {
        String newStr = parfileTextArea.getText();
        String oldStr = getParamString();
        if (!newStr.equals(oldStr)) {
            setParamString(newStr);
        }
    }

    private void handleIFileChanged() {
        String ifileName = sourceProductFileSelector.getSelectedFile().getAbsolutePath();
        MultilevelProcessorRow row = getRow(Processor.MAIN.toString());
        String oldIFile = row.getParamList().getValue(IFILE);
        if (!ifileName.equals(oldIFile)) {

//            getRow("l2gen").clearConfigPanel();

            row.setParamValue(IFILE, ifileName);
            parfileTextArea.setText(getParamString());
        }
    }


    private void handleOdirChanged() {
        String odirName = odirSelector.getFilename();
        MultilevelProcessorRow row = getRow(Processor.MAIN.toString());
        String oldOdir = row.getParamList().getValue("odir");
        if (!odirName.equals(oldOdir)) {
            row.setParamValue("odir", odirName);
            parfileTextArea.setText(getParamString());
        }
    }


    public String getIFile() {
        return getRow(Processor.MAIN.toString()).getParamList().getValue(IFILE);
    }

    public String getFirstIFile() {
        String fileName = getIFile();
        if (fileName.contains(",")) {
            String[] files = fileName.split(",");
            fileName = files[0].trim();
        } else if (fileName.contains(" ")) {
            String[] files = fileName.trim().split(" ");
            fileName = files[0].trim();
        }

        // todo : need to check for file being a list of files.

        return fileName;
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }

    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(name, listener);
    }


}