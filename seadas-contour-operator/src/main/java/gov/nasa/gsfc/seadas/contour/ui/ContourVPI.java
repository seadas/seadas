package gov.nasa.gsfc.seadas.contour.ui;

import com.bc.ceres.glevel.MultiLevelImage;
import com.jidesoft.action.CommandBar;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
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
        Band band = product.getBand("MyAverageBand");


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
        Band band1 = product.getBand("MyAverageBand");
        PlanarImage pi = product.getBand("MyAverageBand").getSourceImage();
        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        pb.setSource("source0", band1.getGeophysicalImage());
        pb.setParameter("levels", contourIntervals);
        RenderedOp dest = JAI.create("Contour", pb);
        Collection<LineString> contours = (Collection<LineString>) dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);


        JTSFrame jtsFrame = new JTSFrame("Contours from source image");
        for (LineString contour : contours) {
            jtsFrame.addGeometry((Geometry) contour, Color.BLUE);
        }

        ImageFrame imgFrame = new ImageFrame(dest.getRendering(), "Source image");
        imgFrame.setLocation(100, 100);
        imgFrame.setVisible(true);

        Dimension size = imgFrame.getSize();
        jtsFrame.setSize(size);
        jtsFrame.setLocation(100 + size.width + 5, 100);
        jtsFrame.setVisible(true);

        return (MultiLevelImage) dest.getRendering();

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