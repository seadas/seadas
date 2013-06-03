package gov.nasa.gsfc.seadas.processing.core;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 5/31/13
 * Time: 10:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genGlobals {


    private static L2genGlobals  l2genGlobals = null;

    private boolean  ifileIndependentMode = false;


    public static L2genGlobals getL2genGlobals() {
        if (l2genGlobals == null) {
            l2genGlobals = new L2genGlobals();
        }
        return l2genGlobals;
    }

    public boolean isIfileIndependentMode() {
        return ifileIndependentMode;
    }

    public void setIfileIndependentMode(boolean ifileIndependentMode) {
        this.ifileIndependentMode = ifileIndependentMode;
    }
}
