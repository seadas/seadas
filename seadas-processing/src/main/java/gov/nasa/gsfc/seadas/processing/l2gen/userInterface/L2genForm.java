/*
Author: Danny Knowles
    Don Shea
*/

package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import gov.nasa.gsfc.seadas.processing.core.*;
import gov.nasa.gsfc.seadas.processing.common.*;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.AppContext;


public class L2genForm extends JPanel implements CloProgramUI {


    private L2genData masterData;

    private L2genMainPanel l2genMainPanel;
    private JCheckBox openInAppCheckBox;
    private final JTabbedPane jTabbedPane = new JTabbedPane();
    private int tabIndex;
    private JCheckBox keepParamsCheckbox;


    public L2genForm(AppContext appContext, String xmlFileName, final File iFile, boolean showIOFields, L2genData.Mode mode, boolean keepParams, boolean ifileIndependent, OCSSW ocssw) {

        masterData = L2genData.getNew(mode, ocssw);
        masterData.setIfileIndependentMode(ifileIndependent);
        masterData.setKeepParams(keepParams);


        setOpenInAppCheckBox(new JCheckBox("Open in " + appContext.getApplicationName()));
        getOpenInAppCheckBox().setSelected(true);

        keepParamsCheckbox = new JCheckBox("Keep params when new ifile is selected");
        keepParamsCheckbox.setSelected(keepParams);

        keepParamsCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                masterData.setKeepParams(keepParamsCheckbox.isSelected());
            }
        });


        masterData.showIOFields = showIOFields;


        SnapApp snapApp = SnapApp.getDefault();
        snapApp.setStatusBarMessage("Initializing L2gen GUI");
        ProgressMonitorSwingWorker pmSwingWorker = new ProgressMonitorSwingWorker(snapApp.getMainFrame(),
                masterData.getGuiName()) {

            @Override
            protected Void doInBackground(com.bc.ceres.core.ProgressMonitor pm) throws Exception {

                pm.beginTask("Initializing " + masterData.getGuiName(), 2);

                try {

                    masterData.initXmlBasedObjects();

                    createMainTab();
                    createProductsTab();
                    createCategoryParamTabs();

                    tabIndex = jTabbedPane.getSelectedIndex();

                    getjTabbedPane().addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent evt) {
                            tabChangeHandler();
                        }
                    });

                    setLayout(new GridBagLayout());

                    add(getjTabbedPane(),
                            new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH));
