package gov.nasa.obpg.seadas.ocssw;

import com.bc.ceres.core.ProgressMonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * An observer that notifies its {@link Handler handlers} about lines of characters that have been written
 * by a process to both {@code stdout} and {@code stderr} output streams.
 *
 * @author Norman Fomferra
 * @since SeaDAS 7.0
 */
public class ProcessObserver {
    private static final String STDOUT = "stdout";
    private static final String STDERR = "stderr";
    private final Process process;
    private final String processName;
    private final ProgressMonitor pm;
    private final ArrayList<Handler> handlers;

    /**
     * Constructor.
     *
     * @param process     The process to be observed
     * @param processName A name that represents the process
     * @param pm          A progress monitor
     */
    public ProcessObserver(final Process process, String processName, ProgressMonitor pm) {
        this.process = process;
        this.processName = processName;
        this.pm = pm;
        this.handlers = new ArrayList<Handler>();
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
    public final void startAndWait() {
        final Thread stdoutReaderThread = new LineReaderThread(STDOUT);
        final Thread stderrReaderThread = new LineReaderThread(STDERR);
        stdoutReaderThread.start();
        stderrReaderThread.start();
        awaitTermintation(stdoutReaderThread, stderrReaderThread);
    }

    private void awaitTermintation(Thread stdoutReaderThread, Thread stderrReaderThread) {
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
            if (pm.isCanceled()) {
                // todo - check what is best done now:
                //        1. just leave, and let the process be unattended
                //      * 2. destroy the process (current impl.)
                //        3. throw a checked ProgressObserverException
                process.destroy();
            }
        }
    }

    /**
     * A handler that will be informed if a new line has been read from either {@code stdout} or {@code stderr}.
     */
    public static interface Handler {
        /**
         * Handle the new line that has been read from {@code stdout}.
         *
         * @param line    The line.
         * @param process The process.
         * @param pm      The progress monitor, that is used to monitor the progress of the running process.
         */
        void handleLineOnStdoutRead(String line, Process process, ProgressMonitor pm);

        /**
         * Handle the new line that has been read from {@code stderr}.
         *
         * @param line    The line.
         * @param process The process.
         * @param pm      The progress monitor, that is used to monitor the progress of the running process.
         */
        void handleLineOnStderrRead(String line, Process process, ProgressMonitor pm);
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
        }

        private void fireLineRead(String line) {
            for (Handler handler : handlers) {
                if (type.equals("stdout")) {
                    handler.handleLineOnStdoutRead(line, process, pm);
                } else {
                    handler.handleLineOnStderrRead(line, process, pm);
                }
            }
        }

    }
}