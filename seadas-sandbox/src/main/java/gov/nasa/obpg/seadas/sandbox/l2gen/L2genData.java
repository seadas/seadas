package gov.nasa.obpg.seadas.sandbox.l2gen;


import com.sun.org.apache.xml.internal.security.algorithms.Algorithm;
import com.sun.xml.internal.rngom.parse.host.Base;
import org.esa.beam.util.StringUtils;
import org.esa.beam.util.math.Array;

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
    public final String PROD = "l2prod";

    public final String PARFILE_CHANGE_EVENT = "PARFILE_TEXT_CHANGE_EVENT";
    public final String MISSION_CHANGE_EVENT = "MISSION_STRING_CHANGE_EVENT";
    public final String WAVELENGTH_LIMITER_CHANGE_EVENT = "UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT";
    public final String PRODUCT_CHANGED_EVENT = "PRODUCT_CHANGED_EVENT";
    public final String DEFAULTS_CHANGED_EVENT = "DEFAULTS_CHANGED_EVENT";

    private final String TARGET_PRODUCT_SUFFIX = "L2";

    // Groupings of Parameter Keys
    private final String[] coordinateParamKeys = {NORTH, SOUTH, WEST, EAST};
    private final String[] pixelLineParamKeys = {SPIXL, EPIXL, DPIXL, SLINE, ELINE, DLINE};
    private final String[] fileIOParamKeys = {IFILE, OFILE};

    private L2genReader l2genReader = new L2genReader(this);

    private HashMap<String, String> parfileHashMap = new HashMap();
    private HashMap<String, String> defaultParfileHashMap = new HashMap();

    private ArrayList<ProductInfo> productInfoArray = new ArrayList<ProductInfo>();

    private ArrayList<WavelengthInfo> wavelengthLimiterArray = new ArrayList<WavelengthInfo>();

    private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    private boolean ignoreProductStateCheck = false;
    private boolean ignoreAlgorithmStateCheck = false;

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


    private void fireEvent(String eventName, Object oldValue, Object newValue) {
        for (EventInfo eventInfo : eventInfos) {
            if (eventName.equals(eventInfo.toString())) {
                eventInfo.fireEvent(oldValue, newValue);
                return;
            }
        }
    }


    private void checkProductState(ProductInfo productInfo) {
        if (ignoreProductStateCheck) {
            return;
        }

        BaseInfo.State newState = productInfo.getState();

        if (productInfo.hasChildren()) {
            boolean selectedFound = false;
            boolean notSelectedFound = false;

            for (BaseInfo aInfo : productInfo.getChildren()) {
                switch (aInfo.getState()) {
                    case SELECTED:
                        selectedFound = true;
                        break;
                    case PARTIAL:
                        selectedFound = true;
                        notSelectedFound = true;
                        break;
                    case NOT_SELECTED:
                        notSelectedFound = true;
                        break;
                }
            }

            if (selectedFound && !notSelectedFound) {
                newState = BaseInfo.State.SELECTED;
            } else if (!selectedFound && notSelectedFound) {
                newState = BaseInfo.State.NOT_SELECTED;
            } else if (selectedFound && notSelectedFound) {
                newState = BaseInfo.State.PARTIAL;
            }

        } else if (productInfo.getState() == BaseInfo.State.PARTIAL) {
            newState = BaseInfo.State.SELECTED;
            debug("in checkProductState converted newState to " + newState);
        }

        if (newState != productInfo.getState()) {
            productInfo.setState(newState);
            debug("in checkProductState newState changed to = " + newState);
            fireEvent(PRODUCT_CHANGED_EVENT);
        }
    }


    private void checkAlgorithmState(AlgorithmInfo algorithmInfo) {

        if (ignoreAlgorithmStateCheck) {
            return;
        }

        BaseInfo.State newState = algorithmInfo.getState();

        if (algorithmInfo.hasChildren()) {
            boolean selectedFound = false;
            boolean notSelectedFound = false;

            for (BaseInfo wInfo : algorithmInfo.getChildren()) {
                switch (wInfo.getState()) {
                    case SELECTED:
                        selectedFound = true;
                        break;
                    case PARTIAL:
                        selectedFound = true;
                        notSelectedFound = true;
                        break;
                    case NOT_SELECTED:
                        notSelectedFound = true;
                        break;
                }
            }

            if (selectedFound && !notSelectedFound) {
                newState = BaseInfo.State.SELECTED;
            } else if (!selectedFound && notSelectedFound) {
                newState = BaseInfo.State.NOT_SELECTED;
            } else if (selectedFound && notSelectedFound) {
                newState = BaseInfo.State.PARTIAL;
            }
        } else if (newState == BaseInfo.State.PARTIAL) {
            newState = BaseInfo.State.SELECTED;
            debug("in checkAlgorithmState converted newState to " + newState);
        }

        disableEvent(PRODUCT_CHANGED_EVENT);

        if (newState != algorithmInfo.getState()) {
            debug("in checkAlgorithmState newState found =" + newState);
            algorithmInfo.setState(newState);
            fireEvent(PRODUCT_CHANGED_EVENT);
        }

        checkProductState(algorithmInfo.getProductInfo());

        enableEvent(PRODUCT_CHANGED_EVENT);
    }


    public void setSelectedInfo(BaseInfo info, BaseInfo.State state) {

        if (state != info.getState()) {
            info.setState(state);
            fireEvent(PRODUCT_CHANGED_EVENT);
        }
    }


    public ArrayList<Object> getSelectedProducts() {

        ArrayList<Object> selectedProducts = new ArrayList<Object>();

        for (ProductInfo productInfo : productInfoArray) {
            for (BaseInfo aInfo : productInfo.getChildren()) {
                if (aInfo.hasChildren()) {
                    for (BaseInfo wInfo : aInfo.getChildren()) {
                        if (wInfo.isSelected()) {
                            selectedProducts.add(wInfo);
                        }
                    }
                } else {
                    if (aInfo.isSelected()) {
                        selectedProducts.add(aInfo);
                    }
                }
            }
        }

        debug("IN getSelectedProducts count = " + selectedProducts.size());

        return selectedProducts;
    }


    public String getProd() {
        ArrayList<String> prodArrayList = new ArrayList<String>();

        for (Object selectedProduct : getSelectedProducts()) {
            if (selectedProduct instanceof AlgorithmInfo) {
                AlgorithmInfo algorithmInfo = (AlgorithmInfo) selectedProduct;
                prodArrayList.add(algorithmInfo.getFullName());
            } else if (selectedProduct instanceof WavelengthInfo) {
                WavelengthInfo wavelengthInfo = (WavelengthInfo) selectedProduct;
                prodArrayList.add(wavelengthInfo.getFullName());
            }
        }

        return StringUtils.join(prodArrayList, " ");
    }


    public void setSelectedWavelengthLimiterArray(String wavelength, boolean selected) {

        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
            if (wavelength.equals(wavelengthInfo.getWavelengthString())) {
                debug(wavelength + ":" + wavelengthInfo.isSelected() + ":" + selected);
                if (selected != wavelengthInfo.isSelected()) {
                    wavelengthInfo.setSelected(selected);
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, WAVELENGTH_LIMITER_CHANGE_EVENT, null, null));
                }
            }
        }
    }

    public boolean hasWavelengthLimiterTypeIii() {

        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
            if (wavelengthInfo.isIR()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasWavelengthLimiterTypeVvv() {

        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
            if (wavelengthInfo.isVisible()) {
                return true;
            }
        }

        return false;
    }

    public boolean isSelectedWavelengthLimiterTypeIii() {

        int count = 0;
        int selectedCount = 0;

        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
            if (wavelengthInfo.isIR() && wavelengthInfo.isSelected()) {
                selectedCount++;
                count++;
            }
        }

        if (count > 0 && selectedCount == count) {
            debug("iii is selected" + count + " " + selectedCount);
            return true;
        } else {
            debug("iii is NOT selected" + count + " " + selectedCount);
            return false;
        }
    }


    public void setSelectedWavelengthTypeIii(boolean selected) {

        debug("setSelectedWavelengthTypeIii called with selected=" + selected);
        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
            if (wavelengthInfo.isIR()) {
                debug("setting  IR wave=" + wavelengthInfo.getWavelengthString() + " to selected=" + selected);
                wavelengthInfo.setSelected(selected);
            }
        }

        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, WAVELENGTH_LIMITER_CHANGE_EVENT, null, null));

    }


    public boolean isSelectedWavelengthLimiterTypeVvv() {

        int count = 0;
        int selectedCount = 0;

        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
            if (wavelengthInfo.isVisible() && wavelengthInfo.isSelected()) {
                selectedCount++;
                count++;
            }
        }

        if (count > 0 && selectedCount == count) {
            return true;
        } else {
            return false;
        }
    }


    public void setSelectedWavelengthTypeVvv(boolean selected) {

        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
            if (wavelengthInfo.isVisible()) {
                wavelengthInfo.setSelected(selected);
            }
        }

        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, WAVELENGTH_LIMITER_CHANGE_EVENT, null, null));
    }


    public void addProductInfoArray(ProductInfo productInfo) {
        productInfoArray.add(productInfo);
    }


    public void clearProductInfoArray() {
        productInfoArray.clear();
    }

    public void sortProductInfoArray(Comparator<ProductInfo> comparator) {
        Collections.sort(productInfoArray, comparator);
    }


    public ArrayList<ProductInfo> getProductInfoArray() {
        return productInfoArray;
    }


    public ArrayList<WavelengthInfo> getWavelengthLimiterArray() {
        return wavelengthLimiterArray;
    }


    private void specifyRegionType(String paramKey) {

        //---------------------------------------------------------------------------------------
        // Determine the regionType for paramKey
        //---------------------------------------------------------------------------------------
        RegionType regionType = null;

        // Look for paramKey in coordinateParamKeys
        for (String currKey : coordinateParamKeys) {
            if (currKey.equals(paramKey)) {
                regionType = RegionType.Coordinates;
            }
        }

        // Look for paramKey in pixelLineParamKeys
        if (regionType == null) {
            for (String currKey : pixelLineParamKeys) {
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
            for (String currKey : pixelLineParamKeys) {
                if (parfileHashMap.containsKey(currKey)) {
                    parfileHashMap.remove(currKey);
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, currKey, null, ""));
                }
            }

        } else if (regionType == RegionType.PixelLines) {
            // Since PixelLines are being used purge Coordinate fields
            for (String currKey : coordinateParamKeys) {
                if (parfileHashMap.containsKey(currKey)) {
                    parfileHashMap.remove(currKey);
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, currKey, null, ""));
                }
            }
        }
    }


    //    For any given paramValue assemble it formatted parfile 'name=value' entry
    //    Do not make an entry if it matches the default value

    private void makeOptionalParfileEntry(StringBuilder stringBuilder, String currKey) {

        boolean makeEntry = false;

        if (parfileHashMap.containsKey(currKey)) {
            if (defaultParfileHashMap.containsKey(currKey)) {
                if (!defaultParfileHashMap.get(currKey).equals(parfileHashMap.get(currKey))) {
                    makeEntry = true;
                    debug("LATER currKey=" + currKey + ":main=" + parfileHashMap.get(currKey) + ":default=" + defaultParfileHashMap.get(currKey) + ":makeEntry=" + makeEntry);
                }
            } else {
                makeEntry = true;
            }
        }

        //      debug("LATER currKey="+currKey+":main="+paramValueHashMap.get(currKey)+":default="+defaultParamValueHashMap.get(currKey)+":makeEntry="+makeEntry);

        if (makeEntry == true) {
            stringBuilder.append(makeParfileEntry(currKey, parfileHashMap.get(currKey)));
        }
    }


    private String makeParfileEntry(String name, String value) {

        StringBuilder stringBuilder = new StringBuilder("");

        stringBuilder.append(name);
        stringBuilder.append("=");
        stringBuilder.append(value);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }


    public void makeParfileSubSection(String title, String[] paramKeys, HashMap<String, Object> paramValueHashMap, StringBuilder parfileText) {
        StringBuilder parfileSubText = new StringBuilder("");

        for (String key : paramKeys) {
            makeOptionalParfileEntry(parfileSubText, key);
            paramValueHashMap.remove(key);
        }
        if (parfileSubText.length() != 0) {
            parfileSubText.insert(0, title);
            parfileText.append(parfileSubText.toString());
        }
    }


    public String getParfile() {

        StringBuilder parfileStringBuilder = new StringBuilder("");
        HashMap<String, Object> paramValueHashMapCopy = new HashMap();

        // Remove prod entry because we do not store it here.
        // This is just a precaution it should never have been stored.
        if (parfileHashMap.containsKey(PROD)) {
            parfileHashMap.remove(PROD);
        }

        for (String currKey : parfileHashMap.keySet()) {
            paramValueHashMapCopy.put(currKey, parfileHashMap.get(currKey));
        }

        makeParfileSubSection("# FILE IO PARAMS\n",
                fileIOParamKeys,
                paramValueHashMapCopy,
                parfileStringBuilder
        );

        parfileStringBuilder.append("# PRODUCT PARAM\n");
        parfileStringBuilder.append(makeParfileEntry(PROD, getProd()));

        makeParfileSubSection("# COORDINATE PARAMS\n",
                coordinateParamKeys,
                paramValueHashMapCopy,
                parfileStringBuilder
        );

        makeParfileSubSection("# PIXEL-LINE PARAMS\n",
                pixelLineParamKeys,
                paramValueHashMapCopy,
                parfileStringBuilder
        );

        String additionalParamKeys[] = new String[paramValueHashMapCopy.size()];

        int i = 0;
        for (String currKey : paramValueHashMapCopy.keySet()) {
            additionalParamKeys[i] = currKey;
            i++;
        }

        makeParfileSubSection("# ADDITIONAL USER PARAMS\n",
                additionalParamKeys,
                paramValueHashMapCopy,
                parfileStringBuilder
        );

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
                    }
                }
            }
        }

        return thisParfileHashMap;
    }

