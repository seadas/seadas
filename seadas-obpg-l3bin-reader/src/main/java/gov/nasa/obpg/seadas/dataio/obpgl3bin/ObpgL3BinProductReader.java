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

package gov.nasa.obpg.seadas.dataio.obpgl3bin;

import com.bc.ceres.core.ProgressMonitor;
import gov.nasa.obpg.seadas.dataio.obpg.ObpgUtils;
import org.esa.beam.dataio.merisl3.ISINGrid;
import org.esa.beam.framework.dataio.AbstractProductReader;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.CrsGeoCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Structure;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The <code>MerisL3ProductReader</code> class is an implementation of the <code>roductReader</code> interface
 * exclusively for data products having the standard MERIS Binned Level-3 format.
 *
 * @author Norman Fomferra
 * @version $Revision$ $Date$
 */
public class ObpgL3BinProductReader extends AbstractProductReader {

    public static final String COL_INDEX_BAND_NAME = "bins";
    private ObpgUtils obpgUtils = new ObpgUtils();
    private ObpgL3BinUtils binUtils = new ObpgL3BinUtils();
    private Map<Band, Variable> variableMap;

    private NetcdfFile ncfile;
    private Product product;
    private ISINGrid grid;
    private int sceneRasterWidth;
    private int sceneRasterHeight;
    private RowInfo[] rowInfo;
    private int [] bins;
    private Object data;

    /**
     * Constructs a new MERIS Binned Level-3 product reader.
     *
     * @param readerPlugIn the plug-in which created this reader instance
     */
    public ObpgL3BinProductReader(ObpgL3BinProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * Reads a data product and returns an in-memory representation of it. This method is called by
     * <code>readProductNodes(input, subsetInfo)</code> of the abstract superclass.
     *
     * @throws IllegalArgumentException if <code>input</code> type is not one of the supported input sources.
     * @throws java.io.IOException      if an I/O error occurs
     */
    @Override
    protected Product readProductNodesImpl() throws IOException {
        String path = getInput().toString();
        ncfile = NetcdfFile.open(path);
        try {

            sceneRasterHeight = ncfile.getRootGroup().findGroup("Level-3 Binned Data").findVariable("BinIndex").getShape(0);
            sceneRasterWidth = sceneRasterHeight * 2;

            grid = new ISINGrid(sceneRasterHeight);

            File productFile = new File(path);
            String productName = ncfile.findGlobalAttribute("Product Name").toString();
            List<Attribute> globalAttributes = ncfile.getGlobalAttributes();

            product = new Product(productName, "NASA-OBPG-L3", sceneRasterWidth, sceneRasterHeight, this);
            product.setFileLocation(productFile);
            obpgUtils.addGlobalMetadata(product, globalAttributes);

            final Variable idxVariable = ncfile.getRootGroup().findGroup("Level-3 Binned Data").findVariable("BinList");
            List<Variable> l3ProdVars = ncfile.getVariables();
            variableMap = binUtils.addBands(product, idxVariable, l3ProdVars);

            if (product.getNumBands() == 0) {
                throw new IOException("No bands found.");
            }

            initGeoCoding();

        } catch (IOException e) {
            dispose();
            throw e;
        }

        return product;
    }

    /**
     * The template method which is called by the {@link org.esa.beam.framework.dataio.AbstractProductReader#readBandRasterDataImpl(int, int, int, int, int, int, org.esa.beam.framework.datamodel.Band, int, int, int, int, org.esa.beam.framework.datamodel.ProductData, com.bc.ceres.core.ProgressMonitor)} }
     * method after an optional spatial subset has been applied to the input parameters.
     * <p/>
     * <p>The destination band, buffer and region parameters are exactly the ones passed to the original {@link
     * org.esa.beam.framework.dataio.AbstractProductReader#readBandRasterDataImpl} call. Since the
     * <code>destOffsetX</code> and <code>destOffsetY</code> parameters are already taken into acount in the
     * <code>sourceOffsetX</code> and <code>sourceOffsetY</code> parameters, an implementor of this method is free to
     * ignore them.
     *
     * @param sourceOffsetX the absolute X-offset in source raster co-ordinates
     * @param sourceOffsetY the absolute Y-offset in source raster co-ordinates
     * @param sourceWidth   the width of region providing samples to be decode given in source raster co-ordinates
     * @param sourceHeight  the height of region providing samples to be decode given in source raster co-ordinates
     * @param sourceStepX   the sub-sampling in X direction within the region providing samples to be decode
     * @param sourceStepY   the sub-sampling in Y direction within the region providing samples to be decode
     * @param destBand      the destination band which identifies the data source from which to decode the sample values
     * @param destBuffer    the destination buffer which receives the sample values to be decode
     * @param destOffsetX   the X-offset in the band's raster co-ordinates
     * @param destOffsetY   the Y-offset in the band's raster co-ordinates
     * @param destWidth     the width of region to be decode given in the band's raster co-ordinates
     * @param destHeight    the height of region to be decode given in the band's raster co-ordinates
     * @param pm            a monitor to inform the user about progress
     * @throws java.io.IOException if  an I/O error occurs
     * @see #getSubsetDef
     */
    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight,
                                          int sourceStepX, int sourceStepY, Band destBand, int destOffsetX,
                                          int destOffsetY, int destWidth, int destHeight, ProductData destBuffer,
                                          ProgressMonitor pm) throws IOException {
        if (sourceStepX != 1 || sourceStepY != 1) {
            throw new IOException("Sub-sampling is not supported by this product reader.");
        }

        if (sourceWidth != destWidth || sourceHeight != destHeight) {
            throw new IllegalStateException("sourceWidth != destWidth || sourceHeight != destHeight");
        }

        final Variable idxVariableParent = ncfile.getRootGroup().findGroup("Level-3 Binned Data").findVariable("BinList");
        final Structure idxStructure = (Structure) idxVariableParent;
        final Variable idxVariable = idxStructure.findVariable("bin_num");
        final Variable variable = variableMap.get(destBand);


        pm.beginTask("Reading band '" + destBand.getName() + "'...", sourceHeight);
        try {
            readBand(variable, idxVariable, sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight, destBuffer, pm);
        } catch (Exception e) {
            final ProductIOException exception = new ProductIOException(e.getMessage());
            exception.setStackTrace(e.getStackTrace());
            throw exception;
        }
    }

