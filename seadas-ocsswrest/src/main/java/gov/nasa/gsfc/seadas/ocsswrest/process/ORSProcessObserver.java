package gov.nasa.gsfc.seadas.ocsswrest.process;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;

import java.io.*;

/**
 * Created by aabduraz on 3/22/16.
 */
public class ORSProcessObserver {

    public static final String PROCESS_INPUT_STREAM_FILE_NAME = "processInputStream.log";
    public static final String PROCESS_ERROR_STREAM_FILE_NAME = "processErrorStream.log";

    private static final String STDOUT = "stdout";
    private static final String STDERR = "stderr";
    private final Process process;
    private final String processName;
    private final String jobId;

    private String processMonitorStdoutTableName;
    private String processMonitorStderrTableName;

    private String processInputStreamFileName;
    private String processErrorStreamFileName;

    /**
     * Constructor.
     *
     * @param process     The process to be observed
     * @param processName A name that represents the process
     */
    public ORSProcessObserver(final Process process, String processName, String jobId) {
        this.process = process;
        this.processName = processName;
        this.jobId = jobId;
        String serverWorkingDir = SQLiteJDBC.retrieveItem(SQLiteJDBC.FILE_TABLE_NAME, jobId, SQLiteJDBC.FileTableFields.WORKING_DIR_PATH.getFieldName());
        processInputStreamFileName = serverWorkingDir + File.separator + jobId + File.separator + PROCESS_INPUT_STREAM_FILE_NAME;
        processErrorStreamFileName = serverWorkingDir + File.separator + jobId + File.separator + PROCESS_ERROR_STREAM_FILE_NAME;
        SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.STARTED.getValue());
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

            System.out.println("reading from process input stream ...");
            InputStream inputStream = null; // = type.equals("stdout") ? process.getInputStream() : process.getErrorStream();
            switch (type) {
                case STDOUT:
                    inputStream = process.getInputStream();
                    //SQLiteJDBC.updateInputStreamItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.INPUTSTREAM.getFieldName(), inputStream);
                    writeProcessStreamToFile(inputStream, processInputStreamFileName);
                case STDERR:
                    inputStream = process.getErrorStream();
                    //SQLiteJDBC.updateInputStreamItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.ERRORSTREAM.getFieldName(), inputStream);
                    writeProcessStreamToFile(inputStream, processErrorStreamFileName);
            }
        }


        private void writeProcessStreamToFile(InputStream inputStream, String fileName) {

            OutputStream outputStream = null;

            try {

                // write the inputStream to a FileOutputStream
                File currentFile = new File(fileName);
                if ( !currentFile.getParentFile().exists() ) {
                    currentFile.getParentFile().mkdir();
                }

                outputStream = new FileOutputStream(new File(fileName), true);

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }

                if (type.equals(STDOUT)) {
                    SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), SQLiteJDBC.ProcessStatusFlag.COMPLETED.getValue());
                }
                System.out.println("Appended process input stream in " + fileName);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    try {
                        // outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}
