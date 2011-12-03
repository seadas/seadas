package gov.nasa.obpg.seadas.dataio.obpg;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.*;
import ucar.ma2.Array;
import ucar.nc2.*;
import ucar.nc2.Dimension;

import java.awt.*;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: seadas
 * Date: 11/14/11
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViirsXDRFileReader extends SeadasFileReader {
    private NetcdfFile geofile;

    ViirsXDRFileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {

        try {
            Group allData = ncFile.findGroup("All_Data");
            List<Dimension> dims;
            if (productReader.getProductType() == SeadasProductReader.ProductType.VIIRS_EDR) {
                dims = allData.getGroups().get(0).getVariables().get(0).getDimensions();
            } else {
                dims = allData.getGroups().get(0).getVariables().get(14).getDimensions();
            }

            int sceneHeight = dims.get(0).getLength();
            int sceneWidth = dims.get(1).getLength();

            String productName = productReader.getInputFile().getName();

            mustFlipX = mustFlipY = mustFlipVIIRS();
            SeadasProductReader.ProductType productType = productReader.getProductType();

            Product product = new Product(productName, productType.toString(), sceneWidth, sceneHeight);
            product.setDescription(productName);

            setStartEndTime(product);

            product.setFileLocation(productReader.getInputFile());
            product.setProductReader(productReader);

            addGlobalAttributeVIIRS();
            addGlobalMetadata(product);

            variableMap = addBands(product, ncFile.getVariables());

            addGeocoding(product);

            product.setAutoGrouping("IOP:QF:nLw");
            addFlagsAndMasks(product);
            return product;
        } catch (Exception e) {
            throw new ProductIOException(e.getMessage());
        }
    }

    @Override
    protected Band addNewBand(Product product, Variable variable) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        Band band = null;
        String[] factors = {"Radiance", "Reflectance", "BulkSST", "SkinSST"};
        int variableRank = variable.getRank();
        if (variableRank == 2) {
            final int[] dimensions = variable.getShape();
            final int height = dimensions[0];
            final int width = dimensions[1];
            if (height == sceneRasterHeight && width == sceneRasterWidth) {
                final String name = variable.getShortName();
                final int dataType = getProductDataType(variable);
                band = new Band(name, dataType, width, height);

                product.addBand(band);

                try {
                    String varname = variable.getShortName();

                    for (String v : factors) {
                        if (v.equals(varname)) {
                            String facvar = v + "Factors";
                            Group group = ncFile.getRootGroup().findGroup("All_Data").getGroups().get(0);
                            Variable factor = group.findVariable(facvar);

                            Array slpoff = factor.read();
                            float slope = slpoff.getFloat(0);

                            float intercept = slpoff.getFloat(1);

                            band.setScalingFactor((double) slope);
                            band.setScalingOffset((double) intercept);
                        }
                    }
                    band.setNoDataValue((double) variable.findAttribute("_FillValue").getNumericValue().floatValue());
                } catch (Exception e) {

                }
            }
        }
        return band;
    }

    public void addGeocoding(final Product product) throws ProductIOException {
        try {
            //todo: refine logic to get correct navGroup
            File inputFile = productReader.getInputFile();
            String navGroup = "All_Data/VIIRS-MOD-GEO-TC_All";
            String geoFileName = getStringAttribute("N_GEO_Ref");
            String path = inputFile.getParent();
            File geocheck = new File(path, geoFileName);
            if (!geocheck.exists()) {
                geoFileName = geoFileName.replaceFirst("GMODO", "GMTCO");
                geocheck = new File(path, geoFileName);
                if (!geocheck.exists()) {
                    if (!inputFile.getName().matches("_c\\d{20}_")) {
                        geoFileName = geoFileName.replaceFirst("_c\\d{20}_", "_");
                    }
                    geocheck = new File(path, geoFileName);
                    if (!geocheck.exists()) {
                        return;
                    }
                }
            }

            geofile = NetcdfFile.open(geocheck.getPath());

            final String longitude = "Longitude";
            final String latitude = "Latitude";

            Band latBand = new Band("latitude", ProductData.TYPE_FLOAT32, product.getSceneRasterWidth(), product.getSceneRasterHeight());
            Band lonBand = new Band("longitude", ProductData.TYPE_FLOAT32, product.getSceneRasterWidth(), product.getSceneRasterHeight());
            product.addBand(latBand);
            product.addBand(lonBand);


            Array latarr = geofile.findVariable(navGroup + "/" + latitude).read();

            Array lonarr = geofile.findVariable(navGroup + "/" + longitude).read();
            float[] latitudes;
            float[] longitudes;
            if (mustFlipX && mustFlipY) {
                latitudes = (float[]) latarr.flip(0).flip(1).copyTo1DJavaArray();
                longitudes = (float[]) lonarr.flip(0).flip(1).copyTo1DJavaArray();
            } else {
                latitudes = (float[]) latarr.getStorage();
                longitudes = (float[]) lonarr.getStorage();
            }

            ProductData lats = ProductData.createInstance(latitudes);
            latBand.setData(lats);
            ProductData lons = ProductData.createInstance(longitudes);
            lonBand.setData(lons);
            product.setGeoCoding(new PixelGeoCoding(latBand, lonBand, null, 5, ProgressMonitor.NULL));

        } catch (Exception e) {
            throw new ProductIOException(e.getMessage());
        }
    }

    public boolean mustFlipVIIRS() throws ProductIOException {
        List<Variable> vars = ncFile.getVariables();
        for (Variable var : vars) {
            if (var.getShortName().contains("DR_Gran_")) {
                List<Attribute> attrs = var.getAttributes();
                for (Attribute attr : attrs) {
                    if (attr.getName().equals("Ascending/Descending_Indicator")) {
                        if (attr.getNumericValue().longValue() == 0) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        throw new ProductIOException("Cannot find Ascending/Decending_Indicator");
    }


    private void setStartEndTime(Product product) throws ProductIOException {
        List<Variable> dataProductList = ncFile.getRootGroup().findGroup("Data_Products").getGroups().get(0).getVariables();
        for (Variable var : dataProductList) {
            if (var.getShortName().contains("DR_Aggr")) {
                String startDate = var.findAttribute("AggregateBeginningDate").getStringValue().trim();
                String startTime = var.findAttribute("AggregateBeginningTime").getStringValue().trim();
                String endDate = var.findAttribute("AggregateEndingDate").getStringValue().trim();
                String endTime = var.findAttribute("AggregateEndingTime").getStringValue().trim();

                final DateFormat dateFormat = ProductData.UTC.createDateFormat("yyyyMMddHHmmss");
                try {
                    String startTimeString = startDate + startTime.substring(0, 6);
                    String endTimeString = endDate + endTime.substring(0, 6);
                    final Date startdate = dateFormat.parse(startTimeString);
                    String startmicroSeconds = startTime.substring(startTimeString.length() - 7, startTimeString.length() - 1);

                    final Date enddate = dateFormat.parse(endTimeString);
                    String endmicroSeconds = endTime.substring(endTimeString.length() - 7, startTimeString.length() - 1);

                    if (mustFlipY) {
                        product.setStartTime(ProductData.UTC.create(enddate, Long.parseLong(endmicroSeconds)));
                        product.setEndTime(ProductData.UTC.create(startdate, Long.parseLong(startmicroSeconds)));
                    } else {
                        product.setStartTime(ProductData.UTC.create(startdate, Long.parseLong(startmicroSeconds)));
                        product.setEndTime(ProductData.UTC.create(enddate, Long.parseLong(endmicroSeconds)));
                    }

                } catch (ParseException e) {
                    throw new ProductIOException("Unable to parse start/end time attributes");
                }

            }
        }
    }

    public void addGlobalAttributeVIIRS() {
        List<Group> DataProductGroups = ncFile.getRootGroup().findGroup("Data_Products").getGroups();

        for (Group dpgroup : DataProductGroups) {
            String groupname = dpgroup.getShortName();
            if (groupname.matches("VIIRS-.*DR$")) {
                List<Variable> vars = dpgroup.getVariables();
                for (Variable var : vars) {
                    String varname = var.getShortName();
                    if (varname.matches(".*_(Aggr|Gran_0)$")) {
                        List<Attribute> attrs = var.getAttributes();
                        for (Attribute attr : attrs) {
                            globalAttributes.add(attr);

                        }
                    }

                }
            }

        }
    }

    private boolean isSpectralBand(Band band) {
        return band.getName().equals("B0") || band.getName().equals("B2") || band.getName().equals(
                "B3") || band.getName().equals("MIR");
    }

    @Override
    protected void addFlagsAndMasks(Product product) {
        //todo: modify colors - use some of the new definitions in SeadasFileReader :)
        Band QFBand = product.getBand("QF1_VIIRSOCCEDR");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("QF1");
            flagCoding.addFlag("412Qual", 0x01, "412nm OC quality");
            flagCoding.addFlag("445Qual", 0x02, "445nm OC quality");
            flagCoding.addFlag("488Qual", 0x04, "488nm OC quality");
            flagCoding.addFlag("555Qual", 0x08, "555nm OC quality");
            flagCoding.addFlag("672Qual", 0x10, "672nm OC quality");
            flagCoding.addFlag("ChlQual", 0x20, "Chlorophyll a quality");
            flagCoding.addFlag("IOP412aQual", 0x40, "IOP (a) 412nm quality");
            flagCoding.addFlag("IOP412sQual", 0x80, "IOP (s) 412nm quality");

            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);


            product.getMaskGroup().add(Mask.BandMathsType.create("412Qual", "Quality flag (poor): nLw at 412nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.412Qual ",
                                                                 Color.YELLOW, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("445Qual", "Quality flag (poor): nLw at 445nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.445Qual ",
                                                                 Color.CYAN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("488Qual", "Quality flag (poor): nLw at 488nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.488Qual ",
                                                                 Color.LIGHT_GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("555Qual", "Quality flag (poor): nLw at 555nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.555Qual ",
                                                                 Color.MAGENTA, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("672Qual", "Quality flag (poor): nLw at 672nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.672Qual ",
                                                                 Color.BLUE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("ChlQual", "Quality flag (poor): Chlorophyll a",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.ChlQual ",
                                                                 Color.GREEN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP412aQual", "Quality flag (poor): IOP (absorption) at 412nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.IOP412aQual ",
                                                                 Color.ORANGE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP412sQual", "Quality flag (poor): IOP (absorption) at 412nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.IOP412sQual ",
                                                                 Color.PINK, 0.2));

        }
        QFBand = product.getBand("QF2_VIIRSOCCEDR");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("QF2");
            flagCoding.addFlag("IOP445aQual", 0x01, "IOP (a) 445nm quality");
            flagCoding.addFlag("IOP445sQual", 0x02, "IOP (s) 445nm quality");
            flagCoding.addFlag("IOP488aQual", 0x04, "IOP (a) 488nm quality");
            flagCoding.addFlag("IOP488sQual", 0x08, "IOP (s) 488nm quality");
            flagCoding.addFlag("IOP555aQual", 0x10, "IOP (a) 555nm quality");
            flagCoding.addFlag("IOP555sQual", 0x20, "IOP (s) 555nm quality");
            flagCoding.addFlag("IOP672aQual", 0x40, "IOP (a) 672nm quality");
            flagCoding.addFlag("IOP672sQual", 0x80, "IOP (s) 672nm quality");
            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);


            product.getMaskGroup().add(Mask.BandMathsType.create("IOP445aQual", "Quality flag (poor): IOP (absorption) at 445nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP445aQual ",
                                                                 Color.YELLOW, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP445sQual", "Quality flag (poor): IOP (scattering) at 445nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP445sQual ",
                                                                 Color.CYAN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP488aQual", "Quality flag (poor): IOP (absorption) at 488nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP488aQual ",
                                                                 Color.LIGHT_GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP488sQual", "Quality flag (poor): IOP (scattering) at 488nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP488sQual ",
                                                                 Color.MAGENTA, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP555aQual", "Quality flag (poor): IOP (absorption) at 555nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP555aQual ",
                                                                 Color.BLUE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP555sQual", "Quality flag (poor): IOP (scattering) at 555nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP555sQual ",
                                                                 Color.GREEN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP672aQual", "Quality flag (poor): IOP (absorption) at 672nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP672aQual ",
                                                                 Color.ORANGE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP672sQual", "Quality flag (poor): IOP (scattering) at 672nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP672sQual ",
                                                                 Color.PINK, 0.2));
        }
         QFBand = product.getBand("QF3_VIIRSOCCEDR");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("QF3");
            flagCoding.addFlag("SDRQual", 0x01, "Input radiance quality");
            flagCoding.addFlag("O3Qual", 0x02, "Input total Ozone Column quality");
            flagCoding.addFlag("WindSpeed", 0x04, "Wind speed > 8m/s (possible whitecap formation)");
            flagCoding.addFlag("AtmWarn", 0x08, "Epsilon value out-of-range for aerosol models (0.85 > eps > 1.35)");

            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);


            product.getMaskGroup().add(Mask.BandMathsType.create("SDRQual", "Input radiance quality (poor)",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.SDRQual",
                                                                 Color.YELLOW, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("O3Qual", "Input Ozone quality (poor)",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.O3Qual",
                                                                 Color.CYAN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("WindSpeed", "Wind speed > 8m/s",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.WindSpeed",
                                                                 Color.LIGHT_GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmWarn", "Atmospheric correction warning",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.AtmWarn",
                                                                 Color.MAGENTA, 0.25));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_O3", "Atmospheric correction failure - Ozone correction",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR & 0x70 ==  0x10",
                                                                 Color.RED, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_WC", "Atmospheric correction failure - Whitecap correction",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR & 0x70 ==  0x20",
                                                                 Color.RED, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_pol", "Atmospheric correction failure - Polarization correction",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR & 0x70 ==  0x30",
                                                                 Color.RED, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_rayleigh", "Atmospheric correction failure - Rayliegh correction",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR & 0x70 ==  0x40",
                                                                 Color.RED, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_aerosol", "Atmospheric correction failure - Aerosol correction",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR & 0x70 ==  0x50",
                                                                 Color.RED, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_difftran", "Atmospheric correction failure - Diffuse transmission zero",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR. & 0x70 ==  0x60",
                                                                 Color.RED, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_NO", "Atmospheric correction failure - no correction possible",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR & 0x70 ==  0x70",
                                                                 Color.RED, 0.0));
        }

        QFBand = product.getBand("QF4_VIIRSOCCEDR");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("QF4");

            flagCoding.addFlag("Ice_Snow", 0x04, "Snow or Ice detected");
            flagCoding.addFlag("HighSolZ", 0x08, "Solar Zenith Angle > 70 deg.");
            flagCoding.addFlag("Glint", 0x10, "Sun Glint");
            flagCoding.addFlag("HighSenZ", 0x20, "Senzor Zenith Angle > 53 deg.");
            flagCoding.addFlag("Shallow", 0x40, "Shallow Water");

            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);



            product.getMaskGroup().add(Mask.BandMathsType.create("Ocean", "Ocean",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR & 0x03 == 0x00",
                                                                 Color.BLUE, 0.7));
            product.getMaskGroup().add(Mask.BandMathsType.create("CoastalWater", "Coastal Water mask",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR & 0x03 == 0x01",
                                                                 Color.GRAY, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("InlandWater", "Inland water mask",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR & 0x03 == 0x02",
                                                                 Color.DARK_GRAY, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("Land", "Land mask",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR & 0x03 == 0x03",
                                                                 Color.GREEN, 0.0));
            product.getMaskGroup().add(Mask.BandMathsType.create("Ice/Snow", "Ice/snow mask.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR.Ice_Snow",
                                                                 Color.lightGray, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("HighSolZ", "Solar Zenith angle > 70 deg.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR.HighSolZ",
                                                                 Color.MAGENTA, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("Glint", "Sun Glint.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR.Glint",
                                                                 Color.YELLOW, 0.1));
            product.getMaskGroup().add(Mask.BandMathsType.create("HighSenZ", "Sensor Zenith angle > 53 deg.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR.HighSenZ",
                                                                 Color.orange, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("ShallowWater", "Shallow Water mask.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR.Shallow",
                                                                 Color.BLUE, 0.5));
        }
        QFBand = product.getBand("QF5_VIIRSOCCEDR");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("QF5");
            flagCoding.addFlag("Straylight", 0x04, "Adjacent pixel not clear, possible straylight contaminated");
            flagCoding.addFlag("Cirrus", 0x08, "Thin Cirrus cloud detected");
            flagCoding.addFlag("Shadow", 0x10, "Cloud shadow detected");
            flagCoding.addFlag("HighAer", 0x20, "Non-cloud obstruction (heavy aerosol load) detected");
            flagCoding.addFlag("AbsAer", 0x40, "Strongly absorbing aerosol detected");
            flagCoding.addFlag("HighAOT", 0x80, "Aerosol optical thickness @ 555nm > 0.3");

            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);


            product.getMaskGroup().add(Mask.BandMathsType.create("Clear", "Confidently Cloud-free.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF5_VIIRSOCCEDR & 0x03 == 0x00",
                                                                 Color.YELLOW, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("LikelyClear", "Probably cloud-free",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR & 0x03 == 0x01",
                                                                 Color.LIGHT_GRAY, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("LikelyCloud", "Probably cloud contaminated.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF5_VIIRSOCCEDR & 0x03 == 0x02",
                                                                 Color.DARK_GRAY, 0.25));
            product.getMaskGroup().add(Mask.BandMathsType.create("Cloud", "Confidently Cloudy.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF5_VIIRSOCCEDR & 0x03 == 0x03",
                                                                 Color.WHITE, 0.0));
            product.getMaskGroup().add(Mask.BandMathsType.create("Straylight", "Adjacent pixel not clear, possible straylight contaminated.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF5_VIIRSOCCEDR.Straylight",
                                                                 Color.ORANGE, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("Cirrus", "Thin Cirrus cloud detected.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF5_VIIRSOCCEDR.Cirrus",
                                                                 Color.BLUE, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("HighAer", "Non-cloud obstruction (heavy aerosol load) detected.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF5_VIIRSOCCEDR.HighAer",
                                                                 Color.MAGENTA, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("AbsAer", "Strongly absorbing aerosol detected.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF5_VIIRSOCCEDR.AbsAer",
                                                                 Color.RED, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("HighAOT", "Aerosol optical thickness @ 555nm > 0.3.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF5_VIIRSOCCEDR.HighAOT",
                                                                 Color.MAGENTA, 0.5));
        }
        QFBand = product.getBand("QF6_VIIRSOCCEDR");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("QF6");
            flagCoding.addFlag("Turbid", 0x01, "Turbid water detected (Rrs @ 555nm > 0.012)");
            flagCoding.addFlag("Coccolithophore", 0x02, "Coccolithophores detected");
            flagCoding.addFlag("HighCDOM", 0x04, "CDOM absorption @ 410nm > 2 m^-1");

            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);


            product.getMaskGroup().add(Mask.BandMathsType.create("Turbid", "Turbid water detected (Rrs @ 555nm > 0.012)",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF6_VIIRSOCCEDR.Turbid ",
                                                                 Color.YELLOW, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("Coccolithophore", "Coccolithophores detected",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF5_VIIRSOCCEDR.Coccolithophore ",
                                                                 Color.CYAN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("HighCDOM", "CDOM absorption @ 410nm > 2 m^-1.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF6_VIIRSOCCEDR.HighCDOM ",
                                                                 Color.GREEN, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("ChlFail", "No Chlorophyll retrieval possible.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF6_VIIRSOCCEDR & 0x18 == 0x00",
                                                                 Color.RED, 0.0));
            product.getMaskGroup().add(Mask.BandMathsType.create("LowChl", "Chlorophyll < 1 mg m^-3",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF6_VIIRSOCCEDR & 0x18 == 0x08",
                                                                 Color.ORANGE, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("ModChl", "Chlorophyll between 1 and 10 mg m^-3",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF6_VIIRSOCCEDR  & 0x18 == 0x10",
                                                                 Color.MAGENTA, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("HighChl", "Chlorphyll > 10 mg m^-3",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF6_VIIRSOCCEDR   & 0x18 == 0x10",
                                                                 Color.RED, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("CarderEmp", "Carder Empirical algorithm used.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF6_VIIRSOCCEDR & 0xE0 == 0x20",
                                                                 Color.BLUE, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("UnpackPig", "Phytoplankton with packaged pigment",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF6_VIIRSOCCEDR & 0xE0 == 0x40",
                                                                 Color.ORANGE, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("WtPigGlobal", "Weighted packaged pigment - global",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF6_VIIRSOCCEDR & 0xE0 == 0x80",
                                                                 Color.GRAY, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("WtPigFull", "Weighted fully packaged pigment",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF6_VIIRSOCCEDR & 0xE0 == 0xA0",
                                                                 Color.LIGHT_GRAY, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("FullPackPig", "Phytoplankton with fully packaged pigment",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF6_VIIRSOCCEDR & 0xE0 == 0xC0",
                                                                 Color.DARK_GRAY, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("NoOCC", "No ocean color chlorphyll retrieval",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF6_VIIRSOCCEDR & 0xE0 == 0xE0",
                                                                 Color.BLACK, 0.1));
        }
        QFBand = product.getBand("QF7_VIIRSOCCEDR");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("QF7");
            flagCoding.addFlag("nLwWarn", 0x01, "nLw out-of-range (< 0.1 or > 40 W m^-2 um^-1 sr^-1)");
            flagCoding.addFlag("ChlWarn", 0x02, "Chlorophyll out-of-range (< 0.05 or > 50 mg m^-3)");
            flagCoding.addFlag("IOPaWarn", 0x04, "IOP absorption out-of-range (< 0.01 or  > 10 m^-1)");
            flagCoding.addFlag("IOPsWarn", 0x08, "IOP scattering out-of-range (< 0.01 or  > 50 m^-1)");
            flagCoding.addFlag("SSTWarn", 0x10, "Input Skin SST poor quality");
            flagCoding.addFlag("Bright", 0x20, "Bright Target flag");


            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);



            product.getMaskGroup().add(Mask.BandMathsType.create("nLwWarn", "nLw out-of-range (< 0.1 or > 40 W m^-2 um^-1 sr^-1)",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF7_VIIRSOCCEDR.nLwWarn",
                                                                 Color.BLUE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("ChlWarn", "Chlorophyll out-of-range (< 0.05 or > 50 mg m^-3)",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF7_VIIRSOCCEDR.ChlWarn",
                                                                 Color.GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOPaWarn", "IOP absorption out-of-range (< 0.01 or  > 10 m^-1)",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF7_VIIRSOCCEDR.IOPaWarn",
                                                                 Color.DARK_GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOPsWarn", "IOP scattering out-of-range (< 0.01 or  > 50 m^-1)",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF7_VIIRSOCCEDR.IOPsWarn",
                                                                 Color.GREEN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("SSTWarn", "Input Skin SST poor quality.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF7_VIIRSOCCEDR.SSTWarn",
                                                                 Color.LIGHT_GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("Bright", "Bright Target flag",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF7_VIIRSOCCEDR.Bright",
                                                                 Color.YELLOW, 0.2));

        }
        QFBand = product.getBand("QF1_VIIRSMBANDSDR");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("QF1SDR");
            flagCoding.addFlag("CalQualGood", 0x00, "Calibration quality - Good");
            flagCoding.addFlag("CalQualBad", 0x01, "Calibration quality - Bad");
            flagCoding.addFlag("NoCal", 0x02, "No Calibration");
            flagCoding.addFlag("NoSatPix", 0x03, "No saturated");
            flagCoding.addFlag("LowSatPix", 0x03, "Some pixels saturated");
            flagCoding.addFlag("SatPix", 0x03, "All pixels saturated");
            flagCoding.addFlag("DataOK", 0x04, "All required data available");
            flagCoding.addFlag("BadEvRDR", 0x08, "Missing EV RDR data.");
            flagCoding.addFlag("BadCalData", 0x10, "Missing cal data (SV, CV, SD, etc)");
            flagCoding.addFlag("BadTherm", 0x20, "Missing Thermistor data");
            flagCoding.addFlag("InRange", 0x40, "All calibrated data within LUT thresholds");
            flagCoding.addFlag("BadRad", 0x40, "Radiance out-of-range LUT threshold");
            flagCoding.addFlag("BadRef", 0x40, "Reflectance out-of-range LUT threshold");
            flagCoding.addFlag("BadRadRef", 0x40, "Both Radiance & Reflectance out-of-range LUT threshold");


            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);



            product.getMaskGroup().add(Mask.BandMathsType.create("CalQualGood", "Calibration quality - Good",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0x02 == 0x00",
                                                                 Color.BLUE, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("CalQualBad", "Calibration quality - Bad",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0x02 == 0x01",
                                                                 Color.GRAY, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("NoCal", "No Calibration",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0x02 == 0x02",
                                                                 Color.DARK_GRAY, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("NoSatPix", "No saturated",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0x0C == 0x00",
                                                                 Color.GREEN, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("LowSatPix", "Some pixels saturated.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0x0C == 0x04",
                                                                 Color.lightGray, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("SatPix", "All pixels saturated",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0x0C == 0x08",
                                                                 Color.MAGENTA, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("DataOK", "All required data available",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0x30 == 0x00",
                                                                 Color.YELLOW, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("BadEvRDR", "Missing EV RDR data.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0x30 == 0x10",
                                                                 Color.orange, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("BadCalData", "Missing cal data (SV, CV, SD, etc).",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0x30 == 0x20",
                                                                 Color.BLUE, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("BadTherm", "Missing Thermistor data.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0x30 == 0x30",
                                                                 Color.BLUE, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("InRange", "All calibrated data within LUT thresholds.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0xC0 == 0x00",
                                                                 Color.BLUE, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("BadRad", "Radiance out-of-range LUT threshold.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0xC0 == 0x40",
                                                                 Color.BLUE, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("BadRef", "Reflectance out-of-range LUT threshold.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0xC0 == 0x80",
                                                                 Color.BLUE, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("BadRadRef", "Both Radiance & Reflectance out-of-range LUT threshold.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSMBANDSDR & 0xC0 == 0xC0",
                                                                 Color.BLUE, 0.5));
        }
    }
}
