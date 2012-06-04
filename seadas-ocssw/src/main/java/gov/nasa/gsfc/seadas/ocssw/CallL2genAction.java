package gov.nasa.gsfc.seadas.ocssw;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.actions.AbstractVisatAction;

import javax.swing.JOptionPane;
import java.awt.Window;
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
public class CallL2genAction extends AbstractVisatAction {

    private static final String PROCESSING_SCAN_REGEX = "Processing scan .+?\\((\\d+) of (\\d+)\\)";
    static final Pattern PROCESSING_SCAN_PATTERN = Pattern.compile(PROCESSING_SCAN_REGEX);

    @Override
    public void actionPerformed(CommandEvent event) {

        final AppContext appContext = getAppContext();
        // UI Ã¶ffnen
        final L2genGiopUI l2genGiopUI = new L2genGiopUI();

        final String title = event.getCommand().getText();
        final Window parent = appContext.getApplicationWindow();
        final ModalDialog modalDialog = new ModalDialog(parent, title, l2genGiopUI, ModalDialog.ID_OK_CANCEL, null);
        modalDialog.getButton(ModalDialog.ID_OK).setText("Run");
        final int dialogResult = modalDialog.show();
        if (dialogResult != ModalDialog.ID_OK) {
            return;
        }

        final Product selectedProduct = l2genGiopUI.getSelectedSourceProduct();
        if (selectedProduct == null) {
            VisatApp.getApp().showErrorDialog("l2genGUI", "No product selected.");
            return;
        }
        final File ocsswRoot;
        try {
            ocsswRoot = OCSSW.getOcsswRoot();
        } catch (IOException e) {
            VisatApp.getApp().showErrorDialog("l2genGUI", e.getMessage());
            return;
        }

        final File inputFile = selectedProduct.getFileLocation();
        final File outputDir = inputFile.getParentFile();
        final File parameterFile = createParameterFile(outputDir, l2genGiopUI.getProcessingParameters());
        if (parameterFile == null) {
            JOptionPane.showMessageDialog(parent,
                                          "Unable to create parameter file '" + parameterFile + "'.",
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }
        final File outputFile = new File(outputDir, "l2genGUI-out-" + Long.toHexString(System.nanoTime()));

        ProgressMonitorSwingWorker swingWorker = new ProgressMonitorSwingWorker<File, Object>(parent, "Running l2genGUI...") {
            @Override
            protected File doInBackground(ProgressMonitor pm) throws Exception {

                final String[] cmdarray = {
                        "${OCSSWROOT}/run/scripts/ocssw_runner".replace("${OCSSWROOT}", ocsswRoot.getPath()),
                        "l2genGUI",
                        "ifile=" + inputFile,
                        "ofile=" + outputFile,
                        "par=" + parameterFile
                };
                final String[] envp = {
                        "OCSSWROOT=${OCSSWROOT}".replace("${OCSSWROOT}", ocsswRoot.getPath()),
                 };

                final Process process = Runtime.getRuntime().exec(cmdarray, envp, outputDir);

                final ProcessObserver processObserver = new ProcessObserver(process, "l2genGUI", pm);
                processObserver.addHandler(new ProgressHandler());
                processObserver.addHandler(new ConsoleHandler());
                processObserver.startAndWait();

                int exitCode = process.exitValue();

                pm.done();

                if (exitCode != 0) {
                    throw new IOException("l2genGUI failed with exit code " + exitCode + ".\nCheck log for more details.");
                }

                appContext.getProductManager().addProduct(ProductIO.readProduct(outputFile));

                return outputFile;
            }

            @Override
            protected void done() {
                try {
                    final File outputFile = get();
                    VisatApp.getApp().showInfoDialog("l2genGUI", "l2genGUI done!\nOutput written to:\n" + outputFile, null);
                } catch (InterruptedException e) {
                    //
                } catch (ExecutionException e) {
                    VisatApp.getApp().showErrorDialog("l2genGUI", e.getMessage());
                }
            }
        };

        swingWorker.execute();
    }

    private File createParameterFile(File outputDir, final String parameterText) {
        try {
            final File tempFile = File.createTempFile("l2genGUI", ".par", outputDir);
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
     * Handler that tries to extract progress from stdout of l2genGUI
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
                    pm.beginTask("l2genGUI", numScans);
                }
                pm.worked(scan - lastScan);
                lastScan = scan;
            }

            pm.setTaskName("l2genGUI");
            pm.setSubTaskName(line);
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process, ProgressMonitor pm) {
        }
    }

    private static class ConsoleHandler implements ProcessObserver.Handler {

        @Override
        public void handleLineOnStdoutRead(String line, Process process, ProgressMonitor pm) {
            System.out.println("l2genGUI: " + line);
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process, ProgressMonitor pm) {
            System.err.println("l2genGUI stderr: " + line);
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