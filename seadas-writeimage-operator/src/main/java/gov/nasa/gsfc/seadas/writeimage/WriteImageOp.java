package gov.nasa.gsfc.seadas.writeimage;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.accessors.DefaultPropertyAccessor;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glayer.*;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glayer.support.LayerUtils;
import com.bc.ceres.grender.Viewport;
import com.bc.ceres.grender.support.BufferedImageRendering;
import com.bc.ceres.grender.support.DefaultViewport;
import com.bc.ceres.swing.figure.Figure;
import com.bc.ceres.swing.figure.FigureCollection;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import gov.nasa.gsfc.seadas.contour.action.ShowVectorContourOverlayAction;
import gov.nasa.gsfc.seadas.contour.data.ContourData;
import gov.nasa.gsfc.seadas.contour.data.ContourInterval;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.framework.ui.product.*;
import org.esa.beam.glayer.ColorBarLayerType;
import org.esa.beam.glayer.GraticuleLayer;
import org.esa.beam.glayer.GraticuleLayerType;
import org.esa.beam.glayer.MaskLayerType;
import org.esa.beam.jai.ImageManager;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.util.PropertyMap;
import org.esa.beam.util.geotiff.GeoTIFF;
import org.esa.beam.util.geotiff.GeoTIFFMetadata;
import org.esa.beam.util.math.MathUtils;
import org.esa.beam.visat.actions.ShowColorBarOverlayAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.jai.JAI;
import javax.media.jai.operator.BandSelectDescriptor;
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
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author kutila
 * @author aynur abdurazik
 */
