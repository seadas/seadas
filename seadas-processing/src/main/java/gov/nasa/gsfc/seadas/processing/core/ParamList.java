package gov.nasa.gsfc.seadas.processing.core;

import com.bc.ceres.core.runtime.RuntimeContext;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: dshea
 * Date: 8/27/12
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParamList {
    private HashMap<String, ParamInfo> paramMap;
    private ArrayList<ParamInfo> paramArray;
    private SwingPropertyChangeSupport propertyChangeSupport;


    public ParamList() {
        paramMap = new HashMap<String, ParamInfo>();
        paramArray = new ArrayList<ParamInfo>();
        propertyChangeSupport = new SwingPropertyChangeSupport(this);
    }

    public void set(ParamList list) {
        paramMap = list.paramMap;
        paramArray = list.paramArray;
        propertyChangeSupport = list.propertyChangeSupport;
    }

    public void addInfo(ParamInfo param) {
        if (param == null) {
            return;
        }
        paramMap.put(param.getName(), param);
        paramArray.add(param);
    }

    public ParamInfo getInfo(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return paramMap.get(name);
    }

    public ParamInfo removeInfo(String name) {
        ParamInfo param = getInfo(name);
        if (param == null) {
            return null;
        }
        paramMap.remove(param.getName());
        paramArray.remove(param);
        return param;
    }

    public String getValue(String name) {
        ParamInfo param = getInfo(name);
        if (param != null) {
            return param.getValue();
        }
        return null;
    }

    public boolean isValueTrue(String name) {
        ParamInfo param = getInfo(name);
        if (param != null) {
            return param.isTrue();
        }
        return false;
    }

    public boolean setValue(String name, String value) {
        ParamInfo param = getInfo(name);
        if (param != null) {
            String oldVal = param.getValue();
            param.setValue(value);
            propertyChangeSupport.firePropertyChange(param.getName(), oldVal, param.getValue());
            return true;
        }
        return false;
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }

    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(name, listener);
    }

    public PropertyChangeListener[] getPropertyChangeListener(String propertyName) {
        return propertyChangeSupport.getPropertyChangeListeners(propertyName);
    }

    public SwingPropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public void appendPropertyChangeSupport(SwingPropertyChangeSupport propertyChangeSupport) {
        PropertyChangeListener[] pr = propertyChangeSupport.getPropertyChangeListeners();
        for (int i = 0; i < pr.length; i++) {
            this.propertyChangeSupport.addPropertyChangeListener(pr[i]);
        }
    }

    public void clearPropertyChangeSupport() {
        //propertyChangeSupport = new SwingPropertyChangeSupport(this);
        PropertyChangeListener[] pr = propertyChangeSupport.getPropertyChangeListeners();
        for (int i = 0; i < pr.length; i++) {
            this.propertyChangeSupport.removePropertyChangeListener(pr[i]);
        }

    }

    // this makes a deep copy of the ParamInfo objects, but a shallow copy of the propertyChangeSupport
    public Object clone() {
        ParamList newList = new ParamList();
        for(ParamInfo param : paramArray) {
            newList.addInfo((ParamInfo)param.clone());
        }
        newList.propertyChangeSupport = propertyChangeSupport;
        return newList;
    }

    public String getParamString(String separator) {
        StringBuffer stringBuffer = new StringBuffer();
        boolean first = true;
        for (ParamInfo param : paramArray) {
            if(param.getType() != ParamInfo.Type.HELP && !param.isDefault()) {
                if (first) {
                    first = false;
                } else {
                    stringBuffer.append(separator);
                }
                String paramString = param.getParamString();
                stringBuffer.append(paramString);
            }
        }
        return stringBuffer.toString();
    }

    public String getParamString() {
        return getParamString(" ");
    }

    protected ArrayList<ParamInfo> makeParamInfoArray(String str) {
        StringBuilder part = new StringBuilder();
        ArrayList<String> parts = new ArrayList<String>();

        // assemble characters into words, use quotation to include spaces in word
        boolean inQuote = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (inQuote) {
                if (c == '"') {
                    inQuote = false;
                } else {
                    part.append(c);
                }
            } else {
                if (c == '"') {
                    inQuote = true;
                } else if (Character.isWhitespace(c)) {
                    if (part.length() > 0) {
                        parts.add(part.toString());
                        part.setLength(0);
                    }
                } else if (c == '=') {
                    if (part.length() > 0) {
                        parts.add(part.toString());
                        part.setLength(0);
                    }
                    parts.add("=");
                } else {
                    part.append(c);
                }
            } // not inQuote
        }
        if(part.length() > 0) {
            parts.add(part.toString());
        }

        // loop through parts creating a map of new ParamInfos
        ArrayList<ParamInfo> params = new ArrayList<ParamInfo>();
        int pairCount = 0;
        for (int i = 1; i < parts.size() - 1; i++) {
            if (parts.get(i).equals("=")) {
                pairCount++;
                if (parts.get(i - 1).equals("=")) {
                    System.out.printf("ParamList.setParamString - Key/Value pair %d missing Key\n", pairCount);
                } else {
                    if (parts.get(i + 1).equals("=") || ((parts.size() > i+2) && parts.get(i + 2).equals("="))) {
                        params.add(new ParamInfo(parts.get(i - 1), ""));
                    } else {
                        params.add(new ParamInfo(parts.get(i - 1), parts.get(i + 1)));
                    }
                }

            }
        }

        return params;
    }

    public void setParamString(String str, boolean retainIFile, boolean setDefaults) {
        ArrayList<ParamInfo> params = makeParamInfoArray(str);

        // loop through params creating a lookup map
        HashMap<String, ParamInfo> map = new HashMap<String, ParamInfo>();
        for(ParamInfo param : params) {
            map.put(param.getName(), param);
        }

        // loop through all parameters setting to default value or new value
        for(ParamInfo param : paramArray) {
            if(retainIFile && (param.getName().equals("ifile") || param.getName().equals("infile") || param.getName().equals("geofile"))) {
                continue;
            }
            ParamInfo newParam = map.get(param.getName());
            if(newParam == null) {
                // not in map so set to default value if needed
                if(setDefaults && !param.isDefault()) {
                    setValue(param.getName(), param.getDefaultValue());
                }
            } else {
                // in map so set to new value
                setValue(param.getName(), newParam.getValue());
            }
        }
    }

    public void setParamString(String str) {
        setParamString(str, false, true);
    }

    public void clear() {
        paramMap.clear();
        paramArray.clear();
    }

    public ArrayList<ParamInfo> getParamArray() {
        return paramArray;
    }

    public int getModifiedParamCount() {
        int count = 0;
        for(ParamInfo param : paramArray) {
            if(param != null && !param.isDefault()) {
                count++;
            }
        }
        return count;
    }

    public boolean isDefault() {
        for(ParamInfo param : paramArray) {
            if(param != null && !param.isDefault()) {
                return false;
            }
        }
        return true;
    }


}