    private void readBand(Variable variable, Variable idxVariable, int sourceOffsetX, int sourceOffsetY,
                          int sourceWidth, int sourceHeight, ProductData destBuffer, ProgressMonitor pm)
                              throws IOException, InvalidRangeException {


        Object buffer = destBuffer.getElems();

        if (rowInfo == null) {
            rowInfo = createRowInfos();
        }
        if (bins == null) {
            bins = (int[]) idxVariable.read().copyTo1DJavaArray();
        }


        final int height = sceneRasterHeight;
        final int width = sceneRasterWidth;
        final ISINGrid grid = this.grid;
        pm.beginTask("Reading band '" + variable.getShortName() + "'...", sourceHeight);
//        Array bob =    idxVariable.read();
        // loop over lines
        try {
            int[] lineOffsets = new int[1];
            int[] lineLengths = new int[1];
            for (int y = sourceOffsetY; y < sourceOffsetY + sourceHeight; y++) {
                if (pm.isCanceled()) {
                    break;
                }
                final int rowIndex = (height - 1) - y;
                final RowInfo rowInfo = this.rowInfo[rowIndex];
                if (rowInfo != null) {

                    final int lineOffset = rowInfo.offset;
                    final int lineLength = rowInfo.length;

                    lineOffsets[0] = lineOffset;
                    lineLengths[0] = lineLength;
                    final Array bindata = variable.read().section(lineOffsets, lineLengths);
//                    final Object bindata = variable.section() //(lineOffset,lineLength).read().getStorage();
//                    final int [] binidx;

//                    synchronized (ncfile) {
////                        bindata = variable.read().copyTo1DJavaArray();
////                            bindata = variable.read(lineOffsets, lineLengths).getStorage();
////                            bindata = variable.readStructure(lineOffset, lineLength).extractMemberArray(prodMember).getStorage();
//                    }
                    int lineIndex0 = 0;
                    for (int x = sourceOffsetX; x < sourceOffsetX + sourceWidth; x++) {
                        final double lon = x * 360.0 / width;
                        final int binIndex = grid.getBinIndex(rowIndex, lon);
                        int lineIndex = -1;
                        for (int i = lineIndex0; i < lineLength; i++) {
                            if (bins[lineOffset + i] >= binIndex) {
                                if (bins[lineOffset + i] == binIndex) {
                                    lineIndex = i;
                                }
                                lineIndex0 = i;
                                break;
                            }
                        }
                        if (lineIndex >= 0) {
                            final int rasterIndex = sourceWidth * (y - sourceOffsetY) + (x - sourceOffsetX);
                            System.arraycopy(bindata.getStorage(), lineIndex, buffer, rasterIndex, 1);
                        }
                    }
                    pm.worked(1);
                }
            }

        } finally {
            pm.done();
        }
    }


