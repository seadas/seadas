package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.core.CoreException;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.runtime.ConfigurationElement;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import gov.nasa.gsfc.seadas.processing.core.ParamUtils;
import gov.nasa.gsfc.seadas.processing.core.ProcessObserver;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.actions.AbstractVisatAction;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A ...
 *
 * @author Norman Fomferra
 * @author Aynur Abdurazik
 * @since SeaDAS 7.0
 */
public class CallCloProgramAction extends AbstractVisatAction {

    private String programName;
    private String dialogTitle;
    private String xmlFileName;

    private boolean printLogToConsole = false;
    private boolean openOutputInApp = true;

    @Override
    public void configure(ConfigurationElement config) throws CoreException {
        programName = getConfigString(config, "programName");
        if (programName == null) {
            throw new CoreException("Missing DefaultOperatorAction property 'programName'.");
        }
        dialogTitle = getValue(config, "dialogTitle", programName);
        xmlFileName = getValue(config, "xmlFileName", ParamUtils.NO_XML_FILE_SPECIFIED);

        super.configure(config);
    }


    public String getXmlFileName() {
        return xmlFileName;
    }

    public CloProgramUI getProgramUI(AppContext appContext) {
        if (programName.indexOf("extract") != -1) {
            return new ExtractorUI(programName, xmlFileName);
        }
        return new ProgramUIFactory(programName, xmlFileName);
    }

    @Override
    public void actionPerformed(CommandEvent event) {
        SeadasLogger.initLogger("ProcessingGUI_log_" + System.getProperty("user.name"), printLogToConsole);
        SeadasLogger.getLogger().setLevel(Level.INFO);

        final AppContext appContext = getAppContext();

        final CloProgramUI cloProgramUI = getProgramUI(appContext);

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

        modalDialog.getButton(ModalDialog.ID_OK).setText("Run");
        modalDialog.getButton(ModalDialog.ID_HELP).setText("");
        modalDialog.getButton(ModalDialog.ID_HELP).setIcon(UIUtils.loadImageIcon("icons/Help24.gif"));

        //Make sure program is only executed when the "run" button is clicked.
        ((JButton) modalDialog.getButton(ModalDialog.ID_OK)).setDefaultCapable(false);
        modalDialog.getJDialog().getRootPane().setDefaultButton(null);

        final int dialogResult = modalDialog.show();

        SeadasLogger.getLogger().info("dialog result: " + dialogResult);

        if (dialogResult != ModalDialog.ID_OK) {
            return;
        }


        final Product selectedProduct = cloProgramUI.getSelectedSourceProduct();

        if (selectedProduct == null) {
            VisatApp.getApp().showErrorDialog(programName, "No product selected.");
            return;
        }
        modalDialog.getButton(ModalDialog.ID_OK).setEnabled(false);

        final ProcessorModel processorModel = cloProgramUI.getProcessorModel();
        openOutputInApp = cloProgramUI.isOpenOutputInApp();


        if (!processorModel.isValidProcessor()) {
            VisatApp.getApp().showErrorDialog(programName, processorModel.getProgramErrorMessage());
            return;

        }

        executeProgram(processorModel);
        //remoteExecuteProgram(processorModel);

        SeadasLogger.deleteLoggerOnExit(true);

    }

    public void remoteExecuteProgram(ProcessorModel pm) {

        RSClient ocsswClient = new RSClient();
        pm.getProgramCmdArray();

        String paramString = pm.getCmdArrayString();
        String[] filesToUpload = pm.getFilesToUpload();

        boolean fileUploadSuccess = ocsswClient.uploadFile(filesToUpload);

        if (fileUploadSuccess) {
            System.out.println("file upload is successful!");

            ocsswClient.uploadParFile(pm.getParString());
            ocsswClient.uploadParam(paramString);
            //ocsswClient.runOCSSW();
        } else {
            System.out.println("file upload failed!");
        }

    }

