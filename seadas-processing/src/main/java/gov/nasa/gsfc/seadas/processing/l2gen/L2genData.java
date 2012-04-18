package gov.nasa.gsfc.seadas.processing.l2gen;


import org.esa.beam.util.StringUtils;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class L2genData {

    private boolean ignoreIfileInParString = true;
    private String OCDATAROOT = System.getenv("OCDATAROOT");

    private String PRODUCT_INFO_XML = "productInfo.xml";
    private String PARAM_INFO_XML = "paramInfo.xml";
    private String DEFAULT_IFILE = "";

    private String PARAM_CATEGORY_INFO_XML = "paramCategoryInfo.xml";
    private String PRODUCT_CATEGORY_INFO_XML = "productCategoryInfo.xml";

    private String initialIfile = DEFAULT_IFILE;

    public boolean ifileIsValid = true;

    public final String PAR = "par";
    public final String GEOFILE = "geofile";
    public final String SPIXL = "spixl";
    public final String EPIXL = "epixl";
    public final String DPIXL = "dpixl";
    public final String SLINE = "sline";
    public final String ELINE = "eline";
    public final String DLINE = "dline";
    public final String NORTH = "north";
    public final String SOUTH = "south";
    public final String WEST = "west";
    public final String EAST = "east";
    public final String IFILE = "ifile";
    public final String OFILE = "ofile";
    public final String L2PROD = "l2prod";

    public final String INVALID_IFILE_EVENT = "INVALID_IFILE_EVENT";
    public final String PARFILE_CHANGE_EVENT = "PARFILE_CHANGE_EVENT";
    //  public final String IFILE = IFILE;
    public final String WAVE_LIMITER_CHANGE_EVENT = "WAVE_LIMITER_CHANGE_EVENT";
    // public final String L2PROD = L2PROD;
    public final String DEFAULTS_CHANGED_EVENT = "DEFAULTS_CHANGED_EVENT";

    private final String TARGET_PRODUCT_SUFFIX = "L2";

    private L2genReader l2genReader = new L2genReader(this);

    private ArrayList<ProductInfo> productInfos = new ArrayList<ProductInfo>();
    private ArrayList<ParamInfo> paramInfos = new ArrayList<ParamInfo>();
    private ArrayList<ParamCategoryInfo> paramCategoryInfos = new ArrayList<ParamCategoryInfo>();
    private ArrayList<ProductCategoryInfo> productCategoryInfos = new ArrayList<ProductCategoryInfo>();


    private ArrayList<WavelengthInfo> waveLimiter = new ArrayList<WavelengthInfo>();

    private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    private L2genPrint l2genPrint = new L2genPrint();

    public String getInitialIfile() {
        return initialIfile;
    }

    public void setInitialIfile(String initialIfile) {
        if (new File(initialIfile).exists()) {
            this.initialIfile = initialIfile;
        }
    }

    public boolean isIgnoreIfileInParString() {
        return ignoreIfileInParString;
    }

    public void setIgnoreIfileInParString(boolean ignoreIfileInParString) {
        this.ignoreIfileInParString = ignoreIfileInParString;
    }

    public enum RegionType {Coordinates, PixelLines}

    public EventInfo[] eventInfos = {
            new EventInfo(L2PROD, this),
    };

    public L2genData() {

    }


    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        boolean found = false;

        for (EventInfo eventInfo : eventInfos) {
            if (propertyName.equals(eventInfo.getName())) {
                eventInfo.addPropertyChangeListener(listener);
                found = true;
            }
        }

        if (!found) {
            propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
        }
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        boolean found = false;

        for (EventInfo eventInfo : eventInfos) {
            if (propertyName.equals(eventInfo.getName())) {
                eventInfo.removePropertyChangeListener(listener);
                found = true;
            }
        }

        if (!found) {
            propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        }
    }


    public void disableEvent(String eventName) {
        for (EventInfo eventInfo : eventInfos) {
            if (eventName.equals(eventInfo.toString())) {
                eventInfo.setEnabled(false);
                //  debug("Disabled event " + eventName + " current enabled count = " + eventInfo.getEnabledCount());
            }
        }
    }

    public void enableEvent(String eventName) {
        for (EventInfo eventInfo : eventInfos) {
            if (eventName.equals(eventInfo.toString())) {
                eventInfo.setEnabled(true);
                //   debug("Enabled event " + eventName + " current enabled count = " + eventInfo.getEnabledCount());
            }
        }
    }

    public void fireEvent(String eventName) {
        fireEvent(eventName, null, null);
    }

    public void fireEvent(String eventName, Object oldValue, Object newValue) {
        for (EventInfo eventInfo : eventInfos) {
            if (eventName.equals(eventInfo.toString())) {
                eventInfo.fireEvent(oldValue, newValue);
                return;
            }
        }
    }


    public void fireAllParamEvents() {
        for (ParamInfo paramInfo : paramInfos) {
            if (paramInfo.getName() != null) {
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, paramInfo.getName(), null, null));
            }
        }
    }


    public void setSelectedInfo(BaseInfo info, BaseInfo.State state) {

        if (state != info.getState()) {
            info.setState(state);
            fireEvent(L2PROD);
        }
    }


    /*

     */
    private String getProdParamValue() {
        ArrayList<String> prodArrayList = new ArrayList<String>();

        for (ProductInfo productInfo : productInfos) {
            for (BaseInfo aInfo : productInfo.getChildren()) {
                if (aInfo.hasChildren()) {
                    AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;

                    if (algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.ALL)) {
                        prodArrayList.add(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.ALL));
                    } else {
                        if (algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.IR)) {
                            prodArrayList.add(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.IR));
                        }
                        if (algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.VISIBLE)) {
                            prodArrayList.add(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.VISIBLE));
                        }

                        for (BaseInfo wInfo : aInfo.getChildren()) {
                            WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;

                            if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.VISIBLE) && !algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.VISIBLE)) {
                                if (wInfo.isSelected()) {
                                    prodArrayList.add(wavelengthInfo.getFullName());
                                }
                            }

                            if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.INFRARED) && !algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.IR)) {
                                if (wInfo.isSelected()) {
                                    prodArrayList.add(wavelengthInfo.getFullName());
                                }
                            }
                        }
                    }
                } else {
                    if (aInfo.isSelected()) {
                        prodArrayList.add(aInfo.getFullName());
                    }
                }
            }
        }

        return StringUtils.join(prodArrayList, " ");
    }


    /**
     * Set wavelength in waveLimiter based on GUI change
     *
     * @param selectedWavelength
     * @param selected
     */
    public void setSelectedWaveLimiter(String selectedWavelength, boolean selected) {

        for (WavelengthInfo wavelengthInfo : waveLimiter) {
            if (selectedWavelength.equals(wavelengthInfo.getWavelengthString())) {
                if (selected != wavelengthInfo.isSelected()) {
                    wavelengthInfo.setSelected(selected);
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, WAVE_LIMITER_CHANGE_EVENT, null, null));
                }
            }
        }
    }


    /**
     * Determine is mission has particular waveType based on what is in the waveLimiter Array
     * <p/>
     * Used by the waveLimiter GUI to enable/disable the appropriate 'Select All' toggle buttons
     *
     * @param waveType
     * @return true if waveType in waveLimiter, otherwise false
     */
    public boolean hasWaveType(WavelengthInfo.WaveType waveType) {

        for (WavelengthInfo wavelengthInfo : waveLimiter) {
            if (wavelengthInfo.isWaveType(waveType)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Determines if all wavelengths for a given wavelength type within the wavelength limiter array are selected
     * <p/>
     * This is used to determine whether the toggle button in the wavelength limiter GUI needs
     * to be in: 'Select All Infrared' mode, 'Deselect All Infrared' mode,
     * 'Select All Visible' mode, or 'Deselect All Visible' mode
     *
     * @return true if all of given wavelength type selected, otherwise false
     */
    public boolean isSelectedAllWaveLimiter(WavelengthInfo.WaveType waveType) {

        int selectedCount = 0;

        for (WavelengthInfo wavelengthInfo : waveLimiter) {
            if (wavelengthInfo.isWaveType(waveType)) {
                if (wavelengthInfo.isSelected()) {
                    selectedCount++;
                } else {
                    return false;
                }
            }
        }

        if (selectedCount > 0) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Sets all wavelengths of a given wavelength type within the wavelength limiter array to selected
     * <p/>
     * This is called by the wavelength limiter GUI toggle buttons and is also used for initializing defaults.
     *
     * @param selected
     */
    public void setSelectedAllWaveLimiter(WavelengthInfo.WaveType waveType, boolean selected) {

        for (WavelengthInfo wavelengthInfo : waveLimiter) {
            if (wavelengthInfo.isWaveType(waveType)) {
                wavelengthInfo.setSelected(selected);
            }
        }

        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, WAVE_LIMITER_CHANGE_EVENT, null, null));

    }

    public void addParamInfo(ParamInfo paramInfo) {
        paramInfos.add(paramInfo);
    }

    public void clearParamInfo() {
        paramInfos.clear();
    }

    public ArrayList<ParamInfo> getParamInfos() {
        return paramInfos;
    }

    public void addProductInfo(ProductInfo productInfo) {
        productInfos.add(productInfo);
    }


    public void clearProductInfos() {
        productInfos.clear();
    }

    public void clearParamInfos() {
        paramInfos.clear();
    }


    public void sortParamCategoryInfos() {
        Collections.sort(paramCategoryInfos);
    }


    public void sortProductCategoryInfos() {
        Collections.sort(productCategoryInfos);
    }


    public void sortParamInfos() {
        Collections.sort(paramInfos);
    }

    public void sortProductInfos(Comparator<ProductInfo> comparator) {
        Collections.sort(productInfos, comparator);
    }


    public ArrayList<ProductInfo> getProductInfos() {
        return productInfos;
    }


    public ArrayList<WavelengthInfo> getWaveLimiter() {
        return waveLimiter;
    }


    /**
     * Handle cases where a change in one param should effect a change in param
     * <p/>
     * In this case specifically coordParams and pixelParams are mutually exclusive
     * so if a param in one group is being set to a non-default value, then set all
     * params in the other group to the defaults
     *
     * @param param
     */
    private void setConflictingParams(String param) {

        // Cleanup input and handle input exceptions
        if (param == null || param.length() == 0) {
            return;
        }
        param = param.trim();


        // Only proceed if param is not equal to default
        for (ParamInfo paramInfo : paramInfos) {
            if (param.equals(paramInfo.getName())) {
                if (paramInfo.getValue() == paramInfo.getDefaultValue()) {
                    return;
                }
                break;
            }
        }


        // Set all params in the other group to the defaults
        coordPixelCheck:
        {
            final String[] coordParams = {NORTH, SOUTH, WEST, EAST};
            final String[] pixelParams = {SPIXL, EPIXL, SLINE, ELINE};

            // Test if param is coordParam
            for (String coordParam : coordParams) {
                if (param.equals(coordParam)) {
                    // Set all pixelParams in paramInfos to defaults
                    for (ParamInfo paramInfo : paramInfos) {
                        for (String pixelParam : pixelParams) {
                            if (pixelParam.equals(paramInfo.getName())) {
                                setParamToDefaults(paramInfo);
                            }
                        }
                    }
                    break coordPixelCheck;
                }
            }

            // Test if param is pixelParam
            for (String pixelParam : pixelParams) {
                if (param.equals(pixelParam)) {
                    // Set all coordParams in paramInfos to defaults
                    for (ParamInfo paramInfo : paramInfos) {
                        for (String coordParam : coordParams) {
                            if (coordParam.equals(paramInfo.getName())) {
                                setParamToDefaults(paramInfo);
                            }
                        }
                    }
                    break coordPixelCheck;
                }
            }
        }
    }


    public String getParfile() {

        StringBuilder par = new StringBuilder("");

        for (ParamCategoryInfo paramCategoryInfo : paramCategoryInfos) {
            StringBuilder currCategoryEntries = new StringBuilder("");

            for (ParamInfo paramInfo : paramCategoryInfo.getParamInfos()) {
                if (paramInfo.getName().equals(L2PROD)) {
                    currCategoryEntries.append(paramInfo.getName() + "=" + getProdParamValue() + "\n");
                } else if (paramInfo.getName().equals(IFILE)) {
                    currCategoryEntries.append(paramInfo.getName() + "=" + paramInfo.getValue() + "\n");
                } else if (paramInfo.getName().equals(PAR)) {
                    // right ignore and do not print todo
                } else if (!paramInfo.getValue().equals(paramInfo.getDefaultValue())) {
                    if (!paramInfo.getName().startsWith("-")) {
                        currCategoryEntries.append(paramInfo.getName() + "=" + paramInfo.getValue() + "\n");
                    }
                }
            }

            if (currCategoryEntries.toString().length() > 0) {
                par.append("# " + paramCategoryInfo.getName() + "\n");
                par.append(currCategoryEntries.toString());
                par.append("\n");
            }
        }

        return par.toString();
    }


    private ArrayList<ParamInfo> parseParfile(String parfileContents) {

        ArrayList<ParamInfo> paramInfos = new ArrayList<ParamInfo>();

        if (parfileContents != null) {

            String parfileLines[] = parfileContents.split("\n");

            for (String parfileLine : parfileLines) {

                // skip the comment lines in file
                if (!parfileLine.trim().startsWith("#")) {

                    String splitLine[] = parfileLine.split("=");
                    if (splitLine.length == 1 || splitLine.length == 2) {
                        String name = splitLine[0].toString().trim();
                        String value = null;

                        if (splitLine.length == 2) {
                            value = splitLine[1].toString().trim();
                        } else if (splitLine.length == 1) {
                            value = ParamInfo.NULL_STRING;
                        }

                        ParamInfo paramInfo = new ParamInfo(name, value);
                        paramInfos.add(paramInfo);
                    }
                }
            }
        }

        return paramInfos;
    }


    private HashMap<String, String> parseParfileOld(String parfileString) {

        HashMap<String, String> thisParfileHashMap = null;

        if (parfileString != null) {

            thisParfileHashMap = new HashMap<String, String>();

            String parfileLines[] = parfileString.split("\n");

            for (String parfileLine : parfileLines) {

                // skip the comment lines in file
                if (!parfileLine.trim().startsWith("#")) {

                    String splitLine[] = parfileLine.split("=");
                    if (splitLine.length == 2) {
                        final String key = splitLine[0].toString().trim();
                        final String value = splitLine[1].toString().trim();
                        thisParfileHashMap.put(key, value);
                    } else if (splitLine.length == 1) {
                        final String key = splitLine[0].toString().trim();
                        if (L2PROD.equals(key)) {
                            thisParfileHashMap.put(key, "");
                        }
                    }
                }
            }
        }

        return thisParfileHashMap;
    }

// DANNY IS REVIEWING CODE AND LEFT OFF HERE


    public void setParamsFromParfile(String parfileContent, boolean checkIgnoreIfileInParString) {

        ArrayList<ParamInfo> parfileParamInfos = parseParfile(parfileContent);

        /*
        Handle IFILE first
         */
        boolean ignoreIfileOfile;
        if (!checkIgnoreIfileInParString || (checkIgnoreIfileInParString && !ignoreIfileInParString)) {
            ignoreIfileOfile = false;
        } else {
            ignoreIfileOfile = true;
        }

        if (getParamValue(L2PROD).equals(ParamInfo.NULL_STRING)) {
            ignoreIfileOfile = false;
        }


        if (!ignoreIfileOfile) {
            for (ParamInfo newParamInfo : parfileParamInfos) {
                if (newParamInfo.getName().toLowerCase().equals(IFILE)) {
                    setParamValue(IFILE, newParamInfo.getValue());
                    break;
                }
            }
        }

        /*
        Set all params contained in parfileContent
        Ignore IFILE (handled earlier) and PAR (which is todo)
         */
        for (ParamInfo newParamInfo : parfileParamInfos) {
            boolean setParamValue = true;

            if (newParamInfo.getName().toLowerCase().equals(OFILE) && ignoreIfileOfile) {
                setParamValue = false;
            }

            if (newParamInfo.getName().toLowerCase().equals(IFILE)) {
                setParamValue = false;
            }

            if (newParamInfo.getName().toLowerCase().equals(PAR)) {
                setParamValue = false;
            }

            if (setParamValue) {
                setParamValue(newParamInfo.getName().toLowerCase(), newParamInfo.getValue());
            }
        }

        /*
        Delete all params NOT contained in parfileContent to defaults (basically set to default)
        Except: L2PROD and IFILE and OFILE remain at current value
         */
        for (ParamInfo paramInfo : paramInfos) {
            if (!paramInfo.getName().equals(L2PROD) && !paramInfo.getName().equals(IFILE) && !paramInfo.getName().equals(OFILE)) {
                boolean paramHandled = false;
                for (ParamInfo parfileParamInfo : parfileParamInfos) {
                    if (paramInfo.getName().toLowerCase().equals(parfileParamInfo.getName().toLowerCase())) {
                        paramHandled = true;
                    }
                }

                if (!paramHandled && (paramInfo.getValue() != paramInfo.getDefaultValue())) {
                    setParamValue(paramInfo.getName(), paramInfo.getDefaultValue());
                }
            }
        }
    }


    public void copyFromProductDefaults() {
        // This method loops through the entire productInfoArray setting all the states to the default state

        boolean productChanged = false;

        for (ProductInfo productInfo : productInfos) {
            for (BaseInfo aInfo : productInfo.getChildren()) {
                if (aInfo.hasChildren()) {
                    for (BaseInfo wInfo : aInfo.getChildren()) {
                        if (wInfo.isSelected() != ((WavelengthInfo) wInfo).isDefaultSelected()) {
                            wInfo.setSelected(((WavelengthInfo) wInfo).isDefaultSelected());
                            productChanged = true;
                        }
                    }
                } else {
                    if (aInfo.isSelected() != ((AlgorithmInfo) aInfo).isDefaultSelected()) {
                        aInfo.setSelected(((AlgorithmInfo) aInfo).isDefaultSelected());
                        productChanged = true;
                    }
                }
            }
        }


        if (productChanged == true) {
            fireEvent(L2PROD);
        }
    }

    public void copyToProductDefaults() {

        for (ProductInfo productInfo : productInfos) {
            for (BaseInfo aInfo : productInfo.getChildren()) {
                if (aInfo.hasChildren()) {
                    for (BaseInfo wInfo : aInfo.getChildren()) {
                        ((WavelengthInfo) wInfo).setDefaultSelected(wInfo.isSelected());
                    }
                } else {
                    ((AlgorithmInfo) aInfo).setDefaultSelected(aInfo.isSelected());
                }
            }
        }
    }

    public String getParamValue(ParamInfo paramInfo) {
        return paramInfo.getValue();
    }

    public String getParamValue(String param) {

        if (param == null) {
            return "";
        }

        if (param.toLowerCase().equals(L2PROD)) {
            return getProdParamValue();
        }

        for (ParamInfo paramInfo : paramInfos) {
            if (paramInfo.getName().toLowerCase().equals(param.toLowerCase())) {
                return paramInfo.getValue();
            }
        }

        return "";
    }


    public boolean getBooleanParamValue(String key) {
        String value = getParamValue(key);

        if (value.equals(ParamInfo.BOOLEAN_TRUE)) {
            return true;
        } else {
            return false;
        }
    }


    public void deleteParamValue(String param) {

        setParamValue(param, ParamInfo.NULL_STRING);
    }

    public void setParamValue(String param, String value) {

        // Cleanup inputs and handle input exceptions
        if (param == null || param.length() == 0) {
            return;
        }
        if (value == null) {
            value = ParamInfo.NULL_STRING;
        }
        param = param.trim();
        value = value.trim();


        for (ParamInfo paramInfo : paramInfos) {
            if (paramInfo.getName().toLowerCase().equals(param.toLowerCase())) {
                setParamValue(paramInfo, value);
                return;
            }
        }
    }


    public void setParamValue(String param, boolean selected) {
        if (selected) {
            setParamValue(param, ParamInfo.BOOLEAN_TRUE);
        } else {
            setParamValue(param, ParamInfo.BOOLEAN_FALSE);
        }
    }

    public void setParamValue(ParamInfo paramInfo, ParamValidValueInfo paramValidValueInfo) {
        if (paramInfo == null || paramValidValueInfo == null) {
            return;
        }

        setParamValue(paramInfo, paramValidValueInfo.getValue());
    }

    public void setParamValue(ParamInfo paramInfo, String value) {
        if (paramInfo == null) {
            return;
        }
        if (value == null) {
            value = ParamInfo.NULL_STRING;
        }

        if (!value.equals(paramInfo.getValue())) {
            if (paramInfo.getName().equals(IFILE)) {
                setIfileParamValue(paramInfo, value);
            } else if (paramInfo.getName().equals(L2PROD)) {
                setProdParamValue(value);
            } else {
                debug("adding " + paramInfo.getName() + "=" + value + " to paramValues");
                if (value.length() > 0) {
                    paramInfo.setValue(value);
                    setConflictingParams(paramInfo.getName());
                } else {
                    paramInfo.setValue(paramInfo.getDefaultValue());
                }

                debug("Firing Event with eventName=" + paramInfo.getName());
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, paramInfo.getName(), null, null));
            }
        }
    }


    public boolean isParamDefault(String param) {

        for (ParamInfo paramInfo : paramInfos) {
            if (paramInfo.getName().toLowerCase().equals(param.toLowerCase())) {
                return isParamDefault(paramInfo);
            }
        }
        return false;
    }

    public boolean isParamDefault(ParamInfo paramInfo) {
        if (paramInfo.getValue().equals(paramInfo.getDefaultValue())) {
            return true;
        } else {
            return false;
        }
    }


    public String getParamDefault(String param) {
        String value = null;

        if (param == null) {
            return null;
        }

        for (ParamInfo paramInfo : paramInfos) {
            if (paramInfo.getName().toLowerCase().equals(param.toLowerCase())) {
                paramInfo.getDefaultValue();
            }
        }
        return value;
    }

    public void setParamToDefaults(ParamInfo paramInfo) {
        // handle input exceptions
        if (paramInfo == null) {
            return;
        }

        setParamValue(paramInfo, paramInfo.getDefaultValue());
    }


    public void setToDefaults(ParamCategoryInfo paramCategoryInfo) {
        for (ParamInfo paramInfo : paramCategoryInfo.getParamInfos()) {
            setParamToDefaults(paramInfo);
        }
    }

    public void setParamToDefaults(String param) {
        // Cleanup input and handle input exceptions
        if (param == null || param.length() == 0) {
            return;
        }
        param = param.trim();

        for (ParamInfo paramInfo : paramInfos) {
            if (paramInfo.getName().equals(param)) {
                setParamValue(paramInfo, paramInfo.getDefaultValue());
                return;
            }
        }

    }

    public void setParamDefaultValue(String param, String value) {

        // Cleanup inputs and handle input exceptions
        if (param == null || param.length() == 0) {
            return;
        }
        if (value == null) {
            value = ParamInfo.NULL_STRING;
        }
        param = param.trim();
        value = value.trim();


        for (ParamInfo paramInfo : paramInfos) {
            if (paramInfo.getName().toLowerCase().equals(param.toLowerCase())) {
                setParamDefaultValue(paramInfo, value);
                return;
            }
        }
    }


    public void setParamDefaultValue(ParamInfo paramInfo, String value) {
        if (value == null) {
            value = ParamInfo.NULL_STRING;
        }
        value = value.trim();

        paramInfo.setDefaultValue(value);
    }


    private void setProdParamValue(String inProd) {

        if (inProd == null) {
            inProd = "";
        }

        // if product changed
        if (!inProd.equals(getParamValue(L2PROD))) {
            TreeSet<String> inProducts = new TreeSet<String>();
            for (String prodEntry : inProd.split(" ")) {
                prodEntry.trim();
                inProducts.add(prodEntry);
            }

            //----------------------------------------------------------------------------------------------------
            // For every product in ProductInfoArray set selected to agree with inProducts
            //----------------------------------------------------------------------------------------------------

            BaseInfo.State newState;

            for (ProductInfo productInfo : productInfos) {
                for (BaseInfo aInfo : productInfo.getChildren()) {
                    AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;

                    if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.NONE) {
                        if (inProducts.contains(algorithmInfo.getFullName())) {
                            newState = AlgorithmInfo.State.SELECTED;
                        } else {
                            newState = AlgorithmInfo.State.NOT_SELECTED;
                        }

                        if (algorithmInfo.getState() != newState) {
                            algorithmInfo.setState(newState);
                        }
                    } else {
                        for (BaseInfo wInfo : aInfo.getChildren()) {
                            WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;

                            if (inProducts.contains(wavelengthInfo.getFullName())) {
                                newState = WavelengthInfo.State.SELECTED;
                            } else {
                                newState = WavelengthInfo.State.NOT_SELECTED;
                            }
                            if (wavelengthInfo.getState() != newState) {
                                wavelengthInfo.setState(newState);
                            }
                        }

                        if (inProducts.contains(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.VISIBLE))) {
                            algorithmInfo.setStateShortcut(AlgorithmInfo.ShortcutType.VISIBLE, AlgorithmInfo.State.SELECTED);
                        }

                        if (inProducts.contains(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.IR))) {
                            algorithmInfo.setStateShortcut(AlgorithmInfo.ShortcutType.IR, AlgorithmInfo.State.SELECTED);
                        }

                        if (inProducts.contains(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.ALL))) {
                            algorithmInfo.setStateShortcut(AlgorithmInfo.ShortcutType.ALL, AlgorithmInfo.State.SELECTED);
                        }
                    }
                }
            }

            fireEvent(L2PROD);
        }
    }

    public boolean isParamCategoryDefault(ParamCategoryInfo paramCategoryInfo) {
        boolean isDefault = true;

        for (ParamInfo paramInfo : paramCategoryInfo.getParamInfos()) {
            if (!paramInfo.isDefault()) {
                isDefault = false;
            }
        }

        return isDefault;
    }


    public String getMissionString() {

        String missionString = "";

        for (ParamInfo paramInfo : paramInfos) {
            if (paramInfo.getName().equals(IFILE)) {
                if (paramInfo.getValue().length() > 0) {
                    File file = new File(paramInfo.getValue());

                    if (file != null && file.getName() != null) {
                        missionString = file.getName().substring(0, 1);
                    }
                }
                return missionString;
            }
        }

        return missionString;
    }


    private String getSensorInfoFilename() {

        // lookup hash relating mission letter with mission directory name
        final HashMap<String, String> missionDirectoryNameHashMap = new HashMap();
        missionDirectoryNameHashMap.put("S", "seawifs");
        missionDirectoryNameHashMap.put("A", "hmodisa");
        missionDirectoryNameHashMap.put("T", "hmodist");

        String missionDirectoryName = missionDirectoryNameHashMap.get(getMissionString());

        // determine the filename which contains the wavelengths
        final StringBuilder sensorInfoFilenameStringBuilder = new StringBuilder("");
        sensorInfoFilenameStringBuilder.append(OCDATAROOT);
        sensorInfoFilenameStringBuilder.append("/");
        sensorInfoFilenameStringBuilder.append(missionDirectoryName);
        sensorInfoFilenameStringBuilder.append("/");
        sensorInfoFilenameStringBuilder.append("msl12_sensor_info.dat");

        return sensorInfoFilenameStringBuilder.toString();
    }


    private void resetWaveLimiter() {
        waveLimiter.clear();

        // determine the filename which contains the wavelengths
        String sensorInfoFilename = getSensorInfoFilename();

        // read in the mission's datafile which contains the wavelengths
        //  final ArrayList<String> SensorInfoArrayList = myReadDataFile(sensorInfoFilename.toString());
        final ArrayList<String> SensorInfoArrayList = l2genReader.readFileIntoArrayList(sensorInfoFilename);
        debug("sensorInfoFilename=" + sensorInfoFilename);


        // loop through datafile
        for (String myLine : SensorInfoArrayList) {

            // skip the comment lines in file
            if (!myLine.trim().startsWith("#")) {

                // just look at value pairs of the form Lambda(#) = #
                String splitLine[] = myLine.split("=");
                if (splitLine.length == 2 &&
                        splitLine[0].trim().startsWith("Lambda(") &&
                        splitLine[0].trim().endsWith(")")
                        ) {

                    // get current wavelength and add into in a JCheckBox
                    final String currWavelength = splitLine[1].trim();

                    WavelengthInfo wavelengthInfo = new WavelengthInfo(currWavelength);
                    waveLimiter.add(wavelengthInfo);
                    debug("wavelengthLimiterArray adding wave=" + wavelengthInfo.getWavelengthString());
                }
            }
        }
    }


    private void resetProductInfos(boolean missionChanged) {

        for (ProductInfo productInfo : productInfos) {
            productInfo.setSelected(false);
            for (BaseInfo aInfo : productInfo.getChildren()) {
                aInfo.setSelected(false);
                AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;

                if (algorithmInfo.getParameterType() != AlgorithmInfo.ParameterType.NONE) {
                    if (missionChanged) {
                        algorithmInfo.clearChildren();
                        for (WavelengthInfo wavelengthInfo : waveLimiter) {
                            boolean addWavelength = false;

                            if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
                                addWavelength = true;
                            } else if (wavelengthInfo.getWavelength() >= WavelengthInfo.INFRARED_LOWER_LIMIT &&
                                    algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.IR) {
                                addWavelength = true;
                            } else if (wavelengthInfo.getWavelength() <= WavelengthInfo.VISIBLE_UPPER_LIMIT &&
                                    algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.VISIBLE) {
                                addWavelength = true;
                            }

                            if (addWavelength) {
                                WavelengthInfo newWavelengthInfo = new WavelengthInfo(wavelengthInfo.getWavelength());
                                newWavelengthInfo.setParent(algorithmInfo);
                                newWavelengthInfo.setDescription(algorithmInfo.getDescription() + ", at " + newWavelengthInfo.getWavelengthString());
                                algorithmInfo.addChild(newWavelengthInfo);
                            }
                        }
                    } else {
                        for (BaseInfo wInfo : algorithmInfo.getChildren()) {
                            wInfo.setSelected(false);
                        }
                    }
                }
            }
        }
    }

    // runs this if IFILE changes
    // it will reset missionString
    // it will reset and make new wavelengthInfoArray
    private void setIfileParamValue(ParamInfo paramInfo, String newIfile) {

        paramInfo.setValue(newIfile);

        if (new File(newIfile).exists()) {
            ifileIsValid = true;

            resetWaveLimiter();
            //  propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, WAVE_LIMITER_CHANGE_EVENT, null, null));
            resetProductInfos(true);

            updateXmlBasedObjects(newIfile);

            copyToProductDefaults();

            setCustomOfile(newIfile);
            setCustomGeofile(newIfile);

            debug(IFILE.toString() + "being fired");
            propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, IFILE, null, getMissionString()));

        } else {
            ifileIsValid = false;
            debug(INVALID_IFILE_EVENT.toString() + "being fired");
            propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, INVALID_IFILE_EVENT, null, getMissionString()));
        }


    }


    private void setCustomOfile(String ifile) {

        String IFILE_STRING_TO_BE_REPLACED[] = {"L1A", "L1B"};
        StringBuilder stringBuilder = new StringBuilder();
        String ofile = null;

        /**
         * replace last occurrence of instance of IFILE_STRING_TO_BE_REPLACED[]
         */
        for (String string_to_be_replaced : IFILE_STRING_TO_BE_REPLACED) {
            if (ifile.toUpperCase().contains(string_to_be_replaced)) {
                int index = ifile.toUpperCase().lastIndexOf(string_to_be_replaced);
                stringBuilder.append(ifile.substring(0, index));
                stringBuilder.append(TARGET_PRODUCT_SUFFIX);
                stringBuilder.append(ifile.substring((index + string_to_be_replaced.length()), ifile.length()));
                ofile = stringBuilder.toString();
                break;
            }
        }

        /**
         * Not found so append it
         */
        if (ofile == null) {
            stringBuilder.append(ifile);
            stringBuilder.append("." + TARGET_PRODUCT_SUFFIX);
            ofile = stringBuilder.toString();
        }

        setParamValue(OFILE, ofile);
    }


    private void setCustomGeofile(String ifile) {

        String geofile = ParamInfo.NULL_STRING;
        String VIIRS_IFILE_PREFIX = "SVM01";
        String VIIRS_GEOFILE_PREFIX = "GMTCO";


        File ifileFile = new File(ifile);



        if (ifileFile.getName().toUpperCase().startsWith(VIIRS_IFILE_PREFIX)) {
            StringBuilder geofileStringBuilder = new StringBuilder();
            geofileStringBuilder.append(ifileFile.getParent());
            geofileStringBuilder.append("/");
            geofileStringBuilder.append(VIIRS_GEOFILE_PREFIX);
            geofileStringBuilder.append(ifileFile.getName().substring(VIIRS_IFILE_PREFIX.length()));

            geofile = geofileStringBuilder.toString();
        } else {


            ArrayList<String> possibleGeoFiles = new ArrayList<String>();

            String STRING_TO_BE_REPLACED[] = {"L1A_LAC", "L1B_LAC"};

            /**
             * replace last occurrence of instance of STRING_TO_BE_REPLACED[]
             */
            for (String string_to_be_replaced : STRING_TO_BE_REPLACED) {
                if (ifile.toUpperCase().contains(string_to_be_replaced)) {

                    int index = ifile.toUpperCase().lastIndexOf(string_to_be_replaced);
                    String start = ifile.substring(0, index);
                    String end = ifile.substring((index + string_to_be_replaced.length()), ifile.length());

                    StringBuilder littleGeo = new StringBuilder(start + "geo" + end);
                    possibleGeoFiles.add(littleGeo.toString());

                    StringBuilder bigGeo = new StringBuilder(start + "GEO" + end);
                    possibleGeoFiles.add(bigGeo.toString());
                    break;
                }
            }

            StringBuilder addedLittleGeo = new StringBuilder(ifile + "." + "geo");
            possibleGeoFiles.add(addedLittleGeo.toString());
            StringBuilder addedBigGeo = new StringBuilder(ifile + "." + "GEO");
            possibleGeoFiles.add(addedBigGeo.toString());


            for (String possibleGeoFile : possibleGeoFiles) {
                if (new File(possibleGeoFile).exists()) {
                    geofile = possibleGeoFile;
                }
            }

        }

        setParamValue(GEOFILE, geofile);
    }


    private void debug(String string) {
        System.out.println(string);
    }


    /**
     * resets productInfos within productCategoryInfos to link to appropriate entry in productInfos
     */
    public void setProductCategoryInfosOld() {
        for (ProductCategoryInfo productCategoryInfo : productCategoryInfos) {
            productCategoryInfo.clearProductInfos();
        }

        for (ProductCategoryInfo productCategoryInfo : productCategoryInfos) {
            for (String categorizedProductName : productCategoryInfo.getProductNames()) {
                for (ProductInfo productInfo : productInfos) {
                    if (categorizedProductName.equals(productInfo.getName())) {
                        productCategoryInfo.addProductInfo(productInfo);
                    }
                }
            }
        }

        for (ProductInfo productInfo : productInfos) {
            boolean found = false;

            for (ProductCategoryInfo productCategoryInfo : productCategoryInfos) {
                for (String categorizedProductName : productCategoryInfo.getProductNames()) {
                    if (categorizedProductName.equals(productInfo.getName())) {
                        found = true;
                    }
                }
            }

            if (!found) {
                for (ProductCategoryInfo productCategoryInfo : productCategoryInfos) {
                    if (productCategoryInfo.isDefaultBucket()) {
                        productCategoryInfo.addProductInfo(productInfo);
                        l2genPrint.adminlog("Dropping uncategorized product '" + productInfo.getName() + "' into the defaultBucket");
                    }
                }
            }
        }
    }


    /**
     * resets productInfos within productCategoryInfos to link to appropriate entry in productInfos
     */
    public void setProductCategoryInfos() {
        for (ProductCategoryInfo productCategoryInfo : productCategoryInfos) {
            productCategoryInfo.clearChildren();
        }

        for (ProductCategoryInfo productCategoryInfo : productCategoryInfos) {
            for (String categorizedProductName : productCategoryInfo.getProductNames()) {
                for (ProductInfo productInfo : productInfos) {
                    if (categorizedProductName.equals(productInfo.getName())) {
                        productCategoryInfo.addChild(productInfo);
                    }
                }
            }
        }

        for (ProductInfo productInfo : productInfos) {
            boolean found = false;

            for (ProductCategoryInfo productCategoryInfo : productCategoryInfos) {
                for (String categorizedProductName : productCategoryInfo.getProductNames()) {
                    if (categorizedProductName.equals(productInfo.getName())) {
                        found = true;
                    }
                }
            }

            if (!found) {
                for (ProductCategoryInfo productCategoryInfo : productCategoryInfos) {
                    if (productCategoryInfo.isDefaultBucket()) {
                        productCategoryInfo.addChild(productInfo);
                        l2genPrint.adminlog("Dropping uncategorized product '" + productInfo.getName() + "' into the defaultBucket");
                    }
                }
            }
        }
    }


    /**
     * resets paramInfos within paramCategoryInfos to link to appropriate entry in paramInfos
     */
    public void setParamCategoryInfos() {
        for (ParamCategoryInfo paramCategoryInfo : paramCategoryInfos) {
            paramCategoryInfo.clearParamInfos();
        }

        for (ParamCategoryInfo paramCategoryInfo : paramCategoryInfos) {
            for (String categorizedParamName : paramCategoryInfo.getParamNames()) {
                for (ParamInfo paramInfo : paramInfos) {
                    if (categorizedParamName.equals(paramInfo.getName())) {
                        paramCategoryInfo.addParamInfos(paramInfo);
                    }
                }
            }
        }


        for (ParamInfo paramInfo : paramInfos) {
            boolean found = false;

            for (ParamCategoryInfo paramCategoryInfo : paramCategoryInfos) {
                for (String categorizedParamName : paramCategoryInfo.getParamNames()) {
                    if (categorizedParamName.equals(paramInfo.getName())) {
                        //  paramCategoryInfo.addParamInfos(paramInfo);
                        found = true;
                    }
                }
            }

            if (!found) {
                for (ParamCategoryInfo paramCategoryInfo : paramCategoryInfos) {
                    if (paramCategoryInfo.isDefaultBucket()) {
                        paramCategoryInfo.addParamInfos(paramInfo);
                        l2genPrint.adminlog("Dropping uncategorized param '" + paramInfo.getName() + "' into the defaultBucket");
                    }
                }
            }
        }


    }


    public boolean compareWavelengthLimiter(WavelengthInfo wavelengthInfo) {
        for (WavelengthInfo wavelengthLimitorInfo : getWaveLimiter()) {
            if (wavelengthLimitorInfo.getWavelength() == wavelengthInfo.getWavelength()) {
                if (wavelengthLimitorInfo.isSelected()) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    public ArrayList<ParamCategoryInfo> getParamCategoryInfos() {
        return paramCategoryInfos;
    }

    public void setParamCategoryInfos(ArrayList<ParamCategoryInfo> paramCategoryInfos) {
        this.paramCategoryInfos = paramCategoryInfos;
    }

    public void addParamCategoryInfo(ParamCategoryInfo paramCategoryInfo) {
        paramCategoryInfos.add(paramCategoryInfo);
    }

    public void clearParamCategoryInfos() {
        paramCategoryInfos.clear();
    }

    public ArrayList<ProductCategoryInfo> getProductCategoryInfos() {
        return productCategoryInfos;
    }

    public void addProductCategoryInfo(ProductCategoryInfo productCategoryInfo) {
        productCategoryInfos.add(productCategoryInfo);
    }

    public void clearProductCategoryInfos() {
        productCategoryInfos.clear();
    }

    private void updateXmlBasedObjects(String ifile) {
        InputStream paramInfoStream = L2genForm.class.getResourceAsStream(getParamInfoXml(ifile));
        l2genReader.updateParamInfosWithXml(paramInfoStream);
    }


    private String getProductInfoXml(String ifile) {

        return PRODUCT_INFO_XML;

    }

    private String getParamInfoXml(String file) {

        return PARAM_INFO_XML;
    }


    public void initXmlBasedObjects() {

        InputStream stream = L2genForm.class.getResourceAsStream(getProductInfoXml(initialIfile));
        l2genReader.readProductsXml(stream);

        InputStream paramInfoStream = L2genForm.class.getResourceAsStream(getParamInfoXml(initialIfile));
        l2genReader.readParamInfoXml(paramInfoStream);

        InputStream paramCategoryInfoStream = L2genForm.class.getResourceAsStream(PARAM_CATEGORY_INFO_XML);
        l2genReader.readParamCategoryXml(paramCategoryInfoStream);
        setParamCategoryInfos();

        InputStream productCategoryInfoStream = L2genForm.class.getResourceAsStream(PRODUCT_CATEGORY_INFO_XML);
        l2genReader.readProductCategoryXml(productCategoryInfoStream);
        setProductCategoryInfos();

    }


    //  The below lines are not currently in use

//
//    private ArrayList<String> myReadDataFile(String fileName) {
//        String lineData;
//        ArrayList<String> fileContents = new ArrayList<String>();
//        BufferedReader moFile = null;
//        try {
//            moFile = new BufferedReader(new FileReader(new File(fileName)));
//            while ((lineData = moFile.readLine()) != null) {
//
//                fileContents.add(lineData);
//            }
//        } catch (IOException e) {
//            ;
//        } finally {
//            try {
//                moFile.close();
//            } catch (Exception e) {
//                //Ignore
//            }
//        }
//        return fileContents;
//    }
//


}


