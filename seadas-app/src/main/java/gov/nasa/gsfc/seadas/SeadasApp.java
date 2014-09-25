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

import com.bc.ceres.swing.actions.*;
import com.bc.ceres.swing.selection.SelectionManager;
import com.jidesoft.action.CommandBar;
import com.jidesoft.action.CommandMenuBar;
import com.jidesoft.action.DockableBar;
import com.jidesoft.action.DockableBarContext;
import org.esa.beam.framework.datamodel.PlacemarkDescriptor;
import org.esa.beam.framework.datamodel.PlacemarkDescriptorRegistry;
import org.esa.beam.framework.ui.application.ApplicationDescriptor;
import org.esa.beam.framework.ui.application.ToolViewDescriptor;
import org.esa.beam.framework.ui.command.Command;
import org.esa.beam.framework.ui.command.ToolCommand;
import org.esa.beam.framework.ui.tool.ToolButtonFactory;
import org.esa.beam.visat.ProductsToolView;
import org.esa.beam.visat.VisatActivator;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.toolviews.imageinfo.ColorManipulationToolView;
import org.esa.beam.visat.toolviews.mask.MaskManagerToolView;
import org.esa.beam.visat.toolviews.nav.NavigationToolView;
import org.esa.beam.visat.toolviews.pixelinfo.PixelInfoToolView;
import org.esa.beam.visat.toolviews.placemark.gcp.GcpManagerToolView;
import org.esa.beam.visat.toolviews.placemark.pin.PinManagerToolView;
import org.esa.beam.visat.toolviews.spectrum.SpectrumToolView;
import org.esa.beam.visat.toolviews.stat.*;
import org.esa.beam.visat.toolviews.worldmap.WorldMapToolView;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * The <code>SeadasApp</code> class represents the SeaDAS UI application.
 *
 * @author Don
 */
public class SeadasApp extends VisatApp {

    public static final String MAIN_MENU_TOOL_BAR_TITLE = "Main Menu";
    public static final String SEADAS_PROC_TOOL_BAR_ID = "seadasProcToolBar";
    public static final String SEADAS_BAND_TOOLS_TOOL_BAR_ID = "seadasBandToolsToolBar";
    public static final String SEADAS_INTERACTIONS_TOOL_BAR_ID = "seadasInteractionsToolBar";
    public static final String SEADAS_ANALYSIS_TOOL_BAR_ID = "seadasAnalysisToolBar";
    public static final String SEADAS_GEOMETRY_TOOL_BAR_ID = "seadasGeometriesToolBar";
    public static final String SEADAS_PINS_TOOL_BAR_ID = "seadasPinsToolBar";
    public static final String SEADAS_GCP_TOOL_BAR_ID = "seadasGcpToolBar";
    public static final String SEADAS_WEST_DOCK_TOOL_BAR_ID = "seadasWestDockToolBar";
    public static final String SEADAS_EAST_DOCK_TOOL_BAR_ID = "seadasEastDockToolBar";
    public static final String SEADAS_BLANK_TOOL_BAR_ID = "seadasBlankToolBar";

    public static final String SEADAS_INTERACTIONS_EXTRAS_TOOL_BAR_ID = "seadasInteractionsExtrasToolBar";


    public static final String SEADAS_STANDARD_LAYERS_TOOL_BAR_ID = "seadasStandardLayersToolBar";
    public static final String SEADAS_VECTOR_LAYERS_TOOL_BAR_ID = "seadasVectorLayersToolBar";
    public static final String SEADAS_DELUXE_TOOLS_TOOL_BAR_ID = "seadasDeluxeToolsToolBar";
    public static final String SEADAS_FILE_TOOL_BAR_ID = MAIN_TOOL_BAR_ID;
    public static final String BEAM_MAIN_TOOL_BAR_ID = "beamMainToolBar";  // gave it a different toolbar and I am hijacking MAIN_TOOL_BAR_ID for the seadasMain


    private final int PADDING = 1;


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
        toolBar.add(Box.createHorizontalStrut(3));
        addCommandsToToolBar(toolBar, new String[]{
                "open"
        });

        return toolBar;
    }

