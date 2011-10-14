package gov.nasa.obpg.seadas.sandbox.l2gen;

import com.sun.org.apache.xml.internal.security.algorithms.Algorithm;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class L2genData {

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
    public final String PROD = "prod";

    public final String PARFILE_TEXT_CHANGE_EVENT_NAME = "parfileTextChangeEvent";
    public final String MISSION_STRING_CHANGE_EVENT_NAME = "missionStringChangeEvent";
    public final String UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT = "updateWavelengthCheckboxStatesEvent";
    public final String WAVE_DEPENDENT_PRODUCT_CHANGED = "waveDependentJListEvent";
    public final String WAVE_INDEPENDENT_PRODUCT_CHANGED = "waveIndependentJListEvent";


    // Groupings of Parameter Keys
    private final String[] coordinateParamKeys = {NORTH, SOUTH, WEST, EAST};
    private final String[] pixelLineParamKeys = {SPIXL, EPIXL, DPIXL, SLINE, ELINE, DLINE};
    private final String[] fileIOParamKeys = {IFILE, OFILE};
    private final String[] remainingGUIParamKeys = {};


    private String OCDATAROOT = System.getenv("OCDATAROOT");
    private HashMap<String, String> paramValueHashMap = new HashMap();
    private HashMap<String, String> defaultParamValueHashMap = new HashMap();
    private TreeSet<String> l2prodlist = new TreeSet<String>();
    private ArrayList<ProductInfo> waveIndependentProductInfoArray = new ArrayList<ProductInfo>();
    private ArrayList<ProductInfo> waveDependentProductInfoArray = new ArrayList<ProductInfo>();

    private ArrayList<WavelengthInfo> wavelengthInfoArray = new ArrayList<WavelengthInfo>();


    private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    private String missionString = "";

    public enum RegionType {Coordinates, PixelLines}


    public EventInfo[] eventInfos = {
            new EventInfo(WAVE_DEPENDENT_PRODUCT_CHANGED, this),
            new EventInfo(WAVE_INDEPENDENT_PRODUCT_CHANGED, this)
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
                System.out.println("Disabling event" + eventName);
                eventInfo.setEnabled(false);
            }
        }
    }

    public void enableEvent(String eventName) {
        for (EventInfo eventInfo : eventInfos) {
            if (eventName.equals(eventInfo.toString())) {

                System.out.println("Enabling event" + eventName);
                eventInfo.setEnabled(true);
            }
        }
    }

    private void fireEvent(String eventName) {
        fireEvent(eventName, null, null);
    }


    private void fireEvent(String eventName, Object oldValue, Object newValue) {
        for (EventInfo eventInfo : eventInfos) {
            if (eventName.equals(eventInfo.toString())) {
                System.out.println("Firing event" + eventName);
                eventInfo.fireEvent(oldValue, newValue);
            }
        }
    }


    public void setSelectedWaveDependentProduct(WavelengthInfo inWavelengthInfo, boolean isSelected) {
        for (ProductInfo productInfo : waveDependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                for (WavelengthInfo wavelengthInfo : algorithmInfo.getWavelengthInfoArray()) {
                    if (inWavelengthInfo == wavelengthInfo) {
                        if (wavelengthInfo.isSelected() != isSelected) {
                            wavelengthInfo.setSelected(isSelected);
                            fireEvent(WAVE_DEPENDENT_PRODUCT_CHANGED);
                            return;
                        }
                    }
                }
            }
        }
    }


    public void setSelectedWaveIndependentProduct(AlgorithmInfo inAlgorithmInfo, boolean isSelected) {

        for (ProductInfo productInfo : waveIndependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                if (inAlgorithmInfo == algorithmInfo) {
                    if (algorithmInfo.isSelected() != isSelected) {
                        algorithmInfo.setSelected(isSelected);
                        fireEvent(WAVE_INDEPENDENT_PRODUCT_CHANGED);
                    }
                }
            }
        }
    }


    public String getProdlist() {
        String prodlist = makeProdlist();

        System.out.println("prodlist=" + prodlist);
        return prodlist;
    }


    private String makeProdlist() {
        StringBuilder prodlist = new StringBuilder("");

        for (ProductInfo productInfo : waveIndependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                if (algorithmInfo.isSelected()) {
                    StringBuilder product = new StringBuilder();

                    product.append(productInfo.getName());

                    if (algorithmInfo.getName() != null) {
                        product.append("_");
                        product.append(algorithmInfo.getName());
                    }

                    if (prodlist.length() > 0) {
                        prodlist.append(" ");
                    }

                    prodlist.append(product);
                }
            }
        }

        for (ProductInfo productInfo : waveDependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                for (WavelengthInfo wavelengthInfo : algorithmInfo.getWavelengthInfoArray()) {
                    if (wavelengthInfo.isSelected()) {
                        StringBuilder product = new StringBuilder();

                        product.append(productInfo.getName());
                        product.append("_");
                        product.append(wavelengthInfo.getWavelengthString());

                        if (algorithmInfo.getName() != null) {
                            product.append("_");
                            product.append(algorithmInfo.getName());
                        }

                        if (prodlist.length() > 0) {
                            prodlist.append(" ");
                        }

                        prodlist.append(product.toString());
                    }
                }
            }
        }

        return prodlist.toString();
    }


    public void setIsSelectedWavelengthInfoArray(String wavelength, boolean isSelected) {

        for (WavelengthInfo wavelengthInfo : wavelengthInfoArray) {
            if (wavelength.equals(wavelengthInfo.getWavelengthString())) {
                System.out.println(wavelength + ":" + wavelengthInfo.isSelected() + ":" + isSelected);
                if (isSelected != wavelengthInfo.isSelected()) {
                    wavelengthInfo.setSelected(isSelected);
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT, null, null));
                }
            }
        }


    }


    public boolean isSelectedWavelengthTypeIii() {

        boolean infraredNotSelectedFound = false;

        for (WavelengthInfo wavelengthInfo : wavelengthInfoArray) {
            if (wavelengthInfo.isIR() && !wavelengthInfo.isSelected()) {
                infraredNotSelectedFound = true;
            }
        }

        if (infraredNotSelectedFound) {
            return false;
        } else {
            return true;
        }
    }


    public void setSelectedWavelengthTypeIii(boolean selectedWavelengthTypeIii) {

        for (WavelengthInfo wavelengthInfo : wavelengthInfoArray) {
            if (wavelengthInfo.isIR()) {
                wavelengthInfo.setSelected(selectedWavelengthTypeIii);
            }
        }

        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT, null, null));

    }

    public boolean isSelectedWavelengthTypeVvv() {
        boolean visibleNotSelectedFound = false;

        for (WavelengthInfo wavelengthInfo : wavelengthInfoArray) {
            if (wavelengthInfo.isVisible() && !wavelengthInfo.isSelected()) {
                visibleNotSelectedFound = true;
            }
        }

        if (visibleNotSelectedFound) {
            return false;
        } else {
            return true;
        }
    }

    public void setSelectedWavelengthTypeVvv(boolean selectedWavelengthTypeVvv) {

        for (WavelengthInfo wavelengthInfo : wavelengthInfoArray) {
            if (wavelengthInfo.isVisible()) {
                wavelengthInfo.setSelected(selectedWavelengthTypeVvv);
            }
        }

        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT, null, null));
    }


    public void addWaveIndependentProductInfoArray(ProductInfo productInfo) {
        waveIndependentProductInfoArray.add(productInfo);
    }

    public void addWaveDependentProductInfoArray(ProductInfo productInfo) {
        waveDependentProductInfoArray.add(productInfo);
    }

    public void clearWaveIndependentProductInfoArray() {
        waveIndependentProductInfoArray.clear();
    }

    public void clearWaveDependentProductInfoArray() {
        waveDependentProductInfoArray.clear();
    }

    public void sortWaveDependentProductInfoArray(Comparator<ProductInfo> comparator) {
        Collections.sort(waveDependentProductInfoArray, comparator);
    }

    public void sortWaveIndependentProductInfoArray(Comparator<ProductInfo> comparator) {
        Collections.sort(waveIndependentProductInfoArray, comparator);
    }

    public ArrayList<ProductInfo> getWaveIndependentProductInfoArray() {
        return waveIndependentProductInfoArray;
    }

    public ArrayList<ProductInfo> getWaveDependentProductInfoArray() {
        return waveDependentProductInfoArray;
    }


    public ArrayList<WavelengthInfo> getWavelengthInfoArray() {
        return wavelengthInfoArray;
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
                paramValueHashMap.remove(currKey);
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(

                        this, currKey, null, ""));

            }

        } else if (regionType == RegionType.PixelLines) {
            // Since PixelLines are being used purge Coordinate fields

            for (String currKey : coordinateParamKeys) {
                paramValueHashMap.remove(currKey);
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(

                        this, currKey, null, ""));

            }

        }
    }


    //    For any given paramValue assemble it formatted parfile 'name=value' entry
    //    Do not make an entry if it matches the default value

    private void makeParfileKeyValueEntry(StringBuilder stringBuilder, String currKey) {

        boolean makeEntry = false;

        if (paramValueHashMap.containsKey(currKey)) {
            if (defaultParamValueHashMap.containsKey(currKey)) {
                if (defaultParamValueHashMap.get(currKey) != paramValueHashMap.get(currKey)) {
                    makeEntry = true;
                }
            } else {
                makeEntry = true;
            }
        }

        if (makeEntry == true) {
            stringBuilder.append(currKey);
            stringBuilder.append("=");
            stringBuilder.append(paramValueHashMap.get(currKey));
            stringBuilder.append("\n");
        }
    }





    public String getParfile() {

        HashMap<String, Object> paramValueCopyHashMap = new HashMap();

        for (String currKey : paramValueHashMap.keySet()) {
            paramValueCopyHashMap.put(currKey, paramValueHashMap.get(currKey));
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


        StringBuilder productBlockStringBuilder = new StringBuilder("");

        paramValueHashMap.put(PROD, getProdlist());

        makeParfileKeyValueEntry(productBlockStringBuilder, PROD);
        paramValueHashMap.remove(PROD);



        if (productBlockStringBuilder.length() > 0) {
            parfileStringBuilder.append("# PRODUCT PARAM\n");
            parfileStringBuilder.append(productBlockStringBuilder.toString());
        }


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

  //      System.out.println("firing PARFILE_TEXT_CHANGE_EVENT_NAME with parfile:" + parfile);
  //      propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PARFILE_TEXT_CHANGE_EVENT_NAME, null, null));
    }


    public void parseParfile(String inParfile) {

        boolean somethingChanged = false;

        String parfileLines[] = inParfile.split("\n");

        for (String myLine : parfileLines) {

            System.out.println(myLine);
            // skip the comment lines in file
            if (!myLine.trim().startsWith("#")) {

                String splitLine[] = myLine.split("=");
                if (splitLine.length == 2) {

                    final String currKey = splitLine[0].toString().trim();
                    final String currValue = splitLine[1].toString().trim();

                    if (!paramValueHashMap.containsKey(currKey) ||
                            (paramValueHashMap.containsKey(currKey) &&
                                    !paramValueHashMap.get(currKey).equals(currValue))) {
                        paramValueHashMap.put(currKey, currValue);

                        if (currKey.equals(IFILE)) {
                            handleIfileKeyChange();
                        }

                        if (currKey.equals(PROD)) {
                            handleProdKeyChange();
                        }
                        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(
                                this, currKey, null, currValue));


                        somethingChanged = true;
                    }

                }
            }
        }

   //     if (somethingChanged = true) {
   //         updateParfile();
   //     }
    }


    public void applyDefaults() {
        for (String currKey : defaultParamValueHashMap.keySet()) {

            if (paramValueHashMap.containsKey(currKey) != true) {
                paramValueHashMap.put(currKey, defaultParamValueHashMap.get(currKey));
            }
        }
    }

    public String getDefaultParamValue(String paramKey) {
        return this.defaultParamValueHashMap.get(paramKey).toString();
    }

    public void setDefaultParamValue(String paramKey, String value) {
        this.defaultParamValueHashMap.put(paramKey.trim(), value.trim());
    }


    public String getParamValue(String paramKey) {
        if (paramValueHashMap.get(paramKey) != null) {
            return paramValueHashMap.get(paramKey);
        } else {
            return "";
        }
    }


    public void setParamValue(String inKey, String inValue) {

        inKey = inKey.trim();

        if (inValue == null) {
            inValue = "";
        } else {
            inValue = inValue.trim();
        }

        System.out.println("setParam " + inKey + "=" + inValue);

        String currentVal = paramValueHashMap.get(inKey);

        if (currentVal == null || !currentVal.equals(inValue)) {
            if (inValue.length() == 0) {
                paramValueHashMap.remove(inKey);
            } else {
                paramValueHashMap.put(inKey, inValue);
                specifyRegionType(inKey);
            }

            if (inKey.equals(IFILE)) {
                handleIfileKeyChange();
            }

            if (inKey.equals(PROD)) {
                handleProdKeyChange();
            }

            propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, inKey, null, null));
        }
    }


    public void handleIfileKeyChange() {
        resetWavelengthInfoArray();
    }


    private WavelengthInfo getWavelengthFromL2prod(String L2prodEntry) {
        for (ProductInfo currProductInfo : waveDependentProductInfoArray) {
            for (AlgorithmInfo currAlgorithmInfo : currProductInfo.getAlgorithmInfoArrayList()) {

                String algorithm = currAlgorithmInfo.getName();
                String product = currAlgorithmInfo.getProductName();

                for (WavelengthInfo wavelengthInfo : wavelengthInfoArray) {

                    String wavelength = wavelengthInfo.toString();


                    if (L2prodEntry.equals(assembleL2productName(product, wavelength, algorithm))) {
                        return wavelengthInfo;
                    }
                }
            }
        }

        return null;
    }


    private AlgorithmInfo getAlgorithmInfoFromL2prod(String L2prodEntry) {

        for (ProductInfo currProductInfo : waveIndependentProductInfoArray) {
            for (AlgorithmInfo currAlgorithmInfo : currProductInfo.getAlgorithmInfoArrayList()) {

                String algorithm = currAlgorithmInfo.getName();
                String product = currAlgorithmInfo.getProductName();

                if (L2prodEntry.equals(assembleL2productName(product, null, algorithm))) {
                    return currAlgorithmInfo;
                }
            }
        }


        for (ProductInfo currProductInfo : waveDependentProductInfoArray) {
            for (AlgorithmInfo currAlgorithmInfo : currProductInfo.getAlgorithmInfoArrayList()) {

                String algorithm = currAlgorithmInfo.getName();
                String product = currAlgorithmInfo.getProductName();

                for (WavelengthInfo wavelengthInfo : wavelengthInfoArray) {

                    String wavelength = wavelengthInfo.toString();

                    if (L2prodEntry.equals(assembleL2productName(product, wavelength, algorithm))) {
                        return currAlgorithmInfo;
                    }
                }
            }
        }

        return null;
    }


    private boolean isValidL2prod(String L2prodEntry) {

        for (ProductInfo currProductInfo : waveIndependentProductInfoArray) {
            for (AlgorithmInfo currAlgorithmInfo : currProductInfo.getAlgorithmInfoArrayList()) {

                String algorithm = currAlgorithmInfo.getName();
                String product = currAlgorithmInfo.getProductName();

                if (L2prodEntry.equals(assembleL2productName(product, null, algorithm))) {
                    return true;
                }
            }
        }


        for (ProductInfo currProductInfo : waveDependentProductInfoArray) {
            for (AlgorithmInfo currAlgorithmInfo : currProductInfo.getAlgorithmInfoArrayList()) {

                String algorithm = currAlgorithmInfo.getName();
                String product = currAlgorithmInfo.getProductName();

                for (WavelengthInfo wavelengthInfo : wavelengthInfoArray) {

                    String wavelength = wavelengthInfo.toString();

                    if (L2prodEntry.equals(assembleL2productName(product, wavelength, algorithm))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    private String assembleL2productName(String product, String wavelength, String algorithm) {

        StringBuilder l2prod = new StringBuilder();

        l2prod.append(product);

        if (wavelength != null) {
            l2prod.append("_");
            l2prod.append(wavelength);
        }

        if (algorithm != null) {
            l2prod.append("_");
            l2prod.append(algorithm);
        }

        return l2prod.toString();
    }


    private void setProdWithProdlist() {

        StringBuilder newProdList = new StringBuilder();

        for (String prodEntry : l2prodlist) {
            newProdList.append(prodEntry);
            newProdList.append(" ");
        }
        paramValueHashMap.put(PROD, newProdList.toString().trim());
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

    private String getProductNameForSingleEntry(ProductInfo productInfo, AlgorithmInfo algorithmInfo) {
        StringBuilder l2genProductName = new StringBuilder();

        l2genProductName.append(productInfo.getName());

        if (algorithmInfo.getName() != null) {
            l2genProductName.append("_");
            l2genProductName.append(algorithmInfo.getName());
        }
        return l2genProductName.toString();
    }


    public void handleProdKeyChange() {

        if (!paramValueHashMap.containsKey(PROD)) {
             paramValueHashMap.put(PROD, "");
        }

        String prodEntries[]= paramValueHashMap.get(PROD).split(" ");

        TreeSet<String> l2prodlistNew = new TreeSet<String>();

        for (String prodEntry : prodEntries) {
            prodEntry.trim();

            if (isValidL2prod(prodEntry)) {
                l2prodlistNew.add(prodEntry);
            }
        }


        if (l2prodlistNew.size() > 0) {
            boolean waveDependentProductInfoArrayChanged = false;

            for (ProductInfo productInfo : waveDependentProductInfoArray) {
                for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                    for (WavelengthInfo wavelengthInfo : algorithmInfo.getWavelengthInfoArray()) {
                        boolean currL2genDataEntryVerified = false;

                        String currL2genDataProductName = getProductNameForSingleEntry(productInfo, algorithmInfo, wavelengthInfo);

                        // if current l2genProduct is in prodlist then make sure it's selection state is true
                        // and then remove it from the prodlist
                        for (String l2prodEntry : l2prodlistNew) {
                            if (currL2genDataProductName.equals(l2prodEntry)) {
                                if (!wavelengthInfo.isSelected()) {
                                    wavelengthInfo.setSelected(true);
                                    waveDependentProductInfoArrayChanged = true;
                                }

                                l2prodlistNew.remove(l2prodEntry);
                                currL2genDataEntryVerified = true;
                                break;
                            }
                        }

                        // if current l2genProduct is NOT in prodlist then make sure it's selection state is false
                        // and then remove it from the prodlist
                        if (!currL2genDataEntryVerified) {
                            if (wavelengthInfo.isSelected()) {
                                wavelengthInfo.setSelected(false);
                                waveDependentProductInfoArrayChanged = true;
                            }
                        }
                    }
                }
            }

            boolean waveIndependentProductInfoArrayChanged = false;

            for (ProductInfo productInfo : waveIndependentProductInfoArray) {
                for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {

                    boolean currL2genDataEntryVerified = false;

                    String currL2genDataProductName = getProductNameForSingleEntry(productInfo, algorithmInfo);

                    // if current l2genProduct is in prodlist then make sure it's selection state is true
                    // and then remove it from the prodlist
                    for (String l2prodEntry : l2prodlistNew) {
                        if (currL2genDataProductName.equals(l2prodEntry)) {
                            if (!algorithmInfo.isSelected()) {
                                algorithmInfo.setSelected(true);
                                waveIndependentProductInfoArrayChanged = true;
                            }

                            l2prodlistNew.remove(l2prodEntry);
                            currL2genDataEntryVerified = true;
                            break;
                        }
                    }

                    // if current l2genProduct is NOT in prodlist then make sure it's selection state is false
                    // and then remove it from the prodlist
                    if (!currL2genDataEntryVerified) {
                        if (algorithmInfo.isSelected()) {
                            algorithmInfo.setSelected(false);
                            waveIndependentProductInfoArrayChanged = true;
                        }
                    }
                }
            }

            if (waveDependentProductInfoArrayChanged) {
                fireEvent(WAVE_DEPENDENT_PRODUCT_CHANGED);
                System.out.println("waveDependentProductInfoArrayChanged");
            }

            if (waveIndependentProductInfoArrayChanged) {
                fireEvent(WAVE_INDEPENDENT_PRODUCT_CHANGED);
            }

        }

        paramValueHashMap.remove(PROD);
        // setProdWithProdlist();

        //     setIsSelectedWavelengthInfoArrayWithProdHash();
        //  setIsSelectedWaveDependentProductInfoArrayWithProdHash();
        //  setIsSelectedWaveIndependentProductInfoArrayWithProdHash();


        //   System.out.println("UPDATE_WAVELENGTH_CHECKBOX_SELECTION_STATES_EVENT to be fired");
        //    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT, null, null));
        //    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, UPDATE_WAVE_DEPENDENT_JLIST_EVENT, null, null));
        //    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, UPDATE_WAVE_INDEPENDENT_JLIST_EVENT, null, null));
    }


    public void setParfile(String inParfile) {

      //  if (!parfile.equals(inParfile)) {
            parseParfile(inParfile);
      //  }
    }

    public String getMissionString() {
        return missionString;
    }

    public void setMissionString(String missionString) {
        if (!missionString.equals(this.missionString)) {
            String oldValue = this.missionString;
            this.missionString = missionString;
            propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, MISSION_STRING_CHANGE_EVENT_NAME, oldValue, missionString));
        }
    }


    // runs this if IFILE changes
    // it will reset missionString
    // it will reset and make new wavelengthInfoArray
    // it will reset Selected values in wavelengthInfoArray based on prodHash settings
    private void resetWavelengthInfoArray() {

        String newMissionString = paramValueHashMap.get(IFILE).substring(0, 1);

        if (!newMissionString.equals(missionString)) {

            missionString = newMissionString;
            wavelengthInfoArray.clear();


            // lookup hash relating mission letter with mission directory name
            final HashMap myMissionLetterHashMap = new HashMap();
            myMissionLetterHashMap.put("S", "seawifs");
            myMissionLetterHashMap.put("A", "modisa");
            myMissionLetterHashMap.put("T", "modist");


            //           setMissionString(missionLetter);
            String missionDirectoryName = (String) myMissionLetterHashMap.get(missionString);
            System.out.println("missionString=" + missionString);

            // determine the filename which contains the wavelengths
            final StringBuilder myFilename = new StringBuilder("");
            myFilename.append(OCDATAROOT);
            myFilename.append("/");
            myFilename.append(missionDirectoryName);
            myFilename.append("/");
            myFilename.append("msl12_sensor_info.dat");

            // read in the mission's datafile which contains the wavelengths
            final ArrayList<String> myAsciiFileArrayList = myReadDataFile(myFilename.toString());


            // loop through datafile
            for (String myLine : myAsciiFileArrayList) {

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

                        System.out.println("currWavelength=" + currWavelength);
                        WavelengthInfo wavelengthInfo = new WavelengthInfo(currWavelength);
                        wavelengthInfoArray.add(wavelengthInfo);


                    }  //end if on value pairs of the form Lambda(#) = #

                }  // end if skipping comments lines

            }  // end for (String myLine : myAsciiFileArrayList)

//                        WavelengthInfo irWavelengthInfo = new WavelengthInfo(null, "iii");
//                        wavelengthInfoArray.add(irWavelengthInfo);
//
//                                    WavelengthInfo visibleWavelengthInfo = new WavelengthInfo(null, "vvv");
//                        wavelengthInfoArray.add(visibleWavelengthInfo);
//
            //      setIsSelectedWavelengthInfoArrayWithProdHash();

            for (WavelengthInfo wavelengthInfo : wavelengthInfoArray) {
                System.out.println("wave=" + wavelengthInfo.getWavelength());
            }

            resetAlgorithmInfoWavelengthInfo();
            propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, MISSION_STRING_CHANGE_EVENT_NAME, null, missionString));
        }
    }


    public void resetAlgorithmInfoWavelengthInfo() {
        for (ProductInfo productInfo : waveDependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                algorithmInfo.clearWavelengthInfoArray();

                for (WavelengthInfo wavelengthInfo : wavelengthInfoArray) {

                    if (wavelengthInfo.getWavelength() < WavelengthInfo.VISIBLE_UPPER_LIMIT) {
                        if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.VISIBLE ||
                                algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
                            WavelengthInfo newWavelengthInfo = new WavelengthInfo(wavelengthInfo.getWavelength());
                            newWavelengthInfo.setAlgorithmInfo(algorithmInfo);
                            algorithmInfo.addWavelengthInfoArray(newWavelengthInfo);
                        }
                    } else {
                        if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.IR ||
                                algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
                            WavelengthInfo newWavelengthInfo = new WavelengthInfo(wavelengthInfo.getWavelength());
                            newWavelengthInfo.setAlgorithmInfo(algorithmInfo);
                            algorithmInfo.addWavelengthInfoArray(newWavelengthInfo);
                        }
                    }
                }
            }
        }
    }


    // apply all entries in l2prodHash to wavelengthInfoArray
    private void setIsSelectedWavelengthInfoArrayWithProdHash() {


        for (WavelengthInfo wavelengthInfo : wavelengthInfoArray) {
            wavelengthInfo.setSelected(false);
        }


        for (String l2prodEntry : l2prodlist) {

            WavelengthInfo wavelengthInfo = getWavelengthFromL2prod(l2prodEntry);

            if (wavelengthInfo != null) {

                for (WavelengthInfo currwavelengthInfo : wavelengthInfoArray) {
                    if (currwavelengthInfo.getWavelength() == wavelengthInfo.getWavelength()) {
                        wavelengthInfo.setSelected(true);
                    }
                }
            }
        }
    }


    private void setIsSelectedWaveIndependentProductInfoArrayWithProdHash() {


        for (ProductInfo productInfo : waveIndependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                algorithmInfo.setSelected(false);
            }
        }


        for (String l2prodEntry : l2prodlist) {
            AlgorithmInfo algorithmInfo = getAlgorithmInfoFromL2prod(l2prodEntry);
            if (algorithmInfo != null) {
                algorithmInfo.setSelected(true);
            } else {
                // todo do something with this like send to log file or something
            }
        }
    }


    private void setIsSelectedWaveDependentProductInfoArrayWithProdHash() {


        for (ProductInfo productInfo : waveDependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                algorithmInfo.setSelected(false);
            }
        }


        for (String l2prodEntry : l2prodlist) {
            AlgorithmInfo algorithmInfo = getAlgorithmInfoFromL2prod(l2prodEntry);
            if (algorithmInfo != null) {
                algorithmInfo.setSelected(true);
            } else {
                // todo do something with this like send to log file or something
            }
        }
    }


    private ArrayList<String> myReadDataFile(String fileName) {
        String lineData;
        ArrayList<String> fileContents = new ArrayList<String>();
        BufferedReader moFile = null;
        try {
            moFile = new BufferedReader(new FileReader(new File(fileName)));
            while ((lineData = moFile.readLine()) != null) {

                fileContents.add(lineData);
            }
        } catch (IOException e) {
            ;
        } finally {
            try {
                moFile.close();
            } catch (Exception e) {
                //Ignore
            }
        }
        return fileContents;
    }


}


