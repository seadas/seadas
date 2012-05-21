package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import gov.nasa.gsfc.seadas.ocssw.ProcessObserver;
import org.esa.beam.visat.VisatApp;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/16/12
 * Time: 4:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProgramExecutor {
    private static final String PROCESSING_SCAN_REGEX = "Processing scan .+?\\((\\d+) of (\\d+)\\)";
    static final Pattern PROCESSING_SCAN_PATTERN = Pattern.compile(PROCESSING_SCAN_REGEX);

        File outputFile;

        public void executeProgram(ProcessorModel pm) {

             final ProcessorModel processorModel = pm;
             if (!processorModel.isValidProcessor()) {
                 VisatApp.getApp().showErrorDialog(processorModel.getProgramName(), processorModel.getProgramErrorMessage());
                 return;

             }



             //final File outputFile = processorModel.getOutputFile();

             //SeadasLogger.getLogger().info("output file: " + outputFile);
             ProgressMonitorSwingWorker swingWorker = new ProgressMonitorSwingWorker<File, Object>(VisatApp.getApp().getApplicationWindow(), "Running" + processorModel.getProgramName() + " ...") {
                 @Override
                 protected File doInBackground(ProgressMonitor pm) throws Exception {

                     //final Process process = Runtime.getRuntime().exec(processorModel.getProgramCmdArray(), processorModel.getProgramEnv(), processorModel.getProgramRoot() );
                     final Process process = processorModel.executeProcess();
                     final ProcessObserver processObserver = new ProcessObserver(process, processorModel.getProgramName(), pm);
                     processObserver.addHandler(new ProgressHandler(processorModel.getProgramName()));
                     processObserver.addHandler(new ConsoleHandler(processorModel.getProgramName()));
                     processObserver.startAndWait();

                     int exitCode = process.exitValue();

                     pm.done();
                     //process.getOutputStream();

                     if (exitCode != 0) {
                         throw new IOException(processorModel.getProgramName() + " failed with exit code " + exitCode + ".\nCheck log for more details.");
                     }
                     outputFile = new File(processorModel.getParamValue(processorModel.getPrimaryOutputFileOptionName())  );
//                     if (openOutputInApp ) {
//                         getAppContext().getProductManager().addProduct(ProductIO.readProduct(outputFile));
//                     }

                     SeadasLogger.getLogger().finest("Final output file name: " + outputFile)  ;

                     return outputFile;
                 }

                 @Override
                 protected void done() {
                     try {
                         final File outputFile = get();
                         VisatApp.getApp().showInfoDialog(processorModel.getProgramName()  , processorModel.getProgramName() + " done!\nOutput written to:\n" + outputFile, null);
                     } catch (InterruptedException e) {
                         //
                     } catch (ExecutionException e) {
                         VisatApp.getApp().showErrorDialog(processorModel.getProgramName(), "execution exception: " + e.getMessage());
                     }
                 }
             };

             swingWorker.execute();
             //swingWorker.get();

         }

        public File getOutputFile(){
            return outputFile;
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
