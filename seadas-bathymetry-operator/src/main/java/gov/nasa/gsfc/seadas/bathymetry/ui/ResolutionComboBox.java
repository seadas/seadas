package gov.nasa.gsfc.seadas.bathymetry.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 9/5/12
 * Time: 11:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResolutionComboBox {
    private BathymetryData bathymetryData;

    private JLabel jLabel;
    private JComboBox jComboBox;
    private int validSelectedIndex;


    public ResolutionComboBox(final BathymetryData bathymetryData) {

        this.bathymetryData = bathymetryData;

        ArrayList<SourceFileInfo> jComboBoxArrayList = new ArrayList<SourceFileInfo>();
        ArrayList<String> toolTipsArrayList = new ArrayList<String>();
        ArrayList<Boolean> enabledArrayList = new ArrayList<Boolean>();

        for (SourceFileInfo sourceFileInfo : bathymetryData.getSourceFileInfos()) {
            jComboBoxArrayList.add(sourceFileInfo);

            if (sourceFileInfo.getDescription() != null) {
                toolTipsArrayList.add(sourceFileInfo.getDescription());
            } else {
                toolTipsArrayList.add(null);
            }

            enabledArrayList.add(new Boolean(sourceFileInfo.isEnabled()));
        }


        final SourceFileInfo[] jComboBoxArray = new SourceFileInfo[jComboBoxArrayList.size()];

        int i = 0;
        for (SourceFileInfo sourceFileInfo : bathymetryData.getSourceFileInfos()) {
            jComboBoxArray[i] = sourceFileInfo;
            i++;
        }

        final String[] toolTipsArray = new String[jComboBoxArrayList.size()];

        int j = 0;
        for (String validValuesToolTip : toolTipsArrayList) {
            toolTipsArray[j] = validValuesToolTip;
            j++;
        }

        final Boolean[] enabledArray = new Boolean[jComboBoxArrayList.size()];

        int k = 0;
        for (Boolean enabled : enabledArrayList) {
            enabledArray[k] = enabled;
            k++;
        }


        jComboBox = new JComboBox(jComboBoxArray);

        final MyComboBoxRenderer myComboBoxRenderer = new MyComboBoxRenderer();
        myComboBoxRenderer.setTooltipList(toolTipsArray);
        myComboBoxRenderer.setEnabledList(enabledArray);

        jComboBox.setRenderer(myComboBoxRenderer);
        jComboBox.setEditable(false);


        for (SourceFileInfo sourceFileInfo : jComboBoxArray) {
            if (sourceFileInfo == bathymetryData.getSourceFileInfo()) {
                jComboBox.setSelectedItem(sourceFileInfo);
            }
        }

        validSelectedIndex = jComboBox.getSelectedIndex();


        jLabel = new JLabel("Bathymetry Dataset");
        jLabel.setToolTipText("Determines which bathymetry source dataset to use");

        addControlListeners();
        addEventHandlers();


    }


    private void addEventHandlers() {
        bathymetryData.addPropertyChangeListener(BathymetryData.FILE_INSTALLED_EVENT2, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateJComboBox();
            }
        });


        bathymetryData.addPropertyChangeListener(BathymetryData.CONFIRMED_REQUEST_TO_INSTALL_FILE_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                SourceFileInfo sourceFileInfo = (SourceFileInfo) jComboBox.getSelectedItem();
                jComboBox.setSelectedIndex(getValidSelectedIndex());
            }
        });
    }

    private void addControlListeners() {
        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SourceFileInfo sourceFileInfo = (SourceFileInfo) jComboBox.getSelectedItem();

                if (sourceFileInfo.isEnabled()) {
                    bathymetryData.setSourceFileInfo(sourceFileInfo);
                    validSelectedIndex = jComboBox.getSelectedIndex();
                } else {
                    // restore to prior selection
//                    jComboBox.setSelectedIndex(selectedIndex);

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            bathymetryData.fireEvent(BathymetryData.PROMPT_REQUEST_TO_INSTALL_FILE_EVENT);
                        }
                    });
                }
            }
        });

    }

    public JLabel getjLabel() {
        return jLabel;
    }

    public JComboBox getjComboBox() {
        return jComboBox;
    }


    public void updateJComboBox() {

        ArrayList<SourceFileInfo> jComboBoxArrayList = new ArrayList<SourceFileInfo>();
        ArrayList<String> toolTipsArrayList = new ArrayList<String>();
        ArrayList<Boolean> enabledArrayList = new ArrayList<Boolean>();

        for (SourceFileInfo sourceFileInfo : bathymetryData.getSourceFileInfos()) {
            jComboBoxArrayList.add(sourceFileInfo);

            if (sourceFileInfo.getDescription() != null) {
                toolTipsArrayList.add(sourceFileInfo.getDescription());
            } else {
                toolTipsArrayList.add(null);
            }

            enabledArrayList.add(new Boolean(sourceFileInfo.isEnabled()));
        }


        final SourceFileInfo[] jComboBoxArray = new SourceFileInfo[jComboBoxArrayList.size()];

        int i = 0;
        for (SourceFileInfo sourceFileInfo : bathymetryData.getSourceFileInfos()) {
            jComboBoxArray[i] = sourceFileInfo;
            i++;
        }

        final String[] toolTipsArray = new String[jComboBoxArrayList.size()];

        int j = 0;
        for (String validValuesToolTip : toolTipsArrayList) {
            toolTipsArray[j] = validValuesToolTip;
            j++;
        }

        final Boolean[] enabledArray = new Boolean[jComboBoxArrayList.size()];

        int k = 0;
        for (Boolean enabled : enabledArrayList) {
            enabledArray[k] = enabled;
            k++;
        }


        final MyComboBoxRenderer myComboBoxRenderer = new MyComboBoxRenderer();
        myComboBoxRenderer.setTooltipList(toolTipsArray);
        myComboBoxRenderer.setEnabledList(enabledArray);

        jComboBox.setRenderer(myComboBoxRenderer);
        jComboBox.setEditable(false);


        for (SourceFileInfo sourceFileInfo : jComboBoxArray) {
            if (sourceFileInfo == bathymetryData.getSourceFileInfo()) {
                jComboBox.setSelectedItem(sourceFileInfo);
            }
        }

        validSelectedIndex = jComboBox.getSelectedIndex();


//        int i = 0;
//        for (SourceFileInfo sourceFileInfo : jComboBoxArray) {
//            enabledArray[i] = sourceFileInfo.isEnabled();
//            i++;
//        }
//
//
//        final MyComboBoxRenderer myComboBoxRenderer = new MyComboBoxRenderer();
//        myComboBoxRenderer.setTooltipList(toolTipsArray);
//        myComboBoxRenderer.setEnabledList(enabledArray);
//
//        jComboBox.setRenderer(myComboBoxRenderer);
//
//
//        for (SourceFileInfo sourceFileInfo : jComboBoxArray) {
//            if (sourceFileInfo == bathymetryData.getSourceFileInfo()) {
//                jComboBox.setSelectedItem(sourceFileInfo);
//            }
//        }
//
//        selectedIndex = jComboBox.getSelectedIndex();
//

    }

    public int getValidSelectedIndex() {
        return validSelectedIndex;
    }

    class MyComboBoxRenderer extends BasicComboBoxRenderer {

        private String[] tooltips;
        private Boolean[] enabledList;


//        public void MyComboBoxRenderer(String[] tooltips) {
//            this.tooltips = tooltips;
//        }


        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            if (isSelected) {
                if (-1 < index && index < tooltips.length) {
                    list.setToolTipText(tooltips[index]);
                }

                if (-1 < index && index < enabledList.length) {
                    if (enabledList[index] == true) {
                        setBackground(Color.blue);
                        setForeground(Color.black);
                    } else {
                        setBackground(Color.blue);
                        setForeground(Color.gray);
                    }
                }
            } else {
                if (-1 < index && index < enabledList.length) {
                    if (enabledList[index] == true) {
                        setBackground(Color.white);
                        setForeground(Color.black);
                    } else {
                        setBackground(Color.white);
                        setForeground(Color.gray);
                    }
                }
            }

            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
            return this;
        }

        public void setTooltipList(String[] tooltipList) {
            this.tooltips = tooltipList;
        }

        public void setEnabledList(Boolean[] enabledList) {
            this.enabledList = enabledList;
        }
    }

}
