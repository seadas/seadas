package gov.nasa.gsfc.seadas.processing.general;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.ValueRange;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.BindingContext;
import gov.nasa.gsfc.seadas.processing.l2gen.GridBagConstraintsCustom;
import gov.nasa.gsfc.seadas.processing.l2gen.ParamInfo;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/3/12
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExtractorUI extends JPanel implements CloProgramUI {

    private final String PIXEL_BUTTON_NAME = "Pixels";
    private final String LONLAT_BUTTON_NAME = "LonLat";

    private ProcessorModel l1aextract;
    private ProcessorModel lonlat2pixline;

    private final SourceProductFileSelector sourceProductSelector;
    private final OutputFileSelector outputFileSelector;


    private JPanel pixelPanel;
    private JPanel newsPanel;
    private JPanel paramPanel;

    private JRadioButton pixelButton;
    private JRadioButton newsButton;

    private JToggleButton pixellonlatSwitch;

    public ExtractorUI(String programName, String xmlFileName) {
        super(new BorderLayout());

        l1aextract = new ProcessorModel(programName, xmlFileName);
        lonlat2pixline = new ProcessorModel("lonlat2pixline", "lonlat2pixline.xml");
        //lonlat2pixline.hasOutputFile(false);
        pixelPanel = createParamPanel(l1aextract.getProgramParamList());
        newsPanel = createParamPanel(lonlat2pixline.getProgramParamList());
        pixelPanel.setName("pixelPanel");
        newsPanel.setName("newsPanel");

        sourceProductSelector = new SourceProductFileSelector(VisatApp.getApp(), "");
        //sourceProductSelector.setProcessorModel(l1aextract);
        sourceProductSelector.initProducts();

        outputFileSelector = new OutputFileSelector(VisatApp.getApp(), "Output File");
        this.setName("mainPanel");
        initUI();
    }


    //private void

    private File getGeoFileName(File inputFile) {

        String geoFileName = inputFile.getName();
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
            l1aextract.setInputFile(inputFile);

            lonlat2pixline.setInputFile(getGeoFileName(inputFile));
        }

        OutputFileSelectorModel outputFileSelectorModel = outputFileSelector.getModel();
        if (outputFileSelectorModel != null) {
            l1aextract.setOutputFileDir(outputFileSelectorModel.getProductDir());
            l1aextract.setOutputFileName(outputFileSelectorModel.getProductFileName());
        }
    }

    public ProcessorModel getProcessorModel() {

        updateProcessorModel();
        if (!pixellonlatSwitch.isSelected()) {
            computePixelsFromLonLat();
        }
        return l1aextract;
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
            //final ProcessObserver processObserver = new ProcessObserver(process, programName, pm);

            //int exitCode = process.exitValue();
            try {
                process.wait();
            } catch (Exception e) {

            }

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            String[] tmp;
            while ((line = br.readLine()) != null) {

                System.out.println(line);
                if (line.indexOf("=") != -1 ) {
                                tmp = line.split("=");

                    System.out.printf("Option name: %1$s  Value: %2$s %n", tmp[0], tmp[1]);
                    l1aextract.updateParamInfo(tmp[0], tmp[1]);
                    //System.out.println();
                }
            }

        } catch (IOException ioe) {

        }

    }

    private JPanel getParamPanel() {
//        pixelButton = new JRadioButton();
//        pixelButton.setText("Pixels");
//        newsButton = new JRadioButton();
//        newsButton.setText("LonLat");
//        newsButton.setSelected(true);

        pixellonlatSwitch = new JToggleButton();
        pixellonlatSwitch.setText("PixLines");
        pixellonlatSwitch.setBorderPainted(false);

        pixellonlatSwitch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                if (pixellonlatSwitch.isSelected()) {
                    pixellonlatSwitch.setBorderPainted(true);
                    System.out.println(pixellonlatSwitch.isSelected());
                    updateProcessorModel();
                    computePixelsFromLonLat();
                    pixelPanel = createParamPanel(l1aextract.getProgramParamList());
                } else {
                    pixellonlatSwitch.setBorderPainted(false);
                }
                updateParamPanel();
            }
        });

