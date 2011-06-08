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
package gov.nasa.obpg.seadas.dataio.obpg;

import org.esa.beam.framework.dataio.AbstractProductReader;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.util.io.BeamFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class ObpgProductReaderPlugInTest {

    private ObpgProductReaderPlugIn plugIn;

    @Before
    public void before() throws Exception {
        plugIn = new ObpgProductReaderPlugIn();
    }

    @Test
    public void testDefaultFileExtensions() {
        final String[] fileExtensions = plugIn.getDefaultFileExtensions();
        assertNotNull(fileExtensions);
        assertEquals(8, fileExtensions.length);
        HashSet<String> extensionSet = new HashSet<String>(Arrays.asList(fileExtensions));
        assertTrue("Could not find \".hdf\" in file extensions", extensionSet.contains(".hdf"));
        assertTrue("Could not find \".L2\" in file extensions", extensionSet.contains(".L2"));
        assertTrue("Could not find \".L2_LAC\" in file extensions", extensionSet.contains(".L2_LAC"));
        assertTrue("Could not find \".L2_LAC_OC\" in file extensions", extensionSet.contains(".L2_LAC_OC"));
        assertTrue("Could not find \".L2_LAC_SST\" in file extensions", extensionSet.contains(".L2_LAC_SST"));
        assertTrue("Could not find \".L2_LAC_SST4\" in file extensions", extensionSet.contains(".L2_LAC_SST4"));
        assertTrue("Could not find \".L2_MLAC\" in file extensions", extensionSet.contains(".L2_MLAC"));
        assertTrue("Could not find \".L2_MLAC_OC\" in file extensions", extensionSet.contains(".L2_MLAC_OC"));
    }

    @Test
    public void testCreateReaderInstance() {
        final ProductReader productReader = plugIn.createReaderInstance();

        assertNotNull(productReader);
        assertTrue(productReader instanceof AbstractProductReader);
        assertTrue(productReader instanceof ObpgProductReader);
    }

    @Test
    public void testGetFormatNames() {
        final String[] formatNames = plugIn.getFormatNames();

        assertNotNull(formatNames);
        assertEquals(1, formatNames.length);
        assertEquals("NASA-OBPG", formatNames[0]);
    }

    @Test
    public void testGetInputTypes() {
        final Class[] classes = plugIn.getInputTypes();

        assertNotNull(classes);
        assertEquals(2, classes.length);
        assertEquals("java.lang.String", classes[0].getName());
        assertEquals("java.io.File", classes[1].getName());
    }

    @Test
    public void testProductFileFilterExtension() {
        final BeamFileFilter beamFileFilter = plugIn.getProductFileFilter();

        assertNotNull(beamFileFilter);
        final String[] extensions = beamFileFilter.getExtensions();
        assertNotNull(extensions);
        assertArrayEquals(extensions, plugIn.getDefaultFileExtensions());
        assertEquals("NASA-OBPG", beamFileFilter.getFormatName());
    }

}