    /**
     * Closes the access to all currently opened resources such as file input streams and all resources of this children
     * directly owned by this reader. Its primary use is to allow the garbage collector to perform a vanilla job.
     * <p/>
     * <p>This method should be called only if it is for sure that this object instance will never be used again. The
     * results of referencing an instance of this class after a call to <code>close()</code> are undefined.
     * <p/>
     * <p>Overrides of this method should always call <code>super.close();</code> after disposing this instance.
     *
     * @throws java.io.IOException if an I/O error occurs
     */
    @Override
    public void close() throws
            IOException {
        super.close();

        if (ncfile != null) {
            ncfile.close();
            ncfile = null;
        }

        product = null;
        grid = null;
        rowInfo = null;
    }

    /////////////////////////////////////////////////////////////////////////
    // private helpers
    /////////////////////////////////////////////////////////////////////////

    private void initGeoCoding() throws IOException {
        float pixelX = 0.0f;
        float pixelY = 0.0f;
        float easting = -180f;
        float northing = +90f;
        float pixelSizeX = 360.0f / sceneRasterWidth;
        float pixelSizeY = 180.0f / sceneRasterHeight;
        try {
            product.setGeoCoding(new CrsGeoCoding(DefaultGeographicCRS.WGS84,
                    sceneRasterWidth, sceneRasterHeight,
                    easting, northing,
                    pixelSizeX, pixelSizeY,
                    pixelX, pixelY));
        } catch (FactoryException e) {
            throw new IOException(e);
        } catch (TransformException e) {
            throw new IOException(e);
        }
    }

    private void dispose() {
        try {
            close();
        } catch (IOException e) {
            // OK
        }
    }

    private RowInfo[] createRowInfos() throws
            IOException {
        final ISINGrid grid = this.grid;
        final RowInfo[] binLines = new RowInfo[sceneRasterHeight];
        final Variable idxVariable = ncfile.getRootGroup().findGroup("Level-3 Binned Data").findVariable("BinList");
        final Structure idxStructure = (Structure) idxVariable;
        final Variable idx = idxStructure.findVariable("bin_num");
        final int[] idxValues;
        synchronized (ncfile) {
            idxValues = (int[]) idx.read().getStorage();
        }
        final Point gridPoint = new Point();
        int lastBinIndex = -1;
        int lastRowIndex = -1;
        int lineOffset = 0;
        int lineLength = 0;
        for (int i = 0; i < idxValues.length; i++) {

            final int binIndex = idxValues[i];
            if (binIndex < lastBinIndex) {
                throw new IOException(
                        "Unrecognized level-3 format. Bins numbers expected to appear in ascending order.");
            }
            lastBinIndex = binIndex;

            grid.getGridPoint(binIndex, gridPoint);
            final int rowIndex = gridPoint.y;

            if (rowIndex != lastRowIndex) {
                if (lineLength > 0) {
                    binLines[lastRowIndex] = new RowInfo(lineOffset, lineLength);
                }
                lineOffset = i;
                lineLength = 0;
            }

            lineLength++;
            lastRowIndex = rowIndex;
        }

        if (lineLength > 0) {
            binLines[lastRowIndex] = new RowInfo(lineOffset, lineLength);
        }

        return binLines;
    }

    private static final class RowInfo {

        // offset of row within file
        final int offset;
        // number of bins per row
        final int length;

        public RowInfo(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }
    }
}