@OperatorMetadata(alias = "WriteImage", version = "0.4", copyright = "(c) 2014 University of Queensland", authors = "Kutila G",
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

    @SourceProduct(alias = "mask", description = "Mask product to be applied.")
    private Product maskProduct;

    @TargetProduct
    private Product targetProduct;

    @Parameter(description = "Name of band containing data. If not provided, first band in the source product is used.")
    private String sourceBandName;

//    @Parameter(description = "Mask color as an RGB value. Defaults to 192,192,192.")
//    private int[] maskColor;
//
//    @Parameter(description = "BandMath value for mask creation. Leave empty if not using a mask."
//            + " The value 'maskband > 1 ? 1 : 0' masks positions where the mask has values larger than 1.")
//    private String maskExpression;

    @Parameter(itemAlias = "mask", description = "Specifies the mask layer(s) in the target image.")
    ImageMask[] masks;

    @Parameter(itemAlias = "contour", description = "Specifies the contour(s) in the target image.")
    Contour[] contours;

    @Parameter(itemAlias = "textAnnotation", description = "Specifies text annotation(s) to be added in the target image.")
    TextAnnotation[] textAnnotations;

    @Parameter(description = "The file to which the image is written.")
    private String filePath;
    @Parameter(description = "The file to which the image 1 is written.")
    private String filePath1;

    @Parameter(description = "The file to which the image 2 is written.")
    private String filePath2;

    @Parameter(description = "The file to which the image 3 is written.")
    private String filePath3;

    @Parameter(description = "The file to which the image 4 is written.")
    private String filePath4;

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
        //final int sceneRasterWidth = this.sourceProduct.getSceneRasterWidth();
        //final int sceneRasterHeight = this.sourceProduct.getSceneRasterHeight();

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
        ImageManager imageManager = ImageManager.getInstance();
        RenderedImage outputImage = imageManager.createColoredBandImage(bands, defaultImageInfo, level);

        // write Image
        JAI.create("filestore", outputImage, filePath, formatName, null);
        writeImage2(band);
        System.out.println(debug.toString());
    }

    private void writeImage2(Band sourceBand) {

        final StringBuilder debug = new StringBuilder();
        debug.append("\n====\n");

//        ApplicationDescriptor ad = new DefaultApplicationDescriptor();
//        VisatApp visatApp = new VisatApp(ad);
        PropertyMap configuration = new PropertyMap();
        ProductSceneImage productSceneImage = new ProductSceneImage(sourceBand, configuration, this.pm);
        ProductSceneView productSceneView = new ProductSceneView(productSceneImage);

        boolean entireImageSelected = true;
        final File file1 = new File(filePath1);
        final File file2 = new File(filePath2);
        final File file3 = new File(filePath3);
        final File file4 = new File(filePath4);
        String imageFormat = "PNG";

        //write3(imageFormat, productSceneView, entireImageSelected, file1);
        productSceneView.setPinOverlayEnabled(true);
        addTextAnnotationLayer(productSceneView);
        productSceneView.setGraticuleOverlayEnabled(true);
        //productSceneView.getProduct().getBandAt(0).getImageInfo().setNoDataColor(Color.RED);

        //productSceneImage.getImageInfo().setNoDataColor(Color.WHITE);
        //productSceneView.setNoDataOverlayEnabled(true);
        //productSceneImage.getImageInfo().setNoDataColor(Color.WHITE);
        //write3(imageFormat, productSceneView, entireImageSelected, file2);

        //getContourLayer(productSceneView, sourceBand);

        //write3(imageFormat, productSceneView, entireImageSelected, file3);


        //write3(imageFormat, productSceneView, entireImageSelected, file4);

//        RenderedImage sourceImage = createImage(imageFormat, productSceneView);
//
//        //ImageLayer finalImageLayer = addLayer(sourceImage, layers);
//        RenderedImage finalImage = addLayers(sourceImage, layers);
//
//
//        writeImage(imageFormat, finalImage, productSceneView, entireImageSelected, file3);


        if (masks.length > 0) {
            this.applyMask(productSceneView);
        }


        //productSceneView.setMaskOverlayEnabled(true);
        //System.out.print("mask layer ");
        //System.out.println(getMaskLayer(sourceProduct.getMaskGroup().get(0)).getId());
        write3(imageFormat, productSceneView, entireImageSelected, file4);
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
        RenderedImage colorBarImage = createImage("PNG", productSceneView);
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
        System.out.print("mask layers: ");
        System.out.println(maskCollectionLayer.getChildren().size());
        for (int i = 0; i < masks.length; i++) {
            imageMask = masks[i];
            //extract mask band, rename it, and add to the source product
            maskSourceBandName = imageMask.getMaskSourceBandName();
            maskBand = maskProduct.getBand(maskSourceBandName);
            maskBand.setName(maskSourceBandName);
            if (!sourceProduct.containsBand(maskSourceBandName)) {
                sourceProduct.addBand(maskBand);
            }
            maskName = imageMask.getMaskName();
            maskDescription = imageMask.getMaskDescription();

            maskExpression = imageMask.getMaskExpression();

            maskColorValueArray = imageMask.getColor();
            maskTransparency = imageMask.getMaskTransparency();

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

                //this.maskProduct.getMaskGroup().add(i, mask);
                this.sourceProduct.getMaskGroup().add(i + existingLayerCount, mask);
                maskCollectionLayer.getChildren().add(i + existingLayerCount, getMaskAsLayer(this.sourceProduct.getMaskGroup().get(maskName)));
                //productSceneView.getRootLayer().getChildren().add(i, getMaskAsLayer(this.sourceProduct.getMaskGroup().get(maskName)));
            }
        }
        List<Layer> layers = productSceneView.getRootLayer().getChildren();
        List<Layer> maskLayers = maskCollectionLayer.getChildren();

        for (Layer layer : layers) {
            if (layer instanceof org.esa.beam.glayer.MaskCollectionLayer) {
                System.out.println("mask layers: " + layer.getChildren().size());

            }
        }

        System.out.print("mask layers: ");
        System.out.println(maskLayers.size());
        for (Layer layer : maskLayers)

        {
            System.out.println(layer.getName() + " " + layer.isVisible() + " " + layer.getId() + " " + maskLayers.indexOf(layer));
            layer.setVisible(true);
            layer.setTransparency(0);
            //if (layer.equals(maskLayers.get(maskLayers.size()-1))) break;
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

    public void getContourLayer(ProductSceneView productSceneView, Band sourceBand) {
        productSceneView.setGcpOverlayEnabled(true);
        Product sourceProduct = productSceneView.getProduct();
        ContourInterval ci = new ContourInterval("contour_test_line_", 0.08, "am5", 1);
        ArrayList<ContourInterval> contourIntervals = new ArrayList<>();
        contourIntervals.add(ci);
        ContourData contourData = new ContourData(sourceBand, "am5", sourceBandName, 1);
        contourData.setContourIntervals(contourIntervals);
        contourData.setBand(sourceBand);

        ShowVectorContourOverlayAction action = new ShowVectorContourOverlayAction();
        action.setGeoCoding((GeoCoding) sourceProduct.getGeoCoding());
        ArrayList<VectorDataNode> vectorDataNodes = action.createVectorDataNodesforContours(contourData);


        for (VectorDataNode vectorDataNode : vectorDataNodes) {
            System.out.println("vector data " + vectorDataNode.toString());
            // remove the old vector data node with the same name.
            if (sourceProduct.getVectorDataGroup().contains(vectorDataNode.getName())) {
                sourceProduct.getVectorDataGroup().remove(sourceProduct.getVectorDataGroup().get(vectorDataNode.getName()));
            }
            //sourceProduct.getVectorDataGroup().add(vectorDataNode);
            productSceneView.getProduct().getVectorDataGroup().add(vectorDataNode);
            if (productSceneView != null) {
                //productSceneView.getRootLayer().getChildren().add(vectorDataNode);
                productSceneView.setLayersVisible(vectorDataNode);
            }
            final LayerFilter nodeFilter = VectorDataLayerFilterFactory.createNodeFilter(vectorDataNode);
            System.out.println("vector filter " + nodeFilter.toString());


            Layer vectorDataLayer = LayerUtils.getChildLayer(productSceneView.getRootLayer(),
                    LayerUtils.SEARCH_DEEP,
                    nodeFilter);
            List<Layer> children = productSceneView.getRootLayer().getChildren();

            for (Layer childLayer : children) {
                //System.out.println("layer height :  " + childLayer.getModelBounds().getHeight());
                System.out.println("child layer name : " + childLayer.getName());
                if (childLayer.getName().equals("Masks")) {
                    System.out.println("mask layer is visible: " + childLayer.isVisible() + " " + childLayer.getChildren().size() + " " + children.indexOf(childLayer));
                    List<Layer> masks = childLayer.getChildren();
                    for (Layer gc : masks) {
                        System.out.println(gc.getName());
                    }
                }
//                Layer grandchildren = (Layer) childLayer.getChildren();
//                if(grandchildren!=null) {
//                    System.out.println("grand child layer name : " + grandchildren.getName());
//                }
            }
            if (vectorDataLayer != null) {
                System.out.println("vector data layer is not null");
                vectorDataLayer.setVisible(true);
            } else {
                System.out.println("vector data layer is null " + vectorDataNode.toString());
            }

        }

    }

    public void addTextAnnotationLayer(ProductSceneView productSceneView) {

        Product sourceProduct = productSceneView.getProduct();

        TextAnnotationDescriptor descriptor = TextAnnotationDescriptor.getInstance();

        PixelPos pixelPos;
        GeoCoding geoCoding = sourceProduct.getGeoCoding();
        GeoPos geoPos;
        Placemark textAnnotationMark;
        Font textFont;
        Color textColor;
        Color textOutlineColor;
        int fontStyle;

        Rectangle data_bounds = sourceBand.getGeophysicalImage().getBounds();
        System.out.println("text annotations size: " + textAnnotations.length);
        for (int i = 0; i < textAnnotations.length; i++) {


//            PixelPos pixelPos = new PixelPos((float) Math.random() * data_bounds.width,
//                    (float) Math.random() * data_bounds.height);
            pixelPos = new PixelPos(textAnnotations[i].getTextAnnotationLocation()[0], textAnnotations[i].getTextAnnotationLocation()[1]);
            //pixelPos = new PixelPos((float) Math.random() * data_bounds.width, (float) Math.random() * data_bounds.height);

            geoPos = geoCoding.getGeoPos(pixelPos, null);
            textAnnotationMark = Placemark.createPointPlacemark(descriptor,
                    textAnnotations[i].getTextAnnotationName(),
                    textAnnotations[i].getTextAnnotationContent(),
                    "This won't show!",
                    pixelPos,
                    geoPos,
                    geoCoding);
            Collection<org.opengis.feature.Property> properties = textAnnotationMark.getFeature().getProperties();
//
//            for (org.opengis.feature.Property property:properties){
//                System.out.println(property.getName() + " " + property.getDescriptor() + property.getType() + property.getValue());
//
//            }

            sourceProduct.getTextAnnotationGroup().add(textAnnotationMark);

            //These can be specified in the xml file
//            Font textFont = new Font("Helvetica", Font.PLAIN, 11);
//            Color textColor = Color.YELLOW;
//            Color textOutlineColor = Color.BLACK;
            //System.out.println(textAnnotations[i].getTextAnnotationFontName() + " " + textAnnotations[i].getTextAnnotationFontStyle() + " " + textAnnotations[i].getTextAnnotationFontSize());
            //System.out.println(textAnnotations[i].getTextAnnotationFontColor()[0] + " " + textAnnotations[i].getTextAnnotationFontColor()[1] + " " + textAnnotations[i].getTextAnnotationFontColor()[2]);
            textFont = new Font(textAnnotations[i].getTextAnnotationFontName(), textAnnotations[i].getTextAnnotationFontStyle(), textAnnotations[i].getTextAnnotationFontSize());
            textColor = new Color(textAnnotations[i].getTextAnnotationFontColor()[0], textAnnotations[i].getTextAnnotationFontColor()[1], textAnnotations[i].getTextAnnotationFontColor()[2]);
            textOutlineColor = Color.BLACK;
            //System.out.println(sourceProduct.getTextAnnotationGroup().get(0).getFeature().getClass().getName() + "style: " + textAnnotationMark.getStyleCss());
            if (textAnnotationMark.getFeature() instanceof SimpleFeaturePointFigure) {
                ((SimpleFeaturePointFigure) textAnnotationMark.getFeature()).updateFontColor(textFont, textColor, textOutlineColor);
            }
            //textAnnotationMark.get

//
            //setTextAnnotationFont(productSceneView, textFont, textColor, textOutlineColor);
            //this will make the text annotation layer visible
            //setTextAnnotationFont(productSceneView, textFont, textColor, textOutlineColor);
        }
        //productSceneView.setPinOverlayEnabled(true);
    }

//    private void setTextAnnotationFont(ProductSceneView productSceneView, Font textFont, Color textColor, Color textOutlineColor) {
//        productSceneView.getRootLayer().getChildren();
//        final FigureCollection figureCollection =
//        final Figure[] figures = figureCollection.getFigures();
//        System.out.println("figure collection " + figures.length);
//        for (Figure figure : figures) {
//            System.out.println(figure.getClass().getName());
//            if (figure instanceof SimpleFeaturePointFigure) {
//                System.out.println("figure name: " + ((SimpleFeaturePointFigure) figure).getSimpleFeature().getName());
//                        ((SimpleFeaturePointFigure) figure).updateFontColor(textFont, textColor, textOutlineColor);
//            }
//        }
//
//        final SimpleFeatureFigure[] sff = productSceneView.getFeatureFigures(false);
//        System.out.println(" sinple feature figure  " + sff.length);
//        for (Figure figure:sff) {
//            System.out.println(figure.getClass().getName());
//            if (figure instanceof SimpleFeaturePointFigure) {
//                System.out.println("figure name: " + ((SimpleFeaturePointFigure) figure).getSimpleFeature().getName());
//                ((SimpleFeaturePointFigure) figure).updateFontColor(textFont, textColor, textOutlineColor);
//            }
//        }
//    }


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
        @Parameter(description = "The location to place a text annotation.")
        private int[] textAnnotationLocation;

        public TextAnnotation() {
        }

        public TextAnnotation(String textAnnotationName,
                              String textAnnotationDescription,
                              String textAnnotationContent,
                              String textAnnotationFontName,
                              int textAnnotationFontStyle,
                              int textAnnotationFontSize,
                              int[] textAnnotationFontColor,
                              int[] textAnnotationLocation) {
            this.textAnnotationName = textAnnotationName;
            this.textAnnotationDescription = textAnnotationDescription;
            this.textAnnotationContent = textAnnotationContent;
            this.textAnnotationFontName = textAnnotationFontName;
            this.textAnnotationFontStyle = textAnnotationFontStyle;
            this.textAnnotationFontSize = textAnnotationFontSize;
            this.textAnnotationFontColor = textAnnotationFontColor;
            this.textAnnotationLocation = textAnnotationLocation;
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
    }

    public static class Contour {

        @Parameter(description = "The name of the contour.")
        String contourName;
        @Parameter(description = "The value of the contour.")
        String contourValue;
        @Parameter(description = "Mask color as an RGB value. Defaults to 192,192,192.")
        private int[] contourLineColor;

        public Contour() {
        }

        public Contour(String contourName, String contourValue, int[] contourLineColor) {
            this.contourName = contourName;
            this.contourValue = contourValue;
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
    }

    public static class ImageMask {

        @Parameter(description = "Name of band that is used to create a mask. If not provided, first band in the mask product is used.")
        private String maskSourceBandName;
        @Parameter(description = "Name of the mask.")
        private String maskName;
        @Parameter(description = "Description of the mask to be added as a layer on the target image.")
        private String maskDescription;
        @Parameter(description = "BandMath value for mask creation. Leave empty if not using a mask."
                + " The value 'maskband > 1 ? 1 : 0' masks positions where the mask has values larger than 1.")
        private String maskExpression;
        @Parameter(description = "Mask color as an RGB value. Defaults to 192,192,192.")
        private int[] maskColor = DEFAULT_MASK_COLOR;
        @Parameter(description = "Transparency of the mask in the target image. Defaults to 0.")
        private double maskTransparency = DEFAULT_MASK_TRANSPARENCY;

        public ImageMask() {
        }

        public ImageMask(String maskExpression, int[] maskColor) {
            this.maskExpression = maskExpression;
            this.maskColor = maskColor;
        }

        public String getMaskExpression() {
            return maskExpression;
        }

        public void setMaskExpression(String maskExpression) {
            this.maskExpression = maskExpression;
        }

        public int[] getColor() {
            return maskColor;
        }

        public void setColor(int[] maskColor) {
            this.maskColor = maskColor;
        }

        public String getMaskSourceBandName() {
            return maskSourceBandName;
        }

        public void setMaskSourceBandName(String maskSourceBandName) {
            this.maskSourceBandName = maskSourceBandName;
        }

        public String getMaskName() {
            return maskName;
        }

        public void setMaskName(String maskName) {
            this.maskName = maskName;
        }

        public String getMaskDescription() {
            return maskDescription;
        }

        public void setMaskDescription(String maskDescription) {
            this.maskDescription = maskDescription;
        }

        public double getMaskTransparency() {
            return maskTransparency;
        }

        public void setMaskTransparency(double maskTransparency) {
            this.maskTransparency = maskTransparency;
        }
    }
}