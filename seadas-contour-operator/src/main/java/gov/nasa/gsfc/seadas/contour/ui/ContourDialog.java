package gov.nasa.gsfc.seadas.contour.ui;

import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.tool.ToolButtonFactory;

import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 9/9/13
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContourDialog extends JDialog {

    private ContourData contourData = null;
    private Component helpButton = null;
    private HelpBroker helpBroker = null;

    private final static String HELP_ID = "contourLines";
    private final static String HELP_ICON = "icons/Help24.gif";

    public ContourDialog(ContourData contourData, boolean masksCreated) {
        this.contourData = contourData;

        initHelpBroker();

        if (helpBroker != null) {
            helpButton = getHelpButton(HELP_ID);
        }

        if (masksCreated) {
            createNotificationUI();
        } else {
            createContourUI();
        }
    }


    protected Component getHelpButton(String helpId) {
        if (helpId != null) {

            final AbstractButton helpButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon(HELP_ICON),
                    false);

            HelpSet helpSet = helpBroker.getHelpSet();
            helpBroker.setCurrentID(helpId);

            if (helpButton != null) {
                helpButton.setToolTipText("Help");
                helpButton.setName("helpButton");
                helpBroker.enableHelpKey(helpButton, helpId, helpSet);
                helpBroker.enableHelpOnButton(helpButton, helpId, helpSet);
            }

            return helpButton;
        }

        return null;
    }


    private void initHelpBroker() {
        HelpSet helpSet = HelpSys.getHelpSet();
        if (helpSet != null) {
            helpBroker = helpSet.createHelpBroker();
            if (helpBroker instanceof DefaultHelpBroker) {
                DefaultHelpBroker defaultHelpBroker = (DefaultHelpBroker) helpBroker;
                defaultHelpBroker.setActivationWindow(this);
            }
        }
    }




    public final void createNotificationUI() {
        JButton createMasks = new JButton("Create New Masks");
        createMasks.setPreferredSize(createMasks.getPreferredSize());
        createMasks.setMinimumSize(createMasks.getPreferredSize());
        createMasks.setMaximumSize(createMasks.getPreferredSize());


        createMasks.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                contourData.setDeleteMasks(true);
                dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(cancelButton.getPreferredSize());
        cancelButton.setMinimumSize(cancelButton.getPreferredSize());
        cancelButton.setMaximumSize(cancelButton.getPreferredSize());

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });

        JLabel filler = new JLabel("                            ");


        JPanel buttonsJPanel = new JPanel(new GridBagLayout());
        buttonsJPanel.add(cancelButton,
                new ExGridBagConstraints(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        buttonsJPanel.add(filler,
                new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        buttonsJPanel.add(createMasks,
                new ExGridBagConstraints(2, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        buttonsJPanel.add(helpButton,
                new ExGridBagConstraints(3, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));


        JLabel jLabel = new JLabel("Masks have already been created for this product");

        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.add(jLabel,
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        jPanel.add(buttonsJPanel,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE));


        add(jPanel);

        setModalityType(ModalityType.APPLICATION_MODAL);


        setTitle("Land Masks");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();


        setPreferredSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setSize(getPreferredSize());

    }

    public final void createContourUI() {


        final int rightInset = 5;

        JPanel contourPanel = new JPanel(new GridBagLayout());
        contourPanel.setBorder(BorderFactory.createTitledBorder(""));

        JTextField minValueField = new JFormattedTextField();
        minValueField.setColumns(8);
        minValueField.setPreferredSize(minValueField.getPreferredSize());
        minValueField.setMaximumSize(minValueField.getPreferredSize());
        minValueField.setMinimumSize(minValueField.getPreferredSize());
        minValueField.setName("Start Value");

        JTextField maxValueField = new JFormattedTextField();
        maxValueField.setColumns(8);
        maxValueField.setPreferredSize(maxValueField.getPreferredSize());
        maxValueField.setMaximumSize(maxValueField.getPreferredSize());
        maxValueField.setMinimumSize(maxValueField.getPreferredSize());
        maxValueField.setName("End Value");

        JTextField numLevelsField = new JFormattedTextField();
        numLevelsField.setColumns(8);
        numLevelsField.setPreferredSize(numLevelsField.getPreferredSize());
        numLevelsField.setMaximumSize(numLevelsField.getPreferredSize());
        numLevelsField.setMinimumSize(numLevelsField.getPreferredSize());
        numLevelsField.setName("Start Value");

        final JRadioButton log = new JRadioButton();
        final JRadioButton linear = new JRadioButton();
        final JRadioButton custom = new JRadioButton();
        custom.setSelected(true);

        log.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 if (log.isSelected()) {
                    linear.setSelected(false);
                     custom.setSelected(false);
                 }  //else {
//                     linear.setSelected(true);
//                 }
             }
         });

        linear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (linear.isSelected()) {
                    log.setSelected(false);
                    custom.setSelected(false);
                } //else {
                    //log.setSelected(true);
                //}
            }
        });

        custom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (custom.isSelected()) {
                    log.setSelected(false);
                    linear.setSelected(false);

                } //else {
                    //log.setSelected(true);
                //}
            }
        });

        JPanel selectionPanel = new JPanel(new GridBagLayout());