//    protected CommandBar createSeadasFileToolBar() {
//        final CommandBar toolBar = createToolBar(SEADAS_FILE_TOOL_BAR_ID, "File");
//        addCommandsToToolBar(toolBar, new String[]{
//                "open"
//        });
//
//        toolBar.add(Box.createHorizontalStrut(PADDING));
//
//        return toolBar;
//    }


    protected CommandBar createBeamMainToolBar() {
        final CommandBar toolBar = createToolBar(BEAM_MAIN_TOOL_BAR_ID, "BEAM: Standard");
        addCommandsToToolBar(toolBar, new String[]{
//                "new",
                "open",
                "save",
                null,
                "preferences",
                "properties",
                null,
                "showUpdateDialog",
                "helpTopics",
        });
        return toolBar;
    }


    protected CommandBar createSeadasDeluxeToolsToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_DELUXE_TOOLS_TOOL_BAR_ID, "Masks");
//        toolBar.add(Box.createHorizontalStrut(PADDING));
//        ArrayList<String> commandIdList = new ArrayList<String>(Arrays.asList(
//                "showContourOverlay"));
//
//        addCommandsToToolBar(toolBar, commandIdList.toArray(new String[0]));
//        toolBar.add(Box.createHorizontalStrut(PADDING));

        addCommandsToToolBar(toolBar, new String[]{
                "magicWandTool"
        });


        return toolBar;
    }


    protected CommandBar createSeadasStandardLayersToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_STANDARD_LAYERS_TOOL_BAR_ID, "Layers");

        String layerEditorToolViewCommandId = null;
        ToolViewDescriptor[] toolViewDescriptors = VisatActivator.getInstance().getToolViewDescriptors();
        for (ToolViewDescriptor toolViewDescriptor : toolViewDescriptors) {
            if (toolViewDescriptor.getId().contains("LayerEditorToolView")) {
                layerEditorToolViewCommandId = new String(toolViewDescriptor.getId() + ".showCmd");
            }
        }

//        toolBar.add(Box.createHorizontalStrut(PADDING));
        ArrayList<String> commandIdList = new ArrayList<String>(Arrays.asList(
                layerEditorToolViewCommandId,
                "showWorldMapOverlay",
                "showNoDataOverlay",
                "exportLegendImageFile",
                "showGraticuleOverlay",
                "showContourOverlay"
               ));

        addCommandsToToolBar(toolBar, commandIdList.toArray(new String[0]));
//        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }

    protected CommandBar createSeadasVectorLayersToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_VECTOR_LAYERS_TOOL_BAR_ID, "Vector Layers");
//        toolBar.add(Box.createHorizontalStrut(PADDING));
        ArrayList<String> commandIdList = new ArrayList<String>(Arrays.asList(
                "showShapeOverlay"));

        Set<PlacemarkDescriptor> placemarkDescriptors = PlacemarkDescriptorRegistry.getInstance().getPlacemarkDescriptors();
        for (PlacemarkDescriptor placemarkDescriptor : placemarkDescriptors) {
            if (placemarkDescriptor.getShowLayerCommandId() != null) {
                String s1 = placemarkDescriptor.getBaseFeatureType().getName().getLocalPart();
                String s2 = placemarkDescriptor.getBaseFeatureType().getName().toString();


//                if (!placemarkDescriptor.getBaseFeatureType().getName().getLocalPart().contains("GroundControlPoint") &&
//                        !placemarkDescriptor.getBaseFeatureType().getName().getLocalPart().contains("Pin")) {
                commandIdList.add(placemarkDescriptor.getShowLayerCommandId());
//                }
            }
        }

        addCommandsToToolBar(toolBar, commandIdList.toArray(new String[0]));
//        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }


    protected CommandBar createSeadasBandToolsToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_BAND_TOOLS_TOOL_BAR_ID, "Band Tools");

//        toolBar.add(Box.createHorizontalStrut(PADDING));

        addCommandsToToolBar(toolBar, new String[]{
                "bandArithmetic",
                "createFilteredBand"
        });

//        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }


    protected CommandBar createSeadasProcToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_PROC_TOOL_BAR_ID, "Proc");

