package gov.nasa.gsfc.seadas.writeimage;

import org.esa.beam.framework.gpf.annotations.OperatorMetadata;


import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.media.jai.JAI;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.ColorPaletteDef;
import org.esa.beam.framework.datamodel.ImageInfo;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.jai.ImageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bc.ceres.core.ProgressMonitor;

/**
 * @author kutila
 * @author aynur abdurazik
 */
@OperatorMetadata(alias = "WriteImage", version = "0.4", copyright = "(c) 2014 University of Queensland", authors = "Kutila G",
        description = "Creates a color image from a single source band.")
public class WriteImageOp extends Operator {
    /**
     * Service Provider Interface
     */
    public static class Spi extends OperatorSpi {
        public Spi() {
            super(WriteImageOp.class);
        }
    }

    private static final String LOGARITHMIC = "log";


    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @SourceProduct(alias = "source", description = "Primary source input from which an RGB image is to be generated.")
    private Product sourceProduct;

    @TargetProduct
    private Product targetProduct;

    @Parameter(description = "Name of band containing data. If not provided, first band in the source product is used.")
    private String sourceBandName;

    @Parameter(description = "The file to which the image is written.")
    private String filePath;

    @Parameter(description = "Output image format", defaultValue = "png")
    private String formatName;

    @Parameter(description = "Color palette definition file", defaultValue = "nofile.cpd")
    private String cpdFilePath;

    @Parameter(description = "Auto distribute points between min/max", defaultValue = "false")
    private boolean cpdAutoDistribute;

    @Parameter(description = "Minimum value of colour scale. Used only if colour palette definition not present.", defaultValue = "0.01")
    private double colourScaleMin;

    @Parameter(description = "Maximum value of colour scale. Used only if colour palette definition not present.", defaultValue = "1.0")
    private double colourScaleMax;

    @Parameter(description = "Output image reduction factor.", defaultValue = "0")
    private int level;

    /**
     * From API docs:
     * <p>
     * The left (at index 0) and right (at index 1) normalized areas of the raster data histogram to
     * be excluded when determining the value range for a linear constrast stretching. Can be null,
     * in this case {0.01, 0.04} resp. 5% of the entire area is skipped.
     */
    private double[] histoSkipRatios = {0.01, 0.04};

    @Parameter(description = "The scale type to apply. Can be 'linear' or 'log'.", defaultValue = "linear")
    private String scaleType;

    private Band sourceBand;

    private ProgressMonitor pm = ProgressMonitor.NULL;

    @Override
    public void initialize() throws OperatorException {
        if (this.sourceProduct == null) {
            throw new OperatorException("Source product is NULL");
        }

        if (this.sourceBandName != null && !this.sourceProduct.containsBand(sourceBandName)) {
            throw new OperatorException("Source does not contain band: " + sourceBandName);
        }


        // - print warning if cpd file is missing. we proceed with default mapping
        final File f = new File(this.cpdFilePath);
        if (f == null || !f.exists()) {
            System.out.println("WARNING: Color palette definition could not be read. Will proceed with default colors.");
            this.log.warn("WARNING: Color palette definition could not be read. Will proceed with default colors.");

            // - colour scale min/max are only used if there is cpd file
            if (!(this.colourScaleMin < this.colourScaleMax)) {
                throw new OperatorException("Error in data range: min should be less than max");
            }
        }

        if (sourceBandName != null) {
            this.sourceBand = this.sourceProduct.getBand(sourceBandName);
        } else {
            this.sourceBand = this.sourceProduct.getBandAt(0);
            this.sourceBandName = this.sourceBand.getName();
        }
        final int sceneRasterWidth = this.sourceProduct.getSceneRasterWidth();
        final int sceneRasterHeight = this.sourceProduct.getSceneRasterHeight();

        // note: need to create one even though it isn't used by this operator
        this.targetProduct = new Product("RGB", "RGB", sceneRasterWidth, sceneRasterHeight);

        final Band myTargetBand = new Band("NullBand", ProductData.TYPE_FLOAT64, sceneRasterWidth, sceneRasterHeight);
        this.targetProduct.addBand(myTargetBand);

        // Important: ensures computeTile() is called only once
        this.targetProduct.setPreferredTileSize(sceneRasterWidth, sceneRasterHeight);

        this.log.info("=== initalized === [{}: {}]", sourceProduct.getName(), sourceBandName);
    }

