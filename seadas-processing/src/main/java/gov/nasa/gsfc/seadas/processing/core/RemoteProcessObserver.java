package gov.nasa.gsfc.seadas.processing.core;

import com.bc.ceres.core.ProgressMonitor;
import gov.nasa.gsfc.seadas.OCSSWInfo;
import gov.nasa.gsfc.seadas.ocssw.OCSSWClient;

import javax.ws.rs.client.WebTarget;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by aabduraz on 9/12/17.
 */
public class RemoteProcessObserver extends ProcessObserver {

    OCSSWInfo ocsswInfo;
    WebTarget target;
    private String jobId;
    private boolean serverProcessCompleted;

    /**
     * Constructor.
     *
     * @param process         The process to be observed
     * @param processName     A name that represents the process
     * @param progressMonitor A progress monitor
     */
    public RemoteProcessObserver(Process process, String processName, ProgressMonitor progressMonitor) {
        super(process, processName, progressMonitor);
        this.ocsswInfo = OCSSWInfo.getInstance();
        OCSSWClient ocsswClient = new OCSSWClient(ocsswInfo.getResourceBaseUri());
        target = ocsswClient.getOcsswWebTarget();
        setProcessExitValue(-1);
        serverProcessCompleted = false;
    }


    /**
     * Starts observing the given process. The method blocks until both {@code stdout} and {@code stderr}
     * streams are no longer available. If the progress monitor is cancelled, the process will be destroyed.
     */
    @Override
    public final void startAndWait() {
        final Thread stdoutReaderThread = new RemoteProcessObserver.LineReaderThread(STDOUT);
        final Thread stderrReaderThread = new RemoteProcessObserver.LineReaderThread(STDERR);
        stdoutReaderThread.start();
        stderrReaderThread.start();
        awaitTermination(stdoutReaderThread, stderrReaderThread);
    }

    private void awaitTermination(Thread stdoutReaderThread, Thread stderrReaderThread) {
        while (stdoutReaderThread.isAlive() && stderrReaderThread.isAlive()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // todo - check what is best done now:
                //      * 1. just leave, and let the process be unattended (current impl.)
                //        2. destroy the process
                //        3. throw a checked ProgressObserverException
                return;
            }
            if (progressMonitor.isCanceled()) {
                // todo - check what is best done now:
                //        1. just leave, and let the process be unattended
                //      * 2. destroy the process (current impl.)
                //        3. throw a checked ProgressObserverException
                process.destroy();
            }
        }
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public boolean isServerProcessCompleted() {
        return serverProcessCompleted;
    }

    public void setServerProcessCompleted(boolean serverProcessCompleted) {
        this.serverProcessCompleted = serverProcessCompleted;
    }

    /**
     * A handler that will be informed if a new line has been read from either {@code stdout} or {@code stderr}.
     */
    public static interface Handler {
        /**
         * Handle the new line that has been read from {@code stdout}.
         *
         * @param line            The line.
         * @param process         The process.
         * @param progressMonitor The progress monitor, that is used to monitor the progress of the running process.
         */
        void handleLineOnStdoutRead(String line, Process process, ProgressMonitor progressMonitor);

        /**
         * Handle the new line that has been read from {@code stderr}.
         *
         * @param line            The line.
         * @param process         The process.
         * @param progressMonitor The progress monitor, that is used to monitor the progress of the running process.
         */
        void handleLineOnStderrRead(String line, Process process, ProgressMonitor progressMonitor);
    }

    private class LineReaderThread extends Thread {
        private final String type;

        public LineReaderThread(String type) {
            super(processName + "-" + type);
            this.type = type;
        }

        @Override
        public void run() {
            try {
                read();
            } catch (IOException e) {
                // cannot be handled
            }
        }

        private void read() throws IOException {
            final InputStream inputStream = type.equals("stdout") ? readProcessStream(ocsswInfo.getProcessInputStreamPort()) : readProcessStream(ocsswInfo.getProcessErrorStreamPort());
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    fireLineRead(line);
                }
            } finally {
                reader.close();
            }
            String processStatus = target.path("ocssw").path("processStatus").path(jobId).request().get(String.class);
            setProcessExitValue(new Integer(processStatus).intValue());
        }


        private InputStream readProcessStream(int portNumber) {
            String hostName = null;
            InputStream inputStream = null;
            try {
                Socket echoSocket = new Socket(hostName, portNumber);
                inputStream = echoSocket.getInputStream();
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host " + hostName);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to " +
                        hostName);
                System.exit(1);
            }
            return inputStream;
        }

        protected void fireLineRead(String line) {
            for (ProcessObserver.Handler handler : handlers) {
                if (type.equals("stdout")) {
                    handler.handleLineOnStdoutRead(line, process, progressMonitor);
                } else {
                    handler.handleLineOnStderrRead(line, process, progressMonitor);
                }
            }
        }

    }
}