//        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                "createSubsetFromView",
                "mosaicAction",
                "collocation"
        });

//        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }


    protected CommandBar createSeadasInteractionsExtrasToolBar() {
        //      final CommandBar toolBar = super.createInteractionsToolBar();

        final CommandBar toolBar = createToolBar(SEADAS_INTERACTIONS_EXTRAS_TOOL_BAR_ID, "Image Interactions (Extras)");

//        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                "drawLineTool",
                "drawPolylineTool",
                "rangeFinder"
        });

//        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }


    protected CommandBar createSeadasInteractionsToolBar() {
        //      final CommandBar toolBar = super.createInteractionsToolBar();

        final CommandBar toolBar = createToolBar(SEADAS_INTERACTIONS_TOOL_BAR_ID, "Image Interactions");
//        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                "selectTool",
                "pannerTool",
                "zoomTool"
        });


        toolBar.add(Box.createHorizontalStrut(PADDING));
        toolBar.add(Box.createHorizontalStrut(PADDING));

//        addCommandsToToolBar(toolBar, new String[]{
//                "magicWandTool"
//        });

//        toolBar.add(Box.createHorizontalStrut(PADDING));

        toolBar.setTitle("Image Interactions");
        return toolBar;
    }


    protected CommandBar createSeadasGeometryToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_GEOMETRY_TOOL_BAR_ID, "Geometries");

//        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                "createVectorDataNode",
                "drawRectangleTool",
                "drawEllipseTool",
                "drawPolygonTool",
                "insertWktAsGeometry"

        });
//        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }


    protected CommandBar createSeadasPinsToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_PINS_TOOL_BAR_ID, "Pins");

//        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                PinManagerToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                "pinTool"
        });
//        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }

    protected CommandBar createSeadasGCPToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_GCP_TOOL_BAR_ID, "Ground Control Points");

//        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                GcpManagerToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                "gcpTool"
        });
//        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }

//    protected void addCommandsToToolBar(final CommandBar toolBar, final int width, final String[] commandIDs) {
//        for (final String commandID : commandIDs) {
//            if (commandID == null) {
//                toolBar.add(ToolButtonFactory.createToolBarSeparator());
//            } else {
//                final Command command = getCommandManager().getCommand(commandID);
//                if (command != null) {
//                    final AbstractButton toolBarButton = command.createToolBarButton();
//                    toolBarButton.addMouseListener(getMouseOverActionHandler());
//                    toolBar.add(toolBarButton);
//                } else {
//                    getLogger().warning(String.format("Toolbar '%s': No command found for ID = '%s'", toolBar.getName(),
//                            commandID));
//                }
//            }
//            toolBar.add(Box.createHorizontalStrut(width));
//        }
//    }

    @Override
    protected void addCommandsToToolBar(final CommandBar toolBar, final String[] commandIDs) {

        for (int i = 0; i < commandIDs.length; i++) {
            final String commandID = commandIDs[i];

            if (commandID == null) {
                toolBar.add(ToolButtonFactory.createToolBarSeparator());
            } else {
                final Command command = getCommandManager().getCommand(commandID);
                if (command != null) {
                    final AbstractButton toolBarButton = command.createToolBarButton();
                    toolBarButton.addMouseListener(getMouseOverActionHandler());
                    toolBar.add(toolBarButton);
                    if (i < (commandIDs.length - 1)) {
                        toolBar.add(Box.createHorizontalStrut(2));
                    }
                } else {
                    getLogger().warning(String.format("Toolbar '%s': No command found for ID = '%s'", toolBar.getName(),
                            commandID));
                }
            }

        }
    }


    protected CommandBar createSeadasWestDockToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_WEST_DOCK_TOOL_BAR_ID, "Window Group (West Dock)");

        String layerManagerToolViewCommandId = null;
        ToolViewDescriptor[] toolViewDescriptors = VisatActivator.getInstance().getToolViewDescriptors();
        for (ToolViewDescriptor toolViewDescriptor : toolViewDescriptors) {
            if (toolViewDescriptor.getId().contains("LayerManagerToolView")) {
                layerManagerToolViewCommandId = new String(toolViewDescriptor.getId() + ".showCmd");
            }
        }

        toolBar.add(Box.createHorizontalStrut(PADDING));

        addCommandsToToolBar(toolBar, new String[]{
                ProductsToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                MaskManagerToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                layerManagerToolViewCommandId,
                ColorManipulationToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX
        });


        return toolBar;
    }


    protected CommandBar createSeadasEastDockToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_EAST_DOCK_TOOL_BAR_ID, "Window Group (East Dock)");

        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                NavigationToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                WorldMapToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                PixelInfoToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX
        });
        toolBar.add(Box.createHorizontalStrut(PADDING));

        return toolBar;
    }


    protected CommandBar createSeadasBlankToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_BLANK_TOOL_BAR_ID, "Empty");

        toolBar.add(Box.createHorizontalStrut(300));

        return toolBar;
    }


    protected CommandBar createSeadasAnalysisToolBar() {
        final CommandBar toolBar = createToolBar(SEADAS_ANALYSIS_TOOL_BAR_ID, "Analysis");
//        toolBar.add(Box.createHorizontalStrut(PADDING));
        addCommandsToToolBar(toolBar, new String[]{
                StatisticsToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                HistogramPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                DensityPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                ScatterPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                ProfilePlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                SpectrumToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX
        });
//        toolBar.add(Box.createHorizontalStrut(PADDING));
        return toolBar;
    }

