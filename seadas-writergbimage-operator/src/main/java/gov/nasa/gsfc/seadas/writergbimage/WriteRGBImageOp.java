package gov.nasa.gsfc.seadas.writergbimage;

import org.esa.beam.framework.gpf.annotations.OperatorMetadata;


import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.media.jai.JAI;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.ColorPaletteDef;
import org.esa.beam.framework.datamodel.ImageInfo;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.framework.datamodel.Stx;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.glayer.GraticuleLayer;
import org.esa.beam.glayer.GraticuleLayerType;
import org.esa.beam.jai.ImageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glayer.CollectionLayer;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerType;
import com.bc.ceres.glayer.LayerTypeRegistry;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.grender.Viewport;
import com.bc.ceres.grender.support.BufferedImageRendering;

/**
 * @author kutila
 * @author aynur abdurazik
 */
@OperatorMetadata(alias = "WriteRGBImage", version = "0.4", copyright = "(c) 2014 University of Queensland", authors = "Kutila G",
        description = "Creates an RGB image from a single source band.")
public class WriteRGBImageOp extends Operator {
    /**
     * Service Provider Interface
     */
    public static class Spi extends OperatorSpi {
        public Spi() {
            super(WriteRGBImageOp.class);
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
        this.log.debug(" WriteRGBImage.computeTile({}, {}) ", rectangle.getX(), rectangle.getY());

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
        ColorPaletteDef cpd = null;
        try {
            cpd = ColorPaletteDef.loadColorPaletteDef(new File(this.cpdFilePath));
        } catch (IOException e) {
            cpd = RGBUtils.buildColorPaletteDef(colourScaleMin, colourScaleMax);
        }

        Band band = this.sourceBand;

        final ImageInfo defaultImageInfo = band.createDefaultImageInfo(histoSkipRatios, this.pm);
        band.setImageInfo(defaultImageInfo);

        if (band.getIndexCoding() != null) {
            band.getImageInfo().setColors(cpd.getColors());
        } else {
            Stx stx = band.getStx();
            band.getImageInfo().setColorPaletteDef(cpd,
                    stx.getMinimum(),
                    stx.getMaximum(), false);

            band.getImageInfo().setLogScaled(cpd.isLogScaled());
            band.getImageInfo().getColorPaletteSourcesInfo().setCpdFileName(cpdFilePath);
        }


        band.getImageInfo().setNoDataColor(Color.WHITE);
        //defaultImageInfo.setNoDataColor(Color.WHITE);

        RasterDataNode[] bands = {band};

        // image looks worse when using either Normalize or Equalize
        defaultImageInfo.setHistogramMatching(ImageInfo.HistogramMatching.None);

        // has no noticeable effect
        defaultImageInfo.setLogScaled(LOGARITHMIC.equalsIgnoreCase(this.scaleType));

        // construct Image
        ImageManager imageManager = ImageManager.getInstance();
        RenderedImage outputImage = imageManager.createColoredBandImage(bands, defaultImageInfo, level);

        //------------------------
        // Test
        //this.testAddingLayers(outputImage, band);
        //------------------------


        // write Image
        JAI.create("filestore", outputImage, filePath, formatName, null);
    }


    protected void testAddingLayers(RenderedImage image, Band band) {
        // 1. create a collection of layers
        CollectionLayer collectionLayer = new CollectionLayer();
        List<Layer> layerChildren = collectionLayer.getChildren();

        // 2. create layers
        ImageLayer imageLayer = new ImageLayer(image);//(RenderedImage)band.getSourceImage());

        LayerType graticuleType = LayerTypeRegistry.getLayerType(GraticuleLayerType.class);
        PropertySet template = graticuleType.createLayerConfig(null);
        template.setValue(GraticuleLayerType.PROPERTY_NAME_RASTER, band);
        GraticuleLayer graticuleLayer = (GraticuleLayer) graticuleType.createLayer(null, template);

//        MaskLayerType type = LayerTypeRegistry.getLayerType(MaskLayerType.class);
//        PropertySet configuration = type.createLayerConfig(null);
//        Mask.BandMathsType.create("coast", "", band.getSceneRasterWidth(), band.getSceneRasterHeight(),
//                "l1_flags.COASTLINE", Color.WHITE, 3.2);

//        Mask coastlineMask;
//        configuration.setValue(MaskLayerType.PROPERTY_NAME_MASK, coastlineMask);
//        Layer coastlineLayer = type.createLayer(null, configuration);


        // 3. add layers to the Layer Collection
        layerChildren.add(0, imageLayer);
        layerChildren.add(0, graticuleLayer);
//        layerChildren.add(0, coastlineLayer);


        // 4. convert Layers into an image
        Rectangle2D modelBounds = collectionLayer.getModelBounds();
        Rectangle2D imageBounds =
                imageLayer.getModelToImageTransform().createTransformedShape(modelBounds).getBounds2D();
        BufferedImage bufferedImage =
                new BufferedImage((int) imageBounds.getWidth(), (int) imageBounds.getHeight(),
                        BufferedImage.TYPE_4BYTE_ABGR);

        BufferedImageRendering rendering = new BufferedImageRendering(bufferedImage);
        Viewport viewport = rendering.getViewport();
        viewport.setModelYAxisDown(imageLayer.getImageToModelTransform().getDeterminant() > 0.0);
        viewport.zoom(modelBounds);
        collectionLayer.render(rendering);
        RenderedImage outputImage = rendering.getImage();

        JAI.create("filestore", outputImage, filePath + "-test.png", formatName, null);

    }


}
