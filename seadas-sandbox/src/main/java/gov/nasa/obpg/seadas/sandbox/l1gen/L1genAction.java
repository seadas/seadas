package gov.nasa.obpg.seadas.sandbox.l1gen;

import gov.nasa.obpg.seadas.sandbox.l1gen.L1genDialog;
import org.esa.beam.framework.ui.ModelessDialog;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.visat.actions.AbstractVisatAction;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class L1genAction extends AbstractVisatAction {

    private ModelessDialog dialog;

    @Override
    public void actionPerformed(CommandEvent event) {
        if (dialog == null) {
            dialog = new L1genDialog(getAppContext(), "l1gen", event.getCommand().getHelpId());
        }
        dialog.show();
    }

}
