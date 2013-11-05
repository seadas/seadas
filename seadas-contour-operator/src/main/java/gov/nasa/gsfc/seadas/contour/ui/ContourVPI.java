package gov.nasa.gsfc.seadas.contour.ui;

import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import com.jidesoft.action.CommandBar;
import gov.nasa.gsfc.seadas.contour.operator.ContourOp;
import gov.nasa.gsfc.seadas.contour.util.ResourceInstallationUtils;
import gov.nasa.gsfc.seadas.watermask.ui.SimpleDialogMessage;
import gov.nasa.gsfc.seadas.watermask.ui.SourceFileInfo;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.ui.command.CommandAdapter;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.visat.AbstractVisatPlugIn;
import org.esa.beam.visat.VisatApp;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.FormatDescriptor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.RenderedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


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
        if (product != null) {
            final ProductNodeGroup<Mask> maskGroup = product.getMaskGroup();
            final ProductNodeGroup<Band> bandGroup = product.getBandGroup();

            /*
               A simple boolean switch to enable this to run with or without the intermediate user dialogs
            */
            boolean useDialogs = true;

            final ContourData contourData = new ContourData();


            if (!useDialogs) {
                contourData.setCreateMasks(true);
            }


            /*
                Determine whether these auxilliary masks and associated products have already be created.
                This would be the case when run a second time on the same product.
            */

            final boolean[] masksCreated = {false};

            for (String name : maskGroup.getNodeNames()) {
                if (name.equals(contourData.getMaskName())) {
                    masksCreated[0] = true;
                }
            }


            for (String name : bandGroup.getNodeNames()) {
                if (name.equals(contourData.getContourBandName())) {
                    masksCreated[0] = true;
                }
            }

            /*
               For the case where this is being run a second time, prompt the user to determine whether to delete
               and re-create the products and masks.
            */

            if (masksCreated[0]) {
                if (useDialogs) {
                    contourData.setDeleteMasks(false);
                    ContourDialog contourDialog = new ContourDialog(contourData, masksCreated[0], product);
                    contourDialog.setVisible(true);
                    contourDialog.dispose();
                }

                if (contourData.isDeleteMasks() || !useDialogs) {
                    masksCreated[0] = false;

                    for (String name : maskGroup.getNodeNames()) {
                        if (name.equals(contourData.getMaskName())) {
                            maskGroup.remove(maskGroup.get(name));
                        }
                    }
                    for (String name : bandGroup.getNodeNames()) {
                        if (name.equals(contourData.getContourBandName())) {
                            bandGroup.remove(bandGroup.get(name));
                        }
                    }
                }
            }


            if (!masksCreated[0]) {
                if (useDialogs) {
                    contourData.setCreateMasks(false);
                    ContourDialog contourDialog = new ContourDialog(contourData, masksCreated[0], product);
                    contourDialog.setVisible(true);
                }

                if (contourData.isCreateMasks()) {
                    final SourceFileInfo sourceFileInfo = contourData.getSourceFileInfo();

                    if (sourceFileInfo.isEnabled()) {

                        ProgressMonitorSwingWorker pmSwingWorker = new ProgressMonitorSwingWorker(visatApp.getMainFrame(),
                                "Creating contour band and mask") {

                            @Override
                            protected Void doInBackground(com.bc.ceres.core.ProgressMonitor pm) throws Exception {

                                pm.beginTask("Creating contour band and mask", 2);

                                try {
                                    Map<String, Object> parameters = new HashMap<String, Object>();

                                    parameters.put("subSamplingFactorX", new Integer(contourData.getSuperSampling()));
                                    parameters.put("subSamplingFactorY", new Integer(contourData.getSuperSampling()));
                                    parameters.put("resolution", sourceFileInfo.getResolution(SourceFileInfo.Unit.METER));
                                    parameters.put("filename", sourceFileInfo.getFile().getName());

                                    /*
                                       Create a new product, which will contain the contour band, then add this band to current product.
                                    */

                                    Product contourProduct = GPF.createProduct(CONTOUR_PRODUCT_NAME, parameters, product);
                                    Band contourBand = contourProduct.getBand(ContourOp.CONTOUR_BAND_NAME);
                                    reformatSourceImage(contourBand, new ImageLayout(product.getBandAt(0).getSourceImage()));
                                    pm.worked(1);
                                    contourBand.setName(contourData.getContourBandName());

                                    product.addBand(contourBand);

                                    String maskMath = contourData.getMaskMath();

                                    Mask contourMask = Mask.BandMathsType.create(
                                            contourData.getMaskName(),
                                            contourData.getMaskDescription(),
                                            product.getSceneRasterWidth(),
                                            product.getSceneRasterHeight(),
                                            contourData.getMaskMath(),
                                            contourData.getMaskColor(),
                                            contourData.getMaskTransparency());
                                    maskGroup.add(contourMask);

                                    pm.worked(1);

                                    String[] bandNames = product.getBandNames();
                                    for (String bandName : bandNames) {
                                        RasterDataNode raster = product.getRasterDataNode(bandName);
                                        if (contourData.isShowMaskAllBands()) {
                                            raster.getOverlayMaskGroup().add(contourMask);
                                        }
                                    }


                                } finally {
                                    pm.done();
                                }
                                return null;
                            }
                        };

                        pmSwingWorker.executeWithBlocking();

                    } else {
                        SimpleDialogMessage dialog = new SimpleDialogMessage(null, "Cannot Create Masks: Resolution File Doesn't Exist");
                        dialog.setVisible(true);
                        dialog.setEnabled(true);

                    }
                }
            }
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

        ArrayList<Double> contourIntervals = new ArrayList<Double>();

        for (double level = 0.2; level < 1.41; level += 0.2) {
            contourIntervals.add(level);
        }
        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        pb.setSource("source0", band.getSourceImage());
        pb.setParameter("levels", contourIntervals);
        RenderedOp dest = JAI.create("Contour", pb);
//        Collection<LineString> contours = (Collection<LineString>) dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);
//
//        JTSFrame jtsFrame = new JTSFrame("Contours from source image");
//        for (LineString contour : contours) {
//            jtsFrame.addGeometry(contour, Color.BLUE);
//        }
//
//        ImageFrame imgFrame = new ImageFrame(image, "Source image");
//        imgFrame.setLocation(100, 100);
//        imgFrame.setVisible(true);
//
//        Dimension size = imgFrame.getSize();
//        jtsFrame.setSize(size);
//        jtsFrame.setLocation(100 + size.width + 5, 100);
//        jtsFrame.setVisible(true);
        return product.getBand("chlor_a").getSourceImage();
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