//
//    @Override
//    protected void loadCommands() {
//        super.loadCommands();
//
//        List<Command> commands = VisatActivator.getInstance().getCommands();
//        for (Command command : commands) {
//            if ("pannerTool".equals(command.getCommandID())) {
//                ToolCommand toolCommand = (ToolCommand) command;
//                selectionInteractor = toolCommand.getInteractor();
//                setActiveInteractor(selectionInteractor);
//                toolCommand.setSelected(true);
//            }
//        }
//    }

    @Override
    protected void applyPreferences() {

        super.applyPreferences();
        setHiddenToolbars();

    }


    protected void setHiddenToolbars() {

        String visibleToolBars[] = {
                MAIN_MENU_TOOL_BAR_TITLE,
                SEADAS_INTERACTIONS_TOOL_BAR_ID,
                SEADAS_ANALYSIS_TOOL_BAR_ID,
                SEADAS_BAND_TOOLS_TOOL_BAR_ID,
                SEADAS_DELUXE_TOOLS_TOOL_BAR_ID,
                SEADAS_FILE_TOOL_BAR_ID,
                SEADAS_GEOMETRY_TOOL_BAR_ID,
                SEADAS_PINS_TOOL_BAR_ID,
                SEADAS_PROC_TOOL_BAR_ID,
                SEADAS_STANDARD_LAYERS_TOOL_BAR_ID,
                SEADAS_BLANK_TOOL_BAR_ID};

        List<String> allDockableBarNames = getMainFrame().getDockableBarManager().getAllDockableBarNames();

        for (String dockableBarName : allDockableBarNames) {
            boolean hide = true;

            for (String visibleBarName : visibleToolBars) {
                if (dockableBarName.equals(visibleBarName)) {
                    hide = false;
                }
            }

            if (hide) {
                DockableBar dockableBar = getMainFrame().getDockableBarManager().getDockableBar(dockableBarName);
                dockableBar.getContext().setInitMode(DockableBarContext.STATE_HIDDEN);
            }
        }
    }


    @Override
    protected CommandBar createMainMenuBar() {
        final CommandMenuBar menuBar = new CommandMenuBar(MAIN_MENU_TOOL_BAR_TITLE);
        menuBar.setHidable(false);
        menuBar.setStretch(true);
        //  menuBar.setOpaque(false);

        menuBar.add(createJMenu("file", "File", 'F'));
        menuBar.add(createJMenu("edit", "Edit", 'E'));
        menuBar.add(createJMenu("view", "View", 'V'));
        menuBar.add(createJMenu("tools", "Tools", 'T'));
        menuBar.add(createJMenu("layers", "Layers", 'L'));
        menuBar.add(createJMenu("processing", "Processing", 'P'));
        menuBar.add(createJMenu("ocprocessing", "DataProcessing", 'D'));
        menuBar.add(createJMenu("analysis", "Analysis", 'A'));
        menuBar.add(createJMenu("info", "Info", 'I'));
        menuBar.add(createJMenu("window", "Window", 'W'));
        menuBar.add(createJMenu("help", "Help", 'H'));

        return menuBar;
    }


    //This enables placing the plugins in a desired menu
    @Override
    protected final void insertCommandMenuItem(Command command) {
        JMenu menu = null;
        String parent = command.getParent();
        if (parent != null) {
            menu = findMenu(parent);
        }
        if (menu == null) {
            if (command.getCommandID().contains("Bathymetry")) {
                menu = findMenu("layers");
                command.setPlaceBefore("showContourOverlay");
            } else if (command.getCommandID().contains("Coastline")) {
                menu = findMenu("layers");
                command.setPlaceBefore("showContourOverlay");
//                command.setSeparatorAfter(true);

            } else {
                menu = createNewMenu(parent);
            }
        }
        if (menu != null) {
            commandMenuInserter.insertCommandIntoMenu(command, menu);
        }
    }

    @Override
    protected void insertCommandMenuItems() {
        super.insertCommandMenuItems();

        JMenu menu = findMenu("edit");

        SelectionManager selectionManager = getApplicationPage().getSelectionManager();
        Action cutAction = new CutAction(selectionManager);
        Action copyAction = new CopyAction(selectionManager);
        Action pasteAction = new PasteAction(selectionManager);
        Action selectAllAction = new SelectAllAction(selectionManager);
        Action deleteAction = new DeleteAction(selectionManager);

//        menu.insert(undoAction, 0);
//        menu.insert(redoAction, 1);
        menu.insertSeparator(0);
        menu.insert(cutAction, 1);
        menu.insert(copyAction, 2);
        menu.insert(pasteAction, 3);
        menu.insert(deleteAction, 4);
        menu.insert(selectAllAction, 5);
        menu.insertSeparator(6);
    }



    @Override
    protected void initClientUI(com.bc.ceres.core.ProgressMonitor pm) {
        try {
            pm.beginTask(String.format("Initialising %s UI components", getAppName()), 18);

//            getMainToolBar().getContext().setInitMode(DockableBarContext.STATE_HIDDEN);
            getMainToolBar().setTitle("File");

            getMainFrame().getDockableBarManager().setRearrangable(true);


            CommandBar seadasBandToolsToolBar = createSeadasBandToolsToolBar();
            seadasBandToolsToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasBandToolsToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasBandToolsToolBar);
            pm.worked(1);

            CommandBar seadasDefaultToolBar = createSeadasProcToolBar();
            seadasDefaultToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasDefaultToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasDefaultToolBar);
            pm.worked(1);


