package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
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

        //lonlat2pixline.hasOutputFile(false);

//        sourceProductSelector = new SourceProductFileSelector(VisatApp.getApp(), "");
//        //sourceProductSelector.setProcessorModel(extractor);
//        sourceProductSelector.initProducts();
//
//        outputFileSelector = new OutputFileSelector(VisatApp.getApp(), "Output File");
//        this.setName("mainPanel");
//        initUI();
    }


    //private void

    private File getGeoFileName(File inputFile) {

        String geoFileName = inputFile.getName();
        if (geoFileName.indexOf(".L2") != -1 || geoFileName.indexOf("S") == 0) {
            return inputFile;
        }
        geoFileName = geoFileName.substring(0, geoFileName.indexOf("."));
        geoFileName = geoFileName.concat(".GEO");

        //System.out.println("geofileName = " + geoFileName);
        return new File(inputFile.getParentFile(), geoFileName);
    }

    public void updateProcessorModel() {

        Product selectedProduct = sourceProductSelector.getSelectedProduct();
        if (sourceProductSelector.getSelectedProduct() != null) {
            final File inputFile = selectedProduct.getFileLocation();
            //System.out.println("update processors model " + inputFile.toString());
            extractor.updateParamInfo(extractor.getPrimaryInputFileOptionName(), inputFile.toString());

            //lonlat2pixline.setInputFile(getGeoFileName(inputFile));
            lonlat2pixline.updateParamInfo(lonlat2pixline.getPrimaryInputFileOptionName(), getGeoFileName(inputFile).toString());
        }
            extractor.updateParamInfo(extractor.getPrimaryOutputFileOptionName(), outputFileSelector.getFileName());

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

                    //System.out.printf("Option name: %1$s  Value: %2$s %n", tmp[0], tmp[1]);
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
                //System.out.println(propertyChangeEvent.getPropertyName());
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
                    //System.out.println(pixellonlatSwitch.isSelected());
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
        //TableLayout paramPanelLayout = new TableLayout(2);
        //paramPanel.setLayout(paramPanelLayout);

//        paramPanel.add(newsPanel, new GridBagConstraintsCustom(0, 0, 3, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH));
//        paramPanel.add(pixelPanel , new GridBagConstraintsCustom(1, 0, 3, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH));
//        paramPanel.add(pixellonlatSwitch, new GridBagConstraintsCustom(3, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        //paramPanelLayout.setCellRowspan(1, 1, 2);
        paramPanel.add(newsPanel,
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2));
        paramPanel.add(pixellonlatSwitch,
                new GridBagConstraintsCustom(1, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, 2));
        paramPanel.add(pixelPanel,
                new GridBagConstraintsCustom(0, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2));
//        mainPanel.add(createFilterPane(),
//                new GridBagConstraintsCustom(3, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
        //paramPanel.add(newsPanel);
        //paramPanel.add(pixellonlatSwitch);
        //paramPanel.add(pixelPanel);

        return paramPanel;
    }

    private void updateParamPanel() {

        //System.out.println(pixellonlatSwitch.isSelected());
       // if (pixellonlatSwitch.isSelected()) {

            paramPanel.remove(paramPanel.getComponent(2));
            paramPanel.add(pixelPanel, new GridBagConstraintsCustom(0, 1, 3, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH));
            pixellonlatSwitch.setEnabled(false);
        pixellonlatSwitch.setBorderPainted(false);
//        } else {
//            paramPanel.remove(pixelPanel);
//            paramPanel.add(newsPanel, new GridBagConstraintsCustom(0, 0, 3, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH));
//        }
        // System.out.println("component 0: " + paramPanel.getComponent(0).getName());
        //System.out.println("component 1: " +paramPanel.getComponent(1).getName());
        //System.out.println("component 2: " +paramPanel.getComponent(2).getName());
        paramPanel.validate();
        paramPanel.repaint(50L);
    }

//    private JPanel createParamPanel(ArrayList<ParamInfo> paramList) {
//
//        JPanel paramPanel = new JPanel();
//        Dimension paramPanelDimension = new Dimension(100, 100);
//        TableLayout lonlatLayout = new TableLayout(2);
//        paramPanel.setLayout(lonlatLayout);
//        paramPanel.setPreferredSize(paramPanelDimension);
//
//        Iterator itr = paramList.iterator();
//        while (itr.hasNext()) {
//            final ParamInfo pi = (ParamInfo) itr.next();
//            if (!(pi.getName().equals(ParamUtils.IFILE) || pi.getName().equals("infile") || pi.getName().equals(ParamUtils.OFILE))) {
//                paramPanel.add(makeOptionField(pi));
//            }
//        }
//
//        paramPanel.addMouseListener(new MouseListener() {
//            @Override
//            public void mouseClicked(MouseEvent mouseEvent) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public void mousePressed(MouseEvent mouseEvent) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent mouseEvent) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public void mouseEntered(MouseEvent mouseEvent) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public void mouseExited(MouseEvent mouseEvent) {
//                //To change body of implemented methods use File | Settings | File Templates.
//
//                validateParams();
//            }
//        });
//        return paramPanel;
//    }
//
//    private JPanel makeOptionField(final ParamInfo pi) {
//
//        final String optionName = pi.getName();
//        final String optionValue = pi.getValue();
//        final JPanel optionPanel = new JPanel();
//        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
//        optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        optionPanel.add(new JLabel(optionName));
//
//
//        final PropertyContainer vc = new PropertyContainer();
//        vc.addProperty(Property.create(optionName, optionValue));
//        vc.getDescriptor(optionName).setDisplayName(optionName);
//
////        final ValueRange valueRange = new ValueRange(-180, 180);
////
////
////        vc.getDescriptor(optionName).setValueRange(valueRange);
//
//        final BindingContext ctx = new BindingContext(vc);
//        final JTextField field = new JTextField();
//        field.setColumns(8);
//        field.setHorizontalAlignment(JFormattedTextField.LEFT);
//        System.out.println(optionName + "  " + optionValue);
//        ctx.bind(optionName, field);
//
//        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {
//
//            @Override
//            public void propertyChange(PropertyChangeEvent pce) {
//                pi.setValue(field.getText());
//
//            }
//        });
//
//        optionPanel.add(field);
//
//        return optionPanel;
//
//    }

    private void validateParams() {

    }

    //int lonLatChanges = 0;

    protected void handleParamChanged() {
        if (lonlat2pixline.isAllParamsValid()) {
            pixellonlatSwitch.setEnabled(true);
            pixellonlatSwitch.setBorderPainted(true);
        }
    }

}
