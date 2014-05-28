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

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.CommandMenuBar;
import org.esa.beam.framework.datamodel.PlacemarkDescriptor;
import org.esa.beam.framework.datamodel.PlacemarkDescriptorRegistry;
import org.esa.beam.framework.ui.application.ApplicationDescriptor;
import org.esa.beam.framework.ui.application.ToolViewDescriptor;
import org.esa.beam.framework.ui.command.Command;
import org.esa.beam.framework.ui.command.CommandManager;
import org.esa.beam.framework.ui.command.ToolCommand;
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
import java.util.*;

/**
 * The <code>SeadasApp</code> class represents the SeaDAS UI application.
 *
 * @author Don
 */
public class SeadasApp extends VisatApp {

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
        final CommandBar toolBar = createToolBar(MAIN_TOOL_BAR_ID, "Standard");
        addCommandsToToolBar(toolBar, new String[]{
                "open",
                null,
                "createSubsetFromView",
                "mosaicAction",
                "collocation",

                "bandArithmetic"
        });
        return toolBar;
    }


    @Override
    protected CommandBar createLayersToolBar() {
        final CommandBar toolBar = createToolBar(LAYERS_TOOL_BAR_ID, "Layers");
        ArrayList<String> commandIdList = new ArrayList<String>(Arrays.asList(
                "showNoDataOverlay",
                "showShapeOverlay",
                "showGraticuleOverlay",
                "showWorldMapOverlay"));
        Set<PlacemarkDescriptor> placemarkDescriptors = PlacemarkDescriptorRegistry.getInstance().getPlacemarkDescriptors();
        for (PlacemarkDescriptor placemarkDescriptor : placemarkDescriptors) {
            if (placemarkDescriptor.getShowLayerCommandId() != null) {
                String s1 = placemarkDescriptor.getBaseFeatureType().getName().getLocalPart();
                String s2 = placemarkDescriptor.getBaseFeatureType().getName().toString();


                if (!placemarkDescriptor.getBaseFeatureType().getName().getLocalPart().contains("GroundControlPoint")) {
                    commandIdList.add(placemarkDescriptor.getShowLayerCommandId());
                }
            }
        }
        addCommandsToToolBar(toolBar, commandIdList.toArray(new String[0]));
        return toolBar;
    }

    @Override
    protected CommandBar createAnalysisToolBar() {
        final CommandBar toolBar = createToolBar(ANALYSIS_TOOL_BAR_ID, "Analysis");
        addCommandsToToolBar(toolBar, new String[]{
                StatisticsToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                HistogramPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                DensityPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                ScatterPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                ProfilePlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
                SpectrumToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX
        });
        return toolBar;
    }

    @Override
    protected CommandBar[] createViewsToolBars() {

        final HashSet<String> excludedIds = new HashSet<>(15);
        // todo - remove bad forward dependencies to tool views (nf - 30.10.2008)
        excludedIds.add(TileCacheDiagnosisToolView.ID);
        excludedIds.add(ProductsToolView.ID);
        excludedIds.add(ColorManipulationToolView.ID);
        excludedIds.add(NavigationToolView.ID);
        excludedIds.add(MaskManagerToolView.ID);
//        excludedIds.add(GcpManagerToolView.ID);
        excludedIds.add(PinManagerToolView.ID);
//        excludedIds.add(LayerManagerToolView.ID);
        excludedIds.add(PixelInfoToolView.ID);
        excludedIds.add(SpectrumToolView.ID);
        excludedIds.add(WorldMapToolView.ID);
        excludedIds.add(PlacemarkEditorToolView.ID);
        excludedIds.add(InformationToolView.ID);
        excludedIds.add(GeoCodingToolView.ID);
        excludedIds.add(StatisticsToolView.ID);
        excludedIds.add(HistogramPlotToolView.ID);
        excludedIds.add(ScatterPlotToolView.ID);
        excludedIds.add(DensityPlotToolView.ID);
        excludedIds.add(ProfilePlotToolView.ID);
        excludedIds.add("org.esa.beam.scripting.visat.ScriptConsoleToolView");
        excludedIds.add("org.esa.beam.visat.toolviews.layermanager.LayerEditorToolView");


        ToolViewDescriptor[] toolViewDescriptors = VisatActivator.getInstance().getToolViewDescriptors();

        Map<String, List<String>> toolBar2commandIds = new HashMap<>();
        for (ToolViewDescriptor toolViewDescriptor : toolViewDescriptors) {
            if (!excludedIds.contains(toolViewDescriptor.getId())) {
                final String commandId = toolViewDescriptor.getId() + ".showCmd";

                String toolBarId = toolViewDescriptor.getToolBarId();
                if (toolBarId == null || toolBarId.isEmpty()) {
                    toolBarId = VIEWS_TOOL_BAR_ID;
                }

                List<String> commandIds = toolBar2commandIds.get(toolBarId);
                if (commandIds == null) {
                    commandIds = new ArrayList<String>(5);
                    toolBar2commandIds.put(toolBarId, commandIds);
                }
                if (!toolViewDescriptor.getId().contains("LayerManagerToolView")) {
                    commandIds.add(commandId);
                }
            }
        }

        List<CommandBar> viewToolBars = new ArrayList<>(5);
        viewToolBars.add(createToolBar(VIEWS_TOOL_BAR_ID, "Views"));
        for (String toolBarId : toolBar2commandIds.keySet()) {
            CommandBar toolBar = getToolBar(toolBarId);
            if (toolBar == null) {
                // todo - use ToolBarDescriptor to define tool bar properties, e.g. title, dockSite, ...  (nf - 20090119)
                toolBar = createToolBar(toolBarId, toolBarId.replace('.', ' ').replace('_', ' '));
                viewToolBars.add(toolBar);

                // 	Retrospectively add "tool bar toggle" menu item
                ShowToolBarAction action = new ShowToolBarAction(toolBarId + ".showToolBar");
                action.setText(toolBarId);
                action.setContexts(new String[]{toolBarId});
                action.setToggle(true);
                action.setSelected(true);
                getCommandManager().addCommand(action);
                JMenu toolBarsMenu = findMenu("toolBars");
                toolBarsMenu.add(action.createMenuItem());
            }
            List<String> commandIds = toolBar2commandIds.get(toolBarId);
            addCommandsToToolBar(toolBar, commandIds.toArray(new String[commandIds.size()]));
        }

        return viewToolBars.toArray(new CommandBar[viewToolBars.size()]);
    }

    @Override
    protected CommandBar createInteractionsToolBar() {
        final CommandBar toolBar = createToolBar(INTERACTIONS_TOOL_BAR_ID, "Interactions");
        addCommandsToToolBar(toolBar, new String[]{
                // These IDs are defined in the module.xml
                "selectTool",
                "pannerTool",
                "zoomTool",
                "magicWandTool",
                "createVectorDataNode",
                "drawRectangleTool",
                "drawEllipseTool",
                "drawPolygonTool",
                "drawLineTool",
                "drawPolylineTool",
                "insertWktAsGeometry",
                "pinTool",
                "gcpTool",
                "rangeFinder",
        });
        return toolBar;
    }

    @Override
    protected void loadCommands() {
        CommandManager commandManager = getCommandManager();
        List<Command> commands = VisatActivator.getInstance().getCommands();
        Map<String, Command> commandMap = new HashMap<>(commands.size() * 2 + 1);
        for (Command command : commands) {
            commandMap.put(command.getCommandID(), command);
        }
        for (Command command : commands) {
            addCommand(commandManager, command, commandMap);
            if ("pannerTool".equals(command.getCommandID())) {
                ToolCommand toolCommand = (ToolCommand) command;
                selectionInteractor = toolCommand.getInteractor();
                setActiveInteractor(selectionInteractor);
                toolCommand.setSelected(true);
            }
        }
//        setSelectToolSelectState(commandManager);
    }

    @Override
    protected CommandBar createMainMenuBar() {
        final CommandMenuBar menuBar = new CommandMenuBar("Main Menu");
        menuBar.setHidable(false);
        menuBar.setStretch(true);


        menuBar.add(createJMenu("file", "File", 'F'));
        menuBar.add(createJMenu("edit", "Edit", 'E'));
        menuBar.add(createJMenu("view", "View", 'V'));
//        menuBar.add(createJMenu("data", "Analysis", 'A',
//                ScatterPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                GeoCodingToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                HistogramPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                InformationToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                ProfilePlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                DensityPlotToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX,
//                StatisticsToolView.ID + SHOW_TOOLVIEW_CMD_POSTFIX
//        ));

        menuBar.add(createJMenu("tools", "Tools", 'T'));
        menuBar.add(createJMenu("processing", "Proc", 'P'));
        menuBar.add(createJMenu("ocprocessing", "OceancolorProc", 'O'));
        menuBar.add(createJMenu("window", "Window", 'W'));
        menuBar.add(createJMenu("help", "Help", 'H'));

        return menuBar;
    }

}




