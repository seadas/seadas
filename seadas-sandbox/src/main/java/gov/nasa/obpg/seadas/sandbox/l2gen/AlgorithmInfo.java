package gov.nasa.obpg.seadas.sandbox.l2gen;

import com.sun.servicetag.SystemEnvironment;

import java.util.ArrayList;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class AlgorithmInfo extends BaseInfo {
    public static enum ParameterType {
        VISIBLE, IR, ALL, NONE
    }

    private static String PARAMTYPE_VISIBLE = "VISIBLE";
    private static String PARAMTYPE_IR = "IR";
    private static String PARAMTYPE_ALL = "ALL";
    private static String PARAMTYPE_NONE = "NONE";

    // These fields are populated according to productList.xml

    private String description = null;
    private String dataType = null;
    private String prefix = null;
    private String suffix = null;
    private String units = null;
    private ParameterType parameterType = null;

    private boolean defaultSelected = false;


    public AlgorithmInfo(String name, String description, ParameterType parameterType) {
        super(name);
        this.description = description;
        this.parameterType = parameterType;
    }

    public AlgorithmInfo(String name, String description, String waveTypeStr) {
        this(name, description, convertWavetype(waveTypeStr));
    }

    public AlgorithmInfo(String name, String description) {
        this(name, description, ParameterType.NONE);
    }

    public AlgorithmInfo() {
        this("", "", ParameterType.NONE);
    }

    public String getProductName() {
        if (getParent() == null) {
            return null;
        }
        return getParent().getName();
    }



    public ProductInfo getProductInfo() {
        return (ProductInfo) getParent();
    }

    public void setProductInfo(ProductInfo productInfo) {
        setParent(productInfo);
    }



    public boolean isDefaultSelected() {
        return defaultSelected;
    }

    public void setDefaultSelected(boolean defaultSelected) {
        this.defaultSelected = defaultSelected;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterTypeStr) {
        this.parameterType = convertWavetype(parameterTypeStr);
    }

    @Override
    public String getFullName() {

        StringBuilder result = new StringBuilder();

        if (prefix != null && !prefix.isEmpty()) {
            result.append(prefix);
        }

        if (suffix != null && !suffix.isEmpty()) {
            result.append(suffix);
        }


        return result.toString().replaceAll("[_]+", "_").replaceAll("[_]$", "");
    }


    private static ParameterType convertWavetype(String str) {
        if (str.compareToIgnoreCase(PARAMTYPE_VISIBLE) == 0) {
            return ParameterType.VISIBLE;
        } else if (str.compareToIgnoreCase(PARAMTYPE_IR) == 0) {
            return ParameterType.IR;
        } else if (str.compareToIgnoreCase(PARAMTYPE_ALL) == 0) {
            return ParameterType.ALL;
        } else if (str.compareToIgnoreCase(PARAMTYPE_NONE) == 0) {
            return ParameterType.NONE;
        } else {
            return null;
        }
    }


}
