/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package gov.nasa.gsfc.seadas;

import com.bc.ceres.core.*;
import com.jidesoft.action.CommandBar;
import com.jidesoft.action.CommandMenuBar;
import com.jidesoft.action.DockableBarContext;
import com.jidesoft.action.DockableBar;
import org.esa.beam.framework.datamodel.PlacemarkDescriptor;
import org.esa.beam.framework.datamodel.PlacemarkDescriptorRegistry;
import org.esa.beam.framework.ui.application.ApplicationDescriptor;
import org.esa.beam.framework.ui.application.ToolViewDescriptor;
import org.esa.beam.framework.ui.command.Command;
import org.esa.beam.framework.ui.command.CommandManager;
import org.esa.beam.framework.ui.command.ToolCommand;
import org.esa.beam.framework.ui.tool.ToolButtonFactory;
import org.esa.beam.visat.ProductsToolView;
import org.esa.beam.visat.VisatActivator;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.actions.CreateSubsetFromViewAction;
import org.esa.beam.visat.actions.ShowToolBarAction;
import org.esa.beam.visat.toolviews.diag.TileCacheDiagnosisToolView;
import org.esa.beam.visat.toolviews.imageinfo.ColorManipulationToolView;
import org.esa.beam.visat.toolviews.layermanager.LayerManagerToolView;
import org.esa.beam.visat.toolviews.mask.MaskManagerToolView;
import org.esa.beam.visat.toolviews.nav.NavigationToolView;
import org.esa.beam.visat.toolviews.pixelinfo.PixelInfoToolView;
import org.esa.beam.visat.toolviews.placemark.PlacemarkEditorToolView;
import org.esa.beam.visat.toolviews.placemark.gcp.GcpManagerToolView;
import org.esa.beam.visat.toolviews.placemark.pin.PinManagerToolView;
import org.esa.beam.visat.toolviews.spectrum.SpectrumToolView;
import org.esa.beam.visat.toolviews.stat.*;
import org.esa.beam.visat.toolviews.worldmap.WorldMapToolView;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * The <code>SeadasApp</code> class represents the SeaDAS UI application.
 *
 * @author Don
 */
public class SeadasApp extends VisatApp {

    public static final String SEADAS_DEFAULT_TOOL_BAR_ID = "seadasDefaultToolBar";
    public static final String SEADAS_EXTRAS_TOOL_BAR_ID = "seadasExtrasToolBar";
    public static final String SEADAS_INTERACTIONS_TOOL_BAR_ID = "seadasInteractionsToolBar";
    public static final String SEADAS_INTERACTIONS_EXTRAS_TOOL_BAR_ID = "seadasInteractionsExtrasToolBar";
    public static final String SEADAS_GEOMETRY_TOOL_BAR_ID = "seadasGeometryToolBar";
    public static final String SEADAS_PINS_TOOL_BAR_ID = "seadasPinsToolBar";
    public static final String SEADAS_GCP_TOOL_BAR_ID = "seadasGcpToolBar";
    public static final String SEADAS_ANALYSIS_TOOL_BAR_ID = "seadasAnalysisToolBar";
    public static final String SEADAS_WEST_DOCK_TOOL_BAR_ID = "seadasWestDockToolBar";
    public static final String SEADAS_BAND_TOOLS_TOOL_BAR_ID = "seadasBandToolsToolBar";

    private final int PADDING = 10;


    private static final String SHOW_TOOLVIEW_CMD_POSTFIX = ".showCmd";

    /**
     * Constructs the SeaDAS UI application instance. The constructor does not start the application nor does it perform any GUI
     * work.
     *
     * @param applicationDescriptor The application descriptor.
     */
    public SeadasApp(ApplicationDescriptor applicationDescriptor) {
        super(applicationDescriptor);
        // SystemUtils.BEAM_HOME_PAGE = "http://seadas.gsfc.nasa.gov/";
    }


    /**
     * Overrides the base class version in order to create a tool bar for VISAT.
     */
    @Override
    protected CommandBar createMainToolBar() {
        final CommandBar toolBar = createToolBar(MAIN_TOOL_BAR_ID, "File");
        addCommandsToToolBar(toolBar, new String[]{
                "open"
        });

        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }


