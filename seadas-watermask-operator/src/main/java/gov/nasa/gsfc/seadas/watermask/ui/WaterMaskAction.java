package gov.nasa.gsfc.seadas.watermask.ui;

import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.rcp.actions.AbstractSnapAction;
import org.esa.snap.rcp.imgfilter.model.Filter;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.operator.FormatDescriptor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.RenderedImage;
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
 */
public class WaterMaskAction extends AbstractSnapAction {

    public static final String COMMAND_ID = "Coastline, Land & Water";
    public static final String TOOL_TIP = "Add coastline, land and water masks";
    //    public static final String ICON = "/org/esa/beam/watermask/ui/icons/coastline_24.png";
    //  public static final String ICON = "icons/Coastline24.png";
    public static final String ICON = "coastline_24.png";

    public static final String LAND_WATER_MASK_OP_ALIAS = "LandWaterMask";
    public static final String TARGET_TOOL_BAR_NAME = "layersToolBar";


    private void showLandWaterCoastMasks(final SnapApp snapApp) {

        final Product product = snapApp.getSelectedProduct(SnapApp.SelectionSourceHint.AUTO);
        if (product != null) {
            final ProductNodeGroup<Mask> maskGroup = product.getMaskGroup();
            final ProductNodeGroup<Band> bandGroup = product.getBandGroup();

            /*
               A simple boolean switch to enable this to run with or without the intermediate user dialogs
            */

            // todo for Danny: turn this boolean on when it is ready
            boolean useDialogs = true;

            final LandMasksData landMasksData = new LandMasksData();


            if (!useDialogs) {
                landMasksData.setCreateMasks(true);
            }


            /*
                Determine whether these auxilliary masks and associated products have already be created.
                This would be the case when run a second time on the same product.
            */

            final boolean[] masksCreated = {false};

            for (String name : maskGroup.getNodeNames()) {
                if (name.equals(landMasksData.getCoastlineMaskName()) ||
                        name.equals(landMasksData.getLandMaskName()) ||
                        name.equals(landMasksData.getWaterMaskName())) {
                    masksCreated[0] = true;
                }
            }


            for (String name : bandGroup.getNodeNames()) {
                if (name.equals(landMasksData.getWaterFractionBandName()) ||
                        name.equals(landMasksData.getWaterFractionSmoothedName())) {
                    masksCreated[0] = true;
                }
            }

            /*
                For the case where this is being run a second time, prompt the user to determine whether to delete
                and re-create the products and masks.
             */

            if (masksCreated[0]) {
                if (useDialogs) {
                    landMasksData.setDeleteMasks(false);
                    LandMasksDialog landMasksDialog = new LandMasksDialog(landMasksData, masksCreated[0]);
                    landMasksDialog.setVisible(true);
                    landMasksDialog.dispose();

                }

                if (landMasksData.isDeleteMasks() || !useDialogs) {
                    masksCreated[0] = false;


                    for (String name : maskGroup.getNodeNames()) {
                        if (name.equals(landMasksData.getCoastlineMaskName()) ||
                                name.equals(landMasksData.getLandMaskName()) ||
                                name.equals(landMasksData.getWaterMaskName())) {
                            maskGroup.remove(maskGroup.get(name));
                        }
                    }

                    for (String name : bandGroup.getNodeNames()) {
                        if (name.equals(landMasksData.getWaterFractionBandName()) ||
                                name.equals(landMasksData.getWaterFractionSmoothedName())) {
                            bandGroup.remove(bandGroup.get(name));
                        }
                    }


                }
            }


            if (!masksCreated[0]) {
                if (useDialogs) {
                    landMasksData.setCreateMasks(false);
                    LandMasksDialog landMasksDialog = new LandMasksDialog(landMasksData, masksCreated[0]);
                    landMasksDialog.setVisible(true);
                }

                if (landMasksData.isCreateMasks()) {
                    final SourceFileInfo sourceFileInfo = landMasksData.getSourceFileInfo();

                    if (sourceFileInfo.isEnabled()) {

                        ProgressMonitorSwingWorker pmSwingWorker = new ProgressMonitorSwingWorker(snapApp.getMainFrame(),
                                "Computing Masks") {

                            @Override
                            protected Void doInBackground(com.bc.ceres.core.ProgressMonitor pm) throws Exception {

                                pm.beginTask("Creating land, water, coastline masks", 2);

                                try {
                                    //  Product landWaterProduct = GPF.createProduct("LandWaterMask", GPF.NO_PARAMS, product);


                                    Map<String, Object> parameters = new HashMap<String, Object>();

                                    parameters.put("superSamplingFactor", new Integer(landMasksData.getSuperSampling()));
                                   // parameters.put("subSamplingFactorY", new Integer(landMasksData.getSuperSampling()));
                                    parameters.put("resolution", sourceFileInfo.getResolution(SourceFileInfo.Unit.METER));
                                    parameters.put("mode", sourceFileInfo.getMode().toString());
                                    parameters.put("worldSourceDataFilename", sourceFileInfo.getFile().getName());
                                    parameters.put("copySourceFile", "false");  // when run in GUI don't do this
                                //    parameters.put("coastalGridSize", landMasksData.getCoastalGridSize());
                                //    parameters.put("coastalSizeTolerance", landMasksData.getCoastalSizeTolerance());
                                    parameters.put("includeMasks", false);  // don't create masks within the operator, do it later
                                    //                             parameters.put("sourceFileInfo", sourceFileInfo);
                                    /*
                                       Create a new product, which will contain the land_water_fraction band
                                    */


                                    Product landWaterProduct = GPF.createProduct(LAND_WATER_MASK_OP_ALIAS, parameters, product);


                                    Band waterFractionBand = landWaterProduct.getBand(landMasksData.getWaterFractionBandName());
                                //    Band coastBand = landWaterProduct.getBand("coast");

                                    // PROBLEM WITH TILE SIZES
                                    // Example: product has tileWidth=498 and tileHeight=611
                                    // resulting image has tileWidth=408 and tileHeight=612
                                    // Why is this happening and where?
                                    // For now we change the image layout here.
                                    reformatSourceImage(waterFractionBand, new ImageLayout(product.getBandAt(0).getSourceImage()));
                              //      reformatSourceImage(coastBand, new ImageLayout(product.getBandAt(0).getSourceImage()));

                                    pm.worked(1);
                                    waterFractionBand.setName(landMasksData.getWaterFractionBandName());

                                    product.addBand(waterFractionBand);

                                    //todo BEAM folks left this as a placeholder
//                    product.addBand(coastBand);

                                    //todo replace with JAI operator "GeneralFilter" which uses a GeneralFilterFunction


                                    int boxSize = landMasksData.getCoastalGridSize();
                                    final Filter meanFilter = new Filter("Mean "+ Integer.toString(boxSize)+"x"+Integer.toString(boxSize), "mean"+Integer.toString(boxSize), Filter.Operation.MEAN, boxSize, boxSize);
                                    final Kernel meanKernel = new Kernel(meanFilter.getKernelWidth(),
                                            meanFilter.getKernelHeight(),
                                            meanFilter.getKernelOffsetX(),
                                            meanFilter.getKernelOffsetY(),
                                            1.0 / meanFilter.getKernelQuotient(),
                                            meanFilter.getKernelElements());


//                                    final Kernel arithmeticMean3x3Kernel = new Kernel(3, 3, 1.0 / 9.0,
//                                            new double[]{
//                                                    +1, +1, +1,
//                                                    +1, +1, +1,
//                                                    +1, +1, +1,
//                                            });
//todo: 4th argument to ConvolutionFilterBand is a dummy value added to make it compile...may want to look at this...
                                    int count = 1;
//                                    final ConvolutionFilterBand filteredCoastlineBand = new ConvolutionFilterBand(
//                                            landMasksData.getWaterFractionSmoothedName(),
//                                            waterFractionBand,
//                                            arithmeticMean3x3Kernel, count);

                                    final FilterBand filteredCoastlineBand = new GeneralFilterBand(landMasksData.getWaterFractionSmoothedName(), waterFractionBand, GeneralFilterBand.OpType.MEAN, meanKernel, count);
                                    if (waterFractionBand instanceof Band) {
                                        ProductUtils.copySpectralBandProperties((Band) waterFractionBand, filteredCoastlineBand);
                                    }



                                    product.addBand(filteredCoastlineBand);


                                    Mask coastlineMask = Mask.BandMathsType.create(
                                            landMasksData.getCoastlineMaskName(),
                                            landMasksData.getCoastlineMaskDescription(),
                                            product.getSceneRasterWidth(),
                                            product.getSceneRasterHeight(),
                                            landMasksData.getCoastalMath(),
                                            landMasksData.getCoastlineMaskColor(),
                                            landMasksData.getCoastlineMaskTransparency());
                                    maskGroup.add(coastlineMask);


                                    Mask landMask = Mask.BandMathsType.create(
                                            landMasksData.getLandMaskName(),
                                            landMasksData.getLandMaskDescription(),
                                            product.getSceneRasterWidth(),
                                            product.getSceneRasterHeight(),
                                            landMasksData.getLandMaskMath(),
                                            landMasksData.getLandMaskColor(),
                                            landMasksData.getLandMaskTransparency());

                                    maskGroup.add(landMask);

                                    
                                    Mask waterMask = Mask.BandMathsType.create(
                                            landMasksData.getWaterMaskName(),
                                            landMasksData.getWaterMaskDescription(),
                                            product.getSceneRasterWidth(),
                                            product.getSceneRasterHeight(),
                                            landMasksData.getWaterMaskMath(),
                                            landMasksData.getWaterMaskColor(),
                                            landMasksData.getWaterMaskTransparency());
                                    maskGroup.add(waterMask);





                                    pm.worked(1);

                                    String[] bandNames = product.getBandNames();
                                    for (String bandName : bandNames) {
                                        RasterDataNode raster = product.getRasterDataNode(bandName);
                                        if (landMasksData.isShowCoastlineMaskAllBands()) {
                                            raster.getOverlayMaskGroup().add(coastlineMask);
                                        }
                                        if (landMasksData.isShowLandMaskAllBands()) {
                                            raster.getOverlayMaskGroup().add(landMask);
                                        }
                                        if (landMasksData.isShowWaterMaskAllBands()) {
                                            raster.getOverlayMaskGroup().add(waterMask);
                                        }
                                    }


//                    snapApp.setSelectedProductNode(waterFractionBand);

//        ProductSceneView selectedProductSceneView = snapApp.getSelectedProductSceneView();
//        if (selectedProductSceneView != null) {
//            RasterDataNode raster = selectedProductSceneView.getRaster();
//            raster.getOverlayMaskGroup().add(landMask);
//            raster.getOverlayMaskGroup().add(coastlineMask);
//            raster.getOverlayMaskGroup().add(waterMask);

//        }


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
        MultiLevelImage waterFractionSourceImage = band.getSourceImage();
        int waterFractionDataType = waterFractionSourceImage.getData().getDataBuffer().getDataType();
        RenderedImage newImage = FormatDescriptor.create(waterFractionSourceImage, waterFractionDataType,
                renderingHints);
        band.setSourceImage(newImage);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }


//    @Override
//    public void start(final SnapApp snapApp) {
//        final ExecCommand action = snapApp.getCommandManager().createExecCommand(COMMAND_ID,
//                new ToolbarCommand(snapApp));
//
//        String iconFilename = ResourceInstallationUtils.getIconFilename(ICON, WaterMaskAction.class);
//        //  action.setLargeIcon(UIUtils.loadImageIcon(ICON));
//        try {
//            URL iconUrl = new URL(iconFilename);
//            ImageIcon imageIcon = new ImageIcon(iconUrl);
//            action.setLargeIcon(imageIcon);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//
//
//        final AbstractButton lwcButton = snapApp.createToolButton(COMMAND_ID);
//        lwcButton.setToolTipText(TOOL_TIP);
//
//        final AbstractButton lwcButton2 = snapApp.createToolButton(COMMAND_ID);
//        lwcButton2.setToolTipText(TOOL_TIP);
//
//        snapApp.getMainFrame().addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowOpened(WindowEvent e) {
//                CommandBar layersBar = snapApp.getToolBar(TARGET_TOOL_BAR_NAME);
//                if (layersBar != null) {
//                    layersBar.add(lwcButton);
//                }
//
//
//                CommandBar seadasDefaultBar = snapApp.getToolBar("seadasDeluxeToolsToolBar");
//                if (seadasDefaultBar != null) {
//                    seadasDefaultBar.add(lwcButton2);
//                }
//            }
//
//        });
//    }
//
//
//    private class ToolbarCommand extends CommandAdapter {
//        private final SnapApp snapApp;
//
//        public ToolbarCommand(SnapApp snapApp) {
//            this.snapApp = snapApp;
//        }
//
//        @Override
//        public void actionPerformed(
//                CommandEvent event) {
//            showLandWaterCoastMasks(
//                    snapApp);
//
//        }
//
//        @Override
//        public void updateState(
//                CommandEvent event) {
//            Product selectedProduct = snapApp.getSelectedProduct();
//            boolean productSelected = selectedProduct != null;
//            boolean hasBands = false;
//            boolean hasGeoCoding = false;
//            if (productSelected) {
//                hasBands = selectedProduct.getNumBands() > 0;
//                hasGeoCoding = selectedProduct.getGeoCoding() != null;
//            }
//            event.getCommand().setEnabled(
//                    productSelected && hasBands && hasGeoCoding);
//        }
//    }
}

