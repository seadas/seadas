package gov.nasa.gsfc.seadas.ocsswrest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by aabduraz on 6/12/17.
 */
public class OCSSWFileServicesTest {
    @Before
    public void setUp() throws Exception {
//        OCSSWConfig ocsswConfig = new OCSSWConfig();
//        ocsswConfig.readProperties();
//        OCSSWServerModel.initiliaze();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void downloadFile() throws Exception {

        OCSSWFileServices ocsswFileServices = new OCSSWFileServices();
        ocsswFileServices.downloadFile("e3111428287d67a772eeb58946ae1bee");
    }

}