    @Override
    protected CommandBar createLayersToolBar() {
        final CommandBar toolBar = createToolBar(LAYERS_TOOL_BAR_ID, "Layers");
        toolBar.add(Box.createHorizontalStrut(PADDING));
        ArrayList<String> commandIdList = new ArrayList<String>(Arrays.asList(
                "showNoDataOverlay",
//                "showShapeOverlay",
                "showGraticuleOverlay",
                "showWorldMapOverlay"));
        Set<PlacemarkDescriptor> placemarkDescriptors = PlacemarkDescriptorRegistry.getInstance().getPlacemarkDescriptors();
        for (PlacemarkDescriptor placemarkDescriptor : placemarkDescriptors) {
            if (placemarkDescriptor.getShowLayerCommandId() != null) {
                String s1 = placemarkDescriptor.getBaseFeatureType().getName().getLocalPart();
                String s2 = placemarkDescriptor.getBaseFeatureType().getName().toString();


                if (!placemarkDescriptor.getBaseFeatureType().getName().getLocalPart().contains("GroundControlPoint") &&
                        !placemarkDescriptor.getBaseFeatureType().getName().getLocalPart().contains("Pin")) {
                    commandIdList.add(placemarkDescriptor.getShowLayerCommandId());
                }
            }
        }
        addCommandsToToolBar(toolBar, commandIdList.toArray(new String[0]));


        return toolBar;
    }

    //    @Override
//    protected CommandBar createAnalysisToolBar() {
//        final CommandBar toolBar = createToolBar(ANALYSIS_TOOL_BAR_ID, "Analysis");
//        addCommandsToToolBar(toolBar, new String[]{
//                StatisticsToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                HistogramPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                DensityPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                ScatterPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                ProfilePlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                SpectrumToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX
//        });
//        return toolBar;
//    }
    protected CommandBar createSeadasBandToolsToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_BAND_TOOLS_TOOL_BAR_ID, "Band Tools");

        toolBar.add(Box.createHorizontalStrut(PADDING));

        addCommandsToToolBar(toolBar, new String[]{
                "bandArithmetic",
                "createFilteredBand"
        });

        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }


    protected CommandBar createSeadasDefaultToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_DEFAULT_TOOL_BAR_ID, "Proc");

        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                "createSubsetFromView",
                "mosaicAction",
                "collocation"
        });

        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }


    protected CommandBar createSeadasExtrasToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_EXTRAS_TOOL_BAR_ID, "SeaDAS Preferred");

        addCommandsToToolBar(toolBar, new String[]{
//                "open",
//                null,
//                "createSubsetFromView",
//                "mosaicAction",
//                "collocation",
                "createFilteredBand"
//
//                "bandArithmetic"
        });
//
        ArrayList<String> commandIdList = new ArrayList<String>(Arrays.asList(
                "showNoDataOverlay"
//                "showShapeOverlay",
                //             "showGraticuleOverlay",
                //              "showWorldMapOverlay"
        ));
        Set<PlacemarkDescriptor> placemarkDescriptors = PlacemarkDescriptorRegistry.getInstance().getPlacemarkDescriptors();
        for (PlacemarkDescriptor placemarkDescriptor : placemarkDescriptors) {
            if (placemarkDescriptor.getShowLayerCommandId() != null) {
                String s1 = placemarkDescriptor.getBaseFeatureType().getName().getLocalPart();
                String s2 = placemarkDescriptor.getBaseFeatureType().getName().toString();


                if (!placemarkDescriptor.getBaseFeatureType().getName().getLocalPart().contains("GroundControlPoint") &&
                        !placemarkDescriptor.getBaseFeatureType().getName().getLocalPart().contains("Pin")) {
                    commandIdList.add(placemarkDescriptor.getShowLayerCommandId());
                }
            }
        }

        addCommandsToToolBar(toolBar, commandIdList.toArray(new String[0]));

//        addCommandsToToolBar(toolBar, new String[]{
//                //        StatisticsToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                //       HistogramPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                //        DensityPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                ScatterPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                ProfilePlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                SpectrumToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                PinManagerToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX
//                //        ColorManipulationToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX
//        });


        return toolBar;
    }


