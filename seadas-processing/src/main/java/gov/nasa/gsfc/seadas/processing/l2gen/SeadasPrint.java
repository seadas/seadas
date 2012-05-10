package gov.nasa.gsfc.seadas.processing.l2gen;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class SeadasPrint {


    private static boolean debug = true;
    private static boolean admin = true;
    private static boolean user = true;


    public SeadasPrint() {
    }


    public static void debug(String message) {
        if (isDebug()) {
            System.out.println(message);
        }
    }

    public static void userlog(String message) {
        if (isUser()) {
            System.out.println(message);
        }
    }

    public static void adminlog(String message) {
        if (isAdmin()) {
            System.out.println(message);
        }
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        SeadasPrint.debug = debug;
    }

    public static boolean isAdmin() {
        return admin;
    }

    public static void setAdmin(boolean admin) {
        SeadasPrint.admin = admin;
    }

    public static boolean isUser() {
        return user;
    }

    public static void setUser(boolean user) {
        SeadasPrint.user = user;
    }
}
