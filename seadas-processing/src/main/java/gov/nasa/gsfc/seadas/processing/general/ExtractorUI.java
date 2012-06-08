package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private File getGeoFileName(File inputFile) {

        String geoFileName = inputFile.getName();
        if (geoFileName.indexOf(".L2") != -1 || geoFileName.indexOf("S") == 0) {
            return inputFile;
        }
        geoFileName = geoFileName.substring(0, geoFileName.indexOf("."));
        geoFileName = geoFileName.concat(".GEO");
        return new File(inputFile.getParentFile(), geoFileName);
    }

    public void updateProcessorModel() {

//        Product selectedProduct = sourceProductSelector.getSelectedProduct();
//        if (sourceProductSelector.getSelectedProduct() != null) {
//            final File inputFile = selectedProduct.getFileLocation();
//            extractor.updateParamInfo(extractor.getPrimaryInputFileOptionName(), inputFile.toString());
//            lonlat2pixline.updateParamInfo(lonlat2pixline.getPrimaryInputFileOptionName(), getGeoFileName(inputFile).toString());
//        }
//        extractor.updateParamInfo(extractor.getPrimaryOutputFileOptionName(), outputFileSelector.getFileName());

    }

    public ProcessorModel getProcessorModel() {

        updateProcessorModel();
        if (!pixellonlatSwitch.isSelected()) {
            computePixelsFromLonLat();
        }
        return extractor;
    }


    private void computePixelsFromLonLat() {


        try {
            final Process process = lonlat2pixline.executeProcess();

            try {
                process.wait();
            } catch (Exception e) {

            }
            SeadasLogger.getLogger().fine("Execution successful!");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            String[] tmp;
            while ((line = br.readLine()) != null) {

                SeadasLogger.getLogger().finest(line);
                if (line.indexOf("=") != -1) {
                    tmp = line.split("=");
                    extractor.updateParamInfo(tmp[0], tmp[1]);
                }
            }

        } catch (IOException ioe) {

        }

    }

    protected JPanel createParamPanel() {

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

        pixellonlatSwitch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                if (pixellonlatSwitch.isSelected()) {
                    pixellonlatSwitch.setBorderPainted(true);
                    //updateProcessorModel();
                    computePixelsFromLonLat();
                    //pixelPanel = createParamPanel(extractor);
                } else {
                    pixellonlatSwitch.setBorderPainted(false);
                }
                updateParamPanel();
            }
        });

        paramPanel = new JPanel(new GridBagLayout());
         paramPanel.add(newsPanel,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2));
        paramPanel.add(pixellonlatSwitch,
                new GridBagConstraintsCustom(1, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, 2));
        paramPanel.add(pixelPanel,
                new GridBagConstraintsCustom(0, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2));
        return paramPanel;
    }

    private void updateParamPanel() {

        paramPanel.remove(paramPanel.getComponent(2));
        paramPanel.add(pixelPanel, new GridBagConstraintsCustom(0, 1, 3, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH));
        pixellonlatSwitch.setEnabled(false);
        pixellonlatSwitch.setBorderPainted(false);
        paramPanel.validate();
        paramPanel.repaint(50L);
    }

    protected void handleParamChanged() {
        if (lonlat2pixline.isAllParamsValid()) {
            pixellonlatSwitch.setEnabled(true);
            pixellonlatSwitch.setBorderPainted(true);
        }
    }

}
