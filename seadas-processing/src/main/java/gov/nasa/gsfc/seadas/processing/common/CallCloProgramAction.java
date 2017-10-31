package gov.nasa.gsfc.seadas.processing.common;

import com.bc.ceres.core.CoreException;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.runtime.ConfigurationElement;
import com.bc.ceres.core.runtime.RuntimeContext;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import gov.nasa.gsfc.seadas.OCSSWInfo;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.core.*;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.CommandManager;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.actions.AbstractVisatAction;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.esa.beam.util.SystemUtils.getApplicationContextId;

/**
 * @author Norman Fomferra
 * @author Aynur Abdurazik
 * @since SeaDAS 7.0
 */
public class CallCloProgramAction extends AbstractVisatAction {

    public static final String CONTEXT_LOG_LEVEL_PROPERTY = getApplicationContextId() + ".logLevel";
    public static final String LOG_LEVEL_PROPERTY = "logLevel";

    private static final Pattern PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private String programName;
    private String dialogTitle;
    private String xmlFileName;

    private boolean printLogToConsole = false;
    private boolean openOutputInApp = true;
    protected OCSSW ocssw;

    protected OCSSWInfo ocsswInfo = OCSSWInfo.getInstance();

    @Override
    public void configure(ConfigurationElement config) throws CoreException {
        programName = getConfigString(config, "programName");
        if (programName == null) {
            throw new CoreException("Missing DefaultOperatorAction property 'programName'.");
        }
        dialogTitle = getValue(config, "dialogTitle", programName);
        xmlFileName = getValue(config, "xmlFileName", ParamUtils.NO_XML_FILE_SPECIFIED);
        super.configure(config);
//        if (programName.equals(OCSSWInfo.OCSSW_INSTALLER_PROGRAM_NAME)) {
//            OCSSWInfo.detectOcssw();
//        }
        super.setEnabled(programName.equals(OCSSWInfo.OCSSW_INSTALLER_PROGRAM_NAME) || ocsswInfo.isOCSSWExist());
    }

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
    public void actionPerformed(CommandEvent event) {

        SeadasLogger.initLogger("ProcessingGUI_log_" + System.getProperty("user.name"), printLogToConsole);
        SeadasLogger.getLogger().setLevel(SeadasLogger.convertStringToLogger(RuntimeContext.getConfig().getContextProperty(LOG_LEVEL_PROPERTY, "OFF")));

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

        SeadasLogger.getLogger().info("dialog result: " + dialogResult);

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
        SeadasLogger.deleteLoggerOnExit(true);
        cloProgramUI.getProcessorModel().fireEvent(L2genData.CANCEL);

    }

    /**
     * @param pm is the model of the ocssw program to be exeuted
     * @output this is executed as a native process
     */
    public void executeProgram(ProcessorModel pm) {

        final ProcessorModel processorModel = pm;

        ProgressMonitorSwingWorker swingWorker = new ProgressMonitorSwingWorker<String, Object>(getAppContext().getApplicationWindow(), "Running " + programName + " ...") {
            @Override
            protected String doInBackground(ProgressMonitor pm) throws Exception {

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

                pm.done();
                SeadasFileUtils.writeToDisk(processorModel.getIFileDir() + System.getProperty("file.separator") + "OCSSW_LOG_" + programName + ".txt",
                        "Execution log for " + "\n" + Arrays.toString(ocssw.getCommandArray()) + "\n" + processorModel.getExecutionLogMessage());

                if (exitCode != 0)
                {
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
                    VisatApp.getApp().showInfoDialog(dialogTitle, "Program execution completed!\n" + ((outputFileName == null) ? "" :
                            (programName.equals(ocsswInfo.OCSSW_INSTALLER_PROGRAM_NAME) ? "" : ("Output written to:\n" + outputFileName))), null);
                    if (programName.equals(ocsswInfo.OCSSW_INSTALLER_PROGRAM_NAME)) {
                        ocssw.updateOCSSWRoot(processorModel.getParamValue("--install-dir"));
                        if (!ocssw.isOCSSWExist()) {
                            enableProcessors();
                        }
                    }
                    ProcessorModel secondaryProcessor = processorModel.getSecondaryProcessor();
                    if (secondaryProcessor != null) {
                        int exitCode = ocssw.execute(secondaryProcessor.getParamList()).exitValue();
                        if (exitCode == 0) {
                            VisatApp.getApp().showInfoDialog(secondaryProcessor.getProgramName(),
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
                File ofile = SeadasFileUtils.createFile(ifileDir, st.nextToken());
                getAppContext().getProductManager().addProduct(ProductIO.readProduct(ofile));
            }
        }
    }

    private void enableProcessors() {

        CommandManager commandManager = getAppContext().getApplicationPage().getCommandManager();
        String namesToExclude = ProcessorTypeInfo.getExcludedProcessorNames();
        for (String processorName : ProcessorTypeInfo.getProcessorNames()) {
            if (!namesToExclude.contains(processorName)) {
                if (commandManager.getCommand(processorName) != null) {
                    commandManager.getCommand(processorName).setEnabled(true);
                }
            }
        }
        commandManager.getCommand("install_ocssw.py").setText("Update Data Processors");
    }

    private void displayMessage(String programName, String message) {
        ScrolledPane messagePane = new ScrolledPane(programName, message, this.getAppContext().getApplicationWindow());
        messagePane.setVisible(true);
    }

    /**
     * Handler that tries to extract progress from stdout of ocssw processor
     */
    private static class ProgressHandler implements ProcessObserver.Handler {
        private boolean progressSeen;
        private int lastScan = 0;
        private String programName;
        private Pattern progressPattern;
        protected String currentText = "Part 1 - ";

        ProgressHandler(String programName, Pattern progressPattern) {
            this.programName = programName;
            this.progressPattern = progressPattern;
        }

        @Override
        public void handleLineOnStdoutRead(String line, Process process, ProgressMonitor progressMonitor) {
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
            System.out.println("handleLineOnStdoutRead ..."  + line);
            progressMonitor.setTaskName(programName);
            progressMonitor.setSubTaskName(line);
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process, ProgressMonitor progressMonitor) {
        }
    }

    private static class ConsoleHandler implements ProcessObserver.Handler {

        String programName;

        private String executionErrorLog = "";

        ConsoleHandler(String programName) {
            this.programName = programName;
        }

        @Override
        public void handleLineOnStdoutRead(String line, Process process, ProgressMonitor progressMonitor) {
            SeadasLogger.getLogger().info(programName + ": " + line);
            executionErrorLog = executionErrorLog + line + "\n";
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process, ProgressMonitor progressMonitor) {
            SeadasLogger.getLogger().info(programName + " stderr: " + line);
            executionErrorLog = executionErrorLog + line + "\n";
        }

        public String getExecutionErrorLog() {
            return executionErrorLog;
        }
    }

    private static class TerminationHandler implements ProcessObserver.Handler {

        @Override
        public void handleLineOnStdoutRead(String line, Process process, ProgressMonitor progressMonitor) {
            if (progressMonitor.isCanceled()) {
                process.destroy();
            }
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process, ProgressMonitor progressMonitor) {
            if (progressMonitor.isCanceled()) {
                process.destroy();
            }
        }
    }

    /**
     * Handler that tries to extract progress from stderr of ocssw_installer.py
     */
    private static class InstallerHandler extends ProgressHandler {

        InstallerHandler(String programName, Pattern progressPattern) {
            super(programName, progressPattern);
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process, ProgressMonitor progressMonitor) {
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