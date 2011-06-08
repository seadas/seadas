/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nasa.obpg.seadas.dataio.obpgl3smi;

import org.esa.beam.framework.datamodel.CrsGeoCoding;
import org.esa.beam.framework.datamodel.Product;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author melliott
 */
public class ObpgL3smiProductReaderTest {
    @Test
    public void testFileRead() throws Exception {
        ObpgL3smiProductReader reader = new ObpgL3smiProductReader(new ObpgL3smiProductReaderPlugIn());
        File input = new File(getClass().getResource("/A20102442010273.L3m_MO_CHL_chlor_a_4km").toURI());
        Product product = reader.readProductNodes(input, null);
        assertNotNull(product);
        assertNotNull(product.getGeoCoding());
        assertTrue(product.getGeoCoding() instanceof CrsGeoCoding);
    }
}
