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
    private final String[] remainingGUIParamKeys = {};

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
                debug("Disabled event " + eventName + " current enabled count = " + eventInfo.getEnabledCount());
            }
        }
    }

    public void enableEvent(String eventName) {
        for (EventInfo eventInfo : eventInfos) {
            if (eventName.equals(eventInfo.toString())) {
                eventInfo.setEnabled(true);
                debug("Enabled event " + eventName + " current enabled count = " + eventInfo.getEnabledCount());
            }
        }
    }

    private void fireEvent(String eventName) {
        fireEvent(eventName, null, null);
    }


    private void fireEvent(String eventName, Object oldValue, Object newValue) {
        for (EventInfo eventInfo : eventInfos) {
            if (eventName.equals(eventInfo.toString())) {
                eventInfo.fireEvent(oldValue, newValue);
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


    public void setSelectedProduct(ProductInfo productInfo, BaseInfo.State state) {

        debug("setSelectedProduct called with state = " + state);

        if (productInfo.getState() == state)
            return;

        disableEvent(PRODUCT_CHANGED_EVENT);

        productInfo.setState(state);

        ignoreProductStateCheck = true;

        for (BaseInfo aInfo : productInfo.getChildren()) {
            setSelectedAlgorithm((AlgorithmInfo) aInfo, state);
        }

        ignoreProductStateCheck = false;

        checkProductState(productInfo);

        fireEvent(PRODUCT_CHANGED_EVENT);
        enableEvent(PRODUCT_CHANGED_EVENT);
    }


    public void setSelectedAlgorithm(AlgorithmInfo algorithmInfo, BaseInfo.State state) {

        debug("setSelectedAlgorithm called with state =" + state);

        if (algorithmInfo.getState() == state)
            return;

        disableEvent(PRODUCT_CHANGED_EVENT);

        if (algorithmInfo.hasChildren()) {

            algorithmInfo.setState(state);

            ignoreAlgorithmStateCheck = true;

            for (BaseInfo wInfo : algorithmInfo.getChildren()) {
                if (state == BaseInfo.State.PARTIAL) {
                    for (WavelengthInfo wavelengthLimitorInfo : wavelengthLimiterArray) {
                        if (wavelengthLimitorInfo.getWavelength() == ((WavelengthInfo) wInfo).getWavelength()) {
                            setSelectedWavelength((WavelengthInfo) wInfo, wavelengthLimitorInfo.getState());
                        }
                    }
                } else {
                    setSelectedWavelength((WavelengthInfo) wInfo, state);
                }
            }

            ignoreAlgorithmStateCheck = false;

        } else {
            if (state == BaseInfo.State.PARTIAL) {
                algorithmInfo.setState(BaseInfo.State.SELECTED);
            } else {
                algorithmInfo.setState(state);
            }
        }

        checkAlgorithmState(algorithmInfo);

        fireEvent(PRODUCT_CHANGED_EVENT);
        enableEvent(PRODUCT_CHANGED_EVENT);
    }


    public void setSelectedWavelength(WavelengthInfo wavelengthInfo, BaseInfo.State state) {

        debug("setSelectedWavelength called with state =" + state);

        if (state == BaseInfo.State.PARTIAL) {
            state = BaseInfo.State.SELECTED;
        }

        if (wavelengthInfo.getState() != state) {
            disableEvent(PRODUCT_CHANGED_EVENT);

            wavelengthInfo.setState(state);

            checkAlgorithmState(wavelengthInfo.getAlgorithmInfo());

            fireEvent(PRODUCT_CHANGED_EVENT);
            enableEvent(PRODUCT_CHANGED_EVENT);
        }
    }


    public void setSelectedInfo(BaseInfo info, BaseInfo.State state) {
        if (info instanceof ProductInfo) {
            setSelectedProduct((ProductInfo) info, state);
        } else if (info instanceof AlgorithmInfo) {
            setSelectedAlgorithm((AlgorithmInfo) info, state);
        } else if (info instanceof WavelengthInfo) {
            setSelectedWavelength((WavelengthInfo) info, state);
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
                    selectedProducts.add(aInfo);
                }
            }
        }

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

        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
            if (wavelengthInfo.isIR()) {
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


    // DANNY IS REVIEWING CODE AND LEFT OFF HERE

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
                parfileHashMap.remove(currKey);
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(

                        this, currKey, null, ""));

            }

        } else if (regionType == RegionType.PixelLines) {
            // Since PixelLines are being used purge Coordinate fields

            for (String currKey : coordinateParamKeys) {
                parfileHashMap.remove(currKey);
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(

                        this, currKey, null, ""));

            }

        }
    }


    //    For any given paramValue assemble it formatted parfile 'name=value' entry
    //    Do not make an entry if it matches the default value

    private void makeParfileKeyValueEntry(StringBuilder stringBuilder, String currKey) {

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
            stringBuilder.append(currKey);
            stringBuilder.append("=");
            stringBuilder.append(parfileHashMap.get(currKey));
            stringBuilder.append("\n");
        }
    }


    private String makeRequiredParfileKeyValueEntry(String name, String value) {

        StringBuilder stringBuilder = new StringBuilder("");


        stringBuilder.append(name);
        stringBuilder.append("=");
        stringBuilder.append(value);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }


    public String getParfile() {

        // Remove prod entry because we do not store it here.
        // This is just a precaution it should never have been stored.
        if (parfileHashMap.containsKey(PROD)) {
            parfileHashMap.remove(PROD);
        }

        HashMap<String, Object> paramValueCopyHashMap = new HashMap();

        for (String currKey : parfileHashMap.keySet()) {
            paramValueCopyHashMap.put(currKey, parfileHashMap.get(currKey));
        }

        StringBuilder parfileStringBuilder = new StringBuilder("");


        StringBuilder fileIOBlockStringBuilder = new StringBuilder("");

        for (String currKey : fileIOParamKeys) {
            makeParfileKeyValueEntry(fileIOBlockStringBuilder, currKey);
            paramValueCopyHashMap.remove(currKey);
        }

        if (fileIOBlockStringBuilder.length() > 0) {
            parfileStringBuilder.append("# FILE IO PARAMS\n");
            parfileStringBuilder.append(fileIOBlockStringBuilder.toString());
        }


        String productEntry = makeRequiredParfileKeyValueEntry(PROD, getProd());
        parfileStringBuilder.append("# PRODUCT PARAM\n");
        parfileStringBuilder.append(productEntry);


        StringBuilder coordinateBlockStringBuilder = new StringBuilder("");

        for (String currKey : coordinateParamKeys) {
            makeParfileKeyValueEntry(coordinateBlockStringBuilder, currKey);

            paramValueCopyHashMap.remove(currKey);
        }


        for (String currKey : pixelLineParamKeys) {
            makeParfileKeyValueEntry(coordinateBlockStringBuilder, currKey);
            paramValueCopyHashMap.remove(currKey);
        }

        if (coordinateBlockStringBuilder.length() > 0) {
            parfileStringBuilder.append("# COORDINATE PARAMS\n");
            parfileStringBuilder.append(coordinateBlockStringBuilder.toString());
        }


        StringBuilder userBlockStringBuilder = new StringBuilder("");

        for (String currKey : paramValueCopyHashMap.keySet()) {
            makeParfileKeyValueEntry(userBlockStringBuilder, currKey);
        }

        if (userBlockStringBuilder.length() > 0) {
            parfileStringBuilder.append("# ADDITIONAL USER PARAMS\n");
            parfileStringBuilder.append(userBlockStringBuilder.toString());
        }

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

        boolean productInfoChanged = false;

        for (ProductInfo productInfo : productInfoArray) {
            for (BaseInfo algorithmInfo : productInfo.getChildren()) {
                for (BaseInfo wavelengthInfo : algorithmInfo.getChildren()) {
                    if (wavelengthInfo.isSelected() != ((WavelengthInfo) wavelengthInfo).isDefaultSelected()) {
                        wavelengthInfo.setSelected(((WavelengthInfo) wavelengthInfo).isDefaultSelected());
                        productInfoChanged = true;
                    }
                }
            }
        }


        if (productInfoChanged == true) {
            fireEvent(PRODUCT_CHANGED_EVENT);
        }
    }

    public void applyParfileDefaults() {

        HashMap<String, String> copyParamValueHashMap = new HashMap<String, String>();

        for (String key : parfileHashMap.keySet()) {
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

        if (inKey != null && inKey.length() > 0) {
            inKey = inKey.trim();

            if (inKey.equals(PROD)) {
                handleProdKeyChange(inValue, false);
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
                debug("PROD=" + inValue);
                handleProdKeyChange(inValue, true);
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


    private String getProductNameForSingleEntry(ProductInfo productInfo, AlgorithmInfo algorithmInfo, WavelengthInfo wavelengthInfo) {
        StringBuilder l2genProductName = new StringBuilder();

        l2genProductName.append(productInfo.getName());
        l2genProductName.append("_");
        l2genProductName.append(wavelengthInfo.getWavelengthString());

        if (algorithmInfo.getName() != null) {
            l2genProductName.append("_");
            l2genProductName.append(algorithmInfo.getName());
        }
        return l2genProductName.toString();
    }


    private void handleProdKeyChange(String newProd, boolean setDefaults) {

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

        boolean productInfoArrayChanged = false;
        boolean defaultsChanged = false;
        //----------------------------------------------------------------------------------------------------
        // For every product in ProductInfoArray set selected to agree with newProdTreeSet
        //----------------------------------------------------------------------------------------------------


        for (ProductInfo productInfo : productInfoArray) {
            for (BaseInfo algorithmInfo : productInfo.getChildren()) {
                for (BaseInfo wInfo : algorithmInfo.getChildren()) {
                    WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;
                    String currL2genDataProductName = getProductNameForSingleEntry(productInfo, (AlgorithmInfo) algorithmInfo, wavelengthInfo);

                    if (newProdTreeSet.contains(currL2genDataProductName)) {
                        debug("TEMP " + wavelengthInfo.toString());
                        if (setDefaults == true) {
                            if (!wavelengthInfo.isDefaultSelected()) {
                                wavelengthInfo.setDefaultSelected(true);
                                debug("setting default " + wavelengthInfo.toString());
                                defaultsChanged = true;
                            }
                        } else {
                            if (!wavelengthInfo.isSelected()) {
                                wavelengthInfo.setSelected(true);
                                debug("setting regular " + wavelengthInfo.toString());

                                productInfoArrayChanged = true;
                            }
                        }

                        newProdTreeSet.remove(currL2genDataProductName);
                    } else {
                        if (setDefaults == true) {
                            if (wavelengthInfo.isDefaultSelected()) {
                                wavelengthInfo.setDefaultSelected(false);
                                debug("setting default " + wavelengthInfo.toString());
                                defaultsChanged = true;
                            }
                        } else {
                            if (wavelengthInfo.isSelected()) {
                                wavelengthInfo.setSelected(false);
                                productInfoArrayChanged = true;
                            }
                        }
                    }
                }
            }
        }


        debug("prod=" + getProd());
        if (setDefaults != true) {
            if (productInfoArrayChanged) {
                debug(" productInfoArrayChanged");
                fireEvent(PRODUCT_CHANGED_EVENT);
            }
        } else {
            if (defaultsChanged) {
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, DEFAULTS_CHANGED_EVENT, null, null));
            }
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

                setDefaultParfile(getL2genDefaults());
                applyParfileDefaults();

                debug(MISSION_CHANGE_EVENT.toString() + "being fired");
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, MISSION_CHANGE_EVENT, null, getMissionString()));
            }
        }
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
                    WavelengthInfo newWavelengthInfo = new WavelengthInfo(null);
                    newWavelengthInfo.setParent(algorithmInfo);
                    algorithmInfo.addChild(newWavelengthInfo);
                } else {
                    for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {

                        if (wavelengthInfo.getWavelength() < WavelengthInfo.VISIBLE_UPPER_LIMIT) {
                            if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.VISIBLE ||
                                    algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
                                WavelengthInfo newWavelengthInfo = new WavelengthInfo(wavelengthInfo.getWavelength());
                                newWavelengthInfo.setParent(algorithmInfo);
                                algorithmInfo.addChild(newWavelengthInfo);
                            }
                        } else {
                            if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.IR ||
                                    algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
                                WavelengthInfo newWavelengthInfo = new WavelengthInfo(wavelengthInfo.getWavelength());
                                newWavelengthInfo.setParent(algorithmInfo);
                                algorithmInfo.addChild(newWavelengthInfo);
                            }
                        }
                    }
                }
            }
        }


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


