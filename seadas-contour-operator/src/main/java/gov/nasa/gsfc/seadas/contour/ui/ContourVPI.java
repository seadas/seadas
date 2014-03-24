package gov.nasa.gsfc.seadas.contour.ui;

import com.bc.ceres.glevel.MultiLevelImage;
import com.jidesoft.action.CommandBar;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import gov.nasa.gsfc.seadas.ContourDescriptor;
import gov.nasa.gsfc.seadas.contour.util.ResourceInstallationUtils;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Mask;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductNodeGroup;
import org.esa.beam.framework.ui.command.CommandAdapter;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.visat.AbstractVisatPlugIn;
import org.esa.beam.visat.VisatApp;
import org.jaitools.swing.ImageFrame;
import org.jaitools.swing.JTSFrame;

import javax.media.jai.*;
import javax.media.jai.operator.FormatDescriptor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.RenderedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;


/**
 * This VISAT PlugIn registers an action which calls the "LandWaterMask" Operator and based on its generated "water_fraction"
 * band, defines 3 masks in the currently selected product:
 * <ol>
 * <li>Water: water_fraction > 90</li>
 * <li>Land: water_fraction < 10</li>
 * <li>Coastline: water_fraction > 0.1 && water_fraction < 99.9 (meaning if water_fraction is not 0 or 100 --> it is a coastline)</li>
 * </ol>
 * <p/>
 * <p/>
 * <i>IMPORTANT Note:
 * This VISAT PlugIn is a workaround.
 * Ideally, users would register an action in BEAM's {@code module.xml} and specify a target toolbar for it.
 * Actions specified in BEAM's {@code module.xml} currently only appear in menus, and not in tool bars
 * (because they are hard-coded in VisatApp).
 * Since this feature is still missing in BEAM, so we have to place the action in its target tool bar
 * ("layersToolBar") manually.</i>
 *
 * @author Tonio Fincke
 * @author Danny Knowles
 * @author Marco Peters
 * @author Aynur Abdurazik
 */
public class ContourVPI extends AbstractVisatPlugIn {

    public static final String COMMAND_ID = "CONTOUR LINES";
    public static final String TOOL_TIP = "Add contour lines to a given band";
    public static final String ICON = "contour_button.gif";
    //public static final String ICON = "contour_web.png";

    public static final String TARGET_TOOL_BAR_NAME = "layersToolBar";
    public static final String CONTOUR_PRODUCT_NAME = "contour";


    @Override
    public void start(final VisatApp visatApp) {
        final ExecCommand action = visatApp.getCommandManager().createExecCommand(COMMAND_ID,
                new ToolbarCommand(visatApp));

        String iconFilename = ResourceInstallationUtils.getIconFilename(ICON, ContourVPI.class);
        //ParameterBlockJAI pbtest = new ParameterBlockJAI("Contour");
        try {
            URL iconUrl = new URL(iconFilename);
            ImageIcon imageIcon = new ImageIcon(iconUrl);
            action.setLargeIcon(imageIcon);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        final AbstractButton lwcButton = visatApp.createToolButton(COMMAND_ID);
        lwcButton.setToolTipText(TOOL_TIP);

        visatApp.getMainFrame().addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                CommandBar layersBar = visatApp.getToolBar(TARGET_TOOL_BAR_NAME);
                layersBar.add(lwcButton);
            }
        });
    }


    private void showContour(final VisatApp visatApp) {

        final Product product = visatApp.getSelectedProduct();
        Band band = product.getBand("chlor_a");


        if (product != null) {
            final ProductNodeGroup<Mask> maskGroup = product.getMaskGroup();
            final ProductNodeGroup<Band> bandGroup = product.getBandGroup();


            ContourDialog contourDialog = new ContourDialog(product);
            contourDialog.setVisible(true);
            contourDialog.dispose();
            band.setSourceImage(getContourImage(product, band));
        }
    }


    private void reformatSourceImage(Band band, ImageLayout imageLayout) {
        RenderingHints renderingHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, imageLayout);
        MultiLevelImage sourceImage = band.getSourceImage();
        int dataType = sourceImage.getData().getDataBuffer().getDataType();
        RenderedImage newImage = FormatDescriptor.create(sourceImage, dataType, renderingHints);
        band.setSourceImage(newImage);
    }

    private MultiLevelImage getContourImage(Product product, Band band) {

        JAI.setDefaultTileSize(new Dimension(512, 512));
        ArrayList<Double> contourIntervals = new ArrayList<Double>();

        for (double level = 1; level < 10; level += 2) {
            contourIntervals.add(level);
        }

        //Contour1Spi cspi = new Contour1Spi();

        //cspi.updateRegistry(JAI.getDefaultInstance().getOperationRegistry());

        OperationRegistry or = JAI.getDefaultInstance().getOperationRegistry();
        String modeName = "rendered";
        String[] descriptorNames;

        for (String name : or.getDescriptorNames(modeName)) {
            System.out.println(name);
        }
        Band band1 = product.getBand("chlor_a");
        PlanarImage pi = product.getBand("chlor_a").getSourceImage();
        MultiLevelImage mli = band1.getSourceImage();
        TiledImage ti = new TiledImage(pi.getMinX(), pi.getMinY(), pi.getWidth(), pi.getHeight(), pi.getTileGridXOffset(), pi.getTileGridYOffset(), pi.getSampleModel(), pi.getColorModel());
        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        pb.setSource("source0", band1.getGeophysicalImage());
        pb.setParameter("levels", contourIntervals);
        RenderedOp dest = JAI.create("Contour", pb);
        Collection<LineString> contours = (Collection<LineString>) dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);

        //BufferedImage g = ti.getAsBufferedImage();     //ti.createGraphics();
