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


        try {
            final Process process = OCSSWRunner.execute(lonlat2pixline.getProgramCmdArray(), lonlat2pixline.getIFileDir());

            try {
                int exitValue = process.waitFor();
            } catch (Exception e) {
                SeadasLogger.getLogger().severe("Execution exception 0 : " + e.getMessage());
            }
            SeadasLogger.getLogger().info("Execution successful!");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            String[] tmp;
            while ((line = br.readLine()) != null) {
                SeadasLogger.getLogger().info(line);
                if (line.indexOf("=") != -1) {
                    tmp = line.split("=");
                    processorModel.updateParamInfo(tmp[0], tmp[1]);
                }
            }

        } catch (IOException ioe) {

            SeadasLogger.getLogger().severe("Execution exception: " + ioe.getMessage());

        }

    }

    @Override
    public JPanel getParamPanel() {

        SeadasLogger.getLogger().info("updating ofile change listener ...  processorModel   " + processorModel.getPrimaryOutputFileOptionName());

        lonlat2pixline = ProcessorModel.valueOf("lonlat2pixline", "lonlat2pixline.xml");
        initLonLatIFile();

        paramUIFactory = new ParamUIFactory(processorModel);
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
//        pixellonlatSwitch = new JToggleButton();
//        pixellonlatSwitch.setText("<html><center>" + "Compute" + "<br>" + " PixLines" + "<br>" + "from LonLat" + "</center></html>");
//        pixellonlatSwitch.setBorderPainted(false);
//        pixellonlatSwitch.setEnabled(false);
//        pixellonlatSwitch.setSelected(false);
//
//        pixellonlatSwitch.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent actionEvent) {
//
//                if (pixellonlatSwitch.isSelected()) {
//                    pixellonlatSwitch.setBorderPainted(true);
//                    computePixelsFromLonLat();
//                } else {
//                    pixellonlatSwitch.setBorderPainted(false);
//                }
//            }
//        });

        paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
        paramPanel.setPreferredSize(new Dimension(700, 400));
        paramPanel.add(newsPanel,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
//        paramPanel.add(pixellonlatSwitch,
//                new GridBagConstraintsCustom(1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        //The following statement will create a space between two panels.
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
                    //processorModel.updateIFileInfo("");
                    return;
                }
                if (programName.equals("l1aextract_seawifs")) {
                    updateParamPanel(pixelPanel, L1AEXTRACT_SEAWIFS_INVALID_PARAMS);
                } else if (programName.equals("l1aextract_modis")) {
                    updateParamPanel(pixelPanel, L1AEXTRACT_MODIS_INVALID_PARAMS);
                }
                //updateParamPanel(programName);
            }
        });

        lonlat2pixline.addPropertyChangeListener(lonlat2pixline.getAllparamInitializedPropertyName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
//                pixellonlatSwitch.setEnabled(true);
//                pixellonlatSwitch.setBorderPainted(true);
//                processorModel.setReadyToRun(true);
                computePixelsFromLonLat();
            }
        });

        String programName = getExtractorProgramName(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
//        if (programName != null && !programName.equals("extractor")) {
//            updateParamPanel(programName);
//        }
        if (programName != null && programName.equals("l1aextract_seawifs")) {
             updateParamPanel(pixelPanel, L1AEXTRACT_SEAWIFS_INVALID_PARAMS);
         } else if (programName != null && programName.equals("l1aextract_modis")) {
             updateParamPanel(pixelPanel, L1AEXTRACT_MODIS_INVALID_PARAMS);
         }
        return paramPanel;
    }

//    private void updateParamPanel(String programName) {
//
//        paramPanel.remove(2);
//        paramPanel.add(getPixelPanel(programName, programName + ".xml"),
//                new GridBagConstraintsCustom(0, 2, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
//        paramPanel.repaint();
//        paramPanel.validate();
//    }

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

//    private JPanel getPixelPanel(String processorName, String xmlFileName) {
//        ProcessorModel extractor = new ProcessorModel(processorName, xmlFileName);
//        extractor.appendPropertyChangeSupport(processorModel.getPropertyChangeSupport());
//        int ifileOrder = extractor.getParamInfo(extractor.getPrimaryInputFileOptionName()).getOrder();
//        int ofileOrder = extractor.getParamInfo(extractor.getPrimaryOutputFileOptionName()).getOrder();
//        extractor.removeParamInfo(extractor.getParamInfo(extractor.getPrimaryInputFileOptionName()));
//        extractor.removeParamInfo(extractor.getParamInfo(extractor.getPrimaryOutputFileOptionName()));
//        extractor.addParamInfo(processorModel.getParamInfo(processorModel.getPrimaryInputFileOptionName()));
//        extractor.addParamInfo(processorModel.getParamInfo(processorModel.getPrimaryOutputFileOptionName()));
//        extractor.getParamInfo(extractor.getPrimaryInputFileOptionName()).setOrder(ifileOrder);
//        extractor.getParamInfo(extractor.getPrimaryOutputFileOptionName()).setOrder(ofileOrder);
//        processorModel.setProgramName(processorName);
//        processorModel.setParamList(extractor.getParamList());
//        processorModel.setAcceptsParFile(extractor.acceptsParFile());
//        processorModel.setHasGeoFile(extractor.hasGeoFile());
//        processorModel.setPrimaryOptions(extractor.getPrimaryOptions());
//        return new ParamUIFactory(processorModel).createParamPanel(processorModel);
//
//    }

    private String getExtractorProgramName(String ifileName) {

        FileInfo ifileInfo = new FileInfo(ifileName);
        SeadasFileUtils.debug("Extractor ifile info: " + ifileInfo.getTypeName() + ifileInfo.getMissionName());
        String programName = null;
        if (ifileInfo.getMissionName() != null && ifileInfo.getTypeName() != null) {
            if (ifileInfo.getMissionName().indexOf("MODIS") != -1 && ifileInfo.getTypeName().indexOf("1A") != -1) {
                programName = "l1aextract_modis";
            } else if (ifileInfo.getMissionName().indexOf("SeaWiFS") != -1 && ifileInfo.getTypeName().indexOf("1A") != -1 ||
                    ifileInfo.getMissionName().indexOf("CZCS") != -1) {
                programName = "l1aextract_seawifs";
            } else if ((ifileInfo.getTypeName().indexOf("L2") != -1 || ifileInfo.getTypeName().indexOf("Level 2") != -1) ||
                    (ifileInfo.getMissionName().indexOf("OCTS") != -1 && (ifileInfo.getTypeName().indexOf("L1") != -1 || ifileInfo.getTypeName().indexOf("Level 1") != -1))) {
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

        if (programName.indexOf("l1aextract_modis") != -1) {
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
