package gov.nasa.gsfc.seadas.processing.core;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 5/31/13
 * Time: 10:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class L2genGlobals2 {


    private static L2genGlobals2 l2genGlobals2 = null;

    public boolean ifileIndependentMode = false;

    private static void init() {
        if (l2genGlobals2 == null) {
            l2genGlobals2 = new L2genGlobals2();
        }
    }


    public static boolean isIfileIndependentMode() {
        init();
        return l2genGlobals2.ifileIndependentMode;
    }

    public static void setIfileIndependentMode(boolean ifileIndependentMode) {
        init();
        l2genGlobals2.ifileIndependentMode = ifileIndependentMode;
    }
}
