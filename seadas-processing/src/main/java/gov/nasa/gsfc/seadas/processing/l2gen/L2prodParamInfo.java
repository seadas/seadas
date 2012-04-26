package gov.nasa.gsfc.seadas.processing.l2gen;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 4/26/12
 * Time: 2:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2prodParamInfo extends ParamInfo {

    private L2genData l2genData;

    public L2prodParamInfo(L2genData l2genData) {

        super(L2genData.L2PROD, "", Type.STRING);
        this.l2genData = l2genData;
    }

    public void setName(String name) {

    }

    public String getValue() {
        return l2genData.getParamValue(L2genData.L2PROD);
    }

    public void setValue(String value) {
        l2genData.setParamValue(L2genData.L2PROD, value);
    }

    public void setType(Type type) {

    }

    public String getDefaultValue() {
        return l2genData.getParamDefault(L2genData.L2PROD);
    }

    public boolean isDefault() {
        return l2genData.isParamDefault(L2genData.L2PROD);
    }

    public void setDefaultValue(String defaultValue) {
        setValue(defaultValue);
        l2genData.setProductDefaults();
    }
}
