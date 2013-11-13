package gov.nasa.gsfc.seadas.processing.l2gen.productData;


import java.util.ArrayList;
import java.util.HashSet;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class L2genAlgorithmInfo extends L2genBaseInfo {

    public static enum ParameterType {
        VISIBLE,
        IR,
        ALL,
        INT,
        NONE
    }


    public final static String
            PARAMTYPE_VISIBLE = "VISIBLE",
            PARAMTYPE_IR = "IR",
            PARAMTYPE_ALL = "ALL",
            PARAMTYPE_NONE = "NONE",
            PARAMTYPE_INT = "INTEGER";

    // These fields are populated according to productInfo.xml

    public ArrayList<L2genWavelengthInfo> waveLimiterInfos;

    private String
            dataType = null,
            prefix = null,
            suffix = null,
            units = null;

    private ParameterType parameterType = null;


    public L2genAlgorithmInfo(String name, String description, ParameterType parameterType, ArrayList<L2genWavelengthInfo> waveLimiterInfos) {
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

    public L2genAlgorithmInfo(ArrayList<L2genWavelengthInfo> waveLimiterInfos) {
        this("", "", ParameterType.NONE, waveLimiterInfos);
    }

    public String getProductName() {
        if (getParent() == null) {
            return null;
        }
        return getParent().getName();
    }


    public L2genProductInfo getProductInfo() {
        return (L2genProductInfo) getParent();
    }

    public void setProductInfo(L2genProductInfo productInfo) {
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

    public static ParameterType convertWavetype(String str) {
        if (str == null) {
            return ParameterType.NONE;
        }
        if (str.compareToIgnoreCase(PARAMTYPE_VISIBLE) == 0) {
            return ParameterType.VISIBLE;
        } else if (str.compareToIgnoreCase(PARAMTYPE_IR) == 0) {
            return ParameterType.IR;
        } else if (str.compareToIgnoreCase(PARAMTYPE_ALL) == 0) {
            return ParameterType.ALL;
        } else if (str.compareToIgnoreCase(PARAMTYPE_INT) == 0) {
            return ParameterType.INT;
        } else if (str.compareToIgnoreCase(PARAMTYPE_NONE) == 0) {
            return ParameterType.NONE;
        } else {
            return ParameterType.NONE;
        }
    }


    private String getShortcutFullname(L2genProductTools.ShortcutType shortcutType) {

        StringBuilder result = new StringBuilder();

        if (prefix != null && !prefix.isEmpty()) {
            result.append(prefix);
        }

        result.append(L2genProductTools.convertShortcutType(shortcutType));

        if (suffix != null && !suffix.isEmpty()) {
            result.append(suffix);
        }

        return result.toString().replaceAll("[_]+", "_").replaceAll("[_]$", "");
    }


    private boolean isSelectedShortcut(L2genProductTools.ShortcutType shortcutType) {
        boolean found = false;

        if (shortcutType == L2genProductTools.ShortcutType.ALL) {
            for (L2genBaseInfo wInfo : getChildren()) {
                L2genWavelengthInfo wavelengthInfo = (L2genWavelengthInfo) wInfo;
                if (wavelengthInfo.isSelected()) {
                    found = true;
                } else {
                    return false;
                }
            }
            return found;

        } else if (shortcutType == L2genProductTools.ShortcutType.VISIBLE) {
            for (L2genBaseInfo wInfo : getChildren()) {
                L2genWavelengthInfo wavelengthInfo = (L2genWavelengthInfo) wInfo;
                if (wavelengthInfo.isWaveType(L2genWavelengthInfo.WaveType.VISIBLE)) {
                    if (wavelengthInfo.isSelected()) {
                        found = true;
                    } else {
                        return false;
                    }
                }
            }
            return found;

        } else if (shortcutType == L2genProductTools.ShortcutType.IR) {
            for (L2genBaseInfo wInfo : getChildren()) {
                L2genWavelengthInfo wavelengthInfo = (L2genWavelengthInfo) wInfo;
                if (wavelengthInfo.isWaveType(L2genWavelengthInfo.WaveType.INFRARED)) {
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

        if (getParameterType() == L2genAlgorithmInfo.ParameterType.NONE) {
            String thisProduct = getFullName();
            if (inProducts.contains(thisProduct)) {
                setState(State.SELECTED);
                inProducts.remove(thisProduct);
            } else {
                setState(State.NOT_SELECTED);
            }
        } else {
            for (L2genBaseInfo wInfo : getChildren()) {
                L2genWavelengthInfo wavelengthInfo = (L2genWavelengthInfo) wInfo;

                if (wavelengthInfo.getWavelength() == L2genProductTools.WAVELENGTH_FOR_IFILE_INDEPENDENT_MODE) {
                    String inProductFound = null;
                    for (String inProduct : inProducts) {

                        boolean matchIsStillPossible = true;

                        if (getPrefix() != null && getPrefix().length() > 0) {
                            if (inProduct.startsWith(getPrefix())) {
                                inProduct = inProduct.substring((getPrefix().length())); //trim off prefix
                            } else {
                                matchIsStillPossible = false;
                            }
                        }

                        if (getSuffix() != null && getPrefix().length() > 0) {
                            if (inProduct.endsWith(getSuffix())) {
                                inProduct = inProduct.substring(0, (inProduct.length() - getSuffix().length())); // trim off suffix
                            } else {
                                matchIsStillPossible = false;
                            }
                        }

                        if (matchIsStillPossible) {
                            if (L2genProductTools.isInteger(inProduct)
                                    || inProduct.equals(L2genProductTools.SHORTCUT_NAMEPART_IR)
                                    || inProduct.equals(L2genProductTools.SHORTCUT_NAMEPART_VISIBLE)
                                    || inProduct.equals(L2genProductTools.SHORTCUT_NAMEPART_ALL)) {
                                inProductFound = inProduct;
                            }
                        }
                    }

                    if (inProductFound != null) {
                        wavelengthInfo.setState(State.SELECTED);
                        inProducts.remove(inProductFound);
                    } else {
                        wavelengthInfo.setState(State.NOT_SELECTED);
                    }


                } else {
                    String wavelengthProduct = wavelengthInfo.getFullName();
                    if (inProducts.contains(wavelengthProduct)) {
                        wavelengthInfo.setState(State.SELECTED);
                        inProducts.remove(wavelengthProduct);
                    } else {
                        wavelengthInfo.setState(State.NOT_SELECTED);
                    }
                }

            }

            String visibleProduct = getShortcutFullname(L2genProductTools.ShortcutType.VISIBLE);
            if (inProducts.contains(visibleProduct)) {
                setStateShortcut(L2genProductTools.ShortcutType.VISIBLE, State.SELECTED);
                inProducts.remove(visibleProduct);
            }

            String irProduct = getShortcutFullname(L2genProductTools.ShortcutType.IR);
            if (inProducts.contains(irProduct)) {
                setStateShortcut(L2genProductTools.ShortcutType.IR, State.SELECTED);
                inProducts.remove(irProduct);
            }

            String allProduct = getShortcutFullname(L2genProductTools.ShortcutType.ALL);
            if (inProducts.contains(allProduct)) {
                setStateShortcut(L2genProductTools.ShortcutType.ALL, State.SELECTED);
                inProducts.remove(allProduct);
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

            for (L2genWavelengthInfo waveLimiterInfo : waveLimiterInfos) {
                for (L2genBaseInfo wInfo : getChildren()) {
                    L2genWavelengthInfo wavelengthInfo = (L2genWavelengthInfo) wInfo;
                    if (wavelengthInfo.getWavelength() == waveLimiterInfo.getWavelength()) {
                        if (wavelengthInfo.isSelected()) {
                            if (waveLimiterInfo.getWaveType() == L2genWavelengthInfo.WaveType.INFRARED) {
                                infraredSelectedCount++;
                            } else if (waveLimiterInfo.getWaveType() == L2genWavelengthInfo.WaveType.VISIBLE) {
                                visibleSelectedCount++;
                            }
                            selectedCount++;
                        }

                        continue;
                    }
                }

                if (waveLimiterInfo.getWaveType() == L2genWavelengthInfo.WaveType.INFRARED) {
                    infraredCount++;
                } else if (waveLimiterInfo.getWaveType() == L2genWavelengthInfo.WaveType.VISIBLE) {
                    visibleCount++;
                }

                count++;
            }


            if (selectedCount == count && selectedCount > 0) {
                l2prod.add(getShortcutFullname(L2genProductTools.ShortcutType.ALL));
            } else {
                if (visibleSelectedCount == visibleCount && visibleSelectedCount > 0) {
                    l2prod.add(getShortcutFullname(L2genProductTools.ShortcutType.VISIBLE));
                }

                if (infraredSelectedCount == infraredCount && infraredSelectedCount > 0) {
                    l2prod.add(getShortcutFullname(L2genProductTools.ShortcutType.IR));
                }

                for (L2genBaseInfo wInfo : getChildren()) {
                    L2genWavelengthInfo wavelengthInfo = (L2genWavelengthInfo) wInfo;
                    if (wInfo.isSelected()) {
                        if (wavelengthInfo.isWaveType(L2genWavelengthInfo.WaveType.VISIBLE)) {
                            if (visibleSelectedCount != visibleCount) {
                                l2prod.add(wavelengthInfo.getFullName());
                            }
                        } else if (wavelengthInfo.isWaveType(L2genWavelengthInfo.WaveType.INFRARED)) {
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
        if (getParameterType() != L2genAlgorithmInfo.ParameterType.NONE) {
            clearChildren();
            for (L2genWavelengthInfo waveLimiterInfo : waveLimiterInfos) {
                boolean addWavelength = false;

                if (getParameterType() == L2genAlgorithmInfo.ParameterType.ALL) {
                    addWavelength = true;
                } else if (waveLimiterInfo.getWavelength() >= L2genWavelengthInfo.INFRARED_LOWER_LIMIT &&
                        getParameterType() == L2genAlgorithmInfo.ParameterType.IR) {
                    addWavelength = true;
                } else if (waveLimiterInfo.getWavelength() <= L2genWavelengthInfo.VISIBLE_UPPER_LIMIT &&
                        getParameterType() == L2genAlgorithmInfo.ParameterType.VISIBLE) {
                    addWavelength = true;
                }

                if (addWavelength) {
                    L2genWavelengthInfo newWavelengthInfo = new L2genWavelengthInfo(waveLimiterInfo.getWavelength());
                    newWavelengthInfo.setParent(this);
                    newWavelengthInfo.setDescription(getDescription() + ", at " + newWavelengthInfo.getWavelengthString());
                    addChild(newWavelengthInfo);
                }
            }
        }
    }

    private void setStateShortcut(L2genProductTools.ShortcutType shortcutType, State state) {
        for (L2genBaseInfo wInfo : getChildren()) {
            L2genWavelengthInfo wavelengthInfo = (L2genWavelengthInfo) wInfo;

            if (wavelengthInfo.isWaveType(L2genWavelengthInfo.WaveType.VISIBLE)) {
                if (shortcutType == L2genProductTools.ShortcutType.ALL || shortcutType == L2genProductTools.ShortcutType.VISIBLE) {
                    wavelengthInfo.setState(state);
                }
            }

            if (wavelengthInfo.isWaveType(L2genWavelengthInfo.WaveType.INFRARED)) {
                if (shortcutType == L2genProductTools.ShortcutType.ALL || shortcutType == L2genProductTools.ShortcutType.IR) {
                    wavelengthInfo.setState(state);
                }
            }
        }
    }
}
