package gov.nasa.gsfc.seadas.processing.core;

import com.bc.ceres.core.ProgressMonitor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * An observer that notifies its {@link Handler handlers} about lines of characters that have been written
 * by a process to both {@code stdout} and {@code stderr} output streams.
 *
 * @author Norman Fomferra
 * @since SeaDAS 7.0
 */
public class ProcessObserver {
    protected static final String STDOUT = "stdout";
    protected static final String STDERR = "stderr";
    protected final Process process;
    protected final String processName;
    protected final ProgressMonitor progressMonitor;
    protected final ArrayList<Handler> handlers;

    private int processExitValue;

    /**
     * Constructor.
     *
     * @param process         The process to be observed
     * @param processName     A name that represents the process
     * @param progressMonitor A progress monitor
     */
    public ProcessObserver(final Process process, String processName, ProgressMonitor progressMonitor) {
        this.process = process;
        this.processName = processName;
        this.progressMonitor = progressMonitor;
        this.handlers = new ArrayList<Handler>();
        processExitValue = -1;
    }

    /**
     * Adds a new handler to this observer.
     *
     * @param handler The new handler.
     */
    public void addHandler(Handler handler) {
        handlers.add(handler);
    }

    /**
     * Starts observing the given process. The method blocks until both {@code stdout} and {@code stderr}
     * streams are no longer available. If the progress monitor is cancelled, the process will be destroyed.
     */
    public void startAndWait() {
        processExitValue = -1;
        final Thread stdoutReaderThread = new LineReaderThread(STDOUT);
        final Thread stderrReaderThread = new LineReaderThread(STDERR);
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
                e.printStackTrace();
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

    public int getProcessExitValue() {
        return processExitValue;
    }

    public void setProcessExitValue(int processExitValue) {
        this.processExitValue = processExitValue;
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
            final InputStream inputStream = type.equals("stdout") ? process.getInputStream() : process.getErrorStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    fireLineRead(line);
                }
            } finally {
                reader.close();
            }
            try {
                processExitValue = process.waitFor();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }

        private void fireLineRead(String line) {
            for (Handler handler : handlers) {
                if (type.equals("stdout")) {
                    handler.handleLineOnStdoutRead(line, process, progressMonitor);
                } else {
                    handler.handleLineOnStderrRead(line, process, progressMonitor);
                }
            }
        }

    }
}