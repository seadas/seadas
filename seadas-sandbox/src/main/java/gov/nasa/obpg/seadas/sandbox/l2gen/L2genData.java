package gov.nasa.obpg.seadas.sandbox.l2gen;


import org.esa.beam.util.StringUtils;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.io.DataInputStream;
import java.util.*;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class L2genData {

    private String OCDATAROOT = System.getenv("OCDATAROOT");

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

    public final String PARFILE_CHANGE_EVENT = "PARFILE_TEXT_CHANGE_EVENT";
    public final String MISSION_CHANGE_EVENT = "MISSION_STRING_CHANGE_EVENT";
    public final String WAVE_LIMITER_CHANGE_EVENT = "UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT";
    public final String PRODUCT_CHANGED_EVENT = "PRODUCT_CHANGED_EVENT";
    public final String DEFAULTS_CHANGED_EVENT = "DEFAULTS_CHANGED_EVENT";

    private final String TARGET_PRODUCT_SUFFIX = "L2";

    // Groupings of Parameter Keys
    private final String[] coordParams = {NORTH, SOUTH, WEST, EAST};
    private final String[] pixelParams = {SPIXL, EPIXL, DPIXL, SLINE, ELINE, DLINE};
    private final String[] fileIOParams = {IFILE, OFILE};

    private L2genReader l2genReader = new L2genReader(this);

    private HashMap<String, String> paramValues = new HashMap();
    private HashMap<String, String> defaultParamValues = new HashMap();

    private ArrayList<ProductInfo> productInfos = new ArrayList<ProductInfo>();
    private ArrayList<ParamOptionsInfo> paramOptionsInfos = new ArrayList<ParamOptionsInfo>();
    private ArrayList<ParamCategoriesInfo> paramCategoriesInfos = new ArrayList<ParamCategoriesInfo>();


    private ArrayList<WavelengthInfo> waveLimiter = new ArrayList<WavelengthInfo>();

    private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    private L2genPrint l2genPrint = new L2genPrint();

    public enum RegionType {Coordinates, PixelLines}

    public EventInfo[] eventInfos = {
            new EventInfo(PRODUCT_CHANGED_EVENT, this),
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


    public void setSelectedInfo(BaseInfo info, BaseInfo.State state) {

        if (state != info.getState()) {
            info.setState(state);
            fireEvent(PRODUCT_CHANGED_EVENT);
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

    public void addParamOptionsInfo(ParamOptionsInfo paramOptionsInfo) {
        paramOptionsInfos.add(paramOptionsInfo);
    }

    public void clearParamOptionsInfo() {
        paramOptionsInfos.clear();
    }

    public ArrayList<ParamOptionsInfo> getParamOptionsInfos() {
        return paramOptionsInfos;
    }

    public void addProductInfo(ProductInfo productInfo) {
        productInfos.add(productInfo);
    }


    public void clearProductInfos() {
        productInfos.clear();
    }

    public void clearParamOptionsInfos() {
        paramOptionsInfos.clear();
    }


    public void sortParamCategoriesInfos(Comparator<ParamCategoriesInfo> comparator) {
            Collections.sort(paramCategoriesInfos, comparator);
    }


    public void sortParamCategoriesInfos() {
            Collections.sort(paramCategoriesInfos);
    }


    public void sortParamOptionsInfos(Comparator<ParamOptionsInfo> comparator) {
        Collections.sort(paramOptionsInfos, comparator);
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


    private void specifyRegionType(String paramKey) {

        //---------------------------------------------------------------------------------------
        // Determine the regionType for paramKey
        //---------------------------------------------------------------------------------------
        RegionType regionType = null;

        // Look for paramKey in coordinateParamKeys
        for (String currKey : coordParams) {
            if (currKey.equals(paramKey)) {
                regionType = RegionType.Coordinates;
            }
        }

        // Look for paramKey in pixelLineParamKeys
        if (regionType == null) {
            for (String currKey : pixelParams) {
                if (currKey.equals(paramKey)) {
                    regionType = RegionType.PixelLines;
                }
            }
        }

        //---------------------------------------------------------------------------------------
        // Perform actions based on the regionType:
        // - if Coordinates are being used purge PixelLine fields
        // - if PixelLines are being used purge Coordinate fields
        //---------------------------------------------------------------------------------------
        if (regionType == RegionType.Coordinates) {
            // Since Coordinates are being used purge PixelLine fields
            for (String currKey : pixelParams) {
                if (paramValues.containsKey(currKey)) {
                    paramValues.remove(currKey);
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, currKey, null, ""));
                }
            }

        } else if (regionType == RegionType.PixelLines) {
            // Since PixelLines are being used purge Coordinate fields
            for (String currKey : coordParams) {
                if (paramValues.containsKey(currKey)) {
                    paramValues.remove(currKey);
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, currKey, null, ""));
                }
            }
        }
    }

    /**
     * Returns a single formatted key-value pair of the form 'key=value' used in the parfile
     *
     * @param name
     * @return
     */
    private String getParamValueEntry(String name) {

        StringBuilder stringBuilder = new StringBuilder("");

        stringBuilder.append(name);
        stringBuilder.append("=");
        stringBuilder.append(getParamValue(name));

        return stringBuilder.toString();
    }


    /**
     * Returns a categorized section of the parfile
     * <p/>
     * Creates a paramValue entry for every param in the inParams Array.
     * Removes that param from the inParamValues HashMap
     * Adds the sectionTitle if there is at least one entry
     *
     * @param sectionTitle
     * @param inParams
     * @param inParamValues
     * @return
     */
    private String getParfileSection(String sectionTitle, String[] inParams, HashMap<String, Object> inParamValues) {

        StringBuilder parfileSection = new StringBuilder("");

        // Add paramValue entries to parfileSection, removing them from paramValues as they are found
        // Do not make an entry if it matches the default value
        for (String param : inParams) {
            boolean makeEntry = false;
            if (inParamValues.containsKey(param)) {
                if (defaultParamValues.containsKey(param)) {
                    if (!defaultParamValues.get(param).equals(inParamValues.get(param))) {
                        makeEntry = true;
                    }
                } else {
                    makeEntry = true;
                }
            }

            if (makeEntry == true) {
                parfileSection.append(getParamValueEntry(param)).append("\n");
            }

            inParamValues.remove(param);
        }

        if (parfileSection.length() > 0) {
            // Only insert sectionTitle if there are entries
            if (sectionTitle != null) {
                parfileSection.insert(0, "\n");
                parfileSection.insert(0, sectionTitle);

            }

            parfileSection.append("\n");
        }

        return parfileSection.toString();
    }


    public String getParfile() {

        StringBuilder parfileStringBuilder = new StringBuilder("");
        HashMap<String, Object> paramValuesCopy = new HashMap();

        // Remove prod entry because we do not store it here.
        // This is just a precaution it should never have been stored.
        if (paramValues.containsKey(L2PROD)) {
            paramValues.remove(L2PROD);
        }

        // make a copy of paramValues so the keys can be deleted as they as used
        for (String currKey : paramValues.keySet()) {
            paramValuesCopy.put(currKey, paramValues.get(currKey));
        }

        parfileStringBuilder.append(
                getParfileSection(
                        "# FILE IO PARAMS",
                        fileIOParams,
                        paramValuesCopy
                ));


        parfileStringBuilder.append("# PRODUCT PARAM").append("\n");
        parfileStringBuilder.append(getParamValueEntry(L2PROD)).append("\n\n");

        parfileStringBuilder.append(
                getParfileSection("# COORDINATE PARAMS",
                        coordParams,
                        paramValuesCopy
                ));

        parfileStringBuilder.append(
                getParfileSection("# PIXEL-LINE PARAMS",
                        pixelParams,
                        paramValuesCopy
                ));

        String additionalParamKeys[] = new String[paramValuesCopy.size()];
        int i = 0;
        for (String currKey : paramValuesCopy.keySet()) {
            additionalParamKeys[i] = currKey;
            i++;
        }

        parfileStringBuilder.append(
                getParfileSection("# ADDITIONAL USER PARAMS",
                        additionalParamKeys,
                        paramValuesCopy
                ));

        return parfileStringBuilder.toString();
    }


    private HashMap<String, String> parseParfile(String parfileString) {

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

    /**
     *
     */
    public void resetAll(boolean missionChanged) {

        if (missionChanged) {
            resetWaveLimiter();
        }

        resetProductInfos(missionChanged);

        String inParfile = getL2genDefaults();

        defaultParamValues.clear();
        defaultParamValues = parseParfile(inParfile);

        String prod = defaultParamValues.get(L2PROD);
        defaultParamValues.remove(L2PROD);

        if (prod != null) {
            setParamValue(L2PROD, prod);
        }

        copyToProductDefaults();

        /**
         * Update to main any default params with different values
         */
        for (String defaultParam : defaultParamValues.keySet()) {
            setParamValue(defaultParam, defaultParamValues.get(defaultParam));
        }

        /**
         * Remove from main any params not in the defaults 
         */

        HashMap<String, String> tmpParamValues = new HashMap<String, String>();
        for (String key : paramValues.keySet()) {
            tmpParamValues.put(key, paramValues.get(key));
        }
        for (String param : tmpParamValues.keySet()) {
            if (!param.equals(IFILE) && !defaultParamValues.containsKey(param)) {
                deleteParamValue(param);
            }
        }

        if (!paramValues.containsKey(OFILE)) {
            setCustomOfile();
        }
    }


    public void setParfile(String newParfile) {

        HashMap<String, String> newParamValues = parseParfile(newParfile);

        if (newParamValues != null && newParamValues.size() > 0) {

            /*
               It present, do the ifile first because it will alter and reset everything
            */
            if (newParamValues.containsKey(IFILE)) {
                setParamValue(IFILE, newParamValues.get(IFILE));
                newParamValues.remove(IFILE);
            }

            /*
               Make a copy of paramValues to serve as a source to loop through
               because the actual paramValues will be altered during the looping
            */
            HashMap<String, String> tmpParamValues = new HashMap<String, String>();
            for (String key : paramValues.keySet()) {
                tmpParamValues.put(key, paramValues.get(key));
            }

            // Remove any keys in paramValues which are not present in newParamValues
            for (String key : tmpParamValues.keySet()) {
                if (!key.equals(IFILE) && !newParamValues.containsKey(key)) {
                    deleteParamValue(key);
                }
            }

            // Set l2prod to defaults if newParamValues does not contain l2prod
            if (!newParamValues.containsKey(L2PROD)) {
                copyFromProductDefaults();
            }

            // Set a paramValues to newParamValues
            for (String key : newParamValues.keySet()) {
                setParamValue(key, newParamValues.get(key));
            }
        }
    }


    public void setParfileOld(String inParfile) {

        HashMap<String, String> inParfileHashMap = parseParfile(inParfile);

        if (inParfileHashMap != null && inParfileHashMap.size() > 0) {

            HashMap<String, String> copyOfParamValues = new HashMap<String, String>();

            for (String key : paramValues.keySet()) {
                copyOfParamValues.put(key, paramValues.get(key));
            }

            // Remove any keys in paramValueHashMap which are not in inParfileHashMap
            for (String key : copyOfParamValues.keySet()) {
                if (!key.equals(IFILE) && !inParfileHashMap.containsKey(key)) {
                    deleteParamValue(key);
                }
            }

            // Do ifile first
            if (inParfileHashMap.containsKey(IFILE)) {
                setParamValue(IFILE, inParfileHashMap.get(IFILE));
                inParfileHashMap.remove(IFILE);
            }

            if (!inParfileHashMap.containsKey(L2PROD)) {
                copyFromProductDefaults();
                inParfileHashMap.remove(L2PROD);
            }
            // Initialize with defaultParamValueHashMap
//            for (String key : defaultParamValues.keySet()) {
//                setParamValue(key, defaultParamValues.get(key));
//            }


            for (String key : inParfileHashMap.keySet()) {
                debug("Setting parfile entry " + key + "=" + inParfileHashMap.get(key));
                setParamValue(key, inParfileHashMap.get(key));
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
            fireEvent(PRODUCT_CHANGED_EVENT);
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

//
//    public void applyParfileDefaults() {
//
//
//        HashMap<String, String> copyParamValueHashMap = new HashMap<String, String>();
//
//        for (String key : paramValues.keySet()) {
//            debug("key=" + key + "value=" + paramValues.get(key));
//            copyParamValueHashMap.put(key, paramValues.get(key));
//        }
//
//        // remove any params which are not in defaultParamValueHashMap
//        for (String key : copyParamValueHashMap.keySet()) {
//            if (!key.equals(IFILE) && !key.equals(OFILE) && !defaultParamValues.containsKey(key)) {
//                deleteParamValue(key);
//            }
//        }
//
//        // Set all  paramValueHashMap with  defaultParamValueHashMap
//        for (String key : defaultParamValues.keySet()) {
//            setParamValue(key, defaultParamValues.get(key));
//        }
//
//        copyFromProductDefaults();
//    }


    public String getParamValue(String key) {

        if (key == null) {
            return "";
        }

        if (key.equals(L2PROD)) {
            return getProdParamValue();
        }

        if (paramValues.get(key) != null) {
            return paramValues.get(key);
        } else {
            return "";
        }
    }


    public void deleteParamValue(String inKey) {

        if (inKey != null && inKey.length() > 0) {
            inKey = inKey.trim();
            if (paramValues.containsKey(inKey)) {
                if (defaultParamValues.containsKey(inKey)) {
                    setParamValue(inKey, defaultParamValues.get(inKey));
                } else {
                    String oldParamValue = paramValues.get(inKey);
                    paramValues.remove(inKey);
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, inKey, oldParamValue, null));
                }
            }
        }
    }

    public void setParamValue(String inKey, String inValue) {

        debug("setParamValue inKey=" + inKey + " inValue=" + inValue);
        if (inKey != null && inKey.length() > 0) {
            inKey = inKey.trim();

            if (inKey.equals(L2PROD)) {
                setProdParamValue(inValue);
            } else {
                if (inValue != null && inValue.length() > 0) {
                    inValue = inValue.trim();
                    debug("new Param" + inKey + "=" + inValue);

                    if (!inValue.equals(paramValues.get(inKey))) {
                        if (inKey.equals(IFILE)) {
                            setIfileParamValue(inValue);
                        } else {
                            paramValues.put(inKey, inValue);
                            specifyRegionType(inKey);
                        }

                        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, inKey, null, null));
                    }
                } else {
                    deleteParamValue(inKey);
                }
            }
        }
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

            fireEvent(PRODUCT_CHANGED_EVENT);
        }
    }


    public String getMissionString() {

        String missionString = "";

        if (paramValues.containsKey(IFILE) && paramValues.get(IFILE) != null) {
            File file = new File(paramValues.get(IFILE));

            if (file != null && file.getName() != null) {
                missionString = file.getName().substring(0, 1);
            }
        }

        return missionString;
    }


    private String getSensorInfoFilename() {

        // lookup hash relating mission letter with mission directory name
        final HashMap<String, String> missionDirectoryNameHashMap = new HashMap();
        missionDirectoryNameHashMap.put("S", "seawifs");
        missionDirectoryNameHashMap.put("A", "modisa");
        missionDirectoryNameHashMap.put("T", "modist");

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
                            if (wavelengthInfo.getWavelength() < WavelengthInfo.VISIBLE_UPPER_LIMIT) {
                                if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.VISIBLE ||
                                        algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
                                    WavelengthInfo newWavelengthInfo = new WavelengthInfo(wavelengthInfo.getWavelength());
                                    newWavelengthInfo.setParent(algorithmInfo);
                                    newWavelengthInfo.setDescription(algorithmInfo.getDescription() + ", at " + newWavelengthInfo.getWavelengthString());
                                    algorithmInfo.addChild(newWavelengthInfo);
                                }
                            } else {
                                if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.IR ||
                                        algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
                                    WavelengthInfo newWavelengthInfo = new WavelengthInfo(wavelengthInfo.getWavelength());
                                    newWavelengthInfo.setParent(algorithmInfo);
                                    newWavelengthInfo.setDescription(algorithmInfo.getDescription() + ", at " + newWavelengthInfo.getWavelengthString());
                                    algorithmInfo.addChild(newWavelengthInfo);
                                }
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
    private void setIfileParamValue(String newIfile) {

        boolean missionChanged = false;
        String previousMissionString = getMissionString();
        paramValues.put(IFILE, newIfile);
        if (getMissionString() != null
                && (previousMissionString == null || !previousMissionString.equals(getMissionString()))) {
            missionChanged = true;
        }

        resetAll(missionChanged);

        debug(MISSION_CHANGE_EVENT.toString() + "being fired");
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, MISSION_CHANGE_EVENT, null, getMissionString()));
    }


    private void setCustomOfile() {

        String ifile = paramValues.get(IFILE);
        String ofile;

        if (ifile != null) {
            String ifileSuffixTrimmedOff;

            int i = ifile.lastIndexOf('.');
            if (i != -1) {
                ifileSuffixTrimmedOff = ifile.substring(0, i);
            } else {
                ifileSuffixTrimmedOff = ifile;
            }

            ofile = ifileSuffixTrimmedOff + "." + TARGET_PRODUCT_SUFFIX;

        } else {
            ofile = "";
        }

        debug("DEBUG ofile=" + ofile);
        paramValues.put(OFILE, ofile);

        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, OFILE, null, null));
    }


    private String getL2genDefaults() {

        //todo add logic to create defaults file

        String L2GEN_DEFAULTS_FILENAME = "l2genDefaults.par";


        InputStream stream = L2genData.class.getResourceAsStream(L2GEN_DEFAULTS_FILENAME);

        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(stream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        StringBuilder stringBuilder = new StringBuilder();
        //Read File Line By Line
        try {
            while ((strLine = br.readLine()) != null) {
                stringBuilder.append(strLine);
                stringBuilder.append("\n");
            }
        } catch (IOException e) {
        }

        return stringBuilder.toString();
    }

    private void debug(String string) {
        System.out.println(string);
    }


    /**
     * resets paramOptionsInfos within paramCategoriesInfos to link to appropriate entry in paramOptionsInfos
     */
    public void setParamCategoriesInfos() {

        for (ParamCategoriesInfo paramCategoriesInfo : paramCategoriesInfos) {
            paramCategoriesInfo.clearParamOptionsInfos();
        }

        for (ParamOptionsInfo paramOptionsInfo : paramOptionsInfos) {
            boolean found = false;

            for (ParamCategoriesInfo paramCategoriesInfo : paramCategoriesInfos) {
                for (String categorizedParamName : paramCategoriesInfo.getParamNames()) {
                    if (categorizedParamName.equals(paramOptionsInfo.getName())) {
                        paramCategoriesInfo.addParamOptionsInfos(paramOptionsInfo);
                        found = true;
                    }
                }
            }

            if (!found) {
                for (ParamCategoriesInfo paramCategoriesInfo : paramCategoriesInfos) {
                    if (paramCategoriesInfo.isDefaultBucket()) {
                        paramCategoriesInfo.addParamOptionsInfos(paramOptionsInfo);
                        l2genPrint.adminlog("Dropping uncategorized param '" + paramOptionsInfo.getName() + "' into the defaultBucket");
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

    public ArrayList<ParamCategoriesInfo> getParamCategoriesInfos() {
        return paramCategoriesInfos;
    }

    public void setParamCategoriesInfos(ArrayList<ParamCategoriesInfo> paramCategoriesInfos) {
        this.paramCategoriesInfos = paramCategoriesInfos;
    }

    public void addParamCategoriesInfo(ParamCategoriesInfo paramCategoriesInfo) {
        paramCategoriesInfos.add(paramCategoriesInfo);
    }

    public void clearParamCategoriesInfos() {
        paramCategoriesInfos.clear();
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


