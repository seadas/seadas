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

    public boolean retainCurrentIfile = true;
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
    public final String SLINE = "sline";
    public final String ELINE = "eline";
    public final String NORTH = "north";
    public final String SOUTH = "south";
    public final String WEST = "west";
    public final String EAST = "east";


    public final String IFILE = "ifile";
    public final String OFILE = "ofile";
    public static final String L2PROD = "l2prod";

    public final String INVALID_IFILE_EVENT = "INVALID_IFILE_EVENT";
    public final String WAVE_LIMITER_CHANGE_EVENT = "WAVE_LIMITER_CHANGE_EVENT";
    public final String RETAIN_IFILE_CHANGE_EVENT = "RETAIN_IFILE_CHANGE_EVENT";


    private L2genReader l2genReader = new L2genReader(this);

    private L2prodParamInfo l2prodParamInfo;  // shortcut path to the one contained in paramInfo
    private ArrayList<ParamInfo> paramInfos = new ArrayList<ParamInfo>();
    private ArrayList<ParamCategoryInfo> paramCategoryInfos = new ArrayList<ParamCategoryInfo>();


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

    public boolean isRetainCurrentIfile() {
        return retainCurrentIfile;
    }

    public void setRetainCurrentIfile(boolean retainCurrentIfile) {

        this.retainCurrentIfile = retainCurrentIfile;
        fireEvent(RETAIN_IFILE_CHANGE_EVENT);
    }


    public enum RegionType {Coordinates, PixelLines}

    public EventInfo[] eventInfos = {
            new EventInfo(L2PROD, this),
    };

    public L2genData() {

    }

    private EventInfo getEventInfo(String name) {
        for (EventInfo eventInfo : eventInfos) {
            if (name.equals(eventInfo.getName())) {
                return eventInfo;
            }
        }
        return null;
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        EventInfo eventInfo = getEventInfo(propertyName);
        if (eventInfo == null) {
            propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
        } else {
            eventInfo.addPropertyChangeListener(listener);
        }
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        EventInfo eventInfo = getEventInfo(propertyName);
        if (eventInfo == null) {
            propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        } else {
            eventInfo.removePropertyChangeListener(listener);
        }
    }


    public void disableEvent(String name) {
        EventInfo eventInfo = getEventInfo(name);
        if (eventInfo == null) {
            debug("disableEvent - eventInfo not found for " + name);
        } else {
            eventInfo.setEnabled(false);
        }
    }

    public void enableEvent(String name) {
        EventInfo eventInfo = getEventInfo(name);
        if (eventInfo == null) {
            debug("enableEvent - eventInfo not found for " + name);
        } else {
            eventInfo.setEnabled(true);
        }
    }

    public void fireEvent(String name) {
        fireEvent(name, null, null);
    }

    public void fireEvent(String name, Object oldValue, Object newValue) {
        EventInfo eventInfo = getEventInfo(name);
        if (eventInfo == null) {
            propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, name, oldValue, newValue));
        } else {
            eventInfo.fireEvent(oldValue, newValue);
        }
    }

    public void fireAllParamEvents() {
        for (ParamInfo paramInfo : paramInfos) {
            if (paramInfo.getName() != null) {
                fireEvent(paramInfo.getName());
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
                    fireEvent(WAVE_LIMITER_CHANGE_EVENT);
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
        fireEvent(WAVE_LIMITER_CHANGE_EVENT);
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


    public void clearParamInfos() {
        paramInfos.clear();
    }


    public void sortParamCategoryInfos() {
        Collections.sort(paramCategoryInfos);
    }


    public void sortParamInfos() {
        Collections.sort(paramInfos);
    }


    public ArrayList<WavelengthInfo> getWaveLimiter() {
        return waveLimiter;
    }


    /**
     * Handle cases where a change in one name should effect a change in name
     * <p/>
     * In this case specifically coordParams and pixelParams are mutually exclusive
     * so if a name in one group is being set to a non-default value, then set all
     * params in the other group to the defaults
     *
     * @param name
     */
    private void setConflictingParams(String name) {

        ParamInfo paramInfo = getParamInfo(name);
        if (paramInfo == null) {
            return;
        }

        // Only proceed if name is not equal to default
        if (paramInfo.getValue() == paramInfo.getDefaultValue()) {
            return;
        }

        // Set all params in the other group to the defaults
        final HashSet<String> coords = new HashSet<String>();
        coords.add(NORTH);
        coords.add(SOUTH);
        coords.add(EAST);
        coords.add(WEST);

        final HashSet<String> pixels = new HashSet<String>();
        pixels.add(SPIXL);
        pixels.add(EPIXL);
        pixels.add(SLINE);
        pixels.add(ELINE);

        // Test if name is coordParam
        if (coords.contains(name)) {
            for (String pixelParam : pixels) {
                setParamToDefaults(getParamInfo(pixelParam));
            }
        }

        // Set all pixelParams in paramInfos to defaults
        if (pixels.contains(name)) {
            for (String coordParam : coords) {
                setParamToDefaults(getParamInfo(coordParam));
            }
        }
    }


    public String getParString() {

        StringBuilder par = new StringBuilder("");

        for (ParamCategoryInfo paramCategoryInfo : paramCategoryInfos) {
            StringBuilder currCategoryEntries = new StringBuilder("");

            for (ParamInfo paramInfo : paramCategoryInfo.getParamInfos()) {
                if (paramInfo.getName().equals(IFILE)) {
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


// DANNY IS REVIEWING CODE AND LEFT OFF HERE


    public void setParString(String parString, boolean ignoreIfile) {

        ArrayList<ParamInfo> parfileParamInfos = parseParfile(parString);

        /*
        Handle IFILE first
         */
        if (!ignoreIfile) {
            for (ParamInfo parfileParamInfo : parfileParamInfos) {
                if (parfileParamInfo.getName().toLowerCase().equals(IFILE)) {
                    setParamValue(IFILE, parfileParamInfo.getValue());
                    break;
                }
            }
        }

        boolean ofileSet = false;
        boolean geofileSet = false;
        /*
        Set all params contained in parString
        Ignore IFILE (handled earlier) and PAR (which is todo)
         */
        for (ParamInfo newParamInfo : parfileParamInfos) {


            if (newParamInfo.getName().toLowerCase().equals(OFILE) && ignoreIfile) {
                continue;
            }

            if (newParamInfo.getName().toLowerCase().equals(GEOFILE) && ignoreIfile) {
                continue;
            }

            if (newParamInfo.getName().toLowerCase().equals(IFILE)) {
                continue;
            }

            if (newParamInfo.getName().toLowerCase().equals(PAR)) {
                continue;
            }

            if (newParamInfo.getName().toLowerCase().equals(L2PROD)) {

                newParamInfo.setValue(sortStringList(newParamInfo.getValue()));
            }

            setParamValue(newParamInfo.getName(), newParamInfo.getValue());

            if (newParamInfo.getName().toLowerCase().equals(GEOFILE)) {
                geofileSet = true;
            }

            if (newParamInfo.getName().toLowerCase().equals(OFILE)) {
                ofileSet = true;
            }
        }

        if (!ofileSet) {
            setCustomOfile();
        }

        if (!geofileSet) {
            setCustomGeofile();
        }

        /*
        Delete all params NOT contained in parString to defaults (basically set to default)
        Except: L2PROD and IFILE  remain at current value
         */
        for (ParamInfo paramInfo : paramInfos) {
            if (!paramInfo.getName().equals(L2PROD) && !paramInfo.getName().equals(IFILE) && !paramInfo.getName().equals(OFILE) && !paramInfo.getName().equals(GEOFILE)) {
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


    public String getParamValue(ParamInfo paramInfo) {
        return paramInfo.getValue();
    }

    public String getParamValue(String param) {

        if (param == null) {
            return "";
        }

        ParamInfo paramInfo = getParamInfo(param);
        if (paramInfo != null) {
            return paramInfo.getValue();
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

        setParamValue(getParamInfo(param), value);
    }


    private ParamInfo getParamInfo(String name) {
        // todo to increase speed load paramInfos into a HashMap (whose key is lower case) for lookup

        if (name == null) {
            return null;
        }
        name = name.trim().toLowerCase();

        for (ParamInfo paramInfo : paramInfos) {
            if (paramInfo.getName().toLowerCase().equals(name)) {
                return paramInfo;
            }
        }

        return null;
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
            if (paramInfo.getName().toLowerCase().equals(IFILE)) {
                setIfileParamValue(paramInfo, value);
            } else {
                if (value.length() > 0) {
                    paramInfo.setValue(value);
                    setConflictingParams(paramInfo.getName());
                } else {
                    paramInfo.setValue(paramInfo.getDefaultValue());
                }
                fireEvent(paramInfo.getName());
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

    public void setParamDefault(String param) {
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


    // runs this if IFILE changes
    // it will reset missionString
    // it will reset and make new wavelengthInfoArray
    private void setIfileParamValue(ParamInfo paramInfo, String newIfile) {

        paramInfo.setValue(newIfile);

        if (new File(newIfile).exists()) {
            ifileIsValid = true;

            resetWaveLimiter();
            //  propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, WAVE_LIMITER_CHANGE_EVENT, null, null));
            l2prodParamInfo.resetProductInfos(true, waveLimiter);

            updateXmlBasedObjects(newIfile);

            setCustomOfile();
            setCustomGeofile();

            //debug(IFILE.toString() + "being fired");
            fireEvent(IFILE);

        } else {
            ifileIsValid = false;
            //debug(INVALID_IFILE_EVENT.toString() + "being fired");
            fireEvent(INVALID_IFILE_EVENT);
        }


    }


    private String getViirsOfilename(String ifile) {

        StringBuilder ofile = new StringBuilder();

        String yearString = ifile.substring(11, 15);
        String monthString = ifile.substring(15, 17);
        String dayOfMonthString = ifile.substring(17, 19);

        String formattedDateString = getFormattedDateString(yearString, monthString, dayOfMonthString);

        String timeString = ifile.substring(21, 27);
        ofile.append("V");
        ofile.append(formattedDateString);
        ofile.append(timeString);

        ofile.append(".");
        ofile.append("L2_NPP");


        return ofile.toString();
    }


    /**
     * Given standard Gregorian date return day of year (Jan 1=1, Feb 1=32, etc)
     *
     * @param year
     * @param month      1-based Jan=1, etc.
     * @param dayOfMonth
     * @return
     */

    private int getDayOfYear(int year, int month, int dayOfMonth) {
        GregorianCalendar gc = new GregorianCalendar(year, month - 1, dayOfMonth);
        return gc.get(GregorianCalendar.DAY_OF_YEAR);
    }


    private String getFormattedDateString(String yearString, String monthString, String dayOfMonthString) {
        int year = Integer.parseInt(yearString);
        int month = Integer.parseInt(monthString);
        int dayOfMonth = Integer.parseInt(dayOfMonthString);
        return getFormattedDateString(year, month, dayOfMonth);
    }


    private String getFormattedDateString(int year, int month, int dayOfMonth) {

        StringBuilder formattedDateString = new StringBuilder(Integer.toString(year));

        int dayOfYear = getDayOfYear(year, month, dayOfMonth);

        StringBuilder dayOfYearString = new StringBuilder(Integer.toString(dayOfYear));

        while (dayOfYearString.toString().length() < 3) {
            dayOfYearString.insert(0, "0");
        }

        formattedDateString.append(dayOfYearString);

        return formattedDateString.toString();
    }


    private void setCustomOfile() {

        String ifile = getParamValue(IFILE);
        String ofile = ParamInfo.NULL_STRING;
        String VIIRS_IFILE_PREFIX = "SVM01";
        File ifileFile = new File(ifile);


        if (ifileFile.getName().toUpperCase().startsWith(VIIRS_IFILE_PREFIX)) {
            ofile = getViirsOfilename(ifile);
        } else {
            String OFILE_REPLACEMENT_STRING = "L2";
            String IFILE_STRING_TO_BE_REPLACED[] = {"L1A", "L1B"};
            StringBuilder stringBuilder = new StringBuilder();

            /**
             * replace last occurrence of instance of IFILE_STRING_TO_BE_REPLACED[]
             */
            for (String string_to_be_replaced : IFILE_STRING_TO_BE_REPLACED) {
                if (ifile.toUpperCase().contains(string_to_be_replaced)) {
                    int index = ifile.toUpperCase().lastIndexOf(string_to_be_replaced);
                    stringBuilder.append(ifile.substring(0, index));
                    stringBuilder.append(OFILE_REPLACEMENT_STRING);
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
                stringBuilder.append("." + OFILE_REPLACEMENT_STRING);
                ofile = stringBuilder.toString();
            }
        }


        setParamValue(OFILE, ofile);
    }


    private void setCustomGeofile() {

        String ifile = getParamValue(IFILE);
        String geofile = ParamInfo.NULL_STRING;
        String VIIRS_IFILE_PREFIX = "SVM01";
        File ifileFile = new File(ifile);


        if (ifileFile.getName().toUpperCase().startsWith(VIIRS_IFILE_PREFIX)) {
            String VIIRS_GEOFILE_PREFIX = "GMTCO";
            StringBuilder geofileStringBuilder = new StringBuilder();
            geofileStringBuilder.append(ifileFile.getParent());
            geofileStringBuilder.append("/");
            geofileStringBuilder.append(VIIRS_GEOFILE_PREFIX);
            geofileStringBuilder.append(ifileFile.getName().substring(VIIRS_IFILE_PREFIX.length()));

            geofile = geofileStringBuilder.toString();
        } else {
            ArrayList<String> possibleGeoFiles = new ArrayList<String>();

            String STRING_TO_BE_REPLACED[] = {"L1A_LAC", "L1B_LAC"};
            String STRING_TO_INSERT[] = {"geo", "GEO"};

            /**
             * replace last occurrence of instance of STRING_TO_BE_REPLACED[]
             */
            for (String string_to_be_replaced : STRING_TO_BE_REPLACED) {
                if (ifile.toUpperCase().contains(string_to_be_replaced)) {

                    int index = ifile.toUpperCase().lastIndexOf(string_to_be_replaced);
                    String start = ifile.substring(0, index);
                    String end = ifile.substring((index + string_to_be_replaced.length()), ifile.length());

                    for (String string_to_insert : STRING_TO_INSERT) {
                        StringBuilder possibleGeofile = new StringBuilder(start + string_to_insert + end);
                        possibleGeoFiles.add(possibleGeofile.toString());
                    }

                    break;
                }
            }

            for (String string_to_insert : STRING_TO_INSERT) {
                StringBuilder possibleGeofile = new StringBuilder(ifile + "." + string_to_insert);
                possibleGeoFiles.add(possibleGeofile.toString());
            }

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

        InputStream paramInfoStream = L2genForm.class.getResourceAsStream(getParamInfoXml(initialIfile));
        l2genReader.readParamInfoXml(paramInfoStream);

//        InputStream stream = L2genForm.class.getResourceAsStream(getProductInfoXml(initialIfile));
//        l2genReader.readProductsXml(stream);


        InputStream paramCategoryInfoStream = L2genForm.class.getResourceAsStream(PARAM_CATEGORY_INFO_XML);
        l2genReader.readParamCategoryXml(paramCategoryInfoStream);
        setParamCategoryInfos();

//        InputStream productCategoryInfoStream = L2genForm.class.getResourceAsStream(PRODUCT_CATEGORY_INFO_XML);
//        l2genReader.readProductCategoryXml(productCategoryInfoStream);
//        setProductCategoryInfos();

    }


    public void setL2prodParamInfo(L2prodParamInfo l2prodParamInfo) {
        this.l2prodParamInfo = l2prodParamInfo;
    }


    public void addProductInfo(ProductInfo productInfo) {
        l2prodParamInfo.addProductInfo(productInfo);
    }


    public void clearProductInfos() {
        l2prodParamInfo.clearProductInfos();
    }


    public void sortProductInfos(Comparator<ProductInfo> comparator) {
        l2prodParamInfo.sortProductInfos(comparator);
    }

    public void setProdToDefault() {
        if (!l2prodParamInfo.isDefault()) {
            l2prodParamInfo.setToDefault();
            fireEvent(L2PROD);
        }
    }


    /**
     * resets productInfos within productCategoryInfos to link to appropriate entry in productInfos
     */
    public void setProductCategoryInfos() {
        l2prodParamInfo.setProductCategoryInfos();
    }

    public ArrayList<ProductCategoryInfo> getProductCategoryInfos() {
        return l2prodParamInfo.getProductCategoryInfos();
    }

    public void addProductCategoryInfo(ProductCategoryInfo productCategoryInfo) {
        l2prodParamInfo.addProductCategoryInfo(productCategoryInfo);
    }

    public void clearProductCategoryInfos() {
        l2prodParamInfo.clearProductCategoryInfos();
    }

    public L2prodParamInfo createL2prodParamInfo(String value) {
        L2prodParamInfo l2prodParamInfo = new L2prodParamInfo(value);
        setL2prodParamInfo(l2prodParamInfo);

        InputStream productInfoStream = L2genForm.class.getResourceAsStream(PRODUCT_INFO_XML);
        l2genReader.readProductsXml(productInfoStream);

        InputStream productCategoryInfoStream = L2genForm.class.getResourceAsStream(PRODUCT_CATEGORY_INFO_XML);
        l2genReader.readProductCategoryXml(productCategoryInfoStream);
        setProductCategoryInfos();
        
        return l2prodParamInfo;
    }

        public String sortStringList(String stringlist) {
        String[] products = stringlist.split("\\s+");
        ArrayList<String> productArrayList = new ArrayList<String>();
        for (String product : products) {
            productArrayList.add(product);
        }
        Collections.sort(productArrayList);

        return StringUtils.join(productArrayList, " ");
    }
}


