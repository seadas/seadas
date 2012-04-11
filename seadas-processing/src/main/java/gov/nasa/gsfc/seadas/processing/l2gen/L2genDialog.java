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

package gov.nasa.gsfc.seadas.processing.l2gen;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.ui.*;
import org.esa.beam.framework.ui.AppContext;

class L2genDialog extends SingleTargetProductDialog {

    private final L2genForm form;

    public static void main(String[] args) {
        final DefaultAppContext context = new DefaultAppContext("L2gen");
        final L2genDialog dialog = new L2genDialog("L2genTestDialog", null, context);
        dialog.show();

    }

    L2genDialog(final String title, final String helpID, AppContext appContext) {
        super(appContext, title, ID_APPLY_CLOSE, helpID);
        //form = new L2genForm(getTargetProductSelector(), appContext);
        form = new L2genForm(appContext);
    }

    @Override
    protected boolean verifyUserInput() {
        if (form.getSourceProduct() == null) {
            showErrorDialog("No product to reproject selected.");
            return false;
        }

        return true;
    }

    @Override
    protected Product createTargetProduct() throws Exception {

        return null;
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


}
