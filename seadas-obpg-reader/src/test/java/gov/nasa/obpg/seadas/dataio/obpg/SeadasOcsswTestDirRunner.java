package gov.nasa.obpg.seadas.dataio.obpg;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A JUnit {@link org.junit.runner.Runner runner} that will only run annotated tests if
 * <ol>
 * <li>the system property {@code seadas.ocssw.test.dir} is set to a path that points to the OCSSW test data directory, or</li>
 * <li>the environment variable {@code OCSSWROOT} is set and the directory {@code $OCSSWROOT/test} exists.</li>
 * </ol>
 * <p/>
 * The OCSSW source can be changed out from its Subversion repository using
 * <pre>
 *     svn co svn://svn0.domain.sdps/OCSSW/trunk
 * </pre>
 *
 * @author Norman Fomferra
 * @see ObpgProductReaderPlugInTest
 * @since SeaDAS 7.0
 */
@Ignore
public class SeadasOcsswTestDirRunner extends BlockJUnit4ClassRunner {

    public static final String SEADAS_OCSSW_TEST_DIR = "seadas.ocssw.test.dir";
    private static List<File> fileList;
    public static final String OCSSWROOT = "OCSSWROOT";

    /**
     * Constructor.
     *
     * @param testClass The test class.
     * @throws InitializationError If runner cannot be initialized.
     */
    public SeadasOcsswTestDirRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    /**
     * Called from JUnit for every test method in the annotated test class.
     * <p/>
     * Subclasses are responsible for making sure that relevant test events are
     * reported through {@code notifier}
     *
     * @param method   The test method.
     * @param notifier The notifier to be informed.
     */
    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        try {
            final File testDir = getTestDir();
            if (testDir != null) {
                super.runChild(method, notifier);
            } else {
                System.out.printf("Test '%s' is ignored, because system property '%s' is not set.\n",
                                  method.getName(),
                                  SEADAS_OCSSW_TEST_DIR);
                notifier.fireTestIgnored(createDescription(method));
            }
        } catch (IOException e) {
            notifier.fireTestFailure(new Failure(createDescription(method), e));
        }
    }

    /**
     * Gets the valid OCSSW test data directory.
     *
     * @return The OCSSW test data directory, or {@code null}, if the system property {@code seadas.ocssw.test.dir} is not set.
     * @throws IOException If the system property is given, but the path is not valid.
     */
    public static File getTestDir() throws IOException {

        // Try system property: -Dseadas.ocssw.test.dir=...
        final String testDirPath = System.getProperty(SEADAS_OCSSW_TEST_DIR);
        if (testDirPath != null) {
            final File testDir = new File(testDirPath);
            if (testDir.exists()) {
                return testDir;
            }
            // This is an error, because we assume that SEADAS_OCSSW_TEST_DIR has been set by intention.
            throw new IOException(String.format("System property '%s' is set to '%s', but this seems not a valid test data path.\n",
                                                SEADAS_OCSSW_TEST_DIR,
                                                testDirPath));
        }


        // Try value of environment variable: export OCSSWROOT=...
        final String ocsswHome = System.getenv(OCSSWROOT);
        if (ocsswHome != null) {
            final File testDir = new File(ocsswHome, "test");
            if (testDir.exists()) {
                return testDir;
            }
            // This is a warning only, because we can't assume that OCSSWROOT has a "test" directory.
            System.out.printf("Warning: Environment variable '%s' is set to '%s', but it does not contain a directory 'test'.\n",
                              OCSSWROOT,
                              ocsswHome);
        }

        return null;
    }

    /**
     * Gets the list of valid OCSSW test files.
     *
     * @return The list of valid OCSSW test files. It will be empty, if the test data directory is not specified, or there are no test files.
     * @throws IOException If the system property is given, but the path is not valid.
     */
    public static List<File> getFileList() throws IOException {
        if (fileList == null) {
            fileList = readFileList(false);
        }
        return Collections.unmodifiableList(fileList);
    }

    private static List<File> readFileList(boolean failFast) throws IOException {
        final File testDir = getTestDir();
        if (testDir == null) {
            return Collections.emptyList();
        }
        final BufferedReader reader = createFileListReader();
        try {
            final ArrayList<File> fileList = new ArrayList<File>();
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    File file = new File(testDir, line);
                    if (!file.exists()) {
                        String message = "Test data file not found: " + file;
                        if (failFast) {
                            throw new IOException(message);
                        } else {
                            System.err.println(message);
                        }
                    }
                    fileList.add(file);
                }
            }
            return fileList;
        } finally {
            reader.close();
        }
    }

    private static BufferedReader createFileListReader() {
        return new BufferedReader(new InputStreamReader(SeadasOcsswTestDirRunner.class.getResourceAsStream("SeadasOcsswTestDirRunner.fileList.txt")));
    }

    private Description createDescription(FrameworkMethod method) {
        return Description.createTestDescription(getTestClass().getJavaClass(), method.getName());
    }
}
