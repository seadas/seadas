package gov.nasa.gsfc.seadas.processing.common;

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
    private JPanel pixelPanel;
    private JPanel newsPanel;
    private JPanel paramPanel;

    private ParamUIFactory paramUIFactory;

    private boolean initiliazed = false;

    public ExtractorUI(String programName, String xmlFileName) {
        super(programName, xmlFileName);
        paramCounter = new HashMap();
        initiliazed = true;
    }

    private void initLonLatProcessor() {
        lonlat2pixline = ProcessorModel.valueOf("lonlat2pixline", "lonlat2pixline.xml");
        lonlat2pixline.addPropertyChangeListener(lonlat2pixline.getAllparamInitializedPropertyName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                processorModel.updateParamInfo(LonLat2PixlineConverter.START_PIXEL_PARAM_NAME, lonlat2pixline.getParamValue(LonLat2PixlineConverter.START_PIXEL_PARAM_NAME));
                processorModel.updateParamInfo(LonLat2PixlineConverter.END_PIXEL_PARAM_NAME, lonlat2pixline.getParamValue(LonLat2PixlineConverter.END_PIXEL_PARAM_NAME));
                processorModel.updateParamInfo(LonLat2PixlineConverter.START_LINE_PARAM_NAME, lonlat2pixline.getParamValue(LonLat2PixlineConverter.START_LINE_PARAM_NAME));
                processorModel.updateParamInfo(LonLat2PixlineConverter.END_LINE_PARAM_NAME, lonlat2pixline.getParamValue(LonLat2PixlineConverter.END_LINE_PARAM_NAME));
            }
        });

        processorModel.addPropertyChangeListener(processorModel.getPrimaryInputFileOptionName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (processorModel.getProgramName()!=null) {
                    lonlat2pixline.updateIFileInfo(getLonLattoPixelsIFileName(processorModel.getParamInfo(processorModel.getPrimaryInputFileOptionName()).getValue().trim(), processorModel.getProgramName()));
                }
            }
        });
        if (processorModel.getParamInfo(processorModel.getPrimaryInputFileOptionName()).getValue().trim().length() > 0) {
            lonlat2pixline.updateIFileInfo(processorModel.getParamInfo(processorModel.getPrimaryInputFileOptionName()).getValue().trim());
        }
    }

    private void initStaticPanels() {
        newsPanel = new ParamUIFactory(lonlat2pixline).createParamPanel(lonlat2pixline);
        newsPanel.setBorder(BorderFactory.createTitledBorder("Lon/Lat"));
        newsPanel.setName("newsPanel");
    }

    @Override
    public JPanel getParamPanel() {

        if (!initiliazed) {
            initLonLatProcessor();
            initStaticPanels();
        }

        SeadasFileUtils.debug("updating ofile change listener ...  processorModel   " + processorModel.getPrimaryOutputFileOptionName());
        paramUIFactory = new ExtractorParamUI(processorModel);
        pixelPanel = paramUIFactory.createParamPanel(processorModel);
        pixelPanel.setBorder(BorderFactory.createTitledBorder("Pixels"));
        pixelPanel.setName("pixelPanel");
        paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
        paramPanel.setPreferredSize(new Dimension(700, 400));
        paramPanel.add(newsPanel,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        paramPanel.add(Box.createRigidArea(new Dimension(100, 50)),
                new GridBagConstraintsCustom(0, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        paramPanel.add(pixelPanel,
                new GridBagConstraintsCustom(0, 2, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        return paramPanel;
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

    HashMap<String, Boolean> paramCounter;
}
