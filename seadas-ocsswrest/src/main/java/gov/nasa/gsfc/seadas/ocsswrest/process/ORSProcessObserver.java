package gov.nasa.gsfc.seadas.ocsswrest.process;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;
import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWConfig;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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

    private int processInputStreamPortNumber;
    private int processErrorStreamPortNumber;


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
        processInputStreamPortNumber = new Integer(System.getProperty(OCSSWConfig.OCSSW_PROCESS_INPUTSTREAM_SOCKET_PORT_NUMBER_PROPERTY)).intValue();
        processErrorStreamPortNumber = new Integer(System.getProperty(OCSSWConfig.OCSSW_PROCESS_ERRORSTREAM_SOCKET_PORT_NUMBER_PROPERTY)).intValue();
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
                e.printStackTrace();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void read() {

            InputStream inputStream = type.equals("stdout") ? process.getInputStream() : process.getErrorStream();
            writeProcessStreamToSocket(inputStream, type.equals("stdout") ? processInputStreamPortNumber : processErrorStreamPortNumber);
        }

        private void writeProcessStreamToSocket(InputStream inputStream, int portNumber) {
            System.out.println("server process observer: " + portNumber);
            try (
                    ServerSocket serverSocket =
                            new ServerSocket(portNumber);
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out =
                            new PrintWriter(clientSocket.getOutputStream(), true);
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
            ) {
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    out.println(inputLine);
                }
                int processStatus = process.waitFor();
                System.out.println("final process status: " + processStatus);
                SQLiteJDBC.updateItem(SQLiteJDBC.PROCESS_TABLE_NAME, jobId, SQLiteJDBC.ProcessTableFields.STATUS.getFieldName(), new Integer(process.exitValue()).toString());
                serverSocket.setReuseAddress(true);
            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port "
                        + portNumber + " or listening for a connection");
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