    @Override
    public void computeTile(final Band targetBand, final Tile targetTile, final ProgressMonitor pm)
            throws OperatorException {
        final Rectangle rectangle = targetTile.getRectangle();
        this.log.debug(" WriteImage.computeTile({}, {}) ", rectangle.getX(), rectangle.getY());

        try {
            this.writeImage();
        } catch (final Exception e) {
            //FIXME: decide whether to throw this upwards
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        this.log.debug("WriteRGBImage.dispose() invoked");
        super.dispose();
    }

    /**
     * Generates and writes an image file from the input Product.
     *
     * @throws Exception
     */
    protected void writeImage() throws Exception {

        Band band = this.sourceBand;
        final ImageInfo defaultImageInfo = band.createDefaultImageInfo(histoSkipRatios, this.pm);
        band.setImageInfo(defaultImageInfo);

        final boolean isLog = LOGARITHMIC.equalsIgnoreCase(this.scaleType);

        final StringBuilder debug = new StringBuilder();
        debug.append("\n====\n");
        final File file = new File(this.cpdFilePath);
        ColorPaletteDef cpd = null;
        try {
            cpd = ColorPaletteDef.loadColorPaletteDef(file);
            debug.append("Successfully loaded color palette definition: ");
            debug.append(this.cpdFilePath);

        } catch (IOException e) {
            cpd = RGBUtils.buildColorPaletteDef(colourScaleMin, colourScaleMax);
            debug.append("Built new color palette definition: ");
        }

        if (band.getIndexCoding() != null) {
            band.getImageInfo().setColors(cpd.getColors());
            debug.append("\n Non-null Index Coding");

        } else {
            debug.append("\n Null Index Coding");

            debug.append("\n CPD Auto Distribute=" + this.cpdAutoDistribute);

//            final Stx stx = band.getStx();
//
//            double minSample = stx.getMinimum();
//            double maxSample = stx.getMaximum();
//
//
//
//            // because min cannot be negative for LOG scaling
//            if (isLog && minSample < 0) {
//                minSample = colourScaleMin;
//                maxSample = colourScaleMax;
//            }

            double minSample = colourScaleMin;
            double maxSample = colourScaleMax;

            band.getImageInfo().setColorPaletteDef(cpd, minSample, maxSample, this.cpdAutoDistribute, defaultImageInfo.isLogScaled(), isLog);
            band.getImageInfo().setLogScaled(isLog);

            debug.append("\n CPD min =");
            debug.append(minSample + "   " + band.getStx().getMinimum() + "   ");
            debug.append(cpd.getMinDisplaySample() + "   "  + band.getImageInfo().getColorPaletteDef().getMinDisplaySample());
            debug.append(band.getImageInfo().isLogScaled());
            debug.append("\n CPD  max =");
            debug.append(maxSample  + "  "  + band.getStx().getMinimum() + "   " + cpd.getMaxDisplaySample() + "   " + band.getImageInfo().getColorPaletteDef().getMaxDisplaySample());
            debug.append("\n ColorScaleMin/Max  = " + colourScaleMin + "; " + colourScaleMax);
            debug.append("\n source scaling=" + (defaultImageInfo.isLogScaled() ? "LOG" : "LINEAR"));
            if (isLog) {
                debug.append("\n target scaling=LOG");

            } else {
                debug.append("\n target scaling=LINEAR");
            }
            debug.append("\n====\n");
        }


        band.getImageInfo().setNoDataColor(Color.WHITE);

        RasterDataNode[] bands = {band};

        // image looks worse when using either Normalize or Equalize
        defaultImageInfo.setHistogramMatching(ImageInfo.HistogramMatching.None);

        // construct Image
        ImageManager imageManager = ImageManager.getInstance();
        RenderedImage outputImage = imageManager.createColoredBandImage(bands, defaultImageInfo, level);

        // write Image
        JAI.create("filestore", outputImage, filePath, formatName, null);

        System.out.println(debug.toString());
    }

}