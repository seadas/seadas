package gov.nasa.gsfc.seadas.writeimage;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glayer.CollectionLayer;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerFilter;
import com.bc.ceres.glayer.LayerTypeRegistry;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glayer.support.LayerUtils;
import com.bc.ceres.grender.Viewport;
import com.bc.ceres.grender.support.BufferedImageRendering;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.Tile;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.gpf.common.ReadOp;
import org.esa.snap.core.layer.GraticuleLayer;
import org.esa.snap.core.layer.GraticuleLayerType;
import org.esa.snap.core.layer.MaskLayerType;
import org.esa.snap.core.util.PropertyMap;
import org.esa.snap.core.util.math.MathUtils;
import org.esa.snap.ui.product.ProductSceneImage;
import org.esa.snap.ui.product.ProductSceneView;
import org.esa.snap.ui.product.SimpleFeaturePointFigure;
import org.esa.snap.ui.product.VectorDataLayerFilterFactory;
import org.geotools.util.logging.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;
import java.util.List;

/**
 * @author aynur abdurazik
 */
@OperatorMetadata(alias = "WriteImage", version = "1.0", copyright = "Ocean Biology Processing Group, NASA", authors = "Kutila G, Aynur Abdurazik",
        description = "Creates a color image from a single source band.")
public class WriteImageOp extends Operator {

    protected static final String[] BMP_FORMAT_DESCRIPTION = {"BMP", "bmp", "BMP - Microsoft Windows Bitmap"};
    protected static final String[] PNG_FORMAT_DESCRIPTION = {"PNG", "png", "PNG - Portable Network Graphics"};
    protected static final String[] JPEG_FORMAT_DESCRIPTION = {
            "JPEG", "jpg,jpeg", "JPEG - Joint Photographic Experts Group"
    };
    protected static final String[] GEOTIFF_FORMAT_DESCRIPTION = {
            "GeoTIFF", "tif,tiff", "GeoTIFF - TIFF with geo-location"
    };

    private int imageWidth;
    private int imageHeight;
    double heightWidthRatio;

    /**
     * Service Provider Interface
     */
    public static class Spi extends OperatorSpi {
        public Spi() {
            super(WriteImageOp.class);
        }
    }


    private static final String LOGARITHMIC = "log";
    private static final int[] DEFAULT_MASK_COLOR = {192, 192, 192};
    private static final double DEFAULT_MASK_TRANSPARENCY = 0;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @SourceProduct(alias = "source", description = "Primary source input from which an RGB image is to be generated.")
    private Product sourceProduct;

    @TargetProduct
    private Product targetProduct;

    @Parameter(alias = "maskFilePath", description = "Source file to generate image masks. It can be a separate file or the same input source file.")
    private String maskProductFilePath;
    @Parameter(alias = "contourFilePath", description = "Source file to generate contour lines. It can be a separate file or the same input source file.")
    private String contourProductFilePath;

    @Parameter(description = "Name of band containing data. If not provided, first band in the source product is used.")
    private String sourceBandName;

//    @Parameter(description = "Mask color as an RGB value. Defaults to 192,192,192.")
//    private int[] imageMaskColor;
//
//    @Parameter(description = "BandMath value for mask creation. Leave empty if not using a mask."
//            + " The value 'maskband > 1 ? 1 : 0' imageMasks positions where the mask has values larger than 1.")
//    private String imageMaskExpression;

    @Parameter(itemAlias = "imageMask", description = "Specifies the mask layer(s) in the target image.")
    ImageMask[] imageMasks = {};

    @Parameter(itemAlias = "contour", description = "Specifies the contour(s) in the target image.")
    Contour[] contours = {};

    @Parameter(itemAlias = "textAnnotation", description = "Specifies text annotation(s) to be added in the target image.")
    TextAnnotation[] textAnnotations = {};

    @Parameter(description = "Add mask layer(s) to the target image.", defaultValue = "false")
    private boolean maskLayer;

    @Parameter(description = "Add contour layer to the target image.", defaultValue = "false")
    private boolean contourLayer;

    @Parameter(description = "Add text annotation layer to the target image.", defaultValue = "false")
    private boolean textAnnotationLayer;

    @Parameter(description = "Add graticule layer to the target image.", defaultValue = "false")
    private boolean graticuleLayer;

    @Parameter(description = "Add lat/lon labels on the graticule layer.", defaultValue = "true")
    private boolean graticuleLayerLabel;

    @Parameter(description = "Add tick to the graticule lat/lon labels.", defaultValue = "true")
    private boolean graticuleLayerTickEnabled;


    @Parameter(description = "The file to which the image is written.")
    private String filePath;

    @Parameter(description = "Output image format", defaultValue = "png")
    private String formatName;

