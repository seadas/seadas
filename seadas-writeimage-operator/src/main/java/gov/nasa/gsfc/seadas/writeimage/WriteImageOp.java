package gov.nasa.gsfc.seadas.writeimage;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.accessors.DefaultPropertyAccessor;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glayer.*;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glayer.support.LayerUtils;
import com.bc.ceres.glayer.swing.LayerCanvas;
import com.bc.ceres.grender.Viewport;
import com.bc.ceres.grender.support.BufferedImageRendering;
import com.bc.ceres.grender.support.DefaultViewport;
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
import org.esa.beam.framework.ui.application.ApplicationDescriptor;
import org.esa.beam.framework.ui.application.support.DefaultApplicationDescriptor;
import org.esa.beam.framework.ui.product.ProductSceneImage;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.framework.ui.product.VectorDataLayerFilterFactory;
import org.esa.beam.glayer.ColorBarLayerType;
import org.esa.beam.glayer.GraticuleLayer;
import org.esa.beam.glayer.GraticuleLayerType;
import org.esa.beam.jai.ImageManager;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.util.PropertyMap;
import org.esa.beam.util.geotiff.GeoTIFF;
import org.esa.beam.util.geotiff.GeoTIFFMetadata;
import org.esa.beam.util.math.MathUtils;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.actions.ShowColorBarOverlayAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.jai.JAI;
import javax.media.jai.operator.BandSelectDescriptor;
import javax.swing.*;
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
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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


    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @SourceProduct(alias = "source", description = "Primary source input from which an RGB image is to be generated.")
    private Product sourceProduct;

    @TargetProduct
    private Product targetProduct;

    @Parameter(description = "Name of band containing data. If not provided, first band in the source product is used.")
    private String sourceBandName;

    @Parameter(description = "The file to which the image is written.")
    private String filePath;
    @Parameter(description = "The file to which the image 1 is written.")
    private String filePath1;

    @Parameter(description = "The file to which the image 2 is written.")
    private String filePath2;

    @Parameter(description = "The file to which the image 3 is written.")
    private String filePath3;

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


        band.getImageInfo().setNoDataColor(Color.WHITE);

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
        String imageFormat = "PNG";

        write3(imageFormat, productSceneView, entireImageSelected, file1);

//
//        ArrayList<Layer> layers = new ArrayList<>();
//
//        Layer colorBarLayer = getColorBarLayer(productSceneView);
//
//        if (colorBarLayer != null) {
//            layers.add(colorBarLayer);
//            debug.append("ColorBarLayer is added to the image \n");
//        }
//        Layer graticuleLayer = getGraticuleLayer(sourceBand);
//        if (graticuleLayer != null) {
//            layers.add(graticuleLayer);
//
//            debug.append("GraticuleLayer is added to the image \n" + graticuleLayer.getTransparency());
//        }

        productSceneView.setGraticuleOverlayEnabled(true);
//        productSceneView.getSceneImage().addLayer(0, graticuleLayer);
//        productSceneView.updateImage();


        write3(imageFormat, productSceneView, entireImageSelected, file2);

        getContourLayer(productSceneView, sourceBand);

        write3(imageFormat, productSceneView, entireImageSelected, file3);

