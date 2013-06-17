/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package gov.nasa.gsfc.seadas.bathymetry.operator;

/**
 * @author Thomas Storm
 */
public class BathymetryMaskClassifierTest {

    private BathymetryMaskClassifier gcClassifier;
    private BathymetryMaskClassifier modisClassifier;
    private BathymetryMaskClassifier fillClassifier;

//    @Before
//    public void setUp() throws Exception {
//        gcClassifier = new BathymetryMaskClassifier(BathymetryMaskClassifier.RESOLUTION_50m, BathymetryMaskClassifier.Mode.SRTM_GC, "50m.zip");
//        modisClassifier = new BathymetryMaskClassifier(BathymetryMaskClassifier.RESOLUTION_50m, BathymetryMaskClassifier.Mode.MODIS, "50m.zip");
//        fillClassifier = new BathymetryMaskClassifier(BathymetryMaskClassifier.RESOLUTION_50m, BathymetryMaskClassifier.Mode.GSHHS, "50m.zip");
//    }
//
//    @Test
//    public void testFill() throws Exception {
//        assertTrue(fillClassifier.isWater(42.908833f, 5.5034647f));
//        assertTrue(fillClassifier.isWater(42.092968f, 4.950571f));
//    }
//
//    @Test
//    public void testGetWatermaskSampleAboveSixtyGC() throws Exception {
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, gcClassifier.getWaterMaskSample(70.860277f, 29.205115f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE, gcClassifier.getWaterMaskSample(70.853971f, 29.210610f));
//
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, gcClassifier.getWaterMaskSample(72.791664f, 105.28333f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE,  gcClassifier.getWaterMaskSample(72.794586f, 105.27786f));
//
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, gcClassifier.getWaterMaskSample(80.19444f, 25.963888f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE,  gcClassifier.getWaterMaskSample(80.14856f, 25.95601f));
//
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, gcClassifier.getWaterMaskSample(80.18703f, 26.04707f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE,  gcClassifier.getWaterMaskSample(80.176834f, 26.054949f));
//    }
//
//    @Test
//    public void testIsWaterCenter() throws Exception {
//
//        assertFalse(gcClassifier.isWater(30.30539f, 111.55285f));
//        assertTrue(gcClassifier.isWater(30.269484f, 111.55418f));
//
//        assertFalse(gcClassifier.isWater(49.68f, 0.581f));
//        assertTrue(gcClassifier.isWater(49.434505f, 0.156014f));
//
//        assertTrue(gcClassifier.isWater(49.33615f, -0.0096f));
//        assertFalse(gcClassifier.isWater(49.32062f, -0.005918f));
//
//        assertFalse(gcClassifier.isWater(46.5f, 0.5f));
//
//        assertTrue(gcClassifier.isWater(5.01f, 0.01f));
//        assertTrue(gcClassifier.isWater(5.95f, 0.93f));
//        assertTrue(gcClassifier.isWater(5.04f, 0.95f));
//
//        assertTrue(gcClassifier.isWater(5.5f, 0.5f));
//        assertFalse(gcClassifier.isWater(5.88f, 0.24f));
//
//        assertTrue(gcClassifier.isWater(43.322360f, 4.157f));
//        assertTrue(gcClassifier.isWater(43.511243f, 3.869841f));
//
//        assertFalse(gcClassifier.isWater(45.981416f, -84.462957f));
//        assertTrue(gcClassifier.isWater(45.967423f, -84.477179f));
//
//        assertTrue(gcClassifier.isWater(53.5f, 5.92f));
//        assertFalse(gcClassifier.isWater(53.458760f, 5.801733f));
//
//        assertTrue(gcClassifier.isWater(-4.347463f, 11.443256f));
//        assertFalse(gcClassifier.isWater(-4.2652f, 11.49324f));
//    }
//
//    @Test
//    @Ignore
//    public void testGetWatermaskSampleBelowSixty() throws Exception {
//
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, modisClassifier.getWaterMaskSample(-62.611664f, -60.20445f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE, modisClassifier.getWaterMaskSample(-62.609562f, -60.204235f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE, modisClassifier.getWaterMaskSample(-62.609562f, -60.20228f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE, modisClassifier.getWaterMaskSample(-62.61169f, -60.202232f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE, modisClassifier.getWaterMaskSample(-62.61392f, -60.202305f));
//
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, modisClassifier.getWaterMaskSample(-62.409748f, -59.565838f));
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, modisClassifier.getWaterMaskSample(-62.412197f, -59.565838f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE,  modisClassifier.getWaterMaskSample(-62.409687f, -59.56362f));
//
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, modisClassifier.getWaterMaskSample(-64.12632f, -56.906746f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE, modisClassifier.getWaterMaskSample(-64.124504f, -56.906593f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE, modisClassifier.getWaterMaskSample(-64.12606f, -56.908825f));
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, modisClassifier.getWaterMaskSample(-64.12833f, -56.911137f));
//    }
//
//    @Test
//    @Ignore
//    public void testGetWatermaskSampleAboveSixtyMODIS() throws Exception {
//
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, modisClassifier.getWaterMaskSample(70.860277f, 29.205115f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE, modisClassifier.getWaterMaskSample(70.853971f, 29.210610f));
//
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, modisClassifier.getWaterMaskSample(72.791664f, 105.28333f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE,  modisClassifier.getWaterMaskSample(72.794586f, 105.27786f));
//
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, modisClassifier.getWaterMaskSample(80.19444f, 25.963888f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE,  modisClassifier.getWaterMaskSample(80.14856f, 25.95601f));
//
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, modisClassifier.getWaterMaskSample(80.18703f, 26.04707f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE,  modisClassifier.getWaterMaskSample(80.176834f, 26.054949f));
//
//        assertEquals(BathymetryMaskClassifier.WATER_VALUE, modisClassifier.getWaterMaskSample(76.36008f, -20.632616f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE, modisClassifier.getWaterMaskSample(76.36008f, -20.63075f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE, modisClassifier.getWaterMaskSample(76.36214f, -20.63075f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE, modisClassifier.getWaterMaskSample(76.362175f, -20.63285f));
//        assertEquals(BathymetryMaskClassifier.LAND_VALUE, modisClassifier.getWaterMaskSample(76.357895f, -20.632656f));
//    }
//
//    @Test
//    public void testGetZipfile() throws Exception {
//        // north-west
//
//        assertEquals("w002n51.img", BathymetryUtils.createImgFileName(51.007f, -1.30f));
//        assertFalse("w001n51.img".equals(BathymetryUtils.createImgFileName(51.007f, -1.30f)));
//
//        assertEquals("w002n48.img", BathymetryUtils.createImgFileName(48.007f, -1.83f));
//        assertFalse("w001n48.img".equals(BathymetryUtils.createImgFileName(48.007f, -1.83f)));
//
//        // north-east
//
//        assertEquals("e000n51.img", BathymetryUtils.createImgFileName(51.007f, 0.30f));
//        assertFalse("e001n51.img".equals(BathymetryUtils.createImgFileName(51.007f, 0.30f)));
//
//        assertEquals("e000n49.img", BathymetryUtils.createImgFileName(49.993961334228516f, 0.006230226717889309f));
//        assertFalse("w001n49.img".equals(BathymetryUtils.createImgFileName(51.007f, 0.30f)));
//
//        assertEquals("e001n51.img", BathymetryUtils.createImgFileName(51.007f, 1.30f));
//        assertFalse("e000n51.img".equals(BathymetryUtils.createImgFileName(51.007f, 1.30f)));
//
//        assertEquals("e000n45.img", BathymetryUtils.createImgFileName(45.001f, 0.005f));
//        assertFalse("w000n45.img".equals(BathymetryUtils.createImgFileName(45.001f, 0.005f)));
//
//        assertEquals("e111n30.img", BathymetryUtils.createImgFileName(30.27f, 111.581f));
//        assertFalse("e111n30.img".equals(BathymetryUtils.createImgFileName(29.01f, 112.01f)));
//
//        // south-west
//
//        assertEquals("w001s01.img", BathymetryUtils.createImgFileName(-0.01f, -0.30f));
//        assertFalse("w000s01.img".equals(BathymetryUtils.createImgFileName(-0.01f, -0.30f)));
//
//        assertEquals("w002s02.img", BathymetryUtils.createImgFileName(-1.01f, -1.30f));
//        assertFalse("w001s01.img".equals(BathymetryUtils.createImgFileName(-1.01f, -1.30f)));
//
//        // south-east
//
//        assertEquals("e000s01.img", BathymetryUtils.createImgFileName(-0.01f, 0.30f));
//        assertFalse("e000s00.img".equals(BathymetryUtils.createImgFileName(-0.01f, 0.30f)));
//
//        assertEquals("e001s01.img", BathymetryUtils.createImgFileName(-0.01f, 1.30f));
//        assertFalse("e001s00.img".equals(BathymetryUtils.createImgFileName(-0.01f, 1.30f)));
//    }
//
//    @Test
//    public void testGetResource() throws Exception {
//        URL resource = getClass().getResource("image.properties");
//        assertNotNull(resource);
//    }
}