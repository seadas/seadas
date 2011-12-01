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
import java.util.HashMap;
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
        final HashMap<String, String> l2BandInfoMap = getL2BandInfoMap();
        final HashMap<String, String> l2FlagsInfoMap = getL2FlagsInfoMap();
        final BitmaskDef[] defs = getDefaultBitmaskDefs(l2FlagsInfoMap);

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

            addBitmaskDefinitions(product, defs);
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
//todo: think about interpreting QF fields for flag coding

//                    FlagCoding flagCoding = null;
//
//                    if (flagCoding == null) {
//                        flagCoding = new FlagCoding(name);
//                    }
//                    final int flagMask = convertToFlagMask(attribName);
//                    flagCoding.addFlag(flagName, flagMask, flagsInfoMap.get(flagName));
//
//                    if (flagCoding != null) {
//                        band.setSampleCoding(flagCoding);
//                        product.getFlagCodingGroup().add(flagCoding);
//                    }
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

    private void addFlagsAndMasks(Product product) {
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


            product.getMaskGroup().add(Mask.BandMathsType.create("412Qual", "Quality flag of nLw at 412nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.412Qual > 0",
                                                                 Color.YELLOW, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("445Qual", "Quality flag of nLw at 445nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.445Qual > 0",
                                                                 Color.CYAN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("488Qual", "Quality flag of nLw at 488nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.488Qual > 0",
                                                                 Color.LIGHT_GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("555Qual", "Quality flag of nLw at 555nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.555Qual > 0",
                                                                 Color.MAGENTA, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("672Qual", "Quality flag of nLw at 672nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.672Qual > 0",
                                                                 Color.BLUE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("ChlQual", "Quality flag of Chlorophyll a",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.ChlQual > 0",
                                                                 Color.GREEN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP412aQual", "Quality flag of IOP (absorption) at 412nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.IOP412aQual > 0",
                                                                 Color.ORANGE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP412sQual", "Quality flag of IOP (absorption) at 412nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF1_VIIRSOCCEDR.IOP412sQual > 0",
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


            product.getMaskGroup().add(Mask.BandMathsType.create("IOP445aQual", "Quality flag of IOP (absorption) at 445nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP445aQual > 0",
                                                                 Color.YELLOW, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP445sQual", "Quality flag of IOP (scattering) at 445nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP445sQual > 0",
                                                                 Color.CYAN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP488aQual", "Quality flag of IOP (absorption) at 488nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP488aQual > 0",
                                                                 Color.LIGHT_GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP488sQual", "Quality flag of IOP (scattering) at 488nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP488sQual > 0",
                                                                 Color.MAGENTA, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP555aQual", "Quality flag of IOP (absorption) at 555nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP555aQual > 0",
                                                                 Color.BLUE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP555sQual", "Quality flag of IOP (scattering) at 555nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP555sQual > 0",
                                                                 Color.GREEN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP672aQual", "Quality flag of IOP (absorption) at 672nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP672aQual > 0",
                                                                 Color.ORANGE, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("IOP672sQual", "Quality flag of IOP (scattering) at 672nm",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF2_VIIRSOCCEDR.IOP672sQual > 0",
                                                                 Color.PINK, 0.2));
        }
         QFBand = product.getBand("QF3_VIIRSOCCEDR");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("QF3");
            flagCoding.addFlag("SDRQual", 0x01, "Input radiance quality");
            flagCoding.addFlag("O3Qual", 0x02, "Input total Ozone Column quality");
            flagCoding.addFlag("WindSpeed", 0x04, "Wind speed > 8m/s (possible whitecap formation)");
            flagCoding.addFlag("AtmWarm", 0x08, "Epsilon value out-of-range for aerosol models (0.85 > eps > 1.35)");
            flagCoding.addFlag("AtmFail_O3", 0x10, "Atmospheric correction failure - Ozone correction");
            flagCoding.addFlag("AtmFail_WC", 0x20, "Atmospheric correction failure - Whitecap correction");
            flagCoding.addFlag("AtmFail_pol", 0x30, "Atmospheric correction failure - Polarization correction");
            flagCoding.addFlag("AtmFail_rayleigh", 0x40, "Atmospheric correction failure - Rayliegh correction");
            flagCoding.addFlag("AtmFail_aerosol", 0x50, "Atmospheric correction failure - Aerosol correction");
            flagCoding.addFlag("AtmFail_difftran", 0x60, "Atmospheric correction failure - Diffuse transmission zero");
            flagCoding.addFlag("AtmFail_NO", 0x70, "Atmospheric correction failure - no correction possible");



            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);


            product.getMaskGroup().add(Mask.BandMathsType.create("SDRQual", "Input radiance quality",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.SDRQual > 0",
                                                                 Color.YELLOW, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("O3Qual", "Input Ozone quality",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.O3Qual > 0",
                                                                 Color.CYAN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("WindSpeed", "Wind speed > 8m/s",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.WindSpeed > 0",
                                                                 Color.LIGHT_GRAY, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmWarn", "Atmospheric correction warning",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.AtmWarn > 0",
                                                                 Color.MAGENTA, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_O3", "Atmospheric correction failure - Ozone correction",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.AtmFail_O3 > 0",
                                                                 Color.RED, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_WC", "Atmospheric correction failure - Whitecap correction",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.AtmFail_WC > 0",
                                                                 Color.RED, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_pol", "Atmospheric correction failure - Polarization correction",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.AtmFail_pol > 0",
                                                                 Color.RED, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_rayleigh", "Atmospheric correction failure - Rayliegh correction",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.AtmFail_rayleigh > 0",
                                                                 Color.RED, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_aerosol", "Atmospheric correction failure - Aerosol correction",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.AtmFail_aerosol > 0",
                                                                 Color.RED, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_difftran", "Atmospheric correction failure - Diffuse transmission zero",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.AtmFail_difftran > 0",
                                                                 Color.RED, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("AtmFail_NO", "Atmospheric correction failure - no correction possible",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF3_VIIRSOCCEDR.AtmFail_NO > 0",
                                                                 Color.RED, 0.2));
        }
        QFBand = product.getBand("QF4_VIIRSOCCEDR");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("QF4");
            flagCoding.addFlag("Glint", 0x10, "Sun Glint");
            flagCoding.addFlag("Land", 0x03, "Land");
            flagCoding.addFlag("Ice_Snow", 0x04, "Snow or Ice detected");
            flagCoding.addFlag("HighSolZ", 0x08, "Solar Zenith Angle > 70 deg.");
            flagCoding.addFlag("HighSenZ", 0x20, "Senzor Zenith Angle > 53 deg.");
            flagCoding.addFlag("Shallow", 0x40, "Shallow Water");

            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);


            product.getMaskGroup().add(Mask.BandMathsType.create("Glint", "Sun Glint.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR.Glint > 0",
                                                                 Color.YELLOW, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("Land", "Land mask",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR.Land > 0",
                                                                 Color.GREEN, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("Ice/Snow", "Ice/snow mask.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR.Ice_Snow > 0",
                                                                 Color.lightGray, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("HighSolZ", "Solar Zenith angle > 70 deg.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR.HighSolZ > 0",
                                                                 Color.MAGENTA, 0.2));
            product.getMaskGroup().add(Mask.BandMathsType.create("HighSenZ", "Sensor Zenith angle > 53 deg.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDR.HighSenZ > 0",
                                                                 Color.orange, 0.5));
            product.getMaskGroup().add(Mask.BandMathsType.create("ShallowWater", "Shallow Water mask.",
                                                                 product.getSceneRasterWidth(),
                                                                 product.getSceneRasterHeight(), "QF4_VIIRSOCCEDRShallow > 0",
                                                                 Color.BLUE, 0.5));
        }

    }
}
