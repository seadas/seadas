package gov.nasa.gsfc.seadas.processing.common;


import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import gov.nasa.gsfc.seadas.OCSSWInfo;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.core.ProcessObserver;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.rcp.actions.AbstractSnapAction;
import org.esa.snap.rcp.util.Dialogs;
import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.ModalDialog;
import org.esa.snap.ui.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Aynur Abdurazik
 * @since SeaDAS 7.0
 */
public class CallCloProgramAction extends AbstractSnapAction {

    public static final String CONTEXT_LOG_LEVEL_PROPERTY = SystemUtils.getApplicationContextId() + ".logLevel";
    public static final String LOG_LEVEL_PROPERTY = "logLevel";

    private static final Pattern PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private String programName;
    private String dialogTitle;
    private String xmlFileName;

    private boolean printLogToConsole = false;
    private boolean openOutputInApp = true;
    protected OCSSW ocssw;

    private static final Set<String> KNOWN_KEYS = new HashSet<>(Arrays.asList("displayName", "programName", "dialogTitle", "helpId", "targetProductNameSuffix"));

    protected OCSSWInfo ocsswInfo = OCSSWInfo.getInstance();

    public static CallCloProgramAction create(Map<String, Object> properties) {
        CallCloProgramAction action = new CallCloProgramAction();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (KNOWN_KEYS.contains(entry.getKey())) {
                action.putValue(entry.getKey(), entry.getValue());
            }
        }
        return action;
    }

