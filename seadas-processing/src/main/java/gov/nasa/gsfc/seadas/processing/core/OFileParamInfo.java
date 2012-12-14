package gov.nasa.gsfc.seadas.processing.core;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 12/11/12
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class OFileParamInfo extends ParamInfo {

    private boolean openInSeadas;

    public OFileParamInfo(String name, String value, Type type, String defaultValue) {
        super(name, value,  type, defaultValue);
    }


    public boolean isOpenInSeadas() {
        return openInSeadas;
    }

    public void setOpenInSeadas(boolean openInSeadas) {
        this.openInSeadas = openInSeadas;
    }
}
