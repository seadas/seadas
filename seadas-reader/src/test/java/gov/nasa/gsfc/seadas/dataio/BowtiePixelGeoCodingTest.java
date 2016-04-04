package gov.nasa.gsfc.seadas.dataio;

import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductSubsetDef;
import org.esa.beam.framework.datamodel.GeoCoding;
import org.esa.beam.framework.datamodel.GeoPos;
import org.esa.beam.framework.datamodel.PixelPos;
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

    @Test
    public void testLatAndLonAreCorrectlySubsetted() throws URISyntaxException, IOException {
        Product product = ProductIO.readProduct(new File(getClass().getResource("bowtiepixelgeocoding_test_product.L2_sub").toURI()));
        GeoCoding sourcceGeoCoding = product.getGeoCoding();
        assertTrue(sourcceGeoCoding instanceof BowtiePixelGeoCoding);

        ProductSubsetDef subsetDef = new ProductSubsetDef();
        subsetDef.setRegion(50, 50, 10, 10);
        subsetDef.addNodeName("chlor_a");
        Product targetProduct = product.createSubset(subsetDef, "subset", "");

        GeoCoding targetGeoCoding = targetProduct.getGeoCoding();
        assertNotNull(targetGeoCoding);
        assertTrue(targetGeoCoding instanceof BowtiePixelGeoCoding);
        assertTrue(targetProduct.containsBand("latitude"));
        assertTrue(targetProduct.containsBand("longitude"));

        PixelPos sourcePixelPos = new PixelPos(50.5f, 50.5f);
        GeoPos expected = sourcceGeoCoding.getGeoPos(sourcePixelPos, new GeoPos());
        PixelPos targetPixelPos = new PixelPos(0.5f, 0.5f);
        GeoPos actual = targetGeoCoding.getGeoPos(targetPixelPos, new GeoPos());
        assertEquals(expected.getLat(), actual.getLat(), 1.0e-6);
        assertEquals(expected.getLon(), actual.getLon(), 1.0e-6);
    }
}
