package gov.nasa.gsfc.seadas.processing.core.ocssw;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by aabduraz on 5/11/17.
 */
public class OCSSWRemoteTest {
    @Test
    public void setIfileName() throws Exception {
        OCSSWRemoteClient ocsswRemote = new OCSSWRemoteClient();
        boolean testFileUpload = ocsswRemote.uploadIFile("/accounts/aabduraz/Downloads/V2017060154800.L2_SNPP_OC.nc");
        assertEquals(true, testFileUpload);
    }

}