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

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.dataio.AbstractProductReader;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.*;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;


// import org.opengis.filter.spatial.Equals;

public class SeadasProductReader extends AbstractProductReader {

    private NetcdfFile ncfile;
    private ProductType productType;
    private SeadasFileReader seadasFileReader;


    enum ProductType {
        Level1A_CZCS("CZCS Level 1A"),
        Level2_CZCS("Level 2"),
        Level1A_Seawifs("SeaWiFS Level 1A"),
        Level1B("Generic Level 1B"),
        Level1B_Modis("MODIS Level 1B"),
        Level2("Level 2"),
        Level3_Bin("Level 3 Binned"),
        SMI("Level 3 Mapped"),
        SeadasMapped("SeaDAS Mapped"),
        VIIRS_EDR("VIIRS EDR");

        private String name;

        private ProductType(String nm) {
            name = nm;
        }

        public String toString() {
            return name;
        }
    }


    /**
     * Constructs a new abstract product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be <code>null</code> for internal reader
     *                     implementations
     */
    protected SeadasProductReader(SeadasProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {

        try {
            Product product;
            final File inFile = getInputFile();
            final String path = inFile.getPath();

            ncfile = NetcdfFile.open(path);
            productType = findProductType();

            switch (productType) {
                case Level2:
                case Level1B:
                case Level1A_CZCS:
                case Level2_CZCS:
                    seadasFileReader = new L2FileReader(this);
                    break;
                case Level1A_Seawifs:
                    seadasFileReader = new L1ASeawifsFileReader(this);
                    break;
                case Level1B_Modis:
                    seadasFileReader = new L1BModisFileReader(this);
                    break;
                case Level3_Bin:
                    seadasFileReader = new L3BinFileReader(this);
                    break;
                case SMI:
                    seadasFileReader = new SMIFileReader(this);
                    break;
                case SeadasMapped:
                    seadasFileReader = new SeadasMappedFileReader(this);
                    break;
                case VIIRS_EDR:
                    seadasFileReader = new ViirsEDRFileReader(this);
                    break;
                default:
                    throw new IOException("Unrecognized product type");
            }

            return seadasFileReader.createProduct();

        } catch (IOException e) {
            throw new ProductIOException(e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        if (getNcfile() != null) {
            getNcfile().close();
        }
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth,
                                          int sourceHeight,
                                          int sourceStepX, int sourceStepY, Band destBand, int destOffsetX,
                                          int destOffsetY, int destWidth, int destHeight,
                                          ProductData destBuffer,
                                          ProgressMonitor pm) throws IOException {


        try {
            seadasFileReader.readBandData(destBand, sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight, destBuffer, pm);
        } catch (Exception e) {
            final ProductIOException exception = new ProductIOException(e.getMessage());
            exception.setStackTrace(e.getStackTrace());
            throw exception;
        }
    }

    public File getInputFile() {
        return ((SeadasProductReaderPlugIn) getReaderPlugIn()).getInputFile(getInput());
    }

    public NetcdfFile getNcfile() {
        return ncfile;
    }

    public ProductType getProductType() {
        return productType;
    }

    public boolean checkSeadasMapped() {
        try {
            List<Variable> seadasMappedVariables = ncfile.getVariables();
            return seadasMappedVariables.get(0).findAttribute("Projection Category").isString();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkModisL1B() {
        Group modisl1bGroup = ncfile.findGroup("MODIS_SWATH_Type_L1B");
        if (modisl1bGroup != null) {
            return true;
        }
        return false;
    }

    public boolean checkViirsEDR() {
        Attribute platformShortName = ncfile.findGlobalAttribute("Platform_Short_Name");
        try {
            if(platformShortName.getStringValue().equals("NPP")) {
                Group dataProduct = ncfile.findGroup("Data_Products");
                if(dataProduct.getGroups().get(0).getShortName().matches("VIIRS.*EDR")) {
                    return true;
                }
            }

        } catch (Exception e) {}
        return false;
    }


    public ProductType findProductType() throws ProductIOException {

        Attribute titleAttr = ncfile.findGlobalAttribute("Title");
        String title = " ";
        if (titleAttr != null) {
            title = titleAttr.getStringValue().trim();
            if (title.equals("CZCS Level-2 Data")) {
                return ProductType.Level2_CZCS;
            } else if (title.contains("Level-1B")) {
                return ProductType.Level1B;
            } else if (title.equals("CZCS Level-1A Data")){
                return ProductType.Level1A_CZCS;
            } else if (title.contains("Level-2")) {
                return ProductType.Level2;
            } else if (title.equals("SeaWiFS Level-1A Data")) {
                return ProductType.Level1A_Seawifs;
            } else if (title.contains("Level-3 Standard Mapped Image")) {
                return ProductType.SMI;
            } else if (title.contains("Level-3 Binned Data")) {
                return ProductType.Level3_Bin;
            }

        } else if (checkModisL1B()) {
            return ProductType.Level1B_Modis;
        } else if (checkViirsEDR()) {
            return ProductType.VIIRS_EDR;
        } else if (checkSeadasMapped()) {
            return ProductType.SeadasMapped;
        }

        throw new ProductIOException("Unrecognized product type");

    }

}
