package gov.nasa.gsfc.seadas.ocsswws.auth;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/11/13
 * Time: 12:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message, String realm) {
        super(message);
        this.realm = realm;
    }

    private String realm = null;

    public String getRealm() {
        return this.realm;
    }

}