//    @Override
//    protected CommandBar[] createViewsToolBars() {
//
//        final HashSet<String> excludedIds = new HashSet<>(15);
//        // todo - remove bad forward dependencies to tool views (nf - 30.10.2008)
//        excludedIds.add(TileCacheDiagnosisToolView.ID);
//        excludedIds.add(ProductsToolView.ID);
////        excludedIds.add(ColorManipulationToolView.ID);
//        excludedIds.add(NavigationToolView.ID);
//        excludedIds.add(MaskManagerToolView.ID);
//        excludedIds.add(GcpManagerToolView.ID);
//        excludedIds.add(PinManagerToolView.ID);
//        excludedIds.add(PixelInfoToolView.ID);
//        excludedIds.add(SpectrumToolView.ID);
//        excludedIds.add(WorldMapToolView.ID);
//        excludedIds.add(PlacemarkEditorToolView.ID);
//        excludedIds.add(InformationToolView.ID);
//        excludedIds.add(GeoCodingToolView.ID);
//        excludedIds.add(StatisticsToolView.ID);
//        excludedIds.add(HistogramPlotToolView.ID);
//        excludedIds.add(ScatterPlotToolView.ID);
//        excludedIds.add(DensityPlotToolView.ID);
//        excludedIds.add(ProfilePlotToolView.ID);
//        excludedIds.add("org.esa.beam.scripting.visat.ScriptConsoleToolView");
//        excludedIds.add("org.esa.beam.visat.toolviews.layermanager.LayerEditorToolView");
//
//
//        ToolViewDescriptor[] toolViewDescriptors = VisatActivator.getInstance().getToolViewDescriptors();
//
//        Map<String, List<String>> toolBar2commandIds = new HashMap<>();
//        for (ToolViewDescriptor toolViewDescriptor : toolViewDescriptors) {
//            if (!excludedIds.contains(toolViewDescriptor.getId())) {
//                final String commandId = toolViewDescriptor.getId() + ".showCmd";
//
//                String toolBarId = toolViewDescriptor.getToolBarId();
//                if (toolBarId == null || toolBarId.isEmpty()) {
//                    toolBarId = VIEWS_TOOL_BAR_ID;
//                }
//
//                List<String> commandIds = toolBar2commandIds.get(toolBarId);
//                if (commandIds == null) {
//                    commandIds = new ArrayList<String>(5);
//                    toolBar2commandIds.put(toolBarId, commandIds);
//                }
//                if (!toolViewDescriptor.getId().contains("LayerManagerToolView")) {
//                    commandIds.add(commandId);
//                }
//            }
//        }
//
//        List<CommandBar> viewToolBars = new ArrayList<>(5);
//        CommandBar viewsToolBar = createToolBar(VIEWS_TOOL_BAR_ID, "Views");
//        viewToolBars.add(viewsToolBar);
//        for (String toolBarId : toolBar2commandIds.keySet()) {
//            CommandBar toolBar = getToolBar(toolBarId);
//            if (toolBar == null) {
//                // todo - use ToolBarDescriptor to define tool bar properties, e.g. title, dockSite, ...  (nf - 20090119)
//                toolBar = createToolBar(toolBarId, toolBarId.replace('.', ' ').replace('_', ' '));
//                viewToolBars.add(toolBar);
//
//                // 	Retrospectively add "tool bar toggle" menu item
//                ShowToolBarAction action = new ShowToolBarAction(toolBarId + ".showToolBar");
//                action.setText(toolBarId);
//                action.setContexts(new String[]{toolBarId});
//                action.setToggle(true);
//                action.setSelected(true);
//                getCommandManager().addCommand(action);
//                JMenu toolBarsMenu = findMenu("toolBars");
//                toolBarsMenu.add(action.createMenuItem());
//            }
//            List<String> commandIds = toolBar2commandIds.get(toolBarId);
//            addCommandsToToolBar(toolBar, commandIds.toArray(new String[commandIds.size()]));
//        }
//
//        addCommandsToToolBar(viewsToolBar, new String[]{
//                StatisticsToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                HistogramPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                DensityPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                ScatterPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                ProfilePlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                SpectrumToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX
//        });
//
//        return viewToolBars.toArray(new CommandBar[viewToolBars.size()]);
//    }

    @Override
    protected CommandBar createInteractionsToolBar() {
        final CommandBar toolBar = super.createInteractionsToolBar();

        toolBar.setTitle("Image Interactions");
        toolBar.getContext().setInitMode(DockableBarContext.STATE_HIDDEN);
        return toolBar;
    }


    protected CommandBar createSeadasInteractionsToolBar() {
        //      final CommandBar toolBar = super.createInteractionsToolBar();

        final CommandBar toolBar = createToolBar(SEADAS_INTERACTIONS_TOOL_BAR_ID, "Image Interactions");

        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                "selectTool",
                "pannerTool",
                "zoomTool"
        });


        toolBar.add(Box.createHorizontalStrut(PADDING));
        toolBar.add(Box.createHorizontalStrut(PADDING));

        addCommandsToToolBar(toolBar, new String[]{
                "magicWandTool",
                "rangeFinder"
        });

        toolBar.add(Box.createHorizontalStrut(PADDING));

        toolBar.setTitle("Image Interactions");
        return toolBar;
    }

    protected CommandBar createSeadasInteractionsExtrasToolBar() {
        //      final CommandBar toolBar = super.createInteractionsToolBar();

        final CommandBar toolBar = createToolBar(SEADAS_INTERACTIONS_EXTRAS_TOOL_BAR_ID, "Image Interactions");
        addCommandsToToolBar(toolBar, new String[]{
                // These IDs are defined in the module.xml
//                "selectTool",
//                "pannerTool",
//                "zoomTool",
//                "magicWandTool",
//                "createVectorDataNode",
//                "drawRectangleTool",
//                "drawEllipseTool",
//                "drawPolygonTool",
                "drawLineTool",
                "drawPolylineTool",
//                "insertWktAsGeometry",
//                "pinTool",
                "gcpTool",
                "rangeFinder"
        });

        //      addCommandsToToolBar(toolBar, new String[]{"insertWktAsGeometry"});
        toolBar.setTitle("Image Interactions");
        return toolBar;
    }


    protected CommandBar createSeadasGeometryToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_GEOMETRY_TOOL_BAR_ID, "Geometries");

        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                "createVectorDataNode",
                "drawRectangleTool",
                "drawEllipseTool",
                "drawPolygonTool",
                "drawLineTool",
                "drawPolylineTool",
                "insertWktAsGeometry"

        });
        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }


    protected CommandBar createSeadasPinsToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_PINS_TOOL_BAR_ID, "Pins");

        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                PinManagerToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                "pinTool"
        });
        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }

    protected CommandBar createSeadasGCPToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_GCP_TOOL_BAR_ID, "Ground Control Points");

        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                GcpManagerToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                "gcpTool"
        });
        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }

    protected void addCommandsToToolBar2(final CommandBar toolBar, final int width, final String[] commandIDs) {
        for (final String commandID : commandIDs) {
            if (commandID == null) {
                toolBar.add(ToolButtonFactory.createToolBarSeparator());
            } else {
                final Command command = getCommandManager().getCommand(commandID);
                if (command != null) {
                    final AbstractButton toolBarButton = command.createToolBarButton();
                    toolBarButton.addMouseListener(getMouseOverActionHandler());
                    toolBar.add(toolBarButton);
                } else {
                    getLogger().warning(String.format("Toolbar '%s': No command found for ID = '%s'", toolBar.getName(),
                            commandID));
                }
            }
            toolBar.add(Box.createHorizontalStrut(width));
        }
    }

    protected CommandBar createSeadasWestDockToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_WEST_DOCK_TOOL_BAR_ID, "West Dock");

        String layerManagerToolViewCommandId = null;
        ToolViewDescriptor[] toolViewDescriptors = VisatActivator.getInstance().getToolViewDescriptors();
        for (ToolViewDescriptor toolViewDescriptor : toolViewDescriptors) {
            if (toolViewDescriptor.getId().contains("LayerManagerToolView")) {
                layerManagerToolViewCommandId = new String(toolViewDescriptor.getId() + ".showCmd");
            }
        }

        toolBar.add(Box.createHorizontalStrut(PADDING));

        addCommandsToToolBar2(toolBar, 24, new String[]{
                ProductsToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                MaskManagerToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                layerManagerToolViewCommandId,
                ColorManipulationToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX
        });

        return toolBar;
    }


    protected CommandBar createSeadasAnalysisToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_ANALYSIS_TOOL_BAR_ID, "Analysis");
        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                StatisticsToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                HistogramPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                DensityPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                ScatterPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                ProfilePlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                SpectrumToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX
        });
        toolBar.add(Box.createHorizontalStrut(PADDING));
        return toolBar;
    }


    @Override
    protected void loadCommands() {
        super.loadCommands();

        List<Command> commands = VisatActivator.getInstance().getCommands();
        for (Command command : commands) {
            if ("pannerTool".equals(command.getCommandID())) {
                ToolCommand toolCommand = (ToolCommand) command;
                selectionInteractor = toolCommand.getInteractor();
                setActiveInteractor(selectionInteractor);
                toolCommand.setSelected(true);
            }
        }
    }

    @Override
    protected CommandBar createMainMenuBar() {
        final CommandMenuBar menuBar = new CommandMenuBar("Main Menu");
        menuBar.setHidable(false);
        menuBar.setStretch(true);


        menuBar.add(createJMenu("file", "File", 'F'));
        menuBar.add(createJMenu("edit", "Edit", 'E'));
        menuBar.add(createJMenu("view", "View", 'V'));

        menuBar.add(createJMenu("tools", "Tools", 'U'));
        menuBar.add(createJMenu("processing", "Proc", 'P'));
        menuBar.add(createJMenu("ocprocessing", "OCproc", 'O'));

        menuBar.add(createJMenu("analysis", "Analysis", 'A'));
        menuBar.add(createJMenu("info", "Info", 'A'));


        menuBar.add(createJMenu("window", "Window", 'W'));
        menuBar.add(createJMenu("help", "Help", 'H'));

        return menuBar;
    }


    @Override
    protected void initClientUI(com.bc.ceres.core.ProgressMonitor pm) {
        try {
            pm.beginTask(String.format("Initialising %s UI components", getAppName()), 5);

            getMainFrame().getDockableBarManager().setRearrangable(false);
            List<String> namesT = getMainFrame().getDockableBarManager().getAllDockableBarNames();

            DockableBar fileDockableBar = getMainFrame().getDockableBarManager().getDockableBar("mainToolBar");
            fileDockableBar.setFloatable(false);
            fileDockableBar.setOpaque(false);
            fileDockableBar.setStretch(false);
            fileDockableBar.setRearrangable(false);
            fileDockableBar.setHidable(false);
            fileDockableBar.setInitIndex(2);
            fileDockableBar.setInitSubindex(0);


            CommandBar analysisToolBar = createAnalysisToolBar();
            analysisToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            analysisToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(analysisToolBar);
            analysisToolBar.getContext().setInitMode(DockableBarContext.STATE_HIDDEN);
            pm.worked(1);


            CommandBar seadasDefaultToolBar = createSeadasDefaultToolBar();
            seadasDefaultToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasDefaultToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasDefaultToolBar);
            pm.worked(1);


            CommandBar seadasBandToolsToolBar = createSeadasBandToolsToolBar();
            seadasBandToolsToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasBandToolsToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasBandToolsToolBar);
            pm.worked(1);

            CommandBar layersToolBar = createLayersToolBar();
            layersToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            layersToolBar.getContext().setInitIndex(2);

            getMainFrame().getDockableBarManager().addDockableBar(layersToolBar);