//

            CommandBar seadasStandardLayersToolBar = createSeadasStandardLayersToolBar();
            seadasStandardLayersToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasStandardLayersToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasStandardLayersToolBar);
            pm.worked(1);


            CommandBar seadasDeluxeToolsToolBar = createSeadasDeluxeToolsToolBar();
            seadasDeluxeToolsToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasDeluxeToolsToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasDeluxeToolsToolBar);
            pm.worked(1);




            CommandBar seadasVectorLayersToolBar = createSeadasVectorLayersToolBar();
            seadasVectorLayersToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasVectorLayersToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasVectorLayersToolBar);

            pm.worked(1);


            CommandBar seadasWestDockToolBar = createSeadasWestDockToolBar();
            seadasWestDockToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasWestDockToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasWestDockToolBar);
           pm.worked(1);

            CommandBar seadasEastDockToolBar = createSeadasEastDockToolBar();
            seadasEastDockToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasEastDockToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasEastDockToolBar);
          pm.worked(1);


//            CommandBar seadasBlankToolBar = createSeadasBlankToolBar();
//            seadasBlankToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_SOUTH);
//            seadasBlankToolBar.getContext().setInitIndex(2);
//            getMainFrame().getDockableBarManager().addDockableBar(seadasBlankToolBar);
//            seadasBlankToolBar.setOpaque(false);
//            pm.worked(1);


            CommandBar seadasInteractionsToolBar = createSeadasInteractionsToolBar();
            seadasInteractionsToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasInteractionsToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasInteractionsToolBar);
            pm.worked(1);


            CommandBar seadasGeometriesToolBar = createSeadasGeometryToolBar();
            seadasGeometriesToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasGeometriesToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasGeometriesToolBar);
            pm.worked(1);


            CommandBar seadasPinsToolBar = createSeadasPinsToolBar();
            seadasPinsToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasPinsToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasPinsToolBar);
            pm.worked(1);

            CommandBar seadasGcpToolBar = createSeadasGCPToolBar();
            seadasGcpToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasGcpToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasGcpToolBar);
            pm.worked(1);


            CommandBar seadasInteractionsExtrasToolBar = createSeadasInteractionsExtrasToolBar();
            seadasInteractionsExtrasToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasInteractionsExtrasToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasInteractionsExtrasToolBar);
            pm.worked(1);

            CommandBar seadasAnalysisToolBar = createSeadasAnalysisToolBar();
            seadasAnalysisToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            seadasAnalysisToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(seadasAnalysisToolBar);
            pm.worked(1);


            // this gets all the plugin toolbars like (time series)
            // I want all these BEFORE the views one so I separated the views out and put later
            CommandBar[] viewToolBars = createViewsToolBars();
            for (CommandBar viewToolBar : viewToolBars) {
                if (!VIEWS_TOOL_BAR_ID.equals(viewToolBar.getName())) {
                    viewToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
                    viewToolBar.getContext().setInitIndex(2);
                    getMainFrame().getDockableBarManager().addDockableBar(viewToolBar);
                }
            }


            CommandBar beamMainToolBar = createBeamMainToolBar();
            beamMainToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            beamMainToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(beamMainToolBar);
            pm.worked(1);

            CommandBar layersToolBar = createLayersToolBar();
            layersToolBar.setTitle("BEAM: Layers");
            layersToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            layersToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(layersToolBar);

            pm.worked(1);


            CommandBar analysisToolBar = createAnalysisToolBar();
            analysisToolBar.setTitle("BEAM: Analysis");
            analysisToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
            analysisToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(analysisToolBar);

            pm.worked(1);


            for (CommandBar viewToolBar : viewToolBars) {
                if (VIEWS_TOOL_BAR_ID.equals(viewToolBar.getName())) {
                    viewToolBar.setTitle("BEAM: Views");
                    viewToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
                    viewToolBar.getContext().setInitIndex(2);
                    getMainFrame().getDockableBarManager().addDockableBar(viewToolBar);
                }
            }


            CommandBar toolsToolBar = createInteractionsToolBar();
            toolsToolBar.setTitle("BEAM: Interactions");
            toolsToolBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_EAST);
            toolsToolBar.getContext().setInitIndex(2);
            getMainFrame().getDockableBarManager().addDockableBar(toolsToolBar);
            pm.worked(1);


            List<Command> commands = VisatActivator.getInstance().getCommands();
            for (Command command : commands) {
                if ("pannerTool".equals(command.getCommandID())) {
                    ToolCommand toolCommand = (ToolCommand) command;
                    selectionInteractor = toolCommand.getInteractor();
                    setActiveInteractor(selectionInteractor);
                    toolCommand.setSelected(true);
                }
            }


            registerForMacOSXEvents();
            pm.worked(1);


//            getStatusBar().setEnabled(false);
//            getStatusBar().setVisible(false);

            int count = 0;
            for (Component component : getStatusBar().getComponents()) {
                if (count > 0) {
                    component.setVisible(false);
                }
                count++;
            }



            getMainFrame().setPreferredSize(new Dimension(1500, 700));
            getMainFrame().setMinimumSize(new Dimension(650, 300));

        } finally {
            pm.done();
        }
    }

    @Override
    public void clearStatusBarMessage() {
        super.clearStatusBarMessage();

        setStatusBarMessage("");
    }

}