//        pixelButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent actionEvent) {
//
//                newsButton.setSelected(!pixelButton.isSelected());
//                //computePixelsFromLonLat();
//                updateParamPanel();
//
//            }
//        });
//
//        newsButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent actionEvent) {
//                pixelButton.setSelected(!newsButton.isSelected());
//
//                updateParamPanel();
//
//            }
//        });


        //paramPanelLayout.setCellColspan(0, 0, 1);
        //paramPanelLayout.setRowFill(1, TableLayout.Fill.HORIZONTAL);

        paramPanel = new JPanel(new GridBagLayout());
        //paramPanel.setLayout(paramPanelLayout);

        paramPanel.add(newsPanel, new GridBagConstraintsCustom(0, 0, 3, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH));
        paramPanel.add(pixellonlatSwitch, new GridBagConstraintsCustom(1, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        //paramPanel.add(pixelButton, new GridBagConstraintsCustom(1, 0, 0, 0, GridBagConstraints.CENTER , GridBagConstraints.NONE ));


        return paramPanel;
    }

    private void initUI() {


        add(sourceProductSelector.createDefaultPanel(), BorderLayout.NORTH);
        add(getParamPanel(), BorderLayout.CENTER);
        add(outputFileSelector.createDefaultPanel(), BorderLayout.SOUTH);
    }

    private void updateParamPanel() {

        System.out.println(pixellonlatSwitch.isSelected());
        if (pixellonlatSwitch.isSelected()) {

            paramPanel.remove(newsPanel);
            paramPanel.add(pixelPanel, new GridBagConstraintsCustom(0, 0, 3, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH));

        } else {
            paramPanel.remove(pixelPanel);
            paramPanel.add(newsPanel, new GridBagConstraintsCustom(0, 0, 3, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH));
        }
        paramPanel.validate();
        paramPanel.repaint(50L);
    }

    private JPanel createParamPanel(ArrayList<ParamInfo> paramList) {

        JPanel paramPanel = new JPanel();
        Dimension paramPanelDimension = new Dimension(100, 100);
        TableLayout lonlatLayout = new TableLayout(2);
        paramPanel.setLayout(lonlatLayout);
        //paramPanel.setBorder(new EmptyBorder(null));
        paramPanel.setPreferredSize(paramPanelDimension);

        Iterator itr = paramList.iterator();
        while (itr.hasNext()) {
            final ParamInfo pi = (ParamInfo) itr.next();
            if (!(pi.getName().equals(ParamUtils.IFILE) || pi.getName().equals("infile") || pi.getName().equals(ParamUtils.OFILE))) {
                paramPanel.add(makeOptionField(pi));
            }
        }

        paramPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                //To change body of implemented methods use File | Settings | File Templates.

                validateParams();
            }
        });
        return paramPanel;
    }

    private JPanel makeOptionField(final ParamInfo pi) {

        final String optionName = pi.getName();
        final String optionValue = pi.getValue();
        final JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionPanel.add(new JLabel(optionName));


        final PropertyContainer vc = new PropertyContainer();
        vc.addProperty(Property.create(optionName, optionValue));
        vc.getDescriptor(optionName).setDisplayName(optionName);

        final ValueRange valueRange = new ValueRange(-180, 180);


        vc.getDescriptor(optionName).setValueRange(valueRange);

        final BindingContext ctx = new BindingContext(vc);
        final JTextField field = new JTextField();
        field.setColumns(8);
        field.setHorizontalAlignment(JFormattedTextField.LEFT);
        System.out.println(optionName + "  " + optionValue);
        ctx.bind(optionName, field);

        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                pi.setValue(field.getText());

            }
        });

        optionPanel.add(field);

        return optionPanel;

    }

    private void validateParams() {

    }

}
