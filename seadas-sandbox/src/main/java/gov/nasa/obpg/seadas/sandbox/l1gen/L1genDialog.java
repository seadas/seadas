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

package gov.nasa.obpg.seadas.sandbox.l1gen;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.ui.*;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModelessDialog;

import javax.swing.*;

class L1genDialog extends ModelessDialog {

    private final L1genForm form;

    public static void main(String[] args) {
        final DefaultAppContext context = new DefaultAppContext("L1gen");
        final L1genDialog dialog = new L1genDialog(context, "L1genTestDialog", null);
        dialog.show();

    }

    L1genDialog(AppContext appContext, final String title, final String helpID) {
        super(appContext.getApplicationWindow(), title, ID_APPLY_CLOSE, helpID);
        form = new L1genForm(appContext);
        setContent(form);

        AbstractButton button = getButton(ID_APPLY);
        button.setText("Run");
        button.setMnemonic('R');
        // button.setEnabled(true);

    }

    @Override
    protected boolean verifyUserInput() {
        if (form.getSourceProduct() == null) {
            showErrorDialog("No product selected.");
            return false;
        }

        return true;
    }


    /**
     * Called if the "Apply" button has been clicked.
     * The default implementation does nothing.
     * Clients should override this method to implement meaningful behaviour.
     */
    @Override
    protected void onApply() {
    }


}