//            layersToolBar.getContext().setInitMode(DockableBarContext.STATE_HIDDEN);
            pm.worked(1);
//

            CommandBar seadasAnalysisToolBar = createSeadasAnalysisToolBar();
            seadasAnalysisToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasAnalysisToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasAnalysisToolBar);
            pm.worked(1);


            CommandBar seadasWestDockToolBar = createSeadasWestDockToolBar();
            seadasWestDockToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_SOUTH);
            seadasWestDockToolBar.add(Box.createHorizontalStrut(50));
            DockableBar westDockableBar = seadasWestDockToolBar;
            westDockableBar.setFloatable(false);
            westDockableBar.setOpaque(false);
            westDockableBar.setStretch(false);
            westDockableBar.setRearrangable(false);
            westDockableBar.setHidable(false);
            westDockableBar.setInitIndex(0);
            westDockableBar.setInitSubindex(0);

            getMainFrame().getDockableBarManager().addDockableBar(westDockableBar);


            pm.worked(1);

            CommandBar seadasInteractionsToolBar = createSeadasInteractionsToolBar();
            seadasInteractionsToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_SOUTH);
            seadasInteractionsToolBar.add(Box.createHorizontalStrut(PADDING));

            DockableBar seadasInteractionsDockableBar = seadasInteractionsToolBar;
            seadasInteractionsDockableBar.setFloatable(false);
            seadasInteractionsDockableBar.setOpaque(false);
            seadasInteractionsDockableBar.setStretch(false);
            seadasInteractionsDockableBar.setRearrangable(true);
            seadasInteractionsDockableBar.setHidable(false);
            seadasInteractionsDockableBar.setInitIndex(0);
            seadasInteractionsDockableBar.setInitSubindex(0);

            getMainFrame().getDockableBarManager().addDockableBar(seadasInteractionsDockableBar);
            pm.worked(1);


            CommandBar seadasGeometriesToolBar = createSeadasGeometryToolBar();
            seadasGeometriesToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_SOUTH);
            getMainFrame().getDockableBarManager().addDockableBar(seadasGeometriesToolBar);
            pm.worked(1);


            CommandBar seadasPinsToolBar = createSeadasPinsToolBar();
            seadasPinsToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_SOUTH);
            getMainFrame().getDockableBarManager().addDockableBar(seadasPinsToolBar);
            pm.worked(1);

            CommandBar seadasGcpToolBar = createSeadasGCPToolBar();
            seadasGcpToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_SOUTH);
            getMainFrame().getDockableBarManager().addDockableBar(seadasGcpToolBar);
            pm.worked(1);