//                    add(getOpenInAppCheckBox(),
//                            new GridBagConstraintsCustom(0, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));


                    JPanel bottomPanel = new JPanel(new GridBagLayout());
                    bottomPanel.add(keepParamsCheckbox,
                            new GridBagConstraintsCustom(0, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
                    bottomPanel.add(getOpenInAppCheckBox(),
                            new GridBagConstraintsCustom(1, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE));


                    add(bottomPanel,
                            new GridBagConstraintsCustom(0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH));


                    masterData.disableEvent(L2genData.PARSTRING);
                    masterData.disableEvent(L2genData.L2PROD);

                    if (!masterData.isIfileIndependentMode()) {
                        if (iFile != null) {
                            masterData.setInitialValues(iFile);
                        } else {
                            masterData.setInitialValues(getInitialSelectedSourceFile());
                        }
                    }

                    //todo this crazy for multilevel processor
//                    if (masterData.isIfileIndependentMode()) {
//                        masterData.fireEvent(L2genData.IFILE);
//                    }

                    masterData.fireAllParamEvents();
                    masterData.enableEvent(L2genData.L2PROD);
                    masterData.enableEvent(L2genData.PARSTRING);
                    pm.worked(1);

                } catch (IOException e) {
                    pm.done();
                    SimpleDialogMessage dialog = new SimpleDialogMessage(null, "ERROR: " + e.getMessage());
                    dialog.setVisible(true);
                    dialog.setEnabled(true);


                } finally {
                    pm.done();
                }
                return null;
            }


        };

        pmSwingWorker.executeWithBlocking();


        masterData.setInitialized(true);
    }

    public L2genForm(AppContext appContext, String xmlFileName, OCSSW ocssw) {

        this(appContext, xmlFileName, null, true, L2genData.Mode.L2GEN, false, false, ocssw);
    }

    public L2genForm(AppContext appContext, String xmlFileName, L2genData.Mode mode, OCSSW ocssw) {

        this(appContext, xmlFileName, null, true, mode, false, false, ocssw);
    }


    private void tabChangeHandler() {
        int oldTabIndex = tabIndex;
        int newTabIndex = jTabbedPane.getSelectedIndex();
        tabIndex = newTabIndex;
        masterData.fireEvent(L2genData.TAB_CHANGE, oldTabIndex, newTabIndex);
    }

    private void createMainTab() {

        final String TAB_NAME = "Main";
        final int tabIndex = jTabbedPane.getTabCount();
        l2genMainPanel = new L2genMainPanel(masterData, tabIndex);
        jTabbedPane.addTab(TAB_NAME, l2genMainPanel.getjPanel());
    }


    private void createProductsTab() {

        final String TAB_NAME = "Products";
        L2genProductsPanel l2genProductsPanel = new L2genProductsPanel(masterData);
        jTabbedPane.addTab(TAB_NAME, l2genProductsPanel);
        final int tabIndex = jTabbedPane.getTabCount() - 1;

        masterData.addPropertyChangeListener(L2genData.L2PROD, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                StringBuilder tabname = new StringBuilder(TAB_NAME);

                if (masterData.isParamDefault(L2genData.L2PROD)) {
                    jTabbedPane.setTitleAt(tabIndex, tabname.toString());
                } else {
                    jTabbedPane.setTitleAt(tabIndex, tabname.append("*").toString());
                }
            }
        });


        masterData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jTabbedPane.setEnabledAt(tabIndex, masterData.isValidIfile());
            }
        });
    }


    private void createCategoryParamTabs() {

        for (final L2genParamCategoryInfo paramCategoryInfo : masterData.getParamCategoryInfos()) {
            if (paramCategoryInfo.isAutoTab() && (paramCategoryInfo.getParamInfos().size() > 0)) {

                L2genCategorizedParamsPanel l2genCategorizedParamsPanel = new L2genCategorizedParamsPanel(masterData, paramCategoryInfo);
                jTabbedPane.addTab(paramCategoryInfo.getName(), l2genCategorizedParamsPanel);
                final int tabIndex = jTabbedPane.getTabCount() - 1;

                /*
                    Add titles to each of the tabs, adding (*) where tab contains a non-default parameter
                 */
                for (ParamInfo paramInfo : paramCategoryInfo.getParamInfos()) {
                    masterData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            StringBuilder stringBuilder = new StringBuilder(paramCategoryInfo.getName());

                            if (masterData.isParamCategoryDefault(paramCategoryInfo)) {
                                jTabbedPane.setTitleAt(tabIndex, stringBuilder.toString());
                            } else {
                                jTabbedPane.setTitleAt(tabIndex, stringBuilder.append("*").toString());
                            }

                        }
                    });
                }
// todo new
                if (masterData.isIfileIndependentMode()) {
                    jTabbedPane.setEnabled(true);
                }

                masterData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        jTabbedPane.setEnabledAt(tabIndex, masterData.isValidIfile());
                    }
                });
            }
        }
    }

    @Override
    public JPanel getParamPanel() {
        return this;
    }

    public ProcessorModel getProcessorModel() {
        return masterData.getProcessorModel();
    }

    public String getParamString() {
        return masterData.getParString();
    }

    public void setParamString(String paramString) {
        masterData.setParString(paramString, false);
    }

    public Product getInitialSelectedSourceProduct() {
        return SnapApp.getDefault().getSelectedProduct(SnapApp.SelectionSourceHint.AUTO);
    }

    public File getInitialSelectedSourceFile() {
        if (getInitialSelectedSourceProduct() != null) {
            return getInitialSelectedSourceProduct().getFileLocation();
        }
        return null;
    }


    public SeadasFileSelector getSourceProductSelector() {
        return l2genMainPanel.getPrimaryIOFilesPanel().getIfileSelector().getFileSelector();
    }


    public File getSelectedSourceProduct() {
        if (getSourceProductSelector() != null) {
            return getSourceProductSelector().getSelectedFile();
        }

        return null;
    }


    public void prepareShow() {
        if (getSourceProductSelector() != null) {
            getSourceProductSelector().initProducts();
        }
    }

    public void prepareHide() {
        if (getSourceProductSelector() != null) {
            getSourceProductSelector().releaseFiles();
        }
    }


    public boolean isOpenOutputInApp() {
        if (getOpenInAppCheckBox() != null) {
            return getOpenInAppCheckBox().isSelected();
        }
        return true;
    }

    public JCheckBox getOpenInAppCheckBox() {
        return openInAppCheckBox;
    }

    public void setOpenInAppCheckBox(JCheckBox openInAppCheckBox) {
        this.openInAppCheckBox = openInAppCheckBox;
    }

    public JTabbedPane getjTabbedPane() {
        return jTabbedPane;
    }

//
//    public L2genData getMasterData() {
//        return masterData;
//    }


}