//    @Override
//    public void configure(ConfigurationElement config) throws CoreException {
//        programName = getConfigString(config, "programName");
//        if (programName == null) {
//            throw new CoreException("Missing DefaultOperatorAction property 'programName'.");
//        }
//        dialogTitle = getValue(config, "dialogTitle", programName);
//        xmlFileName = getValue(config, "xmlFileName", ParamUtils.NO_XML_FILE_SPECIFIED);
//        super.configure(config);
//        //super.setEnabled(programName.equals(OCSSWInfo.OCSSW_INSTALLER_PROGRAM_NAME) || ocsswInfo.isOCSSWExist());
//    }
    public String getXmlFileName() {
        return xmlFileName;
    }

    public CloProgramUI getProgramUI(AppContext appContext) {
        if (programName.indexOf("extract") != -1) {
            return new ExtractorUI(programName, xmlFileName, ocssw);
        } else if (programName.indexOf("modis_GEO") != -1 || programName.indexOf("modis_L1B") != -1) {
            return new ModisGEO_L1B_UI(programName, xmlFileName, ocssw);
        } else if (programName.indexOf(ocsswInfo.OCSSW_INSTALLER_PROGRAM_NAME) != -1) {
            ocssw.downloadOCSSWInstaller();
            if (!ocssw.isOcsswInstalScriptDownloadSuccessful()) {
                return null;
            }

            if (ocsswInfo.getOcsswLocation().equals(OCSSWInfo.OCSSW_LOCATION_LOCAL)) {
                return new OCSSWInstallerFormLocal(appContext, programName, xmlFileName, ocssw);
            } else {
                return new OCSSWInstallerFormRemote(appContext, programName, xmlFileName, ocssw);
            }
        }
        return new ProgramUIFactory(programName, xmlFileName, ocssw);//, multiIFile);
    }

    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }

    public void initializeOcsswClient() {

        ocssw = OCSSW.getOCSSWInstance();
        ocssw.setProgramName(programName);
    }

    @Override
    public void actionPerformed(ActionEvent event) {


        initializeOcsswClient();

        final AppContext appContext = getAppContext();

        final CloProgramUI cloProgramUI = getProgramUI(appContext);

        if (cloProgramUI == null) {
            return;
        }

        final Window parent = appContext.getApplicationWindow();

        final ModalDialog modalDialog = new ModalDialog(parent, dialogTitle, cloProgramUI, ModalDialog.ID_OK_APPLY_CANCEL_HELP, programName);
        modalDialog.getButton(ModalDialog.ID_OK).setEnabled(cloProgramUI.getProcessorModel().isReadyToRun());

        modalDialog.getJDialog().setMaximumSize(modalDialog.getJDialog().getPreferredSize());

        cloProgramUI.getProcessorModel().addPropertyChangeListener(cloProgramUI.getProcessorModel().getRunButtonPropertyName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (cloProgramUI.getProcessorModel().isReadyToRun()) {
                    modalDialog.getButton(ModalDialog.ID_OK).setEnabled(true);
                } else {
                    modalDialog.getButton(ModalDialog.ID_OK).setEnabled(false);
                }
                modalDialog.getJDialog().pack();
            }
        });

        cloProgramUI.getProcessorModel().addPropertyChangeListener("geofile", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                modalDialog.getJDialog().validate();
                modalDialog.getJDialog().pack();
            }
        });

        cloProgramUI.getProcessorModel().addPropertyChangeListener("infile", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                modalDialog.getJDialog().validate();
                modalDialog.getJDialog().pack();
            }
        });

        modalDialog.getButton(ModalDialog.ID_OK).setText("Run");
        modalDialog.getButton(ModalDialog.ID_HELP).setText("");
        modalDialog.getButton(ModalDialog.ID_HELP).setIcon(UIUtils.loadImageIcon("icons/Help24.gif"));

        //Make sure program is only executed when the "run" button is clicked.
        ((JButton) modalDialog.getButton(ModalDialog.ID_OK)).setDefaultCapable(false);
        modalDialog.getJDialog().getRootPane().setDefaultButton(null);

        final int dialogResult = modalDialog.show();

        Logger.getLogger(programName).info("dialog result: " + dialogResult);

        if (dialogResult != ModalDialog.ID_OK) {
            cloProgramUI.getProcessorModel().getParamList().clearPropertyChangeSupport();
            cloProgramUI.getProcessorModel().fireEvent(L2genData.CANCEL);
            return;
        }

        modalDialog.getButton(ModalDialog.ID_OK).setEnabled(false);

        final ProcessorModel processorModel = cloProgramUI.getProcessorModel();
        programName = processorModel.getProgramName();
        openOutputInApp = cloProgramUI.isOpenOutputInApp();

        if (!ocssw.isProgramValid()) {
            return;
        }

        if (programName.equals(ocsswInfo.OCSSW_INSTALLER_PROGRAM_NAME) && !ocssw.isOcsswInstalScriptDownloadSuccessful()) {
            displayMessage(programName, "ocssw installation script does not exist." + "\n" + "Please check network connection and rerun ''Install Processor''");
            return;
        }

        executeProgram(processorModel);
        cloProgramUI.getProcessorModel().fireEvent(L2genData.CANCEL);

    }

    /**
     * @param pm is the model of the ocssw program to be executed
     * @output this is executed as a native process
     */
    public void executeProgram(ProcessorModel pm) {

        final ProcessorModel processorModel = pm;

        ProgressMonitorSwingWorker swingWorker = new ProgressMonitorSwingWorker<String, Object>(getAppContext().getApplicationWindow(), "Running " + programName + " ...") {

            @Override
            protected String doInBackground(com.bc.ceres.core.ProgressMonitor pm) throws Exception {

                ocssw.setMonitorProgress(true);
                final Process process = ocssw.execute(processorModel);//ocssw.execute(processorModel.getParamList()); //OCSSWRunnerOld.execute(processorModel);
                if (process == null) {
                    throw new IOException(programName + " failed to create process.");
                }
                final ProcessObserver processObserver = ocssw.getOCSSWProcessObserver(process, programName, pm);
                final ConsoleHandler ch = new ConsoleHandler(programName);
                if (programName.equals(ocsswInfo.OCSSW_INSTALLER_PROGRAM_NAME)) {
                    processObserver.addHandler(new InstallerHandler(programName, processorModel.getProgressPattern()));
                } else {
                    processObserver.addHandler(new ProgressHandler(programName, processorModel.getProgressPattern()));
                }
                processObserver.addHandler(ch);
                processObserver.startAndWait();
                processorModel.setExecutionLogMessage(ch.getExecutionErrorLog());

                int exitCode = processObserver.getProcessExitValue();

                if (exitCode == 0) {
                    pm.done();
                    String logDir = ocsswInfo.getLogDirPath();
                    SeadasFileUtils.writeToDisk(logDir + File.separator + "OCSSW_LOG_" + programName + ".txt",
                            "Execution log for " + "\n" + Arrays.toString(ocssw.getCommandArray()) + "\n" + processorModel.getExecutionLogMessage());
                } else {
                    throw new IOException(programName + " failed with exit code " + exitCode + ".\nCheck log for more details.");
                }

                ocssw.setMonitorProgress(false);
                return processorModel.getOfileName();
            }

            @Override
            protected void done() {
                try {
                    final String outputFileName = get();
                    ocssw.getOutputFiles(processorModel);
                    displayOutput(processorModel);
                    Dialogs.showInformation(dialogTitle, "Program execution completed!\n" + ((outputFileName == null) ? ""
                            : (programName.equals(ocsswInfo.OCSSW_INSTALLER_PROGRAM_NAME) ? "" : ("Output written to:\n" + outputFileName))), null);
                    if (programName.equals(ocsswInfo.OCSSW_INSTALLER_PROGRAM_NAME) && ocsswInfo.getOcsswLocation().equals(OCSSWInfo.OCSSW_LOCATION_LOCAL)) {
                        ocssw.updateOCSSWRoot(processorModel.getParamValue("--install-dir"));
                        if (!ocssw.isOCSSWExist()) {
                            enableProcessors();
                        }
                    }
                    ProcessorModel secondaryProcessor = processorModel.getSecondaryProcessor();
                    if (secondaryProcessor != null) {
                        ocssw.setIfileName(secondaryProcessor.getParamValue(secondaryProcessor.getPrimaryInputFileOptionName()));
                        int exitCode = ocssw.execute(secondaryProcessor.getParamList()).exitValue();
                        if (exitCode == 0) {
                            Dialogs.showInformation(secondaryProcessor.getProgramName(),
                                    secondaryProcessor.getProgramName() + " done!\n", null);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    displayMessage(programName, "execution exception: " + e.getMessage() + "\n" + processorModel.getExecutionLogMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        swingWorker.execute();
    }

    void displayOutput(ProcessorModel processorModel) throws Exception {
        String ofileName = processorModel.getOfileName();
        if (openOutputInApp) {

            File ifileDir = processorModel.getIFileDir();

            StringTokenizer st = new StringTokenizer(ofileName);
            while (st.hasMoreTokens()) {
                File ofile = SeadasFileUtils.createFile(ocssw.getOfileDir(), st.nextToken());
                getAppContext().getProductManager().addProduct(ProductIO.readProduct(ofile));
            }
        }
    }

    private void enableProcessors() {

//        CommandManager commandManager = getAppContext().getApplicationPage().getCommandManager();
//        String namesToExclude = ProcessorTypeInfo.getExcludedProcessorNames();
//        for (String processorName : ProcessorTypeInfo.getProcessorNames()) {
//            if (!namesToExclude.contains(processorName)) {
//                if (commandManager.getCommand(processorName) != null) {
//                    commandManager.getCommand(processorName).setEnabled(true);
//                }
//            }
//        }
//        commandManager.getCommand("install_ocssw.py").setText("Update Data Processors");
    }

    private void displayMessage(String programName, String message) {
        ScrolledPane messagePane = new ScrolledPane(programName, message, this.getAppContext().getApplicationWindow());
        messagePane.setVisible(true);
    }

    /**
     * Handler that tries to extract progress from stdout of ocssw processor
     */
    public static class ProgressHandler implements ProcessObserver.Handler {

        private boolean progressSeen;
        private boolean stdoutOn;
        private int lastScan = 0;
        private String programName;
        private Pattern progressPattern;
        protected String currentText = "Part 1 - ";

        public ProgressHandler(String programName, Pattern progressPattern) {
            this.programName = programName;
            this.progressPattern = progressPattern;
        }

        @Override
        public void handleLineOnStdoutRead(String line, Process process, com.bc.ceres.core.ProgressMonitor progressMonitor) {
            stdoutOn = true;
            if (!progressSeen) {
                progressSeen = true;
                progressMonitor.beginTask(programName, 1000);
            }

            Matcher matcher = progressPattern.matcher(line);
            if (matcher.find()) {
                int scan = Integer.parseInt(matcher.group(1));
                int numScans = Integer.parseInt(matcher.group(2));

                scan = (scan * 1000) / numScans;
                progressMonitor.worked(scan - lastScan);
                lastScan = scan;
                currentText = line;
            }
            progressMonitor.setTaskName(programName);
            progressMonitor.setSubTaskName(line);
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process,  com.bc.ceres.core.ProgressMonitor progressMonitor) {
            if (!stdoutOn) {
                if (!progressSeen) {
                    progressSeen = true;
                    progressMonitor.beginTask(programName, 1000);
                }

                Matcher matcher = progressPattern.matcher(line);
                if (matcher.find()) {
                    int scan = Integer.parseInt(matcher.group(1));
                    int numScans = Integer.parseInt(matcher.group(2));

                    scan = (scan * 1000) / numScans;
                    progressMonitor.worked(scan - lastScan);
                    lastScan = scan;
                    currentText = line;
                }
                progressMonitor.setTaskName(programName);
                progressMonitor.setSubTaskName(line);
            }
        }
    }

    public static class ConsoleHandler implements ProcessObserver.Handler {

        String programName;

        private String executionErrorLog = "";

        public ConsoleHandler(String programName) {
            this.programName = programName;
        }

        @Override
        public void handleLineOnStdoutRead(String line, Process process,  com.bc.ceres.core.ProgressMonitor progressMonitor) {
            Logger.getLogger(programName).info(programName + ": " + line);
            executionErrorLog = executionErrorLog + line + "\n";
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process,  com.bc.ceres.core.ProgressMonitor progressMonitor) {
            Logger.getLogger(programName).info(programName + " stderr: " + line);
            executionErrorLog = executionErrorLog + line + "\n";
        }

        public String getExecutionErrorLog() {
            return executionErrorLog;
        }
    }

    private static class TerminationHandler implements ProcessObserver.Handler {

        @Override
        public void handleLineOnStdoutRead(String line, Process process,  com.bc.ceres.core.ProgressMonitor progressMonitor) {
            if (progressMonitor.isCanceled()) {
                process.destroy();
            }
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process,  com.bc.ceres.core.ProgressMonitor progressMonitor) {
            if (progressMonitor.isCanceled()) {
                process.destroy();
            }
        }
    }

    /**
     * Handler that tries to extract progress from stderr of ocssw_installer.py
     */
    public static class InstallerHandler extends ProgressHandler {

        public InstallerHandler(String programName, Pattern progressPattern) {
            super(programName, progressPattern);
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process,  com.bc.ceres.core.ProgressMonitor progressMonitor) {
            int len = line.length();
            if (len > 70) {
                String[] parts = line.trim().split("\\s+", 2);
                try {
                    int percent = Integer.parseInt(parts[0]);
                    progressMonitor.setSubTaskName(currentText + " - " + parts[0] + "%");
                } catch (Exception e) {
                    progressMonitor.setSubTaskName(line);
                }
            } else {
                progressMonitor.setSubTaskName(line);
            }
        }
    }

    private class ScrolledPane extends JFrame {

        private JScrollPane scrollPane;

        public ScrolledPane(String programName, String message, Window window) {
            setTitle(programName);
            setSize(500, 500);
            setBackground(Color.gray);
            setLocationRelativeTo(window);
            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BorderLayout());
            getContentPane().add(topPanel);
            JTextArea text = new JTextArea(message);
            scrollPane = new JScrollPane();
            scrollPane.getViewport().add(text);
            topPanel.add(scrollPane, BorderLayout.CENTER);
        }
    }

}
