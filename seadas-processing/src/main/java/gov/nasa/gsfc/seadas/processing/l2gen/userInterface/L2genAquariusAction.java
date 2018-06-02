package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.common.CallCloProgramAction;
import gov.nasa.gsfc.seadas.processing.common.CloProgramUI;
import org.esa.snap.ui.AppContext;


/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 10/24/12
 * Time: 3:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genAquariusAction extends CallCloProgramAction {


    @Override
    public CloProgramUI getProgramUI(AppContext appContext) {
        return new L2genForm(appContext, getXmlFileName(), L2genData.Mode.L2GEN_AQUARIUS, ocssw);
    }

}