// DANNY IS REVIEWING CODE AND LEFT OFF HERE

    public void setParfile(String inParfile) {

        HashMap<String, String> inParfileHashMap = parseParfile(inParfile);

        if (inParfileHashMap != null && inParfileHashMap.size() > 0) {

            HashMap<String, String> tmpParamValueHashMap = new HashMap<String, String>();
            HashMap<String, String> copyParamValueHashMap = new HashMap<String, String>();

            for (String key : parfileHashMap.keySet()) {
                copyParamValueHashMap.put(key, parfileHashMap.get(key));
            }

            // Initialize tmpParamValueHashMap with defaultParamValueHashMap
            for (String key : defaultParfileHashMap.keySet()) {
                tmpParamValueHashMap.put(key, defaultParfileHashMap.get(key));
            }

            // Update  tmpParamValueHashMap  with  inParfileHashMap.
            for (String key : inParfileHashMap.keySet()) {
                tmpParamValueHashMap.put(key, inParfileHashMap.get(key));
            }

            // Remove any keys in paramValueHashMap which are not in tmpParamValueHashMap
            for (String key : copyParamValueHashMap.keySet()) {

                if (!key.equals(IFILE) && !tmpParamValueHashMap.containsKey(key)) {
                    deleteParam(key);
                }
            }

            // Do ifile first
            if (tmpParamValueHashMap.containsKey(IFILE)) {
                setParamValue(IFILE, tmpParamValueHashMap.get(IFILE));

                tmpParamValueHashMap.remove(IFILE);
            }

            for (String key : tmpParamValueHashMap.keySet()) {
                setParamValue(key, tmpParamValueHashMap.get(key));
            }
        }
    }


    public void setDefaultParfile(String defaultParfile) {

        HashMap<String, String> defaultParfileHashMap = parseParfile(defaultParfile);

        if (defaultParfileHashMap != null && defaultParfileHashMap.size() > 0) {
            for (String key : defaultParfileHashMap.keySet()) {
                setDefaultParamValue(key, defaultParfileHashMap.get(key));
            }
        }
    }




    public void applyProductDefaults() {
        // This method loops through the entire productInfoArray setting all the states to the default state

        boolean productChanged = false;

        for (ProductInfo productInfo : productInfoArray) {
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


    public void applyParfileDefaults() {


        HashMap<String, String> copyParamValueHashMap = new HashMap<String, String>();

        for (String key : parfileHashMap.keySet()) {
            debug("key="+key+"value="+parfileHashMap.get(key));
            copyParamValueHashMap.put(key, parfileHashMap.get(key));
        }

        // remove any params which are not in defaultParamValueHashMap
        for (String key : copyParamValueHashMap.keySet()) {
            if (!key.equals(IFILE) && !key.equals(OFILE) && !defaultParfileHashMap.containsKey(key)) {
                deleteParam(key);
            }
        }

        // Set all  paramValueHashMap with  defaultParamValueHashMap
        for (String key : defaultParfileHashMap.keySet()) {
            setParamValue(key, defaultParfileHashMap.get(key));
        }

        applyProductDefaults();
    }


    public String getParamValue(String key) {
        if (key != null && parfileHashMap.get(key) != null) {
            if (key.equals(PROD)) {
                return getProd();
            } else {
                return parfileHashMap.get(key);
            }
        } else {
            return "";
        }
    }

    public void deleteParam(String inKey) {

        if (inKey != null && inKey.length() > 0) {
            inKey = inKey.trim();
            if (parfileHashMap.containsKey(inKey)) {
                if (defaultParfileHashMap.containsKey(inKey)) {
                    parfileHashMap.put(inKey, defaultParfileHashMap.get(inKey));
                } else {
                    parfileHashMap.remove(inKey);
                }

                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, inKey, null, null));
            }
        }
    }

    public void setParamValue(String inKey, String inValue) {

        debug("setParamValue inKey="+inKey+" inValue="+inValue);
        if (inKey != null && inKey.length() > 0) {
            inKey = inKey.trim();

            if (inKey.equals(PROD)) {
                handleProdKeyChange(inValue);
            } else {
                if (inValue != null && inValue.length() > 0) {
                    inValue = inValue.trim();
                    debug("new Param" + inKey + "=" + inValue);

                    if (!inValue.equals(parfileHashMap.get(inKey))) {
                        if (inKey.equals(IFILE)) {
                            handleIfileChange(inValue);
                        } else {
                            parfileHashMap.put(inKey, inValue);
                            specifyRegionType(inKey);
                        }

                        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, inKey, null, null));
                    }
                } else {
                    deleteParam(inKey);
                }
            }
        }
    }


    public void setDefaultParamValue(String inKey, String inValue) {

        if (inKey != null && inKey.length() > 0) {
            inKey = inKey.trim();

            boolean defaultsChanged = false;

            if (inKey.equals(PROD)) {
                debug("DEFAULT   PROD=" + inValue);
                handleDefaultsProdKeyChange(inValue);
            } else {
                if (inValue != null && inValue.length() > 0) {
                    inValue = inValue.trim();

                    if (!inValue.equals(defaultParfileHashMap.get(inKey))) {
                        defaultParfileHashMap.put(inKey, inValue);
                    }
                } else {
                    defaultParfileHashMap.remove(inKey);
                }
            }


            if (defaultsChanged) {
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, DEFAULTS_CHANGED_EVENT, null, null));
            }
        }
    }

    private boolean isValidL2prod(String inProductFullName) {

        if (inProductFullName != null) {
            for (ProductInfo productInfo : productInfoArray) {
                for (BaseInfo aInfo : productInfo.getChildren()) {
                    AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;

                    if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.NONE) {
                        if (inProductFullName.equals(algorithmInfo.getFullName())) {
                            return true;
                        }
                    } else {
                        for (BaseInfo wInfo : aInfo.getChildren()) {
                            WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;

                            if (inProductFullName.equals(wavelengthInfo.getFullName())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }


    private void handleProdKeyChange(String newProd) {

        //----------------------------------------------------------------------------------------------------
        // Put newProd into newProdTreeSet
        //----------------------------------------------------------------------------------------------------

        TreeSet<String> newProdTreeSet = new TreeSet<String>();

        debug("newProd=" + newProd);
        if (newProd != null) {
            for (String prodEntry : newProd.split(" ")) {
                prodEntry.trim();

                if (isValidL2prod(prodEntry)) {
                    newProdTreeSet.add(prodEntry);
                    debug("prodEntry=" + prodEntry);
                }
            }
        }

        //----------------------------------------------------------------------------------------------------
        // For every product in ProductInfoArray set selected to agree with newProdTreeSet
        //----------------------------------------------------------------------------------------------------

        boolean productInfoArrayChanged = false;
        BaseInfo.State newState;

        for (ProductInfo productInfo : productInfoArray) {
            for (BaseInfo aInfo : productInfo.getChildren()) {
                AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;

                if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.NONE) {
                    if (newProdTreeSet.contains(algorithmInfo.getFullName())) {
                        newState = AlgorithmInfo.State.SELECTED;
                    } else {
                        newState = AlgorithmInfo.State.NOT_SELECTED;
                    }

                    if (algorithmInfo.getFullName().equals("adg_carder")) {
                        debug("adg_carder TEST newState="+newState+" getState="+algorithmInfo.getState());
                    }

                    if (algorithmInfo.getState() != newState) {
                        debug("calling getState="+newState+" name="+algorithmInfo.getFullName());
                        algorithmInfo.setState(newState);
                        productInfoArrayChanged = true;
                    }
                } else {
                    for (BaseInfo wInfo : aInfo.getChildren()) {
                        WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;

                        if (newProdTreeSet.contains(wavelengthInfo.getFullName())) {
                            newState = WavelengthInfo.State.SELECTED;
                        } else {
                            newState = WavelengthInfo.State.NOT_SELECTED;
                        }
                        if (wavelengthInfo.getState() != newState) {
                            debug("calling getState="+newState+" name="+wavelengthInfo.getFullName());
                            wavelengthInfo.setState(newState);
                            productInfoArrayChanged = true;
                        }
                    }
                }
            }
        }

        if (productInfoArrayChanged) {
            debug(" productInfoArrayChanged");
            fireEvent(PRODUCT_CHANGED_EVENT);
        }
    }


    private void handleDefaultsProdKeyChange(String newProd) {

        //----------------------------------------------------------------------------------------------------
        // Put newProd into newProdTreeSet
        //----------------------------------------------------------------------------------------------------

        TreeSet<String> newProdTreeSet = new TreeSet<String>();

        debug("newDefaultProd=" + newProd);
        if (newProd != null) {
            for (String prodEntry : newProd.split(" ")) {
                prodEntry.trim();

                if (isValidL2prod(prodEntry)) {
                    newProdTreeSet.add(prodEntry);
                    debug("defaultProdEntry=" + prodEntry);
                }
            }
        }

        //----------------------------------------------------------------------------------------------------
        // For every product in ProductInfoArray set selected to agree with newProdTreeSet
        //----------------------------------------------------------------------------------------------------

        boolean defaultsChanged = false;
        boolean newDefaultSelected;

        for (ProductInfo productInfo : productInfoArray) {
            for (BaseInfo aInfo : productInfo.getChildren()) {
                AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;

                if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.NONE) {
                    if (newProdTreeSet.contains(algorithmInfo.getFullName())) {
                        newDefaultSelected = true;
                    } else {
                        newDefaultSelected = false;
                    }
                    if (algorithmInfo.isDefaultSelected() != newDefaultSelected) {
                        algorithmInfo.setDefaultSelected(newDefaultSelected);
                        defaultsChanged = true;
                    }
                } else {

                    for (BaseInfo wInfo : aInfo.getChildren()) {
                        WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;

                        if (newProdTreeSet.contains(wavelengthInfo.getFullName())) {
                            newDefaultSelected = true;
                        } else {
                            newDefaultSelected = false;
                        }
                        if (wavelengthInfo.isDefaultSelected() != newDefaultSelected) {
                            wavelengthInfo.setDefaultSelected(newDefaultSelected);
                            defaultsChanged = true;
                        }
                    }
                }
            }
        }

        if (defaultsChanged) {
            propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, DEFAULTS_CHANGED_EVENT, null, null));
        }
    }


    public String getMissionString() {

        String missionString = "";

        if (parfileHashMap.containsKey(IFILE) && parfileHashMap.get(IFILE) != null) {
            File file = new File(parfileHashMap.get(IFILE));

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

    // runs this if IFILE changes
    // it will reset missionString
    // it will reset and make new wavelengthInfoArray
    private void handleIfileChange(String newIfile) {

        String previousMissionString = getMissionString();
        parfileHashMap.put(IFILE, newIfile);

        setOfile();

        debug("new missionString=" + getMissionString());
        if (getMissionString() != null) {
            if (previousMissionString == null || !previousMissionString.equals(getMissionString())) {

                wavelengthLimiterArray.clear();

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
                            wavelengthLimiterArray.add(wavelengthInfo);
                            debug("wavelengthLimiterArray adding wave=" + wavelengthInfo.getWavelengthString());
                        }
                    }
                }

                debug("resetWavelengthInfosInProductInfoArray");
                resetWavelengthInfosInProductInfoArray();

                //    defaultsParfile = l2genReader.readFileIntoString(getL2genDefaults());


            }
        }

        setDefaultParfile(getL2genDefaults());
        applyParfileDefaults();

        debug(MISSION_CHANGE_EVENT.toString() + "being fired");
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, MISSION_CHANGE_EVENT, null, getMissionString()));


    }

    private void setOfile() {

        String ifile = parfileHashMap.get(IFILE);
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
        parfileHashMap.put(OFILE, ofile);

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

    public void resetWavelengthInfosInProductInfoArray() {
        for (ProductInfo productInfo : productInfoArray) {
            for (BaseInfo aInfo : productInfo.getChildren()) {
                AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;
                algorithmInfo.clearChildren();

                if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.NONE) {
                    //    WavelengthInfo newWavelengthInfo = new WavelengthInfo(null);
                    //    newWavelengthInfo.setParent(algorithmInfo);
                    //    algorithmInfo.addChild(newWavelengthInfo);
                } else {
                    for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {

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
                }
            }
        }


    }


    public boolean compareWavelengthLimiter(WavelengthInfo wavelengthInfo) {
        for (WavelengthInfo wavelengthLimitorInfo : getWavelengthLimiterArray()) {
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


