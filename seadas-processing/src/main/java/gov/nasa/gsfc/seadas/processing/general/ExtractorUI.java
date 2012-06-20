package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/3/12
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExtractorUI extends ProgramUIFactory {

    private ProcessorModel extractor;
    private ProcessorModel lonlat2pixline;

    private JPanel pixelPanel;
    private JPanel newsPanel;
    private JPanel paramPanel;

    private JToggleButton pixellonlatSwitch;

    private ParamUIFactory paramUIFactory;

    public ExtractorUI(String programName, String xmlFileName) {
        super(programName, xmlFileName);
    }

    private void computePixelsFromLonLat() {


        try {
            final Process process = lonlat2pixline.executeProcess();

            try {
                int exitValue = process.waitFor();
            } catch (Exception e) {
                SeadasLogger.getLogger().severe("Execution exception 0 : " + e.getMessage());
            }
            SeadasLogger.getLogger().info("Execution successful!");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
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

    protected JPanel getParamPanel() {

        SeadasLogger.getLogger().info("updating ofile change listener ...  processorModel   " + processorModel.getPrimaryOutputFileOptionName());

        lonlat2pixline = new ProcessorModel("lonlat2pixline", "lonlat2pixline.xml");

        paramUIFactory = new ParamUIFactory(processorModel);
        pixelPanel = paramUIFactory.createParamPanel(processorModel);

        newsPanel = new ParamUIFactory(lonlat2pixline).createParamPanel(lonlat2pixline);

        pixelPanel.setName("pixelPanel");
        newsPanel.setName("newsPanel");

        pixellonlatSwitch = new JToggleButton();
        pixellonlatSwitch.setText("<html><center>" + "Compute" + "<br>" + " PixLines" + "<br>" + "from LonLat" + "</center></html>");
        pixellonlatSwitch.setBorderPainted(false);
        pixellonlatSwitch.setEnabled(false);
        pixellonlatSwitch.setSelected(false);

        pixellonlatSwitch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                if (pixellonlatSwitch.isSelected()) {
                    pixellonlatSwitch.setBorderPainted(true);
                    computePixelsFromLonLat();
                } else {
                    pixellonlatSwitch.setBorderPainted(false);
                }
            }
        });

        paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
        paramPanel.setPreferredSize(new Dimension(700, 400));
        paramPanel.add(newsPanel,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        paramPanel.add(pixellonlatSwitch,
                new GridBagConstraintsCustom(1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        paramPanel.add(pixelPanel,
                new GridBagConstraintsCustom(0, 2, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));

        disableJPanel(paramPanel);


        processorModel.addPropertyChangeListener("ifile", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //File iFile = new File(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
                String programName = getExtractorProgramName(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
                if (programName == null) {
                    VisatApp.getApp().showErrorDialog("No extractor found for " + processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
                    return;
                }
                updateParamPanel(programName);
            }
        });

        processorModel.addPropertyChangeListener("infile", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //File iFile = new File(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
                String programName = getExtractorProgramName(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
                if (programName == null) {
                    VisatApp.getApp().showErrorDialog("No extractor found for " + processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
                    return;
                }
                updateParamPanel(programName);
            }
        });

        lonlat2pixline.addPropertyChangeListener(lonlat2pixline.getAllparamInitializedPropertyName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                pixellonlatSwitch.setEnabled(true);
                pixellonlatSwitch.setBorderPainted(true);
                processorModel.setReadyToRun(true);
            }
        });
        return paramPanel;
    }

    private void updateParamPanel(String programName) {

        paramPanel.remove(2);
        paramPanel.add(getPixelPanel(programName, programName + ".xml"),
                new GridBagConstraintsCustom(0, 2, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        paramPanel.repaint();
        paramPanel.validate();
    }

    protected void handleParamChanged() {
        if (lonlat2pixline.isAllParamsValid()) {
            pixellonlatSwitch.setEnabled(true);
            pixellonlatSwitch.setBorderPainted(true);
        }
    }

    private JPanel getPixelPanel(String processorName, String xmlFileName) {
        ProcessorModel extractor = new ProcessorModel(processorName, xmlFileName);
        extractor.updateIFileInfo(processorModel.getParamValue(processorModel.getPrimaryInputFileOptionName()));
        extractor.updateOFileInfo(processorModel.getParamValue(processorModel.getPrimaryOutputFileOptionName()));
        updateOfilePropertyChangeListeners(extractor.getPrimaryOutputFileOptionName());
        processorModel.setProgramName(processorName);
        processorModel.setParamList(extractor.getParamList());
        processorModel.setAcceptsParFile(extractor.acceptsParFile());
        processorModel.appendPropertyChangeSupport(extractor.getPropertyChangeSupport());

        processorModel.setHasGeoFile(extractor.hasGeoFile());
        processorModel.setPrimaryInputFileOptionName(extractor.getPrimaryInputFileOptionName());
        processorModel.setPrimaryOutputFileOptionName(extractor.getPrimaryOutputFileOptionName());
        processorModel.setPrimaryOptions(extractor.getPrimaryOptions());

        return new ParamUIFactory(processorModel).createParamPanel(processorModel);

    }

    private void updateOfilePropertyChangeListeners(String ofileOptionName) {
        SeadasLogger.getLogger().info("updating ofile change listener ... " + ofileOptionName + "  " + processorModel.getPrimaryOutputFileOptionName());

        PropertyChangeListener[] pcl = processorModel.getPropertyChangeSupport().getPropertyChangeListeners(processorModel.getPrimaryOutputFileOptionName());
        for (int i = 0; i < pcl.length; i++) {
            processorModel.addPropertyChangeListener(ofileOptionName, pcl[i]);
        }
    }

    private String getExtractorProgramName(String ifileName) {

        System.out.println(ifileName);

        FileInfo ifileInfo = new FileInfo(ifileName);
        System.out.println(ifileInfo.getTypeName() + ifileInfo.getMissionName());
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
            //l1a modis files needs geo files to get pixels from lon lat. Need to get geo file name and check for its existence.
            lonlat2pixline.updateIFileInfo(getLonLattoPixelsIFileName(ifileName, programName));
        }
        return programName;
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

}