//        RenderedImage sourceImage = createImage(imageFormat, productSceneView);
//
//        //ImageLayer finalImageLayer = addLayer(sourceImage, layers);
//        RenderedImage finalImage = addLayers(sourceImage, layers);
//
//
//        writeImage(imageFormat, finalImage, productSceneView, entireImageSelected, file3);

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
        //TODO this needs to be changed to the actual image dimension.
        Dimension dimension = new Dimension(getImageDimensions(view, entireImage));
        System.out.println("Dimension: " + dimension.width + " " + dimension.height);
        dimension = new Dimension(1002, 802);
        System.out.println("Dimension: " + dimension.width + " " + dimension.height);
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
        //vp1.setViewBounds(new Rectangle(0, 60, 2000, 2000));
        System.out.println(" vp1  " + vp1.getViewBounds().getHeight() + "   " + vp1.getViewBounds().getWidth() + " " + vp1.isModelYAxisDown());
        System.out.println("vp1 zoom factor: " + vp1.getZoomFactor());
        System.out.println("vp1 view bounds X and Y: " + vp1.getViewBounds().getX() + " " + vp1.getViewBounds().getY());
        final Viewport vp2 = new DefaultViewport(new Rectangle(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight()),
                vp1.isModelYAxisDown());
        System.out.println(" vp2  " + vp2.getViewBounds().getWidth() + "   " + vp2.getViewBounds().getHeight() + " " + vp2.isModelYAxisDown());
        System.out.println("vp2 zoom factor: " + vp2.getZoomFactor());
        System.out.println("vp2 model to view transform: " + vp2.getModelToViewTransform().toString());
        System.out.println("vp2 model to view transform: " + vp2.getViewToModelTransform().toString());
        System.out.println("vp2 view bounds X and Y: " + vp2.getViewBounds().getX() + " " + vp2.getViewBounds().getY());
        if (fullScene) {
            System.out.println("model bounds: " + view.getBaseImageLayer().getModelBounds().getWidth() + "  " + view.getBaseImageLayer().getModelBounds().getHeight());
            System.out.println("model bounds X and Y: " + view.getBaseImageLayer().getModelBounds().getX() + "  " + view.getBaseImageLayer().getModelBounds().getY());
            System.out.println("model bounds X and Y: " + view.getBaseImageLayer().getModelBounds().getCenterX() + "  " + view.getBaseImageLayer().getModelBounds().getCenterY());
            //view.getBaseImageLayer().getModelBounds().setRect(143, -28, 12, 9);
            //vp2.setViewBounds(new Rectangle(1002, 802));
            //vp2.zoom(view.getBaseImageLayer().getModelBounds());
            vp2.zoom(new Rectangle(143, -28, 15, 12));
            //vp2.setViewBounds(new Rectangle(1139, 979));
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

    private Layer getGraticuleLayer(Band band) {
        LayerType graticuleLayerType = LayerTypeRegistry.getLayerType(GraticuleLayerType.class);
        PropertySet template = graticuleLayerType.createLayerConfig(null);
        template.setValue(GraticuleLayerType.PROPERTY_NAME_RASTER, band);
        GraticuleLayer graticuleLayer = (GraticuleLayer) graticuleLayerType.createLayer(null, template);
        PropertySet propertySet = graticuleLayer.getConfiguration();

        PropertyDescriptor vd20 = new PropertyDescriptor(GraticuleLayerType.PROPERTY_NAME_TEXT_ENABLED_NORTH, Boolean.class);
        vd20.setDefaultValue(GraticuleLayerType.DEFAULT_TEXT_ENABLED_NORTH);
        vd20.setDisplayName("Show Longitude Labels - North");
        vd20.setDefaultConverter();
        Object value = vd20.getDefaultValue();
        Property editorProperty = new Property(vd20, new DefaultPropertyAccessor(value));
        propertySet.addProperty(editorProperty);

        PropertyDescriptor vd21 = new PropertyDescriptor(GraticuleLayerType.PROPERTY_NAME_TEXT_ENABLED_SOUTH, Boolean.class);
        vd21.setDefaultValue(GraticuleLayerType.DEFAULT_TEXT_ENABLED_SOUTH);
        vd21.setDisplayName("Show Longitude Labels - South");
        vd21.setDefaultConverter();
        value = vd21.getDefaultValue();
        editorProperty = new Property(vd21, new DefaultPropertyAccessor(value));
        propertySet.addProperty(editorProperty);

        PropertyDescriptor vd22 = new PropertyDescriptor(GraticuleLayerType.PROPERTY_NAME_TEXT_ENABLED_WEST, Boolean.class);
        vd22.setDefaultValue(GraticuleLayerType.DEFAULT_TEXT_ENABLED_WEST);
        vd22.setDisplayName("Show Latitude Labels - West");
        vd22.setDefaultConverter();
        value = vd22.getDefaultValue();
        editorProperty = new Property(vd22, new DefaultPropertyAccessor(value));
        propertySet.addProperty(editorProperty);

        PropertyDescriptor vd23 = new PropertyDescriptor(GraticuleLayerType.PROPERTY_NAME_TEXT_ENABLED_EAST, Boolean.class);
        vd23.setDefaultValue(GraticuleLayerType.DEFAULT_TEXT_ENABLED_EAST);
        vd23.setDisplayName("Show Latitude Labels - East");
        vd23.setDefaultConverter();
        value = vd23.getDefaultValue();
        editorProperty = new Property(vd23, new DefaultPropertyAccessor(value));
        propertySet.addProperty(editorProperty);

        return graticuleLayer;
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

//    private Layer getMaskLayer(Band band){
//        MaskLayerType maskLayerType = LayerTypeRegistry.getLayerType(MaskLayerType.class);
//        PropertySet configuration = maskLayerType.createLayerConfig(null);
//        Mask.BandMathsType.create("coast", "", band.getSceneRasterWidth(), band.getSceneRasterHeight(), "l1_flags.COASTLINE", Color.black, 0);
//        Mask coastlineMask = maskLayerType.createLayer(ctx, configuration);
//        configuration.setValue(MaskLayerType.PROPERTY_NAME_MASK, coastlineMask);
//        Layer coastlineLayer = maskLayerType.createLayer(null, configuration);
//        return coastlineLayer;
//    }

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

    private void getContourLayer(ProductSceneView productSceneView, Band sourceBand) {
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

            // remove the old vector data node with the same name.
            if (sourceProduct.getVectorDataGroup().contains(vectorDataNode.getName())) {
                sourceProduct.getVectorDataGroup().remove(sourceProduct.getVectorDataGroup().get(vectorDataNode.getName()));
            }
            sourceProduct.getVectorDataGroup().add(vectorDataNode);
            if (productSceneView != null) {
                //productSceneView.getRootLayer().getChildren().add(vectorDataNode);
                productSceneView.setLayersVisible(vectorDataNode);
            }
            final LayerFilter nodeFilter = VectorDataLayerFilterFactory.createNodeFilter(vectorDataNode);
            Layer vectorDataLayer = LayerUtils.getChildLayer(productSceneView.getRootLayer(),
                    LayerUtils.SEARCH_DEEP,
                    nodeFilter);
            if (vectorDataLayer != null) {
                System.out.println("vector data layer is not null");
                vectorDataLayer.setVisible(true);
            } else {
                System.out.println("vector data layer is null "  + vectorDataNode.toString());
            }

        }

    }

    private int toInteger(double value) {
        return MathUtils.floorInt(value);
    }
}