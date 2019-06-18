/*
 * Copyright (C) 2015 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.dataio.netcdf.ProfileWriteContext;
import org.esa.beam.dataio.netcdf.metadata.profiles.hdfeos.HdfEosGeocodingPart;
import org.esa.beam.dataio.netcdf.util.NetcdfFileOpener;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.*;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.arraycopy;

/**
 * Created by IntelliJ IDEA.
 * User: seadas
 * Date: 6/11/19
 * Time: 2:23 PM
  */
public class L2DscovrEpicFileReader extends SeadasFileReader {

    L2DscovrEpicFileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {

        int sceneHeight = 0;
        int sceneWidth = 0;

        List<Dimension> dims = ncFile.getDimensions();
        for (Dimension d : dims) {
            if ((d.getShortName().equalsIgnoreCase("HDFEOS_SWATHS_Aerosol_NearUV_Swath_XDim"))) {
                sceneHeight = d.getLength();
            }
            if ((d.getShortName().equalsIgnoreCase("HDFEOS_SWATHS_Aerosol_NearUV_Swath_YDim"))) {
                sceneWidth = d.getLength();
            }
        }

        String fileName = ncFile.getLocation();
        int index = fileName.lastIndexOf("DSCOVR_EPIC_L2_AER");
        String productName = fileName.substring(index);

        mustFlipY = false;
        mustFlipX = false;
        

        SeadasProductReader.ProductType productType = productReader.getProductType();

        Product product = new Product(productName, productType.toString(), sceneWidth, sceneHeight);
        product.setDescription(productName);

        product.setFileLocation(productReader.getInputFile());
        product.setProductReader(productReader);

        addGlobalMetadata(product);
        addInputParamMetadata(product);
        addBandMetadata(product);
        addScientificMetadata(product);

        variableMap = addBands(product, ncFile.getVariables());

        addPixelGeocoding(product);

        return product;
    }


    private void addPixelGeocoding(final Product product) {

        final String longitude = "Longitude";
        final String latitude = "Latitude";
 
        Band latBand = null;
        Band lonBand = null;

        if (product.containsBand(latitude) && product.containsBand(longitude)) {
            latBand = product.getBand(latitude);
            lonBand = product.getBand(longitude);
            latBand.setNoDataValue(-1.2676506002282294E30);
            lonBand.setNoDataValue(-1.2676506002282294E30);
            latBand.setNoDataValueUsed(true);
            lonBand.setNoDataValueUsed(true);
        }

        String validPixelExpression = "Latitude <= 90.0 and Latitude >= -90.0 and !nan(Latitude)";

        if (latBand != null) {
            // Initializing all pixels to NAN
            // Necessary to ensure the pixels outside the disk not get random default geocoding and a clean worldmap
            product.setGeoCoding(GeoCodingFactory.createPixelGeoCoding(latBand, lonBand, "1==2", 5));
            // Applying geocoding to valid pixels
            product.setGeoCoding(GeoCodingFactory.createPixelGeoCoding(latBand, lonBand, validPixelExpression, 5));
        }
    }


    public synchronized void readBandData(Band destBand, int sourceOffsetX, int sourceOffsetY, int sourceWidth,
                                          int sourceHeight, int sourceStepX, int sourceStepY, ProductData destBuffer,
                                          ProgressMonitor pm) throws IOException, InvalidRangeException {
        
        int widthRemainder = destBand.getSceneRasterWidth() - (sourceOffsetX + sourceWidth);

        if (widthRemainder < 0) {
            sourceWidth += widthRemainder;
        }
        
        start[1] = sourceOffsetY;
        start[0] = sourceOffsetX;
        stride[1] = sourceStepY;
        stride[0] = sourceStepX;
        count[1] = sourceHeight;
        count[0] = sourceWidth;

        Object buffer = destBuffer.getElems();
        Variable variable = variableMap.get(destBand);

        pm.beginTask("Reading band '" + variable.getShortName() + "'...", sourceHeight);
        try {
            Section section = new Section(start, count, stride);

            Array array;
            int[] newshape = {sourceHeight, sourceWidth};

            array = variable.read(section);
            array = array.transpose(0,1);


            if (array.getRank() > 2) {
                array = array.reshapeNoCopy(newshape);
            }
            Object storage;

            if (mustFlipX && !mustFlipY) {
                storage = array.flip(1).copyTo1DJavaArray();
            } else if (!mustFlipX && mustFlipY) {
                storage = array.flip(0).copyTo1DJavaArray();
            } else if (mustFlipX && mustFlipY) {
                storage = array.flip(0).flip(1).copyTo1DJavaArray();
            } else {
                storage = array.copyTo1DJavaArray();
            }

            if (widthRemainder < 0) {
                arraycopy(storage, 0, buffer, 0, destBuffer.getNumElems() + widthRemainder);
            } else {
                arraycopy(storage, 0, buffer, 0, destBuffer.getNumElems());

            }
        } finally {
            pm.done();
        }

    }

}