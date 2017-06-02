package gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

    OCSSWRemote ocsswRemote = new OCSSWRemote();

    @Test
    public void extractFileInfo() throws Exception {

        ocsswRemote.extractFileInfo("/accounts/aabduraz/Downloads/A2011199230500.L1A_LAC", "1");

    }

}