package gov.nasa.obpg.seadas.sandbox.l2gen;

import com.sun.servicetag.SystemEnvironment;
import org.python.antlr.ast.Str;

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

    public static enum ShortcutType {
        VISIBLE, IR, ALL
    }

    private static String SHORTCUT_NAMEPART_VISIBLE = "vvv";
    private static String SHORTCUT_NAMEPART_IR = "iii";
    private static String SHORTCUT_NAMEPART_ALL = "nnn";

    private static String PARAMTYPE_VISIBLE = "VISIBLE";
    private static String PARAMTYPE_IR = "IR";
    private static String PARAMTYPE_ALL = "ALL";
    private static String PARAMTYPE_NONE = "NONE";

    // These fields are populated according to productList.xml


    private String dataType = null;
    private String prefix = null;
    private String suffix = null;
    private String units = null;
    private ParameterType parameterType = null;

    private boolean defaultSelected = false;


    public AlgorithmInfo(String name, String description, ParameterType parameterType) {
        super(name);
        setDescription(description);
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


    private static String convertShortcutType(ShortcutType shortcutType) {
        if (shortcutType == ShortcutType.ALL) {
            return SHORTCUT_NAMEPART_ALL;
        } else if (shortcutType == ShortcutType.IR) {
            return SHORTCUT_NAMEPART_IR;
        } else if (shortcutType == ShortcutType.VISIBLE) {
            return SHORTCUT_NAMEPART_VISIBLE;
        } else {
            return null;
        }
    }

    public String getShortcutFullname(ShortcutType shortcutType) {

        StringBuilder result = new StringBuilder();

        if (prefix != null && !prefix.isEmpty()) {
            result.append(prefix);
        }

        result.append(convertShortcutType(shortcutType));

        if (suffix != null && !suffix.isEmpty()) {
            result.append(suffix);
        }

        return result.toString().replaceAll("[_]+", "_").replaceAll("[_]$", "");
    }


    public boolean isSelectedShortcut(ShortcutType shortcutType) {
        boolean found = false;

        if (shortcutType == ShortcutType.ALL) {
            for (BaseInfo wInfo : getChildren()) {
                WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;
                if (wavelengthInfo.isSelected()) {
                    found = true;
                } else {
                    return false;
                }
            }
            return found;

        } else if (shortcutType == ShortcutType.VISIBLE) {
            for (BaseInfo wInfo : getChildren()) {
                WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;
                if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.VISIBLE)) {
                    if (wavelengthInfo.isSelected()) {
                        found = true;
                    } else {
                        return false;
                    }
                }
            }
            return found;

        } else if (shortcutType == ShortcutType.IR) {
            for (BaseInfo wInfo : getChildren()) {
                WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;
                if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.INFRARED)) {
                    if (wavelengthInfo.isSelected()) {
                        found = true;
                    } else {
                        return false;
                    }
                }
            }
            return found;
        }
        return false;
    }

    public void setStateShortcut(ShortcutType shortcutType, State state) {
        System.out.println("setStateShortcut" + shortcutType + " ---- " + state);
        for (BaseInfo wInfo : getChildren()) {
            WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;

            if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.VISIBLE)) {
                if (shortcutType == ShortcutType.ALL || shortcutType == ShortcutType.VISIBLE) {
                    wavelengthInfo.setState(state);
                }
            }

            if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.INFRARED)) {
                if (shortcutType == ShortcutType.ALL || shortcutType == ShortcutType.IR) {
                    wavelengthInfo.setState(state);
                }
            }
        }
    }


}
