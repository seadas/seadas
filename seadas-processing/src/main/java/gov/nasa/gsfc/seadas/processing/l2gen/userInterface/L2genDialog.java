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

package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.ui.SingleTargetProductDialog;
import org.esa.snap.rcp.util.Dialogs;
import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.DefaultAppContext;


class L2genDialog extends SingleTargetProductDialog {

    private L2genForm form;
    OCSSW ocssw;

    public static void main(String[] args) {
        final DefaultAppContext context = new DefaultAppContext("L2gen");
        final L2genDialog dialog = new L2genDialog("L2genTestDialog", null, context);
        dialog.show();

    }

    L2genDialog(final String title, final String helpID, AppContext appContext) {
        super(appContext, title, ID_APPLY_CLOSE_HELP, helpID);
        //form = new L2genForm(getTargetProductSelector(), appContext);
        form = new L2genForm(appContext, "test", ocssw);

    }

    @Override
    protected boolean verifyUserInput() {
        if (form.getSelectedSourceProduct() == null) {
            Dialogs.showError("No product to reproject selected.");
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
