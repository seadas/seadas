package gov.nasa.gsfc.seadas.processing.l2gen;


import ucar.units.Test;

import java.util.ArrayList;
import java.util.HashSet;

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

    // These fields are populated according to productInfo.xml

    public ArrayList<WavelengthInfo> waveLimiterInfos;
    private String dataType = null;
    private String prefix = null;
    private String suffix = null;
    private String units = null;
    private ParameterType parameterType = null;


    public AlgorithmInfo(String name, String description, ParameterType parameterType, ArrayList<WavelengthInfo> waveLimiterInfos) {
        super(name);
        setDescription(description);
        this.parameterType = parameterType;
        this.waveLimiterInfos = waveLimiterInfos;
    }

//    public AlgorithmInfo(String name, String description, String waveTypeStr) {
//        this(name, description, convertWavetype(waveTypeStr));
//    }
//
//    public AlgorithmInfo(String name, String description) {
//        this(name, description, ParameterType.NONE);
//    }

    public AlgorithmInfo(ArrayList<WavelengthInfo> waveLimiterInfos) {
        this("", "", ParameterType.NONE, waveLimiterInfos);
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

    private String getShortcutFullname(ShortcutType shortcutType) {

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


    private boolean isSelectedShortcut(ShortcutType shortcutType) {
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


    public void setL2prod(HashSet<String> inProducts) {

        if (getParameterType() == AlgorithmInfo.ParameterType.NONE) {
            if (inProducts.contains(getFullName())) {
                setState(AlgorithmInfo.State.SELECTED);
            } else {
                setState(AlgorithmInfo.State.NOT_SELECTED);
            }
        } else {
            for (BaseInfo wInfo : getChildren()) {
                WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;

                if (inProducts.contains(wavelengthInfo.getFullName())) {
                    wavelengthInfo.setState(WavelengthInfo.State.SELECTED);
                } else {
                    wavelengthInfo.setState(WavelengthInfo.State.NOT_SELECTED);
                }
            }

            if (inProducts.contains(getShortcutFullname(AlgorithmInfo.ShortcutType.VISIBLE))) {
                setStateShortcut(AlgorithmInfo.ShortcutType.VISIBLE, AlgorithmInfo.State.SELECTED);
            }

            if (inProducts.contains(getShortcutFullname(AlgorithmInfo.ShortcutType.IR))) {
                setStateShortcut(AlgorithmInfo.ShortcutType.IR, AlgorithmInfo.State.SELECTED);
            }

            if (inProducts.contains(getShortcutFullname(AlgorithmInfo.ShortcutType.ALL))) {
                setStateShortcut(AlgorithmInfo.ShortcutType.ALL, AlgorithmInfo.State.SELECTED);
            }
        }
    }


    public ArrayList<String> getL2prod() {

        ArrayList<String> l2prod = new ArrayList<String>();

        if (hasChildren()) {
            int count = 0;
            int selectedCount = 0;
            int visibleCount = 0;
            int visibleSelectedCount = 0;
            int infraredCount = 0;
            int infraredSelectedCount = 0;

            for (WavelengthInfo waveLimiterInfo : waveLimiterInfos) {
                for (BaseInfo wInfo : getChildren()) {
                    WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;
                    if (wavelengthInfo.getWavelength() == waveLimiterInfo.getWavelength()) {
                        if (wavelengthInfo.isSelected()) {
                            if (waveLimiterInfo.getWaveType() == WavelengthInfo.WaveType.INFRARED) {
                                infraredSelectedCount++;
                            } else if (waveLimiterInfo.getWaveType() == WavelengthInfo.WaveType.VISIBLE) {
                                visibleSelectedCount++;
                            }
                            selectedCount++;
                        }

                        continue;
                    }
                }

                if (waveLimiterInfo.getWaveType() == WavelengthInfo.WaveType.INFRARED) {
                    infraredCount++;
                } else if (waveLimiterInfo.getWaveType() == WavelengthInfo.WaveType.VISIBLE) {
                    visibleCount++;
                }

                count++;
            }


            if (selectedCount == count) {
                l2prod.add(getShortcutFullname(ShortcutType.ALL));
            } else {
                if (visibleSelectedCount == visibleCount) {
                    l2prod.add(getShortcutFullname(ShortcutType.VISIBLE));
                }

                if (infraredSelectedCount == infraredCount) {
                    l2prod.add(getShortcutFullname(ShortcutType.IR));
                }

                for (BaseInfo wInfo : getChildren()) {
                    WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;
                    if (wInfo.isSelected()) {
                        if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.VISIBLE)) {
                            if (visibleSelectedCount != visibleCount) {
                                l2prod.add(wavelengthInfo.getFullName());
                            }
                        } else if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.INFRARED)) {
                            if (infraredSelectedCount != infraredCount) {
                                l2prod.add(wavelengthInfo.getFullName());
                            }
                        } else {
                            l2prod.add(wavelengthInfo.getFullName());
                        }
                    }
                }
            }
        } else {
            if (isSelected()) {
                l2prod.add(getFullName());
            }
        }

        return l2prod;
    }


    public void reset() {

        setSelected(false);
        if (getParameterType() != AlgorithmInfo.ParameterType.NONE) {
            clearChildren();
            for (WavelengthInfo waveLimiterInfo : waveLimiterInfos) {
                boolean addWavelength = false;

                if (getParameterType() == AlgorithmInfo.ParameterType.ALL) {
                    addWavelength = true;
                } else if (waveLimiterInfo.getWavelength() >= WavelengthInfo.INFRARED_LOWER_LIMIT &&
                        getParameterType() == AlgorithmInfo.ParameterType.IR) {
                    addWavelength = true;
                } else if (waveLimiterInfo.getWavelength() <= WavelengthInfo.VISIBLE_UPPER_LIMIT &&
                        getParameterType() == AlgorithmInfo.ParameterType.VISIBLE) {
                    addWavelength = true;
                }

                if (addWavelength) {
                    WavelengthInfo newWavelengthInfo = new WavelengthInfo(waveLimiterInfo.getWavelength());
                    newWavelengthInfo.setParent(this);
                    newWavelengthInfo.setDescription(getDescription() + ", at " + newWavelengthInfo.getWavelengthString());
                    addChild(newWavelengthInfo);
                }
            }
        }
    }

    private void setStateShortcut(ShortcutType shortcutType, State state) {
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
