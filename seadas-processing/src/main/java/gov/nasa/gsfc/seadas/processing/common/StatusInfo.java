package gov.nasa.gsfc.seadas.processing.common;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 4/12/13
 * Time: 2:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class StatusInfo {

    public static enum Id {
        SUCCEED,
        FAIL,
        WARN
    }

    private Id status;
    private String message;

    public StatusInfo() {

    }

    public StatusInfo(Id id) {
        this.status = id;
    }


    public Id getStatus() {
        return status;
    }


    public void setStatus(Id status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
