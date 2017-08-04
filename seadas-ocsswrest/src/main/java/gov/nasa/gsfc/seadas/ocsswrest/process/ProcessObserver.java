package gov.nasa.gsfc.seadas.ocsswrest.process;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by aabduraz on 3/22/16.
 */
public class ProcessObserver {
    private static final String STDOUT = "stdout";
    private static final String STDERR = "stderr";
    private static final String PROCESS_TABLE = "PROCESS_TABLE";
    private final Process process;
    private final String processName;
    private final String  jobID;

    /**
     * Constructor.
     *
     * @param process     The process to be observed
     * @param processName A name that represents the process
     */
    public ProcessObserver(final Process process, String processName, String jobID) {
        this.process = process;
        this.processName = processName;
        this.jobID = jobID;
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
        }
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
            final InputStream inputStream = type.equals(STDOUT) ? process.getInputStream() : process.getErrorStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (type.equals(STDOUT)) {
                        //save input stream in the table (line, process);
                        System.out.println(" in progress monitor: " + STDOUT + "  " +line);
                        SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobID, STDOUT, line);
                    } else {
                        //save error stream in the table (line, process);
                        SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobID, STDERR, line );
                        System.out.println(" in progress monitor: " + STDERR + "  "+ line);
                    }

                }
                SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobID, STDOUT, "done!");
                SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobID, STDERR, "done!");


            } finally {
                reader.close();
            }
        }
    }
}