    @Parameter(description = "Color palette definition file", defaultValue = "nofile.cpd")
    private String cpdFilePath;

    @Parameter(description = "Auto distribute points between min/max", defaultValue = "true")
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

        final int sceneRasterWidth = 1199;
        final int sceneRasterHeight = 951;

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
        this.log.debug("WriteImage.dispose() invoked");
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

            double minSample = colourScaleMin;
            double maxSample = colourScaleMax;

            band.getImageInfo().setColorPaletteDef(cpd, minSample, maxSample, this.cpdAutoDistribute, defaultImageInfo.isLogScaled(), isLog);
            band.getImageInfo().setLogScaled(isLog);

            debug.append("\n CPD min =");
            debug.append(minSample + "   " + band.getStx().getMinimum() + "   ");
            debug.append(cpd.getMinDisplaySample() + "   " + band.getImageInfo().getColorPaletteDef().getMinDisplaySample());
            debug.append(band.getImageInfo().isLogScaled());
            debug.append("\n CPD  max =");
            debug.append(maxSample + "  " + band.getStx().getMinimum() + "   " + cpd.getMaxDisplaySample() + "   " + band.getImageInfo().getColorPaletteDef().getMaxDisplaySample());
            debug.append("\n ColorScaleMin/Max  = " + colourScaleMin + "; " + colourScaleMax);
            debug.append("\n source scaling=" + (defaultImageInfo.isLogScaled() ? "LOG" : "LINEAR"));
            if (isLog) {
                debug.append("\n target scaling=LOG");

            } else {
                debug.append("\n target scaling=LINEAR");
            }
            debug.append("\n====\n");
        }

        //pale grey (gray goose): 209, 208, 206 dark grey: 110, 110, 110, battleship gray: 132, 132, 130, seadas light gray:192, 192, 192
        //band.getImageInfo().setNoDataColor(Color.WHITE);

        RasterDataNode[] bands = {band};

        // image looks worse when using either Normalize or Equalize
        defaultImageInfo.setHistogramMatching(ImageInfo.HistogramMatching.None);

        // construct Image
        //ImageManager imageManager = ImageManager.getInstance();
        //RenderedImage outputImage = imageManager.createColoredBandImage(bands, defaultImageInfo, level);

        // write Image
        //JAI.create("filestore", outputImage, filePath, formatName, null);
        writeImage2(band);
        //System.out.println(debug.toString());
    }

    private void writeImage2(Band sourceBand) {

        final StringBuilder debug = new StringBuilder();
        debug.append("\n====\n");
        PropertyMap configuration = new PropertyMap();
        ProductSceneImage productSceneImage = new ProductSceneImage(sourceBand, configuration, this.pm);
        ProductSceneView productSceneView = new ProductSceneView(productSceneImage);

        boolean entireImageSelected = true;
        final File file = new File(filePath);
        String imageFormat = formatName;//"PNG";

        if (textAnnotationLayer) {
            if (textAnnotations.length > 0) {
                productSceneView.setPinOverlayEnabled(true);
                addTextAnnotationLayer(productSceneView);
            }
        }

        if (graticuleLayer) {
            productSceneView.setGraticuleOverlayEnabled(true);
            List<Layer> layers = productSceneView.getRootLayer().getChildren();
            String pName;
            for (Layer layer:layers) {
                if (layer instanceof GraticuleLayer) {
                    PropertySet ps = layer.getConfiguration();
                    for (Property p:ps.getProperties()) {
                        if (p.getName().contains(GraticuleLayerType.PROPERTY_NAME_TICKMARK_ENABLED)) {
                            try {
                                p.setValue(new Boolean(graticuleLayerTickEnabled));
                            } catch (ValidationException ve ){

                            }

                        } else if (p.getName().contains(GraticuleLayerType.PROPERTY_NAME_TEXT_INSIDE)) {
                            try {
                                p.setValue(new Boolean(graticuleLayerLabel));
                            } catch (ValidationException ve ){

                            }

                        }
                    }
                }
            }
        }

        if (maskLayer) {
            if (imageMasks.length > 0) {
                this.applyMask(productSceneView);
            }
        }

//        if (textAnnotationLayer) {
//            if (textAnnotations.length > 0) {
//                productSceneView.setPinOverlayEnabled(true);
//                addTextAnnotationLayer(productSceneView);
//            }
//        }

        if (contourLayer) {
            if (contours.length > 0) {
                this.addContourLayers(productSceneView);
            }
        }

        write3(imageFormat, productSceneView, entireImageSelected, file);
        System.out.println(debug.toString());
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //get current date time with Date()
        Date date = new Date();
        System.out.println(dateFormat.format(date));
    }

    private void write3(String imageFormat, ProductSceneView view, boolean entireImageSelected, File file) {


        RenderedImage image = createImage(imageFormat, view);
        boolean geoTIFFWritten = false;
        try {
            if (imageFormat.equals("GeoTIFF") && entireImageSelected) {
                final GeoTIFFMetadata metadata = ProductUtils.createGeoTIFFMetadata(view.getProduct());
                if (metadata != null) {
                    GeoTIFF.writeImage(image, file, metadata);
                    geoTIFFWritten = true;
                }
            }
            if (!geoTIFFWritten) {
                if ("JPEG".equalsIgnoreCase(imageFormat)) {
                    image = BandSelectDescriptor.create(image, new int[]{0, 1, 2}, null);
                }
                final OutputStream stream = new FileOutputStream(file);
                try {
                    ImageEncoder encoder = ImageCodec.createImageEncoder(imageFormat, stream, null);
                    encoder.encode(image);
                } finally {
                    stream.close();
                }
            }
        } catch (IOException ioe) {

        }
    }

    private void writeImage(String imageFormat, RenderedImage finalImage, ProductSceneView view, boolean entireImageSelected, File file) {

        boolean geoTIFFWritten = false;
        try {
            if (imageFormat.equals("GeoTIFF") && entireImageSelected) {
                final GeoTIFFMetadata metadata = ProductUtils.createGeoTIFFMetadata(view.getProduct());
                if (metadata != null) {
                    GeoTIFF.writeImage(finalImage, file, metadata);
                    geoTIFFWritten = true;
                }
            }
            if (!geoTIFFWritten) {
                if ("JPEG".equalsIgnoreCase(imageFormat)) {
                    finalImage = BandSelectDescriptor.create(finalImage, new int[]{0, 1, 2}, null);
                }
                final OutputStream stream = new FileOutputStream(file);
                try {
                    ImageEncoder encoder = ImageCodec.createImageEncoder(imageFormat, stream, null);
                    encoder.encode(finalImage);
                } finally {
                    stream.close();
                }
            }
        } catch (IOException ioe) {

        }
    }


    protected RenderedImage createImage(String imageFormat, ProductSceneView view) {
        final boolean useAlpha = !BMP_FORMAT_DESCRIPTION[0].equals(imageFormat) && !JPEG_FORMAT_DESCRIPTION[0].equals(imageFormat);
        final boolean entireImage = true;
        Dimension dimension = new Dimension(getImageDimensions(view, entireImage));
        return createImage(view, entireImage, dimension, useAlpha,
                GEOTIFF_FORMAT_DESCRIPTION[0].equals(imageFormat));
    }

    static RenderedImage createImage(ProductSceneView view, boolean fullScene, Dimension dimension,
                                     boolean alphaChannel, boolean geoReferenced) {
        final int imageType = alphaChannel ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR;
        final BufferedImage bufferedImage = new BufferedImage(dimension.width, dimension.height, imageType);

        final BufferedImageRendering imageRendering = createRendering(view, fullScene,
                geoReferenced, bufferedImage);
        if (!alphaChannel) {
            final Graphics2D graphics = imageRendering.getGraphics();
            graphics.setColor(view.getLayerCanvas().getBackground());
            graphics.fillRect(0, 0, dimension.width, dimension.height);
        }
        view.getRootLayer().render(imageRendering);

        return bufferedImage;
    }

    private static BufferedImageRendering createRendering(ProductSceneView view, boolean fullScene,
                                                          boolean geoReferenced, BufferedImage bufferedImage) {
        final Viewport vp1 = view.getLayerCanvas().getViewport();
        final Viewport vp2 = new DefaultViewport(new Rectangle(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight()),
                vp1.isModelYAxisDown());
        if (fullScene) {
            vp2.zoom(view.getBaseImageLayer().getModelBounds());
        } else {
            setTransform(vp1, vp2);
        }

        final BufferedImageRendering imageRendering = new BufferedImageRendering(bufferedImage, vp2);
        if (geoReferenced) {
            // because image to model transform is stored with the exported image we have to invert
            // image to view transformation
            final AffineTransform m2iTransform = view.getBaseImageLayer().getModelToImageTransform(0);
            final AffineTransform v2mTransform = vp2.getViewToModelTransform();
            v2mTransform.preConcatenate(m2iTransform);
            final AffineTransform v2iTransform = new AffineTransform(v2mTransform);
            final Graphics2D graphics2D = imageRendering.getGraphics();
            v2iTransform.concatenate(graphics2D.getTransform());
            graphics2D.setTransform(v2iTransform);
        }
        return imageRendering;
    }

    private static void setTransform(Viewport vp1, Viewport vp2) {
        vp2.setTransform(vp1);

        final Rectangle rectangle1 = vp1.getViewBounds();
        final Rectangle rectangle2 = vp2.getViewBounds();
        final double w1 = rectangle1.getWidth();
        final double w2 = rectangle2.getWidth();
        final double h1 = rectangle1.getHeight();
        final double h2 = rectangle2.getHeight();
        final double x1 = rectangle1.getX();
        final double y1 = rectangle1.getY();
        final double cx = (x1 + w1) / 2.0;
        final double cy = (y1 + h1) / 2.0;

        final double magnification;
        if (w1 > h1) {
            magnification = w2 / w1;
        } else {
            magnification = h2 / h1;
        }

        final Point2D modelCenter = vp1.getViewToModelTransform().transform(new Point2D.Double(cx, cy), null);
        final double zoomFactor = vp1.getZoomFactor() * magnification;
        if (zoomFactor > 0.0) {
            vp2.setZoomFactor(zoomFactor, modelCenter.getX(), modelCenter.getY());
        }
    }

    private ImageLayer addLayer(RenderedImage renderedImage, Layer layer, Band band) {
        CollectionLayer collectionLayer = new CollectionLayer();
        List<Layer> layerChildren = collectionLayer.getChildren();
        ImageLayer imageLayer = new ImageLayer(renderedImage); // or a RenderedImage
        ImageLayer imageLayer1 = new ImageLayer(band.getSourceImage().getImage(0));

        layerChildren.add(0, imageLayer);
        layerChildren.add(0, layer);
        return imageLayer;
    }

    private RenderedImage addLayers(RenderedImage renderedImage, ArrayList<Layer> layers) {
        CollectionLayer collectionLayer = new CollectionLayer();
        List<Layer> layerChildren = collectionLayer.getChildren();
        ImageLayer imageLayer = new ImageLayer(renderedImage); // or a RenderedImage
        layerChildren.add(0, imageLayer);

        for (Layer layer : layers) {
            if (layer != null)
                layerChildren.add(0, layer);
        }

        Rectangle2D modelBounds = collectionLayer.getModelBounds();
        Rectangle2D imageBounds = imageLayer.getModelToImageTransform().createTransformedShape(modelBounds).getBounds2D();
        BufferedImage bufferedImage = new BufferedImage((int) imageBounds.getWidth(),
                (int) imageBounds.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        BufferedImageRendering rendering = new BufferedImageRendering(bufferedImage);
        Viewport viewport = rendering.getViewport();
        viewport.setModelYAxisDown(imageLayer.getImageToModelTransform().getDeterminant() > 0.0);
        viewport.zoom(modelBounds);
        collectionLayer.render(rendering);
        RenderedImage outputImage = rendering.getImage();

        return outputImage;
    }

    private Layer getColorBarLayer(ProductSceneView productSceneView) {

        ShowColorBarOverlayAction showColorBarOverlayAction = new ShowColorBarOverlayAction();
        RenderedImage colorBarImage = createImage(formatName, productSceneView);//createImage("PNG", productSceneView);

        showColorBarOverlayAction.setColorBarImage(colorBarImage);
        showColorBarOverlayAction.actionPerformed(null);

        Layer colorBarLayer = LayerUtils.getChildLayer(productSceneView.getRootLayer(), LayerUtils.SearchMode.DEEP, new LayerFilter() {
            public boolean accept(Layer layer) {
                return layer.getLayerType() instanceof ColorBarLayerType;
            }
        });

        return colorBarLayer;
    }



    /**
     * If a mask value is provided, creates and applies a Mask to the
     * given ProductSceneView. Silently returns if there is no mask value.
     *
     * @param productSceneView
     */
    private void applyMask(final ProductSceneView productSceneView) {
        ImageMask imageMask;
        String maskSourceBandName;
        String maskName;
        String maskDescription;
        Band maskBand;
        int[] maskColorValueArray;
        String maskExpression;
        double maskTransparency;
        Layer maskCollectionLayer = productSceneView.getSceneImage().getMaskCollectionLayer(true);
        maskCollectionLayer.setVisible(true);
        int existingLayerCount = maskCollectionLayer.getChildren().size();
        Operator readerOp = new ReadOp();
        readerOp.setParameter("file", new File(maskProductFilePath));
        readerOp.initialize();
        Product maskProduct = readerOp.getTargetProduct();
        for (int i = 0; i < imageMasks.length; i++) {
            imageMask = imageMasks[i];
            //extract mask band, rename it, and add to the source product
            maskSourceBandName = imageMask.getImageMaskSourceBandName();
            maskBand = maskProduct.getBand(maskSourceBandName);
            maskBand.setName(maskSourceBandName);
            if (!sourceProduct.containsBand(maskSourceBandName)) {
                sourceProduct.addBand(maskBand);
            }
            maskName = imageMask.getImageMaskName();
            maskDescription = imageMask.getImageMaskDescription();

            maskExpression = imageMask.getImageMaskExpression();

            maskColorValueArray = imageMask.getColor();
            maskTransparency = imageMask.getImageMaskTransparency();

            if (maskExpression != null && maskExpression.length() > 0) {

                if (maskColorValueArray == null || maskColorValueArray.length != 3) {
                    maskColorValueArray = DEFAULT_MASK_COLOR;
                }
                final Color maskColor = new Color(maskColorValueArray[0], maskColorValueArray[1], maskColorValueArray[2]);

                final Mask mask = Mask.BandMathsType.create(maskName,
                        maskDescription,
                        this.sourceBand.getRasterWidth(),
                        this.sourceBand.getRasterHeight(),
                        maskExpression,
                        maskColor,
                        maskTransparency);

                this.sourceProduct.getMaskGroup().add(i + existingLayerCount, mask);
                maskCollectionLayer.getChildren().add(i + existingLayerCount, getMaskAsLayer(this.sourceProduct.getMaskGroup().get(maskName)));
            }
        }

        List<Layer> maskLayers = maskCollectionLayer.getChildren();

        for (Layer layer : maskLayers)
        {
            layer.setVisible(true);
            if (layer.getName().equals("text_annotations")) {
                layer.setTransparency(0);
            } else {
                layer.setTransparency(0);
            }
            System.out.println("layer name: " + layer.getName() + " " + layer.getTransparency());
            if (layer.equals(maskLayers.get(maskLayers.size()-1))) break;
        }
        productSceneView.setMaskOverlayEnabled(true);
    }

    private Layer getMaskAsLayer(final Mask mask) {
        final MaskLayerType maskLayerType = LayerTypeRegistry.getLayerType(MaskLayerType.class);
        final PropertySet configuration = maskLayerType.createLayerConfig(null);
        configuration.setValue(MaskLayerType.PROPERTY_NAME_MASK, mask);
        final Layer maskLayer = maskLayerType.createLayer(null, configuration);
        return maskLayer;
    }

    public Dimension getImageDimensions(ProductSceneView view, boolean full) {
        final Rectangle2D bounds;
        if (full) {
            final ImageLayer imageLayer = view.getBaseImageLayer();
            final Rectangle2D modelBounds = imageLayer.getModelBounds();
            Rectangle2D imageBounds = imageLayer.getModelToImageTransform().createTransformedShape(modelBounds).getBounds2D();

            final double mScale = modelBounds.getWidth() / modelBounds.getHeight();
            final double iScale = imageBounds.getHeight() / imageBounds.getWidth();
            double scaleFactorX = mScale * iScale;
            bounds = new Rectangle2D.Double(0, 0, scaleFactorX * imageBounds.getWidth(), 1 * imageBounds.getHeight());
        } else {
            bounds = view.getLayerCanvas().getViewport().getViewBounds();
        }

        imageWidth = toInteger(bounds.getWidth());
        imageHeight = toInteger(bounds.getHeight());
        heightWidthRatio = (double) imageHeight / (double) imageWidth;
        return new Dimension(imageWidth, imageHeight);
    }

    /**
     * Adds annotation layer.
     * @param productSceneView
     */
    public void addTextAnnotationLayer(ProductSceneView productSceneView) {

        Product sourceProduct = productSceneView.getProduct();

        TextAnnotationDescriptor descriptor = TextAnnotationDescriptor.getInstance();

        PixelPos pixelPos = null;
        GeoCoding geoCoding = sourceProduct.getGeoCoding();
        GeoPos geoPos = null;
        Placemark textAnnotationMark;
        Font textFont;
        Color textColor;
        Color textOutlineColor;
        float[] latlon;
        int[] pixels;
        for (int i = 0; i < textAnnotations.length; i++) {
            latlon = textAnnotations[i].getTextAnnotationLatLon();
            pixels = textAnnotations[i].getTextAnnotationLocation();
            if (latlon != null && latlon.length > 0) {
                geoPos = new GeoPos(latlon[0], latlon[1]);
                pixelPos = geoCoding.getPixelPos(geoPos, null);
            } else if (pixels != null && pixels.length > 0) {
                pixelPos = new PixelPos(pixels[0], pixels[1]);
                geoPos = geoCoding.getGeoPos(pixelPos, null);
            }

            if (pixelPos != null && geoPos !=null ) {
                textAnnotationMark = Placemark.createPointPlacemark(descriptor,
                        textAnnotations[i].getTextAnnotationName(),
                        textAnnotations[i].getTextAnnotationContent(),
                        "This won't show!",
                        pixelPos,
                        geoPos,
                        geoCoding);

                sourceProduct.getTextAnnotationGroup().add(textAnnotationMark);
                textFont = new Font(textAnnotations[i].getTextAnnotationFontName(), textAnnotations[i].getTextAnnotationFontStyle(), textAnnotations[i].getTextAnnotationFontSize());
                textColor = new Color(textAnnotations[i].getTextAnnotationFontColor()[0], textAnnotations[i].getTextAnnotationFontColor()[1], textAnnotations[i].getTextAnnotationFontColor()[2]);
                textOutlineColor = Color.BLACK;
                if (textAnnotationMark.getFeature() instanceof SimpleFeaturePointFigure) {
                    ((SimpleFeaturePointFigure) textAnnotationMark.getFeature()).updateFontColor(textFont, textColor, textOutlineColor);
                }
            }
            //this will make the text annotation layer visible
            //setTextAnnotationFont(productSceneView, textFont, textColor, textOutlineColor);
        }
    }

    /**
     *
     * @param productSceneView
     */
    private void addContourLayers(ProductSceneView productSceneView) {
        ImageMask imageMask;
        String contourSourceBandName;
        Operator readerOp = new ReadOp();
        readerOp.setParameter("file", new File(contourProductFilePath));
        readerOp.initialize();
        Product contourProduct = readerOp.getTargetProduct();
        productSceneView.setGcpOverlayEnabled(true);
        Product sourceProduct = productSceneView.getProduct();
        String filterBandName;
        ContourData contourData;
        Color contourLineColor;
        Band filteredBand;
        Band contourBand;
        for (Contour contour: contours) {
            contourSourceBandName = contour.getContourSourceBandName();
            contourBand = contourProduct.getBand(contourSourceBandName);
            contourBand.setName(contourSourceBandName);
            if (!sourceProduct.containsBand(contourSourceBandName)) {
                sourceProduct.addBand(contourBand);
            }
            filterBandName = contour.getFilterName();
            filteredBand = getFilteredBand(contourBand, contour.getName());
            ContourInterval ci = new ContourInterval(contour.getName(), new Double(contour.getValue()), filterBandName, 1, true); //0.08, "am5", 1);
            contourLineColor = new Color(contour.getColor()[0], contour.getColor()[1], contour.getColor()[2]);
            ci.setLineColor(contourLineColor);
            ArrayList<ContourInterval> contourIntervals = new ArrayList<>();
            contourIntervals.add(ci);
            if (contour.applyFilter){
               contourBand = filteredBand;
            } else {
                contourBand = sourceBand;
            }
            contourData = new ContourData(contourBand, filterBandName, sourceBandName, 1);
            contourData.setContourIntervals(contourIntervals);
            contourData.setBand(contourBand);

            ShowVectorContourOverlayAction action = new ShowVectorContourOverlayAction();
            action.setGeoCoding((GeoCoding) sourceProduct.getGeoCoding());
            ArrayList<VectorDataNode> vectorDataNodes = action.createVectorDataNodesforContours(contourData);


            for (VectorDataNode vectorDataNode : vectorDataNodes) {
                // remove the old vector data node with the same name.
                if (sourceProduct.getVectorDataGroup().contains(vectorDataNode.getName())) {
                    sourceProduct.getVectorDataGroup().remove(sourceProduct.getVectorDataGroup().get(vectorDataNode.getName()));
                }
                productSceneView.getProduct().getVectorDataGroup().add(vectorDataNode);
                if (productSceneView != null) {
                    productSceneView.setLayersVisible(vectorDataNode);
                }
                final LayerFilter nodeFilter = VectorDataLayerFilterFactory.createNodeFilter(vectorDataNode);
                Layer vectorDataLayer = LayerUtils.getChildLayer(productSceneView.getRootLayer(),
                        LayerUtils.SEARCH_DEEP,
                        nodeFilter);
                List<Layer> children = productSceneView.getRootLayer().getChildren();

                if (vectorDataLayer != null) {
                    vectorDataLayer.setVisible(true);
                } else {
                    //System.out.println("vector data layer is null " + vectorDataNode.toString());
                }

            }
        }
    }
    private int toInteger(double value) {
        return MathUtils.floorInt(value);
    }

    public static class TextAnnotation {

        @Parameter(description = "The name of a text annotation.")
        private String textAnnotationName;
        @Parameter(description = "The description of a text annotation.")
        private String textAnnotationDescription;
        @Parameter(description = "The content of a text annotation.")
        private String textAnnotationContent;
        @Parameter(description = "The text font name of a text annotation. Names are Helvetica, SanSerif, Serif, Times New Roman, etc.")
        private String textAnnotationFontName;
        @Parameter(description = "The text font style of a text annotation. Styles are 0 = REGULAR, 1 = BOLD, and 2 = ITALIC.")
        private int textAnnotationFontStyle;
        @Parameter(description = "The text font size of a text annotation.")
        private int textAnnotationFontSize;
        @Parameter(description = "The content of a text annotation.")
        private int[] textAnnotationFontColor;
        @Parameter(description = "The pixel location to place a text annotation.")
        private int[] textAnnotationLocation;
        @Parameter(description = "The lat/lon specification to place a text annotation.")
        private float[] textAnnotationLatLon;

        public TextAnnotation() {
        }

        public TextAnnotation(String textAnnotationName,
                              String textAnnotationDescription,
                              String textAnnotationContent,
                              String textAnnotationFontName,
                              int textAnnotationFontStyle,
                              int textAnnotationFontSize,
                              int[] textAnnotationFontColor,
                              int[] textAnnotationLocation,
                              float[] textAnnotationLatLon) {
            this.textAnnotationName = textAnnotationName;
            this.textAnnotationDescription = textAnnotationDescription;
            this.textAnnotationContent = textAnnotationContent;
            this.textAnnotationFontName = textAnnotationFontName;
            this.textAnnotationFontStyle = textAnnotationFontStyle;
            this.textAnnotationFontSize = textAnnotationFontSize;
            this.textAnnotationFontColor = textAnnotationFontColor;
            this.textAnnotationLocation = textAnnotationLocation;
            this.textAnnotationLatLon = textAnnotationLatLon;
        }

        public String getTextAnnotationName() {
            return textAnnotationName;
        }

        public void setTextAnnotationName(String textAnnotationName) {
            this.textAnnotationName = textAnnotationName;
        }

        public String getTextAnnotationDescription() {
            return textAnnotationDescription;
        }

        public void setTextAnnotationDescription(String textAnnotationDescription) {
            this.textAnnotationDescription = textAnnotationDescription;
        }

        public String getTextAnnotationContent() {
            return textAnnotationContent;
        }

        public void setTextAnnotationContent(String textAnnotationContent) {
            this.textAnnotationContent = textAnnotationContent;
        }

        public int[] getTextAnnotationLocation() {
            return textAnnotationLocation;
        }

        public void setTextAnnotationLocation(int[] textAnnotationLocation) {
            this.textAnnotationLocation = textAnnotationLocation;
        }

        public int getTextAnnotationFontSize() {
            return textAnnotationFontSize;
        }

        public void setTextAnnotationFontSize(int textAnnotationFontSize) {
            this.textAnnotationFontSize = textAnnotationFontSize;
        }

        public int[] getTextAnnotationFontColor() {
            return textAnnotationFontColor;
        }

        public void setTextAnnotationFontColor(int[] textAnnotationFontColor) {
            this.textAnnotationFontColor = textAnnotationFontColor;
        }

        public String getTextAnnotationFontName() {
            return textAnnotationFontName;
        }

        public void setTextAnnotationFontName(String textAnnotationFontName) {
            this.textAnnotationFontName = textAnnotationFontName;
        }

        public int getTextAnnotationFontStyle() {
            return textAnnotationFontStyle;
        }

        public void setTextAnnotationFontStyle(int textAnnotationFontStyle) {
            this.textAnnotationFontStyle = textAnnotationFontStyle;
        }

        public float[] getTextAnnotationLatLon() {
            return textAnnotationLatLon;
        }

        public void setTextAnnotationLatLon(float[] textAnnotationLatLon) {
            this.textAnnotationLatLon = textAnnotationLatLon;
        }
    }

    public static class Contour {

        @Parameter(description = "The source band to create contour lines from.")
        private
        String contourSourceBandName;
        @Parameter(description = "The name of the contour.")
        String contourName;
        @Parameter(description = "The value of the contour.")
        String contourValue;
        @Parameter(description = "Apply filter to the source band before forming contour lines.", defaultValue = "true")
        private boolean applyFilter;
        @Parameter(description = "Name of the filter band.", defaultValue = "am5")
        private String filterName;
        @Parameter(description = "Contour line color as an RGB value. Defaults to black (0,0,0).")
        private int[] contourLineColor;
        @Parameter(description = "Contour line color as an RGB value. Defaults to solid (1.0,0.0).")
        private double[] contourLineStyle;
        @Parameter(description = "Contour line dash length for dashed lines.", defaultValue = "1.0")
        private double contourLineDashLength;
        @Parameter(description = "Contour line space length for dashed lines.", defaultValue = "0")
        private double contourLineSpaceLength;


        public Contour() {
        }

        public Contour(String contourName, String contourValue, int[] contourLineColor, boolean applyFilter, String filterName) {
            this.contourName = contourName;
            this.contourValue = contourValue;
            this.filterName = filterName;
            this.applyFilter = applyFilter;
            this.contourLineColor = contourLineColor;
        }

        public String getName() {
            return contourName;
        }

        public void setName(String contourName) {
            this.contourName = contourName;
        }

        public String getValue() {
            return contourValue;
        }

        public void setValue(String contourValue) {
            this.contourValue = contourValue;
        }

        public int[] getColor() {
            return contourLineColor;
        }

        public void setColor(int[] contourLineColor) {
            this.contourLineColor = contourLineColor;
        }

        public boolean isApplyFilter() {
            return applyFilter;
        }

        public void setApplyFilter(boolean applyFilter) {
            this.applyFilter = applyFilter;
        }

        public String getFilterName() {
            return filterName;
        }

        public void setFilterName(String filterName) {
            this.filterName = filterName;
        }

        public double[] getContourLineStyle() {
            return contourLineStyle;
        }

        public void setContourLineStyle(double[] contourLineStyle) {
            this.contourLineStyle = contourLineStyle;
        }

        public double getContourLineDashLenth() {
            return contourLineDashLength;
        }

        public void setContourLineDashLenth(double contourLineDashLenth) {
            this.contourLineDashLength = contourLineDashLenth;
        }

        public double getContourLineSpaceLength() {
            return contourLineSpaceLength;
        }

        public void setContourLineSpaceLength(double contourLineSpaceLength) {
            this.contourLineSpaceLength = contourLineSpaceLength;
        }

        public String getContourSourceBandName() {
            return contourSourceBandName;
        }

        public void setContourSourceBandName(String contourSourceBandName) {
            this.contourSourceBandName = contourSourceBandName;
        }
    }

    public static class ImageMask {

        @Parameter(description = "Name of band that is used to create a mask. If not provided, first band in the mask product is used.")
        private String imageMaskSourceBandName;
        @Parameter(description = "Name of the mask.")
        private String imageMaskName;
        @Parameter(description = "Description of the mask to be added as a layer on the target image.")
        private String imageMaskDescription;
        @Parameter(description = "BandMath value for mask creation. Leave empty if not using a mask."
                + " The value 'maskband > 1 ? 1 : 0' imageMasks positions where the mask has values larger than 1.")
        private String imageMaskExpression;
        @Parameter(description = "Mask color as an RGB value. Defaults to 192,192,192.")
        private int[] imageMaskColor = DEFAULT_MASK_COLOR;
        @Parameter(description = "Transparency of the mask in the target image. Defaults to 0.")
        private double imageMaskTransparency = DEFAULT_MASK_TRANSPARENCY;

        public ImageMask() {
        }

        public ImageMask(String imageMaskExpression, int[] imageMaskColor) {
            this.imageMaskExpression = imageMaskExpression;
            this.imageMaskColor = imageMaskColor;
        }

        public String getImageMaskExpression() {
            return imageMaskExpression;
        }

        public void setImageMaskExpression(String imageMaskExpression) {
            this.imageMaskExpression = imageMaskExpression;
        }

        public int[] getColor() {
            return imageMaskColor;
        }

        public void setColor(int[] maskColor) {
            this.imageMaskColor = maskColor;
        }

        public String getImageMaskSourceBandName() {
            return imageMaskSourceBandName;
        }

        public void setImageMaskSourceBandName(String imageMaskSourceBandName) {
            this.imageMaskSourceBandName = imageMaskSourceBandName;
        }

        public String getImageMaskName() {
            return imageMaskName;
        }

        public void setImageMaskName(String imageMaskName) {
            this.imageMaskName = imageMaskName;
        }

        public String getImageMaskDescription() {
            return imageMaskDescription;
        }

        public void setImageMaskDescription(String imageMaskDescription) {
            this.imageMaskDescription = imageMaskDescription;
        }

        public double getImageMaskTransparency() {
            return imageMaskTransparency;
        }

        public void setImageMaskTransparency(double imageMaskTransparency) {
            this.imageMaskTransparency = imageMaskTransparency;
        }
    }

    private FilterBand getFilteredBand(Band sourceBand, String contourName) {
        Filter defaultFilter =new Filter("Arithmetic Mean 5x5", "am5", 5, 5, new double[]{
                +1, +1, +1, +1, +1,
                +1, +1, +1, +1, +1,
                +1, +1, +1, +1, +1,
                +1, +1, +1, +1, +1,
                +1, +1, +1, +1, +1,
        }, 25.0);
        RasterDataNode sourceRaster = sourceProduct.getRasterDataNode(sourceBand.getName());
        final FilterBand filteredBand = CreateFilteredBandAction.createFilterBandForGPT(sourceRaster, defaultFilter, sourceBand.getName() + "_am5_" + contourName, 1);
        return filteredBand;
    }
}