//        Graphics2D g2d = g.createGraphics();
//        //final Viewport vp = pi.getViewport();
//        final AffineTransform transformSave = g2d.getTransform();
//        try {
//            final AffineTransform transform = new AffineTransform();
//            transform.concatenate(transformSave);
//            //transform.concatenate(vp.getModelToViewTransform());
//            transform.concatenate(((MultiLevelImage) pi).getModel().getImageToModelTransform(0));
//            g2d.setTransform(transform);
//            g2d.setColor(Color.ORANGE);
//            for (LineString contour : contours) {
//                Coordinate[] coordinates = contour.getCoordinates();
//                int numCoor = coordinates.length;
//                for (int i = 0; i < numCoor - 1; i++) {
//                    g2d.draw(new Line2D.Double(coordinates[i].x, coordinates[i].y, coordinates[i+1].x, coordinates[i+1].y));
//                }
//            }
////            final GeneralPath[] linePaths = graticule.getLinePaths();
////            if (linePaths != null) {
////                drawLinePaths(g2d, linePaths);
////            }
//        } finally {
//            g2d.setTransform(transformSave);
//        }
        JTSFrame jtsFrame = new JTSFrame("Contours from source image");
        for (LineString contour : contours) {
            jtsFrame.addGeometry((Geometry) contour, Color.BLUE);
            Point p = contour.getPointN(0);
            contour.getLength();
        }
        ImageFrame imgFrame = new ImageFrame(dest.getRendering(), "Source image");
        imgFrame.setLocation(100, 100);
        imgFrame.setVisible(true);

        Dimension size = imgFrame.getSize();
        jtsFrame.setSize(size);
        jtsFrame.setLocation(100 + size.width + 5, 100);
        jtsFrame.setVisible(true);

//        jtsFrame.update(g2d);
//        jtsFrame.setLocation(100 + size.width + 15, 100);
//        jtsFrame.setVisible(true);

        return (MultiLevelImage) dest.getRendering();

    }

    private void drawLinePaths(Graphics2D g2d, final GeneralPath[] linePaths) {
        Composite oldComposite = null;
        if (getLineTransparency() > 0.0) {
            oldComposite = g2d.getComposite();
            g2d.setComposite(getAlphaComposite(getLineTransparency()));
        }
        g2d.setPaint(getLineColor());
        g2d.setStroke(new BasicStroke((float) getLineWidth()));
        for (GeneralPath linePath : linePaths) {
            g2d.draw(linePath);
        }
        if (oldComposite != null) {
            g2d.setComposite(oldComposite);
        }
    }

//    private void updateContours() {
//        FigureEditor figureEditor = getFigureEditor(event);
//        figureEditor.getFigureSelection().removeAllFigures();
//        Point2D referencePoint = toModelPoint(event);
//
//        Path2D linePath = new Path2D.Double();
//        linePath.moveTo(referencePoint.getX(), referencePoint.getY());
//        linePath.lineTo(referencePoint.getX(), referencePoint.getY());
//
//        Figure figure = figureEditor.getFigureFactory().createLineFigure(linePath, figureEditor.getDefaultLineStyle());
//        figureEditor.getFigureCollection().addFigure(figure);
//    }

    private Color getLineColor() {
        //return getConfigurationProperty(GraticuleLayerType.PROPERTY_NAME_LINE_COLOR,
        //                                 GraticuleLayerType.DEFAULT_LINE_COLOR);
        return Color.green;
    }

    private double getLineTransparency() {
        //return getConfigurationProperty(GraticuleLayerType.PROPERTY_NAME_LINE_TRANSPARENCY,
        //                                GraticuleLayerType.DEFAULT_LINE_TRANSPARENCY);
        return 0.5;
    }

    private double getLineWidth() {
        //return getConfigurationProperty(GraticuleLayerType.PROPERTY_NAME_LINE_WIDTH,
        //                                GraticuleLayerType.DEFAULT_LINE_WIDTH);
        return 1.0;
    }

    private AlphaComposite getAlphaComposite(double itemTransparancy) {
        double combinedAlpha = (1.0 - getTransparency()) * (1.0 - itemTransparancy);
        return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) combinedAlpha);
    }

    private double getTransparency() {
        return 0.0;
    }

    private class ToolbarCommand extends CommandAdapter {
        private final VisatApp visatApp;

        public ToolbarCommand(VisatApp visatApp) {
            this.visatApp = visatApp;
        }

        @Override
        public void actionPerformed(
                CommandEvent event) {
            showContour(visatApp);

        }

        @Override
        public void updateState(CommandEvent event) {
            Product selectedProduct = visatApp.getSelectedProduct();
            boolean productSelected = selectedProduct != null;
            boolean hasBands = false;
            boolean hasGeoCoding = false;
            if (productSelected) {
                hasBands = selectedProduct.getNumBands() > 0;
                hasGeoCoding = selectedProduct.getGeoCoding() != null;
            }
            event.getCommand().setEnabled(productSelected && hasBands && hasGeoCoding);
        }
    }
}