package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.swing.TableLayout;
import gov.nasa.gsfc.seadas.processing.core.OCSSWRunner;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/3/12
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExtractorUI extends ProgramUIFactory {

    private ProcessorModel lonlat2pixline;
    private final String L1AEXTRACT_SEAWIFS_INVALID_PARAMS = "prodlist";
    private final String L1AEXTRACT_MODIS_INVALID_PARAMS = "prodlist pix_sub sc_sub";
    private final String L1AEXTRACT_VIIRS_INVALID_PARAMS = "prodlist pix_sub sc_sub";
    private JPanel pixelPanel;
    private JPanel newsPanel;
    private JPanel paramPanel;

    private JToggleButton pixellonlatSwitch;

    private ParamUIFactory paramUIFactory;

    public ExtractorUI(String programName, String xmlFileName) {
        super(programName, xmlFileName);
        //processorModel.setReadyToRun(true);
        paramCounter = new HashMap();
    }

    private void computePixelsFromLonLat() {

        LonLat2PixlineConverter lonLat2PixlineConverter = new LonLat2PixlineConverter(lonlat2pixline);
        if (lonLat2PixlineConverter.computePixelsFromLonLat()) {
            processorModel.updateParamInfo(LonLat2PixlineConverter.START_PIXEL_PARAM_NAME, lonLat2PixlineConverter.getSpixl());
            processorModel.updateParamInfo(LonLat2PixlineConverter.END_PIXEL_PARAM_NAME, lonLat2PixlineConverter.getEpixl());
            processorModel.updateParamInfo(LonLat2PixlineConverter.START_LINE_PARAM_NAME, lonLat2PixlineConverter.getSline());
            processorModel.updateParamInfo(LonLat2PixlineConverter.END_LINE_PARAM_NAME, lonLat2PixlineConverter.getEline());
        }
    }

    @Override
    public JPanel getParamPanel() {

        SeadasLogger.getLogger().info("updating ofile change listener ...  processorModel   " + processorModel.getPrimaryOutputFileOptionName());

        lonlat2pixline = ProcessorModel.valueOf("lonlat2pixline", "lonlat2pixline.xml");
        initLonLatIFile();

        paramUIFactory = new ExtractorParamUI(processorModel);
        pixelPanel = paramUIFactory.createParamPanel(processorModel);

        newsPanel = new ParamUIFactory(lonlat2pixline).createParamPanel(lonlat2pixline);
        newsPanel.setBorder(BorderFactory.createTitledBorder("Lon/Lat"));
        pixelPanel.setBorder(BorderFactory.createTitledBorder("Pixels"));


        pixelPanel.setName("pixelPanel");
        newsPanel.setName("newsPanel");

        lonlat2pixline.addPropertyChangeListener("all_lon_lat_params_complete", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                computePixelsFromLonLat();
            }
        });

        paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
        paramPanel.setPreferredSize(new Dimension(700, 400));
        paramPanel.add(newsPanel,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        paramPanel.add(Box.createRigidArea(new Dimension(100, 50)),
                new GridBagConstraintsCustom(0, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        paramPanel.add(pixelPanel,
                new GridBagConstraintsCustom(0, 2, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));

        disableJPanel(paramPanel);


        processorModel.addPropertyChangeListener(processorModel.getPrimaryInputFileOptionName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String programName = getExtractorProgramName(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
                if (programName == null) {
                    VisatApp.getApp().showErrorDialog("No extractor found for " + processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
                    return;
                }
                if (programName.equals("l1aextract_seawifs")) {
                    updateParamPanel(pixelPanel, L1AEXTRACT_SEAWIFS_INVALID_PARAMS);
                } else if (programName.equals("l1aextract_modis")) {
                    updateParamPanel(pixelPanel, L1AEXTRACT_MODIS_INVALID_PARAMS);
                } else if (programName.equals("l1aextract_viirs")) {
                    updateParamPanel(pixelPanel, L1AEXTRACT_VIIRS_INVALID_PARAMS);
                }
            }
        });

        lonlat2pixline.addPropertyChangeListener(lonlat2pixline.getAllparamInitializedPropertyName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                computePixelsFromLonLat();
            }
        });

//        String programName = getExtractorProgramName(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
//        if (programName != null && programName.equals("l1aextract_seawifs")) {
//             updateParamPanel(pixelPanel, L1AEXTRACT_SEAWIFS_INVALID_PARAMS);
//         } else if (programName != null && programName.equals("l1aextract_modis")) {
//             updateParamPanel(pixelPanel, L1AEXTRACT_MODIS_INVALID_PARAMS);
//         } else if (programName != null && programName.equals("l1aextract_viirs")) {
//            updateParamPanel(pixelPanel, L1AEXTRACT_VIIRS_INVALID_PARAMS);
//        }
        return paramPanel;
    }

    private void updateParamPanel(JPanel currentPanel, String paramsToDisable) {

        JPanel newPixelPanel = new JPanel(new TableLayout(4));
        newPixelPanel.setName("pixelPanel");
        newPixelPanel.setBorder(BorderFactory.createTitledBorder("Pixels"));
        JPanel panel = (JPanel) currentPanel.getComponent(1);
        Component[] options = panel.getComponents();
        for (Component option : options) {
            if (paramsToDisable.contains(option.getName())) {
                option.setEnabled(false);
            } else {
                newPixelPanel.add(option);
            }
        }

        paramPanel.remove(2);
        paramPanel.add(newPixelPanel,
                new GridBagConstraintsCustom(0, 2, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        paramPanel.repaint();
        paramPanel.validate();
    }


    private String getExtractorProgramName(String ifileName) {

        FileInfo ifileInfo = new FileInfo(ifileName);
        SeadasFileUtils.debug("Extractor ifile info: " + ifileInfo.getTypeName() + ifileInfo.getMissionName());
        String programName = null;
        if (ifileInfo.getMissionName() != null && ifileInfo.getTypeName() != null) {
            if (ifileInfo.getMissionName().contains("MODIS") && ifileInfo.getTypeName().contains("1A")) {
                programName = "l1aextract_modis";
            } else if (ifileInfo.getMissionName().contains("VIIRS") && ifileInfo.getTypeName().contains("1A")) {
                programName = "l1aextract_viirs";
            }
            else if (ifileInfo.getMissionName().contains("SeaWiFS") && ifileInfo.getTypeName().contains("1A") ||
                    ifileInfo.getMissionName().contains("CZCS")) {
                programName = "l1aextract_seawifs";
            } else if ((ifileInfo.getTypeName().contains("L2") || ifileInfo.getTypeName().contains("Level 2"))||
                    (ifileInfo.getMissionName().contains("OCTS") && (ifileInfo.getTypeName().contains("L1") || ifileInfo.getTypeName().contains("Level 1")))) {
                programName = "l2extract";
            }
        }
        //l1a modis files needs geo files to get pixels from lon lat. Need to get geo file name and check for its existence.

        if (programName != null) {
            lonlat2pixline.updateIFileInfo(getLonLattoPixelsIFileName(ifileName, programName));
        }

        return programName;
    }

    private void initLonLatIFile() {
        if (processorModel.getParamInfo(processorModel.getPrimaryInputFileOptionName()).getValue().trim().length() > 0) {
            lonlat2pixline.updateIFileInfo(processorModel.getParamInfo(processorModel.getPrimaryInputFileOptionName()).getValue().trim());
        }
    }

    private String getLonLattoPixelsIFileName(String ifileName, String programName) {

        if (programName.contains("l1aextract_modis") || programName.contains("l1aextract_viirs")) {
            String geoFileName = (ifileName.substring(0, ifileName.indexOf("."))).concat(".GEO");

            if (new File(geoFileName).exists()) {
                return geoFileName;
            } else {
                VisatApp.getApp().showErrorDialog(ifileName + " requires a GEO file to be extracted. " + geoFileName + " does not exist.");
                return null;
            }

        }
        return ifileName;
    }

    private void addEventListeners() {

        boolean allParamsComplete = false;

        for (final ParamInfo pi : lonlat2pixline.getParamList().getParamArray()) {

            paramCounter.put(pi.getName(), false);
            lonlat2pixline.getParamList().addPropertyChangeListener(pi.getName(), new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    if (validInput(pi.getValue())) {
                        paramCounter.put(pi.getName(), true);
                        updateAllParamsCompleteFlag();
                    }
                }
            });
        }

    }

    private void updateAllParamsCompleteFlag() {
        allParamsComplete = true;
        for (boolean value : paramCounter.values()) {
            allParamsComplete = value && allParamsComplete;
        }
        if (allParamsComplete) {
            computePixelsFromLonLat();
        }
    }

    private boolean validInput(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    boolean allParamsComplete;
    HashMap<String, Boolean> paramCounter;
}
