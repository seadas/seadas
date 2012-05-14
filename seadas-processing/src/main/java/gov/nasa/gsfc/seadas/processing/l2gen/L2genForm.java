/*
Author: Danny Knowles
    Don Shea
*/

package gov.nasa.gsfc.seadas.processing.l2gen;

import gov.nasa.gsfc.seadas.processing.general.CloProgramUI;
import gov.nasa.gsfc.seadas.processing.general.ProcessorModel;
import gov.nasa.gsfc.seadas.processing.general.SourceProductFileSelector;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.AppContext;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;

//test
public class L2genForm extends JTabbedPane implements CloProgramUI {

    private static final String GUI_NAME = "l2gen";

    private L2genData l2genData;
    private L2genMainPanel l2genMainPanel;
    private ProcessorModel processorModel;

    L2genForm(AppContext appContext, String xmlFileName) {

        processorModel = new ProcessorModel(GUI_NAME, xmlFileName);

        l2genData = new L2genData();

        createMainTab(0);
        createProductsTab(1);
        createCategoryParamTabs(2);

        File iFile = l2genMainPanel.getSelectedIFile();
        if (iFile != null) {
            l2genData.setParamValue(L2genData.IFILE, iFile.toString());
        }

        l2genData.fireAllParamEvents();
    }


    private void createMainTab(final int tabIndex) {

        final String TAB_NAME = "Main";
        l2genMainPanel = new L2genMainPanel(l2genData);
        addTab(TAB_NAME, l2genMainPanel);
    }


    private void createProductsTab(final int tabIndex) {

        final String TAB_NAME = "Products";
        L2genProductsPanel l2genProductsPanel = new L2genProductsPanel((l2genData));
        addTab(TAB_NAME, l2genProductsPanel);

        l2genData.addPropertyChangeListener(L2genData.L2PROD, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                StringBuilder tabname = new StringBuilder(TAB_NAME);

                if (l2genData.isParamDefault(L2genData.L2PROD)) {
                    setTitleAt(tabIndex, tabname.toString());
                } else {
                    setTitleAt(tabIndex, tabname.append("*").toString());
                }
            }
        });


        l2genData.addPropertyChangeListener(L2genData.IFILE_VALIDATION_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                setEnabledAt(tabIndex, l2genData.isValidIfile());
            }
        });
    }


   
    private void createCategoryParamTabs(int startTabIndex) {
        int tabIndex = startTabIndex;

        for (final ParamCategoryInfo paramCategoryInfo : l2genData.getParamCategoryInfos()) {
            if (paramCategoryInfo.isAutoTab() && (paramCategoryInfo.getParamInfos().size() > 0)) {
                final int tabIndexFinal = tabIndex;

                L2genCategorizedParamsPanel l2genCategorizedParamsPanel = new L2genCategorizedParamsPanel(l2genData, paramCategoryInfo);
                addTab(paramCategoryInfo.getName(), l2genCategorizedParamsPanel);

                for (ParamInfo paramInfo : paramCategoryInfo.getParamInfos()) {
                    l2genData.addPropertyChangeListener(paramInfo.getName(), new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            StringBuilder stringBuilder = new StringBuilder(paramCategoryInfo.getName());

                            if (l2genData.isParamCategoryDefault(paramCategoryInfo)) {
                                setTitleAt(tabIndexFinal, stringBuilder.toString());
                            } else {
                                setTitleAt(tabIndexFinal, stringBuilder.append("*").toString());
                            }

                        }
                    });
                }


                l2genData.addPropertyChangeListener(L2genData.IFILE_VALIDATION_EVENT, new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        setEnabledAt(tabIndexFinal, l2genData.isValidIfile());
                    }
                });

                tabIndex++;
            }
        }
    }


    public ProcessorModel getProcessorModel() {
        processorModel.setParString(l2genData.getParString(false));
        processorModel.setOutputFile(new File(l2genData.getParamValue(L2genData.OFILE)));
        return processorModel;
    }

    private SourceProductFileSelector getSourceProductSelector() {
        if (l2genMainPanel == null) {
            return null;
        }

        return l2genMainPanel.getSourceProductSelector();
    }

    public Product getSelectedSourceProduct() {
        if (getSourceProductSelector() != null) {
            return getSourceProductSelector().getSelectedProduct();
        } else {
            return null;
        }
    }

    void prepareShow() {
        if (getSourceProductSelector() != null) {
            getSourceProductSelector().initProducts();
        }
    }

    void prepareHide() {
        if (getSourceProductSelector() != null) {
            getSourceProductSelector().releaseProducts();
        }
    }
}