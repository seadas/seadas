package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.core.CoreException;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.runtime.ConfigurationElement;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import gov.nasa.gsfc.seadas.ocssw.ProcessObserver;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ParamUtils;
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

    private static final String PROCESSING_SCAN_REGEX = "Processing scan .+?\\((\\d+) of (\\d+)\\)";
    static final Pattern PROCESSING_SCAN_PATTERN = Pattern.compile(PROCESSING_SCAN_REGEX);

    String programName;
    String dialogTitle;
    String xmlFileName;

    private boolean printLogToConsole = true;
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

    private String getXMLFileName() {
        String xmlFileName = programName + "_paramInfo.xml";
        ProcessorModel pm = new ProcessorModel(programName);
        ParamInfo pi;
        pi = new ParamInfo("dumpOption", "-dump_options_xmlfile");
        //pi.setOrder(1);
        pm.addParamInfo(pi);
        pi = new ParamInfo("ofile", xmlFileName);
        //pi.setOrder(2);
        pm.addParamInfo(pi);


        ProgramExecutor pe = new ProgramExecutor();
        pe.executeProgram(pm);
        SeadasLogger.getLogger().info("xml file name: " + xmlFileName);
        return xmlFileName;
    }

    public String getProgramName() {
        return programName;
    }

    public String getXmlFileName() {
        return xmlFileName;
    }

    public String getDialogTitle() {
        return dialogTitle;
    }


    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    public void setXmlFileName(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }


    public CloProgramUI getProgramUI(AppContext appContext) {
        if (programName.indexOf("extract") != -1) {
            return new ExtractorUI(programName, xmlFileName);
        }

        return new CloProgramUIImpl(programName, xmlFileName);
    }

    @Override
    public void actionPerformed(CommandEvent event) {

        //System.setProperty("user.dir", "/User/Shared/ocssw/test");

        SeadasLogger.initLogger("ProcessingGUI_log", printLogToConsole);
        SeadasLogger.getLogger().setLevel(Level.INFO);

        //xmlFileName = getXMLFileName();

        final AppContext appContext = getAppContext();

        final CloProgramUI cloProgramUI = getProgramUI(appContext);

        final Window parent = appContext.getApplicationWindow();

        final ModalDialog modalDialog = new ModalDialog(parent, dialogTitle, cloProgramUI, ModalDialog.ID_OK_APPLY_CANCEL_HELP, programName);
         modalDialog.getJDialog().getContentPane().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
                System.out.println("window property changed 4");
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

        if (selectedProduct != null) {
            modalDialog.getButton(ModalDialog.ID_OK).setEnabled(false);
        }

        final ProcessorModel processorModel = cloProgramUI.getProcessorModel();
        openOutputInApp = cloProgramUI.isOpenOutputInApp();

        executeProgram(processorModel);

    }

    public void executeProgram(ProcessorModel pm) {

        final ProcessorModel processorModel = pm;
        if (!processorModel.isValidProcessor()) {
            VisatApp.getApp().showErrorDialog(programName, processorModel.getProgramErrorMessage());
            return;

        }


        //final File outputFile = processorModel.getOutputFile();

        //SeadasLogger.getLogger().info("output file: " + outputFile);
        ProgressMonitorSwingWorker swingWorker = new ProgressMonitorSwingWorker<File, Object>(getAppContext().getApplicationWindow(), "Running" + programName + " ...") {
            @Override
            protected File doInBackground(ProgressMonitor pm) throws Exception {

                //final Process process = Runtime.getRuntime().exec(processorModel.getProgramCmdArray(), processorModel.getProgramEnv(), processorModel.getProgramRoot() );
                final Process process = processorModel.executeProcess();
                final ProcessObserver processObserver = new ProcessObserver(process, programName, pm);
                processObserver.addHandler(new ProgressHandler(programName));
                processObserver.addHandler(new ConsoleHandler(programName));
                processObserver.startAndWait();

                int exitCode = process.exitValue();

                pm.done();
                //process.getOutputStream();

                if (exitCode != 0) {
                    throw new IOException(programName + " failed with exit code " + exitCode + ".\nCheck log for more details.");
                }
                File outputFile = new File(processorModel.getParamValue(processorModel.getPrimaryOutputFileOptionName()));
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
                } catch (InterruptedException e) {
                    //
                } catch (ExecutionException e) {
                    VisatApp.getApp().showErrorDialog(programName, "execution exception: " + e.getMessage());
                }
            }
        };

        swingWorker.execute();
        //swingWorker.get();

    }

    /**
     * Handler that tries to extract progress from stdout of ocssw processor
     */
    private static class ProgressHandler implements ProcessObserver.Handler {


        boolean progressSeen;
        int lastScan = 0;
        String programName;

        ProgressHandler(String programName) {
            this.programName = programName;
        }

        @Override
        public void handleLineOnStdoutRead(String line, Process process, ProgressMonitor pm) {

            Matcher matcher = PROCESSING_SCAN_PATTERN.matcher(line);
            if (matcher.find()) {

                int scan = Integer.parseInt(matcher.group(1));
                int numScans = Integer.parseInt(matcher.group(2));

                if (!progressSeen) {
                    progressSeen = true;
                    pm.beginTask(programName, numScans);
                }
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

        ConsoleHandler(String programName) {
            this.programName = programName;
        }

        @Override
        public void handleLineOnStdoutRead(String line, Process process, ProgressMonitor pm) {
            SeadasLogger.getLogger().info(programName + ": " + line);
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process, ProgressMonitor pm) {
            SeadasLogger.getLogger().info(programName + " stderr: " + line);
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

}