//        TableLayout tableLayout = new TableLayout(1);
//        tableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
//        selectionPanel.setLayout(tableLayout);
//        selectionPanel.add(log);
//        selectionPanel.add(linear);
//        selectionPanel.add(custom);

        selectionPanel.add(new JLabel("Log"),
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        selectionPanel.add(log,
                new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        selectionPanel.add(new JLabel("Linear"),
                new ExGridBagConstraints(2, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        selectionPanel.add(linear,
                new ExGridBagConstraints(3, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        selectionPanel.add(new JLabel("Custom"),
                new ExGridBagConstraints(4, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        selectionPanel.add(custom,
                new ExGridBagConstraints(5, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));


        JButton more = new JButton("More...");

         System.out.println(System.getProperty("java.classpath"));
        contourPanel.add(new JLabel("Number of Contour Levels:"),
                new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        contourPanel.add(numLevelsField,
                new ExGridBagConstraints(2, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        contourPanel.add(new JLabel("Start Value"),
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        contourPanel.add(minValueField,
                new ExGridBagConstraints(1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        contourPanel.add(new JLabel("End Value"),
                new ExGridBagConstraints(2, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));

        contourPanel.add(maxValueField,
                new ExGridBagConstraints(3, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

//        contourPanel.add(new JLabel("Log"),
//                new ExGridBagConstraints(0, 2, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));
//
//        contourPanel.add(log,
//                new ExGridBagConstraints(1, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
//
//        contourPanel.add(new JLabel("Linear"),
//                new ExGridBagConstraints(2, 2, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));
//
//        contourPanel.add(linear,
//                new ExGridBagConstraints(3, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
//
//
//        contourPanel.add(new JLabel("Custom"),
//                new ExGridBagConstraints(4, 2, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, rightInset)));
//
//        contourPanel.add(custom,
//                new ExGridBagConstraints(5, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        contourPanel.add(selectionPanel,
                new ExGridBagConstraints(1, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));

        JButton createContourLines = new JButton("Create Contour Lines");
        createContourLines.setPreferredSize(createContourLines.getPreferredSize());
        createContourLines.setMinimumSize(createContourLines.getPreferredSize());
        createContourLines.setMaximumSize(createContourLines.getPreferredSize());


        createContourLines.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                contourData.setCreateMasks(true);
                dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(cancelButton.getPreferredSize());
        cancelButton.setMinimumSize(cancelButton.getPreferredSize());
        cancelButton.setMaximumSize(cancelButton.getPreferredSize());

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });

        JLabel filler = new JLabel("                            ");


        JPanel buttonsJPanel = new JPanel(new GridBagLayout());
        buttonsJPanel.add(cancelButton,
                new ExGridBagConstraints(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE));
        buttonsJPanel.add(filler,
                new ExGridBagConstraints(1, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        buttonsJPanel.add(createContourLines,
                new ExGridBagConstraints(2, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));
        buttonsJPanel.add(helpButton,
                new ExGridBagConstraints(3, 0, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));

        createContourLines.setAlignmentX(0.5f);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(contourPanel,
                new ExGridBagConstraints(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));
        mainPanel.add(buttonsJPanel,
                new ExGridBagConstraints(0, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5));


        add(mainPanel);


        setModalityType(ModalityType.APPLICATION_MODAL);


        setTitle("Contour Lines");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();


        setPreferredSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setSize(getPreferredSize());
    }

}
