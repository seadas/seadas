package gov.nasa.gsfc.seadas.ocssw;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import gov.nasa.gsfc.seadas.OCSSWInfo;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.common.CallCloProgramAction;
import gov.nasa.gsfc.seadas.processing.common.SeadasFileUtils;
import gov.nasa.gsfc.seadas.processing.core.ProcessObserver;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

import java.io.IOException;
import java.util.Arrays;
import org.esa.snap.rcp.SnapApp;

/**
 * Created by aabduraz on 1/17/18.
 */
public class OCSSWExecutionMonitor {

    public void executeWithProgressMonitor(ProcessorModel processorModel, OCSSW ocssw, String programName){

        SnapApp snapApp = SnapApp.getDefault();

        ProgressMonitorSwingWorker swingWorker = new ProgressMonitorSwingWorker<String, Object>(snapApp.getMainFrame(), "Running " + programName + " ...") {
            protected String doInBackground(ProgressMonitor pm) throws Exception {

                ocssw.setMonitorProgress(true);
                ocssw.setIfileName(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
                final Process process = ocssw.execute(processorModel.getParamList());//ocssw.execute(processorModel.getParamList()); //OCSSWRunnerOld.execute(processorModel);
                if (process == null) {
                    throw new IOException(programName + " failed to create process.");
                }
                final ProcessObserver processObserver = ocssw.getOCSSWProcessObserver(process, programName, pm);
                final CallCloProgramAction.ConsoleHandler ch = new CallCloProgramAction.ConsoleHandler(programName);
                if (programName.equals(OCSSWInfo.getInstance().OCSSW_INSTALLER_PROGRAM_NAME)) {
                    processObserver.addHandler(new CallCloProgramAction.InstallerHandler(programName, processorModel.getProgressPattern()));
                } else {
                    processObserver.addHandler(new CallCloProgramAction.ProgressHandler(programName, processorModel.getProgressPattern()));
                }
                processObserver.addHandler(ch);
                processObserver.startAndWait();
                processorModel.setExecutionLogMessage(ch.getExecutionErrorLog());

                int exitCode = processObserver.getProcessExitValue();

                if (exitCode == 0) {
                    pm.done();
                    SeadasFileUtils.writeToDisk(processorModel.getIFileDir() + System.getProperty("file.separator") + "OCSSW_LOG_" + programName + ".txt",
                            "Execution log for " + "\n" + Arrays.toString(ocssw.getCommandArray()) + "\n" + processorModel.getExecutionLogMessage());
                } else
                {
                    throw new IOException(programName + " failed with exit code " + exitCode + ".\nCheck log for more details.");
                }


                ocssw.setMonitorProgress(false);
                return processorModel.getOfileName();
            }
        };
        swingWorker.execute();
    }
}