//            com.jidesoft.action.DockableBarContainer container = getMainFrame().getDockableBarManager().createDockableBarContainer();
//            container.getDockableBarManager().addDockableBar(seadasGeometriesToolBar);
//            container.getDockableBarManager().addDockableBar(seadasPinsToolBar);
//            container.getDockableBarManager().addDockableBar(seadasGcpToolBar);
//            getMainFrame().getDockableBarManager().addDockableBar(container.);


            getMainFrame().setPreferredSize(new Dimension(1000, 500));


//
//            CommandBar[] viewToolBars = createViewsToolBars();
//            for (CommandBar viewToolBar : viewToolBars) {
//                if (VIEWS_TOOL_BAR_ID.equals(viewToolBar.getName())) {
//                    viewToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
//                    viewToolBar.getContext().setInitMode(DockableBarContext.STATE_HIDDEN);
//
//                    viewToolBar.getContext().setInitIndex(2);
//                } else {
//                    viewToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
//                    viewToolBar.getContext().setInitMode(DockableBarContext.STATE_HIDDEN);
//                    viewToolBar.getContext().setInitIndex(2);
//                }
//
//                getMainFrame().getDockableBarManager().addDockableBar(viewToolBar);
//            }
//
//            CommandBar toolsToolBar = createInteractionsToolBar();
//            toolsToolBar.setMinimumSize(new Dimension(500, 500));
//            toolsToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
//            toolsToolBar.getContext().setInitIndex(2);
//            pm.worked(1);
//


            registerForMacOSXEvents();
            pm.worked(1);

//            getMainToolBar().getContext().setInitMode(DockableBarContext.STATE_HIDDEN);
//            getStatusBar().setEnabled(false);
//            getStatusBar().setVisible(false);

            int count = 0;
            for (Component component : getStatusBar().getComponents()) {
                if (count > 0) {
                    component.setVisible(false);
                }
                count++;
            }


        } finally {
            pm.done();
        }
    }

}




