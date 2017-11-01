package gov.nasa.gsfc.seadas.bathymetry.ui;

import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import com.jidesoft.action.CommandBar;
import gov.nasa.gsfc.seadas.bathymetry.util.ResourceInstallationUtils;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.ui.command.CommandAdapter;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.visat.AbstractVisatPlugIn;
import org.esa.beam.visat.VisatApp;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.operator.FormatDescriptor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import gov.nasa.gsfc.seadas.bathymetry.operator.BathymetryOp;
import gov.nasa.gsfc.seadas.bathymetry.ui.BathymetryData;


/**
 * This VISAT PlugIn registers an action which calls the "bathymetry" Operator
 *
 * @author Danny Knowles
 */
public class BathymetryVPI extends AbstractVisatPlugIn {

    public static final String COMMAND_ID = "Bathymetry & Elevation";
    public static final String TOOL_TIP = "Add bathymetry-elevation band and mask";
    public static final String ICON = "bathymetry.png";

    public static final String TARGET_TOOL_BAR_NAME = "layersToolBar";
    public static final String BATHYMETRY_PRODUCT_NAME = "BathymetryOp";


    @Override
    public void start(final VisatApp visatApp) {
        final ExecCommand action = visatApp.getCommandManager().createExecCommand(COMMAND_ID,
                new ToolbarCommand(visatApp));

        String iconFilename = ResourceInstallationUtils.getIconFilename(ICON, BathymetryVPI.class);

        try {
            URL iconUrl = new URL(iconFilename);
            ImageIcon imageIcon = new ImageIcon(iconUrl);
            action.setLargeIcon(imageIcon);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


//        final AbstractButton lwcButton = visatApp.createToolButton(COMMAND_ID);
//        lwcButton.setToolTipText(TOOL_TIP);
//
//        visatApp.getMainFrame().addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowOpened(WindowEvent e) {
//                CommandBar layersBar = visatApp.getToolBar(TARGET_TOOL_BAR_NAME);
//                layersBar.add(lwcButton);
//            }
//        });

        final AbstractButton lwcButton = visatApp.createToolButton(COMMAND_ID);
        lwcButton.setToolTipText(TOOL_TIP);

        final AbstractButton lwcButton2 = visatApp.createToolButton(COMMAND_ID);
        lwcButton2.setToolTipText(TOOL_TIP);

        visatApp.getMainFrame().addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                CommandBar layersBar = visatApp.getToolBar(TARGET_TOOL_BAR_NAME);
                if (layersBar != null) {
                    layersBar.add(lwcButton);
                }


                CommandBar seadasDefaultBar = visatApp.getToolBar("seadasDeluxeToolsToolBar");
                if (seadasDefaultBar != null) {
                    seadasDefaultBar.add(lwcButton2);
                }
            }

        });


    }


    private void showBathymetry(final VisatApp visatApp) {
        final Product product = visatApp.getSelectedProduct();
        if (product != null) {
            final ProductNodeGroup<Mask> maskGroup = product.getMaskGroup();
            final ProductNodeGroup<Band> bandGroup = product.getBandGroup();

            /*
               A simple boolean switch to enable this to run with or without the intermediate user dialogs
            */
            boolean useDialogs = true;

            final BathymetryData bathymetryData = new BathymetryData();


            if (!useDialogs) {
                bathymetryData.setCreateMasks(true);
            }


            /*
                Determine whether these auxilliary masks and associated products have already be created.
                This would be the case when run a second time on the same product.
            */

            final boolean[] masksCreated = {false};
            final boolean[] bandCreated = {false};

            for (String name : maskGroup.getNodeNames()) {
                if (name.equals(bathymetryData.getMaskName())) {
                    masksCreated[0] = true;
                }
            }


            for (String name : bandGroup.getNodeNames()) {
                if (name.equals(BathymetryOp.ELEVATION_BAND_NAME)) {
                    masksCreated[0] = true;
                }
            }

            /*
                For the case where this is being run a second time, prompt the user to determine whether to delete
                and re-create the products and masks.
             */


            if (masksCreated[0]) {
                bandCreated[0] = true;

                if (useDialogs) {
                    bathymetryData.setDeleteMasks(false);
                    BathymetryDialog bathymetryDialog = new BathymetryDialog(bathymetryData, masksCreated[0], bandCreated[0]);
                    bathymetryDialog.setVisible(true);
                    bathymetryDialog.dispose();
                }

                if (bathymetryData.isDeleteMasks() || !useDialogs) {
                    masksCreated[0] = false;


//                    for (String name : bandGroup.getNodeNames()) {
//                        if (
//                                name.equals(bathymetryData.getBathymetryBandName())) {
//  //                          Band bathymetryBand = bandGroup.get(name);
//
////                            product.getBand(name).dispose();
//
//                            bandGroup.remove(bandGroup.get(name));
////                            product.removeBand(bathymetryBand);
//
//                        }
//                    }

                    for (String name : maskGroup.getNodeNames()) {
                        if (name.equals(bathymetryData.getMaskName())) {
//                            maskGroup.get(name).dispose();
                            maskGroup.remove(maskGroup.get(name));
                        }
                    }


                }
            }


            if (!masksCreated[0]) {
                if (useDialogs) {
                    bathymetryData.setCreateMasks(false);
                    BathymetryDialog bathymetryDialog = new BathymetryDialog(bathymetryData, masksCreated[0], bandCreated[0]);
                    bathymetryDialog.setVisible(true);
                }

                if (bathymetryData.isCreateMasks()) {
                    final SourceFileInfo sourceFileInfo = bathymetryData.getSourceFileInfo();


                    if (sourceFileInfo.isEnabled() && sourceFileInfo.getExistingFile() != null) {


                        final String[] msg = {"Creating bathymetry band and mask"};

                        if (bandCreated[0] == true) {
                            msg[0] = "recreating bathymetry mask";
                        }
                        ProgressMonitorSwingWorker pmSwingWorker = new ProgressMonitorSwingWorker(visatApp.getMainFrame(),
                                msg[0]) {

                            @Override
                            protected Void doInBackground(com.bc.ceres.core.ProgressMonitor pm) throws Exception {

                                pm.beginTask(msg[0], 2);

                                try {

                                    if (bandCreated[0] != true) {
                                        Map<String, Object> parameters = new HashMap<String, Object>();

                                        parameters.put("resolution", sourceFileInfo.getResolution(SourceFileInfo.Unit.METER));
                                        parameters.put("filename", sourceFileInfo.getExistingFile().getName());

                                        /*
                                           Create a new product, which will contain the bathymetry band, then add this band to current product.
                                        */

                                        Product bathymetryProduct = GPF.createProduct(BATHYMETRY_PRODUCT_NAME, parameters, product);


                                        Band topographyBand = bathymetryProduct.getBand(BathymetryOp.TOPOGRAPHY_BAND_NAME);
                                        reformatSourceImage(topographyBand, new ImageLayout(product.getBandAt(0).getSourceImage()));
                                        topographyBand.setName(BathymetryOp.TOPOGRAPHY_BAND_NAME);
                                        product.addBand(topographyBand);


                                        Band bathymetryBand = bathymetryProduct.getBand(BathymetryOp.BATHYMETRY_BAND_NAME);
                                        reformatSourceImage(bathymetryBand, new ImageLayout(product.getBandAt(0).getSourceImage()));
                                        bathymetryBand.setName(BathymetryOp.BATHYMETRY_BAND_NAME);
                                        product.addBand(bathymetryBand);


                                        Band elevationBand = bathymetryProduct.getBand(BathymetryOp.ELEVATION_BAND_NAME);
                                        reformatSourceImage(elevationBand, new ImageLayout(product.getBandAt(0).getSourceImage()));
                                        elevationBand.setName(BathymetryOp.ELEVATION_BAND_NAME);
                                        product.addBand(elevationBand);


                                        pm.worked(1);
                                    }


                                    Mask bathymetryMask = Mask.BandMathsType.create(
                                            bathymetryData.getMaskName(),
                                            bathymetryData.getMaskDescription(),
                                            product.getSceneRasterWidth(),
                                            product.getSceneRasterHeight(),
                                            bathymetryData.getMaskMath(),
                                            bathymetryData.getMaskColor(),
                                            bathymetryData.getMaskTransparency());
                                    maskGroup.add(bathymetryMask);

                                    pm.worked(1);

                                    String[] bandNames = product.getBandNames();
                                    for (String bandName : bandNames) {
                                        RasterDataNode raster = product.getRasterDataNode(bandName);
                                        if (bathymetryData.isShowMaskAllBands()) {
                                            raster.getOverlayMaskGroup().add(bathymetryMask);
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
        Raster r = sourceImage.getData();
        DataBuffer db = r.getDataBuffer();
        int t = db.getDataType();
        int dataType = sourceImage.getData().getDataBuffer().getDataType();
        RenderedImage newImage = FormatDescriptor.create(sourceImage, dataType, renderingHints);
        band.setSourceImage(newImage);
    }

    private class ToolbarCommand extends CommandAdapter {
        private final VisatApp visatApp;

        public ToolbarCommand(VisatApp visatApp) {
            this.visatApp = visatApp;
        }

        @Override
        public void actionPerformed(
                CommandEvent event) {
            showBathymetry(visatApp);

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

