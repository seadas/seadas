package gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by aabduraz on 8/25/17.
 */
public class OCSSWConfigTest {
    @Before
public void setUp() {


}

    @Test
    public void readProperties() {

        String configFilePath = "/accounts/aabduraz/SeaDAS/dev/seadas-7.4/seadas/seadas-ocsswrest/config/ocsswservertest.config";
        OCSSWConfig ocsswConfig = new OCSSWConfig(configFilePath);
        ResourceLoader rl = new FileResourceLoader(configFilePath);
        ocsswConfig.readProperties(rl);

    }

}