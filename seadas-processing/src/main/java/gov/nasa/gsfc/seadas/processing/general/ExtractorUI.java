package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.l2gen.GridBagConstraintsCustom;
import org.esa.beam.framework.datamodel.Product;
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
public class ExtractorUI extends CloProgramUIImpl {

    private ProcessorModel extractor;
    private ProcessorModel lonlat2pixline;

    private JPanel pixelPanel;
    private JPanel newsPanel;
    private JPanel paramPanel;

    private JToggleButton pixellonlatSwitch;

    public ExtractorUI(String programName, String xmlFileName) {
        super(programName, xmlFileName);

     }


    //private void

    private File getGeoFileName(File inputFile) {

        String geoFileName = inputFile.getName();
        if (geoFileName.indexOf(".L2") != -1 || geoFileName.indexOf("S") == 0) {
            return inputFile;
        }
        geoFileName = geoFileName.substring(0, geoFileName.indexOf("."));
        geoFileName = geoFileName.concat(".GEO");

        System.out.println("geofileName = " + geoFileName);
        return new File(inputFile.getParentFile(), geoFileName);
    }

    public void updateProcessorModel() {

        Product selectedProduct = sourceProductSelector.getSelectedProduct();
        if (sourceProductSelector.getSelectedProduct() != null) {
            final File inputFile = selectedProduct.getFileLocation();
            System.out.println("update processors model " + inputFile.toString());
            extractor.updateParamInfo(extractor.getPrimaryInputFileOptionName(), inputFile.toString());

            //lonlat2pixline.setInputFile(getGeoFileName(inputFile));
            lonlat2pixline.updateParamInfo(lonlat2pixline.getPrimaryInputFileOptionName(), getGeoFileName(inputFile).toString());
        }

        //OutputFileSelectorModel outputFileSelectorModel = outputFileSelector.getModel();
       // if (outputFileSelectorModel != null) {
            extractor.updateParamInfo(extractor.getPrimaryOutputFileOptionName(), outputFileSelector.getFileName());
            //extractor.setOutputFileDir(outputFileSelectorModel.getProductDir());
            //extractor.setOutputFileName(outputFileSelectorModel.getProductFileName());
        //}
    }

    public ProcessorModel getProcessorModel() {

        updateProcessorModel();
        if (!pixellonlatSwitch.isSelected()) {
            computePixelsFromLonLat();
        }
        return extractor;
    }

    public Product getSelectedSourceProduct() {

        return sourceProductSelector.getSelectedProduct();
    }

    private void computePixelsFromLonLat() {

        if (sourceProductSelector.getSelectedProduct() == null) {
            VisatApp.getApp().showErrorDialog(lonlat2pixline.getProgramName(), "No product selected.");
            return;
        }

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

                    System.out.printf("Option name: %1$s  Value: %2$s %n", tmp[0], tmp[1]);
                    extractor.updateParamInfo(tmp[0], tmp[1]);
                }
            }

        } catch (IOException ioe) {

        }

    }

    protected JPanel createParamPanel() {
        extractor = new ProcessorModel(programName, xmlFileName);
        lonlat2pixline = new ProcessorModel("lonlat2pixline", "lonlat2pixline.xml");


        pixelPanel = createParamPanel(extractor);
        newsPanel = createParamPanel(lonlat2pixline);

        newsPanel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                System.out.println(propertyChangeEvent.getPropertyName());
            }
        });
        newsPanel.getComponent(0);
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
                    System.out.println(pixellonlatSwitch.isSelected());
                    updateProcessorModel();
                    computePixelsFromLonLat();
                    pixelPanel = createParamPanel(extractor);
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

        //System.out.println(pixellonlatSwitch.isSelected());

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
