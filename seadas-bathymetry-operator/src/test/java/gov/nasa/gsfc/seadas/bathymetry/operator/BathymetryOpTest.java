package gov.nasa.gsfc.seadas.bathymetry.operator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.dataop.maptransf.Datum;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.graph.GraphException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by knowles on 5/31/17.
 */
public class BathymetryOpTest {


    @Test
    public void testInstantiationWithGPF() throws GraphException {
        GPF.getDefaultInstance().getOperatorSpiRegistry().loadOperatorSpis();

        HashMap<String, Object> parameters = new HashMap<>();

        final Product sp = createTestProduct(100, 100);
        assertNotNull(sp.getGeoCoding());
        parameters.put("resolution", "1855");
        parameters.put("filename", "ETOPO1_ocssw.nc");

        Product tp = GPF.createProduct("BathymetryOp", parameters, sp);
        assertNotNull(tp);
        assertEquals(100, tp.getSceneRasterWidth());
        assertEquals(50, tp.getSceneRasterHeight());
    }


    private Product createTestProduct(int w, int h) {
        Product product = new Product("p", "t", w, h);

        Placemark[] gcps = {
                Placemark.createPointPlacemark(GcpDescriptor.getInstance(), "p1", "p1", "", new PixelPos(0.5f, 0.5f), new GeoPos(10, -10),
                        null),
                Placemark.createPointPlacemark(GcpDescriptor.getInstance(), "p2", "p2", "", new PixelPos(w - 0.5f, 0.5f), new GeoPos(10, 10),
                        null),
                Placemark.createPointPlacemark(GcpDescriptor.getInstance(), "p3", "p3", "", new PixelPos(w - 0.5f, h - 0.5f), new GeoPos(-10, 10),
                        null),
                Placemark.createPointPlacemark(GcpDescriptor.getInstance(), "p4", "p4", "", new PixelPos(0.5f, h - 0.5f), new GeoPos(-10, -10),
                        null),
        };
        product.setGeoCoding(new GcpGeoCoding(GcpGeoCoding.Method.POLYNOMIAL1, gcps, w, h, Datum.WGS_84));

        Band band1 = product.addBand("radiance_1", ProductData.TYPE_INT32);
        int[] intValues = new int[w * h];
        Arrays.fill(intValues, 1);
        band1.setData(ProductData.createInstance(intValues));

        Band band2 = product.addBand("radiance_2", ProductData.TYPE_FLOAT32);
        float[] floatValues = new float[w * h];
        Arrays.fill(floatValues, 2.5f);
        band2.setData(ProductData.createInstance(floatValues));

        Band band3 = product.addBand("radiance_3", ProductData.TYPE_INT16);
        band3.setScalingFactor(0.5);
        short[] shortValues = new short[w * h];
        Arrays.fill(shortValues, (short) 6);
        band3.setData(ProductData.createInstance(shortValues));

        return product;
    }

}