package gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;

import static org.junit.Assert.*;

/**
 * Created by aabduraz on 6/2/17.
 */
public class OCSSWRemoteTest {


    @Before
    public void setUp() throws Exception {
        OCSSWConfig ocsswConfig = new OCSSWConfig();
        ocsswConfig.readProperties();
        OCSSWServerModel.initiliaze();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getOfileName() throws Exception {

    }



    @Test
    public void extractFileInfo() throws Exception {
        OCSSWRemote ocsswRemote = new OCSSWRemote();

        ocsswRemote.extractFileInfo("/accounts/aabduraz/Downloads/A2011199230500.L1A_LAC", "1");

    }

    @Test
    public void executeProgram() throws Exception {
        OCSSWRemote ocsswRemote = new OCSSWRemote();
        JsonObject jsonObject = Json.createObjectBuilder().add("file_IFILE", "/accounts/aabduraz/test.dir/A2011199230500.L1A_LAC")
                .add("--output_OFILE","--output=/accounts/aabduraz/test.dir/A2011199230500.GEO")
                .add("--verbose_BOOLEAN","--verbose")
                .build();
        ocsswRemote.executeProgram("725c26a6204e8b37613d66f6ea95e4d9", jsonObject);
    }

}