    public void executeProgram(ProcessorModel pm) {

        final ProcessorModel processorModel = pm;
//        if (!processorModel.isValidProcessor()) {
//            VisatApp.getApp().showErrorDialog(programName, processorModel.getProgramErrorMessage());
//            return;
//
//        }

        ProgressMonitorSwingWorker swingWorker = new ProgressMonitorSwingWorker<File, Object>(getAppContext().getApplicationWindow(), "Running " + programName + " ...") {
            @Override
            protected File doInBackground(ProgressMonitor pm) throws Exception {

                final Process process = processorModel.executeProcess();
                final ProcessObserver processObserver = new ProcessObserver(process, programName, pm);
                final ConsoleHandler ch = new ConsoleHandler(programName);
                processObserver.addHandler(new ProgressHandler(programName, processorModel.getProgressPattern()));
                processObserver.addHandler(ch);
                processObserver.startAndWait();
                processorModel.setExecutionLogMessage(ch.getExecutionErrorLog());
                int exitCode = process.exitValue();

                pm.done();

                if (exitCode != 0) {
                    throw new IOException(programName + " failed with exit code " + exitCode + ".\nCheck log for more details.");
                }

                File outputFile = new File(processorModel.getOfileName());
                if (openOutputInApp) {
                    getAppContext().getProductManager().addProduct(ProductIO.readProduct(outputFile));
                }

                SeadasLogger.getLogger().finest("Final output file name: " + outputFile);

                return outputFile;
            }

            @Override
            protected void done() {
                try {
                    final File outputFile = get();

                    VisatApp.getApp().showInfoDialog(programName, programName + " done!\nOutput written to:\n" + outputFile, null);
                    ProcessorModel secondaryProcessor = processorModel.getSecondaryProcessor();
                    if (secondaryProcessor != null) {
                        ProgramExecutor pe = new ProgramExecutor();
                        int exitCode = pe.executeProgram(secondaryProcessor.getProgramCmdArray());
                        if (exitCode == 0) {
                            VisatApp.getApp().showInfoDialog(secondaryProcessor.getProgramName(),
                                    secondaryProcessor.getProgramName() + " done!\n", null);
                        }

                    }
                } catch (InterruptedException e) {
                    //
                } catch (ExecutionException e) {
                    //VisatApp.getApp().showErrorDialog(programName, "execution exception: " + e.getMessage() + "\n" + processorModel.getExecutionLogMessage());
                    displayMessage(programName, "execution exception: " + e.getMessage() + "\n" + processorModel.getExecutionLogMessage());


                }
            }
        };

        swingWorker.execute();
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

        ProgressHandler(String programName, Pattern progressPattern) {
            this.programName = programName;
            this.progressPattern = progressPattern;
        }

        @Override
        public void handleLineOnStdoutRead(String line, Process process, ProgressMonitor pm) {
            if (!progressSeen) {
                progressSeen = true;
                pm.beginTask(programName, 1000);
            }

            Matcher matcher = progressPattern.matcher(line);
            if (matcher.find()) {
                int scan = Integer.parseInt(matcher.group(1));
                int numScans = Integer.parseInt(matcher.group(2));

                scan = (scan * 1000) / numScans;
                pm.worked(scan - lastScan);
                lastScan = scan;
            }

            pm.setTaskName(programName);
            pm.setSubTaskName(line);
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process, ProgressMonitor pm) {
        }
    }

    private static class ConsoleHandler implements ProcessObserver.Handler {

        String programName;

        private String executionErrorLog = "";

        ConsoleHandler(String programName) {
            this.programName = programName;
        }

        @Override
        public void handleLineOnStdoutRead(String line, Process process, ProgressMonitor pm) {
            SeadasLogger.getLogger().info(programName + ": " + line);
            executionErrorLog = executionErrorLog + line + "\n";
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process, ProgressMonitor pm) {
            SeadasLogger.getLogger().info(programName + " stderr: " + line);
            executionErrorLog = executionErrorLog + line + "\n";
        }

        public String getExecutionErrorLog() {
            return executionErrorLog;
        }
    }

    private static class TerminationHandler implements ProcessObserver.Handler {

        @Override
        public void handleLineOnStdoutRead(String line, Process process, ProgressMonitor pm) {
            if (pm.isCanceled()) {
                process.destroy();
            }
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process, ProgressMonitor pm) {
            if (pm.isCanceled()) {
                process.destroy();
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