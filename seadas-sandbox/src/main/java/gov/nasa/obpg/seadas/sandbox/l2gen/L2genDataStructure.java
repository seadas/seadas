package gov.nasa.obpg.seadas.sandbox.l2gen;

import org.omg.PortableInterceptor.ServerRequestInterceptor;

import java.security.PrivateKey;
import java.util.HashMap;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class L2genDataStructure {

    private HashMap<String, Object> paramValueHashMap = new HashMap();
    private HashMap<String, Object> defaultParamValueHashMap = new HashMap();

    private String parfile;

    public final String SPIXL_PARAM_KEY = "spixl";
    public final String EPIXL_PARAM_KEY = "epixl";
    public final String DPIXL_PARAM_KEY = "dpixl";
    public final String SLINE_PARAM_KEY = "sline";
    public final String ELINE_PARAM_KEY = "eline";
    public final String DLINE_PARAM_KEY = "dline";
    public final String NORTH_PARAM_KEY = "north";
    public final String SOUTH_PARAM_KEY = "south";
    public final String WEST_PARAM_KEY = "west";
    public final String EAST_PARAM_KEY = "east";
    public final String IFILE_PARAM_KEY = "ifile";
    public final String OFILE_PARAM_KEY = "ofile";

    // Groupings of Parameter Keys
    private final String[] coordinateParamKeys = {NORTH_PARAM_KEY, SOUTH_PARAM_KEY, WEST_PARAM_KEY, EAST_PARAM_KEY};
    private final String[] pixelLineParamKeys = {SPIXL_PARAM_KEY, EPIXL_PARAM_KEY, DPIXL_PARAM_KEY, SLINE_PARAM_KEY, ELINE_PARAM_KEY, DLINE_PARAM_KEY};
    private final String[] fileIOParamKeys = {IFILE_PARAM_KEY, OFILE_PARAM_KEY};
    private final String[] remainingGUIParamKeys = {};


    private void checkCoordinates(String paramKey) {

        boolean coordinatesSelected = false;

        for (String currKey : coordinateParamKeys) {
            if (currKey.equals(paramKey)) {
                coordinatesSelected = true;
            }
        }

        if (coordinatesSelected == true) {
            for (String currKey : pixelLineParamKeys) {
                paramValueHashMap.put(currKey, "");
            }
        } else {
            boolean pixelLinesSelected = false;

            for (String currKey : pixelLineParamKeys) {
                if (currKey.equals(paramKey)) {
                    pixelLinesSelected = true;
                }
            }

            if (pixelLinesSelected == true) {
                for (String currKey : coordinateParamKeys) {
                    paramValueHashMap.put(currKey, "");
                }
            }
        }
    }

    private void makeParfileKeyValueEntry(StringBuilder stringBuilder, String currKey) {

        boolean makeEntry = true;

        if (defaultParamValueHashMap.containsKey(currKey) == true &&
                defaultParamValueHashMap.get(currKey) == paramValueHashMap.get(currKey)) {
            makeEntry = false;
        }

        if (paramValueHashMap.get(currKey).toString().length() == 0) {
            makeEntry = false;
        }

        if (makeEntry == true) {
            stringBuilder.append(currKey);
            stringBuilder.append("=");
            stringBuilder.append(paramValueHashMap.get(currKey));
            stringBuilder.append("\n");
        }

    }


    private void updateParfile() {

        HashMap<String, Object> paramValueCopyHashMap = new HashMap();

        for (String currKey : paramValueHashMap.keySet()) {
            paramValueCopyHashMap.put(currKey, paramValueHashMap.get(currKey));
        }

        StringBuilder parfileStringBuilder = new StringBuilder("");


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
            parfileStringBuilder.append("# COORDINATES\n");
            parfileStringBuilder.append(coordinateBlockStringBuilder.toString());
        }


        StringBuilder fileIOBlockStringBuilder = new StringBuilder("");

        for (String currKey : fileIOParamKeys) {
            makeParfileKeyValueEntry(fileIOBlockStringBuilder, currKey);
            paramValueCopyHashMap.remove(currKey);
        }

        if (coordinateBlockStringBuilder.length() > 0) {
            parfileStringBuilder.append("# FILE IO\n");
            parfileStringBuilder.append(fileIOBlockStringBuilder.toString());
        }


        StringBuilder userBlockStringBuilder = new StringBuilder("");

        for (String currKey : paramValueCopyHashMap.keySet()) {
            makeParfileKeyValueEntry(userBlockStringBuilder, currKey);

        }

        if (userBlockStringBuilder.length() > 0) {
            parfileStringBuilder.append("# USER\n");
            parfileStringBuilder.append(userBlockStringBuilder.toString());
        }


        parfile = parfileStringBuilder.toString();
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
                        somethingChanged = true;
                    }
                }
            }
        }

        if (somethingChanged = true) {
            updateParfile();
        }
    }


    public L2genDataStructure() {

        for (String currKey : coordinateParamKeys) {
            paramValueHashMap.put(currKey, "");
        }

        for (String currKey : pixelLineParamKeys) {
            paramValueHashMap.put(currKey, "");
        }

        for (String currKey : fileIOParamKeys) {
            paramValueHashMap.put(currKey, "");
        }

        updateParfile();
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
        return this.paramValueHashMap.get(paramKey).toString();
    }

    public void setParamValue(String inKey, String inValue) {

        inKey = inKey.trim();
        inValue = inValue.trim();

        if (!paramValueHashMap.containsKey(inKey) ||
                (paramValueHashMap.containsKey(inKey) &&
                        !paramValueHashMap.get(inKey).equals(inValue))) {
            paramValueHashMap.put(inKey, inValue);

            checkCoordinates(inKey);
            updateParfile();
        }
    }

    public String getParfile() {
        return parfile;
    }

    public void setParfile(String inParfile) {

        if (!parfile.equals(inParfile)) {
            parseParfile(inParfile);
        }
    }
}
