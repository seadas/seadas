package gov.nasa.gsfc.seadas.ocsswrest.utilities;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 4/27/15
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class OCSSWServerPropertyValuesTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetPropValue() throws Exception {
        OCSSWServerPropertyValues propertyValues = new OCSSWServerPropertyValues();
        String sharedDir = propertyValues.getPropValues("serverSharedDirName");
        System.out.println(sharedDir);
        Assert.assertNotNull(sharedDir);

    }
}
