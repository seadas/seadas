package gov.nasa.gsfc.seadas.dataio;

import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.ProductUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class BowtiePixelGeoCodingTest {

    @Test
    public void testTransferGeoCoding() throws URISyntaxException, IOException {
        Product product = ProductIO.readProduct(new File(getClass().getResource("bowtiepixelgeocoding_test_product.L2_sub").toURI()));
        assertTrue(product.getGeoCoding() instanceof BowtiePixelGeoCoding);

        Product targetProduct = new Product("name", "type", product.getSceneRasterWidth(), product.getSceneRasterHeight());

        assertNull(targetProduct.getGeoCoding());
        ProductUtils.copyGeoCoding(product, targetProduct);

        assertNotNull(targetProduct.getGeoCoding());
        assertTrue(targetProduct.getGeoCoding() instanceof BowtiePixelGeoCoding);
    }
}
