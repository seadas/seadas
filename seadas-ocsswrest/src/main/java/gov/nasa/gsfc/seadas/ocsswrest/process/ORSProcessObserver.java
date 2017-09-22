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
    private final String jobID;
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
        SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobID, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.STARTED.getValue());
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
                    System.out.println(" in progress monitor: " + type + "  " + line);
                    if (type.equals("stdout")) {
                        SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobID, SQLiteJDBC.ProcessTableFields.STD_OUT_NAME.getFieldName(), line);
                    } else {
                        SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobID, SQLiteJDBC.ProcessTableFields.STD_OUT_NAME.getFieldName(), line);
                    }

                }
                System.out.println("process completed!");
                System.out.println(" in progress monitor process status before update: " + SQLiteJDBC.retrieveItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobID, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName()));
                SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobID, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.COMPLETED.getValue());
                System.out.println(" in progress monitor process status: " + SQLiteJDBC.ProcessStatusFlag.COMPLETED.getValue());

            } finally {
                reader.close();
            }
        }
    }
}
