package gov.nasa.gsfc.seadas.ocsswrest.process;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aabduraz on 3/22/16.
 */
public class ORSProcessObserver {
    private static final String STDOUT = "stdout";
    private static final String STDERR = "stderr";
    private final Process process;
    private final String processName;
    private final String  jobID;
    private String processMonitorStdoutTableName;
    private String processMonitorStderrTableName;

    /**
     * Constructor.
     *
     * @param process     The process to be observed
     * @param processName A name that represents the process
     */
    public ORSProcessObserver(final Process process, String processName, String jobID) {
        this.process = process;
        this.processName = processName;
        this.jobID = jobID;
        SQLiteJDBC.createProcessMonitorTables(jobID);
        SQLiteJDBC.updateItem(SQLiteJDBC.FILE_TABLE_NAME, jobID, SQLiteJDBC.FileTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.STARTED.getValue());
        processMonitorStdoutTableName = SQLiteJDBC.PROCESS_MONITOR_STDOUT_TABLE_NAME + "_" + jobID;
        processMonitorStderrTableName = SQLiteJDBC.PROCESS_MONITOR_STDERR_TABLE_NAME + "_" + jobID;

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
                SQLiteJDBC.dropProcessMonitorTables(jobID);
                return;
            }
        }

        //Both threads completed monitoring the process. Delete the associated tables.
        SQLiteJDBC.dropProcessMonitorTables(jobID);
        SQLiteJDBC.updateItem(SQLiteJDBC.FILE_TABLE_NAME, jobID, SQLiteJDBC.FileTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.COMPLETED.getValue());

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
                System.out.println(" in progress monitor reading ... " );
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
                        SQLiteJDBC.insertItemInProcessMonitorTables(SQLiteJDBC.PROCESS_STDOUT_DB_URL, processMonitorStdoutTableName, STDOUT, line);
                    } else {
                        //save error stream in the table (line, process);
                        SQLiteJDBC.insertItemInProcessMonitorTables(SQLiteJDBC.PROCESS_STDERR_DB_URL, processMonitorStderrTableName, STDERR, line );
                        System.out.println(" in progress monitor: " + STDERR + "  "+ line);
                    }
                }

            } finally {
                reader.close();
            }
        }
    }
}
