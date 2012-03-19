package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.core.CoreException;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.runtime.ConfigurationElement;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.ocssw.ProcessObserver;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.actions.AbstractVisatAction;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A ...
 *
 * @author Norman Fomferra
 * @since SeaDAS 7.0
 */
public class CallCloProgramAction extends AbstractVisatAction {

    private static final String PROCESSING_SCAN_REGEX = "Processing scan .+?\\((\\d+) of (\\d+)\\)";
    static final Pattern PROCESSING_SCAN_PATTERN = Pattern.compile(PROCESSING_SCAN_REGEX);

    String programName;
    String dialogTitle;
    String xmlFileName;

    @Override
    public void configure(ConfigurationElement config) throws CoreException {
        programName = getConfigString(config, "programName");
        if (programName == null) {
            throw new CoreException("Missing DefaultOperatorAction property 'programName'.");
        }
        dialogTitle = getValue(config, "dialogTitle", programName);
        xmlFileName = getValue(config, "xmlFileName", ParamUtils.NO_XML_FILE_SPECIFIED );
        super.configure(config);
    }

    @Override
    public void actionPerformed(CommandEvent event) {


        final AppContext appContext = getAppContext();

        final Processor processor = new Processor(programName, xmlFileName);
        final CloProgramUI cloProgramUI = new CloProgramUI(programName, dialogTitle, processor.getParamList() );

        //final String title = event.getCommand().getText();
        final Window parent = appContext.getApplicationWindow();
        final ModalDialog modalDialog = new ModalDialog(parent, dialogTitle, cloProgramUI, ModalDialog.ID_OK_CANCEL, null);
        modalDialog.getButton(ModalDialog.ID_OK).setText("Run");
        final int dialogResult = modalDialog.show();
        if (dialogResult != ModalDialog.ID_OK) {
            return;
        }

        final Product selectedProduct = cloProgramUI.getSelectedSourceProduct();
        if (selectedProduct == null) {
            VisatApp.getApp().showErrorDialog(programName, "No product selected.");
            return;
        }
        final File ocsswRoot;
        try {
            ocsswRoot = OCSSW.getOcsswRoot();
        } catch (IOException e) {
            VisatApp.getApp().showErrorDialog(programName, e.getMessage());
            return;
        }
        final String ocsswArch;
        try {
            ocsswArch = OCSSW.getOcsswArch();
        } catch (IOException e) {
            VisatApp.getApp().showErrorDialog(programName, e.getMessage());
            return;
        }

        final File inputFile = selectedProduct.getFileLocation();
        final File outputDir = inputFile.getParentFile();
        final File parameterFile = createParameterFile(outputDir, cloProgramUI.getProcessingParameters());
        if (parameterFile == null) {
            JOptionPane.showMessageDialog(parent,
                                          "Unable to create parameter file '" + parameterFile + "'.",
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }
        final File outputFile = new File(outputDir, "l2gen-out-" + Long.toHexString(System.nanoTime()));

        ProgressMonitorSwingWorker swingWorker = new ProgressMonitorSwingWorker<File, Object>(parent, "Running l2gen...") {
            @Override
            protected File doInBackground(ProgressMonitor pm) throws Exception {

                final String[] cmdarray = {
                        ocsswRoot.getPath() + "/run/bin/" + ocsswArch + "/" + programName,
                        "ifile=" + inputFile,
                        "ofile=" + outputFile,
                        "par=" + parameterFile
                };
                final String[] envp = {
                        "OCSSWROOT=${OCSSWROOT}".replace("${OCSSWROOT}", ocsswRoot.getPath()),
                        "OCSSW_ARCH=${OCSSW_ARCH}".replace("${OCSSW_ARCH}", ocsswArch),
                        "OCDATAROOT=${OCSSWROOT}/run/data".replace("${OCSSWROOT}", ocsswRoot.getPath()),
                };

                final Process process = Runtime.getRuntime().exec(cmdarray, envp, ocsswRoot);

                final ProcessObserver processObserver = new ProcessObserver(process, programName, pm);
                processObserver.addHandler(new ProgressHandler());
                processObserver.addHandler(new ConsoleHandler());
                processObserver.startAndWait();

                int exitCode = process.exitValue();

                pm.done();

                if (exitCode != 0) {
                    throw new IOException("l2gen failed with exit code " + exitCode + ".\nCheck log for more details.");
                }

                appContext.getProductManager().addProduct(ProductIO.readProduct(outputFile));

                return outputFile;
            }

            @Override
            protected void done() {
                try {
                    final File outputFile = get();
                    VisatApp.getApp().showInfoDialog("l2gen", "l2gen done!\nOutput written to:\n" + outputFile, null);
                } catch (InterruptedException e) {
                    //
                } catch (ExecutionException e) {
                    VisatApp.getApp().showErrorDialog("l2gen", e.getMessage());
                }
            }
        };

        swingWorker.execute();
    }

    private File createParameterFile(File outputDir, final String parameterText) {
        try {
            final File tempFile = File.createTempFile("l2gen", ".par", outputDir);
            tempFile.deleteOnExit();
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(tempFile);
                fileWriter.write(parameterText);
            } finally {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }
            return tempFile;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Handler that tries to extract progress from stdout of l2gen
     */
    private static class ProgressHandler implements ProcessObserver.Handler {

        boolean progressSeen;
        int lastScan = 0;

        @Override
        public void handleLineOnStdoutRead(String line, Process process, ProgressMonitor pm) {

            Matcher matcher = PROCESSING_SCAN_PATTERN.matcher(line);
            if (matcher.find()) {

                int scan = Integer.parseInt(matcher.group(1));
                int numScans = Integer.parseInt(matcher.group(2));

                if (!progressSeen) {
                    progressSeen = true;
                    pm.beginTask("l2gen", numScans);
                }
                pm.worked(scan - lastScan);
                lastScan = scan;
            }

            pm.setTaskName("l2gen");
            pm.setSubTaskName(line);
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process, ProgressMonitor pm) {
        }
    }

    private static class ConsoleHandler implements ProcessObserver.Handler {

        @Override
        public void handleLineOnStdoutRead(String line, Process process, ProgressMonitor pm) {
            System.out.println("l2gen: " + line);
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process, ProgressMonitor pm) {
            System.err.println("l2gen stderr: " + line);
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