package gov.nasa.gsfc.seadas.processing.core;

import com.bc.ceres.core.NullProgressMonitor;
import com.bc.ceres.core.ProgressMonitor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * @author Norman Fomferra
 */

public class ProcessObserverTest {

    private File shellExec;

    @Before
    public void setUp() throws Exception {
        String osName = System.getProperty("os.name");
        String shellExt = osName.toLowerCase().contains("windows") ? ".bat" : ".sh";
        URL shellExecUrl = getClass().getResource(getClass().getSimpleName() + shellExt);
        shellExec = new File(shellExecUrl.toURI());
        shellExec.setExecutable(true);
    }

    @Test
    public void testHandlersAreNotified() throws Exception {
        Process process = (Process) Runtime.getRuntime().exec(shellExec.getPath());
        ProcessObserver processObserver = new ProcessObserver(process, "test", new NullProgressMonitor());
        MyHandler handler = new MyHandler();
        processObserver.addHandler(handler);
        processObserver.startAndWait();
        Assert.assertEquals(0, process.exitValue());
        Assert.assertEquals("This is some output for stdout.", handler.stdout);
//        Assert.assertEquals("This is some output for stderr.", handler.stderr);
    }

    private static class MyHandler implements ProcessObserver.Handler {
        String stdout;
        String stderr;

        @Override
        public void handleLineOnStdoutRead(String line, Process process, ProgressMonitor progressMonitor) {
            stdout = line;
        }

        @Override
        public void handleLineOnStderrRead(String line, Process process, ProgressMonitor progressMonitor) {
            stderr = line;
        }
    }
}
