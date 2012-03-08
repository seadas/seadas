package gov.nasa.gsfc.seadas.processing.l2gen;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class L2genPrint {

    boolean debug = true;
    boolean admin = true;
    boolean user = true;


    public L2genPrint() {
    }


    public void debug(String message) {
        if (debug) {
            System.out.println(message);
        }
    }

    public void userlog(String message) {
        if (user) {
            System.out.println(message);
        }
    }

    public void adminlog(String message) {
        if (admin) {
            System.out.println(message);
        }
    }
}
