/*
 *
 * TestGeonav - unit tests for the Geonav class.
 *
 * History:
 * What:                   Who:             When:
 * Original                Matt Elliott     June - July, 2011
 *
 */

package gov.nasa.obpg.seadas.dataio.obpg;

import java.io.*;
import java.util.logging.*;

import org.junit.*;
import static org.junit.Assert.*;

import ucar.nc2.*;

//import org.junit.runner.*;

public class ObpgGeonavTest {
    private static String cwd = System.getProperty("user.dir");
    private static String dataDir = "src/test/resources/data/";
    PrintStream errorFile = null;
    private PrintStream oriErr = System.err;

    @After
    public void after() throws Exception {
        System.setErr(oriErr);
        errorFile.close();
    }
    
    @Before
    public void before() throws Exception {
        errorFile = new PrintStream(new FileOutputStream("obpgGeonavUnitTestErrors.txt"));
        System.setErr(errorFile);
    }

    @Test
    public void testConstants() {
        assertEquals(6378.137, ObpgGeonav.EARTH_RADIUS, 0.0);
        assertEquals(0.003352813, ObpgGeonav.EARTH_FLATTENING_FACTOR, 0.00000001);
        assertEquals(6371.0, ObpgGeonav.EARTH_MEAN_RADIUS, 0.0);
        assertEquals(0.000072912, ObpgGeonav.EARTH_ROTATION_RATE, 0.00000001);
        assertEquals(0.993305615, ObpgGeonav.OMF2, 0.00000001);
    }

    @Test
    public void testCrossProduct() {
        float[] v1 = new float[3];
        v1[0] = 2.0f; v1[1] = 1.0f; v1[2] = 0.0f;
        float[] v2 = new float[3]; 
        v2[0] = 1.0f; v2[1] = 1.0f; v2[2] = 1.0f;
        float[] result = ObpgGeonav.crossProduct(v1, v2);
        assertEquals(1.0f, result[0], 0.0f);
        assertEquals(-2.0f, result[1], 0.0f);
        assertEquals(1.0f, result[2], 0.0f);
    }

    @Ignore
    public void testDoComputations() {
    }

    @Test
    public void testDetermineNumberScanLines() {
        String ncPath = dataDir + "S2007005135838.L1A_GAC";
        try {
            NetcdfFile ncFile = NetcdfFile.open(ncPath);
            int numScanLines = ObpgGeonav.determineNumberScanLines(ncFile);
            ncFile.close();
            assertEquals(3930, numScanLines);
        } catch(IOException ioe) {
            fail("Could not open test file:\n   " + ncPath);
        }
    }

    @Test
    public void testDetermineSeawifsDataType() {
        String gacPath = cwd + "/" + dataDir + "Test.L1A_GAC";
        String hrptPath = dataDir + "Test.L1A_MLAC";
        String lacPath = dataDir + "Test.L1A_LAC";

        try {
            NetcdfFile ncFile = NetcdfFile.open(gacPath);
            ObpgGeonav.DataType seawifsDataType = ObpgGeonav.determineSeawifsDataType(ncFile);
            assertEquals(ObpgGeonav.DataType.GAC, seawifsDataType);
            ncFile.close();
            //Geonav testObj = new Geonav(gacPath);
        } catch(IOException ioe) {
            fail("Could not open test GAC file:\n   " + gacPath);
        }
        try {
            //Geonav testObj = new Geonav(lacPath);
            NetcdfFile ncFile = NetcdfFile.open(lacPath);
            ObpgGeonav.DataType seawifsDataType = ObpgGeonav.determineSeawifsDataType(ncFile);
            ncFile.close();
            assertEquals(ObpgGeonav.DataType.LAC, seawifsDataType);
        } catch(IOException ioe) {
            fail("Could not open test LAC file:\n   " + lacPath);
        }
        try {
            //Geonav testObj = new Geonav(lacPath);
            NetcdfFile ncFile = NetcdfFile.open(hrptPath);
            ObpgGeonav.DataType seawifsDataType = ObpgGeonav.determineSeawifsDataType(ncFile);
            ncFile.close();
            assertEquals(ObpgGeonav.DataType.LAC, seawifsDataType);
        } catch(IOException ioe) {
            fail("Could not open test HRPT file:\n   " + gacPath);
        }
    }

    @Test
    public void testMultiplyMatrices() {
        float[][] m1 = new float[3][3];
        float[][] m2 = new float[3][3];
        m1[0][0] = 0.0f;
        m1[0][1] = 1.0f;
        m1[0][2] = 2.0f;
        m1[1][0] = 1.0f;
        m1[1][1] = 2.0f;
        m1[1][2] = 3.0f;
        m1[2][0] = 3.0f;
        m1[2][1] = 4.0f;
        m1[2][2] = 5.0f;

        m2[0][0] = 1.0f;
        m2[0][1] = 2.0f;
        m2[0][2] = 3.0f;
        m2[1][0] = 1.0f;
        m2[1][1] = 2.0f;
        m2[1][2] = 3.0f;
        m2[2][0] = 1.0f;
        m2[2][1] = 2.0f;
        m2[2][2] = 3.0f;

        float[][] p = ObpgGeonav.multiplyMatrices(m1, m2);

        assertEquals(3.0f, p[0][0], 0.0f);
        assertEquals(6.0f, p[0][1], 0.0f);
        assertEquals(9.0f, p[0][2], 0.0f);
        assertEquals(6.0f, p[1][0], 0.0f);
        assertEquals(12.0f, p[1][1], 0.0f);
        assertEquals(18.0f, p[1][2], 0.0f);
        assertEquals(12.0f, p[2][0], 0.0f);
        assertEquals(24.0f, p[2][1], 0.0f);
        assertEquals(36.0f, p[2][2], 0.0f);
    }

}

//345678901234567890123456789012345678901234567890123456789012345678901234567890
