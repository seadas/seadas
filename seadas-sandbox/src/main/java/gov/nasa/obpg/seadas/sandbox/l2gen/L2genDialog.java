/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package gov.nasa.obpg.seadas.sandbox.l2gen;

import com.bc.ceres.binding.ConversionException;
import com.bc.ceres.binding.ValidationException;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.dataop.dem.ElevationModelDescriptor;
import org.esa.beam.framework.dataop.dem.ElevationModelRegistry;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.ui.*;
import org.esa.beam.framework.ui.AppContext;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.HashMap;
import java.util.Map;

class L2genDialog extends SingleTargetProductDialog {

    private static final String OPERATOR_NAME = "Reproject";
    private final L2genForm form;

    public static void main(String[] args) {
        final DefaultAppContext context = new DefaultAppContext("L2gen");
        final L2genDialog dialog = new L2genDialog(true, "L2genTestDialog", null, context);
        dialog.show();

    }

    L2genDialog(boolean orthorectify, final String title, final String helpID, AppContext appContext) {
        super(appContext, title, ID_APPLY_CLOSE, helpID);
        form = new L2genForm(getTargetProductSelector(), orthorectify, appContext);

        final OperatorSpi operatorSpi = GPF.getDefaultInstance().getOperatorSpiRegistry().getOperatorSpi(OPERATOR_NAME);

        ParameterUpdater parameterUpdater = new ReprojectionParameterUpdater();

        OperatorParameterSupport parameterSupport = new OperatorParameterSupport(operatorSpi.getOperatorClass(),
                                                                                 null,
                                                                                 null,
                                                                                 parameterUpdater);
        OperatorMenu operatorMenu = new OperatorMenu(this.getJDialog(),
                                                     operatorSpi.getOperatorClass(),
                                                     parameterSupport,
                                                     helpID);

        getJDialog().setJMenuBar(operatorMenu.createDefaultMenu());
    }

    @Override
    protected boolean verifyUserInput() {
        if (form.getSourceProduct() == null) {
            showErrorDialog("No product to reproject selected.");
            return false;
        }

        final CoordinateReferenceSystem crs = form.getSelectedCrs();
        if (crs == null) {
            showErrorDialog("No 'Coordinate Reference System' selected.");
            return false;
        }

        String externalDemName = form.getExternalDemName();
        if (externalDemName != null) {
            final ElevationModelRegistry elevationModelRegistry = ElevationModelRegistry.getInstance();
            final ElevationModelDescriptor demDescriptor = elevationModelRegistry.getDescriptor(externalDemName);
            if (demDescriptor == null) {
                showErrorDialog("The DEM '" + externalDemName + "' is not supported.");
                close();
                return false;
            }
            if (demDescriptor.isInstallingDem()) {
                showErrorDialog("The DEM '" + externalDemName + "' is currently being installed.");
                close();
                return false;
            }
            if (!demDescriptor.isDemInstalled()) {
                final boolean ok = demDescriptor.installDemFiles(getParent());
                if (ok) {
                    // close dialog becuase DEM will be installed first
                    close();
                }
                return false;
            }
        }
        return true;
    }

    @Override
    protected Product createTargetProduct() throws Exception {
        final Map<String, Product> productMap = form.getProductMap();
        final Map<String, Object> parameterMap = new HashMap<String, Object>();
        form.updateParameterMap(parameterMap);
        return GPF.createProduct(OPERATOR_NAME, parameterMap, productMap);
    }

    @Override
    public int show() {
        form.prepareShow();
        setContent(form);
        return super.show();
    }

    @Override
    public void hide() {
        form.prepareHide();
        super.hide();
    }

    private class ReprojectionParameterUpdater implements ParameterUpdater {

        @Override
        public void handleParameterSaveRequest(Map<String, Object> parameterMap) {
            form.updateParameterMap(parameterMap);
        }

        @Override
        public void handleParameterLoadRequest(Map<String, Object> parameterMap) throws ValidationException, ConversionException {
            form.updateFormModel(parameterMap);
        }
    }
}
