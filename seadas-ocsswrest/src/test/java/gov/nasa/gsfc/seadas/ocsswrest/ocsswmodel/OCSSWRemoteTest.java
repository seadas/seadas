package gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel;

import gov.nasa.gsfc.seadas.ocsswrest.utilities.ServerSideFileUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import static gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWRemoteImpl.MLP_PROGRAM_NAME;

/**
 * Created by aabduraz on 6/2/17.
 */
public class OCSSWRemoteTest {
    @Test
    public void executeMLP() {
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
        ocsswRemote.executeMLP("65c5712c21bb142bdeaa174920669eff", new File("/accounts/aabduraz/aynur/65c5712c21bb142bdeaa174920669eff/multilevel_processor_parFile.txt"));
    }

    @Test
    public void execute() {
        String parFileLocation = "/accounts/aabduraz/aynur/65c5712c21bb142bdeaa174920669eff/multilevel_processor_parFile.txt";
        String[] commandArray = {MLP_PROGRAM_NAME,  parFileLocation};
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
        final long startTime = System.nanoTime();
        ocsswRemote.execute(ServerSideFileUtilities.concatAll(ocsswRemote.getCommandArrayPrefix(MLP_PROGRAM_NAME), commandArray), new File(parFileLocation).getParent(), "1234");
        final long duration = System.nanoTime() - startTime;
        long start = System.currentTimeMillis();

        System.out.println("execution time is nano seconds: " + duration);
        Date myTime = new Date(duration/1000);

        long end = System.currentTimeMillis();

        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.println("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");

        System.out.println(myTime.getTime());

    }


    @Before
    public void setUp() {
        String configFilePath = "/accounts/aabduraz/TestDir/ocsswrestserver.config";
        OCSSWConfig ocsswConfig = new OCSSWConfig(configFilePath);
        OCSSWServerModel.initiliaze();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void getOfileName() {

    }



    @Test
    public void extractFileInfo() {
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();

        ocsswRemote.extractFileInfo("/accounts/aabduraz/Downloads/A2011199230500.L1A_LAC", "1");

    }

    @Test
    public void executeProgram() {
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
        JsonObject jsonObject = Json.createObjectBuilder().add("file_IFILE", "/accounts/aabduraz/test.dir/A2011199230500.L1A_LAC")
                .add("--output_OFILE","--output=/accounts/aabduraz/test.dir/A2011199230500.GEO")
                .add("--verbose_BOOLEAN","--verbose")
                .build();
        ocsswRemote.executeProgram("725c26a6204e8b37613d66f6ea95e4d9", jsonObject);
    }

}