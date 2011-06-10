package gov.nasa.obpg.seadas.ocssw;

import com.bc.ceres.core.ProgressMonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 */
public class ProcessObserver {
    private final Process process;
    private final String processName;
    private final ProgressMonitor pm;
    private final ArrayList<Handler> handlers;

    public ProcessObserver(final Process process, String processName, ProgressMonitor pm) {
        this.process = process;
        this.processName = processName;
        this.pm = pm;
        this.handlers = new ArrayList<Handler>();
    }

    public void addHandler(Handler handler) {
        handlers.add(handler);
    }

    public final void start() throws InterruptedException, IOException {

        final Thread stdoutProcessor = new Thread(processName + ".stdout") {
            @Override
            public void run() {
                try {
                    processStdout();
                } catch (IOException e) {
                    // cannot be handled
                }
            }
        };
        stdoutProcessor.start();

        final Thread stderrProcessor = new Thread(processName + ".stderr") {
            @Override
            public void run() {
                try {
                    processStderr();
                } catch (IOException e) {
                    // cannot be handled
                }
            }
        };
        stderrProcessor.start();

        while (stdoutProcessor.isAlive() && stderrProcessor.isAlive()) {
            Thread.sleep(100);
            if (pm.isCanceled()) {
                process.destroy();
            }
        }
    }

    private void processStdout() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                fireStdoutSeen(line);
            }
        } finally {
            reader.close();
        }
    }

    private void processStderr() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                fireStderrSeen(line);
            }
        } finally {
            reader.close();
        }
    }

    private void fireStdoutSeen(String line) {
        for (Handler handler : handlers) {
            handler.handleStdoutSeen(line, process, pm);
        }
    }

    private void fireStderrSeen(String line) {
        for (Handler handler : handlers) {
            handler.handleStderrSeen(line, process, pm);
        }
    }

    public static interface Handler {
        void handleStdoutSeen(String line, Process process, ProgressMonitor pm);

        void handleStderrSeen(String line, Process process, ProgressMonitor pm);
    }

}